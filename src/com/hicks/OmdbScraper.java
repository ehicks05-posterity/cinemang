package com.hicks;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OmdbScraper
{
    public static void main(String[] args) throws IOException
    {
        final int THREADS = 16;

        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < THREADS; i++)
        {
            Runnable worker = new ScraperWorker(THREADS, i);
            executor.execute(worker);
        }
        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated())
        {

        }
        System.out.println("\nFinished all threads");
    }

    public static class ScraperWorker implements Runnable
    {
        private final int threads;
        private final int threadIndex;

        ScraperWorker(int threads, int threadIndex)
        {
            this.threads = threads;
            this.threadIndex = threadIndex;
        }

        @Override
        public void run()
        {
            String imdbId;
            String params;
            URL url;
            URLConnection urlConnection;
            BufferedReader in;
            String inputLine;
            List<String> lines;
            DecimalFormat df = new DecimalFormat("0000000");
            File dataFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "omdbData" + threadIndex + ".txt");


            try
            {
                List<String> imdbIds = getIdsFromFile(dataFile);

                File totalsFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "omdbTotals" + threadIndex + ".txt");
                PrintWriter totalsWriter = new PrintWriter(new FileOutputStream(totalsFile, true));
                totalsWriter.println(imdbIds.size());
                totalsWriter.flush();
                totalsWriter.close();

                PrintWriter printWriter = new PrintWriter(new FileOutputStream(dataFile, true));

                int recordsProcessed = 0;
                for (int i = threadIndex; i < 3_000_000; i += threads)
                {
                    if (recordsProcessed % 100 == 0)
                        System.out.println("thread " + threadIndex + " has processed " + recordsProcessed + " records");
                    recordsProcessed++;

                    imdbId = "tt" + df.format(i);
                    if (imdbIds.contains(imdbId)) continue;
                    Thread.sleep(10);

                    params = "?i=" + imdbId + "&tomatoes=true";
                    url = new URL("http://www.omdbapi.com/" + params);
                    urlConnection = url.openConnection();
                    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                    lines = new ArrayList<>();
                    while ((inputLine = in.readLine()) != null)
                    {
                        lines.add(inputLine);
                    }

                    in.close();

                    // parse line
//                    for (String line : lines)
//                        printWriter.println(line);
                }

                printWriter.flush();
                printWriter.close();
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
    }

    private static List<String> getIdsFromFile(File file) throws IOException
    {
        List<String> imdbIds = new ArrayList<>();

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null)
        {
            int indexOfTt = line.indexOf(":\"tt");
            if (indexOfTt > 0)
                imdbIds.add(line.substring(indexOfTt + 2, indexOfTt + 11));
        }

        return imdbIds;
    }

    private static void printRecordCount() throws IOException
    {
        int grandTotal = 0;
        for (int i = 0; i < 16; i++)
        {
            File dataFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "omdbTotals" + i + ".txt");
            BufferedReader reader = new BufferedReader(new FileReader(dataFile));

            String line = reader.readLine();
            int total = Integer.parseInt(line);
            System.out.println("thread" + i + ": " + total);
            grandTotal += total;
        }
        System.out.println(grandTotal);
    }
}
