package com.shinchang.adventure.game;

import java.util.Locale;

/**
 * 게임 진행 상태와 규칙. 안드로이드 의존성이 없어서 PC에서도 테스트/시뮬레이션할 수 있다.
 */
public final class GameState {

    /** 앱을 끈 동안 벌리는 금액 비율 */
    public static final double OFFLINE_RATE = 0.5;
    /** 오프라인 수익 최대 인정 시간 */
    public static final double OFFLINE_MAX_SECONDS = 8 * 3600;

    public static final double TAP_BONUS_PER_SPEED = 0.1;

    private static final String SAVE_VERSION = "v1";

    public double money;
    public double totalEarned;
    public int job;
    /** levels[직업][아이템] */
    public final int[][] levels;
    public boolean autoOn = true;
    public boolean endingSeen;
    public long manualTaps;
    public long lastSavedMillis;

    public GameState() {
        levels = new int[GameData.JOBS.length][];
        for (int j = 0; j < GameData.JOBS.length; j++) {
            levels[j] = new int[GameData.JOBS[j].items.length];
        }
        // 기본형 아이템(도보 배달)은 처음부터 가지고 시작
        levels[0][0] = 1;
    }

    public GameData.Job currentJob() {
        return GameData.JOBS[job];
    }

    /** 직업이 정하는 기본 클릭 금액. 오토클릭 1회도 이 금액을 번다. */
    public double clickValue() {
        return currentJob().clickValue;
    }

    /** 손으로 누르는 클릭은 오토클릭 속도에 비례한 보너스를 받는다 (속도 1회/초당 +10%). */
    public double tapValue() {
        return clickValue() * (1 + autoSpeed() * TAP_BONUS_PER_SPEED);
    }

    /** 오토클릭 속도(회/초). 이전 직업에서 산 아이템도 계속 효과가 있다. */
    public double autoSpeed() {
        double speed = 0;
        for (int j = 0; j <= job; j++) {
            GameData.Item[] items = GameData.JOBS[j].items;
            for (int k = 0; k < items.length; k++) {
                speed += levels[j][k] * items[k].speedPerLevel;
            }
        }
        return speed;
    }

    public double incomePerSecond() {
        return autoOn ? autoSpeed() * clickValue() : 0;
    }

    public void earn(double amount) {
        money += amount;
        totalEarned += amount;
    }

    public double tap() {
        double v = tapValue();
        earn(v);
        manualTaps++;
        return v;
    }

    public void update(double seconds) {
        if (seconds > 0) {
            earn(incomePerSecond() * seconds);
        }
    }

    // ---- 아이템 ----

    public int level(int item) {
        return levels[job][item];
    }

    public double itemCost(int item) {
        GameData.Item it = currentJob().items[item];
        return it.baseCost * Math.pow(GameData.ITEM_COST_GROWTH, levels[job][item]);
    }

    public boolean isUnlocked(int item) {
        return item == 0 || levels[job][item - 1] > 0;
    }

    public boolean isMaxed(int item) {
        return levels[job][item] >= GameData.ITEM_MAX_LEVEL;
    }

    public boolean canBuy(int item) {
        return isUnlocked(item) && !isMaxed(item) && money >= itemCost(item);
    }

    public boolean buy(int item) {
        if (!canBuy(item)) return false;
        money -= itemCost(item);
        levels[job][item]++;
        return true;
    }

    // ---- 전직 ----

    public boolean hasNextJob() {
        return job < GameData.lastJob();
    }

    /** 전직 조건: 현재 직업의 마지막 아이템 보유 */
    public boolean promoteItemReady() {
        GameData.Item[] items = currentJob().items;
        return levels[job][items.length - 1] > 0;
    }

    public boolean canPromote() {
        return hasNextJob() && promoteItemReady() && money >= currentJob().promoteCost;
    }

    public boolean promote() {
        if (!canPromote()) return false;
        money -= currentJob().promoteCost;
        job++;
        if (levels[job][0] == 0) levels[job][0] = 1;
        return true;
    }

    /** 재벌 회장의 마지막 아이템까지 사면 엔딩 */
    public boolean isEnding() {
        GameData.Item[] items = GameData.JOBS[GameData.lastJob()].items;
        return job == GameData.lastJob() && levels[job][items.length - 1] > 0;
    }

    // ---- 오프라인 수익 ----

    /** 앱을 다시 켰을 때 받을 금액. 오토클릭이 켜져 있었을 때만 쌓인다. */
    public double offlineEarnings(long nowMillis) {
        if (lastSavedMillis <= 0 || nowMillis <= lastSavedMillis) return 0;
        double seconds = Math.min((nowMillis - lastSavedMillis) / 1000.0, OFFLINE_MAX_SECONDS);
        return incomePerSecond() * seconds * OFFLINE_RATE;
    }

    // ---- 저장 ----

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        sb.append(SAVE_VERSION).append(';')
                .append(money).append(';')
                .append(totalEarned).append(';')
                .append(job).append(';')
                .append(autoOn ? 1 : 0).append(';')
                .append(endingSeen ? 1 : 0).append(';')
                .append(manualTaps).append(';')
                .append(lastSavedMillis).append(';');
        for (int j = 0; j < levels.length; j++) {
            if (j > 0) sb.append('/');
            for (int k = 0; k < levels[j].length; k++) {
                if (k > 0) sb.append(',');
                sb.append(levels[j][k]);
            }
        }
        return sb.toString();
    }

    /** 저장 데이터가 깨져 있으면 새 게임을 돌려준다. */
    public static GameState deserialize(String data) {
        GameState s = new GameState();
        if (data == null || data.isEmpty()) return s;
        try {
            String[] p = data.split(";", -1);
            if (!SAVE_VERSION.equals(p[0])) return new GameState();
            s.money = Double.parseDouble(p[1]);
            s.totalEarned = Double.parseDouble(p[2]);
            s.job = Math.max(0, Math.min(GameData.lastJob(), Integer.parseInt(p[3])));
            s.autoOn = "1".equals(p[4]);
            s.endingSeen = "1".equals(p[5]);
            s.manualTaps = Long.parseLong(p[6]);
            s.lastSavedMillis = Long.parseLong(p[7]);
            String[] jobs = p[8].split("/");
            for (int j = 0; j < jobs.length && j < s.levels.length; j++) {
                String[] lv = jobs[j].split(",");
                for (int k = 0; k < lv.length && k < s.levels[j].length; k++) {
                    s.levels[j][k] = Math.max(0, Math.min(GameData.ITEM_MAX_LEVEL, Integer.parseInt(lv[k])));
                }
            }
            return s;
        } catch (RuntimeException e) {
            return new GameState();
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "job=%d money=%.0f speed=%.2f", job, money, autoSpeed());
    }
}
