package net.ehicks.cinemang.orm;

import net.ehicks.cinemang.Common;
import org.h2.jdbcx.JdbcConnectionPool;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EOI
{
    private static JdbcConnectionPool cp;

    public static void init()
    {
        String connectionParameters = "TRACE_LEVEL_FILE=1;CACHE_SIZE=131072";
        cp = JdbcConnectionPool.create("jdbc:h2:~/cinemang;" + connectionParameters, "", "");
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

    // -------- String-Based Methods -------- //

    // for INSERT, UPDATE, or DELETE, or DDL statements
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

    public static void execute(String queryString)
    {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();)
        {
            statement.execute(queryString);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    // -------- Object-Based Methods -------- //

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

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(insertStatement);)
        {
            DBMap dbMap = DBMap.getDBMapByClass(object.getClass());
            for (DBMapField dbMapField : dbMap.fields)
            {
                int argIndex = dbMap.fields.indexOf(dbMapField) + 1;

                Object value = dbMapField.getValue(object);
                if (value == null)
                {
                    preparedStatement.setNull(argIndex, Types.NULL);
                    continue;
                }

                setPreparedStatementParameter(preparedStatement, argIndex, value);
            }
            return preparedStatement.executeUpdate();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return 0;
    }

    public static void update(Object object)
    {
        if (object instanceof List)
            _updateFromList((List) object);
        else
            _update(object);
    }

    private static void _updateFromList(List<?> objects)
    {
        int success = 0;
        int fail = 0;
        for (Object object : objects)
        {
            int result = _update(object);
            if (result == 1)
                success++;
            else
                fail++;
        }
        System.out.println("Finished mass update: " + success + " succeeded, " + fail + " failed");
    }

    private static int _update(Object object)
    {
        PSIngredients psIngredients = SQLGenerator.getUpdateStatement(object);
        if (psIngredients == null)
            return 0;

        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(psIngredients.query);)
        {
            int argIndex = 1;
            for (Object arg : psIngredients.args)
                setPreparedStatementParameter(preparedStatement, argIndex++, arg);

            int result = preparedStatement.executeUpdate();
            if (result == 1)
            {
                EOICache.set(object);
                return result;
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return 0;
    }

    public static <T> T executeQueryOneResult(String queryString, List<Object> args)
    {
        List<T> results = executeQuery(queryString, args);
        if (results != null && results.size() > 0)
            return results.get(0);
        return null;
    }

    public static <T> List<T> executeQuery(String queryString)
    {
        return executeQuery(queryString, new ArrayList<>());
    }

    public static <T> List<T> executeQuery(String queryString, List<Object> args)
    {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(queryString))
        {
            int argIndex = 1;
            for (Object arg : args)
                setPreparedStatementParameter(preparedStatement, argIndex++, arg);

            ResultSet resultSet = preparedStatement.executeQuery();

            return ResultSetParser.parseResultSet(queryString, resultSet);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }

        return null;
    }

    public static <T> List<T> executeQueryWithoutPS(String queryString, List<Object> args)
    {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(queryString);)
        {
            return ResultSetParser.parseResultSet(queryString, resultSet);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }

    private static void setPreparedStatementParameter(PreparedStatement ps, int argIndex, Object obj) throws SQLException
    {
        if (obj instanceof String) ps.setString(argIndex, (String) obj);
        if (obj instanceof Integer) ps.setInt(argIndex, (Integer) obj);
        if (obj instanceof Long) ps.setLong(argIndex, (Long) obj);
        if (obj instanceof BigDecimal) ps.setBigDecimal(argIndex, (BigDecimal) obj);
        if (obj instanceof Date) ps.setDate(argIndex, Common.utilDatetoSQLDate((Date) obj));
    }

    public static boolean isTableExists(DBMap dbMap)
    {
        try (Connection connection = getConnection();)
        {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getTables(connection.getCatalog(), null, null, null);)
            {
                List<String> tableNamesFromDb = new ArrayList<>();
                while (resultSet.next())
                {
                    String tableName = resultSet.getString("TABLE_NAME").toUpperCase();
                    tableNamesFromDb.add(tableName);
                    if (tableName.equals(dbMap.tableName.toUpperCase()))
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

    public static String getCurrentSchema()
    {
        try (Connection connection = getConnection();)
        {
            DatabaseMetaData databaseMetaData = connection.getMetaData();
            try (ResultSet resultSet = databaseMetaData.getSchemas(connection.getCatalog(), "");)
            {
                List<String> schemaNames = new ArrayList<>();
                while (resultSet.next())
                {
                    String schemaName = resultSet.getString("TABLE_SCHEM").toUpperCase();
                    System.out.println(schemaName);
                    schemaNames.add(schemaName);
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
        return null;
    }

    public static boolean setSchema(String schemaName)
    {
        try (Connection connection = getConnection();)
        {
            connection.setSchema(schemaName);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        return false;
    }
}
