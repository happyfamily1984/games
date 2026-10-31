package com.shinchang.adventure.game;

/**
 * 직업, 아이템, 중개 계약 정의. 밸런스 수치는 모두 여기서 조절한다.
 */
public final class GameData {

    public static final double ITEM_COST_GROWTH = 1.15;

    public enum JobType {
        /** 클릭 1회마다 돈을 버는 직업 */
        CLICKER,
        /** 제한 시간 안에 목표 클릭 수를 채워 계약을 성사시키는 직업 */
        BROKER
    }

    public static final class Item {
        public final String emoji;
        public final String name;
        /** 그림 리소스 이름 (없으면 이모지로 표시) */
        public final String image;
        public final double baseCost;
        /** 레벨당 오토클릭 속도 (회/초) */
        public final double speedPerLevel;
        public final int maxLevel;

        Item(String emoji, String name, String image, double baseCost, double speedPerLevel, int maxLevel) {
            this.emoji = emoji;
            this.name = name;
            this.image = image;
            this.baseCost = baseCost;
            this.speedPerLevel = speedPerLevel;
            this.maxLevel = maxLevel;
        }
    }

    public static final class Job {
        public final JobType type;
        public final String emoji;
        public final String name;
        public final String story;
        /** CLICKER: 클릭 1회 금액 */
        public final double clickValue;
        /** 다음 직업으로 전직하는 비용 (마지막 직업은 0) */
        public final double promoteCost;
        public final Item[] items;
        /** true 면 아이템 효과가 합산되지 않고 가장 좋은 아이템 하나만 적용된다 (탈것 교체) */
        public final boolean bestItemOnly;

        Job(JobType type, String emoji, String name, String story, double clickValue,
            double promoteCost, Item[] items, boolean bestItemOnly) {
            this.type = type;
            this.emoji = emoji;
            this.name = name;
            this.story = story;
            this.clickValue = clickValue;
            this.promoteCost = promoteCost;
            this.items = items;
            this.bestItemOnly = bestItemOnly;
        }
    }

    /** 공인중개사 계약 */
    public static final class Contract {
        public final int category;
        public final int tier;
        public final String emoji;
        public final String name;
        /** 보증금/월세/매매가 설명 */
        public final String detail;
        /** 중개보수 (매도·매수 또는 임대·임차 양쪽 합산) */
        public final double reward;
        public final int clicks;
        public final int seconds;

        Contract(int category, int tier, String emoji, String name, String detail,
                 double reward, int clicks, int seconds) {
            this.category = category;
            this.tier = tier;
            this.emoji = emoji;
            this.name = name;
            this.detail = detail;
            this.reward = reward;
            this.clicks = clicks;
            this.seconds = seconds;
        }

        public double requiredCps() {
            return (double) clicks / seconds;
        }
    }

    // ------------------------------------------------------------ 1. 배달원

    /** 배달 1건 수입 */
    public static final double DELIVERY_FEE = 4_000;

    private static final Item[] DELIVERY_ITEMS = {
            // 가격: 2025년 국내 신차가 기준 (PCX·NMAX 공식가, 나머지는 해외 가격 환산 추정)
            new Item("🚶", "도보 배달", "veh_walk", 0, 0.5, 1),
            new Item("🚲", "자전거", "veh_bicycle", 300_000, 1, 1),
            new Item("🛵", "CITI 100", "veh_citi100", 2_100_000, 2, 1),
            new Item("🛵", "PCX", "veh_pcx", 4_720_000, 3, 1),
            new Item("🛵", "NMAX 155", "veh_nmax", 5_000_000, 3.5, 1),
            new Item("🛵", "XMAX 300", "veh_xmax", 8_900_000, 5, 1),
            new Item("🛵", "TMAX 650", "veh_tmax", 18_000_000, 7, 1),
            new Item("🏍️", "R3", "veh_r3", 7_500_000, 8, 1),
            new Item("🏍️", "R1000", "veh_r1000", 28_000_000, 10, 1),
    };

    // ------------------------------------------------------------ 2. 공인중개사

    public static final String[] CONTRACT_CATEGORIES = {"월세", "전세", "매매"};
    /** 다음 계약이 열리려면 이전 계약을 몇 번 성사시켜야 하는지 */
    public static final int CONTRACT_UNLOCK_COUNT = 3;

    public static final Contract[] CONTRACTS = {
            rent(0, "🏠", "원룸", 10_000_000, 600_000, true, 10, 3),
            rent(1, "🏘️", "빌라", 30_000_000, 900_000, true, 20, 5),
            rent(2, "🏢", "아파트", 100_000_000, 2_000_000, true, 35, 8),
            rent(3, "🏪", "상가", 20_000_000, 1_500_000, false, 60, 12),
            rent(4, "🏙️", "빌딩", 100_000_000, 5_000_000, false, 110, 20),

            jeonse(0, "🏠", "원룸", 150_000_000, true, 30, 6),
            jeonse(1, "🏘️", "빌라", 250_000_000, true, 45, 8),
            jeonse(2, "🏢", "아파트", 500_000_000, true, 70, 12),
            jeonse(3, "🏪", "상가", 700_000_000, false, 90, 15),
            jeonse(4, "🏙️", "빌딩", 2_000_000_000, false, 160, 25),

            sale(0, "🏠", "원룸", 180_000_000, true, 50, 8),
            sale(1, "🏘️", "빌라", 400_000_000, true, 70, 10),
            sale(2, "🏢", "아파트", 1_500_000_000, true, 110, 15),
            sale(3, "🏪", "상가", 3_000_000_000.0, false, 165, 22),
            sale(4, "🏙️", "빌딩", 10_000_000_000.0, false, 240, 30),
    };

    private static final Item[] BROKER_ITEMS = {
            new Item("📄", "전단지 돌리기", null, 0, 1, 1),
            new Item("📱", "중개 앱 등록", null, 2_000_000, 1, 1),
            new Item("🚗", "중고 경차", null, 8_000_000, 1, 1),
            new Item("🏢", "상가 사무실", null, 30_000_000, 1.5, 1),
            new Item("📺", "부동산 유튜브", null, 60_000_000, 1.5, 1),
            new Item("👥", "중개보조원 채용", null, 150_000_000, 2, 1),
            new Item("🏬", "2호점 오픈", null, 500_000_000, 2, 1),
            new Item("🏗️", "빌딩 전문팀", null, 1_500_000_000, 2.5, 1),
            new Item("🌐", "부동산 플랫폼", null, 5_000_000_000.0, 3, 1),
    };

    // ------------------------------------------------------------ 직업 목록

    public static final Job[] JOBS = {
            new Job(JobType.CLICKER, "🛵", "배달원",
                    "오늘도 콜을 잡는다. 언젠가는 재벌이 될 거야!",
                    DELIVERY_FEE, 30_000_000, DELIVERY_ITEMS, true),
            new Job(JobType.BROKER, "🏠", "공인중개사",
                    "배달하며 외운 골목길이 최고의 무기. 손님을 설득해 계약을 따내자!",
                    0, 5_000_000_000.0, BROKER_ITEMS, false),
            generated("🚒", "소방기술사", "건물을 보다 보니 안전이 보였다. 국가 기술사 합격!",
                    2_000_000, 8e11, 1,
                    new String[][]{
                            {"🧰", "공구 세트"},
                            {"🧯", "점검 장비"},
                            {"🚚", "1톤 트럭"},
                            {"📐", "설계 프로그램"},
                            {"🏢", "점검 회사"},
                            {"📝", "설계 사무소"},
                            {"🦺", "감리 법인"},
                            {"🏙️", "랜드마크 수주"},
                            {"🔥", "소방 엔지니어링 그룹"},
                    }),
            generated("🛍️", "쇼핑몰 사장", "현장에서 번 돈으로 창업. 이제는 내가 사장이다.",
                    100_000_000, 5e13, 1,
                    new String[][]{
                            {"🏪", "스마트스토어"},
                            {"📸", "촬영 스튜디오"},
                            {"💻", "자사몰 오픈"},
                            {"📦", "물류 창고"},
                            {"🌟", "인플루언서 협업"},
                            {"🏷️", "자체 브랜드"},
                            {"🏬", "플래그십 스토어"},
                            {"✈️", "해외 진출"},
                            {"🦄", "유니콘 플랫폼"},
                    }),
            generated("👑", "재벌 회장", "배달부에서 그룹 회장까지. 신창 그룹의 신화가 시작된다.",
                    5_000_000_000.0, 0, 3,
                    new String[][]{
                            {"🏦", "계열사 인수"},
                            {"🛩️", "전용기"},
                            {"🛥️", "슈퍼 요트"},
                            {"🏙️", "그룹 본사 타워"},
                            {"🔬", "반도체 공장"},
                            {"📡", "방송사 인수"},
                            {"⚾", "프로야구단"},
                            {"🚀", "우주항공 사업"},
                            {"🌏", "글로벌 지주회사"},
                    }),
    };

    public static int lastJob() {
        return JOBS.length - 1;
    }

    /** 아이템을 공식으로 만드는 직업. 아이템마다 최대 10레벨까지 강화한다. */
    private static Job generated(String emoji, String name, String story,
                                 double clickValue, double promoteCost, double costScale, String[][] itemNames) {
        Item[] items = new Item[itemNames.length];
        for (int k = 0; k < itemNames.length; k++) {
            double cost = k == 0 ? clickValue * costScale * 8 : clickValue * costScale * 30 * Math.pow(3.6, k - 1);
            double speed = 0.5 * Math.pow(2.0, k);
            items[k] = new Item(itemNames[k][0], itemNames[k][1], null, cost, speed, 10);
        }
        return new Job(JobType.CLICKER, emoji, name, story, clickValue, promoteCost, items, false);
    }

    // ------------------------------------------------------------ 중개보수 계산

    private static Contract rent(int tier, String emoji, String type, double deposit, double monthly,
                                 boolean house, int clicks, int seconds) {
        double fee = Math.round(2 * Brokerage.rent(deposit, monthly, house));
        String detail = "보증금 " + Money.plain(deposit) + " / 월세 " + Money.plain(monthly);
        return new Contract(0, tier, emoji, type + " 월세", detail, fee, clicks, seconds);
    }

    private static Contract jeonse(int tier, String emoji, String type, double deposit,
                                   boolean house, int clicks, int seconds) {
        double fee = Math.round(2 * Brokerage.rent(deposit, 0, house));
        return new Contract(1, tier, emoji, type + " 전세", "전세금 " + Money.plain(deposit), fee, clicks, seconds);
    }

    private static Contract sale(int tier, String emoji, String type, double price,
                                 boolean house, int clicks, int seconds) {
        double fee = Math.round(2 * Brokerage.sale(price, house));
        return new Contract(2, tier, emoji, type + " 매매", "매매가 " + Money.plain(price), fee, clicks, seconds);
    }

    private GameData() {
    }
}
