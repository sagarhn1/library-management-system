package com.sagar.book.controller;

import com.sagar.book.payload.request.BookSearchRequest;
import com.sagar.book.payload.request.CreateBookRequest;
import com.sagar.book.payload.request.UpdateBookRequest;
import com.sagar.book.payload.response.BookResponse;
import com.sagar.book.service.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    @PostMapping
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody CreateBookRequest request) {

        return ResponseEntity.ok(
                bookService.createBook(request)
        );
    }

    @GetMapping
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            Pageable pageable) {

        return ResponseEntity.ok(
                bookService.getAllBooks(pageable)
        );
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable Long bookId) {

        return ResponseEntity.ok(
                bookService.getBookById(bookId)
        );
    }

    @GetMapping("/isbn/{isbn}")
    public ResponseEntity<BookResponse> getBookByIsbn(
            @PathVariable String isbn) {

        return ResponseEntity.ok(
                bookService.getBookByIsbn(isbn)
        );
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateBookRequest request) {

        return ResponseEntity.ok(
                bookService.updateBook(bookId, request)
        );
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<String> deleteBook(
            @PathVariable Long bookId) {

        bookService.deleteBook(bookId);

        return ResponseEntity.ok(
                "Book deleted successfully"
        );
    }

    @PatchMapping("/{bookId}/activate")
    public ResponseEntity<String> activateBook(
            @PathVariable Long bookId) {

        bookService.activateBook(bookId);

        return ResponseEntity.ok(
                "Book activated successfully"
        );
    }

    @PatchMapping("/{bookId}/deactivate")
    public ResponseEntity<String> deactivateBook(
            @PathVariable Long bookId) {

        bookService.deactivateBook(bookId);

        return ResponseEntity.ok(
                "Book deactivated successfully"
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<BookResponse>> getAllActiveBooks() {

        return ResponseEntity.ok(
                bookService.getAllActiveBooks()
        );
    }

    @PostMapping("/search")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @Valid @RequestBody BookSearchRequest request,
            Pageable pageable) {

        return ResponseEntity.ok(
                bookService.searchBooks(request, pageable)
        );
    }
}