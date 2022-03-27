package com.library.inventory.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.inventory.entity.LibraryEvent;
import com.library.inventory.repository.LibraryEventsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class LibraryEventsService {

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    @Autowired
    ObjectMapper objectMapper;

    public void processLibraryEvent(ConsumerRecord<Integer, String> consumerRecord) throws JsonProcessingException {
        LibraryEvent libraryEvent = objectMapper.readValue(consumerRecord.value(), LibraryEvent.class);
        log.info("Library event: {}", libraryEvent);
        switch (libraryEvent.getLibraryEventType()) {
            case NEW:
                save(libraryEvent);
                break;
            case UPDATE:
                validate(libraryEvent);
                save(libraryEvent);
                break;
            default:
                log.info("Invalid library event type");
        }
    }

    public void validate(LibraryEvent libraryEvent) {
        if (libraryEvent.getLibraryEventId() == null) {
            throw new IllegalArgumentException("Library event Id is missing.");
        }
        Optional<LibraryEvent> libraryEvent1 = libraryEventsRepository.findById(libraryEvent.getLibraryEventId());
        if (!libraryEvent1.isPresent()) {
            throw new IllegalArgumentException("Not a valid library event");
        }
        log.info("Validation is successful for the library event: {}", libraryEvent1.get().getLibraryEventId());
    }

    public void save(LibraryEvent libraryEvent) {
        libraryEvent.getBook().setLibraryEvent(libraryEvent);
        libraryEventsRepository.save(libraryEvent);
        log.info("Successfully persisted the library event");
    }
}
