package com.sagar.publisher.service;

import com.sagar.publisher.payload.request.CreatePublisherRequest;
import com.sagar.publisher.payload.request.UpdatePublisherRequest;
import com.sagar.publisher.payload.response.PublisherResponse;

import java.util.List;

public interface PublisherService {

    PublisherResponse createPublisher(CreatePublisherRequest request);

    List<PublisherResponse> getAllPublishers();

    PublisherResponse getPublisherById(Long publisherId);

    PublisherResponse updatePublisher(Long publisherId,
                                      UpdatePublisherRequest request);

    void deletePublisher(Long publisherId);

    void activatePublisher(Long publisherId);

    void deactivatePublisher(Long publisherId);

    List<PublisherResponse> getAllActivePublishers();
}