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
    public final int[] items = new int[RpgData.ITEM_SLOTS];
    public final boolean[] chestOpened = new boolean[RpgData.CHESTS.length];
    public int townMap = RpgData.REBEL, townX = 8, townY = 11;
    public int battlesWon;
    public int stepsWalked;
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
        return ENCOUNTER_RATE * (perkDelivery ? 0.8 : 1);
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
        /** 장(챕터)이 끝났으면 true */
        public boolean chapterClear;
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
            in.lines.add("신창은 주위를 둘러보았다. 별다른 것은 없다.");
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
        if (r.linaLine != null && party.size() > 1) in.lines.add(r.linaLine);
        if (r.flag != 0) flags |= r.flag;
        if (r.item >= 0 && first) addItem(r.item, 1);
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
                    for (String l : npc.linesFor(step)) in.lines.add("[" + npc.name + "] " + l);
                    in.bossBattle = true;
                    return;
                default:
                    break;
            }
        }
        for (String l : npc.linesFor(step)) in.lines.add("[" + npc.name + "] " + l);
        in.action = npc.action == RpgData.ACT_EVENT ? RpgData.ACT_NONE : npc.action;
        in.arg = npc.arg;
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
        in.lines.add("신창은 보물상자를 열었다!");
        if (c[3] < 0) {
            gold += c[4];
            in.lines.add(c[4] + "골드를 손에 넣었다!");
        } else {
            addItem(c[3], c[4]);
            RpgData.Item item = RpgData.ITEMS[c[3]];
            in.lines.add(Josa.eul(item.name + (c[4] > 1 ? " " + c[4] + "개" : "")) + " 손에 넣었다!");
        }
    }

    /** 보스를 쓰러뜨린 뒤 */
    public List<String> onBossDefeated() {
        step = RpgData.STEP_BOSS_DOWN;
        addItem(RpgData.SEAL_SHARD, 1);
        List<String> out = new ArrayList<>();
        for (String l : RpgData.BOSS_DEFEATED) out.add(l);
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
        if (s.kind != RpgData.HEAL_ONE) return "그 주문은 전투 중에만 쓸 수 있다.";
        if (caster.mp < s.mp) return "MP가 모자라다.";
        if (!target.alive()) return Josa.eun(target.name()) + " 쓰러져 있어 여관에서 쉬어야 한다.";
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
        if (!hasItem(itemId) || (item.type != RpgData.WEAPON && item.type != RpgData.ARMOR)) return false;
        removeItem(itemId, 1);
        if (item.type == RpgData.WEAPON) {
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
        return step == RpgData.STEP_PROLOGUE ? RpgData.CHAPTER_TITLES[0] : RpgData.CHAPTER_TITLES[1];
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
                .append(';').append(flags);
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
            s.fixPosition();
            return s;
        } catch (RuntimeException e) {
            return new RpgState();
        }
    }
}
