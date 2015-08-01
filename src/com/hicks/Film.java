package com.hicks;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "films")
public class Film implements Serializable
{
    @Version
    @Column(name = "version")
    private Long version;

    @Id
    @Column(name = "imdb_ID", updatable = false, nullable = false)
    private String imdbID = "";

    @Column(name = "title", length = 2000)
    private String title = "";
    @Column(name = "year")
    private String year = "";
    @Column(name = "rated")
    private String rated = "";
    @Column(name = "runtime")
    private String runtime = "";
    @Column(name = "genre")
    private String genre = "";
    @Column(name = "released")
    private String released = "";

    @Column(name = "director", length = 2000)
    private String director = "";

    @Column(name = "writer", length = 2000)
    private String writer = "";

    @Column(name = "actors", length = 2000)
    private String actors = "";
    @Column(name = "metascore")
    private String metascore = "";
    @Column(name = "imdb_Rating")
    private String imdbRating = "";
    @Column(name = "imdb_Votes")
    private String imdbVotes = "";
    @Column(name = "poster")
    private String poster = "";

    @Column(name = "plot", length = 2000)
    private String plot = "";

    @Column(name = "full_Plot", columnDefinition = "varchar2(32000 CHAR)")
    private String fullPlot = "";
    @Column(name = "language")
    private String language = "";
    @Column(name = "country")
    private String country = "";
    @Column(name = "awards")
    private String awards = "";
    @Column(name = "last_Updated")
    private String lastUpdated = "";

    @Column(name = "tomato_Image")
    private String tomatoImage = "";
    @Column(name = "tomato_Rating")
    private String tomatoRating = "";
    @Column(name = "tomato_Meter")
    private String tomatoMeter = "";
    @Column(name = "tomato_Reviews")
    private String tomatoReviews = "";
    @Column(name = "tomato_Fresh")
    private String tomatoFresh = "";
    @Column(name = "tomato_Rotten")
    private String tomatoRotten = "";

    @Column(name = "tomato_Consensus", length = 2000)
    private String tomatoConsensus = "";
    @Column(name = "tomato_User_Meter")
    private String tomatoUserMeter = "";
    @Column(name = "tomato_User_Rating")
    private String tomatoUserRating = "";
    @Column(name = "tomato_User_Reviews")
    private String tomatoUserReviews = "";

    @Column(name = "dvd")
    private String dvd = "";
    @Column(name = "box_Office")
    private String boxOffice = "";
    @Column(name = "production")
    private String production = "";
    @Column(name = "website")
    private String website = "";
    @Column(name = "rotten_Data_Last_Updated")
    private String rottenDataLastUpdated = "";

    @Column(name = "type")
    private String type = "";

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Film)) return false;
        Film that = (Film) obj;
        return this.imdbID.equals(that.getImdbID());
    }

    @Override
    public int hashCode()
    {
        return 17 * 37 * Integer.valueOf(imdbID);
    }

    public String toString()
    {
        return title;
    }

    // --------

    public static String convertIdToImdbId(String id)
    {
        StringBuilder newId = new StringBuilder(id);
        while (newId.length() < 7)
            newId.insert(0, "0");

        return "tt" + newId.toString();
    }

    public String getComboRating()
    {
        if (imdbRating == null || imdbRating.length() == 0 ||
                tomatoMeter == null || tomatoMeter.length() == 0 ||
                tomatoUserMeter == null ||  tomatoUserMeter.length() == 0)
            return "";

        BigDecimal imdbNormalized = Common.stringToBigDecimal(imdbRating).multiply(BigDecimal.TEN);
        BigDecimal tomatoCritic = Common.stringToBigDecimal(tomatoMeter);
        BigDecimal tomatoUser = Common.stringToBigDecimal(tomatoUserMeter);

        BigDecimal average = imdbNormalized.add(tomatoCritic).add(tomatoUser).divide(new BigDecimal("3"), 0, RoundingMode.HALF_UP);
        return average.toString();
    }

    public String getShortFullPlot()
    {
        int maxLength = 768;
        if (fullPlot == null) return "";
        if (fullPlot.length() > maxLength)
        {
            int lastSpace = fullPlot.substring(0, maxLength).lastIndexOf(" ");
            return fullPlot.substring(0, lastSpace) + "...";
        }
        return fullPlot;
    }

    // -------- Getters / Setters ----------

    public Long getVersion()
    {
        return version;
    }

    public void setVersion(Long version)
    {
        this.version = version;
    }

    public String getImdbID()
    {
        return imdbID;
    }

    public void setImdbID(String imdbID)
    {
        this.imdbID = imdbID;
    }

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

    public String getReleased()
    {
        return released;
    }

    public void setReleased(String released)
    {
        this.released = released;
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

    public String getPoster()
    {
        return poster;
    }

    public void setPoster(String poster)
    {
        this.poster = poster;
    }

    public String getPlot()
    {
        return plot;
    }

    public void setPlot(String plot)
    {
        this.plot = plot;
    }

    public String getFullPlot()
    {
        return fullPlot;
    }

    public void setFullPlot(String fullPlot)
    {
        this.fullPlot = fullPlot;
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

    public String getLastUpdated()
    {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated)
    {
        this.lastUpdated = lastUpdated;
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

    public String getTomatoMeter()
    {
        return tomatoMeter;
    }

    public void setTomatoMeter(String tomatoMeter)
    {
        this.tomatoMeter = tomatoMeter;
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

    public String getRottenDataLastUpdated()
    {
        return rottenDataLastUpdated;
    }

    public void setRottenDataLastUpdated(String rottenDataLastUpdated)
    {
        this.rottenDataLastUpdated = rottenDataLastUpdated;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }
}
