package com.hicks;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SQLGenerator
{
    public static String getCreateTableStatement(DBMap dbMap)
    {
        String createStatement = "create table " + dbMap.tableName + " ";
        String columns = "";
        for (DBMapField dbMapField : dbMap.fields)
        {
            if (columns.length() > 0)
                columns += ", ";
            columns += dbMapField.columnName + " " + dbMapField.getColumnDefinition();
        }
        createStatement += "(" + columns + ");";

        return createStatement;
    }

    public static String getInsertStatement(Object object)
    {
        DBMap dbMap = DBMap.getDBMapByClass(object.getClass());
        String columnNames = "";
        String columnValues = "";
        for (DBMapField dbMapField : dbMap.fields)
        {
            if (columnNames.length() > 0)
                columnNames += ",";
            columnNames += dbMapField.columnName;

            if (columnValues.length() > 0)
                columnValues += ",";
            columnValues += "?";
        }
        return "insert into " + dbMap.tableName + " (" + columnNames + ") values (" + columnValues + ");";
    }

    /*
    * 
    * */
    public static <T> PSIngredients getUpdateStatement(T object)
    {
        DBMap dbMap = DBMap.getDBMapByClass(object.getClass());
        String objectWhereClause = getWhereClause(object);
        T existing = EOI.executeQueryWithPSOneResult("select * from " + dbMap.tableName + objectWhereClause, new ArrayList<>());
        if (existing == null)
            return null;

        String statement = "update " + dbMap.tableName + " set ";
        List<Object> args = new ArrayList<>();
        for (DBMapField dbMapField : dbMap.fields)
        {
            try
            {
                Object valueInDb = dbMapField.getGetter().invoke(existing);
                Object newValue = dbMapField.getGetter().invoke(object);

                boolean bothNull = newValue == null && valueInDb == null;
                boolean bothExist = newValue != null && valueInDb != null;
                boolean equal = bothNull || (bothExist && newValue.equals(valueInDb));
                if (equal)
                    continue;

                if (!statement.endsWith(" set "))
                    statement += ",";

                statement += dbMapField.columnName + "=?";

                args.add(newValue);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                System.out.println(e.getMessage());
            }
        }

        if (args.size() == 0)
            return null;

        return new PSIngredients(statement + objectWhereClause + ";", args);
    }

    public static String getWhereClause(Object object)
    {
        String where = " where ";

        DBMap dbMap = DBMap.getDBMapByClass(object.getClass());
        for (DBMapField pkField : dbMap.getPKFields())
        {
            String columnName = pkField.columnName;
            Object columnValue = null;
            try
            {
                columnValue = pkField.getGetter().invoke(object, null);
            }
            catch (IllegalAccessException | InvocationTargetException e)
            {
                System.out.println(e.getMessage());
            }

            if (columnValue != null)
            {
                where += columnName + "=" + "'" + columnValue.toString() + "'";
            }
        }

        return where;
    }

    public static String getCountVersionOfQuery(String query)
    {
        int indexOfFrom = query.indexOf("from");
        query = query.substring(indexOfFrom);

        int indexOfOrderBy = query.indexOf("order by");
        if (indexOfOrderBy != -1)
            query = query.substring(0, indexOfOrderBy);

        int indexOfGroupBy = query.indexOf("group by");
        if (indexOfGroupBy != -1)
            query = query.substring(0, indexOfGroupBy);

        int indexOfHaving = query.indexOf(" having ");
        if (indexOfHaving != -1)
            query = query.substring(0, indexOfHaving);

        return "select count(*) " + query;
    }
}
