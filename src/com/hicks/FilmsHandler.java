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
    public static String showFilms(HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException
    {
        List<Film> searchResults = (List<Film>) request.getSession().getAttribute("searchResults");
        if (searchResults == null)
            searchResults = performInitialSearch(request);

        request.getSession().setAttribute("searchResults", searchResults);

        request.setAttribute("searchResultsSize", new DecimalFormat("#,###").format(searchResults.size()));
        request.setAttribute("uniqueLanguages", OmdbLoader.getUniqueLanguages());
        request.setAttribute("uniqueGenres", OmdbLoader.getUniqueGenres());

        return "filmsList.jsp";
    }

    private static List<Film> performInitialSearch(HttpServletRequest request) throws ParseException, IOException
    {
        // set some defaults
        String minimumVotes = "1000";
        String imdbRating = "0-10";
        String language = "English";

        request.getSession().setAttribute("minimumVotes", minimumVotes);
        request.getSession().setAttribute("rating", imdbRating);
        request.getSession().setAttribute("language", language);

        return performSearch(request, "", minimumVotes, imdbRating, "", "", language, "");
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

        response.sendRedirect("view?action=form");
    }

    private static List<Film> performSearch(HttpServletRequest request, String titleParam, String minimumVotesParam, String ratingParam,
                                      String fromReleaseDate, String toReleaseDate, String language, String genre) throws ParseException, IOException
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

        Map<String, Object> args = new HashMap<>();
        String query = "select f from Film f where ";
        String whereClause = "";

        if (titleParam.length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " lower(f.title) like :title ";
            args.put("title", titleParam.toLowerCase().replaceAll("\\*","%"));
        }

        if (minimumVotes > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.imdbVotes >= :minimumVotes ";
            args.put("minimumVotes", minimumVotes);
        }

        if (language.length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.language = :language ";
            args.put("language", language);
        }

        if (genre.length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.genre like :genre ";
            args.put("genre", "%" + genre + "%");
        }

        if (fromDate != null)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.released >= :fromDate ";
            args.put("fromDate", fromDate);
        }
        if (toDate != null)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.released <= :toDate ";
            args.put("toDate", toDate);
        }

        if (minimumRating.compareTo(BigDecimal.ZERO) > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.imdbRating >= :minimumRating ";
            args.put("minimumRating", minimumRating);
        }
        if (maximumRating.compareTo(BigDecimal.TEN) < 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.imdbRating <= :maximumRating ";
            args.put("maximumRating", maximumRating);
        }

        if (args.size() == 0) query = query.replace("where", "");

        // sort
        final String column = request.getParameter("sortColumn") == null ? "title" : request.getParameter("sortColumn");
        String direction = request.getParameter("sortDirection");
        if (direction == null) direction = "asc";
        String orderByClause = "";
        if (column.length() > 0)
        {
            orderByClause += " order by f." + column + " " + direction + " nulls last " ;
        }

        // find out size of search results
        String countQuery = query.replace("select f", "select count(f)");
        Long countResult = (Long) Hibernate.executeQuerySingleResult(countQuery + whereClause, args);
        int searchResultsSize = countResult.intValue();
        int pages = 1 + ((searchResultsSize - 1) / 100);

        String pageParam = request.getParameter("page");
        String resetPage = Common.getSafeString(request.getParameter("resetPage"));
        if (pageParam == null || Integer.valueOf(pageParam) > pages || resetPage.equals("yes")) pageParam = "1";

        int pageNumber = Integer.valueOf(pageParam);
        int from = (pageNumber - 1) * 100;

        List<Film> filteredFilms = Hibernate.executeQuery(query + whereClause + orderByClause, args, from, 100);

        request.getSession().setAttribute("sortColumn", column);
        request.getSession().setAttribute("sortDirection", direction);

        request.getSession().setAttribute("minimumVotes", minimumVotesParam);
        request.getSession().setAttribute("title", titleParam);
        request.getSession().setAttribute("rating", ratingParam);
        request.getSession().setAttribute("fromReleaseDate", fromReleaseDate);
        request.getSession().setAttribute("toReleaseDate", toReleaseDate);
        request.getSession().setAttribute("language", language);
        request.getSession().setAttribute("genre", genre);

        request.getSession().setAttribute("pages", pages);
        request.getSession().setAttribute("page", pageParam);
        request.getSession().setAttribute("hasNext", pages > pageNumber);
        request.getSession().setAttribute("hasPrevious", pageNumber > 1);
        request.getSession().setAttribute("searchResultsSize", new DecimalFormat("#,###").format(searchResultsSize));
        request.getSession().setAttribute("searchResults", filteredFilms);
        return filteredFilms;
    }
}
