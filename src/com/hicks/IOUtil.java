package com.hicks;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class IOUtil
{
    static FTPClient prepareFtpClient(String host, String login, String dir) throws IOException
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

            return ftp;
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
}
