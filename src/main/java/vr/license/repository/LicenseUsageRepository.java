package vr.license.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vr.license.model.LicenseUsage;

public interface LicenseUsageRepository extends JpaRepository<LicenseUsage, Long> {
    LicenseUsage findByLicenseKey(String licenseKey);
} 