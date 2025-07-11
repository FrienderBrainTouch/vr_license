package vr.license.model;

import java.time.LocalDateTime;

public class LicenseUsage {
    private Long id;
    private String licenseKey;
    private String deviceId;
    private String appVersion;
    private String platform;
    private LocalDateTime activatedAt;
    private LocalDateTime lastUsedAt;
    private String status;
    private String token;

    // 기본 생성자
    public LicenseUsage() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public void setActivatedAt(LocalDateTime activatedAt) {
        this.activatedAt = activatedAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return "LicenseUsage{" +
                "id=" + id +
                ", licenseKey='" + licenseKey + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", platform='" + platform + '\'' +
                ", activatedAt=" + activatedAt +
                ", lastUsedAt=" + lastUsedAt +
                ", status='" + status + '\'' +
                ", token='" + token + '\'' +
                '}';
    }
}
