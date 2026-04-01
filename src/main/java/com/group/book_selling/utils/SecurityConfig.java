package com.group.book_selling.utils;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.group.book_selling.models.UserRole;
import com.group.book_selling.services.CustomUserDetailServices;

import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public UserDetailsService userDetailsService() {
                return new CustomUserDetailServices();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService());
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationFailureHandler failureHandler) throws Exception {
                return http.csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/",
                                                                "/**",
                                                                "/login",
                                                                "/register",
                                                                "/verify-email",
                                                                "/forgot-password",
                                                                "/reset-password",
                                                                "/auth/**",
                                                                "/error",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**")
                                                .permitAll()
                                                .requestMatchers("/admin/**").hasRole(UserRole.ADMIN.name())
                                                .requestMatchers("/user/**")
                                                .hasAnyRole(UserRole.ADMIN.name(), UserRole.USER.name())
                                                .anyRequest().authenticated())
                                .logout(logout -> logout
                                                .logoutSuccessUrl("/")
                                                .logoutUrl("/logout")
                                                .deleteCookies("JSESSIONID")
                                                .invalidateHttpSession(true)
                                                .clearAuthentication(true).permitAll())
                                .formLogin(form -> form.loginPage("/login")
                                                .loginProcessingUrl("/login")
                                                .defaultSuccessUrl("/")
                                                .failureHandler(failureHandler)
                                                .permitAll())
                                .rememberMe(rememberMe -> rememberMe.key("uniqueAndSecret").tokenValiditySeconds(86400)
                                                .userDetailsService(userDetailsService()))
                                .exceptionHandling(exceptionHandling -> exceptionHandling
                                                .authenticationEntryPoint((req, res, authException) -> {
                                                        if ("GET".equalsIgnoreCase(req.getMethod())) {
                                                                res.sendError(HttpServletResponse.SC_NOT_FOUND);
                                                        } else {
                                                                res.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                                                        }
                                                }).accessDeniedPage("/403"))
                                .build();
        }
}
