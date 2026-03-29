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
}
