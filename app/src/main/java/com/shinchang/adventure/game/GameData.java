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
        BROKER,
        /** 클릭형 + 계약을 성사시키면 일정 시간 오토 수익이 몇 배가 되는 직업 */
        BOOST
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
        /** 이 직업의 계약 목록 (없으면 null) */
        public Contract[] contracts;

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

    /** 공인중개사·소방기술사 계약 */
    public static final class Contract {
        public final int category;
        public final int tier;
        public final String emoji;
        public final String name;
        /** 보증금/월세/매매가 설명 */
        public final String detail;
        /** 중개보수 (매도·매수 또는 임대·임차 양쪽 합산) */
        public final double reward;
        /** 성사 시 오토 수익 배수가 적용되는 시간 (소방기술사) */
        public final double boostSeconds;
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
            this.boostSeconds = 0;
        }

        /** 소방기술사 계약: 성사하면 제한 시간 x3 동안 오토 수익 x3 */
        Contract(int tier, String emoji, String name, String detail, int clicks, int seconds) {
            this.category = 0;
            this.tier = tier;
            this.emoji = emoji;
            this.name = name;
            this.detail = detail;
            this.reward = 0;
            this.clicks = clicks;
            this.seconds = seconds;
            this.boostSeconds = seconds * BOOST_TIME_FACTOR;
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
            // 탈것은 겹치지 않고 하나만 골라 타므로, 상위 탈것일수록 속도 가중치를 크게 준다
            // 한 단계 오를 때마다 오토 배달 속도가 거의 2배
            new Item("🚶", "도보 배달", "veh_walk", 0, 0.5, 1),
            new Item("🚲", "자전거", "veh_bicycle", 300_000, 1, 1),
            new Item("🛵", "CITI 100", "veh_citi100", 2_100_000, 2, 1),
            new Item("🛵", "PCX", "veh_pcx", 4_720_000, 3.8, 1),
            new Item("🛵", "NMAX 155", "veh_nmax", 5_000_000, 7, 1),
            new Item("🛵", "XMAX 300", "veh_xmax", 8_900_000, 13, 1),
            new Item("🛵", "TMAX 650", "veh_tmax", 18_000_000, 24, 1),
            new Item("🏍️", "R3", "veh_r3", 7_500_000, 44, 1),
            new Item("🏍️", "R1000", "veh_r1000", 28_000_000, 80, 1),
    };

    /** 탈것을 되팔 때 받는 비율 */
    public static final double VEHICLE_RESALE_RATE = 0.7;

    // ------------------------------------------------------------ 배달 날씨 (피버타임)

    public static final int CLEAR = 0, RAIN = 1, SNOW = 2;
    public static final String[] WEATHER_NAMES = {"맑음", "우천", "설천"};
    /** 날씨별 배달 1건 수입 */
    public static final double[] WEATHER_FEE = {DELIVERY_FEE, 10_000, 20_000};
    /** 맑은 날 1초마다 비/눈이 시작될 확률 (평균 3분/10분에 한 번) */
    public static final double RAIN_CHANCE_PER_SEC = 1.0 / 180;
    public static final double SNOW_CHANCE_PER_SEC = 1.0 / 600;
    public static final double[] WEATHER_SECONDS = {0, 30, 20};

    public static final double SLIP_PENALTY = 120_000;
    public static final double ACCIDENT_PENALTY = 500_000;
    /** 비/눈 오는 동안 배달 중 1초마다 미끄러질 확률 */
    public static final double[] SLIP_CHANCE_PER_SEC = {0, 0.02, 0.04};
    /** 탈것별 사고 확률 (비 기준 1초당, 눈은 2배). 도보는 사고가 나지 않는다. */
    public static final double[] ACCIDENT_CHANCE_PER_SEC = {
            0, 0.015, 0.012, 0.009, 0.007, 0.005, 0.003, 0.0015, 0.0005};

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
            // 비싼 아이템일수록 자동 클릭 효과가 조금 더 크다
            new Item("📄", "전단지 돌리기", null, 0, 1, 1),
            new Item("📱", "중개 앱 등록", null, 2_000_000, 1, 1),
            new Item("🚗", "중고 경차", null, 8_000_000, 1.2, 1),
            new Item("🏢", "상가 사무실", null, 30_000_000, 1.8, 1),
            new Item("📺", "부동산 유튜브", null, 60_000_000, 2.4, 1),
            new Item("👥", "중개보조원 채용", null, 150_000_000, 3.2, 1),
            new Item("🏬", "2호점 오픈", null, 500_000_000, 4.2, 1),
            new Item("🏗️", "빌딩 전문팀", null, 1_500_000_000, 5.5, 1),
            new Item("🌐", "부동산 플랫폼", null, 5_000_000_000.0, 7, 1),
    };

    // ------------------------------------------------------------ 직업 목록

    public static final Job[] JOBS = {
            new Job(JobType.CLICKER, "🛵", "배달원",
                    "오늘도 콜을 잡는다. 언젠가는 재벌이 될 거야!",
                    DELIVERY_FEE, 200_000_000, DELIVERY_ITEMS, true),
            new Job(JobType.BROKER, "🏠", "공인중개사",
                    "배달하며 외운 골목길이 최고의 무기. 손님을 설득해 계약을 따내자!",
                    0, 5_000_000_000.0, BROKER_ITEMS, false),
            generated(JobType.BOOST, "🚒", "소방기술사", "건물을 보다 보니 안전이 보였다. 국가 기술사 합격!",
                    2_000_000, 3e12, 1,
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
            generated(JobType.CLICKER, "🛍️", "쇼핑몰 CEO", "현장에서 번 돈으로 창업. 이제는 내가 CEO다.",
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
            generated(JobType.CLICKER, "👑", "재벌 회장", "배달부에서 그룹 회장까지. 신창 그룹의 신화가 시작된다.",
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

    // ------------------------------------------------------------ 3. 소방기술사 계약

    /** 계약 성사 시 오토 수익 배수 */
    public static final double BOOST_MULTIPLIER = 3;
    /** 성사 시 배수가 적용되는 시간 = 제한 시간 x 이 값 */
    public static final int BOOST_TIME_FACTOR = 3;
    /** 배수 시간은 계약을 이어서 성사시키면 쌓이며, 최대 이 시간까지 */
    public static final double BOOST_MAX_SECONDS = 600;

    public static final Contract[] FIRE_CONTRACTS = {
            new Contract(0, "🏠", "주택 감지기 점검", "단독주택 화재감지기", 8, 3),
            new Contract(1, "🏪", "상가 소방점검", "근린상가 작동점검", 22, 5),
            new Contract(2, "🏢", "오피스 정밀점검", "업무시설 종합정밀점검", 40, 8),
            new Contract(3, "🏬", "아파트 단지 점검", "1,000세대 단지", 65, 12),
            new Contract(4, "🏭", "공장 소방설계", "물류센터·공장 설계", 95, 16),
            new Contract(5, "🏥", "병원 성능위주설계", "종합병원 성능위주설계", 135, 21),
            new Contract(6, "🏙️", "초고층 빌딩 감리", "50층 이상 빌딩 감리", 180, 26),
            new Contract(7, "✈️", "공항 소방감리", "국제공항 터미널 감리", 240, 32),
    };

    static {
        JOBS[1].contracts = CONTRACTS;
        JOBS[2].contracts = FIRE_CONTRACTS;
    }

    // ------------------------------------------------------------ 신창에게 투자 / 교회 기부 / 완전 리셋

    /** 자기 투자: 모든 직업의 수입이 오른다. 비쌀수록 비율이 크다. 교회 기부로 리셋해도 남는다. */
    public static final class SelfInvestment {
        public final String emoji;
        public final String name;
        public final double cost;
        /** 수입 증가율 (0.05 = +5%) */
        public final double bonus;

        SelfInvestment(String emoji, String name, double cost, double bonus) {
            this.emoji = emoji;
            this.name = name;
            this.cost = cost;
            this.bonus = bonus;
        }
    }

    public static final SelfInvestment[] SELF_INVESTMENTS = {
            new SelfInvestment("💪", "헬스 운동", 100_000, 0.02),
            new SelfInvestment("💻", "PC 자격증 취득", 500_000, 0.03),
            new SelfInvestment("🚗", "1종 운전면허", 2_000_000, 0.05),
            new SelfInvestment("🗣️", "영어 회화", 10_000_000, 0.08),
            new SelfInvestment("📈", "재테크 공부", 50_000_000, 0.12),
            new SelfInvestment("🎓", "MBA 대학원", 300_000_000, 0.20),
            new SelfInvestment("🧘", "명상 리트릿", 2_000_000_000, 0.30),
            new SelfInvestment("🏌️", "골프 인맥 쌓기", 20_000_000_000.0, 0.50),
            new SelfInvestment("🧠", "경영자 코칭", 200_000_000_000.0, 0.80),
            new SelfInvestment("🌍", "세계 일주", 2e12, 1.20),
            new SelfInvestment("🧬", "프리미엄 건강관리", 5e13, 2.00),
            new SelfInvestment("🚀", "우주 여행", 2e15, 3.00),
    };

    /** 교회 기부: 별 1개당 수입 +10% */
    public static final double STAR_BONUS = 0.10;
    /** 별 = (이번 회차 총수입 / 1억)의 세제곱근 */
    public static final double STAR_UNIT = 100_000_000;
    /** 해 = (소지금 / 1조)의 세제곱근 */
    public static final double SUN_UNIT = 1e12;

    /** 해 1개당 수입 +2% (영구) */
    public static final double SUN_BONUS = 0.02;

    // ------------------------------------------------------------ 종교계 (해 300개 이상)

    /** 세속 별 이만큼을 교회에 기부하면 종교계 별 1개 */
    public static final long STARS_PER_HOLY_STAR = 100;
    /** 종교계 별 1개당 영성 획득 +5% */
    public static final double HOLY_STAR_BONUS = 0.05;

    /** 해를 이만큼 모으면 종교에 귀의할 수 있다 */
    public static final long RELIGION_SUNS = 300;

    public static final class Religion {
        public final String emoji;
        public final String name;
        /** 영성 포인트 배수 */
        public final double multiplier;
        /** 직분 (아래에서 위로) */
        public final String[] ranks;

        Religion(String emoji, String name, double multiplier, String... ranks) {
            this.emoji = emoji;
            this.name = name;
            this.multiplier = multiplier;
            this.ranks = ranks;
        }
    }

    /**
     * 게임 속 설정. 직분 이름은 공개된 보도·일반적인 교회 직제를 참고했으며
     * 특정 종교나 단체를 평가하거나 홍보하지 않는다.
     */
    public static final Religion[] RELIGIONS = {
            new Religion("⛪", "일반 교회", 1,
                    "주일예배", "세례교인", "성가대", "구역장", "청년회장", "전도사", "강도사", "목사"),
            new Religion("✝️", "구원파", 8,
                    "새신자", "형제·자매", "성경공부반", "수련회 봉사", "구역 모임 인도", "전도인", "전도집회 강사", "목사"),
            new Religion("📖", "안식일교회", 60,
                    "구도자", "침례교인", "안식일학교 교사", "집사", "장로", "전도사", "목사", "합회장", "연합회장"),
            new Religion("🗼", "여호와의 증인", 450,
                    "성서 연구생", "전도인", "침례받은 전도인", "보조 파이오니아", "정규 파이오니아",
                    "봉사의 종", "장로", "여행하는 감독자", "지부 위원", "통치체 성원"),
            new Religion("🕊️", "하나님의 교회", 3_500,
                    "성도", "집사", "권사", "장로", "전도사", "목사", "당회장", "총회장"),
            new Religion("🌟", "신천지", 10_000_000,
                    "교육생", "성도", "구역장", "청년회장", "교리 강사", "전도사", "교육장", "지파장", "총회 총무"),
    };

    /** 직분별 탭 1회 영성 포인트 */
    public static double religionTapSp(int religion, int rank) {
        return RELIGIONS[religion].multiplier * 10 * Math.pow(1.9, rank);
    }

    /** 직분별 기도(오토) 초당 영성 포인트 */
    public static double religionAutoSp(int religion, int rank) {
        return religionTapSp(religion, rank) * 0.8;
    }

    /** 다음 직분으로 오르는 데 드는 영성 포인트 */
    public static double religionRankCost(int religion, int rank) {
        return religionTapSp(religion, rank) * 300 * Math.pow(1.25, rank);
    }

    /** 다음 종교로 옮기는 데 드는 영성 포인트 (마지막 직분 승급 비용의 3배) */
    public static double religionConvertCost(int religion) {
        return religionRankCost(religion, RELIGIONS[religion].ranks.length - 1) * 3;
    }

    public static int lastReligion() {
        return RELIGIONS.length - 1;
    }

    public static int lastJob() {
        return JOBS.length - 1;
    }

    /** 아이템을 공식으로 만드는 직업. 아이템마다 최대 10레벨까지 강화한다. */
    private static Job generated(JobType type, String emoji, String name, String story,
                                 double clickValue, double promoteCost, double costScale, String[][] itemNames) {
        Item[] items = new Item[itemNames.length];
        for (int k = 0; k < itemNames.length; k++) {
            double cost = k == 0 ? clickValue * costScale * 8 : clickValue * costScale * 30 * Math.pow(3.6, k - 1);
            double speed = 0.5 * Math.pow(2.0, k);
            items[k] = new Item(itemNames[k][0], itemNames[k][1], null, cost, speed, 10);
        }
        return new Job(type, emoji, name, story, clickValue, promoteCost, items, false);
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
