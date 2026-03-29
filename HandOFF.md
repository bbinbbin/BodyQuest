
# BodyQuest Handoff Document

> 마지막 업데이트: 2026-03-29 (프로필 사진 기능 + UI 개선 + 로그인 에러 메시지 개선)
> 이 문서를 읽고 프로젝트 현재 상태를 파악한 뒤, 다음 작업을 이어서 진행하면 됩니다.

---

## 프로젝트 개요

운동을 RPG 퀘스트로 수행하여 캐릭터를 성장시키는 Android 피트니스 앱.
실제 운동 데이터(InBody 등)만으로 스탯을 반영하며, 자기 입력 스탯은 없음.

- **GitHub**: https://github.com/bbinbbin/BodyQuest
- **패키지**: `com.bodyquest.app`
- **minSdk**: 24 / **targetSdk**: 36 / **compileSdk**: 36

---

## 기술 스택 & 버전

| 항목 | 버전 |
|------|------|
| AGP | 8.9.1 |
| Kotlin | 2.1.20 |
| KSP | 2.1.20-1.0.32 |
| Compose BOM | 2024.09.00 |
| Room | 2.7.1 |
| Navigation Compose | 2.9.0 |
| Lifecycle/ViewModel | 2.10.0 |
| Hilt | 2.56.2 |
| Hilt Navigation Compose | 1.2.0 |
| Security Crypto | 1.1.0-alpha06 |
| Firebase BOM | 33.9.0 |
| Firebase Firestore | (BOM 관리) |
| Credential Manager | 1.5.0 |
| Google Identity | 1.1.1 |
| Gradle | 9.3.1 |

- **DI**: Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- **인증**: Firebase Auth (이메일/비밀번호 + Google Sign-In)
- **클라우드 DB**: Firebase Firestore (유저 프로필 + 운동 기록 동기화)
- **로컬 DB**: Room v7 (UI 단일 데이터 소스, Firestore는 클라우드 백업/동기화)
- **이미지 로딩**: Coil 2.6.0 (Compose용)
- **테마**: 항상 다크 모드 (다이나믹 컬러 없음)
- **보안**: network_security_config (HTTPS 강제), EncryptedSharedPreferences 준비 완료

---

## 프로젝트 구조

```
app/src/main/java/com/bodyquest/app/
├── BodyQuestApp.kt              # @HiltAndroidApp Application
├── MainActivity.kt              # @AndroidEntryPoint, BodyQuestNavGraph 호출
│
├── di/
│   ├── DatabaseModule.kt        # @Module: DB, DAO, Repository 제공 (interface → Local 구현체)
│   ├── AuthModule.kt            # @Module: FirebaseAuth, AuthRepository 제공
│   ├── FirestoreModule.kt       # @Module: Firestore, FirestoreUserService, SyncManager 제공
│   └── SecurityModule.kt        # @Module: EncryptedSharedPreferences 제공
│
├── data/
│   ├── local/
│   │   ├── BodyQuestDatabase.kt # Room DB (v7), exportSchema=true, Migration(1,2)~Migration(6,7)
│   │   ├── SeedData.kt          # ~20개 하드코딩 퀘스트 (근력/지구력/밸런스)
│   │   ├── dao/
│   │   │   ├── UserDao.kt       # abstract class, getUser(uid), @Transaction applyWorkoutRewards(), updateProfileImageUrl()
│   │   │   ├── QuestDao.kt      # 카테고리/부위/난이도 필터링
│   │   │   └── WorkoutDao.kt    # 운동 기록 CRUD, 시간 기반 쿼리, getWorkoutByFirestoreId()
│   │   └── entity/
│   │       ├── UserEntity.kt    # id, nickname, job, goal, avatarIndex, stats, xp, level, firebaseUid, email, authProvider, profileImageUrl, updatedAt
│   │       ├── QuestEntity.kt   # id, category, bodyPart, name, difficulty, rewards
│   │       ├── WorkoutEntity.kt # FK(userId→users, questId→quests), 복합인덱스, CASCADE, firestoreId
│   │       └── WorkoutSetEntity.kt # FK(workoutId→workouts), CASCADE
│   ├── remote/
│   │   ├── FirestoreUserService.kt # Firestore CRUD: pushUser, pullUser, pushWorkout, pullAllWorkouts, deleteUser, isNicknameTaken
│   │   └── SyncManager.kt         # 동기화 오케스트레이션: syncOnLogin, pushUserToCloud, pushCompletedWorkout
│   └── repository/
│       ├── AuthRepository.kt        # interface: 인증 추상화 (deleteAccount 포함)
│       ├── FirebaseAuthRepository.kt # Firebase Auth 구현체, 한국어 에러 매핑
│       ├── UserRepository.kt        # interface (getUser(uid), deleteUserByFirebaseUid)
│       ├── LocalUserRepository.kt   # 로컬 구현체
│       ├── QuestRepository.kt       # interface
│       ├── LocalQuestRepository.kt  # 로컬 구현체
│       ├── WorkoutRepository.kt     # interface (getSetsForWorkoutOnce 포함)
│       └── LocalWorkoutRepository.kt # 로컬 구현체
│
├── domain/model/
│   ├── AuthResult.kt              # sealed class: Success/Error
│   ├── Job.kt                     # enum: STRENGTH/ENDURANCE/BALANCE
│   ├── Goal.kt                    # enum: DIET/BULK_UP/MAINTAIN
│   └── StatType.kt                # enum: STRENGTH/ENDURANCE/BALANCE
│
├── ui/
│   ├── common/
│   │   ├── UiState.kt           # sealed interface: Loading/Success/Error
│   │   └── CommonUi.kt          # LoadingScreen, ErrorScreen 공통 컴포넌트
│   │
│   ├── splash/
│   │   ├── SplashScreen.kt      # "시작하기" 버튼 → Intro/Login/Onboarding/Home 4분기
│   │   └── SplashViewModel.kt   # @HiltViewModel, has_logged_in 체크 → 미로그인 기기면 Intro로
│   │
│   ├── intro/
│   │   └── IntroScreen.kt       # HorizontalPager 5장 슬라이드, dot 인디케이터, 다음/시작하기/건너뛰기
│   │
│   ├── login/
│   │   ├── LoginScreen.kt       # 이메일/비밀번호 입력, Google 버튼, 모드 전환(로그인↔회원가입)
│   │   └── LoginViewModel.kt    # @HiltViewModel, 로그인 시 syncOnLogin() + has_logged_in=true 저장
│   │
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt      # 3스텝 (직업→목표→아바타), 에러 메시지 표시
│   │   ├── OnboardingViewModel.kt   # @HiltViewModel, 닉네임 중복 체크 + Firestore push
│   │   ├── JobSelectionPage.kt
│   │   ├── GoalSelectionPage.kt
│   │   └── AvatarCreationPage.kt    # 닉네임 입력 + 남성/여성 아바타 이미지 카드 선택 (avatarIndex: 0=남성, 1=여성)
│   │
│   ├── home/
│   │   ├── HomeScreen.kt           # UiState 분기, 프로필 사진 갤러리/카메라 선택, ActivityResultContracts
│   │   ├── HomeViewModel.kt        # @HiltViewModel, 프로필 사진 업로드 (Base64), @ApplicationContext
│   │   └── components/
│   │       ├── StatBar.kt           # 테두리 있는 프로그레스 바
│   │       ├── XpProgressBar.kt     # 테두리 있는 XP 프로그레스 바
│   │       ├── TodayQuestCard.kt
│   │       ├── ProfileImage.kt      # 원형 프로필 사진 (Base64 디코딩, 아바타 fallback, 카메라 아이콘)
│   │       └── ImagePickerSheet.kt  # 갤러리/카메라 선택 BottomSheet
│   │
│   ├── quest/
│   │   ├── QuestScreen.kt          # 카테고리 선택
│   │   ├── QuestTreeScreen.kt      # UiState 분기, 부위→퀘스트 드릴다운
│   │   ├── QuestDetailScreen.kt    # UiState 분기, 퀘스트 상세
│   │   ├── QuestViewModel.kt       # @HiltViewModel, UiState<QuestTreeState>
│   │   └── QuestDetailViewModel.kt # @HiltViewModel, UiState<QuestEntity>
│   │
│   ├── workout/
│   │   ├── WorkoutScreen.kt
│   │   ├── WorkoutCompleteScreen.kt
│   │   └── WorkoutViewModel.kt     # @HiltViewModel, 운동 완료 시 Firestore push (workout + user)
│   │
│   ├── pvp/PvpScreen.kt            # Coming Soon
│   ├── avatar/AvatarScreen.kt      # 아바타 이미지 전신 표시 + 드래그 좌우 회전 (rotationY, ±75도, 스프링 복귀)
│   ├── profile/
│   │   ├── ProfileScreen.kt        # 로그아웃 + 계정 삭제 (확인 다이얼로그, 로딩/에러 상태)
│   │   └── ProfileViewModel.kt     # @HiltViewModel, signOut(), deleteAccount() (Firestore→Room→Auth 순서)
│   │
│   ├── navigation/
│   │   ├── Screen.kt               # sealed class: 모든 라우트 (Splash, Intro, Login, Onboarding, Home, ...)
│   │   ├── BottomNavBar.kt         # 5탭
│   │   └── BodyQuestNavGraph.kt    # hiltViewModel() 사용, Intro/Login 라우트 포함
│   │
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── util/
    ├── XpCalculator.kt
    └── ImageUtil.kt             # 이미지 압축(512x512 JPEG), 카메라 임시 URI 생성
```

---

## 핵심 설계 원칙

1. **레벨/XP ≠ 스탯**: XP는 운동 수행으로 쌓이고, 스탯은 실측 데이터(InBody 등)로만 변경
2. **자기 입력 스탯 금지**: 온보딩에서 스탯 입력 단계 제거 (초기값 0)
3. **3탭 규칙**: 퀘스트 접근은 카테고리 → 부위 → 퀘스트 최대 3탭
4. **매번 로그인**: 자동 로그인 없음, 앱 실행 시마다 로그인 필수
5. **심박수/칼로리는 시뮬레이션**: 현재 실제 센서 연동 없음
6. **Room = UI 단일 데이터 소스**: Firestore는 클라우드 백업/동기화 채널, 클라우드 실패해도 로컬 동작에 영향 없음

---

## 인증 시스템 (Firebase Auth)

### 앱 플로우
```
Splash ("시작하기")
  → [한 번도 로그인 안 한 기기] Intro (5장 슬라이드) → Login
  → [로그인 이력 있는 기기] Login
Login → [신규] Onboarding → Home
      → [기존/클라우드 복원] Home
Profile → 로그아웃 → Login
Profile → 계정 삭제 → Login (Firestore + Room + Auth 모두 삭제)
```

### 인트로 화면 조건
- `EncryptedSharedPreferences`의 `has_logged_in` 키로 판단
- 로그인 성공 시(`handleAuthSuccess`) `has_logged_in = true` 저장
- 인트로 중 앱 종료 후 재실행 → 인트로 다시 표시 (로그인 전까지 반복)
- 이미지: `res/drawable/intro_1.png` ~ `intro_5.png`

### 지원 로그인 방식
- **이메일/비밀번호**: 가입, 로그인, 비밀번호 찾기
- **Google Sign-In**: Credential Manager API 사용 (기기에 Google 계정 필요)

### 주요 동작
- 회원가입 완료 → "회원가입이 완료되었습니다!" 메시지 → 로그인 화면으로 전환 (자동 로그인 안됨)
- 로그인 ↔ 회원가입 전환 시 입력 필드 전체 초기화 + 포커스 이메일로 이동
- Firebase 에러 메시지 전부 한국어로 매핑
- 로그아웃: Profile 탭에서 가능, 전체 백스택 클리어 후 Login으로
- 계정 삭제: Profile 탭에서 가능, 확인 다이얼로그 → Firestore/Room/Auth 순서 삭제

### Firebase 설정
- **프로젝트**: bodyquest-ce5ab
- **google-services.json**: `app/` 폴더에 위치
- **Web Client ID**: `strings.xml`의 `default_web_client_id`에 저장
- **활성화된 제공자**: Email/Password, Google
- **Firestore**: Cloud Firestore 활성화 (asia-northeast3)

### Firestore 보안 규칙
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
      match /workouts/{workoutId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```
- users 읽기: 인증된 모든 유저 (닉네임 중복 체크용)
- users 쓰기: 본인만
- workouts 읽기/쓰기: 본인만

### DB 연동
- `UserEntity`에 `firebaseUid`, `email`, `authProvider`, `updatedAt` 필드
- `WorkoutEntity`에 `firestoreId` 필드 (클라우드 동기화 중복 방지)
- `firebaseUid`에 UNIQUE 인덱스
- 로그인 성공 → `syncOnLogin()` → Firestore에서 데이터 pull → Room 저장 → 기존 유저 확인 → 없으면 Onboarding

---

## 클라우드 동기화 (Firestore)

### 동기화 전략: Write-Through + Pull-on-Login
- Room이 UI의 단일 데이터 소스, Firestore는 클라우드 백업
- 클라우드 실패 시에도 로컬 동작에 영향 없음 (모든 클라우드 호출 try/catch)

### Firestore 데이터 구조
```
users/{firebaseUid}
  ├── nickname, job, goal, avatarIndex, profileImageUrl (Base64)
  ├── strengthStat, enduranceStat
  ├── xp, level, createdAt, updatedAt, email, authProvider
  │
  └── workouts/{firestoreWorkoutId}
        ├── questId, startTime, endTime, elapsedSeconds
        ├── caloriesBurned, heartRateAvg, completed, xpEarned
        └── sets: [ {setNumber, reps, completed, completedAt} ]  ← 배열
```

### Push 시점 (로컬 → 클라우드)
- 온보딩 완료 (프로필 생성) → `pushUserToCloud()`
- 운동 완료 → `pushCompletedWorkout()` + `pushUserToCloud()`

### Pull 시점 (클라우드 → 로컬)
- 로그인 시 로컬에 유저 없으면 → Firestore에서 유저 + 전체 운동 기록 pull
- 로그인 시 로컬에 유저 있지만 클라우드가 더 최신(updatedAt 비교) → 유저 데이터 업데이트 + 새 운동 기록 pull
- 중복 방지: `firestoreId`로 이미 동기화된 운동 건너뜀

---

## 완료된 아키텍처 리팩토링 (Phase 1~10)

### Phase 1: Hilt DI 도입 ✅
- 수동 DI → Hilt 마이그레이션
- `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`
- DatabaseModule로 DB/DAO/Repository 제공
- NavGraph에서 repository 파라미터 제거, `hiltViewModel()` 사용
- SplashViewModel, QuestDetailViewModel 신규 생성

### Phase 2: XP Race Condition 수정 ✅
- `addXp()` + `updateLevel()` + `updateUser()` 6개 호출 → `applyWorkoutRewards()` 1개로 통합
- UserDao에 `@Transaction` 메서드 추가 (XP+레벨+스탯 원자적 업데이트)
- `userId = 0` 하드코딩 제거

### Phase 3: Sealed UI State + 에러 핸들링 ✅
- `UiState<T>` sealed interface (Loading/Success/Error)
- LoadingScreen, ErrorScreen 공통 컴포넌트
- HomeViewModel: 중첩 collectLatest → `combine()` 수정
- 4개 ViewModel + Screen에 Loading/Error/Success 분기 적용
- WorkoutViewModel `finishWorkout()`에 try-catch

### Phase 4: DB 스키마 개선 ✅
- WorkoutEntity: `userId+completed+startTime` 복합 인덱스 추가
- WorkoutEntity: `userId→users`, `questId→quests` FK 제약 + CASCADE
- DB version 1→2, `exportSchema=true`, Migration(1,2) (테이블 재생성 방식)
- `room.schemaLocation` KSP 인자 설정

### Phase 5: 보안 기반 구축 ✅
- `network_security_config.xml`: cleartext traffic 차단 (HTTPS 강제)
- Repository 3개를 interface로 추출 + Local 구현체 분리 (향후 Remote 대비)
- EncryptedSharedPreferences 제공 SecurityModule 추가

### Phase 6: Dead Code 제거 ✅
- `StatInputPage.kt` 삭제
- OnboardingViewModel dead stat setter 3개 + state 필드 제거
- seed callback 코루틴 스코프 개선 (SupervisorJob)

### Phase 7: 프로덕션 빌드 설정 ✅
- `isMinifyEnabled=true`, `isShrinkResources=true` (release)
- ProGuard 규칙: Room entity, Hilt, Compose, Tink 보존
- `fallbackToDestructiveMigration` 제거
- APK 크기: 18.5MB → 6.7MB (64% 감소)

### Phase 8: Firebase Auth 도입 ✅
- Firebase Auth (이메일/비밀번호 + Google Sign-In) 도입
- AuthRepository 인터페이스 + FirebaseAuthRepository 구현체
- AuthModule (Hilt DI) 추가
- UserEntity에 firebaseUid, email, authProvider 필드 추가 (DB v2→v3)
- LoginScreen + LoginViewModel 신규 생성
- SplashViewModel: 매 실행 시 로그인 필수
- ProfileScreen: 로그아웃 버튼 추가
- ProGuard 규칙: Firebase, Credential Manager 보존

### Phase 9: 버그 수정 + 계정 관리 ✅
- `getUser()`/`getUserOnce()` Firebase UID 기반 조회로 변경 (계정별 데이터 분리)
- 스플래시 화면 "퀸스트" → "퀘스트" 오타 수정 (유니코드 이스케이프)
- 계정 삭제 기능 추가 (Profile 화면, 확인 다이얼로그)
- 삭제 순서: Firestore → Room → Firebase Auth (권한 문제 방지)
- DB v3→v4 + fallbackToDestructiveMigration (기존 데이터 초기화)

### Phase 11: 인트로 슬라이드 화면 + 버그 수정 ✅ (2026-03-28)
- `ui/intro/IntroScreen.kt` 신규 생성 (HorizontalPager 5장, dot 인디케이터, 다음/시작하기/건너뛰기)
- `Screen.kt`에 `Intro` 라우트 추가
- `SplashViewModel`: `has_logged_in` 키(EncryptedSharedPreferences) 기반 인트로 분기
- `LoginViewModel`: 로그인 성공 시 `has_logged_in = true` 저장 (인트로 재표시 방지)
- 인트로 표시 조건: 한 번도 로그인 안 한 기기, 로그인 성공 전까지 반복 표시
- drawable 파일명 수정: `1.png`~`5.png` → `intro_1.png`~`intro_5.png` (Android 리소스 규칙)
- **버그 수정** WorkoutScreen: `val quest = state.quest ?: return` → 로컬 변수 + null 체크로 변경 (빈 화면 → LoadingScreen)
- **버그 수정** BodyQuestDatabase: 퀘스트 씨드 삽입을 코루틴 비동기 → `db.execSQL` 동기 방식으로 변경, `onOpen`에서도 퀘스트 수 확인 후 없으면 재삽입 (레이스 컨디션 + 기존 기기 대응)

### Phase 12: 아바타 이미지 시스템 ✅ (2026-03-29)
- 온보딩 아바타 선택 UI 교체: 이모지 8개 → 남성/여성 실제 캐릭터 이미지 카드
- drawable 리소스 추가: `avatar_male.png`, `avatar_female.png` (Android 리소스명 규칙 적용)
- `avatarIndex` 의미 변경: 0 = 남성, 1 = 여성 (기존 0~7 이모지 인덱스 폐기)
- HomeScreen: 아바타 이미지 Box 제거 → 닉네임 + 직업 배지 + 목표 배지만 표시
- AvatarScreen: Coming Soon → 실제 아바타 화면으로 교체
  - 상단: 전신 아바타 이미지 (ContentScale.Fit)
  - 드래그 제스처로 좌우 회전 (rotationY ±75도, `cameraDistance`로 원근감)
  - 손 떼면 스프링 애니메이션으로 0도 복귀 (dampingRatio=0.6, stiffness=200)
  - 하단 카드: 닉네임 · 직업 · 목표 · 레벨 표시
- AvatarScreen이 `HomeViewModel`을 재사용 (별도 ViewModel 없음)
- NavGraph: `AvatarScreen()` → `AvatarScreen(viewModel = hiltViewModel())`

### Phase 13: UI 개선 + 로그인 에러 메시지 개선 ✅ (2026-03-29)
- 인트로 건너뛰기 버튼 우측 최상단으로 위치 조정 (statusBarsPadding 제거, top=0)
- 로그인 에러 매핑을 문자열 매칭 → Firebase 예외 클래스 기반(`FirebaseAuthInvalidUserException` 등)으로 변경
  - "가입되지 않은 이메일입니다." / "비밀번호가 올바르지 않습니다." 등 정확한 메시지
  - 로그 추가: `Log.e("FirebaseAuth", ...)` 로 실제 에러 클래스/메시지 출력
- 모든 에러/안내 메시지에 마침표(`.`) 통일
- 회원가입 완료 메시지 — 입력 시작하면 자동 제거 (`signUpCompleted = false`)
- XP/스탯 프로그레스 바에 `DarkBorder` 테두리 추가 (0일 때도 바 윤곽 표시)
- 온보딩 마지막 버튼 "시작하기!" → "시작하기" (느낌표 제거)

### Phase 14: 프로필 사진 기능 ✅ (2026-03-29)
- 홈 화면 유저 카드에 원형 프로필 사진 추가 (닉네임 위)
- 기본값: 온보딩에서 선택한 아바타 이미지(남성/여성)를 원형 crop으로 표시
- 프로필 사진 탭 → `ModalBottomSheet`으로 갤러리/카메라 선택
- **갤러리**: `ActivityResultContracts.GetContent("image/*")`
- **카메라**: `ActivityResultContracts.TakePicture` + `FileProvider` + 런타임 카메라 퍼미션
- 이미지 처리: `ImageUtil.compressAndResize()` — 512x512 center crop, JPEG 80% 압축
- **저장 방식: Base64** (Firebase Storage 유료 → Base64로 Room/Firestore 직접 저장)
  - `android.util.Base64.encodeToString(bytes, NO_WRAP)` → `profileImageUrl` 컬럼에 저장
  - 표시 시 `Base64.decode()` → `BitmapFactory.decodeByteArray()` → `Image(bitmap.asImageBitmap())`
- DB v6→v7 마이그레이션: `ALTER TABLE users ADD COLUMN profileImageUrl TEXT`
- `UserDao.updateProfileImageUrl(uid, url, updatedAt)` 쿼리 추가
- `UserRepository` + `LocalUserRepository`에 `updateProfileImageUrl()` 메서드 추가
- `FirestoreUserService`: push/pull에 `profileImageUrl` 필드 포함
- `SyncManager.syncOnLogin()`: 클라우드→로컬 동기화 시 `profileImageUrl` 포함
- `HomeViewModel`: `@ApplicationContext` 주입, `uploadProfileImage(uri)` 메서드
- `ProfileImage` 컴포넌트: Base64 디코딩, 아바타 fallback, 카메라 아이콘 오버레이
- `ImagePickerSheet`: 갤러리/카메라 선택 BottomSheet (Material3 ModalBottomSheet)
- `AndroidManifest.xml`: CAMERA 퍼미션 + FileProvider 추가
- `res/xml/file_paths.xml`: 카메라 임시 파일 경로
- Coil 2.6.0 의존성 추가 (현재 Base64 직접 디코딩이므로 AsyncImage 미사용, 향후 URL 방식 전환 대비)
- 에러 처리: 업로드 실패 시 프로필 아래 빨간 에러 메시지 3초 표시

### Phase 10: Firestore 클라우드 동기화 ✅
- Firebase Firestore 의존성 추가
- FirestoreUserService: Firestore CRUD (push/pull user, push/pull workouts, delete, isNicknameTaken)
- SyncManager: 동기화 오케스트레이션 (syncOnLogin, pushUserToCloud, pushCompletedWorkout)
- FirestoreModule (Hilt DI)
- UserEntity에 updatedAt, WorkoutEntity에 firestoreId 필드 추가 (DB v4→v5)
- LoginViewModel: 로그인 시 syncOnLogin() 호출
- OnboardingViewModel: 프로필 생성 시 Firestore push + 닉네임 중복 체크
- WorkoutViewModel: 운동 완료 시 Firestore push
- 닉네임 중복 체크: Firestore 쿼리, 실패 시 건너뜀 (네트워크 오류 대응)
- 온보딩 에러 메시지 UI 표시 + 저장 중 버튼 비활성화

---

## 현재 구현 완료

- [x] 온보딩 플로우 (직업/목표/아바타) + 닉네임 중복 체크
- [x] 홈 대시보드 (아바타, XP바, 스탯, 오늘퀘스트, 추천퀘스트, 주간활동)
- [x] 퀘스트 선택 (카테고리 → 부위 → 상세)
- [x] 운동 기록 (타이머, 세트 진행, 심박수/칼로리 시뮬레이션)
- [x] 운동 완료 (XP/스탯 트랜잭션 기반 반영, 레벨업)
- [x] 5탭 하단 네비게이션
- [x] 커스텀 앱 아이콘
- [x] 스플래시 화면
- [x] Hilt DI
- [x] Sealed UI State (Loading/Error/Success)
- [x] DB 인덱스/FK/마이그레이션 (v7)
- [x] 네트워크 보안 설정
- [x] Repository 추상화 (interface + Local)
- [x] EncryptedSharedPreferences
- [x] R8 난독화 + ProGuard
- [x] Firebase Auth (이메일/비밀번호 + Google 로그인)
- [x] 로그인/회원가입 UI (한국어 에러 메시지)
- [x] 로그아웃
- [x] 계정 삭제 (Firestore + Room + Auth 완전 삭제)
- [x] Firebase Firestore 클라우드 동기화 (다기기 데이터 공유)
- [x] 닉네임 중복 체크 (Firestore 기반)
- [x] Firebase UID 기반 유저 데이터 조회 (계정별 데이터 분리)
- [x] 인트로 슬라이드 화면 (첫 로그인 전 기기에만 표시, HorizontalPager 5장)
- [x] 온보딩 아바타 선택 — 이모지 → 실제 남성/여성 아바타 이미지 카드 선택
- [x] 홈 화면 — 원형 프로필 사진 + 닉네임/직업/목표 배지 표시
- [x] 프로필 사진 — 갤러리/카메라 선택, Base64로 Room/Firestore 동기화 저장
- [x] 아바타 탭 — 전신 이미지 + 드래그 좌우 회전 (rotationY 3D 효과, 스프링 복귀)
- [x] XP/스탯 프로그레스 바 테두리 (0일 때도 바 윤곽 표시)
- [x] 로그인 에러 메시지 — Firebase 예외 클래스 기반 정확한 한국어 매핑
- [x] 인트로 건너뛰기 버튼 우측 최상단 배치

---

## 미구현 / 다음 작업 후보

### 높은 우선순위
- [ ] **프로필 화면** — 운동 히스토리, 누적 통계, 계정 설정
- [ ] **인바디 데이터 입력** — 프로필에서 인바디 수치 기록 → 스탯 반영
- [ ] **퀘스트 데이터 확장** — 현재 ~20개 → 더 많은 운동 추가
- [ ] **앱 재시작 시 추천퀘스트 고정** — 현재 shuffle로 매번 바뀜, 하루 단위 고정 필요

### 중간 우선순위
- [ ] **3D 아바타 시스템** — Ready Player Me .glb 모델 + SceneView 라이브러리, 360도 회전, 레벨/직업별 장비
  - **준비 중**: Ready Player Me에서 남성/여성 .glb 모델 생성 필요
  - 모델 파일 위치: `app/src/main/assets/avatar_male.glb`, `avatar_female.glb`
  - 모델 준비 완료 후 SceneView 통합 구현 예정
- [ ] **PvP 대전** — 스탯 기반 1:1 비교 대결
- [ ] **알림/리마인더** — 운동 시간 알림
- [ ] **운동 중 실제 센서 연동** — Google Fit / Health Connect API

### 낮은 우선순위
- [ ] **소셜 기능** — 친구, 길드
- [ ] **업적/칭호 시스템**
- [ ] **다국어 지원**

---

## 알려진 이슈 & 주의사항

1. **심박수/칼로리** — 난이도 기반 시뮬레이션, 실제 센서 연동 시 WorkoutViewModel 수정 필요
2. **추천 퀘스트** — `shuffled()` + `combine()` 사용으로 앱 재진입마다 바뀜
3. **estimateCalories** — 70kg 고정 가정, 향후 사용자 체중 반영 필요
4. **mipmap webp 아이콘** — hdpi~xxxhdpi에 기본 Android webp 아이콘 남아있음 (adaptive-icon 우선 적용)
5. **릴리즈 서명** — signing config 미설정, Play Store 배포 전 keystore 생성 필요
6. **DB 마이그레이션 주의** — ALTER TABLE에 `DEFAULT NULL` 쓰면 Room 스키마 검증 실패. `DEFAULT` 절 없이 컬럼 추가해야 함
7. **Firestore 닉네임 중복 체크** — 네트워크/권한 오류 시 건너뜀 (false 반환). Firestore 보안 규칙에서 users 읽기가 인증된 유저 전체에게 열려있어야 동작
8. **아바타 회전 한계** — PNG 이미지 1장으로 구현 → rotationY ±75도로 제한 (90도 초과 시 거울 반전 노출). 360도 완전 회전을 원하면 ①각도별 프레임 이미지 8~16장 또는 ②`.glb` 3D 모델 + SceneView 라이브러리 필요
9. **avatarIndex 하위 호환** — 기존 DB에 0~7 이모지 인덱스로 저장된 유저는 0이면 남성, 1이면 여성으로 표시되고 2~7은 여성 이미지로 fallback됨 (신규 유저만 정확히 동작)
10. **프로필 사진 Base64 저장** — Firestore 문서 1MB 제한 내에서 동작 (512x512 JPEG ≈ 50~100KB → Base64 ≈ 70~130KB). 고해상도 사진이나 다수 필드 추가 시 문서 크기 주의. 향후 Firebase Storage 사용 시 URL 방식으로 전환 가능 (profileImageUrl 컬럼 재활용)

---

## Git 커밋 히스토리

```
ac3de29 fix: 프로필 사진 카메라 아이콘 잘림 수정
87101fd feat: 프로필 사진 기능 추가 — 갤러리/카메라 선택 + Firestore 동기화
f4e8188 fix: XP/스탯 프로그레스 바 테두리 추가 + 온보딩 버튼 느낌표 제거
ae78de0 fix: 로그인 에러 메시지 개선 — 예외 클래스 기반 매핑 + 마침표 통일
4e5f9fa fix: 인트로 화면 건너뛰기 버튼 우측 최상단으로 위치 조정
507f502 feat: 아바타 이미지 선택 + 아바타 탭 회전 기능 추가
8bea080 feat: 퀘스트 선택 화면 — 직업 대신 운동 종류(근력/유산소)로 변경
f7de4b0 feat: Balance 스탯 제거 + 보스 시스템 추가 (DB v6)
6f88917 feat: 인트로 슬라이드 화면 추가 + 퀘스트/운동 버그 수정
c6764d1 docs: HandOFF.md 업데이트 — Firestore 동기화, 계정 관리, 버그 수정 반영
c2a4753 feat: 닉네임 중복 체크 + 계정 삭제 시 Firestore 데이터 정리
a6fffe7 feat: Firebase Firestore 클라우드 동기화 추가
7865374 feat: 계정 삭제 기능 추가 + DB v4 초기화
8ce8968 fix: 스플래시 화면 "퀸스트" → "퀘스트" 오타 수정
d8bde81 fix: 로그인 계정과 무관하게 동일 유저 데이터 표시되는 버그 수정
25be966 docs: HandOFF.md 업데이트 — Firebase Auth 도입 반영
f3ff743 fix: DB 마이그레이션 v2→v3 DEFAULT NULL 제거 — Room 스키마 검증 통과
e969e27 feat: Firebase Auth 도입 — 이메일/비밀번호 + Google 로그인
39550af docs: HandOFF.md 업데이트 — 아키텍처 리팩토링 Phase 1~7 반영
560461b build: 프로덕션 빌드 설정 — R8 난독화, ProGuard 규칙 (Phase 7)
925a2c0 chore: Dead Code 제거 + 코드 정리 (Phase 6)
b744fc4 refactor: 보안 기반 구축 — 네트워크 보안, Repository 추상화, 암호화 (Phase 5)
6a4e30d refactor: DB 스키마 개선 — 인덱스, FK, 마이그레이션 추가 (Phase 4)
e47f66e refactor: Sealed UiState 패턴 적용 + 에러 핸들링 (Phase 3)
f8c0b9e fix: XP race condition 수정 — 트랜잭션 기반 보상 적용 (Phase 2)
67e3942 refactor: 수동 DI를 Hilt로 마이그레이션 (Phase 1)
d4d6433 docs: HandOFF.md 업데이트 — 스플래시 화면 작업 반영
8910714 feat: 스플래시 화면 추가 및 "시작하기" 버튼으로 전환 방식 변경
2ef6d4e docs: HandOFF.md 작성
07ac854 docs: README 작성
bf950e4 feat: BodyQuest 프로토타입 구현
69c28a1 Initial commit: BodyQuest empty Android project
```

---

## 빌드 & 실행

```bash
# 디버그 빌드
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk

# 릴리즈 빌드 (R8 난독화 적용)
./gradlew assembleRelease
# APK: app/build/outputs/apk/release/app-release-unsigned.apk
```

디바이스 테스트: Android Studio에서 USB 디버깅으로 실행 또는 APK 직접 설치.

### Firebase 설정 (신규 환경)
1. `app/google-services.json`이 이미 포함되어 있음
2. Firebase Console: https://console.firebase.google.com (프로젝트: bodyquest-ce5ab)
3. 새 디버그 키로 빌드 시 SHA-1 지문을 Firebase Console에 추가해야 Google 로그인 동작
   - `./gradlew signingReport`로 SHA-1 확인
   - Firebase Console > 프로젝트 설정 > Android 앱 > 디지털 지문 추가
4. Firestore Database가 활성화되어 있어야 클라우드 동기화 동작
   - 위치: asia-northeast3 (서울)
   - 보안 규칙: 위의 "Firestore 보안 규칙" 섹션 참고
