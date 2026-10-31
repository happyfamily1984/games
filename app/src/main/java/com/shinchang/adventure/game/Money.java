package com.shinchang.adventure.game;

import java.util.Locale;

/** 금액을 "12억 3,400만원" 처럼 한국식 단위로 표시한다. */
public final class Money {

    private static final String[] UNITS = {"", "만", "억", "조", "경", "해"};

    public static String format(double won) {
        if (won < 0) return "-" + format(-won);
        if (won < 10_000) return String.format(Locale.KOREA, "%,d원", (long) Math.floor(won));

        int unit = 0;
        double unitValue = 1;
        while (won >= unitValue * 10_000 && unit < UNITS.length - 1) {
            unitValue *= 10_000;
            unit++;
        }
        long big = (long) Math.floor(won / unitValue);
        long small = (long) Math.floor((won - big * unitValue) / (unitValue / 10_000) + 1e-6);
        if (small >= 10_000) {
            big++;
            small -= 10_000;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.KOREA, "%,d", big)).append(UNITS[unit]);
        if (small > 0 && big < 1000) {
            sb.append(' ').append(String.format(Locale.KOREA, "%,d", small)).append(UNITS[unit - 1]);
        }
        return sb.append('원').toString();
    }

    /** "1억 5,000만" 처럼 '원' 없이 표시 (매물 가격용) */
    public static String plain(double won) {
        String s = format(won);
        return s.substring(0, s.length() - 1);
    }

    /** 오토클릭 속도 표시용 */
    public static String speed(double perSecond) {
        if (perSecond < 10) return String.format(Locale.ROOT, "%.1f", perSecond);
        return String.format(Locale.KOREA, "%,d", (long) perSecond);
    }

    private Money() {
    }
}
