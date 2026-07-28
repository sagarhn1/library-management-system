package com.sagar.genre.controller;

import com.sagar.genre.payload.request.CreateGenreRequest;
import com.sagar.genre.payload.request.UpdateGenreRequest;
import com.sagar.genre.payload.response.GenreResponse;
import com.sagar.genre.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/genres")
public class GenreController {

    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<GenreResponse> addGenre(@Valid @RequestBody CreateGenreRequest createGenreRequest){
        GenreResponse createdGenre=genreService.createGenre(createGenreRequest);
        return ResponseEntity.ok(createdGenre);
    }

    @GetMapping
    public ResponseEntity<List<GenreResponse>> getAllGenres(){
        return ResponseEntity.ok(genreService.getAllGenres());
    }

    @GetMapping("/{genreId}")
    public ResponseEntity<GenreResponse> getGenreById(
            @PathVariable Long genreId) {

        return ResponseEntity.ok(
                genreService.getGenreById(genreId)
        );
    }

    @PutMapping("/{genreId}")
    public ResponseEntity<GenreResponse> updateGenre(
            @PathVariable Long genreId,
            @Valid @RequestBody UpdateGenreRequest request) {

        return ResponseEntity.ok(
                genreService.updateGenre(genreId, request)
        );
    }

    @DeleteMapping("/{genreId}")
    public ResponseEntity<String> deleteGenre(
            @PathVariable Long genreId) {

        genreService.deleteGenre(genreId);

        return ResponseEntity.ok("Genre deleted successfully");
    }

    @GetMapping("/top-level")
    public ResponseEntity<List<GenreResponse>> getTopLevelGenres() {

        return ResponseEntity.ok(
                genreService.getTopLevelGenres()
        );
    }

    @GetMapping("/{parentGenreId}/subgenres")
    public ResponseEntity<List<GenreResponse>> getSubGenres(
            @PathVariable Long parentGenreId) {

        return ResponseEntity.ok(
                genreService.getSubGenres(parentGenreId)
        );
    }

    @GetMapping("/active")
    public ResponseEntity<List<GenreResponse>> getAllActiveGenres() {

        return ResponseEntity.ok(
                genreService.getAllActiveGenres()
        );
    }

    @PatchMapping("/{genreId}/deactivate")
    public ResponseEntity<String> deactivateGenre(
            @PathVariable Long genreId) {

        genreService.deactivateGenre(genreId);

        return ResponseEntity.ok("Genre deactivated successfully");
    }

    @PatchMapping("/{genreId}/activate")
    public ResponseEntity<String> activateGenre(
            @PathVariable Long genreId) {

        genreService.activateGenre(genreId);

        return ResponseEntity.ok("Genre activated successfully");
    }
}
