/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.group.book_selling.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.group.book_selling.models.User;
import com.group.book_selling.repositories.IUserRepository;

/**
 *
 * @author Nguyen Duc Trung
 */
@Service
public class UserServices {
    @Autowired
    private IUserRepository userRepository;

    public void save(User user) {
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private void setEmailVerification(User user, String token, java.time.LocalDateTime expires) {
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpires(expires);
        userRepository.save(user);
    }

    private void setPasswordReset(User user, String token, java.time.LocalDateTime expires) {
        user.setPasswordResetToken(token);
        user.setPasswordResetExpires(expires);
        userRepository.save(user);
    }

    public void prepareEmailVerification(User user) {
        String token = java.util.UUID.randomUUID().toString();
        setEmailVerification(user, token, java.time.LocalDateTime.now().plusHours(1));
    }

    public boolean verifyEmailToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        User user = userRepository.findByEmailVerificationToken(token);
        if (user == null) {
            return false;
        }
        if (user.getEmailVerificationExpires() == null
                || user.getEmailVerificationExpires().isBefore(java.time.LocalDateTime.now())) {
            return false;
        }
        user.setEmailVerified(true);
        setEmailVerification(user, null, null);
        return true;
    }

    public void preparePasswordReset(User user) {
        String token = java.util.UUID.randomUUID().toString();
        setPasswordReset(user, token, java.time.LocalDateTime.now().plusHours(1));
    }

    public User findByPasswordResetToken(String token) {
        return userRepository.findByPasswordResetToken(token);
    }

    public boolean resetPassword(String token, String newPassword) {
        if (token == null || token.isBlank()) {
            return false;
        }
        User user = userRepository.findByPasswordResetToken(token);
        if (user == null) {
            return false;
        }
        if (user.getPasswordResetExpires() == null
                || user.getPasswordResetExpires().isBefore(java.time.LocalDateTime.now())) {
            return false;
        }
        user.setPassword(newPassword);
        setPasswordReset(user, null, null);
        return true;
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean checkPassword(String raw, String encoded) {
        return passwordEncoder.matches(raw, encoded);
    }
}
