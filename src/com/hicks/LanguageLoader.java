package com.hicks;

import java.util.*;

public class LanguageLoader
{
    public static List<String> identifyUniqueLanguages(List<Film> films)
    {
        System.out.println("Identifying unique film languages");
        Map<String, FilmLanguage> languagesMap = new HashMap<>();
        for (Film film : films)
        {
            String language = film.getLanguage();
            if (languagesMap.get(language) == null)
                languagesMap.put(language, new FilmLanguage(language, 1));
            else
            {
                FilmLanguage filmLanguage = languagesMap.get(language);
                int occurrences = filmLanguage.getOccurrences();
                filmLanguage.setOccurrences(occurrences + 1);
            }
        }

        List<FilmLanguage> filmLanguages = new ArrayList<>(languagesMap.values());
        filmLanguages.sort(new Comparator<FilmLanguage>()
        {
            @Override
            public int compare(FilmLanguage o1, FilmLanguage o2)
            {
                Integer o1Occurrences = o1.getOccurrences();
                Integer o2Occurrences = o2.getOccurrences();
                return o1Occurrences.compareTo(o2Occurrences);
            }
        });
        Collections.reverse(filmLanguages);

        List<String> languageNames = new ArrayList<>();
        for (FilmLanguage filmLanguage : filmLanguages)
            languageNames.add(filmLanguage.getName());

        System.out.println("...Found " + languageNames.size());
        return languageNames;
    }

    private static class FilmLanguage
    {
        private String name = "";
        private int occurrences;

        public FilmLanguage(String name, int occurrences)
        {
            this.name = name;
            this.occurrences = occurrences;
        }

        public String getName()
        {
            return name;
        }

        public void setName(String name)
        {
            this.name = name;
        }

        public int getOccurrences()
        {
            return occurrences;
        }

        public void setOccurrences(int occurrences)
        {
            this.occurrences = occurrences;
        }
    }

}
