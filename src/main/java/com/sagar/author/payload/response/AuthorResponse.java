package com.sagar.author.payload.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthorResponse {

    private Long id;

    private String name;

    private String biography;

    private LocalDate dateOfBirth;

    private String nationality;

    private String email;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
