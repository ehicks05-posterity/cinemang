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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import net.ehicks.cinemang.DailyFile;

import static info.movito.themoviedbapi.TmdbMovies.MovieMethod;

@Component
public class Seeder
{
    private static final Logger log = LoggerFactory.getLogger(Seeder.class);

    private static final int DAYS_BETWEEN_UPDATES = 14;

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
        Path dailyIdFile = DailyFile.getDailyIdFile();
        int linesRead = 0;
        int fresh = 0;
        int stale = 0;
        int saved = 0;
        int missing = 0;
        int filmsDeleted = 0;

        if (dailyIdFile != null)
        {
            try
            {
                TmdbMovies movies = new TmdbApi(apiKey).getMovies();
                ExecutorService executorService = Executors.newWorkStealingPool(32);

                List<Integer> tmdbIds = Files.readAllLines(dailyIdFile)
                        .stream().map(Integer::parseInt).collect(Collectors.toList());
                log.info("Found " + tmdbIds.size() + " ids in the daily id file...");

                int chunkSize = 5000;
                outer:
                for (int chunk = 0; chunk * chunkSize < tmdbIds.size(); chunk++)
                {
                    int from = chunk * chunkSize;
                    int to = (chunk + 1) * chunkSize;
                    if (to >= tmdbIds.size())
                        to = tmdbIds.size() - 1;

                    List<Integer> idChunk = tmdbIds.subList(from, to);
                    List<Film> filmChunk = filmRepo.findAllById(idChunk);

                    List<CompletableFuture<ProcessTmdbIdResult>> futures = idChunk.stream().map(tmdbId -> {
                        Film film = filmChunk.stream().filter(film1 -> film1.getTmdbId() == tmdbId).findFirst()
                                .orElse(null);

                        return CompletableFuture.supplyAsync(() -> processTmdbId(movies, tmdbId, film), executorService);        
                    }).collect(Collectors.toList());

                    CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
                    combinedFuture.get();

                    for (CompletableFuture<ProcessTmdbIdResult> future : futures) {
                        if (future.get().fresh) fresh++;
                        if (future.get().stale) stale++;
                        if (future.get().saved) saved++;
                    }

                    int IdsScanned = ((chunk + 1) * chunkSize);
                    int percent = IdsScanned / (tmdbIds.size() / 100);
                    log.info("checked " + IdsScanned + " ids (" + percent + "%). saved " + saved + " films.");
                }

                List<Film> filmsMissingFromIdFile = getFilmsMissingFromIdFile(tmdbIds);
                missing = filmsMissingFromIdFile.size();
                deleteFilmsMissingFromIdFile(filmsMissingFromIdFile);
            }
            catch (Exception e)
            {
                log.error(e.getLocalizedMessage(), e);
            }

            log.info("linesRead " + linesRead + // daily file id's scanned 
                    ", fresh " + fresh +        // not updated             
                    ", stale " + stale +        // tried to update         
                    ", saved " + saved +        // successfully saved      
                    ", missing " + missing);    // and removed             
        }
    }

    private List<Film> getFilmsMissingFromIdFile(List<Integer> validTmdbIds)
    {
        List<Film> results = new ArrayList<>();
        Map<Integer, Integer> tmdbIdMap = validTmdbIds.stream()
                .collect(Collectors.toMap(integer -> integer, integer -> integer));

        Page<Film> page;
        Pageable pageable = PageRequest.of(0, 1000, Sort.by("tmdbId").ascending());
        while (true)
        {
            page = filmRepo.findAll(pageable);

            List<Film> missingFromIdFile = page.stream()
                    .filter(film -> !tmdbIdMap.containsKey(film.getTmdbId()))
                    .collect(Collectors.toList());
            results.addAll(missingFromIdFile);

            if (!page.hasNext())
                break;
            pageable = page.nextPageable();
        }

        return results;
    }

    private int deleteFilmsMissingFromIdFile(List<Film> films)
    {
        films.forEach(film -> {
            log.info("Deleting film missing from Daily ID File " + film.toString());
            filmRepo.delete(film);
        });

        return films.size();
    }

    private class ProcessTmdbIdResult
    {
        boolean fresh;
        boolean stale;
        boolean saved;
    }

    private ProcessTmdbIdResult processTmdbId(TmdbMovies movies, int tmdbId, Film film)
    {
        ProcessTmdbIdResult result = new ProcessTmdbIdResult();
        LocalDateTime filmLastUpdated = film != null ? film.getLastUpdated() : null;

        if (filmLastUpdated != null && filmLastUpdated.isAfter(LocalDateTime.now().minusDays(DAYS_BETWEEN_UPDATES)))
        {
            result.fresh = true;
            return result;
        }

        if (filmLastUpdated == null)
        {
            film = getFilm(movies, tmdbId);
            result.stale = true;
            if (film != null)
            {
                filmRepo.save(film);
                result.saved = true;
            }
        }

        return result;
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
