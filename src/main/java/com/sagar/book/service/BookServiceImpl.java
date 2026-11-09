package com.sagar.book.service;

import com.sagar.author.model.Author;
import com.sagar.author.repository.AuthorRepository;
import com.sagar.book.mapper.BookMapper;
import com.sagar.book.model.Book;
import com.sagar.book.model.BookAccessType;
import com.sagar.book.model.UserBookAction;
import com.sagar.book.payload.request.BookSearchRequest;
import com.sagar.book.payload.request.CreateBookRequest;
import com.sagar.book.payload.request.UpdateBookRequest;
import com.sagar.book.payload.response.BookResponse;
import com.sagar.book.repository.BookRepository;
import com.sagar.book.specification.BookSpecification;
import com.sagar.exception.BadRequestException;
import com.sagar.exception.DuplicateResourceException;
import com.sagar.exception.ResourceNotFoundException;
import com.sagar.genre.model.Genre;
import com.sagar.genre.repository.GenreRepository;
import com.sagar.publisher.model.Publisher;
import com.sagar.publisher.repository.PublisherRepository;
import com.sagar.user.model.User;
import com.sagar.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import java.util.List;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import com.sagar.access.model.UserBookAccess;
import com.sagar.access.repository.UserBookAccessRepository;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import java.net.MalformedURLException;
import java.time.LocalDateTime;

    @Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final BookMapper bookMapper;
    private final UserBookAccessRepository userBookAccessRepository;
    private final UserRepository userRepository;

    @Override
    public BookResponse createBook(CreateBookRequest request) {

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new DuplicateResourceException(
                    "Book ISBN already exists");
        }

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found"));

        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Genre not found"));

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Publisher not found"));

        Book book = bookMapper.toEntity(
                request,
                author,
                genre,
                publisher
        );

        Book savedBook = bookRepository.save(book);

        return bookMapper.toResponse(savedBook);
    }

        @Override
        public Page<BookResponse> getAllBooks(
                Pageable pageable,
                String userEmail
        ) {

            return bookRepository.findAll(pageable)
                    .map(book -> toBookResponseForUser(
                            book,
                            userEmail
                    ));
        }

        @Override
        public BookResponse getBookById(
                Long bookId,
                String userEmail
        ) {

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Book not found with id : " + bookId
                            )
                    );

            return toBookResponseForUser(
                    book,
                    userEmail
            );
        }


        @Override
        public BookResponse getBookByIsbn(
                String isbn,
                String userEmail
        ) {

            Book book = bookRepository.findByIsbn(isbn)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Book not found with ISBN : " + isbn
                            )
                    );

            return toBookResponseForUser(
                    book,
                    userEmail
            );
        }

    @Override
    public BookResponse updateBook(Long bookId,
                                   UpdateBookRequest request) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id : " + bookId));

        if (Boolean.TRUE.equals(request.getActive())
                && book.getEbookFileKey() == null) {

            throw new BadRequestException(
                    "Cannot activate a book without uploading the e-book file"
            );
        }

        if (!book.getIsbn().equals(request.getIsbn())
                && bookRepository.existsByIsbn(request.getIsbn())) {

            throw new DuplicateResourceException(
                    "Book ISBN already exists");
        }

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Author not found"));

        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Genre not found"));

        Publisher publisher = publisherRepository.findById(request.getPublisherId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Publisher not found"));

        bookMapper.updateEntity(
                book,
                request,
                author,
                genre,
                publisher
        );

        Book updatedBook = bookRepository.save(book);

        return bookMapper.toResponse(updatedBook);
    }

    @Override
    public void deleteBook(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id : " + bookId));

        bookRepository.delete(book);
    }


    @Override
    public void activateBook(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id : " + bookId));

        if (book.getActive()) {
            throw new BadRequestException(
                    "Book is already active");
        }

        book.setActive(true);

        bookRepository.save(book);
    }


    @Override
    public void deactivateBook(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id : " + bookId));

        if (!book.getActive()) {
            throw new BadRequestException(
                    "Book is already inactive");
        }

        book.setActive(false);

        bookRepository.save(book);
    }

        @Override
        public List<BookResponse> getAllActiveBooks(
                String userEmail
        ) {

            return bookRepository.findByActiveTrueOrderByTitleAsc()
                    .stream()
                    .map(book -> toBookResponseForUser(
                            book,
                            userEmail
                    ))
                    .toList();
        }

        @Override
        public Page<BookResponse> searchBooks(
                BookSearchRequest request,
                Pageable pageable,
                String userEmail
        ) {

            return bookRepository.findAll(
                    BookSpecification.search(request),
                    pageable
            ).map(book -> toBookResponseForUser(
                    book,
                    userEmail
            ));
        }

        @Override
        public void uploadEbook(
                Long bookId,
                MultipartFile file
        ) {

            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Book not found with id: " + bookId
                            )
                    );

            if (file == null || file.isEmpty()) {
                throw new BadRequestException(
                        "E-book file cannot be empty"
                );
            }

            String contentType = file.getContentType();
            String originalFileName = file.getOriginalFilename();

            boolean isPdf =
                    "application/pdf".equalsIgnoreCase(contentType)
                            || (
                            originalFileName != null
                                    && originalFileName
                                    .toLowerCase()
                                    .endsWith(".pdf")
                    );

            if (!isPdf) {
                throw new BadRequestException(
                        "Only PDF files are allowed"
                );
            }

            try {

                Path uploadDirectory =
                        Path.of("uploads", "ebooks")
                                .toAbsolutePath()
                                .normalize();

                Files.createDirectories(uploadDirectory);

                // Generate unique file name.
                String fileName =
                        UUID.randomUUID() + ".pdf";

                Path filePath =
                        uploadDirectory.resolve(fileName);

                Files.copy(
                        file.getInputStream(),
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                // Delete old file after successfully saving the new file.
                if (book.getEbookFilePath() != null) {

                    try {

                        Path oldFilePath =
                                Path.of(book.getEbookFilePath());

                        Files.deleteIfExists(oldFilePath);

                    } catch (IOException ignored) {
                        // New upload should not fail because
                        // the old file could not be deleted.
                    }
                }

                // Update all e-book metadata.
                book.setEbookFileKey(fileName);
                book.setEbookFileName(originalFileName);
                book.setEbookFilePath(filePath.toString());
                book.setEbookContentType(contentType);
                book.setEbookFileSize(file.getSize());

                // Activate after successful e-book upload.
                book.setActive(true);

                bookRepository.save(book);

            } catch (IOException e) {

                throw new BadRequestException(
                        "Failed to upload e-book"
                );
            }
        }


    @Override
    public Resource readEbook(
            Long bookId,
            Authentication authentication
    ) {

        Book book = findBookEntityById(bookId);

        if (!Boolean.TRUE.equals(book.getActive())) {
            throw new BadRequestException(
                    "Book is currently inactive"
            );
        }

        if (book.getEbookFilePath() == null) {
            throw new ResourceNotFoundException(
                    "E-book file not found"
            );
        }

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);

        // Only MEMBER requires book access validation
        if (!isAdmin) {

                User user = getUserByEmail(
                        authentication.getName()
                );

            UserBookAccess access =
                    userBookAccessRepository
                            .findByUserIdAndBookId(
                                    user.getId(),
                                    bookId
                            )
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "You do not have access to this book"
                                    )
                            );

            if (Boolean.TRUE.equals(access.getRevoked())) {
                throw new BadRequestException(
                        "Your access to this book has been revoked"
                );
            }

            if (!access.getAccessExpiresAt()
                    .isAfter(LocalDateTime.now())) {

                throw new BadRequestException(
                        "Your access to this book has expired"
                );
            }
        }

        // Runs for BOTH ADMIN and MEMBER
        try {

            Path filePath = Path.of(book.getEbookFilePath())
                    .toAbsolutePath()
                    .normalize();

            Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException(
                        "E-book file not found on server"
                );
            }

            return resource;

        } catch (MalformedURLException e) {

            throw new BadRequestException(
                    "Invalid e-book file path"
            );
        }
    }

    @Override
    public void uploadCover(
            Long bookId,
            MultipartFile file
    ) {

        Book book = findBookEntityById(bookId);

        if (file == null || file.isEmpty()) {
            throw new BadRequestException(
                    "Cover image cannot be empty"
            );
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !contentType.startsWith("image/")) {

            throw new BadRequestException(
                    "Only image files are allowed"
            );
        }

        try {

            Path uploadDirectory =
                    Path.of("uploads", "covers")
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(uploadDirectory);

            String originalFileName =
                    file.getOriginalFilename();

            String extension = "";

            if (originalFileName != null &&
                    originalFileName.contains(".")) {

                extension = originalFileName.substring(
                        originalFileName.lastIndexOf(".")
                );
            }

            String fileName =
                    UUID.randomUUID() + extension;

            Path filePath =
                    uploadDirectory.resolve(fileName);

            Files.copy(
                    file.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            // Delete previous cover
            if (book.getCoverImagePath() != null) {

                try {
                    Files.deleteIfExists(
                            Path.of(book.getCoverImagePath())
                    );
                } catch (IOException ignored) {
                }
            }

            book.setCoverImageFileName(originalFileName);
            book.setCoverImagePath(filePath.toString());
            book.setCoverImageContentType(contentType);
            book.setCoverImageFileSize(file.getSize());

            bookRepository.save(book);

        } catch (IOException e) {

            throw new BadRequestException(
                    "Failed to upload cover image"
            );
        }
    }

    @Override
    public Resource getBookCover(Long bookId) {

        Book book = findBookEntityById(bookId);

        if (book.getCoverImagePath() == null ||
                book.getCoverImagePath().isBlank()) {

            throw new ResourceNotFoundException(
                    "Book cover image not found"
            );
        }

        try {

            Path filePath = Path.of(
                            book.getCoverImagePath()
                    )
                    .toAbsolutePath()
                    .normalize();

            Resource resource =
                    new UrlResource(filePath.toUri());

            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException(
                        "Book cover image not found on server"
                );
            }

            return resource;

        } catch (MalformedURLException e) {

            throw new BadRequestException(
                    "Invalid book cover image path"
            );
        }
    }


        @Override
        @Transactional(readOnly = true)
        public ResponseEntity<Resource> readEbook(
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

            // Only active books can be read.
            if (!Boolean.TRUE.equals(book.getActive())) {
                throw new BadRequestException(
                        "This book is currently inactive"
                );
            }

            // PDF must exist.
            if (book.getEbookFilePath() == null
                    || book.getEbookFilePath().isBlank()) {

                throw new ResourceNotFoundException(
                        "E-book file not found"
                );
            }

            /*
             * Check whether the user has valid access.
             *
             * This assumes FREE books get UserBookAccess when
             * access is granted, and PAID books get it after
             * successful payment.
             */
            LocalDateTime now = LocalDateTime.now();

            UserBookAccess access =
                    userBookAccessRepository
                            .findByUserIdAndBookId(
                                    user.getId(),
                                    book.getId()
                            )
                            .orElseThrow(() ->
                                    new BadRequestException(
                                            "You do not have access to this book"
                                    )
                            );

            if (Boolean.TRUE.equals(access.getRevoked())
                    || access.getAccessExpiresAt() == null
                    || !access.getAccessExpiresAt().isAfter(now)) {

                throw new BadRequestException(
                        "Your access to this book has expired"
                );
            }

            Path filePath = Path.of(book.getEbookFilePath());

            if (!Files.exists(filePath)
                    || !Files.isRegularFile(filePath)) {

                throw new ResourceNotFoundException(
                        "E-book file not found"
                );
            }

            Resource resource =
                    new FileSystemResource(filePath);

            String contentType = book.getEbookContentType();

            if (contentType == null
                    || contentType.isBlank()) {

                contentType = MediaType.APPLICATION_PDF_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(
                            MediaType.parseMediaType(contentType)
                    )
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            "inline; filename=\""
                                    + book.getEbookFileName()
                                    + "\""
                    )
                    .body(resource);
        }

    /*helper methods used in this service class
    * */
    private Book findBookEntityById(Long bookId) {

        return bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id: " + bookId
                        )
                );
    }

    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }

    private BookResponse toBookResponseForUser(
            Book book,
            String userEmail
    ) {

        BookResponse response =
                bookMapper.toResponse(book);

        User user = getUserByEmail(userEmail);

        UserBookAccess access =
                userBookAccessRepository
                        .findByUserIdAndBookId(
                                user.getId(),
                                book.getId()
                        )
                        .orElse(null);

        if (access != null) {

            boolean hasValidAccess =
                    !Boolean.TRUE.equals(access.getRevoked())
                            && access.getAccessExpiresAt()
                            .isAfter(LocalDateTime.now());

            if (hasValidAccess) {
                response.setUserBookAction(
                        UserBookAction.READ
                );

                return response;
            }

            // User had access before, but it is now expired
            if (!Boolean.TRUE.equals(access.getRevoked())) {
                response.setUserBookAction(
                        UserBookAction.EXPIRED
                );

                return response;
            }
        }

        // No valid access
        if (book.getAccessType() == BookAccessType.FREE) {

            response.setUserBookAction(
                    UserBookAction.GET_FREE
            );

        } else {

            response.setUserBookAction(
                    UserBookAction.BUY
            );
        }

        return response;
    }


}

