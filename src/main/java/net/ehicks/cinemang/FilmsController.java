package net.ehicks.cinemang;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.ehicks.cinemang.beans.Film;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@SessionAttributes({"filmSearchForm", "filmSearchResult"})
public class FilmsController
{
    private static final Logger log = LoggerFactory.getLogger(FilmsController.class);
    private FilmRepository filmRepo;
    private GenreLoader genreLoader;
    private LanguageLoader languageLoader;
    private EntityManager em;

    public FilmsController(FilmRepository filmRepo, GenreLoader genreLoader, LanguageLoader languageLoader, EntityManager em)
    {
        this.filmRepo = filmRepo;
        this.genreLoader = genreLoader;
        this.languageLoader = languageLoader;
        this.em = em;
    }

    // fixes issue when a RequestParam is a null date
    @InitBinder
    protected void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) throws Exception
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        CustomDateEditor editor = new CustomDateEditor(df, true);
        binder.registerCustomEditor(Date.class, editor);
    }

    @ModelAttribute("filmSearchForm")
    public FilmSearchForm filmSearchForm()
    {
        return new FilmSearchForm();
    }

    @ModelAttribute("filmSearchResult")
    public FilmSearchResult filmSearchResult()
    {
        return new FilmSearchResult();
    }

    @GetMapping("/")
    public ModelAndView showFilms(@ModelAttribute("filmSearchForm") FilmSearchForm filmSearchForm,
                                  @ModelAttribute("filmSearchResult") FilmSearchResult filmSearchResult)
    {
        ModelAndView mav = new ModelAndView("index");

        if (filmSearchResult.getSize() == 0)
        {
            filmSearchResult = performSearch(filmSearchForm);
            mav.addObject("filmSearchResult", filmSearchResult);
        }

        mav.addObject("uniqueLanguages", languageLoader.getUniqueLanguages());
        mav.addObject("uniqueGenres", genreLoader.getUniqueGenres());

        return mav;
    }

    @PostMapping("/films/search")
    public ModelAndView handleSearch(
            Model model,
            @RequestParam String title,
            @RequestParam Date fromReleaseDate,
            @RequestParam Date toReleaseDate,
            @RequestParam Integer minimumVotes,
            @RequestParam Double fromRating,
            @RequestParam Double toRating,
            @RequestParam String language,
            @RequestParam String genre,
            @RequestParam String sortColumn,
            @RequestParam String sortDirection,
            @RequestParam Integer page,
            @RequestParam Boolean resetPage
    )
    {
        if (resetPage)
            page = 1;

        FilmSearchForm filmSearchForm = new FilmSearchForm(minimumVotes, title, fromRating, toRating, fromReleaseDate,
                toReleaseDate, language, genre, sortColumn, sortDirection, page);
        FilmSearchResult filmSearchResult = performSearch(filmSearchForm);

        model.addAttribute("filmSearchForm", filmSearchForm);
        model.addAttribute("filmSearchResult", filmSearchResult);

        return new ModelAndView("redirect:/");
    }

    @GetMapping(value = "/films", produces = "application/json")
    @ResponseBody
    public ModelAndView showAllJson(@ModelAttribute("filmSearchForm") FilmSearchForm filmSearchForm,
                              @RequestParam(required = false) Integer page,
                              @RequestParam(required = false) String sortColumn,
                              @RequestParam(required = false) String sortDirection)
    {
        if (page != null) filmSearchForm.setPage(page);
        if (sortColumn != null) filmSearchForm.setSortColumn(sortColumn);
        if (sortDirection != null) filmSearchForm.setSortDirection(sortDirection);
        FilmSearchResult filmSearchResult = performSearch(filmSearchForm);

        ModelAndView mav = new ModelAndView("filmList :: filmList");
        mav.addObject("filmSearchForm", filmSearchForm);
        mav.addObject("filmSearchResult", filmSearchResult);

        return mav;
    }

    @GetMapping(value = "/films/titles", produces = "application/json")
    @ResponseBody
    public String getAjaxTitles(@RequestParam String term) throws IOException
    {
        FilmSearchForm filmSearchForm = new FilmSearchForm();
        filmSearchForm.setTitle(term);
        filmSearchForm.setSortColumn("imdbVotes");

        FilmSearchResult filmSearchResult = performSearch(filmSearchForm);

        List<Film> films = filmSearchResult.getSearchResults();
        if (films.size() > 10)
            films = films.subList(0, 10);

        List<String> titles = films.stream().map(Film::getTitle).collect(Collectors.toList());

        ObjectMapper mapper = new ObjectMapper();

        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.writeValue(out, titles);

        final byte[] data = out.toByteArray();

        return new String(data);
    }

    public class FilmTitleSerializer extends StdSerializer<Film>
    {
        public FilmTitleSerializer()
        {
            this(null);
        }

        public FilmTitleSerializer(Class<Film> t)
        {
            super(t);
        }

        @Override
        public void serialize(Film value, JsonGenerator jgen, SerializerProvider provider) throws IOException
        {

            jgen.writeStartObject();
            jgen.writeStringField("title", value.getTitle());
            jgen.writeEndObject();
        }
    }

    private FilmSearchResult performSearch(FilmSearchForm filmSearchForm)
    {
        return criteriaBuilder(filmSearchForm.getTitle(), filmSearchForm.getLanguage(),
                filmSearchForm.getGenre(), filmSearchForm.getMinVotes(), filmSearchForm.getFromRating(), filmSearchForm.getToRating(),
                filmSearchForm.getFromReleaseDate(), filmSearchForm.getToReleaseDate(), filmSearchForm.getSortColumn(),
                filmSearchForm.getSortDirection(), filmSearchForm.getPage());
    }

    private FilmSearchResult criteriaBuilder(String title, String language, String genre, Integer minVotes, Double minRating,
                                             Double maxRating, Date fromDate, Date toDate, String sortColumn, String sortDirection, Integer page)
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Film> query = cb.createQuery(Film.class);
        Root<Film> filmRoot = query.from(Film.class);

        List<Predicate> predicates = new ArrayList<>();

        if (title.length() > 0)
            predicates.add(cb.like(cb.lower(filmRoot.get("title")), "%" + title.toLowerCase() + "%"));
        if (language.length() > 0)
            predicates.add(cb.equal(filmRoot.get("language"), language));
        if (genre.length() > 0)
            predicates.add(cb.like(cb.lower(filmRoot.get("genre")), "%" + genre.toLowerCase() + "%"));
        if (minVotes != null && minVotes > 0)
            predicates.add(cb.greaterThanOrEqualTo(filmRoot.get("imdbVotes"), minVotes));
        if (fromDate != null)
            predicates.add(cb.greaterThanOrEqualTo(filmRoot.get("released"), fromDate));
        if (toDate != null)
            predicates.add(cb.lessThanOrEqualTo(filmRoot.get("released"), toDate));
        if (minRating != null && minRating > 0)
            predicates.add(cb.greaterThanOrEqualTo(filmRoot.get("cinemangRating"), minRating));
        if (maxRating != null && maxRating < 100)
            predicates.add(cb.lessThanOrEqualTo(filmRoot.get("cinemangRating"), maxRating));

        List<Order> orderList = new ArrayList<>();

        if (sortDirection.equals("asc"))
            orderList.add(cb.asc(filmRoot.get(sortColumn)));
        else
            orderList.add(cb.desc(filmRoot.get(sortColumn)));

        query.select(filmRoot)
                .where(predicates.toArray(new Predicate[]{}))
                .orderBy(orderList);

        TypedQuery<Film> typedQuery = em.createQuery(query);
        long start = System.currentTimeMillis();
        List<Film> results = typedQuery
                .setFirstResult((page - 1) * 100)
                .setMaxResults(100)
                .getResultList();
        log.info("query time: " + (System.currentTimeMillis() - start));

        // count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Film.class)))
                .where(predicates.toArray(new Predicate[]{}));

        TypedQuery<Long> typedCountQuery = em.createQuery(countQuery);
        long size = typedCountQuery.getSingleResult();

        return new FilmSearchResult(page, results, size);
    }
}
