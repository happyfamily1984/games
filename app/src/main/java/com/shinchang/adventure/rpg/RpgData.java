package com.shinchang.adventure.rpg;

/**
 * 환상의 세계 아르카디아: 지도, 등장인물, 몬스터, 아이템, 주문.
 * 모든 이야기와 캐릭터는 이 게임의 오리지널 설정이다.
 */
public final class RpgData {

    // ------------------------------------------------------------ 지도

    public static final int OFFICE = 0, WORLD = 1, REBEL = 2, ERDEN = 3, CAVE1 = 4, CAVE2 = 5;
    public static final String[] MAP_NAMES = {"대통령 집무실", "아르카디아 평원", "리벨 마을", "에르덴 성", "그림자 동굴 1층", "그림자 동굴 2층"};

    /*
     * 타일
     *  . 풀밭  T 숲  f 꽃  = 길  B 다리  ~ 물  ^ 산
     *  V 마을  K 성  O 동굴 입구
     *  , 바닥  # 벽  h 지붕  d 문  r 융단  | 책상  W 차원 이동 장치
     *  X 출구  S 내려가는 계단  U 올라가는 계단  C 보물상자
     */
    static final String[] MAP_OFFICE = {
            "############",
            "#,,,,,,,,,,#",
            "#,,|||||,,,#",
            "#,,,,,,,,,,#",
            "#,,,,,,,,W,#",
            "#,,,,,,,,,,#",
            "#,,,,,,,,,,#",
            "#,,,,,,,,,,#",
            "############",
    };
    static final String[] MAP_WORLD = {
            "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
            "^TTTT.......^^^^^^^^^^^^^^^^^^^^^^",
            "^TTT.........^^^^^^^^^^^^^^...^^^^",
            "^TT...........^^^^^^^^^^^^..O..^^^",
            "^T..............^^^^^^^^^.....TT^^",
            "^.................^^^^^^.....TTT.^",
            "^......TTT............=====TTT...^",
            "^.....TTTTT.........==...TTTT....^",
            "^.....TTTT.........=K=....TT.....^",
            "^......TT.........==.=...........^",
            "^.................=..............^",
            "~~~~~~~~~~~~~~~~~B~~~~~~~~~~~~~~~^",
            "^................=...............^",
            "^.....ff.........=......TTT......^",
            "^....ffff........=.....TTTTT.....^",
            "^.....ff.........=......TTT......^",
            "^................=...............^",
            "^.....============...............^",
            "^.....V...........TTT............^",
            "^.....TT.........TTTTT...........^",
            "^....TTTT.........TTT....~~~~....^",
            "^.....TT................~~~~~~...^",
            "^........................~~~~....^",
            "^................................^",
            "^................................^",
            "^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^",
    };
    static final String[] MAP_REBEL = {
            "##################",
            "#,,,,,,,,,,,,,,,,#",
            "#,####,,,,,,####,#",
            "#,#hh#,,,,,,#hh#,#",
            "#,#hh#,,ff,,#hh#,#",
            "#,##d#,,ff,,##d#,#",
            "#,,,,,,,,,,,,,,,,#",
            "#,,,,,,,,,,,,,,,,#",
            "#,####,,,,,,####,#",
            "#,#hh#,,,,,,#hh#,#",
            "#,#hh#,,,,,,#hh#,#",
            "#,##d#,,,,,,##d#,#",
            "#,,,,,,,,,,,,,,,,#",
            "########XX########",
    };
    static final String[] MAP_ERDEN = {
            "####################",
            "#rrrrrrrrrrrrrrrrrr#",
            "#r################r#",
            "#r#,,,,,,,,,,,,,,#r#",
            "#r#,,,,,rrrr,,,,,#r#",
            "#r#,,,,,rrrr,,,,,#r#",
            "#r#,,,,,rrrr,,,,,#r#",
            "#r#######rr#######r#",
            "#,,,,,,,,rr,,,,,,,,#",
            "#,####,,,rr,,,####,#",
            "#,#hh#,,,rr,,,#hh#,#",
            "#,##d#,,,rr,,,##d#,#",
            "#,,,,,,,,rr,,,,,,,,#",
            "#,ff,,,,,rr,,,,,ff,#",
            "#,,,,,,,,rr,,,,,,,,#",
            "#########XX#########",
    };
    static final String[] MAP_CAVE1 = {
            "######################",
            "#,,,,,#,,,,,,,,,#,,,C#",
            "#,##,,#,,#####,,#,,,,#",
            "#,#,,,,,,#,,,#,,,,##,#",
            "#,#,####,#,C,#,,#,,#,#",
            "#,,,#,,,,#,,,#,,#,,#,#",
            "###,#,####,###,##,,#,#",
            "#,,,#,,,,,,,,,,,,,,#,#",
            "#,#######,####,#####,#",
            "#,,,,,,,#,#,,,,,,,,,,#",
            "#,#####,#,#,########,#",
            "#,#,,,#,,,#,,,,,,,,#,#",
            "#,#,S,#####,######,#,#",
            "#,,,,,,,,,,,,,,,,,,,,#",
            "##########XX##########",
            "######################",
    };
    static final String[] MAP_CAVE2 = {
            "######################",
            "#,,,,,,,,,,,,,,,,,,,,#",
            "#,#######,,,,#######,#",
            "#,#,,,,,#,,,,#,,,,,#,#",
            "#,#,,,,,#,,,,#,,C,,#,#",
            "#,#,,,,,#,,,,#,,,,,#,#",
            "#,###,###,,,,###,###,#",
            "#,,,,,,,,,,,,,,,,,,,,#",
            "####,##########,######",
            "#,,,,,,,,,,,,,,,,,,,,#",
            "#,############,#####,#",
            "#,#,,,,,,,,,,,,,,,,#,#",
            "#,#,########,#####,#,#",
            "#,,,,,,,,,U,,,,,,,,,,#",
            "######################",
            "######################",
    };
    public static final String[][] MAPS = {MAP_OFFICE, MAP_WORLD, MAP_REBEL, MAP_ERDEN, MAP_CAVE1, MAP_CAVE2};

    public static boolean blocks(char t) {
        return t == '#' || t == '~' || t == '^' || t == 'h' || t == 'd' || t == '|' || t == 'W' || t == 'C';
    }

    /** 몬스터가 나오는 지도 */
    public static boolean hasEncounters(int map) {
        return map == WORLD || map == CAVE1 || map == CAVE2;
    }

    /** 지도 이동: (지도, x, y) 에 들어가면 (도착 지도, x, y) 로 */
    public static final int[][] LINKS = {
            // 평원 → 마을 / 성 / 동굴
            {WORLD, 6, 18, REBEL, 8, 12},
            {WORLD, 20, 8, ERDEN, 9, 14},
            {WORLD, 28, 3, CAVE1, 10, 13},
            // 출구 → 평원
            {REBEL, 8, 13, WORLD, 6, 19},
            {REBEL, 9, 13, WORLD, 6, 19},
            {ERDEN, 9, 15, WORLD, 20, 9},
            {ERDEN, 10, 15, WORLD, 20, 9},
            {CAVE1, 10, 14, WORLD, 28, 4},
            {CAVE1, 11, 14, WORLD, 28, 4},
            // 동굴 계단
            {CAVE1, 4, 12, CAVE2, 10, 12},
            {CAVE2, 10, 13, CAVE1, 4, 11},
    };

    /** 마을에 들어가면 그 마을이 "마지막으로 들른 마을"이 된다 (전멸·날개깃 귀환 위치) */
    public static final int[][] TOWN_RETURN = {
            {REBEL, 8, 11},
            {ERDEN, 9, 13},
    };

    // ------------------------------------------------------------ 이야기 단계

    public static final int STEP_PROLOGUE = 0;
    public static final int STEP_ARRIVED = 1;
    public static final int STEP_LETTER = 2;
    public static final int STEP_LINA = 3;
    public static final int STEP_BOSS_DOWN = 4;
    public static final int STEP_CHAPTER1_CLEAR = 5;

    public static final String[] CHAPTER_TITLES = {"프롤로그 「청와대의 머신」", "1장 「배달부 용사」"};

    // ------------------------------------------------------------ 등장인물 (마을 사람)

    public static final int ACT_NONE = 0, ACT_SHOP = 1, ACT_INN = 2, ACT_SAVE = 3, ACT_EVENT = 4;
    public static final int EV_ELDER = 1, EV_KING = 2, EV_BOSS = 3, EV_LINA = 4;

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
            // 프롤로그
            npc(OFFICE, 2, 6, "secretary", "비서실장",
                    at(0, "대통령님, 청와대 금고 가장 깊은 곳에서 이상한 기계가 발견됐습니다.",
                            "설계도도, 제조사도 없습니다. 오른쪽에 있는 저 기계입니다.")),
            npc(OFFICE, 10, 7, "guard", "경호원",
                    at(0, "대통령님, 저 기계에서 가끔 바람 소리가 납니다. 조심하십시오.")),

            // 리벨 마을
            new Npc(REBEL, 4, 6, "elder", "리벨 장로", ACT_EVENT, EV_ELDER, 0, 99, new Object[][]{
                    at(0, "오오… 하늘에서 빛과 함께 떨어진 자여."),
                    at(STEP_LETTER, "편지는 에르덴 성의 왕께 전해 주게.",
                            "다리를 건너 길을 따라 북쪽으로 가면 성이 있다네."),
                    at(STEP_LINA, "견습 기사 리나와 함께라니 든든하군. 그림자 동굴은 북동쪽 산속에 있다네."),
                    at(STEP_BOSS_DOWN, "늑대 울음소리가 그쳤어! 예언은 사실이었군."),
            }),
            act(REBEL, 14, 6, "merchant", "도구점 주인", ACT_SHOP, 0,
                    at(0, "어서 와요! 여행에는 약초가 필수랍니다.")),
            act(REBEL, 4, 12, "innkeeper", "여관 주인", ACT_INN, 6,
                    at(0, "나그네의 여관에 어서 오세요.")),
            act(REBEL, 14, 12, "priest", "마을 사제", ACT_SAVE, 0,
                    at(0, "신의 가호가 함께하기를. 지금까지의 모험을 기록해 드릴까요?")),
            npc(REBEL, 9, 7, "child", "마을 아이",
                    at(0, "아저씨 하늘에서 떨어졌어? 그 가방은 뭐야? 맛있는 거 들었어?"),
                    at(STEP_LINA, "기사 누나다! 멋있다~"),
                    at(STEP_BOSS_DOWN, "이제 밤에 무서운 소리 안 나! 아저씨 최고!")),
            npc(REBEL, 12, 1, "farmer", "농부",
                    at(0, "요즘 들판에 들쥐 도적이 들끓어서 밭일을 못 하겠어.",
                            "근처 몬스터는 약하지만, 다리를 건너면 훨씬 사나워진다오."),
                    at(STEP_BOSS_DOWN, "몬스터가 줄어서 밭일이 한결 편해졌어. 고맙네!")),
            npc(REBEL, 2, 9, "villager", "할머니",
                    at(0, "여관에서 푹 쉬면 상처도 마력도 다 회복된단다.",
                            "사제님께 기도하면 모험을 기록해 주시지.")),

            // 에르덴 성
            new Npc(ERDEN, 9, 4, "king", "에르덴 왕", ACT_EVENT, EV_KING, 0, 99, new Object[][]{
                    at(0, "낯선 이로군. 성 밖은 위험하니 조심하게."),
            }),
            npc(ERDEN, 11, 5, "minister", "대신",
                    at(0, "폐하께서는 그림자 늑대 때문에 밤잠을 이루지 못하십니다."),
                    at(STEP_BOSS_DOWN, "폐하께서 오랜만에 웃으셨습니다. 어서 알현하시지요!"),
                    at(STEP_CHAPTER1_CLEAR, "항구 도시로 가는 배를 준비하고 있습니다. 조금만 기다려 주십시오.")),
            npc(ERDEN, 8, 8, "soldier", "성 병사",
                    at(0, "여기는 에르덴 성이다. 폐하께서는 안쪽 왕좌에 계신다.")),
            npc(ERDEN, 11, 8, "soldier", "성 병사",
                    at(0, "그림자 동굴의 늑대들은 무리를 지어 다닌다. 한 마리씩 노려라.")),
            new Npc(ERDEN, 7, 12, "lina", "견습 기사", ACT_EVENT, EV_LINA, 0, STEP_LETTER, new Object[][]{
                    at(0, "나는 견습 기사 리나! 기사단에 들어가려면 공을 세워야 하는데…",
                            "늑대들 때문에 성 밖으로 나가지 못하게 해요. 아, 답답해!"),
                    at(STEP_LETTER, "장로님 편지를 가져왔다고요? 어서 폐하께 가 보세요! 안쪽 왕좌에 계세요."),
            }),
            act(ERDEN, 4, 12, "merchant", "무기점 주인", ACT_SHOP, 1,
                    at(0, "에르덴 대장간에서 갓 나온 물건들이오!")),
            act(ERDEN, 16, 12, "innkeeper", "여관 주인", ACT_INN, 10,
                    at(0, "기사들도 묵어 가는 여관입니다.")),
            act(ERDEN, 14, 13, "priest", "성당 사제", ACT_SAVE, 0,
                    at(0, "용자여, 모험을 기록하시겠습니까?")),
            npc(ERDEN, 2, 13, "villager", "음유시인",
                    at(0, "♪ 하늘에서 내려온 짐꾼이 어둠을 걷어 내리라~ 오래된 노래지요."),
                    at(STEP_CHAPTER1_CLEAR, "♪ 배달부 용사가 늑대왕을 쓰러뜨렸네~ 새 노래를 지었답니다!")),

            // 그림자 동굴
            new Npc(CAVE2, 5, 4, "boss", "그림자 늑대왕", ACT_EVENT, EV_BOSS, STEP_LINA, STEP_LINA, new Object[][]{
                    at(0, "크르르… 인간 냄새. 하늘에서 떨어진 그 배달부로군.",
                            "어둠의 왕께서 깨어나실 때까지, 이 길은 아무도 지나가지 못한다!"),
            }),
    };

    // ------------------------------------------------------------ 이벤트 대사

    public static final String[] PROLOGUE_MACHINE = {
            "[신창] 이게 그 기계인가…? 버튼이 하나뿐이네.",
            "(버튼을 누르자 눈부신 빛이 방을 가득 채운다!)",
            "……",
            "(정신을 차려 보니 낯선 들판이다. 바람에서 풀 냄새가 난다.)",
            "[신창] 여긴… 어디지? 배달 가방만 남았잖아.",
            "(바로 옆에 작은 마을이 보인다. 들어가 보자.)",
    };

    public static final String[] ELDER_GIVES_LETTER = {
            "[리벨 장로] 오오… 하늘에서 빛과 함께 떨어진 자여.",
            "[리벨 장로] 이 땅 아르카디아에는 오래된 예언이 있지.",
            "[리벨 장로] '세상이 어둠에 잠길 때, 짐을 나르는 자가 용자가 되리라.'",
            "[리벨 장로] 요즘 북동쪽 동굴에서 그림자 늑대들이 몰려나와 에르덴 성으로 가는 길이 끊겼다네.",
            "[리벨 장로] 이 편지를 에르덴 왕께 전해 주겠나? 배달이라면 자신 있어 보이는군.",
            "(「장로의 편지」를 받았다! 50골드를 받았다!)",
            "[리벨 장로] 다리를 건너 길을 따라 북쪽으로 가면 성이 있네. 마을 도구점에서 단단히 준비하게.",
    };

    public static final String[] KING_LINA_JOINS = {
            "[에르덴 왕] 리벨의 장로가 보낸 편지라고? …흠, 하늘에서 온 배달부라.",
            "[에르덴 왕] 북동쪽 산속 동굴에 그림자 늑대왕이 자리를 잡은 뒤로, 짐마차가 하나도 돌아오지 못했네.",
            "[에르덴 왕] 그대가 예언의 용자라면 증명해 보이게. 견습 기사 리나를 붙여 주지.",
            "[리나] 리나라고 해요! 아직 견습이지만 검 솜씨만큼은 자신 있어요.",
            "[리나] 잘 부탁해요, 신창 씨!",
            "(리나가 동료가 되었다!)",
    };

    public static final String[] KING_WAITING = {
            "[에르덴 왕] 그림자 늑대왕은 북동쪽 산속 동굴 가장 깊은 곳에 있네. 무운을 비네.",
    };

    public static final String[] KING_BEFORE_LETTER = {
            "[에르덴 왕] 낯선 이로군. 리벨 마을의 장로와 아는 사이인가? 성 밖은 위험하니 조심하게.",
    };

    public static final String[] KING_CHAPTER_CLEAR = {
            "[에르덴 왕] 오오! 그림자 늑대왕을 쓰러뜨렸단 말인가!",
            "[에르덴 왕] 그대야말로 예언의 용자일세. 이 「용자의 증표」와 1000골드를 받게.",
            "(1000골드를 받았다! 「용자의 증표」를 받았다!)",
            "[에르덴 왕] 허나 늑대왕은 쓰러지며 '어둠의 왕이 깨어난다'고 했다지…",
            "[에르덴 왕] 바다 건너 항구 도시 메르카로 가는 배를 준비하겠네.",
            "[리나] 저도 끝까지 함께할게요. 이제 저도 정식 기사니까요!",
            "— 1장 「배달부 용사」 끝 —",
            "(2장 「상인의 길」은 다음 업데이트에서 이어집니다. 그때까지 레벨을 올려 두세요!)",
    };

    public static final String[] KING_AFTER_CLEAR = {
            "[에르덴 왕] 용자여, 항구 도시로 가는 배는 곧 준비될 걸세.",
    };

    public static final String[] BOSS_DEFEATED = {
            "[그림자 늑대왕] 크윽… 어둠의 왕께서… 깨어나신다…",
            "(그림자 늑대왕은 어둠 속으로 사라졌다)",
            "[리나] 해냈어요! 어서 에르덴 왕께 알려요!",
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
