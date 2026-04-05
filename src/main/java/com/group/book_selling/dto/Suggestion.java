package com.group.book_selling.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Suggestion(String title, String slug, String author, String isbn) {
}
