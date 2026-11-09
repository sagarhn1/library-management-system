package com.sagar.access.service;

import com.sagar.access.model.UserBookAccess;
import com.sagar.access.payload.response.UserBookAccessResponse;
import com.sagar.access.repository.UserBookAccessRepository;
import com.sagar.book.model.Book;
import com.sagar.book.model.BookAccessType;
import com.sagar.book.repository.BookRepository;
import com.sagar.exception.BadRequestException;
import com.sagar.exception.ResourceNotFoundException;
import com.sagar.user.model.User;
import com.sagar.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserBookAccessServiceImpl
        implements UserBookAccessService {

    private final UserBookAccessRepository userBookAccessRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;

    @Override
    @Transactional
    public UserBookAccessResponse requestFreeBookAccess(
            Long bookId,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);
        Book book = getBookById(bookId);

        if (!Boolean.TRUE.equals(book.getActive())) {
            throw new BadRequestException(
                    "Book is currently inactive"
            );
        }

        if (book.getAccessType() != BookAccessType.FREE) {
            throw new BadRequestException(
                    "This book requires payment"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        UserBookAccess userBookAccess =
                userBookAccessRepository
                        .findByUserIdAndBookId(
                                user.getId(),
                                book.getId()
                        )
                        .orElse(null);

        // User already has valid access
        if (userBookAccess != null
                && !Boolean.TRUE.equals(userBookAccess.getRevoked())
                && userBookAccess.getAccessExpiresAt() != null
                && userBookAccess.getAccessExpiresAt().isAfter(now)) {

            throw new BadRequestException(
                    "You already have active access to this book"
            );
        }

        // No record exists -> create one
        if (userBookAccess == null) {
            userBookAccess = UserBookAccess.builder()
                    .user(user)
                    .book(book)
                    .build();
        }

        // Grant or renew access
        userBookAccess.setAccessGrantedAt(now);
        userBookAccess.setAccessExpiresAt(
                now.plusDays(book.getAccessDurationDays())
        );
        userBookAccess.setRevoked(false);

        UserBookAccess savedAccess =
                userBookAccessRepository.save(userBookAccess);

        return toResponse(savedAccess);
    }

    @Override
    public UserBookAccessResponse getBookAccess(
            Long bookId,
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        UserBookAccess access =
                userBookAccessRepository
                        .findByUserIdAndBookId(
                                user.getId(),
                                bookId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "No book access found"
                                )
                        );

        return toResponse(access);
    }

    @Override
    public List<UserBookAccessResponse> getMyAccessibleBooks(
            String userEmail
    ) {

        User user = getUserByEmail(userEmail);

        LocalDateTime now = LocalDateTime.now();

        return userBookAccessRepository
                .findByUserIdAndRevokedFalseAndAccessExpiresAtAfter(
                        user.getId(),
                        now
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private Book getBookById(Long bookId) {

        return bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: " + bookId
                        )
                );
    }

    private UserBookAccessResponse toResponse(
            UserBookAccess access
    ) {

        boolean active =
                !Boolean.TRUE.equals(access.getRevoked())
                        && LocalDateTime.now()
                        .isBefore(access.getAccessExpiresAt());

        return UserBookAccessResponse.builder()
                .id(access.getId())
                .userId(access.getUser().getId())
                .bookId(access.getBook().getId())
                .bookTitle(access.getBook().getTitle())
                .accessGrantedAt(access.getAccessGrantedAt())
                .accessExpiresAt(access.getAccessExpiresAt())
                .revoked(access.getRevoked())
                .active(active)
                .build();
    }


    @Override
    @Transactional
    public void grantFreeBookAccess(
            Long bookId,
            String userEmail
    ) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found"
                        )
                );

        if (!Boolean.TRUE.equals(book.getActive())) {
            throw new BadRequestException(
                    "Book is currently inactive"
            );
        }

        if (book.getAccessType() != BookAccessType.FREE) {
            throw new BadRequestException(
                    "This book requires payment"
            );
        }

        if (book.getEbookFileKey() == null
                || book.getEbookFileKey().isBlank()) {

            throw new BadRequestException(
                    "E-book file is not available"
            );
        }

        LocalDateTime now = LocalDateTime.now();

        UserBookAccess access =
                userBookAccessRepository
                        .findByUserIdAndBookId(
                                user.getId(),
                                book.getId()
                        )
                        .orElseGet(() ->
                                UserBookAccess.builder()
                                        .user(user)
                                        .book(book)
                                        .build()
                        );

        access.setAccessGrantedAt(now);

        access.setAccessExpiresAt(
                now.plusDays(
                        book.getAccessDurationDays()
                )
        );

        access.setRevoked(false);

        userBookAccessRepository.save(access);
    }
}