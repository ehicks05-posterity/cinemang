package com.hicks;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

@WebListener
public class ContextListener implements ServletContextListener
{

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        System.out.println("ContextListener Starting up!");
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        System.out.println("ContextListener Shutting down!");

        // This manually deregisters JDBC driver, which prevents Tomcat 7 from complaining about memory leaks wrt this class
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements())
        {
            Driver driver = drivers.nextElement();
            try
            {
                DriverManager.deregisterDriver(driver);
                System.out.println(String.format("deregistering jdbc driver: %s", driver));
            }
            catch (SQLException e)
            {
                System.out.println(String.format("Error deregistering driver %s", driver));
                System.out.println(e.getMessage());
            }
        }
    }
}