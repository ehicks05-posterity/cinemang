package net.ehicks.cinemang

import net.ehicks.cinemang.beans.Film

data class FilmSearchResult @JvmOverloads constructor(var page: Int = 1,
                                                      var searchResults: List<Film> = listOf(),
                                                      var size: Long = 0) {
    // Derived values
    var pageOfResults: List<Film>? = null
        private set

    val pages: Long
        get() = 1 + (size - 1) / 100

    val isHasNext: Boolean
        get() = pages > Integer.valueOf(page)

    val isHasPrevious: Boolean
        get() = Integer.valueOf(page) > 1

    init {
        this.pageOfResults = searchResults
    }
}
