package com.hicks;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;

public class DebugHandler
{
    public static void getDebugInfo(HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException
    {
        String info = "['" + EOICache.hits.toString() + "','" + EOICache.misses.toString() + "']";

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.print(info);
    }
}
