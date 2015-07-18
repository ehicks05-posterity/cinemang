package com.hicks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser
{
    public static Map<String, String> parseJsonRecord(String record)
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
                inQuotes = !inQuotes;

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

        return properties;
    }

    public static Film getFilmFromJson(Map<String, String> properties)
    {
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
}
