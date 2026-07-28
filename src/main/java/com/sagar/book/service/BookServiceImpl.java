package com.sagar.book.service;

import com.sagar.author.model.Author;
import com.sagar.author.repository.AuthorRepository;
import com.sagar.book.mapper.BookMapper;
import com.sagar.book.model.Book;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final GenreRepository genreRepository;
    private final PublisherRepository publisherRepository;
    private final BookMapper bookMapper;

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
    public Page<BookResponse> getAllBooks(Pageable pageable) {

        return bookRepository.findAll(pageable)
                .map(bookMapper::toResponse);
    }

    @Override
    public BookResponse getBookById(Long bookId) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id : " + bookId));

        return bookMapper.toResponse(book);
    }


    @Override
    public BookResponse getBookByIsbn(String isbn) {

        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with ISBN : " + isbn));

        return bookMapper.toResponse(book);
    }

    @Override
    public BookResponse updateBook(Long bookId,
                                   UpdateBookRequest request) {

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Book not found with id : " + bookId));

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
    public List<BookResponse> getAllActiveBooks() {

        return bookRepository.findByActiveTrueOrderByTitleAsc()
                .stream()
                .map(bookMapper::toResponse)
                .toList();
    }

    @Override
    public Page<BookResponse> searchBooks(
            BookSearchRequest request,
            Pageable pageable) {

        return bookRepository.findAll(
                BookSpecification.search(request),
                pageable
        ).map(bookMapper::toResponse);
    }


}

