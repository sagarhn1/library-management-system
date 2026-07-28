package com.sagar.book.service;

import com.sagar.book.payload.request.BookSearchRequest;
import com.sagar.book.payload.request.CreateBookRequest;
import com.sagar.book.payload.request.UpdateBookRequest;
import com.sagar.book.payload.response.BookResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    BookResponse createBook(CreateBookRequest request);

    Page<BookResponse> getAllBooks(Pageable pageable);

    BookResponse getBookById(Long bookId);

    BookResponse getBookByIsbn(String isbn);

    BookResponse updateBook(Long bookId,
                            UpdateBookRequest request);

    void deleteBook(Long bookId);

    void activateBook(Long bookId);

    void deactivateBook(Long bookId);

    List<BookResponse> getAllActiveBooks();

    Page<BookResponse> searchBooks(
            BookSearchRequest request,
            Pageable pageable);
}