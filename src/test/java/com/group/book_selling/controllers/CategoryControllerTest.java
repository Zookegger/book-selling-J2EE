package com.group.book_selling.controllers;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Category;
import com.group.book_selling.services.BookService;
import com.group.book_selling.services.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CategoryService categoryService;
    @Mock
    private BookService bookService;

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryService, bookService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void list_returnsCategoriesView() throws Exception {
        Category category = Category.builder()
                .id(1L)
                .name("Van hoc")
                .slug("van-hoc")
                .build();

        when(categoryService.findAll()).thenReturn(List.of(category));

        mockMvc.perform(get("/categories"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories/list"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void save_redirectsToAdminList() throws Exception {
        mockMvc.perform(post("/categories/save")
                .param("name", "Van hoc")
                .param("description", "Sach van hoc"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/categories"));

        verify(categoryService).save(any(Category.class));
    }

    @Test
    void delete_whenMissing_returns404() throws Exception {
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay danh muc"))
                .when(categoryService)
                .delete(404L);

        mockMvc.perform(post("/categories/delete/404"))
                .andExpect(status().isNotFound());
    }
}
