package com.sagar.publisher.mapper;

import com.sagar.publisher.model.Publisher;
import com.sagar.publisher.payload.request.CreatePublisherRequest;
import com.sagar.publisher.payload.request.UpdatePublisherRequest;
import com.sagar.publisher.payload.response.PublisherResponse;
import org.springframework.stereotype.Component;

@Component
public class PublisherMapper {

    public Publisher toEntity(CreatePublisherRequest request) {

        return Publisher.builder()
                .name(request.getName())
                .address(request.getAddress())
                .email(request.getEmail())
                .phone(request.getPhone())
                .website(request.getWebsite())
                .build();
    }

    public void updateEntity(
            Publisher publisher,
            UpdatePublisherRequest request) {

        publisher.setName(request.getName());
        publisher.setAddress(request.getAddress());
        publisher.setEmail(request.getEmail());
        publisher.setPhone(request.getPhone());
        publisher.setWebsite(request.getWebsite());
        publisher.setActive(request.getActive());
    }

    public PublisherResponse toResponse(Publisher publisher) {

        return PublisherResponse.builder()
                .id(publisher.getId())
                .name(publisher.getName())
                .address(publisher.getAddress())
                .email(publisher.getEmail())
                .phone(publisher.getPhone())
                .website(publisher.getWebsite())
                .active(publisher.getActive())
                .createdAt(publisher.getCreatedAt())
                .updatedAt(publisher.getUpdatedAt())
                .build();
    }
}