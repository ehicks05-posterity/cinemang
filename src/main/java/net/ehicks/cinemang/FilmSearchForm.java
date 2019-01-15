package net.ehicks.cinemang;

import java.util.Date;

public class FilmSearchForm
{
    private Integer minVotes = 1000;
    private String title = "";
    private Double fromRating = 0.0;
    private Double toRating = 100.0;
    private Date fromReleaseDate;
    private Date toReleaseDate;
    private String language = "";
    private String genre = "";

    private String sortColumn = "cinemangRating";
    private String sortDirection = "desc";
    private Integer page = 1;

    public FilmSearchForm()
    {

    }

    public FilmSearchForm(Integer minVotes, String title, Double fromRating, Double toRating, Date fromReleaseDate, Date toReleaseDate,
                          String language, String genre, String sortColumn, String sortDirection, Integer page)
    {
        this.minVotes = minVotes;
        this.title = title;
        this.fromRating = fromRating;
        this.toRating = toRating;
        this.fromReleaseDate = fromReleaseDate;
        this.toReleaseDate = toReleaseDate;
        this.language = language;
        this.genre = genre;
        this.sortColumn = sortColumn;
        this.sortDirection = sortDirection;
        this.page = page;
    }

    public Integer getMinVotes()
    {
        return minVotes;
    }

    public void setMinVotes(Integer minVotes)
    {
        this.minVotes = minVotes;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Double getFromRating()
    {
        return fromRating;
    }

    public void setFromRating(Double fromRating)
    {
        this.fromRating = fromRating;
    }

    public Double getToRating()
    {
        return toRating;
    }

    public void setToRating(Double toRating)
    {
        this.toRating = toRating;
    }

    public Date getFromReleaseDate()
    {
        return fromReleaseDate;
    }

    public void setFromReleaseDate(Date fromReleaseDate)
    {
        this.fromReleaseDate = fromReleaseDate;
    }

    public Date getToReleaseDate()
    {
        return toReleaseDate;
    }

    public void setToReleaseDate(Date toReleaseDate)
    {
        this.toReleaseDate = toReleaseDate;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getGenre()
    {
        return genre;
    }

    public void setGenre(String genre)
    {
        this.genre = genre;
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

    public Integer getPage()
    {
        return page;
    }

    public void setPage(Integer page)
    {
        this.page = page;
    }
}
