import com.shinchang.adventure.game.GameData;
import com.shinchang.adventure.game.GameState;
import com.shinchang.adventure.game.Money;

/**
 * 밸런스 시뮬레이터: 초당 N회 손으로 탭하면서 효율 좋은 아이템/계약을 고르는 플레이어를 가정한다.
 * 실행: javac -d out app/src/main/java/com/shinchang/adventure/game/*.java tools/Simulate.java && java -cp out Simulate 5
 */
public class Simulate {
    public static void main(String[] args) {
        double tapsPerSec = args.length > 0 ? Double.parseDouble(args[0]) : 5;

        if (args.length > 1 && args[1].equals("contracts")) {
            System.out.println("계약              매물                       보수        클릭/초   필요 클릭속도");
            for (GameData.Contract c : GameData.CONTRACTS) {
                System.out.printf("%-10s %-24s %12s  %3d/%2d초  %.1f회/초%n",
                        c.name, c.detail, Money.format(c.reward), c.clicks, c.seconds, c.requiredCps());
            }
            return;
        }

        GameState s = new GameState();
        double t = 0, dt = 0.1, tapAcc = 0;
        int lastJob = -1, lastItemsBought = 0, lastBest = -1;
        while (!s.isEnding() && t < 200 * 3600) {
            if (s.job != lastJob) {
                System.out.printf("%7.1f분  %s 시작%n", t / 60, s.currentJob().name);
                lastJob = s.job;
            }
            if (s.isBroker() && s.activeContract < 0) {
                int best = bestContract(s, tapsPerSec);
                if (best >= 0) {
                    if (best != lastBest) System.out.printf("%7.1f분    · %s 계약 반복 (보유 %s)%n", t / 60, GameData.CONTRACTS[best].name, Money.format(s.money));
                    lastBest = best;
                    s.startContract(best);
                }
            }
            tapAcc += tapsPerSec * dt;
            while (tapAcc >= 1) {
                s.tap();
                tapAcc -= 1;
            }
            s.update(dt);
            t += dt;
            if (s.canPromote()) {
                s.promote();
                continue;
            }
            int bought = buyItems(s);
            if (bought > 0 && s.job <= 1) {
                lastItemsBought += bought;
                System.out.printf("%7.1f분    └ %s 구매 (오토 %s회/초)%n", t / 60, bestOwned(s), Money.speed(s.autoSpeed()));
            }
        }
        System.out.printf("%7.1f분  엔딩! 총수익 %s%n", t / 60, Money.format(s.totalEarned));
        GameState r = GameState.deserialize(s.serialize());
        System.out.println("저장/불러오기 일치: " + r.serialize().equals(s.serialize()));
    }

    private static String bestOwned(GameState s) {
        GameData.Item[] items = s.currentJob().items;
        for (int k = items.length - 1; k >= 0; k--) if (s.level(k) > 0) return items[k].name;
        return "-";
    }

    /** 손+오토 속도로 시간 안에 끝낼 수 있는 계약 중 보수가 가장 큰 것 (실제 플레이어처럼 새 계약 우선) */
    private static int bestContract(GameState s, double taps) {
        double speed = taps + s.autoSpeed();
        int best = -1;
        double bestRate = 0;
        for (int i = 0; i < GameData.CONTRACTS.length; i++) {
            GameData.Contract c = GameData.CONTRACTS[i];
            if (!s.isContractUnlocked(i)) continue;
            double need = c.clicks / speed;
            if (need > c.seconds * 0.95) continue;
            // 아직 3번 못 채운 새 계약을 우선 (다음 계약을 열기 위해)
            double rate = c.reward * (s.contractDone[i] < GameData.CONTRACT_UNLOCK_COUNT ? 1000 : 1);
            if (rate > bestRate) {
                bestRate = rate;
                best = i;
            }
        }
        return best;
    }

    /** 오토 속도 증가량 대비 가격이 가장 좋은 아이템부터 산다 */
    private static int buyItems(GameState s) {
        int count = 0;
        while (true) {
            int best = -1;
            double bestRatio = 0;
            GameData.Item[] items = s.currentJob().items;
            double now = s.autoSpeed();
            for (int k = 0; k < items.length; k++) {
                if (!s.isUnlocked(k) || s.isMaxed(k)) continue;
                double c = s.itemCost(k);
                double gain = s.currentJob().bestItemOnly ? Math.max(0.01, items[k].speedPerLevel - now) : items[k].speedPerLevel;
                double r = c == 0 ? Double.MAX_VALUE : gain / c;
                // 다음 아이템 해금(=전직 조건)을 위해 첫 레벨은 우대
                if (s.level(k) == 0) r *= 3;
                if (r > bestRatio) {
                    bestRatio = r;
                    best = k;
                }
            }
            if (best < 0 || !s.canBuy(best)) return count;
            s.buy(best);
            count++;
        }
    }
}
