package com.sagar.book.mapper;

import com.sagar.author.model.Author;
import com.sagar.book.model.Book;
import com.sagar.book.payload.request.CreateBookRequest;
import com.sagar.book.payload.request.UpdateBookRequest;
import com.sagar.book.payload.response.BookResponse;
import com.sagar.genre.model.Genre;
import com.sagar.publisher.model.Publisher;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public Book toEntity(CreateBookRequest request,
                         Author author,
                         Genre genre,
                         Publisher publisher) {

        return Book.builder()
                .isbn(request.getIsbn())
                .title(request.getTitle())
                .author(author)
                .genre(genre)
                .publisher(publisher)
                .publishDate(request.getPublishDate())
                .language(request.getLanguage())
                .description(request.getDescription())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getAvailableCopies())
                .price(request.getPrice())
                .build();
    }

    public void updateEntity(Book book,
                             UpdateBookRequest request,
                             Author author,
                             Genre genre,
                             Publisher publisher) {

        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setAuthor(author);
        book.setGenre(genre);
        book.setPublisher(publisher);
        book.setPublishDate(request.getPublishDate());
        book.setLanguage(request.getLanguage());
        book.setDescription(request.getDescription());
        book.setTotalCopies(request.getTotalCopies());
        book.setAvailableCopies(request.getAvailableCopies());
        book.setPrice(request.getPrice());
        book.setActive(request.getActive());
    }

    public BookResponse toResponse(Book book) {

        return BookResponse.builder()
                .id(book.getId())
                .isbn(book.getIsbn())
                .title(book.getTitle())

                .authorId(book.getAuthor().getId())
                .authorName(book.getAuthor().getName())

                .genreId(book.getGenre().getId())
                .genreName(book.getGenre().getName())

                .publisherId(book.getPublisher().getId())
                .publisherName(book.getPublisher().getName())

                .publishDate(book.getPublishDate())
                .language(book.getLanguage())
                .description(book.getDescription())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .price(book.getPrice())
                .active(book.getActive())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}