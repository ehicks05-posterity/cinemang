package com.hicks;

import org.h2.jdbcx.JdbcConnectionPool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EOI
{
//    private static Connection getConnection()
//    {
//        try
//        {
//            Class.forName("org.h2.Driver");
//            return DriverManager.getConnection("jdbc:h2:~/test", "", "");
//        }
//        catch (Exception e)
//        {
//            System.out.println(e.getMessage());
//        }
//
//        return null;
//    }

    private static JdbcConnectionPool cp;

    public static void init()
    {
        cp = JdbcConnectionPool.create("jdbc:h2:~/test;TRACE_LEVEL_FILE=1", "", "");
    }

    public static void destroy()
    {
        executeUpdate("shutdown compact");
        cp.dispose();
    }

    private static Connection getConnection()
    {
        try
        {
            return cp.getConnection();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static void executeUpdate(String queryString)
    {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();)
        {
            int rows = statement.executeUpdate(queryString);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void insert(Object object)
    {
        if (object instanceof List)
            _insertFromList((List) object);
        else
            _insert(object);
    }

    private static void _insertFromList(List<?> objects)
    {
        int success = 0;
        int fail = 0;
        for (Object object : objects)
        {
            int result = _insert(object);
            if (result == 1)
                success++;
            else
                fail++;
        }
        System.out.println("Finished mass create: " + success + " succeeded, " + fail + " failed");
    }

    private static int _insert(Object object)
    {
        String insertStatement = SQLGenerator.getInsertStatement(object);

//        try (PreparedStatement preparedStatement = getConnection().prepareStatement(insertStatement);)
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);)
        {
            DBMap dbMap = DBMap.getDBMapByClass(object.getClass());
            for (DBMapField dbMapField : dbMap.fields)
            {
                int index = dbMap.fields.indexOf(dbMapField) + 1;
                Object value = dbMapField.getValue(object);

                if (value == null && dbMapField.type.equals(DBMapField.TIMESTAMP))
                {
                    preparedStatement.setNull(index, Types.TIMESTAMP);
                    continue;
                }
                if (value == null && dbMapField.type.equals(DBMapField.INTEGER))
                {
                    preparedStatement.setNull(index, Types.TIMESTAMP);
                    continue;
                }
                if (value == null && dbMapField.type.equals(DBMapField.DECIMAL))
                {
                    preparedStatement.setNull(index, Types.DECIMAL);
                    continue;
                }

                if (dbMapField.type.equals(DBMapField.STRING))
                    preparedStatement.setString(index, (String) value);
                if (dbMapField.type.equals(DBMapField.INTEGER))
                    preparedStatement.setInt(index, (Integer) value);
                if (dbMapField.type.equals(DBMapField.LONG))
                    preparedStatement.setLong(index, (Long) value);
                if (dbMapField.type.equals(DBMapField.DECIMAL))
                    preparedStatement.setBigDecimal(index, (BigDecimal) value);
                if (dbMapField.type.equals(DBMapField.TIMESTAMP))
                    preparedStatement.setDate(index, Common.utilDatetoSQLDate((Date) value));
            }
            return preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return 0;
    }



    public static <T> List<T> executeQueryWithPS(String queryString, List<Object> args)
    {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryString))
        {
            int argIndex = 1;
            for (Object arg : args)
            {
                if (arg instanceof String) preparedStatement.setString(argIndex, (String) arg);
                if (arg instanceof Integer) preparedStatement.setInt(argIndex, (Integer) arg);
                if (arg instanceof Long) preparedStatement.setLong(argIndex, (Long) arg);
                if (arg instanceof BigDecimal) preparedStatement.setBigDecimal(argIndex, (BigDecimal) arg);
                if (arg instanceof Date) preparedStatement.setDate(argIndex, Common.utilDatetoSQLDate((Date) arg));
                argIndex++;
            }

            ResultSet resultSet = preparedStatement.executeQuery();
            return parseResultSet(queryString, resultSet);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return null;
    }
    public static <T> List<T> executeQuery(String queryString)
    {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(queryString);)
        {
            return parseResultSet(queryString, resultSet);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static <T> List<T> parseResultSet(String queryString, ResultSet resultSet) throws Exception
    {
        List<T> results = new ArrayList<>();

        SQLQuery sqlQuery = SQLQuery.parseSQL(queryString);
        DBMap dbMap = sqlQuery.dbMap;

        if (dbMap == null)
        {
            int i = 1;
            while (resultSet.next())
            {
                Object object = resultSet.getObject(i);
                results.add((T) object);
                i++;
            }
            return results;
        }

        if (queryString.startsWith("select *"))
        {
//            long start = System.currentTimeMillis();
            while (resultSet.next())
            {
                Object object = getEOIObjectFromResultSet(resultSet, dbMap);
                results.add((T) object);
            }
//            System.out.println("loaded " + results.size() + " objects in " + (System.currentTimeMillis() - start) + "ms");
        }
        else
        {
            List<ProjectionColumn> projectionColumns = ProjectionColumn.getProjectionColumns(queryString, dbMap);
            while (resultSet.next())
            {
                List<Object> list = new ArrayList<>();
                for (ProjectionColumn projectionColumn : projectionColumns)
                {
                    if (projectionColumn.type.equals("STRING"))
                        list.add(resultSet.getString(projectionColumn.columnLabel));
                    if (projectionColumn.type.equals("INTEGER"))
                        list.add(resultSet.getInt(projectionColumn.columnLabel));
                    if (projectionColumn.type.equals("LONG"))
                        list.add(resultSet.getLong(projectionColumn.columnLabel));
                    if (projectionColumn.type.equals("DECIMAL"))
                        list.add(resultSet.getBigDecimal(projectionColumn.columnLabel));
                    if (projectionColumn.type.equals("TIMESTAMP"))
                        list.add(resultSet.getTimestamp(projectionColumn.columnLabel));
                }
                results.add((T) list);
            }
        }
        return results;
    }

    private static Object getEOIObjectFromResultSet(ResultSet resultSet, DBMap dbMap) throws Exception
    {
        Class clazz = dbMap.clazz;
        Object object = dbMap.constructor.newInstance();

        for (DBMapField field : dbMap.getPKFields())
            invokeSetter(resultSet, clazz, object, field);

        // we've filled the PK, can we find it in cache?
        Object fromCache = EOICache.get(object.toString());
        if (fromCache != null)
            return fromCache;

        for (DBMapField field : dbMap.getNonPKFields())
            invokeSetter(resultSet, clazz, object, field);

        EOICache.set(object);

        return object;
    }

    private static void invokeSetter(ResultSet resultSet, Class clazz, Object object, DBMapField field) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, SQLException
    {
        String methodName = "set" + field.fieldName.substring(0, 1).toUpperCase() + field.fieldName.substring(1, field.fieldName.length());

        if (field.type.equals("STRING"))
        {
            Method method = clazz.getDeclaredMethod(methodName, String.class);
            method.invoke(object, resultSet.getString(field.columnName));
        }
        if (field.type.equals("INTEGER"))
        {
            Method method = clazz.getDeclaredMethod(methodName, Integer.class);
            method.invoke(object, resultSet.getInt(field.columnName));
        }
        if (field.type.equals("LONG"))
        {
            Method method = clazz.getDeclaredMethod(methodName, Long.class);
            method.invoke(object, resultSet.getLong(field.columnName));
        }
        if (field.type.equals("DECIMAL"))
        {
            Method method = clazz.getDeclaredMethod(methodName, BigDecimal.class);
            method.invoke(object, resultSet.getBigDecimal(field.columnName));
        }
        if (field.type.equals("TIMESTAMP"))
        {
            Method method = clazz.getDeclaredMethod(methodName, Date.class);
            method.invoke(object, resultSet.getTimestamp(field.columnName));
        }
    }

    public static boolean isTableExists(DBMap dbMap)
    {
        try (Connection connection = getConnection();)
        {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getTables(connection.getCatalog(), null, null, null);)
            {
                while (resultSet.next())
                {
                    String tableName = resultSet.getString("TABLE_NAME");
                    if (tableName.toUpperCase().equals(dbMap.tableName.toUpperCase()))
                        return true;
                }
            }
            catch (Exception e)
            {
                System.out.println(e.getMessage());
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
