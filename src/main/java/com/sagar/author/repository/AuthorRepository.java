package com.sagar.author.repository;

import com.sagar.author.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {

    boolean existsByEmail(String email);

    List<Author> findByActiveTrueOrderByNameAsc();
}
