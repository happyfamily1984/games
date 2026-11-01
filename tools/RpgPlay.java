// 실행: javac -encoding UTF-8 -d out app/src/main/java/com/shinchang/adventure/rpg/{RpgData,RpgState,Hero,Battle,Josa}.java tools/RpgPlay.java && java -cp out RpgPlay
import com.shinchang.adventure.rpg.*;
import java.util.*;

/** 자동 플레이: 길찾기로 걸어가며 전투는 간단한 AI로 진행해 1장을 끝까지 깬다. */
public class RpgPlay {
    static RpgState s; static Random rnd = new Random(7);
    static int battles=0, defeats=0, rests=0; static double seconds=0;

    static void ok(boolean c, String m){ if(!c) throw new AssertionError(m); System.out.println("✓ "+m); }

    // BFS: 지금 지도에서 (tx,ty) 로 가는 첫 방향 (도착점 자체는 막혀 있어도 옆까지)
    static List<Integer> path(int tx, int ty, boolean adjacent) {
        int w=s.mapWidth(), h=s.mapHeight(); int[][] prev=new int[h][w]; for(int[] r:prev) Arrays.fill(r,-1);
        ArrayDeque<int[]> q=new ArrayDeque<>(); q.add(new int[]{s.x,s.y}); prev[s.y][s.x]=9;
        while(!q.isEmpty()){ int[] c=q.poll();
            boolean done = adjacent ? Math.abs(c[0]-tx)+Math.abs(c[1]-ty)==1 : (c[0]==tx&&c[1]==ty);
            if(done){ LinkedList<Integer> out=new LinkedList<>(); int cx=c[0],cy=c[1];
                while(prev[cy][cx]!=9){ int d=prev[cy][cx]; out.addFirst(d); cx-=RpgState.DX[d]; cy-=RpgState.DY[d]; } return out; }
            for(int d=0;d<4;d++){ int nx=c[0]+RpgState.DX[d], ny=c[1]+RpgState.DY[d];
                if(nx<0||ny<0||nx>=w||ny>=h||prev[ny][nx]!=-1) continue;
                boolean target = !adjacent && nx==tx && ny==ty;
                if(!s.walkable(nx,ny) && !target) continue;
                // 지도 이동 칸은 목적지일 때만 밟는다
                if(!target && isLink(nx,ny)) continue;
                prev[ny][nx]=d; q.add(new int[]{nx,ny}); } }
        return null;
    }
    static boolean isLink(int x,int y){ for(int[] l:RpgData.LINKS) if(l[0]==s.map&&l[1]==x&&l[2]==y) return true; return false; }

    static void walkTo(int tx,int ty,boolean adjacent){
        for(int guard=0; guard<200; guard++){
            List<Integer> p=path(tx,ty,adjacent);
            if(p==null) throw new AssertionError("길 없음: "+RpgData.MAP_NAMES[s.map]+" ("+s.x+","+s.y+") → ("+tx+","+ty+")");
            if(p.isEmpty()){ if(adjacent){ for(int d=0;d<4;d++) if(s.x+RpgState.DX[d]==tx&&s.y+RpgState.DY[d]==ty) s.dir=d; } return; }
            int startMap=s.map;
            for(int d:p){ RpgState.MoveResult r=s.move(d); seconds+=0.25;
                if(r.encounter!=null){ fight(r.encounter,false); if(s.map!=startMap) return; break; }
                if(r.warped) return; }
            if(lowHp() && RpgData.hasEncounters(s.map)) return; // 돌아가서 쉬기
        }
        throw new AssertionError("너무 오래 걸림");
    }
    static boolean lowHp(){ for(Hero h:s.party) if(h.hp<h.maxHp()*0.35) return true; return false; }

    static boolean fight(int[] ids, boolean boss){
        battles++; Battle b=new Battle(s, ids, boss, rnd);
        while(!b.over()){
            List<Battle.Action> acts=new ArrayList<>();
            for(int i=0;i<s.party.size();i++){ Hero h=s.party.get(i);
                int heal=-1; for(int sp:h.spells()) if(RpgData.SPELLS[sp].kind==RpgData.HEAL_ONE && h.mp>=RpgData.SPELLS[sp].mp) heal=sp;
                Hero weak=null; for(Hero o:s.party) if(o.alive() && o.hp<o.maxHp()*0.45 && (weak==null||o.hp<weak.hp)) weak=o;
                if(weak!=null && heal>=0) acts.add(new Battle.Action(Battle.SPELL, s.party.indexOf(weak), heal));
                else if(weak!=null && s.hasItem(RpgData.HERB)) acts.add(new Battle.Action(Battle.ITEM, s.party.indexOf(weak), RpgData.HERB));
                else { int atk=-1; for(int sp:h.spells()){ RpgData.Spell x=RpgData.SPELLS[sp]; if((x.kind==RpgData.POWER_HIT||x.kind==RpgData.FIRE_ONE||x.kind==RpgData.FIRE_ALL) && h.mp>=x.mp+(boss?0:4)) atk=sp; }
                    acts.add(atk>=0 ? new Battle.Action(Battle.SPELL,-1,atk) : new Battle.Action(Battle.ATTACK,-1,0)); }
            }
            b.playRound(acts); seconds+=4;
        }
        if(b.lost()){ defeats++; s.defeated(); return false; }
        s.battlesWon++; return true;
    }
    static void rest(){ int[] inn = s.map==RpgData.ERDEN||s.townMap==RpgData.ERDEN ? new int[]{RpgData.ERDEN,16,12,10} : new int[]{RpgData.REBEL,4,12,6};
        goToTown(); RpgData.Npc n=null; for(RpgData.Npc x:RpgData.NPCS) if(x.map==s.map&&x.action==RpgData.ACT_INN) n=x;
        walkTo(n.x,n.y,true); if(!s.rest(n.arg)) { for(Hero h:s.party) h.fullHeal(); } rests++; }
    static void goToTown(){ int guard=0; while(s.map!=s.townMap && guard++<50){ if(s.hasItem(RpgData.WING)){ s.useItemInField(RpgData.WING,s.party.get(0)); continue; }
        // 평원으로 나가 마을 입구로
        if(s.map==RpgData.CAVE2) walkTo(10,13,false); else if(s.map==RpgData.CAVE1) walkTo(10,14,false);
        else if(s.map==RpgData.WORLD) walkTo(s.townMap==RpgData.REBEL?6:20, s.townMap==RpgData.REBEL?18:8,false);
        else if(s.map==RpgData.REBEL) walkTo(8,13,false); else if(s.map==RpgData.ERDEN) walkTo(9,15,false); } }
    static void talk(int map,int x,int y){ walkTo(x,y,true); RpgState.Interaction in=s.interact(); if(in.bossBattle){ boolean w=fight(new int[]{RpgData.WOLF_KING},true); if(w) s.onBossDefeated(); } }
    static void shop(int[] buy){ for(int id:buy){ if(s.gold>=s.price(id)){ s.buy(id); RpgData.Item it=RpgData.ITEMS[id];
        if(it.type!=RpgData.CONSUMABLE){ Hero best=s.party.get(0); for(Hero h:s.party){ int cur= it.type==RpgData.WEAPON? RpgData.ITEMS[h.weapon].power:RpgData.ITEMS[h.armor].power; if(cur<it.power){best=h;break;} } s.equip(best,id);} } } }
    static void grind(int map,int ex,int ey,int targetLevel){ // 마을 근처를 왔다갔다
        while(s.party.get(0).level<targetLevel){ if(lowHp()){ rest(); }
            if(s.map!=RpgData.WORLD && s.map!=map) { goToTown(); if(s.map==RpgData.REBEL) walkTo(8,13,false); else walkTo(9,15,false); }
            int bx=ex+rnd.nextInt(5)-2, by=ey+rnd.nextInt(3)-1; if(s.map==RpgData.WORLD && s.walkable(bx,by) && !isLink(bx,by)) walkTo(bx,by,false); } }
    static String party(){ StringBuilder b=new StringBuilder(); for(Hero h:s.party) b.append(h.name()).append(" Lv").append(h.level).append(" ");
        return b+"| "+s.gold+"G | 전투 "+battles+", 전멸 "+defeats+", "+String.format("%.0f분",seconds/60); }

    public static void main(String[] a){
        s=new RpgState(); s.setRandom(rnd); s.perkBroker=s.perkFire=s.perkFaith=true;
        ok(s.map==RpgData.OFFICE,"집무실에서 시작");
        walkTo(9,4,true); s.interact(); ok(s.map==RpgData.WORLD && s.step==RpgData.STEP_ARRIVED,"차원 이동 장치 → 아르카디아 평원");
        walkTo(6,18,false); ok(s.map==RpgData.REBEL,"리벨 마을 입장");
        talk(RpgData.REBEL,4,6); ok(s.step==RpgData.STEP_LETTER && s.hasItem(RpgData.LETTER),"장로에게 편지 받음");
        walkTo(14,6,true); shop(new int[]{RpgData.COPPER_SWORD, RpgData.LEATHER, RpgData.HERB, RpgData.HERB});
        walkTo(8,13,false); grind(RpgData.REBEL,8,16,5); System.out.println("   다리 남쪽에서 레벨업: "+party());
        walkTo(20,8,false); ok(s.map==RpgData.ERDEN,"에르덴 성 입장");
        talk(RpgData.ERDEN,9,4); ok(s.step==RpgData.STEP_LINA && s.party.size()==2,"왕 알현 → 리나 합류 (Lv"+s.party.get(1).level+")");
        walkTo(9,15,false); grind(RpgData.ERDEN,20,10,9); rest();
        walkTo(4,12,true); shop(new int[]{RpgData.IRON_SWORD, RpgData.CHAIN_MAIL, RpgData.HERB, RpgData.HERB, RpgData.HERB, RpgData.WING});
        System.out.println("   동굴 가기 전: "+party());
        walkTo(9,15,false); walkTo(28,3,false); ok(s.map==RpgData.CAVE1,"그림자 동굴 1층");
        talk(RpgData.CAVE1,20,1); talk(RpgData.CAVE1,11,4); ok(s.chestOpened[0]&&s.chestOpened[1],"1층 보물상자 2개");
        int tries=0; while(s.map!=RpgData.CAVE2 && tries++<20){ if(s.map!=RpgData.CAVE1){ rest(); walkTo(9,15,false); walkTo(28,3,false);} walkTo(4,12,false);} 
        ok(s.map==RpgData.CAVE2,"동굴 2층");
        talk(RpgData.CAVE2,16,4); ok(s.chestOpened[2] && s.hasItem(RpgData.SILVER_ARMOR),"은빛 갑옷 획득"); s.equip(s.party.get(1), RpgData.SILVER_ARMOR);
        System.out.println("   보스 직전: "+party());
        int attempts=0; while(s.step==RpgData.STEP_LINA && attempts++<6){
            if(s.map!=RpgData.CAVE2 || lowHp()){ rest(); walkTo(9,15,false); walkTo(28,3,false); while(s.map==RpgData.CAVE1) walkTo(4,12,false); }
            if(s.map==RpgData.CAVE2) talk(RpgData.CAVE2,5,4);
            if(s.step==RpgData.STEP_LINA) { System.out.println("   보스에게 짐 → 레벨업 후 재도전 "+party()); rest(); walkTo(9,15,false); grind(RpgData.ERDEN,24,8,s.party.get(0).level+1); }
        }
        ok(s.step==RpgData.STEP_BOSS_DOWN,"그림자 늑대왕 격파 (시도 "+attempts+"번) "+party());
        goToTown(); if(s.map!=RpgData.ERDEN){ walkTo(s.map==RpgData.REBEL?8:9, s.map==RpgData.REBEL?13:15,false); walkTo(20,8,false);} 
        RpgState.Interaction in=null; walkTo(9,4,true); in=s.interact(); ok(in.chapterClear && s.step==RpgData.STEP_CHAPTER1_CLEAR && s.hasItem(RpgData.HERO_BADGE),"왕에게 보고 → 1장 클리어");
        String save=s.serialize(); RpgState r=RpgState.deserialize(save); ok(r.serialize().equals(save),"저장/불러오기 일치");
        System.out.println("   최종: "+party()+" | 걸음 "+s.stepsWalked+", 휴식 "+rests);
    }
}
