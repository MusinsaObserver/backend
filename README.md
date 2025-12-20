# 🧥 Musinsa Price Observer - Backend Server

> **무신사 상품 가격 변동 추적 및 푸시 알림 서버**
> *Java 21 & Multi-threading 기반의 고성능 데이터 수집 서버 저장소입니다.*

## 📌 Project Overview
이 프로젝트는 무신사 스토어의 상품 데이터를 수집하여 **가격 변동 히스토리**를 저장하고, 가격 하락 시 사용자에게 **푸시 알림**을 발송하는 백엔드 시스템입니다.
단순 HTML 파싱이 아닌 **Reverse Engineering을 통한 API 직접 호출** 방식을 적용했으며, **ExecutorService를 이용한 병렬 처리**로 수집 속도를 극대화했습니다.

<br>

## 🏗 System Architecture
> **전체 시스템의 데이터 흐름과 병렬 처리 구조도입니다.**

<img src="https://github.com/user-attachments/assets/d507a619-452c-47e0-bbc5-8ea3854fbff7" width="100%" alt="System Architecture Diagram">

---

## 🛠 Tech Stack

| Category | Stack | Details |
| :--- | :--- | :--- |
| **Language** | Java | **JDK 21 LTS** |
| **Framework** | Spring Boot | **3.3.2** |
| **Concurrency** | ExecutorService | **FixedThreadPool(5) Parallel Processing** |
| **Networking** | HttpURLConnection, HttpClient | REST API Communication |
| **Data Processing** | Gson | JSON Parsing |
| **Security** | Spring Security, OAuth2 | **Apple Login & JWT** |
| **Database** | MySQL | Transaction Management |

---

## 🧩 Key Features & Implementation

### 1. 고성능 병렬 데이터 수집 (Parallel Data Collection)
* **API Reverse Engineering:** HTML을 파싱하는 비효율적인 방식 대신, 무신사 내부 API(`api.musinsa.com`)를 분석하여 직접 호출함으로써 데이터 신뢰도와 속도를 높였습니다.
* **Multi-threading:** `ExecutorService`와 `FixedThreadPool(5)`을 적용하여 15개 이상의 카테고리 데이터를 **병렬로 수집(Parallel Crawling)** 합니다.
* **Batch Processing:** 수집된 데이터를 100개 단위로 나누어 저장(`saveProductsInBatches`)함으로써 DB I/O 부하를 최소화했습니다.

### 2. 푸시 알림 시스템 (Notification System)
* **APNs Integration:** `Java 11 HttpClient`를 사용하여 Apple Push Notification Service(APNs)와 직접 통신하는 로직을 구현했습니다.
* **비동기 처리:** 가격 하락 감지 시, 사용자의 디바이스 토큰으로 즉시 알림 패킷을 전송합니다.

### 3. 인증 및 보안 (Security)
* **Apple OAuth2:** 애플 로그인(`Sign in with Apple`)의 공개키(Public Key) 검증 로직을 직접 구현하여 ID Token을 안전하게 복호화합니다.
* **JWT:** 인증된 사용자에게 자체 Access Token을 발급하여 Stateless한 세션 관리를 수행합니다.

---

## 📂 Folder Structure

```bash
src/main/java/observer/backend
├── 📂 service          
│   ├── CrawlerService.java    # 병렬 크롤링 및 배치 처리 (Core)
│   ├── AuthService.java       # Apple 로그인 및 JWT 발급
│   ├── PushNotificationService.java # APNs 알림 발송
│   └── ...
├── 📂 dto              # Data Transfer Objects
├── 📂 entity           # JPA Entities
├── 📂 repository       # Data Access Layer
└── 📂 config           # Security & App Config
