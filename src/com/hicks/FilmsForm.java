package com.hicks;

public class FilmsForm
{
    private String minimumVotesParam;
    private String titleParam;
    private String ratingParam;
    private String fromReleaseDate;
    private String toReleaseDate;
    private String language;
    private String genre;

    private String sortColumn;
    private String sortDirection;
    private String page;

    public FilmsForm(String minVotes, String title, String rating, String fromReleaseDate, String toReleaseDate, String language, String genre)
    {
        this.minimumVotesParam = minVotes;
        this.titleParam = title;
        this.ratingParam = rating;
        this.fromReleaseDate = fromReleaseDate;
        this.toReleaseDate = toReleaseDate;
        this.language = language;
        this.genre = genre;
    }

    // Getter / Setter
    public String getMinimumVotesParam()
    {
        return minimumVotesParam;
    }

    public void setMinimumVotesParam(String minimumVotesParam)
    {
        this.minimumVotesParam = minimumVotesParam;
    }

    public String getTitleParam()
    {
        return titleParam;
    }

    public void setTitleParam(String titleParam)
    {
        this.titleParam = titleParam;
    }

    public String getRatingParam()
    {
        return ratingParam;
    }

    public void setRatingParam(String ratingParam)
    {
        this.ratingParam = ratingParam;
    }

    public String getFromReleaseDate()
    {
        return fromReleaseDate;
    }

    public void setFromReleaseDate(String fromReleaseDate)
    {
        this.fromReleaseDate = fromReleaseDate;
    }

    public String getToReleaseDate()
    {
        return toReleaseDate;
    }

    public void setToReleaseDate(String toReleaseDate)
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

    public String getPage()
    {
        return page;
    }

    public void setPage(String page)
    {
        this.page = page;
    }
}
