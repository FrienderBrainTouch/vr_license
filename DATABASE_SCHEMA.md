# VR 라이센스 관리 시스템 - 데이터베이스 스키마

## 📋 목차

1. [개요](#개요)
2. [테이블 구조](#테이블-구조)
3. [인덱스](#인덱스)
4. [제약 조건](#제약-조건)
5. [샘플 데이터](#샘플-데이터)
6. [마이그레이션](#마이그레이션)

## 🎯 개요

VR 라이센스 관리 시스템의 데이터베이스 스키마입니다. 현재는 인메모리 더미 데이터를 사용하지만, 실제 구현 시에는 MySQL 또는 PostgreSQL을 사용하는 것을 권장합니다.

## 📊 테이블 구조

### 1. licenses 테이블

라이센스 정보를 저장하는 메인 테이블입니다.

```sql
CREATE TABLE licenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_key VARCHAR(20) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    max_devices INT NOT NULL DEFAULT 1,
    max_activations INT NOT NULL DEFAULT 1,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_license_key (license_key),
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_end_date (end_date)
);
```

#### 컬럼 설명
- `id`: 고유 식별자 (자동 증가)
- `license_key`: 라이센스 키 (25A1234-5678 형식)
- `type`: 라이센스 유형 (BASIC, PREMIUM, ENTERPRISE)
- `start_date`: 시작일
- `end_date`: 만료일
- `status`: 상태 (ACTIVE, USED, EXPIRED)
- `max_devices`: 최대 디바이스 수
- `max_activations`: 최대 활성화 수
- `description`: 설명
- `created_at`: 생성일
- `updated_at`: 수정일

### 2. license_usages 테이블

라이센스 사용 기록을 저장하는 테이블입니다.

```sql
CREATE TABLE license_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_key VARCHAR(20) NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    app_version VARCHAR(20),
    platform VARCHAR(20),
    activated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    token TEXT,
    
    FOREIGN KEY (license_key) REFERENCES licenses(license_key) ON DELETE CASCADE,
    INDEX idx_license_key (license_key),
    INDEX idx_device_id (device_id),
    INDEX idx_activated_at (activated_at),
    INDEX idx_status (status)
);
```

#### 컬럼 설명
- `id`: 고유 식별자 (자동 증가)
- `license_key`: 라이센스 키 (licenses 테이블 참조)
- `device_id`: 디바이스 ID
- `app_version`: 앱 버전
- `platform`: 플랫폼 (VR, Mobile 등)
- `activated_at`: 활성화 시간
- `last_used_at`: 마지막 사용 시간
- `status`: 상태 (ACTIVE, INACTIVE)
- `token`: 인증 토큰

### 3. users 테이블 (선택사항)

관리자 사용자 정보를 저장하는 테이블입니다.

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);
```

#### 컬럼 설명
- `id`: 고유 식별자 (자동 증가)
- `username`: 사용자명
- `password`: 암호화된 비밀번호
- `email`: 이메일
- `role`: 역할 (ADMIN, USER)
- `is_active`: 활성 상태
- `created_at`: 생성일
- `last_login_at`: 마지막 로그인 시간

### 4. audit_logs 테이블 (선택사항)

시스템 활동 로그를 저장하는 테이블입니다.

```sql
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(50),
    details JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_action (action),
    INDEX idx_resource_type (resource_type),
    INDEX idx_created_at (created_at),
    INDEX idx_user_id (user_id)
);
```

#### 컬럼 설명
- `id`: 고유 식별자 (자동 증가)
- `user_id`: 사용자 ID (users 테이블 참조)
- `action`: 수행된 액션 (CREATE, UPDATE, DELETE, VERIFY)
- `resource_type`: 리소스 타입 (LICENSE, USER)
- `resource_id`: 리소스 ID
- `details`: 상세 정보 (JSON)
- `ip_address`: IP 주소
- `user_agent`: 사용자 에이전트
- `created_at`: 생성일

## 🔍 인덱스

### 성능 최적화를 위한 인덱스

```sql
-- licenses 테이블 인덱스
CREATE INDEX idx_licenses_status_type ON licenses(status, type);
CREATE INDEX idx_licenses_end_date_status ON licenses(end_date, status);
CREATE INDEX idx_licenses_created_at ON licenses(created_at);

-- license_usages 테이블 인덱스
CREATE INDEX idx_license_usages_license_device ON license_usages(license_key, device_id);
CREATE INDEX idx_license_usages_activated_status ON license_usages(activated_at, status);

-- 복합 인덱스
CREATE INDEX idx_licenses_type_status_date ON licenses(type, status, end_date);
CREATE INDEX idx_license_usages_license_activated ON license_usages(license_key, activated_at);
```

### 인덱스 사용 예시

```sql
-- 활성 라이센스 조회
SELECT * FROM licenses WHERE status = 'ACTIVE' AND end_date >= CURDATE();

-- 특정 디바이스의 라이센스 사용 기록
SELECT * FROM license_usages WHERE license_key = '25A1234-5678' AND device_id = 'device123';

-- 만료된 라이센스 조회
SELECT * FROM licenses WHERE status = 'ACTIVE' AND end_date < CURDATE();

-- 최근 생성된 라이센스 조회
SELECT * FROM licenses WHERE created_at >= DATE_SUB(NOW(), INTERVAL 30 DAY);
```

## 🔒 제약 조건

### 외래 키 제약 조건

```sql
-- license_usages 테이블의 license_key가 licenses 테이블을 참조
ALTER TABLE license_usages 
ADD CONSTRAINT fk_license_usages_license_key 
FOREIGN KEY (license_key) REFERENCES licenses(license_key) 
ON DELETE CASCADE;

-- audit_logs 테이블의 user_id가 users 테이블을 참조
ALTER TABLE audit_logs 
ADD CONSTRAINT fk_audit_logs_user_id 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE SET NULL;
```

### 체크 제약 조건

```sql
-- 라이센스 상태 체크
ALTER TABLE licenses 
ADD CONSTRAINT chk_license_status 
CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED'));

-- 라이센스 유형 체크
ALTER TABLE licenses 
ADD CONSTRAINT chk_license_type 
CHECK (type IN ('TRIAL', 'BASIC', 'PREMIUM', 'ENTERPRISE'));

-- 날짜 체크
ALTER TABLE licenses 
ADD CONSTRAINT chk_license_dates 
CHECK (start_date <= end_date);

-- 사용 기록 상태 체크
ALTER TABLE license_usages 
ADD CONSTRAINT chk_usage_status 
CHECK (status IN ('ACTIVE', 'INACTIVE'));
```

## 📝 샘플 데이터

### licenses 테이블 샘플 데이터

```sql
INSERT INTO licenses (license_key, type, start_date, end_date, status, max_devices, max_activations, description) VALUES
('25A1234-5678', 'BASIC', '2024-01-15', '2024-02-14', 'ACTIVE', 1, 1, 'Basic License - 30 days'),
('25B9876-5432', 'PREMIUM', '2024-01-10', '2024-04-10', 'USED', 1, 1, 'Premium License - 90 days'),
('25C5555-9999', 'BASIC', '2024-01-01', '2024-12-31', 'ACTIVE', 1, 1, 'Offline Test License'),
('25D1111-2222', 'ENTERPRISE', '2024-01-01', '2024-12-31', 'ACTIVE', 5, 10, 'Enterprise License - 365 days'),
('25E3333-4444', 'TRIAL', '2024-01-01', '2024-01-08', 'EXPIRED', 1, 1, 'Trial License - 7 days');
```

### license_usages 테이블 샘플 데이터

```sql
INSERT INTO license_usages (license_key, device_id, app_version, platform, activated_at, status, token) VALUES
('25B9876-5432', 'device123', '1.0.0', 'VR', '2024-01-15 10:30:00', 'ACTIVE', 'TOKEN_25B9876-5432_1705123456789'),
('25C5555-9999', 'device456', '1.0.0', 'VR', '2024-01-16 14:20:00', 'ACTIVE', 'TOKEN_25C5555-9999_1705212345678');
```

### users 테이블 샘플 데이터

```sql
INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@example.com', 'ADMIN'),
('manager', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'manager@example.com', 'ADMIN');
```

### audit_logs 테이블 샘플 데이터

```sql
INSERT INTO audit_logs (user_id, action, resource_type, resource_id, details, ip_address) VALUES
(1, 'CREATE', 'LICENSE', '25A1234-5678', '{"type": "BASIC", "description": "Basic License"}', '192.168.1.100'),
(1, 'VERIFY', 'LICENSE', '25B9876-5432', '{"device_id": "device123", "result": "success"}', '192.168.1.101'),
(1, 'UPDATE', 'LICENSE', '25B9876-5432', '{"status": "USED"}', '192.168.1.100');
```

## 🔄 마이그레이션

### 초기 마이그레이션 스크립트

```sql
-- 1. 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS vr_license CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 2. 테이블 생성
USE vr_license;

-- licenses 테이블
CREATE TABLE licenses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_key VARCHAR(20) NOT NULL UNIQUE,
    type VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    max_devices INT NOT NULL DEFAULT 1,
    max_activations INT NOT NULL DEFAULT 1,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_license_key (license_key),
    INDEX idx_status (status),
    INDEX idx_type (type),
    INDEX idx_end_date (end_date),
    INDEX idx_licenses_status_type (status, type),
    INDEX idx_licenses_end_date_status (end_date, status),
    INDEX idx_licenses_created_at (created_at),
    INDEX idx_licenses_type_status_date (type, status, end_date),
    
    CONSTRAINT chk_license_status CHECK (status IN ('ACTIVE', 'USED', 'EXPIRED')),
    CONSTRAINT chk_license_type CHECK (type IN ('TRIAL', 'BASIC', 'PREMIUM', 'ENTERPRISE')),
    CONSTRAINT chk_license_dates CHECK (start_date <= end_date)
);

-- license_usages 테이블
CREATE TABLE license_usages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    license_key VARCHAR(20) NOT NULL,
    device_id VARCHAR(100) NOT NULL,
    app_version VARCHAR(20),
    platform VARCHAR(20),
    activated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    token TEXT,
    
    FOREIGN KEY (license_key) REFERENCES licenses(license_key) ON DELETE CASCADE,
    INDEX idx_license_key (license_key),
    INDEX idx_device_id (device_id),
    INDEX idx_activated_at (activated_at),
    INDEX idx_status (status),
    INDEX idx_license_usages_license_device (license_key, device_id),
    INDEX idx_license_usages_activated_status (activated_at, status),
    INDEX idx_license_usages_license_activated (license_key, activated_at),
    
    CONSTRAINT chk_usage_status CHECK (status IN ('ACTIVE', 'INACTIVE'))
);

-- users 테이블 (선택사항)
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_role (role)
);

-- audit_logs 테이블 (선택사항)
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    action VARCHAR(50) NOT NULL,
    resource_type VARCHAR(50) NOT NULL,
    resource_id VARCHAR(50),
    details JSON,
    ip_address VARCHAR(45),
    user_agent TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_action (action),
    INDEX idx_resource_type (resource_type),
    INDEX idx_created_at (created_at),
    INDEX idx_user_id (user_id)
);

-- 3. 샘플 데이터 삽입
INSERT INTO licenses (license_key, type, start_date, end_date, status, max_devices, max_activations, description) VALUES
('25A1234-5678', 'BASIC', '2024-01-15', '2024-02-14', 'ACTIVE', 1, 1, 'Basic License - 30 days'),
('25B9876-5432', 'PREMIUM', '2024-01-10', '2024-04-10', 'USED', 1, 1, 'Premium License - 90 days'),
('25C5555-9999', 'BASIC', '2024-01-01', '2024-12-31', 'ACTIVE', 1, 1, 'Offline Test License');

INSERT INTO license_usages (license_key, device_id, app_version, platform, activated_at, status, token) VALUES
('25B9876-5432', 'device123', '1.0.0', 'VR', '2024-01-15 10:30:00', 'ACTIVE', 'TOKEN_25B9876-5432_1705123456789');

INSERT INTO users (username, password, email, role) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', 'admin@example.com', 'ADMIN');
```

### 스키마 버전 관리

```sql
-- 스키마 버전 테이블
CREATE TABLE schema_version (
    version INT PRIMARY KEY,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description VARCHAR(255)
);

-- 초기 버전 기록
INSERT INTO schema_version (version, description) VALUES (1, 'Initial schema creation');
```

## 📊 성능 최적화

### 쿼리 최적화

```sql
-- 자주 사용되는 쿼리들
-- 1. 활성 라이센스 조회
SELECT * FROM licenses 
WHERE status = 'ACTIVE' 
AND end_date >= CURDATE() 
ORDER BY created_at DESC;

-- 2. 라이센스 사용 통계
SELECT 
    l.type,
    COUNT(*) as total_licenses,
    SUM(CASE WHEN l.status = 'ACTIVE' THEN 1 ELSE 0 END) as active_licenses,
    SUM(CASE WHEN l.status = 'USED' THEN 1 ELSE 0 END) as used_licenses,
    SUM(CASE WHEN l.status = 'EXPIRED' THEN 1 ELSE 0 END) as expired_licenses
FROM licenses l
GROUP BY l.type;

-- 3. 디바이스별 라이센스 사용 현황
SELECT 
    lu.device_id,
    l.license_key,
    l.type,
    lu.activated_at,
    lu.status
FROM license_usages lu
JOIN licenses l ON lu.license_key = l.license_key
WHERE lu.status = 'ACTIVE'
ORDER BY lu.activated_at DESC;
```

### 파티셔닝 (대용량 데이터)

```sql
-- 라이센스 테이블을 년도별로 파티셔닝
ALTER TABLE licenses PARTITION BY RANGE (YEAR(created_at)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);

-- 사용 기록 테이블을 월별로 파티셔닝
ALTER TABLE license_usages PARTITION BY RANGE (YEAR(activated_at) * 100 + MONTH(activated_at)) (
    PARTITION p202401 VALUES LESS THAN (202402),
    PARTITION p202402 VALUES LESS THAN (202403),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

## 🔧 유지보수

### 정기적인 정리 작업

```sql
-- 만료된 라이센스 상태 업데이트
UPDATE licenses 
SET status = 'EXPIRED' 
WHERE status = 'ACTIVE' 
AND end_date < CURDATE();

-- 오래된 감사 로그 삭제 (1년 이상)
DELETE FROM audit_logs 
WHERE created_at < DATE_SUB(NOW(), INTERVAL 1 YEAR);

-- 사용되지 않는 토큰 정리
UPDATE license_usages 
SET status = 'INACTIVE' 
WHERE status = 'ACTIVE' 
AND last_used_at < DATE_SUB(NOW(), INTERVAL 30 DAY);
```

### 백업 및 복구

```bash
# 백업 스크립트
#!/bin/bash
DATE=$(date +%Y%m%d_%H%M%S)
mysqldump -u root -p vr_license > backup_vr_license_$DATE.sql

# 복구 스크립트
mysql -u root -p vr_license < backup_vr_license_20240115_143000.sql
```

---

**참고**: 이 스키마는 기본적인 구조를 제공합니다. 실제 구현 시에는 비즈니스 요구사항에 맞게 추가적인 테이블이나 컬럼이 필요할 수 있습니다. 