package net.ehicks.cinemang

import net.ehicks.cinemang.beans.Genre
import net.ehicks.cinemang.beans.Language
import java.time.LocalDate

class FilmSearchForm @JvmOverloads constructor(
        var minVotes: Int? = 100
        , var title: String = ""
        , var fromRating: Double? = 0.0
        , var toRating: Double? = 10.0
        , var fromReleaseDate: LocalDate? = null
        , var toReleaseDate: LocalDate? = null
        , var language: Language? = null
        , var genre: Genre? = null
        , var sortColumn: String = "userVoteAverage"
        , var sortDirection: String = "desc"
        , var page: Int = 1
        , var pageSize: Int = 20
        , var resultView: String = "filmMediaItems"
)