package com.sagar.user.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_user_email", columnList = "email"),
                @Index(name = "idx_user_phone", columnList = "phone"),
                @Index(name = "idx_user_provider", columnList = "auth_provider, provider_id")
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is mandatory")
    @Size(max = 50, message = "First name cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String firstName;

    @NotBlank(message = "Last name is mandatory")
    @Size(max = 50, message = "Last name cannot exceed 50 characters")
    @Column(nullable = false, length = 50)
    private String lastName;

    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "Phone number is mandatory")
    @Pattern(
            regexp = "^[6-9]\\d{9}$",
            message = "Invalid phone number"
    )
    @Column(nullable = false, unique = true, length = 10)
    private String phone;

    /*
     * Password is optional because OAuth2 users authenticate
     * through the external provider instead of a local password.
     *
     * For LOCAL users, this field will contain a BCrypt hash.
     * For OAuth2 users, this field can remain null.
     */
    @Column
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.MEMBER;

    /*
     * Identifies how the user authenticates with the application.
     *
     * LOCAL  -> Email/password authentication
     * GOOGLE -> Google OAuth2 authentication
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    @Builder.Default
    private AuthProvider authProvider = AuthProvider.LOCAL;

    /*
     * Unique identifier assigned by the external authentication provider.
     *
     * Example:
     * Google OAuth2 -> Google's "sub" claim
     *
     * This value is null for LOCAL users.
     */
    @Column(name = "provider_id", length = 100)
    private String providerId;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}