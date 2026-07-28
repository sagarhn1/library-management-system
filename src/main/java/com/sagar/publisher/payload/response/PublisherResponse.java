package com.sagar.publisher.payload.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PublisherResponse {

    private Long id;

    private String name;

    private String address;

    private String email;

    private String phone;

    private String website;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}