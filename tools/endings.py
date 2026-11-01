"""엔딩 그림 생성기 (황제 / 칭송 / 경제적 자유).

vehicles.py 의 Art 를 그대로 쓰고 캔버스 크기만 바꾼다.
실행: python3 tools/endings.py [미리보기 폴더]
"""
import math
import os
import sys

import vehicles
from vehicles import Art, circle, rect

W, H = 240, 160
vehicles.W, vehicles.H = W, H

SKIN = "#F2C19A"
GOLD = "#F5C242"
GOLD_DARK = "#C9962A"
RED = "#B3202A"
HAIR = "#2B2B2B"


def face(a, cx, cy, r, sunglasses=False, smile=True):
    a.fill(circle(cx, cy, r), SKIN)
    a.fill(f"M{cx - r:g},{cy - r * 0.15:g}a{r:g},{r:g} 0 0,1 {2 * r:g},0"
           f"q{-r * 0.3:g},{-r * 0.55:g} {-r:g},{-r * 0.6:g}q{-r * 0.7:g},0 {-r:g},{r * 0.6:g}z", HAIR)
    if sunglasses:
        a.fill(rect(cx - r * 0.75, cy - r * 0.05, r * 0.65, r * 0.38, r * 0.12), "#1B1B1B")
        a.fill(rect(cx + r * 0.1, cy - r * 0.05, r * 0.65, r * 0.38, r * 0.12), "#1B1B1B")
        a.line(f"M{cx - r * 0.1:g},{cy + r * 0.05:g}h{r * 0.2:g}", "#1B1B1B", 1)
    else:
        a.fill(circle(cx - r * 0.35, cy + r * 0.1, r * 0.11), "#2B2B2B")
        a.fill(circle(cx + r * 0.35, cy + r * 0.1, r * 0.11), "#2B2B2B")
        a.fill(circle(cx - r * 0.6, cy + r * 0.38, r * 0.14), "#F59A8B")
        a.fill(circle(cx + r * 0.6, cy + r * 0.38, r * 0.14), "#F59A8B")
    if smile:
        a.line(f"M{cx - r * 0.3:g},{cy + r * 0.45:g}q{r * 0.3:g},{r * 0.3:g} {r * 0.6:g},0", "#7A3B2E", 1.4)


def crown(a, cx, top, w):
    h = w * 0.6
    x0 = cx - w / 2
    a.fill(f"M{x0:g},{top + h:g}L{x0:g},{top + h * 0.25:g}L{x0 + w * 0.25:g},{top + h * 0.6:g}"
           f"L{cx:g},{top:g}L{x0 + w * 0.75:g},{top + h * 0.6:g}L{x0 + w:g},{top + h * 0.25:g}L{x0 + w:g},{top + h:g}z", GOLD)
    a.fill(rect(x0, top + h * 0.82, w, h * 0.22), GOLD_DARK)
    for i, color in enumerate(["#E53935", "#1E88E5", "#43A047"]):
        a.fill(circle(x0 + w * (0.25 + 0.25 * i), top + h * 0.93, w * 0.06), color)


def sparkle(a, x, y, r, color=GOLD):
    a.fill(f"M{x:g},{y - r:g}L{x + r * 0.25:g},{y - r * 0.25:g}L{x + r:g},{y:g}L{x + r * 0.25:g},{y + r * 0.25:g}"
           f"L{x:g},{y + r:g}L{x - r * 0.25:g},{y + r * 0.25:g}L{x - r:g},{y:g}L{x - r * 0.25:g},{y - r * 0.25:g}z", color)


# ---------------------------------------------------------------- 1. 황제 신창
def emperor():
    a = Art()
    a.fill(rect(0, 0, W, H), "#3B1F5C")
    # 커튼과 빛
    for i in range(6):
        x = i * 40
        a.fill(f"M{x:g},0h40v{H:g}q-20,-8 -40,0z", "#4E2A78" if i % 2 else "#45246B")
    a.fill(f"M70,0L170,0L210,{H:g}L30,{H:g}z", "#33FFE9A8")
    # 계단
    a.fill(rect(20, 134, 200, 26), "#7A1F2B")
    a.fill(rect(40, 124, 160, 12), "#8E2633")
    a.fill(rect(0, 150, W, 10), "#5E1621")
    # 왕좌
    a.fill("M78,124L78,46Q78,22 120,16Q162,22 162,46L162,124z", GOLD_DARK)
    a.fill("M86,124L86,50Q86,30 120,25Q154,30 154,50L154,124z", "#8E1B26")
    a.fill(circle(120, 20, 8), GOLD)
    a.fill(rect(70, 92, 18, 34, 4), GOLD).fill(rect(152, 92, 18, 34, 4), GOLD)
    # 망토와 몸
    a.fill("M96,124L100,74Q120,62 140,74L144,124z", RED)
    a.fill("M96,124L100,74L106,72L104,124z", "#F4F4F4")
    a.fill("M144,124L140,74L134,72L136,124z", "#F4F4F4")
    a.fill("M108,124L110,80L130,80L132,124z", "#F5E6C8")
    a.fill(rect(106, 96, 28, 5), GOLD)
    # 홀(지팡이)
    a.line("M150,120L160,62", GOLD_DARK, 3)
    a.fill(circle(160, 60, 5), "#E53935")
    a.fill("M140,96L152,90L156,96L144,102z", SKIN)
    face(a, 120, 58, 13)
    crown(a, 120, 34, 22)
    for x, y, r in [(40, 30, 5), (200, 40, 6), (60, 80, 3), (184, 92, 4), (24, 110, 3), (220, 18, 3)]:
        sparkle(a, x, y, r)
    return a


# ---------------------------------------------------------------- 2. 칭송받는 신창
def praise():
    a = Art()
    a.fill(rect(0, 0, W, H), "#1B2A4A")
    # 스포트라이트
    a.fill("M120,0L60,130L180,130z", "#40FFF3C4")
    a.fill("M20,0L100,130L70,130z", "#22FFFFFF").fill("M220,0L140,130L170,130z", "#22FFFFFF")
    # 단상
    a.fill(rect(80, 96, 80, 14, 3), GOLD_DARK)
    a.fill(rect(88, 86, 64, 12, 3), GOLD)
    # 신창: 두 팔 벌려 인사
    a.fill("M106,88L108,60Q120,52 132,60L134,88z", "#2E7DFF")
    a.line("M108,62L92,46", "#2E7DFF", 6).line("M132,62L148,46", "#2E7DFF", 6)
    a.fill(circle(90, 44, 4), SKIN).fill(circle(150, 44, 4), SKIN)
    face(a, 120, 46, 11)
    crown(a, 120, 26, 18)
    # 꽃가루
    colors = ["#FF7A00", "#E53935", "#43A047", "#1E88E5", GOLD, "#AB47BC"]
    for i in range(28):
        x = (i * 37) % W
        y = (i * 23) % 90 + 4
        a.fill(rect(x, y, 4, 2.5), colors[i % len(colors)])
    # 군중 (실루엣, 손 든 사람들)
    crowd = ["#0E1629", "#152039"]
    for row, (y, r, step, offset) in enumerate([(134, 10, 22, 0), (150, 11, 24, 12)]):
        for i in range(-1, W // step + 2):
            x = i * step + offset
            col = crowd[row]
            a.fill(circle(x, y, r * 0.7), col)
            a.fill(f"M{x - r:g},{H + 2:g}L{x - r:g},{y + r * 0.6:g}Q{x:g},{y + r * 0.1:g} {x + r:g},{y + r * 0.6:g}L{x + r:g},{H + 2:g}z", col)
            if i % 2 == 0:
                a.line(f"M{x - r * 0.7:g},{y + r * 0.8:g}L{x - r * 1.1:g},{y - r * 1.4:g}", col, 3)
                a.line(f"M{x + r * 0.7:g},{y + r * 0.8:g}L{x + r * 1.1:g},{y - r * 1.4:g}", col, 3)
    return a


# ---------------------------------------------------------------- 3. 경제적 자유
def freedom():
    a = Art()
    # 노을 하늘과 바다
    a.fill(rect(0, 0, W, 60), "#FFB067")
    a.fill(rect(0, 0, W, 30), "#FF8A5B")
    a.fill(circle(170, 70, 22), "#FFE08A")
    a.fill(rect(0, 70, W, 40), "#2F7FB5")
    for y in (78, 88, 98):
        a.line(f"M{150:g},{y:g}h40", "#80FFE9A8", 2)
    # 요트
    a.fill("M28,82L72,82L64,90L34,90z", "#FFFFFF")
    a.fill("M48,80L48,52L66,80z", "#F4F4F4").fill("M46,80L46,58L32,80z", "#E8E8E8")
    # 모래사장
    a.fill(f"M0,104Q120,92 {W:g},106L{W:g},{H:g}L0,{H:g}z", "#F3D9A4")
    # 파라솔
    a.line("M64,150L76,74", "#8C6239", 3)
    a.fill("M40,82Q76,52 112,70L76,74z", "#E53935")
    a.fill("M58,76Q76,60 94,72L76,74z", "#FFFFFF")
    # 비치 의자 + 신창
    a.fill("M86,140L120,112L150,112L150,118L124,118L94,142z", "#2E7DFF")
    a.line("M96,140L96,150M146,118L146,150", "#8C6239", 3)
    a.fill("M120,112L140,104L150,112z", "#FFFFFF")
    a.line("M132,112L156,112L166,120", SKIN, 6)
    face(a, 116, 100, 10, sunglasses=True)
    a.fill("M110,112Q120,106 130,112L128,118L112,118z", "#FF7A00")
    # 손에 든 음료
    a.fill(rect(150, 96, 7, 10, 2), "#FFD54F").line("M154,96L158,88", "#E53935", 1.5)
    # 돈다발과 금괴
    a.fill(rect(172, 132, 26, 12, 2), "#4CAF50").fill(rect(176, 126, 26, 12, 2), "#66BB6A")
    a.fill(circle(189, 132, 3), "#2E7D32")
    a.fill("M200,148L206,138L226,138L232,148z", GOLD).fill("M206,138L210,130L224,130L226,138z", GOLD_DARK)
    # 날리는 지폐
    for x, y, rot in [(30, 40, -20), (110, 30, 15), (210, 24, -10), (130, 56, 25)]:
        rad = math.radians(rot)
        dx, dy = math.cos(rad) * 8, math.sin(rad) * 8
        a.fill(f"M{x - dx:.1f},{y - dy:.1f}l{dx * 2:.1f},{dy * 2:.1f}l{-dy * 0.6:.1f},{dx * 0.6:.1f}"
               f"l{-dx * 2:.1f},{-dy * 2:.1f}z", "#66BB6A")
    return a


ENDINGS = [
    ("ending_emperor", "황제가 된 신창", emperor),
    ("ending_praise", "칭송받는 신창", praise),
    ("ending_freedom", "경제적 자유를 이룬 신창", freedom),
]


def main():
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    res = os.path.join(root, "app", "src", "main", "res", "drawable")
    preview = sys.argv[1] if len(sys.argv) > 1 else None
    cards = []
    for name, label, fn in ENDINGS:
        art = fn()
        with open(os.path.join(res, name + ".xml"), "w", encoding="utf-8") as f:
            f.write(art.vector(label))
        cards.append(f"<figure>{art.svg()}<figcaption>{label}</figcaption></figure>")
    if preview:
        os.makedirs(preview, exist_ok=True)
        with open(os.path.join(preview, "endings.html"), "w", encoding="utf-8") as f:
            f.write("<html><body style='margin:0;background:#FFF7E8;font-family:sans-serif;display:grid;"
                    "grid-template-columns:repeat(2,720px);gap:8px'>" + "".join(cards) + "</body></html>")


if __name__ == "__main__":
    main()
