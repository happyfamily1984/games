package com.shinchang.adventure.rpg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** 턴제 전투. 한 라운드씩 명령을 받아 결과 메시지를 돌려준다. */
public final class Battle {

    public static final int ATTACK = 0, SPELL = 1, ITEM = 2, DEFEND = 3, FLEE = 4, FAMILY = 5;
    /** 연합기 「사랑의 완성 · FAMILY」의 피해 */
    public static final int FAMILY_DAMAGE = 99999999;

    public static final class Enemy {
        public final RpgData.Monster m;
        public final String name;
        public int hp;
        /** 스스로 도망쳤으면 true (보상 없음) */
        public boolean fled;
        boolean stolen;

        Enemy(RpgData.Monster m, String name) {
            this.m = m;
            this.name = name;
            this.hp = m.hp;
        }

        public boolean alive() {
            return hp > 0;
        }
    }

    /** 파티 멤버 한 명의 명령 */
    public static final class Action {
        public final int cmd;
        /** 적 번호 또는 아군 번호 (-1: 자동) */
        public final int target;
        /** 주문 번호 또는 아이템 번호 */
        public final int arg;

        public Action(int cmd, int target, int arg) {
            this.cmd = cmd;
            this.target = target;
            this.arg = arg;
        }
    }

    public final List<Enemy> enemies = new ArrayList<>();
    public final boolean boss;
    /** 첫 번째 몬스터 번호 (보스 판별용) */
    public final int firstMonster;
    private final RpgState state;
    /** 행운의 저울: 아군이 적의 공격을 피할 확률 */
    private double partyEvade;
    private final Random random;
    private boolean fled;
    /** 수호의 맹세: 받는 피해 배율 */
    private double guard = 1;
    /** 보스 격노 (HP 절반 이하) */
    private boolean enraged;
    private boolean faltered;
    /** 이번 라운드에 격노했는지 (화면 연출용) */
    public boolean enragedThisRound;
    private final boolean[] defending = new boolean[4];
    private int totalExp, totalGold;
    /** 지금까지 진행한 라운드 수 */
    public int round;
    /** 지금 행동 중인 아군 (회심·슬레이어 계산용) */
    private Hero acting;
    /** 바알세불전의 기적: 0 없음, 1 세계인의 기도, 2 동식물의 기도, 3 창조주의 가호 */
    public int blessing;
    private double atkMul = 1, defMul = 1;
    /** 이번 라운드에 기적이 일어났는지 (화면 연출용) */
    public boolean miracleThisRound;

    /** 연합기를 쓸 수 있는지 (창조주의 가호를 받은 뒤) */
    public boolean familyReady() {
        return blessing >= 3;
    }

    private static final String[][] MIRACLES = {
            {"……", "(신창 일행이 모두 쓰러졌다. 바알세불의 웃음소리가 대성당을 울린다)",
                    "(그때, 아주 멀리서 노랫소리가 들려온다)",
                    "(루멘의 사람들, 메르카의 상인들, 이그나의 대장장이들, 에르덴의 기사들… 온 세상 사람들이 무릎을 꿇고 기도하고 있다)",
                    "[리나의 목소리] 일어나요, 신창 씨!  [피피의 목소리] 신창, 지면 안 돼!",
                    "🙏 세계인들의 기도가 세 사람을 일으켜 세웠다! 🙏",
                    "✨ HP·MP 완전 회복! 공격력 2배! ✨"},
            {"……", "(다시 쓰러진 세 사람 위로, 바알세불의 날개 그림자가 드리운다)",
                    "(땅이 울린다. 숲의 나무들이, 바다의 물고기들이, 하늘의 새들이 같은 쪽을 향해 고개를 숙인다)",
                    "[하루의 울음] 아우우우우——!!  [백년 부엉이 노을] 부엉… 우리 아이를 지켜 다오…",
                    "🌿 모든 동식물의 기도가 세 사람을 다시 일으켜 세웠다! 🌿",
                    "✨ HP·MP 완전 회복! 방어력 2배! ✨"},
            {"……", "(세 번째로 쓰러졌다. 이번에는 아무 소리도 들리지 않는다)",
                    "(완전한 고요. 그리고… 하늘이 열렸다)",
                    "[창조주의 목소리] 나의 아이들아. 너희는 서로를 위해 모든 것을 내려놓았구나.",
                    "[창조주의 목소리] 일어나거라. 그리고 내가 세상에 넣어 둔 가장 큰 힘을 보여 주거라.",
                    "☀️ 창조주의 가호가 세 사람을 감쌌다! ☀️",
                    "✨ 공격력 10배! 매 턴 HP·MP 완전 회복! ✨",
                    "💞 세 사람의 마음이 하나로 이어진다… 연합기 「사랑의 완성 · FAMILY」를 쓸 수 있게 되었다! 💞"},
    };

    /** 바알세불전: 쓰러져도 기적이 일어나 다시 일어선다 */
    private void miracle(List<String> log) {
        if (firstMonster != RpgData.BEELZEBUB || !lost()) return;
        if (blessing < 3) {
            for (String l : MIRACLES[blessing]) log.add(l);
            blessing++;
            if (blessing == 1) atkMul = 2;
            if (blessing == 2) defMul = 2;
            if (blessing == 3) atkMul = 10;
        } else {
            log.add("☀️ 창조주의 가호가 쓰러진 세 사람을 다시 일으킨다!");
        }
        miracleThisRound = true;
        for (Hero h : state.party) h.fullHeal();
    }

    public Battle(RpgState state, int[] monsterIds, boolean boss, Random random) {
        this.state = state;
        this.boss = boss;
        this.firstMonster = monsterIds[0];
        this.random = random;
        // 같은 몬스터가 여럿이면 A, B, C 를 붙인다
        for (int i = 0; i < monsterIds.length; i++) {
            RpgData.Monster m = RpgData.MONSTERS[monsterIds[i]];
            int same = 0;
            for (int id : monsterIds) if (id == monsterIds[i]) same++;
            String name = m.name;
            if (same > 1) {
                int n = 0;
                for (int j = 0; j < i; j++) if (monsterIds[j] == monsterIds[i]) n++;
                name += " " + (char) ('A' + n);
            }
            enemies.add(new Enemy(m, name));
        }
    }

    public String intro() {
        if (boss) return Josa.i(enemies.get(0).name) + " 덤벼들었다!";
        RpgData.Monster first = enemies.get(0).m;
        if (first.rare == RpgData.RARE_EXP) return "✨ 진귀한 " + Josa.i(first.name) + " 나타났다! (재빨라서 금방 도망칠 것 같다!)";
        if (first.rare == RpgData.RARE_GOLD) return "✨ 진귀한 " + Josa.i(first.name) + " 나타났다! (몸에서 금빛이 번쩍인다!)";
        if (enemies.size() == 1) return Josa.i(enemies.get(0).name) + " 나타났다!";
        return "몬스터 무리가 나타났다!";
    }

    public boolean won() {
        for (Enemy e : enemies) if (e.alive()) return false;
        return true;
    }

    public boolean lost() {
        for (Hero h : state.party) if (h.alive()) return false;
        return true;
    }

    public boolean fled() {
        return fled;
    }

    public boolean over() {
        return won() || lost() || fled;
    }

    public List<Enemy> aliveEnemies() {
        List<Enemy> out = new ArrayList<>();
        for (Enemy e : enemies) if (e.alive()) out.add(e);
        return out;
    }

    private int rand(int lo, int hi) {
        return lo + random.nextInt(Math.max(1, hi - lo + 1));
    }

    private int damage(int atk, int def) {
        double base = atk - def / 2.0;
        if (base < 1) return random.nextInt(2);
        return Math.max(1, (int) Math.round(base * (0.85 + random.nextDouble() * 0.3)));
    }

    private Enemy enemyTarget(int index) {
        if (index >= 0 && index < enemies.size() && enemies.get(index).alive()) return enemies.get(index);
        List<Enemy> alive = aliveEnemies();
        return alive.isEmpty() ? null : alive.get(random.nextInt(alive.size()));
    }

    private Hero heroTarget(int index) {
        if (index >= 0 && index < state.party.size() && state.party.get(index).alive()) return state.party.get(index);
        return null;
    }

    private Hero randomAliveHero() {
        List<Hero> alive = new ArrayList<>();
        for (Hero h : state.party) if (h.alive()) alive.add(h);
        return alive.isEmpty() ? null : alive.get(random.nextInt(alive.size()));
    }

    /**
     * 한 라운드를 진행한다. actions 는 파티 순서대로 (쓰러진 멤버는 무시).
     * 첫 번째 명령이 FLEE 면 파티 전체가 도망을 시도한다.
     */
    public boolean enraged() {
        return enraged;
    }

    /**
     * 강적 보너스: 파티 평균 레벨보다 강한 적일수록 경험치가 늘고(레벨 차 1당 +25%, 최대 2.5배),
     * 너무 약한 적(6레벨 이상 아래)은 절반만 준다.
     */
    public static double expFactor(int monsterLevel, double partyLevel) {
        double diff = monsterLevel - partyLevel;
        if (diff <= -6) return 0.5;
        return Math.min(2.5, 1 + 0.25 * Math.max(0, diff));
    }

    private double partyLevel() {
        double sum = 0;
        for (Hero h : state.party) sum += h.level;
        return sum / state.party.size();
    }

    public List<String> playRound(List<Action> actions) {
        List<String> log = new ArrayList<>();
        enragedThisRound = false;
        java.util.Arrays.fill(defending, false);
        round++;
        miracleThisRound = false;
        if (blessing >= 3) {
            for (Hero h : state.party) h.fullHeal();
            log.add("☀️ 창조주의 가호! 일행의 HP·MP가 완전히 회복되었다!");
        }
        // 맹세의 반지: 매 턴 MP 회복
        StringBuilder regen = new StringBuilder();
        for (Hero h : state.party) {
            if (!h.alive() || h.mpRegen() <= 0 || h.mp >= h.maxMp()) continue;
            int before = h.mp;
            h.mp = Math.min(h.maxMp(), h.mp + Math.max(1, h.maxMp() * h.mpRegen() / 100));
            regen.append(regen.length() == 0 ? "" : ", ").append(h.name()).append(" MP +").append(h.mp - before);
        }
        if (regen.length() > 0) log.add("💍 맹세의 반지가 빛난다! (" + regen + ")");

        if (!actions.isEmpty() && actions.get(0).cmd == FLEE) {
            if (!boss && random.nextDouble() < 0.6) {
                log.add(state.party.get(0).name() + " 일행은 도망쳤다!");
                fled = true;
                return log;
            }
            log.add(boss ? "강한 기운에 막혀 도망칠 수 없다!" : "그러나 돌아서는 길이 막혔다!");
            enemyPhase(log);
            return log;
        }

        // 행동 순서: 민첩 + 약간의 운
        List<double[]> order = new ArrayList<>();
        for (int i = 0; i < state.party.size(); i++) {
            Hero h = state.party.get(i);
            if (h.alive()) order.add(new double[]{0, i, h.agi() + random.nextDouble() * h.agi() * 0.5});
        }
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            if (e.alive()) order.add(new double[]{1, i, e.m.agi + random.nextDouble() * e.m.agi * 0.5});
        }
        Collections.sort(order, (a, b) -> Double.compare(b[2], a[2]));
        for (int i = 0; i < state.party.size() && i < actions.size(); i++) {
            if (actions.get(i).cmd == DEFEND) defending[i] = true;
        }

        for (double[] o : order) {
            if (won() || lost()) break;
            int idx = (int) o[1];
            if (o[0] == 0) {
                Hero h = state.party.get(idx);
                if (!h.alive() || idx >= actions.size()) continue;
                heroAct(h, actions.get(idx), log);
            } else {
                Enemy e = enemies.get(idx);
                if (!e.alive()) continue;
                enemyAct(e, log);
                miracle(log);
                if (e.m.special == 2 && random.nextDouble() < (enraged ? 0.55 : 0.35) && !lost()) {
                    log.add(Josa.eun(e.name) + " 쉴 틈도 주지 않는다!");
                    enemyAct(e, log);
                    miracle(log);
                }
            }
        }
        finishLog(log);
        return log;
    }

    private void enemyPhase(List<String> log) {
        for (Enemy e : enemies) {
            if (!e.alive() || lost()) continue;
            enemyAct(e, log);
            miracle(log);
        }
        finishLog(log);
    }

    private void finishLog(List<String> log) {
        if (won()) {
            double base = 0, bonus = 0, pl = partyLevel();
            int defeated = 0;
            totalGold = 0;
            for (Enemy e : enemies) {
                if (e.fled) continue;
                defeated++;
                base += e.m.exp;
                bonus += e.m.exp * (e.m.special == 2 || e.m.rare != 0 ? 1.0 : expFactor(e.m.level, pl));
                totalGold += e.m.gold;
            }
            if (defeated == 0) {
                log.add("몬스터는 모두 사라져 버렸다…");
                return;
            }
            totalExp = (int) Math.round(bonus);
            log.add(boss ? Josa.eul(enemies.get(0).name) + " 쓰러뜨렸다!" : "몬스터를 모두 물리쳤다!");
            if (bonus > base * 1.05) log.add("강적을 물리쳤다! 경험치 보너스 ×" + String.format(java.util.Locale.ROOT, "%.1f", bonus / base));
            log.add(totalExp + "의 경험치를 얻었다. " + totalGold + "골드를 손에 넣었다.");
            state.gold += totalGold;
            for (Hero h : state.party) log.addAll(h.gainExp(totalExp));
        } else if (lost()) {
            log.add(firstMonster == RpgData.HOSTAGE ? "……" : state.party.get(0).name() + " 일행은 전멸했다……");
        }
    }

    /** 진귀한 몬스터는 공격을 잘 피한다 (주문은 절반 확률로만 피한다) */
    private boolean dodged(Enemy t, boolean magic, List<String> log) {
        double chance = magic ? t.m.evade * 0.5 : t.m.evade;
        if (chance <= 0 || random.nextDouble() >= chance) return false;
        log.add(Josa.eun(t.name) + " 재빠르게 몸을 피했다!");
        return true;
    }

    private void heroAct(Hero h, Action a, List<String> log) {
        acting = h;
        try {
            heroActInner(h, a, log);
        } finally {
            acting = null;
        }
    }

    private static final String[] HOSTAGE_LINES = {
            "[천면귀] 움직이지 마! 손가락 하나라도 까딱하면 이 아이는 끝이야!",
            "(민한의 목에 손톱이 닿아 있다. 손을 쓸 수가 없다……)",
            "[천면귀] 호호호! 그래, 그렇게 가만히 맞기만 해!",
            "(아이가 잠결에 '아빠…' 하고 중얼거린다. 검을 쥔 손에 힘이 빠진다)",
            "[천면귀] 용사라더니 고작 이 정도? 아들 하나에 무릎을 꿇다니!",
    };
    private int hostageLine;

    private void heroActInner(Hero h, Action a, List<String> log) {
        if (firstMonster == RpgData.HOSTAGE) {
            // 인질극: 무엇을 해도 반격할 수 없다
            log.add(HOSTAGE_LINES[hostageLine++ % HOSTAGE_LINES.length]);
            log.add(Josa.eun(h.name()) + " 아무것도 할 수 없었다……");
            return;
        }
        switch (a.cmd) {
            case FAMILY: {
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                if (!familyReady()) {
                    log.add("…아직 세 사람의 마음이 하나로 이어지지 않았다.");
                    return;
                }
                log.add("[신창] 지헌, 민한. 손 잡아.");
                log.add("[지헌] 응. 언제나처럼.");
                log.add("[민한] 아빠, 엄마. 하나, 둘, 셋!");
                log.add("(세 사람이 맞잡은 손 사이에서, 해보다 밝은 빛이 터져 나온다)");
                log.add("(배달 가방의 다섯 점 문장, 맹세의 반지, 해의 유산이 하나로 빛난다)");
                log.add("💞 연합기 「사랑의 완성 · FAMILY」!! 💞");
                t.hp = Math.max(0, t.hp - FAMILY_DAMAGE);
                log.add(t.name + "에게 " + String.format(java.util.Locale.ROOT, "%,d", FAMILY_DAMAGE) + "의 피해!!!");
                if (!t.alive() && !boss) log.add(Josa.eul(t.name) + " 물리쳤다!");
                break;
            }
            case ATTACK: {
                if (h.weapon == RpgData.ETERNAL_SUN) {
                    // 초초 전설의 검: 보통 공격이 적 전체를 벤다
                    log.add(Josa.i(h.name()) + " 휘두른 「태초의 해」가 해처럼 타오르며 모든 적을 벤다!");
                    for (Enemy t : aliveEnemies()) {
                        if (dodged(t, false, log)) continue;
                        hitEnemy(t, damage(h.atk(), t.m.def), log);
                    }
                    break;
                }
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                log.add(Josa.eun(h.name()) + " " + Josa.eul(t.name) + " 공격했다!");
                if (dodged(t, false, log)) return;
                hitEnemy(t, damage(h.atk(), t.m.def), log);
                break;
            }
            case DEFEND:
                log.add(Josa.eun(h.name()) + " 몸을 지키고 있다.");
                break;
            case ITEM:
                useItem(h, a, log);
                break;
            case SPELL:
                castSpell(h, a, log);
                break;
            default:
                break;
        }
    }

    private void useItem(Hero h, Action a, List<String> log) {
        RpgData.Item item = RpgData.ITEMS[a.arg];
        if (!state.hasItem(a.arg)) {
            log.add(Josa.eun(h.name()) + " 가방을 뒤졌지만 " + Josa.i(item.name) + " 없었다.");
            return;
        }
        Hero t = heroTarget(a.target);
        if (t == null) t = h;
        state.removeItem(a.arg, 1);
        log.add(Josa.eun(h.name()) + " " + Josa.eul(item.name) + " 사용했다!");
        log.add(state.applyConsumable(a.arg, t));
    }

    private void castSpell(Hero h, Action a, List<String> log) {
        RpgData.Spell s = RpgData.SPELLS[a.arg];
        if (h.mp < s.mp) {
            log.add(Josa.eun(h.name()) + " 「" + s.name + "」" + Josa.eulOf(s.name) + " 쓰려 했지만 MP가 모자라다!");
            return;
        }
        h.mp -= s.mp;
        log.add(Josa.eun(h.name()) + " 「" + s.name + "」" + Josa.eulOf(s.name) + " 썼다!");
        switch (s.kind) {
            case RpgData.HERO_ALL:
                log.add(s.power >= 600 ? "하늘이 갈라지고 성스러운 빛의 폭풍이 몰아친다!"
                        : s.power >= 300 ? "용사의 검이 거대한 빛의 칼날이 되어 적 전체를 베어 낸다!" : "검광이 번쩍이며 적 전체를 가른다!");
                for (Enemy t : aliveEnemies()) {
                    if (dodged(t, false, log)) continue;
                    hitEnemy(t, rand(h.atk() * s.power / 110, h.atk() * s.power / 90), log);
                }
                break;
            case RpgData.HERO_ONE: {
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                log.add(s.power >= 1000 ? "슬레이어의 눈이 빛난다. 한 줄기 섬광이 모든 것을 꿰뚫는다!" : "용사의 검에 빛이 모인다!");
                if (dodged(t, false, log)) return;
                hitEnemy(t, rand(h.atk() * s.power / 110, h.atk() * s.power / 90), log);
                break;
            }
            case RpgData.HOLY_ALL:
                log.add("하늘에서 천벌의 뇌격이 쏟아진다!");
                for (Enemy t : aliveEnemies()) {
                    if (dodged(t, true, log)) continue;
                    hitEnemy(t, rand(s.power * 9 / 10, s.power * 11 / 10), log);
                }
                break;
            case RpgData.REVIVE: {
                Hero t = a.target >= 0 && a.target < state.party.size() ? state.party.get(a.target) : null;
                if (t == null || t.alive()) {
                    log.add("그러나 아무 일도 일어나지 않았다.");
                    return;
                }
                t.hp = Math.max(1, t.maxHp() * s.power / 100);
                log.add(Josa.i(t.name()) + " 되살아났다!");
                break;
            }
            case RpgData.PHYS_ALL:
                log.add("바람을 가르는 일섬!");
                for (Enemy t : aliveEnemies()) {
                    if (dodged(t, false, log)) continue;
                    hitEnemy(t, damage(h.atk(), t.m.def) * s.power / 10, log);
                }
                break;
            case RpgData.WATER_ONE: {
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                if (dodged(t, true, log)) return;
                hitEnemy(t, rand(s.power * 8 / 10, s.power * 12 / 10), log);
                break;
            }
            case RpgData.WATER_ALL:
                log.add("거대한 물결이 몰아친다!");
                for (Enemy t : aliveEnemies()) {
                    if (dodged(t, true, log)) continue;
                    hitEnemy(t, rand(s.power * 8 / 10, s.power * 12 / 10), log);
                }
                break;
            case RpgData.STEAL_GOLD: {
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                if (t.stolen || t.m.gold <= 0) {
                    log.add("[피피] 쳇, 더 나올 게 없나 봐.");
                    return;
                }
                t.stolen = true;
                int got = Math.max(1, (int) Math.round(t.m.gold * (0.3 + random.nextDouble() * 0.3)));
                state.gold += got;
                log.add("[피피] 자자, 이 정도면 공정한 거래지? " + t.name + "에게서 " + got + "골드를 받아 냈다!");
                break;
            }
            case RpgData.EVADE_UP:
                partyEvade = 0.35;
                log.add("저울이 반짝이며 행운이 기울었다! 이번 전투 동안 적의 공격을 자주 피하게 된다!");
                break;
            case RpgData.HEAL_ONE: {
                Hero t = heroTarget(a.target);
                if (t == null) t = h;
                int amount = (int) Math.round(rand(s.power * 9 / 10, s.power * 11 / 10) * state.healBonus());
                int before = t.hp;
                t.hp = Math.min(t.maxHp(), t.hp + amount);
                log.add(Josa.i(t.name()) + " HP를 " + (t.hp - before) + " 회복했다.");
                break;
            }
            case RpgData.FIRE_ONE: {
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                if (dodged(t, true, log)) return;
                hitEnemy(t, (int) Math.round(rand(s.power * 8 / 10, s.power * 12 / 10) * state.fireBonus()), log);
                break;
            }
            case RpgData.FIRE_ALL:
                for (Enemy t : aliveEnemies()) {
                    if (dodged(t, true, log)) continue;
                    hitEnemy(t, (int) Math.round(rand(s.power * 8 / 10, s.power * 12 / 10) * state.fireBonus()), log);
                }
                break;
            case RpgData.POWER_HIT: {
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                hitEnemy(t, (int) Math.round(damage(h.atk(), t.m.def) * 1.8), log);
                break;
            }
            case RpgData.GUARD:
                guard = 0.5;
                log.add("빛의 방패가 일행을 감쌌다! 이번 전투 동안 받는 피해가 절반이 된다!");
                break;
            case RpgData.HEAL_ALL:
                for (Hero t : state.party) {
                    if (!t.alive()) continue;
                    int amount = (int) Math.round(rand(s.power * 9 / 10, s.power * 11 / 10) * state.healBonus());
                    int before = t.hp;
                    t.hp = Math.min(t.maxHp(), t.hp + amount);
                    log.add(Josa.i(t.name()) + " HP를 " + (t.hp - before) + " 회복했다.");
                }
                break;
            case RpgData.BIG_HIT: {
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                log.add("배달 가방의 문장이 눈부시게 빛난다!");
                if (dodged(t, false, log)) return;
                // 방어를 무시하는 일격
                hitEnemy(t, rand(s.power * 9 / 10, s.power * 11 / 10) + h.atk(), log);
                break;
            }
            case RpgData.MULTI_HIT:
                for (int k = 0; k < s.power; k++) {
                    Enemy t = k == 0 ? enemyTarget(a.target) : enemyTarget(-1);
                    if (t == null) break;
                    if (dodged(t, false, log)) continue;
                    hitEnemy(t, (int) Math.round(damage(h.atk(), t.m.def) * 1.1), log);
                }
                break;
            default:
                break;
        }
    }

    private void hitEnemy(Enemy t, int dmg, List<String> log) {
        if (acting != null && dmg > 0) {
            dmg = (int) Math.min(Integer.MAX_VALUE / 4, Math.round(dmg * acting.slayer() * atkMul));
            if (acting.critChance() > 0 && random.nextDouble() < acting.critChance()) {
                dmg *= 2;
                log.add("⚡ 회심의 일격!");
            }
        }
        if (dmg <= 0) {
            log.add("미스! " + Josa.eun(t.name) + " 피해를 받지 않았다.");
            return;
        }
        t.hp = Math.max(0, t.hp - dmg);
        log.add(t.name + "에게 " + dmg + "의 피해!");
        if (!t.alive()) {
            if (!boss) log.add(Josa.eul(t.name) + " 물리쳤다!");
            return;
        }
        if (boss && !enraged && t.hp <= t.m.hp / 2) {
            enraged = true;
            enragedThisRound = true;
            log.add("……!");
            log.add(Josa.i(t.name) + " 몸을 부르르 떤다. 눈이 붉게 타오른다!");
            log.add(Josa.eun(t.name) + " 격노했다! 공격이 한층 거세진다!");
            for (String l : RpgData.bossScript(t.m.sprite)[0]) log.add(l);
        } else if (boss && !faltered && t.hp <= t.m.hp / 5) {
            faltered = true;
            log.add("wolfking".equals(t.m.sprite) ? Josa.i(t.name) + " 비틀거린다. 검은 털 사이로 은빛이 언뜻 비친다…"
                    : Josa.i(t.name) + " 크게 비틀거린다!");
            for (String l : RpgData.bossScript(t.m.sprite)[1]) log.add(l);
        }
    }

    private void enemyAct(Enemy e, List<String> log) {
        // 진귀한 경험치 몬스터는 틈만 나면 도망친다
        if (e.m.flee > 0 && random.nextDouble() < e.m.flee) {
            log.add(Josa.eun(e.name) + " 도망쳐 버렸다!");
            e.fled = true;
            e.hp = 0;
            return;
        }
        if (e.m.special == 1 && random.nextDouble() < 0.3) {
            Hero t = randomAliveHero();
            if (t == null) return;
            log.add(Josa.eun(e.name) + (e.m.level >= 32 ? " 검은 불꽃을 터뜨렸다!" : e.m.level >= 25 ? " 화염을 내뿜었다!"
                    : e.m.level >= 12 ? " 바닷물 폭탄을 쏘았다!" : " 불씨를 뱉었다!"));
            hurtHero(t, (int) Math.round(rand(Math.max(7, e.m.atk / 2), Math.max(11, e.m.atk * 4 / 5)) * guard), log);
            return;
        }
        if (e.m.special == 2 && random.nextDouble() < (enraged ? 0.4 : 0.25)) {
            String move = roar(e.m.sprite);
            log.add(Josa.eun(e.name) + move);
            for (Hero t : state.party) {
                if (t.alive()) hurtHero(t, (int) Math.round(rand(e.m.atk / 2, e.m.atk * 7 / 10) * (enraged ? 1.3 : 1) * guard / defMul), log);
            }
            return;
        }
        Hero t = randomAliveHero();
        if (t == null) return;
        log.add(Josa.i(e.name) + " 덤벼들었다!");
        if (partyEvade > 0 && random.nextDouble() < partyEvade) {
            log.add("행운의 저울이 기울었다! " + Josa.eun(t.name()) + " 공격을 가볍게 피했다!");
            return;
        }
        int idx = state.party.indexOf(t);
        int atk = enraged ? (int) Math.round(e.m.atk * 1.3) : e.m.atk;
        int dmg = (int) Math.round(damage(atk, (int) Math.round(t.def() * defMul)) * guard);
        if (idx >= 0 && defending[idx]) dmg /= 2;
        hurtHero(t, dmg, log);
    }

    private static String roar(String sprite) {
        switch (sprite) {
            case "galleon": return " 갑판 대포를 마구 쏘아 댔다!";
            case "serpent": return " 검은 해일을 일으켰다!";
            case "giant": return " 용암 덩어리를 내리쳤다!";
            case "ignis": return " 화염의 검을 휘둘러 불의 파도를 일으켰다!";
            case "witch": return " 독안개를 퍼뜨렸다!";
            case "lavadragon": return " 용암 브레스를 뿜었다!";
            case "vargas": return " 공허의 검은 불꽃을 터뜨렸다!";
            case "fox": return " 아홉 꼬리로 여우불을 퍼부었다!";
            case "guardian": return " 대지를 내리쳐 바위를 쏟아냈다!";
            case "crystalqueen": return " 수정 가시를 사방으로 쏘았다!";
            case "demonking": return " 요마의 검은 낙뢰를 내리쳤다!";
            case "mammon": return " 황금 비를 퍼부었다!";
            case "arsen": return " 공허의 문을 열어 검은 바람을 쏟아냈다!";
            case "belial": return " 수천 개의 거짓 약속을 외쳤다!";
            case "beelzebub": return " 수억 마리 파리 떼를 쏟아냈다!";
            default: return " 어둠의 포효를 내질렀다!";
        }
    }

    private void hurtHero(Hero t, int dmg, List<String> log) {
        if (dmg <= 0) {
            log.add(Josa.eun(t.name()) + " 재빨리 피했다!");
            return;
        }
        t.hp = Math.max(0, t.hp - dmg);
        log.add(Josa.eun(t.name()) + " " + dmg + "의 피해를 입었다!");
        if (!t.alive()) log.add(Josa.eun(t.name()) + " 쓰러졌다!");
    }
}
