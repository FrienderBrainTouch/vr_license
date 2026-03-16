## VR 라이센스 관리 시스템 계정/접속 정보 정리

- **문서 목적**: 프로젝트 운영/유지보수 시 필요한 계정, 접속 정보, 자격 증명을 한 곳에 정리
- **작성일**: 2026-03-16
- **주의**: 이 문서에는 민감 정보가 포함될 수 있으므로 **내부 공유용으로만 사용**하고, 가능한 한 비밀번호는 직접 값을 적지 말고 “별도 전달”로 표기하는 것을 권장합니다.

---

### 1. 웹 애플리케이션 계정

#### 1.1 관리자 로그인 (현재 구현 상태)

- **로그인 URL**: `http://localhost:8080/login`
- **계정 저장 위치**: 코드 하드코딩 (`AuthController`)
- **기본 계정**
  - 아이디: `admin`
  - 비밀번호: `admin123`

> 이 계정은 **개발/테스트용 임시 계정**입니다. 운영 배포 전에 반드시 다음 중 하나를 수행해야 합니다.
> - DB 기반 계정/권한 시스템 도입 (`users` 테이블 활용)
> - 비밀번호 변경 후, 환경변수/설정 파일로 분리

관련 코드:

- `vr.license.controller.AuthController`
  - `ADMIN_USERNAME = "admin"`
  - `ADMIN_PASSWORD = "admin123"`

---

### 2. 데이터베이스 계정

#### 2.1 애플리케이션 DB 접속 정보 (로컬 기준)

- **설정 파일**: `src/main/resources/application.properties`
- **주요 설정 키**
  - `spring.datasource.url=jdbc:mysql://localhost:3306/vrLicense?...`
  - `spring.datasource.username=<로컬 DB 사용자명>`
  - `spring.datasource.password=<로컬 DB 비밀번호>`

현재 레포지토리에는 실제 값이 포함되어 있으므로, 운영 환경으로 이관 시:

- [ ] `application.properties`에서 민감 값 제거
- [ ] 운영 서버에서는 환경변수 또는 외부 설정(`application-prod.yml`, Secret Manager 등)로 분리

#### 2.2 권장 운영용 계정 전략

- **DB 계정 분리**
  - 애플리케이션 전용 계정(ex. `vr_license_app`)을 생성하여 최소 권한만 부여
  - 운영자/DBA 계정은 별도 (`root`/`admin`)로 유지
- **비밀번호 관리**
  - 비밀번호는 이 문서에 **직접 기입하지 말고**, 패스워드 관리자/보안 메모 등에 보관
  - 이 문서에는 “어디에 저장되어 있는지”만 기술 (예: “사내 1Password Vault - VR팀 공유 금고”)

---

### 3. Git / 레포지토리

- **레포지토리 위치**: 로컬 `vr_license` (원격 URL은 인수인계 시 구두/별도 문서로 전달)
- **브랜치 전략**:
  - 기본 브랜치: `main`
  - 기능 개발 시: `feature/*` (예: `feature/unity-integration`, `feature/db-hardening`)

> 실제 Git 호스팅(GitHub/GitLab/사내 Git) 주소와 접근 권한(계정/팀)은 사내 개발 표준에 따라 별도 문서 또는 위키에 정리하는 것을 권장합니다.

---

### 4. Unity(유니티) 라이선스 서버 URL

현재 이 저장소에는 Unity 프로젝트가 포함되어 있지 않지만, VR 앱에서 사용할 **라이선스 서버 URL** 정책은 다음과 같습니다.

- **개발/로컬 환경**
  - `http://localhost:8080/license/verify`
- **스테이징/테스트 환경**
  - (예시) `https://stg-vr-license.example.com/license/verify`
- **운영 환경**
  - (예시) `https://vr-license.example.com/license/verify`

#### 4.1 설정 방법 가이드 (Unity 쪽)

- URL을 **하드코딩하지 말고**, 다음 중 하나로 분리:
  - ScriptableObject 설정
  - `appsettings.json`/`config.json` 등 외부 설정 파일
  - 빌드 시 환경변수(Define Symbols) 기반 분기
- 인수인계 시점 상태:
  - 아직 정식 운영 도메인이 없으므로,
    - Unity 프로젝트에는 **개발용 또는 스테이징용 URL만 설정**해 두고,
    - 실제 운영 도메인 확정 후 이 값만 교체하는 형태로 설계

---

### 5. 클라우드/서버 계정 (계획/가이드)

`DEPLOYMENT_GUIDE.md`에는 AWS/GCP/Azure 배포 예시가 포함되어 있으나, 실제 계정/리소스는 아직 확정되지 않았거나 외부에 기록되지 않은 상태로 가정합니다.

운영 시 아래 항목을 별도 보안 문서에 정리해야 합니다.

- 클라우드 계정
  - AWS 계정 ID, IAM 사용자/역할 이름
  - GCP/Azure 프로젝트/리소스 그룹 정보
- 서버/인스턴스
  - EC2/VM 인스턴스 접속 계정 (예: `ubuntu`, `ec2-user` 등)
  - SSH 키 관리 위치
- 기타
  - SSL 인증서 발급 계정 (예: Let’s Encrypt 이메일)
  - 모니터링/로그 수집(예: CloudWatch, Stackdriver, Prometheus/Grafana) 접근 계정

> 이 문서는 “어떤 종류의 계정이 필요하고 어디에 기록해야 하는지”를 안내하는 역할을 합니다. 실제 ID/비밀번호/키 값은 별도 보안 채널로 관리하세요.

---

### 6. 인수인계 체크 포인트 (계정 관점)

- [ ] 관리자 웹 계정 정책 확정 (임시 `admin/admin123` 제거 또는 교체)
- [ ] DB 접속 계정/비밀번호, 백업 계정 정리 및 보안 저장소에 등록
- [ ] Git 원격 레포지토리 URL 및 접근 권한(팀/계정) 정리
- [ ] Unity 프로젝트 내 라이선스 서버 URL 설정 방식 및 현재 값 확인
- [ ] 향후 사용할 클라우드/서버 계정 계획 수립 및 문서화

