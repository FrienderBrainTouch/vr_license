package vr.license.dto;

public class VerifyRequest {
    private String licenseKey;
    private String deviceId;
    private String appVersion;
    private String platform;

    // 기본 생성자
    public VerifyRequest() {}

    // Getters and Setters
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

    @Override
    public String toString() {
        return "VerifyRequest{" +
                "licenseKey='" + licenseKey + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", appVersion='" + appVersion + '\'' +
                ", platform='" + platform + '\'' +
                '}';
    }
}
