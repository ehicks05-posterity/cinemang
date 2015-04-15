package com.hicks;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class FilmsHandler
{
    public static void showFilms(HttpServletRequest request, HttpServletResponse response)
    {
        List<Film> films = (List<Film>) request.getSession().getAttribute("films");
        if (films == null) films = new ArrayList<>();
        request.getSession().setAttribute("films", films);
    }

    public static void filterFilms(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String minimumVotesParam = request.getParameter("minimumVotes");
        int minimumVotes = minimumVotesParam.length() > 0 ? Integer.valueOf(minimumVotesParam) : 0;

        String minimumRatingParam = request.getParameter("minimumRating");
        BigDecimal minimumRating = BigDecimal.ZERO;
        try
        {
            minimumRating = new BigDecimal(minimumRatingParam);
        }
        catch (Exception e)
        {

        }

        List<Film> filteredFilms = new ArrayList<>();
        for (Film film : FilmImporter.getFilms())
        {
            boolean enoughVotes = film.getVotes() >= minimumVotes;
            boolean enoughRating = film.getRating().compareTo(minimumRating) >= 0;
            if (enoughVotes && enoughRating)
                filteredFilms.add(film);
        }

        request.getSession().setAttribute("films", filteredFilms);

        response.sendRedirect("view?action=index&minimumVotes=" + minimumVotes + "&minimumRating=" + minimumRating);
    }
}
