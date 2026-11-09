package com.sagar.book.payload.response;

import com.sagar.book.model.BookAccessType;
import com.sagar.book.model.UserBookAction;
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

    /**
     * FREE or PAID
     */
    private BookAccessType accessType;

    /**
     * Price of the book.
     * FREE books have zero price.
     */
    private BigDecimal price;

    /**
     * Number of days the user can access the book
     * after access is granted.
     */
    private Integer accessDurationDays;

    /**
     * Public book cover image.
     */
    private String coverImageUrl;

    private UserBookAction userBookAction;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}