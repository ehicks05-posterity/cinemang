package com.hicks;

import java.util.*;

public class GenreLoader
{
    public static List<String> identifyUniqueGenres(List<Film> films)
    {
        System.out.println("Identifying unique film genres");
        Map<String, Genre> genreMap = new HashMap<>();
        for (Film film : films)
        {
            String[] genres;
            String genreField = film.getGenre();
            if (genreField.length() == 0) continue;

            if (genreField.contains(","))
                genres = genreField.split(",");
            else
                genres = new String[]{genreField};

            for (String genre : genres)
            {
                genre = genre.trim();
                if (genreMap.get(genre) == null)
                    genreMap.put(genre, new Genre(genre, 1));
                else
                {
                    Genre filmGenre = genreMap.get(genre);
                    filmGenre.setOccurrences(filmGenre.getOccurrences() + 1);
                }
            }
        }

        List<Genre> genres = new ArrayList<>(genreMap.values());
        genres.sort(new Comparator<Genre>()
        {
            @Override
            public int compare(Genre o1, Genre o2)
            {
                Integer o1Occurrences = o1.getOccurrences();
                Integer o2Occurrences = o2.getOccurrences();
                return o1Occurrences.compareTo(o2Occurrences);
            }
        });
        Collections.reverse(genres);

        List<String> genreNames = new ArrayList<>();
        for (Genre genre : genres)
            genreNames.add(genre.getName());

        System.out.println("...Found " + genreNames.size());
        return genreNames;
    }

    private static class Genre
    {
        private String name = "";
        private int occurrences;

        public Genre(String name, int occurrences)
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
