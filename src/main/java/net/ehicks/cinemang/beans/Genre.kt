package net.ehicks.cinemang.beans

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Genre @JvmOverloads constructor(
        @Id
        var id: Int = 0,
        var name: String = ""
) : Serializable {
    override fun toString(): String {
        return "Genre $name"
    }
}

@Repository
interface GenreRepository : JpaRepository<Genre, Int>
