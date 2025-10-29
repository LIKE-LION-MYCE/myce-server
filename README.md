# 🎟  MYCE : 박람회 예매/운영 서비스 
<img width="1447" height="805" alt="image" src="https://github.com/user-attachments/assets/2a02e673-0b5e-4a11-91f7-367e47f49161" />

**배포url** : https://www.myce.live

**테스트 계정**  
- 일반 사용자 및 박람회 관리자 계정 : **leoleo / qwer1234**  
- 플랫폼관리자 계정 : **myceadmin01 / qwe123**  

<br/>

## 🧑‍💻 프로젝트 소개

MICE는 Meeting, IncentiveTravel, Convention, Exhibition/Event의 앞글자를 딴 용어입니다.  
MYCE는 위 MICE의 개념을 기반으로, 박람회를 쉽고 스마트하게 운영할 수 있는 **박람회 생애주기 관리 플랫폼**입니다.  
온라인 박람회 **개최부터 예약, 결제, 정산까지 원스톱으로 제공하는 종합 서비스**를 제공하며 다음과 같은 복합적인 의미를 담아내고자 했습니다.
```
- Meet Your Clients & Exhibitions : 고객과 박람회를 한곳에서 함께 관리하세요.
- Manage Your Conferences & Expos : 당신의 박람회를 체계적으로 관리하세요.
- Make Your Conventions Easy: 박람회 운영을 쉽고 스마트하게 만들어보세요.
```


<br/>

### 👥 팀원 소개 및 역할
<img width="1396" height="665" alt="image" src="https://github.com/user-attachments/assets/92e5810b-400e-427f-9d30-2edd3e827753" />

<br/>
<br/>


## ✨ 주요 기능
<img width="1408" height="642" alt="image" src="https://github.com/user-attachments/assets/4dfd731a-e2e7-43b5-926c-2ad04b68f3d4" />

### 🖋️ 분류별 상세 기능

| 구분 | 기능 |
|------|------|
| &nbsp; &nbsp;&nbsp; 사용자 관리 &nbsp;&nbsp;&nbsp;| &nbsp; <ul><li><b>[회원가입/로그인](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/auth)</b>: 일반 회원가입, 소셜 로그인 (OAuth2)</li><li><b>회원등급 시스템</b>: Bronze, Silver, Gold, Platinum, Diamond 등급별 혜택 &nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</li><li><b>[마이페이지](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/member)</b>: 개인정보 관리, 예약 내역, 결제 내역</li><li><b>다국어 지원</b>: 한국어, 영어 등 다국어 인터페이스</li></ul> &nbsp; |
| &nbsp; &nbsp;&nbsp;[박람회 관리](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/expo) &nbsp;| &nbsp; <ul><li><b>박람회 등록</b>: 상세 정보, 이미지, 위치, 일정 설정</li><li><b>카테고리 관리</b>: 다양한 박람회 카테고리 분류 </li><li><b>부스 관리</b>: 박람회 내 개별 부스 정보 관리 </li><li><b>이벤트 관리</b>: 박람회 내 특별 이벤트 스케줄링 </li><li><b>승인 시스템</b>: 플랫폼 관리자의 박람회 승인 프로세스</li></ul> &nbsp;|
| &nbsp; &nbsp;&nbsp;예약 & 티켓&nbsp; | &nbsp; <ul></li><li><b>티켓 시스템</b>: 다양한 티켓 타입 및 가격 설정 </li><li><b>온라인 예약</b>: 실시간 예약 및 재고 관리</li><li><b>[QR 코드](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/qrcode)</b>: 예약 확인 및 입장용 QR 코드 생성 </li><li><b>비회원 예약</b>: 게스트 사용자 예약 지원</li></ul>&nbsp; |
| &nbsp; &nbsp;&nbsp;[결제 & 정산](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/payment) | &nbsp; <ul></li><li><b>통합 결제</b>: 카드결제, 계좌이체, 가상계좌 등 </li><li><b>마일리지 시스템</b>: 등급별 마일리지 적립 및 사용 </li><li><b>환불 처리</b>: 자동화된 환불 프로세스 </li><li><b>정산 관리</b>: 박람회 주최자 정산 시스템</li></ul> &nbsp;|
| &nbsp;&nbsp;&nbsp;&nbsp; [광고 관리](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/advertisement) &nbsp;|&nbsp; <ul></li><li><b>배너 광고</b>: 메인페이지 광고 위치별 관리 </li><li><b>광고 신청</b>: 광고주 신청 및 승인 시스템</li><li><b>요금 설정</b>: 위치별 광고 요금 관리</li></ul> &nbsp;|
| &nbsp; &nbsp;&nbsp;소통 & 지원&nbsp; | &nbsp; <ul></li><li><b>[실시간 채팅](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/chat)</b>: WebSocket 기반 고객 지원 채팅 </li><li><b>[AI 챗봇](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/ai)</b>: Spring AI + AWS Bedrock 연동</li><li><b>[알림 시스템](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/notification)</b>: 실시간 푸시 알림</li><li><b>[이메일 발송](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/notification)</b>: 템플릿 기반 이메일 시스템</li></ul> &nbsp;|
| &nbsp; &nbsp;&nbsp;관리자 기능&nbsp; | &nbsp; <ul></li><li><b>[대시보드](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/dashboard)</b>: 통계 및 현황 모니터링 </li><li><b>박람회/광고 생애 관리</b>: 신청 승인 및 결제/정산 처리</li><li><b>사용자 관리</b>: 회원 정보 및 권한 관리</li><li><b>[시스템 설정](https://github.com/LIKE-LION-MYCE/myce-server/tree/develop/src/main/java/com/myce/system)</b>: 요금, 템플릿 등 시스템 설정</li></ul> &nbsp;|


### 🌟 주요 특징
- 실시간 통신: WebSocket(STOMP)과 SSE를 활용한 실시간 채팅 및 알림 전달
- 결제 : 토스페이먼츠 OpenAPI를 활용한 결제 시스템 구축
- 보안: Spring Security + JWT를 통한 인증/인가 처리
- 확장성: Docker 컨테이너화 및 AWS 클라우드 인프라 설정
- 모니터링: Prometheus & Grafana를 통한 실시간 시스템 모니터링
- API 문서화: Swagger를 통한 자동화된 API 문서화 설정

<br/>



## 🏗 시스템 아키텍처
<img width="1137" height="787" alt="image" src="https://github.com/user-attachments/assets/6026d573-9728-4be9-b186-55ef7cba8dbe" />


**[자동화된 인프라 구축]**   
- 일관성 있고 재현 가능한 배포 환경 구축할 수 있도록 **코드화**
- **Terraform**: AWS 리소스(EC2, S3, CloudFront 등) 자동 생성 및 관리
- **Ansible**: 서버 환경 설정과 소프트웨어 설치 자동화  


**[백엔드 배포]**  
- **EC2 + Docker**: 컨테이너 기반 백엔드 서비스 배포
- **Auto Scaling**: 트래픽에 따른 인스턴스 자동 확장/축소


**[데이터베이스]**  
- **MySQL**: 회원, 박람회, 결제 등 관계형 데이터 저장
- **MongoDB**: 채팅 메시지, 알림 등 비정형 데이터 저장
- **Redis**: JWT 토큰, 세션, 채팅 캐시 등 고속 처리 데이터  


**[프론트엔드 배포]**  
- **S3 Bucket #1**: React 빌드 파일 호스팅  
- **S3 Bucket #2**: 사용자 업로드 이미지/미디어 파일 저장
- **CloudFront**: CDN을 통한 글로벌 빠른 컨텐츠 전송 및 캐싱


**[모니터링 시스템]**  
- **Prometheus**: 메트릭 수집 및 저장  
- **Grafana**: 실시간 대시보드를 통한 시각화 모니터링

  
**[CI/CD 파이프라인]**   
- **GitHub Actions**을 사용한 완전 자동화 : Develop 브랜치 Push될 때 자동 빌드 및 배포 로직이 동작
- **Backend**: Docker 이미지 빌드 → ECR 업로드 → EC2 자동 배포  
- **Frontend**: React 빌드 → S3 업로드 → CloudFront 캐시 무효화


**[성능 최적화 및 테스트]**  
- **Application Load Balancer**: 다중 인스턴스 트래픽 분산  
- **K6**: 부하 테스트를 통한 성능 임계점 측정  
- **Auto Scaling**: CPU/메모리 사용률 기반 자동 스케일링  

<br/>
<br/>

## 🛠 기술 스택

**[Frontend]**  
- React 19.1.1
- Vite 7.0.6
- JavaScript ES6+
- CSS3 Modules

**[Backend]**
- Spring Boot 3.5.4
- Java 21
- Spring Security
- JWT 

**[Database]**
- MySQL 8.0
- MongoDB 4.4
- Redis 6.0

**[Infrastructure]**
- Amazon AWS
  - EC2, S3, CloudFront
- Terraform, Ansible
- Github Action
- Gradle

**[Development]**
- IntelliJ IDEA
- VS Code
- Postman
- Github

<br/>

## 📁 프로젝트 패키징 구조
체계적인 패키지 구조를 기반으로, 모든 기능 개발 시 일관된 개발 방식을 적용하였습니다.
### Frontend Architecture

```
src/
├── 각 분류별 패키지/         # 페이지별 컴포넌트
│   ├── components/
│   ├── layout/
│   └── pages/
├── common/               # 재사용 가능한 공통 컴포넌트
├── services/             # API 통신 레이어
├── utils/                # 공통 유틸리티 함수
├── hooks/                # 커스텀 React Hooks
├── context/              # 전역 상태 관리
└── routs/                # 전체 라우터 관리
```

### Backend Architecture

```
src/
├── 각 분류별 패키지
│   ├── controller/           # REST API 엔드포인트
│   ├── service/              # 비즈니스 로직 처리
│       ├── impl/             # 확장성을 위한 interface 구현
│       └── mapper/           # Entity-Dto 변환 레이어
│   ├── repository/           # 데이터 액세스 레이어
│   ├── entity/               # JPA/MongoDB 엔티티
│   ├── dto/                  # 데이터 전송 객체
│   └── mongodb/
├── common/                   # 공통 로직 처리
│   ├── aop/                  # 공통 로거 
│   ├── exception/            # CustomException 처리
│   └── util/                 # 공통 사용 util
├── auth/                     # 인증/인가 처리
│   └── security/
└── config/                   # 설정 클래스
```

<br/>

## 🗄 ERD
전반적인 박람회 정보 관리 및 예약 정보 관리, 결제 정보 관리에 중점을 두고 설계하였습니다.

<img width="4350" height="2242" alt="02 1조_MYCE_ERD" src="https://github.com/user-attachments/assets/10860eb5-0bb3-4ac4-800c-0937ea95e5f3" />

<br/>
<br/>


## 🔥 성능 최적화 테스트

### 1) Redis 도입으로 인한 실시간 채팅 성능 향상
MYCE 채팅 시스템에서는 MongoDB에 저장된 채팅 데이터의 빠른 조회와 전송을 위해 **Redis 캐싱 시스템을 도입**했습니다.   
Redis는 메모리 기반 Key-Value 저장소로 디스크 I/O 지연 없이 빠른 응답이 가능하여, **실시간성이 핵심인 채팅 서비스에 최적화된 솔루션**이라고 판단했습니다. 
실제 성능 테스트를 진행한 결과, MongoDB 대비 Redis를 활용한 캐싱 시스템에서 현저한 성능 향상을 확인할 수 있었습니다.  
- **메시지 로딩: 1,215.60ms → 492.70ms (59.4% 향상)**
- **메시지 전송: 201.74ms → 104.40ms (48.2% 향상)**
<img width="1494" alt="image" src="https://github.com/user-attachments/assets/26cbd951-28ac-4c9a-90c1-87fe268dc3d2" />

### 2) 안정적인 서비스를 위한 로드밸런싱 및 스케일링 성능 테스트
인기 박람회 예매를 위해 **몰리는 트래픽에도 안정적인 서비스를 구축**하기 위해 로드 밸런싱과 스케일링 테스트를 진행했습니다.  
분산은 **ALB**(아마존에서 제공하는 로드 밸런서)로 스케일링은 **EKS**(아마존 쿠버네티스 서비스)를 사용하였으며, ALB를 사용하여 사용자들이 서버에 접근하는 여러가지 트래픽 부하 테스트를 진행하며 **프로메테우스**와 **그라파나**로 모니터링을 진행하였습니다.  
그 결과, **800명이 도메인에 접속하는 테스트**를 진행해도 오류나 지연 없이 서버가 잘 유지되는 것을 확인할 수 있었습니다.  

<img width="1494" height="927" alt="image" src="https://github.com/user-attachments/assets/d385cb82-8c0c-45d6-a62f-8c40ab12901b" />


