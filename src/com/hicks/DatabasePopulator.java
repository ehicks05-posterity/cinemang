package com.hicks;

import com.hicks.beans.Film;
import org.apache.commons.net.ftp.FTPClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DatabasePopulator
{
    private static final boolean LOAD_SUB_1000_VOTE_MOVIES = true;
    private static int unreadableRows = 0;

    public static List<Film> populateDatabase() throws IOException
    {
        long start = System.currentTimeMillis();
        List<Film> films = loadFromDumpToRam();
        System.out.println("Parsed data files in " + (System.currentTimeMillis() - start) + "ms");

        DecimalFormat df = new DecimalFormat("#,###");
        System.out.println("Films Found: " + df.format(films.size()));
        System.out.println("Unreadable Rows: " + df.format(unreadableRows));

        start = System.currentTimeMillis();
//        saveFilmsWithHibernate(films);
        saveFilmsWithEOI(films);
        System.out.println("Films persisted in " + (System.currentTimeMillis() - start) + "ms");

        return films;
    }

    private static void saveFilmsWithEOI(List<Film> films)
    {
        EOI.insert(films);
    }

    private static List<Film> loadFromDumpToRam() throws IOException
    {
        FTPClient ftp = IOUtil.prepareFtpClient("***REMOVED***", "***REMOVED***", "");

        String omdbFilename = SystemInfo.getProperties().getProperty("omdbZipFileName");

        InputStream inputStream = ftp.retrieveFileStream(omdbFilename);
        ZipInputStream zipIn = new ZipInputStream(inputStream);

        List<Film> films = readZipInputStream(zipIn);
        ftp.logout();
        ftp.disconnect();
        return films;
    }

    private static List<Film> readZipInputStream(ZipInputStream zipIn) throws IOException
    {
        Map<String, Film> filmMap = new HashMap<>();

        for (ZipEntry e; (e = zipIn.getNextEntry()) != null;)
        {
            String zipEntryName = e.getName();
            String entryMBs = " (" + (e.getSize() / (1024 * 1024)) + "MB)";
            System.out.println("reading " + zipEntryName + entryMBs);
            BufferedReader br = new BufferedReader(new InputStreamReader(zipIn));
            br.readLine(); // skip the header
            String line;
            int index = 0;
            while ((line = br.readLine()) != null)
            {
                index++;
                if (index % 100000 == 0)
                    System.out.println("read " + index + " rows...");

                Film newFilm = null;
                if (zipEntryName.equals("omdbMovies.txt"))
                    newFilm = readMovieLine(line);
                if (zipEntryName.equals("tomatoes.txt"))
                    newFilm = readRottenLine(line);
                if (!Arrays.asList("omdbMovies.txt", "tomatoes.txt").contains(zipEntryName))
                    break;

                if (newFilm != null)
                {
                    Film existing = filmMap.get(newFilm.getImdbID());
                    if (existing == null && zipEntryName.equals("omdbMovies.txt"))
                    {
                        if (LOAD_SUB_1000_VOTE_MOVIES || newFilm.getImdbVotes() >= 1000)
                            filmMap.put(newFilm.getImdbID(), newFilm);
                    }
                    if (existing != null && zipEntryName.equals("tomatoes.txt"))
                    {
                        Film mergedFilm = mergeRottenDataIntoFilmData(existing, newFilm);
                        mergedFilm.setCinemangRating(mergedFilm.calculateCinemangRating());

                        if (LOAD_SUB_1000_VOTE_MOVIES || mergedFilm.getImdbVotes() >= 1000)
                            filmMap.put(newFilm.getImdbID(), mergedFilm);
                    }
                }
            }
        }

        return new ArrayList<>(filmMap.values());
    }

    private static Film mergeRottenDataIntoFilmData(Film f1, Film f2)
    {
        Film movieData = f1.getLastUpdated().length() > 0 ? f1 : f2;
        Film rottenData = f1.getLastUpdated().length() > 0 ? f2 : f1;

        movieData.setTomatoImage(rottenData.getTomatoImage());
        movieData.setTomatoRating(rottenData.getTomatoRating());
        movieData.setTomatoMeter(rottenData.getTomatoMeter());
        movieData.setTomatoReviews(rottenData.getTomatoReviews());
        movieData.setTomatoFresh(rottenData.getTomatoFresh());
        movieData.setTomatoRotten(rottenData.getTomatoRotten());
        movieData.setTomatoConsensus(rottenData.getTomatoConsensus());
        movieData.setTomatoUserMeter(rottenData.getTomatoUserMeter());
        movieData.setTomatoUserRating(rottenData.getTomatoUserRating());
        movieData.setTomatoUserReviews(rottenData.getTomatoUserReviews());
        movieData.setDvd(rottenData.getDvd());
        movieData.setBoxOffice(rottenData.getBoxOffice());
        movieData.setProduction(rottenData.getProduction());
        movieData.setWebsite(rottenData.getWebsite());
        movieData.setRottenDataLastUpdated(rottenData.getRottenDataLastUpdated());

        return movieData;
    }

    private static Film readMovieLine(String line)
    {
        try
        {
            String[] tokens = line.split("\t");
            Film film = new Film();

            int i = 1;
            film.setImdbID(tokens[i++]);
            film.setTitle(tokens[i++]);
            film.setYear(tokens[i++]);
            film.setRated(tokens[i++]);
            film.setRuntime(tokens[i++]);
            film.setGenre(tokens[i++]);
            film.setReleased(Common.stringToDate(tokens[i++]));
            film.setDirector(tokens[i++]);
            film.setWriter(tokens[i++]);
            film.setActors(tokens[i++]);
            film.setMetascore(Common.stringToInt(tokens[i++]));
            film.setImdbRating(Common.stringToBigDecimal(tokens[i++]));
            film.setImdbVotes(Common.stringToInt(tokens[i++]));
            film.setPoster(tokens[i++]);
            film.setPlot(tokens[i++]);
            film.setFullPlot(tokens[i++]);
            film.setLanguage(tokens[i++]);
            film.setCountry(tokens[i++]);
            film.setAwards(tokens[i++]);
            film.setLastUpdated(tokens[i++]);

            String[] languages = film.getLanguage().split(",");
            if (languages.length > 1)
                film.setLanguage(languages[0]);

            return film;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            unreadableRows++;
        }

        return null;
    }

    private static Film readRottenLine(String line)
    {
        try
        {
            String[] tokens = line.split("\t");
            Film film = new Film();

            int i = 0;
            film.setImdbID(Film.convertIdToImdbId(tokens[i++]));
            film.setTomatoImage(tokens[i++]);
            film.setTomatoRating(Common.stringToBigDecimal(tokens[i++]));
            film.setTomatoMeter(Common.stringToInt(tokens[i++]));
            film.setTomatoReviews(Common.stringToInt(tokens[i++]));
            film.setTomatoFresh(Common.stringToInt(tokens[i++]));
            film.setTomatoRotten(Common.stringToInt(tokens[i++]));
            film.setTomatoConsensus(tokens[i++]);
            film.setTomatoUserMeter(Common.stringToInt(tokens[i++]));
            film.setTomatoUserRating(Common.stringToBigDecimal(tokens[i++]));
            film.setTomatoUserReviews(Common.stringToInt(tokens[i++]));
            film.setDvd(Common.stringToDate(tokens[i++]));
            film.setBoxOffice(tokens[i++]);
            film.setProduction(tokens[i++]);
            film.setWebsite(tokens[i++]);
            film.setRottenDataLastUpdated(tokens[i++]);

            return film;
        }
        catch (Exception e)
        {
            unreadableRows++;
        }

        return null;
    }
}
