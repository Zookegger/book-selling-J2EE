package com.group.book_selling.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.group.book_selling.models.User;

public interface IUserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.email = :email")
    User findByEmail(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email = :email")
    boolean existsByEmail(@Param("email") String email);

    @Query("SELECT u FROM User u WHERE u.emailVerificationToken = :token")
    User findByEmailVerificationToken(@Param("token") String token);

    @Query("SELECT u FROM User u WHERE u.passwordResetToken = :token")
    User findByPasswordResetToken(@Param("token") String token);
}
