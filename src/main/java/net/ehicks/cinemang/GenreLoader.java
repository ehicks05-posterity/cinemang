package net.ehicks.cinemang;

import net.ehicks.cinemang.beans.Film;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

@Configuration
public class GenreLoader
{
    private static final Logger log = LoggerFactory.getLogger(GenreLoader.class);
    private EntityManager em;

    public GenreLoader(EntityManager em)
    {
        this.em = em;
    }

    private List<String> uniqueGenres = new ArrayList<>();

    public List<String> getUniqueGenres()
    {
        if (uniqueGenres.size() == 0)
        {
            long start = System.currentTimeMillis();
            uniqueGenres = identifyUniqueGenres();
            log.info("Identified " + uniqueGenres.size() + " distinct genres in " + (System.currentTimeMillis() - start) + "ms");
        }
        return uniqueGenres;
    }

    private List<String> identifyUniqueGenres()
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Film> filmRoot = query.from(Film.class);

        query.multiselect(filmRoot.get("genre"), cb.count(filmRoot))
                .where(cb.greaterThan(cb.length(filmRoot.get("genre")), 0))
                .groupBy(filmRoot.get("genre"))
                .orderBy(cb.desc(cb.count(filmRoot)));

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();

        Map<String, Integer> genreMap = new HashMap<>();
        results.forEach(result -> {
            String[] genres = ((String) result.get(0)).split(",");
            Long count = (Long) result.get(1);

            for (String genre : genres)
            {
                genre = genre.trim();
                if (!genreMap.containsKey(genre))
                    genreMap.put(genre, 0);
                genreMap.put(genre, genreMap.get(genre) + count.intValue());
            }
        });

        return genreMap.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
