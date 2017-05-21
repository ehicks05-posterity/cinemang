package net.ehicks.cinemang;

import net.ehicks.cinemang.beans.Film;

import javax.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class Hibernate
{
    private static EntityManagerFactory emf;
    private static EntityManager em;

    static
    {
        try
        {
            emf = Persistence.createEntityManagerFactory("h2-ds");
            em = emf.createEntityManager();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    public static void destroy()
    {
        if (em != null) em.close();
        if (emf != null) emf.close();
    }

    private static void saveFilmsWithHibernate(List<Film> films)
    {
        int persistIndex = 0;
        EntityTransaction transaction = Hibernate.startTransaction();
        for (Film film : films)
        {
            if (film.getImdbVotes() < 1000)
                continue;

            Hibernate.persistAsPartOfTransaction(film);
            persistIndex++;
            if (persistIndex % 10_000 == 0)
                System.out.println(persistIndex + "/" + films.size());
        }
        System.out.println(persistIndex + "/" + films.size());
        System.out.println("committing transaction...");
        Hibernate.commitTransaction(transaction);
    }

    public static List executeQuery(String queryString)
    {
        return executeQuery(queryString, Collections.EMPTY_MAP, 0, 0);
    }

    public static List executeQuery(String queryString, Map<String, Object> args)
    {
        return executeQuery(queryString, args, 0, 0);
    }

    public static List executeQuery(String queryString, Map<String, Object> args, int firstResult, int maxResults)
    {
        Query query = em.createQuery(queryString);
        if (firstResult > 0) query.setFirstResult(firstResult);
        if (maxResults > 0) query.setMaxResults(maxResults);
        setQueryParams(args, query);
        return query.getResultList();
    }

    public static Object executeQuerySingleResult(String queryString)
    {
        return executeQuerySingleResult(queryString, Collections.emptyMap());
    }

    public static Object executeQuerySingleResult(String queryString, Map<String, Object> args)
    {
        Query query = em.createQuery(queryString);
        setQueryParams(args, query);
        return query.getSingleResult();
    }

    private static void setQueryParams(Map<String, Object> args, Query query)
    {
        for (String key : args.keySet())
        {
            if (args.get(key) instanceof Date)
                query.setParameter(key, ((Date) args.get(key)), TemporalType.DATE);
            else
                query.setParameter(key, args.get(key));
        }
    }

    public static void persist(Object obj)
    {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        em.persist(obj);

        try
        {
            transaction.commit();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            transaction.rollback();
        }
    }

    public static void persistAsPartOfTransaction(Object obj)
    {
        em.persist(obj);
    }

    public static EntityTransaction startTransaction()
    {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        return transaction;
    }

    public static void commitTransaction(EntityTransaction transaction)
    {
        transaction.commit();
    }

    public static void flushCache()
    {
        em.flush();
    }
}
