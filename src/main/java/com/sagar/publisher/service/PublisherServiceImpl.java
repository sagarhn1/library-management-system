package com.sagar.publisher.service;

import com.sagar.exception.BadRequestException;
import com.sagar.exception.DuplicateResourceException;
import com.sagar.exception.ResourceNotFoundException;
import com.sagar.publisher.mapper.PublisherMapper;
import com.sagar.publisher.model.Publisher;
import com.sagar.publisher.payload.request.CreatePublisherRequest;
import com.sagar.publisher.payload.request.UpdatePublisherRequest;
import com.sagar.publisher.payload.response.PublisherResponse;
import com.sagar.publisher.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;
    private final PublisherMapper publisherMapper;

    @Override
    public PublisherResponse createPublisher(CreatePublisherRequest request) {

        if (publisherRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException(
                    "Publisher name already exists");
        }

        Publisher publisher = publisherMapper.toEntity(request);

        Publisher savedPublisher = publisherRepository.save(publisher);

        return publisherMapper.toResponse(savedPublisher);
    }

    @Override
    public List<PublisherResponse> getAllPublishers() {

        return publisherRepository.findAll()
                .stream()
                .map(publisherMapper::toResponse)
                .toList();
    }

    @Override
    public PublisherResponse getPublisherById(Long publisherId) {

        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Publisher not found with id : " + publisherId));

        return publisherMapper.toResponse(publisher);
    }

    @Override
    public PublisherResponse updatePublisher(Long publisherId,
                                             UpdatePublisherRequest request) {

        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Publisher not found with id : " + publisherId));

        if (!publisher.getName().equals(request.getName())
                && publisherRepository.existsByName(request.getName())) {

            throw new DuplicateResourceException(
                    "Publisher name already exists");
        }

        publisherMapper.updateEntity(publisher, request);

        Publisher updatedPublisher = publisherRepository.save(publisher);

        return publisherMapper.toResponse(updatedPublisher);
    }

    @Override
    public void deletePublisher(Long publisherId) {

        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Publisher not found with id : " + publisherId));

        publisherRepository.delete(publisher);
    }

    @Override
    public void activatePublisher(Long publisherId) {

        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Publisher not found with id : " + publisherId));

        if (publisher.getActive()) {
            throw new BadRequestException(
                    "Publisher is already active");
        }

        publisher.setActive(true);

        publisherRepository.save(publisher);
    }

    @Override
    public void deactivatePublisher(Long publisherId) {

        Publisher publisher = publisherRepository.findById(publisherId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Publisher not found with id : " + publisherId));

        if (!publisher.getActive()) {
            throw new BadRequestException(
                    "Publisher is already inactive");
        }

        publisher.setActive(false);

        publisherRepository.save(publisher);
    }

    @Override
    public List<PublisherResponse> getAllActivePublishers() {

        return publisherRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(publisherMapper::toResponse)
                .toList();
    }
}