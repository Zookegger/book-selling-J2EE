package com.group.book_selling.controllers;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.services.BookService;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @BeforeEach
    void setUp() {
        HomeController controller = new HomeController(bookService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void homePage_returnsIndexView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void getBestsellers_returnsBestsellersView() throws Exception {
        Category category = Category.builder().id(1L).name("Văn học").slug("van-hoc").build();
        Book book = Book.builder().id(10L).title("Sách hay").slug("sach-hay").description("Mô tả").publicationDate(LocalDate.now()).language("vi").build();

        Map<Category, List<Book>> bestsellers = new LinkedHashMap<>();
        bestsellers.put(category, List.of(book));

        when(bookService.findBestsellersByCategory()).thenReturn(bestsellers);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("popularByCategory"));
    }
}