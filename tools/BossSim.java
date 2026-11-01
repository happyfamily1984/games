// 보스 난이도 측정: 레벨별로 보스전을 여러 번 돌려 승률을 낸다.
// 실행: javac -encoding UTF-8 -d out app/src/main/java/com/shinchang/adventure/rpg/{RpgData,RpgMaps,RpgState,Hero,Battle,Josa}.java tools/BossSim.java && java -cp out BossSim
import com.shinchang.adventure.rpg.*;
import java.util.*;

public class BossSim {
    /** 간단하지만 기술을 잘 쓰는 전투 AI (자동 플레이 검사와 같은 규칙) */
    public static List<Battle.Action> ai(RpgState s, Battle b, int round) {
        List<Battle.Action> acts = new ArrayList<>();
        int hurt = 0;
        for (Hero o : s.party) if (o.alive() && o.hp < o.maxHp() * 0.55) hurt++;
        for (int i = 0; i < s.party.size(); i++) {
            Hero h = s.party.get(i);
            Integer healOne = null, healAll = null, big = null, multi = null, power = null, fireAll = null, fire = null, guard = null;
            for (int sp : h.spells()) {
                RpgData.Spell x = RpgData.SPELLS[sp];
                if (h.mp < x.mp) continue;
                switch (x.kind) {
                    case RpgData.HEAL_ONE: healOne = sp; break;
                    case RpgData.HEAL_ALL: healAll = sp; break;
                    case RpgData.BIG_HIT: big = sp; break;
                    case RpgData.MULTI_HIT: multi = sp; break;
                    case RpgData.POWER_HIT: power = sp; break;
                    case RpgData.FIRE_ALL: fireAll = sp; break;
                    case RpgData.FIRE_ONE: fire = sp; break;
                    case RpgData.GUARD: guard = sp; break;
                    default: break;
                }
            }
            Hero weak = null;
            for (Hero o : s.party) if (o.alive() && o.hp < o.maxHp() * 0.45 && (weak == null || o.hp < weak.hp)) weak = o;
            if (hurt >= 2 && healAll != null) acts.add(new Battle.Action(Battle.SPELL, -1, healAll));
            else if (weak != null && healOne != null) acts.add(new Battle.Action(Battle.SPELL, s.party.indexOf(weak), healOne));
            else if (weak != null && s.hasItem(RpgData.HERB)) acts.add(new Battle.Action(Battle.ITEM, s.party.indexOf(weak), RpgData.HERB));
            else if (b.boss && round == 0 && guard != null) acts.add(new Battle.Action(Battle.SPELL, -1, guard));
            else if (b.aliveEnemies().size() >= 2 && fireAll != null) acts.add(new Battle.Action(Battle.SPELL, -1, fireAll));
            else if (b.boss && big != null) acts.add(new Battle.Action(Battle.SPELL, -1, big));
            else if (b.boss && multi != null) acts.add(new Battle.Action(Battle.SPELL, -1, multi));
            else if (b.boss && power != null) acts.add(new Battle.Action(Battle.SPELL, -1, power));
            else if (b.boss && fire != null) acts.add(new Battle.Action(Battle.SPELL, -1, fire));
            else if (b.boss && h.mp < 6 && s.hasItem(RpgData.MANA_CANDY)) acts.add(new Battle.Action(Battle.ITEM, i, RpgData.MANA_CANDY));
            else acts.add(new Battle.Action(Battle.ATTACK, -1, 0));
        }
        return acts;
    }

    public static void main(String[] args) {
        Random rnd = new Random(1);
        System.out.println("레벨  승률   평균 라운드  (신창: 철검+사슬 갑옷, 리나: 견습 기사의 검+은빛 갑옷, 약초 8·마나 사탕 3)");
        for (int lv = 8; lv <= 16; lv++) {
            int wins = 0, rounds = 0, trials = 400;
            for (int t = 0; t < trials; t++) {
                RpgState s = new RpgState();
                s.setRandom(rnd);
                s.perkFaith = s.perkFire = true;
                s.party.clear();
                Hero a = new Hero(0, lv); a.weapon = RpgData.IRON_SWORD; a.armor = RpgData.CHAIN_MAIL; a.fullHeal();
                Hero b = new Hero(1, lv); b.armor = RpgData.SILVER_ARMOR; b.fullHeal();
                s.party.add(a); s.party.add(b);
                s.items[RpgData.HERB] = 8; s.items[RpgData.MANA_CANDY] = 3;
                Battle bt = new Battle(s, new int[]{RpgData.WOLF_KING}, true, rnd);
                int r = 0;
                while (!bt.over() && r < 80) { bt.playRound(ai(s, bt, r)); r++; }
                if (bt.won()) { wins++; rounds += r; }
            }
            System.out.printf("Lv%-3d %3d%%   %s%n", lv, wins * 100 / trials, wins > 0 ? String.format("%.1f", rounds / (double) wins) : "-");
        }
    }
}
