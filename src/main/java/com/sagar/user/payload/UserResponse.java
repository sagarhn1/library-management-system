package com.sagar.user.payload;

import com.sagar.user.model.Role;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private Role role;

    private Boolean active;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}