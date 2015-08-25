package com.hicks;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

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

    public static byte[] getBytesFromUrlConnection(URL url) throws IOException
    {
        URLConnection uc = url.openConnection();
        int len = uc.getContentLength();
        try (InputStream is = new BufferedInputStream(uc.getInputStream()))
        {
            byte[] data = new byte[len];
            int offset = 0;
            while (offset < len)
            {
                int read = is.read(data, offset, data.length - offset);
                if (read < 0)
                {
                    break;
                }
                offset += read;
            }
            if (offset < len)
            {
                throw new IOException(String.format("Read %d bytes; expected %d", offset, len));
            }
            return data;
        }
    }
}
