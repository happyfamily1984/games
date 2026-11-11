package com.shinchang.adventure.rpg;

/** 도트 그림 데이터 (tools/rpg_art.py 로 생성). '.' 은 투명. */
public final class RpgArt {

    static final java.util.Map<Character, Integer> COLORS = new java.util.HashMap<>();
    static {
        COLORS.put('k', 0xFF1A1A1A);
        COLORS.put('w', 0xFFFFFFFF);
        COLORS.put('s', 0xFFF2C19A);
        COLORS.put('o', 0xFFFF7A00);
        COLORS.put('b', 0xFF2E5EBD);
        COLORS.put('B', 0xFF1B3A7A);
        COLORS.put('r', 0xFFD7262E);
        COLORS.put('R', 0xFF8E1B26);
        COLORS.put('y', 0xFFF5C242);
        COLORS.put('Y', 0xFFC9962A);
        COLORS.put('g', 0xFF4CAF50);
        COLORS.put('G', 0xFF2E7D32);
        COLORS.put('n', 0xFF8C5A2B);
        COLORS.put('N', 0xFF5A3818);
        COLORS.put('e', 0xFF9AA1A8);
        COLORS.put('E', 0xFF5B6168);
        COLORS.put('p', 0xFF7B4BB5);
        COLORS.put('q', 0xFF4A2C73);
        COLORS.put('P', 0xFFF59AB8);
        COLORS.put('c', 0xFF5BC0EB);
        COLORS.put('h', 0xFF2B2B2B);
        COLORS.put('l', 0xFFD9DDE1);
        COLORS.put('t', 0xFFE3C88F);
        COLORS.put('T', 0xFFC9A86A);
        COLORS.put('u', 0xFF9FC6E8);
        COLORS.put('v', 0xFF3F7FC4);
        COLORS.put('f', 0xFFD8C39A);
        COLORS.put('F', 0xFFC1A878);
        COLORS.put('d', 0xFF4A3B2F);
        COLORS.put('D', 0xFF3A2E25);
        COLORS.put('m', 0xFF6E5A48);
        COLORS.put('M', 0xFF55453A);
        COLORS.put('x', 0xFFB0362F);
        COLORS.put('z', 0xFF2A2238);
    }

    static final java.util.Map<String, String[]> CHARS = new java.util.HashMap<>();
    static {
        CHARS.put("hero", new String[]{"...oooo...", "..oooooo..", ".oooooooo.", "..hssssh..", "..skssks..", "..ssssss..", "...ssss...", "..bbbbbb..", ".sbbyybbs.", ".sbbbbbbs.", "..NNNNNN..", "..NN..NN.."});
        CHARS.put("lina", new String[]{"...rrrr...", "..rrrrrr..", ".rrssssrr.", ".rskssksr.", ".rssssssr.", "..rssssr..", "..llllll..", ".slleells.", ".slllllls.", "..eeeeee..", "..BB..BB..", "..kk..kk.."});
        CHARS.put("secretary", new String[]{"...hhhh...", "..hhhhhh..", "..hssssh..", "..skssks..", "..ssssss..", "...ssss...", "..EEEEEE..", ".sEEwwEEs.", ".sEEEEEEs.", "..EEEEEE..", "..EE..EE..", "..kk..kk.."});
        CHARS.put("guard", new String[]{"...hhhh...", "..hhhhhh..", "..hssssh..", "..skssks..", "..ssssss..", "...ssss...", "..kkkkkk..", ".skkwwkks.", ".skkkkkks.", "..kkkkkk..", "..kk..kk..", "..kk..kk.."});
        CHARS.put("elder", new String[]{"...llll...", "..llllll..", "..lssssl..", "..skssks..", "..llllll..", "...llll...", "..pppppp..", ".sppyypps.", ".spppppps.", "..pppppp..", "..pppppp..", "..kk..kk.."});
        CHARS.put("merchant", new String[]{"...nnnn...", "..nnnnnn..", "..nssssn..", "..skssks..", "..ssssss..", "...ssss...", "..gggggg..", ".sggyyggs.", ".sggggggs.", "..gggggg..", "..NN..NN..", "..kk..kk.."});
        CHARS.put("innkeeper", new String[]{"...NNNN...", "..NNNNNN..", "..NssssN..", "..skssks..", "..ssssss..", "...ssss...", "..xxxxxx..", ".sxxwwxxs.", ".sxxxxxxs.", "..xxxxxx..", "..NN..NN..", "..kk..kk.."});
        CHARS.put("priest", new String[]{"...wwww...", "..wwwwww..", "..wssssw..", "..skssks..", "..ssssss..", "...ssss...", "..wwwwww..", ".swwyywws.", ".swwwwwws.", "..wwwwww..", "..wwwwww..", "..kk..kk.."});
        CHARS.put("child", new String[]{"..........", "..........", "...nnnn...", "..nnnnnn..", "..nssssn..", "..skssks..", "...ssss...", "..cccccc..", ".sccccccs.", "..cccccc..", "..bb..bb..", "..kk..kk.."});
        CHARS.put("farmer", new String[]{"...yyyy...", "..yyyyyy..", "..yssssy..", "..skssks..", "..ssssss..", "...ssss...", "..nnnnnn..", ".snnttnns.", ".snnnnnns.", "..nnnnnn..", "..BB..BB..", "..kk..kk.."});
        CHARS.put("villager", new String[]{"...eeee...", "..eeeeee..", "..esssse..", "..skssks..", "..ssssss..", "...ssss...", "..PPPPPP..", ".sPPwwPPs.", ".sPPPPPPs.", "..PPPPPP..", "..PPPPPP..", "..kk..kk.."});
        CHARS.put("king", new String[]{"..y.yy.y..", "..yyyyyy..", "..lssssl..", "..skssks..", "..lssssl..", "...llll...", ".RrrrrrrR.", "RsrryyrrsR", "RRrrrrrrRR", "RRrrrrrrRR", "RRrrrrrrRR", "..kk..kk.."});
        CHARS.put("minister", new String[]{"...hhhh...", "..hhhhhh..", "..hssssh..", "..skssks..", "..ssssss..", "...ssss...", "..BBBBBB..", ".sBByyBBs.", ".sBBBBBBs.", "..BBBBBB..", "..BBBBBB..", "..kk..kk.."});
        CHARS.put("soldier", new String[]{"...eeee...", "..eeeeee..", "..esssse..", "..skssks..", "..ssssss..", "...ssss...", "..vvvvvv..", ".svvyyvvs.", ".svvvvvvs.", "..vvvvvv..", "..NN..NN..", "..kk..kk.."});
        CHARS.put("boss", new String[]{"..........", "qq......qq", "qqq....qqq", "qqqqqqqqqq", "qqrqqqqrqq", "qqqqqqqqqq", ".qqwqqwqq.", "..qqqqqq..", ".qqqqqqqq.", "qqqqqqqqqq", "qq.qqqq.qq", "kk.kkkk.kk"});
        CHARS.put("hunter", new String[]{"...nnnn...", "..nnnnnn..", "..nssssn..", "..skssks..", "..ssssss..", "...ssss...", "..GGGGGG..", ".sGGnnGGs.", ".sGGGGGGs.", "..GGGGGG..", "..NN..NN..", "..kk..kk.."});
        CHARS.put("captain", new String[]{"...BBBB...", "..BBBBBB..", "..BssssB..", "..skssks..", "..ssssss..", "...ssss...", "..wwwwww..", ".swwyywws.", ".swwwwwws.", "..wwwwww..", "..BB..BB..", "..kk..kk.."});
        CHARS.put("baron", new String[]{"...nnnn...", "..nnnnnn..", "..nssssn..", "..skssks..", "..ssssss..", "...ssss...", "..qqqqqq..", ".sqqyyqqs.", ".sqqqqqqs.", "..qqqqqq..", "..qqqqqq..", "..kk..kk.."});
        CHARS.put("apprentice", new String[]{"...yyyy...", "..yyyyyy..", "..yssssy..", "..skssks..", "..ssssss..", "...ssss...", "..cccccc..", ".sccwwccs.", ".sccccccs.", "..cccccc..", "..NN..NN..", "..kk..kk.."});
        CHARS.put("sailor", new String[]{"...NNNN...", "..NNNNNN..", "..NssssN..", "..skssks..", "..ssssss..", "...ssss...", "..wwwwww..", ".swwbbwws.", ".swwwwwws.", "..wwwwww..", "..bb..bb..", "..kk..kk.."});
        CHARS.put("pirate", new String[]{"...rrrr...", "..rrrrrr..", "..rssssr..", "..skssks..", "..ssssss..", "...ssss...", "..RRRRRR..", ".sRRkkRRs.", ".sRRRRRRs.", "..RRRRRR..", "..kk..kk..", "..kk..kk.."});
        CHARS.put("pipi", new String[]{"..........", "u..PPPP..u", "uuPPPPPPuu", "uuPssssPuu", ".uskssksu.", "..ssssss..", "..gggggg..", ".sggyyggs.", "..gggggg..", "..gggggg..", "...s..s...", "...k..k..."});
        CHARS.put("serpent", new String[]{"..........", "...cccc...", "..cccccc..", "..crccrc..", "..cccccc..", "...cccc...", "..vccccv..", ".vvccccvv.", "vvccccccvv", "vccvvvvccv", "vvvvvvvvvv", ".........."});
        CHARS.put("pup", new String[]{"..........", "..........", "..........", "..........", ".n......n.", ".nn....nn.", ".nnnnnnnn.", ".nkknnkkn.", "..nnnnnn..", "..nnnnnn..", "..n.nn.n..", ".........."});
    }

    static final java.util.Map<String, String[]> MONSTERS = new java.util.HashMap<>();
    static {
        MONSTERS.put("rat", new String[]{"................", "................", "..nn........nn..", ".nNNn......nNNn.", ".nNNnnnnnnnnNNn.", "..nnnnnnnnnnnn..", "..nkkkknnkkkkn..", "..nkwkknnkkwkn..", "..nnnnnnnnnnnn..", "...nnnnPPnnnn...", "....nnnnnnnn....", "...nnnnnnnnnn...", "..nnnnnnnnnnnn..", "..nnnn....nnnn..", "..NNN......NNN..", "................"});
        MONSTERS.put("shroom", new String[]{"................", ".....rrrrrr.....", "...rrwwrrwwrr...", "..rrrwwrrwwrrr..", ".rrrrrrrrrrrrrr.", ".rrwwrrrrrrwwrr.", ".rrwwrrrrrrwwrr.", "..rrrrrrrrrrrr..", "....tttttttt....", "....tkttttkt....", "....tttttttt....", "....ttkkkktt....", ".....tttttt.....", "....tttttttt....", "...ss......ss...", "................"});
        MONSTERS.put("bat", new String[]{"................", "................", "................", "pp............pp", "ppp...p..p...ppp", "pppp..pppp..pppp", "pppppppppppppppp", ".pppppkwwkppppp.", "..ppppkkkkpppp..", "...pppPPPPppp...", "....pppppppp....", ".....pp..pp.....", "................", "................", "................", "................"});
        MONSTERS.put("thornwolf", new String[]{"................", "..G..........G..", "..GG........GG..", ".GGGG......GGGG.", ".gggggg..gggggg.", ".ggrggggggggrgg.", ".gggggg..gggggg.", "..gwgg....ggwg..", "..gggggggggggg..", ".Gggggg..gggggG.", "GGggggggggggggGG", ".gggggggggggggg.", "..gggggggggggg..", "..gg..gggg..gg..", "..kk..kkkk..kk..", "................"});
        MONSTERS.put("pixie", new String[]{"................", "...c........c...", "..cuc......cuc..", ".cuuc.yyyy.cuuc.", ".cuucyyyyyycuuc.", "..cucsssssscuc..", "...cskskksksc...", "....ssssssss....", ".....ssssss.....", "....PPPPPPPP....", "...PPPPPPPPPP...", "....PPPPPPPP....", ".....s....s.....", ".....s....s.....", "................", "................"});
        MONSTERS.put("turtle", new String[]{"................", "................", "................", ".....eeeeee.....", "...eeEEeeEEee...", "..eEEeeEEeeEEe..", ".eEeeEEeeEEeeEe.", ".eeEEeeeeeeEEee.", "eEeeeeEEEEeeeeEe", "eeeeeeeeeeeeeeee", ".gggggggggggggg.", "gsg..gggggg..gsg", "ggg...gggg...ggg", "................", "................", "................"});
        MONSTERS.put("spider", new String[]{"................", "................", "e..............e", ".e...EEEEEE...e.", "..e.EEEEEEEE.e..", "e..EEEEEEEEEE..e", ".e.EErEEEErEE.e.", "..eEEEEEEEEEEe..", "e..EEEEEEEEEE..e", ".e.EEEEEEEEEE.e.", "..e.EEEEEEEE.e..", ".e...EEEEEE...e.", "e..............e", "................", "................", "................"});
        MONSTERS.put("skeleton", new String[]{"................", "....llllllll....", "...llllllllll...", "...lkkllllkkl...", "...lkkllllkkl...", "....llllllll....", ".....lkllkl.....", "...llllllllll...", "..l.llllllll.l..", ".l..l.llll.l..l.", "....llllllll....", ".....l.ll.l.....", "....ll.ll.ll....", "....l..ll..l....", "...ll..ll..ll...", "................"});
        MONSTERS.put("shadowwolf", new String[]{"................", "..p..........p..", "..pp........pp..", ".pppp......pppp.", ".qqqqqq..qqqqqq.", ".qqrqqqqqqqqrqq.", ".qqqqqq..qqqqqq.", "..qwqq....qqwq..", "..qqqqqqqqqqqq..", ".pqqqqq..qqqqqp.", "ppqqqqqqqqqqqqpp", ".qqqqqqqqqqqqqq.", "..qqqqqqqqqqqq..", "..qq..qqqq..qq..", "..kk..kkkk..kk..", "................"});
        MONSTERS.put("crab", new String[]{"................", "................", "rr............rr", "r.r..........r.r", ".rr..........rr.", "..r..rrrrrr..r..", ".....rrrrrr.....", "...rrrrrrrrrr...", "..rrkwrrrrwkrr..", ".rrrrrrrrrrrrrr.", "rrrrrrrrrrrrrrrr", ".rrRRRRRRRRRRrr.", "..r.r.r..r.r.r..", ".r..r..rr..r..r.", "................", "................"});
        MONSTERS.put("gull", new String[]{"................", "................", "......llll......", "....llllllll....", "...lkkkllkkkl...", "...lkwkllkwkl...", "....lllyylll....", "..llllllllllll..", ".llllllllllllll.", "eeelllllllllleee", "eeeelll..llleeee", "...elll..llle...", "....y.y..y.y....", "....y.y..y.y....", "................", "................"});
        MONSTERS.put("saltspirit", new String[]{"................", ".......ww.......", "......wwww......", ".....wlwwlw.....", "....wllllllw....", "...wllllllllw...", "...lkllkkllkl...", "...llllllllll...", "...llllllllll...", "....llllllll....", ".....llllll.....", "....l.llll.l....", "...l..l..l..l...", "................", "................", "................"});
        MONSTERS.put("jelly", new String[]{"................", "................", ".....PPPPPP.....", "...PPPPPPPPPP...", "..PPPwPPPPwPPP..", "..PPPPPPPPPPPP..", "..PkPPPkkPPPkP..", "..PPPPPPPPPPPP..", "...PPPPPPPPPP...", "...p.p.pp.p.p...", "...p.p.pp.p.p...", "..p..p....p..p..", "..p...p..p...p..", "................", "................", "................"});
        MONSTERS.put("pirate", new String[]{"................", "....rrrrrrrr....", "...rrrrrrrrrr...", "...skssssssks...", "...ssssssssss...", "....ssssssss....", "...NNNNNNNNNN...", "..wNwNNNNNNwNw..", "..swwwwwwwwwws..", "..swwwwwwwwwws..", ".e.BBBBBBBBBB.e.", "e..BBBBBBBBBB..e", "...BB..BB..BB...", "...kk..kk..kk...", "................", "................"});
        MONSTERS.put("octopus", new String[]{"................", "....pppppppp....", "...pppppppppp...", "..pppppppppppp..", "..ppkwppppwkpp..", "..pppppppppppp..", "...pppppppppp...", "..p.p.pppp.p.p..", ".p..p.p..p.p..p.", ".p.p..p..p..p.p.", "p..p.p....p.p..p", "...p........p...", "................", "................", "................", "................"});
        MONSTERS.put("urchin", new String[]{"................", "...e..e..e..e...", "....e.e..e.e....", "..e.eee..eee.e..", "...eqqqqqqqqe...", "..eqqqqqqqqqqe..", ".eqqrqqqqqqrqqe.", "..qqqqqqqqqqqq..", "eeqqqqqqqqqqqqee", "..qqqqqqqqqqqq..", ".eqqqqqqqqqqqqe.", "..eqqqqqqqqqqe..", "...e.eeeeee.e...", "..e..e....e..e..", "................", "................"});
        MONSTERS.put("golem", new String[]{"................", "....xxxxxxxx....", "...xPPPxxPPPx...", "...xPkPxxPkPx...", "...xPPPxxPPPx...", "..xxxxxxxxxxxx..", ".xxPPPPPPPPPPxx.", "xxPPxPPPPPPxPPxx", "xPPPPPPPPPPPPPPx", "xx.xPPPPPPPPx.xx", "....xPPPPPPx....", "....xPPxxPPx....", "...xxx.xx.xxx...", "...xxx....xxx...", "................", "................"});
        MONSTERS.put("merknight", new String[]{"................", "....eeeeeeee....", "...eeeeeeeeee...", "...esssssssse...", "...skskssksks...", "....ssssssss....", "..cccccccccccc..", "y.cvvvccccvvvc.y", "y.ccccvvvvcccc.y", "y..cccccccccc..y", "...cccccccccc...", "....cccccccc....", "....c.cccc.c....", "...c..cccc..c...", "................", "................"});
        MONSTERS.put("skelsailor", new String[]{"....wwwwwwww....", "...bbbbbbbbbb...", "...llllllllll...", "...lkkllllkkl...", "...lkkllllkkl...", "....llllllll....", ".....lkllkl.....", "...bbbbbbbbbb...", "..l.wbwbbwbw.l..", ".l..l.llll.l..l.", "....llllllll....", ".....l.ll.l.....", "....ll.ll.ll....", "....l..ll..l....", "...ll..ll..ll...", "................"});
        MONSTERS.put("statue", new String[]{"....eeeeeeee....", "...eeeeeeeeee...", "...eEyEeeEyEe...", "...eeeeeeeeee...", "..EEEEEEEEEEEE..", ".eeeeeeeeeeeeee.", "eeeEeeeeeeeeEeee", "eeeeeeeeeeeeeeee", "e.eeeeeeeeeeee.e", "e.eEeeeeeeeeEe.e", "..eeeeeeeeeeee..", "..eeeeeeeeeeee..", "..ee.eeeeee.ee..", "..EE.EEEEEE.EE..", "................", "................"});
        MONSTERS.put("eel", new String[]{"................", "................", "................", "......vvvv......", ".....vvvvvv.....", "....vvkvvkvv....", "...vvvvvvvvvv...", "..vvv......vvv..", ".vvv........vvv.", "vvv..........vvv", "vvvv........vvvv", ".vvvvv....vvvvv.", "...vvvvvvvvvv...", ".....vvvvvv.....", "................", "................"});
        MONSTERS.put("stardust", new String[]{"................", "................", "....y......y....", "...yyy....yyy...", "....y..ww..y....", "......wwww......", "....wwywwyww....", "...wyyyyyyyyw...", "...wykykkykyw...", "....yyyyyyyy....", "...wwyyyyyyww...", "....w.yyyy.w....", "......y..y......", "................", "................", "................"});
        MONSTERS.put("goldtoad", new String[]{"................", "................", "................", "................", "....yyyyyyyy....", "...yYYyyyyYYy...", "..ykwyyyyyywky..", "..yyyyyyyyyyyy..", ".yyyyyyyyyyyyyy.", ".yYyyyYyyYyyyYy.", "yyyyyyyyyyyyyyyy", "yyYyyyyYYyyyyYyy", ".yy..yyyyyy..yy.", "Yy...YyyyyY...yY", "................", "................"});
        MONSTERS.put("pearl", new String[]{"................", "................", "....u......u....", "...uuu....uuu...", "....u..ww..u....", "......wwww......", "....wwPwwPww....", "...wPPPPPPPPw...", "...wPkPkkPkPw...", "....PPPPPPPP....", "...wwPPPPPPww...", "....w.PPPP.w....", "......P..P......", "................", "................", "................"});
        MONSTERS.put("gemcrab", new String[]{"................", "................", "cc............cc", "c.c..........c.c", ".cc..y....y..cc.", "..c.yby..yby.c..", ".....ybyyby.....", "...ycbcyycbcy...", "..ycbcbccbcbcy..", ".cccbcbccbcbccc.", "cccccccccccccccc", ".ccrrrrrrrrrrcc.", "..c.c.c..c.c.c..", ".c..c..cc..c..c.", "................", "................"});
        MONSTERS.put("wolfking", new String[]{"........................", "...y................y...", "...yy..............yy...", "..ypyy............yypy..", "..ppppy..........ypppp..", ".ppppppp........ppppppp.", ".pqqqqqqqq....qqqqqqqqp.", ".qqqqqqqqqqqqqqqqqqqqqq.", "qqqrrqqqqqqqqqqqqqqrrqqq", "qqqrrqqqqqqqqqqqqqqrrqqq", ".qqqqqqqqqqqqqqqqqqqqqq.", "..qqwqwqqqqqqqqqqwqwqq..", "...qqqqqqqqqqqqqqqqqq...", "..pqqqqqqqqqqqqqqqqqqp..", ".ppqqqqqqqqqqqqqqqqqqpp.", "ppqqqqqqqqqqqqqqqqqqqqpp", ".qqqqqqqqqqqqqqqqqqqqqq.", "..qqqq..qqqqqqqq..qqqq..", "..qqq....qqqqqq....qqq..", "..kkk....kkkkkk....kkk.."});
        MONSTERS.put("galleon", new String[]{"........................", "....kkkkkkkkkkkkkkkk....", "...kkkkkkkkkkkkkkkkkk...", "..kkkkkkwkkkkkkwkkkkkk..", ".kkkkkkkkkkkkkkkkkkkkkk.", "....ssssssssssssssss....", "....skksssskksssskks....", "....ssssssssssssssss....", "....rrssssssssssssrr....", "...rrrrrrrrrrrrrrrrrr...", "....rrrrrrrrrrrrrrrr....", "..RRRRNNNNNNNNNNNNRRRR..", ".RRRRNNyNNNNNNNNyNNRRRR.", "sRRRRNNNNNNNNNNNNNNRRRRs", "s.RRRNNNNNNNNNNNNNNRRR.s", "...RRNNNNNNNNNNNNNNRR...", "....BBBBBBBBBBBBBBBB....", "....BBBB..BBBB..BBBB....", "....kkkk..kkkk..kkkk....", "....kkkk..kkkk..kkkk...."});
        MONSTERS.put("serpent", new String[]{"........................", "........cccccccc........", ".......cccccccccc.......", "......cccrccccrccc......", "......cccccccccccc......", ".......ccwccccwcc.......", "........cccccccc........", ".........cccccc.........", "....vvvv.cccccc.vvvv....", "...vvvvvvccccccvvvvvv...", "..vvccvvvvccccvvvvccvv..", ".vvccccvvvvvvvvvvccccvv.", "vvcc..ccvvvvvvvvcc..ccvv", "vcc....ccvvvvvvcc....ccv", "vcc....ccvvvvvvcc....ccv", "vvcc..ccvvvvvvvvcc..ccvv", ".vvccccvvvvvvvvvvccccvv.", "..vvvvvvvvvvvvvvvvvvvv..", "....vvvvvvvvvvvvvvvv....", "........................"});
    }

    static final java.util.Map<String, String[]> TILES = new java.util.HashMap<>();
    static {
        TILES.put(".", new String[]{"gggggggg", "gGgggggg", "ggggggGg", "gggGgggg", "gggggggg", "ggggGggg", "gGgggggg", "gggggggG"});
        TILES.put("f", new String[]{"gggggggg", "gPgggygg", "ggggggGg", "gggygggg", "ggPggggg", "gggggPgg", "gyggggGg", "gggggggg"});
        TILES.put("=", new String[]{"tttttttt", "ttTttttt", "tttttTtt", "tttttttt", "tTtttttt", "ttttttTt", "tttTtttt", "tttttttt"});
        TILES.put("B", new String[]{"vvvvvvvv", "nnnnnnnn", "NnnnNnnn", "nnnnnnnn", "NNNNNNNN", "nnnnnnnn", "nnnNnnnN", "vvvvvvvv"});
        TILES.put("~", new String[]{"vvvvvvvv", "vvuuvvvv", "vvvvvvvv", "vvvvvuuv", "vvvvvvvv", "vuuvvvvv", "vvvvvvvv", "vvvvvvuv"});
        TILES.put("~1", new String[]{"vvvvvvvv", "vvvuuvvv", "vvvvvvvv", "vvvvvvuu", "vvvvvvvv", "uuvvvvvv", "vvvvvvvv", "vvvvvvvu"});
        TILES.put("^", new String[]{"gggwwggg", "ggwwewgg", "ggeeeegg", "geeEeeeg", "geEeeEeg", "eeeeEeee", "eEeeeeEe", "eeeEeeee"});
        TILES.put("T", new String[]{"ggGGGGgg", "gGGgGGGg", "GGgGGgGG", "GGGGGGGG", "gGGGgGGg", "ggGGGGgg", "gggnnggg", "gggnnggg"});
        TILES.put("V", new String[]{"gggggggg", "grrggrrg", "rrrrrrrr", "rwwrrwwr", "twntwntt", "tnntnntt", "gggggggg", "gGgggggg"});
        TILES.put("K", new String[]{"gegegegg", "geeeeegg", "geeeeegg", "eekeekee", "eeeeeeee", "eeekkeee", "eeekkeee", "gggggggg"});
        TILES.put("O", new String[]{"eeeEeeee", "eEeeeeEe", "eekkkkee", "ekkkkkke", "ekkkkkke", "ekkkkkke", "gggggggg", "gGgggggg"});
        TILES.put(",", new String[]{"ffffffff", "fFffffff", "ffffffFf", "ffffffff", "ffffffff", "ffFfffff", "ffffffff", "fffffFff"});
        TILES.put("#", new String[]{"EEEEEEEE", "eeeEeeee", "eeeEeeee", "EEEEEEEE", "eEeeeeEe", "eEeeeeEe", "EEEEEEEE", "eeeEeeee"});
        TILES.put("h", new String[]{"rRrRrRrR", "RrRrRrRr", "rrrrrrrr", "RRRRRRRR", "rRrRrRrR", "RrRrRrRr", "rrrrrrrr", "RRRRRRRR"});
        TILES.put("d", new String[]{"EnnnnnnE", "EnNnnNnE", "EnNnnNnE", "EnnnnnnE", "EnNnnyyE", "EnNnnNnE", "EnNnnNnE", "EnnnnnnE"});
        TILES.put("r", new String[]{"rrrrrrrr", "rRrrrrRr", "rrrrrrrr", "rrrRRrrr", "rrrRRrrr", "rrrrrrrr", "rRrrrrRr", "rrrrrrrr"});
        TILES.put("|", new String[]{"nnnnnnnn", "NNNNNNNN", "nnnnnnnn", "nNnnnnNn", "nNnnnnNn", "nNnnnnNn", "NNNNNNNN", "ffffffff"});
        TILES.put("W", new String[]{"fpppppff", "pcccccpf", "pcwwccpf", "pcccccpf", "ppppppff", "pqyqqpff", "pqqqqpff", "fkkkkfff"});
        TILES.put("X", new String[]{"FFFFFFFF", "FfFFFFfF", "FFFFFFFF", "FFFfFFFF", "FFFFFFFF", "FfFFFFfF", "FFFFFFFF", "FFFFFFFF"});
        TILES.put("S", new String[]{"mmmmmmmm", "mkkkkkkm", "mkMMMMkm", "mkMkkMkm", "mkMkkMkm", "mkMMMMkm", "mkkkkkkm", "mmmmmmmm"});
        TILES.put("U", new String[]{"mmmmmmmm", "mllllllm", "mlEEEElm", "mlEllElm", "mlEllElm", "mlEEEElm", "mllllllm", "mmmmmmmm"});
        TILES.put("C", new String[]{"ffffffff", "fNNNNNNf", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "ffffffff"});
        TILES.put("c", new String[]{"ffffffff", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "ffffffff"});
        TILES.put(";", new String[]{"gggggggg", "ggggGggg", "gggggggg", "gGgggggg", "gggggggg", "gggggGgg", "gggggggg", "ggGggggg"});
        TILES.put(":", new String[]{"TTTTTTTT", "tttTtttt", "tttTtttt", "TTTTTTTT", "ttttttTt", "ttttttTt", "TTTTTTTT", "tTtttttt"});
        TILES.put("H", new String[]{"NNNNNNNN", "nnnnnnnn", "nNnnnnNn", "NNNNNNNN", "nnnnnnnn", "nnnNnnnn", "NNNNNNNN", "nnnnnnnn"});
        TILES.put("+", new String[]{"NNNNNNNN", "NttttttN", "NttttttN", "NttTtttN", "NttttttN", "NttttttN", "NtttttTN", "NttttttN"});
        TILES.put("M", new String[]{"EEEEEEEE", "EyyyyyyE", "EybcpgyE", "EyrbyuyE", "EyggbcyE", "EyyyyyyE", "EEEEEEEE", "eeeEeeee"});
        TILES.put("k", new String[]{"NNNNNNNN", "NrbgyrbN", "NrbgyrbN", "NNNNNNNN", "NygrbygN", "NygrbygN", "NNNNNNNN", "NnnnnnnN"});
        TILES.put("g", new String[]{"nnnnnnnn", "gGngGngG", "GynGynGy", "nnnnnnnn", "gGngGngG", "GynGynGy", "nnnnnnnn", "nnnnnnnn"});
        TILES.put("b", new String[]{"........", ".wwwwww.", ".wwwwww.", ".rrrrrr.", ".rrrrrr.", ".rRrrRr.", ".rrrrrr.", ".NNNNNN."});
        TILES.put("t", new String[]{"........", ".nnnnnn.", "nnnnnnnn", "nNnnnnNn", ".N....N.", ".N....N.", "........", "........"});
        TILES.put("o", new String[]{"........", "..nnnn..", ".nNNNNn.", ".nnnnnn.", ".nNNNNn.", ".nnnnnn.", "..nnnn..", "........"});
        TILES.put("x", new String[]{"........", ".NNNNNN.", ".NnnnNN.", ".NnNnnN.", ".NnnNnN.", ".NNnnnN.", ".NNNNNN.", "........"});
        TILES.put("F", new String[]{"........", ".n...n..", "nnnnnnnn", ".n...n..", ".n...n..", "nnnnnnnn", ".n...n..", "........"});
        TILES.put("Q", new String[]{"........", ".eeeeee.", "eEEEEEEe", "eEvvvvEe", "eEvuvvEe", "eEEEEEEe", ".eeeeee.", "........"});
        TILES.put("Y", new String[]{"........", ".nnnnnn.", ".nttttn.", ".nTTTTn.", ".nnnnnn.", "...nn...", "...nn...", "........"});
        TILES.put("P", new String[]{"........", "nnnnnnnn", "NNNNNNNN", "nnnnnnnn", "n......n", "n......n", "N......N", "........"});
        TILES.put("A", new String[]{"........", ".y.yy.y.", "wwwwwwww", "llllllll", "lYlllYll", "llllllll", "llllllll", "........"});
        TILES.put("N", new String[]{"........", ".eeeeee.", "eucuucue", "eccwwcce", "euccccue", "eccuucce", ".eeeeee.", "........"});
        TILES.put("L", new String[]{"...yy...", "..yYYy..", "...yy...", "...EE...", "....E...", "....E...", "...EEE..", "........"});
        TILES.put("Z", new String[]{".yyyyyy.", ".yRRRRy.", ".yRyyRy.", ".yRRRRy.", "yyyyyyyy", "yRRRRRRy", "yyyyyyyy", ".Y....Y."});
        TILES.put("j", new String[]{"........", "..GGGG..", ".GgGGgG.", "GGgGGGGG", "GgGGgGgG", ".GGGGGG.", "..G..G..", "........"});
        TILES.put("e", new String[]{"........", "..eeee..", ".eyyyye.", ".eykkye.", ".eyyyye.", "..eyye..", "...ee...", "........"});
        TILES.put("i", new String[]{"dddddddd", "dDdyyddd", "dddyYddd", "ddddnddd", "dDddnddd", "ddddnDdd", "dddddddd", "dDdddddd"});
        TILES.put("_", new String[]{"nnnnnnnn", "NNNNNNNN", "tnntnntn", "nnnnnnnn", "NNNNNNNN", "nnntnnnt", "nnnnnnnn", "NNNNNNNN"});
        TILES.put("v", new String[]{"vvvvvvvv", "vvvnnvvv", "vvnwwnvv", "vnnnnnnv", "nNNNNNNn", "vNnnnnNv", "vvNNNNvv", "vvvvvvvv"});
        TILES.put("I", new String[]{"gggrrggg", "ggryyrgg", "gggwwggg", "gggrrggg", "gggwwggg", "gggrrggg", "ggwwwwgg", "gggggggg"});
        TILES.put("G", new String[]{"vvvvvvvv", "vveeeevv", "veuvvuev", "vevvvvev", "vevvvvev", "veuvvuev", "vveeeevv", "vvvvvvvv"});
        TILES.put("G1", new String[]{"vveeeevv", "veeeeeev", "eeMMMMee", "eeMkkMee", "eeMkkMee", "eeMMMMee", "veeeeeev", "vveeeevv"});
        TILES.put("sea,", new String[]{"eeeeeeee", "eueeeeee", "eeeeeeue", "eeeeeeee", "eeeueeee", "eeeeeeeu", "eeeeeeee", "eueeeeee"});
        TILES.put("sea#", new String[]{"BBBBBBBB", "BvBBBvBB", "BBBBBBvB", "vBBBvBBB", "BBBBBBBB", "BvBBBBvB", "BBBvBBBB", "BBBBBBBB"});
        TILES.put("seaX", new String[]{"llllllll", "leeeeeel", "leeeeeel", "leeeeeel", "leeeeeel", "leeeeeel", "leeeeeel", "leeeeeel"});
        TILES.put("seaC", new String[]{"eeeeeeee", "eNNNNNNe", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "eeeeeeee"});
        TILES.put("seac", new String[]{"eeeeeeee", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "eeeeeeee"});
        TILES.put("seai", new String[]{"BBBBBBBB", "BvBPPBBB", "BBPxPBBB", "BBBxBBBB", "BvBxBBvB", "BBBxBBBB", "BBBBBBBB", "BvBBBBBB"});
        TILES.put("tmp,", new String[]{"llllllll", "lclllllc", "llllllll", "lllcllll", "llllllll", "lclllllc", "llllllll", "llllclll"});
        TILES.put("tmp#", new String[]{"cccccccc", "vvvvvvvv", "cvvccvvc", "cvvccvvc", "cccccccc", "vccvvccv", "vccvvccv", "cccccccc"});
        TILES.put("tmpX", new String[]{"cccccccc", "cllllllc", "cllllllc", "cllllllc", "cllllllc", "cllllllc", "cllllllc", "cllllllc"});
        TILES.put("tmpC", new String[]{"llllllll", "lNNNNNNl", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "llllllll"});
        TILES.put("tmpc", new String[]{"llllllll", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "llllllll"});
        TILES.put("tmpi", new String[]{"llllllll", "lllyylll", "llyYYyll", "lllyylll", "lllcclll", "lllcclll", "llccccll", "llllllll"});
        TILES.put("cave,", new String[]{"mmmmmmmm", "mMmmmmmm", "mmmmmmMm", "mmmmmmmm", "mmmmmmmm", "mmMmmmmm", "mmmmmmmm", "mmmmmMmm"});
        TILES.put("cave#", new String[]{"dddddddd", "dDddDddd", "ddddddDd", "DddddDdd", "dddDdddd", "dDddddDd", "ddddDddd", "dDdddddd"});
        TILES.put("caveX", new String[]{"llllllll", "lmmmmmml", "lmmmmmml", "lmmmmmml", "lmmmmmml", "lmmmmmml", "lmmmmmml", "lmmmmmml"});
        TILES.put("caveC", new String[]{"mmmmmmmm", "mNNNNNNm", "NnnnnnnN", "NNNNNNNN", "NnnyynnN", "NnnnnnnN", "NNNNNNNN", "mmmmmmmm"});
        TILES.put("cavec", new String[]{"mmmmmmmm", "NnnnnnnN", "NkkkkkkN", "NNNNNNNN", "NnnnnnnN", "NnnnnnnN", "NNNNNNNN", "mmmmmmmm"});
    }

    public static String[] character(String key) {
        String[] s = CHARS.get(key);
        return s != null ? s : CHARS.get("villager");
    }

    public static String[] monster(String key) {
        return MONSTERS.get(key);
    }

    public static String[] tile(String key) {
        return TILES.get(key);
    }

    public static int color(char c) {
        Integer v = COLORS.get(c);
        return v == null ? 0 : v;
    }

    private RpgArt() {
    }
}
