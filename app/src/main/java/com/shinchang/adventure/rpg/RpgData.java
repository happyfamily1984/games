package com.shinchang.adventure.rpg;

/**
 * 환상의 세계 아르카디아: 지도, 등장인물, 몬스터, 아이템, 주문.
 * 모든 이야기와 캐릭터는 이 게임의 오리지널 설정이다.
 */
public final class RpgData {

    // ------------------------------------------------------------ 지도

    public static final int OFFICE = 0, WORLD = 1, REBEL = 2, ERDEN = 3, CAVE1 = 4, CAVE2 = 5;
    public static final String[] MAP_NAMES = {"청와대 집무실", "아르카디아 평원", "리벨 마을", "에르덴 성", "그림자 동굴 1층", "그림자 동굴 2층"};

    /** 지도 그림은 tools/rpg_maps.py 가 만든 RpgMaps 에 있다 */
    public static final String[][] MAPS = {RpgMaps.OFFICE, RpgMaps.WORLD, RpgMaps.REBEL, RpgMaps.ERDEN, RpgMaps.CAVE1, RpgMaps.CAVE2};

    /** 지나갈 수 없는 타일 */
    public static boolean blocks(char t) {
        return "#~^H|WCbtkoxFQYPAMgNLZije".indexOf(t) >= 0;
    }

    /** 몬스터가 나오는 지도 */
    public static boolean hasEncounters(int map) {
        return map == WORLD || map == CAVE1 || map == CAVE2;
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
    };

    /** 마을에 들어가면 그 마을이 "마지막으로 들른 마을"이 된다 (전멸·날개깃 귀환 위치) */
    public static final int[][] TOWN_RETURN = {
            {REBEL, 12, 19},
            {ERDEN, 13, 25},
    };

    /** 처음 시작 위치 (집무실) */
    public static final int START_X = 6, START_Y = 5;

    // ------------------------------------------------------------ 이야기 단계 · 단서

    public static final int STEP_PROLOGUE = 0;
    public static final int STEP_ARRIVED = 1;
    public static final int STEP_LETTER = 2;
    public static final int STEP_LINA = 3;
    public static final int STEP_BOSS_DOWN = 4;
    public static final int STEP_CHAPTER1_CLEAR = 5;

    public static final String[] CHAPTER_TITLES = {"프롤로그 「청와대의 머신」", "1장 「배달부 용사」"};

    /** 찾아낸 단서 (비트) */
    public static final int FLAG_RECORD = 1, FLAG_CHRONICLE = 2, FLAG_MURAL = 4, FLAG_LIBRARY = 8, FLAG_DIARY = 16;
    public static final String[] CLUE_NAMES = {"금고의 경고문", "리벨 연대기", "성당 벽화", "에르덴 서고의 기록", "로웰의 일기"};
    /** 단서 메뉴에 보여 줄 요약 */
    public static final String[] CLUE_TEXTS = {
            "청와대 금고: '문 저편에서 오는 자는 짐꾼이 아니다' — 서명 E. 가방과 같은 문양.",
            "300년 전 짐꾼 카이른이 다섯 봉인석으로 공허의 왕을 가뒀다. 곁에 현자 아르센과 누이 엘라.",
            "리벨 성당: 짐꾼은 마지막에 짐을 모두 내려놓고 웃는다. 곁에 등을 돌린 회색 망토.",
            "공허의 왕은 욕망을 먹고 '가장 많이 가진 자'의 몸을 탐한다. 다섯 번째 봉인석의 기록은 찢겨 있다.",
            "회색 망토 '문지기'가 봉인석을 깨뜨렸다. 가장 많이 가진 자가 오면 그릇이 완성된다고 했다. 로웰은 바다로.",
    };

    // ------------------------------------------------------------ 등장인물 (마을 사람)

    public static final int ACT_NONE = 0, ACT_SHOP = 1, ACT_INN = 2, ACT_SAVE = 3, ACT_EVENT = 4;
    public static final int EV_ELDER = 1, EV_KING = 2, EV_BOSS = 3;

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
                    at(STEP_BOSS_DOWN, "봉인석이 깨져 있었다고? …그렇다면 예언의 '다섯 빛'은 다섯 개의 봉인석일세.",
                            "누군가 일부러 봉인을 풀고 있다는 뜻이야. 대체 무엇을 위해서…"),
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
            npc(REBEL, 14, 10, "child", "마을 아이",
                    at(0, "아저씨 하늘에서 떨어졌어? 그 가방 문양 멋있다!",
                            "성당 벽화에도 똑같은 게 있어. 장로 할아버지가 그건 '짐꾼의 문장'이래."),
                    at(STEP_LINA, "기사 누나다! 우리 아빠가 그러는데 누나네 아빠가 제일 센 기사였대."),
                    at(STEP_BOSS_DOWN, "이제 밤에 늑대 소리 안 나! 아저씨 최고!")),
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
                            "단장님 방패엔 짐꾼의 문장이 새겨져 있었지. 단장님 집안은 대대로 카이른 기사단이었거든."),
                    at(STEP_LINA, "리나를 부탁하네, 짐꾼 양반. 저 녀석, 검은 나보다 낫다니까.")),
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
                    at(STEP_CHAPTER1_CLEAR, "♪ 배달부 용사가 늑대왕을 쓰러뜨렸네~ 새 노래를 지었답니다!",
                            "그런데 이상하죠. 옛 노래의 끝 구절이 자꾸 떠올라요. '짐을 내려놓으리'…")),
            npc(ERDEN, 21, 18, "villager", "아낙",
                    at(0, "메르카에서 오던 소금배가 끊긴 지 오래예요. 바다도 요즘 이상하대요.",
                            "밤바다가 푸르스름하게 빛난다나 봐요.")),

            // ---- 그림자 동굴
            new Npc(CAVE2, 5, 4, "boss", "그림자 늑대왕", ACT_EVENT, EV_BOSS, STEP_LINA, STEP_LINA, new Object[][]{
                    at(0, "크르르… 문 저편의 냄새. 그분이 기다리던 자로군.",
                            "깨진 봉인에서 흘러나온 어둠이 우리를 바꿨다. 이제 우리는 공허의 왕의 이빨이다!",
                            "짐꾼이든 그릇이든, 여기서 끝이다!"),
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

    public static final int LOWELL_DIARY = 32, SEAL_SHARD = 33;

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
            "[에르덴 왕] 용자여, 메르카로 가는 배는 곧 준비될 걸세. 모르칸이 서두르고 있다네.",
    };

    public static final String[] BOSS_DEFEATED = {
            "[그림자 늑대왕] 크윽… 문지기님… 짐꾼이… 왔습니다… 그릇이… 왔…",
            "(그림자 늑대왕은 어둠 속으로 흩어졌다)",
            "(늑대왕이 있던 자리에 깨진 돌 조각이 떨어져 있다. 「깨진 봉인석」을 손에 넣었다!)",
            "[리나] 이 돌… 봉인석이에요? 누가 이렇게 깨뜨린 거죠?",
            "[신창] '그릇'이 왔다고…? 무슨 뜻이지.",
            "[리나] 일단 폐하께 알려요!",
    };

    // ------------------------------------------------------------ 아이템

    public static final int CONSUMABLE = 0, WEAPON = 1, ARMOR = 2, KEY = 3;

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

    public static final int HERB = 0, MANA_CANDY = 1, WING = 2;
    public static final int STICK = 10, COPPER_SWORD = 11, SQUIRE_SWORD = 12, IRON_SWORD = 13, KNIGHT_SPEAR = 14;
    public static final int CLOTHES = 20, LEATHER = 21, CHAIN_MAIL = 22, SILVER_ARMOR = 23;
    public static final int LETTER = 30, HERO_BADGE = 31;
    public static final int ITEM_SLOTS = 40;

    public static final Item[] ITEMS = new Item[ITEM_SLOTS];

    static {
        put(new Item(HERB, "약초", CONSUMABLE, 30, 8, "HP를 30쯤 회복"));
        put(new Item(MANA_CANDY, "마나 사탕", CONSUMABLE, 15, 30, "MP를 15 회복"));
        put(new Item(WING, "날개깃", CONSUMABLE, 0, 25, "마지막으로 들른 마을로 돌아간다"));
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
    }

    private static void put(Item item) {
        ITEMS[item.id] = item;
    }

    public static final int[][] SHOPS = {
            {HERB, WING, COPPER_SWORD, LEATHER},
            {HERB, MANA_CANDY, WING, IRON_SWORD, CHAIN_MAIL, KNIGHT_SPEAR},
    };

    /** 보물상자: {지도, x, y, 아이템(-1 이면 골드), 개수 또는 골드} */
    public static final int[][] CHESTS = {
            {CAVE1, 20, 1, -1, 300},
            {CAVE1, 11, 4, HERB, 3},
            {CAVE2, 16, 4, SILVER_ARMOR, 1},
    };

    // ------------------------------------------------------------ 주문 · 특기

    public static final int HEAL_ONE = 0, FIRE_ONE = 1, FIRE_ALL = 2, POWER_HIT = 3, GUARD = 4;

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
            return kind == FIRE_ONE || kind == POWER_HIT;
        }

        public boolean targetsAlly() {
            return kind == HEAL_ONE;
        }
    }

    public static final Spell[] SPELLS = {
            new Spell("치유의 빛", 0, 3, 3, HEAL_ONE, 30),
            new Spell("불꽃", 0, 5, 4, FIRE_ONE, 18),
            new Spell("대치유", 0, 8, 7, HEAL_ONE, 75),
            new Spell("불꽃의 폭풍", 0, 10, 8, FIRE_ALL, 24),
            new Spell("강타", 1, 4, 3, POWER_HIT, 18),
            new Spell("수호의 맹세", 1, 7, 4, GUARD, 0),
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
    };

    public static final int MAX_LEVEL = 30;

    /** 다음 레벨까지 필요한 누적 경험치 */
    public static int expForLevel(int level) {
        if (level <= 1) return 0;
        return (int) Math.round(8 * Math.pow(level - 1, 2.15));
    }

    // ------------------------------------------------------------ 몬스터

    public static final class Monster {
        public final String name;
        public final String sprite;
        public final int hp, atk, def, agi, exp, gold;
        /** 0 없음, 1 불씨 뱉기, 2 어둠의 포효(전체), 3 두 번 행동 */
        public final int special;

        Monster(String name, String sprite, int hp, int atk, int def, int agi, int exp, int gold, int special) {
            this.name = name;
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
            SPIDER = 6, SKELETON = 7, SHADOW_WOLF = 8, WOLF_KING = 9;

    public static final Monster[] MONSTERS = {
            new Monster("들쥐 도적", "rat", 9, 7, 2, 5, 3, 5, 0),
            new Monster("독버섯 꼬마", "shroom", 12, 8, 3, 3, 4, 4, 0),
            new Monster("박쥐날개", "bat", 8, 8, 1, 9, 4, 3, 0),
            new Monster("가시늑대", "thornwolf", 20, 12, 5, 7, 9, 10, 0),
            new Monster("장난꾸러기 요정", "pixie", 16, 9, 4, 8, 8, 12, 1),
            new Monster("돌껍질 거북", "turtle", 28, 11, 12, 2, 12, 14, 0),
            new Monster("동굴 거미", "spider", 24, 14, 6, 8, 14, 12, 0),
            new Monster("해골 병사", "skeleton", 34, 16, 9, 5, 20, 20, 0),
            new Monster("그림자 늑대", "shadowwolf", 40, 18, 9, 9, 26, 25, 0),
            new Monster("그림자 늑대왕", "wolfking", 320, 23, 12, 10, 300, 300, 2),
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
    };

    public static int encounterZone(int map, int x, int y) {
        if (map == CAVE1) return 2;
        if (map == CAVE2) return 3;
        return y > 11 ? 0 : 1;
    }

    private RpgData() {
    }
}
