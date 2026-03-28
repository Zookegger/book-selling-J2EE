package com.group.book_selling.controllers;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.hasSize;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.group.book_selling.models.Author;
import com.group.book_selling.repository.IAuthorRepository;

@ExtendWith(MockitoExtension.class)
class AuthorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private IAuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        AuthorController controller = new AuthorController(authorRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void findAll_returns200AndAuthorList() throws Exception {
        Author author = Author.builder()
                .id(1L)
                .name("Test Author")
                .slug("test-author")
                .email("author@example.com")
                .build();

        when(authorRepository.findAll(any(org.springframework.data.domain.Sort.class))).thenReturn(List.of(author));

        mockMvc.perform(get("/api/authors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].slug").value("test-author"));
    }

    @Test
    void findById_whenMissing_returns404() throws Exception {
        when(authorRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/authors/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_generatesSlugAndReturns201() throws Exception {
        when(authorRepository.save(any(Author.class))).thenAnswer(invocation -> {
            Author saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        String body = """
                {
                  "name": "Nguyen Nhat Anh",
                                    "slug": "tam",
                  "email": "nna@example.com",
                  "bio": "Tac gia noi tieng"
                }
                """;

        mockMvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.slug").value("nguyen-nhat-anh"));

        ArgumentCaptor<Author> captor = ArgumentCaptor.forClass(Author.class);
        verify(authorRepository).save(captor.capture());
        org.junit.jupiter.api.Assertions.assertEquals("nguyen-nhat-anh", captor.getValue().getSlug());
    }
}
