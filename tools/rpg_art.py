"""아르카디아 도트 그림 생성기.

타일(8x8), 인물(10x12, 좌우 대칭), 몬스터(16x16, 보스 24x20)를 문자 격자로 정의하고
RpgArt.java 와 미리보기 HTML 을 만든다.
실행: python3 tools/rpg_art.py [미리보기 폴더]
"""
import os
import sys

PALETTE = {
    "k": "#1A1A1A", "w": "#FFFFFF", "s": "#F2C19A", "o": "#FF7A00", "b": "#2E5EBD", "B": "#1B3A7A",
    "r": "#D7262E", "R": "#8E1B26", "y": "#F5C242", "Y": "#C9962A", "g": "#4CAF50", "G": "#2E7D32",
    "n": "#8C5A2B", "N": "#5A3818", "e": "#9AA1A8", "E": "#5B6168", "p": "#7B4BB5", "q": "#4A2C73",
    "P": "#F59AB8", "c": "#5BC0EB", "h": "#2B2B2B", "l": "#D9DDE1", "t": "#E3C88F", "T": "#C9A86A",
    "u": "#9FC6E8", "v": "#3F7FC4", "f": "#D8C39A", "F": "#C1A878", "d": "#4A3B2F", "D": "#3A2E25",
    "m": "#6E5A48", "M": "#55453A", "x": "#B0362F", "z": "#2A2238",
}


def mirror(rows):
    return [r + r[::-1] for r in rows]


def subst(rows, mapping):
    return ["".join(mapping.get(ch, ch) for ch in r) for r in rows]


# ---------------------------------------------------------------- 인물 (왼쪽 절반 5칸)
PERSON = mirror([
    "...HH", "..HHH", "..Hss", "..sks", "..sss", "...ss",
    "..CCC", ".sCCD", ".sCCC", "..CCC", "..NN.", "..kk.",
])
ROBE = mirror([
    "...HH", "..HHH", "..Hss", "..sks", "..sss", "...ss",
    "..CCC", ".sCCD", ".sCCC", "..CCC", "..CCC", "..kk.",
])
ELDER = mirror([
    "...HH", "..HHH", "..Hss", "..sks", "..HHH", "...HH",
    "..CCC", ".sCCD", ".sCCC", "..CCC", "..CCC", "..kk.",
])
KING = mirror([
    "..y.y", "..yyy", "..Hss", "..sks", "..Hss", "...HH",
    ".RCCC", "RsCCy", "RRCCC", "RRCCC", "RRCCC", "..kk.",
])
CHILD = mirror([
    ".....", ".....", "...HH", "..HHH", "..Hss", "..sks",
    "...ss", "..CCC", ".sCCC", "..CCC", "..NN.", "..kk.",
])
HERO = mirror([
    "...oo", "..ooo", ".oooo", "..hss", "..sks", "..sss",
    "...ss", "..bbb", ".sbby", ".sbbb", "..NNN", "..NN.",
])
LINA = mirror([
    "...rr", "..rrr", ".rrss", ".rsks", ".rsss", "..rss",
    "..lll", ".slle", ".slll", "..eee", "..BB.", "..kk.",
])
SOLDIER = mirror([
    "...ee", "..eee", "..ess", "..sks", "..sss", "...ss",
    "..CCC", ".sCCD", ".sCCC", "..CCC", "..NN.", "..kk.",
])
BOSS_MAP = mirror([
    ".....", "qq...", "qqq..", "qqqqq", "qqrqq", "qqqqq",
    ".qqwq", "..qqq", ".qqqq", "qqqqq", "qq.qq", "kk.kk",
])

CHARS = {
    "hero": HERO,
    "lina": LINA,
    "secretary": subst(PERSON, {"H": "h", "C": "E", "D": "w", "N": "E"}),
    "guard": subst(PERSON, {"H": "h", "C": "k", "D": "w", "N": "k"}),
    "elder": subst(ELDER, {"H": "l", "C": "p", "D": "y"}),
    "merchant": subst(PERSON, {"H": "n", "C": "g", "D": "y", "N": "N"}),
    "innkeeper": subst(PERSON, {"H": "N", "C": "x", "D": "w", "N": "N"}),
    "priest": subst(ROBE, {"H": "w", "C": "w", "D": "y"}),
    "child": subst(CHILD, {"H": "n", "C": "c", "N": "b"}),
    "farmer": subst(PERSON, {"H": "y", "C": "n", "D": "t", "N": "B"}),
    "villager": subst(ROBE, {"H": "e", "C": "P", "D": "w"}),
    "king": subst(KING, {"H": "l", "C": "r", "D": "y"}),
    "minister": subst(ROBE, {"H": "h", "C": "B", "D": "y"}),
    "soldier": subst(SOLDIER, {"C": "v", "D": "y", "N": "N"}),
    "boss": BOSS_MAP,
    "hunter": subst(PERSON, {"H": "n", "C": "G", "D": "n", "N": "N"}),
    "pup": mirror([
        ".....", ".....", ".....", ".....", ".n...", ".nn..",
        ".nnnn", ".nkkn", "..nnn", "..nnn", "..n.n", ".....",
    ]),
}

# ---------------------------------------------------------------- 몬스터 (왼쪽 절반 8칸)
MONSTERS = {
    "rat": mirror([
        "........", "........", "..nn....", ".nNNn...", ".nNNnnnn", "..nnnnnn",
        "..nkkkkn", "..nkwkkn", "..nnnnnn", "...nnnnP", "....nnnn", "...nnnnn",
        "..nnnnnn", "..nnnn..", "..NNN...", "........",
    ]),
    "shroom": mirror([
        "........", ".....rrr", "...rrwwr", "..rrrwwr", ".rrrrrrr", ".rrwwrrr",
        ".rrwwrrr", "..rrrrrr", "....tttt", "....tktt", "....tttt", "....ttkk",
        ".....ttt", "....tttt", "...ss...", "........",
    ]),
    "bat": mirror([
        "........", "........", "........", "pp......", "ppp...p.", "pppp..pp",
        "pppppppp", ".pppppkw", "..ppppkk", "...pppPP", "....pppp", ".....pp.",
        "........", "........", "........", "........",
    ]),
    "thornwolf": mirror([
        "........", "..G.....", "..GG....", ".GGGG...", ".gggggg.", ".ggrgggg",
        ".gggggg.", "..gwgg..", "..gggggg", ".Gggggg.", "GGgggggg", ".ggggggg",
        "..gggggg", "..gg..gg", "..kk..kk", "........",
    ]),
    "pixie": mirror([
        "........", "...c....", "..cuc...", ".cuuc.yy", ".cuucyyy", "..cucsss",
        "...csksk", "....ssss", ".....sss", "....PPPP", "...PPPPP", "....PPPP",
        ".....s..", ".....s..", "........", "........",
    ]),
    "turtle": mirror([
        "........", "........", "........", ".....eee", "...eeEEe", "..eEEeeE",
        ".eEeeEEe", ".eeEEeee", "eEeeeeEE", "eeeeeeee", ".ggggggg", "gsg..ggg",
        "ggg...gg", "........", "........", "........",
    ]),
    "spider": mirror([
        "........", "........", "e.......", ".e...EEE", "..e.EEEE", "e..EEEEE",
        ".e.EErEE", "..eEEEEE", "e..EEEEE", ".e.EEEEE", "..e.EEEE", ".e...EEE",
        "e.......", "........", "........", "........",
    ]),
    "skeleton": mirror([
        "........", "....llll", "...lllll", "...lkkll", "...lkkll", "....llll",
        ".....lkl", "...lllll", "..l.llll", ".l..l.ll", "....llll", ".....l.l",
        "....ll.l", "....l..l", "...ll..l", "........",
    ]),
    "shadowwolf": mirror([
        "........", "..p.....", "..pp....", ".pppp...", ".qqqqqq.", ".qqrqqqq",
        ".qqqqqq.", "..qwqq..", "..qqqqqq", ".pqqqqq.", "ppqqqqqq", ".qqqqqqq",
        "..qqqqqq", "..qq..qq", "..kk..kk", "........",
    ]),
}

# 보스: 24x20 (왼쪽 절반 12칸)
MONSTERS["wolfking"] = mirror([
    "............", "...y........", "...yy.......", "..ypyy......", "..ppppy.....", ".ppppppp....",
    ".pqqqqqqqq..", ".qqqqqqqqqqq", "qqqrrqqqqqqq", "qqqrrqqqqqqq", ".qqqqqqqqqqq", "..qqwqwqqqqq",
    "...qqqqqqqqq", "..pqqqqqqqqq", ".ppqqqqqqqqq", "ppqqqqqqqqqq", ".qqqqqqqqqqq", "..qqqq..qqqq",
    "..qqq....qqq", "..kkk....kkk",
])

# ---------------------------------------------------------------- 타일 (8x8)
GRASS = ["gggggggg", "gGgggggg", "ggggggGg", "gggGgggg", "gggggggg", "ggggGggg", "gGgggggg", "gggggggG"]
TILES = {
    ".": GRASS,
    "f": ["gggggggg", "gPgggygg", "ggggggGg", "gggygggg", "ggPggggg", "gggggPgg", "gyggggGg", "gggggggg"],
    "=": ["tttttttt", "ttTttttt", "tttttTtt", "tttttttt", "tTtttttt", "ttttttTt", "tttTtttt", "tttttttt"],
    "B": ["vvvvvvvv", "nnnnnnnn", "NnnnNnnn", "nnnnnnnn", "NNNNNNNN", "nnnnnnnn", "nnnNnnnN", "vvvvvvvv"],
    "~": ["vvvvvvvv", "vvuuvvvv", "vvvvvvvv", "vvvvvuuv", "vvvvvvvv", "vuuvvvvv", "vvvvvvvv", "vvvvvvuv"],
    "~1": ["vvvvvvvv", "vvvuuvvv", "vvvvvvvv", "vvvvvvuu", "vvvvvvvv", "uuvvvvvv", "vvvvvvvv", "vvvvvvvu"],
    "^": ["gggwwggg", "ggwwewgg", "ggeeeegg", "geeEeeeg", "geEeeEeg", "eeeeEeee", "eEeeeeEe", "eeeEeeee"],
    "T": ["ggGGGGgg", "gGGgGGGg", "GGgGGgGG", "GGGGGGGG", "gGGGgGGg", "ggGGGGgg", "gggnnggg", "gggnnggg"],
    "V": ["gggggggg", "grrggrrg", "rrrrrrrr", "rwwrrwwr", "twntwntt", "tnntnntt", "gggggggg", "gGgggggg"],
    "K": ["gegegegg", "geeeeegg", "geeeeegg", "eekeekee", "eeeeeeee", "eeekkeee", "eeekkeee", "gggggggg"],
    "O": ["eeeEeeee", "eEeeeeEe", "eekkkkee", "ekkkkkke", "ekkkkkke", "ekkkkkke", "gggggggg", "gGgggggg"],
    ",": ["ffffffff", "fFffffff", "ffffffFf", "ffffffff", "ffffffff", "ffFfffff", "ffffffff", "fffffFff"],
    "#": ["EEEEEEEE", "eeeEeeee", "eeeEeeee", "EEEEEEEE", "eEeeeeEe", "eEeeeeEe", "EEEEEEEE", "eeeEeeee"],
    "h": ["rRrRrRrR", "RrRrRrRr", "rrrrrrrr", "RRRRRRRR", "rRrRrRrR", "RrRrRrRr", "rrrrrrrr", "RRRRRRRR"],
    "d": ["EnnnnnnE", "EnNnnNnE", "EnNnnNnE", "EnnnnnnE", "EnNnnyyE", "EnNnnNnE", "EnNnnNnE", "EnnnnnnE"],
    "r": ["rrrrrrrr", "rRrrrrRr", "rrrrrrrr", "rrrRRrrr", "rrrRRrrr", "rrrrrrrr", "rRrrrrRr", "rrrrrrrr"],
    "|": ["nnnnnnnn", "NNNNNNNN", "nnnnnnnn", "nNnnnnNn", "nNnnnnNn", "nNnnnnNn", "NNNNNNNN", "ffffffff"],
    "W": ["fpppppff", "pcccccpf", "pcwwccpf", "pcccccpf", "ppppppff", "pqyqqpff", "pqqqqpff", "fkkkkfff"],
    "X": ["FFFFFFFF", "FfFFFFfF", "FFFFFFFF", "FFFfFFFF", "FFFFFFFF", "FfFFFFfF", "FFFFFFFF", "FFFFFFFF"],
    "S": ["mmmmmmmm", "mkkkkkkm", "mkMMMMkm", "mkMkkMkm", "mkMkkMkm", "mkMMMMkm", "mkkkkkkm", "mmmmmmmm"],
    "U": ["mmmmmmmm", "mllllllm", "mlEEEElm", "mlEllElm", "mlEllElm", "mlEEEElm", "mllllllm", "mmmmmmmm"],
    "C": ["ffffffff", "fNNNNNNf", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "ffffffff"],
    "c": ["ffffffff", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "ffffffff"],
    # 마을 바닥·벽
    ";": ["gggggggg", "ggggGggg", "gggggggg", "gGgggggg", "gggggggg", "gggggGgg", "gggggggg", "ggGggggg"],
    ":": ["TTTTTTTT", "tttTtttt", "tttTtttt", "TTTTTTTT", "ttttttTt", "ttttttTt", "TTTTTTTT", "tTtttttt"],
    "H": ["NNNNNNNN", "nnnnnnnn", "nNnnnnNn", "NNNNNNNN", "nnnnnnnn", "nnnNnnnn", "NNNNNNNN", "nnnnnnnn"],
    "+": ["NNNNNNNN", "NttttttN", "NttttttN", "NttTtttN", "NttttttN", "NttttttN", "NtttttTN", "NttttttN"],
    "M": ["EEEEEEEE", "EyyyyyyE", "EybcpgyE", "EyrbyuyE", "EyggbcyE", "EyyyyyyE", "EEEEEEEE", "eeeEeeee"],
    "k": ["NNNNNNNN", "NrbgyrbN", "NrbgyrbN", "NNNNNNNN", "NygrbygN", "NygrbygN", "NNNNNNNN", "NnnnnnnN"],
    "g": ["nnnnnnnn", "gGngGngG", "GynGynGy", "nnnnnnnn", "gGngGngG", "GynGynGy", "nnnnnnnn", "nnnnnnnn"],
    # 가구·물건 (바탕 투명: 주변 바닥 위에 그린다)
    "b": ["........", ".wwwwww.", ".wwwwww.", ".rrrrrr.", ".rrrrrr.", ".rRrrRr.", ".rrrrrr.", ".NNNNNN."],
    "t": ["........", ".nnnnnn.", "nnnnnnnn", "nNnnnnNn", ".N....N.", ".N....N.", "........", "........"],
    "o": ["........", "..nnnn..", ".nNNNNn.", ".nnnnnn.", ".nNNNNn.", ".nnnnnn.", "..nnnn..", "........"],
    "x": ["........", ".NNNNNN.", ".NnnnNN.", ".NnNnnN.", ".NnnNnN.", ".NNnnnN.", ".NNNNNN.", "........"],
    "F": ["........", ".n...n..", "nnnnnnnn", ".n...n..", ".n...n..", "nnnnnnnn", ".n...n..", "........"],
    "Q": ["........", ".eeeeee.", "eEEEEEEe", "eEvvvvEe", "eEvuvvEe", "eEEEEEEe", ".eeeeee.", "........"],
    "Y": ["........", ".nnnnnn.", ".nttttn.", ".nTTTTn.", ".nnnnnn.", "...nn...", "...nn...", "........"],
    "P": ["........", "nnnnnnnn", "NNNNNNNN", "nnnnnnnn", "n......n", "n......n", "N......N", "........"],
    "A": ["........", ".y.yy.y.", "wwwwwwww", "llllllll", "lYlllYll", "llllllll", "llllllll", "........"],
    "N": ["........", ".eeeeee.", "eucuucue", "eccwwcce", "euccccue", "eccuucce", ".eeeeee.", "........"],
    "L": ["...yy...", "..yYYy..", "...yy...", "...EE...", "....E...", "....E...", "...EEE..", "........"],
    "Z": [".yyyyyy.", ".yRRRRy.", ".yRyyRy.", ".yRRRRy.", "yyyyyyyy", "yRRRRRRy", "yyyyyyyy", ".Y....Y."],
    "j": ["........", "..GGGG..", ".GgGGgG.", "GGgGGGGG", "GgGGgGgG", ".GGGGGG.", "..G..G..", "........"],
    "e": ["........", "..eeee..", ".eyyyye.", ".eykkye.", ".eyyyye.", "..eyye..", "...ee...", "........"],
    "i": ["dddddddd", "dDdyyddd", "dddyYddd", "ddddnddd", "dDddnddd", "ddddnDdd", "dddddddd", "dDdddddd"],
    # 동굴용
    "cave,": ["mmmmmmmm", "mMmmmmmm", "mmmmmmMm", "mmmmmmmm", "mmmmmmmm", "mmMmmmmm", "mmmmmmmm", "mmmmmMmm"],
    "cave#": ["dddddddd", "dDddDddd", "ddddddDd", "DddddDdd", "dddDdddd", "dDddddDd", "ddddDddd", "dDdddddd"],
    "caveX": ["llllllll", "lmmmmmml", "lmmmmmml", "lmmmmmml", "lmmmmmml", "lmmmmmml", "lmmmmmml", "lmmmmmml"],
    "caveC": ["mmmmmmmm", "mNNNNNNm", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "mmmmmmmm"],
    "cavec": ["mmmmmmmm", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "mmmmmmmm"],
}


def check():
    for name, rows in CHARS.items():
        assert len(rows) == 12 and all(len(r) == 10 for r in rows), name
    for name, rows in MONSTERS.items():
        w = 24 if name == "wolfking" else 16
        h = 20 if name == "wolfking" else 16
        assert len(rows) == h and all(len(r) == w for r in rows), (name, [len(r) for r in rows])
    for name, rows in TILES.items():
        assert len(rows) == 8 and all(len(r) == 8 for r in rows), name
    for group in (CHARS, MONSTERS, TILES):
        for name, rows in group.items():
            for r in rows:
                for ch in r:
                    assert ch == "." or ch in PALETTE, (name, ch)


def java_table(name, table):
    out = [f"    static final java.util.Map<String, String[]> {name} = new java.util.HashMap<>();", "    static {"]
    for key, rows in table.items():
        joined = ", ".join(f'"{r}"' for r in rows)
        out.append(f'        {name}.put("{key}", new String[]{{{joined}}});')
    out.append("    }")
    return "\n".join(out)


def write_java(path):
    pal = ", ".join(f"'{k}', 0xFF{v[1:]}" for k, v in PALETTE.items())
    pairs = "\n".join(f"        COLORS.put('{k}', 0xFF{v[1:].upper()});" for k, v in PALETTE.items())
    src = f"""package com.shinchang.adventure.rpg;

/** 도트 그림 데이터 (tools/rpg_art.py 로 생성). '.' 은 투명. */
public final class RpgArt {{

    static final java.util.Map<Character, Integer> COLORS = new java.util.HashMap<>();
    static {{
{pairs}
    }}

{java_table("CHARS", CHARS)}

{java_table("MONSTERS", MONSTERS)}

{java_table("TILES", TILES)}

    public static String[] character(String key) {{
        String[] s = CHARS.get(key);
        return s != null ? s : CHARS.get("villager");
    }}

    public static String[] monster(String key) {{
        return MONSTERS.get(key);
    }}

    public static String[] tile(String key) {{
        return TILES.get(key);
    }}

    public static int color(char c) {{
        Integer v = COLORS.get(c);
        return v == null ? 0 : v;
    }}

    private RpgArt() {{
    }}
}}
"""
    with open(path, "w", encoding="utf-8") as f:
        f.write(src)


def preview(folder):
    def svg(rows, scale):
        h, w = len(rows), len(rows[0])
        rects = []
        for y, r in enumerate(rows):
            for x, ch in enumerate(r):
                if ch != ".":
                    rects.append(f'<rect x="{x}" y="{y}" width="1.02" height="1.02" fill="{PALETTE[ch]}"/>')
        return (f'<svg viewBox="0 0 {w} {h}" width="{w * scale}" height="{h * scale}" '
                f'shape-rendering="crispEdges">{"".join(rects)}</svg>')

    parts = ["<h3>타일</h3><div>"]
    parts += [f'<figure>{svg(r, 8)}<figcaption>{k}</figcaption></figure>' for k, r in TILES.items()]
    parts += ["</div><h3>인물</h3><div>"]
    parts += [f'<figure>{svg(r, 6)}<figcaption>{k}</figcaption></figure>' for k, r in CHARS.items()]
    parts += ["</div><h3>몬스터</h3><div>"]
    parts += [f'<figure>{svg(r, 6)}<figcaption>{k}</figcaption></figure>' for k, r in MONSTERS.items()]
    parts += ["</div>"]
    html = ("<html><body style='background:#222;color:#eee;font-family:sans-serif;width:1100px'>"
            "<style>div{display:flex;flex-wrap:wrap;gap:6px}figure{margin:4px;text-align:center;font-size:11px}</style>"
            + "".join(parts) + "</body></html>")
    os.makedirs(folder, exist_ok=True)
    with open(os.path.join(folder, "rpg_art.html"), "w", encoding="utf-8") as f:
        f.write(html)


def main():
    check()
    root = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    write_java(os.path.join(root, "app", "src", "main", "java", "com", "shinchang", "adventure", "rpg", "RpgArt.java"))
    if len(sys.argv) > 1:
        preview(sys.argv[1])


if __name__ == "__main__":
    main()
