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
    private static List<Film> searchResults = new ArrayList<>();

    public static void showFilms(HttpServletRequest request, HttpServletResponse response) throws ParseException
    {

        List<Film> films = searchResults;
        if (films.size() == 0) performSearch(request, "", "", "", "", "", "English");
        films = searchResults;
        int pages = 1 + ((films.size() - 1) / 100);

        List<Film> filmsOnPage = new ArrayList<>();
        String pageParam = request.getParameter("page");
        if (pageParam == null || Integer.valueOf(pageParam) > pages) pageParam = "1";

        int pageNumber = Integer.valueOf(pageParam);

        int from = (pageNumber - 1) * 100;
        int filmsAfterFrom = films.size() - from;
        int to = filmsAfterFrom < 100 ? from + filmsAfterFrom : from + 100;

        filmsOnPage.addAll(films.subList(from, to));

        request.setAttribute("uniqueLanguages", FilmImporter.getUniqueLanguages());
        request.setAttribute("pages", pages);
        request.setAttribute("page", pageParam);
        request.setAttribute("hasNext", pages > pageNumber);
        request.setAttribute("hasPrevious", pageNumber > 1);
        request.setAttribute("filmsOnPage", filmsOnPage);
    }

    public static void filterFilms(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
    {
        String titleParam = request.getParameter("title");
        String minimumVotesParam = request.getParameter("minimumVotes");
        String ratingParam = request.getParameter("fldRating");
        String fromReleaseDate = request.getParameter("fromReleaseDateDatepicker");
        String toReleaseDate = request.getParameter("toReleaseDateDatepicker");
        String language = request.getParameter("language");

        performSearch(request, titleParam, minimumVotesParam, ratingParam, fromReleaseDate, toReleaseDate, language);

        response.sendRedirect("view?action=index");
    }

    private static void performSearch(HttpServletRequest request, String titleParam, String minimumVotesParam, String ratingParam, String fromReleaseDate, String toReleaseDate, String language) throws ParseException
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
            if (fromReleaseDate.length() > 0) fromDate = new SimpleDateFormat("mm/dd/YYYY").parse(fromReleaseDate);
            if (toReleaseDate.length() > 0) toDate = new SimpleDateFormat("mm/dd/YYYY").parse(toReleaseDate);
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
        }

        List<Film> filteredFilms = new ArrayList<>();
        for (Film film : FilmImporter.getFilms())
        {
            boolean matchesTitle = film.getTitle().toLowerCase().contains(titleParam.toLowerCase());

            boolean enoughVotes = film.getVotes() >= minimumVotes;

            boolean matchesLanguage = language.length() == 0 || film.getLanguage().equals(language);

            Date releaseDate = film.getReleaseDate();
            boolean afterOrEqualsFromDate = fromDate == null || releaseDate.after(fromDate) || releaseDate.equals(fromDate);
            boolean beforeOrEqualsToDate = toDate == null || releaseDate.before(toDate) || releaseDate.equals(toDate);
            boolean releaseDateInRange = afterOrEqualsFromDate && beforeOrEqualsToDate;

            BigDecimal rating = film.getRating();
            boolean validRating = rating.compareTo(minimumRating) >= 0 && rating.compareTo(maximumRating) <= 0;
            if (matchesTitle && enoughVotes && validRating && releaseDateInRange && matchesLanguage)
                filteredFilms.add(film);
        }

        String filmsCount = new DecimalFormat("#,###").format(filteredFilms.size());

        request.getSession().setAttribute("minimumVotes", minimumVotesParam);
        request.getSession().setAttribute("title", titleParam);
        request.getSession().setAttribute("rating", ratingParam);
        request.getSession().setAttribute("fromReleaseDate", fromReleaseDate);
        request.getSession().setAttribute("toReleaseDate", toReleaseDate);
        request.getSession().setAttribute("language", language);
        request.getSession().setAttribute("filmsCount", filmsCount);
        searchResults = filteredFilms;
    }

    public static void sortFilms(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String column = request.getParameter("column");
        String direction = request.getParameter("direction");
        if (direction == null) direction = "asc";

        List<Film> films = searchResults;
        List<Film> sortedFilms = new ArrayList<>(films);

        sortFilms(sortedFilms, column);
        if (direction.equals("desc")) Collections.reverse(sortedFilms);

        searchResults = sortedFilms;

        response.sendRedirect("view?action=index&column=" + column + "&direction=" + direction);
    }

    private static void sortFilms(List<Film> films, final String column)
    {
        films.sort(new Comparator<Film>()
        {
            @Override
            public int compare(Film o1, Film o2)
            {
                if (column.equals("title")) return o1.getTitle().compareToIgnoreCase(o2.getTitle());
                if (column.equals("releaseDate")) return o1.getReleaseDate().compareTo(o2.getReleaseDate());
                if (column.equals("votes")) return ((Integer)o1.getVotes()).compareTo(o2.getVotes());
                if (column.equals("rating")) return (o1.getRating()).compareTo(o2.getRating());
                if (column.equals("language")) return (o1.getLanguage()).compareTo(o2.getLanguage());

                return 0;
            }
        });
    }
}
