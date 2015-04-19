package com.hicks;

public class FilmLanguage
{
    private String name = "";
    private int occurrences;

    public FilmLanguage(String name, int occurrences)
    {
        this.name = name;
        this.occurrences = occurrences;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getOccurrences()
    {
        return occurrences;
    }

    public void setOccurrences(int occurrences)
    {
        this.occurrences = occurrences;
    }
}
