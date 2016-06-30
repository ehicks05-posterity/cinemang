package com.hicks;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DBMapField
{
    public static final String STRING = "STRING";           // varchar2
    public static final String INTEGER = "INTEGER";         // integer
    public static final String LONG = "LONG";         // bigint
    public static final String DECIMAL = "DECIMAL";         // decimal
    public static final String TIMESTAMP = "TIMESTAMP";     // timestamp

    public DBMap dbMap;
    public String className = "";
    public String fieldName = "";
    public String columnName = "";
    public String type = "";
    public int length;
    public int precision;
    public int scale;
    public boolean nullable;
    public boolean primaryKey;
    public String declaredColumnDefinition = "";

    public String getColumnDefinition()
    {
        if (declaredColumnDefinition.length() > 0)
            return declaredColumnDefinition;

        String columnDef = "";
        if (type.equals(DBMapField.STRING))
        {
            if (length == 0)
                length = 255;
            columnDef += "varchar2(" + length + ")";
        }
        if (type.equals(DBMapField.INTEGER))
            columnDef += "integer";
        if (type.equals(DBMapField.LONG))
            columnDef += "bigint";
        if (type.equals(DBMapField.DECIMAL))
        {
            if (precision == 0)
                precision = 24;
            if (scale == 0)
                scale = 2;
            columnDef += "decimal(" + precision + "," + scale + ")";
        }
        if (type.equals(DBMapField.TIMESTAMP))
            columnDef += "timestamp";

        if (primaryKey)
            columnDef += " PRIMARY KEY";
        if (!nullable)
            columnDef += " NOT NULL";

        return columnDef;
    }

    public Method getGetter()
    {
        try
        {
            return dbMap.clazz.getDeclaredMethod("get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1, fieldName.length()));
        }
        catch (NoSuchMethodException e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Object getValue(Object object)
    {
        Method getter = getGetter();
        try
        {
            Object value = getter.invoke(object);
            if (type.equals(DBMapField.STRING))
                return value;
            if (type.equals(DBMapField.INTEGER))
                return value;
            if (type.equals(DBMapField.LONG))
                return value;
            if (type.equals(DBMapField.DECIMAL))
                return value;
            if (type.equals(DBMapField.TIMESTAMP))
                return value;
        }
        catch (InvocationTargetException | IllegalAccessException e)
        {
            System.out.println(e.getMessage());
        }

        return null;
    }
}
