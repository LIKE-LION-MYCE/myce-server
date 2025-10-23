# QueryDSL 패키지

## 📦 패키지 구조

```
com.myce.querydsl/
├── controller/
│   └── QueryDslSearchController.java   # QueryDSL 검색 API 엔드포인트
├── service/
│   ├── QueryDslSearchService.java      # QueryDSL 검색 서비스 인터페이스
│   └── impl/
│       └── QueryDslSearchServiceImpl.java  # QueryDSL 검색 비즈니스 로직 구현
└── README.md                            # 이 파일

com.myce.expo/ (기존 패키지 재사용)
├── repository/
│   ├── ReviewRepository.java           # JPA + QueryDSL 통합 Repository
│   ├── CustomReviewRepository.java     # QueryDSL 메소드 정의
│   └── ReviewRepositoryImpl.java       # QueryDSL 쿼리 실제 구현
└── dto/
    ├── ReviewResponse.java             # 리뷰 단건 응답 DTO (재사용)
    └── ReviewListResponse.java         # 리뷰 목록 응답 DTO (재사용)
```

**설계 결정:**
- ✅ Controller, Service는 `querydsl` 패키지에 분리 → 검색 API 독립성
- ✅ Repository, DTO는 `expo` 패키지 재사용 → 중복 코드 방지

---

## 🎯 목적

**복잡한 동적 쿼리**를 처리하기 위한 QueryDSL 전용 패키지

- 일반적인 CRUD는 기존 Repository 사용
- **동적 조건이 필요한 검색 기능**만 이 패키지에서 처리

---

## 🚀 사용법

### API 엔드포인트

```http
GET /api/querydsl/search/reviews?expoId=1&minRating=4&keyword=좋았어요
```

### 파라미터

| 파라미터 | 필수 | 설명 | 예시 |
|----------|------|------|------|
| `expoId` | ✅ | 박람회 ID | `1` |
| `minRating` | ❌ | 최소 평점 (1~5) | `4` |
| `keyword` | ❌ | 검색 키워드 (제목/내용) | `좋았어요` |

### 예시 요청

```bash
# 1. 박람회 1번의 모든 리뷰
curl "http://localhost:8080/api/querydsl/search/reviews?expoId=1"

# 2. 평점 4점 이상 리뷰만
curl "http://localhost:8080/api/querydsl/search/reviews?expoId=1&minRating=4"

# 3. "좋았어요" 키워드 포함 리뷰
curl "http://localhost:8080/api/querydsl/search/reviews?expoId=1&keyword=좋았어요"

# 4. 복합 조건 (평점 4점 이상 + 키워드)
curl "http://localhost:8080/api/querydsl/search/reviews?expoId=1&minRating=4&keyword=추천"
```

---

## 🔄 QueryDSL 전체 플로우 (Repository → Controller)

### 1️⃣ Repository 계층 (쿼리 실행)

```
HTTP 요청
    ↓
Controller (파라미터 바인딩)
    ↓
Service (비즈니스 로직)
    ↓
ReviewRepository.searchReviews() ← CustomReviewRepository 인터페이스 메소드
    ↓
ReviewRepositoryImpl ← QueryDSL 구현체 (Spring이 자동 감지)
    ↓
JPAQueryFactory.selectFrom(QReview) ← Q클래스 사용
    ↓
WHERE 조건 동적 생성 (BooleanExpression)
    ↓
Database SQL 실행
    ↓
List<Review> 반환
    ↓
Service에서 DTO 변환
    ↓
Controller에서 JSON 응답
```

### 2️⃣ 핵심 동작 원리

**CustomReviewRepository + ReviewRepositoryImpl 패턴:**
```java
// 1. 인터페이스 정의
public interface CustomReviewRepository {
    List<Review> searchReviews(Long expoId, Integer minRating, String keyword);
}

// 2. 구현체 (Impl 네이밍 필수!)
@Repository
public class ReviewRepositoryImpl implements CustomReviewRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Review> searchReviews(...) {
        return queryFactory.selectFrom(QReview.review)
            .where(
                expoIdEq(expoId),      // null이면 조건 무시
                ratingGoe(minRating),  // null이면 조건 무시
                keywordContains(keyword) // null이면 조건 무시
            )
            .fetch();
    }
}

// 3. Spring Data JPA가 자동 통합
public interface ReviewRepository extends JpaRepository<Review, Long>,
                                         CustomReviewRepository {
    // JPA 메소드 + QueryDSL 메소드 모두 사용 가능!
}
```

**동적 조건 처리 (BooleanExpression):**
```java
private BooleanExpression expoIdEq(Long expoId) {
    return expoId != null ? review.expo.id.eq(expoId) : null;
    // null 반환 → QueryDSL이 자동으로 WHERE절에서 제외
}
```

### 3️⃣ 실제 데이터 흐름 예시

```
요청: GET /api/querydsl/search/reviews?expoId=1&minRating=4&keyword=좋았어요

1. Controller 파라미터 받기
   expoId = 1, minRating = 4, keyword = "좋았어요"

2. Service 호출
   searchService.searchReviews(1, 4, "좋았어요")

3. Repository (QueryDSL) 실행
   WHERE expo_id = 1
     AND rating >= 4
     AND (title LIKE '%좋았어요%' OR content LIKE '%좋았어요%')
   ORDER BY created_at DESC

4. Database → List<Review> 조회

5. Service에서 Entity → DTO 변환
   reviews.stream().map(ReviewResponse::new).toList()

6. 평균 평점 추가
   response.setAverageRating(reviewRepository.findAverageRatingByExpoId(1))

7. Controller → JSON 응답
   {
     "reviews": [...],
     "averageRating": 4.5,
     "totalElements": 15
   }
```

---

## 💡 QueryDSL 동작 원리

### 동적 쿼리란?

파라미터가 **null이면 해당 조건을 자동으로 제외**하는 쿼리

```java
// expoId=1, minRating=null, keyword=null
→ WHERE expo_id = 1

// expoId=1, minRating=4, keyword=null
→ WHERE expo_id = 1 AND rating >= 4

// expoId=1, minRating=4, keyword="좋았어요"
→ WHERE expo_id = 1
  AND rating >= 4
  AND (title LIKE '%좋았어요%' OR content LIKE '%좋았어요%')
```

### 구현 위치

실제 QueryDSL 구현은 **Repository 계층**에 있습니다:

- `CustomReviewRepository` (인터페이스)
- `ReviewRepositoryImpl` (구현체) - 실제 QueryDSL 코드

---

## 🔧 확장 가능성

다른 검색 기능도 쉽게 추가할 수 있습니다:

```java
// QueryDslSearchController.java
@GetMapping("/expos")
public ResponseEntity<ExpoListResponse> searchExpos(...) {
    // 박람회 동적 검색
}

@GetMapping("/booths")
public ResponseEntity<BoothListResponse> searchBooths(...) {
    // 부스 동적 검색
}
```

---

## 📚 참고 자료

### QueryDSL 관련 파일

1. **설정**
   - `build.gradle` - QueryDSL 의존성
   - `QueryDslConfig.java` - JPAQueryFactory 빈 등록

2. **Repository 계층** (실제 쿼리 구현)
   - `com.myce.expo.repository.CustomReviewRepository`
   - `com.myce.expo.repository.ReviewRepositoryImpl`

3. **Q클래스** (자동 생성)
   - `build/generated/sources/annotationProcessor/java/main/com/myce/expo/entity/QReview.java`
   - 
## ⚠️ 주의사항

1. **기존 API와 분리**
   - 일반 리뷰 CRUD: `/api/reviews/**`
   - QueryDSL 검색: `/api/querydsl/search/**`

2. **Security 설정**
   - 비회원도 접근 가능하도록 설정됨 (`SecurityConfig.java` 참고)

3. **팀원 공유**
   - 이 패키지는 **QueryDSL 학습 및 데모용**입니다
   - 실제 프로젝트에서는 필요에 따라 통합하거나 분리 사용
