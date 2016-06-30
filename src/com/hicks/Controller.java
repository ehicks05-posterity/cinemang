package com.hicks;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Properties;

@WebServlet("/view")
public class Controller extends HttpServlet
{
    private static final boolean DEBUG = false;
    private static final boolean DROP_TABLES = false;

    @Override
    public void init() throws ServletException
    {
        long controllerStart = System.currentTimeMillis();

        EOI.init();
        SystemInfo.setServletContext(getServletContext());

        long subTaskStart = System.currentTimeMillis();
        DBMap.loadDbMaps();
        System.out.println("Loaded DBMAPS in " + (System.currentTimeMillis() - subTaskStart) + "ms");

        if (DROP_TABLES)
        {
            subTaskStart = System.currentTimeMillis();
            for (DBMap dbMap : DBMap.dbMaps)
                EOI.executeUpdate("drop table " + dbMap.tableName);
            System.out.println("Dropped existing tables in " + (System.currentTimeMillis() - subTaskStart) + "ms");
        }

        subTaskStart = System.currentTimeMillis();
        for (DBMap dbMap : DBMap.dbMaps)
            ensureTableExists(dbMap);
        System.out.println("Made sure all tables exist (recreating if necessary) in " + (System.currentTimeMillis() - subTaskStart) + "ms");

        if (DEBUG)
            for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments())
                System.out.println(argument);

        System.out.println("Max Memory: " + new DecimalFormat("#,###").format(Runtime.getRuntime().maxMemory()));

        loadProperties();

//        FilmLoader.initFilms();
        long startupTime = System.currentTimeMillis() - controllerStart;
        System.out.println("Cinemang started in " + startupTime + " ms");
    }

    private static void ensureTableExists(DBMap dbMap)
    {
        String query = "SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA='PUBLIC' AND TABLE_NAME='" + dbMap.tableName.toUpperCase() + "';";
        List result = (List) EOI.executeQuery(query);

        if (result.size() > 0)
            return;

        String createTableStatement = SQLGenerator.getCreateTableStatement(dbMap);
        EOI.executeUpdate(createTableStatement);
    }

    private void loadProperties()
    {
        try
        {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.properties");
            Properties props = new Properties();
            props.load(inputStream);
            SystemInfo.setProperties(props);
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void destroy()
    {
        EOI.destroy();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        long start = System.currentTimeMillis();
        String tab1   = request.getParameter("tab1") == null ? "home" : request.getParameter("tab1");
        String action = request.getParameter("action") == null ? "form" : request.getParameter("action");

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
            if (action.equals("getNewPage"))
            {
                FilmsHandler.getNewPage(request, response);
                return;
            }
            if (action.equals("getPoster"))
            {
                FilmsHandler.getPoster(request, response);
                return;
            }
            if (action.equals("debug"))
            {
                DebugHandler.getDebugInfo(request, response);
                return;
            }
        }
        catch (ParseException e)
        {
            System.out.println(e.getMessage());
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher(viewJsp);
        dispatcher.forward(request, response);

        System.out.println(System.currentTimeMillis() - start + " ms for last request " + request.getQueryString());
    }
}
