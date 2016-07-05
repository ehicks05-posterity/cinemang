package com.hicks;

import java.util.ArrayList;
import java.util.List;

public class CachePreloader
{
    public static void preload()
    {
        int i = 0;
        int limit = 1000;
        List result = EOI.executeQueryWithPSOneResult("select count(*) from films", new ArrayList<>());
        long resultSize = (Long) result.get(0);

        while (SystemInfo.getFreeRamMb() > 100 && i*limit < resultSize)
        {
            int offset = i * limit;
            EOI.executeQuery("select * from films order by cinemang_rating desc, imdb_id nulls last limit 1000 offset " + offset);
            System.out.println("Loaded into cache films " + offset + " to " + (offset + limit) + ". free ram:" + SystemInfo.getFreeRamMb() / 1024 / 1024);
            System.out.println("--> cache now holds: " + EOICache.cache.size());
            i++;
        }
    }
}
