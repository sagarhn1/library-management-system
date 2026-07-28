package com.sagar.genre.service.impl;

import com.sagar.exception.BadRequestException;
import com.sagar.exception.DuplicateResourceException;
import com.sagar.exception.ResourceNotFoundException;
import com.sagar.genre.mapper.GenreMapper;
import com.sagar.genre.model.Genre;
import com.sagar.genre.payload.request.CreateGenreRequest;
import com.sagar.genre.payload.request.UpdateGenreRequest;
import com.sagar.genre.payload.response.GenreResponse;
import com.sagar.genre.repository.GenreRepository;
import com.sagar.genre.service.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {

    private final GenreMapper genreMapper;
    private final GenreRepository genreRepository;

    @Override
    public GenreResponse createGenre(CreateGenreRequest createGenreRequest) {

        Genre parentGenre = null;

        if (createGenreRequest.getParentGenreId() != null) {
            parentGenre = genreRepository.findById(createGenreRequest.getParentGenreId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Parent genre not found"));
        }

        if (genreRepository.existsByCode(createGenreRequest.getCode())) {
            throw new DuplicateResourceException("Genre code already exists");
        }


        int displayOrder = (int) genreRepository.countByParentGenre(parentGenre) + 1;

        Genre genre = genreMapper.toEntity(createGenreRequest);

        genre.setParentGenre(parentGenre);
        genre.setDisplayOrder(displayOrder);

        Genre savedGenre = genreRepository.save(genre);

        return genreMapper.toResponse(savedGenre);
    }

    @Override
    public List<GenreResponse> getAllGenres() {

        List<Genre> genres = genreRepository.findAllByOrderByDisplayOrderAsc();

        return genres.stream()
                .map(genreMapper::toResponse)
                .toList();
    }

    @Override
    public GenreResponse getGenreById(Long genreId) {

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Genre not found with id : " + genreId));

        return genreMapper.toResponse(genre);
    }

    @Override
    public GenreResponse updateGenre(Long genreId,
                                     UpdateGenreRequest request) {

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Genre not found with id : " + genreId));

        if (!genre.getCode().equals(request.getCode())
                && genreRepository.existsByCode(request.getCode())) {

            throw new DuplicateResourceException(
                    "Genre code already exists");
        }

        Genre parentGenre = null;

        if (request.getParentGenreId() != null) {

            if (request.getParentGenreId().equals(genreId)) {
                throw new BadRequestException(
                        "Genre cannot be its own parent");
            }

            parentGenre = genreRepository.findById(request.getParentGenreId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Parent genre not found"));
        }

        genreMapper.updateEntity(
                genre,
                request,
                parentGenre
        );

        Genre updatedGenre = genreRepository.save(genre);

        return genreMapper.toResponse(updatedGenre);
    }

    @Override
    public void deleteGenre(Long genreId) {

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Genre not found with id : " + genreId));

        if (genreRepository.existsByParentGenreId(genreId)) {
            throw new BadRequestException(
                    "Cannot delete a genre that has sub genres");
        }

        // Later
        // if (bookRepository.existsByGenreId(genreId)) {
        //     throw new BadRequestException(
        //             "Cannot delete a genre that contains books");
        // }

        genreRepository.delete(genre);
    }

    @Override
    public List<GenreResponse> getTopLevelGenres() {

        return genreRepository.findByParentGenreIsNullOrderByDisplayOrderAsc()
                .stream()
                .map(genreMapper::toResponse)
                .toList();
    }

    @Override
    public List<GenreResponse> getSubGenres(Long parentGenreId) {

        if(!genreRepository.existsById(parentGenreId)) {
            throw new ResourceNotFoundException(
                    "Genre not found with id : " + parentGenreId);
        }

        return genreRepository.findByParentGenreIdOrderByDisplayOrderAsc(parentGenreId)
                .stream()
                .map(genreMapper::toResponse)
                .toList();
    }

    @Override
    public List<GenreResponse> getAllActiveGenres() {

        return genreRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .map(genreMapper::toResponse)
                .toList();
    }

    @Override
    public void deactivateGenre(Long genreId) {

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Genre not found with id : " + genreId));

        if (!genre.getActive()) {
            throw new BadRequestException(
                    "Genre is already inactive");
        }

        genre.setActive(false);

        genreRepository.save(genre);
    }

    @Override
    public void activateGenre(Long genreId) {

        Genre genre = genreRepository.findById(genreId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Genre not found with id : " + genreId));

        if (genre.getActive()) {
            throw new BadRequestException(
                    "Genre is already active");
        }

        genre.setActive(true);

        genreRepository.save(genre);
    }


}