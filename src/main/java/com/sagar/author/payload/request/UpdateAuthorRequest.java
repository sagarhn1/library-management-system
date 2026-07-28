package com.sagar.author.payload.request;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateAuthorRequest {

    private String name;

    private String biography;

    private LocalDate dateOfBirth;

    private String nationality;

    private String email;

    private Boolean active;
}
