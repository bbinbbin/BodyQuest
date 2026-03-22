# BodyQuest

실제 운동 데이터로 캐릭터를 성장시키는 피트니스 RPG 앱.

## 컨셉

운동을 퀘스트로 수행하고, XP와 스탯을 쌓아 캐릭터를 키워나가는 게임형 운동 앱입니다.

- **퀘스트 시스템** — 근력/지구력/밸런스 카테고리의 운동 퀘스트 수행
- **캐릭터 성장** — 운동 완료 시 XP 획득 & 레벨업, 스탯(근력/지구력/밸런스) 성장
- **직업 시스템** — 전사(근력), 레인저(지구력), 수호자(밸런스) 중 선택
- **스탯은 실측 데이터만** — 인바디, 체력 테스트 등 실제 데이터로만 스탯 반영 (자기 입력 없음)

## 기술 스택

| 영역 | 기술 |
|------|------|
| 언어 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 로컬 DB | Room |
| 네비게이션 | Jetpack Compose Navigation |
| 상태 관리 | ViewModel + StateFlow |
| 어노테이션 | KSP |
| DI | Manual (Application class) |
| 최소 SDK | 24 (Android 7.0) |

## 프로젝트 구조

```
app/src/main/java/com/bodyquest/app/
├── data/
│   ├── local/
│   │   ├── dao/          # Room DAO (User, Quest, Workout)
│   │   ├── entity/       # Room Entity
│   │   ├── BodyQuestDatabase.kt
│   │   └── SeedData.kt   # 초기 퀘스트 데이터
│   └── repository/       # Repository layer
├── domain/model/         # Job, Goal, StatType enums
├── ui/
│   ├── onboarding/       # 온보딩 (직업/목표/아바타 선택)
│   ├── home/             # 홈 대시보드
│   ├── quest/            # 퀘스트 선택 (카테고리→부위→퀘스트)
│   ├── workout/          # 운동 기록 & 완료
│   ├── pvp/              # PvP (Coming Soon)
│   ├── avatar/           # 아바타 (Coming Soon)
│   ├── profile/          # 프로필 (Coming Soon)
│   ├── navigation/       # NavGraph, BottomNavBar, Screen routes
│   └── theme/            # 다크 게이밍 테마 (Color, Theme, Type)
├── util/                 # XpCalculator
├── BodyQuestApp.kt       # Application class
└── MainActivity.kt
```

## 현재 구현 상태 (프로토타입)

- [x] 온보딩 플로우 (직업/목표/아바타 선택)
- [x] 홈 대시보드 (스탯, XP, 추천 퀘스트, 오늘의 퀘스트, 주간 활동)
- [x] 퀘스트 선택 (카테고리 → 부위 → 퀘스트 상세)
- [x] 운동 기록 (타이머, 세트 진행, 심박수/칼로리 시뮬레이션)
- [x] 운동 완료 (XP/스탯 반영, 레벨업)
- [x] 5탭 하단 네비게이션
- [x] 커스텀 앱 아이콘
- [ ] PvP 대전
- [ ] 아바타 커스터마이징
- [ ] 프로필 & 인바디 데이터 연동
- [ ] 서버 연동

## 빌드 & 실행

```bash
# 디버그 빌드
./gradlew assembleDebug

# APK 위치
app/build/outputs/apk/debug/app-debug.apk
```

Android Studio에서 직접 실행하거나, APK를 디바이스에 설치하여 테스트할 수 있습니다.

## 디자인

다크 게이밍 테마를 기반으로 합니다.

- **배경**: #0D0D1A (Deep Dark)
- **메인 색상**: #8B5CF6 (Neon Purple)
- **근력**: #EF4444 (Red) / **지구력**: #3B82F6 (Blue) / **밸런스**: #10B981 (Green)
- **XP**: #FFD740 (Gold)
