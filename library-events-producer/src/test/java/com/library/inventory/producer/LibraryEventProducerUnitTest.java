package com.library.inventory.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.inventory.domain.Book;
import com.library.inventory.domain.LibraryEvent;
import com.library.inventory.domain.LibraryEventType;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.util.concurrent.SettableListenableFuture;

import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LibraryEventProducerUnitTest {

    @Mock
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Spy
    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    LibraryEventProducer libraryEventProducer;

    @Test
    void sendLibraryEvent_approach2_failure() throws JsonProcessingException, ExecutionException, InterruptedException {

        //given
        LibraryEvent libraryEvent = new LibraryEvent().builder()
                .libraryEventId(1)
                .book(new Book().builder().bookId(1).bookName(null).author(null).build())
                .libraryEventType(LibraryEventType.NEW)
                .build();
        SettableListenableFuture future = new SettableListenableFuture();
        future.setException(new RuntimeException("Exception in kafka flow!"));
        //when
        when(kafkaTemplate.send(isA(ProducerRecord.class))).thenReturn(future);
        assertThrows(Exception.class,()->libraryEventProducer.sendLibraryEventApproach2(libraryEvent).get());

        //then
    }

}
