// 실행: javac -encoding UTF-8 -d out app/src/main/java/com/shinchang/adventure/rpg/{RpgData,RpgMaps,RpgState,Hero,Battle,Josa}.java tools/BossSim.java tools/RpgPlay.java && java -cp out RpgPlay
import com.shinchang.adventure.rpg.*;
import java.util.*;

/** 자동 플레이: 길찾기로 걸어가며 전투는 BossSim 의 AI 로 진행해 1장과 2장을 끝까지 깬다. */
public class RpgPlay {
    static RpgState s; static Random rnd = new Random(Long.getLong("seed", 7));
    static int battles = 0, defeats = 0, rests = 0, rareExp = 0, rareGold = 0; static double seconds = 0;
    static int[] levelAt = new int[45];
    static Set<String> once = new HashSet<>();

    static void ok(boolean c, String m) { if (!c) throw new AssertionError(m + "  @" + RpgData.MAP_NAMES[s.map] + " (" + s.x + "," + s.y + ") step " + s.step); if (once.add(m)) System.out.println("✓ " + m); }

    // ---- 길찾기 ----
    static List<Integer> path(int tx, int ty, boolean adjacent) {
        int w = s.mapWidth(), h = s.mapHeight(); int[][] prev = new int[h][w]; for (int[] r : prev) Arrays.fill(r, -1);
        ArrayDeque<int[]> q = new ArrayDeque<>(); q.add(new int[]{s.x, s.y}); prev[s.y][s.x] = 9;
        while (!q.isEmpty()) { int[] c = q.poll();
            boolean done = adjacent ? Math.abs(c[0] - tx) + Math.abs(c[1] - ty) == 1 : (c[0] == tx && c[1] == ty);
            if (done) { LinkedList<Integer> out = new LinkedList<>(); int cx = c[0], cy = c[1];
                while (prev[cy][cx] != 9) { int d = prev[cy][cx]; out.addFirst(d); cx -= RpgState.DX[d]; cy -= RpgState.DY[d]; } return out; }
            for (int d = 0; d < 4; d++) { int nx = c[0] + RpgState.DX[d], ny = c[1] + RpgState.DY[d];
                if (nx < 0 || ny < 0 || nx >= w || ny >= h || prev[ny][nx] != -1) continue;
                boolean target = !adjacent && nx == tx && ny == ty;
                if (!s.walkable(nx, ny) && !target) continue;
                if (!target && isLink(nx, ny)) continue;
                prev[ny][nx] = d; q.add(new int[]{nx, ny}); } }
        return null;
    }
    static boolean isLink(int x, int y) { for (int[] l : RpgData.LINKS) if (l[0] == s.map && l[1] == x && l[2] == y) return true; return false; }

    static void walkTo(int tx, int ty, boolean adjacent) {
        for (int guard = 0; guard < 300; guard++) {
            List<Integer> p = path(tx, ty, adjacent);
            if (p == null) throw new AssertionError("길 없음: " + RpgData.MAP_NAMES[s.map] + " (" + s.x + "," + s.y + ") → (" + tx + "," + ty + ")");
            if (p.isEmpty()) { if (adjacent) for (int d = 0; d < 4; d++) if (s.x + RpgState.DX[d] == tx && s.y + RpgState.DY[d] == ty) s.dir = d; return; }
            int startMap = s.map;
            for (int d : p) { RpgState.MoveResult r = s.move(d); seconds += 0.25;
                if (r.encounter != null) { fight(r.encounter, false); if (s.map != startMap) return; break; }
                if (r.warped) return; }

        }
        throw new AssertionError("너무 오래 걸림");
    }
    static boolean lowHp() { int down = 0, mp = 0, maxMp = 0; for (Hero h : s.party) { if (h.hp < h.maxHp() * 0.35) return true; if (!h.alive()) down++; mp += h.mp; maxMp += h.maxMp(); } return down > 0 || mp < maxMp * 0.15; }

    // ---- 전투 ----
    static boolean fight(int[] ids, boolean boss) {
        battles++; Battle b = new Battle(s, ids, boss, rnd); int r = 0;
        int rare = RpgData.MONSTERS[ids[0]].rare;
        int goldBefore = s.gold, expBefore = s.party.get(0).exp;
        while (!b.over()) { b.playRound(BossSim.ai(s, b, r++)); seconds += boss ? 8 : 5; }
        if (rare == RpgData.RARE_EXP && b.won() && s.party.get(0).exp > expBefore + 300) rareExp++;
        if (rare == RpgData.RARE_GOLD && b.won() && s.gold > goldBefore + 300) rareGold++;
        for (Hero h : s.party) if (h.id == 0 && levelAt[h.level] == 0) levelAt[h.level] = (int) Math.round(seconds / 60);
        if (b.lost()) { defeats++; s.defeated(); return false; }
        s.battlesWon++; return true;
    }

    // ---- 마을 ----
    static boolean isTown(int m) { return m == RpgData.REBEL || m == RpgData.ERDEN || m == RpgData.MERCA || m == RpgData.SALT; }
    static boolean ch2(int m) { return m >= RpgData.WORLD2; }
    static void exitTown() {
        switch (s.map) {
            case RpgData.REBEL: walkTo(12, 21, false); break;
            case RpgData.ERDEN: walkTo(13, 27, false); break;
            case RpgData.MERCA: walkTo(0, 11, false); break;
            case RpgData.SALT: walkTo(11, 0, false); break;
            case RpgData.LIGHTHOUSE: walkTo(6, 12, false); break;
            default: break;
        }
    }
    static void sail() { RpgData.Npc cap = null; for (RpgData.Npc n : s.visibleNpcs()) if (n.sprite.equals("captain")) cap = n; walkTo(cap.x, cap.y, true); s.interact(); }
    static void goToTown() {
        int guard = 0;
        while (s.map != s.townMap && guard++ < 80) {
            if (s.hasItem(RpgData.WING) && !isTown(s.map)) { s.useItemInField(RpgData.WING, s.party.get(0)); continue; }
            switch (s.map) {
                case RpgData.CAVE2: walkTo(10, 13, false); break;
                case RpgData.CAVE1: walkTo(10, 14, false); break;
                case RpgData.SPRAY2: walkTo(11, 13, false); break;
                case RpgData.SPRAY1: walkTo(11, 14, false); break;
                case RpgData.TEMPLE2: walkTo(12, 13, false); break;
                case RpgData.TEMPLE1: walkTo(11, 14, false); break;
                case RpgData.WORLD:
                    if (s.townMap == RpgData.REBEL) walkTo(6, 18, false); else walkTo(20, 8, false); break;
                case RpgData.WORLD2:
                    if (s.townMap == RpgData.SALT) walkTo(28, 19, false); else walkTo(5, 17, false); break;
                default:
                    if (s.map == RpgData.ERDEN && ch2(s.townMap)) sail();
                    else if (s.map == RpgData.MERCA && !ch2(s.townMap)) sail();
                    else exitTown();
            }
        }
    }
    /** 계산대 너머 사람과도 말할 수 있게 서는 위치를 고른다 */
    static RpgState.Interaction talkNpc(RpgData.Npc n) {
        char below = s.tile(n.x, n.y + 1), above = s.tile(n.x, n.y - 1);
        if (below == '|') { walkTo(n.x, n.y + 2, false); s.dir = RpgState.UP; }
        else if (above == '|') { walkTo(n.x, n.y - 2, false); s.dir = RpgState.DOWN; }
        else walkTo(n.x, n.y, true);
        return s.interact();
    }
    static RpgData.Npc npc(String name) { for (RpgData.Npc n : s.visibleNpcs()) if (n.name.equals(name)) return n; throw new AssertionError("없는 사람: " + name); }
    static RpgData.Npc npcWith(int action) { for (RpgData.Npc n : s.visibleNpcs()) if (n.action == action) return n; return null; }
    static void rest() {
        goToTown(); RpgData.Npc n = npcWith(RpgData.ACT_INN);
        RpgState.Interaction in = talkNpc(n); ok(in.action == RpgData.ACT_INN, RpgData.MAP_NAMES[s.map] + " 여관 주인과 대화");
        if (!s.rest(n.arg)) for (Hero h : s.party) h.fullHeal();
        rests++;
    }
    static void shop(int[] buy) {
        RpgState.Interaction in = talkNpc(npcWith(RpgData.ACT_SHOP)); ok(in.action == RpgData.ACT_SHOP, RpgData.MAP_NAMES[s.map] + " 상점과 대화");
        for (int id : buy) {
            RpgData.Item it = RpgData.ITEMS[id];
            if (s.gold < s.price(id)) continue;
            if (it.type != RpgData.CONSUMABLE) {
                Hero best = null; int gain = 0;
                for (Hero h : s.party) { int cur = it.type == RpgData.WEAPON ? RpgData.ITEMS[h.weapon].power : RpgData.ITEMS[h.armor].power; if (it.power - cur > gain) { gain = it.power - cur; best = h; } }
                if (best == null) continue;
                s.buy(id); s.equip(best, id);
            } else s.buy(id);
        }
    }
    static void checkNpcs() {
        for (RpgData.Npc n : s.visibleNpcs()) {
            boolean counter = s.tile(n.x, n.y + 1) == '|' || s.tile(n.x, n.y - 1) == '|';
            boolean reach = path(n.x, n.y, true) != null || (counter && (path(n.x, n.y + 2, false) != null || path(n.x, n.y - 2, false) != null));
            ok(reach, RpgData.MAP_NAMES[s.map] + "의 " + n.name + "에게 갈 수 있음");
        }
    }
    static void read(int x, int y) { walkTo(x, y, true); s.interact(); }

    // ---- 레벨업 ----
    static int lv() { return s.party.get(0).level; }
    static void grindWorld(int cx, int cy, int target) {
        int world = s.map;
        while (lv() < target) {
            if (lowHp()) { rest(); exitTown(); }
            if (s.map != world) { if (isTown(s.map)) exitTown(); else goToTown(); continue; }
            int bx = cx + rnd.nextInt(7) - 3, by = cy + rnd.nextInt(5) - 2;
            if (s.walkable(bx, by) && !isLink(bx, by) && path(bx, by, false) != null) walkTo(bx, by, false);
        }
    }
    /** 던전 안에서 계단 근처를 돌며 강한 적을 사냥한다 */
    static void dungeonGrind(Runnable enter, int dungeon, int cx, int cy, int target) {
        while (lv() < target) {
            if (s.map != dungeon || lowHp()) { rest(); enter.run(); }
            int bx = cx + rnd.nextInt(7) - 3, by = cy;
            if (s.walkable(bx, by) && !isLink(bx, by) && path(bx, by, false) != null) walkTo(bx, by, false);
        }
    }
    static String party() { StringBuilder b = new StringBuilder(); for (Hero h : s.party) b.append(h.name()).append(" Lv").append(h.level).append(" ");
        return b + "| " + s.gold + "G | 전투 " + battles + ", 전멸 " + defeats + ", " + String.format("%.0f분", seconds / 60); }

    // ---- 던전 들어가기 ----
    static void enterCave2() { goToTown(); if (s.map == RpgData.REBEL || s.map == RpgData.ERDEN) exitTown(); walkTo(28, 3, false); while (s.map == RpgData.CAVE1) walkTo(4, 12, false); }
    static void enterSpray2() { goToTown(); exitTown(); walkTo(7, 2, false); while (s.map == RpgData.SPRAY1) walkTo(4, 11, false); }
    static void enterTemple2() { goToTown(); exitTown(); walkTo(33, 3, false); while (s.map == RpgData.TEMPLE1) walkTo(6, 8, false); }

    static void bossFight(Runnable enter, int dungeon, int nx, int ny, int doneStep, int gcx, int gcy) {
        int attempts = 0;
        while (s.step < doneStep && attempts++ < 10) {
            rest(); restock(); enter.run(); // 결전 전에는 언제나 여관에서 쉬고 장비와 약을 채운다
            walkTo(nx, ny, true);
            if (s.map != dungeon) continue;
            RpgState.Interaction in = s.interact();
            ok(in.bossBattle, RpgData.MONSTERS[in.bossMonster].name + " 결전 시작");
            boolean won = fight(new int[]{in.bossMonster}, true);
            if (won) { s.onBossDefeated(in.bossMonster); break; }
            System.out.println("   " + RpgData.MONSTERS[in.bossMonster].name + "에게 짐 → 던전에서 강적 사냥 후 재도전 " + party());
            dungeonGrind(enter, dungeon, gcx, gcy, lv() + 1);
        }
        ok(s.step >= doneStep, "보스 격파 (시도 " + attempts + "번) " + party());
    }

    public static void main(String[] a) {
        s = new RpgState(); s.setRandom(rnd); s.perkBroker = s.perkFire = s.perkFaith = true;

        // ===================== 프롤로그 · 1장
        ok(s.map == RpgData.OFFICE, "집무실에서 시작");
        read(15, 4); ok(s.hasFlag(RpgData.FLAG_RECORD), "금고 기록 상자");
        walkTo(15, 2, true); s.interact(); ok(s.map == RpgData.WORLD && s.step == RpgData.STEP_ARRIVED, "차원 이동 장치 → 아르카디아");
        walkTo(6, 18, false); ok(s.map == RpgData.REBEL, "리벨 마을");
        checkNpcs();
        talkNpc(npc("리벨 장로")); ok(s.step == RpgData.STEP_LETTER, "장로의 편지");
        read(3, 3); read(17, 20);
        shop(new int[]{RpgData.COPPER_SWORD, RpgData.LEATHER, RpgData.HERB, RpgData.HERB});
        exitTown(); grindWorld(8, 16, 5);
        walkTo(20, 8, false); ok(s.map == RpgData.ERDEN, "에르덴 성"); checkNpcs();
        read(5, 2);
        talkNpc(npc("에르덴 왕")); ok(s.party.size() == 2, "리나 합류");
        exitTown(); grindWorld(20, 10, 9); rest();
        shop(new int[]{RpgData.IRON_SWORD, RpgData.CHAIN_MAIL, RpgData.HERB, RpgData.HERB, RpgData.HERB, RpgData.WING});
        exitTown(); walkTo(28, 3, false);
        read(20, 1); read(11, 4);
        while (s.map == RpgData.CAVE1) walkTo(4, 12, false);
        read(16, 4); s.equip(s.party.get(1), RpgData.SILVER_ARMOR);
        read(17, 11); ok(s.hasFlag(RpgData.FLAG_DIARY), "로웰의 일기");
        bossFight(RpgPlay::enterCave2, RpgData.CAVE2, 5, 4, RpgData.STEP_BOSS_DOWN, 10, 13);
        goToTown(); if (s.map != RpgData.ERDEN) { exitTown(); walkTo(20, 8, false); }
        RpgState.Interaction clear1 = talkNpc(npc("에르덴 왕"));
        ok(clear1.chapterClear && s.step == RpgData.STEP_CHAPTER1_CLEAR, "1장 클리어");
        double ch1 = seconds; int battles1 = battles;
        System.out.println("   ── 1장 끝: " + party());

        // ===================== 2장 「상인의 길」
        talkNpc(npc("모르칸의 선장")); ok(s.map == RpgData.MERCA && s.step == RpgData.STEP_SAILED, "배를 타고 메르카로");
        checkNpcs();
        read(17, 2); ok(s.hasFlag(RpgData.FLAG_LEDGER), "길드 장부");
        read(23, 14); ok(s.hasFlag(RpgData.FLAG_SHIPLOG), "선박 기록");
        talkNpc(npc("길드장 바론")); ok(s.step == RpgData.STEP_GUILD_REFUSED, "길드장이 거절, 피피 이야기");
        RpgState.Interaction pq = talkNpc(npc("요정 상인 피피")); ok(pq.lines.size() > 3, "피피의 부탁: 저울 되찾기");
        rest();
        shop(new int[]{RpgData.HI_HERB, RpgData.HI_HERB, RpgData.HI_HERB, RpgData.WING});
        exitTown(); ok(s.map == RpgData.WORLD2, "메르카 평원");
        grindWorld(9, 15, 15);
        goToTown(); // 메르카
        for (int i = 0; i < 4; i++) { RpgData.Npc ws = npc("메르카 무기점"); talkNpc(ws); shopAt(new int[]{RpgData.STEEL_SWORD, RpgData.STEEL_ARMOR}); }
        System.out.println("   물보라 동굴 가기 전: " + party());
        exitTown(); walkTo(7, 2, false); ok(s.map == RpgData.SPRAY1, "물보라 동굴 1층");
        read(22, 1); read(11, 4);
        while (s.map == RpgData.SPRAY1) walkTo(4, 11, false);
        ok(s.map == RpgData.SPRAY2, "물보라 동굴 2층");
        read(20, 3);
        bossFight(RpgPlay::enterSpray2, RpgData.SPRAY2, 5, 4, RpgData.STEP_GALLEON_DOWN, 11, 13);
        ok(s.hasItem(RpgData.PIPI_SCALE), "피피의 저울 되찾음");
        goToTown(); if (s.map != RpgData.MERCA) { exitTown(); walkTo(5, 17, false); }
        talkNpc(npc("요정 상인 피피")); ok(s.party.size() == 3 && s.step == RpgData.STEP_PIPI, "피피 합류 (Lv" + s.party.get(2).level + ")");
        exitTown(); walkTo(28, 19, false); ok(s.map == RpgData.SALT, "소금꽃 마을"); checkNpcs();
        ok(s.npcAt(19, 11) != null, "어부로 돌아온 갈리온");
        exitTown(); walkTo(31, 5, false); ok(s.map == RpgData.LIGHTHOUSE, "곶의 등대"); checkNpcs();
        read(2, 2); ok(s.hasFlag(RpgData.FLAG_LIGHTLOG), "등대 일지");
        talkNpc(npc("등대지기 노아")); ok(s.step == RpgData.STEP_LAMP && s.hasItem(RpgData.LIGHTHOUSE_LAMP), "등대의 등불");
        exitTown();
        grindWorld(26, 12, 20);
        goToTown();
        if (s.map != RpgData.MERCA) { s.townMap = RpgData.MERCA; goToTown(); }
        for (int i = 0; i < 4; i++) { talkNpc(npc("메르카 무기점")); shopAt(new int[]{RpgData.DRAGON_SPEAR, RpgData.CORAL_ARMOR, RpgData.STEEL_SWORD, RpgData.STEEL_ARMOR}); }
        talkNpc(npc("메르카 도구점")); shopAt(new int[]{RpgData.HI_HERB, RpgData.HI_HERB, RpgData.HI_HERB, RpgData.HI_HERB, RpgData.PHOENIX, RpgData.MANA_CANDY, RpgData.MANA_CANDY});
        System.out.println("   해저 신전 가기 전: " + party());
        exitTown(); walkTo(33, 3, false); ok(s.map == RpgData.TEMPLE1, "등불로 열린 해저 신전 1층");
        read(2, 2); read(22, 5);
        while (s.map == RpgData.TEMPLE1) walkTo(6, 8, false);
        ok(s.map == RpgData.TEMPLE2, "해저 신전 2층");
        read(21, 6); read(2, 7); read(4, 8);
        ok(s.hasItem(RpgData.BAROS_COMPASS) && s.hasFlag(RpgData.FLAG_PROPHECY), "바로스의 나침반, 문지기의 각인");
        for (Hero h : s.party) if (RpgData.ITEMS[h.armor].power < 34) { s.equip(h, RpgData.SEA_ARMOR); break; }
        bossFight(RpgPlay::enterTemple2, RpgData.TEMPLE2, 11, 3, RpgData.STEP_SERPENT_DOWN, 12, 11);
        s.townMap = RpgData.MERCA; goToTown();
        RpgState.Interaction clear2 = talkNpc(npc("길드장 바론"));
        ok(clear2.chapterClear && s.step == RpgData.STEP_CHAPTER2_CLEAR, "2장 클리어");
        ok(String.join("\n", clear2.lines).contains("형……"), "나침반을 전하면 바론의 대사가 달라짐");
        ok(s.clues().size() == RpgData.CLUE_NAMES.length, "단서 " + s.clues().size() + "개 모두");
        String save = s.serialize(); RpgState r = RpgState.deserialize(save); ok(r.serialize().equals(save), "저장/불러오기 일치");

        double ch2 = seconds - ch1;
        System.out.println("   ── 2장 끝: " + party());
        System.out.printf("   1장 %.0f분 (전투 %d) / 2장 %.0f분 (전투 %d) → 2장은 1장의 %.1f배%n", ch1 / 60, battles1, ch2 / 60, battles - battles1, ch2 / ch1);
        System.out.println("   진귀한 몬스터 처치: 경험치형 " + rareExp + "번, 골드형 " + rareGold + "번 / 휴식 " + rests + "번");
        StringBuilder lvs = new StringBuilder("   레벨 도달 시각(분): "); for (int i = 2; i < levelAt.length; i++) if (levelAt[i] > 0) lvs.append(i).append("=").append(levelAt[i]).append(" ");
        System.out.println(lvs);
    }

    /** 마을의 모든 상점을 돌며 장비를 갈아 끼우고 약을 채운다 */
    static void restock() {
        for (RpgData.Npc n : s.visibleNpcs()) {
            if (n.action != RpgData.ACT_SHOP) continue;
            talkNpc(n);
            List<Integer> want = new ArrayList<>();
            int[] stock = RpgData.SHOPS[n.arg];
            for (int i = stock.length - 1; i >= 0; i--) if (RpgData.ITEMS[stock[i]].type != RpgData.CONSUMABLE) want.add(stock[i]);
            for (int k = 0; k < 2; k++) for (int id : stock) if (RpgData.ITEMS[id].type == RpgData.CONSUMABLE && id != RpgData.WING && id != RpgData.HERB) want.add(id);
            if (!ch2(s.map)) for (int k = 0; k < 3; k++) want.add(RpgData.HERB);
            int[] arr = new int[want.size()]; for (int i = 0; i < arr.length; i++) arr[i] = want.get(i);
            shopAt(arr);
        }
    }

    /** 이미 상점 앞에 서 있을 때 사기 */
    static void shopAt(int[] buy) {
        for (int id : buy) {
            RpgData.Item it = RpgData.ITEMS[id];
            if (s.gold < s.price(id)) continue;
            if (it.type != RpgData.CONSUMABLE) {
                Hero best = null; int gain = 0;
                for (Hero h : s.party) { int cur = it.type == RpgData.WEAPON ? RpgData.ITEMS[h.weapon].power : RpgData.ITEMS[h.armor].power; if (it.power - cur > gain) { gain = it.power - cur; best = h; } }
                if (best == null) continue;
                s.buy(id); s.equip(best, id);
            } else s.buy(id);
        }
    }
}
