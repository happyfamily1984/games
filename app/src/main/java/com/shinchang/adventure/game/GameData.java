package com.shinchang.adventure.game;

/**
 * 직업과 아이템 정의. 수치는 공식으로 생성해서 밸런스를 한 곳에서 조절한다.
 */
public final class GameData {

    public static final int ITEM_MAX_LEVEL = 10;
    /** 아이템 레벨이 오를 때마다 가격이 몇 배씩 오르는지 */
    public static final double ITEM_COST_GROWTH = 1.15;

    public static final class Item {
        public final String emoji;
        public final String name;
        public final double baseCost;
        /** 레벨당 오토클릭 속도 증가량 (회/초) */
        public final double speedPerLevel;

        Item(String emoji, String name, double baseCost, double speedPerLevel) {
            this.emoji = emoji;
            this.name = name;
            this.baseCost = baseCost;
            this.speedPerLevel = speedPerLevel;
        }
    }

    public static final class Job {
        public final String emoji;
        public final String name;
        public final String story;
        public final double clickValue;
        /** 이 직업에서 다음 직업으로 전직하는 비용 (마지막 직업은 0) */
        public final double promoteCost;
        public final Item[] items;

        Job(String emoji, String name, String story, double clickValue, double promoteCost, Item[] items) {
            this.emoji = emoji;
            this.name = name;
            this.story = story;
            this.clickValue = clickValue;
            this.promoteCost = promoteCost;
            this.items = items;
        }
    }

    public static final Job[] JOBS = {
            job("🛵", "배달원", "오늘도 콜을 잡는다. 언젠가는 재벌이 될 거야!",
                    100, 5e6, 1,
                    new String[][]{
                            {"🚶", "도보 배달"},
                            {"🚲", "자전거"},
                            {"🛵", "CITI 100"},
                            {"🛵", "PCX"},
                            {"🛵", "NMAX 155"},
                            {"🛵", "XMAX 300"},
                            {"🛵", "TMAX 650"},
                            {"🏍️", "R3"},
                            {"🏍️", "R1000"},
                    }),
            job("🏠", "공인중개사", "배달하며 외운 골목길이 최고의 무기. 매물은 내가 제일 잘 안다.",
                    4_000, 1e9, 4,
                    new String[][]{
                            {"📄", "전단지 돌리기"},
                            {"📱", "중개 앱 등록"},
                            {"🚗", "중고 경차"},
                            {"🏢", "상가 사무실"},
                            {"📺", "부동산 유튜브"},
                            {"👥", "직원 채용"},
                            {"🏬", "프랜차이즈 지점"},
                            {"🏗️", "빌딩 전문팀"},
                            {"🌐", "부동산 플랫폼"},
                    }),
            job("🚒", "소방기술사", "건물을 보다 보니 안전이 보였다. 국가 기술사 합격!",
                    150_000, 8e10, 15,
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
            job("🛍️", "쇼핑몰 사장", "현장에서 번 돈으로 창업. 이제는 내가 사장이다.",
                    6_000_000, 6e12, 50,
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
            job("👑", "재벌 회장", "배달부에서 그룹 회장까지. 신창 그룹의 신화가 시작된다.",
                    250_000_000, 0, 150,
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

    /**
     * @param costScale 이전 직업에서 쌓은 오토 속도가 이어지므로, 높은 직업일수록 아이템을 더 비싸게 만든다
     */
    private static Job job(String emoji, String name, String story,
                           double clickValue, double promoteCost, double costScale, String[][] itemNames) {
        Item[] items = new Item[itemNames.length];
        for (int k = 0; k < itemNames.length; k++) {
            double cost = k == 0 ? clickValue * costScale * 8 : clickValue * costScale * 30 * Math.pow(3.6, k - 1);
            double speed = 0.5 * Math.pow(2.0, k);
            items[k] = new Item(itemNames[k][0], itemNames[k][1], cost, speed);
        }
        return new Job(emoji, name, story, clickValue, promoteCost, items);
    }

    private GameData() {
    }
}
