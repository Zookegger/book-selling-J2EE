package com.group.book_selling.controllers;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.group.book_selling.models.Publisher;
import com.group.book_selling.repository.IPublisherRepository;

@ExtendWith(MockitoExtension.class)
class PublisherControllerTest {

    private MockMvc mockMvc;

        @Mock
    private IPublisherRepository publisherRepository;

        @BeforeEach
        void setUp() {
                PublisherController controller = new PublisherController(publisherRepository);
                this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        }

    @Test
    void update_existingPublisher_returns200() throws Exception {
        Publisher existing = Publisher.builder()
                .id(3L)
                .name("Old Publisher")
                .slug("old-publisher")
                .contactEmail("old@example.com")
                .build();

        when(publisherRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(publisherRepository.save(any(Publisher.class))).thenAnswer(invocation -> invocation.getArgument(0));

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
        when(publisherRepository.findById(999L)).thenReturn(Optional.empty());

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
