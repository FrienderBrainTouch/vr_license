package vr.license.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    // 하드코딩된 관리자 계정 (실제로는 데이터베이스에서 관리)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    // 로그인 페이지
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // 로그인 처리
    @PostMapping("/login")
    public String login(@RequestParam String username, 
                       @RequestParam String password, 
                       HttpSession session, 
                       RedirectAttributes redirectAttributes) {
        
        if (ADMIN_USERNAME.equals(username) && ADMIN_PASSWORD.equals(password)) {
            // 로그인 성공
            session.setAttribute("user", username);
            session.setAttribute("isLoggedIn", true);
            return "redirect:/";
        } else {
            // 로그인 실패
            redirectAttributes.addFlashAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "redirect:/login";
        }
    }

    // 로그아웃 처리
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    // 홈페이지 (로그인 필요)
    @GetMapping("/")
    public String home(HttpSession session, Model model) {
        // 로그인 체크
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            return "redirect:/login";
        }
        
        model.addAttribute("currentTime", java.time.LocalDateTime.now());
        return "home";
    }
} 