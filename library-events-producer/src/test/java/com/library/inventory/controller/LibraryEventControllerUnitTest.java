package com.library.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.inventory.domain.Book;
import com.library.inventory.domain.LibraryEvent;
import com.library.inventory.domain.LibraryEventType;
import com.library.inventory.producer.LibraryEventProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockConstruction;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryEventsController.class)
@AutoConfigureMockMvc
public class LibraryEventControllerUnitTest {

    @Autowired
    MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    LibraryEventProducer libraryEventProducer;

    @Test
    void postLibraryEvent() throws Exception {
        //given
        LibraryEvent libraryEvent = new LibraryEvent().builder()
                .libraryEventId(1)
                .book(new Book().builder().bookId(1).bookName("RDPD").author("RK").build())
                .libraryEventType(LibraryEventType.NEW)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        //when
        doNothing().when(libraryEventProducer).sendLibraryEventApproach2(isA(LibraryEvent.class));
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/v1/libraryevent")
                                .content(objectMapper.writeValueAsString(libraryEvent).toString())
                                .contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isCreated());

        //then

    }

    @Test
    void postLibraryEvent_4xx() throws Exception {
        //given
        LibraryEvent libraryEvent = new LibraryEvent().builder()
                .libraryEventId(1)
                .book(new Book().builder().bookId(1).bookName(null).author(null).build())
                .libraryEventType(LibraryEventType.NEW)
                .build();
        HttpHeaders headers = new HttpHeaders();
        headers.set("content-type", MediaType.APPLICATION_JSON_VALUE.toString());
        HttpEntity<LibraryEvent> request = new HttpEntity<>(libraryEvent, headers);

        //when
        doNothing().when(libraryEventProducer).sendLibraryEventApproach2(isA(LibraryEvent.class));
        String expectedErrorMSg = "book.author - must not be null , book.bookName - must not be null";
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/v1/libraryevent")
                                .content(objectMapper.writeValueAsString(libraryEvent).toString())
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content().string(expectedErrorMSg));

        //then

    }
}
