
# BodyQuest Handoff Document

> 마지막 업데이트: 2026-04-07 (Phase 52: 운동 GIF 이미지 시스템 — Coil GIF 디코더 + 24개 운동 적용)
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
│   │   ├── BodyQuestDatabase.kt # Room DB (v14), exportSchema=true, Migration(1,2)~Migration(13,14)
│   │   ├── SeedData.kt          # STRENGTH 개별운동 29개 + ENDURANCE 8개 + BALANCE 9개 + 보스 150개
│   │   ├── dao/
│   │   │   ├── UserDao.kt       # abstract class, getUser(uid), @Transaction applyWorkoutRewards(), updateProfileImageUrl()
│   │   │   ├── QuestDao.kt      # 카테고리/부위/난이도 필터링
│   │   │   ├── WorkoutDao.kt    # 운동 기록 CRUD, 시간 기반 쿼리, getWorkoutByFirestoreId()
│   │   │   └── BossProgressDao.kt # getProgressForUser(Flow), getProgress(one-shot), @Upsert (composite PK)
│   │   └── entity/
│   │       ├── UserEntity.kt    # id, nickname, job, goal, avatarIndex, stats, xp, level, firebaseUid, email, authProvider, profileImageUrl, updatedAt, equippedSkinId, gachaTickets
│   │       ├── QuestEntity.kt   # id, category, bodyPart, name, difficulty, rewards
│   │       ├── WorkoutEntity.kt # FK(userId→users, questId→quests), 복합인덱스, CASCADE, firestoreId
│   │       ├── WorkoutSetEntity.kt # FK(workoutId→workouts), CASCADE
│   │       └── BossProgressEntity.kt # boss_progress 테이블: bossId, userId (composite PK), isCleared, performance
│   ├── remote/
│   │   ├── FirestoreUserService.kt # Firestore CRUD: pushUser, pullUser, pushWorkout, pullAllWorkouts, deleteUser, isNicknameTaken
│   │   └── SyncManager.kt         # 동기화 오케스트레이션: syncOnLogin, pushUserToCloud, pushCompletedWorkout, pushBossProgressToCloud
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
│   ├── StatType.kt                # enum: STRENGTH/ENDURANCE/BALANCE
│   └── SkinItem.kt                # SkinCategory enum(상의/하의/신발/장갑/모자+emoji+color), SkinItem(id, name, category), ALL_SKINS(15개)
│
├── ui/
│   ├── common/
│   │   ├── UiState.kt           # sealed interface: Loading/Success/Error
│   │   └── CommonUi.kt          # LoadingScreen, ErrorScreen 공통 컴포넌트
│   │
│   ├── splash/
│   │   ├── SplashScreen.kt      # "시작하기" 버튼 → Intro/Login/Onboarding/Home 4분기
│   │   └── SplashViewModel.kt   # @HiltViewModel, 세션 타임아웃 15분 체크 → 유효 시 Home 직행
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
│   ├── boss/
│   │   ├── BossScreen.kt           # 그룹별 LazyRow, BattleOverlay(performance 뱃지), RequirementGauge, 등급카드(S/A/B)
│   │   ├── BossViewModel.kt        # BossWithProgress(isCleared, clearedGrade), calcPerformance(), confirmBattle()
│   │   └── BattleLogGenerator.kt   # generateBattleLogs(performance) — 랜덤 로그 + 등급별 결과 메시지
│   ├── avatar/AvatarScreen.kt      # 남성: 스프라이트 24프레임 회전, 여성: 단일 이미지
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
4. **세션 타임아웃**: 백그라운드 15분 이내 복귀 시 로그인 스킵, 초과 시 재로그인 필수
5. **심박수/칼로리는 시뮬레이션**: 현재 실제 센서 연동 없음
6. **Room = UI 단일 데이터 소스**: Firestore는 클라우드 백업/동기화 채널, 클라우드 실패해도 로컬 동작에 영향 없음

---

## 인증 시스템 (Firebase Auth)

### 앱 플로우
```
Splash ("시작하기")
  → [한 번도 로그인 안 한 기기] Intro (5장 슬라이드) → Login
  → [로그인 이력 있는 기기 + 세션 만료(15분 초과)] Login
  → [로그인 이력 있는 기기 + 세션 유효(15분 이내)] Home 직행 (syncOnLogin 포함)
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
      match /bossProgress/{bossId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
      match /inventory/{skinId} {
        allow read, write: if request.auth != null && request.auth.uid == userId;
      }
    }
  }
}
```
- users 읽기: 인증된 모든 유저 (닉네임 중복 체크용)
- users 쓰기: 본인만
- workouts 읽기/쓰기: 본인만
- bossProgress 읽기/쓰기: 본인만

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
  ├── workouts/{firestoreWorkoutId}
  │     ├── questId, startTime, endTime, elapsedSeconds
  │     ├── caloriesBurned, heartRateAvg, completed, xpEarned
  │     └── sets: [ {setNumber, reps, completed, completedAt} ]  ← 배열
  │
  └── bossProgress/{bossId}
        ├── bossId, isCleared, performance
```

### Push 시점 (로컬 → 클라우드)
- 온보딩 완료 (프로필 생성) → `pushUserToCloud()`
- 운동 완료 → `pushCompletedWorkout()` + `pushUserToCloud()`
- 보스 클리어 → `pushBossProgressToCloud()`

### Pull 시점 (클라우드 → 로컬)
- 로그인 시 로컬에 유저 없으면 → Firestore에서 유저 + 전체 운동 기록 pull
- 로그인 시 로컬에 유저 있지만 클라우드가 더 최신(updatedAt 비교) → 유저 데이터 업데이트 + 새 운동 기록 pull
- 로그인 시 boss progress 항상 pull (updatedAt과 무관, 보스 클리어는 user updatedAt을 변경하지 않으므로)
- 중복 방지: `firestoreId`로 이미 동기화된 운동 건너뜀, boss progress는 @Upsert로 덮어쓰기

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

### Phase 15: 남성 아바타 360도 스프라이트 시트 회전 ✅ (2026-03-29)
- `drawable/avatar_male_360.png` 추가: 15도 간격 24프레임 스프라이트 시트 (1536×2754px, 6열×4행, 프레임당 256×688px)
- `AvatarScreen.kt` 전면 교체: `MaleAvatarView()` (스프라이트 렌더링) + `FemaleAvatarView()` (단일 이미지)
- `MaleAvatarView` 핵심 구현:
  - `detectDragGestures` → 누적 드래그량 `totalDragX` 추적
  - 프레임 인덱스: `((totalDragX * 0.5f / 15f).roundToInt() % 24 + 24) % 24`
  - `Canvas` + `drawIntoCanvas` + `nativeCanvas.drawBitmap(srcRect, dstRect, null)`: `android.graphics.Rect`(srcRect) + `android.graphics.RectF`(dstRect)
  - 손을 떼도 프레임 유지 (스프링 복귀 없음, 원하는 각도 고정)
- 드래그 민감도: `DRAG_SENSITIVITY = 0.5f`, 프레임 당 15도
- NavGraph: `AvatarScreen(viewModel = hiltViewModel<HomeViewModel>())` 유지
- 이전 `rotationY` 3D 회전 방식 폐기 → 프레임 기반 스프라이트 방식으로 전환

### Phase 16: 직업 선택 화면 전면 개편 ✅ (2026-03-29)
- `JobSelectionPage.kt` 전면 재작성 (이전: 직업명+설명만 / 이후: 캐치프레이즈 + 특성 2개 항목)
- 카드 데이터 구조 `JobCardInfo(catchphrase, features: List<String>)`:
  - STRENGTH: "몸으로 증명하는 힘" / ["힘 성장 속도 증가", "고강도 운동에 유리"]
  - ENDURANCE: "멈추지 않는 지속력" / ["지구력 성장 속도 증가", "장시간 운동에 유리"]
  - BALANCE: "지속 가능한 성장" / ["힘과 지구력 균형 성장", "혼합 콘텐츠에 유리"]
- 선택 애니메이션: `animateFloatAsState(tween 200ms)` — 선택 카드 scale 1.05, 미선택 alpha 0.5
- 레이아웃: 좌측 56dp 원형 아이콘, 우측 직업명 + 캐치프레이즈 + 특성 불릿(5dp 원 + 텍스트)
- 선택 상태: border 2dp + color, background color 15% alpha
- **shadow 완전 제거**: Material3 다크 모드에서 `Modifier.shadow(elevation)` → Surface 내부 검정 오버레이 버그 발생 → border + background tint만 사용
- `OnboardingScreen`: step 0 버튼 텍스트 "다음" → "이 직업으로 시작하기"
- 하단 안내 문구: "직업은 이후에도 변경할 수 있습니다"

### Phase 17: 직업별 스탯 배율 시스템 ✅ (2026-03-29)
- `WorkoutViewModel.finishWorkout()` + `loadCompleteData()` 양쪽에 배율 로직 추가:
  ```kotlin
  val statMultiplier = when (user.job) {
      "STRENGTH"  -> if (quest.statType == "STRENGTH") 2.0f else 1.0f
      "ENDURANCE" -> if (quest.statType == "ENDURANCE") 2.0f else 1.0f
      "BALANCE"   -> 1.5f
      else        -> 1.0f
  }
  val actualStatReward = (quest.statReward * statMultiplier).roundToInt()
  ```
- `import kotlin.math.roundToInt` 추가 (`.toInt()` 대신 반올림으로 소수점 손실 방지)
- `WorkoutCompleteState`에 `baseStatReward: Int` 필드 추가 (직업 효과 적용 전 기본값)
- `SeedData.kt`: 모든 statReward를 짝수로 통일 (BALANCE ×1.5 시 소수점 방지)
  - 회복 조깅: `statReward = 1` → `2`
  - 사이클링: `statReward = 3` → `4`
- **ViewModel 인스턴스 이슈**: `WorkoutCompleteScreen`은 NavGraph 다른 백스택 항목이라 `hiltViewModel()`이 별도 인스턴스 생성 → `finishWorkout()` state를 볼 수 없음 → `loadCompleteData()` 항상 호출됨. 양쪽에 배율 로직 중복 필수.

### Phase 18: 운동 완료 화면 직업 효과 표시 ✅ (2026-03-29)
- `WorkoutCompleteScreen`: `statReward != baseStatReward`일 때 기본→최종 분기 표시:
  - 직업 효과 있음: `+{base}` (onSurface, SemiBold) → `+{final}` (statType.color, Bold, titleLarge) [직업 효과 배지]
  - 직업 효과 없음: `+{final}` (statType.color, Bold, titleLarge) 단독 표시
- "직업 효과" 배지: `Surface(RoundedCornerShape(6dp), color = statType.color.copy(0.15f))` + Text
- `loadCompleteData()`에서 `leveledUp` 제거 (재로그인 후 히스토리 조회 시 레벨업 여부 알 수 없음 — 재표시 안 함)

### Phase 19: 보스 전투 애니메이션 시스템 ✅ (2026-03-29)
- **신규 파일**: `domain/model/BattleLog.kt` — `LogType` enum (START/ATTACK/REACTION/CRISIS/FINISH/RESULT) + `BattleLog(message, type)` data class
- **신규 파일**: `ui/boss/BattleLogGenerator.kt` — 공격/반응/위기/마무리 로그 풀 + `generateBattleLogs()` 랜덤 3~5개 중간 로그 생성
- **`BossViewModel`**: `challengeBoss()` 내부에서 `delay(700L)` 기반 순차 로그 추가 코루틴 실행
  - `BossState`에 `battleLogs`, `isBattleActive`, `isBattleComplete`, `battleResult` 필드 추가
  - `confirmBattle()`: 성공 시 그냥 닫기, 실패 시 상세 다이얼로그(`challengeResult`) 표시
- **`BossScreen`**: `BattleOverlay` 풀스크린 컴포넌트 추가
  - `AnimatedVisibility(fadeIn + slideInVertically)` 로 등장
  - `LazyColumn` + `LaunchedEffect(logs.size)` → 새 로그마다 자동 스크롤
  - 로그 스타일: START(기본) / ATTACK(💥NeonOrange) / REACTION(✨NeonGreen) / CRISIS(⚠NeonRed) / FINISH(🔥NeonPurple) / RESULT(★XpGold)
  - `isBattleComplete` 시 확인 버튼 `fadeIn(500ms)` 등장
  - 기존 `ChallengeResultDialog` (텍스트 결과) 폐기 → 전투 오버레이로 대체

### Phase 20: 보스 그룹별 가로 스크롤 UI ✅ (2026-03-29)
- 단일 `LazyColumn` 보스 목록 → 그룹별 섹션 + `LazyRow` 구조로 변경
- 그룹 순서: 💪 근력 보스(NeonRed) → 🏃 지구력 보스(NeonBlue) → ⚡ 하이브리드 보스(NeonPurple)
- 보스 카드: `fillMaxWidth` → **220dp 고정 너비** (수평 스크롤용), 타입 배지 카드 내부에서 제거
- `RequirementChip`: 가로 정렬 `SpaceBetween`, `fillMaxWidth` 적용

### Phase 21: 보스 진행 시스템 — 순서 잠금 + 하루 1회 제한 ✅ (2026-03-29)
- **신규 Entity**: `BossProgressEntity` — `boss_progress` 테이블 (`bossId`, `userId`, `lastDefeatedAt: Long`)
  - 유저별 보스별 UNIQUE 인덱스 (`userId`, `bossId`)
- **신규 DAO**: `BossProgressDao` — `getProgressForUser(userId)` Flow, `getProgress()`, `@Upsert`
- **DB v7 → v8**: `MIGRATION_7_8` — `boss_progress` 테이블 + UNIQUE 인덱스 생성
- **`BossRepository`** 인터페이스 + `LocalBossRepository` 구현체에 `getProgressForUser()`, `recordDefeat()` 추가
- **`DatabaseModule`**: `BossProgressDao` 제공, `LocalBossRepository` 생성자 업데이트
- **`BossViewModel`** 전면 재작성:
  - `BossState.bosses: List<BossEntity>` → `bossGroups: Map<String, List<BossWithProgress>>`
  - `BossWithProgress(boss, isLocked, isCompletedToday)` data class 추가
  - `buildBossGroups()`: 타입별 `requiredLevel` 오름차순 정렬 → 이전 보스 클리어 여부로 `isLocked` 결정
  - `isToday()`: `Calendar` 기반 당일 판정
  - `combine(getAllBosses(), getProgressForUser())` 로 실시간 Flow 결합
  - `confirmBattle()`: 성공 시 `recordDefeat()` 호출 → Flow 자동 갱신
- **보스 카드 상태 3단계**:
  - 🔒 잠금: 이전 보스 미클리어 → Lock 아이콘 + "잠금" (수치 dimmed)
  - ✅ 오늘 완료: 당일 클리어 → CheckCircle + "오늘 완료" (NeonGreen)
  - 도전하기: 잠금 해제 + 미완료 → 타입 색상 버튼

### Phase 23: 보스 시스템 전면 재설계 ✅ (2026-03-29)

#### 보스 구성 (150개)
- **3타입 × 50보스**: STRENGTH(id 1~50) / ENDURANCE(id 51~100) / HYBRID(id 101~150)
- **이름 생성**: 10 접두어 × 5 접미어 (타입별 테마)
  - STRENGTH 접두어: 잠든/눈뜬/성난/무쇠/화강암의/불굴의/분노한/용암의/화염의/전설의
  - ENDURANCE 접두어: 미풍의/산들의/거센/폭풍의/번개의/질풍의/회오리의/태풍의/폭풍우의/전설의
  - HYBRID 접두어: 고요한/균형의/이중의/혼돈의/융합의/조화의/복합의/이원의/초월의/전설의
- **난이도 공식** `computeBaseStat(index)`: 기초값 10, index 1~15 구간 +3.0, 16~30 구간 +2.5, 31~49 구간 +2.0
- **타입별 스탯 비율**: STRENGTH(reqStr=base, reqEnd=base×0.3) / ENDURANCE(reqStr=base×0.3, reqEnd=base) / HYBRID(reqStr=base×0.8, reqEnd=base×0.8)
- `BossEntity.order = index` (0~49), `requiredLevel = index`
- `@ColumnInfo(name = "bossOrder")` — SQL 예약어 `order` 충돌 회피

#### 진행 시스템 변경
- **하루 1회 제한 제거**: 언제든 재도전 가능 (횟수 제한 없음)
- **영구 클리어** (`isCleared = true`): 한번 클리어하면 영구 기록, 이전 보스 클리어 시 다음 보스 잠금 해제
- **재도전**: 클리어 완료 카드에 "재도전" 버튼 추가 (재클리어 시 성능 등급 덮어쓰기)

#### 성능 평가 (등급)
- `calcPerformance()`: `margin = (user.strengthStat - boss.requiredStrength) + (user.enduranceStat - boss.requiredEndurance)`
  - margin ≥ 50 → "압도적인 승리" (S)
  - margin ≥ 20 → "안정적인 승리" (A)
  - margin < 20 → "간신히 승리" (B)
- 등급은 `BossProgressEntity.performance`에 저장 → 재클리어 시 갱신

#### 클리어 카드 UI
- 게이지 바 완전 제거 → **등급 문자(S/A/B) 48sp Bold + 클리어 설명 문구** 표시
  - S(압도적 클리어): NeonPurple / A(안정적 클리어): NeonGreen / B(간신히 클리어): NeonOrange
- 클리어 설명 문구: 알파벳 아래 `labelSmall` + `TextMuted` 색상으로 작고 흐리게 표시
- "재도전" 버튼: 타입 색상 60% 투명도

#### BattleOverlay 개선
- 결과 로그 메시지: "압도적으로 쓰러뜨렸습니다!" / "안정적으로 쓰러뜨렸습니다!" / "간신히 쓰러뜨렸습니다!" / "결국 무너졌습니다."
- 전투 완료 + 승리 시 제목 아래 성능 뱃지 출현 (S=NeonPurple, A=NeonGreen, B=NeonOrange, RoundedCornerShape(20dp))
- 확인 버튼 레이블: `"${performance}! 돌아가기"` (예: "압도적인 승리! 돌아가기")

#### DB 변경
- **v8 → v9** (`MIGRATION_8_9`): `bosses` DROP+재생성(bossOrder 컬럼 추가), `boss_progress` DROP+재생성(composite PK + isCleared)
- **v9 → v10** (`MIGRATION_9_10`): `boss_progress`에 `performance TEXT NOT NULL DEFAULT ''` ALTER ADD
- `insertSeedBosses()`: `bossOrder` 컬럼 포함하여 INSERT
- `onOpen` 콜백: 보스 수 0이면 150개 재시드 (마이그레이션 후 자동 재삽입)

#### 제거된 로직
- `isCompletedToday`, `lastDefeatedAt`, `isToday()` Calendar 체크 — 완전 삭제
- `recordDefeat(userId, bossId, timestamp)` → `recordClear(userId, bossId, performance)`
- `BossProgressDao.getProgress(userId, bossId)` — composite PK @Upsert로 불필요

### Phase 24: 보스 진행 Firestore 동기화 ✅ (2026-03-30)
- **`FirestoreUserService`**: `pushBossProgress()`, `pullAllBossProgress()` 메서드 추가
  - Firestore 경로: `users/{uid}/bossProgress/{bossId}`
  - 저장 필드: `bossId`, `isCleared`, `performance`
- **`SyncManager`**: `BossProgressDao` 주입, `pullBossProgressFromCloud()` + `pushBossProgressToCloud()` 추가
  - `syncOnLogin()`에서 boss progress 항상 pull (`updatedAt` 조건 밖으로 분리)
  - 보스 클리어는 user `updatedAt`을 변경하지 않으므로, `updatedAt` 비교와 무관하게 동기화
- **`FirestoreModule`**: `SyncManager` 생성자에 `BossProgressDao` 추가
- **`BossViewModel`**: `SyncManager` 주입, `confirmBattle()`에서 로컬 저장 후 Firestore push
- **`deleteUser()`**: `bossProgress` 서브컬렉션도 함께 삭제
- **Firestore 보안 규칙**: `bossProgress/{bossId}` 규칙 추가 (본인만 읽기/쓰기)

### Phase 25: 보스 최고 등급 보존 ✅ (2026-03-30)
- **`BossProgressDao`**: `getProgress(userId, bossId)` one-shot 쿼리 추가
- **`LocalBossRepository`**: `recordClear()` 로직 변경
  - 기존 등급과 새 등급을 비교하여 상위 등급만 저장 (S > A > B)
  - `performanceRank()`: 압도적인 승리=3, 안정적인 승리=2, 간신히 승리=1
  - `betterPerformance()`: 두 등급 중 높은 것 반환
  - 반환값을 `String`으로 변경 → ViewModel에서 Firestore push 시 최고 등급 사용
- **`BossRepository` interface**: `recordClear()` 반환 타입 `Unit` → `String`
- **`BossViewModel`**: `confirmBattle()`에서 `bestPerformance`를 받아 Firestore push

### Phase 27: 아바타 회전 제거 — 2D 단일 이미지로 단순화 ✅ (2026-03-30)
- 스프라이트 시트(`avatar_male_360`) 기반 360도 회전 로직 전체 제거
- `MaleAvatarView()` / `FemaleAvatarView()` 분리 구조 → 단일 `Image` 컴포넌트로 통합
- 남성/여성 모두 `avatar_male.png` / `avatar_female.png` 단일 이미지 표시
- "← 좌우로 드래그 →" 힌트 텍스트 제거
- `avatar_male_360.png` 스프라이트 시트 파일은 drawable에 잔류 (삭제 가능)

### Phase 28: 스킨 뽑기 (Gacha) 기능 추가 ✅ (2026-03-30)
- **신규 파일**: `ui/gacha/GachaScreen.kt`, `ui/gacha/GachaViewModel.kt`
- **GachaPhase** 3단계 상태 머신: `IDLE → SPINNING → REVEALED`
  - IDLE: "?" 카드 + "✨ 뽑기" 버튼
  - SPINNING (2.5초): 보라/핑크 아크 회전 글로우 링 + "?" 카드 펄싱 애니메이션
  - REVEALED: 화면 플래시 → 스킨 이미지 스프링 등장 + "언더아머 티셔츠를 뽑았다!" (XpGold)
- **애니메이션 버그 수정**: `LaunchedEffect(phase)` → `LaunchedEffect(animTrigger)` 로 변경
  - phase 변경 시 코루틴이 취소되어 `showFlash=false` / `revealVisible=true`가 실행되지 않던 문제 해결
- **drawable**: `언더아머남성.png` → `skin_underarmour_male.png` (Android 리소스 규칙)
- **이미지 배경 제거**: rembg(u2net AI 모델)로 배경 픽셀 alpha=0 처리
  - 기존 PNG에 반투명 배경 픽셀이 있어 어두운 배경에서 점박이처럼 보이던 문제 해결
- `Screen.Gacha` 라우트 추가, 아바타 탭에 "✨ 스킨 뽑기" 버튼 추가
- `GACHA_SKIN_ID = "underarmour_male"` 상수 — 추후 확률 테이블로 교체 예정

### Phase 29: 스킨 인벤토리 ✅ (2026-03-30)

#### 데이터 레이어
- **신규 Entity**: `SkinInventoryEntity` — `skin_inventory` 테이블 (`skinId`, `userId` 복합 PK, `count`)
- **신규 DAO**: `SkinInventoryDao` — `getInventory(Flow)`, `getItem()`, `@Upsert`
- **DB v10 → v11** (`MIGRATION_10_11`): `skin_inventory` 테이블 생성
- **`SkinInventoryRepository`** 인터페이스 + `LocalSkinInventoryRepository` 구현체
  - `addOrIncrement()`: 기존 항목 있으면 count+1, 없으면 count=1로 신규 생성
- **`SkinItem`** 도메인 모델 (`domain/model/SkinItem.kt`)
  - `ALL_SKINS` 목록으로 스킨 ID ↔ 이름/이미지 매핑 관리

#### Firestore 동기화
- Firestore 경로: `users/{uid}/inventory/{skinId}` — 필드: `skinId`, `count`
- **`FirestoreUserService`**: `pushSkinInventory()`, `pullAllSkinInventory()` 추가
- **`SyncManager`**: `skinInventoryDao` 주입, `pullSkinInventoryFromCloud()` + `pushSkinInventoryToCloud()` 추가
  - `syncOnLogin()`에서 인벤토리 항상 pull (bossProgress와 동일 방식)
- **`deleteUser()`**: `inventory` 서브컬렉션도 함께 삭제

#### 뽑기 연동
- **`GachaViewModel`**: `onGachaResolved(skinId)` — Room count+1 → Firestore push
- **`GachaScreen`**: phase=REVEALED 전환 시 즉시 `onGachaResolved()` 호출 (확인 버튼과 무관하게 저장)

#### UI
- **`InventoryScreen`**: 2열 `LazyVerticalGrid`, 스킨 이미지 + 이름 표시
  - 개수 배지: count ≥ 2일 때 이미지 우측 하단에 `×N` (NeonPurple 원형 배지)
  - 빈 인벤토리: "아직 획득한 스킨이 없습니다" 안내 메시지
- **아바타 탭**: "인벤토리" `OutlinedButton` 추가 (스킨 뽑기 버튼 아래)
- `Screen.Inventory` 라우트 추가

### Phase 30: 스킨 착용 시스템 (DB v12) ✅ (2026-03-30)
- **`UserEntity`**: `equippedSkinId: String? = null` 필드 추가
- **DB v11 → v12** (`MIGRATION_11_12`): `ALTER TABLE users ADD COLUMN equippedSkinId TEXT`
- **`UserDao`**: `updateEquippedSkin(uid, skinId)` 쿼리 추가
- **`UserRepository` / `LocalUserRepository`**: `updateEquippedSkin()` 메서드 추가
- **`FirestoreUserService`**: `pushUser` / `pullUser`에 `equippedSkinId` 필드 포함
- **`InventoryViewModel`**: `equippedSkinId: StateFlow<String?>`, `equipSkin()`, `unequipSkin()` 추가
  - 장착/해제 후 `syncManager.pushUserToCloud()` 호출
- **`AvatarScreen`**: 장착된 스킨 PNG를 기본 아바타 위에 `ContentScale.Fit` 오버레이 표시
- **`InventoryScreen`**: 스킨 카드 클릭 → 장착하기/해제하기/취소 AlertDialog
  - 장착중: XpGold 테두리(2dp) + CheckCircle 아이콘 + "장착중" 텍스트

### Phase 31: 스킨 이미지 처리 (여우 티셔츠 + 장갑) ✅ (2026-03-30)
- **여우 티셔츠** (`여우티셔츠남성.png` → `skin_fox_tshirt_male.png`):
  - rembg(u2net AI) 2차 적용 + alpha < 50 강제 투명 처리
  - 아바타 어깨 폭(765px) 기준 리사이즈 → 1536×2754 캔버스에 재배치
  - 칼라 위치를 아바타 목 위치(23%, row 635)에 정렬
  - 기존 `skin_underarmour_male.png` 삭제
- **장갑** (`장갑.png` → `skin_gloves_male.png`):
  - rembg 배경 제거 + alpha < 30 투명 처리
  - 좌/우 장갑 분리 크롭 후 리사이즈(높이 450px)
  - 아바타 팔 위치(왼팔 col 445, 오른팔 col 1117, top row 1580)에 배치
- **`SkinItem.kt`**: `ALL_SKINS`에 여우 티셔츠 + 기본 장갑 추가
- **`GachaScreen.kt`**: `GACHA_SKIN_ID` 상수 제거 → `ALL_SKINS.random()`으로 랜덤 뽑기

### Phase 32: 스킨 시스템 전면 재설계 — 텍스트 기반 ✅ (2026-03-30)
- **배경**: 스킨 PNG 이미지가 아바타 비율에 맞지 않아 이미지 방식 포기
- **스킨 이미지 전체 삭제**: `skin_fox_tshirt_male.png`, `skin_gloves_male.png` 삭제
- **`SkinItem.kt` 전면 재설계**:
  - `drawableRes: Int` 제거
  - `SkinCategory` enum 추가: 상의/하의/신발/장갑/모자 (각 `displayName`, `emoji`, `color`)
  - `SkinItem(id, name, category)`
  - `ALL_SKINS` 15개 (카테고리별 3개):
    - 상의: 기본 티셔츠 / 후드티 / 운동복 상의
    - 하의: 기본 반바지 / 트레이닝 팬츠 / 레깅스
    - 신발: 기본 운동화 / 러닝화 / 하이탑
    - 장갑: 기본 장갑 / 웨이트 장갑 / 권투 장갑
    - 모자: 기본 캡 / 비니 / 헤드밴드
- **`GachaScreen`**: `RevealedCard` → 이미지 대신 이모지+이름+카테고리 뱃지 카드
- **`InventoryScreen`**: 이미지 카드 → 이모지+카테고리 뱃지 텍스트 카드
  - 장착 버튼: "장착하기" 클릭 → "구현 중입니다. 곧 찾아뵙겠습니다." AlertDialog
- **`InventoryViewModel`**: `equippedSkinId`, `equipSkin()`, `unequipSkin()`, `SyncManager`, `UserRepository` 의존성 제거
- **`AvatarScreen`**: 스킨 오버레이 이미지 제거 (기본 아바타 단일 표시)
- `UserEntity.equippedSkinId` 컬럼은 DB에 잔류 (향후 착용 시스템 구현 시 재활용)

### Phase 33: 코드 품질 개선 — 버그 수정 + 로깅 + UI 개선 ✅ (2026-04-02)

#### 버그 수정
- **프로필 사진 EXIF 회전 보정**: `ImageUtil.compressAndResize()`에서 `ExifInterface`로 회전 정보 읽어서 `Matrix.postRotate()`로 바르게 회전 적용. `androidx.exifinterface:exifinterface:1.4.1` 의존성 추가
- **이미지 처리 IO 스레드 이동**: `HomeViewModel.uploadProfileImage()`에서 `ImageUtil.compressAndResize()` + `Base64.encodeToString()`을 `withContext(Dispatchers.IO)` 블록으로 이동 → ANR 방지
- **Flow 수집 코루틴 누적 방지**: `HomeViewModel`에 `loadJob`/`subJobs` 필드 추가, `retry()` 시 이전 코루틴 취소 후 재시작. `collectLatest` 내부에서도 새 user 값 올 때 서브 Job 정리
- **SharedPreferences `.apply()` → `.commit()`**: `LoginViewModel.handleAuthSuccess()`에서 `has_logged_in` 플래그 저장을 동기적으로 변경 → 앱 강제 종료 시 플래그 유실 방지

#### 로깅 개선
- **HomeViewModel 예외 로그 추가**: `loadTodaysQuests`, `loadRecommendedQuests`, `loadWeekWorkouts` 3곳의 `catch (_: Exception) { }` → `catch (e: Exception) { Log.w("HomeViewModel", ..., e) }`
- **SyncManager 예외 로그 추가**: 8개 catch 블록 모두 `Log.w("SyncManager", ..., e)` 추가 (syncOnLogin, pullWorkouts, pullBossProgress, pullSkinInventory, pushSkinInventory, pushBossProgress, pushUser, pushCompletedWorkout)

#### UI 개선
- **홈 추천 퀘스트 XP 표시**: Row + 2개 Text → `buildAnnotatedString`으로 한 줄 표시 (`"초급 · 15분 · + 100 XP"`, TextMuted + NeonPurple 색상 유지)
- **홈 주간 활동 체크 아이콘**: `"✓"` 텍스트 → `Icons.Default.Check` 아이콘 (18dp, 더 크고 선명)
- **보스 카드 높이 고정**: `BossCard` Surface에 `height(230.dp)` + `Arrangement.SpaceBetween` 적용 → 클리어 전후 카드 크기 통일

### Phase 34: 프로필 화면 구현 ✅ (2026-04-02)

#### 데이터 레이어
- **`WorkoutDao`**: 4개 집계 쿼리 추가 (마이그레이션 불필요, @Query만 추가)
  - `getCompletedWorkoutCount(userId)`: `COUNT(*)` — 완료 운동 총 횟수
  - `getTotalXpEarned(userId)`: `SUM(xpEarned)` — 총 XP 획득량
  - `getTotalElapsedSeconds(userId)`: `SUM(elapsedSeconds)` — 총 운동 시간
  - `getRecentCompletedWorkouts(userId, limit)`: 최근 N개 완료 운동 (히스토리용)
- **`BossProgressDao`**: `getClearedBossCount(userId)` 추가 — 클리어 보스 수
- **`WorkoutRepository` / `LocalWorkoutRepository`**: 4개 메서드 추가 (DAO 위임)
- **`BossRepository` / `LocalBossRepository`**: `getClearedBossCount()` 메서드 추가

#### ViewModel
- **`ProfileViewModel`** 전면 재작성:
  - `ProfileState` (cumulativeStats, workoutHistory, accountInfo)
  - `UiState<ProfileState>` + 기존 `DeleteState` 병존
  - `combine()`으로 4개 통계 Flow 결합 (workoutCount, totalXp, totalSeconds, bossClears)
  - 운동 히스토리: 최근 20개, `questRepository.getQuestById()`로 퀘스트명 해석
  - `loadJob`/`subJobs` 패턴으로 코루틴 관리 (HomeViewModel과 동일)
  - `formatElapsedTime()`: 초 → `"3시간 25분"` 형식
  - `formatAuthProvider()`: `"GOOGLE"` → `"Google"`, `"EMAIL"` → `"이메일"`
  - 기존 `signOut()`, `deleteAccount()` 완전 유지

#### UI
- **`ProfileScreen`** 전면 재작성:
  - `Column + verticalScroll` 스크롤 레이아웃
  - **섹션 1: 누적 통계** — 2×2 그리드 `ProfileStatCard` (총 운동/총 XP/운동 시간/보스 클리어), DarkSurface 배경, NeonGreen 값 표시
  - **섹션 2: 운동 히스토리** — `WorkoutHistoryRow` forEach (퀘스트명 + 날짜 + XP), `HorizontalDivider` 구분, 빈 상태 안내 문구
  - **섹션 3: 계정 정보** — `AccountInfoRow` (이메일/가입일/로그인 방법), label-value 좌우 배치
  - **섹션 4: 로그아웃/계정 삭제** — 기존 기능 하단 배치, AlertDialog 유지
  - `UiState` 분기 (Loading/Error/Success)

> userId 타입 주의: `WorkoutDao`는 `Long`(Room ID = user.id), `BossProgressDao`는 `String`(firebaseUid = authRepository.currentUserId)

### Phase 35: 뽑기 티켓 시스템 ✅ (2026-04-03)

#### 데이터 레이어
- **`UserEntity`**: `gachaTickets: Int = 0` 필드 추가
- **DB v12 → v13** (`MIGRATION_12_13`): `ALTER TABLE users ADD COLUMN gachaTickets INTEGER NOT NULL DEFAULT 0`
- **`UserDao`**: `updateGachaTickets(uid, tickets, updatedAt)` 쿼리 추가
- **`UserRepository` / `LocalUserRepository`**: `updateGachaTickets()` 메서드 추가

#### 보스 클리어 보상
- **`BossRepository`**: `ClearResult(previousPerformance, bestPerformance)` data class 추가, `recordClear()` 반환 타입 변경
- **`LocalBossRepository`**: `recordClear()`에서 이전 등급도 함께 반환
- **`BossViewModel.confirmBattle()`**: 등급별 티켓 지급 (S=3, A=2, B=1), 재도전 시 등급 향상분만 추가
- **`BossResult`**: `ticketsEarned: Int = 0` 필드 추가
- **`BossScreen`**: `TicketRewardDialog` — 보스 클리어 후 "🎫 뽑기 티켓 +N" 다이얼로그 표시

#### 뽑기 제한
- **`GachaViewModel`**: `UserRepository` 주입, `ticketCount: StateFlow<Int>`, `consumeTicket(): Boolean`
- **`GachaScreen`**: "🎫 보유 티켓: N장" 표시, 0장이면 버튼 비활성화 + "티켓이 없습니다"

#### Firestore 동기화
- **`FirestoreUserService`**: push/pull에 `gachaTickets` 필드 추가
- **`SyncManager`**: `syncOnLogin()` 병합 시 `gachaTickets` 포함

### Phase 36: 퀘스트 데이터 확장 — 15개→36개 + BALANCE 카테고리 ✅ (2026-04-03)
- **`SeedData.kt`**: 퀘스트 15개 → 36개로 확장
  - STRENGTH 기존 부위 난이도 채우기: 등/하체 고급, 어깨/팔 중급·고급 (+6개)
  - STRENGTH 코어(복근) 초·중·고급 추가 (+3개)
  - ENDURANCE 줄넘기 초·중·고급 추가 (+3개)
  - BALANCE(신규): 요가 3개, 스트레칭 3개, 필라테스 3개 (+9개)
- **`QuestScreen.kt`**: BALANCE "균형 운동" 🧘 카테고리 카드 추가 (NeonGreen)
- **`QuestTreeScreen.kt`**: BALANCE 카테고리 "균형 운동" + NeonGreen 색상 매핑 추가
- **`BodyQuestDatabase.kt`**: `onOpen` 시드 로직 `questCount == 0` → `questCount < seedQuests.size` 변경 (기존 기기에서도 새 퀘스트 자동 삽입)
- BALANCE 퀘스트 스탯: 요가/스트레칭 → ENDURANCE, 필라테스 → STRENGTH

### Phase 37: UI 텍스트 품질 개선 ✅ (2026-04-03)
- **문장 부호 통일**: 에러 메시지 16곳 마침표, 안내 문장 8곳 마침표, 지시형 7곳 마침표, 질문형 3곳 물음표 추가
- **퀘스트 화면 XP 표시**: Row + 복수 Text → `buildAnnotatedString`으로 한 줄 통합 (`"15분 · 3세트 x 12회 · + 30 XP"`)
- **퀘스트 상세 보상 표시**: `+60` → `+ 60` 공백 추가
- **운동 카테고리 띄어쓰기**: "근력운동" → "근력 운동", "유산소운동" → "유산소 운동", "균형운동" → "균형 운동"

### Phase 41: GLB 파일 지원 + 테스트 탭 뒤로가기 버튼 ✅ (2026-04-05)

#### 개요
- 기존 OBJ 전용 테스트 탭에 GLB(glTF 2.0 Binary) 파일 지원 추가
- assets에 .glb 파일이 있으면 GLB 우선 로드, 없으면 OBJ fallback
- 테스트 탭 좌상단 뒤로가기 버튼 추가

#### GlbParser (`GlbParser.kt`)
- 추가 라이브러리 없이 `org.json` + `java.nio.ByteBuffer`만 사용
- **GLB 포맷 파싱 순서**:
  1. `readBytes()` → `ByteBuffer(LITTLE_ENDIAN)`
  2. GLB 헤더 magic(0x46546C67) 검증
  3. JSON chunk (0x4E4F534A) → JSONObject
  4. BIN chunk (0x004E4942) → sliced ByteBuffer
  5. accessors[], bufferViews[], meshes[], nodes[] 파싱
  6. `computeWorldMatrices()`: BFS로 씬 그래프 순회 → 노드별 월드 변환 행렬 계산
  7. 각 노드의 모든 primitive 처리 (primitive별로 INDEX 배열 분리)
  8. 전체 AABB로 정규화
  9. step = max(1, totalTris / 120,000) 서브샘플링
  10. `ObjModel` 반환
- **posCache**: 같은 POSITION accessor를 공유하는 primitive들(Nomad Sculpt 패치 구조) 처리
  - `HashMap<posAccIdx, FloatArray>` — 월드 변환 및 AABB 갱신을 1회만 수행
  - primitive마다 별도 INDEX 배열 → 각각 `MeshData`로 추가
- **노드 계층**: `nodeLocalMatrix()` (matrix 배열 16개 또는 IDENTITY), `matMul4()` 4×4 column-major 행렬 곱
- **구멍 없는 렌더링**: "삼각형 수 가장 많은 primitive만 선택" 버그 수정 → 모든 primitive 렌더링
- **TARGET_TRIANGLES = 120,000** (ObjParser의 60,000보다 2배 — GLB는 이미 최적화된 포맷)

#### TestScreen 변경 사항
- `onBack: () -> Unit = {}` 파라미터 추가
- `loadingFileName` 상태 변수 → 로딩 중 파일명 표시
- **GLB 우선 로딩**: `assets.list("")?.firstOrNull { it.endsWith(".glb") }` → GlbParser → 실패 시 OBJ fallback
- **뒤로가기 버튼**: `IconButton(Icons.AutoMirrored.Filled.ArrowBack)` → `Box`의 `when` 블록 이후에 선언 (AndroidView 위에 렌더링되도록 z-order 보장)
- `BodyQuestNavGraph`: `TestScreen(onBack = { navController.popBackStack() })`

#### 파일 구조
```
app/src/main/assets/           ← 모델 파일 위치 (drawable 아님!)
  ├── testbear.obj             ← OBJ fallback용
  └── *.glb                   ← GLB 자동 감지 (첫 번째 .glb 파일 사용)
ui/test/
  ├── ObjParser.kt             # OBJ 파서 (2패스, 서브샘플링)
  ├── GlbParser.kt             # GLB 파서 (glTF 2.0 Binary)
  ├── ModelRenderer.kt         # OpenGL ES 2.0 렌더러
  ├── ModelGLSurfaceView.kt    # GLSurfaceView + 터치 드래그
  └── TestScreen.kt            # Compose 화면 (GLB 우선 + 뒤로가기)
```

### Phase 42: 코드 품질/보안 전면 점검 ✅ (2026-04-06)

#### Phase 1: 데이터 안전 + 크래시 방지
- **SkinInventory Race Condition 수정**: `addOrIncrement()`의 읽기→쓰기 패턴을 원자적 `UPDATE` 쿼리(`incrementCount`)로 교체
- **Base64 이미지 크기 제한**: `ImageUtil.compressAndResize()`에서 JPEG 품질을 단계적 하향하며 500KB 이하로 압축
- **GlbParser indices 범위 검증**: `triIdx * 3 + 2 >= indices.size` 체크 추가

#### Phase 2: 안정성 + 동시성
- **WorkoutViewModel 보상 실패 알림**: `rewardError` 상태 추가, catch 블록에 `Log.e()` + UI 에러 배너
- **BossViewModel 상태 원자성**: 모든 `_uiState.value = ...` → `_uiState.update {}` 원자적 업데이트 (5곳)
- **SimpleDateFormat 스레드 안전성**: ProfileViewModel 필드 → `loadCalendarData()` 내 로컬 변수로 이동
- **GachaViewModel**: `collect()` → `collectLatest()` 변경
- **OnboardingViewModel 닉네임 중복 체크**: 네트워크 실패 시 에러 메시지 표시 + 저장 중단
- **N+1 쿼리 최적화**: `QuestDao.getQuestsByIds()` 배치 쿼리 추가, ProfileViewModel에서 Map 캐시 사용

#### Phase 3: 보안
- **AppLogger 유틸 추가** (`util/AppLogger.kt`): `BuildConfig.DEBUG` 조건으로 릴리스 빌드 로그 비활성화
- **프로덕션 로그 민감 정보 제거**: HomeViewModel(UID/URI), FirebaseAuthRepository(스택트레이스), LoginScreen, SyncManager 전부 `AppLogger` 교체
- **백업 규칙 완성**: `backup_rules.xml` + `data_extraction_rules.xml`에서 DB/SharedPreferences 백업 제외
- **Firestore deleteUser 배치 처리**: `WriteBatch` + `chunked(500)` 적용
- **ProfileViewModel Firestore 삭제 실패 로깅**: 빈 catch → `AppLogger.e()` 추가
- `buildFeatures { buildConfig = true }` 추가 (AGP 8.x 대응)

#### Phase 4: 코드 품질
- **빈 catch 블록 로깅 추가**: GachaViewModel(2곳), ObjParser, GlbParser에 `AppLogger.w()` 추가
- **FK 누락 TODO 코멘트**: BossProgressEntity, SkinInventoryEntity에 향후 v14 FK 추가 TODO
- **MIGRATION_3_4 빈 마이그레이션 추가**: v3→v4 안전 경로 제공
- **UserDao 중복 메서드 정리**: `getUserByFirebaseUid()` 제거 → `getUserOnce()`로 통합 (SyncManager, LoginViewModel 호출부 수정)

### Phase 48: 여성 아바타 교체 + 여성 전용 스킨 시스템 구축 ✅ (2026-04-06)

#### 아바타 변경
- `avatar_female.png` 파일로 여성 아바타 교체 (기존 `female.png` 삭제)
- `AvatarScreen`: `avatarIndex == 1`일 때 `R.drawable.avatar_female` 사용

#### SkinItem 구조 변경
- `SkinItem`에 `avatarFilter: Int?` 필드 추가 (`null`=공통, `0`=남성, `1`=여성)
- 기존 텍스트 스킨 15개 전부 제거
- 여성 전용 이미지 스킨 3종으로 교체:
  - `skin_f_white_tshirt` — 흰색 티셔츠 (TOP)
  - `skin_f_blue_bra` — 파란 스포츠브라 (TOP)
  - `skin_f_yellow_pants` — 노란 트레이닝바지 (BOTTOM)

#### 뽑기 풀 필터링
- `GachaViewModel`: `avatarIndex: StateFlow<Int>` 추가 (유저 아바타 인덱스 실시간 관찰)
- `GachaScreen`: `avatarFilter == null || avatarFilter == avatarIndex` 조건으로 스킨 풀 필터링
- 뽑기 풀 비었을 때 크래시 방지: `skinPool.isEmpty()` 체크 후 버튼 비활성화 + 안내 문구

#### 스킨 이미지 리소스
- drawable 파일명 소문자 변환 (Android 리소스 규칙)
- 스킨 이미지: `skin_f_*.png` (뽑기 결과 카드/인벤토리 미리보기용)
- 결과 이미지: `result_f_*.png` (아바타 착용 상태 최종 렌더)

### Phase 49: 스킨 착용 시스템 구현 ✅ (2026-04-06)

#### InventoryViewModel
- `equippedTopId: StateFlow<String?>` — TOP 슬롯 실시간 관찰
- `equippedBottomId: StateFlow<String?>` — BOTTOM 슬롯 실시간 관찰
- `isEquipped(skin, topId, bottomId)` — 카테고리별 장착 여부 판정
- `equipSkin(skin)` / `unequipSkin(skin)` — 카테고리에 맞는 슬롯에 저장, Firestore push

#### InventoryScreen
- 장착 중인 스킨 카드: 보라색 2dp 테두리 + "장착중" 뱃지 + 이름 색상 강조
- 다이얼로그: 장착 중이면 "해제하기", 아니면 "장착하기"
- `showComingSoon` 임시 다이얼로그 제거

### Phase 50: 여성 스킨 시스템 전면 재설계 — TOP/BOTTOM + 결과 이미지 룩업 (DB v15) ✅ (2026-04-06)

#### 설계 변경
- **기존**: 스킨 PNG를 아바타 위에 오버레이 (이미지 크기/배경 문제로 실패)
- **변경**: 스킨 조합별 미리 렌더링된 결과 이미지를 룩업 테이블로 교체

#### 결과 이미지 5종 (모두 1536×2754 동일 크기)
| 파일 | 조합 |
|------|------|
| `result_f_white_tshirt.png` | 흰색 티셔츠만 |
| `result_f_blue_bra.png` | 파란 스포츠브라만 |
| `result_f_yellow_pants.png` | 노란 트레이닝바지만 |
| `result_f_white_tshirt_yellow_pants.png` | 흰티 + 노란바지 |
| `result_f_blue_bra_yellow_pants.png` | 파란브라 + 노란바지 |

#### DB v14 → v15
- `UserEntity`: `equippedBottomId: String? = null` 필드 추가 (BOTTOM 슬롯)
- `MIGRATION_14_15`: `ALTER TABLE users ADD COLUMN equippedBottomId TEXT`
- `UserDao`: `updateEquippedBottom(uid, skinId)` 쿼리 추가
- `UserRepository` / `LocalUserRepository`: `updateEquippedBottom()` 메서드 추가
- `FirestoreUserService`: `equippedBottomId` push/pull 포함

#### AvatarScreen 결과 이미지 룩업
```kotlin
fun femaleAvatarRes(topId: String?, bottomId: String?): Int = when {
    topId == "skin_f_white_tshirt" && bottomId == "skin_f_yellow_pants" -> result_f_white_tshirt_yellow_pants
    topId == "skin_f_blue_bra"    && bottomId == "skin_f_yellow_pants" -> result_f_blue_bra_yellow_pants
    topId == "skin_f_white_tshirt" -> result_f_white_tshirt
    topId == "skin_f_blue_bra"     -> result_f_blue_bra
    bottomId == "skin_f_yellow_pants" -> result_f_yellow_pants
    else -> avatar_female
}
```

#### 스킨 이미지 후처리
- rembg(u2net)로 스킨 단품 이미지 3종 검정 배경 제거 (투명 60~70%)
- 원본 파일: `drawable/Skin/Female/` 폴더에 보관

#### 스킨 파일 위치
```
drawable/
  avatar_female.png                         ← 기본 여성 아바타 (1536×2754)
  skin_f_white_tshirt.png                   ← 스킨 단품 (뽑기/인벤토리 미리보기)
  skin_f_blue_bra.png
  skin_f_yellow_pants.png
  result_f_white_tshirt.png                 ← 착용 결과 이미지 (AvatarScreen 표시)
  result_f_blue_bra.png
  result_f_yellow_pants.png
  result_f_white_tshirt_yellow_pants.png
  result_f_blue_bra_yellow_pants.png
  Skin/Female/                              ← 원본 한글 파일명 보관
    흰색티셔츠.png / 흰색티셔츠_결과.png 등
```

### Phase 52: 운동 GIF 이미지 시스템 ✅ (2026-04-07)

#### 개요
- AI(DALL-E)로 운동별 시작/완료 자세 2장 생성 → rembg 배경 제거 → 2프레임 GIF 합성
- 해부학적 일러스트 스타일 (타겟 근육 빨간색/주황색 하이라이트, 흰색 배경)
- Coil GIF 디코더로 앱 내 애니메이션 재생

#### 이미지 생성 파이프라인
- **`make_exercise_gif.py`**: 대화형 GIF 생성 도우미 스크립트
  - 운동 하나씩 프롬프트 표시 → 이미지 다운로드 → 배경 제거 + GIF 합성 자동화
  - Downloads 폴더 최근 파일 자동 감지 (Enter 입력)
  - 중간 종료 후 재실행 시 완료된 운동 스킵하고 이어서 진행
- **`exercise_image_prompts.md`**: 46개 운동별 프롬프트 문서 (공통 스타일 + 자세 설명)
- 이미지 경로: `images/raw/` (원본) → `images/gif/` (합성 결과) → `assets/exercise_gif/` (앱 에셋)

#### 앱 연동
- **coil-gif 2.6.0** 의존성 추가 (`libs.versions.toml` + `build.gradle.kts`)
- **`BodyQuestApp`**: `ImageLoaderFactory` 구현, `GifDecoder.Factory()` 등록
- **`ExerciseImages.kt`** (`domain/model/`): 운동 ID → `file:///android_asset/exercise_gif/` 경로 매핑
  - `getGifPath(questId)`: GIF 있으면 assets URI 반환, 없으면 null
  - `hasGif(questId)`: GIF 존재 여부 확인
- **`QuestTreeScreen`**: 썸네일에 GIF 표시 (AsyncImage), 없으면 FitnessCenter 아이콘 fallback
- **`WorkoutScreen`**: 가이드 카드에 GIF 120dp 표시, 없으면 아이콘 fallback

#### 현재 완료된 GIF (24/46)
- **가슴 5/5** ✅: 푸시업, 벤치프레스, 인클라인 프레스, 덤벨 플라이, 딥스
- **등 5/5** ✅: 풀업, 바벨 로우, 랫풀다운, 시티드 로우, 데드리프트
- **하체 5/5** ✅: 스쿼트, 레그프레스, 런지, 레그컬, 불가리안 스플릿
- **어깨 5/5** ✅: 숄더프레스, 사이드 레터럴 레이즈, 프론트 레이즈, 페이스풀, 밀리터리 프레스
- **팔 4/4** ✅: 바이셉 컬, 트라이셉 익스텐션, 해머 컬, 클로즈그립 벤치프레스
- **코어 0/5**, **ENDURANCE 0/8**, **BALANCE 0/9**

#### 새 GIF 추가 절차
1. `python make_exercise_gif.py` 실행 → 프롬프트 복사 → ChatGPT 이미지 생성 → 다운로드 → Enter
2. `images/gif/`에서 `assets/exercise_gif/`로 복사
3. `ExerciseImages.kt` gifMap에 항목 추가

#### 파일 구조
```
images/
  raw/                              ← 원본 이미지 (A/B 프레임)
  gif/                              ← 합성된 GIF
assets/exercise_gif/                ← 앱 에셋 (빌드에 포함)
  exercise_str_chest_pushup.gif
  exercise_str_chest_bench_press.gif
  ...
make_exercise_gif.py                ← GIF 생성 도우미 스크립트
exercise_image_prompts.md           ← 프롬프트 문서
```

### Phase 43: 추천 퀘스트 하루 고정 + UI 수정 ✅ (2026-04-06)
- **추천 퀘스트 하루 고정**: `LocalDate.now().toEpochDay()` 기반 시드로 `shuffled(dailyRandom)` — 같은 날 같은 추천
- **스플래시 캐치프레이즈 마침표 제거**: "운동을 퀘스트로, 몸을 레전드로." → 마침표 제거

### Phase 44: BALANCE 퀘스트 양쪽 스탯 보상 ✅ (2026-04-06)
- **StatType enum에 BALANCE 추가**: `BALANCE("균형", NeonGreen)`
- **SeedData**: BALANCE 퀘스트 9개 모두 `statType = "BALANCE"`로 통일 (기존 ENDURANCE/STRENGTH 혼재 → 통일)
- **UserDao**: `updateRewardsBalance()` 양쪽 스탯 동시 UPDATE 쿼리, `applyWorkoutRewards`에 `newStatValueSecond` 파라미터 추가
- **WorkoutViewModel**: BALANCE일 때 `statReward`를 근력/지구력 절반씩 분배 (홀수 시 근력 +1)
- **WorkoutCompleteScreen**: BALANCE → "근력 +N / 지구력 +N" 양쪽 표시
- **QuestDetailScreen**: BALANCE → "근력 +N / 지구력 +N" 표시, STRENGTH/ENDURANCE → 한국어 스탯명
- **BodyQuestDatabase onOpen**: 기존 BALANCE 퀘스트 statType 자동 마이그레이션

### Phase 45: STRENGTH 퀘스트 개별 운동으로 교체 ✅ (2026-04-06)
- 기존 18개 "루틴" → **29개 개별 운동**으로 변경
  - 가슴 5개: 푸시업, 벤치프레스, 인클라인 프레스, 덤벨 플라이, 딥스
  - 등 5개: 풀업, 바벨 로우, 랫풀다운, 시티드 로우, 데드리프트
  - 하체 5개: 스쿼트, 레그프레스, 런지, 레그컬, 불가리안 스플릿 스쿼트
  - 어깨 5개: 숄더프레스, 사이드 레터럴 레이즈, 프론트 레이즈, 페이스풀, 밀리터리 프레스
  - 팔 4개: 바이셉 컬, 트라이셉 익스텐션, 해머 컬, 클로즈그립 벤치프레스
  - 코어 5개: 플랭크, 크런치, 레그레이즈, 바이시클 크런치, 행잉 레그레이즈
- **onOpen**: 기존 루틴 데이터(`_beginner` ID 패턴) 감지 시 자동으로 삭제 후 새 데이터 삽입

### Phase 46: STRENGTH 운동 세트/무게/횟수 설정 UI ✅ (2026-04-06)
- **WorkoutSetEntity**: `weight: Double = 0.0` 필드 추가
- **DB v13 → v14**: `MIGRATION_13_14` — `workout_sets`에 `weight` 컬럼 추가
- **STRENGTH 운동 진행 화면**: 세트 테이블 뷰로 전면 개편
  - 전체 세트를 한 화면에 테이블로 표시
  - 세트별 개별 무게(kg)/횟수 입력 가능
  - 세트별 개별 체크(✓) 버튼으로 완료 처리
  - +/− 버튼으로 세트 추가/삭제
  - 첫 세트 체크 시 타이머 자동 시작, 모든 세트 체크 시 자동 운동 종료
- **운동 가이드 카드**: 진행 화면 상단에 가이드 카드 + ON/OFF 토글 (플레이스홀더, 나중에 GIF 교체)
- **QuestDetailScreen**: STRENGTH "세트" → "추천 세트" 표시, "운동 시작!" → "운동 시작" 느낌표 제거
- **Firestore 동기화**: push/pull에 weight 필드 추가
- ENDURANCE/BALANCE는 기존 타이머 UI 유지

### Phase 47: 운동 목록 마지막 수행일 + 썸네일 ✅ (2026-04-06)
- **WorkoutDao**: `getLastCompletionTimes(userId)` — 퀘스트별 마지막 수행 시간 배치 쿼리
- **QuestViewModel**: `lastDoneMap: Map<String, Long>` 로드 (AuthRepository, UserRepository, WorkoutRepository 주입)
- **QuestTreeScreen**: 운동 항목에 난이도별 색상 아이콘 + "N일 전" / "오늘" / "어제" 마지막 수행일 표시
- 수행일과 시간/XP 정보 동시 표시
- 운동 이미지는 플레이스홀더 (디자이너 이미지 준비 시 교체 예정)

### Phase 40: 3D OBJ 뷰어 테스트 탭 ✅ (2026-04-03)

#### 개요
- 하단 탭 6번째 "테스트 (🔧)" 탭 추가
- OBJ 파일을 OpenGL ES 2.0으로 렌더링, 상하좌우 드래그로 회전

#### 파일 구조
```
app/src/main/assets/testbear.obj     ← 모델 파일 위치 (drawable 아님!)
ui/test/
  ├── ObjParser.kt          # OBJ 파서 (2패스, 서브샘플링)
  ├── ModelRenderer.kt      # OpenGL ES 2.0 렌더러
  ├── ModelGLSurfaceView.kt # GLSurfaceView + 터치 드래그
  └── TestScreen.kt         # Compose 화면
```

#### ObjParser (`ObjParser.kt`)
- **2패스 파싱**: Pass1 라인 카운트(v/f) → 서브샘플링 스텝 계산 → Pass2 실제 파싱
- **서브샘플링**: `step = max(1, faceCount×2 / 60_000)` — 파일 크기에 따라 자동 조정
- **메모리 효율**: 박싱 없는 `FloatArray` 사용 (100MB 파일 기준 피크 ~20MB)
- **지원 형식**:
  - 정점: `v x y z` 또는 `v x y z r g b` (Nomad Sculpt 컬러 포함 포맷)
  - 면: `f v`, `f v/vt`, `f v//vn`, `f v/vt/vn` (슬래시 혼합 모두 지원)
  - 팬 삼각분할 (삼각형·쿼드·n각형 모두 처리)
  - `vn` 없을 시 면 법선 자동 계산 (cross product)
  - 음수 상대 인덱스 지원
- **정규화**: 바운딩 박스 중앙 정렬 + ±1 크기로 자동 스케일

#### ModelRenderer (`ModelRenderer.kt`)
- OpenGL ES 2.0 GLSL 셰이더
  - Vertex shader: MVP 행렬 × 회전 행렬 적용
  - Fragment shader: 양면 디퓨즈 라이팅 (front×0.75 + back×0.25 + ambient 0.2)
- `@Volatile var rotX / rotY` — UI 스레드 → GL 스레드 안전한 전달
- 웜 브라운 컬러 (0.78, 0.58, 0.40) + `perspectiveM` FOV 50°

#### ModelGLSurfaceView (`ModelGLSurfaceView.kt`)
- `GLSurfaceView` 서브클래스, `RENDERMODE_CONTINUOUSLY`
- `onTouchEvent`: ACTION_DOWN/MOVE → `renderer.rotY += dx×0.4f`, `rotX` ±89° 클램프

#### TestScreen (`TestScreen.kt`)
- `Dispatchers.IO`에서 OBJ 로드 (대용량 파일 대응)
- 상태: 로딩 중 / 에러 메시지 / 3D 뷰어
- 하단: "← → 좌우 / ↑ ↓ 상하 드래그하여 회전" + 폴리곤 수 표시

#### 주의사항
- OBJ 파일은 `res/drawable/`가 아닌 **`app/src/main/assets/`** 에 넣어야 함
  (`drawable`은 .xml/.png만 허용, 빌드 실패)
- 100MB 이상 대용량 파일은 로딩에 3~10초 소요 (IO 스레드에서 처리)
- 목표 폴리곤 수 60,000 삼각형 (서브샘플링으로 자동 조절)
- 모델 파일 교체: `assets/testbear.obj` 덮어쓰기만 하면 됨

#### 네비게이션
- `Screen.ModelTest("model_test")` 추가
- `BottomNavBar`: `Icons.Default.Build` 아이콘, "테스트" 레이블
- `bottomNavRoutes`에 추가 (바텀바 표시)

### Phase 39: 프로필 운동 히스토리 달력 UI + 스탯 획득량 표시 ✅ (2026-04-03)

#### 데이터 레이어
- **`WorkoutDao`**: `getCompletedWorkoutsSince(userId, startTime)` 쿼리 추가 (startTime 이후 완료 운동 전체, 달력용)
- **`WorkoutRepository` / `LocalWorkoutRepository`**: 위 메서드 추가

#### ViewModel
- **`WorkoutHistoryItem`**: `statType: String`, `statGained: Int` 필드 추가 / `date: String` → `dateKey: String`("yyyy-MM-dd") 변경
- **`ProfileState`**: `workoutHistory: List<WorkoutHistoryItem>` → **`calendarData: Map<String, List<WorkoutHistoryItem>>`** 교체 (dateKey → 해당 날 운동 목록)
- **`ProfileViewModel.loadCalendarData(userJob)`**:
  - 6개월치 완료 운동 로드 (`getCompletedWorkoutsSince`)
  - 날짜별 그룹핑
  - 직업 배율 적용 후 `statGained` 계산: STRENGTH·ENDURANCE 해당 운동 ×2.0 / BALANCE ×1.5 / 기타 ×1.0

#### UI (`ProfileScreen`)
- **`WorkoutCalendarSection`**: 운동 히스토리 리스트 → 월별 달력으로 교체
  - `<` / `>` 버튼으로 월 이동
  - 요일 헤더: 일(NeonRed)~토
  - `CalendarDayCell`: 운동 있는 날 — NeonPurple 반투명 원+테두리, 선택 시 채워진 원, 2건 이상 점(·) 표시 / 오늘 — NeonGreen 테두리
  - 운동 있는 날 탭 → 달력 아래 요약 카드 출현: "대표 퀘스트명 외 N건"
  - 요약 카드 탭 → **`WorkoutDetailDialog`** 상세 다이얼로그
    - 첫 번째 운동에 "대표" 뱃지
    - 각 항목: 퀘스트명 / 운동 시간 / + XP / 스탯 획득량
- **`WorkoutDetailRow`**: 스탯 획득량 표시 추가
  - 근력 + N (NeonRed) / 지구력 + N (NeonBlue) / 균형 + N (NeonOrange)
  - statGained = 0이면 미표시

### Phase 38: 홈 보스 진행률 + 프로필 직업 배율 + 주간 운동 그래프 ✅ (2026-04-03)

#### 홈 화면 — 보스 진행률
- **`BossDao`**: `getTotalBossCount()` 쿼리 추가
- **`BossRepository` / `LocalBossRepository`**: `getTotalBossCount()` 메서드 추가
- **`HomeViewModel`**: `BossRepository` 주입, `loadBossProgress()` — `combine(clearedCount, totalCount)` Flow 결합
- **`HomeState`**: `clearedBossCount`, `totalBossCount` 필드 추가
- **`HomeScreen`**: 주간 활동 아래에 보스 진행률 프로그레스 바 + "N / 150" 수치 표시 (NeonRed)

#### 프로필 화면 — 직업 배율 안내
- **`ProfileState`**: `userJob: String` 필드 추가
- **`ProfileViewModel`**: 유저 로드 시 `userJob` 설정
- **`ProfileScreen`**: 누적 통계 아래에 직업 효과 카드 — 직업 배지 + 배율 텍스트 (STRENGTH: x2.0, ENDURANCE: x2.0, BALANCE: x1.5)

#### 프로필 화면 — 주간 운동 통계 바 차트
- **`DailyWorkoutStat`**: `label: String`, `count: Int` data class 추가
- **`ProfileState`**: `weeklyStats: List<DailyWorkoutStat>` 필드 추가
- **`ProfileViewModel`**: `loadWeeklyStats()` — 이번 주 월~일 운동 기록을 일별 집계
- **`ProfileScreen`**: 직업 배율 아래에 바 차트 — 월~일 7개 바, 운동 횟수 비례 높이, 숫자 표시

### Phase 26: 세션 타임아웃 15분 ✅ (2026-03-30)
- **기존**: 앱 실행 시 무조건 `signOut()` 후 로그인 필수
- **변경**: 백그라운드 15분 이내 복귀 시 로그인 스킵 → Home 직행
- **`MainActivity`**: `DefaultLifecycleObserver.onStop()`에서 `last_active_time` 타임스탬프를 `EncryptedSharedPreferences`에 저장
- **`SplashViewModel.checkUser()`** 전면 변경:
  - `has_logged_in` false → Intro
  - `has_logged_in` true + Firebase 인증 살아있음 + `elapsed < 15분` → `syncOnLogin()` 후 Home 직행
  - `has_logged_in` true + 세션 만료/인증 없음 → `signOut()` 후 Login
- `SyncManager` 주입 추가 (세션 유효 시에도 클라우드 동기화 수행)
- `SESSION_TIMEOUT_MS = 15 * 60 * 1000L` (companion object 상수)

---

### Phase 22: 보스 요구 조건 게이지 UX ✅ (2026-03-29)
- **`StatStatus`** enum: SUFFICIENT / NORMAL / LOW
  - `getStatStatus()`: 비율 ≥1.0 → SUFFICIENT, ≥0.7 → NORMAL, <0.7 → LOW
  - 색상: 🟢 NeonGreen / 🟠 NeonOrange / 🔴 NeonRed
  - 텍스트: 충분함 / 가능 / 부족
- **`RequirementGauge`** (기존 `RequirementChip` 대체):
  - 숫자 완전 제거 (`current / required` 표시 없음)
  - 라벨(좌) + 상태 텍스트(우) → 색상 게이지 바 (높이 5dp, `RoundedCornerShape(3dp)`)
  - `animateFloatAsState(tween 600ms, FastOutSlowInEasing)` → 카드 진입 시 게이지 차오르는 효과
  - 잠금 상태: 회색 빈 게이지
- **실패 다이얼로그 자연어 개선**:
  - 기존: "• 근력 +20 필요 / • 레벨 +3 필요" (숫자 노출)
  - 변경: `buildFailureMessage()` — "근력과 레벨이 부족합니다." (자연어)
  - `buildMotivationMessage()` — 부족 항목에 맞는 행동 유도 메시지 (NeonOrange 배지):
    - 근력만: "근력 운동을 추천합니다."
    - 지구력만: "지구력 운동을 추천합니다."
    - 레벨만: "조금 더 운동하면 레벨이 올라갑니다."
    - 복합: "꾸준한 운동으로 강해질 수 있습니다."

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
- [x] DB 인덱스/FK/마이그레이션 (v14, workout_sets.weight 컬럼 추가)
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
- [x] 아바타 탭 — 남성 360도 스프라이트 시트 회전 (24프레임, 드래그 감도 0.5), 여성 단일 이미지
- [x] XP/스탯 프로그레스 바 테두리 (0일 때도 바 윤곽 표시)
- [x] 로그인 에러 메시지 — Firebase 예외 클래스 기반 정확한 한국어 매핑
- [x] 인트로 건너뛰기 버튼 우측 최상단 배치
- [x] 직업 선택 화면 개편 — 캐치프레이즈 + 특성 불릿 + 선택 애니메이션 (scale/alpha)
- [x] 직업별 스탯 배율 — STRENGTH/ENDURANCE 해당 운동 ×2.0, BALANCE 전체 ×1.5
- [x] 운동 완료 화면 직업 효과 표시 — 기본 보상 → 최종 보상 + "직업 효과" 배지
- [x] 보스 전투 애니메이션 — 700ms 순차 로그 출력, LogType별 색상, 전투 오버레이
- [x] 보스 화면 그룹별 가로 스크롤 — 근력/지구력/하이브리드 LazyRow
- [x] 보스 시스템 재설계 — 150보스(3타입×50), 영구 클리어, 순서 잠금, 재도전 (DB v10)
- [x] 보스 요구 조건 게이지 — 숫자 제거, StatStatus 색상 게이지 + 자연어 실패 메시지
- [x] 보스 성능 평가 — margin 공식으로 S/A/B 등급 산정 + 클리어 카드에 등급 표시
- [x] 보스 클리어 카드 — 게이지 대신 48sp 등급 문자 + 설명 문구(labelSmall/TextMuted) + 재도전 버튼
- [x] 전투 오버레이 — 등급별 결과 메시지 + 성능 뱃지 + 확인 버튼에 성능 텍스트 표시
- [x] 보스 진행 Firestore 동기화 — 클리어 시 push, 로그인 시 항상 pull, 계정 삭제 시 정리
- [x] 보스 최고 등급 보존 — 재도전 시 S > A > B 비교 후 상위 등급만 저장
- [x] 세션 타임아웃 15분 — 백그라운드 15분 이내 복귀 시 로그인 스킵, Home 직행
- [x] 아바타 탭 — 회전 제거, 남성/여성 단일 이미지로 단순화 (2D)
- [x] 스킨 뽑기 (Gacha) — IDLE/SPINNING/REVEALED 3단계 애니메이션, 결과 공개
- [x] 스킨 이미지 배경 제거 — rembg AI(u2net) 처리, alpha=0 투명 배경
- [x] 스킨 인벤토리 — Room DB v11 + Firestore 동기화, 2열 그리드, ×N 개수 배지
- [x] 스킨 뽑기 랜덤화 — ALL_SKINS.random()으로 15개 중 랜덤 선택
- [x] 스킨 시스템 재설계 — 이미지 제거, 텍스트+이모지+카테고리 배지 기반, 카테고리 5종×3개=15개
- [x] 인벤토리 장착 버튼 — "구현 중입니다. 곧 찾아뵙겠습니다." 알림 다이얼로그
- [x] 코드 품질 개선 — EXIF 회전 보정, IO 스레드 이미지 처리, Flow 누적 방지, 예외 로깅
- [x] 홈 UI 개선 — 추천 퀘스트 XP 한 줄 표시, 주간 활동 Check 아이콘, 보스 카드 높이 고정
- [x] 프로필 화면 — 누적 통계(4종), 운동 히스토리(최근 20개), 계정 정보, 로그아웃/삭제
- [x] 뽑기 티켓 시스템 — 보스 클리어 시 등급별 티켓 지급 (S=3, A=2, B=1), 재도전 시 차이분만 추가
- [x] 퀘스트 데이터 확장 — 15개→36개, BALANCE 카테고리 (요가/스트레칭/필라테스) 추가
- [x] UI 텍스트 품질 개선 — 문장 부호 통일, XP 한 줄 표시, 카테고리 띄어쓰기
- [x] 홈 보스 진행률 — 프로그레스 바 + N/150 수치 표시
- [x] 프로필 직업 배율 안내 — 직업 배지 + 배율 텍스트 카드
- [x] 프로필 주간 운동 그래프 — 월~일 바 차트
- [x] 프로필 운동 히스토리 달력 UI — 월 이동 네비게이션, 운동 있는 날 보라색 원, 날짜 탭 시 요약 카드
- [x] 운동 히스토리 날짜 상세 다이얼로그 — 대표 운동 뱃지, 퀘스트명·운동시간·XP·스탯 획득량 표시
- [x] 3D OBJ 뷰어 테스트 탭 — OpenGL ES 2.0, 드래그 회전, 100MB 대용량 파일 대응 서브샘플링
- [x] GLB(glTF 2.0 Binary) 파일 지원 — assets에 .glb 있으면 우선 로드, GlbParser (org.json + ByteBuffer), Nomad Sculpt 다중 primitive/노드 계층 지원
- [x] 테스트 탭 뒤로가기 버튼 — 좌상단 ArrowBack IconButton, AndroidView 위에 z-order 보장
- [x] 코드 품질/보안 전면 점검 — Race Condition 수정, 원자적 상태 업데이트, AppLogger, 백업 규칙, Firestore 배치 삭제
- [x] 추천 퀘스트 하루 고정 — 날짜 기반 시드로 동일 추천 유지
- [x] BALANCE 양쪽 스탯 보상 — statReward를 근력/지구력 절반씩 분배
- [x] STRENGTH 개별 운동 교체 — 18개 루틴 → 29개 개별 운동 (벤치프레스, 스쿼트 등)
- [x] STRENGTH 세트 테이블 UI — 세트별 무게/횟수 입력, 개별 체크, +/− 세트 추가/삭제
- [x] 운동 가이드 카드 — 진행 화면 상단 플레이스홀더 + ON/OFF 토글
- [x] 운동 목록 마지막 수행일 — "N일 전" 표시 + 시간/XP 동시 표시
- [x] 운동 목록 썸네일 — GIF 있으면 표시, 없으면 난이도별 색상 아이콘 fallback
- [x] 운동 GIF 이미지 시스템 — Coil GIF 디코더 + ExerciseImages 매핑 + 24/46개 적용 (가슴 5, 등 5, 하체 5, 어깨 5, 팔 4)
- [x] 여성 아바타 교체 — `avatar_female.png` 적용, `avatarIndex==1`에 반영
- [x] 스킨 시스템 재구축 — 여성 전용 3종 (흰색 티셔츠/파란 스포츠브라/노란 트레이닝바지), `avatarFilter` 필드로 성별 필터링
- [x] 뽑기 풀 필터링 — `avatarIndex` 기반, 풀 비었을 때 크래시 방지
- [x] 스킨 장착 시스템 — TOP/BOTTOM 슬롯 분리 (DB v15 `equippedBottomId`), 카테고리별 equip/unequip
- [x] 인벤토리 장착 UI — 장착 중 카드 보라 테두리/뱃지, 장착하기↔해제하기 다이얼로그
- [x] 아바타 스킨 표시 — 오버레이 대신 조합별 결과 이미지 룩업 (5종 조합)
- [x] 스킨 이미지 배경 제거 — rembg로 단품 스킨 3종 검정 배경 투명화

---

## 미구현 / 다음 작업 후보

### 높은 우선순위
- [ ] **운동 GIF 나머지 22개 추가** — 현재 24/46 완료. `make_exercise_gif.py`로 이어서 생성 → `assets/exercise_gif/`에 복사 → `ExerciseImages.kt` 매핑 추가
  - 코어 5개, ENDURANCE 8개, BALANCE 9개 남음
- [ ] **인바디 데이터 입력** — 프로필에서 인바디 수치 기록 → 스탯 반영

### 중간 우선순위
- [ ] **스킨 추가** — 현재 여성 전용 3종만 존재. 남성 스킨 및 추가 여성 스킨 제작 필요. 새 스킨 추가 시 `ALL_SKINS`, `femaleAvatarRes()` 룩업 테이블, 결과 이미지 모두 추가 필요
- [ ] **스킨 조합 확장** — 현재 결과 이미지가 없는 조합(파란브라+흰티 동시 착용 불가 등) 처리 및 신규 조합 결과 이미지 제작
- [ ] **스킨 확률 테이블** — 현재 `ALL_SKINS.random()` 균일 확률. 희귀도별 가중 확률 도입 가능
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
8. **아바타 이미지** — 여성: `avatar_female.png` + 조합별 결과 이미지 룩업. 남성: `avatar_male.png` 단일 이미지 (남성 스킨 미구현)
9. **avatarIndex 하위 호환** — 기존 DB에 0~7 이모지 인덱스로 저장된 유저는 0이면 남성, 1이면 여성으로 표시되고 2~7은 여성 이미지로 fallback됨 (신규 유저만 정확히 동작)
10. **프로필 사진 Base64 저장** — Firestore 문서 1MB 제한 내에서 동작 (512x512 JPEG ≈ 50~100KB → Base64 ≈ 70~130KB). 고해상도 사진이나 다수 필드 추가 시 문서 크기 주의. 향후 Firebase Storage 사용 시 URL 방식으로 전환 가능 (profileImageUrl 컬럼 재활용)
11. ~~**보스 진행 데이터 미동기화**~~ — **Phase 24에서 해결**. `bossProgress` 서브컬렉션으로 Firestore 동기화 완료.
12. ~~**보스 등급 재클리어 시 덮어쓰기**~~ — **Phase 25에서 해결**. 최고 등급 보존 로직 추가 (S > A > B 비교).
13. ~~**스킨 뽑기 확률**~~ — **Phase 32에서 해결**. `ALL_SKINS.random()` 균일 확률로 15개 중 선택.
14. ~~**Firestore 보안 규칙**~~ — **Phase 34 이후 해결**. `inventory/{skinId}` 서브컬렉션 보안 규칙 Firebase Console에서 추가 완료 (2026-04-03).
15. ~~**스킨 착용 미구현**~~ — **Phase 49~50에서 해결**. TOP/BOTTOM 슬롯 분리(DB v15), 결과 이미지 룩업 방식으로 구현 완료.
16. **스킨 ID 변경 시 인벤토리 orphan** — `skin_inventory` 테이블의 `skinId`가 `ALL_SKINS`에 없으면 인벤토리에서 미표시. 스킨 id 변경 시 기존 데이터 정리 필요.
17. **새 스킨/조합 추가 절차** — `ALL_SKINS`에 스킨 추가 → 결과 이미지 drawable 추가 → `AvatarScreen.femaleAvatarRes()` 룩업 테이블 업데이트 → `GachaScreen/InventoryScreen.skinDrawableRes()` 업데이트 3곳 모두 수정 필요.

---

## Git 커밋 히스토리

```
fee5cd4 chore: 구 스킨/아바타 이미지 삭제 + DB 스키마 및 Skin 원본 파일 추가
6f1f7c0 fix: 스킨 이미지 검정 배경 제거 — rembg 처리 (흰티 62.8%, 파란브라 69.4%, 노란바지 60.5% 투명)
a7ddaed feat: 여성 스킨 시스템 전면 재설계 — TOP/BOTTOM 슬롯 + 결과 이미지 룩업 (DB v15)
0c3883c feat: 스킨 장착 시스템 구현 — 인벤토리에서 장착 시 아바타에 이미지 오버레이
649b6b4 fix: 스킨 풀 비었을 때 뽑기 크래시 수정 — skinPool 비어있으면 버튼 비활성화
4fdc057 refactor: 기존 텍스트 스킨 15개 제거 — 여성 전용 이미지 스킨 3종만 유지
8285d79 feat: 여성 아바타 교체 + 여성 전용 이미지 스킨 3종 추가
06fba9e docs: HandOFF.md 업데이트 — Phase 42~47 반영
(Phase 40 커밋: 3D OBJ 뷰어 테스트 탭 — assets/ 이동, ObjParser 2패스 재작성, OOM 수정)
4a9a783 docs: HandOFF.md 업데이트 — Phase 39 반영 (달력 UI + 스탯 획득량)
04bafce feat: 프로필 운동 히스토리 → 달력 UI + 스탯 획득량 표시
cbcd550 docs: HandOFF.md 업데이트 — Phase 35~38 반영 (뽑기 티켓, 퀘스트 확장, UI 개선, 홈/프로필 기능)
88dd542 feat: 홈 보스 진행률 + 프로필 직업 배율 안내 + 주간 운동 그래프
582e8bd fix: 운동 카테고리 띄어쓰기 수정 — 근력운동 → 근력 운동
3747a61 fix: 전체 앱 문장 부호 통일 — 마침표/물음표 누락 수정
fba129e fix: 퀘스트 화면 UI 개선 — XP 한 줄 표시, BALANCE 카테고리 매핑
f5785bc feat: 퀘스트 데이터 확장 — 15개→36개, BALANCE 카테고리 추가
9ecbec7 feat: 뽑기 티켓 시스템 — 보스 클리어 시 등급별 티켓 지급 (S=3, A=2, B=1)
d728866 docs: HandOFF.md 업데이트 — Phase 33~34 반영 (코드 품질 개선, 프로필 화면)
b408a6f feat: 프로필 화면 구현 — 누적 통계, 운동 히스토리, 계정 정보
1e58f21 fix: LoginViewModel SharedPreferences .apply() → .commit() — 플래그 저장 보장
188211f fix: SyncManager 전체 catch 블록에 경고 로그 추가
a16eebc fix: HomeViewModel 서브 로딩 함수 예외 로그 추가
0864de0 fix: 홈 화면 UI 개선 — 추천 퀘스트 XP 한 줄 표시, 주간 활동 체크 아이콘 변경
393806c fix: 보스 카드 높이 고정 — 클리어 전후 카드 크기 통일 (230dp)
ca28ffd fix: HomeViewModel retry 시 Flow 수집 코루틴 누적 방지 — Job 관리 추가
e4d65bc fix: 프로필 사진 EXIF 회전 보정 — 카메라 촬영 시 이미지 회전 문제 해결
6f62915 fix: 프로필 사진 이미지 처리를 IO 스레드로 이동 — ANR 방지
d81acb4 refactor: 스킨 시스템 재설계 — 텍스트 기반 15개 스킨, 장착 구현 중 알림
1271dce feat: 스킨 인벤토리 구현 — Room + Firestore 동기화
22d0e7f fix: 언더아머 티셔츠 이미지 배경 제거 — rembg AI 처리
5e69c4d fix: 뽑기 결과 카드 배경 제거 — PNG 투명도 그대로 표시
6630ba3 fix: 뽑기 애니메이션 흰 화면 버그 수정
ebdc8cb feat: 스킨 뽑기 기능 추가 — 뽑기 애니메이션 + 결과 공개
21d1f28 refactor: 아바타 회전 기능 제거 — 2D 단일 이미지로 단순화
7c88647 docs: HandOFF.md 업데이트 — 보스 Firestore 동기화, 최고 등급 보존, 세션 타임아웃 반영
678a0e9 feat: 세션 타임아웃 15분 — 백그라운드 15분 이내 복귀 시 로그인 스킵
2b64b4e fix: 보스 재도전 시 최고 등급 보존 — S > A > B 비교 후 상위 등급만 저장
98fe708 feat: 보스 진행 Firestore 동기화 — push/pull + 로그인 시 자동 동기화
(Phase 19~23 커밋들: 보스 전면 재설계, 전투 애니메이션, 그룹 가로 스크롤, 진행 시스템, 게이지 UX)
(Phase 15~18 커밋들: 아바타 360도 스프라이트, 직업 선택 UI 개편, 직업별 배율, 운동 완료 효과 표시)
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
