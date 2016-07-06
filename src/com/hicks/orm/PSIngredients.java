package com.hicks.orm;

import java.util.List;

public class PSIngredients
{
    String query = "";
    List<Object> args;

    public PSIngredients(String query, List<Object> args)
    {
        this.query = query;
        this.args = args;
    }
}
