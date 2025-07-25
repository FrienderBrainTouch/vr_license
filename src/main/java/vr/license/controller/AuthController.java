package vr.license.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vr.license.model.License;
import vr.license.repository.LicenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Comparator;
import java.util.stream.Collectors;
import java.util.List;

@Controller
public class AuthController {

    // 하드코딩된 관리자 계정 (실제로는 데이터베이스에서 관리)
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    @Autowired
    private LicenseRepository licenseRepository;

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
    public String home(@RequestParam(value = "filter", required = false, defaultValue = "all") String filter, HttpSession session, Model model) {
        // 로그인 체크
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            return "redirect:/login";
        }

        // 만료일이 지난 라이선스 자동 만료 처리
        List<License> licenses = licenseRepository.findAll();
        boolean updated = false;
        for (License license : licenses) {
            if (license.getEndDate() != null && java.time.LocalDateTime.now().isAfter(license.getEndDate()) && !"EXPIRED".equals(license.getStatus())) {
                license.setStatus("EXPIRED");
                licenseRepository.save(license);
                updated = true;
            }
        }
        if (updated) {
            licenses = licenseRepository.findAll(); // 갱신 후 재조회
        }

        model.addAttribute("totalLicenses", licenses.size());
        model.addAttribute("activeLicenses", licenses.stream().filter(l -> "ACTIVE".equals(l.getStatus())).count());
        model.addAttribute("expiredLicenses", licenses.stream().filter(l -> "EXPIRED".equals(l.getStatus())).count());
        model.addAttribute("usedLicenses", licenses.stream().filter(l -> "USED".equals(l.getStatus())).count());
        model.addAttribute("recentLicenses", licenses.stream().sorted(Comparator.comparing(License::getCreatedAt).reversed()).limit(5).collect(Collectors.toList()));
        model.addAttribute("currentTime", java.time.LocalDateTime.now());
        // 필터링
        List<License> filtered;
        switch (filter) {
            case "active":
                filtered = licenses.stream().filter(l -> "ACTIVE".equals(l.getStatus()))
                    .sorted(Comparator.comparing((License l) -> {
                        String status = l.getStatus();
                        if ("USED".equalsIgnoreCase(status) || "사용중".equals(status)) return 0;
                        if ("ACTIVE".equalsIgnoreCase(status) || "활성".equals(status)) return 1;
                        if ("EXPIRED".equalsIgnoreCase(status) || "만료".equals(status)) return 2;
                        return 3;
                    }).thenComparing(License::getCreatedAt, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
                break;
            case "expired":
                filtered = licenses.stream().filter(l -> "EXPIRED".equals(l.getStatus()))
                    .sorted(Comparator.comparing((License l) -> {
                        String status = l.getStatus();
                        if ("USED".equalsIgnoreCase(status) || "사용중".equals(status)) return 0;
                        if ("ACTIVE".equalsIgnoreCase(status) || "활성".equals(status)) return 1;
                        if ("EXPIRED".equalsIgnoreCase(status) || "만료".equals(status)) return 2;
                        return 3;
                    }).thenComparing(License::getCreatedAt, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
                break;
            case "used":
                filtered = licenses.stream().filter(l -> "USED".equals(l.getStatus()))
                    .sorted(Comparator.comparing((License l) -> {
                        String status = l.getStatus();
                        if ("USED".equalsIgnoreCase(status) || "사용중".equals(status)) return 0;
                        if ("ACTIVE".equalsIgnoreCase(status) || "활성".equals(status)) return 1;
                        if ("EXPIRED".equalsIgnoreCase(status) || "만료".equals(status)) return 2;
                        return 3;
                    }).thenComparing(License::getCreatedAt, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
                break;
            default:
                filtered = licenses.stream()
                    .sorted(Comparator.comparing((License l) -> {
                        String status = l.getStatus();
                        if ("USED".equalsIgnoreCase(status) || "사용중".equals(status)) return 0;
                        if ("ACTIVE".equalsIgnoreCase(status) || "활성".equals(status)) return 1;
                        if ("EXPIRED".equalsIgnoreCase(status) || "만료".equals(status)) return 2;
                        return 3;
                    }).thenComparing(License::getCreatedAt, Comparator.reverseOrder()))
                    .collect(Collectors.toList());
        }
        model.addAttribute("licenses", filtered);
        model.addAttribute("filter", filter);
        return "home";
    }
} 