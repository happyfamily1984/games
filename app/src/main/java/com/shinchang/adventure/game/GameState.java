package com.shinchang.adventure.game;

import java.util.Locale;
import java.util.Random;

/**
 * 게임 진행 상태와 규칙. 안드로이드 의존성이 없어서 PC에서도 테스트/시뮬레이션할 수 있다.
 */
public final class GameState {

    /** 앱을 끈 동안 벌리는 금액 비율 */
    public static final double OFFLINE_RATE = 0.5;
    /** 오프라인 수익 최대 인정 시간 */
    public static final double OFFLINE_MAX_SECONDS = 8 * 3600;
    /** 오토 모드에서 계약이 끝난 뒤 같은 계약을 다시 시작하기까지의 대기 시간 */
    public static final double CONTRACT_RESTART_SECONDS = 1.0;

    private static final String SAVE_VERSION = "v7";

    public double money;
    /** 이번 회차(교회 기부 이후) 총수입. 별 개수 계산에 쓴다. */
    public double totalEarned;

    // ---- 회차를 넘어 남는 것 ----
    /** 교회 기부로 받은 별. 완전 리셋 때만 사라진다. */
    public long stars;
    /** 완전 리셋으로 받은 해 (기능은 추후 업데이트) */
    public long suns;
    public int donations;
    /** 신창에게 투자한 항목. 교회 기부로 리셋해도 남는다. */
    public final boolean[] selfOwned = new boolean[GameData.SELF_INVESTMENTS.length];

    // ---- 종교계 (회차·완전 리셋과 무관하게 남는다) ----
    /** 한 번이라도 종교에 귀의했는지 */
    public boolean religionUnlocked;
    /** 지금 종교계 화면에 있는지 */
    public boolean inReligion;
    public int religion;
    public int rank;
    /** 영성 포인트 */
    public double sp;
    public double spTotal;
    /** 종교계 별: 세속 별 100개를 교회에 기부할 때마다 1개 */
    public long holyStars;
    public int job;
    /** 지금까지 도달한 가장 높은 직업. 그 아래 직업으로는 언제든 전환할 수 있다. */
    public int highestJob;
    /** levels[직업][아이템] */
    public final int[][] levels;
    public boolean autoOn = true;
    public boolean endingSeen;
    public long manualTaps;
    public long lastSavedMillis;

    // ---- 계약 (공인중개사·소방기술사) ----
    /** contractDone[직업][계약] = 성사 횟수 */
    public final int[][] contractDone;
    /** 진행 중인 계약 (-1 이면 없음) */
    public int activeContract = -1;
    public double contractClicks;
    public double contractTimeLeft;
    /** 오토 모드에서 반복할 계약 */
    public int lastContract = -1;
    private double restartWait;
    /** 소방기술사: 오토 수익 배수가 남은 시간 */
    public double boostLeft;

    // ---- 배달 탈것 / 날씨 ----
    /** 지금 타고 있는 탈것 (배달원) */
    public int selectedVehicle;
    /** 직업별로 지금까지 산 적 있는 가장 높은 아이템 (팔아도 다음 아이템 해금은 유지) */
    public final int[] maxEverItem = new int[GameData.JOBS.length];
    public int weather = GameData.CLEAR;
    public double weatherLeft;
    /** 마지막 손 클릭 뒤 지난 시간. 오토가 꺼져 있고 손도 안 쓰면 배달 중이 아니므로 사고도 없다. */
    private double sinceTap = 999;
    private Random random = new Random();

    /** 날씨/사고 알림용 */
    public int weatherSeq;
    public int incidentSeq;
    /** 1 = 슬립, 2 = 사고 */
    public int incidentType;
    public double incidentPenalty;

    /** 계약 결과 알림용. 결과가 나올 때마다 eventSeq 가 1씩 오른다. */
    public int eventSeq;
    public boolean eventSuccess;
    public int eventContract;

    public GameState() {
        levels = new int[GameData.JOBS.length][];
        contractDone = new int[GameData.JOBS.length][];
        for (int j = 0; j < GameData.JOBS.length; j++) {
            levels[j] = new int[GameData.JOBS[j].items.length];
            GameData.Contract[] cs = GameData.JOBS[j].contracts;
            contractDone[j] = new int[cs == null ? 0 : cs.length];
        }
        // 기본형 아이템(도보 배달)은 처음부터 가지고 시작
        levels[0][0] = 1;
    }

    public GameData.Job currentJob() {
        return GameData.JOBS[job];
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    public boolean isDelivery() {
        return job == 0;
    }

    public boolean isBroker() {
        return currentJob().type == GameData.JobType.BROKER;
    }

    public boolean isBoostJob() {
        return currentJob().type == GameData.JobType.BOOST;
    }

    public boolean hasContracts() {
        return currentJob().contracts != null;
    }

    /** 현재 직업의 계약 목록 (없으면 빈 배열) */
    public GameData.Contract[] contracts() {
        GameData.Contract[] cs = currentJob().contracts;
        return cs == null ? new GameData.Contract[0] : cs;
    }

    public int[] done() {
        return contractDone[job];
    }

    /** 지금 적용 중인 오토 수익 배수 */
    public double boostMultiplier() {
        return isBoostJob() && boostLeft > 0 ? GameData.BOOST_MULTIPLIER : 1;
    }

    /** 클릭 1회 금액. 공인중개사는 클릭으로 바로 돈을 벌지 않는다. */
    public double clickValue() {
        if (isBroker()) return 0;
        if (isDelivery()) return GameData.WEATHER_FEE[weather];
        return currentJob().clickValue;
    }

    /** 현재 직업의 오토클릭 속도(회/초) */
    public double autoSpeed() {
        GameData.Job j = currentJob();
        // 탈것은 겹치지 않는다: 지금 고른 탈것 하나의 속도만 적용
        if (isDelivery()) return j.items[selectedVehicle].speedPerLevel;
        double speed = 0;
        for (int k = 0; k < j.items.length; k++) {
            if (levels[job][k] == 0) continue;
            double s = levels[job][k] * j.items[k].speedPerLevel;
            speed = j.bestItemOnly ? Math.max(speed, s) : speed + s;
        }
        return speed;
    }

    /** 오토클릭으로 버는 초당 금액 (공인중개사는 반복 계약 기준 예상치, 소방기술사는 배수 포함) */
    public double incomePerSecond() {
        return baseIncomePerSecond() * boostMultiplier() * incomeMultiplier();
    }

    private double baseIncomePerSecond() {
        if (!autoOn) return 0;
        if (!isBroker()) return autoSpeed() * clickValue();
        return autoContractIncome(lastContract);
    }

    /** 오토클릭만으로 계약을 반복할 때의 초당 수입. 시간 안에 못 채우면 0. */
    public double autoContractIncome(int contract) {
        if (contract < 0) return 0;
        double speed = autoSpeed();
        GameData.Contract c = contracts()[contract];
        if (speed <= 0 || c.clicks / speed > c.seconds) return 0;
        return c.reward / (c.clicks / speed + CONTRACT_RESTART_SECONDS);
    }

    /** 게임 플레이로 번 돈. 별·자기 투자 배수를 곱해서 더하고, 실제로 더한 금액을 돌려준다. */
    public double earn(double base) {
        double actual = base * incomeMultiplier();
        receive(actual);
        return actual;
    }

    /** 이미 배수가 적용된 금액을 그대로 더한다 */
    public void receive(double actual) {
        money += actual;
        totalEarned += actual;
    }

    // ---- 수입 배수 ----

    /** 별(신의 축복) 배수: 별 1개당 +10% */
    public double starMultiplier() {
        return 1 + stars * GameData.STAR_BONUS;
    }

    /** 자기 투자로 오른 수입 비율 합 (0.25 = +25%) */
    public double selfBonus() {
        double b = 0;
        for (int i = 0; i < selfOwned.length; i++) if (selfOwned[i]) b += GameData.SELF_INVESTMENTS[i].bonus;
        return b;
    }

    /** 해 배수: 해 1개당 +2% (영구) */
    public double sunMultiplier() {
        return 1 + suns * GameData.SUN_BONUS;
    }

    /** 모든 수입에 곱해지는 배수 */
    public double incomeMultiplier() {
        return starMultiplier() * (1 + selfBonus()) * sunMultiplier();
    }

    // ---- 종교계 ----

    public boolean canEnterReligion() {
        return religionUnlocked || suns >= GameData.RELIGION_SUNS;
    }

    public boolean enterReligion() {
        if (!canEnterReligion()) return false;
        religionUnlocked = true;
        inReligion = true;
        return true;
    }

    public void leaveReligion() {
        inReligion = false;
    }

    public GameData.Religion currentReligion() {
        return GameData.RELIGIONS[religion];
    }

    /** 종교계 별 배수: 1개당 영성 획득 +5% */
    public double spMultiplier() {
        return 1 + holyStars * GameData.HOLY_STAR_BONUS;
    }

    public double tapSp() {
        return GameData.religionTapSp(religion, rank) * spMultiplier();
    }

    public double autoSp() {
        return autoOn ? GameData.religionAutoSp(religion, rank) * spMultiplier() : 0;
    }

    /** 지금 교회에 별을 기부하면 받을 종교계 별 */
    public long holyStarsForDonation() {
        return stars / GameData.STARS_PER_HOLY_STAR;
    }

    public boolean canDonateStars() {
        return holyStarsForDonation() >= 1;
    }

    /** 세속 별을 100개 단위로 교회에 기부한다. 남는 별은 그대로 둔다. */
    public long donateStars() {
        long gained = holyStarsForDonation();
        if (gained < 1) return 0;
        stars -= gained * GameData.STARS_PER_HOLY_STAR;
        holyStars += gained;
        return gained;
    }

    /** 종교계에서 한 번 누른다 (기도). 얻은 영성 포인트를 돌려준다. */
    public double religionTap() {
        double v = tapSp();
        sp += v;
        spTotal += v;
        return v;
    }

    public boolean isLastRank() {
        return rank >= currentReligion().ranks.length - 1;
    }

    public double rankUpCost() {
        return GameData.religionRankCost(religion, rank);
    }

    public boolean canRankUp() {
        return !isLastRank() && sp >= rankUpCost();
    }

    public boolean rankUp() {
        if (!canRankUp()) return false;
        sp -= rankUpCost();
        rank++;
        return true;
    }

    public boolean hasNextReligion() {
        return religion < GameData.lastReligion();
    }

    public double convertCost() {
        return GameData.religionConvertCost(religion);
    }

    public boolean canConvert() {
        return hasNextReligion() && isLastRank() && sp >= convertCost();
    }

    /** 다음 종교로 옮긴다. 신천지로 갈 때는 모든 영성 포인트를 잃고 새로 시작한다. */
    public boolean convert() {
        if (!canConvert()) return false;
        religion++;
        rank = 0;
        if (religion == GameData.lastReligion()) {
            sp = 0;
        } else {
            sp -= GameData.religionConvertCost(religion - 1);
        }
        return true;
    }

    // ---- 신창에게 투자 ----

    public boolean isSelfUnlocked(int i) {
        return i == 0 || selfOwned[i - 1];
    }

    public boolean canInvest(int i) {
        return !selfOwned[i] && isSelfUnlocked(i) && money >= GameData.SELF_INVESTMENTS[i].cost;
    }

    public boolean invest(int i) {
        if (!canInvest(i)) return false;
        money -= GameData.SELF_INVESTMENTS[i].cost;
        selfOwned[i] = true;
        return true;
    }

    // ---- 교회 기부 (리셋 멀티플라이어) ----

    /** 지금 전액 기부하면 받을 별 */
    public long starsForDonation() {
        return (long) Math.floor(Math.cbrt(totalEarned / GameData.STAR_UNIT) + 1e-9);
    }

    public boolean canDonate() {
        return starsForDonation() >= 1;
    }

    /** 전액을 기부하고 처음부터. 별과 자기 투자, 해는 남는다. */
    public GameState donate() {
        if (!canDonate()) return this;
        GameState n = new GameState();
        n.stars = stars + starsForDonation();
        n.suns = suns;
        n.donations = donations + 1;
        System.arraycopy(selfOwned, 0, n.selfOwned, 0, selfOwned.length);
        n.autoOn = autoOn;
        n.copyReligion(this);
        return n;
    }

    // ---- 완전 리셋 ----

    /** 지금 완전 리셋하면 받을 해 (소지금 기준) */
    public long sunsForFullReset() {
        return (long) Math.floor(Math.cbrt(money / GameData.SUN_UNIT) + 1e-9);
    }

    /** 별과 자기 투자까지 모두 지우고 처음부터. 해만 남는다. */
    public GameState fullReset() {
        GameState n = new GameState();
        n.suns = suns + sunsForFullReset();
        n.autoOn = autoOn;
        n.copyReligion(this);
        return n;
    }

    private void copyReligion(GameState o) {
        religionUnlocked = o.religionUnlocked;
        inReligion = false;
        religion = o.religion;
        rank = o.rank;
        sp = o.sp;
        spTotal = o.spTotal;
        holyStars = o.holyStars;
    }

    /** 손으로 한 번 누른다. 번 돈을 돌려준다 (계약 중이거나 공인중개사면 계약 진행만 하므로 0). */
    public double tap() {
        manualTaps++;
        if (activeContract >= 0) {
            contractClicks += 1;
            checkContract();
            return 0;
        }
        if (isBroker()) return 0;
        sinceTap = 0;
        return earn(clickValue());
    }

    public void update(double seconds) {
        if (seconds <= 0) return;
        if (!isBroker()) {
            if (isDelivery()) updateWeather(seconds);
            receive(incomePerSecond() * seconds);
            updateReligion(seconds);
            if (isBoostJob()) {
                boostLeft = Math.max(0, boostLeft - seconds);
                // 소방 계약은 손으로만 클릭한다
                if (activeContract >= 0) {
                    contractTimeLeft -= seconds;
                    checkContract();
                }
            }
            return;
        }
        updateReligion(seconds);
        if (activeContract >= 0) {
            if (autoOn) contractClicks += autoSpeed() * seconds;
            contractTimeLeft -= seconds;
            checkContract();
        } else if (autoOn && lastContract >= 0 && isContractUnlocked(lastContract)) {
            restartWait -= seconds;
            if (restartWait <= 0) startContract(lastContract);
        }
    }

    /** 종교에 귀의한 뒤에는 세속 화면에 있어도 기도(오토)로 영성 포인트가 쌓인다 */
    private void updateReligion(double seconds) {
        if (!religionUnlocked) return;
        double v = autoSp() * seconds;
        sp += v;
        spTotal += v;
    }

    /** 앱을 끈 동안 쌓인 영성 포인트 */
    public double offlineSp(long nowMillis) {
        if (!religionUnlocked || lastSavedMillis <= 0 || nowMillis <= lastSavedMillis) return 0;
        double seconds = Math.min((nowMillis - lastSavedMillis) / 1000.0, OFFLINE_MAX_SECONDS);
        return autoSp() * seconds * OFFLINE_RATE;
    }

    // ---- 날씨 ----

    private void updateWeather(double seconds) {
        sinceTap += seconds;
        if (weather == GameData.CLEAR) {
            if (random.nextDouble() < GameData.SNOW_CHANCE_PER_SEC * seconds) {
                setWeather(GameData.SNOW);
            } else if (random.nextDouble() < GameData.RAIN_CHANCE_PER_SEC * seconds) {
                setWeather(GameData.RAIN);
            }
            return;
        }
        weatherLeft -= seconds;
        if (weatherLeft <= 0) {
            setWeather(GameData.CLEAR);
            return;
        }
        boolean delivering = autoOn || sinceTap < 2;
        if (!delivering) return;
        double accident = GameData.ACCIDENT_CHANCE_PER_SEC[selectedVehicle] * (weather == GameData.SNOW ? 2 : 1);
        if (random.nextDouble() < accident * seconds) {
            penalty(2, GameData.ACCIDENT_PENALTY);
        } else if (random.nextDouble() < GameData.SLIP_CHANCE_PER_SEC[weather] * seconds) {
            penalty(1, GameData.SLIP_PENALTY);
        }
    }

    /** 테스트/연출용으로 날씨를 바로 바꾼다 */
    public void setWeather(int w) {
        weather = w;
        weatherLeft = GameData.WEATHER_SECONDS[w];
        weatherSeq++;
    }

    private void penalty(int type, double amount) {
        incidentType = type;
        incidentPenalty = Math.min(money, amount);
        money -= incidentPenalty;
        incidentSeq++;
    }

    // ---- 탈것 ----

    public boolean canSelectVehicle(int k) {
        return isDelivery() && levels[0][k] > 0 && k != selectedVehicle;
    }

    public boolean selectVehicle(int k) {
        if (!canSelectVehicle(k)) return false;
        selectedVehicle = k;
        return true;
    }

    public double sellPrice(int k) {
        return currentJob().items[k].baseCost * GameData.VEHICLE_RESALE_RATE;
    }

    /** 도보 배달은 팔 수 없다 */
    public boolean canSell(int k) {
        return isDelivery() && k > 0 && levels[0][k] > 0;
    }

    public boolean sell(int k) {
        if (!canSell(k)) return false;
        money += sellPrice(k);
        levels[0][k] = 0;
        if (selectedVehicle == k) selectedVehicle = bestOwnedItem();
        return true;
    }

    public int bestOwnedItem() {
        for (int k = levels[job].length - 1; k >= 0; k--) {
            if (levels[job][k] > 0) return k;
        }
        return 0;
    }

    // ---- 계약 ----

    public boolean isContractUnlocked(int i) {
        int need = unlockRequirement(i);
        return need < 0 || done()[need] >= GameData.CONTRACT_UNLOCK_COUNT;
    }

    /** 잠긴 계약을 열려면 성사시켜야 하는 계약 (-1 이면 처음부터 열림) */
    public int unlockRequirement(int i) {
        GameData.Contract c = contracts()[i];
        if (c.tier == 0 && c.category == 0) return -1;
        return c.tier > 0 ? contractIndex(c.category, c.tier - 1) : contractIndex(c.category - 1, 1);
    }

    private int contractIndex(int category, int tier) {
        GameData.Contract[] cs = contracts();
        for (int i = 0; i < cs.length; i++) {
            if (cs[i].category == category && cs[i].tier == tier) return i;
        }
        throw new IllegalArgumentException("no contract " + category + "/" + tier);
    }

    /** 계약을 시작한다. 진행 중인 다른 계약이 있으면 그 계약은 취소된다. */
    public boolean startContract(int i) {
        if (!hasContracts() || !isContractUnlocked(i)) return false;
        activeContract = i;
        // 오토 반복은 공인중개사만
        lastContract = isBroker() ? i : -1;
        contractClicks = 0;
        contractTimeLeft = contracts()[i].seconds;
        return true;
    }

    public void stopAutoRepeat() {
        lastContract = -1;
    }

    private void checkContract() {
        if (activeContract < 0) return;
        GameData.Contract c = contracts()[activeContract];
        if (contractClicks >= c.clicks) {
            earn(c.reward);
            if (c.boostSeconds > 0) boostLeft = Math.min(GameData.BOOST_MAX_SECONDS, boostLeft + c.boostSeconds);
            done()[activeContract]++;
            finishContract(true);
        } else if (contractTimeLeft <= 0) {
            finishContract(false);
        }
    }

    private void finishContract(boolean success) {
        eventSeq++;
        eventSuccess = success;
        eventContract = activeContract;
        activeContract = -1;
        contractClicks = 0;
        contractTimeLeft = 0;
        restartWait = CONTRACT_RESTART_SECONDS;
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
        return item == 0 || maxEverItem[job] >= item - 1;
    }

    public boolean isMaxed(int item) {
        return levels[job][item] >= currentJob().items[item].maxLevel;
    }

    public boolean canBuy(int item) {
        return isUnlocked(item) && !isMaxed(item) && money >= itemCost(item);
    }

    public boolean buy(int item) {
        if (!canBuy(item)) return false;
        money -= itemCost(item);
        levels[job][item]++;
        maxEverItem[job] = Math.max(maxEverItem[job], item);
        // 새 탈것을 사면 바로 그 탈것을 탄다
        if (isDelivery()) selectedVehicle = item;
        return true;
    }

    // ---- 전직 ----

    /** 전직은 지금까지 도달한 가장 높은 직업에서만 할 수 있다 */
    public boolean hasNextJob() {
        return job == highestJob && job < GameData.lastJob();
    }

    public boolean canSwitchTo(int j) {
        return j >= 0 && j <= highestJob && j != job;
    }

    /** 이미 도달한 다른 직업으로 바꾼다. 진행 중인 계약은 취소된다. */
    public boolean switchJob(int j) {
        if (!canSwitchTo(j)) return false;
        job = j;
        weather = GameData.CLEAR;
        weatherLeft = 0;
        activeContract = -1;
        lastContract = -1;
        return true;
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
        highestJob = job;
        weather = GameData.CLEAR;
        weatherLeft = 0;
        activeContract = -1;
        lastContract = -1;
        // 첫 아이템(기본형)을 가지고 시작해서 바로 오토클릭이 돈다
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
        // 소방기술사 배수는 앱을 켜 둔 동안만 적용
        return baseIncomePerSecond() * incomeMultiplier() * seconds * OFFLINE_RATE;
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
        sb.append(';');
        for (int j = 0; j < contractDone.length; j++) {
            if (j > 0) sb.append('/');
            for (int i = 0; i < contractDone[j].length; i++) {
                if (i > 0) sb.append(',');
                sb.append(contractDone[j][i]);
            }
        }
        sb.append(';').append(lastContract).append(';').append(highestJob)
                .append(';').append(selectedVehicle).append(';');
        for (int j = 0; j < maxEverItem.length; j++) {
            if (j > 0) sb.append(',');
            sb.append(maxEverItem[j]);
        }
        sb.append(';').append(boostLeft)
                .append(';').append(stars)
                .append(';').append(suns)
                .append(';').append(donations).append(';');
        for (boolean b : selfOwned) sb.append(b ? '1' : '0');
        sb.append(';').append(religionUnlocked ? 1 : 0)
                .append(';').append(inReligion ? 1 : 0)
                .append(';').append(religion)
                .append(';').append(rank)
                .append(';').append(sp)
                .append(';').append(spTotal)
                .append(';').append(holyStars);
        return sb.toString();
    }

    /** 저장 데이터가 없거나 깨져 있거나 예전 버전이면 새 게임을 돌려준다. */
    public static GameState deserialize(String data) {
        GameState s = new GameState();
        if (data == null || data.isEmpty()) return s;
        try {
            String[] p = data.split(";", -1);
            boolean v2 = "v2".equals(p[0]);
            boolean v3 = "v3".equals(p[0]);
            boolean v4 = "v4".equals(p[0]);
            boolean v5 = "v5".equals(p[0]);
            boolean v6 = "v6".equals(p[0]);
            if (!SAVE_VERSION.equals(p[0]) && !v2 && !v3 && !v4 && !v5 && !v6) return new GameState();
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
                    int max = GameData.JOBS[j].items[k].maxLevel;
                    s.levels[j][k] = Math.max(0, Math.min(max, Integer.parseInt(lv[k])));
                }
            }
            // v2·v3 는 공인중개사 계약 기록만 있었다
            String[] doneJobs = v2 || v3 ? new String[]{"", p[9]} : p[9].split("/", -1);
            for (int j = 0; j < doneJobs.length && j < s.contractDone.length; j++) {
                if (doneJobs[j].isEmpty()) continue;
                String[] done = doneJobs[j].split(",");
                for (int i = 0; i < done.length && i < s.contractDone[j].length; i++) {
                    s.contractDone[j][i] = Math.max(0, Integer.parseInt(done[i]));
                }
            }
            s.highestJob = Math.max(s.job, Math.min(GameData.lastJob(), Integer.parseInt(p[11])));
            int last = Integer.parseInt(p[10]);
            s.lastContract = s.isBroker() && last >= 0 && last < GameData.CONTRACTS.length ? last : -1;
            if (!v2 && !v3) s.boostLeft = Math.max(0, Math.min(GameData.BOOST_MAX_SECONDS, Double.parseDouble(p[14])));
            if (!v2 && !v3 && !v4) {
                s.stars = Math.max(0, Long.parseLong(p[15]));
                s.suns = Math.max(0, Long.parseLong(p[16]));
                s.donations = Math.max(0, Integer.parseInt(p[17]));
                for (int i = 0; i < p[18].length() && i < s.selfOwned.length; i++) {
                    s.selfOwned[i] = p[18].charAt(i) == '1';
                }
            }
            if (!v2 && !v3 && !v4 && !v5) {
                s.religionUnlocked = "1".equals(p[19]);
                s.inReligion = s.religionUnlocked && "1".equals(p[20]);
                s.religion = Math.max(0, Math.min(GameData.lastReligion(), Integer.parseInt(p[21])));
                s.rank = Math.max(0, Math.min(s.currentReligion().ranks.length - 1, Integer.parseInt(p[22])));
                s.sp = Math.max(0, Double.parseDouble(p[23]));
                s.spTotal = Math.max(0, Double.parseDouble(p[24]));
            }
            if (!v2 && !v3 && !v4 && !v5 && !v6) s.holyStars = Math.max(0, Long.parseLong(p[25]));
            if (v2) {
                // v2 저장에는 탈것 선택/해금 기록이 없으니 가진 아이템으로 채운다
                for (int j = 0; j < s.levels.length; j++) {
                    for (int k = 0; k < s.levels[j].length; k++) if (s.levels[j][k] > 0) s.maxEverItem[j] = k;
                }
                s.selectedVehicle = s.maxEverItem[0];
            } else {
                String[] ever = p[13].split(",");
                for (int j = 0; j < ever.length && j < s.maxEverItem.length; j++) {
                    s.maxEverItem[j] = Math.max(0, Math.min(s.levels[j].length - 1, Integer.parseInt(ever[j])));
                }
                s.selectedVehicle = Integer.parseInt(p[12]);
            }
            if (s.selectedVehicle < 0 || s.selectedVehicle >= s.levels[0].length || s.levels[0][s.selectedVehicle] == 0) {
                int best = 0;
                for (int k = 0; k < s.levels[0].length; k++) if (s.levels[0][k] > 0) best = k;
                s.selectedVehicle = best;
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
