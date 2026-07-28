package com.sagar.publisher.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePublisherRequest {

    @NotBlank(message = "Publisher name is required")
    private String name;

    private String address;

    @Email(message = "Invalid email")
    private String email;

    private String phone;

    private String website;
}