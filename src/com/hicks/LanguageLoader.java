package com.hicks;

import java.util.ArrayList;
import java.util.List;

public class LanguageLoader
{
    private static List<String> uniqueLanguages = new ArrayList<>();

    public static List<String> getUniqueLanguages()
    {
        if (uniqueLanguages.size() == 0)
        {
            uniqueLanguages = Hibernate.executeQuery("select distinct(f.language) from Film f where f.language is not null group by f.language order by count(f.language) desc");
            System.out.println("Identified " + uniqueLanguages.size() + " distinct languages");
        }
        return uniqueLanguages;
    }
}
