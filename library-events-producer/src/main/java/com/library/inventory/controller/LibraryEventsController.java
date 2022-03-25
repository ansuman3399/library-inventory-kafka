package com.library.inventory.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.library.inventory.domain.LibraryEvent;
import com.library.inventory.domain.LibraryEventType;
import com.library.inventory.producer.LibraryEventProducer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@Slf4j
public class LibraryEventsController {

    @Autowired
    LibraryEventProducer libraryEventProducer;

    @PostMapping("/v1/libraryevent")
    public ResponseEntity<LibraryEvent> postLibraryEvent(@RequestBody LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        log.info("Inside postlibraryEvvent controller!");
        //invoke kafka producer
        //SendResult<Integer, String> sendResult = libraryEventProducer.sendLibraryEventSynchronous(libraryEvent); //approach-2 synchronous response
        //libraryEventProducer.sendLibraryEventApproach2(libraryEvent);//approach-3 using producer record

        libraryEvent.setLibraryEventType(LibraryEventType.NEW);
        libraryEventProducer.sendLibraryEvent(libraryEvent);//approach-1 asynchronous response
        return ResponseEntity.status(HttpStatus.CREATED).body(libraryEvent);
    }

}
