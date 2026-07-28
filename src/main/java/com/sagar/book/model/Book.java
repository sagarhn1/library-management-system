package com.sagar.book.model;

import com.sagar.author.model.Author;
import com.sagar.genre.model.Genre;
import com.sagar.publisher.model.Publisher;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "ISBN is mandatory")
    @Column(nullable = false, unique = true)
    private String isbn;

    @NotBlank(message = "Book title is mandatory")
    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id", nullable = false)
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    private LocalDate publishDate;

    @NotBlank(message = "Language is mandatory")
    private String language;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    @Column(length = 1000)
    private String description;

    @NotNull(message = "Total copies is mandatory")
    @Min(value = 1, message = "Total copies must be at least 1")
    @Column(nullable = false)
    private Integer totalCopies;

    @NotNull(message = "Available copies is mandatory")
    @Min(value = 0, message = "Available copies cannot be negative")
    @Column(nullable = false)
    private Integer availableCopies;

    @DecimalMin(value = "0.0", inclusive = true,
            message = "Price cannot be negative")
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @AssertTrue(message = "Available copies cannot exceed total copies")
    public boolean isAvailableCopiesValid() {

        if (totalCopies == null || availableCopies == null) {
            return true;
        }

        return availableCopies <= totalCopies;
    }
}