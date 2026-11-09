package com.sagar.publisher.controller;

import com.sagar.publisher.payload.request.CreatePublisherRequest;
import com.sagar.publisher.payload.request.UpdatePublisherRequest;
import com.sagar.publisher.payload.response.PublisherResponse;
import com.sagar.publisher.service.PublisherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/publishers")
public class PublisherController {

    private final PublisherService publisherService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<PublisherResponse> createPublisher(
            @Valid @RequestBody CreatePublisherRequest request
    ) {

        return ResponseEntity.ok(
                publisherService.createPublisher(request)
        );
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<PublisherResponse>> getAllPublishers() {

        return ResponseEntity.ok(
                publisherService.getAllPublishers()
        );
    }

    @GetMapping("/{publisherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<PublisherResponse> getPublisherById(
            @PathVariable Long publisherId
    ) {

        return ResponseEntity.ok(
                publisherService.getPublisherById(publisherId)
        );
    }

    @PutMapping("/{publisherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<PublisherResponse> updatePublisher(
            @PathVariable Long publisherId,
            @Valid @RequestBody UpdatePublisherRequest request
    ) {

        return ResponseEntity.ok(
                publisherService.updatePublisher(
                        publisherId,
                        request
                )
        );
    }

    @DeleteMapping("/{publisherId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletePublisher(
            @PathVariable Long publisherId
    ) {

        publisherService.deletePublisher(publisherId);

        return ResponseEntity.ok(
                "Publisher deleted successfully"
        );
    }

    @PatchMapping("/{publisherId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> activatePublisher(
            @PathVariable Long publisherId
    ) {

        publisherService.activatePublisher(publisherId);

        return ResponseEntity.ok(
                "Publisher activated successfully"
        );
    }

    @PatchMapping("/{publisherId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<String> deactivatePublisher(
            @PathVariable Long publisherId
    ) {

        publisherService.deactivatePublisher(publisherId);

        return ResponseEntity.ok(
                "Publisher deactivated successfully"
        );
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN', 'MEMBER')")
    public ResponseEntity<List<PublisherResponse>> getAllActivePublishers() {

        return ResponseEntity.ok(
                publisherService.getAllActivePublishers()
        );
    }
}