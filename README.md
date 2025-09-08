# TLS Checker

Java 1.8과 Spring Boot 2를 사용하여 도메인의 TLS 지원 현황과 사용 가능한 암호화 알고리즘을 확인하는 프로그램입니다.

[![GitHub release](https://img.shields.io/github/release/wooix/tls-checker.svg)](https://github.com/wooix/tls-checker/releases)
[![GitHub license](https://img.shields.io/github/license/wooix/tls-checker.svg)](https://github.com/wooix/tls-checker/blob/main/LICENSE)
[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.18-brightgreen.svg)](https://spring.io/projects/spring-boot)

## 📥 다운로드 및 설치

### GitHub에서 다운로드
```bash
# 저장소 클론
git clone https://github.com/wooix/tls-checker.git
cd tls-checker

# 또는 ZIP 파일 다운로드
# https://github.com/wooix/tls-checker/archive/refs/heads/main.zip
```

### 최신 릴리스 다운로드
- [릴리스 페이지](https://github.com/wooix/tls-checker/releases)에서 다운로드
- `tls-checker-0.0.1-SNAPSHOT.jar` 파일을 다운로드하여 사용

## 기능

- 도메인의 TLS 버전 지원 현황 확인 (TLSv1, TLSv1.1, TLSv1.2, TLSv1.3)
- 지원되는 암호화 스위트(Cipher Suite) 목록 표시
- SSL 인증서 정보 확인
- xterm 콘솔에서 색상과 함께 결과 출력
- 명령행 인수 또는 대화형 모드 지원
- 대화형 모드에서 10초 입력 타임아웃
- 깔끔한 ASCII 테이블 포맷팅 (유니코드 문자 사용 안함)

## 요구사항

- Java 1.8 이상
- Maven 3.6 이상

## 빌드 및 실행

### 빌드
```bash
mvn clean package
```

### 실행 방법

#### 1. 명령행 인수로 실행
```bash
java -jar target/tls-checker-0.0.1-SNAPSHOT.jar google.com
```

#### 2. 대화형 모드로 실행
```bash
java -jar target/tls-checker-0.0.1-SNAPSHOT.jar
```

대화형 모드에서는 다음과 같은 명령을 사용할 수 있습니다:
- 도메인명 입력: TLS 지원 현황 확인
- `help`: 사용법 출력
- `quit` 또는 `exit`: 프로그램 종료
- 입력 타임아웃: 10초 (입력이 없으면 자동 종료)

## 출력 예시

```
+==============================================================================+
|                        TLS Support Status: google.com                        |
+==============================================================================+

+------------------------------------------------------------------------------+
|                             TLS Version: TLSv1.2                             |
+------------------------------------------------------------------------------+
| Status : SUPPORTED                                                             |
| Cipher Suites : (34 total)                                                     |
|  :   • TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384                                 |
|  :   • TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256                                 |
|  :   • TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384                                   |
|  :   • TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256                                   |
|  :   ... (total 34 suites)                                                     |
| Certificate Subject : CN=*.google.com                                          |
| Certificate Issuer : CN=WE2, O=Google Trust Services, C=US                     |
| Valid Period : 2025-08-18 17:40:09 ~ 2025-11-10 17:40:08                       |
| Signature Algorithm : SHA256withECDSA                                          |
+------------------------------------------------------------------------------+

+------------------------------------------------------------------------------+
|                             TLS Version: TLSv1.3                             |
+------------------------------------------------------------------------------+
| Status : SUPPORTED                                                             |
| Cipher Suites : (37 total)                                                     |
|  :   • TLS_AES_256_GCM_SHA384                                                  |
|  :   • TLS_AES_128_GCM_SHA256                                                  |
|  :   • TLS_CHACHA20_POLY1305_SHA256                                            |
|  :   ... (total 37 suites)                                                     |
| Certificate Subject : CN=*.google.com                                          |
| Certificate Issuer : CN=WE2, O=Google Trust Services, C=US                     |
| Valid Period : 2025-08-18 17:40:09 ~ 2025-11-10 17:40:08                       |
| Signature Algorithm : SHA256withECDSA                                          |
+------------------------------------------------------------------------------+

+------------------------------------------------------------------------------+
|                                   SUMMARY                                    |
+------------------------------------------------------------------------------+
| Supported TLS Versions : 2/4                                                   |
| Supported Versions : TLSv1.2, TLSv1.3                                          |
|  :                                                                             |
| Security Recommendations :                                                     |
| TLS 1.3 : SUPPORTED - Latest Security Standard                                 |
| TLS 1.2 : SUPPORTED - Secure Version                                           |
+------------------------------------------------------------------------------+
```

## 프로젝트 구조

```
src/main/java/ksd/std/tlschecker/
├── TlsCheckerApplication.java          # 메인 애플리케이션 클래스
├── service/
│   └── TlsCheckerService.java          # TLS 체크 서비스
└── util/
    └── ConsoleOutputUtil.java          # 콘솔 출력 유틸리티
```

## 주요 클래스

### TlsCheckerApplication
- Spring Boot 메인 애플리케이션
- 명령행 인수 처리 및 대화형 모드 관리
- 도메인 정규화 및 유효성 검사

### TlsCheckerService
- TLS 버전별 지원 여부 확인
- 암호화 스위트 및 인증서 정보 수집
- SSL 연결 및 핸드셰이크 처리

### ConsoleOutputUtil
- xterm 콘솔용 색상 출력
- 결과 포맷팅 및 테이블 표시
- 보안 권장사항 제공

## 보안 고려사항

- 모든 인증서를 신뢰하는 TrustManager 사용 (개발/테스트 목적)
- 실제 운영 환경에서는 적절한 인증서 검증 로직 구현 필요
- 연결 시간 제한: 10초
- 대화형 모드에서 입력 타임아웃: 10초

## 특징

- **깔끔한 ASCII 출력**: 완벽한 터미널 호환성을 위해 ASCII 128 문자만 사용
- **국제 호환성**: 글로벌 호환성을 위해 영문 메시지만 사용
- **완벽한 테이블 정렬**: 80자 고정 너비로 정확한 포맷팅
- **색상 출력**: 가독성 향상을 위한 ANSI 색상 코드
- **포괄적인 TLS 테스트**: 모든 TLS 버전 테스트 (1.0, 1.1, 1.2, 1.3)
- **인증서 정보**: 주체, 발급자, 유효기간, 서명 알고리즘 표시
- **보안 권장사항**: 보안 평가 및 권장사항 제공

## 🤝 기여하기

이 프로젝트에 기여하고 싶으시다면:

1. **Fork** 이 저장소
2. **Feature branch** 생성 (`git checkout -b feature/AmazingFeature`)
3. **Commit** 변경사항 (`git commit -m 'Add some AmazingFeature'`)
4. **Push** 브랜치에 (`git push origin feature/AmazingFeature`)
5. **Pull Request** 생성

## 📝 이슈 및 버그 리포트

- [Issues](https://github.com/wooix/tls-checker/issues)에서 버그 리포트나 기능 요청
- 새로운 이슈를 생성하기 전에 기존 이슈를 확인해주세요

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](https://github.com/wooix/tls-checker/blob/main/LICENSE) 파일을 참조하세요.