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
    /** 장신구 (-1 이면 없음) */
    public int accessory = -1;
    /** 결혼으로 깨어난 힘 (신창: 용사 전직, 지헌: 불의 축복) */
    public boolean awakened;

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

    /** 용사로 전직한 신창 */
    public boolean isHero() {
        return awakened && (id == 0 || id == 4);
    }

    /** 각성하면 능력치가 크게 오른다: {곱, 더함} */
    private int boost(int value, double heroMul, int heroAdd, double blessMul, int blessAdd) {
        if (!awakened) return value;
        return id == 0 || id == 4 ? (int) Math.round(value * heroMul) + heroAdd : (int) Math.round(value * blessMul) + blessAdd;
    }

    public int maxHp() {
        return boost(base().hp + base().ghp * (level - 1), 2.0, 300, 1.4, 100);
    }

    public int maxMp() {
        return boost(base().mp + base().gmp * (level - 1), 2.0, 100, 1.5, 60);
    }

    public int atk() {
        return boost(base().atk + base().gatk * (level - 1), 1.8, 60, 1.3, 20) + RpgData.ITEMS[weapon].power;
    }

    public int def() {
        return boost(base().def + base().gdef * (level - 1), 1.6, 40, 1.3, 20) + RpgData.ITEMS[armor].power;
    }

    public int agi() {
        return boost(base().agi + base().gagi * (level - 1), 1.6, 20, 1.2, 10);
    }

    /** 회심의 일격 확률 */
    public double critChance() {
        return isHero() ? 0.3 : 0;
    }

    /** 슬레이어: 주는 피해 배율 */
    public double slayer() {
        return isHero() ? 1.5 : 1;
    }

    /** 장신구의 MP 회복률 (%) */
    public int mpRegen() {
        return accessory >= 0 && RpgData.ITEMS[accessory] != null ? RpgData.ITEMS[accessory].power : 0;
    }

    public String job() {
        if (id == 0) return awakened ? "용사 · 슬레이어" : "배달부";
        if (id == 3) return awakened ? "불의 무녀 (불의 축복)" : "대장장이의 딸";
        if (id == 4) return awakened ? "해의 계승자" : "숲의 아이";
        if (id == 5) return awakened ? "해의 늑대" : "숲의 늑대";
        if (id == 1) return "기사";
        return "요정 상인";
    }

    /** 주문의 주인 번호: 각성한 사람은 (번호 + 10) 주문도 쓴다 */
    private boolean owns(RpgData.Spell s) {
        return s.owner == id || (awakened && s.owner == id + 10);
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
            if (owns(s) && s.level <= level) out.add(i);
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
                if (owns(s) && s.level == level) msgs.add(Josa.eun(name()) + " 「" + s.name + "」" + Josa.eulOf(s.name) + " 익혔다!");
            }
        }
        return msgs;
    }

    public int expToNext() {
        if (level >= RpgData.MAX_LEVEL) return 0;
        return RpgData.expForLevel(level + 1) - exp;
    }

    String serialize() {
        return id + ":" + level + ":" + exp + ":" + hp + ":" + mp + ":" + weapon + ":" + armor + ":" + accessory + ":" + (awakened ? 1 : 0);
    }

    static Hero deserialize(String s) {
        String[] p = s.split(":");
        Hero h = new Hero(Integer.parseInt(p[0]), Integer.parseInt(p[1]));
        h.exp = Integer.parseInt(p[2]);
        h.weapon = Integer.parseInt(p[5]);
        h.armor = Integer.parseInt(p[6]);
        if (p.length > 8) {
            h.accessory = Integer.parseInt(p[7]);
            h.awakened = "1".equals(p[8]);
        }
        h.hp = Math.max(0, Math.min(h.maxHp(), Integer.parseInt(p[3])));
        h.mp = Math.max(0, Math.min(h.maxMp(), Integer.parseInt(p[4])));
        return h;
    }
}
