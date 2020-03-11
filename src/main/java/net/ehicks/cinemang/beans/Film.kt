package net.ehicks.cinemang.beans

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
data class Film @JvmOverloads constructor(
        @Id
        var tmdbId: Int = 0,
        var imdbId: String = "",
        var title: String = "",
        var year: String = "",

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(name = "film_genre",
                joinColumns = [JoinColumn(name = "film_id")],
                inverseJoinColumns = [JoinColumn(name = "genre_id")])
        var genres: Set<Genre> = HashSet(),

        @ManyToOne(fetch = FetchType.LAZY)
        var language: Language = Language("TEMP", "TEMP"),
        var director: String = "",
        var writer: String = "",
        var actors: String = "",
        var posterPath: String = "",
        @Column(length = 4000)
        var overview: String = "",
        var runtime: Int = 0,
        var rated: String = "",
        var revenue: Long = 0L,
        var released: LocalDate = LocalDate.now(),
        var userVoteAverage: Float = 0f,
        var userVoteCount: Int = 0,
        var lastUpdated: LocalDateTime = LocalDateTime.now()
) : Serializable {
    override fun toString(): String {
        return "Film $title"
    }

    fun getGenreString(): String {
        return if (genres.isEmpty())
            ""
        else
            genres.map { it.name }.reduce { acc, s -> "$acc, $s" }
    }

    fun getPrimaryGenre(): String {
        return if (genres.isEmpty())
            ""
        else
            genres.first().name
    }

    fun getRuntimeString(): String {
        val hours = runtime / 60
        val mins = runtime - (hours * 60)
        return "${hours}h ${mins}min"
    }

    fun getUserVoteCountString(): String {
        return if (userVoteCount < 1000)
            userVoteCount.toString()
        else
            (userVoteCount / 1000).toString() + "k"
    }
}

@Repository
interface FilmRepository : JpaRepository<Film, Int>
