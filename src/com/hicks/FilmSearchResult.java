package com.hicks;

import com.hicks.beans.Film;

import java.text.DecimalFormat;
import java.util.List;

public class FilmSearchResult
{
    private List<Film> searchResults;
    private long size;
    private String page;
    private String sortColumn;
    private String sortDirection;

    public FilmSearchResult(String pageParam, List<Film> searchResults, String sortColumn, String sortDirection)
    {
        this(pageParam, searchResults, sortColumn, sortDirection, 0);
    }

    public FilmSearchResult(String pageParam, List<Film> searchResults, String sortColumn, String sortDirection, long size)
    {
        this.page = pageParam;
        this.searchResults = searchResults;
        this.sortColumn = sortColumn;
        this.sortDirection = sortDirection;
        this.size = size;
    }

    // Derived values
    public List<Film> getPageOfResults()
    {
        if (SystemInfo.isLoadDbToRam())
        {
            if (page == null || Integer.valueOf(page) > getPages())
                page = "1";

            int from = (Integer.valueOf(page) - 1) * 100;
            int to = from + 100;
            if (to > getSearchResultsSize())
                to = (int) getSearchResultsSize();

            return searchResults.subList(from, to);
        }
        else
        {
            return searchResults;
        }
    }

    public long getSearchResultsSize()
    {
        if (SystemInfo.isLoadDbToRam())
            return searchResults.size();
        else
            return size;
    }

    public String getPrettySearchResultsSize()
    {
        if (SystemInfo.isLoadDbToRam())
            return new DecimalFormat("#,###").format(searchResults.size());
        else
            return new DecimalFormat("#,###").format(size);
    }

    public long getPages()
    {
        return 1 + ((getSearchResultsSize() - 1) / 100);
    }

    public boolean isHasNext()
    {
        return getPages() > Integer.valueOf(page);
    }

    public boolean isHasPrevious()
    {
        return Integer.valueOf(page) > 1;
    }

    // Getter / Setter
    public List<Film> getSearchResults()
    {
        return searchResults;
    }

    public void setSearchResults(List<Film> searchResults)
    {
        this.searchResults = searchResults;
    }

    public long getSize()
    {
        return size;
    }

    public void setSize(long size)
    {
        this.size = size;
    }

    public String getPage()
    {
        return page;
    }

    public void setPage(String page)
    {
        this.page = page;
    }

    public String getSortColumn()
    {
        return sortColumn;
    }

    public void setSortColumn(String sortColumn)
    {
        this.sortColumn = sortColumn;
    }

    public String getSortDirection()
    {
        return sortDirection;
    }

    public void setSortDirection(String sortDirection)
    {
        this.sortDirection = sortDirection;
    }
}
