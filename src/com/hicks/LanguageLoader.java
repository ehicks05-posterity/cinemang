package com.hicks;

import com.hicks.orm.EOI;

import java.util.ArrayList;
import java.util.List;

public class LanguageLoader
{
    private static List<String> uniqueLanguages = new ArrayList<>();

    public static List<String> getUniqueLanguages()
    {
        if (uniqueLanguages.size() == 0)
        {
//            uniqueLanguages = Hibernate.executeQuery("select distinct(f.language) from Film f where f.language is not null group by f.language order by count(f.language) desc");
            List<List<Object>> results = EOI.executeQuery("select language, count(language) from films where language is not null group by language order by count(language) desc");
            for (List<Object> row : results)
            {
                String language = (String) row.get(0);

                if (language.length() == 0)
                    continue;

                uniqueLanguages.add(language);
            }
            System.out.println("Identified " + results.size() + " distinct languages");
        }
        return uniqueLanguages;
    }
}
