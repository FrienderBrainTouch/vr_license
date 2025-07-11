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

@Controller
@RequestMapping("/license")
public class LicenseController {

    // Dummy data storage
    private static final Map<String, License> licenses = new HashMap<>();
    private static final Map<String, LicenseUsage> usageRecords = new HashMap<>();

    static {
        // Initial dummy data with 25A1234-5678 format
        License license1 = new License();
        license1.setId(1L);
        license1.setLicenseKey("25A1234-5678");
        license1.setType("BASIC");
        license1.setStartDate(LocalDate.of(2024, 1, 15));
        license1.setEndDate(LocalDate.of(2024, 2, 14));
        license1.setStatus("ACTIVE");
        license1.setMaxDevices(1);
        license1.setMaxActivations(1);
        license1.setDescription("Basic License - 30 days");
        license1.setCreatedAt(LocalDateTime.now());
        licenses.put(license1.getLicenseKey(), license1);

        License license2 = new License();
        license2.setId(2L);
        license2.setLicenseKey("25B9876-5432");
        license2.setType("PREMIUM");
        license2.setStartDate(LocalDate.of(2024, 1, 10));
        license2.setEndDate(LocalDate.of(2024, 4, 10));
        license2.setStatus("USED");
        license2.setMaxDevices(1);
        license2.setMaxActivations(1);
        license2.setDescription("Premium License - 90 days");
        license2.setCreatedAt(LocalDateTime.now());
        licenses.put(license2.getLicenseKey(), license2);

        License license3 = new License();
        license3.setId(3L);
        license3.setLicenseKey("25C5555-9999");
        license3.setType("BASIC");
        license3.setStartDate(LocalDate.of(2024, 1, 1));
        license3.setEndDate(LocalDate.of(2024, 12, 31));
        license3.setStatus("ACTIVE");
        license3.setMaxDevices(1);
        license3.setMaxActivations(1);
        license3.setDescription("Offline Test License");
        license3.setCreatedAt(LocalDateTime.now());
        licenses.put(license3.getLicenseKey(), license3);
    }

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
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            return "redirect:/login";
        }
        
        model.addAttribute("licenses", licenses.values());
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

    // License creation API (requires login)
    @PostMapping("/create")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createLicense(@RequestBody LicenseRequest request, HttpSession session) {
        // Login check for API
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Auto-generate license key if not provided in request
            String licenseKey = request.getLicenseKey();
            if (licenseKey == null || licenseKey.trim().isEmpty()) {
                licenseKey = generateLicenseKey();
            }

            // Check for duplicates
            if (licenses.containsKey(licenseKey)) {
                response.put("success", false);
                response.put("message", "License key already exists.");
                return ResponseEntity.badRequest().body(response);
            }

            // Create new license
            License license = new License();
            license.setId((long) (licenses.size() + 1));
            license.setLicenseKey(licenseKey);
            license.setType(request.getLicenseType());
            license.setStartDate(LocalDate.parse(request.getStartDate()));
            license.setEndDate(LocalDate.parse(request.getEndDate()));
            license.setStatus("ACTIVE");
            license.setMaxDevices(request.getMaxDevices());
            license.setMaxActivations(request.getMaxActivations());
            license.setDescription(request.getDescription());
            license.setCreatedAt(LocalDateTime.now());

            licenses.put(licenseKey, license);

            response.put("success", true);
            response.put("message", "License created successfully.");
            response.put("license", license);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error occurred while creating license: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // VR App License Verification API (NO LOGIN REQUIRED - for VR apps)
    @PostMapping("/verify")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> verifyLicense(@RequestBody VerifyRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String licenseKey = request.getLicenseKey();
            String deviceId = request.getDeviceId();

            // Check if license exists
            License license = licenses.get(licenseKey);
            if (license == null) {
                response.put("success", false);
                response.put("message", "유효하지 않은 라이센스 키입니다.");
                response.put("code", "INVALID_LICENSE");
                return ResponseEntity.badRequest().body(response);
            }

            // Check license status
            if ("EXPIRED".equals(license.getStatus())) {
                response.put("success", false);
                response.put("message", "라이센스가 만료되었습니다.");
                response.put("code", "EXPIRED_LICENSE");
                return ResponseEntity.badRequest().body(response);
            }

            if ("USED".equals(license.getStatus())) {
                response.put("success", false);
                response.put("message", "라이센스가 이미 사용되었습니다.");
                response.put("code", "USED_LICENSE");
                return ResponseEntity.badRequest().body(response);
            }

            // Check expiration date
            if (LocalDate.now().isAfter(license.getEndDate())) {
                license.setStatus("EXPIRED");
                response.put("success", false);
                response.put("message", "라이센스가 만료되었습니다.");
                response.put("code", "EXPIRED_LICENSE");
                return ResponseEntity.badRequest().body(response);
            }

            // Create usage record
            LicenseUsage usage = new LicenseUsage();
            usage.setId((long) (usageRecords.size() + 1));
            usage.setLicenseKey(licenseKey);
            usage.setDeviceId(deviceId);
            usage.setAppVersion(request.getAppVersion());
            usage.setPlatform(request.getPlatform());
            usage.setActivatedAt(LocalDateTime.now());
            usage.setStatus("ACTIVE");

            usageRecords.put(licenseKey, usage);

            // Change license status to used
            license.setStatus("USED");

            // Build response data
            Map<String, Object> licenseData = new HashMap<>();
            licenseData.put("licenseKey", license.getLicenseKey());
            licenseData.put("type", license.getType());
            licenseData.put("startDate", license.getStartDate().toString());
            licenseData.put("endDate", license.getEndDate().toString());
            licenseData.put("maxDevices", license.getMaxDevices());
            licenseData.put("maxActivations", license.getMaxActivations());

            response.put("success", true);
            response.put("message", "라이센스 인증이 성공했습니다.");
            response.put("license", licenseData);
            response.put("token", generateToken(license, usage));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error occurred during license verification: " + e.getMessage());
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
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("licenses", new ArrayList<>(licenses.values()));
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
        
        License license = licenses.get(licenseKey);
        if (license == null) {
            response.put("success", false);
            response.put("message", "License not found.");
            return ResponseEntity.notFound().build();
        }

        LicenseUsage usage = usageRecords.get(licenseKey);
        
        response.put("success", true);
        response.put("license", license);
        response.put("usage", usage);
        
        return ResponseEntity.ok(response);
    }

    // License revocation API (requires login)
    @PostMapping("/{licenseKey}/revoke")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> revokeLicense(@PathVariable String licenseKey, HttpSession session) {
        // Login check for API
        if (session.getAttribute("isLoggedIn") == null || !(Boolean) session.getAttribute("isLoggedIn")) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Authentication required.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        License license = licenses.get(licenseKey);
        if (license == null) {
            response.put("success", false);
            response.put("message", "License not found.");
            return ResponseEntity.notFound().build();
        }

        license.setStatus("EXPIRED");
        
        response.put("success", true);
        response.put("message", "License revoked successfully.");
        
        return ResponseEntity.ok(response);
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
}
