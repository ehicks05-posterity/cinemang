package net.ehicks.cinemang;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.movito.themoviedbapi.TmdbApi;
import info.movito.themoviedbapi.TmdbMovies;
import info.movito.themoviedbapi.model.MovieDb;
import info.movito.themoviedbapi.model.ReleaseDate;
import info.movito.themoviedbapi.model.ReleaseInfo;
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
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import static info.movito.themoviedbapi.TmdbMovies.MovieMethod;

@Component
public class Seeder
{
    private static final Logger log = LoggerFactory.getLogger(Seeder.class);

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

    @Scheduled(cron = "${cinemang.seeder.cron}")
    public void runTask()
    {
        log.info("Starting Seeder.runTask...");
        long start = System.currentTimeMillis();

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
        Path dailyIdFile = getDailyIdFile();
        int linesRead = 0;
        int filmsTooFreshToRequest = 0;
        int filmsRequested = 0;
        int filmsSaved = 0;
        
        if (dailyIdFile != null)
        {
            try (InputStream fileStream = new FileInputStream(dailyIdFile.toFile());
                 InputStream gzipStream = new GZIPInputStream(fileStream);
                 Reader decoder = new InputStreamReader(gzipStream, Charset.defaultCharset());
                 BufferedReader buffered = new BufferedReader(decoder);)
            {
                TmdbMovies movies = new TmdbApi(apiKey).getMovies();
                ObjectMapper mapper = new ObjectMapper();
                String line;
                List<Integer> tmdbIds = new ArrayList<>();
                while ((line = buffered.readLine()) != null)
                {
                    linesRead++;
                    if (linesRead % 1000 == 0)
                    {
                        log.info("linesRead: " + linesRead +
                                ", filmsTooFreshToRequest: " + filmsTooFreshToRequest +
                                ", filmsRequested: " + filmsRequested +
                                ", filmsSaved: " + filmsSaved);
                    }

                    int id = mapper.readTree(line).get("id").asInt();

                    tmdbIds.add(id);
                    if (tmdbIds.size() >= 1000)
                    {
                        List<Film> films = filmRepo.findAllById(tmdbIds);
                        Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getTmdbId, film -> film));
                        for (int tmdbId : tmdbIds)
                        {
                            int[] results = processTmdbId(movies, filmMap, tmdbId);
                            filmsTooFreshToRequest += results[0];
                            filmsRequested += results[1];
                            filmsSaved += results[2];
                        }

                        tmdbIds.clear();
                    }
                }

                // deal with leftovers
                List<Film> films = filmRepo.findAllById(tmdbIds);
                Map<Integer, Film> filmMap = films.stream().collect(Collectors.toMap(Film::getTmdbId, film -> film));
                for (int tmdbId : tmdbIds)
                {
                    int[] results = processTmdbId(movies, filmMap, tmdbId);
                    filmsTooFreshToRequest += results[0];
                    filmsRequested += results[1];
                    filmsSaved += results[2];
                }

                tmdbIds.clear();
            }
            catch (Exception e)
            {
                log.error(e.getLocalizedMessage(), e);
            }
        }
    }

    private int[] processTmdbId(TmdbMovies movies, Map<Integer, Film> filmMap, int tmdbId)
    {
        int[] results = new int[3];
        Film film = filmMap.get(tmdbId);

        if (film != null && film.getLastUpdated().isAfter(LocalDateTime.now().minusDays(7)))
        {
            results[0] = 1;
            return results;
        }

        if (film == null)
        {
            film = getFilm(movies, tmdbId);
            results[1] = 1;
            if (film != null)
            {
                filmRepo.save(film);
                results[2] = 1;
            }
        }

        return results;
    }

    private Path getDailyIdFile()
    {
        LocalDateTime localDateTime = LocalDateTime.now(ZoneId.of("Z"));
        if (localDateTime.getHour() < 9)
            localDateTime = localDateTime.minusDays(1);

        String MM = localDateTime.format(DateTimeFormatter.ofPattern("MM"));
        String dd = localDateTime.format(DateTimeFormatter.ofPattern("dd"));
        String YYYY = localDateTime.format(DateTimeFormatter.ofPattern("YYYY"));

        String tmpDir = System.getProperty("java.io.tmpdir");
        String dailyFilename = MM + "-" + dd + "-" + YYYY + ".json.gz";
        Path dailyIdFile = Paths.get(tmpDir, dailyFilename);
        if (!dailyIdFile.toFile().exists())
        {
            try (InputStream in = new URL("http://files.tmdb.org/p/exports/movie_ids_" + MM + "_" + dd + "_" + YYYY + ".json.gz").openStream();)
            {
                Path temp = Files.createFile(dailyIdFile);
                Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
            }
            catch (Exception e)
            {
                log.error(e.getLocalizedMessage(), e);
                return null;
            }
        }

        return dailyIdFile;
    }

    private Film getFilm(TmdbMovies movies, int tmdbId)
    {
        MovieDb movie = null;

        try {
            movie = movies.getMovie(tmdbId, "en", MovieMethod.releases, MovieMethod.release_dates, MovieMethod.credits);
        }
        catch (Exception e)
        {
            log.error(e.getLocalizedMessage(), e);
        }

        if (movie == null)
        {
            log.error("tmdb returned null movie");
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
