# BodyQuest Handoff Document

> 마지막 업데이트: 2026-03-22
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
| Gradle | 9.3.1 |

- DI: 수동 (BodyQuestApp에서 lazy 초기화, Hilt 미사용)
- 서버: 없음 (Room 로컬 DB만 사용)
- 테마: 항상 다크 모드 (다이나믹 컬러 없음)

---

## 프로젝트 구조

```
app/src/main/java/com/bodyquest/app/
├── BodyQuestApp.kt              # Application: DB + 3 Repository lazy 초기화
├── MainActivity.kt              # 단일 Activity, BodyQuestNavGraph 호출
│
├── data/
│   ├── local/
│   │   ├── BodyQuestDatabase.kt # Room DB (v1), SeedDatabaseCallback으로 초기 퀘스트 삽입
│   │   ├── SeedData.kt          # ~20개 하드코딩 퀘스트 (근력/지구력/밸런스)
│   │   ├── dao/
│   │   │   ├── UserDao.kt       # getUser(Flow), getUserOnce, insertUser, updateUser, addXp, updateLevel, updateStat
│   │   │   ├── QuestDao.kt      # getQuestsByCategory, getQuestsByBodyPart, getQuestById, getBodyParts
│   │   │   └── WorkoutDao.kt    # insertWorkout, updateWorkout, getWorkoutById, getTodaysCompleted, getWeekWorkouts, WorkoutSet CRUD
│   │   └── entity/
│   │       ├── UserEntity.kt    # id, nickname, job, goal, avatarIndex, strength/endurance/balanceStat, xp, level
│   │       ├── QuestEntity.kt   # id, category, bodyPart, name, difficulty, sets, repsPerSet, xpReward, statType, statReward
│   │       ├── WorkoutEntity.kt # id, questId, userId, startTime, endTime, elapsedSeconds, calories, heartRate, completed, xpEarned
│   │       └── WorkoutSetEntity.kt # id, workoutId(FK), setNumber, reps, completed, completedAt
│   └── repository/
│       ├── UserRepository.kt    # DAO 래퍼
│       ├── QuestRepository.kt   # DAO 래퍼
│       └── WorkoutRepository.kt # DAO 래퍼
│
├── domain/model/
│   ├── Job.kt                   # enum: STRENGTH(전사,Red), ENDURANCE(레인저,Blue), BALANCE(수호자,Green)
│   ├── Goal.kt                  # enum: DIET, BULK_UP, MAINTAIN
│   └── StatType.kt              # enum: STRENGTH(근력,Red), ENDURANCE(지구력,Blue), BALANCE(밸런스,Green)
│
├── ui/
│   ├── onboarding/
│   │   ├── OnboardingScreen.kt      # 3스텝 온보딩 (직업→목표→아바타)
│   │   ├── OnboardingViewModel.kt   # 상태관리, Factory 패턴
│   │   ├── JobSelectionPage.kt      # 3개 직업 카드
│   │   ├── GoalSelectionPage.kt     # 3개 목표 카드
│   │   ├── AvatarCreationPage.kt    # 닉네임 + 8개 이모지 아바타
│   │   └── StatInputPage.kt        # ⚠️ 미사용 (온보딩에서 제거됨, 파일만 존재)
│   │
│   ├── home/
│   │   ├── HomeScreen.kt           # 대시보드: 아바타카드, XP바, 스탯, 오늘퀘스트, 추천퀘스트, 주간활동
│   │   ├── HomeViewModel.kt        # user/todaysQuests/recommendedQuests/weekWorkoutDays 로딩
│   │   └── components/
│   │       ├── StatBar.kt          # 애니메이션 수평 스탯 바
│   │       ├── XpProgressBar.kt    # 골드 XP 프로그레스 바
│   │       └── TodayQuestCard.kt   # 완료 퀘스트 카드
│   │
│   ├── quest/
│   │   ├── QuestScreen.kt          # 3개 카테고리 선택 (근력/지구력/밸런스)
│   │   ├── QuestTreeScreen.kt      # 2단계 드릴다운: 부위 → 퀘스트 목록
│   │   ├── QuestDetailScreen.kt    # 퀘스트 상세 + "운동 시작!" 버튼
│   │   └── QuestViewModel.kt       # TreeLevel 상태머신 (BODY_PART ↔ QUEST_LIST)
│   │
│   ├── workout/
│   │   ├── WorkoutScreen.kt        # 타이머, 세트진행, 심박수/칼로리, 시작/일시정지/완료
│   │   ├── WorkoutCompleteScreen.kt # 운동요약, XP/스탯 보상, 레벨업 알림
│   │   └── WorkoutViewModel.kt     # 타이머, 심박수 시뮬레이션, XP/스탯 적용, Factory 패턴
│   │
│   ├── pvp/PvpScreen.kt            # 플레이스홀더 (Coming Soon)
│   ├── avatar/AvatarScreen.kt      # 플레이스홀더 (Coming Soon)
│   ├── profile/ProfileScreen.kt    # 플레이스홀더 (Coming Soon)
│   │
│   ├── navigation/
│   │   ├── Screen.kt               # sealed class: 모든 라우트 정의
│   │   ├── BottomNavBar.kt         # 5탭: 홈/퀘스트/PvP/아바타/프로필
│   │   └── BodyQuestNavGraph.kt    # NavHost + 모든 화면 라우팅
│   │
│   └── theme/
│       ├── Color.kt                # 다크 배경 + 네온 액센트 컬러
│       ├── Theme.kt                # 항상 다크 테마
│       └── Type.kt                 # 전체 타이포그래피
│
└── util/
    └── XpCalculator.kt             # xpForNextLevel(level*100), calculateNewLevel
```

---

## 핵심 설계 원칙

1. **레벨/XP ≠ 스탯**: XP는 운동 수행으로 쌓이고, 스탯은 실측 데이터(InBody 등)로만 변경
2. **자기 입력 스탯 금지**: 온보딩에서 스탯 입력 단계를 제거함 (초기값 0). 프로필에서 인바디 데이터 연동 예정
3. **3탭 규칙**: 퀘스트 접근은 카테고리 → 부위 → 퀘스트 최대 3탭
4. **오프라인 퍼스트**: 서버 없이 Room DB만으로 동작
5. **심박수/칼로리는 시뮬레이션**: 현재 실제 센서 연동 없음, 난이도 기반 시뮬레이션

---

## 현재 구현 완료 (프로토타입)

- [x] 온보딩 플로우 (직업/목표/아바타)
- [x] 홈 대시보드 (아바타, XP바, 스탯, 오늘퀘스트, 추천퀘스트 3개, 주간활동 체크)
- [x] 퀘스트 선택 (카테고리 → 부위 → 상세)
- [x] 운동 기록 (타이머, 세트 진행, 심박수/칼로리 시뮬레이션)
- [x] 운동 완료 (XP/스탯 반영, 레벨업)
- [x] 5탭 하단 네비게이션
- [x] 커스텀 앱 아이콘 (보라색 방패 + 덤벨)
- [x] 홈에서 추천퀘스트 클릭 → 퀘스트 상세로 바로 이동

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
- [ ] **DB 마이그레이션 전략** — Room version 올릴 때 대비

### 낮은 우선순위
- [ ] **서버 연동** — 유저 데이터 클라우드 동기화
- [ ] **소셜 기능** — 친구, 길드
- [ ] **업적/칭호 시스템**
- [ ] **다국어 지원**

---

## 알려진 이슈 & 주의사항

1. **StatInputPage.kt** — 온보딩에서 제거했지만 파일이 남아있음. 삭제해도 무방
2. **심박수/칼로리** — 현재 난이도 기반 시뮬레이션, 실제 센서 연동 시 WorkoutViewModel 수정 필요
3. **추천 퀘스트** — `shuffled()` 사용으로 앱 재진입마다 바뀜
4. **주간 활동 weekStart** — `Calendar.set(DAY_OF_WEEK)` 로케일 이슈 있어서 while 루프로 수동 계산하도록 수정함
5. **XP 적용 로직** — WorkoutViewModel.finishWorkout()에서 addXp + updateUser 두 번 호출됨, 리팩토링 여지 있음
6. **mipmap webp 아이콘** — hdpi~xxxhdpi에 기본 Android webp 아이콘이 남아있음. adaptive-icon(anydpi-v26)이 우선 적용되므로 동작에 문제없음

---

## Git 커밋 히스토리

```
07ac854 docs: README 작성
bf950e4 feat: BodyQuest 프로토타입 구현
69c28a1 Initial commit: BodyQuest empty Android project
```

---

## 빌드 & 실행

```bash
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

디바이스 테스트: Android Studio에서 USB 디버깅으로 실행 또는 APK 직접 설치.
