// 실행: javac -encoding UTF-8 -d out app/src/main/java/com/shinchang/adventure/rpg/{RpgData,RpgMaps,RpgState,Hero,Battle,Josa}.java tools/BossSim.java tools/RpgPlay.java && java -cp out RpgPlay
import com.shinchang.adventure.rpg.*;
import java.util.*;

/** 자동 플레이: 길찾기로 걸어가며 전투는 BossSim 의 AI 로 진행해 1장과 2장을 끝까지 깬다. */
public class RpgPlay {
    static RpgState s; static Random rnd = new Random(Long.getLong("seed", 7));
    static int battles = 0, defeats = 0, rests = 0, rareExp = 0, rareGold = 0; static double seconds = 0;
    static int[] levelAt = new int[RpgData.MAX_LEVEL + 2];
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
    static boolean isTown(int m) { return m == RpgData.REBEL || m == RpgData.ERDEN || m == RpgData.MERCA || m == RpgData.SALT || m == RpgData.IGNA || m == RpgData.BEASTDEN || m == RpgData.ROA || m == RpgData.LUMEN; }
    static boolean ch2(int m) { return m >= RpgData.WORLD2; }
    static void exitTown() {
        switch (s.map) {
            case RpgData.REBEL: walkTo(12, 21, false); break;
            case RpgData.ERDEN: walkTo(13, 27, false); break;
            case RpgData.MERCA: walkTo(0, 11, false); break;
            case RpgData.SALT: walkTo(11, 0, false); break;
            case RpgData.LIGHTHOUSE: walkTo(6, 12, false); break;
            case RpgData.IGNA: walkTo(12, 19, false); break;
            case RpgData.BEASTDEN: walkTo(11, 17, false); break;
            case RpgData.ROA: walkTo(12, 19, false); break;
            case RpgData.LUMEN: walkTo(12, 19, false); break;
            case RpgData.LAKE: walkTo(11, 15, false); break;
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
                case RpgData.TRIAL_P: case RpgData.TRIAL_A: case RpgData.TRIAL_S: walkTo(11, 14, false); break;
                case RpgData.LAVA2: walkTo(11, 13, false); break;
                case RpgData.LAVA1: walkTo(11, 14, false); break;
                case RpgData.FORT3: walkTo(12, 12, false); break;
                case RpgData.FORT2: walkTo(16, 13, false); break;
                case RpgData.FORT1: walkTo(11, 14, false); break;
                case RpgData.WORLD3: walkTo(8, 17, false); break;
                case RpgData.LAKE: walkTo(11, 15, false); break;
                case RpgData.RUINS2: walkTo(12, 13, false); break;
                case RpgData.RUINS1: walkTo(11, 14, false); break;
                case RpgData.CRYSTAL2: walkTo(10, 13, false); break;
                case RpgData.CRYSTAL1: walkTo(10, 14, false); break;
                case RpgData.DEMON2: walkTo(12, 12, false); break;
                case RpgData.DEMON3: walkTo(12, 12, false); break;
                case RpgData.WORLD5: walkTo(17, 18, false); break;
                case RpgData.GOLD3: walkTo(12, 11, false); break;
                case RpgData.GOLD2: walkTo(11, 13, false); break;
                case RpgData.GOLD1: walkTo(11, 14, false); break;
                case RpgData.TOWER3: walkTo(12, 12, false); break;
                case RpgData.TOWER2: walkTo(16, 13, false); break;
                case RpgData.TOWER1: walkTo(11, 14, false); break;
                case RpgData.CATH3: walkTo(12, 11, false); break;
                case RpgData.CATH2: walkTo(12, 13, false); break;
                case RpgData.CATH1: walkTo(11, 14, false); break;
                case RpgData.RUINS3: walkTo(11, 13, false); break;
                case RpgData.CRYSTAL3: walkTo(11, 12, false); break;
                case RpgData.MOON2: walkTo(12, 12, false); break;
                case RpgData.MOON1: walkTo(11, 14, false); break;
                case RpgData.DEMON1: walkTo(11, 14, false); break;
                case RpgData.WORLD4:
                    if (s.townMap == RpgData.ROA) walkTo(14, 18, false); else walkTo(6, 6, false); break;
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
    static void enterRuins2() { goToTown(); exitTown(); walkTo(28, 6, false); while (s.map == RpgData.RUINS1) walkTo(6, 8, false); }
    static void enterRuins3() { enterRuins2(); while (s.map == RpgData.RUINS2) walkTo(12, 3, false); }
    static void enterCrystal2() { goToTown(); exitTown(); walkTo(30, 19, false); while (s.map == RpgData.CRYSTAL1) walkTo(4, 12, false); }
    static void enterCrystal3() { enterCrystal2(); while (s.map == RpgData.CRYSTAL2) walkTo(5, 4, false); }
    static void enterMoon2() { s.townMap = RpgData.BEASTDEN; goToTown(); exitTown(); walkTo(10, 9, false); while (s.map == RpgData.MOON1) walkTo(4, 4, false); }
    static void enterDemon1() { goToTown(); exitTown(); walkTo(18, 3, false); }
    static void enterDemon3() { enterDemon1(); while (s.map == RpgData.DEMON1) walkTo(18, 5, false); }
    static void enterDemon2() { enterDemon3(); while (s.map == RpgData.DEMON3) walkTo(3, 2, false); }
    static void enterTrialP() { goToTown(); exitTown(); walkTo(4, 10, false); }
    static void enterTrialA() { goToTown(); exitTown(); walkTo(12, 12, false); }
    static void enterTrialS() { goToTown(); exitTown(); walkTo(8, 21, false); }
    static void enterGold3() { goToTown(); exitTown(); walkTo(6, 10, false); while (s.map == RpgData.GOLD1) walkTo(4, 11, false); while (s.map == RpgData.GOLD2) walkTo(5, 4, false); }
    static void enterTower2() { goToTown(); exitTown(); walkTo(29, 9, false); while (s.map == RpgData.TOWER1) walkTo(18, 5, false); }
    static void enterTower3() { enterTower2(); while (s.map == RpgData.TOWER2) walkTo(3, 12, false); }
    static void enterCath3() { goToTown(); exitTown(); walkTo(17, 3, false); while (s.map == RpgData.CATH1) walkTo(6, 8, false); while (s.map == RpgData.CATH2) walkTo(11, 3, false); }
    static void enterLava2() { goToTown(); exitTown(); walkTo(24, 12, false); while (s.map == RpgData.LAVA1) walkTo(4, 11, false); }
    static void enterFort1() { goToTown(); exitTown(); walkTo(14, 7, false); }
    static void enterFort2() { enterFort1(); while (s.map == RpgData.FORT1) walkTo(18, 5, false); }
    static void enterFort3() { enterFort2(); while (s.map == RpgData.FORT2) walkTo(3, 12, false); }
    static void enterCave2() { goToTown(); if (s.map == RpgData.REBEL || s.map == RpgData.ERDEN) exitTown(); walkTo(28, 3, false); while (s.map == RpgData.CAVE1) walkTo(4, 12, false); }
    static void enterSpray2() { goToTown(); exitTown(); walkTo(7, 2, false); while (s.map == RpgData.SPRAY1) walkTo(4, 11, false); }
    static void enterTemple2() { goToTown(); exitTown(); walkTo(33, 3, false); while (s.map == RpgData.TEMPLE1) walkTo(6, 8, false); }

    static void bossFight(Runnable enter, int dungeon, int nx, int ny, int doneStep, int gcx, int gcy) {
        bossFight(enter, dungeon, nx, ny, () -> s.step >= doneStep, gcx, gcy);
    }

    static void bossFight(Runnable enter, int dungeon, int nx, int ny, java.util.function.BooleanSupplier done, int gcx, int gcy) {
        int attempts = 0;
        while (!done.getAsBoolean() && attempts++ < 10) {
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
        ok(done.getAsBoolean(), "보스 격파 (시도 " + attempts + "번) " + party());
    }

    public static void main(String[] a) {
        s = new RpgState(); s.setRandom(rnd); s.perkBroker = s.perkFire = s.perkFaith = true;
        s.suns = Long.getLong("suns", 1500);

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
        RpgState.Interaction early = talkNpc(npc("요정 상인 피피")); ok(early.lines.size() == 2, "아직 피피는 부탁하지 않음 (눈에 힘이 없다)");
        RpgState.Interaction ti = talkNpc(npc("검객 무현")); ok(ti.lines.size() > 5, "검객 무현: 세 수련의 던전");
        double trialStart = seconds;
        exitTown();
        walkTo(4, 10, false); ok(s.map == RpgData.TRIAL_P, "힘의 던전"); read(22, 5);
        bossFight(RpgPlay::enterTrialP, RpgData.TRIAL_P, 11, 2, () -> s.hasFlag(RpgData.FLAG_TRIAL_P), 11, 12);
        goToTown(); exitTown(); walkTo(12, 12, false); ok(s.map == RpgData.TRIAL_A, "민첩의 던전"); read(11, 8);
        bossFight(RpgPlay::enterTrialA, RpgData.TRIAL_A, 11, 2, () -> s.hasFlag(RpgData.FLAG_TRIAL_A), 11, 12);
        goToTown(); exitTown(); walkTo(8, 21, false); ok(s.map == RpgData.TRIAL_S, "정신의 던전"); read(22, 6); read(2, 3);
        bossFight(RpgPlay::enterTrialS, RpgData.TRIAL_S, 11, 2, () -> s.hasFlag(RpgData.FLAG_TRIAL_S), 11, 12);
        Hero sc = s.party.get(0);
        ok(sc.trained == 7 && s.npcAt(11, 2) == null, "세 수련 완료 (수호자 사라짐) 공격" + sc.atk() + " 민첩" + sc.agi() + " MP" + sc.maxMp());
        goToTown();
        RpgState.Interaction ms = talkNpc(npc("검객 무현"));
        ok(ms.awakened && s.hasFlag(RpgData.FLAG_MISSION), "마물 소식 → 신창이 용자의 사명을 받아들임");
        boolean resolve = false; for (int sp : sc.spells()) if (RpgData.SPELLS[sp].owner == 20) resolve = true;
        ok(resolve, "「용자의 결의」 기술: 결의의 일격 · 정신 통일");
        System.out.println("   수련 구간: " + String.format("%.0f분", (seconds - trialStart) / 60) + " " + party());
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
        ok(s.clues().size() == 11, "1·2장 단서 " + s.clues().size() + "개 모두");
        String save = s.serialize(); RpgState r = RpgState.deserialize(save); ok(r.serialize().equals(save), "저장/불러오기 일치");

        double ch2 = seconds - ch1; int battles2 = battles - battles1;
        System.out.println("   ── 2장 끝: " + party());

        // ===================== 3장 「용사의 탄생」
        RpgState.Interaction sail = talkNpc(npc("길드의 선장"));
        ok(sail.bossBattle && sail.bossMonster == RpgData.VARGAS0, "이그나행 배 → 바르가스의 습격");
        Battle amb = new Battle(s, new int[]{RpgData.VARGAS0}, true, rnd); int ar = 0;
        while (!amb.over() && ar < 60) amb.playRound(BossSim.ai(s, amb, ar++));
        ok(amb.lost(), "바르가스에게는 이길 수 없다 (" + ar + "라운드)");
        List<String> after = s.onAmbush();
        ok(s.party.size() == 1 && s.reserve.size() == 2 && s.gold == 0 && s.map == RpgData.IGNA && s.step == RpgData.STEP_ALONE
                && s.party.get(0).weapon == RpgData.STICK && !s.hasItem(RpgData.HI_HERB), "동료·골드·장비를 모두 잃고 이그나에서 눈을 뜸");
        ok(String.join("\n", after).contains("3장"), "3장 제목");
        checkNpcs();
        talkNpc(npc("지헌")); ok(s.party.size() == 2 && s.party.get(1).id == 3 && s.step == RpgData.STEP_JIHEON, "지헌 합류 (Lv" + s.party.get(1).level + ")");
        read(9, 3); ok(s.hasFlag(RpgData.FLAG_SMITHLOG), "도건의 작업 일지");
        read(16, 11);
        double ch3start = seconds; int b3 = battles;
        System.out.println("   고난의 시작: " + party());
        // 맨몸으로 섬 몬스터를 잡아 장비값을 번다
        exitTown(); ok(s.map == RpgData.WORLD3, "이그나 섬");
        read(16, 20);
        while (s.gold < 3200 + 2800 && lv() < 40) { grindWorld(10, 15, lv() + 1); }
        goToTown(); restock();
        System.out.println("   장비를 갖춤: " + party() + " / " + RpgData.ITEMS[s.party.get(0).weapon].name + ", " + RpgData.ITEMS[s.party.get(0).armor].name);
        exitTown(); grindWorld(19, 12, 29);
        goToTown(); restock();
        exitTown(); walkTo(24, 12, false); ok(s.map == RpgData.LAVA1, "용암 동굴 1층");
        read(22, 1); read(13, 5);
        while (s.map == RpgData.LAVA1) walkTo(4, 11, false);
        ok(s.map == RpgData.LAVA2, "용암 동굴 2층"); read(21, 3);
        bossFight(RpgPlay::enterLava2, RpgData.LAVA2, 5, 4, RpgData.STEP_GIANT_DOWN, 11, 12);
        ok(s.hasItem(RpgData.FLAME_ORE) && s.hasItem(RpgData.DOGEON_HAMMER), "불꽃 원석과 도건의 망치");
        double beforeWedding = seconds;
        goToTown();
        RpgState.Interaction wed = talkNpc(npc("이그나 촌장"));
        Hero hero = s.party.get(0), ji = s.member(3);
        ok(wed.awakened && s.step == RpgData.STEP_WEDDING && hero.isHero() && ji.awakened, "혼례 → 신창 용사로 전직, 지헌 불의 축복");
        ok(hero.weapon == RpgData.HERO_SWORD && hero.accessory == RpgData.VOW_RING && ji.accessory == RpgData.VOW_RING, "용사의 검·맹세의 반지 장비");
        System.out.println("   각성: " + hero.name() + " " + hero.job() + " HP" + hero.maxHp() + " MP" + hero.maxMp() + " 공격" + hero.atk() + " 방어" + hero.def() + " 민첩" + hero.agi()
                + " / 주문 " + hero.spells().size() + "개 | 지헌 HP" + ji.maxHp() + " 공격" + ji.atk());
        int slayStart = battles, slayLost = defeats;
        exitTown(); walkTo(14, 7, false); ok(s.map == RpgData.FORT1, "검은 불길이 걷힌 흑염의 요새");
        read(6, 5);
        bossFight(RpgPlay::enterFort1, RpgData.FORT1, 18, 7, RpgData.STEP_GEN1, 11, 12);
        while (s.map == RpgData.FORT1) walkTo(18, 5, false);
        ok(s.map == RpgData.FORT2, "요새 2층");
        read(12, 8); ok(s.hasFlag(RpgData.FLAG_ORDER), "요새의 명령서 (모르칸의 배신)"); read(21, 6);
        bossFight(RpgPlay::enterFort2, RpgData.FORT2, 1, 8, RpgData.STEP_GEN2, 16, 12);
        while (s.map == RpgData.FORT2) walkTo(3, 12, false);
        ok(s.map == RpgData.FORT3, "요새 옥좌");
        bossFight(RpgPlay::enterFort3, RpgData.FORT3, 11, 5, RpgData.STEP_GEN3, 12, 11);
        read(22, 6); read(5, 2); ok(s.hasFlag(RpgData.FLAG_FIRESEAL), "불의 봉인 제단");
        bossFight(RpgPlay::enterFort3, RpgData.FORT3, 11, 2, RpgData.STEP_REVENGE, 12, 11);
        ok(s.hasItem(RpgData.FIRE_SHARD), "바르가스 격파, 복수 완성");
        int slayBattles = battles - slayStart;
        System.out.println("   슬레이어 구간: 전투 " + slayBattles + "번, 전멸 " + (defeats - slayLost) + "번, " + String.format("%.0f분", (seconds - beforeWedding) / 60) + " " + party());
        goToTown();
        checkNpcs();
        talkNpc(npc("이그나 촌장")); ok(s.step == RpgData.STEP_HOME, "집을 짓고 가정을 꾸림");
        RpgState.Interaction birth = talkNpc(npc("지헌 어머니 해련"));
        ok(birth.chapterClear && s.step == RpgData.STEP_CHAPTER3_CLEAR, "민한 탄생 → 3장 클리어");
        ok(s.npcAt(4, 4) != null && s.npcAt(4, 4).name.equals("아기 민한"), "집에 아기 민한");
        ok(s.clues().size() == 14, "3장까지 단서 " + s.clues().size() + "개 모두");
        String save3 = s.serialize(); RpgState r3 = RpgState.deserialize(save3);
        ok(r3.serialize().equals(save3) && r3.party.get(0).isHero() && r3.reserve.size() == 2, "3장 저장/불러오기 (각성·장신구·헤어진 동료)");
        double ch3 = seconds - ch3start;
        System.out.println("   ── 3장 끝: " + party());

        int battles3 = battles - b3;

        // ===================== 4장 「해를 물려받은 아이」
        double ch4start = seconds; int b4 = battles;
        talkNpc(npc("지헌 어머니 해련")); ok(s.step == RpgData.STEP_FAMILY && RpgData.chapterOf(s.step) == 4, "4장 시작: 가족의 기쁨, 민한이 자란다");
        checkNpcs();
        talkNpc(npc("인자한 할머니"));
        talkNpc(npc("어린 민한")); ok(s.step == RpgData.STEP_KIDNAPPED, "할머니에게 유인되어 민한이 납치됨");
        rest();
        exitTown(); walkTo(22, 18, false); ok(s.map == RpgData.LAKE, "안개 호수");
        walkTo(11, 9, true); RpgState.Interaction fx = s.interact();
        ok(fx.bossBattle && fx.bossMonster == RpgData.FOX1, "천면귀 첫 결전");
        ok(fight(new int[]{RpgData.FOX1}, true), "신창·지헌이 천면귀를 물리침 " + party());
        s.onBossDefeated(RpgData.FOX1);
        ok(s.pendingBoss == RpgData.HOSTAGE && s.step == RpgData.STEP_HOSTAGE, "천면귀가 민한을 인질로 잡음");
        s.pendingBoss = -1;
        Battle hb = new Battle(s, new int[]{RpgData.HOSTAGE}, true, rnd); int hr = 0;
        while (!hb.over() && hr < 40) hb.playRound(BossSim.ai(s, hb, hr++));
        ok(hb.lost() && hb.enemies.get(0).hp == hb.enemies.get(0).m.hp, "인질극: 반격하지 못하고 쓰러짐 (" + hr + "라운드)");
        s.onPetrified();
        ok(s.party.size() == 2 && s.party.get(0).id == 4 && s.party.get(1).id == 5 && s.map == RpgData.BEASTDEN
                && s.step == RpgData.STEP_MINHAN && s.member(0) == null, "석상이 되어 흩어짐 → 8년 후 민한과 하루의 모험");
        checkNpcs();
        talkNpc(npc("백년 부엉이 노을"));
        System.out.println("   민한의 출발: " + party());
        exitTown(); walkTo(14, 18, false); ok(s.map == RpgData.ROA, "로아 마을"); checkNpcs();
        read(9, 2); ok(s.hasFlag(RpgData.FLAG_SUNLORE), "해의 제단 벽화");
        RpgState.Interaction lg = talkNpc(npc("해의 제단지기"));
        Hero mh = s.party.get(0), haru = s.party.get(1);
        ok(lg.awakened && s.step == RpgData.STEP_LEGACY && mh.isHero() && haru.awakened, "아버지의 유산: 민한이 해의 계승자로");
        RpgState.Interaction lg2 = talkNpc(npc("해의 제단지기")); ok(lg2.action == RpgData.ACT_LEGACY, "해의 제단 열림 (해 " + s.availableSuns() + "개)");
        long sunsBefore = s.availableSuns();
        int[][] buy = s.suns >= 1260 ? new int[][]{{RpgData.ETERNAL_SUN, 0}, {RpgData.HEAVEN_MAIL, 0}, {RpgData.SUN_HEART, 0}, {RpgData.SUN_MANE, 1}}
                : new int[][]{{RpgData.DAWN_BLADE, 0}, {RpgData.HEAVEN_MAIL, 0}, {RpgData.SUN_HEART, 0}, {RpgData.SUN_MANE, 1}};
        for (int[] b : buy) { if (s.buyLegacy(b[0])) s.equip(s.party.get(b[1]), b[0]); }
        ok(s.availableSuns() < sunsBefore, "해로 전설의 장비 구입: " + RpgData.ITEMS[mh.weapon].name + ", " + RpgData.ITEMS[mh.armor].name
                + ", " + RpgData.ITEMS[mh.accessory].name + " (남은 해 " + s.availableSuns() + ")");
        System.out.println("   전설 장비: 민한 HP" + mh.maxHp() + " 공격" + mh.atk() + " 방어" + mh.def() + " | 하루 HP" + haru.maxHp() + " 공격" + haru.atk() + " 방어" + haru.def());
        restock();
        int legacyBattles = battles, legacyLost = defeats;
        exitTown(); walkTo(28, 6, false); ok(s.map == RpgData.RUINS1, "바람의 고원 유적");
        read(2, 2); read(21, 2); read(22, 5);
        while (s.map == RpgData.RUINS1) walkTo(6, 8, false);
        read(21, 6);
        while (s.map == RpgData.RUINS2) walkTo(12, 3, false);
        ok(s.map == RpgData.RUINS3, "고원 유적 3층"); read(22, 5); read(2, 6);
        bossFight(RpgPlay::enterRuins3, RpgData.RUINS3, 12, 2, RpgData.STEP_DAD_FOUND, 11, 12);
        ok(s.step == RpgData.STEP_DAD_FOUND, "아빠의 석상을 찾음");
        goToTown(); exitTown(); walkTo(30, 19, false); ok(s.map == RpgData.CRYSTAL1, "수정 동굴");
        read(20, 1); read(11, 4);
        while (s.map == RpgData.CRYSTAL1) walkTo(4, 12, false);
        read(16, 4);
        while (s.map == RpgData.CRYSTAL2) walkTo(5, 4, false);
        ok(s.map == RpgData.CRYSTAL3, "수정 동굴 3층"); read(11, 3);
        bossFight(RpgPlay::enterCrystal3, RpgData.CRYSTAL3, 4, 2, RpgData.STEP_MOM_FOUND, 11, 11);
        ok(s.step == RpgData.STEP_MOM_FOUND, "엄마의 석상을 찾음 (달빛 이슬 이야기)");
        s.townMap = RpgData.BEASTDEN; goToTown();
        RpgState.Interaction mq = talkNpc(npc("백년 부엉이 노을")); ok(String.join("", mq.lines).contains("달빛 샘"), "부엉이: 달빛 샘으로");
        exitTown(); walkTo(10, 9, false); ok(s.map == RpgData.MOON1, "달빛 숲길"); read(21, 1);
        while (s.map == RpgData.MOON1) walkTo(4, 4, false);
        ok(s.map == RpgData.MOON2, "달빛 샘"); read(22, 6);
        bossFight(RpgPlay::enterMoon2, RpgData.MOON2, 11, 3, RpgData.STEP_DEW, 12, 11);
        ok(s.items[RpgData.MOON_DEW] == 2, "숲의 주인을 구하고 달빛 이슬 2개");
        enterCrystal3(); walkTo(4, 2, true); s.interact();
        ok(s.step == RpgData.STEP_MOM_BACK && s.member(3) != null, "엄마 지헌이 돌아옴");
        goToTown(); enterRuins3(); walkTo(12, 2, true); s.interact();
        ok(s.step == RpgData.STEP_DAD_BACK && s.member(0) != null && s.member(0).isHero() && s.party.size() == 4, "아빠 신창이 돌아옴 (가족 4명)");
        goToTown(); restock();
        exitTown(); walkTo(18, 3, false); ok(s.map == RpgData.DEMON1, "안개가 걷힌 요마성");
        read(6, 5);
        bossFight(RpgPlay::enterDemon1, RpgData.DEMON1, 18, 7, RpgData.STEP_FOX_DOWN, 11, 12);
        while (s.map == RpgData.DEMON1) walkTo(18, 5, false);
        ok(s.map == RpgData.DEMON3, "요마성 2층"); read(21, 2);
        while (s.map == RpgData.DEMON3) walkTo(3, 2, false);
        ok(s.map == RpgData.DEMON2, "요마성 옥좌");
        read(22, 6); read(5, 2); ok(s.hasFlag(RpgData.FLAG_PACT), "요마왕의 맹약");
        bossFight(RpgPlay::enterDemon2, RpgData.DEMON2, 11, 2, RpgData.STEP_KING_DOWN, 12, 11);
        System.out.println("   유산 이후: 전투 " + (battles - legacyBattles) + "번, 전멸 " + (defeats - legacyLost) + "번 " + party());
        s.townMap = RpgData.BEASTDEN; goToTown();
        RpgState.Interaction end4 = talkNpc(npc("백년 부엉이 노을"));
        ok(end4.chapterClear && s.step == RpgData.STEP_CHAPTER4_CLEAR, "요마왕 토벌 → 4장 클리어");
        ok(s.clues().size() == 16, "4장까지 단서 " + s.clues().size() + "개 모두");
        String save4 = s.serialize(); RpgState r4 = RpgState.deserialize(save4);
        ok(r4.serialize().equals(save4) && r4.sunsSpent == s.sunsSpent, "4장 저장/불러오기 (쓴 해 " + s.sunsSpent + ")");
        double ch4 = seconds - ch4start; int battles4 = battles - b4;
        System.out.println("   ── 4장 끝: " + party());

        // ===================== 최종장 「영원한 전설」
        double ch5start = seconds; int b5 = battles, d5 = defeats;
        talkNpc(npc("백년 부엉이 노을"));
        ok(s.step == RpgData.STEP_WORLD && s.map == RpgData.LUMEN && s.party.size() == 3 && s.party.get(0).id == 0
                && s.member(3) != null && s.member(4) != null, "최종장: 신창·지헌·민한 셋이 성도 루멘으로 (하루는 숲에)");
        checkNpcs();
        talkNpc(npc("엘라")); ok(s.step == RpgData.STEP_TRUTH, "엘라가 밝힌 세 악마의 정체");
        restock();
        exitTown(); ok(s.map == RpgData.WORLD5, "판테온 대륙");
        walkTo(6, 10, false); ok(s.map == RpgData.GOLD1, "황금 금고");
        read(22, 1); read(11, 4);
        while (s.map == RpgData.GOLD1) walkTo(4, 11, false);
        read(20, 3);
        while (s.map == RpgData.GOLD2) walkTo(5, 4, false);
        ok(s.map == RpgData.GOLD3, "맘몬의 보물고"); read(22, 6); read(2, 2);
        bossFight(RpgPlay::enterGold3, RpgData.GOLD3, 11, 2, RpgData.STEP_MAMMON_DOWN, 12, 10);
        goToTown(); checkNpcs(); ok(s.npcAt(15, 9) != null && s.npcAt(17, 9) != null, "구해 낸 리나와 피피가 루멘에");
        exitTown(); walkTo(29, 9, false); ok(s.map == RpgData.TOWER1, "권좌의 탑");
        read(6, 5);
        while (s.map == RpgData.TOWER1) walkTo(18, 5, false);
        read(21, 6);
        bossFight(RpgPlay::enterTower2, RpgData.TOWER2, 1, 8, RpgData.STEP_ARSEN_DOWN, 16, 12);
        while (s.map == RpgData.TOWER2) walkTo(3, 12, false);
        read(5, 2); read(22, 6);
        bossFight(RpgPlay::enterTower3, RpgData.TOWER3, 11, 2, RpgData.STEP_BELIAL_DOWN, 12, 11);
        goToTown(); ok(s.npcAt(22, 12) != null, "되찾은 기사단장 로웰");
        exitTown(); walkTo(17, 3, false); ok(s.map == RpgData.CATH1, "타락한 대성당");
        read(2, 2); read(21, 2); read(22, 5);
        while (s.map == RpgData.CATH1) walkTo(6, 8, false);
        read(21, 6);
        while (s.map == RpgData.CATH2) walkTo(11, 3, false);
        ok(s.map == RpgData.CATH3, "반역자의 제단"); read(22, 9);
        System.out.println("   바알세불 앞: " + party());
        walkTo(11, 2, true); RpgState.Interaction bz = s.interact();
        ok(bz.bossBattle && bz.bossMonster == RpgData.BEELZEBUB, "바알세불 결전");
        Battle fb = new Battle(s, new int[]{RpgData.BEELZEBUB}, true, rnd); int fr = 0; int[] phaseAt = new int[4];
        while (!fb.over() && fr < 200) { fb.playRound(BossSim.ai(s, fb, fr++)); if (phaseAt[fb.blessing] == 0) phaseAt[fb.blessing] = fr; }
        ok(fb.blessing == 3, "세계인의 기도(" + phaseAt[1] + "턴) → 동식물의 기도(" + phaseAt[2] + "턴) → 창조주의 가호(" + phaseAt[3] + "턴)");
        ok(fb.won(), "연합기 「사랑의 완성 · FAMILY」로 바알세불 격파 (" + fr + "턴)");
        s.onBossDefeated(RpgData.BEELZEBUB);
        ok(s.step == RpgData.STEP_ENDING && RpgData.chapterOf(s.step) == 5, "엔딩: 영원한 전설");
        ok(s.clues().size() == RpgData.CLUE_NAMES.length, "단서 " + s.clues().size() + "개 모두");
        String save5 = s.serialize(); ok(RpgState.deserialize(save5).serialize().equals(save5), "최종 저장/불러오기");
        double ch5 = seconds - ch5start;
        System.out.println("   ── 최종장 끝: " + party() + " / 최종장 전투 " + (battles - b5) + "번, 전멸 " + (defeats - d5) + "번, " + String.format("%.0f분", ch5 / 60));

        System.out.printf("   1장 %.0f분 (전투 %d) / 2장 %.0f분 (전투 %d) / 3장 %.0f분 (전투 %d) / 4장 %.0f분 (전투 %d)%n",
                ch1 / 60, battles1, ch2 / 60, battles2, ch3 / 60, battles3, ch4 / 60, battles4);
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
