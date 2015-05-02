package com.hicks;

import com.owlike.genson.Genson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class OmdbLoader
{
    private static List<Film> films = new ArrayList<>();
    private static int goodReads = 0;
    private static int badReads = 0;
    private static List<String> uniqueLanguages = new ArrayList<>();
    private static List<String> uniqueGenres = new ArrayList<>();

    public static List<Film> getFilms()
    {
        return films;
    }

    public static void loadFilms()
    {
        DecimalFormat df = new DecimalFormat("#,###");

        String path = System.getProperty("java.io.tmpdir") + File.separator + "omdbData";
        for (int i = 0; i < 16; i++)
        {
            List<Film> films = readDataFile(path + i + ".txt");
            System.out.println("Movies loaded from file " + i + ": " + df.format(films.size()));
            OmdbLoader.films.addAll(films);
        }
        System.out.println("Total Movies Loaded: " + df.format(OmdbLoader.films.size()));
        System.out.println("Successful Reads: " + df.format(goodReads));
        System.out.println("Bad Reads: " + df.format(badReads));
        System.out.println("Total Reads: " + df.format((goodReads + badReads)));
    }

    public static List<Film> readDataFile(String path)
    {
        List<Film> films = new ArrayList<>();

        try
        {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String line;
            Film film;
            Genson genson = new Genson();

            while ((line = in.readLine()) != null)
            {
                try
                {
                    film = genson.deserialize(line, Film.class);
                    String[] languages = film.getLanguage().split(",");
                    if (languages.length > 1)
                        film.setLanguage(languages[0]);
                    if (!film.getImdbRating().equals("N/A") && !film.getTomatoMeter().equals("N/A") && !film.getReleased().equals("N/A") && !film.getLanguage().equals("N/A"))
                        films.add(film);
                    goodReads++;
                }
                catch (Exception e)
                {
                    badReads++;
                }
            }
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }

        return films;
    }

    public static List<String> getUniqueLanguages()
    {
        if (uniqueLanguages.size() == 0)
            uniqueLanguages = LanguageLoader.identifyUniqueLanguages(films);
        return uniqueLanguages;
    }

    public static List<String> getUniqueGenres()
    {
        if (uniqueGenres.size() == 0)
            uniqueGenres = GenreLoader.identifyUniqueGenres(films);
        return uniqueGenres;
    }
}
