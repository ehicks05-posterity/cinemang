package com.hicks;

import java.io.Serializable;

public class Film implements Serializable
{
    private String title = "";
    private String year = "";
    private String rated = "";
    private String released;
    private String runtime = "";
    private String genre = "";
    private String director = "";
    private String writer = "";
    private String actors = "";
    private String plot = "";
    private String language = "";
    private String country = "";
    private String awards = "";
    private String poster = "";

    private String metascore;

    private String imdbRating;
    private String imdbVotes;
    private String imdbID = "";

    private String type = "";

    private String tomatoMeter;
    private String tomatoImage = "";
    private String tomatoRating;
    private String tomatoReviews;
    private String tomatoFresh;
    private String tomatoRotten;
    private String tomatoConsensus = "";
    private String tomatoUserMeter;
    private String tomatoUserRating;
    private String tomatoUserReviews;

    private String dvd;
    private String boxOffice = "";
    private String production = "";
    private String website = "";

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Film)) return false;
        Film that = (Film) obj;
        return this.imdbID.equals(that.getImdbID());
    }

    public String toString()
    {
        return title;
    }

    // -------- Getters / Setters ----------

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getYear()
    {
        return year;
    }

    public void setYear(String year)
    {
        this.year = year;
    }

    public String getRated()
    {
        return rated;
    }

    public void setRated(String rated)
    {
        this.rated = rated;
    }

    public String getReleased()
    {
        return released;
    }

    public void setReleased(String released)
    {
        this.released = released;
    }

    public String getRuntime()
    {
        return runtime;
    }

    public void setRuntime(String runtime)
    {
        this.runtime = runtime;
    }

    public String getGenre()
    {
        return genre;
    }

    public void setGenre(String genre)
    {
        this.genre = genre;
    }

    public String getDirector()
    {
        return director;
    }

    public void setDirector(String director)
    {
        this.director = director;
    }

    public String getWriter()
    {
        return writer;
    }

    public void setWriter(String writer)
    {
        this.writer = writer;
    }

    public String getActors()
    {
        return actors;
    }

    public void setActors(String actors)
    {
        this.actors = actors;
    }

    public String getPlot()
    {
        return plot;
    }

    public void setPlot(String plot)
    {
        this.plot = plot;
    }

    public String getLanguage()
    {
        return language;
    }

    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getCountry()
    {
        return country;
    }

    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getAwards()
    {
        return awards;
    }

    public void setAwards(String awards)
    {
        this.awards = awards;
    }

    public String getPoster()
    {
        return poster;
    }

    public void setPoster(String poster)
    {
        this.poster = poster;
    }

    public String getMetascore()
    {
        return metascore;
    }

    public void setMetascore(String metascore)
    {
        this.metascore = metascore;
    }

    public String getImdbRating()
    {
        return imdbRating;
    }

    public void setImdbRating(String imdbRating)
    {
        this.imdbRating = imdbRating;
    }

    public String getImdbVotes()
    {
        return imdbVotes;
    }

    public void setImdbVotes(String imdbVotes)
    {
        this.imdbVotes = imdbVotes;
    }

    public String getImdbID()
    {
        return imdbID;
    }

    public void setImdbID(String imdbID)
    {
        this.imdbID = imdbID;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getTomatoMeter()
    {
        return tomatoMeter;
    }

    public void setTomatoMeter(String tomatoMeter)
    {
        this.tomatoMeter = tomatoMeter;
    }

    public String getTomatoImage()
    {
        return tomatoImage;
    }

    public void setTomatoImage(String tomatoImage)
    {
        this.tomatoImage = tomatoImage;
    }

    public String getTomatoRating()
    {
        return tomatoRating;
    }

    public void setTomatoRating(String tomatoRating)
    {
        this.tomatoRating = tomatoRating;
    }

    public String getTomatoReviews()
    {
        return tomatoReviews;
    }

    public void setTomatoReviews(String tomatoReviews)
    {
        this.tomatoReviews = tomatoReviews;
    }

    public String getTomatoFresh()
    {
        return tomatoFresh;
    }

    public void setTomatoFresh(String tomatoFresh)
    {
        this.tomatoFresh = tomatoFresh;
    }

    public String getTomatoRotten()
    {
        return tomatoRotten;
    }

    public void setTomatoRotten(String tomatoRotten)
    {
        this.tomatoRotten = tomatoRotten;
    }

    public String getTomatoConsensus()
    {
        return tomatoConsensus;
    }

    public void setTomatoConsensus(String tomatoConsensus)
    {
        this.tomatoConsensus = tomatoConsensus;
    }

    public String getTomatoUserMeter()
    {
        return tomatoUserMeter;
    }

    public void setTomatoUserMeter(String tomatoUserMeter)
    {
        this.tomatoUserMeter = tomatoUserMeter;
    }

    public String getTomatoUserRating()
    {
        return tomatoUserRating;
    }

    public void setTomatoUserRating(String tomatoUserRating)
    {
        this.tomatoUserRating = tomatoUserRating;
    }

    public String getTomatoUserReviews()
    {
        return tomatoUserReviews;
    }

    public void setTomatoUserReviews(String tomatoUserReviews)
    {
        this.tomatoUserReviews = tomatoUserReviews;
    }

    public String getDvd()
    {
        return dvd;
    }

    public void setDvd(String dvd)
    {
        this.dvd = dvd;
    }

    public String getBoxOffice()
    {
        return boxOffice;
    }

    public void setBoxOffice(String boxOffice)
    {
        this.boxOffice = boxOffice;
    }

    public String getProduction()
    {
        return production;
    }

    public void setProduction(String production)
    {
        this.production = production;
    }

    public String getWebsite()
    {
        return website;
    }

    public void setWebsite(String website)
    {
        this.website = website;
    }
}
