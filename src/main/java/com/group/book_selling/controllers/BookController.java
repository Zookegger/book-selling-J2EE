package com.group.book_selling.controllers;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.group.book_selling.models.Author;
import com.group.book_selling.models.Book;
import com.group.book_selling.models.Category;
import com.group.book_selling.models.Publisher;
import com.group.book_selling.repository.IAuthorRepository;
import com.group.book_selling.repository.IBookRepository;
import com.group.book_selling.repository.ICategoryRepository;
import com.group.book_selling.repository.IPublisherRepository;
import com.group.book_selling.services.BookService;
import com.group.book_selling.utils.SlugUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * Controller MVC cho quản lý sách.
 *
 * <p>Controller này hiển thị trang sách và xử lý form tạo/cập nhật/xóa.</p>
 */
@Controller
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService; 
    private final IBookRepository bookRepository;
    private final IAuthorRepository authorRepository;
    private final ICategoryRepository categoryRepository;
    private final IPublisherRepository publisherRepository;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword, Model model) {
        List<Book> books;
        if (keyword == null || keyword.trim().isEmpty()) {
            books = bookRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        } else {
            books = bookRepository.searchBooks(keyword.trim());
        }
        model.addAttribute("books", books);
        model.addAttribute("keyword", keyword);
        return "books/search";
    }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String keyword, Model model) {
        return list(keyword, model);
    }

    @GetMapping("/autocomplete")
    @ResponseBody
    public List<String> autocomplete(@RequestParam String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        String query = keyword.trim();
        return bookRepository.searchBooks(query).stream()
                .flatMap(book -> {
                    List<String> suggestions = new ArrayList<>();
                    if (book.getTitle() != null && !book.getTitle().isBlank()) {
                        suggestions.add(book.getTitle());
                    }
                    if (book.getIsbn() != null && !book.getIsbn().isBlank()) {
                        suggestions.add(book.getIsbn());
                    }
                    if (book.getPublisher() != null && book.getPublisher().getName() != null && !book.getPublisher().getName().isBlank()) {
                        suggestions.add(book.getPublisher().getName());
                    }
                    if (book.getAuthors() != null) {
                        book.getAuthors().stream()
                                .map(Author::getName)
                                .filter(name -> name != null && !name.isBlank())
                                .forEach(suggestions::add);
                    }
                    return suggestions.stream();
                })
                .distinct()
                .limit(12)
                .toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        if (!model.containsAttribute("bookRequest")) {
            model.addAttribute("bookRequest", emptyBookRequest());
        }
        addReferenceData(model);
        
        model.addAttribute("book", null); 
        model.addAttribute("pageTitle", "Thêm sách mới");
        
        return "books/form";
    }

    @PreAuthorize("hasRole('ADMIN')")
@PostMapping("/new")
public String create(@Valid @ModelAttribute("bookRequest") BookRequest request,
        BindingResult result,
        @RequestParam(value = "coverFile", required = false) MultipartFile coverFile, 
        Model model,
        RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
        addReferenceData(model);
        model.addAttribute("book", null); 
        model.addAttribute("pageTitle", "Thêm sách mới");
        return "books/form";
    }

    try {
        Book book = new Book();
        applyRequestToBook(book, request);

        // XỬ LÝ LƯU FILE
        if (coverFile != null && !coverFile.isEmpty()) {
            String originalFileName = StringUtils.cleanPath(coverFile.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + originalFileName; 
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = coverFile.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            // Gán đường dẫn ảnh mới vào database
            book.setCoverImage("/uploads/" + fileName);
        }

        bookRepository.save(book);
        redirectAttributes.addFlashAttribute("successMessage", "Tạo sách thành công.");
        return "redirect:/books/" + book.getSlug();

    } catch (Exception e) {
        e.printStackTrace(); // In ra console để bạn xem nếu vẫn bị lỗi 500
        model.addAttribute("errorMessage", "Lỗi lưu file: " + e.getMessage());
        addReferenceData(model);
        model.addAttribute("book", null);
        model.addAttribute("pageTitle", "Thêm sách mới");
        return "books/form";
    }
}
    @GetMapping("/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        Book book = bookRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay sach"));
        model.addAttribute("book", book);
        return "books/detail";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{slug}/edit")
public String showEditForm(@PathVariable String slug, Model model) {
    Book book = bookRepository.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

    // Nếu chưa có bookRequest trong model (lần đầu vào trang edit)
    if (!model.containsAttribute("bookRequest")) {
        model.addAttribute("bookRequest", createRequest(book));
    }
    
    addReferenceData(model);
    model.addAttribute("book", book);
    model.addAttribute("pageTitle", "Chỉnh sửa sách: " + book.getTitle());
    
    return "books/form";
}
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/{slug}/edit")
public String update(@PathVariable String slug,
        @Valid @ModelAttribute("bookRequest") BookRequest request,
        BindingResult result,
        @RequestParam(value = "coverFile", required = false) MultipartFile coverFile, // PHẢI thêm tham số này
        Model model,
        RedirectAttributes redirectAttributes) {
        
    Book existing = bookRepository.findBySlug(slug)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy sách"));

    if (result.hasErrors()) {
        addReferenceData(model);
        model.addAttribute("book", existing);
        model.addAttribute("pageTitle", "Chỉnh sửa sách");
        return "books/form";
    }

    try {
        // 1. Cập nhật các thông tin cơ bản từ form
        applyRequestToBook(existing, request);

        // 2. Xử lý nếu người dùng có chọn ảnh mới
        if (coverFile != null && !coverFile.isEmpty()) {
            String originalFileName = StringUtils.cleanPath(coverFile.getOriginalFilename());
            String fileName = System.currentTimeMillis() + "_" + originalFileName; 
            String uploadDir = "uploads/";
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = coverFile.getInputStream()) {
                Path filePath = uploadPath.resolve(fileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Cập nhật đường dẫn ảnh mới
            existing.setCoverImage("/uploads/" + fileName);
        }

        // 3. Lưu vào Database
        Book updated = bookRepository.save(existing);

        redirectAttributes.addFlashAttribute("successMessage", "Cập nhật sách thành công.");
        return "redirect:/books/" + updated.getSlug();

    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("errorMessage", "Lỗi khi cập nhật ảnh: " + e.getMessage());
        addReferenceData(model);
        model.addAttribute("book", existing);
        return "books/form";
    }
}
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{slug}/delete")
    public String delete(@PathVariable String slug, RedirectAttributes redirectAttributes) {
        Book book = bookRepository.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Khong tim thay sach"));
        bookRepository.delete(book);
        redirectAttributes.addFlashAttribute("successMessage", "Xóa sách thành công.");
        return "redirect:/books";
    }

    private BookRequest emptyBookRequest() {
    return new BookRequest(
            "",                 // title
            "",                 // subtitle
            "",                 // description
            "",                 // isbn
            null,               // publicationDate
            "en",               // language
            null,               // pageCount
            null,               // publisherId
            new ArrayList<>(),  // authorIds (Sửa null thành ArrayList rỗng)
            new ArrayList<>(),  // categoryIds (Sửa null thành ArrayList rỗng)
            null,               // coverImage
            new ArrayList<>()   // formats (Sửa null thành ArrayList rỗng)
    );
}

    private BookRequest createRequest(Book book) {
        return new BookRequest(
                book.getTitle(),
                book.getSubtitle(),
                book.getDescription(),
                book.getIsbn(),
                book.getPublicationDate(),
                book.getLanguage(),
                book.getPageCount(),
                book.getPublisher() == null ? null : book.getPublisher().getId(),
                book.getAuthors() == null ? new ArrayList<>() : book.getAuthors().stream().map(Author::getId).toList(),
                book.getCategories() == null ? new ArrayList<>() : book.getCategories().stream().map(Category::getId).toList(),
                book.getCoverImage(),
                book.getFormats());
    }

    private void addReferenceData(Model model) {
        model.addAttribute("publishers", publisherRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("authors", authorRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("categories", categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
    }

private void applyRequestToBook(Book target, BookRequest request) {
    target.setTitle(request.title());
    target.setSlug(SlugUtils.slugify(request.title()));
    target.setSubtitle(request.subtitle());
    target.setDescription(request.description());
    target.setIsbn(request.isbn());
    target.setPublicationDate(request.publicationDate());
    target.setLanguage(request.language());
    target.setPageCount(request.pageCount());

    if (request.coverImage() != null && !request.coverImage().isBlank()) {
        target.setCoverImage(request.coverImage());
    }

    target.setPublisher(resolvePublisher(request.publisherId()));
    target.setAuthors(resolveAuthors(request.authorIds()));
    target.setCategories(resolveCategories(request.categoryIds()));
    target.setFormats(request.formats() == null ? new ArrayList<>() : new ArrayList<>(request.formats()));
}

    private Publisher resolvePublisher(Long publisherId) {
        if (publisherId == null) {
            return null;
        }

        return publisherRepository.findById(publisherId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "publisherId khong hop le: " + publisherId));
    }

    private List<Author> resolveAuthors(List<Long> authorIds) {
        if (authorIds == null || authorIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Author> authors = authorRepository.findAllById(authorIds);
        validateResolvedCount("authorIds", authorIds, authors.size());
        return authors;
    }

    private List<Category> resolveCategories(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new ArrayList<>();
        }

        List<Category> categories = categoryRepository.findAllById(categoryIds);
        validateResolvedCount("categoryIds", categoryIds, categories.size());
        return categories;
    }

    private void validateResolvedCount(String fieldName, List<Long> requestedIds, int resolvedCount) {
        int uniqueCount = Set.copyOf(requestedIds).size();
        if (uniqueCount != resolvedCount) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    fieldName + " co gia tri khong ton tai trong he thong");
        }
    }

    @GetMapping("/genres")
    public String categoriesPage(Model model) {
        addCategoryData(model);
        return "categories";
    }

    private void addCategoryData(Model model) {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
        List<CategoryWithCount> categoryWithCount = categories.stream()
                .map(c -> new CategoryWithCount(c.getId(), c.getName(), bookRepository.countByCategories_Id(c.getId())))
                .toList();

        model.addAttribute("categoryWithCount", categoryWithCount);
        model.addAttribute("selectedCategoryIds", List.of());
    }

    public record CategoryWithCount(Long id, String name, Long count) {}
}
