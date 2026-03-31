/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.group.book_selling.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.group.book_selling.models.User;
import com.group.book_selling.repository.IUserRepository;

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

    public void prepareEmailVerification(User user) {
        String token = java.util.UUID.randomUUID().toString();
        user.setEmailVerificationToken(token);
        user.setEmailVerificationExpires(java.time.LocalDateTime.now().plusHours(1));
    }

    public boolean verifyEmailToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        User user = userRepository.findByEmailVerificationToken(token);
        if (user == null) {
            return false;
        }
        if (user.getEmailVerificationExpires() == null || user.getEmailVerificationExpires().isBefore(java.time.LocalDateTime.now())) {
            return false;
        }
        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setEmailVerificationExpires(null);
        userRepository.save(user);
        return true;
    }
}
