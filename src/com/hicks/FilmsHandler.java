package com.hicks;

import com.hicks.beans.Film;
import com.hicks.orm.EOI;
import com.hicks.orm.SQLGenerator;
import org.apache.commons.lang3.StringEscapeUtils;

import javax.imageio.ImageIO;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class FilmsHandler
{
    public static String showFilms(HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException
    {
        FilmSearchResult filmSearchResult = (FilmSearchResult) request.getSession().getAttribute("filmSearchResult");
        if (filmSearchResult == null)
        {
            // set some defaults
            FilmsForm filmsForm = new FilmsForm("1000", "", "0-100", "", "", "English", "");
            request.getSession().setAttribute("filmsForm", filmsForm);

            filmSearchResult = performSearch(request, filmsForm);
            request.getSession().setAttribute("filmSearchResult", filmSearchResult);
        }

        request.setAttribute("uniqueLanguages", LanguageLoader.getUniqueLanguages());
        request.setAttribute("uniqueGenres", GenreLoader.getUniqueGenres());

        return "/WEB-INF/webroot/filmsList.jsp";
    }

    public static void getPoster(HttpServletRequest request, HttpServletResponse response) throws ParseException, IOException
    {
        String imdbId = request.getParameter("imdbId");
        URL url = new URL("http://img.omdbapi.com?apikey=9011ed1e&i=" + imdbId);
        byte[] imageResponse;

        try
        {
            imageResponse = IOUtil.getBytesFromUrlConnection(url);
            boolean transparent = Common.getSafeString(request.getParameter("transparent")).equals("true");
            if (transparent)
                imageResponse = getTransparentPoster(imageResponse);
        }
        catch (Exception e)
        {
            System.out.println("No poster found for " + imdbId);
            return;
        }

        String base64Image = Base64.getEncoder().encodeToString(imageResponse);
        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.print("data:image/jpeg;base64," + base64Image);
    }

    private static byte[] getTransparentPoster(byte[] imageData) throws ParseException, IOException
    {
        BufferedImage img = ImageIO.read(new ByteArrayInputStream(imageData));
        double scaling = 2;
        int scaledWidth = (int) (img.getWidth() * scaling);
        int scaledHeight = (int) (img.getHeight() * scaling);
        BufferedImage tran = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);

        float opacity = 0.1f;
        Graphics2D g2d = tran.createGraphics();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        g2d.scale(scaling, scaling);
        g2d.drawImage(img, null, 0, 0);
        g2d.dispose();

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(tran, "png", os);

        return os.toByteArray();
    }

    public static void filterFilms(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
    {
        String title = request.getParameter("title");
        String minimumVotes = request.getParameter("minimumVotes");
        String rating = request.getParameter("fldRating");
        String fromReleaseDate = request.getParameter("fromReleaseDateDatepicker");
        String toReleaseDate = request.getParameter("toReleaseDateDatepicker");
        String language = request.getParameter("language");
        String genre = request.getParameter("fldGenre");

        FilmsForm filmsForm = new FilmsForm(minimumVotes, title, rating, fromReleaseDate, toReleaseDate, language, genre);
        if (request.getParameter("resetPage").equals("yes"))
            filmsForm.setPage("1");

        FilmSearchResult filmSearchResult = performSearch(request, filmsForm);
        request.getSession().setAttribute("filmsForm", filmsForm);
        request.getSession().setAttribute("filmSearchResult", filmSearchResult);

        response.sendRedirect("view?action=form");
    }

    public static void ajaxGetNewPage(HttpServletRequest request, HttpServletResponse response) throws IOException, ParseException
    {
        String newPage = request.getParameter("page");
        String newSortColumn = request.getParameter("sortColumn");
        String newSortDirection = request.getParameter("sortDirection");

        FilmSearchResult filmSearchResult;
        if (SystemInfo.isLoadDbToRam())
        {
            filmSearchResult = (FilmSearchResult) request.getSession().getAttribute("filmSearchResult");

            boolean resort = (newSortColumn != null && !newSortColumn.equals(filmSearchResult.getSortColumn())) ||
                    (newSortDirection != null && !newSortDirection.equals(filmSearchResult.getSortDirection()));

            if (newPage != null) filmSearchResult.setPage(newPage);
            if (newSortColumn != null) filmSearchResult.setSortColumn(newSortColumn);
            if (newSortDirection != null) filmSearchResult.setSortDirection(newSortDirection);

            if (resort)
                filmSearchResult.setSearchResults(sortFilmsInMemory(filmSearchResult.getSearchResults(), filmSearchResult.getSortColumn(), filmSearchResult.getSortDirection()));
        }
        else
        {
            FilmsForm filmsForm = (FilmsForm) request.getSession().getAttribute("filmsForm");
            if (newSortColumn != null) filmsForm.setSortColumn(newSortColumn);
            if (newSortDirection != null) filmsForm.setSortDirection(newSortDirection);
            if (newPage != null) filmsForm.setPage(newPage);
            request.getSession().setAttribute("filmsForm", filmsForm);

            filmSearchResult = performSearch(request, filmsForm);
        }

        request.getSession().setAttribute("filmSearchResult", filmSearchResult);

        JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

        for (Film film : filmSearchResult.getPageOfResults())
        {
            String title = film.getTitle();
            if (title.length() > 50)
                title = title.substring(0, 50);

            String released = "";
            if (film.getReleased() != null)
                released = new SimpleDateFormat("yyyy").format(film.getReleased());

            JsonObject jsonObject = Json.createObjectBuilder()
                    .add("imdbId", film.getImdbID())
                    .add("title", escapeHtml(title))
                    .add("prettyPlot", escapeHtml(film.getPrettyPlot()))
                    .add("director", escapeHtml(film.getDirector()))
                    .add("actors", escapeHtml(film.getActors()))
                    .add("runtime", film.getRuntime())
                    .add("tomatoConsensus", escapeHtml(film.getTomatoConsensus()))
                    .add("tomatoImage", film.getTomatoImage())
                    .add("cinemangRating", film.getCinemangRating() == null ? 0 : film.getCinemangRating())
                    .add("tomatoMeter", film.getTomatoMeter() == null ? 0 : film.getTomatoMeter())
                    .add("tomatoUserMeter", film.getTomatoUserMeter() == null ? 0 : film.getTomatoUserMeter())
                    .add("imdbRating", film.getImdbRating() == null ? BigDecimal.ZERO : film.getImdbRating())
                    .add("released", released)
                    .add("imdbVotes", new DecimalFormat("#,###").format(film.getImdbVotes()))
                    .add("language", film.getLanguage())
                    .add("genre", film.getGenre())
                    .build();
            jsonArrayBuilder.add(jsonObject);
        }

        JsonArray jsonArray = jsonArrayBuilder.build();
        response.getOutputStream().print(jsonArray.toString());
    }

    private static String escapeHtml(String input)
    {
        return StringEscapeUtils.escapeHtml4((input));
    }

    private static FilmSearchResult performSearch(HttpServletRequest request, FilmsForm filmsForm) throws ParseException, IOException
    {
        // parse request fields
        int minimumVotes = filmsForm.getMinimumVotesParam().length() > 0 ? Integer.valueOf(filmsForm.getMinimumVotesParam()) : 0;

        String[] ratingRange = filmsForm.getRatingParam().split("-");
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
            if (filmsForm.getFromReleaseDate().length() > 0) fromDate = new SimpleDateFormat("MM/dd/yyyy").parse(filmsForm.getFromReleaseDate());
            if (filmsForm.getToReleaseDate().length() > 0) toDate = new SimpleDateFormat("MM/dd/yyyy").parse(filmsForm.getToReleaseDate());
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
        }

        // parse sorting fields
        String sortColumn = filmsForm.getSortColumn();
        String sortDirection = filmsForm.getSortDirection();
        if (sortColumn == null)
        {
            if (request.getParameter("sortColumn") == null)
            {
                sortColumn = "cinemang_rating";
                sortDirection = "desc";
            }
            else
                sortColumn = request.getParameter("sortColumn");
        }

        String directionParam = request.getParameter("sortDirection");
        if (sortDirection == null) sortDirection = directionParam == null ? "asc" : directionParam;

        boolean loadFromDb = true;
        boolean loadFromMemory = !loadFromDb;

        String page = filmsForm.getPage();
        if (page == null)
        {
            page = request.getParameter("page");
            if (page == null) page = "1";
        }

        List<Film> filteredFilms = new ArrayList<>();
        if (loadFromDb)
        {
            SQLQuery filmQuery = buildFilmSQLQuery(filmsForm, minimumVotes, fromDate, toDate, minimumRating, maximumRating, sortColumn, sortDirection, page);
            String countVersionOfQuery = SQLGenerator.getCountVersionOfQuery(filmQuery.queryString);

            List result = EOI.executeQueryOneResult(countVersionOfQuery, filmQuery.args);
            long resultSize = (Long) result.get(0);
            // build count(*) query
            filteredFilms = EOI.executeQuery(filmQuery.queryString, filmQuery.args);
            return new FilmSearchResult(page, filteredFilms, sortColumn, sortDirection, resultSize);
        }

        if (loadFromMemory)
        {
            filteredFilms = searchFilmsInMemory(filmsForm.getTitleParam(), filmsForm.getLanguage(), filmsForm.getGenre(), minimumVotes, minimumRating, maximumRating, fromDate, toDate);
            filteredFilms = sortFilmsInMemory(filteredFilms, sortColumn, sortDirection);
        }

        return new FilmSearchResult(page, filteredFilms, sortColumn, sortDirection);
    }

    private static class SQLQuery
    {
        public String queryString;
        public List<Object> args = new ArrayList<>();

        public SQLQuery(String queryString, List<Object> args)
        {
            this.queryString = queryString;
            this.args = args;
        }
    }

    private static class HQLQuery
    {
        public String queryString;
        public Map<String, Object> args = new HashMap<>();

        public HQLQuery(String queryString, Map<String, Object> args)
        {
            this.queryString = queryString;
            this.args = args;
        }
    }

    private static SQLQuery buildFilmSQLQuery(FilmsForm filmsForm, int minimumVotes, Date fromDate, Date toDate, BigDecimal minimumRating,
                                              BigDecimal maximumRating, String sortColumn, String sortDirection, String page)
    {
        List<Object> args = new ArrayList<>();
        String selectClause = "select * from films where ";
        String whereClause = "";

        if (filmsForm.getTitleParam().length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " lower(title) like ? ";
            args.add(filmsForm.getTitleParam().toLowerCase().replaceAll("\\*","%"));
        }

        if (minimumVotes > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " imdb_votes >= ? ";
            args.add(minimumVotes);
        }

        if (filmsForm.getLanguage().length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " language = ? ";
            args.add(filmsForm.getLanguage());
        }

        if (filmsForm.getGenre().length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " genre like ? ";
            args.add("%" + filmsForm.getGenre() + "%");
        }

        if (fromDate != null)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " released >= ? ";
            args.add(fromDate);
        }
        if (toDate != null)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " released <= ? ";
            args.add(toDate);
        }

        if (minimumRating.compareTo(BigDecimal.ZERO) > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " imdb_rating >= ? ";
            args.add(minimumRating);
        }
        if (maximumRating.compareTo(BigDecimal.TEN) < 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " imdb_rating <= ? ";
            args.add(maximumRating);
        }

        if (args.size() == 0) selectClause = selectClause.replace("where", "");

        String orderByClause = "";
        if (sortColumn.length() > 0)
        {
            orderByClause += " order by " + sortColumn + " " + sortDirection + ", imdb_id nulls last " ;
        }

        String limit = "100";
        String offset = String.valueOf((Integer.valueOf(page) - 1) * 100);
        String paginationClause = " limit " + limit + " offset " + offset;

        String completeQuery = selectClause + whereClause + orderByClause + paginationClause;
        return new SQLQuery(completeQuery, args);
    }

    private static HQLQuery buildFilmSQLQueryHQL(FilmsForm filmsForm, int minimumVotes, Date fromDate, Date toDate, BigDecimal minimumRating, BigDecimal maximumRating, String sortColumn, String sortDirection)
    {
        Map<String, Object> args = new HashMap<>();
        String selectClause = "select f from Film f where ";
        String whereClause = "";

        if (filmsForm.getTitleParam().length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " lower(f.title) like :title ";
            args.put("title", filmsForm.getTitleParam().toLowerCase().replaceAll("\\*","%"));
        }

        if (minimumVotes > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.imdbVotes >= :minimumVotes ";
            args.put("minimumVotes", minimumVotes);
        }

        if (filmsForm.getLanguage().length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.language = :language ";
            args.put("language", filmsForm.getLanguage());
        }

        if (filmsForm.getGenre().length() > 0)
        {
            if (whereClause.length() > 0) whereClause += " and ";
            whereClause += " f.genre like :genre ";
            args.put("genre", "%" + filmsForm.getGenre() + "%");
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

        if (args.size() == 0) selectClause = selectClause.replace("where", "");

        String orderByClause = "";
        if (sortColumn.length() > 0)
        {
            orderByClause += " order by f." + sortColumn + " " + sortDirection + " nulls last " ;
        }

        String completeQuery = selectClause + whereClause + orderByClause;
        return new HQLQuery(completeQuery, args);
    }

    private static List<Film> searchFilmsInMemory(String titleParam, String language, String genre, int minimumVotes, BigDecimal minimumRating, BigDecimal maximumRating, Date fromDate, Date toDate)
    {
        List<Film> filteredFilms = new ArrayList<>();
        for (Film film : FilmLoader.getFilms())
        {
            if (titleParam.length() > 0)
                if (!film.getTitle().toLowerCase().contains(titleParam.toLowerCase()))
                    continue;

            if (minimumVotes > 0)
                if (film.getImdbVotes() != null && film.getImdbVotes() < minimumVotes)
                    continue;

            if (language.length() > 0)
                if (!film.getLanguage().equals(language))
                    continue;

            if (genre.length() > 0)
                if (!film.getGenre().contains(genre))
                    continue;

            if (fromDate != null || toDate != null)
            {
                Date releaseDate = film.getReleased();
                if (releaseDate == null)
                    continue;

                boolean afterOrEqualsFromDate = fromDate == null || releaseDate.after(fromDate) || releaseDate.equals(fromDate);
                boolean beforeOrEqualsToDate = toDate == null || releaseDate.before(toDate) || releaseDate.equals(toDate);
                boolean releaseDateInRange = afterOrEqualsFromDate && beforeOrEqualsToDate;
                if (!releaseDateInRange)
                    continue;
            }

            if (minimumRating.compareTo(BigDecimal.ZERO) > 0 || maximumRating.compareTo(BigDecimal.valueOf(100)) < 0)
            {
                int cinemangRating = film.getCinemangRating() == null ? 0 : film.getCinemangRating();
                BigDecimal rating = new BigDecimal(cinemangRating);
                boolean validRating = rating.compareTo(minimumRating) >= 0 && rating.compareTo(maximumRating) <= 0;
                if (!validRating)
                    continue;
            }

            filteredFilms.add(film);
        }

        return filteredFilms;
    }

    private static List<Film> sortFilmsInMemory(List<Film> filmsToSort, String _sortColumn, String sortDirection) throws IOException
    {
        final String sortColumn = _sortColumn == null ? "title" : _sortColumn;
        if (sortDirection == null) sortDirection = "asc";

        List<Film> sortedFilms = new ArrayList<>(filmsToSort);

        sortedFilms.sort(new Comparator<Film>()
        {
            @Override
            public int compare(Film o1, Film o2)
            {
                Object value1 = null;
                Object value2 = null;

                if (sortColumn.equals("title"))
                {
                    value1 = o1.getTitle();
                    value2 = o2.getTitle();
                }
                if (sortColumn.equals("language"))
                {
                    value1 = o1.getLanguage();
                    value2 = o2.getLanguage();
                }
                if (sortColumn.equals("released"))
                {
                    value1 = o1.getReleased();
                    value2 = o2.getReleased();
                }
                if (sortColumn.equals("metascore"))
                {
                    value1 = o1.getMetascore();
                    value2 = o2.getMetascore();
                }
                if (sortColumn.equals("cinemangRating") || sortColumn.equals("cinemang_rating"))
                {
                    value1 = o1.getCinemangRating();
                    value2 = o2.getCinemangRating();
                }
                if (sortColumn.equals("tomatoMeter") || sortColumn.equals("tomato_meter"))
                {
                    value1 = o1.getTomatoMeter();
                    value2 = o2.getTomatoMeter();
                }
                if (sortColumn.equals("tomatoUserMeter") || sortColumn.equals("tomato_user_meter"))
                {
                    value1 = o1.getTomatoUserMeter();
                    value2 = o2.getTomatoUserMeter();
                }
                if (sortColumn.equals("imdbVotes") || sortColumn.equals("imdb_votes"))
                {
                    value1 = o1.getImdbVotes();
                    value2 = o2.getImdbVotes();
                }
                if (sortColumn.equals("imdbRating") || sortColumn.equals("imdb_rating"))
                {
                    value1 = o1.getImdbRating();
                    value2 = o2.getImdbRating();
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

        if (sortDirection.equals("desc")) Collections.reverse(sortedFilms);

        return sortedFilms;
    }
}
