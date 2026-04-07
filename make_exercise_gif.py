"""
BodyQuest 운동 GIF 생성 도우미

사용법:
  pip install rembg Pillow
  python make_exercise_gif.py

- 운동 하나씩 프롬프트를 보여주고, 이미지를 넣으면 자동 처리
- 이미 완료된 운동은 자동 스킵
- 중간에 종료(q)하고 나중에 다시 실행하면 이어서 진행
"""

import os
import io
import sys
import glob
import shutil
from PIL import Image

# rembg는 처음 이미지 처리할 때 lazy import (로딩이 느려서)
rembg_session = None

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
RAW_DIR = os.path.join(BASE_DIR, "images", "raw")
GIF_DIR = os.path.join(BASE_DIR, "images", "gif")
SIZE = (512, 512)
FRAME_DURATION = 800

STYLE_PREFIX = """anatomical exercise illustration, realistic human figure with visible muscle definition,
target muscles highlighted in red/orange color, clean white background,
detailed exercise form showing proper technique, gym equipment included if needed,
medical illustration style, centered composition, full body visible,
no text, no watermark, 1024x1024"""

EXERCISES = [
    # (id, 한글이름, A프롬프트, B프롬프트)

    # ── STRENGTH: 가슴 ──
    ("str_chest_pushup", "푸시업",
     "push-up top position, arms fully extended, body in straight plank line, side view",
     "push-up bottom position, arms bent 90 degrees, chest near floor, body straight, side view"),

    ("str_chest_bench_press", "벤치프레스",
     "lying on flat bench, arms fully extended upward holding barbell, side view",
     "lying on flat bench, barbell lowered to chest, elbows bent 90 degrees, side view"),

    ("str_chest_incline_press", "인클라인 프레스",
     "lying on incline bench (45 degrees), arms fully extended upward holding barbell, side view",
     "lying on incline bench (45 degrees), barbell lowered to upper chest, elbows bent, side view"),

    ("str_chest_dumbbell_fly", "덤벨 플라이",
     "lying on flat bench, both arms extended upward holding dumbbells together, front view",
     "lying on flat bench, arms spread wide to sides holding dumbbells, slight elbow bend, front view"),

    ("str_chest_dips", "딥스",
     "on parallel bars, arms fully extended, body upright, slight forward lean, front view",
     "on parallel bars, arms bent, body lowered, elbows at 90 degrees, slight forward lean, front view"),

    # ── STRENGTH: 등 ──
    ("str_back_pullup", "풀업",
     "hanging from pull-up bar, arms fully extended, body straight, front view",
     "chin above pull-up bar, arms bent, body pulled up, front view"),

    ("str_back_barbell_row", "바벨 로우",
     "bent over at 45 degrees, arms extended downward holding barbell, side view",
     "bent over at 45 degrees, barbell pulled up to lower chest, elbows behind body, side view"),

    ("str_back_lat_pulldown", "랫풀다운",
     "seated at cable machine, arms extended upward gripping wide bar, front view",
     "seated at cable machine, bar pulled down to upper chest, elbows at sides, front view"),

    ("str_back_seated_row", "시티드 로우",
     "seated at cable row machine, arms extended forward gripping handle, torso upright, side view",
     "seated at cable row machine, handle pulled to abdomen, elbows behind body, chest out, side view"),

    ("str_back_deadlift", "데드리프트",
     "standing upright, holding barbell at hip level, arms straight down, side view",
     "bent forward at hips, barbell at shin level, knees slightly bent, back flat, side view"),

    # ── STRENGTH: 하체 ──
    ("str_legs_squat", "스쿼트",
     "standing upright, barbell on upper back/shoulders, feet shoulder width apart, side view",
     "deep squat position, thighs parallel to floor, barbell on upper back, side view"),

    ("str_legs_leg_press", "레그프레스",
     "seated in leg press machine, legs extended pushing platform, side view",
     "seated in leg press machine, knees bent 90 degrees toward chest, side view"),

    ("str_legs_lunge", "런지",
     "standing upright, feet together, hands on hips, side view",
     "front leg bent 90 degrees in lunge position, back knee near floor, torso upright, side view"),

    ("str_legs_leg_curl", "레그컬",
     "lying face down on leg curl machine, legs extended straight, side view",
     "lying face down on leg curl machine, heels curled up toward glutes, side view"),

    ("str_legs_bulgarian_split", "불가리안 스플릿 스쿼트",
     "standing with rear foot elevated on bench behind, front leg straight, holding dumbbells, side view",
     "rear foot on bench, front knee bent 90 degrees in deep lunge, torso upright, holding dumbbells, side view"),

    # ── STRENGTH: 어깨 ──
    ("str_shoulder_press", "숄더프레스",
     "seated, holding dumbbells at shoulder level, elbows bent 90 degrees, front view",
     "seated, arms fully extended overhead holding dumbbells, front view"),

    ("str_shoulder_lateral_raise", "사이드 레터럴 레이즈",
     "standing upright, arms at sides holding dumbbells, front view",
     "standing upright, arms raised to sides at shoulder height holding dumbbells, T-shape, front view"),

    ("str_shoulder_front_raise", "프론트 레이즈",
     "standing upright, arms at sides holding dumbbells in front of thighs, side view",
     "standing upright, arms raised straight forward to shoulder height holding dumbbells, side view"),

    ("str_shoulder_face_pull", "페이스풀",
     "standing facing cable machine, arms extended forward gripping rope attachment, front view",
     "standing, rope pulled to face level, elbows flared out high, hands near ears, front view"),

    ("str_shoulder_military_press", "밀리터리 프레스",
     "standing, holding barbell at shoulder/collarbone level, elbows forward, front view",
     "standing, arms fully extended overhead holding barbell, front view"),

    # ── STRENGTH: 팔 ──
    ("str_arms_bicep_curl", "바이셉 컬",
     "standing upright, arms extended down holding dumbbells, palms forward, front view",
     "standing upright, dumbbells curled up to shoulder level, elbows at sides, front view"),

    ("str_arms_tricep_extension", "트라이셉 익스텐션",
     "standing, both hands holding one dumbbell behind head, elbows pointing up, side view",
     "standing, arms extended overhead holding dumbbell, elbows straight, side view"),

    ("str_arms_hammer_curl", "해머 컬",
     "standing upright, arms at sides holding dumbbells with neutral grip (palms facing body), front view",
     "standing upright, dumbbells curled up to shoulder level with neutral grip, elbows at sides, front view"),

    ("str_arms_close_grip_bench", "클로즈그립 벤치프레스",
     "lying on flat bench, arms extended upward holding barbell with narrow grip, side view",
     "lying on flat bench, barbell lowered to lower chest with narrow grip, elbows tucked close to body, side view"),

    # ── STRENGTH: 코어 ──
    ("str_core_plank", "플랭크",
     "forearm plank position, body in straight line from head to heels, forearms on ground, side view",
     "high plank position, arms fully extended, body in straight line from head to heels, side view"),

    ("str_core_crunch", "크런치",
     "lying on back, knees bent, feet flat on floor, hands behind head, shoulders on ground, side view",
     "lying on back, knees bent, upper back curled up off floor, hands behind head, crunching forward, side view"),

    ("str_core_leg_raise", "레그레이즈",
     "lying flat on back, legs extended straight on floor, arms at sides, side view",
     "lying flat on back, legs raised straight up to 90 degrees, arms at sides, side view"),

    ("str_core_bicycle_crunch", "바이시클 크런치",
     "lying on back, hands behind head, right elbow touching left knee, right leg extended, side view",
     "lying on back, hands behind head, left elbow touching right knee, left leg extended, side view"),

    ("str_core_hanging_leg_raise", "행잉 레그레이즈",
     "hanging from pull-up bar, arms extended, legs hanging straight down, front view",
     "hanging from pull-up bar, legs raised straight forward to 90 degrees (L-shape), front view"),

    # ── ENDURANCE: 달리기 ──
    ("end_light_run", "가볍게 달리기",
     "jogging pose, right foot forward, left arm forward, relaxed stride, side view",
     "jogging pose, left foot forward, right arm forward, relaxed stride, side view"),

    ("end_interval", "인터벌 트레이닝",
     "sprinting pose, explosive stride, right knee high, left arm pumping forward, side view",
     "sprinting pose, explosive stride, left knee high, right arm pumping forward, side view"),

    ("end_long_distance", "장거리 달리기",
     "steady running pose, right foot forward, upright posture, efficient form, side view",
     "steady running pose, left foot forward, upright posture, efficient form, side view"),

    ("end_recovery_jog", "회복 조깅",
     "slow light jogging pose, short stride, right foot forward, very relaxed form, side view",
     "slow light jogging pose, short stride, left foot forward, very relaxed form, side view"),

    # ── ENDURANCE: 자전거 ──
    ("end_cycling", "사이클링",
     "riding stationary bike, right leg extended on pedal, left leg bent up, hands on handlebars, side view",
     "riding stationary bike, left leg extended on pedal, right leg bent up, hands on handlebars, side view"),

    # ── ENDURANCE: 줄넘기 ──
    ("end_jumprope_beginner", "줄넘기 기초",
     "standing with jump rope, feet on ground, rope behind body, arms at sides holding handles, front view",
     "mid-jump with jump rope, feet off ground, rope passing overhead, slight knee bend, front view"),

    ("end_jumprope_intermediate", "줄넘기 인터벌",
     "standing with jump rope, feet on ground, ready position, front view",
     "high jump with jump rope, knees tucked slightly higher, rope under feet, dynamic pose, front view"),

    ("end_jumprope_advanced", "줄넘기 고강도",
     "standing with jump rope, athletic stance, ready for double under, front view",
     "explosive high jump, arms crossed (crossover), rope under feet, intense dynamic pose, front view"),

    # ── BALANCE: 요가 ──
    ("bal_yoga_beginner", "입문 요가",
     "standing mountain pose (tadasana), arms at sides, feet together, upright posture, front view",
     "warrior I pose (virabhadrasana I), front knee bent, arms extended overhead, side view"),

    ("bal_yoga_intermediate", "빈야사 요가",
     "downward facing dog pose, inverted V shape, hands and feet on floor, side view",
     "upward facing dog pose (cobra), arms extended, chest lifted, hips near floor, side view"),

    ("bal_yoga_advanced", "파워 요가",
     "crow pose (bakasana), hands on floor, knees resting on upper arms, feet off ground, front view",
     "warrior III pose, standing on one leg, body and other leg parallel to floor, arms forward, side view"),

    # ── BALANCE: 스트레칭 ──
    ("bal_stretch_beginner", "기본 스트레칭",
     "standing upright, arms at sides, neutral relaxed posture, front view",
     "standing, reaching both arms overhead in full body stretch, slight back arch, front view"),

    ("bal_stretch_intermediate", "동적 스트레칭",
     "standing leg swing position, one leg swinging forward, arms out for balance, side view",
     "standing leg swing position, same leg swinging backward, arms out for balance, side view"),

    ("bal_stretch_advanced", "딥 스트레칭",
     "seated forward fold, legs extended, reaching toward toes, side view",
     "pigeon pose, one leg forward bent, back leg extended behind, torso upright, side view"),

    # ── BALANCE: 필라테스 ──
    ("bal_pilates_beginner", "입문 필라테스",
     "lying on back, knees bent, arms at sides, neutral spine position, side view",
     "hundred position, legs raised at 45 degrees, arms extended alongside body, upper back curled up, side view"),

    ("bal_pilates_intermediate", "중급 필라테스",
     "seated V-sit position, knees bent, arms reaching forward, balanced on sit bones, side view",
     "full teaser position, legs extended at 45 degrees, arms parallel to legs, balanced on sit bones, side view"),

    ("bal_pilates_advanced", "고급 필라테스",
     "side plank position, one arm extended, body in straight line, side view",
     "side plank with leg lift, top leg raised, top arm reaching up, star shape, side view"),
]


def init_rembg():
    global rembg_session
    if rembg_session is None:
        print("\n  rembg 모델 로딩 중... (최초 1회만 느림)")
        from rembg import new_session
        rembg_session = new_session("u2net")
        print("  rembg 준비 완료!\n")


def remove_bg(img_path: str) -> Image.Image:
    from rembg import remove
    init_rembg()
    with open(img_path, "rb") as f:
        result = remove(f.read(), session=rembg_session)
    return Image.open(io.BytesIO(result)).convert("RGBA")


def make_gif(ex_id: str) -> bool:
    a_path = os.path.join(RAW_DIR, f"{ex_id}_a.png")
    b_path = os.path.join(RAW_DIR, f"{ex_id}_b.png")

    if not os.path.exists(a_path) or not os.path.exists(b_path):
        return False

    gif_path = os.path.join(GIF_DIR, f"exercise_{ex_id}.gif")
    if os.path.exists(gif_path):
        return True

    print("  배경 제거 + GIF 생성 중...")
    img_a = remove_bg(a_path).resize(SIZE, Image.LANCZOS)
    img_b = remove_bg(b_path).resize(SIZE, Image.LANCZOS)

    img_a.save(
        gif_path,
        save_all=True,
        append_images=[img_b],
        duration=FRAME_DURATION,
        loop=0,
        disposal=2,
    )
    print(f"  GIF 생성 완료! → {gif_path}")
    return True


def find_image_file(user_input: str) -> str | None:
    """사용자 입력에서 이미지 경로를 찾는다."""
    path = user_input.strip().strip('"').strip("'")

    if os.path.isfile(path):
        return path

    # Downloads 폴더에서 찾기
    downloads = os.path.expanduser("~/Downloads")
    dl_path = os.path.join(downloads, path)
    if os.path.isfile(dl_path):
        return dl_path

    # Downloads에서 가장 최근 png/jpg/webp 찾기
    if path.lower() in ("", "l", "last", "latest", "최근"):
        for ext in ("*.png", "*.jpg", "*.jpeg", "*.webp"):
            files = glob.glob(os.path.join(downloads, ext))
            if files:
                latest = max(files, key=os.path.getmtime)
                return latest

    return None


def get_progress():
    """완료된 운동 수 계산."""
    done = 0
    for ex_id, _, _, _ in EXERCISES:
        gif_path = os.path.join(GIF_DIR, f"exercise_{ex_id}.gif")
        if os.path.exists(gif_path):
            done += 1
    return done


def main():
    os.makedirs(RAW_DIR, exist_ok=True)
    os.makedirs(GIF_DIR, exist_ok=True)

    total = len(EXERCISES)
    done = get_progress()

    print("=" * 60)
    print("  BodyQuest 운동 GIF 생성 도우미")
    print("=" * 60)
    print(f"\n  총 {total}개 운동 | 완료: {done}개 | 남은: {total - done}개\n")
    print("  사용법:")
    print("    1. 표시된 프롬프트를 ChatGPT에 복사해서 이미지 생성")
    print("    2. 다운로드한 이미지 경로를 입력 (또는 'l'로 최근 다운로드 자동 감지)")
    print("    3. A/B 두 장이 모이면 자동으로 GIF 생성")
    print()
    print("  명령어:")
    print("    l 또는 Enter  → Downloads 폴더 최근 이미지 자동 감지")
    print("    s             → 이 운동 건너뛰기")
    print("    q             → 종료 (나중에 다시 실행하면 이어서)")
    print("=" * 60)

    for i, (ex_id, name, prompt_a, prompt_b) in enumerate(EXERCISES):
        gif_path = os.path.join(GIF_DIR, f"exercise_{ex_id}.gif")
        if os.path.exists(gif_path):
            continue  # 이미 완료된 운동 스킵

        a_path = os.path.join(RAW_DIR, f"{ex_id}_a.png")
        b_path = os.path.join(RAW_DIR, f"{ex_id}_b.png")

        done = get_progress()
        print(f"\n{'━' * 60}")
        print(f"  [{done + 1}/{total}] {name} ({ex_id})")
        print(f"{'━' * 60}")

        # ── 프레임 A ──
        if not os.path.exists(a_path):
            full_prompt_a = f"{STYLE_PREFIX},\n{prompt_a}"
            print(f"\n  📋 프레임 A (시작 자세) — 아래 프롬프트를 복사하세요:\n")
            print(f"  ┌{'─' * 56}┐")
            for line in full_prompt_a.split("\n"):
                print(f"  │ {line:<55}│")
            print(f"  └{'─' * 56}┘")
            print()

            while True:
                user = input("  이미지 경로 (l=최근 다운로드 / s=건너뛰기 / q=종료): ").strip()
                if user.lower() == "q":
                    print("\n  저장하고 종료합니다. 다시 실행하면 이어서 진행됩니다.")
                    return
                if user.lower() == "s":
                    break
                img_path = find_image_file(user if user else "latest")
                if img_path:
                    shutil.copy2(img_path, a_path)
                    print(f"  ✅ A 프레임 저장: {a_path}")
                    break
                else:
                    print("  ❌ 파일을 찾을 수 없습니다. 다시 입력해주세요.")

            if user.lower() == "s":
                continue

        # ── 프레임 B ──
        if not os.path.exists(b_path):
            full_prompt_b = f"{STYLE_PREFIX},\n{prompt_b}"
            print(f"\n  📋 프레임 B (완료 자세) — 아래 프롬프트를 복사하세요:\n")
            print(f"  ┌{'─' * 56}┐")
            for line in full_prompt_b.split("\n"):
                print(f"  │ {line:<55}│")
            print(f"  └{'─' * 56}┘")
            print()

            while True:
                user = input("  이미지 경로 (l=최근 다운로드 / s=건너뛰기 / q=종료): ").strip()
                if user.lower() == "q":
                    print("\n  저장하고 종료합니다. 다시 실행하면 이어서 진행됩니다.")
                    return
                if user.lower() == "s":
                    break
                img_path = find_image_file(user if user else "latest")
                if img_path:
                    shutil.copy2(img_path, b_path)
                    print(f"  ✅ B 프레임 저장: {b_path}")
                    break
                else:
                    print("  ❌ 파일을 찾을 수 없습니다. 다시 입력해주세요.")

            if user.lower() == "s":
                continue

        # ── GIF 생성 ──
        if os.path.exists(a_path) and os.path.exists(b_path):
            make_gif(ex_id)

    done = get_progress()
    print(f"\n{'=' * 60}")
    print(f"  🎉 완료! {done}/{total}개 GIF 생성됨")
    print(f"  결과 폴더: {GIF_DIR}")
    print(f"{'=' * 60}")


if __name__ == "__main__":
    main()
