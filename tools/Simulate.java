import com.shinchang.adventure.game.GameData;
import com.shinchang.adventure.game.GameState;
import com.shinchang.adventure.game.Money;

/**
 * 밸런스 시뮬레이터: 초당 N회 손으로 탭하면서 가장 효율 좋은 아이템을 사는 플레이어를 가정한다.
 * 실행: javac -d out app/src/main/java/com/shinchang/adventure/game/*.java tools/Simulate.java && java -cp out Simulate 3
 */
public class Simulate {
    public static void main(String[] args) {
        double tapsPerSec = args.length > 0 ? Double.parseDouble(args[0]) : 3;
        GameState s = new GameState();
        double t = 0, dt = 1;
        int lastJob = -1;
        while (!s.isEnding() && t < 100 * 3600) {
            if (s.job != lastJob) {
                System.out.printf("%6.1f분  %s 시작  (속도 %s회/초)%n", t / 60, s.currentJob().name, Money.speed(s.autoSpeed()));
                lastJob = s.job;
            }
            s.update(dt);
            s.earn(s.tapValue() * tapsPerSec * dt);
            t += dt;
            if (s.canPromote()) { s.promote(); continue; }
            // 가성비(속도/가격) 최고 아이템 구매
            boolean bought = true;
            while (bought) {
                bought = false;
                int best = -1; double bestRatio = 0;
                GameData.Item[] items = s.currentJob().items;
                for (int k = 0; k < items.length; k++) {
                    if (!s.isUnlocked(k) || s.isMaxed(k)) continue;
                    double c = s.itemCost(k);
                    double r = c == 0 ? Double.MAX_VALUE : items[k].speedPerLevel / c;
                    // 다음 아이템 해금을 위해 첫 레벨은 우대
                    if (s.level(k) == 0) r *= 3;
                    if (r > bestRatio) { bestRatio = r; best = k; }
                }
                if (best >= 0 && s.canBuy(best)) { s.buy(best); bought = true; }
            }
        }
        System.out.printf("%6.1f분  엔딩! 총수익 %s, 속도 %s회/초%n", t / 60, Money.format(s.totalEarned), Money.speed(s.autoSpeed()));
        GameState r = GameState.deserialize(s.serialize());
        System.out.println("저장/불러오기 일치: " + r.serialize().equals(s.serialize()));
        for (double v : new double[]{100, 12_345, 3e8, 123_456_789_012.0, 5.5e16}) System.out.println(v + " -> " + Money.format(v));
    }
}
