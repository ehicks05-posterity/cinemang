package net.ehicks.cinemang;

import net.ehicks.cinemang.beans.Film;

import java.text.DecimalFormat;
import java.util.List;

public class FilmSearchResult
{
    private List<Film> searchResults;
    private long size;
    private int page;
    private String sortColumn;
    private String sortDirection;

    public FilmSearchResult(int page, List<Film> searchResults, String sortColumn, String sortDirection)
    {
        this(page, searchResults, sortColumn, sortDirection, 0);
    }

    public FilmSearchResult(int page, List<Film> searchResults, String sortColumn, String sortDirection, long size)
    {
        this.page = page;
        this.searchResults = searchResults;
        this.sortColumn = sortColumn;
        this.sortDirection = sortDirection;
        this.size = size;
    }

    // Derived values
    public List<Film> getPageOfResults()
    {
        return searchResults;
    }

    public long getPages()
    {
        return 1 + ((getSize() - 1) / 100);
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

    public int getPage()
    {
        return page;
    }

    public void setPage(int page)
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
