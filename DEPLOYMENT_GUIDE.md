# VR 라이센스 관리 시스템 - 배포 가이드

## 📋 목차

1. [개요](#개요)
2. [환경 요구사항](#환경-요구사항)
3. [개발 환경 설정](#개발-환경-설정)
4. [프로덕션 환경 설정](#프로덕션-환경-설정)
5. [Docker 배포](#docker-배포)
6. [클라우드 배포](#클라우드-배포)
7. [모니터링 및 로깅](#모니터링-및-로깅)
8. [백업 및 복구](#백업-및-복구)
9. [보안 설정](#보안-설정)
10. [문제 해결](#문제-해결)

## 🎯 개요

VR 라이센스 관리 시스템의 배포 가이드입니다. 개발 환경부터 프로덕션 환경까지 단계별로 설정하는 방법을 설명합니다.

## 🖥️ 환경 요구사항

### 최소 요구사항
- **Java**: 17 이상
- **메모리**: 2GB RAM
- **저장공간**: 10GB
- **네트워크**: 인터넷 연결

### 권장 요구사항
- **Java**: 17 LTS
- **메모리**: 4GB RAM
- **저장공간**: 50GB SSD
- **CPU**: 2코어 이상
- **네트워크**: 고속 인터넷 연결

### 데이터베이스 요구사항
- **MySQL**: 8.0 이상 (권장)
- **PostgreSQL**: 13 이상 (대안)
- **메모리**: 1GB 이상
- **저장공간**: 20GB 이상

## 🛠️ 개발 환경 설정

### 1. 로컬 개발 환경

#### 필수 소프트웨어 설치
```bash
# Java 17 설치 (Ubuntu/Debian)
sudo apt update
sudo apt install openjdk-17-jdk

# Java 17 설치 (CentOS/RHEL)
sudo yum install java-17-openjdk-devel

# Java 17 설치 (macOS)
brew install openjdk@17

# Gradle 설치
sudo apt install gradle  # Ubuntu/Debian
brew install gradle      # macOS
```

#### 프로젝트 설정
```bash
# 프로젝트 클론
git clone <repository-url>
cd vr_license

# 의존성 설치
./gradlew build

# 개발 서버 실행
./gradlew bootRun
```

#### 개발 환경 설정 파일
```yaml
# application-dev.yml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true

server:
  port: 8080

logging:
  level:
    vr.license: DEBUG
    org.springframework.web: INFO
```

### 2. IDE 설정

#### IntelliJ IDEA 설정
1. **프로젝트 열기**: File → Open → vr_license 폴더 선택
2. **JDK 설정**: File → Project Structure → Project SDK → 17 선택
3. **Gradle 설정**: File → Settings → Build Tools → Gradle → Gradle JVM → 17 선택
4. **실행 설정**: Run → Edit Configurations → + → Spring Boot → Main class: vr.license.LicenseApplication

#### VS Code 설정
```json
// .vscode/settings.json
{
    "java.configuration.runtimes": [
        {
            "name": "JavaSE-17",
            "path": "/usr/lib/jvm/java-17-openjdk",
            "default": true
        }
    ],
    "java.compile.nullAnalysis.mode": "automatic"
}
```

## 🚀 프로덕션 환경 설정

### 1. 서버 준비

#### Ubuntu 20.04 LTS 기준
```bash
# 시스템 업데이트
sudo apt update && sudo apt upgrade -y

# Java 17 설치
sudo apt install openjdk-17-jdk -y

# MySQL 8.0 설치
sudo apt install mysql-server -y

# Nginx 설치 (로드 밸런서용)
sudo apt install nginx -y

# 방화벽 설정
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw enable
```

### 2. 데이터베이스 설정

#### MySQL 설정
```bash
# MySQL 보안 설정
sudo mysql_secure_installation

# 데이터베이스 생성
sudo mysql -u root -p
```

```sql
-- MySQL에서 실행
CREATE DATABASE vr_license CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'vr_license_user'@'localhost' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON vr_license.* TO 'vr_license_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

#### 데이터베이스 스키마 생성
```bash
# 스키마 생성 스크립트 실행
mysql -u vr_license_user -p vr_license < database_schema.sql
```

### 3. 애플리케이션 설정

#### 프로덕션 설정 파일
```yaml
# application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://localhost:3306/vr_license?useSSL=false&serverTimezone=UTC
    username: ${DB_USERNAME:vr_license_user}
    password: ${DB_PASSWORD:secure_password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true

server:
  port: 8080
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
    key-store-type: PKCS12

logging:
  level:
    vr.license: INFO
    org.springframework.web: WARN
  file:
    name: logs/vr-license.log
    max-size: 100MB
    max-history: 30

jwt:
  secret: ${JWT_SECRET:your_jwt_secret_key_here}
  expiration: 86400000 # 24 hours

# 보안 설정
security:
  bcrypt:
    strength: 12
  session:
    timeout: 3600 # 1 hour
```

#### 환경 변수 설정
```bash
# /etc/environment에 추가
export DB_USERNAME=vr_license_user
export DB_PASSWORD=secure_password
export JWT_SECRET=your_super_secret_jwt_key_here
export SSL_PASSWORD=your_ssl_password_here
```

### 4. SSL 인증서 설정

#### Let's Encrypt 인증서 발급
```bash
# Certbot 설치
sudo apt install certbot python3-certbot-nginx -y

# 인증서 발급
sudo certbot --nginx -d your-domain.com

# 자동 갱신 설정
sudo crontab -e
# 다음 줄 추가: 0 12 * * * /usr/bin/certbot renew --quiet
```

## 🐳 Docker 배포

### 1. Dockerfile

```dockerfile
# Dockerfile
FROM openjdk:17-jdk-slim

# 메타데이터
LABEL maintainer="your-email@example.com"
LABEL version="1.0.0"
LABEL description="VR License Management System"

# 작업 디렉토리 설정
WORKDIR /app

# 애플리케이션 JAR 파일 복사
COPY build/libs/vr-license-0.0.1-SNAPSHOT.jar app.jar

# 포트 노출
EXPOSE 8080

# 헬스체크
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# JVM 옵션 설정
ENV JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC -XX:+UseContainerSupport"

# 애플리케이션 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 2. Docker Compose

```yaml
# docker-compose.yml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=db
      - DB_USERNAME=vr_license_user
      - DB_PASSWORD=secure_password
      - JWT_SECRET=your_jwt_secret_key
    depends_on:
      - db
    volumes:
      - ./logs:/app/logs
      - ./ssl:/app/ssl
    restart: unless-stopped
    networks:
      - vr-license-network

  db:
    image: mysql:8.0
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: vr_license
      MYSQL_USER: vr_license_user
      MYSQL_PASSWORD: secure_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./database_schema.sql:/docker-entrypoint-initdb.d/init.sql
    restart: unless-stopped
    networks:
      - vr-license-network

  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
    restart: unless-stopped
    networks:
      - vr-license-network

volumes:
  mysql_data:

networks:
  vr-license-network:
    driver: bridge
```

### 3. Nginx 설정

```nginx
# nginx.conf
events {
    worker_connections 1024;
}

http {
    upstream app_servers {
        server app:8080;
    }

    server {
        listen 80;
        server_name your-domain.com;
        return 301 https://$server_name$request_uri;
    }

    server {
        listen 443 ssl http2;
        server_name your-domain.com;

        ssl_certificate /etc/nginx/ssl/cert.pem;
        ssl_certificate_key /etc/nginx/ssl/key.pem;
        ssl_protocols TLSv1.2 TLSv1.3;
        ssl_ciphers ECDHE-RSA-AES256-GCM-SHA512:DHE-RSA-AES256-GCM-SHA512:ECDHE-RSA-AES256-GCM-SHA384:DHE-RSA-AES256-GCM-SHA384;
        ssl_prefer_server_ciphers off;

        location / {
            proxy_pass http://app_servers;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }
    }
}
```

### 4. Docker 배포 명령어

```bash
# 이미지 빌드
docker build -t vr-license:latest .

# 컨테이너 실행
docker-compose up -d

# 로그 확인
docker-compose logs -f app

# 컨테이너 중지
docker-compose down

# 데이터베이스 백업
docker exec vr-license_db_1 mysqldump -u root -p vr_license > backup.sql
```

## ☁️ 클라우드 배포

### 1. AWS 배포

#### EC2 인스턴스 설정
```bash
# Amazon Linux 2 기준
sudo yum update -y
sudo yum install java-17-amazon-corretto -y
sudo yum install mysql -y
sudo yum install nginx -y

# 보안 그룹 설정
# - SSH (22)
# - HTTP (80)
# - HTTPS (443)
# - 애플리케이션 (8080)
```

#### RDS MySQL 설정
```sql
-- RDS에서 실행
CREATE DATABASE vr_license CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'vr_license_user'@'%' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON vr_license.* TO 'vr_license_user'@'%';
FLUSH PRIVILEGES;
```

#### AWS 배포 스크립트
```bash
#!/bin/bash
# deploy.sh

# 애플리케이션 다운로드
wget https://github.com/your-repo/vr-license/releases/latest/download/vr-license.jar

# 환경 변수 설정
export DB_HOST=your-rds-endpoint.amazonaws.com
export DB_USERNAME=vr_license_user
export DB_PASSWORD=secure_password
export JWT_SECRET=your_jwt_secret

# 애플리케이션 실행
nohup java -jar vr-license.jar --spring.profiles.active=prod > app.log 2>&1 &
```

### 2. Google Cloud Platform 배포

#### Cloud Run 배포
```yaml
# cloudbuild.yaml
steps:
  - name: 'gcr.io/cloud-builders/docker'
    args: ['build', '-t', 'gcr.io/$PROJECT_ID/vr-license', '.']
  - name: 'gcr.io/cloud-builders/docker'
    args: ['push', 'gcr.io/$PROJECT_ID/vr-license']
  - name: 'gcr.io/cloud-builders/gcloud'
    args:
      - 'run'
      - 'deploy'
      - 'vr-license'
      - '--image'
      - 'gcr.io/$PROJECT_ID/vr-license'
      - '--region'
      - 'us-central1'
      - '--platform'
      - 'managed'
      - '--allow-unauthenticated'
```

### 3. Azure 배포

#### Azure App Service 배포
```bash
# Azure CLI 설치 후
az login
az group create --name vr-license-rg --location eastus
az appservice plan create --name vr-license-plan --resource-group vr-license-rg --sku B1
az webapp create --name vr-license-app --resource-group vr-license-rg --plan vr-license-plan --runtime "JAVA|17-java17"
az webapp config appsettings set --name vr-license-app --resource-group vr-license-rg --settings DB_HOST=your-azure-sql-server.database.windows.net
```

## 📊 모니터링 및 로깅

### 1. Spring Boot Actuator 설정

```yaml
# application.yml에 추가
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
```

### 2. Prometheus 설정

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'vr-license'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
```

### 3. Grafana 대시보드

```json
{
  "dashboard": {
    "title": "VR License System Dashboard",
    "panels": [
      {
        "title": "License Verifications",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(license_verification_total[5m])",
            "legendFormat": "Verifications/sec"
          }
        ]
      },
      {
        "title": "Active Licenses",
        "type": "stat",
        "targets": [
          {
            "expr": "active_licenses_total",
            "legendFormat": "Active Licenses"
          }
        ]
      }
    ]
  }
}
```

### 4. 로그 설정

```yaml
# logback-spring.xml
<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/vr-license.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/vr-license.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE"/>
    </root>
</configuration>
```

## 💾 백업 및 복구

### 1. 데이터베이스 백업

```bash
#!/bin/bash
# backup.sh

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backup"
DB_NAME="vr_license"
DB_USER="vr_license_user"
DB_PASS="secure_password"

# 백업 생성
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# 압축
gzip $BACKUP_DIR/backup_$DATE.sql

# 30일 이상 된 백업 삭제
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +30 -delete

echo "Backup completed: backup_$DATE.sql.gz"
```

### 2. 자동 백업 스케줄

```bash
# crontab 설정
crontab -e

# 매일 새벽 2시에 백업 실행
0 2 * * * /path/to/backup.sh

# 매주 일요일 새벽 3시에 전체 백업
0 3 * * 0 /path/to/full_backup.sh
```

### 3. 복구 스크립트

```bash
#!/bin/bash
# restore.sh

BACKUP_FILE=$1
DB_NAME="vr_license"
DB_USER="vr_license_user"
DB_PASS="secure_password"

if [ -z "$BACKUP_FILE" ]; then
    echo "Usage: $0 <backup_file>"
    exit 1
fi

# 압축 해제
gunzip -c $BACKUP_FILE > temp_restore.sql

# 복구 실행
mysql -u $DB_USER -p$DB_PASS $DB_NAME < temp_restore.sql

# 임시 파일 삭제
rm temp_restore.sql

echo "Restore completed from $BACKUP_FILE"
```

## 🔒 보안 설정

### 1. 방화벽 설정

```bash
# UFW 설정 (Ubuntu)
sudo ufw default deny incoming
sudo ufw default allow outgoing
sudo ufw allow ssh
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 8080/tcp
sudo ufw enable
```

### 2. SSL/TLS 설정

```yaml
# application.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}
    key-store-type: PKCS12
    key-alias: vr-license
  http2:
    enabled: true
```

### 3. 보안 헤더 설정

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers()
                .frameOptions().deny()
                .contentTypeOptions()
                .and()
                .httpStrictTransportSecurity()
                .and()
                .contentSecurityPolicy("default-src 'self'")
                .and()
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/license/verify").permitAll()
                .anyRequest().authenticated();
        
        return http.build();
    }
}
```

## 🔧 문제 해결

### 1. 일반적인 문제들

#### 애플리케이션 시작 실패
```bash
# 로그 확인
tail -f logs/vr-license.log

# 포트 확인
netstat -tulpn | grep 8080

# Java 프로세스 확인
ps aux | grep java
```

#### 데이터베이스 연결 실패
```bash
# MySQL 상태 확인
sudo systemctl status mysql

# 연결 테스트
mysql -u vr_license_user -p -h localhost vr_license

# 포트 확인
netstat -tulpn | grep 3306
```

#### 메모리 부족
```bash
# 메모리 사용량 확인
free -h

# JVM 힙 크기 조정
java -Xmx4g -Xms2g -jar app.jar
```

### 2. 로그 분석

#### 에러 로그 검색
```bash
# 에러 로그만 확인
grep "ERROR" logs/vr-license.log

# 특정 시간대 로그 확인
grep "2024-01-15 10:" logs/vr-license.log

# 라이센스 인증 로그 확인
grep "License verification" logs/vr-license.log
```

#### 성능 모니터링
```bash
# CPU 사용량
top -p $(pgrep java)

# 메모리 사용량
jstat -gc $(pgrep java)

# 스레드 덤프
jstack $(pgrep java) > thread_dump.txt
```

### 3. 복구 절차

#### 애플리케이션 재시작
```bash
# 프로세스 종료
pkill -f vr-license

# 애플리케이션 재시작
nohup java -jar vr-license.jar --spring.profiles.active=prod > app.log 2>&1 &
```

#### 데이터베이스 복구
```bash
# MySQL 재시작
sudo systemctl restart mysql

# 데이터베이스 복구
mysql -u root -p < backup.sql
```

---

**참고**: 이 배포 가이드는 기본적인 설정을 제공합니다. 실제 운영 환경에서는 보안, 성능, 확장성 등을 고려하여 추가적인 설정이 필요할 수 있습니다. 