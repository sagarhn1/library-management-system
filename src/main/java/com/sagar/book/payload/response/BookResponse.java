package com.sagar.book.payload.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookResponse {

    private Long id;

    private String isbn;

    private String title;

    private Long authorId;
    private String authorName;

    private Long genreId;
    private String genreName;

    private Long publisherId;
    private String publisherName;

    private LocalDate publishDate;

    private String language;

    private String description;

    private Integer totalCopies;

    private Integer availableCopies;

    private BigDecimal price;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}