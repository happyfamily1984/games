package com.shinchang.adventure.rpg;

import java.util.ArrayList;
import java.util.List;

/** 파티 멤버. 능력치는 레벨과 장비로 계산한다. */
public final class Hero {

    public final int id;
    public int level = 1;
    public int exp;
    public int hp;
    public int mp;
    public int weapon;
    public int armor;

    public Hero(int id, int level) {
        this.id = id;
        RpgData.HeroBase b = base();
        this.level = Math.max(1, level);
        this.exp = RpgData.expForLevel(this.level);
        this.weapon = b.weapon;
        this.armor = b.armor;
        this.hp = maxHp();
        this.mp = maxMp();
    }

    public RpgData.HeroBase base() {
        return RpgData.HEROES[id];
    }

    public String name() {
        return base().name;
    }

    public int maxHp() {
        return base().hp + base().ghp * (level - 1);
    }

    public int maxMp() {
        return base().mp + base().gmp * (level - 1);
    }

    public int atk() {
        return base().atk + base().gatk * (level - 1) + RpgData.ITEMS[weapon].power;
    }

    public int def() {
        return base().def + base().gdef * (level - 1) + RpgData.ITEMS[armor].power;
    }

    public int agi() {
        return base().agi + base().gagi * (level - 1);
    }

    public boolean alive() {
        return hp > 0;
    }

    public void fullHeal() {
        hp = maxHp();
        mp = maxMp();
    }

    /** 지금 쓸 수 있는 주문/특기 번호 */
    public List<Integer> spells() {
        List<Integer> out = new ArrayList<>();
        for (int i = 0; i < RpgData.SPELLS.length; i++) {
            RpgData.Spell s = RpgData.SPELLS[i];
            if (s.owner == id && s.level <= level) out.add(i);
        }
        return out;
    }

    /** 경험치를 얻고, 오른 레벨·새 주문 메시지를 돌려준다 */
    public List<String> gainExp(int amount) {
        List<String> msgs = new ArrayList<>();
        if (!alive()) return msgs;
        exp += amount;
        while (level < RpgData.MAX_LEVEL && exp >= RpgData.expForLevel(level + 1)) {
            int oldHp = maxHp(), oldMp = maxMp();
            level++;
            hp += maxHp() - oldHp;
            mp += maxMp() - oldMp;
            msgs.add(Josa.eun(name()) + " 레벨 " + level + "(으)로 올랐다!");
            for (RpgData.Spell s : RpgData.SPELLS) {
                if (s.owner == id && s.level == level) msgs.add(Josa.eun(name()) + " 「" + s.name + "」" + Josa.eulOf(s.name) + " 익혔다!");
            }
        }
        return msgs;
    }

    public int expToNext() {
        if (level >= RpgData.MAX_LEVEL) return 0;
        return RpgData.expForLevel(level + 1) - exp;
    }

    String serialize() {
        return id + ":" + level + ":" + exp + ":" + hp + ":" + mp + ":" + weapon + ":" + armor;
    }

    static Hero deserialize(String s) {
        String[] p = s.split(":");
        Hero h = new Hero(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
        h.exp = Integer.parseInt(p[2]);
        h.weapon = Integer.parseInt(p[5]);
        h.armor = Integer.parseInt(p[6]);
        h.hp = Math.max(0, Math.min(h.maxHp(), Integer.parseInt(p[3])));
        h.mp = Math.max(0, Math.min(h.maxMp(), Integer.parseInt(p[4])));
        return h;
    }
}
