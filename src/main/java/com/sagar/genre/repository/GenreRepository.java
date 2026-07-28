package com.sagar.genre.repository;

import com.sagar.genre.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GenreRepository extends JpaRepository<Genre,Long> {

    long countByParentGenre(Genre parentGenre);

    boolean existsByCode(String code);

    List<Genre> findAllByOrderByDisplayOrderAsc();

    List<Genre> findByParentGenreIsNullOrderByDisplayOrderAsc();

    List<Genre> findByParentGenreIdOrderByDisplayOrderAsc(Long parentGenreId);

    boolean existsByParentGenreId(Long parentGenreId);

    List<Genre> findByActiveTrueOrderByDisplayOrderAsc();

}
