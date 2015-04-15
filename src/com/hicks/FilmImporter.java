package com.hicks;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class FilmImporter
{
    private static List<Film> films = new ArrayList<>();

    public static List<Film> getFilms()
    {
        return films;
    }

    public static void performImport()
    {
        String temp = System.getProperty("java.io.tmpdir");
        File ratingsFile = new File(temp + File.separator + "ratings.list.gz");
        File ratingsFileUnzipped = new File(temp + File.separator + "ratings.list");

        if (!ratingsFile.exists()) getRatingsFile(ratingsFile);
        if (!ratingsFileUnzipped.exists()) getUnzippedRatingsFile(ratingsFile, ratingsFileUnzipped);

        films = getFilmsFromFile(ratingsFileUnzipped);
    }

    private static List<Film> getFilmsFromFile(File ratingsFileUnzipped)
    {
        List<String> lines = new ArrayList<>();

        try
        {
            lines = Files.readAllLines(ratingsFileUnzipped.toPath(), Charset.forName("ISO-8859-1"));
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        List<Film> films = new ArrayList<>();
        boolean enteredRatings = false;
        boolean foundHeader = false;
        for (String line : lines)
        {
            if (line.equals("MOVIE RATINGS REPORT")) enteredRatings = true;
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
                String name = tokens.get(2);

//                if (votes > 10_000)
                    films.add(new Film(name, rating, votes));
            }
        }
        return films;
    }

    private static void getUnzippedRatingsFile(File ratingsFile, File ratingsFileUnzipped)
    {
        try
        {
            FileInputStream fileInputStream = new FileInputStream(ratingsFile);
            GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);

            ByteArrayOutputStream unzippedData = new ByteArrayOutputStream();

            int bytesRead;
            byte[] buffer = new byte[1024];

            while ((bytesRead = gzipInputStream.read(buffer)) > 0)
            {
                unzippedData.write(buffer, 0, bytesRead);
            }

            FileOutputStream unzippedOutputStream = new FileOutputStream(ratingsFileUnzipped);
            unzippedOutputStream.write(unzippedData.toByteArray());
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static void getRatingsFile(File ratingsFile)
    {
        String host = "ftp.fu-berlin.de";

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

            ftp.login("anonymous", "");
            // transfer files
            ftp.enterLocalPassiveMode();
            ftp.changeWorkingDirectory("pub/misc/movies/database/");
            System.out.println("Current directory is " + ftp.printWorkingDirectory());

            ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftp.setFileTransferMode(FTP.COMPRESSED_TRANSFER_MODE);

            InputStream inputStream = ftp.retrieveFileStream("ratings.list.gz");
            FileOutputStream fileOutputStream = new FileOutputStream(ratingsFile);
            //Using org.apache.commons.io.IOUtils
            IOUtils.copy(inputStream, fileOutputStream);
            fileOutputStream.flush();
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(inputStream);

            ftp.logout();
            ftp.disconnect();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
