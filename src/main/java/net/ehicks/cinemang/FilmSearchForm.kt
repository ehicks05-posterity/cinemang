package net.ehicks.cinemang

import java.util.Date

class FilmSearchForm(
        var minVotes: Int? = 1000
        , var title: String = ""
        , var fromRating: Double? = 0.0
        , var toRating: Double? = 100.0
        , var fromReleaseDate: Date? = null
        , var toReleaseDate: Date? = null
        , var language: String = ""
        , var genre: String = ""
        , var sortColumn: String = "cinemangRating"
        , var sortDirection: String = "desc"
        , var page: Int? = 1
)