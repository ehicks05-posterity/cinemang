package net.ehicks.cinemang.beans

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Language @JvmOverloads constructor(
        @Id
        var id: String = "",
        var name: String = "",
        var count: Long = 0
) : Serializable {
    override fun toString(): String {
        return "Language $name"
    }
}

@Repository
interface LanguageRepository : JpaRepository<Language, String> {
    fun findByOrderByCountDesc(): List<Language>
}
