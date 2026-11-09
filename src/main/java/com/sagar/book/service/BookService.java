package com.sagar.book.service;

import com.sagar.book.payload.request.BookSearchRequest;
import com.sagar.book.payload.request.CreateBookRequest;
import com.sagar.book.payload.request.UpdateBookRequest;
import com.sagar.book.payload.response.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import java.util.List;

public interface BookService {

    BookResponse createBook(CreateBookRequest request);

    Page<BookResponse> getAllBooks(
            Pageable pageable,
            String userEmail
    );

    BookResponse getBookById(
            Long bookId,
            String userEmail
    );

    BookResponse getBookByIsbn(
            String isbn,
            String userEmail
    );

    BookResponse updateBook(Long bookId,
                            UpdateBookRequest request);

    void deleteBook(Long bookId);

    void activateBook(Long bookId);

    void deactivateBook(Long bookId);

    List<BookResponse> getAllActiveBooks(
            String userEmail
    );

    Page<BookResponse> searchBooks(
            BookSearchRequest request,
            Pageable pageable,
            String userEmail
    );

    void uploadEbook(
            Long bookId,
            MultipartFile file
    );

    Resource readEbook(
            Long bookId,
            Authentication authentication
    );

    void uploadCover(
            Long bookId,
            MultipartFile file
    );

    Resource getBookCover(Long bookId);

    ResponseEntity<Resource> readEbook(
            Long bookId,
            String userEmail
    );
}