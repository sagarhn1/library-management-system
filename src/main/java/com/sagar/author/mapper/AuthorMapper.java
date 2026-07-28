package com.sagar.author.mapper;

import com.sagar.author.model.Author;
import com.sagar.author.payload.request.CreateAuthorRequest;
import com.sagar.author.payload.request.UpdateAuthorRequest;
import com.sagar.author.payload.response.AuthorResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

    public Author toEntity(CreateAuthorRequest request) {

        return Author.builder()
                .name(request.getName())
                .biography(request.getBiography())
                .dateOfBirth(request.getDateOfBirth())
                .nationality(request.getNationality())
                .email(request.getEmail())
                .build();
    }

    public void updateEntity(
            Author author,
            UpdateAuthorRequest request) {

        author.setName(request.getName());
        author.setBiography(request.getBiography());
        author.setDateOfBirth(request.getDateOfBirth());
        author.setNationality(request.getNationality());
        author.setEmail(request.getEmail());
        author.setActive(request.getActive());
    }

    public AuthorResponse toResponse(Author author) {

        return AuthorResponse.builder()
                .id(author.getId())
                .name(author.getName())
                .biography(author.getBiography())
                .dateOfBirth(author.getDateOfBirth())
                .nationality(author.getNationality())
                .email(author.getEmail())
                .active(author.getActive())
                .createdAt(author.getCreatedAt())
                .updatedAt(author.getUpdatedAt())
                .build();
    }
}