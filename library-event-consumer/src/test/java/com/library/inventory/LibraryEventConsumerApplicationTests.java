package com.library.inventory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.library.inventory.consumer.LibraryEventsConsumer;
import com.library.inventory.entity.LibraryEvent;
import com.library.inventory.repository.LibraryEventsRepository;
import com.library.inventory.service.LibraryEventsService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
@EmbeddedKafka(topics = {"library-events"}, partitions = 3)
@TestPropertySource(properties = {"spring.kafka.producer.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}"})
class LibraryEventConsumerApplicationTests {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    KafkaTemplate<Integer, String> kafkaTemplate;

    @Autowired
    KafkaListenerEndpointRegistry endpointRegistry;

    @SpyBean
    LibraryEventsConsumer libraryEventsConsumer;

    @SpyBean
    LibraryEventsService libraryEventsService;

    @Autowired
    LibraryEventsRepository libraryEventsRepository;

    @AfterEach
    void tearDown(){
        libraryEventsRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        for (MessageListenerContainer container : endpointRegistry.getListenerContainers()) {
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void PublishNewLibraryEvent() throws ExecutionException, InterruptedException, JsonProcessingException {
        //given
        String json = "{\n" +
                "    \"libraryEventId\": 1,\n" +
                "    \"book\": {\n" +
                "        \"bookId\": 1,\n" +
                "        \"bookName\": \"think and grow rich-6\",\n" +
                "        \"author\": \"Nepoleon Hill\"\n" +
                "    }\n" +
                "}";

        kafkaTemplate.sendDefault(json).get();

        CountDownLatch latch = new CountDownLatch(1);
        latch.await(3, TimeUnit.SECONDS);

        //then
        verify(libraryEventsConsumer, times(1)).onMessage(isA(ConsumerRecord.class));
        verify(libraryEventsService, times(1)).processLibraryEvent(isA(ConsumerRecord.class));

        List<LibraryEvent> list = (List<LibraryEvent>) libraryEventsRepository.findAll();
        assert list.size() == 1;
        list.forEach(library ->{
            assert library.getLibraryEventId()!=null;
            assertEquals(1,library.getBook().getBookId());
        });

    }

}
