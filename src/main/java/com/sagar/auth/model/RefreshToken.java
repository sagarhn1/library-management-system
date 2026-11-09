package com.sagar.auth.model;

import com.sagar.user.model.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_token", columnList = "token"),
                @Index(name = "idx_refresh_token_user", columnList = "user_id")
        }
)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Unique refresh-token identifier/value.
     */
    @Column(nullable = false, unique = true, length = 500)
    private String token;

    /**
     * User associated with this refresh token.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Time after which the refresh token can no longer be used.
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Allows explicit invalidation during logout or token rotation.
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean revoked = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}