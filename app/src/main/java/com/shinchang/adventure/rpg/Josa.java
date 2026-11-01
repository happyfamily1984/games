package com.shinchang.adventure.rpg;

/** 한국어 조사: 앞 글자의 받침에 따라 을/를, 이/가, 은/는, 와/과 를 고른다. */
public final class Josa {

    private static boolean hasFinal(String word) {
        if (word == null || word.isEmpty()) return false;
        char c = word.charAt(word.length() - 1);
        if (c < 0xAC00 || c > 0xD7A3) return false;
        return (c - 0xAC00) % 28 != 0;
    }

    public static String eul(String w) {
        return w + (hasFinal(w) ? "을" : "를");
    }

    /** 단어 뒤에 붙일 을/를 만 (따옴표로 감쌀 때) */
    public static String eulOf(String w) {
        return hasFinal(w) ? "을" : "를";
    }

    public static String i(String w) {
        return w + (hasFinal(w) ? "이" : "가");
    }

    public static String eun(String w) {
        return w + (hasFinal(w) ? "은" : "는");
    }

    public static String wa(String w) {
        return w + (hasFinal(w) ? "과" : "와");
    }

    private Josa() {
    }
}
