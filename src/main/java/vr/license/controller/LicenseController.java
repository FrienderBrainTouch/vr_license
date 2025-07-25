package vr.license.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import vr.license.dto.LicenseRequest;
import vr.license.dto.VerifyRequest;
import vr.license.model.License;
import vr.license.model.LicenseUsage;
import jakarta.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import vr.license.repository.LicenseRepository;
import vr.license.repository.LicenseUsageRepository;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import jakarta.annotation.PostConstruct;

@Controller
@RequestMapping("/license")
public class LicenseController {

    @Autowired
    private LicenseRepository licenseRepository;
    @Autowired
    private LicenseUsageRepository licenseUsageRepository;

    // License creation page (requires login)
    @GetMapping("/create")
    public String createLicensePage(HttpSession session) {
        // Login check
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            return "redirect:/login";
        }
        return "license/create";
    }

    // License management page (requires login)
    @GetMapping("/manage")
    public String manageLicensePage(Model model, HttpSession session) {
        // Login check
        // if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
        //     return "redirect:/login";
        // }
        // 만료일이 지난 라이선스 자동 만료 처리
        List<License> licenses = licenseRepository.findAll();
        System.out.println("[만료체크] 전체 라이선스 개수: " + licenses.size());
        boolean updated = false;
        int checked = 0;
        for (License license : licenses) {
            checked++;
            System.out.println("[만료체크] #" + checked + " | key: " + license.getLicenseKey() + ", type: " + license.getType() + ", endDate: " + license.getEndDate() + ", status: " + license.getStatus());
            if (license.getEndDate() != null && java.time.LocalDateTime.now().isAfter(license.getEndDate()) && !"EXPIRED".equals(license.getStatus())) {
                System.out.println("[만료체크] → 만료 처리 대상! key: " + license.getLicenseKey());
                license.setStatus("EXPIRED");
                licenseRepository.save(license);
                updated = true;
            }
        }
        if (checked == 0) {
            System.out.println("[만료체크] for문에 진입했으나 라이선스가 없습니다.");
        }
        if (updated) {
            System.out.println("[만료체크] 만료 처리 후 라이선스 목록 재조회");
            licenses = licenseRepository.findAll(); // 갱신 후 재조회
        }
        // 정렬: 사용중 > 활성 > 만료 > 기타, 각 그룹 내 최신 생성일 내림차순
        System.out.println("[정렬전] 라이선스 목록:");
        for (License l : licenses) {
            System.out.println("  - " + l.getLicenseKey() + " | status: " + l.getStatus() + " | createdAt: " + l.getCreatedAt());
        }
        
        licenses.sort(Comparator
            .comparing((License l) -> {
                String status = l.getStatus();
                if ("IN_USE".equalsIgnoreCase(status) || "사용중".equals(status)) return 0;
                if ("ACTIVE".equalsIgnoreCase(status) || "활성".equals(status)) return 1;
                if ("EXPIRED".equalsIgnoreCase(status) || "만료".equals(status)) return 2;
                return 3; // 기타 상태
            })
            .thenComparing(License::getCreatedAt, Comparator.reverseOrder())
        );
        
        System.out.println("[정렬후] 라이선스 목록:");
        for (License l : licenses) {
            System.out.println("  - " + l.getLicenseKey() + " | status: " + l.getStatus() + " | createdAt: " + l.getCreatedAt());
        }
        model.addAttribute("licenses", licenses);
        return "license/manage";
    }

    // VR app simulator page (requires login)
    @GetMapping("/simulator")
    public String simulatorPage(HttpSession session) {
        // Login check
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            return "redirect:/login";
        }
        return "license/simulator";
    }

    // 라이선스 생성 API (requires login)
    @PostMapping("/create")
    public String createLicense(@ModelAttribute LicenseRequest request, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("error", "Authentication required.");
            return "redirect:/login";
        }
        try {
            String licenseKey = request.getLicenseKey();
            if (licenseKey == null || licenseKey.trim().isEmpty()) {
                licenseKey = generateLicenseKey();
            }
            if (licenseRepository.findByLicenseKey(licenseKey) != null) {
                redirectAttributes.addFlashAttribute("error", "라이센스 키가 이미 존재합니다.");
                return "redirect:/";
            }
            License license = new License();
            license.setLicenseKey(licenseKey);
            license.setType(request.getLicenseType());
            license.setStartDate(java.time.LocalDateTime.parse(request.getStartDate()));
            if ("CUSTOM".equals(request.getLicenseType())) {
                license.setEndDate(java.time.LocalDateTime.parse(request.getEndDate()));
            } else {
                license.setEndDate(null); // 최초 인증 시점에 부여
            }
            license.setStatus("ACTIVE");
            license.setMaxDevices(request.getMaxDevices());
            license.setMaxActivations(request.getMaxActivations());
            license.setDescription(request.getDescription());
            license.setCreatedAt(java.time.LocalDateTime.now());
            licenseRepository.save(license);
            redirectAttributes.addFlashAttribute("success", "라이센스가 성공적으로 생성되었습니다.");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "생성 중 오류 발생: " + e.getMessage());
            return "redirect:/";
        }
    }

    // 라이센스 인증 API (NO LOGIN REQUIRED - for VR apps)
    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyLicense(@RequestBody String rawJson) {
        Map<String, Object> response = new HashMap<>();
        try {
            System.out.println("Received raw JSON: " + rawJson);
            
            // Jackson ObjectMapper를 사용하여 JSON 파싱
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> jsonMap = mapper.readValue(rawJson, Map.class);
            String licenseKey = (String) jsonMap.get("key");
            
            System.out.println("Extracted license key: " + licenseKey);
            
            License license = licenseRepository.findByLicenseKey(licenseKey);
            System.out.println("Found license: " + (license != null ? license.toString() : "null"));
            
            if (license == null) {
                System.out.println("License not found in database");
                response.put("status", "NOT_FOUND");
                response.put("expiry", "");
                return ResponseEntity.ok(response);
            }
            
            System.out.println("License found: " + license.toString());
            System.out.println("Current time: " + java.time.LocalDateTime.now());
            System.out.println("License end date: " + license.getEndDate());
            
            // 만료일 체크
            if (license.getEndDate() != null && java.time.LocalDateTime.now().isAfter(license.getEndDate())) {
                System.out.println("License is expired, updating status to EXPIRED");
                license.setStatus("EXPIRED");
                licenseRepository.save(license);
                response.put("status", "EXPIRED");
                response.put("expiry", license.getEndDate().toString());
                return ResponseEntity.ok(response);
            }
            
            // 최초 인증: endDate가 null이고 CUSTOM이 아니면 타입에 따라 만료일 계산
            if (license.getEndDate() == null && !"CUSTOM".equals(license.getType())) {
                System.out.println("First activation, calculating end date");
                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                int days = 0;
                switch (license.getType()) {
                    case "TRIAL": days = 7; break;
                    case "BASIC": days = 30; break;
                    case "PREMIUM": days = 90; break;
                    case "ENTERPRISE": days = 365; break;
                    default: days = 30;
                }
                license.setEndDate(now.plusDays(days));
                System.out.println("Updated license with end date: " + license.getEndDate());
            }
            
            // 인증 성공 시 상태를 'IN_USE'로 변경
            if (!"IN_USE".equals(license.getStatus())) {
                System.out.println("License status updated to IN_USE");
                license.setStatus("IN_USE");
            }
            licenseRepository.save(license);
            
            // 인증 성공 시 사용 기록 업데이트
            System.out.println("License is valid, updating usage record");
            LicenseUsage usage = licenseUsageRepository.findByLicenseKey(licenseKey);
            if (usage == null) {
                usage = new LicenseUsage();
                usage.setLicenseKey(licenseKey);
                usage.setDeviceId("VR_DEVICE"); // 기본값
                usage.setActivatedAt(java.time.LocalDateTime.now());
                usage.setLastUsedAt(java.time.LocalDateTime.now());
                licenseUsageRepository.save(usage);
                System.out.println("Created new usage record");
            } else {
                usage.setLastUsedAt(java.time.LocalDateTime.now());
                licenseUsageRepository.save(usage);
                System.out.println("Updated existing usage record");
            }
            
            response.put("status", "VALID");
            response.put("expiry", license.getEndDate() != null ? license.getEndDate().toString() : "");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "ERROR");
            response.put("expiry", "");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // License list API (requires login)
    @GetMapping("/list")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLicenses(HttpSession session) {
        // Login check for API
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        // 만료일이 지난 라이선스 자동 만료 처리
        List<License> licenses = licenseRepository.findAll();
        boolean updated = false;
        for (License license : licenses) {
            System.out.println("Checking license: " + license.getLicenseKey() + ", endDate: " + license.getEndDate() + ", status: " + license.getStatus());
            if (license.getEndDate() != null && java.time.LocalDateTime.now().isAfter(license.getEndDate()) && !"EXPIRED".equals(license.getStatus())) {
                System.out.println("Expiring license: " + license.getLicenseKey());
                license.setStatus("EXPIRED");
                licenseRepository.save(license);
                updated = true;
            }
        }
        if (updated) {
            licenses = licenseRepository.findAll(); // 갱신 후 재조회
        }
        // 정렬: 사용중 > 활성 > 만료 > 기타, 각 그룹 내 최신 생성일 내림차순
        licenses.sort(Comparator
            .comparing((License l) -> {
                String status = l.getStatus();
                if ("IN_USE".equalsIgnoreCase(status) || "사용중".equals(status)) return 0;
                if ("ACTIVE".equalsIgnoreCase(status) || "활성".equals(status)) return 1;
                if ("EXPIRED".equalsIgnoreCase(status) || "만료".equals(status)) return 2;
                return 3; // 기타 상태
            })
            .thenComparing(License::getCreatedAt, Comparator.reverseOrder())
        );
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("licenses", licenses);
        return ResponseEntity.ok(response);
    }

    // License detail API (requires login)
    @GetMapping("/{licenseKey}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getLicense(@PathVariable String licenseKey, HttpSession session) {
        // Login check for API
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        License license = licenseRepository.findByLicenseKey(licenseKey);
        if (license == null) {
            response.put("success", false);
            response.put("message", "License not found.");
            return ResponseEntity.notFound().build();
        }

        LicenseUsage usage = licenseUsageRepository.findByLicenseKey(licenseKey);
        
        response.put("success", true);
        response.put("license", license);
        response.put("usage", usage);
        
        return ResponseEntity.ok(response);
    }

    // License revocation API (requires login)
    @PostMapping("/{licenseKey}/revoke")
    public String revokeLicense(@PathVariable String licenseKey, HttpSession session, RedirectAttributes redirectAttributes) {
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            redirectAttributes.addFlashAttribute("error", "Authentication required.");
            return "redirect:/login";
        }
        License license = licenseRepository.findByLicenseKey(licenseKey);
        if (license == null) {
            redirectAttributes.addFlashAttribute("error", "라이센스를 찾을 수 없습니다.");
            return "redirect:/";
        }
        license.setStatus("EXPIRED");
        licenseRepository.save(license);
        redirectAttributes.addFlashAttribute("success", "라이센스가 만료(취소) 처리되었습니다.");
        return "redirect:/";
    }

    // Auto-generate license key with 25A1234-5678 format
    private String generateLicenseKey() {
        // Get current year (last 2 digits)
        int currentYear = java.time.LocalDate.now().getYear() % 100;
        
        // Generate random uppercase letter
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        char randomLetter = letters.charAt(new Random().nextInt(letters.length()));
        
        // Generate random 4-digit number for first part
        int firstPart = new Random().nextInt(9000) + 1000; // 1000-9999
        
        // Generate random 4-digit number for second part
        int secondPart = new Random().nextInt(9000) + 1000; // 1000-9999
        
        // Format: YYALLLL-MMMM (YY=year, A=letter, LLLL=first 4 digits, MMMM=second 4 digits)
        return String.format("%02d%c%04d-%04d", currentYear, randomLetter, firstPart, secondPart);
    }

    // Generate token (dummy)
    private String generateToken(License license, LicenseUsage usage) {
        // In real implementation, use JWT or other token generation method
        long epoch = usage.getActivatedAt().toEpochSecond(java.time.ZoneOffset.UTC);
        return "TOKEN_" + license.getLicenseKey() + "_" + epoch;
    }

    // 12시간마다 만료 체크 스케줄러
    @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // 12시간 = 12 * 60 * 60 * 1000 밀리초
    public void checkExpiredLicenses() {
        System.out.println("[스케줄러] 12시간마다 만료 체크 시작 - " + java.time.LocalDateTime.now());
        performExpirationCheck();
    }

    // 서버 시작 시 즉시 만료 체크
    @PostConstruct
    public void checkExpiredLicensesOnStartup() {
        System.out.println("[시작체크] 서버 시작 시 만료 체크 시작 - " + java.time.LocalDateTime.now());
        performExpirationCheck();
    }

    // 만료 체크 공통 메서드
    private void performExpirationCheck() {
        List<License> licenses = licenseRepository.findAll();
        System.out.println("[만료체크] 전체 라이선스 개수: " + licenses.size());
        int expiredCount = 0;
        for (License license : licenses) {
            if (license.getEndDate() != null && java.time.LocalDateTime.now().isAfter(license.getEndDate()) && !"EXPIRED".equals(license.getStatus())) {
                System.out.println("[만료체크] 만료 처리: " + license.getLicenseKey() + " (endDate: " + license.getEndDate() + ")");
                license.setStatus("EXPIRED");
                licenseRepository.save(license);
                expiredCount++;
            }
        }
        System.out.println("[만료체크] 만료 처리 완료 - " + expiredCount + "개 라이선스 만료 처리됨");
    }
}
