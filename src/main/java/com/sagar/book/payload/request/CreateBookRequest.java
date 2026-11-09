package com.sagar.book.payload.request;

import com.sagar.book.model.BookAccessType;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateBookRequest {

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Title is required")
    private String title;

    @NotNull(message = "Author is required")
    private Long authorId;

    @NotNull(message = "Genre is required")
    private Long genreId;

    @NotNull(message = "Publisher is required")
    private Long publisherId;

    private LocalDate publishDate;

    @NotBlank(message = "Language is required")
    private String language;

    private String description;

    @NotNull(message = "Access type is required")
    private BookAccessType accessType;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Price cannot be negative"
    )
    private BigDecimal price;

    @NotNull(message = "Access duration is required")
    @Min(
            value = 1,
            message = "Access duration must be at least 1 day"
    )
    private Integer accessDurationDays;

    @AssertTrue(
            message = "FREE books must have zero price and "
                    + "PAID books must have a price greater than zero"
    )
    public boolean isPriceValid() {

        if (accessType == null) {
            return true;
        }

        if (accessType == BookAccessType.FREE) {
            return price == null
                    || price.compareTo(BigDecimal.ZERO) == 0;
        }

        return price != null
                && price.compareTo(BigDecimal.ZERO) > 0;
    }
}