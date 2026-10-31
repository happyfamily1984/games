"""탈것 옆모습 그림 생성기.

같은 도형 정의로 미리보기용 SVG와 안드로이드 VectorDrawable XML을 함께 만든다.
실행: python3 tools/vehicles.py [미리보기 폴더]
"""
import os
import sys

W, H = 160, 100
GROUND = 94

TIRE = "#2B2B2B"
RIM = "#A7B0BA"
HUB = "#4A4F55"
SEAT = "#262626"
BOX = "#FFFFFF"
BOX_LINE = "#FF7A00"
METAL = "#5B6168"
LIGHT = "#FFE9A8"


def circle(cx, cy, r):
    return (f"M{cx - r:g},{cy:g}a{r:g},{r:g} 0 1,0 {2 * r:g},0"
            f"a{r:g},{r:g} 0 1,0 {-2 * r:g},0z")


def rect(x, y, w, h, rx=0):
    if rx == 0:
        return f"M{x:g},{y:g}h{w:g}v{h:g}h{-w:g}z"
    return (f"M{x + rx:g},{y:g}h{w - 2 * rx:g}a{rx:g},{rx:g} 0 0,1 {rx:g},{rx:g}"
            f"v{h - 2 * rx:g}a{rx:g},{rx:g} 0 0,1 {-rx:g},{rx:g}h{-(w - 2 * rx):g}"
            f"a{rx:g},{rx:g} 0 0,1 {-rx:g},{-rx:g}v{-(h - 2 * rx):g}"
            f"a{rx:g},{rx:g} 0 0,1 {rx:g},{-rx:g}z")


class Art:
    def __init__(self):
        self.items = []  # (d, fill, stroke, width)

    def fill(self, d, color):
        self.items.append((d, color, None, 0))
        return self

    def line(self, d, color, width):
        self.items.append((d, None, color, width))
        return self

    def wheel(self, cx, r, spokes=False):
        cy = GROUND - r
        self.fill(circle(cx, cy, r), TIRE)
        self.fill(circle(cx, cy, r * 0.66), RIM)
        if spokes:
            self.fill(circle(cx, cy, r * 0.58), "#E9EDF1")
            for i in range(6):
                import math
                a = math.pi * i / 6
                dx, dy = math.cos(a) * r * 0.58, math.sin(a) * r * 0.58
                self.line(f"M{cx - dx:.1f},{cy - dy:.1f}L{cx + dx:.1f},{cy + dy:.1f}", RIM, 1)
        self.fill(circle(cx, cy, r * 0.22), HUB)
        return self

    def delivery_box(self, x, y, w=30, h=26, strut_to=None):
        if strut_to:
            # 짐받이: 박스 아래 받침대와 차체로 내려가는 지지대
            self.line(f"M{x + w * 0.65:g},{y + h:g}L{strut_to[0]:g},{strut_to[1]:g}", METAL, 3)
            self.line(f"M{x + 2:g},{y + h + 1:g}h{w - 4:g}", METAL, 3)
        self.fill(rect(x, y, w, h, 3), BOX)
        self.line(rect(x, y, w, h, 3), "#C9CED4", 1.2)
        self.fill(rect(x, y + h * 0.42, w, h * 0.18), BOX_LINE)
        return self

    @staticmethod
    def css(color):
        # 안드로이드 #AARRGGBB -> CSS rgba()
        if len(color) == 9:
            a, r, g, b = (int(color[i:i + 2], 16) for i in (1, 3, 5, 7))
            return f"rgba({r},{g},{b},{a / 255:.2f})"
        return color

    def svg(self):
        out = [f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W * 3}" height="{H * 3}">']
        for d, f, s, w in self.items:
            if f:
                out.append(f'<path d="{d}" fill="{self.css(f)}"/>')
            else:
                out.append(f'<path d="{d}" fill="none" stroke="{s}" stroke-width="{w}" '
                           f'stroke-linecap="round" stroke-linejoin="round"/>')
        out.append("</svg>")
        return "\n".join(out)

    def vector(self, comment):
        out = ['<?xml version="1.0" encoding="utf-8"?>',
               f"<!-- {comment} (tools/vehicles.py 로 생성) -->",
               '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
               f'    android:width="{W}dp"', f'    android:height="{H}dp"',
               f'    android:viewportWidth="{W}"', f'    android:viewportHeight="{H}">']
        for d, f, s, w in self.items:
            if f:
                out.append(f'    <path android:fillColor="{f}" android:pathData="{d}" />')
            else:
                out.append(f'    <path android:strokeColor="{s}" android:strokeWidth="{w}" '
                           f'android:strokeLineCap="round" android:strokeLineJoin="round" '
                           f'android:pathData="{d}" />')
        out.append("</vector>")
        return "\n".join(out) + "\n"


def shadow(a, x0=14, x1=146):
    cx, rx = (x0 + x1) / 2, (x1 - x0) / 2
    a.fill(f"M{cx - rx:g},{GROUND:g}a{rx:g},3 0 1,0 {2 * rx:g},0a{rx:g},3 0 1,0 {-2 * rx:g},0z", "#22000000")


# ---------------------------------------------------------------- 도보
def walk():
    a = Art()
    shadow(a, 50, 112)
    skin, shirt, pants = "#F2C19A", "#FF7A00", "#35506E"
    # 뒷다리, 뒷팔
    a.line("M80,62L70,78L62,92", pants, 9)
    a.line("M80,34L70,46L64,56", "#E06000", 6)
    # 배달 가방
    a.fill(rect(52, 26, 22, 26, 3), BOX).line(rect(52, 26, 22, 26, 3), "#C9CED4", 1.2)
    a.fill(rect(52, 36, 22, 5), BOX_LINE)
    # 몸통
    a.fill("M72,30Q82,24 92,30L90,64L74,64z", shirt)
    # 앞다리
    a.line("M84,62L94,76L106,90", pants, 9)
    a.fill("M100,87h12a3,3 0 0,1 0,6h-14z", SEAT)
    a.fill("M56,88h10v6h-12z", SEAT)
    # 머리, 헬멧
    a.fill(circle(84, 17, 9), skin)
    a.fill("M74,16a10,10 0 0,1 20,-2l-2,2h-18z", "#2E7DFF")
    # 앞팔 + 휴대폰
    a.line("M88,34L98,46L106,40", shirt, 6)
    a.fill(rect(104, 33, 5, 9, 1), "#333333")
    return a


# ---------------------------------------------------------------- 자전거
def bicycle():
    a = Art()
    shadow(a)
    frame = "#2FA36B"
    a.wheel(40, 21, spokes=True).wheel(120, 21, spokes=True)
    rear, front = (40, 73), (120, 73)
    crank = (76, 73)
    seat_top, head = (66, 40), (110, 38)
    a.line(f"M{rear[0]},{rear[1]}L{crank[0]},{crank[1]}L{seat_top[0]},{seat_top[1]}z", frame, 4)
    a.line(f"M{seat_top[0]},{seat_top[1]}L{head[0]},{head[1]}L{crank[0]},{crank[1]}", frame, 4)
    a.line(f"M{head[0]},{head[1]}L{front[0]},{front[1]}", METAL, 4)
    a.line("M108,36L106,28L116,26", METAL, 3)
    a.fill("M58,34h18a3,3 0 0,1 0,6h-18a3,3 0 0,1 0,-6z", SEAT)
    a.line("M66,40L66,34", METAL, 3)
    a.fill(circle(76, 73, 6), METAL)
    a.line("M76,73L82,82", SEAT, 3)
    # 앞 바구니
    a.line("M114,24h22l-3,14h-17z", "#8C6239", 2)
    a.line("M118,24l-1,14M124,24v14M130,24l-1,14M115,31h20", "#8C6239", 1.2)
    # 뒤 짐받이 배달통
    a.line("M42,48L66,48", METAL, 3)
    a.delivery_box(30, 22, 34, 26)
    return a


# ---------------------------------------------------------------- CITI 100 (언더본, 큰 바퀴, 레그실드)
def citi100():
    a = Art()
    shadow(a)
    red, white = "#D7262E", "#F4F4F4"
    a.wheel(38, 19, spokes=True).wheel(124, 19, spokes=True)
    # 앞 포크, 앞 펜더
    a.line("M124,75L112,30", METAL, 5)
    a.fill("M106,58a19,19 0 0,1 34,4l-4,0a15,15 0 0,0 -27,-3z", red)
    # 뒤 스윙암, 엔진, 머플러
    a.line("M38,75L78,72", METAL, 5)
    a.fill("M70,62h20l4,12h-22z", "#6B7179")
    a.line("M74,80L30,80", "#9AA1A8", 5)
    # 레그 실드 + 언더본 + 사이드커버
    a.fill("M106,30C116,38 116,60 104,72L94,72C102,60 102,42 98,34z", red)
    a.fill("M98,58C86,60 78,58 72,52L60,44L50,44C44,46 40,54 44,62L66,66C80,68 92,68 100,66z", white)
    a.fill("M50,44L80,44C84,44 86,50 82,54L58,58z", red)
    # 시트, 핸들, 라이트
    a.fill("M46,40h36a4,4 0 0,1 0,8h-36a4,4 0 0,1 0,-8z", SEAT)
    a.fill("M100,22h20a3,3 0 0,1 0,6h-20z", "#3A3A3A")
    a.fill("M108,16h16a6,6 0 0,1 0,12h-16z", red)
    a.fill(circle(124, 22, 5), LIGHT)
    # 뒤 짐받이 배달통
    a.line("M26,40L52,40", METAL, 3)
    a.delivery_box(22, 14, 32, 26)
    return a


# ---------------------------------------------------------------- PCX (매끈한 곡선, 평평한 발판)
def pcx():
    a = Art()
    shadow(a)
    body, trim = "#F1F3F5", "#B71C2B"
    a.wheel(40, 16).wheel(124, 16)
    a.line("M124,78L114,36", METAL, 5)
    a.fill("M110,64a16,16 0 0,1 28,2l-4,0a12,12 0 0,0 -21,-1z", body)
    a.fill("M58,70L36,78L30,74L54,62z", "#7C838B")
    a.fill("M36,74h22v6h-24z", "#9AA1A8")
    # 앞 카울: 둥글고 길게 흐르는 곡선
    a.fill("M108,28C122,28 130,40 128,54C126,64 118,72 106,74L98,74C104,60 104,44 100,34z", body)
    # 낮고 평평한 발판
    a.fill("M100,74L66,74C62,74 60,72 62,68L100,66z", "#3D4248")
    # 뒤 바디: 길고 부드러운 꼬리
    a.fill("M62,70C56,52 40,44 26,46C22,50 24,58 30,62L50,72z", body)
    a.fill("M28,52C40,48 54,52 60,62L56,64C50,56 40,53 30,56z", trim)
    # 시트(롱시트), 레그실드 크롬 라인
    a.fill("M30,42C44,38 60,38 70,44L70,48C58,46 44,46 32,48z", SEAT)
    a.line("M106,32C112,44 112,60 104,72", "#C0C6CC", 1.5)
    # 스크린, 핸들, 라이트
    a.fill("M104,28L112,12L118,13L114,28z", "#9FC6E8")
    a.fill("M98,26h14a3,3 0 0,1 0,6h-14z", "#3A3A3A")
    a.fill("M122,36C128,38 130,44 129,48L124,46z", LIGHT)
    a.delivery_box(20, 14, 32, 26, (44, 50))
    return a


# ---------------------------------------------------------------- NMAX (각진 스포티, 높은 스텝)
def nmax():
    a = Art()
    shadow(a)
    body, dark = "#34495E", "#1E2B38"
    a.wheel(40, 16).wheel(124, 16)
    a.line("M124,78L114,36", METAL, 5)
    a.fill("M110,64L120,58L136,60L138,66L132,66L124,62L114,66z", body)
    a.fill("M58,70L36,78L30,74L54,62z", "#7C838B")
    a.fill("M34,74h24v6h-26z", "#9AA1A8")
    # 각진 앞 카울
    a.fill("M104,26L120,30L130,44L126,58L112,72L98,72L104,56L100,34z", body)
    a.fill("M120,30L130,44L122,46L112,32z", dark)
    # 높은 센터 터널 스텝
    a.fill("M100,72L66,72L62,64L80,62L100,58z", dark)
    # 각진 뒤 바디 + 블루 포인트
    a.fill("M64,68L58,52L44,44L24,44L22,52L30,62L50,70z", body)
    a.fill("M24,48L46,48L56,58L50,60L42,52L26,52z", "#2E7DFF")
    a.fill("M26,40L58,38L70,44L70,48L56,44L28,46z", SEAT)
    # 짧은 스크린, 핸들, 날카로운 라이트
    a.fill("M104,26L110,14L116,15L114,28z", "#7FA9CC")
    a.fill("M96,24h14a3,3 0 0,1 0,6h-14z", "#3A3A3A")
    a.fill("M124,40L132,46L128,48L122,44z", LIGHT)
    a.delivery_box(18, 12, 32, 26, (42, 48))
    return a


# ---------------------------------------------------------------- XMAX 300 (큰 맥시 스쿠터, 높은 스크린)
def xmax():
    a = Art()
    shadow(a, 10, 150)
    body, dark = "#8C939B", "#2F3338"
    a.wheel(38, 18).wheel(126, 18)
    a.line("M126,76L114,32", METAL, 6)
    a.fill("M110,60L122,54L138,56L142,62L134,62L126,58L114,62z", dark)
    a.fill("M58,70L34,78L28,74L54,62z", "#7C838B")
    a.fill("M30,72h26v8h-28z", "#9AA1A8")
    # 크고 높은 앞부분
    a.fill("M100,22L118,24L132,40L130,56L114,72L96,72L102,54L96,30z", body)
    a.fill("M118,24L132,40L124,44L112,28z", dark)
    a.fill("M96,72L64,72L60,64L98,60z", dark)
    # 크고 두툼한 뒤 바디
    a.fill("M64,68L58,48L40,40L18,42L16,52L26,64L50,70z", body)
    a.fill("M18,46L42,44L56,56L50,58L40,50L20,52z", dark)
    # 2단 시트 + 등받이
    a.fill("M24,36L50,34L58,38L72,40L72,46L56,44L48,40L26,42z", SEAT)
    # 높은 스크린
    a.fill("M100,22L106,2L114,3L114,24z", "#9FC6E8")
    a.fill("M92,20h16a3,3 0 0,1 0,6h-16z", "#3A3A3A")
    a.fill("M124,36L134,42L130,46L122,40z", LIGHT)
    a.delivery_box(10, 6, 34, 28, (36, 44))
    return a


# ---------------------------------------------------------------- TMAX (스포츠 맥시, 날카롭고 낮게)
def tmax():
    a = Art()
    shadow(a, 10, 150)
    body, accent = "#1C1D21", "#C9A227"
    a.wheel(36, 18).wheel(128, 18)
    a.line("M128,76L114,30", "#C9A227", 6)
    a.fill("M112,58L126,50L142,54L146,60L136,60L128,56L116,60z", body)
    # 스윙암(알루미늄), 업스웹 머플러
    a.fill("M60,70L36,78L30,74L56,62z", "#9AA1A8")
    a.fill("M26,64L54,70L52,76L22,70z", "#3A3D42")
    # 날카로운 앞 카울
    a.fill("M98,24L120,24L138,42L130,56L114,72L94,72L102,54L94,30z", body)
    a.fill("M120,24L138,42L128,44L114,28z", "#3A3D42")
    a.fill("M94,72L62,72L58,62L98,58z", "#3A3D42")
    # 날렵한 꼬리
    a.fill("M62,68L56,46L36,38L12,34L14,44L28,60L50,70z", body)
    a.fill("M14,38L38,42L52,54L46,56L36,48L16,44z", accent)
    a.fill("M22,32L50,32L58,36L72,38L72,44L56,42L48,38L24,38z", SEAT)
    # 낮은 스포츠 스크린, 핸들, 날카로운 라이트
    a.fill("M98,24L108,10L114,11L112,26z", "#6E8CA6")
    a.fill("M90,20h16a3,3 0 0,1 0,6h-16z", "#3A3A3A")
    a.fill("M126,36L138,42L134,46L122,40z", LIGHT)
    a.delivery_box(8, 2, 34, 28, (34, 40))
    return a


# ---------------------------------------------------------------- 스포츠 바이크 공용
def sport_bike(body, accent, under, tank_hump, aggressive):
    a = Art()
    shadow(a)
    a.wheel(38, 19).wheel(124, 19)
    # 도립/정립 포크, 스윙암
    a.line("M124,75L108,22", "#C9A227" if aggressive else METAL, 6)
    a.line("M38,75L74,64", "#7C838B", 6)
    # 엔진/프레임
    a.fill("M64,48L96,46L100,66L72,70z", "#3A3D42")
    a.fill(circle(84, 62, 7), "#6B7179")
    # 머플러
    if aggressive:
        a.fill("M40,46L62,50L60,56L36,52z", "#9AA1A8")
    else:
        a.fill("M62,70L34,62L36,56L66,64z", "#9AA1A8")
    # 앞 펜더
    a.fill("M110,58a19,19 0 0,1 26,-2l-3,3a15,15 0 0,0 -20,2z", body)
    # 풀 카울
    a.fill("M114,20L136,36L132,50L116,62L96,70L80,68L86,50L96,36z", body)
    a.fill("M112,24L130,36L126,44L104,40z", accent)
    a.fill("M96,70L80,68L82,60L100,62z", under)
    # 연료탱크 + 시트 + 높은 꼬리
    a.fill(f"M70,34C74,{tank_hump} 98,{tank_hump} 104,32L96,46L70,46z", body)
    a.fill("M46,36L72,38L72,46L48,44z", SEAT)
    a.fill("M18,24L50,34L66,48L44,50L30,40z", body)
    a.fill("M20,26L42,32L50,40L34,38z", accent)
    # 스크린, 클립온, 헤드라이트
    a.fill("M114,20L120,8L126,10L122,24z", "#7FA9CC")
    a.line("M102,26L112,30", "#3A3A3A", 3)
    a.fill("M130,34L138,38L136,42L128,40z", LIGHT)
    a.delivery_box(12, 4, 30, 24, (30, 36))
    return a


def r3():
    return sport_bike("#1B4FC7", "#E9ECEF", "#14161A", 22, False)


def r1000():
    return sport_bike("#16307A", "#0FA3E0", "#C9A227", 18, True)


VEHICLES = [
    ("veh_walk", "도보 배달", walk),
    ("veh_bicycle", "자전거", bicycle),
    ("veh_citi100", "CITI 100", citi100),
    ("veh_pcx", "PCX", pcx),
    ("veh_nmax", "NMAX 155", nmax),
    ("veh_xmax", "XMAX 300", xmax),
    ("veh_tmax", "TMAX", tmax),
    ("veh_r3", "R3", r3),
    ("veh_r1000", "R1000", r1000),
]


def main():
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    res = os.path.join(root, "app", "src", "main", "res", "drawable")
    preview = sys.argv[1] if len(sys.argv) > 1 else None
    cards = []
    for name, label, fn in VEHICLES:
        art = fn()
        with open(os.path.join(res, name + ".xml"), "w", encoding="utf-8") as f:
            f.write(art.vector(label))
        cards.append(f'<figure>{art.svg()}<figcaption>{label}</figcaption></figure>')
    if preview:
        os.makedirs(preview, exist_ok=True)
        html = ("<html><body style='margin:0;background:#FFF7E8;font-family:sans-serif;"
                "display:grid;grid-template-columns:repeat(3,480px);gap:8px'>"
                + "".join(cards) + "</body></html>")
        with open(os.path.join(preview, "vehicles.html"), "w", encoding="utf-8") as f:
            f.write(html)


if __name__ == "__main__":
    main()
