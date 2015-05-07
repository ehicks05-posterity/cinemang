package com.hicks;

import org.apache.commons.io.IOUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class IOUtil
{
    static List<Film> streamFtpFile(String host, String dir, String login, String remoteFilename) throws IOException
    {
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
            ZipInputStream zipIn = new ZipInputStream(inputStream);

            List<Film> films = new ArrayList<>();
            for (ZipEntry e; (e = zipIn.getNextEntry()) != null;)
            {
                System.out.println("reading zipEntry..." + e.getName() + " " + (e.getSize() / (1024 * 1024)) + "MB");
                BufferedReader br = new BufferedReader(new InputStreamReader(zipIn));
                String line;
                while ((line = br.readLine()) != null)
                {
                    OmdbLoader.parseLine(films, line);
                }
            }

            ftp.logout();
            ftp.disconnect();

            return films;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    static File downloadFile(String host, String dir, String login, String remoteFilename) throws IOException
    {
        File zippedFile = new File(System.getProperty("java.io.tmpdir") + File.separator + remoteFilename);
        if (zippedFile.exists())
        {
            System.out.println("Found " + zippedFile.getCanonicalPath());
            return zippedFile;
        }

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

    static File unzipFile(File input) throws IOException
    {
        File unzippedFile = new File(System.getProperty("java.io.tmpdir") + File.separator + input.getName().replace(".gz", ""));
        if (unzippedFile.exists())
        {
            System.out.println("Found " + unzippedFile.getCanonicalPath());
            return unzippedFile;
        }
        System.out.println("Unzipping " + input.getCanonicalPath());
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

    static void writeFilmDataToDisk(List<Film> films, File file)
    {
        try
        {
            System.out.println("Writing film data to dump file " + file.getCanonicalPath());
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(films);
            oos.flush();
        }
        catch (Exception e)
        {
            System.out.println("Problem serializing: " + e);
        }
    }

    static List<Film> readFilmDataFromDisk(File file)
    {
        List<Film> films = null;

        try
        {
            System.out.println("Reading film data from dump file " + file.getCanonicalPath());
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            films = (List<Film>) (ois.readObject());
        }
        catch (Exception e)
        {
            System.out.println("Problem serializing: " + e);
        }

        return films;
    }
}
