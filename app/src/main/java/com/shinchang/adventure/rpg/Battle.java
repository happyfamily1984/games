package com.shinchang.adventure.rpg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** 턴제 전투. 한 라운드씩 명령을 받아 결과 메시지를 돌려준다. */
public final class Battle {

    public static final int ATTACK = 0, SPELL = 1, ITEM = 2, DEFEND = 3, FLEE = 4;

    public static final class Enemy {
        public final RpgData.Monster m;
        public final String name;
        public int hp;

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
    private final RpgState state;
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

    public Battle(RpgState state, int[] monsterIds, boolean boss, Random random) {
        this.state = state;
        this.boss = boss;
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
            totalGold += m.gold;
        }
    }

    public String intro() {
        if (boss) return Josa.i(enemies.get(0).name) + " 덤벼들었다!";
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

        if (!actions.isEmpty() && actions.get(0).cmd == FLEE) {
            if (!boss && random.nextDouble() < 0.6) {
                log.add("신창 일행은 도망쳤다!");
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
                if (e.m.special == 2 && random.nextDouble() < (enraged ? 0.55 : 0.35) && !lost()) {
                    log.add(Josa.eun(e.name) + " 쉴 틈도 주지 않는다!");
                    enemyAct(e, log);
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
        }
        finishLog(log);
    }

    private void finishLog(List<String> log) {
        if (won()) {
            double base = 0, bonus = 0, pl = partyLevel();
            for (Enemy e : enemies) {
                base += e.m.exp;
                bonus += e.m.exp * expFactor(e.m.level, pl);
            }
            totalExp = (int) Math.round(bonus);
            log.add(boss ? Josa.eul(enemies.get(0).name) + " 쓰러뜨렸다!" : "몬스터를 모두 물리쳤다!");
            if (bonus > base * 1.05) log.add("강적을 물리쳤다! 경험치 보너스 ×" + String.format(java.util.Locale.ROOT, "%.1f", bonus / base));
            log.add(totalExp + "의 경험치를 얻었다. " + totalGold + "골드를 손에 넣었다.");
            state.gold += totalGold;
            for (Hero h : state.party) log.addAll(h.gainExp(totalExp));
        } else if (lost()) {
            log.add("신창 일행은 전멸했다……");
        }
    }

    private void heroAct(Hero h, Action a, List<String> log) {
        switch (a.cmd) {
            case ATTACK: {
                Enemy t = enemyTarget(a.target);
                if (t == null) return;
                log.add(Josa.eun(h.name()) + " " + Josa.eul(t.name) + " 공격했다!");
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
                hitEnemy(t, (int) Math.round(rand(s.power * 8 / 10, s.power * 12 / 10) * state.fireBonus()), log);
                break;
            }
            case RpgData.FIRE_ALL:
                for (Enemy t : aliveEnemies()) {
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
                // 방어를 무시하는 일격
                hitEnemy(t, rand(s.power * 9 / 10, s.power * 11 / 10) + h.atk(), log);
                break;
            }
            case RpgData.MULTI_HIT:
                for (int k = 0; k < s.power; k++) {
                    Enemy t = k == 0 ? enemyTarget(a.target) : enemyTarget(-1);
                    if (t == null) break;
                    hitEnemy(t, (int) Math.round(damage(h.atk(), t.m.def) * 1.1), log);
                }
                break;
            default:
                break;
        }
    }

    private void hitEnemy(Enemy t, int dmg, List<String> log) {
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
            log.add(Josa.i(t.name) + " 비틀거린다. 검은 털 사이로 은빛이 언뜻 비친다…");
            for (String l : RpgData.bossScript(t.m.sprite)[1]) log.add(l);
        }
    }

    private void enemyAct(Enemy e, List<String> log) {
        if (e.m.special == 1 && random.nextDouble() < 0.3) {
            Hero t = randomAliveHero();
            if (t == null) return;
            log.add(Josa.eun(e.name) + " 불씨를 뱉었다!");
            hurtHero(t, rand(7, 11), log);
            return;
        }
        if (e.m.special == 2 && random.nextDouble() < (enraged ? 0.4 : 0.25)) {
            log.add(Josa.eun(e.name) + " 어둠의 포효를 내질렀다!");
            for (Hero t : state.party) if (t.alive()) hurtHero(t, (int) Math.round(rand(22, 32) * (enraged ? 1.3 : 1) * guard), log);
            return;
        }
        Hero t = randomAliveHero();
        if (t == null) return;
        log.add(Josa.i(e.name) + " 덤벼들었다!");
        int idx = state.party.indexOf(t);
        int atk = enraged ? (int) Math.round(e.m.atk * 1.3) : e.m.atk;
        int dmg = (int) Math.round(damage(atk, t.def()) * guard);
        if (idx >= 0 && defending[idx]) dmg /= 2;
        hurtHero(t, dmg, log);
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
