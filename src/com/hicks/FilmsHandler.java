package com.hicks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class FilmsHandler
{
    public static String showFilms(HttpServletRequest request, HttpServletResponse response) throws ParseException
    {
        List<Film> searchResults = (List<Film>) request.getSession().getAttribute("searchResults");
        if (searchResults == null)
            searchResults = performInitialSearch(request);

        request.getSession().setAttribute("searchResults", searchResults);

        int pages = 1 + ((searchResults.size() - 1) / 100);

        String pageParam = request.getParameter("page");
        if (pageParam == null || Integer.valueOf(pageParam) > pages) pageParam = "1";

        int pageNumber = Integer.valueOf(pageParam);

        List<Film> filmsOnPage = getAPageOfFilms(searchResults, pageNumber);

        request.setAttribute("searchResultsSize", new DecimalFormat("#,###").format(searchResults.size()));
        request.setAttribute("uniqueLanguages", OmdbLoader.getUniqueLanguages());
        request.setAttribute("uniqueGenres", OmdbLoader.getUniqueGenres());
        request.setAttribute("pages", pages);
        request.setAttribute("page", pageParam);
        request.setAttribute("hasNext", pages > pageNumber);
        request.setAttribute("hasPrevious", pageNumber > 1);
        request.setAttribute("filmsOnPage", filmsOnPage);

        return "webroot/filmsList.jsp";
    }

    private static List<Film> performInitialSearch(HttpServletRequest request) throws ParseException
    {
        // set some defaults
        String minimumVotes = "0";
        String rating = "0-10";
        String language = "";

        request.getSession().setAttribute("minimumVotes", minimumVotes);
        request.getSession().setAttribute("rating", rating);
        request.getSession().setAttribute("language", language);

        return performSearch(request, "", minimumVotes, rating, "", "", language, "");
    }

    private static List<Film> getAPageOfFilms(List<Film> films, int pageNumber)
    {
        List<Film> filmsOnPage = new ArrayList<>();

        int from = (pageNumber - 1) * 100;
        int filmsAfterFrom = films.size() - from;
        int to = filmsAfterFrom < 100 ? from + filmsAfterFrom : from + 100;

        filmsOnPage.addAll(films.subList(from, to));
        return filmsOnPage;
    }

    public static void filterFilms(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
    {
        String titleParam = request.getParameter("title");
        String minimumVotesParam = request.getParameter("minimumVotes");
        String ratingParam = request.getParameter("fldRating");
        String fromReleaseDate = request.getParameter("fromReleaseDateDatepicker");
        String toReleaseDate = request.getParameter("toReleaseDateDatepicker");
        String language = request.getParameter("language");
        String genre = request.getParameter("fldGenre");

        List<Film> searchResults = performSearch(request, titleParam, minimumVotesParam, ratingParam, fromReleaseDate, toReleaseDate, language, genre);

        searchResults = sortFilms(request, searchResults);
        request.getSession().setAttribute("searchResults", searchResults);

        response.sendRedirect("view?action=form");
    }

    private static List<Film> performSearch(HttpServletRequest request, String titleParam, String minimumVotesParam, String ratingParam,
                                      String fromReleaseDate, String toReleaseDate, String language, String genre) throws ParseException
    {
        int minimumVotes = minimumVotesParam.length() > 0 ? Integer.valueOf(minimumVotesParam) : 0;

        String[] ratingRange = ratingParam.split("-");
        BigDecimal minimumRating = BigDecimal.ZERO;
        BigDecimal maximumRating = new BigDecimal("10");
        try
        {
            minimumRating = new BigDecimal(ratingRange[0]);
            maximumRating = new BigDecimal(ratingRange[1]);
        }
        catch (Exception e)
        {

        }

        Date fromDate = null;
        Date toDate = null;

        try
        {
            if (fromReleaseDate.length() > 0) fromDate = new SimpleDateFormat("MM/dd/yyyy").parse(fromReleaseDate);
            if (toReleaseDate.length() > 0) toDate = new SimpleDateFormat("MM/dd/yyyy").parse(toReleaseDate);
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
        }

        List<Film> films = OmdbLoader.getFilms();
        List<Film> filteredFilms = new ArrayList<>();
        for (Film film : films)
        {
            if (titleParam.length() > 0)
                if (!film.getTitle().toLowerCase().contains(titleParam.toLowerCase()))
                    continue;

            if (minimumVotes > 0)
                if (Common.stringToInt(film.getImdbVotes()) < minimumVotes)
                    continue;

            if (language.length() > 0)
                if (!film.getLanguage().equals(language))
                    continue;

            if (genre.length() > 0)
                if (!film.getGenre().contains(genre))
                    continue;

            if (fromDate != null || toDate != null)
            {
                Date releaseDate = Common.stringToDate(film.getReleased());
                if (releaseDate == null)
                    continue;

                boolean afterOrEqualsFromDate = fromDate == null || releaseDate.after(fromDate) || releaseDate.equals(fromDate);
                boolean beforeOrEqualsToDate = toDate == null || releaseDate.before(toDate) || releaseDate.equals(toDate);
                boolean releaseDateInRange = afterOrEqualsFromDate && beforeOrEqualsToDate;
                if (!releaseDateInRange)
                    continue;
            }

            if (minimumRating.compareTo(BigDecimal.ZERO) > 0 || maximumRating.compareTo(BigDecimal.TEN) < 0)
            {
                BigDecimal rating = Common.stringToBigDecimal(film.getImdbRating());
                boolean validRating = rating.compareTo(minimumRating) >= 0 && rating.compareTo(maximumRating) <= 0;
                if (!validRating)
                    continue;
            }

            filteredFilms.add(film);
        }

        request.getSession().setAttribute("minimumVotes", minimumVotesParam);
        request.getSession().setAttribute("title", titleParam);
        request.getSession().setAttribute("rating", ratingParam);
        request.getSession().setAttribute("fromReleaseDate", fromReleaseDate);
        request.getSession().setAttribute("toReleaseDate", toReleaseDate);
        request.getSession().setAttribute("language", language);
        request.getSession().setAttribute("genre", genre);
        return filteredFilms;
    }

    private static List<Film> sortFilms(HttpServletRequest request, List<Film> filmsToSort) throws IOException
    {
        final String column = request.getParameter("sortColumn") == null ? "title" : request.getParameter("sortColumn");
        String direction = request.getParameter("sortDirection");
        if (direction == null) direction = "asc";

        List<Film> sortedFilms = new ArrayList<>(filmsToSort);

        sortedFilms.sort(new Comparator<Film>()
        {
            @Override
            public int compare(Film o1, Film o2)
            {
                Object value1 = null;
                Object value2 = null;

                if (column.equals("title"))
                {
                    value1 = o1.getTitle();
                    value2 = o2.getTitle();
                }
                if (column.equals("language"))
                {
                    value1 = o1.getLanguage();
                    value2 = o2.getLanguage();
                }
                if (column.equals("releaseDate"))
                {
                    value1 = Common.stringToDate(o1.getReleased());
                    value2 = Common.stringToDate(o2.getReleased());
                }
                if (column.equals("metascore"))
                {
                    value1 = Common.stringToInt(o1.getMetascore());
                    value2 = Common.stringToInt(o2.getMetascore());
                }
                if (column.equals("comboRating"))
                {
                    value1 = Common.stringToInt(o1.getComboRating());
                    value2 = Common.stringToInt(o2.getComboRating());
                }
                if (column.equals("tomatoMeter"))
                {
                    value1 = Common.stringToInt(o1.getTomatoMeter());
                    value2 = Common.stringToInt(o2.getTomatoMeter());
                }
                if (column.equals("tomatoUserMeter"))
                {
                    value1 = Common.stringToInt(o1.getTomatoUserMeter());
                    value2 = Common.stringToInt(o2.getTomatoUserMeter());
                }
                if (column.equals("imdbVotes"))
                {
                    value1 = Common.stringToInt(o1.getImdbVotes());
                    value2 = Common.stringToInt(o2.getImdbVotes());
                }
                if (column.equals("imdbRating"))
                {
                    value1 = Common.stringToBigDecimal(o1.getImdbRating());
                    value2 = Common.stringToBigDecimal(o2.getImdbRating());
                }

                if (value1 == null && value2 == null) return 0;
                if (value1 == null) return -1;
                if (value2 == null) return 1;

                if (value1 instanceof Integer) return ((Integer)value1).compareTo((Integer) value2);
                if (value1 instanceof String) return ((String)value1).compareToIgnoreCase((String) value2);
                if (value1 instanceof Date) return ((Date)value1).compareTo((Date) value2);
                if (value1 instanceof BigDecimal) return  ((BigDecimal)value1).compareTo((BigDecimal) value2);

                return 0;
            }
        });

        if (direction.equals("desc")) Collections.reverse(sortedFilms);

        request.getSession().setAttribute("sortColumn", column);
        request.getSession().setAttribute("sortDirection", direction);

        return sortedFilms;
    }
}
