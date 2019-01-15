package net.ehicks.cinemang;

import net.ehicks.cinemang.beans.Film;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class LanguageLoader
{
    private static final Logger log = LoggerFactory.getLogger(LanguageLoader.class);
    private EntityManager em;

    public LanguageLoader(EntityManager em)
    {
        this.em = em;
    }

    private static List<String> uniqueLanguages = new ArrayList<>();

    public List<String> getUniqueLanguages()
    {
        if (uniqueLanguages.size() == 0)
        {
            long start = System.currentTimeMillis();
            uniqueLanguages = identifyUniqueLanguages();
            log.info("Identified " + uniqueLanguages.size() + " distinct languages in " + (System.currentTimeMillis() - start) + "ms");
        }
        return uniqueLanguages;
    }

    public List<String> identifyUniqueLanguages()
    {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Tuple> query = cb.createTupleQuery();
        Root<Film> filmRoot = query.from(Film.class);

        query.distinct(true).multiselect(filmRoot.get("language"), cb.count(filmRoot))
                .where(cb.greaterThan(cb.length(filmRoot.get("language")), 0))
                .groupBy(filmRoot.get("language"))
                .orderBy(cb.desc(cb.count(filmRoot)));

        TypedQuery<Tuple> typedQuery = em.createQuery(query);
        List<Tuple> results = typedQuery.getResultList();
        return results.stream().map(result -> (String) result.get(0)).collect(Collectors.toList());
    }
}
