package com.sagar.author.service;

import com.sagar.author.mapper.AuthorMapper;
import com.sagar.author.model.Author;
import com.sagar.author.payload.request.CreateAuthorRequest;
import com.sagar.author.payload.request.UpdateAuthorRequest;
import com.sagar.author.payload.response.AuthorResponse;
import com.sagar.author.repository.AuthorRepository;
import com.sagar.exception.BadRequestException;
import com.sagar.exception.DuplicateResourceException;
import com.sagar.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    @Override
    public AuthorResponse createAuthor(CreateAuthorRequest request) {

        if (authorRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Author email already exists");
        }

        Author author = authorMapper.toEntity(request);

        Author savedAuthor = authorRepository.save(author);

        return authorMapper.toResponse(savedAuthor);
    }

    @Override
    public List<AuthorResponse> getAllAuthors() {

        return authorRepository.findAll()
                .stream()
                .map(authorMapper::toResponse)
                .toList();
    }

    @Override
    public AuthorResponse getAuthorById(Long authorId) {

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found with id : " + authorId));

        return authorMapper.toResponse(author);
    }

    @Override
    public AuthorResponse updateAuthor(Long authorId,
                                       UpdateAuthorRequest request) {

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found with id : " + authorId));

        if (!author.getEmail().equals(request.getEmail())
                && authorRepository.existsByEmail(request.getEmail())) {

            throw new DuplicateResourceException(
                    "Author email already exists");
        }

        authorMapper.updateEntity(author, request);

        Author updatedAuthor = authorRepository.save(author);

        return authorMapper.toResponse(updatedAuthor);
    }

    @Override
    public void deleteAuthor(Long authorId) {

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found with id : " + authorId));

        authorRepository.delete(author);
    }

    @Override
    public void activateAuthor(Long authorId) {

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found with id : " + authorId));

        if (author.getActive()) {
            throw new BadRequestException(
                    "Author is already active");
        }

        author.setActive(true);

        authorRepository.save(author);
    }

    @Override
    public void deactivateAuthor(Long authorId) {

        Author author = authorRepository.findById(authorId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found with id : " + authorId));

        if (!author.getActive()) {
            throw new BadRequestException(
                    "Author is already inactive");
        }

        author.setActive(false);

        authorRepository.save(author);
    }

    @Override
    public List<AuthorResponse> getAllActiveAuthors() {

        return authorRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(authorMapper::toResponse)
                .toList();
    }
}