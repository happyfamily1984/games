package com.shinchang.adventure.rpg;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 아르카디아 모험 상태: 위치, 파티, 가방, 이야기 진행, 저장.
 * 안드로이드 의존성이 없어서 PC 에서 테스트할 수 있다.
 */
public final class RpgState {

    public static final int UP = 0, RIGHT = 1, DOWN = 2, LEFT = 3;
    public static final int[] DX = {0, 1, 0, -1};
    public static final int[] DY = {-1, 0, 1, 0};

    private static final String SAVE_VERSION = "r1";
    /** 한 걸음마다 몬스터가 나올 확률 */
    public static final double ENCOUNTER_RATE = 1.0 / 14;

    public int step = RpgData.STEP_PROLOGUE;
    public int map = RpgData.OFFICE;
    public int x = RpgData.START_X, y = RpgData.START_Y;
    public int dir = UP;
    public int gold = 30;
    public final List<Hero> party = new ArrayList<>();
    /** 잠시 헤어진 동료 (3장에서 공허로 끌려간 리나·피피) */
    public final List<Hero> reserve = new ArrayList<>();
    public final int[] items = new int[RpgData.ITEM_SLOTS];
    public final boolean[] chestOpened = new boolean[RpgData.CHESTS.length];
    public int townMap = RpgData.REBEL, townX = 8, townY = 11;
    public int battlesWon;
    public int stepsWalked;
    /** 세속에서 모은 해 (저장하지 않고, 시작할 때 세속 진행에서 읽는다) */
    public long suns;
    /** 해의 제단에서 쓴 해 */
    public long sunsSpent;
    /** 보스를 쓰러뜨린 직후 이어서 시작할 전투 (저장하지 않음) */
    public int pendingBoss = -1;
    /** 찾아낸 단서 비트 (RpgData.FLAG_*) */
    public int flags;

    // 앞선 인생에서 얻은 특기 (저장하지 않고, 시작할 때 세속 진행에서 읽는다)
    public boolean perkDelivery = true;
    public boolean perkBroker;
    public boolean perkFire;
    public boolean perkFaith;

    private Random random = new Random();

    public RpgState() {
        party.add(new Hero(0, 1));
        items[RpgData.HERB] = 3;
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public Random random() {
        return random;
    }

    // ---- 특기 ----

    /** 배달 경험: 길을 잘 알아서 몬스터를 덜 만난다 */
    public double encounterRate() {
        double r = ENCOUNTER_RATE * (perkDelivery ? 0.8 : 1);
        // 흑염의 요새에는 마물이 우글거린다
        if ("fort".equals(RpgData.theme(map)) || "demon".equals(RpgData.theme(map))) r *= 2.2;
        // 4장 던전에도 마물이 많다
        String th = RpgData.theme(map);
        if ("ruin".equals(th) || "ice".equals(th) || "moon".equals(th)) r *= 1.6;
        return r;
    }

    /** 공인중개사 경험: 상점 10% 할인 */
    public int price(int itemId) {
        int p = RpgData.ITEMS[itemId].price;
        return perkBroker ? (int) Math.ceil(p * 0.9) : p;
    }

    public int sellPrice(int itemId) {
        return RpgData.ITEMS[itemId].price / 2;
    }

    /** 신앙: 회복 주문 +30% */
    public double healBonus() {
        return perkFaith ? 1.3 : 1;
    }

    /** 소방기술사 경험: 불을 잘 다룬다, 불꽃 주문 +20% */
    public double fireBonus() {
        return perkFire ? 1.2 : 1;
    }

    // ---- 지도 ----

    public String[] mapRows() {
        return RpgData.MAPS[map];
    }

    public int mapWidth() {
        return mapRows()[0].length();
    }

    public int mapHeight() {
        return mapRows().length;
    }

    public char tile(int tx, int ty) {
        String[] rows = mapRows();
        if (ty < 0 || ty >= rows.length || tx < 0 || tx >= rows[0].length()) return '#';
        char t = rows[ty].charAt(tx);
        if (t == 'C' && chestAt(map, tx, ty) >= 0 && chestOpened[chestAt(map, tx, ty)]) return 'c';
        return t;
    }

    public static int chestAt(int map, int tx, int ty) {
        for (int i = 0; i < RpgData.CHESTS.length; i++) {
            int[] c = RpgData.CHESTS[i];
            if (c[0] == map && c[1] == tx && c[2] == ty) return i;
        }
        return -1;
    }

    public RpgData.Npc npcAt(int tx, int ty) {
        for (RpgData.Npc n : RpgData.NPCS) {
            if (n.map == map && n.x == tx && n.y == ty && n.visible(step)) return n;
        }
        return null;
    }

    public List<RpgData.Npc> visibleNpcs() {
        List<RpgData.Npc> out = new ArrayList<>();
        for (RpgData.Npc n : RpgData.NPCS) if (n.map == map && n.visible(step)) out.add(n);
        return out;
    }

    public boolean walkable(int tx, int ty) {
        char t = tile(tx, ty);
        if (RpgData.blocks(t) || t == 'c') return false;
        // 해저 신전 입구는 등대의 등불을 받아야 열린다
        if (t == 'G' && step < RpgData.STEP_LAMP) return false;
        // 흑염의 요새는 결혼(각성) 뒤에 검은 불길이 걷힌다
        if (t == 'J' && step < RpgData.STEP_WEDDING) return false;
        // 요마성은 부모를 모두 되찾은 뒤에 안개가 걷힌다
        if (t == 'J' && map == RpgData.WORLD4 && step < RpgData.STEP_DAD_BACK) return false;
        return npcAt(tx, ty) == null;
    }

    // ---- 이동 ----

    /** 한 칸 걷기의 결과 */
    public static final class MoveResult {
        public boolean moved;
        public boolean warped;
        /** 몬스터가 나오면 몬스터 번호들 */
        public int[] encounter;
        public String message;
    }

    public MoveResult move(int d) {
        dir = d;
        MoveResult r = new MoveResult();
        int nx = x + DX[d], ny = y + DY[d];
        if (!walkable(nx, ny)) return r;
        x = nx;
        y = ny;
        r.moved = true;
        stepsWalked++;
        // 맹세의 반지: 걸을 때도 MP가 조금씩 찬다
        for (Hero h : party) if (h.alive() && h.mpRegen() > 0) h.mp = Math.min(h.maxMp(), h.mp + Math.max(1, h.maxMp() / 50));
        for (int[] link : RpgData.LINKS) {
            if (link[0] == map && link[1] == x && link[2] == y) {
                warp(link[3], link[4], link[5]);
                r.warped = true;
                r.message = RpgData.MAP_NAMES[map];
                return r;
            }
        }
        if (RpgData.hasEncounters(map) && tile(x, y) != 'B' && random.nextDouble() < encounterRate()) {
            int[][] groups = RpgData.ENCOUNTERS[RpgData.encounterZone(map, x, y)];
            r.encounter = groups[random.nextInt(groups.length)];
            // 가끔 진귀한 몬스터가 섞여 나온다
            int rare = RpgData.rareMonsterAt(map, x, y);
            if (rare >= 0) {
                double chance = RpgData.MONSTERS[rare].rare == RpgData.RARE_EXP ? RpgData.RARE_EXP_CHANCE : RpgData.RARE_GOLD_CHANCE;
                if (random.nextDouble() < chance) r.encounter = new int[]{rare};
            }
        }
        return r;
    }

    /** 지도가 바뀌어 예전 저장 위치가 벽 속이면, 가장 가까운 걸을 수 있는 칸으로 옮긴다 */
    void fixPosition() {
        if (x >= 0 && y >= 0 && x < mapWidth() && y < mapHeight() && walkable(x, y)) return;
        for (int r = 1; r < 40; r++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dx = -r; dx <= r; dx++) {
                    int nx = x + dx, ny = y + dy;
                    if (nx < 0 || ny < 0 || nx >= mapWidth() || ny >= mapHeight()) continue;
                    if (walkable(nx, ny) && !isLink(nx, ny)) {
                        x = nx;
                        y = ny;
                        return;
                    }
                }
            }
        }
        map = RpgData.OFFICE;
        x = RpgData.START_X;
        y = RpgData.START_Y;
    }

    private boolean isLink(int tx, int ty) {
        for (int[] l : RpgData.LINKS) if (l[0] == map && l[1] == tx && l[2] == ty) return true;
        return false;
    }

    public void warp(int toMap, int tx, int ty) {
        map = toMap;
        x = tx;
        y = ty;
        for (int[] t : RpgData.TOWN_RETURN) {
            if (t[0] == map) {
                townMap = t[0];
                townX = t[1];
                townY = t[2];
            }
        }
    }

    public int facingX() {
        return x + DX[dir];
    }

    public int facingY() {
        return y + DY[dir];
    }

    // ---- 조사 / 대화 ----

    /** 조사 결과: 보여 줄 대사와, 이어서 열 화면 */
    public static final class Interaction {
        public final List<String> lines = new ArrayList<>();
        public int action = RpgData.ACT_NONE;
        public int arg;
        /** 보스전을 시작해야 하면 true */
        public boolean bossBattle;
        /** 보스 몬스터 번호 */
        public int bossMonster = -1;
        /** 배를 타고 지도가 바뀌었으면 true */
        public boolean sailed;
        /** 장(챕터)이 끝났으면 true */
        public boolean chapterClear;
        /** 결혼으로 각성했으면 true (화면 연출) */
        public boolean awakened;
    }

    public Interaction interact() {
        Interaction in = new Interaction();
        int fx = facingX(), fy = facingY();
        RpgData.Npc npc = npcAt(fx, fy);
        // 계산대 너머의 사람과도 대화할 수 있다
        if (npc == null && tile(fx, fy) == '|') npc = npcAt(fx + DX[dir], fy + DY[dir]);
        if (npc != null) {
            talk(npc, in);
            return in;
        }
        RpgData.Readable rd = readableAt(map, fx, fy);
        if (rd != null) {
            read(rd, in);
            return in;
        }
        char t = tile(fx, fy);
        if (t == 'C') {
            openChest(chestAt(map, fx, fy), in);
        } else if (t == 'c') {
            in.lines.add("보물상자는 비어 있다.");
        } else if (t == 'W') {
            if (step == RpgData.STEP_PROLOGUE) {
                for (String l : RpgData.PROLOGUE_MACHINE) in.lines.add(l);
                step = RpgData.STEP_ARRIVED;
                warp(RpgData.WORLD, 5, 18);
            }
        } else if (RpgData.examineDefault(t) != null) {
            in.lines.add(RpgData.examineDefault(t));
        } else {
            in.lines.add(Josa.eun(party.get(0).name()) + " 주위를 둘러보았다. 별다른 것은 없다.");
        }
        return in;
    }

    public static RpgData.Readable readableAt(int map, int tx, int ty) {
        for (RpgData.Readable r : RpgData.READABLES) {
            if (r.map == map && r.x == tx && r.y == ty) return r;
        }
        return null;
    }

    public boolean hasFlag(int f) {
        return (flags & f) != 0;
    }

    private void read(RpgData.Readable r, Interaction in) {
        boolean first = r.item >= 0 ? !hasItem(r.item) : true;
        for (String l : r.lines) {
            // 아이템을 받는 줄은 처음 읽을 때만
            if (!first && l.startsWith("(「") && l.endsWith("손에 넣었다!)")) continue;
            in.lines.add(l);
        }
        if (r.linaLine != null && speakerInParty(r.linaLine)) in.lines.add(r.linaLine);
        if (r.flag != 0) flags |= r.flag;
        if (r.item >= 0 && first) addItem(r.item, 1);
    }

    /** "[리나] ..." 같은 대사는 그 사람이 일행에 있을 때만 */
    private boolean speakerInParty(String line) {
        int end = line.indexOf(']');
        if (!line.startsWith("[") || end < 0) return party.size() > 1;
        String who = line.substring(1, end);
        for (Hero h : party) if (h.name().equals(who)) return true;
        return false;
    }

    public Hero member(int heroId) {
        for (Hero h : party) if (h.id == heroId) return h;
        return null;
    }

    /** 지금까지 찾은 단서 이름들 */
    public List<String> clues() {
        List<String> out = new ArrayList<>();
        for (int i = 0; i < RpgData.CLUE_NAMES.length; i++) {
            if (hasFlag(1 << i)) out.add(RpgData.CLUE_NAMES[i]);
        }
        return out;
    }

    private void talk(RpgData.Npc npc, Interaction in) {
        if (npc.action == RpgData.ACT_EVENT) {
            switch (npc.arg) {
                case RpgData.EV_ELDER:
                    if (step == RpgData.STEP_ARRIVED) {
                        for (String l : RpgData.ELDER_GIVES_LETTER) in.lines.add(l);
                        step = RpgData.STEP_LETTER;
                        items[RpgData.LETTER] = 1;
                        gold += 50;
                        return;
                    }
                    break;
                case RpgData.EV_KING:
                    kingEvent(in);
                    return;
                case RpgData.EV_HUNTER:
                    if (step >= RpgData.STEP_BOSS_DOWN && !hasItem(RpgData.LUPUS_BELL)) {
                        for (String l : RpgData.HUNTER_GIVES_BELL) in.lines.add(l);
                        addItem(RpgData.LUPUS_BELL, 1);
                        addItem(RpgData.MANA_CANDY, 2);
                        return;
                    }
                    break;
                case RpgData.EV_BOSS:
                case RpgData.EV_MIDBOSS:
                case RpgData.EV_BOSS2:
                case RpgData.EV_GIANT:
                case RpgData.EV_GEN1:
                case RpgData.EV_GEN2:
                case RpgData.EV_GEN3:
                case RpgData.EV_VARGAS:
                    for (String l : npc.linesFor(step)) in.lines.add(l.startsWith("[") || l.startsWith("(") ? l : "[" + npc.name + "] " + l);
                    in.bossBattle = true;
                    in.bossMonster = bossFor(npc.arg);
                    return;
                case RpgData.EV_SHIP_TO_IGNA:
                    // 3장: 바다 위의 습격 (이길 수 없는 전투)
                    for (String l : RpgData.SAIL_TO_IGNA) in.lines.add(l);
                    in.bossBattle = true;
                    in.bossMonster = RpgData.VARGAS0;
                    return;
                case RpgData.EV_JIHEON:
                    if (step == RpgData.STEP_ALONE) {
                        for (String l : RpgData.JIHEON_JOINS) in.lines.add(l);
                        Hero jiheon = new Hero(3, Math.max(20, party.get(0).level - 3));
                        party.add(jiheon);
                        gold += 300;
                        step = RpgData.STEP_JIHEON;
                        return;
                    }
                    break;
                case RpgData.EV_KID:
                    if (step == RpgData.STEP_FAMILY) {
                        for (String l : RpgData.KID_LURED) in.lines.add(l);
                        step = RpgData.STEP_KIDNAPPED;
                        return;
                    }
                    break;
                case RpgData.EV_FOX1:
                    for (String l : npc.linesFor(step)) in.lines.add(l.startsWith("[") || l.startsWith("(") ? l : "[" + npc.name + "] " + l);
                    in.bossBattle = true;
                    in.bossMonster = step == RpgData.STEP_HOSTAGE ? RpgData.HOSTAGE : RpgData.FOX1;
                    return;
                case RpgData.EV_MOONLORD:
                    for (String l : npc.linesFor(step)) in.lines.add(l.startsWith("[") || l.startsWith("(") ? l : "[" + npc.name + "] " + l);
                    in.bossBattle = true;
                    in.bossMonster = RpgData.MOON_LORD;
                    return;
                case RpgData.EV_FOX2:
                case RpgData.EV_KING4:
                    for (String l : npc.linesFor(step)) in.lines.add(l.startsWith("[") || l.startsWith("(") ? l : "[" + npc.name + "] " + l);
                    in.bossBattle = true;
                    in.bossMonster = npc.arg == RpgData.EV_FOX2 ? RpgData.FOX2 : RpgData.DEMON_KING;
                    return;
                case RpgData.EV_OWL:
                    if (step == RpgData.STEP_MOM_FOUND) {
                        // 달빛 이슬은 달빛 샘에서 (보스를 쓰러뜨려야 얻는다)
                        for (String l : RpgData.MOON_QUEST) in.lines.add(l);
                        return;
                    }
                    if (step == RpgData.STEP_KING_DOWN) {
                        for (String l : RpgData.CHAPTER4_END) in.lines.add(l);
                        step = RpgData.STEP_CHAPTER4_CLEAR;
                        gold += 20000;
                        for (Hero h : party) h.fullHeal();
                        in.chapterClear = true;
                        return;
                    }
                    break;
                case RpgData.EV_ALTAR:
                    if (step == RpgData.STEP_MINHAN) {
                        for (String l : RpgData.LEGACY) in.lines.add(l);
                        for (Hero h : party) {
                            h.awakened = true;
                            h.fullHeal();
                        }
                        addItem(RpgData.DAD_BAG, 1);
                        step = RpgData.STEP_LEGACY;
                        in.awakened = true;
                        return;
                    }
                    if (step > RpgData.STEP_MINHAN) {
                        in.lines.add("[해의 제단지기] 아버지의 해로 무구를 바꾸겠나? (남은 해 " + availableSuns() + "개)");
                        in.action = RpgData.ACT_LEGACY;
                        return;
                    }
                    break;
                case RpgData.EV_DAD_STATUE:
                    if (step < RpgData.STEP_LEGACY) {
                        in.lines.add("(석상 앞의 바닥이 꿈틀거린다. 거대한 무언가가 석상을 지키고 있다)");
                        in.lines.add("(지금의 힘으로는 다가갈 수 없을 것 같다. 로아 마을의 '해의 제단'을 먼저 찾아가 보자)");
                        return;
                    }
                    if (step == RpgData.STEP_LEGACY) {
                        in.lines.add("(석상에 다가가자 땅이 흔들리며, 석상을 지키던 돌 거인이 일어섰다!)");
                        in.lines.add("[석상 수호자] …돌이 된 자를… 건드리지 마라……");
                        in.lines.add("[민한] 이 석상… 왠지 알 것 같아. 비켜!");
                        in.bossBattle = true;
                        in.bossMonster = RpgData.GUARDIAN;
                        return;
                    }
                    if (step == RpgData.STEP_MOM_BACK) {
                        for (String l : RpgData.DAD_BACK) in.lines.add(l);
                        removeItem(RpgData.MOON_DEW, 1);
                        rejoin(0);
                        step = RpgData.STEP_DAD_BACK;
                        return;
                    }
                    in.lines.add("(아빠의 석상이다. 가슴께에서 희미한 빛이 깜빡인다)");
                    in.lines.add(step >= RpgData.STEP_DEW ? "[민한] 엄마 먼저 깨우고 올게, 아빠. 조금만 기다려."
                            : "[민한] 기다려, 아빠. 꼭 되돌릴 방법을 찾을게.");
                    return;
                case RpgData.EV_MOM_STATUE:
                    if (step < RpgData.STEP_DAD_FOUND) {
                        in.lines.add("(수정이 석상을 겹겹이 감싸고 있다. 수정 너머에서 날카로운 기운이 느껴진다)");
                        in.lines.add("(아직은 다가갈 수 없을 것 같다)");
                        return;
                    }
                    if (step == RpgData.STEP_DAD_FOUND) {
                        in.lines.add("(석상에 손을 뻗자, 동굴 천장의 수정이 쩌억 갈라지며 거대한 거미가 내려왔다!)");
                        in.lines.add("[수정 여왕] 키키키… 이 예쁜 석상은… 내 거야……");
                        in.bossBattle = true;
                        in.bossMonster = RpgData.CRYSTAL_QUEEN;
                        return;
                    }
                    if (step == RpgData.STEP_DEW) {
                        for (String l : RpgData.MOM_BACK) in.lines.add(l);
                        removeItem(RpgData.MOON_DEW, 1);
                        rejoin(3);
                        step = RpgData.STEP_MOM_BACK;
                        return;
                    }
                    in.lines.add("(엄마의 석상이다. 손끝에 작은 불꽃이 깜빡인다)");
                    in.lines.add("[민한] 달빛 이슬… 숲의 부엉이 할아버지한테 물어봐야 해.");
                    return;
                case RpgData.EV_IGNA_ELDER:
                    if (step == RpgData.STEP_GIANT_DOWN) {
                        for (String l : RpgData.WEDDING) in.lines.add(l);
                        wedding();
                        in.awakened = true;
                        return;
                    }
                    if (step == RpgData.STEP_REVENGE) {
                        for (String l : RpgData.HOME) in.lines.add(l);
                        step = RpgData.STEP_HOME;
                        return;
                    }
                    break;
                case RpgData.EV_BIRTH:
                    if (step == RpgData.STEP_CHAPTER3_CLEAR) {
                        // 4장의 시작
                        for (String l : RpgData.FAMILY) in.lines.add(l);
                        step = RpgData.STEP_FAMILY;
                        return;
                    }
                    if (step == RpgData.STEP_HOME) {
                        for (String l : RpgData.BIRTH) in.lines.add(l);
                        step = RpgData.STEP_CHAPTER3_CLEAR;
                        gold += 5000;
                        for (Hero h : party) h.fullHeal();
                        in.chapterClear = true;
                        return;
                    }
                    break;
                case RpgData.EV_SHIP_TO_MERCA:
                    if (step == RpgData.STEP_CHAPTER1_CLEAR) {
                        for (String l : RpgData.SAIL_TO_MERCA_FIRST) in.lines.add(l);
                        step = RpgData.STEP_SAILED;
                    } else {
                        for (String l : RpgData.SAIL_TO_MERCA) in.lines.add(l);
                    }
                    warp(RpgData.PIER_MERCA[0], RpgData.PIER_MERCA[1], RpgData.PIER_MERCA[2]);
                    in.sailed = true;
                    return;
                case RpgData.EV_SHIP_TO_ERDEN:
                    for (String l : RpgData.SAIL_TO_ERDEN) in.lines.add(l);
                    warp(RpgData.PIER_ERDEN[0], RpgData.PIER_ERDEN[1], RpgData.PIER_ERDEN[2]);
                    in.sailed = true;
                    return;
                case RpgData.EV_GUILD:
                    guildEvent(in);
                    return;
                case RpgData.EV_PIPI:
                    if (step == RpgData.STEP_GALLEON_DOWN) {
                        for (String l : RpgData.PIPI_JOINS) in.lines.add(l);
                        removeItem(RpgData.PIPI_SCALE, 1);
                        Hero pipi = new Hero(2, Math.max(15, party.get(0).level - 1));
                        party.add(pipi);
                        step = RpgData.STEP_PIPI;
                        return;
                    }
                    if (step == RpgData.STEP_GUILD_REFUSED) {
                        String[] lines = hasFlagSeen(RpgData.PIPI_QUEST) ? RpgData.PIPI_WAITING : RpgData.PIPI_QUEST;
                        for (String l : lines) in.lines.add(l);
                        return;
                    }
                    break;
                case RpgData.EV_NOAH:
                    if (step == RpgData.STEP_PIPI) {
                        for (String l : RpgData.NOAH_GIVES_LAMP) in.lines.add(l);
                        addItem(RpgData.LIGHTHOUSE_LAMP, 1);
                        step = RpgData.STEP_LAMP;
                        return;
                    }
                    break;
                default:
                    break;
            }
        }
        for (String l : npc.linesFor(step)) in.lines.add("[" + npc.name + "] " + l);
        in.action = npc.action == RpgData.ACT_EVENT ? RpgData.ACT_NONE : npc.action;
        in.arg = npc.arg;
    }

    /** 해의 제단에서 쓸 수 있는 해 */
    public long availableSuns() {
        return Math.max(0, suns - sunsSpent);
    }

    public static int legacyPrice(int itemId) {
        for (int[] g : RpgData.LEGACY_GOODS) if (g[0] == itemId) return g[1];
        return -1;
    }

    /** 해로 전설의 무구 사기 */
    public boolean buyLegacy(int itemId) {
        int p = legacyPrice(itemId);
        if (p < 0 || availableSuns() < p) return false;
        sunsSpent += p;
        addItem(itemId, 1);
        return true;
    }

    /** 헤어졌던 동료가 돌아온다 */
    private void rejoin(int heroId) {
        for (int i = 0; i < reserve.size(); i++) {
            Hero h = reserve.get(i);
            if (h.id == heroId) {
                reserve.remove(i);
                h.fullHeal();
                party.add(h);
                return;
            }
        }
    }

    /** 인질극에서 쓰러지면: 신창과 지헌은 석상이 되어 흩어지고, 민한은 먼 숲에서 자란다 */
    public List<String> onPetrified() {
        List<String> out = new ArrayList<>();
        for (String l : RpgData.PETRIFIED) out.add(l);
        for (Hero h : party) {
            h.fullHeal();
            reserve.add(h);
        }
        party.clear();
        Hero minhan = new Hero(4, 22);
        Hero haru = new Hero(5, 22);
        party.add(minhan);
        party.add(haru);
        gold = 500;
        addItem(RpgData.HI_HERB, 5);
        step = RpgData.STEP_MINHAN;
        warp(RpgData.BEASTDEN, 11, 8);
        return out;
    }

    static int bossFor(int event) {
        switch (event) {
            case RpgData.EV_BOSS: return RpgData.WOLF_KING;
            case RpgData.EV_MIDBOSS: return RpgData.GALLEON;
            case RpgData.EV_BOSS2: return RpgData.SERPENT;
            case RpgData.EV_GIANT: return RpgData.GIANT;
            case RpgData.EV_GEN1: return RpgData.IGRON;
            case RpgData.EV_GEN2: return RpgData.WITCH;
            case RpgData.EV_GEN3: return RpgData.VOLKA;
            default: return RpgData.VARGAS;
        }
    }

    /** 바다 위에서 바르가스에게 지면: 동료를 잃고, 가진 것을 모두 잃고, 이그나에서 눈을 뜬다 */
    public List<String> onAmbush() {
        List<String> out = new ArrayList<>();
        for (String l : RpgData.AMBUSH_AFTER) out.add(l);
        Hero hero = party.get(0);
        reserve.clear();
        for (int i = 1; i < party.size(); i++) reserve.add(party.get(i));
        party.clear();
        party.add(hero);
        hero.weapon = RpgData.STICK;
        hero.armor = RpgData.CLOTHES;
        for (int i = 0; i < items.length; i++) {
            RpgData.Item it = RpgData.ITEMS[i];
            if (it == null || it.type != RpgData.KEY) items[i] = 0;
        }
        gold = 0;
        hero.hp = Math.max(1, hero.maxHp() / 3);
        hero.mp = 0;
        step = RpgData.STEP_ALONE;
        warp(RpgData.IGNA, 5, 4);
        out.add("— " + RpgData.CHAPTER_TITLES[3] + " —");
        for (String l : RpgData.WAKE_UP) out.add(l);
        return out;
    }

    /** 혼례: 신창은 용사로 전직하고, 지헌은 불의 축복을 받는다 */
    private void wedding() {
        step = RpgData.STEP_WEDDING;
        removeItem(RpgData.FLAME_ORE, 1);
        Hero hero = party.get(0);
        hero.awakened = true;
        addItem(RpgData.HERO_SWORD, 1);
        addItem(RpgData.PHOENIX_ARMOR, 1);
        addItem(RpgData.VOW_RING, 2);
        equip(hero, RpgData.HERO_SWORD);
        equip(hero, RpgData.PHOENIX_ARMOR);
        equip(hero, RpgData.VOW_RING);
        Hero jiheon = member(3);
        if (jiheon != null) {
            jiheon.awakened = true;
            addItem(RpgData.BRIDE_DRESS, 1);
            equip(jiheon, RpgData.BRIDE_DRESS);
            equip(jiheon, RpgData.VOW_RING);
        }
        for (Hero h : party) h.fullHeal();
    }

    private void kingEvent(Interaction in) {
        String[] lines;
        switch (step) {
            case RpgData.STEP_LETTER:
                lines = RpgData.KING_LINA_JOINS;
                step = RpgData.STEP_LINA;
                items[RpgData.LETTER] = 0;
                Hero lina = new Hero(1, Math.max(3, party.get(0).level));
                party.add(lina);
                break;
            case RpgData.STEP_LINA:
                lines = RpgData.KING_WAITING;
                break;
            case RpgData.STEP_BOSS_DOWN:
                lines = concat(RpgData.KING_CLEAR_OPEN,
                        hasFlag(RpgData.FLAG_DIARY) ? RpgData.KING_CLEAR_DIARY : RpgData.KING_CLEAR_NO_DIARY,
                        RpgData.KING_CLEAR_CLOSE);
                step = RpgData.STEP_CHAPTER1_CLEAR;
                gold += 1000;
                items[RpgData.HERO_BADGE] = 1;
                in.chapterClear = true;
                break;
            case RpgData.STEP_CHAPTER1_CLEAR:
                lines = RpgData.KING_AFTER_CLEAR;
                break;
            default:
                lines = RpgData.KING_BEFORE_LETTER;
                break;
        }
        for (String l : lines) in.lines.add(l);
    }

    private static String[] concat(String[]... parts) {
        List<String> out = new ArrayList<>();
        for (String[] p : parts) for (String l : p) out.add(l);
        return out.toArray(new String[0]);
    }

    private void openChest(int index, Interaction in) {
        if (index < 0 || chestOpened[index]) return;
        chestOpened[index] = true;
        int[] c = RpgData.CHESTS[index];
        in.lines.add(Josa.eun(party.get(0).name()) + " 보물상자를 열었다!");
        if (c[3] < 0) {
            gold += c[4];
            in.lines.add(c[4] + "골드를 손에 넣었다!");
        } else {
            addItem(c[3], c[4]);
            RpgData.Item item = RpgData.ITEMS[c[3]];
            in.lines.add(Josa.eul(item.name + (c[4] > 1 ? " " + c[4] + "개" : "")) + " 손에 넣었다!");
        }
    }

    /** 같은 긴 대사를 두 번 보여 주지 않으려고, 한 번 본 대사 묶음을 기억한다 (저장하지 않음) */
    private final java.util.Set<String[]> seen = new java.util.HashSet<>();

    private boolean hasFlagSeen(String[] lines) {
        return !seen.add(lines);
    }

    private void guildEvent(Interaction in) {
        String[] lines;
        switch (step) {
            case RpgData.STEP_SAILED:
                lines = RpgData.GUILD_FIRST;
                step = RpgData.STEP_GUILD_REFUSED;
                break;
            case RpgData.STEP_SERPENT_DOWN:
                lines = concat(RpgData.GUILD_CLEAR_OPEN,
                        hasItem(RpgData.BAROS_COMPASS) ? RpgData.GUILD_CLEAR_COMPASS : RpgData.GUILD_CLEAR_NO_COMPASS,
                        RpgData.GUILD_CLEAR_CLOSE);
                removeItem(RpgData.BAROS_COMPASS, 1);
                step = RpgData.STEP_CHAPTER2_CLEAR;
                gold += 5000;
                in.chapterClear = true;
                break;
            case RpgData.STEP_CHAPTER2_CLEAR:
                lines = RpgData.GUILD_AFTER_CLEAR;
                break;
            default:
                lines = RpgData.GUILD_WAITING;
                break;
        }
        for (String l : lines) in.lines.add(l);
    }

    /** 보스를 쓰러뜨린 뒤 */
    public List<String> onBossDefeated() {
        return onBossDefeated(RpgData.WOLF_KING);
    }

    public List<String> onBossDefeated(int monster) {
        String[] lines;
        if (monster == RpgData.FOX1) {
            step = RpgData.STEP_HOSTAGE;
            pendingBoss = RpgData.HOSTAGE;
            lines = RpgData.FOX1_DEFEATED;
        } else if (monster == RpgData.GUARDIAN) {
            step = RpgData.STEP_DAD_FOUND;
            lines = RpgData.DAD_FOUND;
        } else if (monster == RpgData.CRYSTAL_QUEEN) {
            step = RpgData.STEP_MOM_FOUND;
            lines = RpgData.MOM_FOUND;
        } else if (monster == RpgData.MOON_LORD) {
            step = RpgData.STEP_DEW;
            addItem(RpgData.MOON_DEW, 2);
            lines = RpgData.MOONLORD_DEFEATED;
        } else if (monster == RpgData.FOX2) {
            step = RpgData.STEP_FOX_DOWN;
            lines = RpgData.FOX2_DEFEATED;
        } else if (monster == RpgData.DEMON_KING) {
            step = RpgData.STEP_KING_DOWN;
            lines = RpgData.KING4_DEFEATED;
        } else if (monster == RpgData.GIANT) {
            step = RpgData.STEP_GIANT_DOWN;
            addItem(RpgData.FLAME_ORE, 1);
            addItem(RpgData.DOGEON_HAMMER, 1);
            lines = RpgData.GIANT_DEFEATED;
        } else if (monster == RpgData.IGRON) {
            step = RpgData.STEP_GEN1;
            lines = RpgData.GEN1_DEFEATED;
        } else if (monster == RpgData.WITCH) {
            step = RpgData.STEP_GEN2;
            lines = RpgData.GEN2_DEFEATED;
        } else if (monster == RpgData.VOLKA) {
            step = RpgData.STEP_GEN3;
            lines = RpgData.GEN3_DEFEATED;
        } else if (monster == RpgData.VARGAS) {
            step = RpgData.STEP_REVENGE;
            addItem(RpgData.FIRE_SHARD, 1);
            lines = RpgData.VARGAS_DEFEATED;
        } else if (monster == RpgData.GALLEON) {
            step = RpgData.STEP_GALLEON_DOWN;
            addItem(RpgData.PIPI_SCALE, 1);
            lines = RpgData.GALLEON_DEFEATED;
        } else if (monster == RpgData.SERPENT) {
            step = RpgData.STEP_SERPENT_DOWN;
            addItem(RpgData.SEA_SHARD, 1);
            lines = RpgData.SERPENT_DEFEATED;
        } else {
            step = RpgData.STEP_BOSS_DOWN;
            addItem(RpgData.SEAL_SHARD, 1);
            lines = RpgData.BOSS_DEFEATED;
        }
        List<String> out = new ArrayList<>();
        for (String l : lines) out.add(l);
        return out;
    }

    // ---- 가방 ----

    public boolean hasItem(int id) {
        return items[id] > 0;
    }

    public void addItem(int id, int count) {
        items[id] = Math.min(99, items[id] + count);
    }

    public void removeItem(int id, int count) {
        items[id] = Math.max(0, items[id] - count);
    }

    /** 가방에 있는 아이템 번호들 (중요한 물건 포함) */
    public List<Integer> bagItems() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < items.length; i++) if (items[i] > 0 && RpgData.ITEMS[i] != null) out.add(i);
        return out;
    }

    /** 약초·마나 사탕 효과. 결과 메시지를 돌려준다. */
    public String applyConsumable(int id, Hero target) {
        RpgData.Item item = RpgData.ITEMS[id];
        if (id == RpgData.HERB) {
            if (!target.alive()) return Josa.eun(target.name()) + " 쓰러져 있어 약초가 듣지 않는다.";
            int before = target.hp;
            target.hp = Math.min(target.maxHp(), target.hp + item.power + random.nextInt(8));
            return Josa.i(target.name()) + " HP를 " + (target.hp - before) + " 회복했다.";
        }
        if (id == RpgData.HI_HERB) {
            if (!target.alive()) return Josa.eun(target.name()) + " 쓰러져 있어 약초가 듣지 않는다.";
            int before = target.hp;
            target.hp = Math.min(target.maxHp(), target.hp + item.power + random.nextInt(20));
            return Josa.i(target.name()) + " HP를 " + (target.hp - before) + " 회복했다.";
        }
        if (id == RpgData.PHOENIX) {
            if (target.alive()) return Josa.eun(target.name()) + " 쓰러져 있지 않다. 깃털이 바람에 날아갔다…";
            target.hp = Math.max(1, target.maxHp() / 2);
            return Josa.i(target.name()) + " 되살아났다!";
        }
        if (id == RpgData.ELIXIR) {
            if (!target.alive()) return Josa.eun(target.name()) + " 쓰러져 있어 엘릭서가 듣지 않는다.";
            target.fullHeal();
            return Josa.i(target.name()) + " HP와 MP를 모두 회복했다!";
        }
        if (id == RpgData.MANA_CANDY) {
            int before = target.mp;
            target.mp = Math.min(target.maxMp(), target.mp + item.power);
            return Josa.i(target.name()) + " MP를 " + (target.mp - before) + " 회복했다.";
        }
        return "아무 일도 일어나지 않았다.";
    }

    /** 필드에서 아이템 쓰기 */
    public String useItemInField(int id, Hero target) {
        RpgData.Item item = RpgData.ITEMS[id];
        if (!hasItem(id)) return Josa.i(item.name) + " 없다.";
        if (item.type == RpgData.KEY) return item.desc + ".";
        if (item.type != RpgData.CONSUMABLE) return "장비는 「장비」 메뉴에서 바꿀 수 있다.";
        if (id == RpgData.WING) {
            if (map == RpgData.OFFICE) return "여기서는 쓸 수 없다.";
            removeItem(id, 1);
            warp(townMap, townX, townY);
            return "날개깃을 하늘 높이 던졌다! " + RpgData.MAP_NAMES[map] + "(으)로 돌아왔다.";
        }
        removeItem(id, 1);
        return applyConsumable(id, target);
    }

    /** 필드에서 회복 주문 쓰기 */
    public String castInField(Hero caster, int spellIndex, Hero target) {
        RpgData.Spell s = RpgData.SPELLS[spellIndex];
        if (!s.fieldUsable()) return "그 주문은 전투 중에만 쓸 수 있다.";
        if (caster.mp < s.mp) return "MP가 모자라다.";
        String used = Josa.eun(caster.name()) + " 「" + s.name + "」" + Josa.eulOf(s.name) + " 썼다! ";
        if (s.kind == RpgData.REVIVE) {
            if (target.alive()) return Josa.eun(target.name()) + " 쓰러져 있지 않다.";
            caster.mp -= s.mp;
            target.hp = Math.max(1, target.maxHp() * s.power / 100);
            return used + Josa.i(target.name()) + " 되살아났다!";
        }
        if (s.kind == RpgData.HEAL_ALL) {
            caster.mp -= s.mp;
            for (Hero h : party) if (h.alive()) h.hp = Math.min(h.maxHp(), h.hp + (int) Math.round(s.power * healBonus()));
            return used + "일행의 HP가 회복되었다.";
        }
        if (!target.alive()) return Josa.eun(target.name()) + " 쓰러져 있다. 되살리는 주문이나 소생의 깃털이 필요하다.";
        caster.mp -= s.mp;
        int before = target.hp;
        target.hp = Math.min(target.maxHp(), target.hp + (int) Math.round(s.power * healBonus()));
        return Josa.eun(caster.name()) + " 「" + s.name + "」" + Josa.eulOf(s.name) + " 썼다! "
                + Josa.i(target.name()) + " HP를 " + (target.hp - before) + " 회복했다.";
    }

    // ---- 상점 · 여관 · 장비 ----

    public boolean buy(int itemId) {
        int p = price(itemId);
        if (gold < p) return false;
        gold -= p;
        addItem(itemId, 1);
        return true;
    }

    /** 장착 중인 장비는 팔 수 없다 */
    public boolean canSell(int itemId) {
        RpgData.Item item = RpgData.ITEMS[itemId];
        return item != null && item.type != RpgData.KEY && items[itemId] > 0 && item.price > 0;
    }

    public boolean sell(int itemId) {
        if (!canSell(itemId)) return false;
        removeItem(itemId, 1);
        gold += sellPrice(itemId);
        return true;
    }

    /** 가방의 장비를 장착하고, 벗은 장비는 가방으로 */
    public boolean equip(Hero h, int itemId) {
        RpgData.Item item = RpgData.ITEMS[itemId];
        if (!hasItem(itemId) || (item.type != RpgData.WEAPON && item.type != RpgData.ARMOR && item.type != RpgData.ACCESSORY)) return false;
        removeItem(itemId, 1);
        if (item.type == RpgData.ACCESSORY) {
            if (h.accessory >= 0) addItem(h.accessory, 1);
            h.accessory = itemId;
        } else if (item.type == RpgData.WEAPON) {
            addItem(h.weapon, 1);
            h.weapon = itemId;
        } else {
            addItem(h.armor, 1);
            h.armor = itemId;
        }
        return true;
    }

    public int innPrice(int perPerson) {
        return perPerson * party.size();
    }

    public boolean rest(int perPerson) {
        int p = innPrice(perPerson);
        if (gold < p) return false;
        gold -= p;
        for (Hero h : party) h.fullHeal();
        return true;
    }

    /** 전멸: 골드 절반을 잃고 마지막 마을에서 눈을 뜬다 */
    public void defeated() {
        gold /= 2;
        warp(townMap, townX, townY);
        for (Hero h : party) h.fullHeal();
    }

    public String chapterTitle() {
        return RpgData.CHAPTER_TITLES[RpgData.chapterOf(step)];
    }

    // ---- 저장 ----

    public String serialize() {
        StringBuilder sb = new StringBuilder(SAVE_VERSION);
        sb.append(';').append(step).append(';').append(map).append(';').append(x).append(';').append(y)
                .append(';').append(dir).append(';').append(gold).append(';');
        for (int i = 0; i < party.size(); i++) {
            if (i > 0) sb.append('/');
            sb.append(party.get(i).serialize());
        }
        sb.append(';');
        for (int i = 0; i < items.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(items[i]);
        }
        sb.append(';');
        for (boolean b : chestOpened) sb.append(b ? '1' : '0');
        sb.append(';').append(townMap).append(';').append(townX).append(';').append(townY)
                .append(';').append(battlesWon).append(';').append(stepsWalked)
                .append(';').append(flags).append(';');
        for (int i = 0; i < reserve.size(); i++) {
            if (i > 0) sb.append('/');
            sb.append(reserve.get(i).serialize());
        }
        sb.append(';').append(sunsSpent);
        return sb.toString();
    }

    public static RpgState deserialize(String data) {
        RpgState s = new RpgState();
        if (data == null || data.isEmpty()) return s;
        try {
            String[] p = data.split(";", -1);
            if (!SAVE_VERSION.equals(p[0])) return new RpgState();
            s.step = Integer.parseInt(p[1]);
            s.map = Math.max(0, Math.min(RpgData.MAPS.length - 1, Integer.parseInt(p[2])));
            s.x = Integer.parseInt(p[3]);
            s.y = Integer.parseInt(p[4]);
            s.dir = Integer.parseInt(p[5]) & 3;
            s.gold = Math.max(0, Integer.parseInt(p[6]));
            s.party.clear();
            for (String h : p[7].split("/")) s.party.add(Hero.deserialize(h));
            String[] it = p[8].split(",");
            for (int i = 0; i < it.length && i < s.items.length; i++) s.items[i] = Math.max(0, Integer.parseInt(it[i]));
            for (int i = 0; i < p[9].length() && i < s.chestOpened.length; i++) s.chestOpened[i] = p[9].charAt(i) == '1';
            s.townMap = Integer.parseInt(p[10]);
            s.townX = Integer.parseInt(p[11]);
            s.townY = Integer.parseInt(p[12]);
            for (int[] t : RpgData.TOWN_RETURN) {
                if (t[0] == s.townMap) {
                    s.townX = t[1];
                    s.townY = t[2];
                }
            }
            s.battlesWon = Integer.parseInt(p[13]);
            s.stepsWalked = Integer.parseInt(p[14]);
            if (p.length > 15) s.flags = Integer.parseInt(p[15]);
            if (p.length > 16 && !p[16].isEmpty()) for (String h : p[16].split("/")) s.reserve.add(Hero.deserialize(h));
            if (p.length > 17) s.sunsSpent = Long.parseLong(p[17]);
            s.fixPosition();
            return s;
        } catch (RuntimeException e) {
            return new RpgState();
        }
    }
}
