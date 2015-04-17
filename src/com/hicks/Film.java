package com.hicks;

import java.math.BigDecimal;
import java.util.Date;

public class Film
{
    private String title = "";
    private int year;
    private Date releaseDate;
    private BigDecimal rating;
    private int votes;

    public Film(String title, int year, BigDecimal rating, int votes)
    {
        this.title = title;
        this.year = year;
        this.rating = rating;
        this.votes = votes;
    }

    public Film(String title, Date releaseDate)
    {
        this.title = title;
        this.releaseDate = releaseDate;
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
        return title + year + " - " + " - " + rating + " - " + votes;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public int getYear()
    {
        return year;
    }

    public void setYear(int year)
    {
        this.year = year;
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
}
