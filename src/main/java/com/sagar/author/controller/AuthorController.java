package com.sagar.author.controller;

import com.sagar.author.payload.request.CreateAuthorRequest;
import com.sagar.author.payload.request.UpdateAuthorRequest;
import com.sagar.author.payload.response.AuthorResponse;
import com.sagar.author.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    /**
     * Creates a new author.
     *
     * Only ADMIN and LIBRARIAN can create authors.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<AuthorResponse> createAuthor(
            @Valid @RequestBody CreateAuthorRequest request
    ) {

        return ResponseEntity.ok(
                authorService.createAuthor(request)
        );
    }

    /**
     * Retrieves all authors.
     *
     * Accessible to all authenticated users.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {

        return ResponseEntity.ok(
                authorService.getAllAuthors()
        );
    }

    /**
     * Retrieves an author by ID.
     *
     * Accessible to all authenticated users.
     */
    @GetMapping("/{authorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<AuthorResponse> getAuthorById(
            @PathVariable Long authorId
    ) {

        return ResponseEntity.ok(
                authorService.getAuthorById(authorId)
        );
    }

    /**
     * Updates an existing author.
     *
     * Only ADMIN and LIBRARIAN can update authors.
     */
    @PutMapping("/{authorId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable Long authorId,
            @Valid @RequestBody UpdateAuthorRequest request
    ) {

        return ResponseEntity.ok(
                authorService.updateAuthor(authorId, request)
        );
    }

    /**
     * Deletes an author.
     *
     * Only ADMIN can permanently delete authors.
     */
    @DeleteMapping("/{authorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAuthor(
            @PathVariable Long authorId
    ) {

        authorService.deleteAuthor(authorId);

        return ResponseEntity.ok(
                "Author deleted successfully"
        );
    }

    /**
     * Activates an author.
     *
     * Only ADMIN and LIBRARIAN can change author status.
     */
    @PatchMapping("/{authorId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> activateAuthor(
            @PathVariable Long authorId
    ) {

        authorService.activateAuthor(authorId);

        return ResponseEntity.ok(
                "Author activated successfully"
        );
    }

    /**
     * Deactivates an author.
     *
     * Only ADMIN and LIBRARIAN can change author status.
     */
    @PatchMapping("/{authorId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> deactivateAuthor(
            @PathVariable Long authorId
    ) {

        authorService.deactivateAuthor(authorId);

        return ResponseEntity.ok(
                "Author deactivated successfully"
        );
    }

    /**
     * Retrieves all active authors.
     *
     * Accessible to all authenticated users.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<AuthorResponse>> getAllActiveAuthors() {

        return ResponseEntity.ok(
                authorService.getAllActiveAuthors()
        );
    }
}