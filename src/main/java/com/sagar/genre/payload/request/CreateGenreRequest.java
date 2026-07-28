package com.sagar.genre.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGenreRequest {

    @NotBlank(message = "Genre code is mandatory")
    private String code;

    @NotBlank(message = "Genre name is mandatory")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private Boolean active = true;

    private Long parentGenreId;
}