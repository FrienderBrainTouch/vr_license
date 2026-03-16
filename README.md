## VR 라이센스 관리 시스템

VR 콘텐츠 및 앱을 위한 라이센스 발급, 관리, 인증을 제공하는 웹 기반 시스템입니다.  
관리자는 웹에서 라이센스를 생성·관리하고, VR 앱은 서버와 연동하여 라이센스 인증 및 오프라인 토큰 검증을 수행할 수 있습니다.

- **최초 작성일**: 2024-xx-xx (필요 시 보정)
- **최종 수정일**: 2026-03-16

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
- **Database**: MySQL (로컬 `vrLicense` 기준)
- **Build Tool**: Gradle

---

## 📁 프로젝트 구조

```
vr_license/
├── src/main/java/vr/license/
│   ├── controller/         # 웹 컨트롤러 (Auth, License 등)
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
├── HANDOVER.md             # 인수인계용 개요/흐름 문서
├── ACCOUNTS.md             # 계정/접속 정보 및 URL 정책
├── DATABASE_SCHEMA.md      # DB 스키마 및 마이그레이션
├── DEPLOYMENT_GUIDE.md     # 배포/운영 가이드
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

> 기본적으로 로컬 MySQL(`vrLicense`) DB를 사용하도록 설정되어 있습니다. 다른 DB 환경(MySQL 서버, 클라우드 DB 등) 사용 시 `application.properties` 또는 프로파일별 설정 파일을 수정하세요.

관련 상세 내용:

- 인수인계 개요 및 기능 흐름: `HANDOVER.md`
- 계정/접속 정보 및 Unity 라이선스 URL 정책: `ACCOUNTS.md`
- 배포/운영/인프라 설정: `DEPLOYMENT_GUIDE.md`
- DB 구조 및 마이그레이션: `DATABASE_SCHEMA.md`

---

## 📋 주요 API

### 라이센스 인증 (VR 앱용)
- **POST** `/license/verify`
    - 요청 예시: `{ "key": "25A1234-5678" }`
    - 응답 예시: `{ "status": "VALID", "expiry": "2026-03-31T23:59:59" }`

### 라이센스 관리
- **GET** `/license/list` : 라이센스 목록 조회 (로그인 필요, JSON 응답)
- **POST** `/license/create` : 라이센스 생성 (웹 폼/관리자용)
- **POST** `/license/{licenseKey}/revoke` : 라이센스 만료 처리

> Unity/VR 앱에서 사용할 구체적인 호출 방식과 서버 URL 정책은 `ACCOUNTS.md`의 “Unity 라이선스 서버 URL” 섹션을 참고하세요.

---

## 🎮 VR 앱 인증 시나리오

- **정상 인증**: 유효한 라이센스 → 상태 `IN_USE`로 변경 → 만료일 계산/저장 → VR 앱 사용 가능
- **만료**: 만료일이 지난 라이센스 → 상태 `EXPIRED`로 변경 → 인증 실패
- **미등록**: 존재하지 않는 라이센스 키 → `NOT_FOUND` 응답
- **오프라인 사용**: 응답의 `expiry` 값과 토큰/캐시를 활용해 클라이언트 단에서 오프라인 허용 로직 구현 (클라이언트 책임)

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

- 중복 사용 방지(1회성/제한적 사용 키)
- 만료일 자동 체크 및 상태 자동 갱신
- 토큰/사용기록 기반 인증 기록 (`LicenseUsage`)
- 관리자 로그인 세션 기반 접근 제어 (추후 DB 기반 계정 시스템 권장)

보안 관련 심화 설정은 `DEPLOYMENT_GUIDE.md` 및 `DATABASE_SCHEMA.md`의 제약/인덱스 섹션을 참고하세요.

---

## 🚧 개발 예정

- [ ] 외부 DB(MySQL 등) 완전 연동 및 운영 스키마 고도화
- [ ] JWT 기반 토큰 발급
- [ ] 라이센스 갱신 및 플랜 변경 기능
- [ ] 사용 통계 대시보드
- [ ] REST API 문서(Swagger/OpenAPI)
- [ ] 단위/통합 테스트

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
