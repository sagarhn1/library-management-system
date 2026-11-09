package com.sagar.access.controller;

import com.sagar.access.payload.response.UserBookAccessResponse;
import com.sagar.access.service.UserBookAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/book-access")
public class UserBookAccessController {

    private final UserBookAccessService userBookAccessService;

    /**
     * Requests access to a FREE book.
     */
    @PostMapping("/books/{bookId}")
    public ResponseEntity<UserBookAccessResponse> requestFreeBookAccess(
            @PathVariable Long bookId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userBookAccessService.requestFreeBookAccess(
                        bookId,
                        authentication.getName()
                )
        );
    }

    /**
     * Gets the current user's access details for a book.
     */
    @GetMapping("/books/{bookId}")
    public ResponseEntity<UserBookAccessResponse> getBookAccess(
            @PathVariable Long bookId,
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userBookAccessService.getBookAccess(
                        bookId,
                        authentication.getName()
                )
        );
    }

    /**
     * Gets all books currently accessible by the logged-in user.
     */
    @GetMapping("/my-books")
    public ResponseEntity<List<UserBookAccessResponse>> getMyAccessibleBooks(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userBookAccessService.getMyAccessibleBooks(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/my-library")
    public ResponseEntity<List<UserBookAccessResponse>> getMyLibrary(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                userBookAccessService.getMyAccessibleBooks(
                        authentication.getName()
                )
        );
    }

    @PostMapping("/books/{bookId}/free-access")
    public ResponseEntity<String> grantFreeBookAccess(
            @PathVariable Long bookId,
            Authentication authentication
    ) {

        userBookAccessService.grantFreeBookAccess(
                bookId,
                authentication.getName()
        );

        return ResponseEntity.ok(
                "Free book access granted successfully"
        );
    }
}