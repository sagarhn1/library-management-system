package com.sagar.author.service;

import com.sagar.author.payload.request.CreateAuthorRequest;
import com.sagar.author.payload.request.UpdateAuthorRequest;
import com.sagar.author.payload.response.AuthorResponse;

import java.util.List;

public interface AuthorService {

    AuthorResponse createAuthor(CreateAuthorRequest request);

    List<AuthorResponse> getAllAuthors();

    AuthorResponse getAuthorById(Long authorId);

    AuthorResponse updateAuthor(Long authorId,
                                UpdateAuthorRequest request);

    void deleteAuthor(Long authorId);

    void activateAuthor(Long authorId);

    void deactivateAuthor(Long authorId);

    List<AuthorResponse> getAllActiveAuthors();
}