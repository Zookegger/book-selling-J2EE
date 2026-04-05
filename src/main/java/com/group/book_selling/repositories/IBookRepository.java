package com.group.book_selling.repositories;

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

    // Tìm sách theo danh sách ID
    List<Book> findByIdIn(List<Long> ids);

    // Tìm sách theo keyword và categorySlug (nếu có)
    @Query("SELECT DISTINCT b FROM Book b JOIN b.categories c WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND c.slug = :categorySlug " +
           "ORDER BY b.id DESC")
    List<Book> searchBooksByCategorySlug(@Param("keyword") String keyword, @Param("categorySlug") String categorySlug);

        @Query("SELECT DISTINCT b FROM Book b JOIN b.categories c WHERE c.slug = :categorySlug ORDER BY b.id DESC")
        List<Book> findByCategorySlug(@Param("categorySlug") String categorySlug);

    // Tìm sách theo keyword và publisherSlug 
    @Query("SELECT DISTINCT b FROM Book b JOIN b.publisher p WHERE " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND p.slug = :publisherSlug " +
           "ORDER BY b.id DESC")
    List<Book> searchBooksByPublisherSlug(@Param("keyword") String keyword, @Param("publisherSlug") String publisherSlug);

    // Tìm tất cả sách của một NXB theo slug (Sắp xếp mới nhất lên đầu)
    @Query("SELECT DISTINCT b FROM Book b JOIN b.publisher p WHERE p.slug = :publisherSlug ORDER BY b.id DESC")
    List<Book> findByPublisherSlug(@Param("publisherSlug") String publisherSlug);

    // Số lượng sách theo thể loại
    long countByCategories_Id(Long categoryId);

    // Sách phổ biến (mới nhất) theo thể loại
    List<Book> findTop5ByCategories_IdOrderByCreatedAtDesc(Long categoryId);
}
