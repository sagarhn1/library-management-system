package com.sagar.genre.controller;

import com.sagar.genre.payload.request.CreateGenreRequest;
import com.sagar.genre.payload.request.UpdateGenreRequest;
import com.sagar.genre.payload.response.GenreResponse;
import com.sagar.genre.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    /**
     * Creates a new genre.
     *
     * Only ADMIN and LIBRARIAN can create genres.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<GenreResponse> addGenre(
            @Valid @RequestBody CreateGenreRequest createGenreRequest
    ) {

        GenreResponse createdGenre =
                genreService.createGenre(createGenreRequest);

        return ResponseEntity.ok(createdGenre);
    }

    /**
     * Retrieves all genres.
     *
     * Accessible to all authenticated users.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<GenreResponse>> getAllGenres() {

        return ResponseEntity.ok(
                genreService.getAllGenres()
        );
    }

    /**
     * Retrieves a genre by its ID.
     */
    @GetMapping("/{genreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<GenreResponse> getGenreById(
            @PathVariable Long genreId
    ) {

        return ResponseEntity.ok(
                genreService.getGenreById(genreId)
        );
    }

    /**
     * Updates an existing genre.
     *
     * Only ADMIN and LIBRARIAN can update genres.
     */
    @PutMapping("/{genreId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<GenreResponse> updateGenre(
            @PathVariable Long genreId,
            @Valid @RequestBody UpdateGenreRequest request
    ) {

        return ResponseEntity.ok(
                genreService.updateGenre(genreId, request)
        );
    }

    /**
     * Deletes a genre.
     *
     * Only ADMIN can permanently delete genres.
     */
    @DeleteMapping("/{genreId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteGenre(
            @PathVariable Long genreId
    ) {

        genreService.deleteGenre(genreId);

        return ResponseEntity.ok(
                "Genre deleted successfully"
        );
    }

    /**
     * Retrieves all top-level genres.
     */
    @GetMapping("/top-level")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<GenreResponse>> getTopLevelGenres() {

        return ResponseEntity.ok(
                genreService.getTopLevelGenres()
        );
    }

    /**
     * Retrieves all subgenres of a parent genre.
     */
    @GetMapping("/{parentGenreId}/subgenres")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<GenreResponse>> getSubGenres(
            @PathVariable Long parentGenreId
    ) {

        return ResponseEntity.ok(
                genreService.getSubGenres(parentGenreId)
        );
    }

    /**
     * Retrieves all active genres.
     */
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<GenreResponse>> getAllActiveGenres() {

        return ResponseEntity.ok(
                genreService.getAllActiveGenres()
        );
    }

    /**
     * Deactivates a genre.
     *
     * Only ADMIN and LIBRARIAN can change genre status.
     */
    @PatchMapping("/{genreId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> deactivateGenre(
            @PathVariable Long genreId
    ) {

        genreService.deactivateGenre(genreId);

        return ResponseEntity.ok(
                "Genre deactivated successfully"
        );
    }

    /**
     * Activates a genre.
     *
     * Only ADMIN and LIBRARIAN can change genre status.
     */
    @PatchMapping("/{genreId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> activateGenre(
            @PathVariable Long genreId
    ) {

        genreService.activateGenre(genreId);

        return ResponseEntity.ok(
                "Genre activated successfully"
        );
    }
}