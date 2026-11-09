package com.sagar.access.service;

import com.sagar.access.payload.response.UserBookAccessResponse;

import java.util.List;

public interface UserBookAccessService {

    /**
     * Grants access to a free book for the currently authenticated user.
     *
     * @param bookId book identifier
     * @param userEmail authenticated user's email
     * @return granted book access details
     */
    UserBookAccessResponse requestFreeBookAccess(
            Long bookId,
            String userEmail
    );

    /**
     * Returns the access details of a specific user for a book.
     */
    UserBookAccessResponse getBookAccess(
            Long bookId,
            String userEmail
    );

    /**
     * Returns all books the user currently has access to.
     */
    List<UserBookAccessResponse> getMyAccessibleBooks(
            String userEmail
    );

    void grantFreeBookAccess(
            Long bookId,
            String userEmail
    );
}