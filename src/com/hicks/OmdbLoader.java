package com.hicks;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try
        {
            OmdbLoader.films = IOUtil.streamFtpFile("***REMOVED***", "", "***REMOVED***", "omdb.zip");
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        DecimalFormat df = new DecimalFormat("#,###");
        System.out.println("Total Movies Loaded: " + df.format(OmdbLoader.films.size()));
        System.out.println("Successful Reads: " + df.format(goodReads));
        System.out.println("Bad Reads: " + df.format(badReads));
        System.out.println("Total Reads: " + df.format((goodReads + badReads)));
    }

    static void parseLine(List<Film> films, String line)
    {
        try
        {
            Film film = parseOmdbRecord(line);
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

    static Film parseOmdbRecord(String record)
    {
        record = record.substring(1, record.length() - 1); // remove curly brackets

        Map<String, String> properties = new HashMap<>();
        List<Integer> commaIndices = new ArrayList<>();

        boolean inQuotes = false;
        boolean nextCharIsEscape = false;

        for (int i = 0; i < record.length(); i++)
        {
            String ch = record.substring(i, i + 1);
            if (ch.equals("\\"))
            {
                nextCharIsEscape = true;
                continue;
            }

            if (ch.equals("\"") && !nextCharIsEscape)
            {
                inQuotes = !inQuotes;
            }

            if (ch.equals(",") && !inQuotes)
                commaIndices.add(i);

            nextCharIsEscape = false;
        }

        List<String> keyValuePairs = new ArrayList<>();
        for (int i = 0; i < commaIndices.size(); i++)
        {
            if (i == 0)
                keyValuePairs.add(record.substring(0, commaIndices.get(i)));
            else if (i == commaIndices.size() - 1)
                keyValuePairs.add(record.substring(commaIndices.get(i)));
            else
                keyValuePairs.add(record.substring(commaIndices.get(i - 1) + 1, commaIndices.get(i)));
        }

        for (String keyValuePair : keyValuePairs)
        {
            keyValuePair = keyValuePair.substring(1, keyValuePair.length() - 1); // remove outer quotes

            String[] tokens = keyValuePair.split("\":\"");
            properties.put(tokens[0], tokens[1]);
        }

        if (properties.get("Response") != null && properties.get("Response").equals("False"))
            return null;

        Film film = new Film();

        film.setTitle(properties.get("Title"));
        film.setYear(properties.get("Year"));
        film.setRated(properties.get("Rated"));
        film.setReleased(properties.get("Released"));
        film.setRuntime(properties.get("Runtime"));
        film.setGenre(properties.get("Genre"));
        film.setDirector(properties.get("Director"));
        film.setWriter(properties.get("Writer"));
        film.setActors(properties.get("Actors"));
        film.setPlot(properties.get("Plot"));
        film.setLanguage(properties.get("Language"));
        film.setCountry(properties.get("Country"));
        film.setAwards(properties.get("Awards"));
        film.setPoster(properties.get("Poster"));
        film.setMetascore(properties.get("Metascore"));
        film.setImdbRating(properties.get("imdbRating"));
        film.setImdbVotes(properties.get("imdbVotes"));
        film.setImdbID(properties.get("imdbID"));
        film.setType(properties.get("Type"));
        film.setTomatoMeter(properties.get("tomatoMeter"));
        film.setTomatoImage(properties.get("tomatoImage"));
        film.setTomatoRating(properties.get("tomatoRating"));
        film.setTomatoReviews(properties.get("tomatoReviews"));
        film.setTomatoFresh(properties.get("tomatoFresh"));
        film.setTomatoRotten(properties.get("tomatoRotten"));
        film.setTomatoConsensus(properties.get("tomatoConsensus"));
        film.setTomatoUserMeter(properties.get("tomatoUserMeter"));
        film.setTomatoUserRating(properties.get("tomatoUserRating"));
        film.setTomatoUserReviews(properties.get("tomatoUserReviews"));
        film.setDvd(properties.get("DVD"));
        film.setBoxOffice(properties.get("BoxOffice"));
        film.setProduction(properties.get("Production"));
        film.setWebsite(properties.get("Website"));

        return film;
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
