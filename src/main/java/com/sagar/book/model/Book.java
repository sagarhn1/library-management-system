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
@Table(
        name = "books",
        indexes = {
                @Index(
                        name = "idx_book_isbn",
                        columnList = "isbn"
                ),
                @Index(
                        name = "idx_book_title",
                        columnList = "title"
                )
        }
)
public class Book {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @NotBlank(message = "ISBN is mandatory")
    @Column(
            nullable = false,
            unique = true,
            length = 20
    )
    private String isbn;

    @NotBlank(message = "Book title is mandatory")
    @Column(nullable = false)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "author_id",
            nullable = false
    )
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "genre_id",
            nullable = false
    )
    private Genre genre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "publisher_id",
            nullable = false
    )
    private Publisher publisher;

    private LocalDate publishDate;

    @NotBlank(message = "Language is mandatory")
    private String language;

    @Size(
            max = 1000,
            message = "Description cannot exceed 1000 characters"
    )
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull(message = "Access type is mandatory")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookAccessType accessType;

    @DecimalMin(
            value = "0.0",
            inclusive = true,
            message = "Price cannot be negative"
    )
    @Column(
            precision = 10,
            scale = 2
    )
    private BigDecimal price;

    @NotNull(message = "Access duration is mandatory")
    @Min(
            value = 1,
            message = "Access duration must be at least 1 day"
    )
    @Column(nullable = false)
    private Integer accessDurationDays;


    /*
     * Initially null because the book metadata is created
     * before the e-book PDF is uploaded.
     *
     * This is updated by the e-book upload API.
     */
    @Column(unique = true)
    private String ebookFileKey;


    /*
     * E-book file metadata.
     */
    @Column(name = "ebook_file_name")
    private String ebookFileName;

    @Column(name = "ebook_file_path")
    private String ebookFilePath;

    @Column(name = "ebook_content_type")
    private String ebookContentType;

    @Column(name = "ebook_file_size")
    private Long ebookFileSize;


    /*
     * Cover image metadata.
     * All fields are optional.
     */
    @Column(length = 500)
    private String coverImageFileName;

    @Column(length = 1000)
    private String coverImagePath;

    @Column(length = 100)
    private String coverImageContentType;

    private Long coverImageFileSize;


    /*
     * New books are inactive by default.
     *
     * A book should be activated only after the
     * required e-book file is uploaded.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = false;


    @CreationTimestamp
    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    /**
     * FREE books must have zero price.
     * PAID books must have a price greater than zero.
     */
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
                    || price.compareTo(
                    BigDecimal.ZERO
            ) == 0;
        }

        return price != null
                && price.compareTo(
                BigDecimal.ZERO
        ) > 0;
    }
}