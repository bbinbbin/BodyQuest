# BodyQuest Handoff Document

> 마지막 업데이트: 2026-03-23 (아키텍처 리팩토링 Phase 1~7 완료)
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
| Gradle | 9.3.1 |

- **DI**: Hilt (`@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`)
- **서버**: 없음 (Room 로컬 DB만 사용, 향후 클라우드 연동 대비 Repository 추상화 완료)
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
│   └── SecurityModule.kt        # @Module: EncryptedSharedPreferences 제공
│
├── data/
│   ├── local/
│   │   ├── BodyQuestDatabase.kt # Room DB (v2), exportSchema=true, Migration(1,2)
│   │   ├── SeedData.kt          # ~20개 하드코딩 퀘스트 (근력/지구력/밸런스)
│   │   ├── dao/
│   │   │   ├── UserDao.kt       # abstract class, @Transaction applyWorkoutRewards()
│   │   │   ├── QuestDao.kt      # 카테고리/부위/난이도 필터링
│   │   │   └── WorkoutDao.kt    # 운동 기록 CRUD, 시간 기반 쿼리
│   │   └── entity/
│   │       ├── UserEntity.kt    # id, nickname, job, goal, avatarIndex, stats, xp, level
│   │       ├── QuestEntity.kt   # id, category, bodyPart, name, difficulty, rewards
│   │       ├── WorkoutEntity.kt # FK(userId→users, questId→quests), 복합인덱스, CASCADE
│   │       └── WorkoutSetEntity.kt # FK(workoutId→workouts), CASCADE
│   └── repository/
│       ├── UserRepository.kt        # interface
│       ├── LocalUserRepository.kt   # 로컬 구현체
│       ├── QuestRepository.kt       # interface
│       ├── LocalQuestRepository.kt  # 로컬 구현체
│       ├── WorkoutRepository.kt     # interface
│       └── LocalWorkoutRepository.kt # 로컬 구현체
│
├── domain/model/
│   ├── Job.kt                   # enum: STRENGTH/ENDURANCE/BALANCE
│   ├── Goal.kt                  # enum: DIET/BULK_UP/MAINTAIN
│   └── StatType.kt              # enum: STRENGTH/ENDURANCE/BALANCE
│
├── ui/
│   ├── common/
│   │   ├── UiState.kt           # sealed interface: Loading/Success/Error
│   │   └── CommonUi.kt          # LoadingScreen, ErrorScreen 공통 컴포넌트
│   │
│   ├── splash/
│   │   ├── SplashScreen.kt      # "시작하기" 버튼, SplashViewModel 사용
│   │   └── SplashViewModel.kt   # @HiltViewModel, user 확인 → 온보딩/홈 분기
│   │
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt      # 3스텝 (직업→목표→아바타)
│   │   ├── OnboardingViewModel.kt   # @HiltViewModel, isSaving/error 상태, try-catch
│   │   ├── JobSelectionPage.kt
│   │   ├── GoalSelectionPage.kt
│   │   └── AvatarCreationPage.kt
│   │
│   ├── home/
│   │   ├── HomeScreen.kt           # UiState 분기 (Loading/Error/Success)
│   │   ├── HomeViewModel.kt        # @HiltViewModel, UiState<HomeState>, combine()
│   │   └── components/
│   │       ├── StatBar.kt
│   │       ├── XpProgressBar.kt
│   │       └── TodayQuestCard.kt
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
│   │   └── WorkoutViewModel.kt     # @HiltViewModel, 트랜잭션 기반 보상 적용, try-catch
│   │
│   ├── pvp/PvpScreen.kt            # Coming Soon
│   ├── avatar/AvatarScreen.kt      # Coming Soon
│   ├── profile/ProfileScreen.kt    # Coming Soon
│   │
│   ├── navigation/
│   │   ├── Screen.kt               # sealed class: 모든 라우트
│   │   ├── BottomNavBar.kt         # 5탭
│   │   └── BodyQuestNavGraph.kt    # hiltViewModel() 사용, repository 파라미터 없음
│   │
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── util/
    └── XpCalculator.kt
```

---

## 핵심 설계 원칙

1. **레벨/XP ≠ 스탯**: XP는 운동 수행으로 쌓이고, 스탯은 실측 데이터(InBody 등)로만 변경
2. **자기 입력 스탯 금지**: 온보딩에서 스탯 입력 단계 제거 (초기값 0)
3. **3탭 규칙**: 퀘스트 접근은 카테고리 → 부위 → 퀘스트 최대 3탭
4. **오프라인 퍼스트**: 서버 없이 Room DB만으로 동작
5. **심박수/칼로리는 시뮬레이션**: 현재 실제 센서 연동 없음

---

## 완료된 아키텍처 리팩토링 (Phase 1~7)

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

---

## 현재 구현 완료

- [x] 온보딩 플로우 (직업/목표/아바타)
- [x] 홈 대시보드 (아바타, XP바, 스탯, 오늘퀘스트, 추천퀘스트, 주간활동)
- [x] 퀘스트 선택 (카테고리 → 부위 → 상세)
- [x] 운동 기록 (타이머, 세트 진행, 심박수/칼로리 시뮬레이션)
- [x] 운동 완료 (XP/스탯 트랜잭션 기반 반영, 레벨업)
- [x] 5탭 하단 네비게이션
- [x] 커스텀 앱 아이콘
- [x] 스플래시 화면
- [x] Hilt DI
- [x] Sealed UI State (Loading/Error/Success)
- [x] DB 인덱스/FK/마이그레이션
- [x] 네트워크 보안 설정
- [x] Repository 추상화 (interface + Local)
- [x] EncryptedSharedPreferences
- [x] R8 난독화 + ProGuard

---

## 미구현 / 다음 작업 후보

### 높은 우선순위
- [ ] **프로필 화면** — 운동 히스토리, 누적 통계, 계정 설정
- [ ] **인바디 데이터 입력** — 프로필에서 인바디 수치 기록 → 스탯 반영
- [ ] **퀘스트 데이터 확장** — 현재 ~20개 → 더 많은 운동 추가
- [ ] **앱 재시작 시 추천퀘스트 고정** — 현재 shuffle로 매번 바뀜, 하루 단위 고정 필요

### 중간 우선순위
- [ ] **아바타 시스템** — 레벨/직업별 장비, 외형 커스터마이징
- [ ] **PvP 대전** — 스탯 기반 1:1 비교 대결
- [ ] **알림/리마인더** — 운동 시간 알림
- [ ] **운동 중 실제 센서 연동** — Google Fit / Health Connect API
- [ ] **클라우드 DB 연동** — Firebase/Supabase + Repository 원격 구현체

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

---

## Git 커밋 히스토리

```
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
