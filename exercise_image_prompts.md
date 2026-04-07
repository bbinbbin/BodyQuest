# BodyQuest 운동 GIF 생성 프롬프트

> 운동별 시작/완료 자세 2장 생성 → rembg 배경 제거 → 2프레임 GIF 합성

---

## 공통 프롬프트 템플릿

모든 운동에 아래 스타일 프리픽스를 붙인 후, 자세 설명만 교체합니다.

```
anatomical exercise illustration, realistic human figure with visible muscle definition,
target muscles highlighted in red/orange color, clean white background,
detailed exercise form showing proper technique, gym equipment included if needed,
medical illustration style, centered composition, full body visible, side view,
no text, no watermark, 1024x1024
```

> **참고 (레퍼런스: 헤비(Hevy) 앱 스타일)**
> - 리얼한 인체 비율 + 타겟 근육 빨간색/주황색 하이라이트
> - 흰색 배경 (rembg로 제거 후 앱 다크 테마에서 투명하게 표시)
> - `side view`는 운동에 따라 `front view` / `3/4 view`로 변경
> - 기구(바벨, 덤벨, 벤치, 케이블 머신 등)도 함께 표현
> - 한 세션에서 최대한 많이 생성하여 스타일 일관성 유지

---

## STRENGTH: 가슴 (5개)

### 1. 푸시업 (str_chest_pushup)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `push-up top position, arms fully extended, body in straight plank line, side view` |
| B (완료) | `push-up bottom position, arms bent 90 degrees, chest near floor, body straight, side view` |

### 2. 벤치프레스 (str_chest_bench_press)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying on flat bench, arms fully extended upward holding barbell, side view` |
| B (완료) | `lying on flat bench, barbell lowered to chest, elbows bent 90 degrees, side view` |

### 3. 인클라인 프레스 (str_chest_incline_press)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying on incline bench (45 degrees), arms fully extended upward holding barbell, side view` |
| B (완료) | `lying on incline bench (45 degrees), barbell lowered to upper chest, elbows bent, side view` |

### 4. 덤벨 플라이 (str_chest_dumbbell_fly)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying on flat bench, both arms extended upward holding dumbbells together, front view` |
| B (완료) | `lying on flat bench, arms spread wide to sides holding dumbbells, slight elbow bend, front view` |

### 5. 딥스 (str_chest_dips)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `on parallel bars, arms fully extended, body upright, slight forward lean, front view` |
| B (완료) | `on parallel bars, arms bent, body lowered, elbows at 90 degrees, slight forward lean, front view` |

---

## STRENGTH: 등 (5개)

### 6. 풀업 (str_back_pullup)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `hanging from pull-up bar, arms fully extended, body straight, front view` |
| B (완료) | `chin above pull-up bar, arms bent, body pulled up, front view` |

### 7. 바벨 로우 (str_back_barbell_row)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `bent over at 45 degrees, arms extended downward holding barbell, side view` |
| B (완료) | `bent over at 45 degrees, barbell pulled up to lower chest, elbows behind body, side view` |

### 8. 랫풀다운 (str_back_lat_pulldown)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `seated at cable machine, arms extended upward gripping wide bar, front view` |
| B (완료) | `seated at cable machine, bar pulled down to upper chest, elbows at sides, front view` |

### 9. 시티드 로우 (str_back_seated_row)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `seated at cable row machine, arms extended forward gripping handle, torso upright, side view` |
| B (완료) | `seated at cable row machine, handle pulled to abdomen, elbows behind body, chest out, side view` |

### 10. 데드리프트 (str_back_deadlift)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing upright, holding barbell at hip level, arms straight down, side view` |
| B (완료) | `bent forward at hips, barbell at shin level, knees slightly bent, back flat, side view` |

---

## STRENGTH: 하체 (5개)

### 11. 스쿼트 (str_legs_squat)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing upright, barbell on upper back/shoulders, feet shoulder width apart, side view` |
| B (완료) | `deep squat position, thighs parallel to floor, barbell on upper back, side view` |

### 12. 레그프레스 (str_legs_leg_press)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `seated in leg press machine, legs extended pushing platform, side view` |
| B (완료) | `seated in leg press machine, knees bent 90 degrees toward chest, side view` |

### 13. 런지 (str_legs_lunge)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing upright, feet together, hands on hips, side view` |
| B (완료) | `front leg bent 90 degrees in lunge position, back knee near floor, torso upright, side view` |

### 14. 레그컬 (str_legs_leg_curl)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying face down on leg curl machine, legs extended straight, side view` |
| B (완료) | `lying face down on leg curl machine, heels curled up toward glutes, side view` |

### 15. 불가리안 스플릿 스쿼트 (str_legs_bulgarian_split)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing with rear foot elevated on bench behind, front leg straight, holding dumbbells, side view` |
| B (완료) | `rear foot on bench, front knee bent 90 degrees in deep lunge, torso upright, holding dumbbells, side view` |

---

## STRENGTH: 어깨 (5개)

### 16. 숄더프레스 (str_shoulder_press)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `seated, holding dumbbells at shoulder level, elbows bent 90 degrees, front view` |
| B (완료) | `seated, arms fully extended overhead holding dumbbells, front view` |

### 17. 사이드 레터럴 레이즈 (str_shoulder_lateral_raise)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing upright, arms at sides holding dumbbells, front view` |
| B (완료) | `standing upright, arms raised to sides at shoulder height holding dumbbells, T-shape, front view` |

### 18. 프론트 레이즈 (str_shoulder_front_raise)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing upright, arms at sides holding dumbbells in front of thighs, side view` |
| B (완료) | `standing upright, arms raised straight forward to shoulder height holding dumbbells, side view` |

### 19. 페이스풀 (str_shoulder_face_pull)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing facing cable machine, arms extended forward gripping rope attachment, front view` |
| B (완료) | `standing, rope pulled to face level, elbows flared out high, hands near ears, front view` |

### 20. 밀리터리 프레스 (str_shoulder_military_press)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing, holding barbell at shoulder/collarbone level, elbows forward, front view` |
| B (완료) | `standing, arms fully extended overhead holding barbell, front view` |

---

## STRENGTH: 팔 (4개)

### 21. 바이셉 컬 (str_arms_bicep_curl)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing upright, arms extended down holding dumbbells, palms forward, front view` |
| B (완료) | `standing upright, dumbbells curled up to shoulder level, elbows at sides, front view` |

### 22. 트라이셉 익스텐션 (str_arms_tricep_extension)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing, both hands holding one dumbbell behind head, elbows pointing up, side view` |
| B (완료) | `standing, arms extended overhead holding dumbbell, elbows straight, side view` |

### 23. 해머 컬 (str_arms_hammer_curl)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing upright, arms at sides holding dumbbells with neutral grip (palms facing body), front view` |
| B (완료) | `standing upright, dumbbells curled up to shoulder level with neutral grip, elbows at sides, front view` |

### 24. 클로즈그립 벤치프레스 (str_arms_close_grip_bench)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying on flat bench, arms extended upward holding barbell with narrow grip (hands close together), side view` |
| B (완료) | `lying on flat bench, barbell lowered to lower chest with narrow grip, elbows tucked close to body, side view` |

---

## STRENGTH: 코어 (5개)

### 25. 플랭크 (str_core_plank)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `forearm plank position, body in straight line from head to heels, forearms on ground, side view` |
| B (완료) | `forearm plank position, body in straight line, slight emphasis on engaged core (tighter form), side view` |

> 플랭크는 정적 운동이므로 A/B 차이가 미미합니다. 약간의 각도 변화나 호흡 표현으로 차별화합니다.

### 26. 크런치 (str_core_crunch)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying on back, knees bent, feet flat on floor, hands behind head, shoulders on ground, side view` |
| B (완료) | `lying on back, knees bent, upper back curled up off floor, hands behind head, crunching forward, side view` |

### 27. 레그레이즈 (str_core_leg_raise)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying flat on back, legs extended straight on floor, arms at sides, side view` |
| B (완료) | `lying flat on back, legs raised straight up to 90 degrees, arms at sides, side view` |

### 28. 바이시클 크런치 (str_core_bicycle_crunch)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying on back, hands behind head, right elbow touching left knee, right leg extended, side view` |
| B (완료) | `lying on back, hands behind head, left elbow touching right knee, left leg extended, side view` |

### 29. 행잉 레그레이즈 (str_core_hanging_leg_raise)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `hanging from pull-up bar, arms extended, legs hanging straight down, front view` |
| B (완료) | `hanging from pull-up bar, legs raised straight forward to 90 degrees (L-shape), front view` |

---

## ENDURANCE: 달리기 (4개)

### 30. 가볍게 달리기 (end_light_run)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `jogging pose, right foot forward, left arm forward, relaxed stride, side view` |
| B (완료) | `jogging pose, left foot forward, right arm forward, relaxed stride, side view` |

### 31. 인터벌 트레이닝 (end_interval)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `sprinting pose, explosive stride, right knee high, left arm pumping forward, side view` |
| B (완료) | `sprinting pose, explosive stride, left knee high, right arm pumping forward, side view` |

### 32. 장거리 달리기 (end_long_distance)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `steady running pose, right foot forward, upright posture, efficient form, side view` |
| B (완료) | `steady running pose, left foot forward, upright posture, efficient form, side view` |

### 33. 회복 조깅 (end_recovery_jog)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `slow light jogging pose, short stride, right foot forward, very relaxed form, side view` |
| B (완료) | `slow light jogging pose, short stride, left foot forward, very relaxed form, side view` |

---

## ENDURANCE: 자전거 (1개)

### 34. 사이클링 (end_cycling)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `riding stationary bike, right leg extended on pedal, left leg bent up, hands on handlebars, side view` |
| B (완료) | `riding stationary bike, left leg extended on pedal, right leg bent up, hands on handlebars, side view` |

---

## ENDURANCE: 줄넘기 (3개)

### 35. 줄넘기 기초 (end_jumprope_beginner)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing with jump rope, feet on ground, rope behind body, arms at sides holding handles, front view` |
| B (완료) | `mid-jump with jump rope, feet off ground, rope passing overhead, slight knee bend, front view` |

### 36. 줄넘기 인터벌 (end_jumprope_intermediate)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing with jump rope, feet on ground, ready position, front view` |
| B (완료) | `high jump with jump rope, knees tucked slightly higher, rope under feet, dynamic pose, front view` |

### 37. 줄넘기 고강도 (end_jumprope_advanced)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing with jump rope, athletic stance, ready for double under, front view` |
| B (완료) | `explosive high jump, arms crossed (crossover), rope under feet, intense dynamic pose, front view` |

---

## BALANCE: 요가 (3개)

### 38. 입문 요가 (bal_yoga_beginner)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing mountain pose (tadasana), arms at sides, feet together, upright posture, front view` |
| B (완료) | `warrior I pose (virabhadrasana I), front knee bent, arms extended overhead, side view` |

### 39. 빈야사 요가 (bal_yoga_intermediate)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `downward facing dog pose, inverted V shape, hands and feet on floor, side view` |
| B (완료) | `upward facing dog pose (cobra), arms extended, chest lifted, hips near floor, side view` |

### 40. 파워 요가 (bal_yoga_advanced)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `crow pose (bakasana), hands on floor, knees resting on upper arms, feet off ground, front view` |
| B (완료) | `warrior III pose, standing on one leg, body and other leg parallel to floor, arms forward, side view` |

---

## BALANCE: 스트레칭 (3개)

### 41. 기본 스트레칭 (bal_stretch_beginner)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing upright, arms at sides, neutral relaxed posture, front view` |
| B (완료) | `standing, reaching both arms overhead in full body stretch, slight back arch, front view` |

### 42. 동적 스트레칭 (bal_stretch_intermediate)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `standing leg swing position, one leg swinging forward, arms out for balance, side view` |
| B (완료) | `standing leg swing position, same leg swinging backward, arms out for balance, side view` |

### 43. 딥 스트레칭 (bal_stretch_advanced)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `seated forward fold, legs extended, reaching toward toes, side view` |
| B (완료) | `pigeon pose, one leg forward bent, back leg extended behind, torso upright, side view` |

---

## BALANCE: 필라테스 (3개)

### 44. 입문 필라테스 (bal_pilates_beginner)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `lying on back, knees bent, arms at sides, neutral spine position (mat pilates starting), side view` |
| B (완료) | `hundred position, legs raised at 45 degrees, arms extended alongside body, upper back curled up, side view` |

### 45. 중급 필라테스 (bal_pilates_intermediate)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `seated V-sit position (teaser prep), knees bent, arms reaching forward, balanced on sit bones, side view` |
| B (완료) | `full teaser position, legs extended at 45 degrees, arms parallel to legs, balanced on sit bones, side view` |

### 46. 고급 필라테스 (bal_pilates_advanced)
| 프레임 | 자세 설명 |
|--------|-----------|
| A (시작) | `side plank position, one arm extended, body in straight line, side view` |
| B (완료) | `side plank with leg lift, top leg raised, top arm reaching up, star shape, side view` |

---

## GIF 합성 스크립트

```python
"""
사용법:
1. pip install rembg Pillow
2. 이미지를 images/raw/ 폴더에 {id}_a.png, {id}_b.png 형식으로 저장
3. python make_exercise_gifs.py
4. 결과물: images/gif/ 폴더에 exercise_{id}.gif 생성
"""

import os
import io
from PIL import Image
from rembg import remove

RAW_DIR = "images/raw"
GIF_DIR = "images/gif"
SIZE = (512, 512)
FRAME_DURATION = 800  # ms

EXERCISES = [
    "str_chest_pushup", "str_chest_bench_press", "str_chest_incline_press",
    "str_chest_dumbbell_fly", "str_chest_dips",
    "str_back_pullup", "str_back_barbell_row", "str_back_lat_pulldown",
    "str_back_seated_row", "str_back_deadlift",
    "str_legs_squat", "str_legs_leg_press", "str_legs_lunge",
    "str_legs_leg_curl", "str_legs_bulgarian_split",
    "str_shoulder_press", "str_shoulder_lateral_raise", "str_shoulder_front_raise",
    "str_shoulder_face_pull", "str_shoulder_military_press",
    "str_arms_bicep_curl", "str_arms_tricep_extension", "str_arms_hammer_curl",
    "str_arms_close_grip_bench",
    "str_core_plank", "str_core_crunch", "str_core_leg_raise",
    "str_core_bicycle_crunch", "str_core_hanging_leg_raise",
    "end_light_run", "end_interval", "end_long_distance", "end_recovery_jog",
    "end_cycling",
    "end_jumprope_beginner", "end_jumprope_intermediate", "end_jumprope_advanced",
    "bal_yoga_beginner", "bal_yoga_intermediate", "bal_yoga_advanced",
    "bal_stretch_beginner", "bal_stretch_intermediate", "bal_stretch_advanced",
    "bal_pilates_beginner", "bal_pilates_intermediate", "bal_pilates_advanced",
]

def remove_bg(img_path: str) -> Image.Image:
    with open(img_path, "rb") as f:
        result = remove(f.read())
    return Image.open(io.BytesIO(result)).convert("RGBA")

def make_gif(id: str):
    a_path = os.path.join(RAW_DIR, f"{id}_a.png")
    b_path = os.path.join(RAW_DIR, f"{id}_b.png")

    if not os.path.exists(a_path) or not os.path.exists(b_path):
        print(f"  SKIP {id} (파일 없음)")
        return

    img_a = remove_bg(a_path).resize(SIZE, Image.LANCZOS)
    img_b = remove_bg(b_path).resize(SIZE, Image.LANCZOS)

    out_path = os.path.join(GIF_DIR, f"exercise_{id}.gif")
    img_a.save(
        out_path,
        save_all=True,
        append_images=[img_b],
        duration=FRAME_DURATION,
        loop=0,
        disposal=2,  # 이전 프레임 지우고 새로 그림 (투명 배경 깨짐 방지)
    )
    print(f"  OK {out_path}")

if __name__ == "__main__":
    os.makedirs(GIF_DIR, exist_ok=True)
    print(f"총 {len(EXERCISES)}개 운동 처리 시작\n")
    for ex_id in EXERCISES:
        make_gif(ex_id)
    print(f"\n완료! {GIF_DIR} 폴더를 확인하세요.")
```

---

## 작업 체크리스트

- [ ] 공통 프롬프트 스타일 테스트 (2~3개 운동으로 먼저 생성해서 스타일 확인)
- [ ] STRENGTH 가슴 5개 × 2장 = 10장 생성
- [ ] STRENGTH 등 5개 × 2장 = 10장 생성
- [ ] STRENGTH 하체 5개 × 2장 = 10장 생성
- [ ] STRENGTH 어깨 5개 × 2장 = 10장 생성
- [ ] STRENGTH 팔 4개 × 2장 = 8장 생성
- [ ] STRENGTH 코어 5개 × 2장 = 10장 생성
- [ ] ENDURANCE 달리기 4개 × 2장 = 8장 생성
- [ ] ENDURANCE 자전거 1개 × 2장 = 2장 생성
- [ ] ENDURANCE 줄넘기 3개 × 2장 = 6장 생성
- [ ] BALANCE 요가 3개 × 2장 = 6장 생성
- [ ] BALANCE 스트레칭 3개 × 2장 = 6장 생성
- [ ] BALANCE 필라테스 3개 × 2장 = 6장 생성
- [ ] rembg 배경 제거 + GIF 합성 (Python 스크립트 실행)
- [ ] drawable에 복사 + ExerciseImages.kt 매핑 파일 작성
- [ ] QuestTreeScreen 썸네일 연결
- [ ] WorkoutScreen 가이드 카드 연결

**총 이미지: 46개 × 2장 = 92장 → 46개 GIF**
