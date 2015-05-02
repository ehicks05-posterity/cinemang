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
import java.util.HashMap;
import java.util.Map;

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
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String tab1   = request.getParameter("tab1") == null ? "home" : request.getParameter("tab1");
        String action = request.getParameter("action") == null ? "index" : request.getParameter("action");

        try
        {
            if (action.equals("index")) FilmsHandler.showFilms(request, response);
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

        String viewJsp = getAction(tab1, action);
        RequestDispatcher dispatcher = request.getRequestDispatcher(viewJsp);
        dispatcher.forward(request, response);
    }

    private static String getAction(String tab1, String action)
    {
        Map<String, String> actionMap = getActionMap(tab1);
        return actionMap.get(action);
    }

    private static Map<String, String> getActionMap(String tab1)
    {
        if (tab1.equals("home"))
        {
            Map<String, String> actionMap = new HashMap<>();
            actionMap.put("index", "webroot/filmsList.jsp");

            return actionMap;
        }

        return null;
    }
}
