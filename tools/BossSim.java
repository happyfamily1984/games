// 보스 난이도 측정: 레벨별로 보스전을 여러 번 돌려 승률을 낸다.
// 실행: javac -encoding UTF-8 -d out app/src/main/java/com/shinchang/adventure/rpg/{RpgData,RpgMaps,RpgState,Hero,Battle,Josa}.java tools/BossSim.java && java -cp out BossSim
import com.shinchang.adventure.rpg.*;
import java.util.*;

public class BossSim {
    /** 앱의 자동 전투와 같은 AI */
    public static List<Battle.Action> ai(RpgState s, Battle b, int round) {
        return BattleAi.choose(s, b);
    }

    static int[] ids = {0, 1, 2};
    static boolean awake;

    static RpgState party(int lv, int members, int[][] gear, int[][] items) {
        RpgState s = new RpgState();
        s.perkFaith = s.perkFire = true;
        s.party.clear();
        for (int i = 0; i < members; i++) {
            Hero h = new Hero(ids[i], ids[i] == 3 ? Math.max(20, lv - 3) : lv);
            h.awakened = awake;
            if (awake) h.accessory = RpgData.VOW_RING;
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
        ids = new int[]{0, 3};
        run("3장 화염 거인 (신창 흑요석 검+화산석 / 지헌 망치+화산재 로브, 상급 약초 6·소생 2·마나 3)", RpgData.GIANT, 2,
                new int[][]{{RpgData.OBSIDIAN_SWORD, RpgData.VOLCANO_ARMOR}, {-1, RpgData.ASH_ROBE}},
                new int[][]{{RpgData.HI_HERB, 6}, {RpgData.MANA_CANDY, 3}, {RpgData.PHOENIX, 2}}, 26, 34);
        awake = true;
        int[][] wed = {{RpgData.HERO_SWORD, RpgData.PHOENIX_ARMOR}, {-1, RpgData.BRIDE_DRESS}};
        int[][] elix = {{RpgData.ELIXIR, 3}, {RpgData.HI_HERB, 6}};
        run("3장 (각성 후) 화염 기사단장 이그론", RpgData.IGRON, 2, wed, elix, 30, 34);
        run("3장 (각성 후) 독안개 마녀 모르가", RpgData.WITCH, 2, wed, elix, 30, 34);
        run("3장 (각성 후) 용암룡 볼카", RpgData.VOLKA, 2, wed, elix, 30, 34);
        run("3장 (각성 후) 흑염의 마장군 바르가스", RpgData.VARGAS, 2, wed, elix, 30, 38);
        System.out.println("== 요새 잡몹 무리 (각성 후 Lv32): 한 번 전투에 걸리는 라운드");
        for (int[] g : RpgData.ENCOUNTERS[15]) {
            Random rnd = new Random(3); int wins = 0, rounds = 0;
            for (int t = 0; t < 200; t++) {
                RpgState s = party(32, 2, wed, elix); s.setRandom(rnd);
                Battle bt = new Battle(s, g, false, rnd); int r = 0;
                while (!bt.over() && r < 40) { bt.playRound(ai(s, bt, r)); r++; }
                if (bt.won()) { wins++; rounds += r; }
            }
            System.out.printf("  %-40s 승률 %3d%%  %.1f라운드%n", java.util.Arrays.toString(g), wins / 2, rounds / (double) Math.max(1, wins));
        }
    }
}
