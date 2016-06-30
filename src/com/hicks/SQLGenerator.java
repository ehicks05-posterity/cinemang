package com.hicks;

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
        String insertStatement = "insert into " + dbMap.tableName + " ";
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
        insertStatement += "(" + columnNames + ") values (" + columnValues + ");";

        return insertStatement;
    }
}
