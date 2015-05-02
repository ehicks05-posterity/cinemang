package com.hicks;

import com.owlike.genson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

public class Film implements Serializable
{
    @JsonProperty("Title")
    private String title = "";
    @JsonProperty("Year")
    private String year = "";
    @JsonProperty("Rated")
    private String rated = "";
    @JsonProperty("Released")
    private String released;
    @JsonProperty("Runtime")
    private String runtime = "";
    @JsonProperty("Genre")
    private String genre = "";
    @JsonProperty("Director")
    private String director = "";
    @JsonProperty("Writer")
    private String writer = "";
    @JsonProperty("Actors")
    private String actors = "";
    @JsonProperty("Plot")
    private String plot = "";
    @JsonProperty("Language")
    private String language = "";
    @JsonProperty("Country")
    private String country = "";
    @JsonProperty("Awards")
    private String awards = "";
    @JsonProperty("Poster")
    private String poster = "";

    @JsonProperty("Metascore")
    private String metascore;

    private String imdbRating;
    private String imdbVotes;
    private String imdbID = "";

    @JsonProperty("Type")
    private String type = "";

    @JsonProperty("tomatoMeter")
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

    @JsonProperty("DVD")
    private String dvd;
    @JsonProperty("BoxOffice")
    private String boxOffice = "";
    @JsonProperty("Production")
    private String production = "";
    @JsonProperty("Website")
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

    // -------- JSON Type Conversion ----------

//    @JsonProperty("Released")
//    public void setReleased(String released)
//    {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
//
//        try
//        {
//            LocalDate date = LocalDate.parse(released, formatter);
//            Instant instant = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
//            this.released = Date.from(instant);
//        }
//        catch (DateTimeParseException e)
//        {
//            System.out.println(e.getMessage());
//        }
//    }
//
//    @JsonProperty("Metascore")
//    public void setMetascore(String metascore)
//    {
//        this.metascore = Common.stringToInt(metascore);
//    }
//
//    @JsonProperty("imdbRating")
//    public void setImdbRating(String imdbRating)
//    {
//        this.imdbRating = Common.stringToBigDecimal(imdbRating);
//    }
//
//    @JsonProperty("imdbVotes")
//    public void setImdbVotes(String imdbVotes)
//    {
//        this.imdbVotes = Common.stringToInt(imdbVotes);
//    }
//
//    @JsonProperty("tomatoMeter")
//    public void setTomatoMeter(String tomatoMeter)
//    {
//        this.tomatoMeter = Common.stringToInt(tomatoMeter);
//    }
//
//    @JsonProperty("tomatoRating")
//    public void setTomatoRating(String tomatoRating)
//    {
//        this.tomatoRating = Common.stringToBigDecimal(tomatoRating);
//    }
//
//    @JsonProperty("tomatoReviews")
//    public void setTomatoReviews(String tomatoReviews)
//    {
//        this.tomatoReviews = Common.stringToInt(tomatoReviews);
//    }
//
//    @JsonProperty("tomatoFresh")
//    public void setTomatoFresh(String tomatoFresh)
//    {
//        this.tomatoFresh = Common.stringToInt(tomatoFresh);
//    }
//
//    @JsonProperty("tomatoRotten")
//    public void setTomatoRotten(String tomatoRotten)
//    {
//        this.tomatoRotten = Common.stringToInt(tomatoRotten);
//    }
//
//    @JsonProperty("tomatoUserMeter")
//    public void setTomatoUserMeter(String tomatoUserMeter)
//    {
//        this.tomatoUserMeter = Common.stringToInt(tomatoUserMeter);
//    }
//
//    @JsonProperty("tomatoUserRating")
//    public void setTomatoUserRating(String tomatoUserRating)
//    {
//        this.tomatoUserRating = Common.stringToBigDecimal(tomatoUserRating);
//    }
//
//    @JsonProperty("tomatoUserReviews")
//    public void setTomatoUserReviews(String tomatoUserReviews)
//    {
//        this.tomatoUserReviews = Common.stringToInt(tomatoUserReviews);
//    }
//
//    @JsonProperty("DVD")
//    public void setDvd(String dvd)
//    {
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH);
//
//        try
//        {
//            LocalDate date = LocalDate.parse(dvd, formatter);
//            Instant instant = date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
//            this.dvd = Date.from(instant);
//        }
//        catch (DateTimeParseException e)
//        {
//            System.out.println(e.getMessage());
//        }
//    }

    // -------- Getters / Setters ----------

    public String getTitle()
    {
        return title;
    }

    @JsonProperty("Title")
    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getYear()
    {
        return year;
    }

    @JsonProperty("Year")
    public void setYear(String year)
    {
        this.year = year;
    }

    public String getRated()
    {
        return rated;
    }

    @JsonProperty("Rated")
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

    @JsonProperty("Runtime")
    public void setRuntime(String runtime)
    {
        this.runtime = runtime;
    }

    public String getGenre()
    {
        return genre;
    }

    @JsonProperty("Genre")
    public void setGenre(String genre)
    {
        this.genre = genre;
    }

    public String getDirector()
    {
        return director;
    }

    @JsonProperty("Director")
    public void setDirector(String director)
    {
        this.director = director;
    }

    public String getWriter()
    {
        return writer;
    }

    @JsonProperty("Writer")
    public void setWriter(String writer)
    {
        this.writer = writer;
    }

    public String getActors()
    {
        return actors;
    }

    @JsonProperty("Actors")
    public void setActors(String actors)
    {
        this.actors = actors;
    }

    public String getPlot()
    {
        return plot;
    }

    @JsonProperty("Plot")
    public void setPlot(String plot)
    {
        this.plot = plot;
    }

    public String getLanguage()
    {
        return language;
    }

    @JsonProperty("Language")
    public void setLanguage(String language)
    {
        this.language = language;
    }

    public String getCountry()
    {
        return country;
    }

    @JsonProperty("Country")
    public void setCountry(String country)
    {
        this.country = country;
    }

    public String getAwards()
    {
        return awards;
    }

    @JsonProperty("Awards")
    public void setAwards(String awards)
    {
        this.awards = awards;
    }

    public String getPoster()
    {
        return poster;
    }

    @JsonProperty("Poster")
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

    @JsonProperty("Type")
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

    @JsonProperty("BoxOffice")
    public void setBoxOffice(String boxOffice)
    {
        this.boxOffice = boxOffice;
    }

    public String getProduction()
    {
        return production;
    }

    @JsonProperty("Production")
    public void setProduction(String production)
    {
        this.production = production;
    }

    public String getWebsite()
    {
        return website;
    }

    @JsonProperty("Website")
    public void setWebsite(String website)
    {
        this.website = website;
    }
}
