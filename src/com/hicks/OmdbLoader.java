package com.hicks;

import org.apache.commons.net.ftp.FTPClient;

import javax.persistence.EntityTransaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class OmdbLoader
{
    private static List<Film> films = new ArrayList<>();
    private static int unreadableRows = 0;
    private static List<String> uniqueLanguages = new ArrayList<>();
    private static List<String> uniqueGenres = new ArrayList<>();
    private static boolean loadingMode = true;

    public static List<Film> getFilms()
    {
        return films;
    }

    public static void loadFilms()
    {
        try
        {
            Long filmsInDb = (Long) Hibernate.executeQuerySingleResult("select count(f) from Film f");
            if (filmsInDb == 0 && loadingMode)
            {
                films = importFilmsFromOmdbDump();

                DecimalFormat df = new DecimalFormat("#,###");
                System.out.println("Films Found: " + df.format(films.size()));
                System.out.println("Unreadable Rows: " + df.format(unreadableRows));

//                films = removeFilmsWithMissingData(films);
//                System.out.println("Films Remaining after Filtering: " + df.format(films.size()));

                int persistIndex = 0;
                EntityTransaction transaction = Hibernate.startTransaction();
                for (Film film : films)
                {
                    Hibernate.persistAsPartOfTransaction(film);
                    persistIndex++;
                    if (persistIndex % 100000 == 0)
                        System.out.println(persistIndex + "/" + films.size());
                }
                System.out.println(persistIndex + "/" + films.size());
                System.out.println("committing transaction...");
                Hibernate.commitTransaction(transaction);
            }
            getUniqueGenres();
            getUniqueLanguages();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private static List<Film> importFilmsFromOmdbDump() throws IOException
    {
        FTPClient ftp = IOUtil.prepareFtpClient("***REMOVED***", "***REMOVED***", "");

        InputStream inputStream = ftp.retrieveFileStream("omdb0715.zip");
        ZipInputStream zipIn = new ZipInputStream(inputStream);

        List<Film> films = readZipInputStream(zipIn);
        ftp.logout();
        ftp.disconnect();
        return films;
    }

    private static List<Film> readZipInputStream(ZipInputStream zipIn) throws IOException
    {
        Map<String, Film> filmMap = new HashMap<>();

        for (ZipEntry e; (e = zipIn.getNextEntry()) != null;)
        {
            System.out.println("reading " + e.getName() + " (" + (e.getSize() / (1024 * 1024)) + "MB)");
            BufferedReader br = new BufferedReader(new InputStreamReader(zipIn));
            br.readLine(); // skip the header
            String line;
            while ((line = br.readLine()) != null)
            {
                Film newFilm = null;
                if (e.getName().equals("omdbMovies.txt"))
                    newFilm = readMovieLine(line);
                if (e.getName().equals("tomatoes.txt"))
                    newFilm = readRottenLine(line);

                if (newFilm != null)
                {
                    Film existing = filmMap.get(newFilm.getImdbID());
                    if (existing == null)
                        filmMap.put(newFilm.getImdbID(), newFilm);
                    else
                    {
                        Film mergedFilm = mergeRottenDataIntoFilmData(existing, newFilm);
                        mergedFilm.setCinemangRating(mergedFilm.calculateCinemangRating());

                        filmMap.put(newFilm.getImdbID(), mergedFilm);
                    }
                }
            }
        }

        return new ArrayList<>(filmMap.values());
    }

    private static Film mergeRottenDataIntoFilmData(Film f1, Film f2)
    {
        Film movieData = f1.getLastUpdated().length() > 0 ? f1 : f2;
        Film rottenData = f1.getLastUpdated().length() > 0 ? f2 : f1;

        movieData.setTomatoImage(rottenData.getTomatoImage());
        movieData.setTomatoRating(rottenData.getTomatoRating());
        movieData.setTomatoMeter(rottenData.getTomatoMeter());
        movieData.setTomatoReviews(rottenData.getTomatoReviews());
        movieData.setTomatoFresh(rottenData.getTomatoFresh());
        movieData.setTomatoRotten(rottenData.getTomatoRotten());
        movieData.setTomatoConsensus(rottenData.getTomatoConsensus());
        movieData.setTomatoUserMeter(rottenData.getTomatoUserMeter());
        movieData.setTomatoUserRating(rottenData.getTomatoUserRating());
        movieData.setTomatoUserReviews(rottenData.getTomatoUserReviews());
        movieData.setDvd(rottenData.getDvd());
        movieData.setBoxOffice(rottenData.getBoxOffice());
        movieData.setProduction(rottenData.getProduction());
        movieData.setWebsite(rottenData.getWebsite());
        movieData.setRottenDataLastUpdated(rottenData.getRottenDataLastUpdated());

        return movieData;
    }

    static Film readMovieLine(String line)
    {
        try
        {
            String[] tokens = line.split("\t");
            Film film = new Film();

            int i = 1;
            film.setImdbID(tokens[i++]);
            film.setTitle(tokens[i++]);
            film.setYear(tokens[i++]);
            film.setRated(tokens[i++]);
            film.setRuntime(tokens[i++]);
            film.setGenre(tokens[i++]);
            film.setReleased(Common.stringToDate(tokens[i++]));
            film.setDirector(tokens[i++]);
            film.setWriter(tokens[i++]);
            film.setActors(tokens[i++]);
            film.setMetascore(Common.stringToInt(tokens[i++]));
            film.setImdbRating(Common.stringToBigDecimal(tokens[i++]));
            film.setImdbVotes(Common.stringToInt(tokens[i++]));
            film.setPoster(tokens[i++]);
            film.setPlot(tokens[i++]);
            film.setFullPlot(tokens[i++]);
            film.setLanguage(tokens[i++]);
            film.setCountry(tokens[i++]);
            film.setAwards(tokens[i++]);
            film.setLastUpdated(tokens[i++]);

            String[] languages = film.getLanguage().split(",");
            if (languages.length > 1)
                film.setLanguage(languages[0]);

            return film;
        }
        catch (Exception e)
        {
            unreadableRows++;
        }

        return null;
    }

    static Film readRottenLine(String line)
    {
        try
        {
            String[] tokens = line.split("\t");
            Film film = new Film();

            int i = 0;
            film.setImdbID(Film.convertIdToImdbId(tokens[i++]));
            film.setTomatoImage(tokens[i++]);
            film.setTomatoRating(Common.stringToBigDecimal(tokens[i++]));
            film.setTomatoMeter(Common.stringToInt(tokens[i++]));
            film.setTomatoReviews(Common.stringToInt(tokens[i++]));
            film.setTomatoFresh(Common.stringToInt(tokens[i++]));
            film.setTomatoRotten(Common.stringToInt(tokens[i++]));
            film.setTomatoConsensus(tokens[i++]);
            film.setTomatoUserMeter(Common.stringToInt(tokens[i++]));
            film.setTomatoUserRating(Common.stringToBigDecimal(tokens[i++]));
            film.setTomatoUserReviews(Common.stringToInt(tokens[i++]));
            film.setDvd(Common.stringToDate(tokens[i++]));
            film.setBoxOffice(tokens[i++]);
            film.setProduction(tokens[i++]);
            film.setWebsite(tokens[i++]);
            film.setRottenDataLastUpdated(tokens[i++]);

            return film;
        }
        catch (Exception e)
        {
            unreadableRows++;
        }

        return null;
    }

    private static List<Film> removeFilmsWithMissingData(List<Film> films)
    {
        for (Iterator<Film> i = films.iterator(); i.hasNext();)
        {
            Film film = i.next();
            if (film.getImdbRating() == null || film.getTomatoMeter() == null ||
                    film.getReleased() == null || film.getLanguage().isEmpty())
                i.remove();
        }
        return films;
    }

    public static List<String> getUniqueLanguages()
    {
        if (uniqueLanguages.size() == 0)
        {
            uniqueLanguages = Hibernate.executeQuery("select distinct(f.language) from Film f where f.language is not null group by f.language order by count(f.language) desc");
            System.out.println("Identified " + uniqueLanguages.size() + " distinct languages");
        }
        return uniqueLanguages;
    }

    public static List<String> getUniqueGenres()
    {
        if (uniqueGenres.size() == 0)
        {
            uniqueGenres = GenreLoader.identifyUniqueGenres();
            System.out.println("Identified " + uniqueGenres.size() + " distinct genres");
        }
        return uniqueGenres;
    }
}
