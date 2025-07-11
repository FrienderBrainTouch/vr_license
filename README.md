# VR 라이센스 관리 시스템

VR 앱을 위한 라이센스 관리 및 인증 시스템입니다. 웹에서 라이센스를 생성하고 관리하며, VR 앱에서는 온라인 인증 후 오프라인에서 토큰 기반으로 라이센스를 체크할 수 있습니다.

## 🚀 주요 기능

### 웹 관리 시스템
- **라이센스 생성**: 다양한 유형의 라이센스 생성 (체험판, 기본, 프리미엄, 엔터프라이즈)
- **라이센스 관리**: 생성된 라이센스 모니터링 및 상태 관리
- **VR 앱 시뮬레이터**: VR 앱에서의 라이센스 인증 과정 시뮬레이션

### VR 앱 인증 시스템
- **온라인 인증**: 처음 라이센스 입력 시 서버에서 인증
- **오프라인 토큰**: 인증 후 로컬 토큰으로 오프라인 체크
- **중복 사용 방지**: 한 번 사용된 라이센스는 재사용 불가
- **기간 체크**: 라이센스 만료일 자동 체크

## 🛠 기술 스택

- **Backend**: Spring Boot 3.3.13, Java 17
- **Frontend**: Thymeleaf, Bootstrap 5, Font Awesome
- **Build Tool**: Gradle
- **Database**: 인메모리 (더미 데이터)

## 📁 프로젝트 구조

```
vr_license/
├── src/main/java/vr/license/
│   ├── controller/
│   │   ├── HomeController.java      # 홈 페이지 컨트롤러
│   │   └── LicenseController.java   # 라이센스 관리 컨트롤러
│   ├── dto/
│   │   ├── LicenseRequest.java      # 라이센스 생성 요청 DTO
│   │   └── VerifyRequest.java       # 라이센스 인증 요청 DTO
│   ├── model/
│   │   ├── License.java             # 라이센스 엔티티
│   │   └── LicenseUsage.java        # 라이센스 사용 기록 엔티티
│   └── LicenseApplication.java      # 메인 애플리케이션
├── src/main/resources/
│   ├── templates/
│   │   ├── home.html               # 메인 페이지
│   │   └── license/
│   │       ├── create.html         # 라이센스 생성 페이지
│   │       ├── manage.html         # 라이센스 관리 페이지
│   │       └── simulator.html      # VR 앱 시뮬레이터
│   └── application.properties      # 애플리케이션 설정
└── build.gradle                   # Gradle 빌드 설정
```

## 🚀 실행 방법

### 1. 프로젝트 클론
```bash
git clone <repository-url>
cd vr_license
```

### 2. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 3. 웹 브라우저에서 접속
```
http://localhost:8080
```

## 📋 API 문서

### 라이센스 인증 API (VR 앱용)

**POST** `/license/verify`

VR 앱에서 라이센스를 인증할 때 사용하는 API입니다.

#### 요청 예시
```json
{
  "licenseKey": "25A1234-5678",
  "deviceId": "device123",
  "appVersion": "1.0.0",
  "platform": "VR"
}
```

#### 응답 예시 (성공)
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

#### 응답 예시 (실패)
```json
{
  "success": false,
  "message": "유효하지 않은 라이센스 키입니다.",
  "code": "INVALID_LICENSE"
}
```

### 라이센스 관리 API

**GET** `/license/list` - 라이센스 목록 조회
**GET** `/license/{licenseKey}` - 라이센스 상세 조회
**POST** `/license/create` - 라이센스 생성
**POST** `/license/{licenseKey}/revoke` - 라이센스 취소

## 🎮 VR 앱 시뮬레이터

VR 앱에서의 라이센스 인증 과정을 시뮬레이션할 수 있습니다.

### 시나리오 1: 유효한 라이센스 활성화
- 유효한 라이센스 키로 인증
- 성공 시 로컬 토큰 생성
- VR 앱에서 컨텐츠 사용 가능

### 시나리오 2: 만료된 라이센스
- 만료된 라이센스 키로 인증 시도
- 인증 실패 및 만료 메시지 표시

### 시나리오 3: 오프라인 모드
- 인터넷 연결 없이 로컬 토큰 체크
- 라이센스 유효성 및 만료일 확인

## 🔧 라이센스 유형

| 유형 | 기간 | 설명 |
|------|------|------|
| TRIAL | 7일 | 체험판 라이센스 |
| BASIC | 30일 | 기본 라이센스 |
| PREMIUM | 90일 | 프리미엄 라이센스 |
| ENTERPRISE | 365일 | 엔터프라이즈 라이센스 |

## 🔒 보안 기능

- **중복 사용 방지**: 한 번 사용된 라이센스는 재사용 불가
- **기간 체크**: 라이센스 만료일 자동 확인
- **토큰 기반 인증**: 안전한 로컬 토큰 생성
- **디바이스 추적**: 사용 디바이스 정보 기록

## 🚧 개발 중인 기능

- [ ] 데이터베이스 연동 (MySQL/PostgreSQL)
- [ ] JWT 토큰 인증
- [ ] 라이센스 갱신 기능
- [ ] 사용 통계 대시보드
- [ ] REST API 문서 (Swagger)
- [ ] 단위 테스트 추가

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다.

## 🤝 기여하기

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 생성해주세요.
