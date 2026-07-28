package com.sagar.book.payload.request;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookSearchRequest {

    private String keyword;

    private Long authorId;

    private Long genreId;

    private Long publisherId;

    private String language;

    private Boolean active;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;
}