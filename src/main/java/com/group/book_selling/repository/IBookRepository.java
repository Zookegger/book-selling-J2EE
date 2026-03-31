package com.group.book_selling.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.group.book_selling.models.Book;

public interface IBookRepository extends JpaRepository<Book, Long> {

    // Tìm theo slug
    Optional<Book> findBySlug(String slug);

    // Tìm theo ISBN
    Optional<Book> findByIsbn(String isbn);

    // SEARCH FUNCTION
    @Query("""
        SELECT DISTINCT b FROM Book b
        LEFT JOIN b.authors a
        LEFT JOIN b.publisher p
        WHERE LOWER(b.title) LIKE LOWER(CONCAT('%',:keyword,'%'))
        OR LOWER(a.name) LIKE LOWER(CONCAT('%',:keyword,'%'))
        OR LOWER(b.isbn) LIKE LOWER(CONCAT('%',:keyword,'%'))
        OR LOWER(p.name) LIKE LOWER(CONCAT('%',:keyword,'%'))
    """)
    List<Book> searchBooks(@Param("keyword") String keyword);

    // Tìm sách theo danh sách ID thể loại
    @Query("SELECT DISTINCT b FROM Book b JOIN b.categories c WHERE c.id IN :categoryIds")
    List<Book> findDistinctByCategories_IdIn(@Param("categoryIds") List<Long> categoryIds);

    // Số lượng sách theo thể loại
    long countByCategories_Id(Long categoryId);
}
