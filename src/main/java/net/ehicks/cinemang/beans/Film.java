package net.ehicks.cinemang.beans;

import net.ehicks.cinemang.Common;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

@Entity
@Table(name = "films")
public class Film implements Serializable
{
    @Id
    @Column(updatable = false, nullable = false)
    private String imdbId = "";

    private String title = "";
    private String year = "";
    private String rated = "";
    private String runtime = "";
    private String genre = "";
    @Temporal(TemporalType.DATE)
    private Date released;

    @Type(type="org.hibernate.type.TextType")
    private String director = "";

    @Type(type="org.hibernate.type.TextType")
    private String writer = "";

    @Type(type="org.hibernate.type.TextType")
    private String actors = "";
    private Integer metascore;
    private BigDecimal imdbRating;
    private Integer imdbVotes;
    private String poster = "";

    private Integer cinemangRating = 0;

    @Type(type="org.hibernate.type.TextType")
    private String plot = "";

    @Type(type="org.hibernate.type.TextType")
    private String fullPlot = "";
    private String language = "";
    private String country = "";
    private String awards = "";
    private String lastUpdated = "";

    private String tomatoImage = "";
    private BigDecimal tomatoRating;
    private Integer tomatoMeter;
    private Integer tomatoReviews;
    private Integer tomatoFresh;
    private Integer tomatoRotten;

    @Type(type="org.hibernate.type.TextType")
    private String tomatoConsensus = "";
    private Integer tomatoUserMeter;
    private BigDecimal tomatoUserRating;
    private Integer tomatoUserReviews;

    @Temporal(TemporalType.DATE)
    private Date dvd;
    private String boxOffice = "";
    private String production = "";
    private String website = "";
    private String rottenDataLastUpdated = "";

    private String type = "";

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Film)) return false;
        Film that = (Film) obj;
        return this.imdbId.equals(that.getImdbId());
    }

    @Override
    public int hashCode()
    {
        return 17 * 37 * Integer.valueOf(imdbId);
    }

    public String toString()
    {
        return "Film " + imdbId;
    }

    // --------

    public static String convertIdToImdbId(String id)
    {
        StringBuilder newId = new StringBuilder(id);
        while (newId.length() < 7)
            newId.insert(0, "0");

        return "tt" + newId.toString();
    }

    public Integer calculateCinemangRating()
    {
        if (imdbRating == null ||
                tomatoMeter == null || tomatoMeter == 0 ||
                tomatoUserMeter == null ||  tomatoUserMeter == 0)
            return 0;

        BigDecimal imdbNormalized = imdbRating.multiply(BigDecimal.TEN);
        BigDecimal tomatoCritic = Common.integerToBigDecimal(tomatoMeter);
        BigDecimal tomatoUser = Common.integerToBigDecimal(tomatoUserMeter);

        BigDecimal average = imdbNormalized.add(tomatoCritic).add(tomatoUser).divide(new BigDecimal("3"), 0, RoundingMode.HALF_UP);
        return average.intValue();
    }

    public String getPrettyPlot()
    {
        String plot = fullPlot;
        if (plot == null || plot.length() == 0)
            plot = this.plot;
        if (plot == null || plot.length() == 0)
            return "";

        int maxLength = 768;
        if (plot.length() > maxLength)
        {
            int lastSpace = plot.substring(0, maxLength).lastIndexOf(" ");
            return plot.substring(0, lastSpace) + "...";
        }
        return plot;
    }

    // -------- Getters / Setters ----------

    public String getImdbId()
    {
        return imdbId;
    }

    public void setImdbId(String imdbId)
    {
        this.imdbId = imdbId;
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

    public Date getReleased()
    {
        return released;
    }

    public void setReleased(Date released)
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

    public Integer getMetascore()
    {
        return metascore;
    }

    public void setMetascore(Integer metascore)
    {
        this.metascore = metascore;
    }

    public BigDecimal getImdbRating()
    {
        return imdbRating;
    }

    public void setImdbRating(BigDecimal imdbRating)
    {
        this.imdbRating = imdbRating;
    }

    public Integer getImdbVotes()
    {
        return imdbVotes;
    }

    public void setImdbVotes(Integer imdbVotes)
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

    public Integer getCinemangRating()
    {
        return cinemangRating;
    }

    public void setCinemangRating(Integer cinemangRating)
    {
        this.cinemangRating = cinemangRating;
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

    public BigDecimal getTomatoRating()
    {
        return tomatoRating;
    }

    public void setTomatoRating(BigDecimal tomatoRating)
    {
        this.tomatoRating = tomatoRating;
    }

    public Integer getTomatoMeter()
    {
        return tomatoMeter;
    }

    public void setTomatoMeter(Integer tomatoMeter)
    {
        this.tomatoMeter = tomatoMeter;
    }

    public Integer getTomatoReviews()
    {
        return tomatoReviews;
    }

    public void setTomatoReviews(Integer tomatoReviews)
    {
        this.tomatoReviews = tomatoReviews;
    }

    public Integer getTomatoFresh()
    {
        return tomatoFresh;
    }

    public void setTomatoFresh(Integer tomatoFresh)
    {
        this.tomatoFresh = tomatoFresh;
    }

    public Integer getTomatoRotten()
    {
        return tomatoRotten;
    }

    public void setTomatoRotten(Integer tomatoRotten)
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

    public Integer getTomatoUserMeter()
    {
        return tomatoUserMeter;
    }

    public void setTomatoUserMeter(Integer tomatoUserMeter)
    {
        this.tomatoUserMeter = tomatoUserMeter;
    }

    public BigDecimal getTomatoUserRating()
    {
        return tomatoUserRating;
    }

    public void setTomatoUserRating(BigDecimal tomatoUserRating)
    {
        this.tomatoUserRating = tomatoUserRating;
    }

    public Integer getTomatoUserReviews()
    {
        return tomatoUserReviews;
    }

    public void setTomatoUserReviews(Integer tomatoUserReviews)
    {
        this.tomatoUserReviews = tomatoUserReviews;
    }

    public Date getDvd()
    {
        return dvd;
    }

    public void setDvd(Date dvd)
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
