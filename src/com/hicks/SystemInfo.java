package com.hicks;

import javax.servlet.ServletContext;
import java.util.Properties;

public class SystemInfo
{
    private static Properties properties;
    private static ServletContext servletContext;
    private static boolean loadDbToRam;

    public static Properties getProperties()
    {
        return properties;
    }

    public static void setProperties(Properties properties)
    {
        SystemInfo.properties = properties;
    }

    public static ServletContext getServletContext()
    {
        return servletContext;
    }

    public static void setServletContext(ServletContext servletContext)
    {
        SystemInfo.servletContext = servletContext;
    }

    public static boolean isLoadDbToRam()
    {
        return loadDbToRam;
    }

    public static void setLoadDbToRam(boolean loadDbToRam)
    {
        SystemInfo.loadDbToRam = loadDbToRam;
    }
}
