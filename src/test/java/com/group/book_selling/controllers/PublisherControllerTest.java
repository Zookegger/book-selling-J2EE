package com.group.book_selling.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
      Publisher existing = new Publisher();
      existing.setId(3L);
      existing.setLogo("/uploads/publishers/old.png");
      when(publisherService.findById(3L)).thenReturn(existing);

        when(publisherService.update(eq(3L), any(Publisher.class))).thenAnswer(invocation -> {
            Publisher request = invocation.getArgument(1);
            request.setId(3L);
            request.setSlug("nha-xuat-ban-tre");
            request.setActive(true);
            return request;
        });

      mockMvc.perform(post("/publishers/save")
        .param("id", "3")
        .param("name", "Nha Xuat Ban Tre")
        .param("slug", "tam")
        .param("description", "NXB lon")
        .param("contactEmail", "contact@nxbtre.vn")
        .param("active", "true"))
        .andExpect(status().is3xxRedirection())
        .andExpect(redirectedUrl("/publishers"));

      verify(publisherService).update(eq(3L), any(Publisher.class));
    }

    @Test
    void update_whenMissing_returns404() throws Exception {
      when(publisherService.findById(999L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay nha xuat ban"));

      mockMvc.perform(post("/publishers/save")
        .param("id", "999")
        .param("name", "Missing Publisher")
        .param("slug", "tam")
        .param("contactEmail", "missing@example.com")
        .param("active", "true"))
                .andExpect(status().isNotFound());
    }
}
