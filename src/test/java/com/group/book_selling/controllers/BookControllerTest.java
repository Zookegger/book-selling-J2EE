package com.group.book_selling.controllers;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Book;
import com.group.book_selling.services.BookService;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @BeforeEach
    void setUp() {
        BookController controller = new BookController(bookService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_withInvalidPublisherId_returns400() throws Exception {
        when(bookService.createBook(any(BookRequest.class), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "publisherId khong hop le: 999"));

        mockMvc.perform(post("/books/new")
                .param("title", "Toi thay hoa vang tren co xanh")
                .param("description", "Noi dung mo ta")
                .param("publicationDate", "2010-01-01")
                .param("publisherId", "999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withValidRelations_redirectsToBookDetail() throws Exception {
        Book saved = Book.builder()
            .id(99L)
            .title("Cho toi xin mot ve di tuoi tho")
            .slug("cho-toi-xin-mot-ve-di-tuoi-tho")
            .description("Noi dung sach")
            .publicationDate(LocalDate.of(2008, 1, 1))
            .language("en")
            .build();

        when(bookService.createBook(any(BookRequest.class), any())).thenReturn(saved);

        mockMvc.perform(post("/books/new")
                .param("title", "Cho toi xin mot ve di tuoi tho")
                .param("description", "Noi dung sach")
                .param("publicationDate", "2008-01-01")
                .param("publisherId", "1")
                .param("authorIds", "2")
                .param("categoryIds", "3"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books/cho-toi-xin-mot-ve-di-tuoi-tho"));
    }
}

