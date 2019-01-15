package net.ehicks.cinemang;

public class SystemInfo
{
    private static boolean loadDbToRam;

    public static String getZipPath()
    {
        return "g:/omdb0616.zip";
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
