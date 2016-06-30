package com.hicks;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Common
{
    private static SimpleDateFormat mmddyyyy = new SimpleDateFormat("MM/dd/yyyy");
    private static SimpleDateFormat yyyymmdd = new SimpleDateFormat("yyyy-MM-dd");

    public static java.sql.Date utilDatetoSQLDate(Date utilDate)
    {
        java.util.Calendar cal = Calendar.getInstance();
        cal.setTime(utilDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new java.sql.Date(cal.getTime().getTime()); // your sql date
    }

    public static Date stringToDate(String input)
    {
        Date result = null;
        if (input == null || input.length() == 0) return null;
        try
        {
            result = yyyymmdd.parse(input);
        }
        catch (ParseException e)
        {
//            System.out.println(e.getMessage());
        }
        if (result == null)
        {
            try
            {
                result = mmddyyyy.parse(input);
            }
            catch (ParseException e)
            {
//                System.out.println(e.getMessage());
            }
        }
        return result;
    }

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

    public static String getSafeString(String input)
    {
        if (input == null) return "";
        return input;
    }
}
