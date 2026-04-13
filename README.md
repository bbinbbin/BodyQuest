# BodyQuest

운동을 RPG 퀘스트로 수행하여 캐릭터를 성장시키는 Android 피트니스 앱.

## 컨셉

운동을 퀘스트로 수행하고, XP와 스탯을 쌓아 캐릭터를 키워나가는 게임형 운동 앱입니다.

- **퀘스트 시스템** — 근력/지구력/밸런스 3개 카테고리, 46개 운동 퀘스트
- **캐릭터 성장** — 운동 완료 시 XP 획득 & 레벨업, 스탯(근력/지구력) 성장
- **직업 시스템** — STRENGTH(근력 ×2.0) / ENDURANCE(지구력 ×2.0) / BALANCE(전체 ×1.5)
- **보스 시스템** — 150보스(3타입×50), 순서 잠금, S/A/B 등급 평가, 전투 애니메이션
- **스킨 & 뽑기** — 보스 클리어 시 뽑기 티켓 획득, 스킨 장착으로 아바타 커스터마이징
- **스탯은 실측 데이터만** — 인바디, 체력 테스트 등 실제 데이터로만 스탯 반영 (자기 입력 없음)

## 기술 스택

| 항목 | 버전/기술 |
|------|-----------|
| 언어 | Kotlin 2.1.20 |
| UI | Jetpack Compose + Material 3 (BOM 2024.09.00) |
| 로컬 DB | Room 2.7.1 (DB v17, exportSchema=true) |
| 네비게이션 | Navigation Compose 2.9.0 |
| 상태 관리 | ViewModel + StateFlow (Lifecycle 2.10.0) |
| DI | Hilt 2.56.2 |
| 인증 | Firebase Auth (이메일/비밀번호 + Google Sign-In) |
| 클라우드 DB | Firebase Firestore (asia-northeast3) |
| 이미지 | Coil 2.6.0 |
| 보안 | EncryptedSharedPreferences, network_security_config (HTTPS 강제) |
| 빌드 | AGP 8.9.1, Gradle 9.3.1, R8 난독화 |
| Wear OS | Wear Compose 1.4.0 + Data Layer API (play-services-wearable 19.0.0) |
| 최소 SDK | 24 (폰) / 26 (워치) / targetSdk 36 (폰) / 34 (워치) / compileSdk 36 |

## 프로젝트 구조

```
app/src/main/java/com/bodyquest/app/
├── BodyQuestApp.kt              # @HiltAndroidApp Application
├── MainActivity.kt              # @AndroidEntryPoint
│
├── di/                          # Hilt DI 모듈
│   ├── DatabaseModule.kt        # DB, DAO, Repository
│   ├── AuthModule.kt            # FirebaseAuth, AuthRepository
│   ├── FirestoreModule.kt       # Firestore, SyncManager
│   └── SecurityModule.kt        # EncryptedSharedPreferences
│
├── data/
│   ├── local/
│   │   ├── BodyQuestDatabase.kt # Room DB v17, Migration(1,2)~(16,17)
│   │   ├── SeedData.kt          # STRENGTH 29개 + ENDURANCE 8개 + BALANCE 9개 + 보스 150개 (inputType 지정)
│   │   ├── dao/                  # UserDao, QuestDao, WorkoutDao, BossProgressDao, SkinInventoryDao
│   │   └── entity/              # User, Quest, Workout, WorkoutSet, BossProgress, SkinInventory
│   ├── remote/
│   │   ├── FirestoreUserService.kt  # Firestore CRUD
│   │   └── SyncManager.kt          # 동기화 오케스트레이션
│   └── repository/              # interface + Local 구현체
│
├── domain/model/                # Job, Goal, StatType, ExerciseInputType, SkinItem, AuthResult
│
├── ui/
│   ├── splash/                  # 스플래시 (세션 타임아웃 15분 체크)
│   ├── intro/                   # 인트로 슬라이드 5장
│   ├── login/                   # 로그인/회원가입 (Firebase Auth)
│   ├── onboarding/              # 온보딩 (직업→목표→아바타)
│   ├── home/                    # 홈 대시보드
│   ├── quest/                   # 퀘스트 선택 (카테고리→부위→퀘스트)
│   ├── workout/                 # 운동 기록 & 완료
│   ├── boss/                    # 보스 전투 (150보스, 등급 평가, 전투 오버레이)
│   ├── avatar/                  # 아바타 (스킨 착용 표시)
│   ├── gacha/                   # 스킨 뽑기 (3단계 애니메이션)
│   ├── inventory/               # 스킨 인벤토리 (장착/해제)
│   ├── profile/                 # 프로필 (통계, 달력, 주간 그래프, 계정 관리)
│   ├── pvp/                     # PvP (Coming Soon)
│   ├── navigation/              # NavGraph, BottomNavBar, Screen routes
│   ├── common/                  # UiState, LoadingScreen, ErrorScreen
│   └── theme/                   # 다크 게이밍 테마
│
└── util/                        # XpCalculator, ImageUtil, AppLogger

wear/src/main/java/com/bodyquest/wear/
├── BodyQuestWearApp.kt          # @HiltAndroidApp
├── MainActivity.kt              # @AndroidEntryPoint
├── di/WearableModule.kt         # NodeClient, MessageClient 제공
├── data/PhoneConnectionRepository.kt  # 폰 연결 감지 + 메시지 송신
└── ui/
    ├── WearHomeScreen.kt        # 연결 상태 + 테스트 핑
    ├── WearHomeViewModel.kt     # 연결 모니터링
    └── theme/Theme.kt           # Wear Material 테마
```

## 주요 기능

### 인증 & 동기화
- Firebase Auth (이메일/비밀번호 + Google Sign-In)
- 아이디 저장 기능 (EncryptedSharedPreferences)
- Firestore 클라우드 동기화 (Write-Through + Pull-on-Login, 스탯 max merge)
- 세션 타임아웃 15분 (백그라운드 복귀 시 자동 판단)
- 계정 삭제 (Firestore + Room + Auth 완전 삭제, 클라이언트 권한 검증)

### 퀘스트 & 운동
- 46개 운동 퀘스트 (STRENGTH 29개, ENDURANCE 8개, BALANCE 9개)
- inputType 기반 운동 입력 분기: WEIGHT_REPS(중량+횟수) / REPS_ONLY(횟수만) / TIME_ONLY(시간) / MIXED(횟수+시간)
- WEIGHT_REPS/REPS_ONLY/MIXED: 세트 테이블 UI (무게/횟수 입력, 개별 체크)
- TIME_ONLY STRENGTH: 세트별 목표 시간 설정 + 카운트업 타이머 (목표 달성 시 완료, 미달 시 무효)
- ENDURANCE/BALANCE: 전체 타이머 UI
- 세트 간 휴식 타이머: 60초 카운트다운 풀스크린 오버레이 + 건너뛰기
- 운동 완료 요약: 총 세트/횟수/볼륨(kg) 조건부 표시
- 직업별 스탯 배율 적용, BALANCE 양쪽 스탯 분배
- 운동 GIF 가이드 46개 (Coil GIF 디코더)

### 보스 시스템
- 150보스 (근력 50 + 지구력 50 + 하이브리드 50)
- 순서 잠금, 영구 클리어, 재도전
- S/A/B 등급 평가 (최고 등급 보존)
- 전투 애니메이션 오버레이

### 스킨 & 뽑기
- 보스 클리어 시 뽑기 티켓 획득 (S=3, A=2, B=1)
- 3단계 뽑기 애니메이션 (IDLE → SPINNING → REVEALED)
- TOP/BOTTOM/HAT 슬롯 개별 장착 + SET 스킨 (장착 시 전체 슬롯 초기화)
- 남성 스킨 6종 (개별 3 + 세트 3) / 여성 스킨 6종 (개별 4 + 세트 2)
- 조합별 결과 이미지 룩업 (여성 12개, 남성 8개)
- 스킨 분해: 60% 확률로 뽑기 티켓 1장 획득
- 인벤토리 관리 (티켓 수 표시) + Firestore 동기화

### 프로필
- 누적 통계 (총 운동, 총 XP, 운동 시간, 보스 클리어)
- 운동 히스토리 달력 UI (월 이동, 날짜별 상세)
- 주간 운동 바 차트
- 프로필 사진 (갤러리/카메라, Base64 저장)

## 빌드 & 실행

```bash
# 폰 앱 디버그 빌드
./gradlew :app:assembleDebug

# 워치 앱 디버그 빌드
./gradlew :wear:assembleDebug

# 릴리즈 빌드 (서명 설정 필요)
./gradlew assembleRelease

# APK 위치
app/build/outputs/apk/debug/app-debug.apk
wear/build/outputs/apk/debug/wear-debug.apk
```

Android Studio에서 직접 실행하거나, APK를 디바이스에 설치하여 테스트할 수 있습니다.

## 디자인

항상 다크 모드 게이밍 테마를 사용합니다.

- **배경**: #0D0D1A (Deep Dark)
- **메인 색상**: #8B5CF6 (Neon Purple)
- **근력**: #EF4444 (Red) / **지구력**: #3B82F6 (Blue) / **밸런스**: #10B981 (Green)
- **XP**: #FFD740 (Gold)

## 앱 플로우

```
Splash → [첫 기기] Intro(5장) → Login
       → [세션 만료] Login
       → [세션 유효] Home 직행
Login  → [신규] Onboarding(직업→목표→아바타) → Home
       → [기존] Home
```
