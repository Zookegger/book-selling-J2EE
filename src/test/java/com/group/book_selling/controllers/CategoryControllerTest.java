package com.group.book_selling.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.group.book_selling.models.Category;
import com.group.book_selling.repository.ICategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ICategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        CategoryController controller = new CategoryController(categoryRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void create_withNullAncestors_initializesEmptyList() throws Exception {
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        String body = """
                {
                  "name": "Van hoc",
                                    "slug": "tam",
                                    "description": "Sach van hoc",
                                    "orderIndex": 0
                }
                """;

        mockMvc.perform(post("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.slug").value("van-hoc"));

        ArgumentCaptor<Category> captor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertNotNull(captor.getValue().getAncestors());
        org.junit.jupiter.api.Assertions.assertTrue(captor.getValue().getAncestors().isEmpty());
    }

    @Test
    void delete_whenMissing_returns404() throws Exception {
        when(categoryRepository.existsById(404L)).thenReturn(false);

        mockMvc.perform(delete("/api/categories/404"))
                .andExpect(status().isNotFound());
    }
}
