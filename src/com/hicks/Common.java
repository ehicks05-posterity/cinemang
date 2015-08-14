package com.hicks;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Common
{
    public static int stringToInt(String input)
    {
        int result = 0;
        if (input == null || input.length() == 0) return result;
        input = input.replaceAll(",","");
        try
        {
            result = Integer.parseInt(input);
        }
        catch (Exception e)
        {
//            System.out.println(e.getMessage());
        }
        return result;
    }

    public static BigDecimal stringToBigDecimal(String input)
    {
        BigDecimal result = BigDecimal.ZERO;
        if (input == null || input.length() == 0) return result;
        try
        {
            result = new BigDecimal(input);
        }
        catch (Exception e)
        {
//            System.out.println(e.getMessage());
        }
        return result;
    }

    public static BigDecimal integerToBigDecimal(Integer input)
    {
        BigDecimal result = BigDecimal.ZERO;
        if (input == null) return result;
        try
        {
            result = new BigDecimal(input);
        }
        catch (Exception e)
        {
//            System.out.println(e.getMessage());
        }
        return result;
    }

    public static Date stringToDate(String input)
    {
        Date result = null;
        if (input == null || input.length() == 0) return result;
        try
        {
            result = new SimpleDateFormat("yyyy-MM-dd").parse(input);
        }
        catch (Exception e)
        {
//            System.out.println(e.getMessage());
        }
        return result;
    }
}
