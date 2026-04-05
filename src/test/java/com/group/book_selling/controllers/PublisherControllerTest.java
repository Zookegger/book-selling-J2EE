package com.group.book_selling.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test; // Thêm cái này
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any; // Thêm cái này
import static org.mockito.ArgumentMatchers.eq; // Thêm cái này
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // Thêm cái này
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import com.group.book_selling.models.Publisher;
import com.group.book_selling.services.BookService;
import com.group.book_selling.services.PublisherService;

@ExtendWith(MockitoExtension.class)
class PublisherControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PublisherService publisherService;

  
    @Mock
    private BookService bookService;

    @BeforeEach
    void setUp() {

        PublisherController controller = new PublisherController(publisherService, bookService);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void update_existingPublisher_returns200() throws Exception {
        when(publisherService.update(eq(3L), any(Publisher.class))).thenAnswer(invocation -> {
            Publisher request = invocation.getArgument(1);
            request.setId(3L);
            request.setSlug("nha-xuat-ban-tre");
            request.setActive(true);
            return request;
        });

        String body = """
                {
                  "name": "Nha Xuat Ban Tre",
                  "slug": "tam",
                  "description": "NXB lon",
                  "contactEmail": "contact@nxbtre.vn",
                  "isActive": true
                }
                """;

        mockMvc.perform(put("/api/publishers/3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Nha Xuat Ban Tre"))
                .andExpect(jsonPath("$.slug").value("nha-xuat-ban-tre"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void update_whenMissing_returns404() throws Exception {
        when(publisherService.update(eq(999L), any(Publisher.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nha xuat ban"));

        String body = """
                {
                  "name": "Missing Publisher",
                  "slug": "tam",
                  "contactEmail": "missing@example.com",
                  "isActive": true
                }
                """;

        mockMvc.perform(put("/api/publishers/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isNotFound());
    }
}