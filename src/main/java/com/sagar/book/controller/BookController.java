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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    /**
     * Creates a new book.
     * Accessible only to administrators and librarians.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody CreateBookRequest request
    ) {

        return ResponseEntity.ok(
                bookService.createBook(request)
        );
    }

    /**
     * Retrieves all books with pagination.
     * Accessible to all authenticated users.
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookResponse>> getAllBooks(
            Pageable pageable,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bookService.getAllBooks(
                        pageable,
                        authentication.getName()
                )
        );
    }

    /**
     * Retrieves a book by its identifier.
     * Accessible to all authenticated users.
     */
    @GetMapping("/{bookId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookResponse> getBookById(
            @PathVariable Long bookId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bookService.getBookById(
                        bookId,
                        authentication.getName()
                )
        );
    }

    /**
     * Retrieves a book using its ISBN.
     * Accessible to all authenticated users.
     */
    @GetMapping("/isbn/{isbn}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookResponse> getBookByIsbn(
            @PathVariable String isbn,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bookService.getBookByIsbn(
                        isbn,
                        authentication.getName()
                )
        );
    }

    /**
     * Updates an existing book.
     * Accessible only to administrators and librarians.
     */
    @PutMapping("/{bookId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponse> updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateBookRequest request
    ) {

        return ResponseEntity.ok(
                bookService.updateBook(bookId, request)
        );
    }

    /**
     * Permanently deletes a book.
     * Accessible only to administrators.
     */
    @DeleteMapping("/{bookId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteBook(
            @PathVariable Long bookId
    ) {

        bookService.deleteBook(bookId);

        return ResponseEntity.ok(
                "Book deleted successfully"
        );
    }

    /**
     * Activates a book.
     * Accessible to administrators and librarians.
     */
    @PatchMapping("/{bookId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> activateBook(
            @PathVariable Long bookId
    ) {

        bookService.activateBook(bookId);

        return ResponseEntity.ok(
                "Book activated successfully"
        );
    }

    /**
     * Deactivates a book.
     * Accessible to administrators and librarians.
     */
    @PatchMapping("/{bookId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> deactivateBook(
            @PathVariable Long bookId
    ) {

        bookService.deactivateBook(bookId);

        return ResponseEntity.ok(
                "Book deactivated successfully"
        );
    }

    /**
     * Retrieves all active books.
     * Accessible to all authenticated users.
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookResponse>> getAllActiveBooks(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bookService.getAllActiveBooks(
                        authentication.getName()
                )
        );
    }

    /**
     * Searches books using dynamic filters with pagination.
     * Accessible to all authenticated users.
     */
    @PostMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<BookResponse>> searchBooks(
            @Valid @RequestBody BookSearchRequest request,
            Pageable pageable,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                bookService.searchBooks(
                        request,
                        pageable,
                        authentication.getName()
                )
        );
    }


    @PostMapping(
            value = "/{bookId}/ebook",
            consumes = "multipart/form-data"
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadEbook(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file
    ) {

        bookService.uploadEbook(bookId, file);

        return ResponseEntity.ok(
                "E-book uploaded successfully"
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{bookId}/read")
    public ResponseEntity<Resource> readEbook(
            @PathVariable Long bookId,
            Authentication authentication
    ) {

        Resource resource = bookService.readEbook(
                bookId,
                authentication
        );

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline"
                )
                .body(resource);
    }

    @PostMapping(
            value = "/{bookId}/cover",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> uploadCover(
            @PathVariable Long bookId,
            @RequestParam("file") MultipartFile file
    ) {

        bookService.uploadCover(bookId, file);

        return ResponseEntity.ok(
                "Book cover uploaded successfully"
        );
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{bookId}/cover")
    public ResponseEntity<Resource> getBookCover(
            @PathVariable Long bookId
    ) {

        Resource resource = bookService.getBookCover(bookId);

        return ResponseEntity.ok()
                .body(resource);
    }
}