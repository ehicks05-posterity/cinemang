import java.math.BigDecimal;

public class Film
{
    private String title = "";
    private BigDecimal rating;
    private int votes;

    public Film(String title, BigDecimal rating, int votes)
    {
        this.title = title;
        this.rating = rating;
        this.votes = votes;
    }

    public String toString()
    {
        return title + " - " + rating + " - " + votes;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
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
