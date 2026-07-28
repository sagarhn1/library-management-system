package com.sagar.book.specification;

import com.sagar.book.model.Book;
import com.sagar.book.payload.request.BookSearchRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    public static Specification<Book> search(BookSearchRequest request) {

        return (root, query, criteriaBuilder) -> {

            List<Predicate> predicates = new ArrayList<>();

            // Keyword Search (Title or ISBN)

            if (request.getKeyword() != null &&
                    !request.getKeyword().trim().isEmpty()) {

                Predicate titlePredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("title")),
                                "%" + request.getKeyword().toLowerCase() + "%"
                        );

                Predicate isbnPredicate =
                        criteriaBuilder.like(
                                criteriaBuilder.lower(root.get("isbn")),
                                "%" + request.getKeyword().toLowerCase() + "%"
                        );

                predicates.add(
                        criteriaBuilder.or(titlePredicate, isbnPredicate)
                );
            }

            // Author

            if (request.getAuthorId() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("author").get("id"),
                                request.getAuthorId()
                        )
                );
            }

            // Genre

            if (request.getGenreId() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("genre").get("id"),
                                request.getGenreId()
                        )
                );
            }

            // Publisher

            if (request.getPublisherId() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("publisher").get("id"),
                                request.getPublisherId()
                        )
                );
            }

            // Language

            if (request.getLanguage() != null &&
                    !request.getLanguage().isBlank()) {

                predicates.add(
                        criteriaBuilder.equal(
                                criteriaBuilder.lower(root.get("language")),
                                request.getLanguage().toLowerCase()
                        )
                );
            }

            // Active

            if (request.getActive() != null) {

                predicates.add(
                        criteriaBuilder.equal(
                                root.get("active"),
                                request.getActive()
                        )
                );
            }

            // Minimum Price

            if (request.getMinPrice() != null) {

                predicates.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("price"),
                                request.getMinPrice()
                        )
                );
            }

            // Maximum Price

            if (request.getMaxPrice() != null) {

                predicates.add(
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("price"),
                                request.getMaxPrice()
                        )
                );
            }

            return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}