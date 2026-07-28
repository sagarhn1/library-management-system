package com.sagar.genre.service;

import com.sagar.genre.payload.request.CreateGenreRequest;
import com.sagar.genre.payload.request.UpdateGenreRequest;
import com.sagar.genre.payload.response.GenreResponse;

import java.util.List;

public interface GenreService {

    GenreResponse createGenre(CreateGenreRequest createGenreRequest);

    List<GenreResponse> getAllGenres();

    GenreResponse getGenreById(Long genreId);

    GenreResponse updateGenre(Long genreId,
                              UpdateGenreRequest request);

    void deleteGenre(Long genreId);

    List<GenreResponse> getTopLevelGenres();

    List<GenreResponse> getSubGenres(Long parentGenreId);

    List<GenreResponse> getAllActiveGenres();

    void deactivateGenre(Long genreId);

    void activateGenre(Long genreId);

}
