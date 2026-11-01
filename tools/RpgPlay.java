// 실행: javac -encoding UTF-8 -d out app/src/main/java/com/shinchang/adventure/rpg/{RpgData,RpgMaps,RpgState,Hero,Battle,Josa}.java tools/RpgPlay.java && java -cp out RpgPlay
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

    static int[] levelAt = new int[40];
    static boolean fight(int[] ids, boolean boss){
        battles++; Battle b=new Battle(s, ids, boss, rnd); int r=0;
        while(!b.over()){ b.playRound(BossSim.ai(s,b,r++)); seconds+= boss?8:5; }
        for(Hero h:s.party) if(h.id==0 && levelAt[h.level]==0) { levelAt[h.level]=(int)Math.round(seconds/60); }
        if(b.lost()){ defeats++; s.defeated(); return false; }
        s.battlesWon++; return true;
    }
    static void rest(){ goToTown(); RpgData.Npc n=null; for(RpgData.Npc x:RpgData.NPCS) if(x.map==s.map&&x.action==RpgData.ACT_INN) n=x;
        // 여관 주인은 계산대 너머에 있다: 리벨은 아래에서 위로, 에르덴은 위에서 아래로
        if(s.map==RpgData.REBEL) across(n.x,n.y+2,RpgState.UP); else across(n.x,n.y-2,RpgState.DOWN);
        RpgState.Interaction in=s.interact(); ok(in.action==RpgData.ACT_INN,"여관 주인과 계산대 너머 대화",true);
        if(!s.rest(n.arg)) { for(Hero h:s.party) h.fullHeal(); } rests++; }
    static void across(int sx,int sy,int dir){ walkTo(sx,sy,false); s.dir=dir; }
    static java.util.Set<String> once=new java.util.HashSet<>();
    static void ok(boolean c,String m,boolean quietRepeat){ if(!c) throw new AssertionError(m); if(once.add(m)) System.out.println("✓ "+m); }
    static void goToTown(){ int guard=0; while(s.map!=s.townMap && guard++<50){ if(s.hasItem(RpgData.WING)){ s.useItemInField(RpgData.WING,s.party.get(0)); continue; }
        // 평원으로 나가 마을 입구로
        if(s.map==RpgData.CAVE2) walkTo(10,13,false); else if(s.map==RpgData.CAVE1) walkTo(10,14,false);
        else if(s.map==RpgData.WORLD) walkTo(s.townMap==RpgData.REBEL?6:20, s.townMap==RpgData.REBEL?18:8,false);
        else exitTown(); } }
    static void exitTown(){ if(s.map==RpgData.REBEL) walkTo(12,21,false); else if(s.map==RpgData.ERDEN) walkTo(13,27,false); }
    static void talk(int map,int x,int y){ walkTo(x,y,true); RpgState.Interaction in=s.interact(); if(in.bossBattle){ boolean w=fight(new int[]{RpgData.WOLF_KING},true); if(w) s.onBossDefeated(); } }
    static void shop(int[] buy){ for(int id:buy){ if(s.gold>=s.price(id)){ s.buy(id); RpgData.Item it=RpgData.ITEMS[id];
        if(it.type!=RpgData.CONSUMABLE){ Hero best=s.party.get(0); for(Hero h:s.party){ int cur= it.type==RpgData.WEAPON? RpgData.ITEMS[h.weapon].power:RpgData.ITEMS[h.armor].power; if(cur<it.power){best=h;break;} } s.equip(best,id);} } } }
    static void grind(int map,int ex,int ey,int targetLevel){ // 마을 근처를 왔다갔다
        while(s.party.get(0).level<targetLevel){ if(lowHp()){ rest(); }
            if(s.map!=RpgData.WORLD && s.map!=map) { goToTown(); exitTown(); }
            int bx=ex+rnd.nextInt(5)-2, by=ey+rnd.nextInt(3)-1; if(s.map==RpgData.WORLD && s.walkable(bx,by) && !isLink(bx,by)) walkTo(bx,by,false); } }
    /** 동굴 2층 계단 근처에서 강한 적을 사냥한다 (지치면 여관에 다녀온다) */
    static void caveGrind(int targetLevel){
        while(s.party.get(0).level<targetLevel){
            if(s.map!=RpgData.CAVE2 || lowHp()){ rest(); exitTown(); walkTo(28,3,false); while(s.map==RpgData.CAVE1) walkTo(4,12,false); }
            int bx=8+rnd.nextInt(7), by=13; if(s.walkable(bx,by) && !isLink(bx,by)) walkTo(bx,by,false);
        }
        rest(); exitTown(); walkTo(28,3,false); while(s.map==RpgData.CAVE1) walkTo(4,12,false);
    }

    static String party(){ StringBuilder b=new StringBuilder(); for(Hero h:s.party) b.append(h.name()).append(" Lv").append(h.level).append(" ");
        return b+"| "+s.gold+"G | 전투 "+battles+", 전멸 "+defeats+", "+String.format("%.0f분",seconds/60); }

    public static void main(String[] a){
        s=new RpgState(); s.setRandom(rnd); s.perkBroker=s.perkFire=s.perkFaith=true;
        ok(s.map==RpgData.OFFICE,"집무실에서 시작");
        walkTo(15,4,true); RpgState.Interaction rec=s.interact(); ok(s.hasFlag(RpgData.FLAG_RECORD) && rec.lines.size()>3,"금고 기록 상자 → 단서 「금고의 경고문」");
        walkTo(15,2,true); s.interact(); ok(s.map==RpgData.WORLD && s.step==RpgData.STEP_ARRIVED,"차원 이동 장치 → 아르카디아 평원");
        walkTo(6,18,false); ok(s.map==RpgData.REBEL,"리벨 마을 입장");
        talk(RpgData.REBEL,4,5); ok(s.step==RpgData.STEP_LETTER && s.hasItem(RpgData.LETTER),"장로에게 편지 받음");
        walkTo(3,3,true); s.interact(); ok(s.hasFlag(RpgData.FLAG_CHRONICLE),"장로 책장 「리벨 연대기」");
        walkTo(17,20,true); s.interact(); ok(s.hasFlag(RpgData.FLAG_MURAL),"성당 벽화");
        across(19,5,RpgState.UP); RpgState.Interaction shopIn=s.interact(); ok(shopIn.action==RpgData.ACT_SHOP,"도구점: 계산대 너머 주인과 대화");
        shop(new int[]{RpgData.COPPER_SWORD, RpgData.LEATHER, RpgData.HERB, RpgData.HERB});
        for(RpgData.Npc n:s.visibleNpcs()){ if(n.y>=1) { boolean reach = path(n.x,n.y,true)!=null || n.action==RpgData.ACT_SHOP || n.action==RpgData.ACT_INN; ok(reach, "리벨 "+n.name+"에게 갈 수 있음", true);} }
        exitTown(); grind(RpgData.REBEL,8,16,5); System.out.println("   다리 남쪽에서 레벨업: "+party());
        walkTo(20,8,false); ok(s.map==RpgData.ERDEN,"에르덴 성 입장");
        for(RpgData.Npc n:s.visibleNpcs()){ boolean reach = path(n.x,n.y,true)!=null || n.action==RpgData.ACT_SHOP || n.action==RpgData.ACT_INN; ok(reach, "에르덴 "+n.name+"에게 갈 수 있음", true); }
        walkTo(5,2,true); s.interact(); ok(s.hasFlag(RpgData.FLAG_LIBRARY),"에르덴 서고 책장");
        talk(RpgData.ERDEN,13,3); ok(s.step==RpgData.STEP_LINA && s.party.size()==2,"왕 알현 → 리나 합류 (Lv"+s.party.get(1).level+")");
        exitTown(); grind(RpgData.ERDEN,20,10,9); rest();
        across(5,21,RpgState.DOWN); ok(s.interact().action==RpgData.ACT_SHOP,"무기점: 계산대 너머 주인과 대화");
        shop(new int[]{RpgData.IRON_SWORD, RpgData.CHAIN_MAIL, RpgData.HERB, RpgData.HERB, RpgData.HERB, RpgData.WING});
        System.out.println("   동굴 가기 전: "+party());
        exitTown(); walkTo(28,3,false); ok(s.map==RpgData.CAVE1,"그림자 동굴 1층");
        talk(RpgData.CAVE1,20,1); talk(RpgData.CAVE1,11,4); ok(s.chestOpened[0]&&s.chestOpened[1],"1층 보물상자 2개");
        int tries=0; while(s.map!=RpgData.CAVE2 && tries++<20){ if(s.map!=RpgData.CAVE1){ rest(); exitTown(); walkTo(28,3,false);} walkTo(4,12,false);} 
        ok(s.map==RpgData.CAVE2,"동굴 2층");
        talk(RpgData.CAVE2,16,4); ok(s.chestOpened[2] && s.hasItem(RpgData.SILVER_ARMOR),"은빛 갑옷 획득"); s.equip(s.party.get(1), RpgData.SILVER_ARMOR);
        walkTo(17,11,true); RpgState.Interaction sh=s.interact(); ok(s.hasFlag(RpgData.FLAG_DIARY) && s.hasItem(RpgData.LOWELL_DIARY) && sh.lines.get(sh.lines.size()-1).startsWith("[리나]"),"로웰의 방패와 일기 (리나 반응)");
        System.out.println("   보스 직전: "+party());
        int attempts=0; while(s.step==RpgData.STEP_LINA && attempts++<6){
            if(s.map!=RpgData.CAVE2 || lowHp()){ rest(); exitTown(); walkTo(28,3,false); while(s.map==RpgData.CAVE1) walkTo(4,12,false); }
            if(s.map==RpgData.CAVE2) talk(RpgData.CAVE2,5,4);
            if(s.step==RpgData.STEP_LINA) { System.out.println("   보스에게 짐 → 동굴 2층에서 강적 사냥 후 재도전 "+party()); caveGrind(s.party.get(0).level+1); }
        }
        ok(s.step==RpgData.STEP_BOSS_DOWN && s.hasItem(RpgData.SEAL_SHARD),"그림자 늑대왕 격파 + 깨진 봉인석 (시도 "+attempts+"번) "+party());
        goToTown(); if(s.map!=RpgData.ERDEN){ exitTown(); walkTo(20,8,false);} 
        RpgState.Interaction in=null; walkTo(13,3,true); in=s.interact(); ok(in.chapterClear && s.step==RpgData.STEP_CHAPTER1_CLEAR && s.hasItem(RpgData.HERO_BADGE),"왕에게 보고 → 1장 클리어");
        ok(String.join("\n", in.lines).contains("로웰이 살아 있다니"),"일기를 찾았으면 왕의 대사가 달라짐");
        ok(s.clues().size()==5,"단서 5개 모두: "+s.clues());
        exitTown(); walkTo(6,18,false); walkTo(9,9,true); RpgState.Interaction bell=s.interact();
        ok(s.hasItem(RpgData.LUPUS_BELL) && bell.lines.size()>3,"보스 후 사냥꾼 브람에게 「루프스의 방울」");
        ok(s.npcAt(14,9)!=null && s.npcAt(14,9).name.equals("콩이"),"콩이가 리벨 마을로 돌아옴");
        String save=s.serialize(); RpgState r=RpgState.deserialize(save); ok(r.serialize().equals(save),"저장/불러오기 일치");
        System.out.println("   최종: "+party()+" | 걸음 "+s.stepsWalked+", 휴식 "+rests);
        StringBuilder lv=new StringBuilder("   레벨 도달 시각(분): "); int prev=0;
        for(int i=2;i<levelAt.length;i++) if(levelAt[i]>0||i==2){ lv.append("Lv").append(i).append("=").append(levelAt[i]).append(" (+").append(levelAt[i]-prev).append(") "); prev=levelAt[i]; }
        System.out.println(lv);
    }
}
