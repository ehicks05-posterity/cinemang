package com.hicks;

import java.text.DecimalFormat;
import java.util.List;

public class FilmSearchResult
{
    private List<Film> searchResults;

    private String page;
    private String sortColumn;
    private String sortDirection;

    public FilmSearchResult(String pageParam, List<Film> searchResults, String sortColumn, String sortDirection)
    {
        this.page = pageParam;
        this.searchResults = searchResults;
        this.sortColumn = sortColumn;
        this.sortDirection = sortDirection;
    }

    // Derived values
    public List<Film> getPageOfResults()
    {
        if (page == null || Integer.valueOf(page) > getPages())
            page = "1";

        int from = (Integer.valueOf(page) - 1) * 100;
        int to = from + 100;
        if (to > getSearchResultsSize())
            to = getSearchResultsSize();

        return searchResults.subList(from, to);
    }

    public int getSearchResultsSize()
    {
        return searchResults.size();
    }

    public String getPrettySearchResultsSize()
    {
        return new DecimalFormat("#,###").format(searchResults.size());
    }

    public int getPages()
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
