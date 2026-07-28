package com.sagar.genre.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "genres")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Genre code is mandatory")
    @Size(max=20)
    @Column(nullable = false, unique = true ,length = 20)
    private String code;

    @NotBlank(message = "Genre name is mandatory")
    @Size(max=100)
    @Column(nullable = false)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Min(value = 0, message = "Display order cannot be negative")
    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_genre_id")
    @JsonBackReference
    private Genre parentGenre;

    @OneToMany(
            mappedBy = "parentGenre",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @JsonManagedReference
    private List<Genre> subGenres = new ArrayList<>();

     // @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL)
//    @JsonManagedReference
//    private List<Book> books = new ArrayList<>();
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}