# VR 라이센스 관리 시스템 - 백엔드 API 문서

## 📋 목차

1. [시스템 개요](#시스템-개요)
2. [기술 스택](#기술-스택)
3. [프로젝트 구조](#프로젝트-구조)
4. [데이터 모델](#데이터-모델)
5. [API 명세](#api-명세)
6. [인증 및 보안](#인증-및-보안)
7. [라이센스 키 형식](#라이센스-키-형식)
8. [에러 처리](#에러-처리)
9. [개발 가이드](#개발-가이드)
10. [테스트 가이드](#테스트-가이드)

## 🎯 시스템 개요

VR 앱을 위한 라이센스 관리 및 인증 시스템입니다. 웹에서 라이센스를 생성하고 관리하며, VR 앱에서는 온라인 인증 후 오프라인에서 토큰 기반으로 라이센스를 체크할 수 있습니다.

### 주요 기능
- **라이센스 생성 및 관리**: 웹 인터페이스를 통한 라이센스 생성, 조회, 취소
- **VR 앱 인증**: VR 앱에서 라이센스 키를 통한 온라인 인증
- **오프라인 토큰**: 인증 후 로컬 토큰으로 오프라인 체크
- **중복 사용 방지**: 한 번 사용된 라이센스는 재사용 불가
- **기간 체크**: 라이센스 만료일 자동 체크

## 🛠 기술 스택

- **Framework**: Spring Boot 3.3.13
- **Language**: Java 17
- **Build Tool**: Gradle
- **Template Engine**: Thymeleaf
- **Database**: 인메모리 (더미 데이터) - 실제 구현 시 MySQL/PostgreSQL 권장
- **Authentication**: Session-based (웹 관리자용)
- **API**: RESTful API (VR 앱용)

## 📁 프로젝트 구조

```
src/main/java/vr/license/
├── controller/
│   ├── AuthController.java      # 인증 컨트롤러 (로그인/로그아웃)
│   ├── HomeController.java      # 홈 페이지 컨트롤러
│   └── LicenseController.java   # 라이센스 관리 컨트롤러
├── dto/
│   ├── LicenseRequest.java      # 라이센스 생성 요청 DTO
│   └── VerifyRequest.java       # 라이센스 인증 요청 DTO
├── model/
│   ├── License.java             # 라이센스 엔티티
│   └── LicenseUsage.java        # 라이센스 사용 기록 엔티티
└── LicenseApplication.java      # 메인 애플리케이션

src/main/resources/
├── templates/                   # Thymeleaf 템플릿
│   ├── home.html               # 메인 페이지
│   ├── auth/
│   │   └── login.html          # 로그인 페이지
│   └── license/
│       ├── create.html         # 라이센스 생성 페이지
│       ├── manage.html         # 라이센스 관리 페이지
│       └── simulator.html      # VR 앱 시뮬레이터
└── application.properties      # 애플리케이션 설정
```

## 📊 데이터 모델

### License 엔티티

```java
public class License {
    private Long id;                    // 고유 ID
    private String licenseKey;          // 라이센스 키 (25A1234-5678 형식)
    private String type;                // 라이센스 유형 (BASIC, PREMIUM, ENTERPRISE)
    private LocalDate startDate;        // 시작일
    private LocalDate endDate;          // 만료일
    private String status;              // 상태 (ACTIVE, USED, EXPIRED)
    private Integer maxDevices;         // 최대 디바이스 수
    private Integer maxActivations;     // 최대 활성화 수
    private String description;         // 설명
    private LocalDateTime createdAt;    // 생성일
    private LocalDateTime updatedAt;    // 수정일
}
```

### LicenseUsage 엔티티

```java
public class LicenseUsage {
    private Long id;                    // 고유 ID
    private String licenseKey;          // 라이센스 키
    private String deviceId;            // 디바이스 ID
    private String appVersion;          // 앱 버전
    private String platform;            // 플랫폼 (VR, Mobile 등)
    private LocalDateTime activatedAt;  // 활성화 시간
    private LocalDateTime lastUsedAt;   // 마지막 사용 시간
    private String status;              // 상태 (ACTIVE, INACTIVE)
    private String token;               // 인증 토큰
}
```

### DTO 클래스들

#### LicenseRequest (라이센스 생성 요청)
```java
public class LicenseRequest {
    private String licenseKey;          // 라이센스 키 (선택사항, 자동생성 가능)
    private String licenseType;         // 라이센스 유형
    private String startDate;           // 시작일 (YYYY-MM-DD)
    private String endDate;             // 만료일 (YYYY-MM-DD)
    private String description;         // 설명
    private Integer maxDevices;         // 최대 디바이스 수
    private Integer maxActivations;     // 최대 활성화 수
}
```

#### VerifyRequest (라이센스 인증 요청)
```java
public class VerifyRequest {
    private String licenseKey;          // 라이센스 키
    private String deviceId;            // 디바이스 ID
    private String appVersion;          // 앱 버전
    private String platform;            // 플랫폼
}
```

## 🔌 API 명세

### 1. 라이센스 인증 API (VR 앱용)

**POST** `/license/verify`

VR 앱에서 라이센스를 인증할 때 사용하는 API입니다. 로그인이 필요하지 않습니다.

#### 요청 헤더
```
Content-Type: application/json
```

#### 요청 본문
```json
{
  "licenseKey": "25A1234-5678",
  "deviceId": "device123",
  "appVersion": "1.0.0",
  "platform": "VR"
}
```

#### 응답 (성공 - 200 OK)
```json
{
  "success": true,
  "message": "라이센스 인증이 성공했습니다.",
  "license": {
    "licenseKey": "25A1234-5678",
    "type": "BASIC",
    "startDate": "2024-01-15",
    "endDate": "2024-02-14",
    "maxDevices": 1,
    "maxActivations": 1
  },
  "token": "TOKEN_25A1234-5678_1705123456789"
}
```

#### 응답 (실패 - 400 Bad Request)
```json
{
  "success": false,
  "message": "유효하지 않은 라이센스 키입니다.",
  "code": "INVALID_LICENSE"
}
```

#### 에러 코드
- `INVALID_LICENSE`: 유효하지 않은 라이센스 키
- `EXPIRED_LICENSE`: 만료된 라이센스
- `USED_LICENSE`: 이미 사용된 라이센스

### 2. 라이센스 생성 API (관리자용)

**POST** `/license/create`

관리자가 새로운 라이센스를 생성할 때 사용하는 API입니다. 로그인이 필요합니다.

#### 요청 헤더
```
Content-Type: application/json
```

#### 요청 본문
```json
{
  "licenseKey": "25A1234-5678",
  "licenseType": "BASIC",
  "startDate": "2024-01-15",
  "endDate": "2024-02-14",
  "description": "Basic License - 30 days",
  "maxDevices": 1,
  "maxActivations": 1
}
```

#### 응답 (성공 - 200 OK)
```json
{
  "success": true,
  "message": "License created successfully.",
  "license": {
    "id": 1,
    "licenseKey": "25A1234-5678",
    "type": "BASIC",
    "startDate": "2024-01-15",
    "endDate": "2024-02-14",
    "status": "ACTIVE",
    "maxDevices": 1,
    "maxActivations": 1,
    "description": "Basic License - 30 days",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

#### 응답 (실패 - 401 Unauthorized)
```json
{
  "success": false,
  "message": "Authentication required."
}
```

### 3. 라이센스 목록 조회 API (관리자용)

**GET** `/license/list`

생성된 모든 라이센스를 조회하는 API입니다. 로그인이 필요합니다.

#### 응답 (성공 - 200 OK)
```json
{
  "success": true,
  "licenses": [
    {
      "id": 1,
      "licenseKey": "25A1234-5678",
      "type": "BASIC",
      "startDate": "2024-01-15",
      "endDate": "2024-02-14",
      "status": "ACTIVE",
      "maxDevices": 1,
      "maxActivations": 1,
      "description": "Basic License - 30 days",
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

### 4. 라이센스 상세 조회 API (관리자용)

**GET** `/license/{licenseKey}`

특정 라이센스의 상세 정보를 조회하는 API입니다. 로그인이 필요합니다.

#### 응답 (성공 - 200 OK)
```json
{
  "success": true,
  "license": {
    "id": 1,
    "licenseKey": "25A1234-5678",
    "type": "BASIC",
    "startDate": "2024-01-15",
    "endDate": "2024-02-14",
    "status": "USED",
    "maxDevices": 1,
    "maxActivations": 1,
    "description": "Basic License - 30 days",
    "createdAt": "2024-01-15T10:30:00"
  },
  "usage": {
    "id": 1,
    "licenseKey": "25A1234-5678",
    "deviceId": "device123",
    "appVersion": "1.0.0",
    "platform": "VR",
    "activatedAt": "2024-01-15T10:30:00",
    "status": "ACTIVE",
    "token": "TOKEN_25A1234-5678_1705123456789"
  }
}
```

### 5. 라이센스 취소 API (관리자용)

**POST** `/license/{licenseKey}/revoke`

라이센스를 취소하는 API입니다. 로그인이 필요합니다.

#### 응답 (성공 - 200 OK)
```json
{
  "success": true,
  "message": "License revoked successfully."
}
```

## 🔐 인증 및 보안

### 웹 관리자 인증
- **방식**: Session-based 인증
- **로그인**: `/login` (POST)
- **로그아웃**: `/logout` (GET)
- **기본 계정**: admin / admin123

### VR 앱 인증
- **방식**: API 키 기반 (라이센스 키)
- **인증**: `/license/verify` (POST)
- **토큰**: 인증 성공 시 로컬 토큰 생성

### 보안 고려사항
1. **HTTPS 사용**: 프로덕션 환경에서는 반드시 HTTPS 사용
2. **API Rate Limiting**: 과도한 요청 방지
3. **입력 검증**: 모든 입력값 검증 및 sanitization
4. **토큰 보안**: JWT 또는 암호화된 토큰 사용 권장

## 🔑 라이센스 키 형식

### 형식: `YYALLLL-MMMM`
- **YY**: 현재 년도의 마지막 2자리 (예: 25)
- **A**: 랜덤 대문자 (A-Z)
- **LLLL**: 랜덤 4자리 숫자 (1000-9999)
- **MMMM**: 랜덤 4자리 숫자 (1000-9999)

### 예시
- `25A1234-5678`
- `25B9876-5432`
- `25C5555-9999`

### 생성 로직
```java
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
    
    // Format: YYALLLL-MMMM
    return String.format("%02d%c%04d-%04d", currentYear, randomLetter, firstPart, secondPart);
}
```

## ⚠️ 에러 처리

### HTTP 상태 코드
- **200 OK**: 성공
- **400 Bad Request**: 잘못된 요청 (유효하지 않은 라이센스, 만료된 라이센스 등)
- **401 Unauthorized**: 인증 필요
- **404 Not Found**: 리소스를 찾을 수 없음
- **500 Internal Server Error**: 서버 내부 오류

### 에러 응답 형식
```json
{
  "success": false,
  "message": "에러 메시지",
  "code": "ERROR_CODE"
}
```

### 주요 에러 코드
- `INVALID_LICENSE`: 유효하지 않은 라이센스 키
- `EXPIRED_LICENSE`: 만료된 라이센스
- `USED_LICENSE`: 이미 사용된 라이센스
- `AUTHENTICATION_REQUIRED`: 인증 필요
- `LICENSE_NOT_FOUND`: 라이센스를 찾을 수 없음
- `DUPLICATE_LICENSE`: 중복된 라이센스 키

## 🚀 개발 가이드

### 1. 개발 환경 설정

#### 필수 요구사항
- Java 17 이상
- Gradle 7.0 이상
- IDE (IntelliJ IDEA, Eclipse, VS Code 등)

#### 프로젝트 클론 및 실행
```bash
# 프로젝트 클론
git clone <repository-url>
cd vr_license

# 의존성 설치
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

#### 접속 정보
- **웹 관리자**: http://localhost:8080
- **API 엔드포인트**: http://localhost:8080/license/verify
- **기본 계정**: admin / admin123

### 2. 데이터베이스 연동

현재는 인메모리 더미 데이터를 사용하지만, 실제 구현 시에는 데이터베이스 연동이 필요합니다.

#### MySQL 연동 예시
```yaml
# application.yml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vr_license
    username: root
    password: password
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

#### JPA 엔티티 예시
```java
@Entity
@Table(name = "licenses")
public class License {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String licenseKey;
    
    @Enumerated(EnumType.STRING)
    private LicenseType type;
    
    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;
    
    @Enumerated(EnumType.STRING)
    private LicenseStatus status;
    
    // ... getters and setters
}
```

### 3. 보안 강화

#### JWT 토큰 구현
```java
@Component
public class JwtTokenProvider {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private long jwtExpiration;
    
    public String generateToken(License license, LicenseUsage usage) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setSubject(license.getLicenseKey())
                .claim("deviceId", usage.getDeviceId())
                .claim("licenseType", license.getType())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

#### API Rate Limiting
```java
@Configuration
public class RateLimitConfig {
    
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }
}
```

### 4. 로깅 및 모니터링

#### 로깅 설정
```yaml
# application.yml
logging:
  level:
    vr.license: DEBUG
    org.springframework.web: INFO
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

#### API 모니터링
```java
@RestControllerAdvice
public class ApiLoggingAdvice {
    
    private static final Logger logger = LoggerFactory.getLogger(ApiLoggingAdvice.class);
    
    @Before("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void logApiCall(JoinPoint joinPoint) {
        logger.info("API Call: {} - {}", 
                   joinPoint.getSignature().getName(),
                   joinPoint.getArgs());
    }
}
```

## 🧪 테스트 가이드

### 1. 단위 테스트

#### LicenseController 테스트
```java
@SpringBootTest
@AutoConfigureTestDatabase
class LicenseControllerTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testVerifyLicense_Success() {
        // Given
        VerifyRequest request = new VerifyRequest();
        request.setLicenseKey("25A1234-5678");
        request.setDeviceId("test-device");
        request.setAppVersion("1.0.0");
        request.setPlatform("VR");
        
        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/license/verify", request, Map.class);
        
        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue((Boolean) response.getBody().get("success"));
    }
    
    @Test
    void testVerifyLicense_InvalidKey() {
        // Given
        VerifyRequest request = new VerifyRequest();
        request.setLicenseKey("INVALID-KEY");
        request.setDeviceId("test-device");
        
        // When
        ResponseEntity<Map> response = restTemplate.postForEntity(
            "/license/verify", request, Map.class);
        
        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse((Boolean) response.getBody().get("success"));
    }
}
```

### 2. 통합 테스트

#### API 통합 테스트
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LicenseApiIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void testCompleteLicenseFlow() {
        // 1. 라이센스 생성
        LicenseRequest createRequest = new LicenseRequest();
        createRequest.setLicenseType("BASIC");
        createRequest.setStartDate("2024-01-15");
        createRequest.setEndDate("2024-02-14");
        createRequest.setDescription("Test License");
        createRequest.setMaxDevices(1);
        createRequest.setMaxActivations(1);
        
        ResponseEntity<Map> createResponse = restTemplate.postForEntity(
            "/license/create", createRequest, Map.class);
        
        assertEquals(HttpStatus.OK, createResponse.getStatusCode());
        
        // 2. 라이센스 인증
        String licenseKey = (String) createResponse.getBody().get("licenseKey");
        
        VerifyRequest verifyRequest = new VerifyRequest();
        verifyRequest.setLicenseKey(licenseKey);
        verifyRequest.setDeviceId("test-device");
        verifyRequest.setAppVersion("1.0.0");
        verifyRequest.setPlatform("VR");
        
        ResponseEntity<Map> verifyResponse = restTemplate.postForEntity(
            "/license/verify", verifyRequest, Map.class);
        
        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());
        assertTrue((Boolean) verifyResponse.getBody().get("success"));
    }
}
```

### 3. 성능 테스트

#### JMeter 테스트 계획
```xml
<?xml version="1.0" encoding="UTF-8"?>
<jmeterTestPlan version="1.2" properties="5.0">
  <hashTree>
    <TestPlan guiclass="TestPlanGui" testclass="TestPlan" testname="License API Test">
      <elementProp name="TestPlan.arguments" elementType="Arguments">
        <collectionProp name="Arguments.arguments"/>
      </elementProp>
      <boolProp name="TestPlan.functional_mode">false</boolProp>
      <stringProp name="TestPlan.comments"></stringProp>
      <boolProp name="TestPlan.tearDown_on_shutdown">true</boolProp>
      <boolProp name="TestPlan.serialize_threadgroups">false</boolProp>
    </TestPlan>
    <hashTree>
      <ThreadGroup guiclass="ThreadGroupGui" testclass="ThreadGroup" testname="License Verification">
        <elementProp name="ThreadGroup.main_controller" elementType="LoopController">
          <boolProp name="LoopController.continue_forever">false</boolProp>
          <stringProp name="LoopController.loops">100</stringProp>
        </elementProp>
        <stringProp name="ThreadGroup.num_threads">10</stringProp>
        <stringProp name="ThreadGroup.ramp_time">10</stringProp>
        <boolProp name="ThreadGroup.scheduler">false</boolProp>
        <stringProp name="ThreadGroup.duration"></stringProp>
        <stringProp name="ThreadGroup.delay"></stringProp>
      </ThreadGroup>
      <hashTree>
        <HTTPSamplerProxy guiclass="HttpTestSampleGui" testclass="HTTPSamplerProxy">
          <elementProp name="HTTPsampler.Arguments" elementType="Arguments">
            <collectionProp name="Arguments.arguments">
              <elementProp name="" elementType="HTTPArgument">
                <boolProp name="HTTPArgument.always_encode">false</boolProp>
                <stringProp name="Argument.value">{"licenseKey":"25A1234-5678","deviceId":"test-device","appVersion":"1.0.0","platform":"VR"}</stringProp>
                <stringProp name="Argument.metadata">=</stringProp>
              </elementProp>
            </collectionProp>
          </elementProp>
          <stringProp name="HTTPSampler.domain">localhost</stringProp>
          <stringProp name="HTTPSampler.port">8080</stringProp>
          <stringProp name="HTTPSampler.protocol">http</stringProp>
          <stringProp name="HTTPSampler.path">/license/verify</stringProp>
          <stringProp name="HTTPSampler.method">POST</stringProp>
          <boolProp name="HTTPSampler.follow_redirects">true</boolProp>
          <boolProp name="HTTPSampler.auto_redirects">false</boolProp>
          <boolProp name="HTTPSampler.use_keepalive">true</boolProp>
          <boolProp name="HTTPSampler.DO_MULTIPART_POST">false</boolProp>
          <stringProp name="HTTPSampler.contentEncoding">UTF-8</stringProp>
          <stringProp name="HTTPSampler.path">/license/verify</stringProp>
        </HTTPSamplerProxy>
      </hashTree>
    </hashTree>
  </hashTree>
</jmeterTestPlan>
```

## 📝 배포 가이드

### 1. 프로덕션 환경 설정

#### application-prod.yml
```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:mysql://prod-db:3306/vr_license
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

server:
  port: 8080
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_PASSWORD}

logging:
  level:
    vr.license: INFO
  file:
    name: logs/vr-license.log
    max-size: 100MB
    max-history: 30

jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000 # 24 hours
```

### 2. Docker 배포

#### Dockerfile
```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY build/libs/vr-license-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

#### docker-compose.yml
```yaml
version: '3.8'

services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_USERNAME=vr_license_user
      - DB_PASSWORD=secure_password
      - JWT_SECRET=your_jwt_secret_key
    depends_on:
      - db
    volumes:
      - ./logs:/app/logs

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

volumes:
  mysql_data:
```

### 3. 모니터링 및 로깅

#### Prometheus 메트릭
```java
@Configuration
public class MetricsConfig {
    
    @Bean
    MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
    
    @EventListener
    public void handleLicenseVerification(LicenseVerificationEvent event) {
        Counter.builder("license.verification.total")
               .tag("result", event.isSuccess() ? "success" : "failure")
               .register(meterRegistry())
               .increment();
    }
}
```

## 🔧 문제 해결

### 1. 일반적인 문제들

#### 라이센스 인증 실패
- **원인**: 잘못된 라이센스 키 형식
- **해결**: 키 형식이 `25A1234-5678` 형식인지 확인

#### 데이터베이스 연결 오류
- **원인**: 데이터베이스 설정 오류
- **해결**: application.yml의 데이터베이스 설정 확인

#### 토큰 검증 실패
- **원인**: JWT 시크릿 키 불일치
- **해결**: JWT_SECRET 환경변수 확인

### 2. 로그 분석

#### 로그 레벨 설정
```yaml
logging:
  level:
    vr.license: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

#### 로그 패턴
```
2024-01-15 10:30:00 [http-nio-8080-exec-1] INFO  vr.license.controller.LicenseController - License verification request: 25A1234-5678
2024-01-15 10:30:00 [http-nio-8080-exec-1] DEBUG vr.license.controller.LicenseController - License found: 25A1234-5678
2024-01-15 10:30:00 [http-nio-8080-exec-1] INFO  vr.license.controller.LicenseController - License verification successful
```

## 📞 지원 및 문의

### 개발팀 연락처
- **프론트엔드 개발자**: [연락처 정보]
- **백엔드 개발자**: [연락처 정보]
- **프로젝트 매니저**: [연락처 정보]

### 문서 버전
- **버전**: 1.0.0
- **최종 업데이트**: 2024-01-15
- **작성자**: [작성자 정보]

### 변경 이력
- **v1.0.0** (2024-01-15): 초기 문서 작성
- 라이센스 키 형식을 25A1234-5678로 통일
- API 명세 및 에러 처리 가이드 추가
- 개발 및 배포 가이드 작성

---

**참고**: 이 문서는 VR 라이센스 관리 시스템의 백엔드 개발을 위한 가이드입니다. 실제 구현 시에는 보안, 성능, 확장성 등을 고려하여 추가적인 검토가 필요합니다. 