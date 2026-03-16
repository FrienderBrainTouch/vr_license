## VR 라이센스 관리 시스템 인수인계 문서

- **프로젝트명**: VR 라이센스 관리 시스템 (`vr_license`)
- **작성일**: 2026-03-16
- **작성자**: (인수인계 시 담당자 이름 기입)

---

### 1. 개요

- **역할**: VR 콘텐츠/앱을 위한 라이센스 발급, 관리, 인증 서버
- **주요 대상**:
  - 내부 운영자(관리자): 웹 대시보드에서 라이센스 생성·조회·만료 처리
  - VR 앱(클라이언트): `/license/verify` API로 라이센스 검증
- **현재 상태**:
  - 로컬/내부 환경에서 동작 확인
  - 정식 서비스 도메인으로 배포는 아직 진행되지 않음

---

### 2. 기술 스택 및 구조

- **Backend**: Spring Boot 3.x, Java 17, Spring MVC, Spring Data JPA
- **View**: Thymeleaf + Bootstrap 5
- **DB**: MySQL (로컬 `vrLicense` 스키마 사용)
- **빌드/실행**: Gradle (`./gradlew bootRun`)

주요 패키지 구조:

- `vr.license.controller`
  - `AuthController`: 관리자 로그인 및 대시보드 진입
  - `LicenseController`: 라이센스 생성, 관리, VR 앱 인증 API
- `vr.license.repository`
  - `LicenseRepository`, `LicenseUsageRepository`: 라이센스/사용기록 JPA 리포지토리
- `vr.license.model`
  - `License`, `LicenseUsage`: 라이센스와 사용기록 엔티티
- `src/main/resources/templates`
  - `home.html`, `license/create.html`, `license/manage.html`, `license/simulator.html`, `auth/login.html` 등

데이터베이스 구조는 `DATABASE_SCHEMA.md`에 상세히 정리되어 있으며, 실제 운영 시 해당 스키마를 기준으로 MySQL에 생성하면 됩니다.

---

### 3. 실행/개발 환경

#### 3.1 로컬 실행 절차

1. **소스 체크아웃**
   - Git에서 `vr_license` 프로젝트 클론
2. **DB 준비**
   - MySQL에 `vrLicense`(또는 `vr_license`) 데이터베이스 생성
   - 초기 스키마 및 샘플 데이터는 `DATABASE_SCHEMA.md`의 SQL 참고
3. **환경 설정**
   - `src/main/resources/application.properties` 확인/수정  
     - DB URL, 계정, 비밀번호 설정 (현재는 로컬 `root` 계정 기준)
4. **실행**
   - `./gradlew bootRun` (Windows에서는 `gradlew.bat bootRun`)
   - 브라우저에서 `http://localhost:8080` 접속

#### 3.2 관리자 계정 (임시)

- `AuthController`에 하드코딩 되어 있음:
  - 아이디: `admin`
  - 비밀번호: `admin123`
- 실제 운영 전에는 **DB 기반 계정 관리로 교체**하거나, 최소한 비밀번호를 변경하고 환경변수/설정 파일로 분리 필요.

---

### 4. 주요 기능 흐름

#### 4.1 관리자 웹 (대시보드)

- `/login`
  - `admin` / `admin123` 로그인 시 세션에 `isLoggedIn=true` 설정
- `/` (홈 대시보드)
  - 로그인 필요
  - 전체 라이센스 수, 상태별 통계, 최근 생성 라이센스 목록 출력
  - 만료일이 지난 라이센스는 진입 시 자동으로 `EXPIRED` 처리
- `/license/create`
  - 라이센스 생성 화면
  - 키를 비워둘 경우 `YYX1234-5678` 형식으로 자동 생성
- `/license/manage`
  - 라이센스 목록/상태 조회
  - 상태/생성일 기준으로 정렬

#### 4.2 VR 앱 연동 (라이센스 인증 API)

- 엔드포인트: `POST /license/verify`
- 요청 JSON 예시:
  ```json
  {
    "key": "25A1234-5678"
  }
  ```
- 응답 예시:
  ```json
  {
    "status": "VALID",    // VALID, EXPIRED, NOT_FOUND, ERROR
    "expiry": "2026-03-31T23:59:59"
  }
  ```
- 내부 처리:
  - 라이센스 존재 여부 확인 (`LicenseRepository.findByLicenseKey`)
  - 만료일이 지났으면 `EXPIRED`로 상태 업데이트
  - 최초 인증(만료일 null & 커스텀 타입 아님)인 경우 유형(TRIAL/BASIC/PREMIUM/ENTERPRISE)에 따라 endDate 계산 후 저장
  - 상태를 `IN_USE`로 변경
  - `LicenseUsage`에 사용 기록 생성/갱신

---

### 5. Unity(유니티) 클라이언트 연동 개요

현재 Unity 쪽 프로젝트는 이 저장소에 포함되어 있지 않지만, VR 앱은 아래 방식으로 이 서버와 통신하게 됩니다.

- **기본 호출 URL (개발/로컬)**  
  - `POST http://localhost:8080/license/verify`
- **추후 운영 배포 시**  
  - 도메인 확정 후:  
    - `POST https://{운영도메인}/license/verify`
- Unity C# 측에서 해야 할 일:
  - `HttpClient` 또는 `UnityWebRequest`를 사용해 위 URL로 JSON `{ "key": "<라이센스키>" }` POST
  - 응답의 `status`, `expiry`를 파싱해 게임 내 접근 허용/차단 처리
  - **현재는 아직 정식 도메인이 없으므로**, Unity 프로젝트 내 라이센스 서버 URL을 **로컬/스테이징용 URL로만 설정**해 두고, 배포 직전에 운영 도메인으로 한 번에 교체할 계획을 문서화

> 인수인계 시점에는 “Unity 라이선스 서버 URL”을 하드코딩하지 말고, **설정 파일 또는 ScriptableObject/환경설정 값으로 분리**하는 것을 권장합니다. 실제 도메인 확정 후 이 값만 교체하면 됩니다.

---

### 6. 데이터베이스 및 계정 정보 요약

- **애플리케이션 DB 설정**: `src/main/resources/application.properties`
  - `spring.datasource.url=jdbc:mysql://localhost:3306/vrLicense?...`
  - `spring.datasource.username` / `spring.datasource.password`
- **스키마 정의**: `DATABASE_SCHEMA.md`
  - `licenses`, `license_usages` 기본 테이블
  - 선택적 `users`, `audit_logs` 테이블 설계 포함

> 실제 운영 환경에서는 DB 계정/비밀번호를 환경변수 또는 별도 설정 파일로 분리하고, Git에는 노출하지 않도록 재구성 필요합니다.

---

### 7. 배포 및 운영 관련 문서

- `DEPLOYMENT_GUIDE.md`
  - 서버 준비, Docker, 클라우드 배포(AWS/GCP/Azure) 등 전체 배포 절차 정리
- `DATABASE_SCHEMA.md`
  - DB 구조 및 마이그레이션 스크립트
- (본 문서) `HANDOVER.md`
  - 인수인계를 위한 개요, 흐름, 계정/URL 요약

---

### 8. 인수인계 체크리스트

- [ ] 로컬 환경에서 `./gradlew bootRun` 실행 및 `/login` 접속 확인
- [ ] MySQL 스키마/접속 확인 (`DATABASE_SCHEMA.md` 기준)
- [ ] 관리자 계정/비밀번호 변경 또는 DB 기반 계정으로 전환 계획 수립
- [ ] Unity 클라이언트에서 호출할 라이센스 서버 URL을 **설정화**하고, 현재는 개발/스테이징 URL로만 설정
- [ ] 정식 운영 도메인 확정 시:
  - [ ] `DEPLOYMENT_GUIDE.md`에 실제 도메인/환경 변수 값 반영
  - [ ] Unity 프로젝트의 서버 URL을 `https://{운영도메인}/license/verify`로 교체
  - [ ] SSL 인증서/방화벽 등 보안 설정 최종 점검

