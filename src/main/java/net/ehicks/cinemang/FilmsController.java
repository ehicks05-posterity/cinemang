package net.ehicks.cinemang;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import net.ehicks.cinemang.beans.*;
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
import java.time.LocalDate;
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
    private GenreRepository genreRepository;
    private LanguageRepository languageRepository;
    private EntityManager em;

    public FilmsController(FilmRepository filmRepo, GenreRepository genreRepository,
                           LanguageRepository languageRepository, EntityManager em)
    {
        this.filmRepo = filmRepo;
        this.genreRepository = genreRepository;
        this.languageRepository = languageRepository;
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

        mav.addObject("filmSearchForm", filmSearchForm);
        mav.addObject("languages", languageRepository.findByOrderByCountDesc());
        mav.addObject("genres", genreRepository.findAll());
        mav.addObject("filmCount", filmRepo.count());

        return mav;
    }

    @PostMapping("/films/search")
    public ModelAndView handleSearch(Model model,
                                     @ModelAttribute("filmSearchForm") FilmSearchForm filmSearchForm,
                                     @ModelAttribute("filmSearchResult") FilmSearchResult filmSearchResult)
    {
        model.addAttribute("filmSearchForm", filmSearchForm);
        model.addAttribute("filmSearchResult", performSearch(filmSearchForm));
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

        ModelAndView mav = filmSearchForm.getResultView().equals("filmList") ? new ModelAndView("filmList :: filmList") : new ModelAndView("filmMediaItems :: filmMediaItems");
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
        filmSearchForm.setSortColumn("userVoteCount");

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
        return criteriaBuilder(filmSearchForm);
    }

    private FilmSearchResult criteriaBuilder(FilmSearchForm form)
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Film> query = cb.createQuery(Film.class);
        Root<Film> filmRoot = query.from(Film.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(cb.isNotNull(filmRoot.get("released")));
        predicates.add(cb.isNotNull(filmRoot.get("imdbId")));
        predicates.add(cb.le(cb.length(filmRoot.get("title")), 255));

        if (form.getTitle().length() > 0)
            predicates.add(cb.like(cb.lower(filmRoot.get("title")), "%" + form.getTitle().toLowerCase() + "%"));
        if (form.getLanguage() != null)
            predicates.add(cb.equal(filmRoot.get("language").get("id"), form.getLanguage()));
        if (form.getGenre() != null)
        {
            predicates.add(cb.isMember(form.getGenre(), filmRoot.get("genres")));
        }
        if (form.getFromVotes() != null && form.getFromVotes() > 0)
            predicates.add(cb.greaterThanOrEqualTo(filmRoot.get("userVoteCount"), form.getFromVotes()));
        if (form.getToVotes() != null && form.getToVotes() > 0)
            predicates.add(cb.lessThanOrEqualTo(filmRoot.get("userVoteCount"), form.getToVotes()));
        if (form.getFromReleaseDate() != null)
            predicates.add(cb.greaterThanOrEqualTo(filmRoot.get("released"), form.getFromReleaseDate()));
        if (form.getToReleaseDate() != null)
            predicates.add(cb.lessThanOrEqualTo(filmRoot.get("released"), form.getToReleaseDate()));
        if (form.getFromRating() != null && form.getFromRating() > 0)
            predicates.add(cb.greaterThanOrEqualTo(filmRoot.get("userVoteAverage"), form.getFromRating()));
        if (form.getToRating() != null && form.getToRating() < 10)
            predicates.add(cb.lessThanOrEqualTo(filmRoot.get("userVoteAverage"), form.getToRating()));

        List<Order> orderList = new ArrayList<>();

        if (form.getSortDirection().equals("asc"))
            orderList.add(cb.asc(filmRoot.get(form.getSortColumn())));
        else
            orderList.add(cb.desc(filmRoot.get(form.getSortColumn())));
        
        orderList.add(cb.asc(filmRoot.get("tmdbId")));

        query.select(filmRoot)
                .where(predicates.toArray(new Predicate[]{}))
                .orderBy(orderList);

        TypedQuery<Film> typedQuery = em.createQuery(query);
        long start = System.currentTimeMillis();
        List<Film> results = typedQuery
                .setFirstResult((form.getPage() - 1) * form.getPageSize())
                .setMaxResults(form.getPageSize())
                .getResultList();
        log.info("query time: " + (System.currentTimeMillis() - start));

        // count query
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        countQuery.select(cb.count(countQuery.from(Film.class)))
                .where(predicates.toArray(new Predicate[]{}));

        TypedQuery<Long> typedCountQuery = em.createQuery(countQuery);
        long size = typedCountQuery.getSingleResult();

        return new FilmSearchResult(form.getPage(), results, size, form.getPageSize());
    }
}
