package com.sagar.book.repository;

import com.sagar.book.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends
        JpaRepository<Book, Long>,
        JpaSpecificationExecutor<Book> {

    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByActiveTrueOrderByTitleAsc();
}