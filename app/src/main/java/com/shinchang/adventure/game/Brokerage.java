package com.shinchang.adventure.game;

/**
 * 부동산 중개보수 상한 (한쪽 당사자 기준, 2021.10 개정 요율표).
 * 주택 외(상가·빌딩)는 0.9% 이내.
 */
public final class Brokerage {

    private static final double NON_HOUSE_RATE = 0.009;

    /** 임대차(전세·월세). 거래금액 = 보증금 + 월세×100, 5천만원 미만이면 월세×70 으로 다시 계산 */
    public static double rent(double deposit, double monthly, boolean house) {
        double amount = deposit + monthly * 100;
        if (amount < 50_000_000) amount = deposit + monthly * 70;
        if (!house) return amount * NON_HOUSE_RATE;
        if (amount < 50_000_000) return Math.min(amount * 0.005, 200_000);
        if (amount < 100_000_000) return Math.min(amount * 0.004, 300_000);
        if (amount < 600_000_000) return amount * 0.003;
        if (amount < 1_200_000_000) return amount * 0.004;
        if (amount < 1_500_000_000) return amount * 0.005;
        return amount * 0.006;
    }

    /** 매매·교환 */
    public static double sale(double price, boolean house) {
        if (!house) return price * NON_HOUSE_RATE;
        if (price < 50_000_000) return Math.min(price * 0.006, 250_000);
        if (price < 200_000_000) return Math.min(price * 0.005, 800_000);
        if (price < 900_000_000) return price * 0.004;
        if (price < 1_200_000_000) return price * 0.005;
        if (price < 1_500_000_000) return price * 0.006;
        return price * 0.007;
    }

    private Brokerage() {
    }
}
