package com.library.inventory.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Book {
    @NotNull
    private int bookId;
    @NotNull
    private String bookName;
    @NotNull
    private String author;
}
