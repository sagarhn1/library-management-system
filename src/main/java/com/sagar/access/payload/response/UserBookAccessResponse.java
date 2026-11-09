package com.sagar.access.payload.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserBookAccessResponse {

    private Long id;

    private Long userId;

    private Long bookId;

    private String bookTitle;

    private LocalDateTime accessGrantedAt;

    private LocalDateTime accessExpiresAt;

    private Boolean revoked;

    private Boolean active;
}