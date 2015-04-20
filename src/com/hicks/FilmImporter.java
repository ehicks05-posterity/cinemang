package com.hicks;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FilmImporter
{
    private static final boolean MEMORY_CONSTRAINED = false;
    private static List<Film> films = new ArrayList<>();
    private static final List<String> MONTHS = Arrays.asList("January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December");
    private static List<String> uniqueLanguages = new ArrayList<>();

    public static List<Film> getFilms()
    {
        return films;
    }

    public static void importFilmData()
    {
        try
        {
            File mergedFilmData = new File(System.getProperty("java.io.tmpdir") + File.separator + "mergedFilmData.dmp");
            if (mergedFilmData.exists())
            {
                films = IOUtil.readFilmDataFromDisk(mergedFilmData);
                return;
            }

            File ratingsFile = getFtpFile("ratings.list.gz");
            File releaseDatesFile = getFtpFile("release-dates.list.gz");
            File languagesFile = getFtpFile("language.list.gz");
            File genresFile = getFtpFile("genres.list.gz");
            System.out.println("Done getting data files");

            System.out.println("Parsing " + ratingsFile.getCanonicalPath());
            List<Film> ratingsFileFilms = parseRatingsFile(ratingsFile);

            File filmsFromRatingsFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "ratings.dmp");
            if (MEMORY_CONSTRAINED)
                IOUtil.writeFilmDataToDisk(ratingsFileFilms, filmsFromRatingsFile);

            System.out.println("Parsing " + releaseDatesFile.getCanonicalPath());
            List<Film> releaseDatesFileFilms = parseReleaseDatesFile(releaseDatesFile);

            File filmsFromReleaseDatesFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "releaseDates.dmp");
            if (MEMORY_CONSTRAINED)
                IOUtil.writeFilmDataToDisk(releaseDatesFileFilms, filmsFromReleaseDatesFile);

            System.out.println("Parsing " + languagesFile.getCanonicalPath());
            List<Film> languagesFileFilms = parseLanguagesFile(languagesFile);

            File filmsFromLanguagesFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "languages.dmp");
            if (MEMORY_CONSTRAINED)
                IOUtil.writeFilmDataToDisk(languagesFileFilms, filmsFromLanguagesFile);

            System.out.println("Parsing " + genresFile.getCanonicalPath());
            List<Film> genresFileFilms = parseGenresFile(genresFile);

            File filmsFromGenresFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "genres.dmp");
            if (MEMORY_CONSTRAINED)
                IOUtil.writeFilmDataToDisk(genresFileFilms, filmsFromGenresFile);

            if (!MEMORY_CONSTRAINED) films = mergeFilmLists(ratingsFileFilms, releaseDatesFileFilms, languagesFileFilms, genresFileFilms);
            if (MEMORY_CONSTRAINED) films = mergeFilmLists(filmsFromRatingsFile, filmsFromReleaseDatesFile, filmsFromLanguagesFile, filmsFromGenresFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static List<Film> mergeFilmLists(List<Film> ratingsFileFilms, List<Film> releaseDatesFileFilms, List<Film> languagesFileFilms, List<Film> genresFileFilms)
    {
        System.out.println("Merging film data from each file");

        Map<String, Film> titleMap = new HashMap<>();

        for (Film film : ratingsFileFilms)
            titleMap.put(film.getTitle(), film);

        for (Film film : releaseDatesFileFilms)
            if (titleMap.containsKey(film.getTitle()))
                titleMap.get(film.getTitle()).setReleaseDate(film.getReleaseDate());

        for (Film film : languagesFileFilms)
            if (titleMap.containsKey(film.getTitle()))
                titleMap.get(film.getTitle()).setLanguage(film.getLanguage());

        for (Film film : genresFileFilms)
            if (titleMap.containsKey(film.getTitle()))
                titleMap.get(film.getTitle()).setGenres(film.getGenres());

        List<Film> mergedFilms = new ArrayList<>(titleMap.values());
        for (Iterator<Film> i = mergedFilms.iterator(); i.hasNext();)
        {
            Film film = i.next();
            if (film.getReleaseDate() == null || film.getLanguage().length() == 0) i.remove();
        }

        IOUtil.writeFilmDataToDisk(mergedFilms, new File(System.getProperty("java.io.tmpdir") + File.separator + "mergedFilmData.dmp"));

        return mergedFilms;
    }

    private static List<Film> mergeFilmLists(File ratings, File releaseDates, File languages, File genres)
    {
        System.out.println("Merging film data from each file");

        Map<String, Film> titleMap = new HashMap<>();
        for (Film film : IOUtil.readFilmDataFromDisk(ratings))
            titleMap.put(film.getTitle(), film);

        for (Film film : IOUtil.readFilmDataFromDisk(releaseDates))
            if (titleMap.containsKey(film.getTitle()))
                titleMap.get(film.getTitle()).setReleaseDate(film.getReleaseDate());

        for (Film film : IOUtil.readFilmDataFromDisk(languages))
            if (titleMap.containsKey(film.getTitle()))
                titleMap.get(film.getTitle()).setLanguage(film.getLanguage());

        for (Film film : IOUtil.readFilmDataFromDisk(genres))
            if (titleMap.containsKey(film.getTitle()))
                titleMap.get(film.getTitle()).setGenres(film.getGenres());

        List<Film> mergedFilms = new ArrayList<>(titleMap.values());
        for (Iterator<Film> i = mergedFilms.iterator(); i.hasNext();)
        {
            Film film = i.next();
            if (film.getReleaseDate() == null || film.getLanguage().length() == 0) i.remove();
        }

        IOUtil.writeFilmDataToDisk(mergedFilms, new File(System.getProperty("java.io.tmpdir") + File.separator + "mergedFilmData.dmp"));

        return mergedFilms;
    }

    private static List<Film> parseReleaseDatesFile(File file) throws ParseException, IOException
    {
        Map<String, Film> filmMap = new HashMap<>();

        List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("ISO-8859-1"));

        boolean enteredDataTable = false;
        for (String line : lines)
        {
            if (line.equals("=================="))
            {
                enteredDataTable = true;
                continue;
            }

            if (enteredDataTable)
            {
                if (line.equals("--------------------------------------------------------------------------------")) break;

                // FILE FORMAT:
                // Title (year)\tCountryCode:d MMM yyyy
                String[] tokens = line.split("\\t+");
                String title = tokens[0];
                String countryRelease = tokens[1];
                String[] countryReleaseTokens = countryRelease.split(":");
                String country = countryReleaseTokens[0];
                String releaseDate = countryReleaseTokens[1];

                if (!country.equals("USA")) continue;

                Date release = null;
                try
                {
                    if (releaseDate.length() == 4)
                        release = new SimpleDateFormat("yyyy").parse(releaseDate);
                    if (release == null)
                        for (String month : MONTHS)
                            if (releaseDate.startsWith(month)) release = new SimpleDateFormat("MMM yyyy").parse(releaseDate);
                    if (release == null)
                        release = new SimpleDateFormat("d MMM yyyy").parse(releaseDate);
                }
                catch (ParseException e)
                {
                    System.out.println(e.getMessage());
                }

                if (release != null)
                {
                    Film film = filmMap.get(title);
                    if (film == null)
                        filmMap.put(title, new Film(title, release));
                    else
                    {
                        Film filmFromMap = filmMap.get(title);
                        if (release.compareTo(filmFromMap.getReleaseDate()) < 0)
                            filmFromMap.setReleaseDate(release);
                    }
                }
            }
        }
        return new ArrayList<>(filmMap.values());
    }

    private static List<Film> parseGenresFile(File file) throws ParseException, IOException
    {
        List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("ISO-8859-1"));

        List<Film> films = new ArrayList<>();
        Map<String, List<String>> genreMap = new HashMap<>();

        boolean enteredSection = false;
        boolean foundBlankLineBeforeData = false;
        for (String line : lines)
        {
            if (line.equals("8: THE GENRES LIST"))
            {
                enteredSection = true;
                continue;
            }
            if (enteredSection && line.length() == 0)
            {
                foundBlankLineBeforeData = true;
                continue;
            }

            if (enteredSection && foundBlankLineBeforeData)
            {
                if (line.length() == 0) break;

                // File Format:
                // Title\tGenre
                String[] tokens = line.split("\\t+");

                String title = tokens[0];
                String genre = tokens[1];

                List<String> genres = genreMap.get(title);
                if (genres == null)
                {
                    genres = new ArrayList<>(Arrays.asList(genre));
                    genreMap.put(title, genres);
                }
                else genres.add(genre);

                films.add(new Film(title, genres));
            }
        }
        return films;
    }

    private static List<Film> parseLanguagesFile(File file) throws ParseException, IOException
    {
        Map<String, Film> filmMap = new HashMap<>();

        List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("ISO-8859-1"));

        boolean enteredDataTable = false;
        for (String line : lines)
        {
            if (line.equals("============="))
            {
                enteredDataTable = true;
                continue;
            }

            if (enteredDataTable)
            {
                if (line.equals("--------------------------------------------------------------------------------")) break;

                // FILE FORMAT:
                // Title (year)\tLanguage (subtitle info)
                String[] tokens = line.split("\\t+");
                String title = tokens[0];
                String languageAndSubtitleInfo = tokens[1];
                int indexOfOpenParen = languageAndSubtitleInfo.indexOf("(");
                String language;
                if (indexOfOpenParen > 0)
                    language = languageAndSubtitleInfo.substring(0, indexOfOpenParen);
                else
                    language = languageAndSubtitleInfo;

                if (language != null)
                {
                    Film film = filmMap.get(title);
                    if (film == null)
                        filmMap.put(title, new Film(title, language));
                    else
                    {
                        Film filmFromMap = filmMap.get(title);
                        if (!filmFromMap.getLanguage().equals("English"))
                            filmFromMap.setLanguage(language);
                    }
                }
            }
        }
        return new ArrayList<>(filmMap.values());
    }

    private static List<Film> parseRatingsFile(File file) throws ParseException, IOException
    {
        List<String> lines = Files.readAllLines(file.toPath(), Charset.forName("ISO-8859-1"));

        List<Film> films = new ArrayList<>();
        boolean enteredRatings = false;
        boolean foundHeader = false;
        for (String line : lines)
        {
            if (line.equals("MOVIE RATINGS REPORT"))
            {
                enteredRatings = true;
                continue;
            }
            if (enteredRatings && line.equals("New  Distribution  Votes  Rank  Title"))
            {
                foundHeader = true;
                continue;
            }

            if (enteredRatings && foundHeader)
            {
                if (line.length() == 0) break;

                // 0-5 star
                // 6-15 rating distribution
//                String star = line.substring(0, 5).trim();
//                String ratingDistribution = line.substring(6, 16);

                String variableWidthPortion = line.substring(16);
                String[] rawTokens = variableWidthPortion.split("  ");
                List<String> tokens = new ArrayList<>();

                for (String token : rawTokens)
                    if (token.length() > 0)
                        tokens.add(token.trim());

                int votes = Integer.parseInt(tokens.get(0));
                BigDecimal rating = new BigDecimal(tokens.get(1));
                String title = tokens.get(2);

                if (title.startsWith("\"") || title.contains("(V)") || title.contains("VG") || title.contains("(TV)")) continue;

                films.add(new Film(title, rating, votes));
            }
        }
        return films;
    }

    private static File getFtpFile(String remoteFilename) throws IOException
    {
        String host = "ftp.fu-berlin.de";
        String dir = "pub/misc/movies/database/";
        String login = "anonymous";

        File zippedFile = IOUtil.downloadFile(host, dir, login, remoteFilename);
        return IOUtil.unzipFile(zippedFile);
    }

    public static List<String> getUniqueLanguages()
    {
        if (uniqueLanguages.size() == 0) uniqueLanguages = identifyUniqueLanguages(films);
        return uniqueLanguages;
    }

    public static List<String> identifyUniqueLanguages(List<Film> films)
    {
        System.out.println("Identifying unique film languages");
        Map<String, FilmLanguage> languagesMap = new HashMap<>();
        for (Film film : films)
        {
            String language = film.getLanguage();
            if (languagesMap.get(language) == null)
                languagesMap.put(language, new FilmLanguage(language, 1));
            else
            {
                FilmLanguage filmLanguage = languagesMap.get(language);
                int occurrences = filmLanguage.getOccurrences();
                filmLanguage.setOccurrences(occurrences + 1);
            }
        }

        List<FilmLanguage> filmLanguages = new ArrayList<>(languagesMap.values());
        filmLanguages.sort(new Comparator<FilmLanguage>()
        {
            @Override
            public int compare(FilmLanguage o1, FilmLanguage o2)
            {
                Integer o1Occurrences = o1.getOccurrences();
                Integer o2Occurrences = o2.getOccurrences();
                return o1Occurrences.compareTo(o2Occurrences);
            }
        });
        Collections.reverse(filmLanguages);

        List<String> languageNames = new ArrayList<>();
        for (FilmLanguage filmLanguage : filmLanguages)
            languageNames.add(filmLanguage.getName());

        return languageNames;
    }
}
