package com.group.book_selling.validators;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

@Target({TYPE, FIELD})
@Retention(RUNTIME)
@Constraint(validatedBy = UniqueIsbnValidator.class)
public @interface UniqueIsbn {
    String message() default "ISBN already exists";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
