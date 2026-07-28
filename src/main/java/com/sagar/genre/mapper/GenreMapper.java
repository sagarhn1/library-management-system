package com.sagar.genre.mapper;

import com.sagar.genre.model.Genre;
import com.sagar.genre.payload.request.CreateGenreRequest;
import com.sagar.genre.payload.request.UpdateGenreRequest;
import com.sagar.genre.payload.response.GenreResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class GenreMapper {

    public GenreResponse toResponse(Genre savedGenre) {

        return GenreResponse.builder()
                .id(savedGenre.getId())
                .code(savedGenre.getCode())
                .name(savedGenre.getName())
                .description(savedGenre.getDescription())
                .displayOrder(savedGenre.getDisplayOrder())
                .active(savedGenre.getActive())
                .parentGenreId(
                        savedGenre.getParentGenre() != null
                                ? savedGenre.getParentGenre().getId()
                                : null
                )
                .subGenreIds(
                        savedGenre.getSubGenres() == null
                                ? List.of()
                                : savedGenre.getSubGenres()
                                .stream()
                                .map(Genre::getId)
                                .toList()
                )
                .createdAt(savedGenre.getCreatedAt())
                .updatedAt(savedGenre.getUpdatedAt())
                .build();
    }

    public void updateEntity(
            Genre genre,
            UpdateGenreRequest request,
            Genre parentGenre) {

        genre.setCode(request.getCode());
        genre.setName(request.getName());
        genre.setDescription(request.getDescription());
        genre.setActive(request.getActive());
        genre.setParentGenre(parentGenre);
    }

    public Genre toEntity(CreateGenreRequest request) {

        return Genre.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .active(request.getActive())
                .build();
    }
}