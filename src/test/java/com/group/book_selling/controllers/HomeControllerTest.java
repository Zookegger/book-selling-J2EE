package com.group.book_selling.controllers;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.repository.IBookRepository;
import com.group.book_selling.repository.ICategoryRepository;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private ICategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        HomeController controller = new HomeController(bookRepository, categoryRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void getGenres_returnsCategoriesView() throws Exception {
        Category category = Category.builder().id(1L).name("Văn học").slug("van-hoc").build();

        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(category));
        when(bookRepository.countByCategories_Id(1L)).thenReturn(1L);

        mockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/categories"))
                .andExpect(model().attributeExists("categoryWithCount"));
    }

    @Test
    void getBestsellers_returnsBestsellersView() throws Exception {
        Category category = Category.builder().id(1L).name("Văn học").slug("van-hoc").build();
        Book book = Book.builder().id(10L).title("Sách hay").slug("sach-hay").description("Mô tả").publicationDate(LocalDate.now()).language("vi").build();

        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(category));
        when(bookRepository.findTop5ByCategories_IdOrderByCreatedAtDesc(1L)).thenReturn(List.of(book));

        mockMvc.perform(get("/bestsellers"))
                .andExpect(status().isOk())
                .andExpect(view().name("books/bestsellers"))
                .andExpect(model().attributeExists("popularByCategory"));
    }
}