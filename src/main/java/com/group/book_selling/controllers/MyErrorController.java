package com.group.book_selling.controllers;

import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class MyErrorController implements ErrorController {

    @RequestMapping("/error")
    public Object handleError(HttpServletRequest request, Model model) {

        String path = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        // 🔥 Nếu là API → trả JSON
        if (path != null && path.startsWith("/api")) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("API not found: " + path);
        }

        // 👇 phần xử lý cho giao diện (HTML)
        Object statusObj = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Integer statusCode = null;

        if (statusObj != null) {
            try {
                statusCode = Integer.valueOf(statusObj.toString());
            } catch (NumberFormatException ex) {
                statusCode = null;
            }
        }

        String message = (String) request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        if (message == null) {
            Object ex = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
            if (ex != null) {
                message = ex.toString();
            }
        }

        model.addAttribute("status", statusCode);
        model.addAttribute("errorMessage", message == null ? "Đã xảy ra lỗi" : message);
        model.addAttribute("path", path == null ? "" : path);

        if (statusCode != null && statusCode == 404) {
            return "error/404";
        }

        return "error/generic";
    }
}