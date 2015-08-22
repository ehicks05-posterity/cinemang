package com.hicks;

import java.util.*;

public class GenreLoader
{
    public static List<String> identifyUniqueGenres()
    {
        Map<String, Genre> genreMap = new HashMap<>();
        List<Object[]> results = Hibernate.executeQuery("select count(f.genre), genre from Film f where f.genre is not null group by f.genre");
        String[] genreTokens;

        for (Object[] result : results)
        {
            Long count = (Long)result[0];
            String genreField = (String)result[1];

            if (genreField.length() == 0) continue;

            if (genreField.contains(","))
                genreTokens = genreField.split(",");
            else
                genreTokens = new String[]{genreField};

            for (String genre : genreTokens)
            {
                genre = genre.trim();
                if (genreMap.get(genre) == null)
                    genreMap.put(genre, new Genre(genre, count));
                else
                {
                    Genre filmGenre = genreMap.get(genre);
                    filmGenre.setOccurrences(filmGenre.getOccurrences() + count);
                }
            }
        }

        List<Genre> genres = new ArrayList<>(genreMap.values());
        genres.sort(new Comparator<Genre>()
        {
            @Override
            public int compare(Genre o1, Genre o2)
            {
                Long o1Occurrences = o1.getOccurrences();
                Long o2Occurrences = o2.getOccurrences();
                return o1Occurrences.compareTo(o2Occurrences);
            }
        });
        Collections.reverse(genres);

        List<String> genreNames = new ArrayList<>();
        for (Genre genre : genres)
            genreNames.add(genre.getName());

        return genreNames;
    }

    private static class Genre
    {
        private String name = "";
        private long occurrences;

        public Genre(String name, long occurrences)
        {
            this.name = name;
            this.occurrences = occurrences;
        }

        public String toString()
        {
            return name + " " + occurrences;
        }

        public String getName()
        {
            return name;
        }

        public long getOccurrences()
        {
            return occurrences;
        }

        public void setOccurrences(long occurrences)
        {
            this.occurrences = occurrences;
        }
    }

}
