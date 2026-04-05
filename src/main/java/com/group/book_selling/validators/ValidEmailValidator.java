package com.group.book_selling.validators;

import org.springframework.beans.factory.annotation.Autowired;

import com.group.book_selling.repositories.IUserRepository;

import jakarta.validation.ConstraintValidator;

public class ValidEmailValidator implements ConstraintValidator<ValidEmail, String> {
    @Autowired
    private IUserRepository userRepository;

    @Override
    public boolean isValid(String email, jakarta.validation.ConstraintValidatorContext context) {
        if (email == null || email.isEmpty()) {
            return true; // Let @NotBlank handle this case
        }
        
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        if (!email.matches(emailRegex)) {
            return false;
        }

        return !userRepository.existsByEmail(email);
    }
}
