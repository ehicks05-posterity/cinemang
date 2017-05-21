package net.ehicks.cinemang;

import net.ehicks.cinemang.beans.Film;
import net.ehicks.cinemang.orm.EOI;

import java.text.DecimalFormat;
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
            long filmCount;
            if (SystemInfo.isLoadDbToRam())
            {
                films = Film.getAllFilms();
                filmCount = films.size();
            }
            else
                filmCount = EOI.executeQueryOneResult("select count(*) from films;", new ArrayList<>());

            System.out.println("DB holds " + new DecimalFormat("#,###").format(filmCount) + " films.");
            if (filmCount == 0)
                DatabasePopulator.populateDatabase();

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
