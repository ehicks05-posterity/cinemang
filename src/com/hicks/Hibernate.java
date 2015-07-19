package com.hicks;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class Hibernate
{
    private static EntityManagerFactory emf;
    private static EntityManager em;

    static
    {
        try
        {
            emf = Persistence.createEntityManagerFactory("h2-ds");
            em = Hibernate.getEntityManagerFactory().createEntityManager();
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

    public static List executeQuery(String query, Object... args)
    {
        return em.createQuery(query).getResultList();
    }

    public static void persist(Object obj)
    {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        em.persist(obj);

        transaction.commit();
    }

    public static EntityManagerFactory getEntityManagerFactory()
    {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("h2-ds");
        return emf;
    }
}
