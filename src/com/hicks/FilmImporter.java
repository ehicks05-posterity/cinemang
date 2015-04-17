package com.hicks;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

public class FilmImporter
{
    private static List<Film> films = new ArrayList<>();

    public static List<Film> getFilms()
    {
        return films;
    }

    public static void importFilmData()
    {
        try
        {
            File ratingsFile = getFtpFile("ratings.list.gz");
            File releaseDatesFile = getFtpFile("release-dates.list.gz");
            List<Film> ratingsFileFilms = parseRatingsFile(ratingsFile);
            List<Film> releaseDatesFileFilms = parseReleaseDatesFile(releaseDatesFile);
            films = mergeFilmLists(ratingsFileFilms, releaseDatesFileFilms);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static List<Film> mergeFilmLists(List<Film> ratingsFileFilms, List<Film> releaseDatesFileFilms)
    {
        Map<String, Film> titleMap = new HashMap<>();
        for (Film film : ratingsFileFilms)
            titleMap.put(film.getTitle(), film);

        for (Film releaseDatesFileFilm : releaseDatesFileFilms)
        {
            if (titleMap.containsKey(releaseDatesFileFilm.getTitle()))
                titleMap.get(releaseDatesFileFilm.getTitle()).setReleaseDate(releaseDatesFileFilm.getReleaseDate());
        }

        List<Film> mergedFilms = new ArrayList<>(titleMap.values());
        for (Iterator<Film> i = mergedFilms.iterator(); i.hasNext();)
        {
            Film film = i.next();
            if (film.getReleaseDate() == null) i.remove();
        }

        return mergedFilms;
    }

    private static List<Film> parseReleaseDatesFile(File file) throws ParseException, IOException
    {
        Map<String, Film> filmMap = new HashMap<>();

        List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("ISO-8859-1"));

        boolean enteredDataTable = false;
        for (String line : lines)
        {
            if (line.equals("=================="))
            {
                enteredDataTable = true;
                continue;
            }

            if (enteredDataTable)
            {
                if (line.equals("--------------------------------------------------------------------------------")) break;

                // FILE FORMAT:
                // Title (year)\tCountryCode:d MMM yyyy
                String[] tokens = line.split("\\t+");
                String title = tokens[0];
                String countryRelease = tokens[1];
                String[] countryReleaseTokens = countryRelease.split(":");
                String country = countryReleaseTokens[0];
                String releaseDate = countryReleaseTokens[1];

                if (!country.equals("USA")) continue;

                Date release = null;
                try
                {
                    release = new SimpleDateFormat("d MMM yyyy").parse(releaseDate);
                }
                catch (ParseException e)
                {
                    System.out.println(e.getMessage());
                }

                if (release != null)
                {
                    Film film = filmMap.get(title);
                    if (film == null)
                        filmMap.put(title, new Film(title, release));
                    else
                    {
                        Film filmFromMap = filmMap.get(title);
                        if (release.compareTo(filmFromMap.getReleaseDate()) < 0)
                            filmFromMap.setReleaseDate(release);
                    }
                }
            }
        }
        return new ArrayList<>(filmMap.values());
    }

    private static List<Film> parseRatingsFile(File file) throws ParseException, IOException
    {
        List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("ISO-8859-1"));

        List<Film> films = new ArrayList<>();
        boolean enteredRatings = false;
        boolean foundHeader = false;
        for (String line : lines)
        {
            if (line.equals("MOVIE RATINGS REPORT"))
            {
                enteredRatings = true;
                continue;
            }
            if (enteredRatings && line.equals("New  Distribution  Votes  Rank  Title"))
            {
                foundHeader = true;
                continue;
            }

            if (enteredRatings && foundHeader)
            {
                if (line.length() == 0) break;

                // 0-5 star
                // 6-15 rating distribution
                String star = line.substring(0, 5).trim();
                String ratingDistribution = line.substring(6, 16);

                String variableWidthPortion = line.substring(16);
                String[] rawTokens = variableWidthPortion.split("  ");
                List<String> tokens = new ArrayList<>();

                for (String token : rawTokens)
                    if (token.length() > 0)
                        tokens.add(token.trim());

                int votes = Integer.parseInt(tokens.get(0));
                BigDecimal rating = new BigDecimal(tokens.get(1));
                String nameWithYear = tokens.get(2);

                String yearWithParens = "";
                Pattern pattern = Pattern.compile("\\(\\d{4}\\)");
                Matcher matcher = pattern.matcher(nameWithYear);
                if (matcher.find())
                {
                    yearWithParens = matcher.group(0);
                    if (yearWithParens.length() != 6) continue;
                }
                else
                    continue;
                int year = Integer.parseInt(yearWithParens.substring(1, 5));
                String name = nameWithYear.replace(yearWithParens, "").trim();

                films.add(new Film(nameWithYear, year, rating, votes));
            }
        }
        return films;
    }

    private static File getFtpFile(String remoteFilename)
    {
        String host = "ftp.fu-berlin.de";
        String dir = "pub/misc/movies/database/";
        String login = "anonymous";

        File zippedFile = downloadFile(host, dir, login, remoteFilename);
        return unzipFile(zippedFile);
    }

    private static File downloadFile(String host, String dir, String login, String remoteFilename)
    {
        File zippedFile = new File(System.getProperty("java.io.tmpdir") + File.separator + remoteFilename);
        if (zippedFile.exists()) return zippedFile;

        FTPClient ftp = new FTPClient();
        FTPClientConfig config = new FTPClientConfig();
        ftp.configure(config);
        try
        {
            int reply;
            ftp.connect(host);
            System.out.println("Connected to " + host + ".");
            System.out.print(ftp.getReplyString());

            // After connection attempt, you should check the reply code to verify success.
            reply = ftp.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftp.disconnect();
                System.err.println("FTP server refused connection.");
                System.exit(1);
            }

            ftp.login(login, "");
            // transfer files
            ftp.enterLocalPassiveMode();
            ftp.changeWorkingDirectory(dir);

            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);

            InputStream inputStream = ftp.retrieveFileStream(remoteFilename);
            FileOutputStream fileOutputStream = new FileOutputStream(zippedFile);
            System.out.println("Downloading " + host + "/" + dir + remoteFilename);
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.flush();
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(inputStream);

            ftp.logout();
            ftp.disconnect();

            return zippedFile;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    private static File unzipFile(File input)
    {
        File unzippedFile = new File(System.getProperty("java.io.tmpdir") + File.separator + input.getName().replace(".gz", ""));
        if (unzippedFile.exists()) return unzippedFile;

        try
        {
            GZIPInputStream gzipInputStream = new GZIPInputStream(new FileInputStream(input));
            ByteArrayOutputStream unzippedData = new ByteArrayOutputStream();

            int bytesRead;
            byte[] buffer = new byte[1024];

            while ((bytesRead = gzipInputStream.read(buffer)) > 0)
            {
                unzippedData.write(buffer, 0, bytesRead);
            }

            FileOutputStream unzippedOutputStream = new FileOutputStream(unzippedFile);
            unzippedOutputStream.write(unzippedData.toByteArray());
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        return unzippedFile;
    }
}
