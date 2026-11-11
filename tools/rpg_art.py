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
    "captain": subst(SOLDIER, {"e": "B", "E": "B", "C": "w", "D": "y", "N": "B"}),
    "baron": subst(ROBE, {"H": "n", "C": "q", "D": "y"}),
    "apprentice": subst(PERSON, {"H": "y", "C": "c", "D": "w", "N": "N"}),
    "sailor": subst(PERSON, {"H": "N", "C": "w", "D": "b", "N": "b"}),
    "pirate": subst(PERSON, {"H": "r", "C": "R", "D": "k", "N": "k"}),
    "pipi": mirror([
        ".....", "u..PP", "uuPPP", "uuPss", ".usks", "..sss",
        "..ggg", ".sggy", "..ggg", "..ggg", "...s.", "...k.",
    ]),
    "serpent": mirror([
        ".....", "...cc", "..ccc", "..crc", "..ccc", "...cc",
        "..vcc", ".vvcc", "vvccc", "vccvv", "vvvvv", ".....",
    ]),
    "pup": mirror([
        ".....", ".....", ".....", ".....", ".n...", ".nn..",
        ".nnnn", ".nkkn", "..nnn", "..nnn", "..n.n", ".....",
    ]),
}

# 3장 인물
CHARS.update({
    "jiheon": mirror([
        "...hh", "..hhh", ".hhss", ".hsks", ".hsss", ".h.ss",
        "..ooo", ".soow", ".sooo", "..xxx", "..xxx", "..kk.",
    ]),
    "hero2": mirror([
        "...yy", "..yyy", ".ryyy", "..yss", "..sks", "..sss",
        "r..ss", "rrlll", "rsllY", "rrlll", "r.lll", "..kk.",
    ]),
    "baby": mirror([
        ".....", ".....", ".....", ".....", "...ww", "..wss",
        "..wks", "..wss", "..www", ".wwPw", "..www", ".....",
    ]),
    "giant": mirror([
        ".xxxx", "xxxxx", "xxyxx", "xxxxx", "..xxx", "xxooo",
        "xxook", "x.ooo", "..xxx", "..xxx", ".xx.x", ".xx.x",
    ]),
    "ignis": subst(SOLDIER, {"e": "h", "E": "h", "C": "R", "D": "o", "N": "k"}),
    "witch": mirror([
        "....q", "...qq", "..qqq", ".qqqq", "..Pss", "..sks",
        "..sss", "..qqq", ".sqqq", "..qqq", ".qqqq", "..kk.",
    ]),
    "lavadragon": mirror([
        ".....", "...oo", "..ooo", "..oyo", "..ooo", "...oo",
        "..xoo", ".xxoo", "xxooo", "xooxx", "xxxxx", ".....",
    ]),
    "vargas": mirror([
        "..h..", "..hhh", ".hhhh", ".hhrh", ".hhhh", "..hhh",
        "xhhhh", "xhkhr", "xxhhh", "xxhhh", "x.hhh", "..kk.",
    ]),
})

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

# 2장 몬스터
MONSTERS.update({
    "crab": mirror(["........", "........", "rr......", "r.r.....", ".rr.....", "..r..rrr", ".....rrr", "...rrrrr",
                    "..rrkwrr", ".rrrrrrr", "rrrrrrrr", ".rrRRRRR", "..r.r.r.", ".r..r..r", "........", "........"]),
    "gull": mirror(["........", "........", "......ll", "....llll", "...lkkkl", "...lkwkl", "....llly", "..llllll",
                    ".lllllll", "eeelllll", "eeeelll.", "...elll.", "....y.y.", "....y.y.", "........", "........"]),
    "saltspirit": mirror(["........", ".......w", "......ww", ".....wlw", "....wlll", "...wllll", "...lkllk", "...lllll",
                          "...lllll", "....llll", ".....lll", "....l.ll", "...l..l.", "........", "........", "........"]),
    "jelly": mirror(["........", "........", ".....PPP", "...PPPPP", "..PPPwPP", "..PPPPPP", "..PkPPPk", "..PPPPPP",
                     "...PPPPP", "...p.p.p", "...p.p.p", "..p..p..", "..p...p.", "........", "........", "........"]),
    "pirate": mirror(["........", "....rrrr", "...rrrrr", "...sksss", "...sssss", "....ssss", "...NNNNN", "..wNwNNN",
                      "..swwwww", "..swwwww", ".e.BBBBB", "e..BBBBB", "...BB..B", "...kk..k", "........", "........"]),
    "octopus": mirror(["........", "....pppp", "...ppppp", "..pppppp", "..ppkwpp", "..pppppp", "...ppppp", "..p.p.pp",
                       ".p..p.p.", ".p.p..p.", "p..p.p..", "...p....", "........", "........", "........", "........"]),
    "urchin": mirror(["........", "...e..e.", "....e.e.", "..e.eee.", "...eqqqq", "..eqqqqq", ".eqqrqqq", "..qqqqqq",
                      "eeqqqqqq", "..qqqqqq", ".eqqqqqq", "..eqqqqq", "...e.eee", "..e..e..", "........", "........"]),
    "golem": mirror(["........", "....xxxx", "...xPPPx", "...xPkPx", "...xPPPx", "..xxxxxx", ".xxPPPPP", "xxPPxPPP",
                     "xPPPPPPP", "xx.xPPPP", "....xPPP", "....xPPx", "...xxx.x", "...xxx..", "........", "........"]),
    "merknight": mirror(["........", "....eeee", "...eeeee", "...essss", "...sksks", "....ssss", "..cccccc", "y.cvvvcc",
                         "y.ccccvv", "y..ccccc", "...ccccc", "....cccc", "....c.cc", "...c..cc", "........", "........"]),
    "skelsailor": mirror(["....wwww", "...bbbbb", "...lllll", "...lkkll", "...lkkll", "....llll", ".....lkl", "...bbbbb",
                          "..l.wbwb", ".l..l.ll", "....llll", ".....l.l", "....ll.l", "....l..l", "...ll..l", "........"]),
    "statue": mirror(["....eeee", "...eeeee", "...eEyEe", "...eeeee", "..EEEEEE", ".eeeeeee", "eeeEeeee", "eeeeeeee",
                      "e.eeeeee", "e.eEeeee", "..eeeeee", "..eeeeee", "..ee.eee", "..EE.EEE", "........", "........"]),
    "eel": mirror(["........", "........", "........", "......vv", ".....vvv", "....vvkv", "...vvvvv", "..vvv...",
                   ".vvv....", "vvv.....", "vvvv....", ".vvvvv..", "...vvvvv", ".....vvv", "........", "........"]),
    "stardust": mirror(["........", "........", "....y...", "...yyy..", "....y..w", "......ww", "....wwyw", "...wyyyy",
                        "...wykyk", "....yyyy", "...wwyyy", "....w.yy", "......y.", "........", "........", "........"]),
    "goldtoad": mirror(["........", "........", "........", "........", "....yyyy", "...yYYyy", "..ykwyyy", "..yyyyyy",
                        ".yyyyyyy", ".yYyyyYy", "yyyyyyyy", "yyYyyyyY", ".yy..yyy", "Yy...Yyy", "........", "........"]),
    "pearl": mirror(["........", "........", "....u...", "...uuu..", "....u..w", "......ww", "....wwPw", "...wPPPP",
                     "...wPkPk", "....PPPP", "...wwPPP", "....w.PP", "......P.", "........", "........", "........"]),
    "gemcrab": mirror(["........", "........", "cc......", "c.c.....", ".cc..y..", "..c.yby.", ".....yby", "...ycbcy",
                       "..ycbcbc", ".cccbcbc", "cccccccc", ".ccrrrrr", "..c.c.c.", ".c..c..c", "........", "........"]),
})

# 3장 몬스터
MONSTERS.update({
    "ashbat": subst(MONSTERS["bat"], {"p": "E", "k": "r", "w": "o", "P": "x"}),
    "flamewolf": subst(MONSTERS["shadowwolf"], {"p": "o", "q": "x", "r": "y", "w": "y"}),
    "hellhound": subst(MONSTERS["thornwolf"], {"G": "k", "g": "h", "r": "o", "w": "y"}),
    "lavaturtle": subst(MONSTERS["turtle"], {"e": "o", "E": "x", "g": "N", "s": "r"}),
    "ember": subst(MONSTERS["saltspirit"], {"w": "y", "l": "o", "k": "r"}),
    "magma": subst(MONSTERS["jelly"], {"P": "o", "p": "x", "w": "y"}),
    "imp": subst(MONSTERS["pixie"], {"c": "x", "u": "o", "y": "r", "s": "x", "P": "R", "k": "y"}),
    "blackknight": subst(MONSTERS["merknight"], {"e": "h", "c": "k", "v": "x", "y": "o", "s": "h", "k": "r"}),
    "blackgolem": subst(MONSTERS["golem"], {"x": "h", "P": "k", "k": "r"}),
    "ruby": subst(MONSTERS["stardust"], {"y": "r", "w": "P"}),
    "lavatoad": subst(MONSTERS["goldtoad"], {"Y": "o"}),
    "voidmage": subst(MONSTERS["skelsailor"], {"w": "q", "b": "q", "l": "p", "k": "y"}),
    "lizard": mirror(["........", "........", "........", "........", "........", "......hh", ".....hhy", "....hhhh",
                      "..hhhhhh", ".hhEhhhh", "hh.hhhhh", "h..hh.hh", "...h...h", "........", "........", "........"]),
    "boar": mirror(["........", "........", "........", "....N...", "...NNN..", "..nnnnnn", ".nnnnkkn", ".nnnnnnn",
                    "wnnnnnnn", ".wnnnPPP", "..nnnPkP", "..nnnnnn", "...nn.nn", "...NN.NN", "........", "........"]),
    "flamebeast": mirror(["........", "..o.....", ".ooo..o.", ".oxoooo.", "..xxxxxx", ".xxxxxxx", "xxxyxxxx", "xxxxxxxx",
                          "xxxxxooo", ".xxxoooo", "..xxxxxx", "..xxxxxx", "..xx..xx", "..xx..xx", "..kk..kk", "........"]),
    "wyvern": mirror(["........", "x.......", "xx......", "xxx...xx", "xxxx.xxx", "xxxxxxyx", ".xxxxxxx", "..xxxxoo",
                      "...xxooo", "....xxoo", ".....xxx", "....xx.x", "....k..k", "........", "........", "........"]),
})

# 보스: 24x20 (왼쪽 절반 12칸)
MONSTERS["wolfking"] = mirror([
    "............", "...y........", "...yy.......", "..ypyy......", "..ppppy.....", ".ppppppp....",
    ".pqqqqqqqq..", ".qqqqqqqqqqq", "qqqrrqqqqqqq", "qqqrrqqqqqqq", ".qqqqqqqqqqq", "..qqwqwqqqqq",
    "...qqqqqqqqq", "..pqqqqqqqqq", ".ppqqqqqqqqq", "ppqqqqqqqqqq", ".qqqqqqqqqqq", "..qqqq..qqqq",
    "..qqq....qqq", "..kkk....kkk",
])
MONSTERS["galleon"] = mirror([
    "............", "....kkkkkkkk", "...kkkkkkkkk", "..kkkkkkwkkk", ".kkkkkkkkkkk", "....ssssssss",
    "....skkssssk", "....ssssssss", "....rrssssss", "...rrrrrrrrr", "....rrrrrrrr", "..RRRRNNNNNN",
    ".RRRRNNyNNNN", "sRRRRNNNNNNN", "s.RRRNNNNNNN", "...RRNNNNNNN", "....BBBBBBBB", "....BBBB..BB",
    "....kkkk..kk", "....kkkk..kk",
])
MONSTERS["serpent"] = mirror([
    "............", "........cccc", ".......ccccc", "......cccrcc", "......cccccc", ".......ccwcc",
    "........cccc", ".........ccc", "....vvvv.ccc", "...vvvvvvccc", "..vvccvvvvcc", ".vvccccvvvvv",
    "vvcc..ccvvvv", "vcc....ccvvv", "vcc....ccvvv", "vvcc..ccvvvv", ".vvccccvvvvv", "..vvvvvvvvvv",
    "....vvvvvvvv", "............",
])

MONSTERS["giant"] = mirror([
    "............", ".......xxxxx", "......xxxxxx", "......xxyxxx", "......xxxxxx", "...xxxxxxxxx",
    "..xxxxxxxooo", ".xxxx.xxoooo", ".xxx..xxkooo", ".xxx..xxxooo", ".ooo..xxxxxx", ".ooo...xxxxx",
    "........xxxx", ".......xxxxx", "......xxxx..", "......xxxx..", ".....xxxxx..", ".....xxxxx..",
    "....xxxxxx..", "............",
])
MONSTERS["ignis"] = mirror([
    "............", "........hhhh", ".......hhhhh", ".......hhrrh", ".......hhhhh", "........hhhh",
    "..o...hhhhhh", "..oo.hhhRRhh", "..ooohhhRRRh", "..ohhhhhRRRR", "..o.hhhhhRRR", "..o..hhhhhhh",
    "..y...hhhhhh", "..y....hhhhh", ".......hhh.h", ".......hhh.h", "......hhhh.h", "......hhh..h",
    "......kkk..k", "............",
])
MONSTERS["witch"] = mirror([
    "............", "...........q", "..........qq", ".........qqq", "........qqqq", "......qqqqqq",
    "....qqqqqqqq", "........ssss", "........skss", "........ssss", "......pppppp", ".....ppppppp",
    "....sppqpppp", "...s.ppqqppp", "....pppqqqpp", "...ppppqqqqp", "..pppppqqqqq", "..pppppppppp",
    "..qqqqqqqqqq", "............",
])
MONSTERS["lavadragon"] = mirror([
    "............", "....x.......", "....xx......", "...xxxx...xx", "...xxxxx.xxx", "....xxxxxxxx",
    ".......xxxyx", "......xxxxxx", "......xxxxoo", ".....xxxoooo", "....xxxxxooo", "...xxxxxxxxx",
    "..xxxxxxxxxx", ".xxxxooooxxx", "xxxxoooooxxx", "xxxooooooxxx", ".xxxooooxxxx", "..xxxxxxxxxx",
    "....xxxx..xx", "............",
])
MONSTERS["vargas"] = mirror([
    "............", "......h.....", "......hh....", ".......hhhhh", "......hhhhhh", "......hhhrrh",
    "......hhhhhh", "..xx...hhhhh", ".xxxx.hhhhhh", "xxhhhhhhhxhh", "xhhhhhhhhxxh", "xhhhhhhhhxxx",
    "xhhkhhhhhhhh", "x.hhhhhhhhhh", "..hhhhhhhhhh", "..zhhhhhhhhh", "...zzhhhhzzh", "....hhhh..hh",
    "....kkkk..kk", "............",
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
    # 2장: 부두, 배, 등대, 해저 신전 입구
    "_": ["nnnnnnnn", "NNNNNNNN", "tnntnntn", "nnnnnnnn", "NNNNNNNN", "nnntnnnt", "nnnnnnnn", "NNNNNNNN"],
    "v": ["vvvvvvvv", "vvvnnvvv", "vvnwwnvv", "vnnnnnnv", "nNNNNNNn", "vNnnnnNv", "vvNNNNvv", "vvvvvvvv"],
    "I": ["gggrrggg", "ggryyrgg", "gggwwggg", "gggrrggg", "gggwwggg", "gggrrggg", "ggwwwwgg", "gggggggg"],
    "G": ["vvvvvvvv", "vveeeevv", "veuvvuev", "vevvvvev", "vevvvvev", "veuvvuev", "vveeeevv", "vvvvvvvv"],
    "G1": ["vveeeevv", "veeeeeev", "eeMMMMee", "eeMkkMee", "eeMkkMee", "eeMMMMee", "veeeeeev", "vveeeevv"],
    # 바다 동굴
    "sea,": ["eeeeeeee", "eueeeeee", "eeeeeeue", "eeeeeeee", "eeeueeee", "eeeeeeeu", "eeeeeeee", "eueeeeee"],
    "sea#": ["BBBBBBBB", "BvBBBvBB", "BBBBBBvB", "vBBBvBBB", "BBBBBBBB", "BvBBBBvB", "BBBvBBBB", "BBBBBBBB"],
    "seaX": ["llllllll", "leeeeeel", "leeeeeel", "leeeeeel", "leeeeeel", "leeeeeel", "leeeeeel", "leeeeeel"],
    "seaC": ["eeeeeeee", "eNNNNNNe", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "eeeeeeee"],
    "seac": ["eeeeeeee", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "eeeeeeee"],
    "seai": ["BBBBBBBB", "BvBPPBBB", "BBPxPBBB", "BBBxBBBB", "BvBxBBvB", "BBBxBBBB", "BBBBBBBB", "BvBBBBBB"],
    # 해저 신전
    "tmp,": ["llllllll", "lclllllc", "llllllll", "lllcllll", "llllllll", "lclllllc", "llllllll", "llllclll"],
    "tmp#": ["cccccccc", "vvvvvvvv", "cvvccvvc", "cvvccvvc", "cccccccc", "vccvvccv", "vccvvccv", "cccccccc"],
    "tmpX": ["cccccccc", "cllllllc", "cllllllc", "cllllllc", "cllllllc", "cllllllc", "cllllllc", "cllllllc"],
    "tmpC": ["llllllll", "lNNNNNNl", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "llllllll"],
    "tmpc": ["llllllll", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "llllllll"],
    "tmpi": ["llllllll", "lllyylll", "llyYYyll", "lllyylll", "lllcclll", "lllcclll", "llcccclll"[:8], "llllllll"],
    # 3장: 이그나 섬
    "a": ["MMMMMMMM", "MmMMMMEM", "MMMMMMMM", "MMMEMMMM", "MMMMMmMM", "MEMMMMMM", "MMMMMMEM", "MMmMMMMM"],
    "n": ["hhhhhhhh", "hEhhhhhh", "hhhhhEhh", "hhhhhhhh", "hhEhhhhh", "hhhhhhhE", "hhhhEhhh", "hEhhhhhh"],
    "l": ["oooxoooo", "oyoooxoo", "ooooyooo", "xoooooyo", "ooyooooo", "oooooxoo", "oxooyooo", "ooooooox"],
    "l1": ["ooooxooo", "ooyoooox", "xoooyooo", "oooooooy", "oxooyooo", "ooooooxo", "ooxoooyo", "yooooooo"],
    "J": ["hhhhhhhh", "hxhhhhxh", "hhkkkkhh", "hkxxxxkh", "hkxoyxkh", "hkxxxxkh", "hkkkkkkh", "MMMMMMMM"],
    "J1": ["hhhhhhhh", "hEhhhhEh", "hhkkkkhh", "hkkkkkkh", "hkkkkkkh", "hkkkkkkh", "hkkkkkkh", "MMMMMMMM"],
    # 용암 동굴
    "lava,": ["DDDDDDDD", "DxDDDDDD", "DDDDDDxD", "DDDDDDDD", "DDDxDDDD", "DDDDDDDD", "DDDDDxDD", "DxDDDDDD"],
    "lava#": ["NNNNNNNN", "NdNNNoNN", "NNNNNNdN", "dNNNNNNN", "NNNoNNNN", "NdNNNNdN", "NNNNdNNN", "NNNNNNNN"],
    "lavaX": ["oooooooo", "oDDDDDDo", "oDDDDDDo", "oDDDDDDo", "oDDDDDDo", "oDDDDDDo", "oDDDDDDo", "oDDDDDDo"],
    "lavaC": ["DDDDDDDD", "DNNNNNND", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "DDDDDDDD"],
    "lavac": ["DDDDDDDD", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "DDDDDDDD"],
    "lavai": ["NNNNNNNN", "NdNyyNNN", "NNyoyNNN", "NNNoNNNN", "NdNnNNdN", "NNNnNNNN", "NNNNNNNN", "NdNNNNNN"],
    # 흑염의 요새
    "fort,": ["zzzzzzzz", "zqzzzzqz", "zzzzzzzz", "zzzqqzzz", "zzzqqzzz", "zzzzzzzz", "zqzzzzqz", "zzzzzzzz"],
    "fort#": ["hhhhhhhh", "EhhhEhhh", "EhhhEhhh", "hhhhhhhh", "hhEhhhEh", "hhEhhhEh", "hhhhhhhh", "EhhhEhhh"],
    "fortX": ["qqqqqqqq", "qzzzzzzq", "qzzzzzzq", "qzzzzzzq", "qzzzzzzq", "qzzzzzzq", "qzzzzzzq", "qzzzzzzq"],
    "fortC": ["zzzzzzzz", "zNNNNNNz", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "zzzzzzzz"],
    "fortc": ["zzzzzzzz", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "zzzzzzzz"],
    "forti": ["hhhhhhhh", "hEhppEhh", "hhpqphhh", "hhhqhhhh", "hEhEhhEh", "hhhEhhhh", "hhhhhhhh", "hEhhhhhh"],
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
        big = name in ("wolfking", "galleon", "serpent", "giant", "ignis", "witch", "lavadragon", "vargas")
        w = 24 if big else 16
        h = 20 if big else 16
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
