package net.ehicks.cinemang;

import net.ehicks.cinemang.beans.Film;
import net.ehicks.cinemang.orm.EOI;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DatabasePopulator
{
    private static final boolean LOAD_SUB_1000_VOTE_MOVIES = false;

    private static int filmsInsertedFromMoviesFile = 0;
    private static int filmsUpdatedFromTomatoesFile = 0;
    private static int films = 0;
    private static int filmsSkipped = 0;
    private static int unreadableRows = 0;

    public static void populateDatabase() throws IOException
    {
        long start = System.currentTimeMillis();

        String omdbFilename = SystemInfo.getProperties().getProperty("omdbZipFileName");

        ZipFile zipFile = new ZipFile("C:" + File.separator + "temp" + File.separator + omdbFilename);

        ZipEntry e = zipFile.getEntry("omdbMovies.txt");
        readZipEntry(zipFile, e);

        e = zipFile.getEntry("tomatoes.txt");
        readZipEntry(zipFile, e);

        DecimalFormat df = new DecimalFormat("#,###");
        System.out.println("Films Found: " + df.format(films));
        System.out.println("Films inserted from omdbMovies: " + df.format(filmsInsertedFromMoviesFile));
        System.out.println("Films updated with data from tomatoes: " + df.format(filmsUpdatedFromTomatoesFile));
        System.out.println("Unreadable Rows: " + df.format(unreadableRows));
        System.out.println("Films Skipped from omdb file: " + df.format(filmsSkipped));

        System.out.println("Parsed data files and Films persisted in " + (System.currentTimeMillis() - start) + "ms");
    }

    private static void readZipEntry(ZipFile zipFile, ZipEntry e) throws IOException
    {
        String zipEntryName = e.getName();
        String entryMBs = " (" + (e.getSize() / (1024 * 1024)) + "MB)";
        System.out.println("reading " + zipEntryName + entryMBs);

        BufferedReader br = new BufferedReader(new InputStreamReader(zipFile.getInputStream(e)));
        br.readLine(); // skip the header
        String line;
        int index = 0;
        while ((line = br.readLine()) != null)
        {
            index++;
            if (index % 10_000 == 0)
                System.out.println("read " + index + " rows...");

            Film newFilm = null;
            if (zipEntryName.equals("omdbMovies.txt"))
                newFilm = readMovieLine(line);
            if (zipEntryName.equals("tomatoes.txt"))
                newFilm = readRottenLine(line);
            if (!Arrays.asList("omdbMovies.txt", "tomatoes.txt").contains(zipEntryName))
                break;

            if (newFilm != null)
            {
                if (zipEntryName.equals("omdbMovies.txt"))
                {
                    if (LOAD_SUB_1000_VOTE_MOVIES || newFilm.getImdbVotes() >= 1000)
                    {
                        EOI.insert(newFilm);
                        films++;
                        filmsInsertedFromMoviesFile++;
                    }
                    else
                        filmsSkipped++;
                }
                if (zipEntryName.equals("tomatoes.txt"))
                {
                    Film existing = Film.getByImdbId(newFilm.getImdbID());
                    if (existing != null)
                    {
                        Film mergedFilm = mergeRottenDataIntoFilmData(existing, newFilm);
                        mergedFilm.setCinemangRating(mergedFilm.calculateCinemangRating());
                        EOI.update(mergedFilm);
                        filmsUpdatedFromTomatoesFile++;
                    }
                }
            }
        }
    }

    private static Film mergeRottenDataIntoFilmData(Film f1, Film f2)
    {
        Film movieData = f1.getLastUpdated().length() > 0 ? f1 : f2;
        Film rottenData = f1.getLastUpdated().length() > 0 ? f2 : f1;

        Film mergedData = new Film();
        mergedData.setImdbID(movieData.getImdbID());
        mergedData.setTitle(movieData.getTitle());
        mergedData.setYear(movieData.getYear());
        mergedData.setRated(movieData.getRated());
        mergedData.setRuntime(movieData.getRuntime());
        mergedData.setGenre(movieData.getGenre());
        mergedData.setReleased(movieData.getReleased());
        mergedData.setDirector(movieData.getDirector());
        mergedData.setWriter(movieData.getWriter());
        mergedData.setActors(movieData.getActors());
        mergedData.setMetascore(movieData.getMetascore());
        mergedData.setImdbRating(movieData.getImdbRating());
        mergedData.setImdbVotes(movieData.getImdbVotes());
        mergedData.setPoster(movieData.getPoster());
        mergedData.setPlot(movieData.getPlot());
        mergedData.setFullPlot(movieData.getFullPlot());
        mergedData.setLanguage(movieData.getLanguage());
        mergedData.setCountry(movieData.getCountry());
        mergedData.setAwards(movieData.getAwards());
        mergedData.setLastUpdated(movieData.getLastUpdated());

        mergedData.setTomatoImage(rottenData.getTomatoImage());
        mergedData.setTomatoRating(rottenData.getTomatoRating());
        mergedData.setTomatoMeter(rottenData.getTomatoMeter());
        mergedData.setTomatoReviews(rottenData.getTomatoReviews());
        mergedData.setTomatoFresh(rottenData.getTomatoFresh());
        mergedData.setTomatoRotten(rottenData.getTomatoRotten());
        mergedData.setTomatoConsensus(rottenData.getTomatoConsensus());
        mergedData.setTomatoUserMeter(rottenData.getTomatoUserMeter());
        mergedData.setTomatoUserRating(rottenData.getTomatoUserRating());
        mergedData.setTomatoUserReviews(rottenData.getTomatoUserReviews());
        mergedData.setDvd(rottenData.getDvd());
        mergedData.setBoxOffice(rottenData.getBoxOffice());
        mergedData.setProduction(rottenData.getProduction());
        mergedData.setWebsite(rottenData.getWebsite());
        mergedData.setRottenDataLastUpdated(rottenData.getRottenDataLastUpdated());

        return mergedData;
    }

    private static Film readMovieLine(String line)
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
            System.out.println(e.getMessage());
            unreadableRows++;
        }

        return null;
    }

    private static Film readRottenLine(String line)
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
}
