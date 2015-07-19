package com.hicks;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.ParseException;

@WebServlet("/view")
public class Controller extends HttpServlet
{
    private static final boolean DEBUG = false;

    @Override
    public void init() throws ServletException
    {
        if (DEBUG)
        {
            System.out.println("Max Memory: " + new DecimalFormat("#,###").format(Runtime.getRuntime().maxMemory()));
            for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments())
                System.out.println(argument);
        }

        OmdbLoader.loadFilms();
    }

    @Override
    public void destroy()
    {
        Hibernate.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String tab1   = request.getParameter("tab1") == null ? "home" : request.getParameter("tab1");
        String action = request.getParameter("action") == null ? "index" : request.getParameter("action");

        String viewJsp = "";
        try
        {
            if (action.equals("form"))
                viewJsp = FilmsHandler.showFilms(request, response);
            if (action.equals("filterFilms"))
            {
                FilmsHandler.filterFilms(request, response);
                return;
            }
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(viewJsp);
        dispatcher.forward(request, response);
    }
}
