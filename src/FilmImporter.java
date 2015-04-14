import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class FilmImporter
{
    public static void main(String[] args) throws Exception
    {
        File ratingsFile = new File("ratings.list.gz");
        File ratingsFileUnzipped = new File("ratings.list");

        if (!ratingsFile.exists()) getRatingsFile();
        if (!ratingsFileUnzipped.exists()) getUnzippedRatingsFile(ratingsFile, ratingsFileUnzipped);

        List<Film> films = new ArrayList<>();

        boolean enteredRatings = false;
        boolean foundHeader = false;
        List<String> lines = Files.readAllLines(ratingsFileUnzipped.toPath(), Charset.forName("ISO-8859-1"));
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
                if (line.substring(0,6).contains("*")) System.out.println(line);
                // 0-5 star
                String star = line.substring(0, 5).trim();
                // 6-15 rating distribution
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

                if (votes > 10_000) films.add(new Film(name, rating, votes));
            }
        }

        for (Film film : films) System.out.println(film);
        System.out.println("# of films: " + films.size());
    }

    private static void getUnzippedRatingsFile(File ratingsFile, File ratingsFileUnzipped) throws IOException
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

    private static void getRatingsFile()
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

            FTPFile[] ftpFiles = ftp.listFiles();
            for (FTPFile ftpFile : ftpFiles)
                if (ftpFile.getName().equals("ratings.list.gz"))
                {
                    InputStream inputStream = ftp.retrieveFileStream(ftpFile.getName());
                    FileOutputStream fileOutputStream = new FileOutputStream(ftpFile.getName());
                    //Using org.apache.commons.io.IOUtils
                    IOUtils.copy(inputStream, fileOutputStream);
                    fileOutputStream.flush();
                    IOUtils.closeQuietly(fileOutputStream);
                    IOUtils.closeQuietly(inputStream);
                }


            ftp.logout();
            ftp.disconnect();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
