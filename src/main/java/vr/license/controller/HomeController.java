package vr.license.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/about")
    public String about(Model model, HttpSession session) {
        // 로그인 체크
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            return "redirect:/login";
        }
        
        model.addAttribute("title", "프로젝트 정보");
        model.addAttribute("description", "Spring Boot와 Thymeleaf를 사용한 웹 애플리케이션입니다.");
        model.addAttribute("technologies", Arrays.asList("Spring Boot", "Thymeleaf", "Java", "Gradle"));
        return "about";
    }
    
    @GetMapping("/contact")
    public String contact(Model model, HttpSession session) {
        // 로그인 체크
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            return "redirect:/login";
        }
        
        model.addAttribute("title", "연락처");
        model.addAttribute("email", "contact@example.com");
        model.addAttribute("phone", "010-1234-5678");
        return "contact";
    }
} 