package com.shinchang.adventure.rpg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 자동 전투: 회복·부활을 먼저 챙기고, 무리에는 전체 공격, 보스에는 가장 센 단일 기술을 쓴다. */
public final class BattleAi {

    private static final int[] GROUP = {RpgData.HERO_ALL, RpgData.HOLY_ALL, RpgData.FIRE_ALL, RpgData.WATER_ALL, RpgData.PHYS_ALL};
    private static final int[] SINGLE = {RpgData.HERO_ONE, RpgData.BIG_HIT, RpgData.MULTI_HIT, RpgData.POWER_HIT,
            RpgData.WATER_ONE, RpgData.FIRE_ONE};

    public static List<Battle.Action> choose(RpgState s, Battle b) {
        List<Battle.Action> acts = new ArrayList<>();
        int hurt = 0;
        Hero dead = null;
        for (Hero o : s.party) {
            if (o.alive() && o.hp < o.maxHp() * 0.55) hurt++;
            if (!o.alive() && dead == null) dead = o;
        }
        List<Battle.Enemy> alive = b.aliveEnemies();
        boolean group = alive.size() >= 2;
        int enemyHp = 0;
        for (Battle.Enemy e : alive) enemyHp += e.hp;
        boolean revived = false;
        int round = b.round;
        for (int i = 0; i < s.party.size(); i++) {
            Hero h = s.party.get(i);
            if (b.familyReady() && i == 0 && h.alive()) {
                acts.add(new Battle.Action(Battle.FAMILY, -1, 0));
                continue;
            }
            // 종류별로 쓸 수 있는 가장 센 기술
            Map<Integer, Integer> sp = new HashMap<>();
            for (int k : h.spells()) {
                RpgData.Spell x = RpgData.SPELLS[k];
                if (h.mp < x.mp) continue;
                Integer cur = sp.get(x.kind);
                if (cur == null || RpgData.SPELLS[cur].power <= x.power) sp.put(x.kind, k);
            }
            Hero weak = null;
            for (Hero o : s.party) if (o.alive() && o.hp < o.maxHp() * 0.45 && (weak == null || o.hp < weak.hp)) weak = o;
            // 약한 잡몹은 그냥 때린다 (MP 아끼기)
            boolean tough = b.boss || enemyHp > h.atk() * 2 || h.mpRegen() > 0;
            Battle.Action a = null;
            if (dead != null && !revived && sp.containsKey(RpgData.REVIVE)) {
                a = new Battle.Action(Battle.SPELL, s.party.indexOf(dead), sp.get(RpgData.REVIVE));
                revived = true;
            } else if (dead != null && !revived && s.hasItem(RpgData.PHOENIX)) {
                a = new Battle.Action(Battle.ITEM, s.party.indexOf(dead), RpgData.PHOENIX);
                revived = true;
            } else if (hurt >= 2 && sp.containsKey(RpgData.HEAL_ALL)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.HEAL_ALL));
            } else if (weak != null && sp.containsKey(RpgData.HEAL_ONE)) {
                a = new Battle.Action(Battle.SPELL, s.party.indexOf(weak), sp.get(RpgData.HEAL_ONE));
            } else if (weak != null && sp.containsKey(RpgData.HEAL_ALL)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.HEAL_ALL));
            } else if (weak != null && s.hasItem(RpgData.HI_HERB)) {
                a = new Battle.Action(Battle.ITEM, s.party.indexOf(weak), RpgData.HI_HERB);
            } else if (weak != null && s.hasItem(RpgData.HERB)) {
                a = new Battle.Action(Battle.ITEM, s.party.indexOf(weak), RpgData.HERB);
            } else if (b.boss && round == 0 && sp.containsKey(RpgData.GUARD)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.GUARD));
            } else if (b.boss && round <= 1 && sp.containsKey(RpgData.EVADE_UP)) {
                a = new Battle.Action(Battle.SPELL, -1, sp.get(RpgData.EVADE_UP));
            }
            if (a == null && group && tough) {
                for (int kind : GROUP) {
                    if (sp.containsKey(kind)) {
                        a = new Battle.Action(Battle.SPELL, -1, sp.get(kind));
                        break;
                    }
                }
            }
            if (a == null && (b.boss || (tough && !group))) {
                for (int kind : SINGLE) {
                    if (sp.containsKey(kind)) {
                        a = new Battle.Action(Battle.SPELL, -1, sp.get(kind));
                        break;
                    }
                }
            }
            if (a == null && b.boss && h.mp < 8 && s.hasItem(RpgData.MANA_CANDY)) {
                a = new Battle.Action(Battle.ITEM, i, RpgData.MANA_CANDY);
            }
            if (a == null) a = new Battle.Action(Battle.ATTACK, -1, 0);
            acts.add(a);
        }
        return acts;
    }

    private BattleAi() {
    }
}
