package vr.license.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vr.license.model.License;

public interface LicenseRepository extends JpaRepository<License, Long> {
    License findByLicenseKey(String licenseKey);
} 