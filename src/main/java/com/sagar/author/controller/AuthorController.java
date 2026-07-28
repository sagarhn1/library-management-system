package com.sagar.author.controller;

import com.sagar.author.payload.request.CreateAuthorRequest;
import com.sagar.author.payload.request.UpdateAuthorRequest;
import com.sagar.author.payload.response.AuthorResponse;
import com.sagar.author.service.AuthorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService authorService;

    @PostMapping
    public ResponseEntity<AuthorResponse> createAuthor(
            @Valid @RequestBody CreateAuthorRequest request) {

        return ResponseEntity.ok(
                authorService.createAuthor(request)
        );
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponse>> getAllAuthors() {

        return ResponseEntity.ok(
                authorService.getAllAuthors()
        );
    }

    @GetMapping("/{authorId}")
    public ResponseEntity<AuthorResponse> getAuthorById(
            @PathVariable Long authorId) {

        return ResponseEntity.ok(
                authorService.getAuthorById(authorId)
        );
    }

    @PutMapping("/{authorId}")
    public ResponseEntity<AuthorResponse> updateAuthor(
            @PathVariable Long authorId,
            @Valid @RequestBody UpdateAuthorRequest request) {

        return ResponseEntity.ok(
                authorService.updateAuthor(authorId, request)
        );
    }

    @DeleteMapping("/{authorId}")
    public ResponseEntity<String> deleteAuthor(
            @PathVariable Long authorId) {

        authorService.deleteAuthor(authorId);

        return ResponseEntity.ok("Author deleted successfully");
    }

    @PatchMapping("/{authorId}/activate")
    public ResponseEntity<String> activateAuthor(
            @PathVariable Long authorId) {

        authorService.activateAuthor(authorId);

        return ResponseEntity.ok("Author activated successfully");
    }

    @PatchMapping("/{authorId}/deactivate")
    public ResponseEntity<String> deactivateAuthor(
            @PathVariable Long authorId) {

        authorService.deactivateAuthor(authorId);

        return ResponseEntity.ok("Author deactivated successfully");
    }

    @GetMapping("/active")
    public ResponseEntity<List<AuthorResponse>> getAllActiveAuthors() {

        return ResponseEntity.ok(
                authorService.getAllActiveAuthors()
        );
    }
}