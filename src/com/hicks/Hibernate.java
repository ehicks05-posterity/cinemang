package com.hicks;

import javax.persistence.*;
import java.util.List;

public class Hibernate
{
    private static EntityManagerFactory emf;
    private static EntityManager em;

    static
    {
        try
        {
            emf = Persistence.createEntityManagerFactory("oracle-ds");
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

    public static List executeQuery(String queryString, Object... args)
    {
        Query query = em.createQuery(queryString);
        for (Object arg : args)
            query.setParameter(1, arg);
        return query.getResultList();
    }

    public static void persist(Object obj)
    {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        em.persist(obj);

        transaction.commit();
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
