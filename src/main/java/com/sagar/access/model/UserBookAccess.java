package com.sagar.access.model;

import com.sagar.book.model.Book;
import com.sagar.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "user_book_access",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_book_access",
                        columnNames = {
                                "user_id",
                                "book_id"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * User who received access to the book.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "user_id",
            nullable = false
    )
    private User user;

    /*
     * Book the user is allowed to access.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "book_id",
            nullable = false
    )
    private Book book;

    /*
     * When access was granted.
     */
    @Column(nullable = false)
    private LocalDateTime accessGrantedAt;

    /*
     * Access becomes invalid after this time.
     */
    @Column(nullable = false)
    private LocalDateTime accessExpiresAt;

    /*
     * Allows admin to manually revoke access before expiry.
     */
    @Builder.Default
    @Column(nullable = false)
    private Boolean revoked = false;

    @CreationTimestamp
    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}