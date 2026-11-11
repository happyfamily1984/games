package com.shinchang.adventure.rpg;

/**
 * 환상의 세계 아르카디아: 지도, 등장인물, 몬스터, 아이템, 주문.
 * 모든 이야기와 캐릭터는 이 게임의 오리지널 설정이다.
 */
public final class RpgData {

    // ------------------------------------------------------------ 지도

    public static final int OFFICE = 0, WORLD = 1, REBEL = 2, ERDEN = 3, CAVE1 = 4, CAVE2 = 5,
            WORLD2 = 6, MERCA = 7, SALT = 8, LIGHTHOUSE = 9, SPRAY1 = 10, SPRAY2 = 11, TEMPLE1 = 12, TEMPLE2 = 13,
            WORLD3 = 14, IGNA = 15, LAVA1 = 16, LAVA2 = 17, FORT1 = 18, FORT2 = 19, FORT3 = 20,
            LAKE = 21, WORLD4 = 22, BEASTDEN = 23, ROA = 24, RUINS1 = 25, RUINS2 = 26, CRYSTAL1 = 27, CRYSTAL2 = 28, DEMON1 = 29, DEMON2 = 30,
            RUINS3 = 31, CRYSTAL3 = 32, MOON1 = 33, MOON2 = 34, DEMON3 = 35;
    public static final String[] MAP_NAMES = {"청와대 집무실", "아르카디아 평원", "리벨 마을", "에르덴 성", "그림자 동굴 1층", "그림자 동굴 2층",
            "메르카 평원", "항구 도시 메르카", "소금꽃 마을", "곶의 등대", "물보라 동굴 1층", "물보라 동굴 2층", "해저 신전 1층", "해저 신전 2층",
            "이그나 섬", "화산섬 마을 이그나", "용암 동굴 1층", "용암 동굴 2층", "흑염의 요새 1층", "흑염의 요새 2층", "흑염의 요새 옥좌",
            "안개 호수", "실바 대륙", "짐승의 보금자리", "로아 마을", "바람의 고원 유적 1층", "바람의 고원 유적 2층",
            "수정 동굴 1층", "수정 동굴 2층", "요마성 1층", "요마성 옥좌",
            "바람의 고원 유적 3층", "수정 동굴 3층", "달빛 숲길", "달빛 샘", "요마성 2층"};

    /** 지도 그림은 tools/rpg_maps.py 가 만든 RpgMaps 에 있다 */
    public static final String[][] MAPS = {RpgMaps.OFFICE, RpgMaps.WORLD, RpgMaps.REBEL, RpgMaps.ERDEN, RpgMaps.CAVE1, RpgMaps.CAVE2,
            RpgMaps.WORLD2, RpgMaps.MERCA, RpgMaps.SALT, RpgMaps.LIGHTHOUSE, RpgMaps.SPRAY1, RpgMaps.SPRAY2, RpgMaps.TEMPLE1, RpgMaps.TEMPLE2,
            RpgMaps.WORLD3, RpgMaps.IGNA, RpgMaps.LAVA1, RpgMaps.LAVA2, RpgMaps.FORT1, RpgMaps.FORT2, RpgMaps.FORT3,
            RpgMaps.LAKE, RpgMaps.WORLD4, RpgMaps.BEASTDEN, RpgMaps.ROA, RpgMaps.RUINS1, RpgMaps.RUINS2,
            RpgMaps.CRYSTAL1, RpgMaps.CRYSTAL2, RpgMaps.DEMON1, RpgMaps.DEMON2,
            RpgMaps.RUINS3, RpgMaps.CRYSTAL3, RpgMaps.MOON1, RpgMaps.MOON2, RpgMaps.DEMON3};

    /** 지도 분위기: 동굴(갈색), 바다 동굴(푸른색), 신전(청록 돌) */
    public static String theme(int map) {
        if (map == CAVE1 || map == CAVE2) return "cave";
        if (map == SPRAY1 || map == SPRAY2) return "sea";
        if (map == TEMPLE1 || map == TEMPLE2) return "tmp";
        if (map == LAVA1 || map == LAVA2) return "lava";
        if (map == FORT1 || map == FORT2 || map == FORT3) return "fort";
        if (map == RUINS1 || map == RUINS2 || map == RUINS3) return "ruin";
        if (map == CRYSTAL1 || map == CRYSTAL2 || map == CRYSTAL3) return "ice";
        if (map == DEMON1 || map == DEMON2 || map == DEMON3) return "demon";
        if (map == MOON1 || map == MOON2) return "moon";
        return null;
    }

    /** 바깥이 바다인 지도 */
    public static boolean isOverworld(int map) {
        return map == WORLD || map == WORLD2 || map == WORLD3 || map == WORLD4;
    }

    /** 지나갈 수 없는 타일 */
    public static boolean blocks(char t) {
        return "#~^H|WCbtkoxFQYPAMgNLZijevql".indexOf(t) >= 0;
    }

    /** 몬스터가 나오는 지도 */
    public static boolean hasEncounters(int map) {
        return map == WORLD || map == CAVE1 || map == CAVE2 || map == WORLD2
                || map == SPRAY1 || map == SPRAY2 || map == TEMPLE1 || map == TEMPLE2
                || map == WORLD3 || map == LAVA1 || map == LAVA2 || map == FORT1 || map == FORT2 || map == FORT3
                || map == WORLD4 || map == RUINS1 || map == RUINS2 || map == CRYSTAL1 || map == CRYSTAL2 || map == DEMON1 || map == DEMON2
                || map == RUINS3 || map == CRYSTAL3 || map == MOON1 || map == MOON2 || map == DEMON3;
    }

    /** 지도 이동: (지도, x, y) 에 들어가면 (도착 지도, x, y) 로 */
    public static final int[][] LINKS = {
            // 평원 → 마을 / 성 / 동굴
            {WORLD, 6, 18, REBEL, 12, 20},
            {WORLD, 20, 8, ERDEN, 13, 26},
            {WORLD, 28, 3, CAVE1, 10, 13},
            // 출구 → 평원
            {REBEL, 12, 21, WORLD, 6, 19},
            {REBEL, 13, 21, WORLD, 6, 19},
            {ERDEN, 13, 27, WORLD, 20, 9},
            {ERDEN, 14, 27, WORLD, 20, 9},
            {CAVE1, 10, 14, WORLD, 28, 4},
            {CAVE1, 11, 14, WORLD, 28, 4},
            // 동굴 계단
            {CAVE1, 4, 12, CAVE2, 10, 12},
            {CAVE2, 10, 13, CAVE1, 4, 11},
            // 2장: 메르카 평원
            {WORLD2, 5, 17, MERCA, 1, 12},
            {WORLD2, 28, 19, SALT, 11, 1},
            {WORLD2, 31, 5, LIGHTHOUSE, 6, 11},
            {WORLD2, 7, 2, SPRAY1, 11, 13},
            {WORLD2, 33, 3, TEMPLE1, 11, 13},
            {MERCA, 0, 11, WORLD2, 5, 18},
            {MERCA, 0, 12, WORLD2, 5, 18},
            {SALT, 11, 0, WORLD2, 28, 20},
            {SALT, 12, 0, WORLD2, 28, 20},
            {LIGHTHOUSE, 6, 12, WORLD2, 31, 6},
            {SPRAY1, 11, 14, WORLD2, 7, 3},
            {SPRAY1, 12, 14, WORLD2, 7, 3},
            {SPRAY1, 4, 11, SPRAY2, 11, 12},
            {SPRAY2, 11, 13, SPRAY1, 3, 11},
            {TEMPLE1, 11, 14, WORLD2, 32, 3},
            {TEMPLE1, 12, 14, WORLD2, 32, 3},
            {TEMPLE1, 6, 8, TEMPLE2, 12, 12},
            {TEMPLE2, 12, 13, TEMPLE1, 7, 8},
            // 3장: 이그나 섬
            {WORLD3, 8, 17, IGNA, 12, 18},
            {WORLD3, 24, 12, LAVA1, 11, 13},
            {WORLD3, 14, 7, FORT1, 11, 13},
            {IGNA, 12, 19, WORLD3, 8, 18},
            {IGNA, 13, 19, WORLD3, 8, 18},
            {LAVA1, 11, 14, WORLD3, 24, 13},
            {LAVA1, 12, 14, WORLD3, 24, 13},
            {LAVA1, 4, 11, LAVA2, 11, 12},
            {LAVA2, 11, 13, LAVA1, 3, 11},
            {FORT1, 11, 14, WORLD3, 14, 8},
            {FORT1, 12, 14, WORLD3, 14, 8},
            {FORT1, 18, 5, FORT2, 16, 12},
            {FORT2, 16, 13, FORT1, 18, 6},
            {FORT2, 3, 12, FORT3, 12, 11},
            {FORT3, 12, 12, FORT2, 3, 11},
            // 4장
            {WORLD3, 22, 18, LAKE, 11, 14},
            {LAKE, 11, 15, WORLD3, 22, 17},
            {LAKE, 12, 15, WORLD3, 22, 17},
            {WORLD4, 6, 6, BEASTDEN, 11, 16},
            {WORLD4, 14, 18, ROA, 12, 18},
            {WORLD4, 28, 6, RUINS1, 11, 13},
            {WORLD4, 30, 19, CRYSTAL1, 10, 13},
            {WORLD4, 18, 3, DEMON1, 11, 13},
            {BEASTDEN, 11, 17, WORLD4, 6, 7},
            {BEASTDEN, 12, 17, WORLD4, 6, 7},
            {ROA, 12, 19, WORLD4, 14, 19},
            {ROA, 13, 19, WORLD4, 14, 19},
            {RUINS1, 11, 14, WORLD4, 28, 7},
            {RUINS1, 12, 14, WORLD4, 28, 7},
            {RUINS1, 6, 8, RUINS2, 12, 12},
            {RUINS2, 12, 13, RUINS1, 7, 8},
            {CRYSTAL1, 10, 14, WORLD4, 30, 20},
            {CRYSTAL1, 11, 14, WORLD4, 30, 20},
            {CRYSTAL1, 4, 12, CRYSTAL2, 10, 12},
            {CRYSTAL2, 10, 13, CRYSTAL1, 4, 11},
            {DEMON1, 11, 14, WORLD4, 18, 4},
            {DEMON1, 12, 14, WORLD4, 18, 4},
            {DEMON1, 18, 5, DEMON3, 12, 11},
            {DEMON3, 12, 12, DEMON1, 18, 6},
            {DEMON3, 3, 2, DEMON2, 12, 11},
            {DEMON2, 12, 12, DEMON3, 3, 3},
            {RUINS2, 12, 3, RUINS3, 11, 12},
            {RUINS3, 11, 13, RUINS2, 12, 4},
            {CRYSTAL2, 5, 4, CRYSTAL3, 11, 11},
            {CRYSTAL3, 11, 12, CRYSTAL2, 5, 5},
            {WORLD4, 10, 9, MOON1, 11, 13},
            {MOON1, 11, 14, WORLD4, 10, 10},
            {MOON1, 12, 14, WORLD4, 10, 10},
            {MOON1, 4, 4, MOON2, 12, 11},
            {MOON2, 12, 12, MOON1, 4, 5},
    };

    /** 마을에 들어가면 그 마을이 "마지막으로 들른 마을"이 된다 (전멸·날개깃 귀환 위치) */
    public static final int[][] TOWN_RETURN = {
            {REBEL, 12, 19},
            {ERDEN, 13, 25},
            {MERCA, 3, 12},
            {SALT, 11, 7},
            {IGNA, 12, 17},
            {BEASTDEN, 11, 15},
            {ROA, 12, 17},
    };

    /** 배를 타고 내리는 곳 */
    public static final int[] PIER_ERDEN = {ERDEN, 23, 19};
    public static final int[] PIER_MERCA = {MERCA, 14, 22};

    /** 처음 시작 위치 (집무실) */
    public static final int START_X = 6, START_Y = 5;

    // ------------------------------------------------------------ 이야기 단계 · 단서

    public static final int STEP_PROLOGUE = 0;
    public static final int STEP_ARRIVED = 1;
    public static final int STEP_LETTER = 2;
    public static final int STEP_LINA = 3;
    public static final int STEP_BOSS_DOWN = 4;
    public static final int STEP_CHAPTER1_CLEAR = 5;
    // 2장
    public static final int STEP_SAILED = 6;
    public static final int STEP_GUILD_REFUSED = 7;
    public static final int STEP_GALLEON_DOWN = 8;
    public static final int STEP_PIPI = 9;
    public static final int STEP_LAMP = 10;
    public static final int STEP_SERPENT_DOWN = 11;
    public static final int STEP_CHAPTER2_CLEAR = 12;
    // 3장
    public static final int STEP_ALONE = 13;
    public static final int STEP_JIHEON = 14;
    public static final int STEP_GIANT_DOWN = 15;
    public static final int STEP_WEDDING = 16;
    public static final int STEP_GEN1 = 17;
    public static final int STEP_GEN2 = 18;
    public static final int STEP_GEN3 = 19;
    public static final int STEP_REVENGE = 20;
    public static final int STEP_HOME = 21;
    public static final int STEP_CHAPTER3_CLEAR = 22;
    // 4장
    public static final int STEP_FAMILY = 23;
    public static final int STEP_KIDNAPPED = 24;
    public static final int STEP_HOSTAGE = 25;
    public static final int STEP_MINHAN = 26;
    public static final int STEP_LEGACY = 27;
    public static final int STEP_DAD_FOUND = 28;
    public static final int STEP_MOM_FOUND = 29;
    public static final int STEP_DEW = 30;
    public static final int STEP_MOM_BACK = 31;
    public static final int STEP_DAD_BACK = 32;
    public static final int STEP_FOX_DOWN = 33;
    public static final int STEP_KING_DOWN = 34;
    public static final int STEP_CHAPTER4_CLEAR = 35;

    public static final String[] CHAPTER_TITLES = {"프롤로그 「청와대의 머신」", "1장 「배달부 용사」", "2장 「상인의 길」", "3장 「용사의 탄생」",
            "4장 「해를 물려받은 아이」"};

    public static int chapterOf(int step) {
        if (step == STEP_PROLOGUE) return 0;
        if (step < STEP_SAILED) return 1;
        if (step < STEP_ALONE) return 2;
        return step < STEP_FAMILY ? 3 : 4;
    }

    /** 찾아낸 단서 (비트) */
    public static final int FLAG_RECORD = 1, FLAG_CHRONICLE = 2, FLAG_MURAL = 4, FLAG_LIBRARY = 8, FLAG_DIARY = 16,
            FLAG_LEDGER = 32, FLAG_SHIPLOG = 64, FLAG_LIGHTLOG = 128, FLAG_TEMPLE = 256, FLAG_PROPHECY = 512, FLAG_COMPASS = 1024,
            FLAG_SMITHLOG = 2048, FLAG_ORDER = 4096, FLAG_FIRESEAL = 8192,
            FLAG_SUNLORE = 16384, FLAG_PACT = 32768;
    public static final String[] CLUE_NAMES = {"금고의 경고문", "리벨 연대기", "성당 벽화", "에르덴 서고의 기록", "로웰의 일기",
            "길드 장부", "선박 기록", "등대 일지", "해저 신전 벽화", "문지기의 각인", "바로스의 나침반",
            "도건의 작업 일지", "요새의 명령서", "불의 봉인 제단",
            "해의 제단 벽화", "요마왕의 맹약"};
    /** 단서 메뉴에 보여 줄 요약 */
    public static final String[] CLUE_TEXTS = {
            "청와대 금고: '문 저편에서 오는 자는 짐꾼이 아니다' — 서명 E. 가방과 같은 문양.",
            "300년 전 짐꾼 카이른이 다섯 봉인석으로 공허의 왕을 가뒀다. 곁에 현자 아르센과 누이 엘라.",
            "리벨 성당: 짐꾼은 마지막에 짐을 모두 내려놓고 웃는다. 곁에 등을 돌린 회색 망토.",
            "공허의 왕은 욕망을 먹고 '가장 많이 가진 자'의 몸을 탐한다. 다섯 번째 봉인석의 기록은 찢겨 있다.",
            "회색 망토 '문지기'가 봉인석을 깨뜨렸다. 가장 많이 가진 자가 오면 그릇이 완성된다고 했다. 로웰은 바다로.",
            "메르카 길드의 봉인석 발굴 자금은 에르덴의 'M'이 댔다. 깨진 조각은 목걸이로 가공돼 팔려 나갔다.",
            "기사 로웰은 한 달 전, 회색 망토를 쫓아 북쪽 화산섬 이그나행 화물선을 탔다.",
            "쌍둥이 바로스와 바론. 10년 전 바로스는 봉인을 지키러 해저 신전에 들어가 돌아오지 않았다.",
            "카이른이 바다의 봉인석을 내려놓을 때, 다섯 점 문양 망토를 두른 여인이 등불을 들고 있었다.",
            "'그릇이 오는 날, 다섯 봉인은 하나씩 열린다.' 문지기는 신창이 오기를 기다려 봉인을 열고 있다.",
            "'바론에게. 돈보다 무거운 걸 잊지 마.' 바로스가 남긴 말.",
            "회색 망토가 대장장이 도건에게 '그릇을 묶을 족쇄'를 주문했다. 그릇은 다 잃고 빈 몸으로 이 섬에 온다고 했다.",
            "모르칸이 바르가스에게: 바다에서 동료를 떼어 내고, 짐꾼은 '비워서' 살려 두라. 배신은 처음부터였다.",
            "불의 봉인은 나누는 손으로만 지켜진다. 문지기: '그릇을 비워라. 잃게 하라. 빈 그릇에 왕이 깃든다.'",
            "해를 모은 자가 짐을 내려놓을 때, 그 해는 아이에게 내려가 아이를 지킨다. 해는 신창의 유산이었다.",
            "문지기와 요마왕의 맹약: 다시 가득 찬 짐꾼에게서 가장 소중한 것을 빼앗을 것. 해를 물려받은 아이를 데려올 것.",
    };

    // ------------------------------------------------------------ 등장인물 (마을 사람)

    public static final int ACT_NONE = 0, ACT_SHOP = 1, ACT_INN = 2, ACT_SAVE = 3, ACT_EVENT = 4, ACT_LEGACY = 5;
    public static final int EV_ELDER = 1, EV_KING = 2, EV_BOSS = 3, EV_HUNTER = 4,
            EV_SHIP_TO_MERCA = 5, EV_SHIP_TO_ERDEN = 6, EV_GUILD = 7, EV_PIPI = 8, EV_NOAH = 9, EV_MIDBOSS = 10, EV_BOSS2 = 11,
            EV_SHIP_TO_IGNA = 12, EV_JIHEON = 13, EV_GIANT = 14, EV_IGNA_ELDER = 15, EV_GEN1 = 16, EV_GEN2 = 17, EV_GEN3 = 18,
            EV_VARGAS = 19, EV_BIRTH = 20,
            EV_KID = 21, EV_FOX1 = 22, EV_OWL = 23, EV_ALTAR = 24, EV_DAD_STATUE = 25, EV_MOM_STATUE = 26, EV_FOX2 = 27, EV_KING4 = 28, EV_MOONLORD = 29;

    public static final class Npc {
        public final int map, x, y;
        public final String sprite;
        public final String name;
        /** {최소 단계, 대사...} 중 지금 단계 이하에서 가장 큰 것 */
        public final Object[][] lines;
        public final int action;
        public final int arg;
        /** 보이는 단계 범위 */
        public final int minStep, maxStep;

        Npc(int map, int x, int y, String sprite, String name, int action, int arg, int minStep, int maxStep, Object[][] lines) {
            this.map = map;
            this.x = x;
            this.y = y;
            this.sprite = sprite;
            this.name = name;
            this.action = action;
            this.arg = arg;
            this.minStep = minStep;
            this.maxStep = maxStep;
            this.lines = lines;
        }

        public boolean visible(int step) {
            return step >= minStep && step <= maxStep;
        }

        public String[] linesFor(int step) {
            Object[] best = lines[0];
            for (Object[] l : lines) if ((Integer) l[0] <= step) best = l;
            String[] out = new String[best.length - 1];
            for (int i = 1; i < best.length; i++) out[i - 1] = (String) best[i];
            return out;
        }
    }

    private static Object[] at(int step, String... lines) {
        Object[] o = new Object[lines.length + 1];
        o[0] = step;
        System.arraycopy(lines, 0, o, 1, lines.length);
        return o;
    }

    private static Npc npc(int map, int x, int y, String sprite, String name, Object[]... lines) {
        return new Npc(map, x, y, sprite, name, ACT_NONE, 0, 0, 99, lines);
    }

    private static Npc act(int map, int x, int y, String sprite, String name, int action, int arg, Object[]... lines) {
        return new Npc(map, x, y, sprite, name, action, arg, 0, 99, lines);
    }

    public static final Npc[] NPCS = {
            // ---- 프롤로그: 청와대
            npc(OFFICE, 3, 5, "secretary", "비서실장",
                    at(0, "대통령님, 청와대 금고 가장 안쪽 방에서 이 기계가 발견됐습니다.",
                            "봉인 기록을 뒤져 봤는데, 설치한 사람을 아는 이가 아무도 없습니다. 서명은 'E' 한 글자뿐이었습니다.",
                            "금고 문은 오른쪽입니다. 기록 상자도 그 안에 있습니다.")),
            npc(OFFICE, 12, 6, "guard", "경호원",
                    at(0, "대통령님, 밤마다 금고에서 바람 소리가 납니다.",
                            "어젯밤엔… 할머니 목소리 같은 것도 들렸습니다. '아직이야, 아직'이라고요.",
                            "…헛것을 들은 거겠지요.")),

            // ---- 리벨 마을
            new Npc(REBEL, 4, 5, "elder", "리벨 장로", ACT_EVENT, EV_ELDER, 0, 99, new Object[][]{
                    at(0, "오오… 하늘에서 빛과 함께 떨어진 자여."),
                    at(STEP_LETTER, "편지는 에르덴 왕께 전해 주게. 다리를 건너 길을 따라 북쪽이네.",
                            "가는 길에 성당 벽화도 꼭 보고 가게. 자네 가방의 문양이 거기 있다네."),
                    at(STEP_LINA, "리나… 로웰 단장의 딸이로군. 아버지를 닮아 고집이 세지.",
                            "그림자 동굴은 북동쪽 산속일세. 카이른의 봉인석이 무사하기를…"),
                    at(STEP_BOSS_DOWN, "늑대왕이 루프스였다고…? 300년 동안 봉인을 지킨 수호 늑대가 어둠에 먹히다니.",
                            "봉인석이 깨져 있었다면, 예언의 '다섯 빛'은 다섯 개의 봉인석일세.",
                            "누군가 일부러 봉인을 풀고 있어. 루프스마저 어둠에 빠뜨리면서까지. 대체 무엇을 위해서…"),
                    at(STEP_CHAPTER1_CLEAR, "자네 가방이 처음보다 무거워 보이는군. 짐꾼은 원래 그런 법이지.",
                            "하지만 기억하게. 벽화 속 짐꾼은 마지막에 짐을 내려놓고 웃고 있었다네."),
            }),
            act(REBEL, 19, 3, "merchant", "도구점 주인", ACT_SHOP, 0,
                    at(0, "어서 와요! 약초는 여행의 기본이랍니다."),
                    at(STEP_LINA, "기사님과 함께라니! 동굴에 가신다면 약초를 넉넉히 챙기세요.")),
            act(REBEL, 4, 15, "innkeeper", "여관 주인", ACT_INN, 6,
                    at(0, "나그네의 여관에 어서 오세요.",
                            "요즘 손님이 통 없네요. 지난달엔 회색 망토를 쓴 손님 하나가 밤새 지도에 뭔가를 그리다 갔지요.")),
            act(REBEL, 20, 18, "priest", "마을 사제", ACT_SAVE, 0,
                    at(0, "신의 가호가 함께하기를.",
                            "양쪽 벽의 벽화는 300년 전 짐꾼의 이야기랍니다. 마지막 그림의 뜻은 아무도 몰라요.",
                            "지금까지의 모험을 기록해 드리지요.")),
            npc(REBEL, 14, 10, "child", "미나",
                    at(0, "아저씨 하늘에서 떨어졌어? 그 가방 문양 멋있다! 성당 벽화에도 똑같은 게 있어.",
                            "…있잖아, 우리 강아지 콩이가 한 달 전에 동굴 쪽으로 뛰어가서 안 돌아와.",
                            "루프스 할아버지 따라간 것 같아. 콩이는 루프스 할아버지를 엄청 좋아했거든."),
                    at(STEP_LINA, "기사 누나다! 누나, 동굴에 가면 콩이 좀 찾아 줘. 갈색이고 귀가 하나 접혔어."),
                    at(STEP_BOSS_DOWN, "콩이!! 콩이가 돌아왔어! 아저씨가 데려다준 거지?",
                            "루프스 할아버지는…? …그렇구나. 할아버지가 콩이를 지켜 줬구나.",
                            "(미나는 콩이를 꼭 끌어안고 한참 울었다)"),
                    at(STEP_CHAPTER1_CLEAR, "콩이랑 매일 성당에 가서 루프스 할아버지한테 인사해. 아저씨도 꼭 다시 와!")),
            new Npc(REBEL, 14, 9, "pup", "콩이", ACT_NONE, 0, STEP_BOSS_DOWN, 99, new Object[][]{
                    at(0, "멍! 멍! (꼬리를 마구 흔들며 신창의 배달 가방 냄새를 맡는다)"),
            }),
            new Npc(REBEL, 9, 9, "hunter", "사냥꾼 브람", ACT_EVENT, EV_HUNTER, 0, 99, new Object[][]{
                    at(0, "북동쪽 동굴엔 원래 은빛 늑대 '루프스'가 살았어. 봉인석을 지키는 수호 늑대였지.",
                            "내가 꼬마였을 때 녀석은 마을까지 내려와 아이들이랑 놀다 가곤 했다네. 목엔 기사단 방울을 달고.",
                            "그런데 한 달 전부터 루프스가 안 보여. 대신 검은 늑대들이 내려왔지… 설마 그 녀석이."),
                    at(STEP_LINA, "동굴에 간다고? …혹시 루프스를 보거든, 녀석이 아직 녀석인지 봐 주게."),
                    at(STEP_CHAPTER1_CLEAR, "루프스 몫까지 잘 싸워 주게. 녀석은 끝까지 수호 늑대였어."),
            }),
            npc(REBEL, 23, 11, "farmer", "농부",
                    at(0, "한 달 전 새벽이었지. 회색 망토를 뒤집어쓴 사내가 북동쪽 동굴로 올라가는 걸 봤어.",
                            "지팡이 끝이 푸르스름하게 빛났지. 그 뒤로 늑대들이 내려오기 시작했다네.",
                            "근처 몬스터는 약하지만, 다리를 건너면 훨씬 사나워진다오."),
                    at(STEP_BOSS_DOWN, "늑대들이 사라졌어! 그런데 그 회색 망토는 끝내 내려오지 않았지…")),
            npc(REBEL, 8, 12, "villager", "할머니",
                    at(0, "여관에서 푹 쉬면 상처도 마력도 다 회복된단다. 사제님께 기도하면 기록도 남겨 주시지.",
                            "카이른 이야기? 다섯 빛을 다 나른 뒤 어디론가 사라졌다더구나.",
                            "누구는 '문 저편'으로 갔다고 하고… 옛날이야기지 뭐.")),
            npc(REBEL, 21, 13, "merchant", "떠돌이 행상",
                    at(0, "나는 바다 건너 메르카에서 온 행상이오. 길이 끊겨 발이 묶였지.",
                            "메르카엔 요즘 이상한 소문이 돌아. 상인 길드가 '빛나는 푸른 돌'을 금값에 사들인다나."),
                    at(STEP_CHAPTER1_CLEAR, "메르카로 간다고? 길드장은 돈 냄새를 기막히게 맡는 사람이니 조심하시오.")),

            // ---- 에르덴 성
            new Npc(ERDEN, 13, 3, "king", "에르덴 왕", ACT_EVENT, EV_KING, 0, 99, new Object[][]{
                    at(0, "낯선 이로군."),
            }),
            npc(ERDEN, 16, 4, "minister", "대신 모르칸",
                    at(0, "폐하께서는 그림자 늑대 때문에 밤잠을 이루지 못하십니다."),
                    at(STEP_LETTER, "문 저편에서 왔다고? …흥미롭군.",
                            "그쪽 세계엔 어떤 장치가 있었소? 문을 여는 장치 말이오. 어디에 두었소?",
                            "(대신의 눈빛이 이상할 만큼 반짝인다)"),
                    at(STEP_BOSS_DOWN, "봉인석 조각이라… 폐하보다 내가 먼저 보면 좋았을 텐데. 아, 아무것도 아니오."),
                    at(STEP_CHAPTER1_CLEAR, "메르카로 가는 배는 내가 직접 주선하겠소. …물론 폐하를 위해서지.")),
            npc(ERDEN, 12, 11, "soldier", "성 병사",
                    at(0, "여기는 에르덴 성이다. 폐하께서는 융단 끝 옥좌에 계신다."),
                    at(STEP_LINA, "로웰 단장님은 회색 망토를 '문지기'라고 부르셨지. 그게 무슨 뜻인지는 아무도 몰라.")),
            npc(ERDEN, 15, 11, "soldier", "성 병사",
                    at(0, "그림자 늑대들은 무리를 지어 다닌다. 한 마리씩 노려라.",
                            "동굴에 가려면 다리 북쪽 몬스터를 이길 실력은 돼야 해.")),
            npc(ERDEN, 7, 3, "minister", "서고지기",
                    at(0, "이곳 서고엔 짐꾼 카이른 시대의 기록이 남아 있소. 책장을 살펴보시오.",
                            "다만 다섯 번째 봉인석에 관한 장은… 누군가 찢어 갔소. 언제부터였는지 모르겠소."),
                    at(STEP_BOSS_DOWN, "봉인석이 깨졌다면, 남은 넷도 위험하오. 메르카의 '푸른 돌' 소문… 설마.")),
            npc(ERDEN, 20, 5, "soldier", "기사 하멜",
                    at(0, "리나는 단장님이 사라진 뒤로 매일 성문 앞 분수에서 기다린다네.",
                            "단장님 방패엔 짐꾼의 문장이 새겨져 있었지. 단장님 집안은 대대로 카이른 기사단이었거든.",
                            "단장님은 수호 늑대 루프스와 함께 봉인을 지키셨어. 루프스 목에 기사단 방울을 걸어 준 것도 단장님이지."),
                    at(STEP_LINA, "리나를 부탁하네, 짐꾼 양반. 저 녀석, 검은 나보다 낫다니까.",
                            "늑대왕은 보통 놈이 아니야. 다리 북쪽에서 몸을 충분히 단련하고 가게. 동굴 2층 늑대들을 이길 정도는 돼야 해."),
                    at(STEP_BOSS_DOWN, "늑대왕이… 루프스였다고. 단장님이 아시면 얼마나 마음 아파하실지.")),
            new Npc(ERDEN, 11, 17, "lina", "견습 기사", ACT_NONE, 0, 0, STEP_LETTER, new Object[][]{
                    at(0, "나는 견습 기사 리나. 아버지… 기사단장 로웰이 동굴에 간 지 한 달째 돌아오지 않아요.",
                            "찾으러 가고 싶은데, 견습은 성 밖에 나가면 안 된대요. 아, 답답해!"),
                    at(STEP_LETTER, "장로님 편지를 가져왔다고요? 어서 폐하께 가 보세요!",
                            "…잠깐, 그 가방 문양. 아버지 방패에 있던 거랑 똑같아요!"),
            }),
            act(ERDEN, 5, 23, "merchant", "무기점 주인", ACT_SHOP, 1,
                    at(0, "에르덴 대장간에서 갓 나온 물건들이오! 동굴에 가려면 철검쯤은 있어야지.")),
            act(ERDEN, 20, 23, "innkeeper", "여관 주인", ACT_INN, 10,
                    at(0, "기사들도 묵어 가는 여관입니다.")),
            act(ERDEN, 4, 16, "priest", "예배당 사제", ACT_SAVE, 0,
                    at(0, "용자여, 모험을 기록해 드리지요.")),
            npc(ERDEN, 17, 15, "villager", "음유시인",
                    at(0, "♪ 다섯 빛을 진 짐꾼이 문 저편에서 오네~",
                            "♪ 공허의 왕은 욕망을 먹고 자라네~",
                            "♪ 짐꾼은 마지막에 짐을 내려놓으리~ …오래된 노래지요. 끝 구절은 저도 뜻을 몰라요."),
                    at(STEP_CHAPTER1_CLEAR, "♪ 어둠에 먹힌 은빛 늑대여, 그래도 작은 생명을 품었네~",
                            "♪ 짐꾼의 손에 편히 잠드니, 은빛 방울 소리 다시 울리네~ …루프스를 위한 새 노래랍니다.",
                            "그런데 이상하죠. 옛 노래의 끝 구절이 자꾸 떠올라요. '짐을 내려놓으리'…")),
            npc(ERDEN, 21, 18, "villager", "아낙",
                    at(0, "메르카에서 오던 소금배가 끊긴 지 오래예요. 바다도 요즘 이상하대요.",
                            "밤바다가 푸르스름하게 빛난다나 봐요.")),

            // ---- 그림자 동굴
            new Npc(CAVE2, 5, 4, "boss", "그림자 늑대왕", ACT_EVENT, EV_BOSS, STEP_LINA, STEP_LINA, new Object[][]{
                    at(0, "(동굴 가장 깊은 곳. 깨진 돌 위에 거대한 검은 늑대가 웅크리고 있다)",
                            "크르르… 문 저편의 냄새. 그분이 기다리던 자로군.",
                            "(늑대의 목에서 녹슨 방울이 딸랑, 하고 울렸다)",
                            "[리나] 저 방울… 기사단의 방울이에요! 설마… 수호 늑대 루프스?",
                            "루프스… 그 이름은 어둠에 먹혔다. 봉인이 깨진 날, 우리는 공허의 왕의 이빨이 되었다!",
                            "[신창] 리나, 저 녀석 등 뒤를 봐. 뭔가를… 감싸고 있어.",
                            "짐꾼이든 그릇이든… 이 돌에 다가오는 자는 물어뜯는다!",
                            "[리나] 신창 씨, 온다!"),
            }),

            // ================================================== 2장 「상인의 길」
            // ---- 에르덴 부두 / 메르카 부두의 선장
            new Npc(ERDEN, 24, 18, "captain", "모르칸의 선장", ACT_EVENT, EV_SHIP_TO_MERCA, STEP_CHAPTER1_CLEAR, 99, new Object[][]{
                    at(0, "대신 모르칸 님의 명으로 메르카까지 모시겠습니다."),
            }),
            new Npc(MERCA, 14, 23, "captain", "모르칸의 선장", ACT_EVENT, EV_SHIP_TO_ERDEN, STEP_SAILED, 99, new Object[][]{
                    at(0, "에르덴으로 돌아가시겠습니까?"),
            }),

            // ---- 메르카: 상인 길드
            act(MERCA, 11, 3, "merchant", "길드 접수원", ACT_NONE, 0,
                    at(0, "상인 길드 메르카 본부입니다. 외지인은 길드장님 허가 없이는 거래할 수 없어요.",
                            "길드장 바론 님은 안쪽 집무실에 계십니다."),
                    at(STEP_SERPENT_DOWN, "길드장님이 푸른 돌 거래를 모두 끊으라고 하셨어요. 10년 만에 처음 보는 표정이었어요.")),
            new Npc(MERCA, 19, 3, "baron", "길드장 바론", ACT_EVENT, EV_GUILD, 0, 99, new Object[][]{
                    at(0, "바쁘니 용건만 말하시오."),
            }),
            npc(MERCA, 13, 6, "apprentice", "견습 상인 테오",
                    at(0, "길드장님은 원래 저렇지 않았대요. 10년 전 쌍둥이 형 바로스 님이 바다에서 실종된 뒤로…",
                            "바로스 님은 늘 '푸른 돌은 팔면 안 된다'고 하셨대요. 길드장님은 그 말을 듣지 않았고요.",
                            "…요즘 길드장님, 밤마다 부두에 나가서 바다만 보세요."),
                    at(STEP_SERPENT_DOWN, "길드장님이 웃으셨어요! 그리고 저한테 처음으로 '고맙다'고 하셨어요.")),

            // ---- 메르카: 피피 (동료가 되기 전, 시장 노점)
            new Npc(MERCA, 10, 10, "pipi", "요정 상인 피피", ACT_EVENT, EV_PIPI, STEP_SAILED, STEP_GALLEON_DOWN, new Object[][]{
                    at(0, "어서 와~ 요정 상인 피피의 노점! …손님 아니면 저리 가. 지금 장사 망해서 기분 별로거든."),
            }),
            npc(MERCA, 18, 10, "merchant", "목걸이 상인",
                    at(0, "푸른 돌 목걸이 사세요~ 바다에서 건진 귀한 보석이에요!",
                            "…요즘 이걸 산 사람들이 열이 난다는 소문이 있긴 한데, 기분 탓이겠죠?"),
                    at(STEP_SERPENT_DOWN, "길드가 푸른 돌을 전부 거둬 갔어요. 그런데 손님들 열이 내렸다니, 진짜 그 돌 때문이었나 봐요.")),
            npc(MERCA, 10, 14, "villager", "생선 장수",
                    at(0, "10년 전부터 바다가 미쳤어. 밤바다가 푸르게 빛나는 날이면 배가 돌아오질 않아.",
                            "다들 '심해의 군주가 깨어났다'고 하지. 원래는 바다를 지켜 주던 큰 뱀이었다는데."),
                    at(STEP_SERPENT_DOWN, "어젯밤 바다가 잔잔했어! 10년 만에 처음이야. 고기도 다시 잡히겠지.")),
            npc(MERCA, 20, 19, "sailor", "선원",
                    at(0, "모르칸 대신의 배라고? 그 배는 요상해. 화물칸에 늘 푸른 돌을 잔뜩 싣고 에르덴으로 돌아가거든."),
                    at(STEP_SERPENT_DOWN, "모르칸의 배가 오늘은 빈 배로 떠난대. 길드가 돌을 안 팔았거든.")),
            npc(MERCA, 26, 16, "minister", "항만 관리인",
                    at(0, "10년째 바다가 거칠어서 배가 거의 못 떠요. 예외는 모르칸 대신의 배뿐이죠.",
                            "아, 한 달 전엔 에르덴 기사 한 분이 화산섬 이그나행 화물선을 기어이 타고 가셨어요. 선박 기록에 있을 거예요.")),
            act(MERCA, 3, 2, "merchant", "메르카 도구점", ACT_SHOP, 2,
                    at(0, "바다 건너온 물건도 있답니다. 상급 약초는 꼭 챙기세요.")),
            act(MERCA, 25, 2, "merchant", "메르카 무기점", ACT_SHOP, 3,
                    at(0, "해적이 들끓으니 무기가 잘 팔려요. 해룡의 창은 메르카 명물이지요.")),
            act(MERCA, 3, 14, "innkeeper", "갈매기 여관", ACT_INN, 20,
                    at(0, "갈매기 여관입니다. 파도 소리 들으며 푹 쉬세요.")),
            act(MERCA, 25, 10, "priest", "항구 예배당 사제", ACT_SAVE, 0,
                    at(0, "바다에 나가는 이들을 위해 기도합니다. 모험을 기록해 드리지요.")),
            new Npc(MERCA, 2, 8, "guard", "회색 옷의 사내", ACT_NONE, 0, STEP_SAILED, STEP_GUILD_REFUSED, new Object[][]{
                    at(0, "……짐꾼이 왔군. 문지기님께 알려야겠어.",
                            "(사내는 신창을 흘끗 보더니 골목으로 사라지려 한다)",
                            "…뭘 보시오. 볼일 없소."),
            }),
            npc(MERCA, 16, 16, "child", "부두 꼬마",
                    at(0, "저기 등대 보여? 등대지기 노아 할아버지는 10년째 매일 밤 불을 켜. 누굴 기다린대."),
                    at(STEP_SERPENT_DOWN, "노아 할아버지가 오늘은 등대 불을 엄청 밝게 켰어! 무슨 좋은 일 있나 봐.")),

            // ---- 소금꽃 마을
            npc(SALT, 4, 4, "elder", "소금꽃 촌장",
                    at(0, "푸른 열병이 돌고 있네. 길드가 바다에서 건진 돌 부스러기를 버린 뒤로, 그 물을 마신 사람들이 앓아누웠지.",
                            "등대지기 노아라면 뭔가 알 걸세. 바다 일은 그 사람이 제일 잘 알아. 마을 북쪽 길로 곶 끝까지 가게."),
                    at(STEP_SERPENT_DOWN, "열이 내렸어! 다들 일어나 그물을 손질하고 있다네. 자네들 덕분이야.")),
            npc(SALT, 17, 4, "farmer", "앓는 어부",
                    at(0, "으… 몸이 불덩이 같아… 꿈마다 파란 뱀이 나와…"),
                    at(STEP_SERPENT_DOWN, "개운해! 꿈속 파란 뱀이 '미안하다'고 하고 사라졌어. 이상한 꿈이지?")),
            npc(SALT, 19, 4, "villager", "앓는 아낙",
                    at(0, "남편이 길드에서 푸른 돌 목걸이를 사 왔어요… 그날부터 온 식구가 열이 나요."),
                    at(STEP_SERPENT_DOWN, "목걸이는 바다에 돌려보냈어요. 몸이 이렇게 가벼울 수가!")),
            npc(SALT, 17, 11, "child", "마리",
                    at(0, "우리 아빠는 소금꽃 마을 최고의 어부였어. 그런데 1년 전 회색 옷 남자가 금화를 잔뜩 주고 간 뒤로 해적이 됐대.",
                            "아빠는 수염이 빨개서 '붉은 수염 갈리온'이라고 불려. 북서쪽 물보라 동굴에 산대…",
                            "아빠한테 이 조개 목걸이 줬었는데, 아직 갖고 있을까? …아빠를 미워하지 말아 줘."),
                    at(STEP_GALLEON_DOWN, "아빠가 돌아왔어! 내 조개 목걸이를 아직 갖고 있었어. 다시 어부가 된대!",
                            "아저씨, 고마워. 아빠를 때려 줘서. …아니, 데려와 줘서.")),
            new Npc(SALT, 19, 11, "pirate", "어부 갈리온", ACT_NONE, 0, STEP_GALLEON_DOWN, 99, new Object[][]{
                    at(0, "…부끄럽소. 회색 망토가 준 금화에 눈이 멀어 1년을 해적으로 살았소.",
                            "그자가 이런 말을 했지. '그릇은 이미 문을 건넜다. 이제 바다의 봉인만 열면 된다.'",
                            "…그릇이 당신이었군, 짐꾼. 조심하시오. 그자는 당신을 부수려는 게 아니라 '채우려는' 거요.",
                            "해저 신전엔 길드장 바론의 쌍둥이 형 바로스가 있소. 10년 전 봉인을 지키러 들어가 돌아오지 못했지."),
                    at(STEP_SERPENT_DOWN, "바다가 잔잔해졌소. 내일은 마리와 함께 첫 그물을 던지러 가오."),
            }),
            act(SALT, 3, 11, "innkeeper", "어부의 여관", ACT_INN, 14,
                    at(0, "어부들이 쉬어 가는 작은 여관이오.")),
            act(SALT, 7, 11, "merchant", "소금꽃 잡화점", ACT_SHOP, 4,
                    at(0, "소금, 그물, 약초… 어촌에 필요한 건 다 있소.")),
            npc(SALT, 11, 13, "sailor", "늙은 어부",
                    at(0, "등대 불빛이 10년째 꺼지지 않는 이유? 노아 영감이 누군가를 기다리고 있거든.",
                            "쌍둥이 중 형, 바로스. 둘 다 저 등대에서 자라다시피 했지."),
                    at(STEP_SERPENT_DOWN, "노아 영감이 오늘 등대에서 노래를 부르더군. 그 영감 노래하는 건 처음 봤소.")),

            // ---- 곶의 등대
            new Npc(LIGHTHOUSE, 6, 2, "elder", "등대지기 노아", ACT_EVENT, EV_NOAH, 0, 99, new Object[][]{
                    at(0, "바로스와 바론… 쌍둥이는 어릴 적 이 등대에서 살다시피 했지.",
                            "10년 전, 바로스는 '길드가 봉인석을 캐면 안 된다'며 해저 신전으로 내려갔고 돌아오지 않았어.",
                            "바론은 그날 이후 돈만 세는 사람이 됐지. 형을 잃은 죄책감을 금화로 덮으려는 게야.",
                            "나는 매일 밤 등불을 켜네. 바로스가 돌아올 길을 비추려고.",
                            "해저 신전은 등대의 등불이 있어야 길이 열려. …하지만 낯선 이에게 내줄 순 없네."),
                    at(STEP_LAMP, "곶 끝 바다에 등불을 비추게. 신전으로 가는 돌계단이 떠오를 걸세."),
                    at(STEP_SERPENT_DOWN, "바로스가… 돌아왔구나. 이제 등불을 꺼도 되겠어.",
                            "아니, 오늘 밤은 더 밝게 켜야지. 바론이 형을 배웅할 수 있도록."),
            }),

            // ---- 물보라 동굴: 중간 보스
            new Npc(SPRAY2, 5, 4, "pirate", "붉은 수염 갈리온", ACT_EVENT, EV_MIDBOSS, STEP_GUILD_REFUSED, STEP_GUILD_REFUSED, new Object[][]{
                    at(0, "(동굴 끝, 보물 더미 위에 붉은 수염의 사내가 앉아 있다)",
                            "하! 요정 꼬마의 저울을 찾으러 왔나? 이건 이제 내 거다!",
                            "회색 망토 나리가 그러더군. 짐꾼이 오면 붙잡아 두라고. 금화를 산더미처럼 준다고!",
                            "[리나] 당신… 소금꽃 마을 마리의 아버지죠? 마리가 기다리고 있어요!",
                            "…그 이름을 꺼내지 마라! 이제 와서 돌아갈 수 있을 것 같으냐!",
                            "[신창] 돌아갈 수 있어요. 짐은 언제든 내려놓을 수 있으니까.",
                            "닥쳐라! 덤벼라, 짐꾼!"),
            }),

            // ---- 해저 신전: 보스
            new Npc(TEMPLE2, 11, 3, "serpent", "심해의 군주", ACT_EVENT, EV_BOSS2, STEP_LAMP, STEP_LAMP, new Object[][]{
                    at(0, "(신전 가장 깊은 곳. 금 간 푸른 봉인석을 거대한 바다뱀이 칭칭 감고 있다)",
                            "(바다뱀의 비늘 사이로 사람의 얼굴 같은 그림자가 일렁인다)",
                            "…쉬이이… 문 저편의… 그릇…",
                            "[???] …오지… 마… 바론… 팔지… 마…",
                            "[피피] 저 목소리… 바로스 아저씨야! 뱀 안에 바로스 아저씨가 있어!",
                            "[리나] 어둠에 먹힌 수호자가, 봉인을 지키러 온 사람까지 삼킨 거예요…!",
                            "[신창] 둘 다 데려가자. 바로스 씨도, 이 바다의 수호자도.",
                            "쉬이이이익——!!"),
            }),

            // ---- 3장: 메르카 부두의 길드 배
            new Npc(MERCA, 15, 23, "sailor", "길드의 선장", ACT_EVENT, EV_SHIP_TO_IGNA, STEP_CHAPTER2_CLEAR, STEP_CHAPTER2_CLEAR, new Object[][]{
                    at(0, "바론 님의 명으로 이그나까지 모시겠습니다."),
            }),

            // ---- 3장: 이그나 마을
            new Npc(IGNA, 6, 4, "jiheon", "지헌", ACT_EVENT, EV_JIHEON, STEP_ALONE, STEP_ALONE, new Object[][]{
                    at(0, "아, 깼어요?"),
            }),
            new Npc(IGNA, 8, 6, "villager", "지헌 어머니 해련", ACT_EVENT, EV_BIRTH, STEP_ALONE, 99, new Object[][]{
                    at(0, "딸애가 바다에서 사람을 주워 왔지 뭐요. 제 아비를 닮아서 다친 건 못 지나쳐요.",
                            "푹 쉬시오. 여기 침대는 원래 그 애 아비 거였소."),
                    at(STEP_JIHEON, "지헌이를 부탁하오. 겉으론 씩씩해도 제 아비 일로 밤마다 운다오.",
                            "용암 동굴엔 그이가 만든 화염 거인이 있소. 원래는 순한 녀석이었는데…"),
                    at(STEP_GIANT_DOWN, "원석을 찾았다고? …그이가 그 원석으로 마지막에 벼리고 싶어 하던 게 뭔지 아시오?",
                            "딸의 혼인 반지였다오. 촌장님께 화덕을 빌리러 가 보시오."),
                    at(STEP_WEDDING, "우리 사위! 몸조심하시오. 지헌이도. 둘 다 꼭 살아서 돌아와야 하오."),
                    at(STEP_REVENGE, "돌아왔구려… 그이의 원수를 갚아 줘서 고맙소. 이제 어디 가지 말고 여기서 사시오."),
                    at(STEP_CHAPTER3_CLEAR, "민한이가 아빠를 닮아 손이 커요. 언젠가 큰 짐도 번쩍 들겠지.",
                            "…무거운 짐 말고, 좋은 것만 들고 살았으면 좋겠소."),
            }),
            new Npc(IGNA, 4, 4, "baby", "아기 민한", ACT_NONE, 0, STEP_CHAPTER3_CLEAR, 99, new Object[][]{
                    at(0, "(아기 민한이 꼬물꼬물 손을 뻗는다) 아부… 아부부……",
                            "(작은 손이 신창의 손가락을 꼭 쥐었다. 생각보다 힘이 세다)"),
            }),
            new Npc(IGNA, 21, 4, "elder", "이그나 촌장", ACT_EVENT, EV_IGNA_ELDER, STEP_ALONE, 99, new Object[][]{
                    at(0, "한 달 전 화산에서 검은 요새가 솟은 뒤로, 마을 남자들이 하나둘 끌려갔소.",
                            "남은 건 늙은이와 아녀자와 아이들뿐이오. 요새로 가는 길은 검은 불길이 막고 있지.",
                            "동쪽 용암 동굴엔 대장장이 도건이 만든 '화염 거인'이 사오. 광맥을 지키던 순한 놈인데, 요즘은 미쳐 날뛴다오."),
                    at(STEP_WEDDING, "요새로 가는 길의 검은 불길이 걷혔소. 화산 기슭, 북쪽 길 끝이오.",
                            "용사여, 끌려간 마을 사람들을 부탁하오. 그리고… 신부를 울리지 마시오."),
                    at(STEP_HOME, "아기 울음소리가 곧 들리겠군. 이그나에 십 년 만의 경사요.",
                            "해련 할멈에게 가 보시오. 산파 노릇은 그 할멈이 이 섬에서 제일이오."),
                    at(STEP_CHAPTER3_CLEAR, "민한이가 첫 울음을 터뜨린 날, 화산이 조용해졌소. 섬 전체가 그 애를 반긴 게요."),
            }),
            npc(IGNA, 4, 10, "apprentice", "견습 대장장이 단",
                    at(0, "도건 스승님은 '쇠는 불을 견뎌야 단단해진다'고 하셨어요. 사람도 그렇대요.",
                            "스승님 망치는 용암 동굴에서 사라졌어요. 원석을 캐러 가셨다가……"),
                    at(STEP_WEDDING, "지헌 누나가 벼린 검이라니! 스승님보다 낫다고는 말 못 하지만… 아니, 나을지도요."),
                    at(STEP_HOME, "대장간을 다시 세우는 거, 저도 도울게요! 이번엔 요람도 만들어야겠네요.")),
            npc(IGNA, 16, 9, "child", "꼬마 솔",
                    at(0, "우리 아빠도 요새로 끌려갔어. 검은 갑옷 아저씨가 '쓸모 있는 몸'이래.",
                            "아저씨가 데려와 줄 거지? …근데 아저씨 맨손이잖아."),
                    at(STEP_WEDDING, "와, 반짝반짝 갑옷! 이제 진짜 용사 같아! 우리 아빠 꼭 데려와 줘!"),
                    at(STEP_REVENGE, "아빠가 돌아왔어! 용사 아저씨 최고! 나도 커서 용사 할 거야!")),
            npc(IGNA, 21, 10, "villager", "이웃 할머니",
                    at(0, "지헌이가 사내를 업고 오던 날, 온 마을이 다 들썩였지. 그 애가 누굴 저렇게 챙기는 건 처음이야."),
                    at(STEP_GIANT_DOWN, "둘이 동굴에서 돌아오는 걸 봤는데, 지헌이 귀가 빨갛더라. 호호."),
                    at(STEP_WEDDING, "혼례 잔치 국수 맛있었지? 지헌이 웃는 걸 한 달 만에 봤어."),
                    at(STEP_HOME, "배가 불러 오는구먼. 봄이면 셋이 되겠어.")),
            npc(IGNA, 9, 17, "sailor", "이그나 어부",
                    at(0, "검은 모래 해변엔 바다에서 떠밀려 온 게 많소. 부서진 배 잔해도 거기 있지.",
                            "요새가 솟은 뒤로 섬 몬스터가 사나워졌소. 맨몸으로 나가면 큰일 나오."),
                    at(STEP_REVENGE, "요새의 검은 불이 꺼졌소! 바다도 다시 잠잠해졌고.")),
            new Npc(IGNA, 18, 9, "farmer", "돌아온 사내", ACT_NONE, 0, STEP_REVENGE, 99, new Object[][]{
                    at(0, "요새 감옥에서 한 달을 갇혀 있었소. 그런 우리를 용사 부부가 꺼내 줬지!",
                            "감옥에서 들었는데, 바르가스가 '로웰이라는 기사는 문지기님이 직접 데려갔다'고 하더군."),
            }),
            act(IGNA, 4, 12, "merchant", "이그나 도구점", ACT_SHOP, 5,
                    at(0, "화산섬이라 뭐든 비싸요. 흑요석 검은 우리 섬 명물이에요."),
                    at(STEP_WEDDING, "용사님! 신혼 선물로 깎아 드리고 싶지만… 장사는 장사라서요.")),
            act(IGNA, 8, 12, "innkeeper", "화산 여관", ACT_INN, 30,
                    at(0, "온천이 딸린 여관이오. 화산 덕분에 물은 늘 따뜻하지.")),
            act(IGNA, 17, 14, "priest", "불의 사당 무녀", ACT_SAVE, 0,
                    at(0, "불꽃은 쌓아 두면 꺼지고, 나누면 번진답니다. 모험을 기록해 드리지요."),
                    at(STEP_WEDDING, "두 분의 맹세를 불꽃이 기억하고 있어요. 모험을 기록해 드리지요.")),

            // ---- 3장: 용암 동굴 보스
            new Npc(LAVA2, 5, 4, "giant", "화염 거인", ACT_EVENT, EV_GIANT, STEP_JIHEON, STEP_JIHEON, new Object[][]{
                    at(0, "(용암 웅덩이 사이로, 바위와 쇠를 이어 붙인 거인이 몸을 일으킨다. 가슴에서 검은 불꽃이 새어 나온다)",
                            "[지헌] 화염 거인…! 아버지가 광맥을 지키라고 만든 아이예요. 저 검은 불꽃… 바르가스 짓이야.",
                            "(거인의 발치에 낡은 망치 하나가 박혀 있다. 손잡이에 '도건'이라는 글씨)",
                            "[지헌] 아버지 망치…! 아버지는 여기까지 와서……",
                            "[신창] 지헌 씨, 물러서지 마요. 이번엔… 아무도 안 잃어.",
                            "쿠오오오오——!!"),
            }),

            // ---- 3장: 흑염의 요새 (결혼 뒤)
            new Npc(FORT1, 18, 7, "ignis", "화염 기사단장 이그론", ACT_EVENT, EV_GEN1, STEP_WEDDING, STEP_WEDDING, new Object[][]{
                    at(0, "하찮은 배달부가 용사 흉내라. 바르가스 님의 검을 받기 전에 내가 먼저 재로 만들어 주마!",
                            "[지헌] 마을 사람들을 끌고 간 게 당신들이죠? 하나도 빠짐없이 돌려받을 거예요.",
                            "[신창] 비켜. 오늘은 한 명도 안 놓쳐."),
            }),
            new Npc(FORT2, 1, 8, "witch", "독안개 마녀 모르가", ACT_EVENT, EV_GEN2, STEP_GEN1, STEP_GEN1, new Object[][]{
                    at(0, "후후, 신혼부부? 사랑 놀음이 그렇게 좋아? 둘이 같이 죽으면 더 낭만적이겠지?",
                            "[신창] 미안하지만 우리 둘은 같이 살기로 정했어.",
                            "[지헌] 그것도 아주 오래오래요."),
            }),
            new Npc(FORT3, 11, 5, "lavadragon", "용암룡 볼카", ACT_EVENT, EV_GEN3, STEP_GEN2, STEP_GEN2, new Object[][]{
                    at(0, "(용암 속에서 거대한 용이 머리를 들어 올린다. 옥좌로 가는 문을 막아선 채 불길을 뿜는다)",
                            "크르르… 이 문 너머는 바르가스 님의 옥좌. 지나가려면 나를 넘어라!"),
            }),
            new Npc(FORT3, 11, 2, "vargas", "흑염의 마장군 바르가스", ACT_EVENT, EV_VARGAS, STEP_GEN3, STEP_GEN3, new Object[][]{
                    at(0, "(검은 불꽃의 옥좌 앞. 바르가스가 천천히 일어선다)",
                            "왔군, 짐꾼. 아니… 그 빛은 뭐지? 너는 비어야 했다. 다 잃고 텅 비어서 그릇이 되어야 했단 말이다!",
                            "[신창] 다 잃었지. 그런데 비지 않았어. 잃은 자리에 다른 게 들어왔거든.",
                            "[지헌] 당신이 우리 아버지를 죽였어요. 대장간을 태우고, 마을 사람들을 끌고 갔어요.",
                            "[신창] 그리고 내 동료들을 데려갔지. 리나와 피피는 어디 있어!",
                            "크하하! 알고 싶으면 나를 꺾어 봐라! 문지기님께 받은 공허의 불꽃으로, 이번엔 너도 재로 만들어 주마!"),
            }),

            // ---- 4장: 이그나 (민한이 자라는 동안)
            new Npc(IGNA, 14, 16, "kid", "어린 민한", ACT_EVENT, EV_KID, STEP_FAMILY, STEP_FAMILY, new Object[][]{
                    at(0, "아빠! 아빠!"),
            }),
            new Npc(IGNA, 16, 18, "granny", "인자한 할머니", ACT_NONE, 0, STEP_FAMILY, STEP_FAMILY, new Object[][]{
                    at(0, "호호, 처음 보는 얼굴이라고? 바다 건너 섬에서 온 떠돌이 약초꾼이라오.",
                            "저 아이가 용사님 아들이구먼. 눈이 반짝반짝, 궁금한 게 많은 눈이야.",
                            "(할머니의 그림자가 잠깐… 꼬리가 여러 개 달린 짐승 모양으로 일렁인 것 같다)"),
            }),
            new Npc(IGNA, 18, 18, "villager", "걱정 많은 아낙", ACT_NONE, 0, STEP_KIDNAPPED, STEP_HOSTAGE, new Object[][]{
                    at(0, "민한이가 그 약초꾼 할머니 손을 잡고 남동쪽 꽃길로 가는 걸 봤어요!",
                            "그 너머는 안개 호수예요… 어서 가 보세요!"),
            }),

            // ---- 4장: 안개 호수
            new Npc(LAKE, 11, 9, "fox", "인자한 할머니?", ACT_EVENT, EV_FOX1, STEP_KIDNAPPED, STEP_HOSTAGE, new Object[][]{
                    at(0, "(안개 낀 호숫가. 할머니가 잠든 민한을 무릎에 누이고 자장가를 흥얼거리고 있다)",
                            "[신창] 민한아!!",
                            "호호… 왔구먼, 용사님. 생각보다 빠르네.",
                            "[지헌] 그 아이에게서 손 떼요. 당장.",
                            "(할머니의 몸이 뒤틀리며 아홉 개의 꼬리가 펼쳐진다. 얼굴이 천 개의 가면처럼 바뀐다)",
                            "[천면귀] 문지기님 말씀이 맞았어. 빈 그릇이 다시 가득 찼다고. 아내에, 집에, 아들까지!",
                            "[천면귀] 그러니 다시 비워 줘야지. 이번엔 가장 소중한 것부터. 호호호!"),
                    at(STEP_HOSTAGE, "(천면귀가 민한의 목에 날카로운 손톱을 들이대고 있다)",
                            "[천면귀] 한 발짝이라도 움직이면… 알지?"),
            }),
            new Npc(LAKE, 12, 9, "kid", "잠든 민한", ACT_NONE, 0, STEP_KIDNAPPED, STEP_HOSTAGE, new Object[][]{
                    at(0, "(민한은 깊은 잠에 빠져 있다. 손에 호숫가에서 딴 꽃을 쥐고 있다)"),
            }),

            // ---- 4장: 짐승의 보금자리
            new Npc(BEASTDEN, 11, 4, "owl", "백년 부엉이 노을", ACT_EVENT, EV_OWL, STEP_MINHAN, 99, new Object[][]{
                    at(0, "부엉. 민한아, 사람의 마을 '로아'는 숲을 나가 남동쪽 길 끝이란다.",
                            "네가 이 숲에 떨어지던 날 메고 있던 그 낡은 가방… 로아의 '해의 제단'에 가져가 보거라."),
                    at(STEP_LEGACY, "해의 유산이라… 네 아버지는 문 저편에서 해를 수없이 모았다더구나. 그 해가 지금 너를 지키는 게야.",
                            "동쪽 고원의 유적에 사람 모양 석상이 있다는 소문이 있다. 가 보거라."),
                    at(STEP_DAD_FOUND, "아버지를 찾았구나…! 그럼 어머니도 멀지 않았을 게다. 동남쪽 수정 동굴을 찾아보거라."),
                    at(STEP_MOM_FOUND, "달빛 이슬은 숲 깊은 곳 '달빛 샘'에 고여 있단다. 보금자리 동쪽의 달빛 숲길로 들어가거라.",
                            "…그런데 요즘 샘을 지키던 숲의 주인이 어둠에 먹혔다는 소문이 있구나. 조심하거라."),
                    at(STEP_DEW, "달빛 이슬을 석상에 부어 주거라. 어머니부터 가 보는 게 좋겠구나. 이슬의 빛은 어머니의 마법에서 왔으니."),
                    at(STEP_DAD_BACK, "북쪽 산의 요마성으로 가는 길이 열렸다. 네 가족을 갈라놓은 그 여우가 거기 있다."),
                    at(STEP_CHAPTER4_CLEAR, "부엉. 언제든 놀러 오너라. 숲은 늘 네 집이란다."),
            }),
            act(BEASTDEN, 5, 9, "bear", "곰 어미 두두", ACT_INN, 0,
                    at(0, "크릉. 우리 아기 민한, 다쳤니? 이리 와서 둥지에서 자렴. 공짜야, 공짜.")),
            act(BEASTDEN, 18, 9, "deer", "흰 사슴", ACT_SAVE, 0,
                    at(0, "(흰 사슴이 이마를 민한의 손에 댄다. 지금까지의 모험이 숲에 새겨진다)")),
            npc(BEASTDEN, 8, 13, "rabbit", "토끼 삼남매",
                    at(0, "민한 형아다! 형아 또 사냥 가? 우리도 데려가!",
                            "형아는 맨날 밤에 별 보면서 엄마 아빠 생각하잖아. 우리 다 알아."),
                    at(STEP_MOM_BACK, "형아 엄마다! 진짜 예쁘다! 형아랑 눈이 똑같아!")),
            npc(BEASTDEN, 16, 13, "squirrel", "다람쥐 톡톡",
                    at(0, "도토리 줄까? 아, 너 이제 사람 음식 먹어야 한다고? 쳇.",
                            "몬스터가 사나워졌어. 숲 밖은 조심해. 특히 반짝이는 무지개 거북은 쫓아가 봐! 금화를 잔뜩 떨군대."),
                    at(STEP_DAD_BACK, "너희 아빠 진짜 세다! 곰 아줌마보다 세!")),

            // ---- 4장: 로아 마을
            new Npc(ROA, 12, 3, "elder", "해의 제단지기", ACT_EVENT, EV_ALTAR, STEP_MINHAN, 99, new Object[][]{
                    at(0, "해의 제단에 온 것을 환영하오."),
            }),
            act(ROA, 4, 12, "merchant", "로아 도구점", ACT_SHOP, 6,
                    at(0, "숲 소년이구나! 골드가 있으면 뭐든 팔지.")),
            act(ROA, 8, 12, "innkeeper", "들꽃 여관", ACT_INN, 40,
                    at(0, "들꽃 여관이에요. 짐승 냄새는… 괜찮아요, 씻고 주무세요.")),
            act(ROA, 19, 14, "priest", "로아 예배당 사제", ACT_SAVE, 0,
                    at(0, "길 잃은 아이에게 빛이 있기를. 모험을 기록해 드리지요.")),
            npc(ROA, 16, 9, "farmer", "로아 농부",
                    at(0, "동쪽 고원 유적에 사람 석상이 하나 있는데, 밤마다 손끝이 빛난대. 무섭지?",
                            "그 석상 앞을 돌 거인이 지키고 있어서 아무도 못 가."),
                    at(STEP_DAD_FOUND, "고원 석상이 네 아버지였다고? 세상에…")),
            npc(ROA, 4, 10, "villager", "로아 할머니",
                    at(0, "수정 동굴엔 우는 여인의 석상이 있다는구나. 동굴이 울 때마다 수정이 자란다지.",
                            "…진짜 할머니인지 의심하는 눈이구나. 어릴 때 나쁜 할머니를 만났니? 저런."),
                    at(STEP_MOM_BACK, "어머니를 되찾았다니! 그 우는 수정이 이제 웃는 소리를 낸다는구나.")),
            npc(ROA, 21, 10, "child", "로아 꼬마",
                    at(0, "형 늑대랑 다녀? 멋있다! 늑대 만져 봐도 돼?",
                            "북쪽 산 꼭대기엔 요마성이 있어. 검은 안개 때문에 아무도 못 올라가."),
                    at(STEP_DAD_BACK, "요마성 안개가 걷혔어! 형이 한 거지?")),

            // ---- 4장: 석상이 된 부모
            new Npc(RUINS3, 12, 2, "statue_dad", "석상", ACT_EVENT, EV_DAD_STATUE, STEP_MINHAN, STEP_MOM_BACK, new Object[][]{
                    at(0, "(검을 들어 무언가를 막으려는 자세의 석상이다)"),
            }),
            new Npc(CRYSTAL3, 4, 2, "statue_mom", "석상", ACT_EVENT, EV_MOM_STATUE, STEP_MINHAN, STEP_DEW, new Object[][]{
                    at(0, "(두 손을 앞으로 뻗은 여인의 석상이다)"),
            }),

            // ---- 4장: 달빛 샘
            new Npc(MOON2, 11, 3, "forestlord", "어둠에 먹힌 숲의 주인", ACT_EVENT, EV_MOONLORD, STEP_MOM_FOUND, STEP_MOM_FOUND, new Object[][]{
                    at(0, "(달빛 샘 앞. 거대한 사슴뿔을 단 검은 짐승이 샘을 막아서고 있다. 샘물이 검게 물들어 있다)",
                            "[민한] 숲의 주인님…? 부엉이 할아버지가 말한, 샘을 지키던 분이야.",
                            "[하루] 크르르… (하루의 털이 곤두선다)",
                            "그르르… 사람의… 아이… 짐승의 눈물은… 이제… 내 것……",
                            "[민한] 그 눈물은 나를 위해 다들 흘려 준 거야. 돌려줘!"),
            }),

            // ---- 4장: 요마성
            new Npc(DEMON1, 18, 7, "fox", "천면귀", ACT_EVENT, EV_FOX2, STEP_DAD_BACK, STEP_DAD_BACK, new Object[][]{
                    at(0, "어머, 어머. 이게 누구야? 그때 그 꼬마? 다 컸네?",
                            "[민한] …할머니. 아니, 천면귀.",
                            "[천면귀] 돌로 만들어 흩어 놨는데, 그걸 다 찾아서 되살렸다고? 짐승한테 키워진 주제에!",
                            "[지헌] 그 짐승들이 당신보다 백 배는 따뜻했어요.",
                            "[신창] 8년이다. 내 아들의 8년을 돌려받겠다.",
                            "[천면귀] 호호호! 이번엔 인질도 없어. 정정당당히… 아니, 비겁하게 가 볼까!"),
            }),
            new Npc(DEMON2, 11, 2, "demonking", "요마왕 다르칸", ACT_EVENT, EV_KING4, STEP_FOX_DOWN, STEP_FOX_DOWN, new Object[][]{
                    at(0, "(검은 옥좌에서 뿔 달린 거인이 몸을 일으킨다. 요마들이 일제히 엎드린다)",
                            "천면귀가 졌다고? 쓸모없는 것. 하긴, 가족이란 것은 늘 계산을 망치지.",
                            "[민한] 당신이 시켰어? 우리 가족을 부수라고.",
                            "[다르칸] 시킨 건 문지기다. 나는 그 대가로 이 대륙을 받기로 했을 뿐.",
                            "[다르칸] 짐꾼이 다시 비어야 그릇이 된다더군. 아내와 아들을 빼앗으면 비겠지. 그런데……",
                            "[신창] 안 비었어. 아들이 8년 동안 혼자 채워 왔거든.",
                            "[민한] 아빠, 엄마. 하루. 같이 가요. 이번엔 우리가 지킬 차례예요!"),
            }),
    };

    // ------------------------------------------------------------ 살펴볼 수 있는 것 (단서)

    public static final class Readable {
        public final int map, x, y;
        /** 처음 읽을 때 세우는 단서 (0 이면 없음) */
        public final int flag;
        /** 처음 읽을 때 받는 아이템 (-1 이면 없음) */
        public final int item;
        public final String[] lines;
        /** 리나가 동료일 때 덧붙는 대사 (없으면 null) */
        public final String linaLine;

        Readable(int map, int x, int y, int flag, int item, String linaLine, String... lines) {
            this.map = map;
            this.x = x;
            this.y = y;
            this.flag = flag;
            this.item = item;
            this.linaLine = linaLine;
            this.lines = lines;
        }
    }

    public static final int LOWELL_DIARY = 32, SEAL_SHARD = 33, LUPUS_BELL = 34;

    public static final Readable[] READABLES = {
            // 청와대
            new Readable(OFFICE, 6, 1, 0, -1, null,
                    "역대 대통령들의 초상화가 걸려 있다. 신창의 초상화는 아직 걸리지 않았다."),
            new Readable(OFFICE, 15, 4, FLAG_RECORD, -1, null,
                    "「금고 봉인 기록」",
                    "'이 장치를 결코 작동시키지 말 것. 문 저편에서 오는 자는 짐꾼이 아니다.' — E.",
                    "(종이 귀퉁이에 낯선 문양이 찍혀 있다. 원 안에 다섯 개의 점.)",
                    "[신창] …이 문양, 내 배달 가방에 있는 거랑 똑같잖아?",
                    "[신창] 첫 배달 날, 이름 모를 할머니가 이 가방을 주면서 그랬지. '언젠가 아주 무거운 짐을 나르게 될 거야.'"),
            // 리벨
            new Readable(REBEL, 11, 18, 0, -1, null,
                    "「리벨 마을」 북쪽: 에르덴 성 / 북동쪽 산: 출입 금지 (그림자 동굴)"),
            new Readable(REBEL, 3, 3, FLAG_CHRONICLE, -1, null,
                    "「리벨 연대기」를 펼쳤다.",
                    "'300년 전, 형체 없는 공허의 왕이 땅을 삼키려 하였다. 짐꾼 카이른이 다섯 봉인석을 대륙 끝까지 날라 그것을 가두었다.'",
                    "'카이른의 곁에는 문을 만드는 현자 아르센과, 그의 누이 엘라가 있었다.'",
                    "'봉인이 끝난 밤, 남매는 크게 다투었고 둘 다 자취를 감추었다.'"),
            new Readable(REBEL, 4, 3, 0, -1, null, "약초 도감과 오래된 지도책이 꽂혀 있다."),
            new Readable(REBEL, 17, 20, FLAG_MURAL, -1, null,
                    "벽화: 원 안의 다섯 점 문양을 새긴 가방을 멘 사람이, 빛나는 돌 다섯 개를 지고 산과 바다와 불과 빛을 지나 걷는다.",
                    "(그 가방의 문양은… 신창의 가방과 똑같다.)"),
            new Readable(REBEL, 22, 20, FLAG_MURAL, -1, "[리나] 짐을… 내려놓고 웃고 있네요. 무거운 걸 다 나른 사람의 얼굴이에요.",
                    "벽화: 마지막 그림. 짐꾼은 등의 짐을 모두 내려놓고, 검은 구름이 걷힌 하늘을 올려다보며 웃고 있다.",
                    "그 곁에 회색 망토를 쓴 사람이 등을 돌린 채 서 있다."),
            new Readable(REBEL, 12, 9, 0, -1, null, "깊은 우물이다. 들여다보니… 대통령 시절보다 얼굴이 조금 편안해 보인다."),
            // 에르덴
            new Readable(ERDEN, 12, 24, 0, -1, null, "「에르덴 성」 남쪽: 리벨 마을 / 북동쪽 산: 그림자 동굴 (기사단 외 출입 금지)"),
            new Readable(ERDEN, 13, 2, 0, -1, null,
                    "에르덴 왕가의 옥좌. 등받이에 원 안의 다섯 점 문양이 새겨져 있다.",
                    "옥좌 아래 글귀: '짐꾼에게 길을 내준 왕가, 짐꾼을 잊지 않으리.'"),
            new Readable(ERDEN, 5, 2, FLAG_LIBRARY, -1, null,
                    "「공허의 왕에 관하여」",
                    "'그것은 형체가 없다. 사람의 끝없는 욕망을 먹고 자라며, 가장 많이 가진 자의 몸을 탐한다.'",
                    "[신창] 가장 많이 가진 자라…"),
            new Readable(ERDEN, 6, 2, FLAG_LIBRARY, -1, null,
                    "「다섯 봉인석」",
                    "'그림자의 돌, 바다의 돌, 불의 돌, 빛의 돌. 그리고 다섯 번째는…'",
                    "(다음 장이 찢겨 나가 있다.)"),
            new Readable(ERDEN, 7, 2, FLAG_LIBRARY, -1, null,
                    "「현자 아르센」",
                    "'차원의 문을 세운 현자. 봉인은 미봉책일 뿐이니 공허의 왕을 담을 그릇을 찾아 다스려야 한다고 주장하였다.'",
                    "'누이 엘라는 이를 크게 반대하였다. 그날 밤 이후 남매를 본 사람은 없다.'"),
            new Readable(ERDEN, 8, 2, 0, -1, null, "에르덴 왕국의 법전과 세금 장부다. 대통령 시절이 떠올라 머리가 아프다."),
            // 그림자 동굴
            new Readable(CAVE1, 5, 12, 0, -1, "[리나] 이건 기사단 표식이에요! 아버지가 여기까지 오셨어요.",
                    "벽에 긁힌 글씨가 있다. '회색 망토… 2층… 봉인석을…'",
                    "글씨 옆에 원 안의 다섯 점 문양이 새겨져 있다."),
            new Readable(CAVE2, 17, 11, FLAG_DIARY, LOWELL_DIARY,
                    "[리나] 아버지…! 살아 계셔. 바다로 가신 거야. 꼭 찾을 거예요.",
                    "짐꾼의 문양이 새겨진 기사의 방패가 떨어져 있다. 방패 뒤에 일기장이 끼워져 있다.",
                    "「로웰의 일기」 '회색 망토의 사내가 지팡이로 봉인석을 내리쳤다. 그는 자신을 문지기라 불렀다.'",
                    "'그는 말했다. 문 저편에서 가장 많이 가진 자가 오면, 공허의 왕께 바칠 그릇이 완성된다고.'",
                    "'나는 그자를 쫓아 바다로 간다. 리나, 미안하다. 이 방패는 짐꾼에게 길을 알려 주리라.'",
                    "(「로웰의 일기」를 손에 넣었다!)"),
            // ---- 2장
            new Readable(MERCA, 2, 10, 0, -1, null,
                    "「항구 도시 메르카」 북쪽: 상인 길드 / 남쪽: 부두 / 서쪽 출구: 메르카 평원"),
            new Readable(MERCA, 17, 2, FLAG_LEDGER, -1, "[피피] 'M'… 에르덴에서 온 'M'이라면? 냄새가 나. 돈 냄새 말고, 음모 냄새.",
                    "「길드 장부」를 펼쳤다.",
                    "'푸른 돌 발굴 자금: 금화 3만 — 에르덴, M.'",
                    "'푸른 돌 판매처: 대륙 각지. 부스러기는 목걸이로 가공할 것.'",
                    "[신창] 에르덴의 M… 대신 모르칸?"),
            new Readable(MERCA, 18, 2, 0, -1, null, "길드의 오래된 계약서들이다. 10년 전 장부 한 권만 유독 손때가 묻어 있다."),
            new Readable(MERCA, 23, 14, FLAG_SHIPLOG, -1, "[리나] 아버지가… 이그나로! 살아 계셔. 회색 망토를 아직 쫓고 계신 거야.",
                    "「선박 기록」을 넘겼다.",
                    "'한 달 전. 에르덴 기사 로웰, 화산섬 이그나행 화물선 승선. 사유: 회색 망토의 사내 추적.'",
                    "'같은 날, 회색 망토의 사내 역시 이그나행 배를 수소문함. 배삯은 금화로 치름.'"),
            new Readable(MERCA, 24, 14, 0, -1, null, "메르카 항로도다. 북쪽 끝에 붉게 칠한 섬 '이그나'가 있다."),
            new Readable(SALT, 10, 2, 0, -1, null, "「소금꽃 마을」 북쪽 길: 메르카 평원 / 곶 끝: 등대"),
            new Readable(SALT, 3, 3, 0, -1, null, "물때표와 그물 수선법이 적힌 책이다. 맨 뒷장에 아이 글씨로 '아빠 빨리 와'라고 적혀 있다."),
            new Readable(LIGHTHOUSE, 2, 2, FLAG_LIGHTLOG, -1, null,
                    "「등대 일지」를 펼쳤다.",
                    "'○월 ○일. 바로스가 해저 신전으로 내려갔다. 길드의 발굴을 막겠다며. 바론은 따라가지 않았다.'",
                    "'○월 ○일. 오늘도 바로스는 돌아오지 않았다. 밤바다가 푸르게 빛났다.'",
                    "'○월 ○일. 바론이 처음으로 등대에 왔다. 아무 말 없이 바다만 보다 갔다. 손에 형의 나침반 상자를 쥐고 있었다. 상자는 비어 있었다.'"),
            new Readable(LIGHTHOUSE, 3, 2, 0, -1, null, "바다 지도와 별자리 책이다. 곶 끝 바다에 '등불로 여는 계단'이 그려져 있다."),
            new Readable(SPRAY1, 5, 2, 0, -1, null,
                    "벽에 해적들의 낙서가 있다. '두목은 회색 망토에게 금화를 받았다. 푸른 돌을 에르덴 배에 실어 나르면 두 배.'"),
            new Readable(TEMPLE1, 2, 2, FLAG_TEMPLE, -1, "[피피] 등불 든 여자… 노아 할아버지 등불이랑 똑같이 생겼어.",
                    "벽화: 짐꾼 카이른이 푸른 돌을 바다 깊이 내려놓는다.",
                    "곁에서 한 여인이 등불을 들고 길을 비춘다. 여인의 망토 자락에 원 안의 다섯 점 문양이 있다."),
            new Readable(TEMPLE1, 21, 2, FLAG_TEMPLE, -1, null,
                    "벽화: 거대한 바다뱀이 돌을 감싸고 잠든다.",
                    "글귀: '수호자 심해의 군주. 어둠이 닿지 않는 한, 그는 바다의 친구라.'"),
            new Readable(TEMPLE2, 2, 7, FLAG_PROPHECY, -1, "[리나] '그릇이 오는 날'… 신창 씨가 온 날부터 봉인이 하나씩 깨지고 있어요.",
                    "누군가 지팡이로 새긴 듯한 새 글귀가 벽을 긁고 있다.",
                    "'그릇이 오는 날, 다섯 봉인은 하나씩 열린다. 그릇이 가득 차면 문이 완성되리라. — 문지기'"),
            new Readable(TEMPLE2, 4, 8, FLAG_COMPASS, RpgData.BAROS_COMPASS, "[피피] 이 나침반… 저울에 올리면 바늘이 멈추질 않아. 엄청 무거운 마음이 담겨 있어.",
                    "바닷물에 삭은 가방이 떨어져 있다. 안에 녹슨 나침반이 있다.",
                    "뒷면에 새겨진 글씨: '바론에게. 돈보다 무거운 걸 잊지 마. — 바로스'",
                    "(「바로스의 나침반」을 손에 넣었다!)"),
            // ---- 3장
            new Readable(WORLD3, 16, 20, 0, -1, null,
                    "부서진 배의 잔해다. 바론의 길드 깃발이 반쯤 타 있다.",
                    "(모래 속에 동전 하나가 박혀 있다. 피피의 동전 주머니에서 나온 것이다)",
                    "[신창] ……피피."),
            new Readable(WORLD3, 17, 21, 0, -1, null,
                    "부러진 돛대다. 리나의 방패에 있던 다섯 점 문양 조각이 걸려 있다.",
                    "[신창] 리나… 기다려. 반드시 찾으러 갈게."),
            new Readable(IGNA, 9, 3, FLAG_SMITHLOG, -1, "[지헌] 아버지… 끝까지 거절하셨구나. 그래서……",
                    "「도건의 작업 일지」를 펼쳤다.",
                    "'한 달 전. 회색 망토의 사내가 찾아와 「그릇을 묶을 족쇄」를 벼려 달라 했다. 금화를 산더미처럼 내밀었지만 거절했다.'",
                    "'사내가 웃으며 말했다. 그릇은 곧 이 섬에 온다. 다 잃고 빈 몸으로.'",
                    "'오늘 화산에서 검은 요새가 솟았다. …지헌이 혼례 올리는 날, 반지만은 내 손으로 벼려 주고 싶은데.'"),
            new Readable(IGNA, 11, 17, 0, -1, null, "「이그나 마을」 북쪽 화산 기슭: 흑염의 요새 (출입 금지) / 동쪽: 용암 동굴"),
            new Readable(IGNA, 16, 11, 0, -1, "[지헌] …서로의 짐을 나눈다. 왠지 우리 얘기 같네요. 아, 아니에요!",
                    "벽화: 불꽃 앞에서 두 사람이 손을 맞잡고 있다. 한 사람의 등에는 다섯 점 문장의 가방.",
                    "글귀: '서로의 짐을 나누기로 불꽃 앞에 맹세한 자, 짐꾼에서 용사로 깨어나리라.'"),
            new Readable(IGNA, 22, 11, 0, -1, null,
                    "벽화: 용사가 된 짐꾼이 수많은 마물 사이를 빛처럼 가른다.",
                    "그 뒤에서 한 여인이 불꽃으로 길을 밝힌다."),
            new Readable(IGNA, 8, 4, 0, -1, "[지헌] 아버지 모루예요. 어릴 때 여기 올라가서 혼나곤 했어요.",
                    "대장간 모루다. 망치 자국이 수천 개 패어 있다."),
            new Readable(FORT2, 12, 8, FLAG_ORDER, -1, "[지헌] 이 사람… 당신을 일부러 이 섬으로 보낸 거예요.",
                    "탁자 위에 봉인된 편지가 놓여 있다.",
                    "'바르가스 장군께. 짐꾼 일행을 이그나행 배에 태웠소. 바다 위에서 동료를 떼어 내고, 짐꾼은 비워서 살려 두시오. 그릇이 깨지면 안 되니. — M'",
                    "[신창] M… 모르칸. 에르덴의 대신이 처음부터……"),
            new Readable(FORT3, 5, 2, FLAG_FIRESEAL, -1, null,
                    "제단에 오래된 글귀가 있다.",
                    "'불의 봉인은 나누는 손으로만 지켜진다. 쌓는 손이 닿으면 불은 검게 타오르리라. — 카이른'",
                    "(그 아래 누군가 새로 새긴 글씨) '그릇을 비워라. 잃게 하라. 빈 그릇에 왕이 깃든다. — 문지기'"),
            // ---- 4장
            new Readable(LAKE, 18, 4, 0, -1, null, "호숫가에 하얀 꽃이 피어 있다. 민한이 손에 쥐고 있던 꽃과 같다."),
            new Readable(ROA, 9, 2, FLAG_SUNLORE, -1, null,
                    "벽화: 문 저편에서 해를 수없이 모은 사람이 있다. 해는 그의 가방에 하나씩 쌓인다.",
                    "글귀: '해를 모은 자가 짐을 내려놓을 때, 그 해는 아이에게 내려가 아이를 지키리라.'",
                    "[민한] 해를 모은 사람… 아빠?"),
            new Readable(ROA, 11, 17, 0, -1, null, "「로아 마을」 북쪽: 해의 제단 / 동쪽 고원: 바람의 고원 유적 / 동남쪽: 수정 동굴"),
            new Readable(RUINS1, 2, 2, 0, -1, null,
                    "벽화: 바람의 신전. 짐꾼 카이른이 이곳에서 잠시 짐을 내려놓고 쉬었다고 적혀 있다."),
            new Readable(RUINS1, 21, 2, 0, -1, null,
                    "벽화: 돌이 된 사람들 곁에 짐승들이 모여 있다. 짐승들이 흘린 달빛 같은 눈물이 돌을 적신다."),
            new Readable(RUINS3, 2, 6, 0, -1, null,
                    "벽화: 바람의 신전 가장 깊은 곳. 돌이 된 짐꾼 곁에서 아이가 울고 있고, 그 눈물이 해처럼 빛난다."),
            new Readable(MOON2, 11, 1, 0, -1, null, "달빛 샘이다. 물속에서 은빛 알갱이들이 반짝인다."),
            new Readable(DEMON2, 5, 2, FLAG_PACT, -1, null,
                    "제단 위에 피로 쓴 계약서가 놓여 있다.",
                    "'문지기 아르센과 요마왕 다르칸의 맹약. 짐꾼이 다시 가득 차거든, 가장 소중한 것을 빼앗아 비울 것. 대가로 실바 대륙을 준다.'",
                    "'짐꾼의 아이는 반드시 데려올 것. 그 아이는 해를 물려받았다. — 아르센'",
                    "[신창] 문지기는 민한이까지… 처음부터 노리고 있었던 거야."),
    };

    /** 단서가 아닌 물건을 살폈을 때 */
    public static String examineDefault(char t) {
        switch (t) {
            case 'k': return "책이 빼곡하게 꽂혀 있다.";
            case 'b': return "푹신해 보이는 침대다. 쉬려면 여관 주인에게 말을 걸자.";
            case 'o': return "통 안에서 곡식 냄새가 난다.";
            case 'x': return "나무 상자다. 단단히 못이 박혀 있다.";
            case 't': return "탁자 위에 식은 차가 놓여 있다.";
            case 'Q': return "깊은 우물이다.";
            case 'N': return "맑은 물이 솟는다. 바닥에 동전 몇 개가 가라앉아 있다.";
            case 'P': return "반들반들하게 닳은 의자다.";
            case 'A': return "제단 위에 촛불이 조용히 타고 있다.";
            case 'g': return "잘 자란 밭이다. 늑대 때문에 수확을 미루고 있는 듯하다.";
            case 'F': return "나무 울타리다.";
            case 'L': return "등불이 은은하게 밝혀져 있다.";
            case 'i': return "벽의 횃불이 일렁인다. 누군가 최근에 불을 붙인 듯하다.";
            case 'Z': return "에르덴 왕가의 옥좌다.";
            case 'j': return "덤불이 바스락거린다.";
            case 'M': return "오래된 벽화다.";
            case 'Y': return "표지판이다.";
            case 'l': return "부글부글 끓는 용암이다. 가까이 가면 눈썹이 탈 것 같다.";
            default: return null;
        }
    }

    // ------------------------------------------------------------ 이벤트 대사

    public static final String[] PROLOGUE_MACHINE = {
            "[신창] 이게 그 기계인가…? 버튼이 하나뿐이네.",
            "(손을 뻗자, 배달 가방의 문양이 희미하게 빛났다)",
            "(버튼을 누르자 눈부신 빛이 방을 가득 채운다!)",
            "(빛 너머에서 누군가의 목소리가 들린 것 같다. '이제야… 때가 됐구나.')",
            "……",
            "(정신을 차려 보니 낯선 들판이다. 바람에서 풀 냄새가 난다.)",
            "[신창] 여긴… 어디지? 지갑도 휴대폰도 없고, 배달 가방만 남았잖아.",
            "(바로 옆에 작은 마을이 보인다. 들어가 보자.)",
    };

    public static final String[] ELDER_GIVES_LETTER = {
            "[리벨 장로] 오오… 하늘의 빛과 함께 떨어진 자여. 그 가방, 잠시 보여 주겠나?",
            "[리벨 장로] …틀림없군. 원 안의 다섯 점. 300년 전 '첫 번째 짐꾼' 카이른의 문장일세.",
            "[리벨 장로] 이 땅에는 예언이 있지. '세상이 다시 어둠에 잠길 때, 다섯 빛을 나르는 자가 문 저편에서 오리라.'",
            "[신창] 문 저편… 제가 온 곳 말인가요?",
            "[리벨 장로] 한 달 전부터 북동쪽 동굴에서 그림자 늑대가 쏟아져 나와 에르덴 성으로 가는 길이 끊겼다네.",
            "[리벨 장로] 그 동굴 깊은 곳엔 카이른이 남긴 봉인석 하나가 잠들어 있지. 우연이라 보기 어렵네.",
            "[리벨 장로] 이 편지를 에르덴 왕께 전해 주게. 자네가 예언의 짐꾼일지도 모른다고 적어 두었네.",
            "(「장로의 편지」를 받았다! 50골드를 받았다!)",
            "[신창] 결국 여기서도 배달이네…",
            "[리벨 장로] 허허. 성당 벽화를 보고 가게. 예언이 그려져 있지. 다리를 건너 북쪽으로 가면 성이네.",
    };

    public static final String[] HUNTER_GIVES_BELL = {
            "[사냥꾼 브람] …늑대왕이 마지막에 은빛으로 돌아왔다고? 그래, 역시 루프스였군.",
            "[사냥꾼 브람] 어둠에 먹히고도 콩이를 지켰다니. 녀석다워.",
            "[사냥꾼 브람] 이건 루프스가 새끼 때 떨어뜨린 방울이야. 내가 평생 간직했지. 이제 자네가 가져가게.",
            "(「루프스의 방울」을 받았다! 마나 사탕 2개를 받았다!)",
            "[사냥꾼 브람] 녀석을 편히 보내 줘서 고맙네, 짐꾼.",
    };

    public static final String[] KING_BEFORE_LETTER = {
            "[에르덴 왕] 낯선 이로군. 리벨 마을의 장로와는 아는 사이인가? 성 밖은 위험하니 조심하게.",
    };

    public static final String[] KING_LINA_JOINS = {
            "[에르덴 왕] 리벨 장로의 편지라… '카이른의 문장을 지닌 자가 문 저편에서 왔다'고?",
            "[에르덴 왕] (신창의 가방을 보고) …옥좌에 새겨진 문양과 같군.",
            "[에르덴 왕] 한 달 전, 그림자 동굴의 봉인석을 살피러 간 기사단장 로웰이 돌아오지 않았네.",
            "[에르덴 왕] 그 뒤로 늑대들이 길을 끊었지. 봉인에 무슨 일이 생긴 게 틀림없네.",
            "[에르덴 왕] 그대가 정말 짐꾼이라면, 동굴 가장 깊은 곳의 봉인석이 무사한지 확인해 주게.",
            "[리나] 폐하! 제가 함께 가겠습니다. 아버지를… 로웰 단장을 찾아야 해요!",
            "[에르덴 왕] …리나. 좋다. 견습 기사 리나에게 짐꾼의 호위를 명한다.",
            "[리나] 신창 씨, 잘 부탁해요. 아버지는 꼭 살아 계실 거예요.",
            "(리나가 동료가 되었다!)",
    };

    public static final String[] KING_WAITING = {
            "[에르덴 왕] 그림자 동굴은 북동쪽 산속. 봉인석을 부탁하네. 그리고… 로웰의 소식도.",
    };

    /** 1장 클리어 (로웰의 일기를 찾았는지에 따라 가운데 대사가 달라진다) */
    public static final String[] KING_CLEAR_OPEN = {
            "[에르덴 왕] 오오! 그림자 늑대왕을 쓰러뜨렸단 말인가!",
            "[신창] 동굴 깊은 곳에서 이 돌 조각이 나왔습니다.",
            "[에르덴 왕] (조각을 살피며) …카이른의 봉인석이로군. 누군가 일부러 깨뜨린 흔적이 있어.",
            "[에르덴 왕] 늑대들은 봉인이 깨진 틈으로 새어 나온 어둠에 물든 것일세.",
            "[리나] 폐하… 늑대왕은 수호 늑대 루프스였어요. 마지막엔 은빛으로 돌아와서, 마을 아이의 강아지를 지키고 있었어요.",
            "[에르덴 왕] 루프스가…! 로웰과 함께 봉인을 지키던 그 늑대가. 봉인을 깬 자는 수호자마저 어둠에 빠뜨린 것이군.",
    };

    public static final String[] KING_CLEAR_DIARY = {
            "[리나] 폐하, 동굴에서 아버지의 방패와 일기를 찾았어요. 아버지는 '문지기'라는 회색 망토를 쫓아 바다로 가셨대요.",
            "[에르덴 왕] 로웰이 살아 있다니…! 문지기… 그자가 봉인을 깨뜨린 장본인이로군.",
            "[에르덴 왕] '가장 많이 가진 자를 그릇으로 삼는다'라… (왕이 신창을 오래 바라본다)",
    };

    public static final String[] KING_CLEAR_NO_DIARY = {
            "[리나] 아버지의 흔적은… 찾지 못했어요.",
            "[에르덴 왕] 동굴 2층 어딘가에 로웰이 무언가를 남겼을지도 모르네. 다시 살펴보게.",
    };

    public static final String[] KING_CLEAR_CLOSE = {
            "[에르덴 왕] 카이른의 봉인석은 다섯. 그중 바다의 봉인석은 항구 도시 메르카에 있다고 전해지네.",
            "[에르덴 왕] 그대를 예언의 짐꾼, 용자로 인정하네. 「용자의 증표」와 1000골드를 받게.",
            "(1000골드를 받았다! 「용자의 증표」를 받았다!)",
            "[대신 모르칸] 폐하, 메르카로 가는 배는 제가 주선하겠습니다. (…짐꾼이 직접 바다로 와 주신다니, 일이 쉬워지는군)",
            "[리나] 저도 끝까지 함께할게요. 이제 견습이 아니라 정식 기사로서!",
            "— 1장 「배달부 용사」 끝 —",
            "(2장 「상인의 길」은 다음 업데이트에서 이어집니다. 메뉴 「단서」에서 지금까지 모은 단서를 볼 수 있어요.)",
    };

    public static final String[] KING_AFTER_CLEAR = {
            "[에르덴 왕] 용자여, 메르카로 가는 배가 준비됐네. 성 아랫마을 동쪽 부두의 선장에게 말을 걸게.",
    };

    public static final String[] BOSS_DEFEATED = {
            "(그림자 늑대왕의 몸에서 검은 안개가 피어오른다…)",
            "(안개가 걷히자, 그 자리에 은빛 털의 늙은 늑대가 쓰러져 있었다)",
            "[리나] 루프스…!",
            "(은빛 늑대는 신창의 가방 문장을 한참 바라보더니, 꼬리를 한 번 흔들었다)",
            "[루프스] …짐꾼… 문지기를… 막아… 줘… 그자는… 너를……",
            "(딸랑. 녹슨 방울이 마지막으로 울리고, 루프스는 편안한 얼굴로 눈을 감았다)",
            "……",
            "(루프스가 몸으로 감싸던 자리에서 작은 강아지가 낑낑거리며 기어 나왔다. 목줄에 '콩이'라고 적혀 있다)",
            "[리나] 리벨 마을 미나의 강아지예요… 어둠에 먹힌 채로도, 이 아이를 지키고 있었던 거야.",
            "(루프스가 누워 있던 돌 위에서 「깨진 봉인석」을 손에 넣었다!)",
            "[신창] '그릇'이라… 문지기는 대체 나를 뭐로 보는 거지.",
            "[리나] 콩이를 미나에게 데려다주고, 폐하께도 알려요. 루프스의 몫까지요.",
    };

    // ---- 2장 이벤트 대사

    public static final String[] SAIL_TO_MERCA_FIRST = {
            "[모르칸의 선장] 대신 모르칸 님의 명으로 메르카까지 모시겠습니다. 바다가 거칠지만 이 배만은 무사하지요.",
            "[리나] 이상하네요. 10년째 배가 못 뜬다는 바다를, 대신의 배만 무사히 다닌다니.",
            "(배는 사흘 밤낮을 달려 남쪽 바다를 건넜다. 밤마다 바다가 푸르스름하게 빛났다)",
            "[모르칸의 선장] 메르카에 도착했습니다. …대신께서 길드장 바론을 꼭 만나 보라 하셨습니다.",
            "(바닷바람에 소금 냄새와 돈 냄새가 섞여 있다)",
    };

    public static final String[] SAIL_TO_MERCA = {"(배를 타고 메르카로 건너갔다)"};
    public static final String[] SAIL_TO_ERDEN = {"(배를 타고 에르덴 성 부두로 돌아왔다)"};

    public static final String[] GUILD_FIRST = {
            "[길드장 바론] 에르덴에서 온 짐꾼이라… 모르칸 대신에게 얘기는 들었소.",
            "[신창] 길드가 사들인다는 푸른 돌 말입니다. 그건 봉인석일지도 모릅니다. 깨지면 바다가…",
            "[길드장 바론] 하! 돈이 되는 돌이 봉인석이든 무엇이든. 이 도시에선 저울 위에 올라간 것만 진짜요.",
            "[길드장 바론] 외지인이 길드 일에 끼어들 자격은 없소. 바다에 나갈 배도, 신전 가는 길도 내줄 수 없지.",
            "[길드장 바론] …그런 헛소리를 하던 요정 상인이 하나 있었지. 피피라고. 시장에서 쫓겨나 노점이나 하고 있을 거요.",
            "(바론은 책상 위의 빈 나침반 상자를 무심코 쓰다듬었다)",
    };

    public static final String[] GUILD_WAITING = {
            "[길드장 바론] 길드 일에 끼어들지 마시오. 바다는 메르카의 것이오.",
    };

    public static final String[] PIPI_QUEST = {
            "[피피] 길드장이 날 보냈다고? 흥.",
            "[피피] 그 푸른 돌, 우리 집안 대대로 내려오는 '마음의 저울'에 올렸더니 바늘이 부러질 듯 기울더라.",
            "[피피] 마음의 저울은 물건에 담긴 '마음의 무게'를 달아. 그건 보석이 아니야. 무언가를 가두고 있는 돌이야.",
            "[피피] 그래서 거래를 거부했더니 길드에서 쫓겨났지. 게다가… 해적들이 내 저울을 훔쳐 갔어! 북서쪽 물보라 동굴로 갔대.",
            "[피피] 저울을 되찾아 주면 너희랑 같이 가 줄게. 장사꾼은 빚은 꼭 갚거든.",
            "[리나] 물보라 동굴… 메르카 평원 북서쪽 산이에요.",
    };

    public static final String[] PIPI_WAITING = {
            "[피피] 내 저울은 북서쪽 물보라 동굴에 있어! 해적 두목이 가져갔대. 부탁해~",
    };

    public static final String[] PIPI_JOINS = {
            "[피피] 내 저울!! 진짜 찾아왔구나!",
            "[피피] (피피가 저울에 신창의 배달 가방을 올려 본다)",
            "[피피] …어라? 바늘이 반대로 기울어. 이런 건 처음이야. 텅 빈 것처럼 가벼운데, 동시에 엄청 무거워.",
            "[피피] 너, 대체 뭘 그렇게 많이 짊어지고 살았어? 돈? 권력? …아니면 그걸 다 쌓느라 비워 둔 무언가?",
            "[신창] …글쎄. 나도 잘 모르겠어.",
            "[피피] 좋아, 약속대로 같이 갈게! 요정 상인 피피, 오늘부터 짐꾼 일행이다!",
            "(피피가 동료가 되었다!)",
            "[피피] 아, 그리고. 해저 신전에 가려면 등대지기 노아 할아버지의 등불이 필요해. 할아버지는 날 알아. 소금꽃 마을 쪽 곶 끝이야!",
    };

    public static final String[] NOAH_GIVES_LAMP = {
            "[등대지기 노아] 그 저울… 피피로구나! 바로스가 아끼던 꼬마 요정.",
            "[피피] 노아 할아버지, 이 사람 가방을 저울에 달았더니 반대로 기울었어. 이런 사람이라면 믿어도 돼.",
            "[등대지기 노아] …그래. 바로스도 그런 사람이었지. 자기보다 남의 짐을 먼저 들던 녀석.",
            "[등대지기 노아] 이 등불을 가져가게. 곶 끝 바다에 비추면 신전으로 가는 돌계단이 떠오를 걸세.",
            "(「등대의 등불」을 받았다!)",
            "[등대지기 노아] 그리고 부탁하네. 바로스를… 데려와 주게. 살아 있든, 아니든.",
    };

    public static final String[] GALLEON_DEFEATED = {
            "[갈리온] 크윽…",
            "(갈리온은 무릎을 꿇고 조개 목걸이를 움켜쥐었다)",
            "[갈리온] …1년이야. 1년 동안 금화를 세느라 딸 얼굴을 잊고 살았어.",
            "[신창] 마을로 돌아가요. 마리가 기다리고 있어요.",
            "[갈리온] …저울은 저기 있소. 그리고 이것도 알아 두시오.",
            "[갈리온] 회색 망토는 에르덴의 높은 양반과 손을 잡았소. 금화엔 에르덴 왕가의 문장이 찍혀 있었지.",
            "(「피피의 저울」을 되찾았다!)",
            "[리나] 에르덴 왕가의 금화… 폐하일 리는 없어요. 그렇다면…",
            "[신창] 피피에게 저울을 돌려주러 가자.",
    };

    public static final String[] SERPENT_DEFEATED = {
            "(심해의 군주의 몸에서 검은 물결이 빠져나간다)",
            "(푸른 빛 속에서 한 남자의 혼이 떠올랐다. 젊은 상인의 모습이다)",
            "[바로스] …10년 만에 숨을 쉬는 것 같군. 고맙소, 짐꾼.",
            "[바로스] 나는 봉인을 지키려다 어둠에 먹힌 수호자와 하나가 되어 버렸소. 수호자가 끝까지 버틴 덕에 봉인석은 금만 갔지.",
            "[피피] 아저씨…!",
            "[바로스] 피피, 저울을 지켜 줬구나. 착하다.",
            "[바로스] 바론에게 전해 주시오. 나는 원망하지 않았다고. …돈을 세던 그 손으로, 이젠 사람을 안아 주라고.",
            "(바로스의 혼은 웃으며 푸른 빛 속으로 사라졌다)",
            "(바다뱀은 은은한 푸른빛으로 돌아와, 금 간 봉인석을 조용히 감싸고 다시 잠들었다)",
            "(봉인석에서 떨어진 조각을 손에 넣었다. 「바다의 봉인석 조각」)",
            "[리나] 이번엔 완전히 깨지기 전에 지켰어요. 문지기 뜻대로는 안 돼요.",
            "[피피] …바론 아저씨한테 가자. 꼭 전해야 할 말이 있어.",
    };

    public static final String[] GUILD_CLEAR_OPEN = {
            "[길드장 바론] …돌아왔군. 신전은, 바로스는……",
    };

    public static final String[] GUILD_CLEAR_COMPASS = {
            "[신창] 바로스 씨가 이걸 남겼습니다.",
            "(바론은 녹슨 나침반을 받아 들고, 뒷면의 글씨를 오래오래 바라보았다)",
            "[길드장 바론] '돈보다 무거운 걸 잊지 마'… 형은 10년 전에도 똑같은 말을 했지.",
            "[길드장 바론] 나는 그 말을 듣지 않고 봉인석을 캐려 했고, 형은 그걸 막으려다… 나 대신 바다에 남았어.",
            "[길드장 바론] (나침반을 쥔 손이 떨린다) …형. 형……",
            "[피피] 아저씨가 원망하지 않는대. 돈을 세던 손으로, 이젠 사람을 안아 주래.",
            "(바론은 처음으로 견습 상인 테오의 어깨를 끌어안았다)",
    };

    public static final String[] GUILD_CLEAR_NO_COMPASS = {
            "[신창] 바로스 씨는… 원망하지 않는다고 전해 달랬습니다.",
            "[길드장 바론] …형다운 말이군. (바론은 빈 나침반 상자를 꼭 쥐었다)",
            "[피피] 신전 2층 어딘가에 아저씨 가방이 있었을지도 몰라…",
    };

    public static final String[] GUILD_CLEAR_CLOSE = {
            "[길드장 바론] 고백하겠소. 봉인석 발굴 자금은 에르덴의 대신 모르칸이 댔소. 회색 망토의 금화와 함께.",
            "[길드장 바론] 모르칸은 '짐꾼이 바다로 오면 반드시 신전으로 보내라'고 했지. 당신을 미끼로 쓰려 했던 거요.",
            "[리나] 대신이…! 그럼 아버지가 실종된 것도…",
            "[길드장 바론] 기사 로웰은 한 달 전 북쪽 화산섬 이그나행 배를 탔소. 회색 망토를 쫓는다며.",
            "[길드장 바론] 길드는 오늘부로 푸른 돌 거래를 끊겠소. 그리고 이건… 형이 지키고 싶어 했던 것들에 쓰시오.",
            "(5000골드를 받았다!)",
            "[피피] 바론 아저씨가 돈을 내놓다니! 오늘 저울이 고장 났나 봐.",
            "[신창] 다섯 봉인석 중 둘에 금이 갔어. 문지기는 다음 봉인을 노릴 거야.",
            "[리나] 불의 봉인석… 그리고 아버지가 있는 이그나로 가요.",
            "— 2장 「상인의 길」 끝 —",
            "[길드장 바론] 부두에 이그나로 가는 길드의 배를 대 두겠소. 형의 나침반이 북쪽을 가리키더군.",
    };

    public static final String[] GUILD_AFTER_CLEAR = {
            "[길드장 바론] 이그나로 가는 배는 부두에 있소. 길드의 선장에게 말을 거시오.",
    };

    // ---- 3장 이벤트 대사

    public static final String[] SAIL_TO_IGNA = {
            "[길드의 선장] 바론 님의 명입니다. 이그나까지 모시겠습니다. 형님의 나침반이 북쪽을 가리킨다고 하셨지요.",
            "[리나] 아버지가 계신 섬… 이번엔 꼭 만날 거예요.",
            "[피피] 화산섬이라니, 내 날개 그을리면 신창이 책임져!",
            "(배는 북쪽으로 나흘을 달렸다. 닷새째 밤, 하늘이 검붉게 물들었다)",
            "(갑판 위로 검은 불꽃이 솟구친다. 불꽃 속에서 흑철 갑옷의 거한이 걸어 나왔다)",
            "[???] 짐꾼. 모르칸 대신이 길을 아주 잘 닦아 주었군.",
            "[신창] 모르칸…? 너는 누구냐!",
            "[흑염의 마장군] 문지기님의 검, 흑염의 마장군 바르가스. 그릇을 모시러 왔다. 곁가지는 치우고.",
            "[리나] 신창 씨, 뒤로! 기사의 이름으로, 한 발짝도 못 지나가!",
            "[피피] 저울이 부러질 것 같아… 저 녀석, 무게가 없어. 텅 비었어!",
    };

    public static final String[] AMBUSH_AFTER = {
            "……",
            "(검은 불꽃이 배를 삼켰다. 쓰러진 신창의 눈앞에서, 바르가스가 리나와 피피를 움켜쥔다)",
            "[바르가스] 짐꾼은 아직 덜 찼다. 더 잃고, 더 비어야 그릇이 된다.",
            "[리나] 신창 씨……! 살아요! 반드시……!",
            "[피피] 신창! 이거… 가져가……! (피피가 던진 동전 하나가 신창의 손에 떨어졌다)",
            "(공허의 균열이 열리고, 두 사람은 바르가스와 함께 어둠 속으로 사라졌다)",
            "(배는 두 동강이 났고, 신창은 검은 바다로 떨어졌다……)",
            "……",
            "(모든 것을 잃었다. 골드도, 도구도, 장비도. 그리고 동료도.)",
    };

    public static final String[] WAKE_UP = {
            "(타닥, 타닥. 장작 타는 소리와 쇠 냄새에 눈을 떴다)",
            "(낯선 천장. 몸은 붕대투성이다. 손안에는 피피의 동전 하나만 남아 있다)",
            "[신창] 리나… 피피……",
            "(대답은 없다. 옆에서 누군가 꾸벅꾸벅 졸고 있다)",
    };

    public static final String[] JIHEON_JOINS = {
            "[???] 아, 깼어요? 사흘이나 못 일어나서 걱정했어요.",
            "[지헌] 저는 지헌이에요. 이그나 대장장이 도건의 딸. 검은 모래 해변에 쓰러져 있는 걸 제가 업어 왔어요.",
            "[신창] 같이 있던 사람들은… 기사 한 명이랑, 작은 요정은요?",
            "[지헌] ……해변엔 당신뿐이었어요. 부서진 배 조각이랑, 이 머리끈 하나.",
            "(지헌이 내민 것은 리나의 붉은 머리끈이었다)",
            "[신창] (말없이 머리끈을 쥐었다. 주먹이 떨린다)",
            "[지헌] …검은 불꽃의 갑옷 사내였죠? 한 달 전 화산에서 요새가 솟던 날, 그자가 마을에 왔어요.",
            "[지헌] 아버지가 그자의 주문을 거절하셨어요. 그날 밤 대장간이 불탔고, 아버지는… 돌아오지 못하셨어요.",
            "[지헌] 당신 눈을 보니 알겠어요. 혼자 그자에게 가려는 거죠? 그 몸으로, 맨손으로.",
            "[신창] ……",
            "[지헌] 그럼 저도 갈게요. 혼자 가면 죽어요. 둘이 가면… 조금 덜 죽겠죠.",
            "[지헌] 먼저 동쪽 용암 동굴부터요. 아버지가 마지막으로 캐러 가신 '불꽃 원석'과 아버지 망치가 거기 있을 거예요.",
            "[지헌] 그 원석이면 검은 불꽃도 벨 수 있다고, 아버지가 그러셨어요.",
            "(지헌이 동료가 되었다!)",
            "[지헌] …그리고 그 차림으로 나갈 건 아니죠? 이거 받아요. 제 비상금이에요. (300골드를 받았다)",
            "[지헌] 나머지는 몬스터를 잡아서 벌어요. 이 섬에선 다들 그렇게 살아요.",
    };

    public static final String[] GIANT_DEFEATED = {
            "(거인의 가슴에서 검은 불꽃이 빠져나가고, 붉은 원석의 빛이 돌아온다)",
            "[화염 거인] …지…헌… 아가씨……",
            "[지헌] 기억하는구나… 아버지가 너를 만들 때 내가 이름 지어 줬잖아. '불똥이'.",
            "[화염 거인] 도건… 님이… 마지막으로… 원석을… 아가씨… 혼례에… 쓰라고……",
            "(거인은 가슴에서 빛나는 원석을 꺼내 지헌의 손에 쥐여 주고, 조용히 바위로 돌아갔다)",
            "(「불꽃 원석」을 손에 넣었다! 「도건의 망치」를 손에 넣었다!)",
            "[지헌] 혼, 혼례…? 아, 아니에요! 그런 뜻 아니에요! 가요, 빨리!",
            "[신창] (지헌 씨 귀가 새빨개졌다)",
            "[지헌] 마을로 돌아가요. 촌장님께 화덕을 빌려야 해요.",
    };

    public static final String[] WEDDING = {
            "[이그나 촌장] 불꽃 원석을 찾아왔다고? 도건의 망치까지!",
            "[지헌] 촌장님, 마을 화덕을 하룻밤만 빌려 주세요. 아버지가 못다 한 일을 제가 할게요.",
            "(그날 밤, 이그나의 화덕은 밤새 꺼지지 않았다. 도건의 망치 소리가 새벽까지 이어졌다)",
            "(새벽. 땀범벅이 된 지헌이 검 한 자루와 작은 상자 하나를 들고 나왔다)",
            "[지헌] 검은… 당신 거예요. 그리고 이건……",
            "(상자 안에는 붉게 빛나는 반지 두 개가 들어 있었다)",
            "[지헌] 아버지가 원석으로 만들고 싶어 하셨던 거래요. 제 혼인 반지. …엄마가 그러더라고요.",
            "[지헌] 그래서, 그게… 하나는 제 거고, 하나는……",
            "[신창] (고개를 끄덕이며 반지 하나를 집었다)",
            "[신창] 나는 평생 남의 짐만 날랐어요. 돈도, 표도, 믿음도 끝없이 쌓기만 했고요.",
            "[신창] 그런데 다 잃고 나서야 알았어요. 내가 정말 지고 싶은 건 그런 게 아니었다는 걸.",
            "[신창] 지헌 씨. 당신의 짐을 나도 같이 지게 해 줄래요? 평생.",
            "[지헌] ……바보. 그건 제가 먼저 하려던 말이에요.",
            "(그날 오후, 불의 사당에서 온 마을 사람이 지켜보는 가운데 두 사람은 혼인의 맹세를 했다)",
            "[불의 사당 무녀] 불꽃 앞에서 서로의 짐을 나누기로 맹세한 두 사람에게, 불의 축복을!",
            "(맹세의 반지가 타오르듯 빛난다. 그 순간, 신창의 배달 가방 문장이 하얗게 불타올랐다!)",
            "(가방 깊은 곳에서 오래된 목소리가 울린다. 첫 배달 날 가방을 건네던 할머니의 목소리다)",
            "[???] '이제야 됐구나. 가진 것을 쌓기만 하는 자는 그릇이 되고, 나누는 자는 용사가 된단다.'",
            "(빛이 신창의 몸을 감싼다. 잠들어 있던 힘이 하나씩 깨어난다)",
            "✨ 신창은 「배달부」에서 「용사」로 전직했다! ✨",
            "✨ 새 능력치가 깨어났다: 회심의 일격 30% · 슬레이어(주는 피해 1.5배) · HP·MP·공격·방어·민첩 대폭 상승 ✨",
            "✨ 용사의 기술: 섬광 베기 · 용사의 일격 · 기가 슬래시 · 천벌의 뇌격 · 용사의 연격 · 생명의 빛 · 부활의 기적 · 용기의 함성 ✨",
            "✨ 지헌에게 불의 축복이 깃들었다: 용광로 · 사랑의 기도 · 불사조의 날개 ✨",
            "(「용사의 검」「불사조 갑옷」「불꽃 드레스」, 「맹세의 반지」 2개를 받아 장비했다!)",
            "[지헌] 반지가… 따뜻해요. 싸우는 동안에도 마력이 끝없이 차오를 것 같아요.",
            "[이그나 촌장] 요새로 가는 길을 막던 검은 불길이 걷혔소! 북쪽 화산 기슭으로 가시오, 용사여!",
            "[신창] 리나, 피피. 조금만 기다려. 그리고 도건 어르신의 몫까지… 이번엔 내가 되찾으러 간다.",
    };

    public static final String[] GEN1_DEFEATED = {
            "[이그론] 이, 이 힘은…! 바르가스 님이 말씀하신 '빈 그릇'이 아니다… 이건……",
            "(이그론은 재가 되어 흩어졌다. 허리춤에서 감옥 열쇠가 떨어졌다)",
            "(쇠창살 너머에서 끌려온 이그나 사람들의 환호성이 들린다!)",
            "[지헌] 다들 무사해요! 아래층으로 가요. 아직 끝나지 않았어요.",
    };

    public static final String[] GEN2_DEFEATED = {
            "[모르가] 말도 안 돼… 내 독안개가 반지의 불꽃에 타 버리다니……",
            "(마녀는 연기처럼 흩어졌다)",
            "[지헌] 같이 살기로 정했다는 말, 진심이었죠?",
            "[신창] 응. 그러니까 여기서 안 죽어.",
    };

    public static final String[] GEN3_DEFEATED = {
            "(용암룡은 비명을 지르며 용암 속으로 가라앉았다. 옥좌로 가는 문이 열렸다)",
            "[지헌] 저 위에… 그자가 있어요.",
    };

    public static final String[] VARGAS_DEFEATED = {
            "(바르가스의 흑철 갑옷에 금이 가고, 검은 불꽃이 하늘로 흩어진다)",
            "[바르가스] 이럴 수가… 빈 그릇이… 용사가 되다니… 문지기님의 계획이……",
            "[신창] 리나와 피피는 어디 있지.",
            "[바르가스] …크크. 살아는 있다. 공허의 성 깊은 곳에… 문지기님의 제물로……",
            "[바르가스] 기사 로웰도 이미 문지기님 손에 있다… 그리고 너를 이 섬으로 보낸 건… 모르칸… 네 편인 척하는 그 대신이지……",
            "(바르가스는 검은 재가 되어 무너졌다. 옥좌 뒤에서 붉은 빛이 새어 나온다)",
            "(옥좌 뒤에 숨겨져 있던 불의 봉인석. 금이 갔지만 아직 깨지지 않았다)",
            "(「불의 봉인석 조각」을 손에 넣었다!)",
            "[지헌] …아버지. 끝났어요. 원수를… 갚았어요.",
            "(지헌은 도건의 망치를 가슴에 안고 오래 울었다. 신창은 말없이 그 어깨를 감쌌다)",
            "[신창] 살아 있대. 리나도, 피피도. …다행이다. 정말 다행이다.",
            "[지헌] 그럼 데리러 가요. 하지만 그 전에… 집에 가요. 우리 집에.",
    };

    public static final String[] HOME = {
            "[이그나 촌장] 돌아왔구려! 끌려갔던 마을 사람들이 모두 내려왔소! 용사 부부 만세!",
            "(마을 어귀에서 해련이 지헌을 끌어안고 엉엉 울었다)",
            "[지헌] 다녀왔어요, 가 아니라… '다녀왔어'라고 해 줘요. 이제 여기가 당신 집이니까.",
            "[신창] …다녀왔어.",
            "(마을 사람들이 모여 불탄 대장간을 다시 세웠다. 신창은 생전 처음 '배달'이 아닌 '집'을 지었다)",
            "(지붕을 올리던 날, 지헌이 신창의 귀에 무언가를 속삭였다)",
            "[신창] …정말? 정말로?",
            "[지헌] 응. 봄이 오면, 셋이 돼요.",
            "(신창은 그 자리에 주저앉아 한참을 웃었다. 그리고 조금 울었다)",
            "[이그나 촌장] 하하! 경사로다! 해련 할멈에게 가 보시오. 산파 노릇은 그 할멈이 이 섬에서 제일이오.",
    };

    public static final String[] BIRTH = {
            "[해련] 사위! 오늘이오! 지헌이 진통이 시작됐소! 밖에서 기다리시오!",
            "(긴 밤이었다. 화산도, 바다도, 마을도 숨을 죽였다)",
            "(신창은 대장간 앞을 밤새 서성였다. 어떤 보스 앞에서도 떨리지 않던 손이 떨렸다)",
            "(새벽녘, 첫 햇살과 함께 우렁찬 울음소리가 이그나에 울려 퍼졌다)",
            "[해련] 아들이오! 손이 아주 큼직한 사내아이요!",
            "(지헌의 품에 안긴 작은 아기가 신창의 손가락을 꼭 쥐었다)",
            "[지헌] 이름… 정했어요?",
            "[신창] 민한. 우리 아들, 민한.",
            "[지헌] 민한아, 아빠야. 세상에서 제일 무거운 짐을 지던 사람이었는데… 이제는 너를 안고 있어.",
            "(아기 민한이 방긋 웃었다. 배달 가방의 문장이 그 웃음에 맞춰 따스하게 빛났다)",
            "[신창] (리나, 피피, 그리고 아직 만나지 못한 로웰 단장님. 꼭 데리러 갈게.)",
            "[신창] (이번엔 지켜야 할 집이 있으니까.)",
            "(마을 사람들이 축하금을 모아 주었다. 5000골드를 받았다!)",
            "— 3장 「용사의 탄생」 끝 —",
            "(4장은 다음 업데이트에서 이어집니다. 메뉴 「단서」에서 3장의 단서도 확인해 보세요.)",
    };

    // ---- 4장 이벤트 대사

    public static final String[] FAMILY = {
            "(민한이 태어난 날 밤, 이그나 마을은 밤새 잔치를 벌였다)",
            "[이그나 촌장] 용사의 아들이 태어났다! 오늘은 화덕도 쉬지 않는다!",
            "[해련] 사위, 아기 좀 안아 봐요. 아이고, 손 떨리는 것 좀 봐.",
            "[지헌] 민한아, 여기가 네 집이야. 엄마, 아빠, 할머니, 그리고 이 섬 사람들 모두가 네 가족이야.",
            "(신창은 아기 민한을 안고 해가 뜰 때까지 마을을 걸었다. 배달 가방을 멘 채로)",
            "……",
            "— 그리고 몇 해가 흘렀다 —",
            "(민한은 무엇이든 궁금해하는 아이로 자랐다. 화덕 불꽃은 왜 파란지, 바다 끝엔 뭐가 있는지)",
            "(대장간 바닥에 엎드려 개미를 몇 시간씩 들여다보다가, 아빠의 배달 가방을 끌고 다니며 '배달 놀이'를 하곤 했다)",
            "[지헌] 민한이 또 어디 갔지? …또 해변이네. 저 애는 아빠를 닮아서 가만히 있질 않아요.",
            "[신창] 하하, 나는 어릴 때 저렇게 궁금한 게 많지 않았는데. 엄마 닮은 거야.",
            "(요즘 마을에 처음 보는 약초꾼 할머니 하나가 머물고 있다. 아이들에게 사탕을 나눠 주는 인자한 할머니다)",
    };

    public static final String[] KID_LURED = {
            "[어린 민한] 아빠! 아빠! 그 할머니가 그러는데, 호숫가에 밤에만 빛나는 나비가 있대!",
            "[신창] 안개 호수? 거긴 멀어. 혼자 가면 안 돼, 알았지?",
            "[어린 민한] 칫… 알았어. 그럼 아빠 가방 메고 배달 놀이 하러 갈래!",
            "(그날 밤, 신창은 민한의 머리를 쓰다듬으며 재웠다. 민한은 꿈속에서도 나비 이야기를 했다)",
            "……",
            "(다음 날 새벽. 민한의 침대가 비어 있었다. 배달 가방도 사라졌다)",
            "[지헌] 민한아? 민한아!!",
            "[걱정 많은 아낙] 용사님! 새벽에 민한이가 그 약초꾼 할머니 손을 잡고 남동쪽 꽃길로 가는 걸 봤어요!",
            "[신창] 안개 호수……! 지헌, 가자!",
    };

    public static final String[] FOX1_DEFEATED = {
            "(천면귀의 가면이 하나둘 깨져 나간다. 꼬리가 하나, 둘 꺾인다)",
            "[신창] 끝이다. 민한이를 내놔.",
            "[천면귀] 호… 호호… 정말 세구나, 용사님. 그런데 말이야……",
            "(천면귀가 순식간에 잠든 민한을 낚아채 목에 손톱을 들이댔다!)",
            "[천면귀] 한 발짝만 더 와 봐. 이 작은 목이 어떻게 되는지.",
            "[지헌] 안 돼……!! 민한아!!",
            "[천면귀] 검 내려놔. 마법도 멈추고. 그리고… 맞아. 그냥 맞기만 해. 호호호!",
    };

    public static final String[] PETRIFIED = {
            "(신창과 지헌은 끝내 무릎을 꿇었다. 단 한 번도 반격하지 않았다)",
            "[천면귀] 호호, 이게 용사? 아들 하나에 이렇게 무너지다니. 역시 가득 찬 그릇은 깨기도 쉽구나.",
            "[천면귀] 석화의 저주. 돌이 되어 세상 끝으로 흩어져라!",
            "(신창의 발끝부터 회색 돌이 번져 올라간다)",
            "[신창] 민한아… 아빠가… 미안……",
            "(돌이 되어 가는 지헌이 마지막 힘을 짜내 손을 뻗었다. 손끝에서 붉은 불꽃 나비가 날아올랐다)",
            "[지헌] (불꽃아… 내 아이를… 멀리, 안전한 곳으로……)",
            "(불꽃 나비가 민한과 배달 가방을 감싸더니, 빛이 되어 하늘 저편으로 사라졌다)",
            "[천면귀] 뭐야?! 아이가… 어디로!",
            "(두 개의 석상은 검은 바람에 휩쓸려 세상 어딘가로 흩어졌다)",
            "……",
            "(먼 대륙, 깊은 숲. 하늘에서 떨어진 작은 불꽃이 풀숲에 내려앉았다)",
            "(울고 있는 아이를 처음 발견한 것은 늑대 새끼 하루였다. 곰 어미가 아이를 품었고, 부엉이가 이름을 물었다)",
            "[어린 민한] …민한. 내 이름은… 민한……",
            "(짐승들은 사람의 아이를 정성껏 길렀다. 아이는 사냥을 배우고, 숲의 말을 배우고, 별을 보며 엄마 아빠를 그리워했다)",
            "— 8년 후 —",
            "(민한은 열네 살이 되었다. 곁에는 늘 늑대 하루가 있다. 어깨에는 그날의 낡은 배달 가방이 메여 있다)",
            "[민한] 하루. 나 이제 갈 거야. 엄마 아빠를 찾으러.",
            "[하루] 아우우— (같이 간다는 뜻이다)",
            "(민한과 하루가 일행이 되었다! 지금부터 민한의 모험이 시작된다)",
    };

    public static final String[] LEGACY = {
            "[해의 제단지기] 숲에서 온 소년이로군. 그 가방… 잠깐 제단에 올려 보겠나?",
            "(민한이 배달 가방을 제단에 올리자, 원 안의 다섯 점 문장이 눈부시게 빛났다!)",
            "(가방 깊은 곳에서 작은 해들이 하나둘 떠올라 제단 위를 맴돈다)",
            "[해의 제단지기] 이럴 수가… 해다. 진짜 해의 조각이야. 이렇게나 많이!",
            "[해의 제단지기] 이 해는 문 저편에서 한 사람이 평생을 바쳐 쌓아 올린 것이다. 모든 걸 내려놓고 다시 시작할 때마다 하나씩 얻는 것이지.",
            "[민한] 문 저편… 엄마가 말해 줬어. 아빠는 문 저편에서 왔다고.",
            "[해의 제단지기] 그렇다면 이건 네 아버지의 유산이다. 아버지가 너에게 남긴 것이야.",
            "[해의 제단지기] 해의 제단은 해를 받고 전설의 무구를 내어 준다. 해 1000개를 바치면 태초의 해로 벼린 검까지도.",
            "(해의 빛이 민한과 하루에게 스며든다. 몸 깊은 곳에서 아버지를 닮은 힘이 깨어난다)",
            "✨ 민한이 「해의 계승자」로 깨어났다! 회심의 일격 30% · 슬레이어 · 능력치 대폭 상승 ✨",
            "✨ 해의 기술: 햇살 베기 · 태양의 일격 · 해돋이 · 가족의 빛 ✨",
            "✨ 하루에게도 해의 축복이 깃들었다! ✨",
            "[해의 제단지기] 제단에 말을 걸면 언제든 해로 무구를 바꿔 주겠네. 아버지의 해를 아끼지 말게. 그러라고 남긴 거니까.",
    };

    public static final String[] DAD_FOUND = {
            "(석상 수호자가 무너지자, 석상을 덮고 있던 이끼가 바람에 날려 갔다)",
            "(드러난 얼굴은… 민한이 8년 동안 별을 보며 떠올리던 얼굴이었다)",
            "[민한] 아…빠……?",
            "(석상의 가슴께, 돌 속에서 아주 희미한 빛이 깜빡인다. 심장 소리 같다)",
            "[민한] 아빠! 아빠!! 나 민한이야! 나 이렇게 컸어! 나……",
            "(석상은 대답이 없다. 하지만 검을 든 돌 손이 아주 조금, 떨린 것 같았다)",
            "[하루] 끼잉… (하루가 석상의 발치에 몸을 웅크린다)",
            "[민한] …기다려, 아빠. 엄마도 찾아서, 꼭 둘 다 되돌릴게.",
    };

    public static final String[] MOM_FOUND = {
            "(수정 여왕이 산산조각 나자, 동굴 가득 울리던 울음소리가 멎었다)",
            "(수정에 둘러싸인 여인의 석상. 두 손을 앞으로 뻗은 채, 무언가를 멀리 보내는 자세다)",
            "[민한] 엄마… 그날 나를 보내던 그 손이야……",
            "(석상에서 따뜻한 불꽃이 아주 작게 피어올라, 민한의 손등에 내려앉았다)",
            "[지헌의 목소리] (민한아… 우리 아기… 많이 컸구나……)",
            "[민한] 엄마! 엄마 목소리야!",
            "[지헌의 목소리] (돌의 저주는… 짐승들이 지닌 '달빛 이슬'로만 풀 수 있어… 너를 길러 준 그 숲에……)",
            "(불꽃이 사그라들었다. 하지만 석상의 얼굴은 웃고 있는 것처럼 보였다)",
            "[민한] 숲으로 돌아가자, 하루. 부엉이 할아버지한테!",
    };

    public static final String[] MOON_QUEST = {
            "[백년 부엉이 노을] 부엉… 달빛 이슬을 찾는구나. 결국 그날이 왔어.",
            "[백년 부엉이 노을] 달빛 이슬은 짐승이 사람을 위해 흘린 눈물이란다. 네가 처음 왔던 밤 우리가 흘린 눈물이, 숲 깊은 '달빛 샘'에 고여 있지.",
            "[곰 어미 두두] 크릉. 그런데 샘을 지키던 숲의 주인이 요즘 어둠에 먹혔대. 요마성에서 검은 안개가 내려온 뒤로…",
            "[백년 부엉이 노을] 보금자리 동쪽의 달빛 숲길로 가거라. 샘의 주인을 어둠에서 꺼내 주면, 이슬을 내어 줄 게다.",
    };

    public static final String[] DEW_GIVEN = {
            "[백년 부엉이 노을] 부엉… 달빛 이슬을 찾는구나. 결국 그날이 왔어.",
            "[백년 부엉이 노을] 달빛 이슬은 짐승이 사람을 위해 흘린 눈물이란다. 백 년에 한 방울 맺히지.",
            "[곰 어미 두두] 크릉. 네가 처음 왔던 밤, 우리 모두 울었단다. 이렇게 작은 아기가 혼자서…",
            "[흰 사슴] (사슴이 고개를 숙이자 뿔 끝에서 은빛 이슬이 떨어진다)",
            "[토끼 삼남매] 형아, 우리 것도! 우리도 울었어!",
            "(짐승들이 모은 눈물이 부엉이의 깃털 위에서 두 방울의 달빛 이슬이 되었다)",
            "(「달빛 이슬」 2개를 받았다!)",
            "[백년 부엉이 노을] 네가 우리를 가족이라 불러 줘서 고맙구나, 민한아. 이제 진짜 가족을 데려오거라.",
    };

    public static final String[] MOONLORD_DEFEATED = {
            "(검은 안개가 빠져나가자, 숲의 주인은 은빛 사슴의 모습으로 돌아왔다)",
            "[숲의 주인] …고맙구나, 짐승들의 아이야. 어둠이 내 눈을 가렸었다.",
            "(샘물이 다시 맑아지고, 바닥에서 은빛 이슬들이 떠오른다)",
            "[숲의 주인] 이 이슬은 네가 이 숲에 온 밤, 모두가 흘린 눈물이다. 곰도, 사슴도, 부엉이도, 작은 토끼들도.",
            "[숲의 주인] 가져가거라. 이 눈물로 네 진짜 가족을 데려오너라.",
            "(「달빛 이슬」 2개를 받았다!)",
            "[민한] 고마워요… 다들, 고마워.",
            "[하루] 아우우— (하루가 달을 향해 길게 울었다)",
            "[민한] 엄마부터 가자, 하루. 수정 동굴 3층으로!",
    };

    public static final String[] MOM_BACK = {
            "(민한이 달빛 이슬을 석상에 부었다. 은빛이 돌을 타고 번지며, 회색 돌이 하나둘 갈라져 떨어진다)",
            "(갈라진 돌 사이로 붉은 머리카락이, 따뜻한 손이, 그리고 눈물 젖은 얼굴이 드러났다)",
            "[지헌] ……민한…아……?",
            "[민한] 엄마!!",
            "(지헌은 아들을 끌어안았다. 8년 동안 하지 못한 포옹이었다)",
            "[지헌] 이렇게… 이렇게 컸구나… 미안해, 미안해 민한아. 엄마가 지켜 주지 못해서……",
            "[민한] 엄마가 지켜 줬잖아. 그날, 불꽃 나비로. 덕분에 나 하루랑 숲에서 잘 컸어.",
            "[지헌] (하루를 보며) 네가… 우리 아이를 키워 줬구나. 고마워. 정말 고마워.",
            "[하루] 왕! (꼬리를 마구 흔든다)",
            "(지헌이 다시 일행이 되었다!)",
            "[지헌] 아빠는…? 아빠도 찾았어?",
            "[민한] 응! 고원 유적에 있어. 같이 가요, 엄마!",
    };

    public static final String[] DAD_BACK = {
            "(마지막 달빛 이슬이 석상을 적신다. 검을 든 손에서부터 돌이 녹아내린다)",
            "[신창] ……민…한……",
            "(신창이 무릎을 꿇었다. 키가 훌쩍 자란 소년이 그 앞에 서 있었다)",
            "[민한] 아빠. 나 민한이야. 열네 살이야.",
            "[신창] (떨리는 손으로 아들의 얼굴을 만진다) …8년. 8년이나… 아빠가 한 번도 못 안아 줬구나.",
            "[민한] 지금 안아 주면 되잖아.",
            "(신창은 아들을, 그리고 아내를 한꺼번에 끌어안았다. 오래도록 놓지 않았다)",
            "[민한] 아빠, 이거. 아빠 가방이야. 그 안에 해가 잔뜩 있었어. 아빠 유산이라던데?",
            "[신창] 해……? 그건… 아빠가 문 저편에서 몇 번이고 모든 걸 내려놓을 때마다 받은 거야.",
            "[신창] 그게 너를 지켰구나. 내려놓은 것들이, 결국 너한테 갔어.",
            "(신창이 다시 일행이 되었다!)",
            "[지헌] 이제 끝내러 가요. 우리 가족을 이렇게 만든 그 여우한테.",
            "(북쪽 산의 요마성을 뒤덮고 있던 검은 안개가 걷히기 시작했다!)",
    };

    public static final String[] FOX2_DEFEATED = {
            "[천면귀] 말도… 안 돼… 인질만 있었으면… 인질만……",
            "[민한] 인질 같은 거 없어도, 할머니는 처음부터 졌어. 우리 가족은 한 번도 흩어진 적 없으니까.",
            "(천면귀의 마지막 가면이 깨지자, 늙고 작은 여우 한 마리가 남았다. 여우는 안개처럼 흩어졌다)",
            "[지헌] …끝났어요. 이제 위층만 남았어요.",
    };

    public static final String[] KING4_DEFEATED = {
            "(요마왕 다르칸의 거대한 몸이 무너진다. 요마성 전체가 흔들린다)",
            "[다르칸] 해의 아이… 문지기는… 너를 두려워했다… 짐꾼보다… 너를……",
            "[다르칸] 공허의 성에서… 문지기가… 기다린다… 가득 찬 가족을… 어떻게 비울지… 보자꾸나……",
            "(다르칸은 검은 재가 되어 흩어졌다. 요마성을 덮고 있던 어둠이 걷히고, 아침 해가 비쳐 들었다)",
            "[신창] 끝났다. 민한아, 지헌아. 집에 가자.",
            "[민한] 응. 근데 아빠, 하루도 같이 가도 되지? 부엉이 할아버지한테도 인사하고!",
    };

    public static final String[] CHAPTER4_END = {
            "[백년 부엉이 노을] 부엉! 돌아왔구나. 셋이서… 아니, 넷이서.",
            "[신창] 우리 아들을 길러 주셔서 고맙습니다. 이 은혜는 평생 잊지 않겠습니다.",
            "[곰 어미 두두] 크릉… 우리 민한이 데려가는 거야? (곰 어미가 커다란 눈물을 뚝뚝 흘린다)",
            "[민한] 자주 올게, 두두 엄마. 약속.",
            "[지헌] 민한이에겐 엄마가 둘이네요. …질투 나는데요?",
            "(그날 밤 숲에서는 사람과 짐승이 함께 둘러앉아 잔치를 벌였다)",
            "(신창은 모닥불 너머로 웃고 있는 아내와 아들, 그리고 아들 곁에 엎드린 늑대를 오래 바라보았다)",
            "[신창] (리나, 피피. 로웰 단장님. 이제 우리 가족이 데리러 갈게. 문지기, 기다려라.)",
            "(요마왕이 남긴 보물을 받았다. 20000골드!)",
            "— 4장 「해를 물려받은 아이」 끝 —",
            "(5장은 다음 업데이트에서 이어집니다. 메뉴 「단서」에서 4장의 단서도 확인해 보세요.)",
    };

    /** 보스 전투 중 연출 대사: {격노할 때, 쓰러지기 직전} */
    public static String[][] bossScript(String sprite) {
        if ("wolfking".equals(sprite)) {
            return new String[][]{
                    {"[리나] 버텨요! 저 녀석도… 괴로워하고 있어요!"},
                    {"(딸랑… 어둠 속에서 방울 소리가 희미하게 울린다)", "[신창] 조금만 더…! 돌아와, 루프스!"},
            };
        }
        if ("galleon".equals(sprite)) {
            return new String[][]{
                    {"[갈리온] 크하하! 금화만 있으면 뭐든 살 수 있다! 용서도, 딸도!"},
                    {"(갈리온의 품에서 낡은 조개 목걸이가 떨어졌다. 아이가 만든 듯한 서툰 솜씨다)", "[갈리온] 마리… 의… 목걸이……"},
            };
        }
        if ("serpent".equals(sprite)) {
            return new String[][]{
                    {"[바로스의 목소리] …아파… 바론… 미안해…", "[피피] 버텨, 아저씨! 금방 꺼내 줄게!"},
                    {"(비늘 사이로 푸른 빛이 새어 나온다. 어둠이 걷히고 있다)", "[신창] 지금이야! 마지막 한 번만 더!"},
            };
        }
        if ("giant".equals(sprite)) {
            return new String[][]{
                    {"[지헌] 불똥아! 정신 차려! 나야, 지헌이야!"},
                    {"(거인의 가슴 속 원석이 붉게 깜빡인다. 검은 불꽃이 흔들린다)", "[신창] 조금만 더…! 지헌 씨, 같이!"},
            };
        }
        if ("vargas".equals(sprite)) {
            return new String[][]{
                    {"[바르가스] 크하하! 좋다, 짐꾼! 이 불꽃은 문지기님께 받은 공허의 불이다!", "[지헌] 반지가 뜨거워요… 우리 불이 더 세요!"},
                    {"(흑철 갑옷에 금이 간다. 그 틈으로 텅 빈 어둠이 보인다)", "[신창] 이걸로 끝이다. 도건 어르신, 리나, 피피의 몫이다!"},
            };
        }
        if ("fox".equals(sprite)) {
            return new String[][]{
                    {"[천면귀] 호호호! 가면이 하나 깨졌다고 끝인 줄 알았니?"},
                    {"(가면 아래로 늙은 여우의 얼굴이 언뜻 비친다)", "[지헌] 조금만 더…!"},
            };
        }
        if ("demonking".equals(sprite)) {
            return new String[][]{
                    {"[다르칸] 크하하! 가족 놀이는 여기까지다!", "[민한] 아빠, 엄마, 하루! 한 번 더!"},
                    {"(다르칸의 뿔에 금이 간다)", "[신창] 민한아, 마지막은 네가 해라!", "[민한] 응! 이건… 아빠의 해야!"},
            };
        }
        if ("forestlord".equals(sprite)) {
            return new String[][]{
                    {"(숲의 주인이 고통스럽게 울부짖는다. 검은 뿔 사이로 은빛이 새어 나온다)"},
                    {"[민한] 정신 차려요, 숲의 주인님! 조금만 더!"},
            };
        }
        if ("guardian".equals(sprite) || "crystalqueen".equals(sprite)) {
            return new String[][]{
                    {"(수호자가 석상을 감싸듯 몸을 웅크리며 분노한다!)"},
                    {"[민한] 비켜! 그 안에 우리 가족이 있어!"},
            };
        }
        if ("ignis".equals(sprite) || "witch".equals(sprite) || "lavadragon".equals(sprite)) {
            return new String[][]{
                    {"(검은 불꽃이 거세게 일렁인다!)"},
                    {"[지헌] 거의 다 됐어요!"},
            };
        }
        return new String[][]{{}, {}};
    }

    // ------------------------------------------------------------ 아이템

    public static final int CONSUMABLE = 0, WEAPON = 1, ARMOR = 2, KEY = 3, ACCESSORY = 4;

    public static final class Item {
        public final int id;
        public final String name;
        public final int type;
        public final int power;
        public final int price;
        public final String desc;

        Item(int id, String name, int type, int power, int price, String desc) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.power = power;
            this.price = price;
            this.desc = desc;
        }
    }

    public static final int HERB = 0, MANA_CANDY = 1, WING = 2, HI_HERB = 3, PHOENIX = 4, ELIXIR = 5;
    public static final int STICK = 10, COPPER_SWORD = 11, SQUIRE_SWORD = 12, IRON_SWORD = 13, KNIGHT_SPEAR = 14,
            STEEL_SWORD = 15, DRAGON_SPEAR = 16, COIN_POUCH = 17, OBSIDIAN_SWORD = 18, SMITH_HAMMER = 19;
    public static final int CLOTHES = 20, LEATHER = 21, CHAIN_MAIL = 22, SILVER_ARMOR = 23,
            STEEL_ARMOR = 24, CORAL_ARMOR = 25, FAIRY_CLOAK = 26, SEA_ARMOR = 27, VOLCANO_ARMOR = 28, SMITH_APRON = 29;
    public static final int LETTER = 30, HERO_BADGE = 31;
    public static final int PIPI_SCALE = 35, LIGHTHOUSE_LAMP = 36, BAROS_COMPASS = 37, SEA_SHARD = 38;
    public static final int HERO_SWORD = 40, PHOENIX_ARMOR = 41, BRIDE_DRESS = 42, VOW_RING = 43,
            FLAME_ORE = 44, DOGEON_HAMMER = 45, FIRE_SHARD = 46, ASH_ROBE = 47,
            WOOD_SPEAR = 48, LEAF_CLOAK = 49, WOLF_FANG = 50, WOLF_FUR = 51,
            SUN_SWORD = 52, SUN_ARMOR = 53, DAWN_BLADE = 54, HEAVEN_MAIL = 55, SUN_HEART = 56, SUN_MANE = 57, SUN_STAFF = 58,
            ETERNAL_SUN = 59, MOON_DEW = 60, DAD_BAG = 61, ROA_SPEAR = 62, RANGER_MAIL = 63;
    public static final int ITEM_SLOTS = 64;

    public static final Item[] ITEMS = new Item[ITEM_SLOTS];

    static {
        put(new Item(HERB, "약초", CONSUMABLE, 30, 8, "HP를 30쯤 회복"));
        put(new Item(MANA_CANDY, "마나 사탕", CONSUMABLE, 15, 30, "MP를 15 회복"));
        put(new Item(WING, "날개깃", CONSUMABLE, 0, 25, "마지막으로 들른 마을로 돌아간다"));
        put(new Item(HI_HERB, "상급 약초", CONSUMABLE, 120, 60, "HP를 120쯤 회복"));
        put(new Item(PHOENIX, "소생의 깃털", CONSUMABLE, 50, 300, "쓰러진 동료를 HP 절반으로 되살린다"));
        put(new Item(STEEL_SWORD, "강철검", WEAPON, 24, 2400, "공격 +24"));
        put(new Item(DRAGON_SPEAR, "해룡의 창", WEAPON, 32, 6200, "공격 +32"));
        put(new Item(COIN_POUCH, "동전 주머니", WEAPON, 14, 500, "공격 +14 (피피가 던지는 묵직한 동전)"));
        put(new Item(STEEL_ARMOR, "강철 갑옷", ARMOR, 18, 2000, "방어 +18"));
        put(new Item(CORAL_ARMOR, "산호 갑옷", ARMOR, 26, 5400, "방어 +26"));
        put(new Item(FAIRY_CLOAK, "요정 망토", ARMOR, 12, 900, "방어 +12"));
        put(new Item(SEA_ARMOR, "해신의 갑옷", ARMOR, 34, 12000, "방어 +34"));
        put(new Item(PIPI_SCALE, "피피의 저울", KEY, 0, 0, "물건에 담긴 마음의 무게를 다는 요정의 저울"));
        put(new Item(LIGHTHOUSE_LAMP, "등대의 등불", KEY, 0, 0, "곶 끝 바다에 비추면 해저 신전으로 가는 계단이 떠오른다"));
        put(new Item(BAROS_COMPASS, "바로스의 나침반", KEY, 0, 0, "'바론에게. 돈보다 무거운 걸 잊지 마.'"));
        put(new Item(SEA_SHARD, "바다의 봉인석 조각", KEY, 0, 0, "금 간 바다의 봉인석에서 떨어진 조각"));
        put(new Item(ELIXIR, "엘릭서", CONSUMABLE, 9999, 0, "HP와 MP를 모두 회복"));
        put(new Item(OBSIDIAN_SWORD, "흑요석 검", WEAPON, 40, 3200, "공격 +40"));
        put(new Item(SMITH_HAMMER, "대장장이 망치", WEAPON, 22, 600, "공격 +22"));
        put(new Item(VOLCANO_ARMOR, "화산석 갑옷", ARMOR, 32, 2800, "방어 +32"));
        put(new Item(SMITH_APRON, "대장장이 앞치마", ARMOR, 12, 400, "방어 +12"));
        put(new Item(ASH_ROBE, "화산재 로브", ARMOR, 26, 2200, "방어 +26"));
        put(new Item(HERO_SWORD, "용사의 검", WEAPON, 110, 0, "지헌이 불꽃 원석으로 벼린 검. 공격 +110"));
        put(new Item(PHOENIX_ARMOR, "불사조 갑옷", ARMOR, 70, 0, "방어 +70"));
        put(new Item(BRIDE_DRESS, "불꽃 드레스", ARMOR, 60, 0, "혼례 날 마을이 지어 준 옷. 방어 +60"));
        put(new Item(VOW_RING, "맹세의 반지", ACCESSORY, 30, 0, "매 턴 MP를 30% 회복, 걸을 때도 조금씩 회복"));
        put(new Item(FLAME_ORE, "불꽃 원석", KEY, 0, 0, "도건이 마지막으로 캐려던 원석. 손에 쥐면 따뜻하다"));
        put(new Item(DOGEON_HAMMER, "도건의 망치", KEY, 0, 0, "이그나의 대장장이 도건의 망치. 손잡이가 반질반질하다"));
        put(new Item(FIRE_SHARD, "불의 봉인석 조각", KEY, 0, 0, "바르가스의 옥좌 뒤에서 찾은 불의 봉인석 조각"));
        put(new Item(WOOD_SPEAR, "나무 창", WEAPON, 16, 80, "민한이 숲에서 깎은 창. 공격 +16"));
        put(new Item(LEAF_CLOAK, "잎사귀 망토", ARMOR, 10, 60, "방어 +10"));
        put(new Item(WOLF_FANG, "늑대 이빨", WEAPON, 14, 0, "하루의 이빨. 공격 +14"));
        put(new Item(WOLF_FUR, "두꺼운 털", ARMOR, 12, 0, "하루의 털. 방어 +12"));
        put(new Item(ROA_SPEAR, "로아의 강철창", WEAPON, 60, 3600, "공격 +60"));
        put(new Item(RANGER_MAIL, "숲지기 갑옷", ARMOR, 45, 3200, "방어 +45"));
        put(new Item(SUN_SWORD, "태양의 검", WEAPON, 90, 0, "해의 제단 무구. 공격 +90"));
        put(new Item(SUN_ARMOR, "태양의 갑옷", ARMOR, 60, 0, "해의 제단 무구. 방어 +60"));
        put(new Item(SUN_MANE, "일륜의 갈기", ARMOR, 90, 0, "짐승을 위한 해의 갑옷. 방어 +90"));
        put(new Item(DAWN_BLADE, "전설의 검 「일광검」", WEAPON, 170, 0, "새벽빛을 벼린 전설의 검. 공격 +170"));
        put(new Item(HEAVEN_MAIL, "전설의 갑옷 「천광의 갑주」", ARMOR, 120, 0, "하늘빛 전설의 갑옷. 방어 +120"));
        put(new Item(SUN_HEART, "전설의 목걸이 「해의 심장」", ACCESSORY, 40, 0, "매 턴 MP를 40% 회복, 걸을 때도 회복"));
        put(new Item(SUN_STAFF, "전설의 지팡이 「일륜」", WEAPON, 130, 0, "불꽃 무녀를 위한 전설의 지팡이. 공격 +130"));
        put(new Item(ETERNAL_SUN, "초초 전설의 검 「태초의 해 · 영겁」", WEAPON, 480, 0, "태초의 해로 벼린 검. 공격 +480, 보통 공격이 적 전체를 벤다"));
        put(new Item(MOON_DEW, "달빛 이슬", KEY, 0, 0, "짐승이 사람을 위해 흘린 눈물. 돌의 저주를 푼다"));
        put(new Item(DAD_BAG, "아빠의 배달 가방", KEY, 0, 0, "원 안에 다섯 점 문장. 안에 해가 잠들어 있다"));
        put(new Item(STICK, "나무 막대", WEAPON, 2, 10, "공격 +2"));
        put(new Item(COPPER_SWORD, "구리 검", WEAPON, 6, 100, "공격 +6"));
        put(new Item(SQUIRE_SWORD, "견습 기사의 검", WEAPON, 8, 160, "공격 +8"));
        put(new Item(IRON_SWORD, "철검", WEAPON, 12, 380, "공격 +12"));
        put(new Item(KNIGHT_SPEAR, "기사의 창", WEAPON, 18, 900, "공격 +18"));
        put(new Item(CLOTHES, "천 옷", ARMOR, 2, 10, "방어 +2"));
        put(new Item(LEATHER, "가죽 갑옷", ARMOR, 5, 90, "방어 +5"));
        put(new Item(CHAIN_MAIL, "사슬 갑옷", ARMOR, 10, 350, "방어 +10"));
        put(new Item(SILVER_ARMOR, "은빛 갑옷", ARMOR, 16, 1200, "방어 +16"));
        put(new Item(LETTER, "장로의 편지", KEY, 0, 0, "에르덴 왕께 전할 편지"));
        put(new Item(HERO_BADGE, "용자의 증표", KEY, 0, 0, "에르덴 왕이 인정한 용자의 증표"));
        put(new Item(LOWELL_DIARY, "로웰의 일기", KEY, 0, 0, "기사단장 로웰이 남긴 일기. 회색 망토 '문지기'를 쫓아 바다로 갔다"));
        put(new Item(SEAL_SHARD, "깨진 봉인석", KEY, 0, 0, "그림자의 봉인석 조각. 누군가 일부러 깨뜨렸다"));
        put(new Item(LUPUS_BELL, "루프스의 방울", KEY, 0, 0, "수호 늑대 루프스가 달고 다니던 기사단 방울. 흔들면 맑은 소리가 난다"));
    }

    private static void put(Item item) {
        ITEMS[item.id] = item;
    }

    public static final int[][] SHOPS = {
            {HERB, WING, COPPER_SWORD, LEATHER},
            {HERB, MANA_CANDY, WING, IRON_SWORD, CHAIN_MAIL, KNIGHT_SPEAR},
            {HI_HERB, MANA_CANDY, PHOENIX, WING},
            {STEEL_SWORD, DRAGON_SPEAR, STEEL_ARMOR, CORAL_ARMOR},
            {HERB, HI_HERB, MANA_CANDY, WING},
            {HI_HERB, MANA_CANDY, PHOENIX, WING, OBSIDIAN_SWORD, VOLCANO_ARMOR, ASH_ROBE},
            {HI_HERB, MANA_CANDY, PHOENIX, WING, ROA_SPEAR, RANGER_MAIL},
    };

    /** 해의 제단: {아이템, 해 개수} */
    public static final int[][] LEGACY_GOODS = {
            {SUN_SWORD, 20},
            {SUN_ARMOR, 20},
            {SUN_MANE, 40},
            {SUN_HEART, 60},
            {SUN_STAFF, 60},
            {DAWN_BLADE, 80},
            {HEAVEN_MAIL, 80},
            {ETERNAL_SUN, 1000},
    };

    /** 보물상자: {지도, x, y, 아이템(-1 이면 골드), 개수 또는 골드} */
    public static final int[][] CHESTS = {
            {CAVE1, 20, 1, -1, 300},
            {CAVE1, 11, 4, HERB, 3},
            {CAVE2, 16, 4, SILVER_ARMOR, 1},
            {SPRAY1, 22, 1, HI_HERB, 3},
            {SPRAY1, 11, 4, -1, 1500},
            {SPRAY2, 20, 3, STEEL_ARMOR, 1},
            {TEMPLE1, 22, 5, PHOENIX, 2},
            {TEMPLE2, 21, 6, SEA_ARMOR, 1},
            {LAVA1, 22, 1, HI_HERB, 4},
            {LAVA1, 13, 5, -1, 2000},
            {LAVA2, 21, 3, VOLCANO_ARMOR, 1},
            {FORT1, 6, 5, ELIXIR, 3},
            {FORT2, 21, 6, -1, 20000},
            {FORT3, 22, 6, ELIXIR, 5},
            {RUINS1, 22, 5, ELIXIR, 2},
            {RUINS2, 21, 6, -1, 30000},
            {CRYSTAL1, 20, 1, PHOENIX, 3},
            {CRYSTAL1, 11, 4, ELIXIR, 2},
            {CRYSTAL2, 16, 4, -1, 40000},
            {DEMON1, 6, 5, ELIXIR, 3},
            {DEMON2, 22, 6, ELIXIR, 5},
            {RUINS3, 22, 5, ELIXIR, 3},
            {CRYSTAL3, 11, 3, -1, 50000},
            {MOON1, 21, 1, PHOENIX, 3},
            {MOON2, 22, 6, ELIXIR, 3},
            {DEMON3, 21, 2, ELIXIR, 4},
    };

    // ------------------------------------------------------------ 주문 · 특기

    public static final int HEAL_ONE = 0, FIRE_ONE = 1, FIRE_ALL = 2, POWER_HIT = 3, GUARD = 4,
            HEAL_ALL = 5, BIG_HIT = 6, MULTI_HIT = 7, REVIVE = 8, PHYS_ALL = 9, STEAL_GOLD = 10,
            WATER_ONE = 11, WATER_ALL = 12, EVADE_UP = 13,
            HERO_ALL = 14, HERO_ONE = 15, HOLY_ALL = 16;

    public static final class Spell {
        public final String name;
        public final int owner;
        public final int level;
        public final int mp;
        public final int kind;
        public final int power;

        Spell(String name, int owner, int level, int mp, int kind, int power) {
            this.name = name;
            this.owner = owner;
            this.level = level;
            this.mp = mp;
            this.kind = kind;
            this.power = power;
        }

        public boolean targetsEnemy() {
            return kind == FIRE_ONE || kind == POWER_HIT || kind == BIG_HIT || kind == MULTI_HIT
                    || kind == STEAL_GOLD || kind == WATER_ONE || kind == HERO_ONE;
        }

        public boolean targetsAlly() {
            return kind == HEAL_ONE || kind == REVIVE;
        }

        /** 필드에서도 쓸 수 있는 주문 */
        public boolean fieldUsable() {
            return kind == HEAL_ONE || kind == HEAL_ALL || kind == REVIVE;
        }
    }

    public static final Spell[] SPELLS = {
            // 신창: 초반 기술은 소박하지만, Lv9 이후 기술은 판을 바꿀 만큼 강하다
            new Spell("치유의 빛", 0, 3, 3, HEAL_ONE, 32),
            new Spell("불꽃", 0, 5, 4, FIRE_ONE, 22),
            new Spell("대치유", 0, 8, 6, HEAL_ONE, 90),
            new Spell("불꽃의 폭풍", 0, 9, 7, FIRE_ALL, 44),
            new Spell("치유의 비", 0, 11, 9, HEAL_ALL, 70),
            new Spell("짐꾼의 일격", 0, 13, 10, BIG_HIT, 150),
            // 리나
            new Spell("강타", 1, 4, 3, POWER_HIT, 20),
            new Spell("수호의 맹세", 1, 7, 4, GUARD, 0),
            new Spell("기사의 연격", 1, 10, 6, MULTI_HIT, 3),
            // 2장에서 새로 배우는 기술 (번호는 뒤에 붙인다)
            new Spell("되살림", 0, 15, 12, REVIVE, 50),
            new Spell("짐꾼의 대일격", 0, 21, 16, BIG_HIT, 320),
            new Spell("질풍 베기", 1, 15, 8, PHYS_ALL, 13),
            new Spell("검기 폭풍", 1, 20, 12, MULTI_HIT, 5),
            // 피피
            new Spell("물방울 탄환", 2, 1, 3, WATER_ONE, 40),
            new Spell("흥정", 2, 1, 2, STEAL_GOLD, 0),
            new Spell("해일", 2, 18, 9, WATER_ALL, 75),
            new Spell("행운의 저울", 2, 22, 10, EVADE_UP, 0),
            // 지헌
            new Spell("따스한 손길", 3, 1, 4, HEAL_ONE, 110),
            new Spell("대장간 불꽃", 3, 1, 7, FIRE_ALL, 60),
            new Spell("담금질의 맹세", 3, 1, 6, GUARD, 0),
            new Spell("치유의 노래", 3, 1, 10, HEAL_ALL, 90),
            new Spell("되살림의 불씨", 3, 28, 14, REVIVE, 60),
            // 용사로 깨어난 신창 (주인 번호 10 = 각성한 0번)
            new Spell("섬광 베기", 10, 1, 10, HERO_ALL, 160),
            new Spell("용사의 일격", 10, 1, 12, HERO_ONE, 500),
            new Spell("기가 슬래시", 10, 1, 24, HERO_ALL, 320),
            new Spell("천벌의 뇌격", 10, 1, 30, HOLY_ALL, 900),
            new Spell("용사의 연격", 10, 1, 18, MULTI_HIT, 7),
            new Spell("생명의 빛", 10, 1, 20, HEAL_ALL, 400),
            new Spell("부활의 기적", 10, 1, 20, REVIVE, 100),
            new Spell("용기의 함성", 10, 1, 10, GUARD, 0),
            new Spell("슬레이어 일섬", 10, 40, 36, HERO_ONE, 1000),
            new Spell("성광 폭풍", 10, 45, 48, HERO_ALL, 600),
            // 불의 축복을 받은 지헌 (13 = 각성한 3번)
            new Spell("용광로", 13, 1, 20, FIRE_ALL, 300),
            new Spell("사랑의 기도", 13, 1, 24, HEAL_ALL, 500),
            new Spell("불사조의 날개", 13, 1, 20, REVIVE, 100),
            // 민한
            new Spell("숲의 숨결", 4, 1, 5, HEAL_ONE, 150),
            new Spell("짐승의 발톱", 4, 1, 6, MULTI_HIT, 3),
            new Spell("바람 가르기", 4, 22, 10, PHYS_ALL, 14),
            new Spell("짐승들의 노래", 4, 26, 14, HEAL_ALL, 220),
            // 해의 계승자 민한 (14 = 깨어난 4번)
            new Spell("햇살 베기", 14, 1, 12, HERO_ALL, 200),
            new Spell("태양의 일격", 14, 1, 14, HERO_ONE, 600),
            new Spell("해돋이", 14, 1, 30, HOLY_ALL, 1500),
            new Spell("가족의 빛", 14, 1, 20, REVIVE, 100),
            new Spell("일식", 14, 35, 40, HERO_ALL, 450),
            // 하루
            new Spell("물어뜯기", 5, 1, 0, POWER_HIT, 0),
            new Spell("늑대의 포효", 5, 1, 4, GUARD, 0),
            new Spell("질풍 돌격", 5, 24, 6, MULTI_HIT, 4),
            new Spell("해의 송곳니", 15, 1, 10, HERO_ONE, 350),
    };

    // ------------------------------------------------------------ 동료

    public static final class HeroBase {
        public final String name;
        public final String sprite;
        public final int hp, mp, atk, def, agi;
        public final int ghp, gmp, gatk, gdef, gagi;
        public final int weapon, armor;

        HeroBase(String name, String sprite, int hp, int mp, int atk, int def, int agi,
                 int ghp, int gmp, int gatk, int gdef, int gagi, int weapon, int armor) {
            this.name = name;
            this.sprite = sprite;
            this.hp = hp;
            this.mp = mp;
            this.atk = atk;
            this.def = def;
            this.agi = agi;
            this.ghp = ghp;
            this.gmp = gmp;
            this.gatk = gatk;
            this.gdef = gdef;
            this.gagi = gagi;
            this.weapon = weapon;
            this.armor = armor;
        }
    }

    public static final HeroBase[] HEROES = {
            new HeroBase("신창", "hero", 30, 8, 8, 5, 6, 7, 3, 3, 2, 2, STICK, CLOTHES),
            new HeroBase("리나", "lina", 40, 4, 11, 7, 5, 9, 1, 4, 3, 1, SQUIRE_SWORD, LEATHER),
            new HeroBase("피피", "pipi", 26, 14, 9, 5, 12, 6, 3, 3, 2, 3, COIN_POUCH, FAIRY_CLOAK),
            new HeroBase("지헌", "jiheon", 44, 22, 10, 7, 8, 9, 4, 3, 3, 2, SMITH_HAMMER, SMITH_APRON),
            new HeroBase("민한", "minhan", 60, 20, 18, 10, 14, 10, 4, 5, 3, 3, WOOD_SPEAR, LEAF_CLOAK),
            new HeroBase("하루", "haru", 70, 10, 22, 10, 18, 11, 1, 5, 3, 3, WOLF_FANG, WOLF_FUR),
    };

    public static final int MAX_LEVEL = 99;

    /** 다음 레벨까지 필요한 누적 경험치 */
    public static int expForLevel(int level) {
        if (level <= 1) return 0;
        return (int) Math.round(7 * Math.pow(level - 1, 2.3));
    }

    // ------------------------------------------------------------ 몬스터

    public static final class Monster {
        public final String name;
        public final String sprite;
        public final int hp, atk, def, agi, exp, gold;
        /** 0 없음, 1 불씨 뱉기, 2 보스(어둠의 포효·두 번 행동·격노) */
        public final int special;
        /** 강함의 기준 레벨. 파티보다 높으면 경험치 보너스 */
        public final int level;
        /** 공격을 피할 확률 (진귀한 경험치 몬스터) */
        public final double evade;
        /** 매 턴 도망칠 확률 */
        public final double flee;
        /** 0 보통, RARE_EXP 경험치 몬스터, RARE_GOLD 골드 몬스터 */
        public final int rare;

        Monster(String name, String sprite, int level, int hp, int atk, int def, int agi, int exp, int gold, int special) {
            this(name, sprite, level, hp, atk, def, agi, exp, gold, special, 0, 0, 0);
        }

        Monster(String name, String sprite, int level, int hp, int atk, int def, int agi, int exp, int gold, int special,
                double evade, double flee, int rare) {
            this.evade = evade;
            this.flee = flee;
            this.rare = rare;
            this.name = name;
            this.level = level;
            this.sprite = sprite;
            this.hp = hp;
            this.atk = atk;
            this.def = def;
            this.agi = agi;
            this.exp = exp;
            this.gold = gold;
            this.special = special;
        }
    }

    public static final int RAT = 0, SHROOM = 1, BAT = 2, THORN_WOLF = 3, PIXIE = 4, TURTLE = 5,
            SPIDER = 6, SKELETON = 7, SHADOW_WOLF = 8, WOLF_KING = 9,
            CRAB = 10, GULL = 11, SALT_SPIRIT = 12, JELLY = 13, PIRATE = 14, OCTOPUS = 15, URCHIN = 16, CORAL_GOLEM = 17,
            MER_KNIGHT = 18, SKEL_SAILOR = 19, STATUE = 20, EEL = 21,
            STARDUST = 22, GOLD_TOAD = 23, PEARL = 24, GEM_CRAB = 25, GALLEON = 26, SERPENT = 27,
            ASH_BAT = 28, LIZARD = 29, BOAR = 30, EMBER = 31, MAGMA = 32, IMP = 33, FLAME_WOLF = 34, LAVA_TURTLE = 35,
            BLACK_KNIGHT = 36, HELLHOUND = 37, FLAME_BEAST = 38, VOID_MAGE = 39, WYVERN = 40, BLACK_GOLEM = 41,
            RUBY = 42, LAVA_TOAD = 43, VARGAS0 = 44, GIANT = 45, IGRON = 46, WITCH = 47, VOLKA = 48, VARGAS = 49,
            MOTH = 50, MANTIS = 51, TROLL = 52, MANA_SHROOM = 53, GRIFFIN = 54, ROCK_GIANT = 55, MUMMY = 56, CURSE_STATUE = 57,
            ICE_WOLF = 58, CRYSTAL_GOLEM = 59, CRYSTAL_BAT = 60, DEMON_SOLDIER = 61, NINE_TAIL = 62, SHADOW_YOKAI = 63,
            DEMON_CAPTAIN = 64, SUN_FAIRY = 65, RAINBOW_TURTLE = 66, FOX1 = 67, HOSTAGE = 68, GUARDIAN = 69,
            CRYSTAL_QUEEN = 70, FOX2 = 71, DEMON_KING = 72,
            MOON_MOTH = 73, DARK_BEAR = 74, MOON_LORD = 75;
    public static final int RARE_EXP = 1, RARE_GOLD = 2;

    public static final Monster[] MONSTERS = {
            //          이름, 그림, 레벨, HP, 공격, 방어, 민첩, 경험치, 골드, 특수
            new Monster("들쥐 도적", "rat", 1, 9, 7, 2, 5, 3, 5, 0),
            new Monster("독버섯 꼬마", "shroom", 2, 12, 8, 3, 3, 4, 4, 0),
            new Monster("박쥐날개", "bat", 2, 8, 8, 1, 9, 4, 3, 0),
            new Monster("가시늑대", "thornwolf", 5, 22, 13, 6, 7, 14, 12, 0),
            new Monster("장난꾸러기 요정", "pixie", 5, 18, 10, 5, 8, 13, 14, 1),
            new Monster("돌껍질 거북", "turtle", 6, 32, 12, 14, 2, 20, 18, 0),
            new Monster("동굴 거미", "spider", 8, 30, 17, 7, 9, 30, 16, 0),
            new Monster("해골 병사", "skeleton", 9, 44, 20, 10, 5, 44, 24, 0),
            new Monster("그림자 늑대", "shadowwolf", 11, 58, 23, 11, 10, 68, 32, 0),
            new Monster("그림자 늑대왕", "wolfking", 15, 1700, 46, 17, 11, 700, 600, 2),
            // 2장
            new Monster("갯바위 게", "crab", 13, 80, 30, 26, 6, 32, 40, 0),
            new Monster("갈매기 도적", "gull", 13, 60, 32, 14, 20, 30, 55, 0),
            new Monster("소금 정령", "saltspirit", 14, 70, 28, 18, 12, 33, 36, 1),
            new Monster("물보라 해파리", "jelly", 15, 66, 30, 12, 14, 37, 30, 1),
            new Monster("해적 졸개", "pirate", 16, 110, 36, 20, 12, 45, 70, 0),
            new Monster("동굴 문어", "octopus", 17, 130, 38, 18, 10, 54, 50, 0),
            new Monster("독가시 성게", "urchin", 17, 90, 34, 40, 4, 53, 48, 0),
            new Monster("산호 골렘", "golem", 18, 200, 42, 30, 5, 68, 80, 0),
            new Monster("인어 기사", "merknight", 20, 170, 48, 30, 16, 86, 90, 0),
            new Monster("해골 선원", "skelsailor", 21, 190, 50, 28, 12, 94, 100, 0),
            new Monster("고대 수호석상", "statue", 22, 280, 52, 44, 4, 112, 110, 0),
            new Monster("심연 장어", "eel", 23, 220, 56, 30, 20, 122, 90, 1),
            // 진귀한 몬스터: 경험치형은 잘 피하고 잘 도망친다, 골드형은 약하지만 단단하다
            new Monster("별가루 요정", "stardust", 13, 8, 6, 60, 40, 450, 10, 0, 0.7, 0.3, RARE_EXP),
            new Monster("황금 두꺼비", "goldtoad", 6, 150, 4, 10, 1, 12, 450, 0, 0, 0, RARE_GOLD),
            new Monster("진주 요정", "pearl", 24, 14, 20, 80, 50, 1300, 20, 0, 0.7, 0.3, RARE_EXP),
            new Monster("보석 소라게", "gemcrab", 15, 460, 8, 26, 2, 40, 2600, 0, 0, 0, RARE_GOLD),
            // 2장 보스
            new Monster("붉은 수염 갈리온", "galleon", 20, 3800, 72, 34, 14, 1600, 1500, 2),
            new Monster("심해의 군주", "serpent", 27, 6800, 84, 44, 17, 3500, 4000, 2),
            // 3장: 이그나 섬 (동료를 잃은 고난의 시기)
            new Monster("재 박쥐", "ashbat", 25, 150, 78, 34, 24, 55, 70, 0),
            new Monster("흑요석 도마뱀", "lizard", 26, 220, 84, 56, 10, 62, 85, 0),
            new Monster("화산 멧돼지", "boar", 27, 270, 90, 42, 12, 70, 90, 0),
            new Monster("불씨 정령", "ember", 27, 170, 82, 36, 18, 72, 80, 1),
            new Monster("용암 슬라임", "magma", 28, 260, 88, 66, 6, 80, 100, 0),
            new Monster("화염 도깨비", "imp", 29, 240, 96, 46, 24, 90, 110, 1),
            new Monster("흑염 늑대", "flamewolf", 30, 300, 102, 52, 20, 100, 120, 0),
            new Monster("용암 거북", "lavaturtle", 31, 460, 98, 90, 4, 118, 150, 0),
            // 흑염의 요새: 보스급 마물 무리 (용사가 된 뒤에 싸운다)
            new Monster("흑염 기사", "blackknight", 30, 1100, 150, 90, 18, 63, 200, 0),
            new Monster("지옥 사냥개", "hellhound", 31, 900, 160, 70, 30, 66, 190, 0),
            new Monster("화염 마수", "flamebeast", 32, 1500, 170, 85, 14, 81, 250, 1),
            new Monster("공허 마도사", "voidmage", 32, 1000, 165, 75, 22, 78, 260, 1),
            new Monster("용암 와이번", "wyvern", 33, 1700, 180, 90, 26, 96, 300, 0),
            new Monster("흑염 골렘", "blackgolem", 34, 2600, 190, 130, 6, 129, 400, 0),
            // 3장 진귀한 몬스터
            new Monster("루비 요정", "ruby", 32, 18, 30, 100, 60, 2800, 30, 0, 0.7, 0.3, RARE_EXP),
            new Monster("금빛 화산 두꺼비", "lavatoad", 28, 900, 10, 40, 1, 60, 4000, 0, 0, 0, RARE_GOLD),
            // 3장 보스
            new Monster("흑염의 마장군 바르가스", "vargas", 70, 99999, 260, 400, 60, 0, 0, 2),
            new Monster("화염 거인", "giant", 32, 3400, 150, 56, 14, 2400, 2500, 2),
            new Monster("화염 기사단장 이그론", "ignis", 38, 8000, 175, 100, 24, 3000, 3000, 2),
            new Monster("독안개 마녀 모르가", "witch", 40, 9000, 185, 90, 30, 3500, 3500, 2),
            new Monster("용암룡 볼카", "lavadragon", 42, 12000, 200, 120, 20, 4500, 4500, 2),
            new Monster("흑염의 마장군 바르가스", "vargas", 45, 20000, 230, 140, 32, 8000, 10000, 2),
            // 4장: 실바 대륙 (민한의 모험)
            new Monster("독가루 나방", "moth", 20, 160, 70, 40, 24, 55, 70, 1),
            new Monster("칼날 사마귀", "mantis", 22, 200, 82, 50, 26, 65, 80, 0),
            new Monster("숲 트롤", "troll", 24, 320, 90, 55, 8, 80, 95, 0),
            new Monster("마나 버섯", "manashroom", 23, 180, 72, 45, 10, 70, 75, 1),
            new Monster("그리핀", "griffin", 34, 900, 200, 110, 40, 64, 260, 0),
            new Monster("바위 거인", "rockgiant", 36, 1500, 220, 160, 6, 76, 300, 0),
            new Monster("미라 기사", "mummy", 37, 1100, 230, 120, 20, 80, 320, 0),
            new Monster("저주 석상", "cursestatue", 38, 1600, 220, 180, 5, 84, 340, 1),
            new Monster("얼음 늑대", "icewolf", 38, 1200, 240, 120, 36, 84, 320, 0),
            new Monster("수정 골렘", "crystalgolem", 40, 2000, 250, 190, 6, 96, 380, 0),
            new Monster("수정 박쥐", "crystalbat", 39, 800, 230, 100, 44, 80, 300, 0),
            new Monster("요마 병사", "demonsoldier", 42, 1700, 270, 160, 24, 104, 420, 0),
            new Monster("구미 여우", "ninetail", 43, 1400, 280, 140, 40, 108, 440, 1),
            new Monster("그림자 요괴", "shadowyokai", 44, 1300, 290, 130, 30, 112, 460, 1),
            new Monster("요마 대장", "demoncaptain", 46, 2800, 310, 190, 18, 144, 600, 0),
            new Monster("해 요정", "sunfairy", 40, 20, 40, 150, 70, 2500, 50, 0, 0.7, 0.3, RARE_EXP),
            new Monster("무지개 거북", "rainbowturtle", 30, 2500, 20, 100, 1, 100, 12000, 0, 0, 0, RARE_GOLD),
            // 4장 보스
            new Monster("천면귀", "fox", 55, 14000, 300, 180, 30, 5000, 3000, 2),
            new Monster("민한을 인질로 잡은 천면귀", "fox", 99, 999999, 450, 999, 99, 0, 0, 2),
            new Monster("석상 수호자", "guardian", 38, 9000, 260, 150, 14, 2000, 4000, 2),
            new Monster("수정 여왕", "crystalqueen", 42, 12000, 290, 160, 26, 2500, 5000, 2),
            new Monster("천면귀 (본모습)", "fox", 55, 30000, 380, 200, 36, 6000, 10000, 2),
            new Monster("요마왕 다르칸", "demonking", 60, 60000, 450, 230, 34, 20000, 30000, 2),
            // 달빛 숲길
            new Monster("달빛 나방", "moonmoth", 36, 900, 230, 110, 40, 80, 280, 1),
            new Monster("어둠 곰", "darkbear", 38, 1700, 250, 150, 10, 90, 320, 0),
            new Monster("어둠에 먹힌 숲의 주인", "forestlord", 44, 16000, 300, 170, 28, 3000, 6000, 2),
    };

    /** 지역별 몬스터 무리 후보: 몬스터 번호 목록 */
    public static final int[][][] ENCOUNTERS = {
            // 0: 다리 남쪽 평원
            {{RAT}, {RAT, RAT}, {SHROOM}, {BAT}, {RAT, SHROOM}, {BAT, BAT}, {RAT, RAT, RAT}},
            // 1: 다리 북쪽 평원
            {{THORN_WOLF}, {PIXIE}, {TURTLE}, {SHROOM, PIXIE}, {THORN_WOLF, BAT}, {PIXIE, PIXIE}, {TURTLE, SHROOM}},
            // 2: 동굴 1층
            {{BAT, BAT}, {SPIDER}, {SKELETON}, {SPIDER, BAT}, {SKELETON, BAT}, {SPIDER, SPIDER}},
            // 3: 동굴 2층
            {{SKELETON}, {SHADOW_WOLF}, {SPIDER, SKELETON}, {SHADOW_WOLF, BAT}, {SKELETON, SKELETON}},
            // 4: 메르카 평원 서쪽 (강 서쪽)
            {{CRAB}, {GULL}, {SALT_SPIRIT}, {CRAB, GULL}, {GULL, GULL}, {SALT_SPIRIT, CRAB}},
            // 5: 메르카 평원 동쪽 (강 동쪽, 곶)
            {{JELLY}, {PIRATE}, {JELLY, GULL}, {PIRATE, SALT_SPIRIT}, {JELLY, JELLY}, {PIRATE, CRAB}},
            // 6: 물보라 동굴 1층
            {{PIRATE}, {OCTOPUS}, {URCHIN}, {PIRATE, PIRATE}, {OCTOPUS, JELLY}, {URCHIN, PIRATE}},
            // 7: 물보라 동굴 2층
            {{CORAL_GOLEM}, {OCTOPUS, PIRATE}, {URCHIN, URCHIN}, {CORAL_GOLEM, JELLY}, {PIRATE, PIRATE, OCTOPUS}},
            // 8: 해저 신전 1층
            {{MER_KNIGHT}, {SKEL_SAILOR}, {MER_KNIGHT, URCHIN}, {SKEL_SAILOR, OCTOPUS}, {CORAL_GOLEM, MER_KNIGHT}},
            // 9: 해저 신전 2층
            {{STATUE}, {EEL}, {MER_KNIGHT, SKEL_SAILOR}, {EEL, MER_KNIGHT}, {STATUE, EEL}},
            // 10: 이그나 섬 남쪽 (마을·해변)
            {{ASH_BAT}, {LIZARD}, {BOAR}, {ASH_BAT, ASH_BAT}, {LIZARD, ASH_BAT}, {EMBER}},
            // 11: 이그나 섬 북쪽·동쪽 (화산 기슭)
            {{BOAR, EMBER}, {MAGMA}, {IMP}, {FLAME_WOLF}, {BOAR, BOAR}, {MAGMA, ASH_BAT}},
            // 12: 용암 동굴 1층
            {{MAGMA}, {IMP}, {FLAME_WOLF}, {IMP, EMBER}, {MAGMA, IMP}, {LAVA_TURTLE}},
            // 13: 용암 동굴 2층
            {{LAVA_TURTLE}, {FLAME_WOLF, IMP}, {MAGMA, MAGMA}, {LAVA_TURTLE, EMBER}, {FLAME_WOLF, FLAME_WOLF}},
            // 14: 흑염의 요새 1층
            {{BLACK_KNIGHT, BLACK_KNIGHT, HELLHOUND}, {HELLHOUND, HELLHOUND, HELLHOUND}, {BLACK_KNIGHT, VOID_MAGE, HELLHOUND},
                    {FLAME_BEAST, HELLHOUND}, {BLACK_KNIGHT, BLACK_KNIGHT, BLACK_KNIGHT, BLACK_KNIGHT}},
            // 15: 흑염의 요새 2층
            {{FLAME_BEAST, VOID_MAGE, BLACK_KNIGHT}, {WYVERN, HELLHOUND, HELLHOUND}, {VOID_MAGE, VOID_MAGE, VOID_MAGE},
                    {FLAME_BEAST, FLAME_BEAST, BLACK_KNIGHT}, {WYVERN, BLACK_KNIGHT, VOID_MAGE, HELLHOUND}},
            // 16: 흑염의 요새 옥좌
            {{BLACK_GOLEM, WYVERN}, {WYVERN, WYVERN, FLAME_BEAST}, {BLACK_GOLEM, VOID_MAGE, VOID_MAGE},
                    {BLACK_GOLEM, BLACK_GOLEM}, {WYVERN, FLAME_BEAST, VOID_MAGE, BLACK_KNIGHT}},
            // 17: 실바 대륙 서쪽 (숲)
            {{MOTH}, {MANTIS}, {TROLL}, {MANA_SHROOM}, {MOTH, MOTH}, {MANTIS, MANA_SHROOM}},
            // 18: 실바 대륙 동쪽 (고원)
            {{GRIFFIN}, {ROCK_GIANT}, {GRIFFIN, GRIFFIN}, {ROCK_GIANT, GRIFFIN}, {GRIFFIN, MANTIS, MANTIS}},
            // 19: 고원 유적 1층
            {{MUMMY}, {CURSE_STATUE}, {MUMMY, MUMMY}, {GRIFFIN, MUMMY}, {CURSE_STATUE, MUMMY}},
            // 20: 고원 유적 2층
            {{CURSE_STATUE, MUMMY}, {MUMMY, MUMMY, MUMMY}, {CURSE_STATUE, CURSE_STATUE}, {ROCK_GIANT, MUMMY}},
            // 21: 수정 동굴 1층
            {{ICE_WOLF}, {CRYSTAL_BAT, CRYSTAL_BAT}, {ICE_WOLF, CRYSTAL_BAT}, {CRYSTAL_GOLEM}},
            // 22: 수정 동굴 2층
            {{CRYSTAL_GOLEM, ICE_WOLF}, {ICE_WOLF, ICE_WOLF, CRYSTAL_BAT}, {CRYSTAL_GOLEM, CRYSTAL_BAT, CRYSTAL_BAT}},
            // 23: 요마성 1층
            {{DEMON_SOLDIER, DEMON_SOLDIER, NINE_TAIL}, {SHADOW_YOKAI, SHADOW_YOKAI, DEMON_SOLDIER}, {DEMON_CAPTAIN, NINE_TAIL},
                    {NINE_TAIL, NINE_TAIL, NINE_TAIL, SHADOW_YOKAI}},
            // 24: 요마성 옥좌
            {{DEMON_CAPTAIN, DEMON_SOLDIER, DEMON_SOLDIER}, {DEMON_CAPTAIN, DEMON_CAPTAIN},
                    {NINE_TAIL, SHADOW_YOKAI, DEMON_CAPTAIN, DEMON_SOLDIER}},
            // 25: 달빛 숲길
            {{MOON_MOTH}, {DARK_BEAR}, {MOON_MOTH, MOON_MOTH}, {DARK_BEAR, ICE_WOLF}, {MOON_MOTH, MANTIS, MANTIS}},
            // 26: 달빛 샘
            {{DARK_BEAR, DARK_BEAR}, {MOON_MOTH, MOON_MOTH, MOON_MOTH}, {DARK_BEAR, MOON_MOTH, ICE_WOLF}},
            // 27: 고원 유적 3층
            {{CURSE_STATUE, CURSE_STATUE, MUMMY}, {ROCK_GIANT, ROCK_GIANT}, {MUMMY, MUMMY, MUMMY, GRIFFIN}},
            // 28: 수정 동굴 3층
            {{CRYSTAL_GOLEM, CRYSTAL_GOLEM}, {ICE_WOLF, ICE_WOLF, ICE_WOLF}, {CRYSTAL_GOLEM, CRYSTAL_BAT, ICE_WOLF, CRYSTAL_BAT}},
            // 29: 요마성 2층
            {{DEMON_SOLDIER, DEMON_SOLDIER, DEMON_SOLDIER}, {NINE_TAIL, NINE_TAIL, SHADOW_YOKAI},
                    {DEMON_CAPTAIN, SHADOW_YOKAI, NINE_TAIL}, {DEMON_CAPTAIN, DEMON_CAPTAIN, DEMON_SOLDIER}},
    };

    public static int encounterZone(int map, int x, int y) {
        switch (map) {
            case CAVE1: return 2;
            case CAVE2: return 3;
            case WORLD2: return x < 17 ? 4 : 5;
            case SPRAY1: return 6;
            case SPRAY2: return 7;
            case TEMPLE1: return 8;
            case TEMPLE2: return 9;
            case WORLD3: return y < 12 || x > 16 ? 11 : 10;
            case LAVA1: return 12;
            case LAVA2: return 13;
            case FORT1: return 14;
            case FORT2: return 15;
            case FORT3: return 16;
            case WORLD4: return x < 16 ? 17 : 18;
            case RUINS1: return 19;
            case RUINS2: return 20;
            case CRYSTAL1: return 21;
            case CRYSTAL2: return 22;
            case DEMON1: return 23;
            case DEMON2: return 24;
            case MOON1: return 25;
            case MOON2: return 26;
            case RUINS3: return 27;
            case CRYSTAL3: return 28;
            case DEMON3: return 29;
            default: return y > 11 ? 0 : 1;
        }
    }

    /** 진귀한 몬스터가 나올 확률 */
    public static final double RARE_EXP_CHANCE = 0.05, RARE_GOLD_CHANCE = 0.06;

    /**
     * 진귀한 몬스터 후보: 보스 앞 던전에서는 경험치 몬스터, 마을 근교 평원에서는 골드 몬스터.
     * 해당 없으면 -1.
     */
    public static int rareMonsterAt(int map, int x, int y) {
        if (map == CAVE1 || map == CAVE2) return STARDUST;
        if (map == SPRAY1 || map == SPRAY2 || map == TEMPLE1 || map == TEMPLE2) return PEARL;
        if (map == WORLD && (near(x, y, 6, 18) || near(x, y, 20, 8))) return GOLD_TOAD;
        if (map == WORLD2 && (near(x, y, 5, 17) || near(x, y, 28, 19))) return GEM_CRAB;
        if (map == LAVA1 || map == LAVA2 || map == FORT1 || map == FORT2 || map == FORT3) return RUBY;
        if (map == WORLD3 && near(x, y, 8, 17)) return LAVA_TOAD;
        if (map == RUINS1 || map == RUINS2 || map == RUINS3 || map == CRYSTAL1 || map == CRYSTAL2 || map == CRYSTAL3
                || map == DEMON1 || map == DEMON2 || map == DEMON3 || map == MOON1 || map == MOON2) return SUN_FAIRY;
        if (map == WORLD4 && (near(x, y, 6, 6) || near(x, y, 14, 18))) return RAINBOW_TURTLE;
        return -1;
    }

    private static boolean near(int x, int y, int tx, int ty) {
        return Math.abs(x - tx) + Math.abs(y - ty) <= 7;
    }

    private RpgData() {
    }
}
