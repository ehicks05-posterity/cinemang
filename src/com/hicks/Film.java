package com.hicks;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Film implements Serializable
{
    private String title = "";
    private Date releaseDate;
    private BigDecimal rating;
    private int votes;
    private String language = "";
    private List<String> genres = new ArrayList<>();

    public Film(String title, BigDecimal rating, int votes)
    {
        this.title = title;
        this.rating = rating;
        this.votes = votes;
    }

    public Film(String title, Date releaseDate)
    {
        this.title = title;
        this.releaseDate = releaseDate;
    }

    public Film(String title, String language)
    {
        this.title = title;
        this.language = language;
    }

    public Film(String title, List<String> genres)
    {
        this.title = title;
        this.genres = genres;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Film)) return false;
        Film that = (Film) obj;
        return this.title.equals(that.getTitle());
    }

    public String toString()
    {
        return title;
    }

    // ------------------
    public String getGenresAsString()
    {
        StringBuilder sb = new StringBuilder();
        for (String genre : genres)
        {
            if (sb.length() > 0) sb.append(", ");
            sb.append(genre);
        }
        return sb.toString();
    }

    // ------------------
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public Date getReleaseDate()
    {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate)
    {
        this.releaseDate = releaseDate;
    }

    public BigDecimal getRating()
    {
        return rating;
    }

    public void setRating(BigDecimal rating)
    {
        this.rating = rating;
    }

    public int getVotes()
    {
        return votes;
    }

    public void setVotes(int votes)
    {
        this.votes = votes;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public List<String> getGenres()
    {
        return genres;
    }

    public void setGenres(List<String> genres)
    {
        this.genres = genres;
    }
}
