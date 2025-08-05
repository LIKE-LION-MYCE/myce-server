# MYCE 이미지 업로드 시스템 - 팀원 사용 가이드

## 🚀 시스템 배포 완료!

## 📋 팀원들이 알아야 할 핵심 사항

### 🎯 가장 중요한 규칙

```javascript
// ❌ 절대 하지 마세요
const { uploadUrl } = response.data;
setExpo({posterUrl: uploadUrl}); // 15분 후 만료됨!

// ✅ 항상 이렇게 하세요  
const { cdnUrl } = response.data;
setExpo({posterUrl: cdnUrl}); // 영구 사용 가능
```

**핵심:** DB에는 항상 **CDN URL**만 저장하세요!

---

## 💻 실제 사용 방법

### 1. 컴포넌트 Import

```javascript
import ImageUpload from '../common/commponents/imageUpload/ImageUpload';
```

### 2. 기본 사용 패턴

```javascript
const MyForm = () => {
  const [formData, setFormData] = useState({
    title: '',
    imageUrl: '' // CDN URL 저장용
  });

  // 이미지 업로드 성공 시
  const handleImageSuccess = (cdnUrl) => {
    setFormData(prev => ({
      ...prev,
      imageUrl: cdnUrl // 중요: CDN URL 저장
    }));
  };

  // 이미지 업로드 실패 시
  const handleImageError = (error) => {
    console.error('업로드 실패:', error);
    alert(`이미지 업로드 실패: ${error}`);
  };

  // 폼 제출
  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // 이미지 업로드 확인
    if (!formData.imageUrl) {
      alert('이미지를 업로드해주세요.');
      return;
    }

    // 서버에 데이터 전송
    await fetch('/api/your-endpoint', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({
        title: formData.title,
        imageUrl: formData.imageUrl // CDN URL 전송
      })
    });
  };

  return (
    <form onSubmit={handleSubmit}>
      <input 
        value={formData.title}
        onChange={(e) => setFormData(prev => ({...prev, title: e.target.value}))}
        placeholder="제목 입력"
      />
      
      <ImageUpload
        onUploadSuccess={handleImageSuccess}
        onUploadError={handleImageError}
      />
      
      {/* 업로드된 이미지 미리보기 */}
      {formData.imageUrl && (
        <div>
          <p>✅ 이미지 업로드 완료</p>
          <img 
            src={formData.imageUrl} 
            alt="미리보기"
            style={{maxWidth: '200px', maxHeight: '200px'}}
          />
        </div>
      )}
      
      <button type="submit">저장</button>
    </form>
  );
};
```

### 3. 이미지 조회 패턴

```javascript
const MyList = () => {
  const [items, setItems] = useState([]);

  useEffect(() => {
    fetch('/api/your-endpoint')
      .then(res => res.json())
      .then(data => setItems(data));
  }, []);

  return (
    <div>
      {items.map(item => (
        <div key={item.id}>
          <h3>{item.title}</h3>
          {/* CDN URL로 이미지 표시 */}
          <img 
            src={item.imageUrl}
            alt={item.title}
            onError={(e) => {
              e.target.src = '/images/placeholder.png'; // 대체 이미지
            }}
            style={{width: '200px', height: '150px', objectFit: 'cover'}}
          />
        </div>
      ))}
    </div>
  );
};
```

---

## 🔧 백엔드 Entity 설정

### JPA Entity 실제 예시

**Expo Entity (박람회 포스터):**
```java
@Entity
@Table(name = "expo")
public class Expo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expo_id")
    private Long id;
    
    @Column(name = "expo_title", length = 100, nullable = false)
    private String expoTitle;
    
    @Column(name = "thumbnail_url", length = 500, nullable = false)
    private String thumbnailUrl; // ← CDN URL 저장 (기존 필드 활용)
    
    // 기타 필드들...
}
```

**Advertisement Entity (광고 이미지):**
```java
@Entity  
@Table(name = "advertisement")
public class Advertisement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "advertisement_id")
    private Long id;
    
    @Column(name = "title", length = 100, nullable = false)
    private String title;
    
    @Column(name = "image_url", length = 500, nullable = false) 
    private String imageUrl; // ← CDN URL 저장 (기존 필드 활용)
    
    // 기타 필드들...
}
```

**Member Entity (프로필 이미지 - 추가 필요):**
```java
@Entity
@Table(name = "member") 
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;
    
    @Column(name = "name", length = 10, nullable = false)
    private String name;
    
    // 프로필 이미지 필드 추가 필요
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl; // ← 새로 추가할 필드
    
    // 기타 필드들...
}
```

### DTO 예시

```java
public class ExpoCreateRequest {
    private String name;
    private String posterUrl; // 프론트엔드에서 CDN URL 전송
    
    // getters, setters...
}

public class ExpoResponse {
    private Long id;
    private String name;
    private String posterUrl; // CDN URL 반환
    
    // getters, setters...
}
```

---

## 📱 사용 가능한 곳들

### 현재 적용 가능한 페이지들

1. **박람회 등록** (`/expo-apply`)
   - 포스터 이미지 업로드
   - 갤러리 이미지 업로드

2. **광고 등록** (`/ad-apply`)  
   - 광고 이미지 업로드

3. **마이페이지** 
   - 프로필 이미지 업로드

4. **관리자 페이지**
   - 배너 이미지 업로드
   - 공지사항 이미지 업로드

---

## ⚠️ 주의사항 및 에러 처리

### 1. 파일 제한사항

```javascript
// ImageUpload 컴포넌트가 자동으로 검증하는 항목들
- 파일 크기: 10MB 이하
- 파일 타입: JPEG, PNG, GIF, WebP만 허용
- 파일 개수: 한 번에 1개씩만
```

### 2. 에러 처리 패턴

```javascript
const handleImageError = (error) => {
  console.error('이미지 업로드 에러:', error);
  
  // 사용자 친화적 메시지 표시
  if (error.includes('크기')) {
    alert('파일 크기가 너무 큽니다. 10MB 이하로 선택해주세요.');
  } else if (error.includes('형식')) {
    alert('지원하지 않는 파일 형식입니다. JPEG, PNG, GIF, WebP 파일만 가능합니다.');
  } else {
    alert('이미지 업로드에 실패했습니다. 다시 시도해주세요.');
  }
};
```

### 3. 로딩 상태 처리

```javascript
const [uploading, setUploading] = useState(false);

const handleImageSuccess = (cdnUrl) => {
  setUploading(false);
  setFormData(prev => ({...prev, imageUrl: cdnUrl}));
};

const handleImageError = (error) => {
  setUploading(false);
  // 에러 처리...
};

// ImageUpload 컴포넌트는 내부적으로 로딩 상태를 관리하지만,
// 필요시 외부에서도 상태 관리 가능
```

---

## 🧪 테스트 방법

### 개발 환경에서 테스트

```bash
# 1. 백엔드 실행
cd myce-server
./gradlew bootRun

# 2. 프론트엔드 실행
cd myce-client  
npm run dev

# 3. 브라우저에서 확인
http://localhost:5173
```

### 테스트 체크리스트

- [ ] 이미지 파일 선택 시 미리보기 표시
- [ ] 드래그 앤 드롭 동작 확인
- [ ] 큰 파일(10MB 초과) 업로드 시 에러 메시지 
- [ ] 잘못된 파일 형식 업로드 시 에러 메시지
- [ ] 업로드 완료 후 CDN URL 정상 생성
- [ ] 새로고침 후에도 이미지 정상 표시
- [ ] 모바일에서도 정상 동작

---

## 🔗 관련 파일 위치

### 프론트엔드 주요 파일

```
myce-client/
├── src/common/commponents/imageUpload/
│   ├── ImageUpload.jsx          # 메인 컴포넌트
│   └── ImageUpload.module.css   # 스타일
```

### 백엔드 주요 파일

```
myce-server/
├── src/main/java/com/myce/common/
│   ├── controller/ImageController.java  # Presigned URL API
│   └── config/S3Config.java            # AWS S3 설정
└── src/main/resources/
    └── application.yml                  # 환경 설정
```

---

## 🚀 배포 후 해야 할 일

### 1. PR 생성 및 코드 리뷰

**백엔드 PR:** https://github.com/LIKE-LION-MYCE/myce-server/pull/new/feature/backend-s3-image-upload

**프론트엔드 PR:** https://github.com/LIKE-LION-MYCE/myce-client/pull/new/feature/frontend-s3-image-upload

### 2. develop 브랜치 merge 후

```bash
# 최신 develop pull
git checkout develop
git pull origin develop

# 기존 feature 브랜치 삭제
git branch -d feature/frontend-s3-image-upload
git branch -d feature/backend-s3-image-upload
```

### 3. 실제 페이지에 적용

기존 파일들을 수정해서 ImageUpload 컴포넌트를 사용하도록 변경:

```javascript
// 예: ExpoApply.jsx 수정
import ImageUpload from '../common/commponents/imageUpload/ImageUpload';

// 기존 파일 input을 ImageUpload로 교체
<ImageUpload 
  onUploadSuccess={handlePosterUpload}
  onUploadError={handleUploadError}
/>
```

---

## 💡 팁 및 모범 사례

### 1. State 구조 권장사항

```javascript
// ✅ 좋은 구조
const [formData, setFormData] = useState({
  title: '',
  description: '',
  posterUrl: '',        // 메인 이미지
  galleryUrls: []       // 여러 이미지 (필요시)
});

// ❌ 피해야 할 구조  
const [posterFile, setPosterFile] = useState(null);     // 파일 객체 저장 X
const [uploadUrl, setUploadUrl] = useState('');         // 임시 URL 저장 X
```

### 2. 이미지 표시 모범 사례

```javascript
// 반응형 이미지 표시
<img 
  src={imageUrl}
  alt="설명"
  style={{
    width: '100%',
    maxWidth: '400px', 
    height: 'auto',
    objectFit: 'cover',
    borderRadius: '8px'
  }}
  onError={(e) => {
    e.target.src = '/images/placeholder.png';
  }}
/>
```

### 3. 에러 경계 설정

```javascript
// 이미지 로드 실패 시 대체 UI
{imageUrl ? (
  <img src={imageUrl} alt="이미지" onError={handleImageError} />
) : (
  <div className="image-placeholder">
    이미지가 없습니다
  </div>
)}
```

---

## 🔧 문제 해결

### 자주 발생하는 문제들

**Q: 이미지 업로드 후 바로 보이지 않아요**
A: CDN 캐시 때문일 수 있습니다. 몇 초 후 다시 시도해보세요.

**Q: CORS 에러가 발생해요**  
A: S3 버킷 CORS 설정이 필요합니다. 정환님에게 문의하세요.

**Q: 업로드가 너무 느려요**
A: 파일 크기를 확인하고, 필요시 압축 후 업로드하세요.

**Q: 개발환경에서 이미지가 안 보여요**
A: 백엔드 서버가 실행 중인지 확인하세요 (`localhost:8080`).



**핵심만 기억하세요: Upload URL로 업로드하고, CDN URL을 DB에 저장!** 🚀