package com.library.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryEvent {
    private int libraryEventId;
    private LibraryEventType libraryEventType;
    @NotNull
    @Valid
    private Book book;
}
