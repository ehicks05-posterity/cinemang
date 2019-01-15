package net.ehicks.cinemang;

import net.ehicks.cinemang.beans.Film;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FilmRepository extends JpaRepository<Film, String>
{
}
