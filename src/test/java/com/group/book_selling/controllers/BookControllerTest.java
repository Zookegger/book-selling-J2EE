package com.group.book_selling.controllers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.group.book_selling.models.Author;
import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.models.Publisher;
import com.group.book_selling.repository.IAuthorRepository;
import com.group.book_selling.repository.IBookRepository;
import com.group.book_selling.repository.ICategoryRepository;
import com.group.book_selling.repository.IPublisherRepository;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;

    private MockMvc homeMockMvc;

    @Mock
    private IBookRepository bookRepository;

    @Mock
    private IAuthorRepository authorRepository;

    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private IPublisherRepository publisherRepository;

    @BeforeEach
    void setUp() {
        BookController controller = new BookController(bookRepository, authorRepository, categoryRepository, publisherRepository);
        this.mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        HomeController homeController = new HomeController(categoryRepository, bookRepository);
        this.homeMockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
    }

    @Test
    void create_withInvalidPublisherId_returns400() throws Exception {
        when(publisherRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/books/new")
                .param("title", "Toi thay hoa vang tren co xanh")
                .param("description", "Noi dung mo ta")
                .param("publicationDate", "2010-01-01")
                .param("publisherId", "999"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_withValidRelations_redirectsToBookDetail() throws Exception {
        Publisher publisher = Publisher.builder()
                .id(1L)
                .name("NXB Tre")
                .slug("nxb-tre")
                .contactEmail("nxb@example.com")
                .build();

        Author author = Author.builder()
                .id(2L)
                .name("Nguyen Nhat Anh")
                .slug("nguyen-nhat-anh")
                .email("nna@example.com")
                .build();

        Category category = Category.builder()
                .id(3L)
                .name("Van hoc")
                .slug("van-hoc")
                .build();

        when(publisherRepository.findById(1L)).thenReturn(Optional.of(publisher));
        when(authorRepository.findAllById(List.of(2L))).thenReturn(List.of(author));
        when(categoryRepository.findAllById(List.of(3L))).thenReturn(List.of(category));

        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book saved = invocation.getArgument(0);
            saved.setId(99L);
            if (saved.getPublicationDate() == null) {
                saved.setPublicationDate(LocalDate.now());
            }
            return saved;
        });

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

    @Test
    void genres_displaysCategoriesWithCount() throws Exception {
        Category cat1 = Category.builder()
                .id(1L)
                .name("Tieu thuyet")
                .slug("tieu-thuyet")
                .build();

        Category cat2 = Category.builder()
                .id(2L)
                .name("Khoa hoc")
                .slug("khoa-hoc")
                .build();

        when(categoryRepository.findAll(any(Sort.class))).thenReturn(List.of(cat1, cat2));
        when(bookRepository.countByCategories_Id(1L)).thenReturn(5L);
        when(bookRepository.countByCategories_Id(2L)).thenReturn(3L);

        homeMockMvc.perform(get("/genres"))
                .andExpect(status().isOk())
                .andExpect(view().name("categories"))
                .andExpect(model().attributeExists("categoryWithCount"))
                .andExpect(model().attributeExists("selectedCategoryIds"));
    }
}
