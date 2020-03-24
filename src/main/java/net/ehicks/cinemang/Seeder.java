package net.ehicks.cinemang;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.ReleaseDate;
import info.movito.themoviedbapi.model.people.PersonCast;
import info.movito.themoviedbapi.model.people.PersonCrew;
import info.movito.themoviedbapi.tools.ApiUrl;
import info.movito.themoviedbapi.tools.RequestMethod;
import net.ehicks.cinemang.beans.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static info.movito.themoviedbapi.TmdbMovies.MovieMethod;

@Component
public class Seeder
{
    private static final Logger log = LoggerFactory.getLogger(Seeder.class);

    private static final int DAYS_BETWEEN_UPDATES = 21;
    private static final int DAYS_TO_DELETE_MISSING_ID = 28; // keep higher than DAYS_BETWEEN_UPDATES

    private FilmRepository filmRepo;
    private GenreRepository genreRepo;
    private LanguageRepository languageRepo;

    public Seeder(FilmRepository filmRepo, GenreRepository genreRepo, LanguageRepository languageRepo)
    {
        this.filmRepo = filmRepo;
        this.genreRepo = genreRepo;
        this.languageRepo = languageRepo;
    }

    @Value("${cinemang.tmdb.apikey}")
    private String apiKey;

    @PostConstruct
    public void onStartup() {
        Executors.newFixedThreadPool(1).submit(Seeder.this::run);
    }

    @Scheduled(cron = "${cinemang.seeder.cron}")
    public void runTask()
    {
        run();
    }

    private void run()
    {
        log.info("Starting Seeder.runTask...");
        long start = System.currentTimeMillis();

        if (apiKey == null || apiKey.isEmpty())
        {
            log.error("Seeder will not run. API key is missing.");
            return;
        }

        getGenres();
        getLanguages();
        getFilms();
        getLanguageCounts();

        log.info("Seeder.runTask finished in " + (System.currentTimeMillis() - start) + "ms");
    }

    public void getGenres()
    {
        log.info("Getting genres...");
        if (genreRepo.count() != 0)
            return;

        new TmdbApi(apiKey).getGenre()
                .getGenreList("en")
                .forEach(genre -> genreRepo.save(new Genre(genre.getId(), genre.getName())));
    }

    public void getLanguages()
    {
        log.info("Getting languages...");
        if (languageRepo.count() != 0)
            return;

        try
        {
            String results = new TmdbApi(apiKey).requestWebPage(new ApiUrl("configuration/languages"), null, RequestMethod.GET);
            JsonNode array = new ObjectMapper().readTree(results);
            array.elements().forEachRemaining(jsonNode -> {
                String code = jsonNode.get("iso_639_1").textValue();
                String englishName = jsonNode.get("english_name").textValue();
                languageRepo.save(new net.ehicks.cinemang.beans.Language(code, englishName));
            });
        }
        catch (Exception e)
        {
            log.error(e.getLocalizedMessage(), e);
        }
    }

    public void getLanguageCounts()
    {
        log.info("Getting language counts...");
        languageRepo.findAll().forEach(language -> {
            language.setCount(filmRepo.countByLanguage(language));
            languageRepo.save(language);
        });
    }

    public void getFilms()
    {
        log.info("Getting films...");
        Path dailyIdFile = getDailyIdFile();
        int linesRead = 0;
        int filmsTooFreshToRequest = 0;
        int filmsRequested = 0;
        int filmsSaved = 0;
        int filmsMissing = 0;
        int filmsDeleted = 0;

        if (dailyIdFile != null)
        {
            try
            {
                TmdbMovies movies = new TmdbApi(apiKey).getMovies();

                List<Integer> tmdbIds = Files.readAllLines(dailyIdFile)
                        .stream().map(Integer::parseInt).collect(Collectors.toList());

                Map<Integer, LocalDateTime> filmMap = filmRepo.findAllBy()
                        .stream().collect(Collectors.toMap(FilmIdAndLastUpdated::getTmdbId, FilmIdAndLastUpdated::getLastUpdated));

                for (Integer tmdbId : tmdbIds)
                {
                    if (filmsSaved > tmdbIds.size() / DAYS_BETWEEN_UPDATES)
                    {
                        log.info("1/" + DAYS_BETWEEN_UPDATES + " movies from tmdb have been saved. Stopping now.");
                        break;
                    }

                    int[] results = processTmdbId(movies, filmMap, tmdbId);
                    filmsTooFreshToRequest += results[0];
                    filmsRequested += results[1];
                    filmsSaved += results[2];
                    linesRead++;
                }

                List<Film> filmsMissingFromIdFile = getFilmsMissingFromIdFile(tmdbIds, filmMap);
                filmsMissing = filmsMissingFromIdFile.size();
                filmsDeleted = deleteFilmsMissingFromIdFile(filmsMissingFromIdFile);
            }
            catch (Exception e)
            {
                log.error(e.getLocalizedMessage(), e);
            }

            log.info("linesRead: " + linesRead +
                    ", tooFreshToRequest: " + filmsTooFreshToRequest +
                    ", requested: " + filmsRequested +
                    ", saved: " + filmsSaved +
                    ", missing: " + filmsMissing +
                    ", deleted: " + filmsDeleted);
        }
    }

    private List<Film> getFilmsMissingFromIdFile(List<Integer> validTmdbIds, Map<Integer, LocalDateTime> filmMap)
    {
        Map<Integer, Integer> tmdbIdMap = validTmdbIds.stream()
                .collect(Collectors.toMap(integer -> integer, integer -> integer));

        return filmMap.keySet().stream()
                .filter(key -> !tmdbIdMap.containsKey(key))
                .map(key -> filmRepo.findById(key).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private int deleteFilmsMissingFromIdFile(List<Film> films)
    {
        List<Film> oldEnoughToDelete = films.stream()
                .filter(film -> film.getLastUpdated().isBefore(LocalDateTime.now().minusDays(DAYS_TO_DELETE_MISSING_ID)))
                .collect(Collectors.toList());

        oldEnoughToDelete.forEach(film -> {
            log.info("Deleting film missing from Daily ID File and not updated for " +
                    DAYS_TO_DELETE_MISSING_ID + " days: " + film.toString());
            filmRepo.delete(film);
        });

        return oldEnoughToDelete.size();
    }

    private int[] processTmdbId(TmdbMovies movies, Map<Integer, LocalDateTime> filmMap, int tmdbId)
    {
        int[] results = new int[3];
        LocalDateTime filmLastUpdated = filmMap.get(tmdbId);

        if (filmLastUpdated != null && filmLastUpdated.isAfter(LocalDateTime.now().minusDays(DAYS_BETWEEN_UPDATES)))
        {
            results[0] = 1;
            return results;
        }

        if (filmLastUpdated == null)
        {
            Film film = getFilm(movies, tmdbId);
            results[1] = 1;
            if (film != null)
            {
                filmRepo.save(film);
                results[2] = 1;
            }
        }

        return results;
    }

    private String getDailyFilename()
    {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Z"));
        if (localDateTime.getHour() < 9)
            localDateTime = localDateTime.minusDays(1);

        String MM = localDateTime.format(DateTimeFormatter.ofPattern("MM"));
        String dd = localDateTime.format(DateTimeFormatter.ofPattern("dd"));
        String YYYY = localDateTime.format(DateTimeFormatter.ofPattern("YYYY"));

        return "movie_ids_" + MM + "_" + dd + "_" + YYYY + ".json.gz";
    }

    private Path getDailyFilePath()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tmpDir, getDailyFilename());
    }

    private Path getDailyFileUnzippedPath()
    {
        String tmpDir = System.getProperty("java.io.tmpdir");
        return Paths.get(tmpDir, getDailyFilename().replace(".json.gz", ".txt"));
    }

    private Path getDailyIdFile()
    {
        Path dailyIdFileUnzipped = getDailyFileUnzippedPath();
        if (dailyIdFileUnzipped.toFile().exists())
            return dailyIdFileUnzipped;

        Path dailyIdFile = downloadDailyIdFile();
        if (dailyIdFile == null)
            return null;

        return convertDailyIdFileToTextFile(dailyIdFile);
    }

    private Path downloadDailyIdFile()
    {
        try (InputStream in = new URL("http://files.tmdb.org/p/exports/" + getDailyFilename()).openStream();)
        {
            Path temp = Files.createFile(getDailyFilePath());
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            return temp;
        }
        catch (Exception e)
        {
            log.error(e.getLocalizedMessage(), e);
        }
        return null;
    }

    private Path convertDailyIdFileToTextFile(Path dailyIdFile)
    {
        Path unzipped = Paths.get(dailyIdFile.toString().replace(".json.gz", ".txt"));
        try (InputStream fileStream = new FileInputStream(dailyIdFile.toFile());
             InputStream gzipStream = new GZIPInputStream(fileStream);
             Reader decoder = new InputStreamReader(gzipStream, Charset.defaultCharset());
             BufferedReader buffered = new BufferedReader(decoder);
             FileWriter fileWriter = new FileWriter(unzipped.toFile());
             PrintWriter printWriter = new PrintWriter(fileWriter))
        {
            ObjectMapper mapper = new ObjectMapper();

            String line;
            while ((line = buffered.readLine()) != null)
            {
                int id = mapper.readTree(line).get("id").asInt();
                printWriter.println(id);
            }
        }
        catch (Exception e)
        {
            log.error(e.getLocalizedMessage());
            return null;
        }

        return unzipped;
    }

    private Film getFilm(TmdbMovies movies, int tmdbId)
    {
        MovieDb movie = null;

        try {
            movie = movies.getMovie(tmdbId, "en", MovieMethod.releases, MovieMethod.release_dates, MovieMethod.credits);
        }
        catch (Exception e)
        {
            log.debug("tmdbId: " + tmdbId + "... Error Message: " + e.getLocalizedMessage());
            return null;
        }

        String director = movie.getCrew().stream()
                .filter(crew -> crew.getJob().equals("Director"))
                .map(PersonCrew::getName).findFirst().orElse("");

        String writer = movie.getCrew().stream()
                .filter(crew -> crew.getJob().equals("Writer"))
                .map(PersonCrew::getName).findFirst().orElse("");

        String cast = movie.getCast().stream()
                .sorted(Comparator.comparing(PersonCast::getOrder))
                .takeWhile(actor -> actor.getOrder() < 3)
                .map(PersonCast::getName)
                .reduce((s, s2) -> s + ", " + s2).orElse("");

        // look for oldest theatrical USA release
        ReleaseDate releaseDate = movie.getReleases().stream()
                .filter(releaseInfo -> releaseInfo.getCountry().equals("US"))
                .flatMap(releaseInfo -> releaseInfo.getReleaseDates().stream())
                .filter(releaseDate1 -> releaseDate1.getType().equals("3"))
                .min(Comparator.comparing(ReleaseDate::getReleaseDate))
                .orElse(null);

        String certification = "";
        if (releaseDate != null)
            certification = releaseDate.getCertification();

        // if not found, relax the country requirement...
        if (releaseDate == null)
        {
            releaseDate = movie.getReleases().stream()
                    .flatMap(releaseInfo -> releaseInfo.getReleaseDates().stream())
                    .filter(releaseDate1 -> releaseDate1.getType().equals("3"))
                    .min(Comparator.comparing(ReleaseDate::getReleaseDate))
                    .orElse(null);
        }

        // if not found, relax the release type requirement...
        if (releaseDate == null)
        {
            releaseDate = movie.getReleases().stream()
                    .flatMap(releaseInfo -> releaseInfo.getReleaseDates().stream())
                    .min(Comparator.comparing(ReleaseDate::getReleaseDate))
                    .orElse(null);
        }

        LocalDate released = releaseDate != null
                ? LocalDate.parse(releaseDate.getReleaseDate(), DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                : null;

        Set<Genre> genres = movie.getGenres().stream()
                .map(genre -> new Genre(genre.getId(), genre.getName()))
                .collect(Collectors.toSet());

        Language language = languageRepo.findById(movie.getOriginalLanguage()).orElse(null);
        if (language == null)
        {
            log.error(movie + " missing original language");
            return null;
        }

        Film film = new Film();
        film.setTmdbId(movie.getId());
        film.setImdbId(movie.getImdbID());
        film.setTitle(movie.getTitle());
        film.setYear(movie.getReleaseDate());
        film.setRated(certification);
        film.setRuntime(movie.getRuntime());
        film.setGenres(genres);
        film.setReleased(released);
        film.setDirector(director);
        film.setWriter(writer);
        film.setActors(cast);
        film.setUserVoteAverage(movie.getVoteAverage());
        film.setUserVoteCount(movie.getVoteCount());
        film.setPosterPath(movie.getPosterPath() == null ? "" : movie.getPosterPath());
        film.setOverview(movie.getOverview());
        film.setLanguage(language);
        film.setRevenue(movie.getRevenue());

        return film;
    }
}
