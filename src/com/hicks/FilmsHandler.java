package com.hicks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FilmsHandler
{
    public static void showFilms(HttpServletRequest request, HttpServletResponse response)
    {

        List<Film> films = (List<Film>) request.getSession().getAttribute("films");
        if (films == null) performSearch(request, "", "", "");
        films = (List<Film>) request.getSession().getAttribute("films");
        int pages = 1 + ((films.size() - 1) / 100);

        List<Film> filmsOnPage = new ArrayList<>();
        String pageParam = request.getParameter("page");
        if (pageParam == null || Integer.valueOf(pageParam) > pages) pageParam = "1";

        int pageNumber = Integer.valueOf(pageParam);

        int from = (pageNumber - 1) * 100;
        int filmsAfterFrom = films.size() - from;
        int to = filmsAfterFrom < 100 ? from + filmsAfterFrom : from + 100;

        filmsOnPage.addAll(films.subList(from, to));

        request.setAttribute("pages", pages);
        request.setAttribute("page", pageParam);
        request.setAttribute("hasNext", pages > pageNumber);
        request.setAttribute("hasPrevious", pageNumber > 1);
        request.setAttribute("filmsOnPage", filmsOnPage);
    }

    public static void filterFilms(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String titleParam = request.getParameter("title");
        String minimumVotesParam = request.getParameter("minimumVotes");
        String ratingParam = request.getParameter("fldRating");

        performSearch(request, titleParam, minimumVotesParam, ratingParam);

        response.sendRedirect("view?action=index");
    }

    private static void performSearch(HttpServletRequest request, String titleParam, String minimumVotesParam, String ratingParam)
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

        List<Film> filteredFilms = new ArrayList<>();
        for (Film film : FilmImporter.getFilms())
        {
            boolean matchesTitle = film.getTitle().toLowerCase().contains(titleParam.toLowerCase());

            boolean enoughVotes = film.getVotes() >= minimumVotes;

            BigDecimal rating = film.getRating();
            boolean validRating = rating.compareTo(minimumRating) >= 0 && rating.compareTo(maximumRating) <= 0;
            if (matchesTitle && enoughVotes && validRating)
                filteredFilms.add(film);
        }

        request.getSession().setAttribute("minimumVotes", minimumVotesParam);
        request.getSession().setAttribute("title", titleParam);
        request.getSession().setAttribute("rating", ratingParam);
        request.getSession().setAttribute("films", filteredFilms);
    }

    public static void sortFilms(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String column = request.getParameter("column");
        String direction = request.getParameter("direction");
        if (direction == null) direction = "asc";

        List<Film> films = (List<Film>) request.getSession().getAttribute("films");
        List<Film> sortedFilms = new ArrayList<>(films);

        sortFilms(sortedFilms, column);
        if (direction.equals("desc")) Collections.reverse(sortedFilms);

        request.getSession().setAttribute("films", sortedFilms);

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
                if (column.equals("year")) return ((Integer)o1.getYear()).compareTo(o2.getYear());
                if (column.equals("releaseDate")) return o1.getReleaseDate().compareTo(o2.getReleaseDate());
                if (column.equals("votes")) return ((Integer)o1.getVotes()).compareTo(o2.getVotes());
                if (column.equals("rating")) return (o1.getRating()).compareTo(o2.getRating());

                return 0;
            }
        });
    }
}
