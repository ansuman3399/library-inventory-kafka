package com.library.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryEvent {
    private int libraryEventId;
    private LibraryEventType libraryEventType;
    private Book book;
}
