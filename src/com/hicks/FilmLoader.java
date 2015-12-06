package com.hicks;

import java.util.ArrayList;
import java.util.List;

public class FilmLoader
{
    private static List<Film> films = new ArrayList<>();

    public static List<Film> getFilms()
    {
        return films;
    }

    public static void initFilms()
    {
        try
        {
            films = Film.getAllFilmsWithManyVotes();
            if (films.size() == 0)
                films = DatabasePopulator.populateDatabase();

            GenreLoader.getUniqueGenres();
            LanguageLoader.getUniqueLanguages();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
