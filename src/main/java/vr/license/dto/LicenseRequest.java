package vr.license.dto;

public class LicenseRequest {
    private String licenseKey;
    private String licenseType;
    private String startDate;
    private String endDate;
    private String description;
    private Integer maxDevices;
    private Integer maxActivations;

    // 기본 생성자
    public LicenseRequest() {}

    // Getters and Setters
    public String getLicenseKey() {
        return licenseKey;
    }

    public void setLicenseKey(String licenseKey) {
        this.licenseKey = licenseKey;
    }

    public String getLicenseType() {
        return licenseType;
    }

    public void setLicenseType(String licenseType) {
        this.licenseType = licenseType;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMaxDevices() {
        return maxDevices;
    }

    public void setMaxDevices(Integer maxDevices) {
        this.maxDevices = maxDevices;
    }

    public Integer getMaxActivations() {
        return maxActivations;
    }

    public void setMaxActivations(Integer maxActivations) {
        this.maxActivations = maxActivations;
    }

    @Override
    public String toString() {
        return "LicenseRequest{" +
                "licenseKey='" + licenseKey + '\'' +
                ", licenseType='" + licenseType + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", description='" + description + '\'' +
                ", maxDevices=" + maxDevices +
                ", maxActivations=" + maxActivations +
                '}';
    }
}
