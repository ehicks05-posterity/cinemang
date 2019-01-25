package net.ehicks.cinemang;

import net.ehicks.cinemang.beans.Film;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Component
public class DatabasePopulator
{
    @Value("${omdbZipFilePath}")
    private String omdbZipFilePath;

    private static final int MIN_VOTES_TO_IMPORT = 1000;

    private static int filmsInsertedFromMoviesFile = 0;
    private static int filmsUpdatedFromTomatoesFile = 0;
    private static int films = 0;
    private static int filmsSkipped = 0;
    private static int unreadableRows = 0;

    private FilmRepository filmRepo;

    public DatabasePopulator(FilmRepository filmRepo)
    {
        this.filmRepo = filmRepo;
    }

    public void populateDatabase() throws IOException
    {
        long start = System.currentTimeMillis();

        long filmCount = filmRepo.count();

        System.out.println("DB holds " + new DecimalFormat("#,###").format(filmCount) + " films.");
        if (filmCount > 0)
            return;

        String omdbZipPath = omdbZipFilePath;
        ZipFile zipFile = new ZipFile(omdbZipPath);

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

    private void readZipEntry(ZipFile zipFile, ZipEntry e) throws IOException
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
            if (index % 100_000 == 0)
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
                    if (newFilm.getImdbVotes() >= MIN_VOTES_TO_IMPORT)
                    {
                        filmRepo.save(newFilm);
                        films++;
                        filmsInsertedFromMoviesFile++;
                    }
                    else
                        filmsSkipped++;
                }
                if (zipEntryName.equals("tomatoes.txt"))
                {
                    Optional<Film> existing = filmRepo.findById(newFilm.getImdbId());
                    if (existing.isPresent())
                    {
                        Film mergedFilm = mergeRottenDataIntoFilmData(existing.get(), newFilm);
                        mergedFilm.setCinemangRating(mergedFilm.calculateCinemangRating());
                        filmRepo.save(mergedFilm);
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
        mergedData.setImdbId(movieData.getImdbId());
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
            film.setImdbId(tokens[i++]);
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
            film.setImdbId(Film.convertIdToImdbId(tokens[i++]));
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
