package com.sagar.publisher.repository;

import com.sagar.publisher.model.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PublisherRepository extends JpaRepository<Publisher, Long> {

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    List<Publisher> findByActiveTrueOrderByNameAsc();


}