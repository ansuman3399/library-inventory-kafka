package com.library.inventory.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.inventory.domain.LibraryEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Component
@Slf4j
public class LibraryEventProducer {

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    ObjectMapper objectMapper;

    public void sendLibraryEvent(LibraryEvent libraryEvent) throws JsonProcessingException {
        int key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);

        ListenableFuture<SendResult<Integer, String>> sendResultListenableFuture = kafkaTemplate.sendDefault(key, value);
        sendResultListenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

    public void handleFailure(int key, String value, Throwable ex) {
        log.error("Error sending the message with exception : {}", ex.getLocalizedMessage());
        try {
            throw ex;
        } catch (Throwable th) {
            log.error("Error on failure:{}", th.getMessage());
        }
    }

    public void handleSuccess(int key, String value, SendResult<Integer, String> result) {
        log.info("Message sent successfully for the key : {} and the value : {} and to the partition: {}", key, value, result.getRecordMetadata().partition());
    }

    public SendResult<Integer, String> sendLibraryEventSynchronous(LibraryEvent libraryEvent) throws JsonProcessingException, ExecutionException, InterruptedException {
        int key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        SendResult<Integer, String> sendResult = null;
        try {
            sendResult = kafkaTemplate.sendDefault(key, value).get();
        } catch (ExecutionException | InterruptedException e) {
            log.error("ExecutionException/InterruptedException while sending the message : {}", e.getMessage());
            throw e;
        } catch (Exception ex) {
            log.error("Exception while sending the message:{}", ex.getMessage());
            ex.printStackTrace();
        }
        return sendResult;
    }

    public void sendLibraryEventApproach2(LibraryEvent libraryEvent) throws JsonProcessingException {
        int key = libraryEvent.getLibraryEventId();
        String value = objectMapper.writeValueAsString(libraryEvent);
        //create a producer record
        List<Header> headers = Arrays.asList(new RecordHeader("event-source", "scanner".getBytes(StandardCharsets.UTF_8)));
        ProducerRecord<Integer, String> record = new ProducerRecord<>("library-events", null, key, value, headers);

        ListenableFuture<SendResult<Integer, String>> sendResultListenableFuture = kafkaTemplate.send(record);
        sendResultListenableFuture.addCallback(new ListenableFutureCallback<SendResult<Integer, String>>() {
            @Override
            public void onFailure(Throwable ex) {
                handleFailure(key, value, ex);
            }

            @Override
            public void onSuccess(SendResult<Integer, String> result) {
                handleSuccess(key, value, result);
            }
        });
    }

}
