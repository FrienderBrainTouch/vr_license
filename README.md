# VR 라이센스 관리 시스템

VR 콘텐츠 및 앱을 위한 라이센스 발급, 관리, 인증을 제공하는 웹 기반 시스템입니다. 
관리자는 웹에서 라이센스를 생성·관리하고, VR 앱은 서버와 연동하여 라이센스 인증 및 오프라인 토큰 검증을 수행할 수 있습니다.

---

## 🏗️ 주요 기능

- **라이센스 생성**: 다양한 유형(체험판, 기본, 프리미엄, 엔터프라이즈, 커스텀) 라이센스 발급
- **라이센스 관리**: 상태(활성, 만료, 사용중)별 라이센스 목록 및 만료 처리
- **대시보드**: 전체/상태별 라이센스 통계, 최근 생성 라이센스, 시스템 정보 제공
- **VR 앱 인증 API**: VR 앱에서 라이센스 인증 및 오프라인 토큰 발급
- **보안**: 중복 사용 방지, 만료일 자동 체크, 디바이스 정보 기록

---

## 🛠️ 기술 스택

- **Backend**: Spring Boot 3.3.13, Java 17, Spring Data JPA
- **Frontend**: Thymeleaf, Bootstrap 5
- **Database**: (기본) 인메모리, MySQL 연동 가능
- **Build Tool**: Gradle

---

## 📁 프로젝트 구조

```
vr_license/
├── src/main/java/vr/license/
│   ├── controller/         # 웹 컨트롤러 (Auth, Home, License)
│   ├── dto/                # 데이터 전송 객체
│   ├── model/              # 엔티티/도메인 모델
│   ├── repository/         # JPA 리포지토리
│   └── LicenseApplication.java
├── src/main/resources/
│   ├── templates/          # Thymeleaf 템플릿(뷰)
│   │   ├── home.html
│   │   └── auth/
│   │       └── login.html
│   └── application.properties
├── build.gradle
└── README.md
```

---

## ⚡️ 실행 방법

1. **프로젝트 클론**
    ```bash
    git clone <repository-url>
    cd vr_license
    ```

2. **애플리케이션 실행**
    ```bash
    ./gradlew bootRun
    ```

3. **웹 브라우저에서 접속**
    ```
    http://localhost:8080
    ```

> 기본적으로 인메모리 DB로 동작합니다. MySQL 등 외부 DB 연동 시 `application.properties`를 수정하세요.

---

## 📋 주요 API

### 라이센스 인증 (VR 앱용)
- **POST** `/license/verify`
    - 요청: `{ "licenseKey": "...", "deviceId": "...", ... }`
    - 성공 시 토큰 및 라이센스 정보 반환

### 라이센스 관리
- **GET** `/license/list` : 라이센스 목록 조회
- **POST** `/license/create` : 라이센스 생성
- **POST** `/license/{licenseKey}/revoke` : 라이센스 만료 처리

---

## 🎮 VR 앱 인증 시나리오

- **정상 인증**: 유효한 라이센스 → 토큰 발급 → VR 앱 사용 가능
- **만료/중복**: 만료 또는 이미 사용된 라이센스 → 인증 실패
- **오프라인**: 발급받은 토큰으로 오프라인 상태에서 라이센스 체크

---

## 🔧 라이센스 유형

| 유형        | 기간   | 설명                |
|-------------|--------|---------------------|
| TRIAL       | 7일    | 체험판              |
| BASIC       | 30일   | 기본 라이센스       |
| PREMIUM     | 90일   | 프리미엄            |
| ENTERPRISE  | 365일  | 엔터프라이즈        |
| CUSTOM      | 직접입력| 커스텀(기간 지정)   |

---

## 🔒 보안 및 정책

- 중복 사용 방지(1회성 키)
- 만료일 자동 체크
- 토큰 기반 인증
- 디바이스 정보 기록

---

## 🚧 개발 예정

- [ ] 외부 DB(MySQL 등) 완전 연동
- [ ] JWT 인증
- [ ] 라이센스 갱신
- [ ] 사용 통계 대시보드
- [ ] REST API 문서(Swagger)
- [ ] 단위 테스트

---

## 📝 라이선스

MIT License

---

## 🤝 기여 방법

1. Fork 후 브랜치 생성
2. 기능 개발 및 커밋
3. PR(Pull Request) 생성

---

## 📞 문의

이슈로 문의 남겨주세요.
