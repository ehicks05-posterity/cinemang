package com.hicks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class EOICache
{
    public static ConcurrentMap<String, Object> cache = new ConcurrentHashMap<>();

    public static AtomicInteger hits = new AtomicInteger();
    public static AtomicInteger misses = new AtomicInteger();

    public static Object get(String key)
    {
        Object object = cache.get(key);

        if (object != null)
            hits.incrementAndGet();
        else
            misses.incrementAndGet();

        return object;
    }

    public static void set(Object object)
    {
        cache.put(object.toString(), object);
    }
}
