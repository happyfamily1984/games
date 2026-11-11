// 보스 난이도 측정: 레벨별로 보스전을 여러 번 돌려 승률을 낸다.
// 실행: javac -encoding UTF-8 -d out app/src/main/java/com/shinchang/adventure/rpg/{RpgData,RpgMaps,RpgState,Hero,Battle,Josa}.java tools/BossSim.java && java -cp out BossSim
import com.shinchang.adventure.rpg.*;
import java.util.*;

public class BossSim {
    /** 기술을 잘 쓰는 전투 AI (자동 플레이 검사와 같은 규칙) */
    public static List<Battle.Action> ai(RpgState s, Battle b, int round) {
        List<Battle.Action> acts = new ArrayList<>();
        int hurt = 0;
        Hero dead = null;
        for (Hero o : s.party) {
            if (o.alive() && o.hp < o.maxHp() * 0.55) hurt++;
            if (!o.alive() && dead == null) dead = o;
        }
        boolean group = b.aliveEnemies().size() >= 2;
        boolean revived = false;
        for (int i = 0; i < s.party.size(); i++) {
            Hero h = s.party.get(i);
            Map<Integer, Integer> sp = new HashMap<>();
            for (int k : h.spells()) {
                RpgData.Spell x = RpgData.SPELLS[k];
                if (h.mp < x.mp) continue;
                // 같은 종류면 더 강한(나중에 배운) 기술
                sp.put(x.kind, k);
            }
            Hero weak = null;
            for (Hero o : s.party) if (o.alive() && o.hp < o.maxHp() * 0.45 && (weak == null || o.hp < weak.hp)) weak = o;
            Battle.Action a;
            if (dead != null && !revived && sp.containsKey(RpgData.REVIVE)) {
                a = new Battle.Action(Battle.SPELL, s.party.indexOf(dead), sp.get(RpgData.REVIVE));
                revived = true;
            } else if (dead != null && !revived && s.hasItem(RpgData.PHOENIX)) {
                a = new Battle.Action(Battle.ITEM, s.party.indexOf(dead), RpgData.PHOENIX);
                revived = true;
            } else if (hurt >= 2 && sp.containsKey(RpgData.HEAL_ALL)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.HEAL_ALL));
            } else if (weak != null && sp.containsKey(RpgData.HEAL_ONE)) {
                a = new Battle.Action(Battle.SPELL, s.party.indexOf(weak), sp.get(RpgData.HEAL_ONE));
            } else if (weak != null && s.hasItem(RpgData.HI_HERB)) {
                a = new Battle.Action(Battle.ITEM, s.party.indexOf(weak), RpgData.HI_HERB);
            } else if (weak != null && s.hasItem(RpgData.HERB)) {
                a = new Battle.Action(Battle.ITEM, s.party.indexOf(weak), RpgData.HERB);
            } else if (b.boss && round == 0 && sp.containsKey(RpgData.GUARD)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.GUARD));
            } else if (b.boss && round <= 1 && sp.containsKey(RpgData.EVADE_UP)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.EVADE_UP));
            } else if (group && sp.containsKey(RpgData.FIRE_ALL)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.FIRE_ALL));
            } else if (group && sp.containsKey(RpgData.WATER_ALL)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.WATER_ALL));
            } else if (group && sp.containsKey(RpgData.PHYS_ALL)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.PHYS_ALL));
            } else if (b.boss && sp.containsKey(RpgData.BIG_HIT)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.BIG_HIT));
            } else if (b.boss && sp.containsKey(RpgData.MULTI_HIT)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.MULTI_HIT));
            } else if (b.boss && sp.containsKey(RpgData.POWER_HIT)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.POWER_HIT));
            } else if (b.boss && sp.containsKey(RpgData.WATER_ONE)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.WATER_ONE));
            } else if (b.boss && sp.containsKey(RpgData.FIRE_ONE)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.FIRE_ONE));
            } else if (b.boss && h.mp < 8 && s.hasItem(RpgData.MANA_CANDY)) {
                a = new Battle.Action(Battle.ITEM, i, RpgData.MANA_CANDY);
            } else {
                a = new Battle.Action(Battle.ATTACK, -1, 0);
            }
            acts.add(a);
        }
        return acts;
    }

    static RpgState party(int lv, int members, int[][] gear, int[][] items) {
        RpgState s = new RpgState();
        s.perkFaith = s.perkFire = true;
        s.party.clear();
        for (int i = 0; i < members; i++) {
            Hero h = new Hero(i, lv);
            if (gear[i][0] >= 0) h.weapon = gear[i][0];
            if (gear[i][1] >= 0) h.armor = gear[i][1];
            h.fullHeal();
            s.party.add(h);
        }
        for (int[] it : items) s.items[it[0]] = it[1];
        return s;
    }

    static void run(String title, int boss, int members, int[][] gear, int[][] items, int from, int to) {
        System.out.println("== " + title);
        Random rnd = new Random(1);
        for (int lv = from; lv <= to; lv++) {
            int wins = 0, rounds = 0, trials = 300;
            for (int t = 0; t < trials; t++) {
                RpgState s = party(lv, members, gear, items);
                s.setRandom(rnd);
                Battle bt = new Battle(s, new int[]{boss}, true, rnd);
                int r = 0;
                while (!bt.over() && r < 80) { bt.playRound(ai(s, bt, r)); r++; }
                if (bt.won()) { wins++; rounds += r; }
            }
            System.out.printf("  Lv%-3d %3d%%   %s라운드%n", lv, wins * 100 / trials, wins > 0 ? String.format("%.1f", rounds / (double) wins) : "-");
        }
    }

    public static void main(String[] args) {
        run("1장 그림자 늑대왕 (신창 철검+사슬, 리나 은빛 갑옷, 약초 8·마나 사탕 3)", RpgData.WOLF_KING, 2,
                new int[][]{{RpgData.IRON_SWORD, RpgData.CHAIN_MAIL}, {-1, RpgData.SILVER_ARMOR}},
                new int[][]{{RpgData.HERB, 8}, {RpgData.MANA_CANDY, 3}}, 10, 14);
        run("2장 중간 보스 붉은 수염 갈리온 (신창·리나, 강철검+강철 갑옷 / 기사의 창+은빛 갑옷, 상급 약초 6·마나 사탕 3)", RpgData.GALLEON, 2,
                new int[][]{{RpgData.STEEL_SWORD, RpgData.STEEL_ARMOR}, {RpgData.KNIGHT_SPEAR, RpgData.SILVER_ARMOR}},
                new int[][]{{RpgData.HI_HERB, 6}, {RpgData.MANA_CANDY, 3}, {RpgData.PHOENIX, 1}}, 16, 23);
        run("2장 보스 심해의 군주 (3명, 해룡의 창+산호 / 강철검+산호 / 동전 주머니+요정 망토, 상급 약초 8·소생 2·마나 4)", RpgData.SERPENT, 3,
                new int[][]{{RpgData.DRAGON_SPEAR, RpgData.CORAL_ARMOR}, {RpgData.STEEL_SWORD, RpgData.CORAL_ARMOR}, {-1, -1}},
                new int[][]{{RpgData.HI_HERB, 8}, {RpgData.MANA_CANDY, 4}, {RpgData.PHOENIX, 2}}, 21, 30);
    }
}
