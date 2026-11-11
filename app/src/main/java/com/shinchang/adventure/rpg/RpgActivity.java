package com.shinchang.adventure.rpg;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shinchang.adventure.game.GameState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** 환상의 세계 아르카디아 RPG 화면. */
public class RpgActivity extends Activity {

    private static final String PREFS = "shinchang";
    private static final String KEY_RPG = "rpg";
    private static final String KEY_MAIN = "save";
    private static final long MOVE_REPEAT_MS = 170;
    private static final int DIALOG_THEME = android.R.style.Theme_Material_Dialog_Alert;

    private RpgState state;
    private RpgView view;
    private TextView statusBox, messageBox, mapLabel;
    private View padPanel, battlePanel;
    private final Handler handler = new Handler(Looper.getMainLooper());

    // 대사창
    private final ArrayDeque<String> messages = new ArrayDeque<>();
    private Runnable messagesDone;
    private String typingText = "";
    private int typed;
    private boolean messageOpen;

    // 이동
    private int heldDir = -1;
    private boolean dialogOpen;

    // 전투
    private Battle battle;
    private final List<Battle.Action> actions = new ArrayList<>();
    private int commandHero;

    private final Runnable typer = new Runnable() {
        @Override
        public void run() {
            if (typed < typingText.length()) {
                typed = Math.min(typingText.length(), typed + 2);
                messageBox.setText(typingText.substring(0, typed) + (typed >= typingText.length() ? "  ▼" : ""));
                handler.postDelayed(this, 18);
            }
        }
    };

    private final Runnable repeatMove = new Runnable() {
        @Override
        public void run() {
            if (heldDir < 0) return;
            doMove(heldDir);
            handler.postDelayed(this, MOVE_REPEAT_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        state = RpgState.deserialize(prefs().getString(KEY_RPG, null));
        applyPerks();
        buildUi();
        view.setState(state);
        refreshStatus();

        if (state.step == RpgData.STEP_PROLOGUE && state.stepsWalked == 0) {
            say(introLines(), null);
        } else {
            showMapLabel(RpgData.MAP_NAMES[state.map]);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        heldDir = -1;
        save();
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private void save() {
        prefs().edit().putString(KEY_RPG, state.serialize()).apply();
    }

    /** 세속에서의 경력이 특기가 된다 */
    private void applyPerks() {
        GameState main = GameState.deserialize(prefs().getString(KEY_MAIN, null));
        state.perkDelivery = true;
        state.perkBroker = main.highestJob >= 1;
        state.perkFire = main.highestJob >= 2;
        state.perkFaith = main.religionUnlocked;
    }

    private List<String> introLines() {
        List<String> l = new ArrayList<>();
        l.add("— " + RpgData.CHAPTER_TITLES[0] + " —");
        l.add("도보 배달로 시작해 재벌, 종교계를 거쳐 마침내 대통령이 된 신창.");
        l.add("그의 곁에는 언제나 낡은 배달 가방이 있었다. 첫 배달 날, 이름 모를 할머니에게 받은 가방이다.");
        l.add("어느 밤, 청와대 금고 가장 안쪽 방에서 정체를 알 수 없는 기계가 발견되었다…");
        StringBuilder perks = new StringBuilder("앞선 인생의 경험이 힘이 된다:\n· 배달 경험 – 길을 잘 알아 몬스터를 덜 만난다");
        if (state.perkBroker) perks.append("\n· 협상의 달인 – 상점 10% 할인");
        if (state.perkFire) perks.append("\n· 소방 기술 – 불꽃 주문 +20%");
        if (state.perkFaith) perks.append("\n· 깊은 신앙 – 회복 주문 +30%");
        l.add(perks.toString());
        l.add("(아래 십자 버튼으로 걷고, 「조사」 버튼으로 말을 걸거나 살펴보세요. 「메뉴」에서 도구와 장비를 볼 수 있어요.)");
        return l;
    }

    // ---- 화면 구성 ----

    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }

    private TextView windowText(int sp) {
        TextView t = new TextView(this);
        t.setBackgroundResource(com.shinchang.adventure.R.drawable.bg_dq_window);
        t.setTextColor(0xFFFFFFFF);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        t.setPadding(dp(12), dp(8), dp(12), dp(8));
        t.setLineSpacing(dp(2), 1f);
        return t;
    }

    private Button dqButton(String label, int sp) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextColor(0xFFFFFFFF);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackgroundResource(com.shinchang.adventure.R.drawable.bg_dq_button);
        b.setPadding(0, 0, 0, 0);
        return b;
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF000000);

        FrameLayout game = new FrameLayout(this);
        view = new RpgView(this);
        game.addView(view, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        statusBox = windowText(12);
        FrameLayout.LayoutParams slp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.START);
        slp.setMargins(dp(8), dp(8), dp(8), dp(8));
        game.addView(statusBox, slp);

        mapLabel = windowText(15);
        mapLabel.setTypeface(Typeface.DEFAULT_BOLD);
        FrameLayout.LayoutParams mlp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP | Gravity.END);
        mlp.setMargins(dp(8), dp(8), dp(8), dp(8));
        mapLabel.setVisibility(View.GONE);
        game.addView(mapLabel, mlp);

        messageBox = windowText(15);
        messageBox.setMinHeight(dp(96));
        FrameLayout.LayoutParams msg = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM);
        msg.setMargins(dp(8), dp(8), dp(8), dp(8));
        messageBox.setVisibility(View.GONE);
        messageBox.setOnClickListener(v -> advanceMessage());
        game.addView(messageBox, msg);
        root.addView(game, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1));

        FrameLayout controls = new FrameLayout(this);
        controls.setPadding(dp(10), dp(6), dp(10), dp(10));
        padPanel = buildPad();
        battlePanel = buildBattlePanel();
        battlePanel.setVisibility(View.GONE);
        controls.addView(padPanel);
        controls.addView(battlePanel);
        root.addView(controls, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(200)));
        setContentView(root);
        com.shinchang.adventure.SystemBars.avoid(this);
    }

    private View buildPad() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        GridLayout pad = new GridLayout(this);
        pad.setColumnCount(3);
        pad.setRowCount(3);
        String[] arrows = {"▲", "▶", "▼", "◀"};
        int[][] cells = {{0, 1}, {1, 2}, {2, 1}, {1, 0}};
        int size = dp(58);
        for (int d = 0; d < 4; d++) {
            Button b = dqButton(arrows[d], 20);
            final int dir = d;
            b.setOnTouchListener((v, e) -> {
                int a = e.getActionMasked();
                if (a == MotionEvent.ACTION_DOWN) {
                    v.setPressed(true);
                    heldDir = dir;
                    handler.removeCallbacks(repeatMove);
                    repeatMove.run();
                } else if (a == MotionEvent.ACTION_UP || a == MotionEvent.ACTION_CANCEL) {
                    v.setPressed(false);
                    if (heldDir == dir) heldDir = -1;
                }
                return true;
            });
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(cells[d][0]), GridLayout.spec(cells[d][1]));
            lp.width = size;
            lp.height = size;
            lp.setMargins(dp(2), dp(2), dp(2), dp(2));
            pad.addView(b, lp);
        }
        row.addView(pad);

        View spacer = new View(this);
        row.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1));

        LinearLayout actionsCol = new LinearLayout(this);
        actionsCol.setOrientation(LinearLayout.VERTICAL);
        Button a = dqButton("조사 / 확인", 16);
        a.setOnClickListener(v -> onConfirm());
        Button menu = dqButton("메뉴", 16);
        menu.setOnClickListener(v -> openMenu());
        actionsCol.addView(a, new LinearLayout.LayoutParams(dp(130), dp(84)));
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(dp(130), dp(64));
        mlp.topMargin = dp(10);
        actionsCol.addView(menu, mlp);
        row.addView(actionsCol);
        return row;
    }

    private View buildBattlePanel() {
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        String[] labels = {"⚔️ 싸운다", "✨ 주문", "🎒 도구", "🛡️ 방어", "🏃 도망", "확인 ▶"};
        for (int i = 0; i < labels.length; i++) {
            Button b = dqButton(labels[i], 16);
            final int cmd = i;
            b.setOnClickListener(v -> onBattleCommand(cmd));
            GridLayout.LayoutParams lp = new GridLayout.LayoutParams(GridLayout.spec(i / 3, 1f), GridLayout.spec(i % 3, 1f));
            lp.width = 0;
            lp.height = dp(84);
            lp.setMargins(dp(4), dp(4), dp(4), dp(4));
            grid.addView(b, lp);
        }
        return grid;
    }

    private void refreshStatus() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < state.party.size(); i++) {
            Hero h = state.party.get(i);
            if (i > 0) sb.append('\n');
            sb.append(String.format(Locale.KOREA, "%s  Lv %d\nHP %d/%d  MP %d/%d", h.name(), h.level, h.hp, h.maxHp(), h.mp, h.maxMp()));
            if (!h.alive()) sb.append(" 💀");
        }
        sb.append(String.format(Locale.KOREA, "\nG %,d", state.gold));
        statusBox.setText(sb.toString());
    }

    private void showMapLabel(String name) {
        mapLabel.setText(name);
        mapLabel.setVisibility(View.VISIBLE);
        handler.removeCallbacks(hideLabel);
        handler.postDelayed(hideLabel, 1800);
    }

    private final Runnable hideLabel = () -> mapLabel.setVisibility(View.GONE);

    // ---- 대사창 ----

    private void say(List<String> lines, Runnable done) {
        messages.clear();
        messages.addAll(lines);
        messagesDone = done;
        messageOpen = true;
        messageBox.setVisibility(View.VISIBLE);
        nextMessage();
    }

    private void say(String line, Runnable done) {
        say(Arrays.asList(line), done);
    }

    private void nextMessage() {
        if (messages.isEmpty()) {
            messageOpen = false;
            messageBox.setVisibility(battle != null ? View.VISIBLE : View.GONE);
            Runnable d = messagesDone;
            messagesDone = null;
            refreshStatus();
            if (d != null) d.run();
            return;
        }
        typingText = messages.poll();
        typed = 0;
        handler.removeCallbacks(typer);
        typer.run();
        refreshStatus();
        view.invalidate();
    }

    private void advanceMessage() {
        if (!messageOpen) return;
        if (typed < typingText.length()) {
            typed = typingText.length();
            messageBox.setText(typingText + "  ▼");
            return;
        }
        nextMessage();
    }

    /** 대사창에 고정 문구 (전투 명령 고르는 중) */
    private void prompt(String text) {
        messageBox.setVisibility(View.VISIBLE);
        messageBox.setText(text);
    }

    private boolean busy() {
        return messageOpen || dialogOpen || battle != null;
    }

    // ---- 필드 ----

    private void doMove(int d) {
        if (busy()) return;
        int ox = state.x, oy = state.y, oldMap = state.map;
        RpgState.MoveResult r = state.move(d);
        if (!r.moved) {
            view.invalidate();
            return;
        }
        if (r.warped || state.map != oldMap) {
            view.setState(state);
            showMapLabel(RpgData.MAP_NAMES[state.map]);
            save();
        } else {
            view.stepped(ox, oy);
        }
        if (r.encounter != null) {
            heldDir = -1;
            startBattle(r.encounter, false);
        }
    }

    private void onConfirm() {
        if (messageOpen) {
            advanceMessage();
            return;
        }
        if (busy()) return;
        int oldMap = state.map;
        int oldChapter = RpgData.chapterOf(state.step);
        RpgState.Interaction in = state.interact();
        Runnable after = () -> afterInteraction(in, oldMap, oldChapter);
        if (in.lines.isEmpty()) after.run();
        else say(in.lines, after);
    }

    private void afterInteraction(RpgState.Interaction in, int oldMap, int oldChapter) {
        if (state.map != oldMap) {
            view.setState(state);
            showMapLabel(RpgData.MAP_NAMES[state.map]);
            int chapter = RpgData.chapterOf(state.step);
            if (chapter != oldChapter) say("— " + RpgData.CHAPTER_TITLES[chapter] + " —", null);
        }
        if (in.bossBattle) {
            startBattle(new int[]{in.bossMonster}, true);
            return;
        }
        if (in.chapterClear) {
            save();
            Toast.makeText(this, RpgData.chapterOf(state.step) == 1 ? "1장 클리어! 진행이 기록되었습니다."
                    : "2장 클리어! 진행이 기록되었습니다.", Toast.LENGTH_LONG).show();
        }
        switch (in.action) {
            case RpgData.ACT_SHOP:
                openShop(in.arg);
                break;
            case RpgData.ACT_INN:
                openInn(in.arg);
                break;
            case RpgData.ACT_SAVE:
                save();
                say("[사제] 신의 이름으로 모험을 기록했습니다. 부디 무사하시길.", null);
                break;
            default:
                break;
        }
        save();
        refreshStatus();
    }

    // ---- 전투 ----

    private void startBattle(int[] ids, boolean boss) {
        battle = new Battle(state, ids, boss, state.random());
        view.setBattle(battle);
        padPanel.setVisibility(View.GONE);
        battlePanel.setVisibility(View.VISIBLE);
        setCommandsEnabled(false);
        if (boss) {
            view.bossIntro();
            say(Arrays.asList("⚔️ 결전 ⚔️", battle.intro()), this::beginCommands);
        } else {
            say(battle.intro(), this::beginCommands);
        }
    }

    private void setCommandsEnabled(boolean on) {
        GridLayout g = (GridLayout) battlePanel;
        for (int i = 0; i < g.getChildCount(); i++) g.getChildAt(i).setEnabled(on || i == 5);
    }

    private void beginCommands() {
        actions.clear();
        commandHero = 0;
        nextCommand();
    }

    private void nextCommand() {
        while (commandHero < state.party.size() && !state.party.get(commandHero).alive()) {
            actions.add(new Battle.Action(Battle.DEFEND, -1, 0));
            commandHero++;
        }
        if (commandHero >= state.party.size()) {
            playRound();
            return;
        }
        setCommandsEnabled(true);
        Hero h = state.party.get(commandHero);
        prompt(Josa.eun(h.name()) + " 어떻게 할까?");
    }

    private void addAction(Battle.Action a) {
        actions.add(a);
        commandHero++;
        nextCommand();
    }

    private void onBattleCommand(int cmd) {
        if (battle == null) return;
        if (cmd == 5) {
            onConfirm();
            return;
        }
        if (messageOpen) return;
        Hero h = state.party.get(commandHero);
        switch (cmd) {
            case 0:
                chooseEnemy(t -> addAction(new Battle.Action(Battle.ATTACK, t, 0)));
                break;
            case 1:
                chooseSpell(h);
                break;
            case 2:
                chooseBattleItem();
                break;
            case 3:
                addAction(new Battle.Action(Battle.DEFEND, -1, 0));
                break;
            case 4:
                actions.clear();
                actions.add(new Battle.Action(Battle.FLEE, -1, 0));
                playRound();
                break;
            default:
                break;
        }
    }

    private interface Pick {
        void picked(int index);
    }

    private void chooseFrom(String title, String[] options, Pick pick) {
        dialogOpen = true;
        new AlertDialog.Builder(this, DIALOG_THEME)
                .setTitle(title)
                .setItems(options, (d, which) -> {
                    dialogOpen = false;
                    pick.picked(which);
                })
                .setOnCancelListener(d -> dialogOpen = false)
                .show();
    }

    private void chooseEnemy(Pick pick) {
        List<Battle.Enemy> alive = battle.aliveEnemies();
        if (alive.size() == 1) {
            pick.picked(battle.enemies.indexOf(alive.get(0)));
            return;
        }
        String[] names = new String[alive.size()];
        for (int i = 0; i < names.length; i++) names[i] = alive.get(i).name;
        chooseFrom("누구를?", names, i -> pick.picked(battle.enemies.indexOf(alive.get(i))));
    }

    private void chooseAlly(Pick pick) {
        String[] names = new String[state.party.size()];
        for (int i = 0; i < names.length; i++) {
            Hero h = state.party.get(i);
            names[i] = h.name() + "  HP " + h.hp + "/" + h.maxHp() + (h.alive() ? "" : " (쓰러짐)");
        }
        if (names.length == 1) {
            pick.picked(0);
            return;
        }
        chooseFrom("누구에게?", names, pick);
    }

    private void chooseSpell(Hero h) {
        List<Integer> spells = h.spells();
        if (spells.isEmpty()) {
            Toast.makeText(this, Josa.eun(h.name()) + " 아직 주문을 모른다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[spells.size()];
        for (int i = 0; i < names.length; i++) {
            RpgData.Spell s = RpgData.SPELLS[spells.get(i)];
            names[i] = s.name + "  (MP " + s.mp + ")";
        }
        chooseFrom(h.name() + "의 주문 · MP " + h.mp, names, i -> {
            int idx = spells.get(i);
            RpgData.Spell s = RpgData.SPELLS[idx];
            if (s.targetsEnemy()) chooseEnemy(t -> addAction(new Battle.Action(Battle.SPELL, t, idx)));
            else if (s.targetsAlly()) chooseAlly(t -> addAction(new Battle.Action(Battle.SPELL, t, idx)));
            else addAction(new Battle.Action(Battle.SPELL, -1, idx));
        });
    }

    private void chooseBattleItem() {
        List<Integer> usable = new ArrayList<>();
        for (int id : new int[]{RpgData.HERB, RpgData.HI_HERB, RpgData.MANA_CANDY, RpgData.PHOENIX}) if (state.hasItem(id)) usable.add(id);
        if (usable.isEmpty()) {
            Toast.makeText(this, "전투에서 쓸 도구가 없다.", Toast.LENGTH_SHORT).show();
            return;
        }
        String[] names = new String[usable.size()];
        for (int i = 0; i < names.length; i++) names[i] = RpgData.ITEMS[usable.get(i)].name + " × " + state.items[usable.get(i)];
        chooseFrom("도구", names, i -> chooseAlly(t -> addAction(new Battle.Action(Battle.ITEM, t, usable.get(i)))));
    }

    private void playRound() {
        setCommandsEnabled(false);
        List<String> log = battle.playRound(new ArrayList<>(actions));
        if (battle.enragedThisRound) view.setEnraged(true);
        // 적이 맞으면 깜빡이게: 첫 피해 대상
        for (int i = 0; i < battle.enemies.size(); i++) {
            if (!battle.enemies.get(i).alive() || battle.enemies.get(i).hp < battle.enemies.get(i).m.hp) {
                view.flash(i);
                break;
            }
        }
        say(log, this::afterRound);
    }

    private void afterRound() {
        if (battle.won()) {
            boolean boss = battle.boss;
            int bossId = battle.firstMonster;
            state.battlesWon++;
            endBattle();
            if (boss) {
                view.victoryFlash();
                say(state.onBossDefeated(bossId), this::save);
            }
            return;
        }
        if (battle.lost()) {
            endBattle();
            state.defeated();
            view.setState(state);
            say(Arrays.asList("……", "정신을 차려 보니 " + RpgData.MAP_NAMES[state.map] + "이었다.",
                    "가지고 있던 골드가 절반이 되었다."), this::save);
            return;
        }
        if (battle.fled()) {
            endBattle();
            return;
        }
        beginCommands();
    }

    private void endBattle() {
        battle = null;
        view.setBattle(null);
        battlePanel.setVisibility(View.GONE);
        padPanel.setVisibility(View.VISIBLE);
        messageBox.setVisibility(messageOpen ? View.VISIBLE : View.GONE);
        refreshStatus();
        save();
    }

    // ---- 메뉴 ----

    private void openMenu() {
        if (busy()) return;
        String[] items = {"📜 상태", "🎒 도구", "✨ 주문", "🗡️ 장비", "📖 단서", "💾 기록하기", "🏛️ 세속으로 돌아가기"};
        chooseFrom(state.chapterTitle(), items, i -> {
            if (i >= 4) i = i == 4 ? 99 : i - 1;
            switch (i) {
                case 99:
                    showClues();
                    return;
                case 0:
                    showStatus();
                    break;
                case 1:
                    openBag();
                    break;
                case 2:
                    openFieldSpells();
                    break;
                case 3:
                    openEquip();
                    break;
                case 4:
                    save();
                    Toast.makeText(this, "기록했습니다.", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    save();
                    finish();
                    break;
            }
        });
    }

    private void showClues() {
        StringBuilder sb = new StringBuilder();
        int found = 0;
        for (int i = 0; i < RpgData.CLUE_NAMES.length; i++) {
            if (state.hasFlag(1 << i)) {
                found++;
                sb.append("◆ ").append(RpgData.CLUE_NAMES[i]).append("\n").append(RpgData.CLUE_TEXTS[i]).append("\n\n");
            } else {
                sb.append("◇ ???\n아직 찾지 못한 단서. 마을과 동굴의 책장·벽화·물건을 「조사」해 보자.\n\n");
            }
        }
        sb.append("찾은 단서 ").append(found).append(" / ").append(RpgData.CLUE_NAMES.length);
        dialogOpen = true;
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("📖 단서").setMessage(sb.toString())
                .setPositiveButton("닫기", (d, w) -> dialogOpen = false)
                .setOnCancelListener(d -> dialogOpen = false).show();
    }

    private void showStatus() {
        StringBuilder sb = new StringBuilder();
        for (Hero h : state.party) {
            sb.append(String.format(Locale.KOREA, "【%s】 Lv %d\nHP %d/%d  MP %d/%d\n공격 %d  방어 %d  민첩 %d\n무기: %s\n갑옷: %s\n다음 레벨까지 %d\n\n",
                    h.name(), h.level, h.hp, h.maxHp(), h.mp, h.maxMp(), h.atk(), h.def(), h.agi(),
                    RpgData.ITEMS[h.weapon].name, RpgData.ITEMS[h.armor].name, h.expToNext()));
        }
        sb.append(String.format(Locale.KOREA, "골드 %,d · 승리한 전투 %d번 · 걸음 %,d", state.gold, state.battlesWon, state.stepsWalked));
        dialogOpen = true;
        new AlertDialog.Builder(this, DIALOG_THEME).setTitle("📜 상태").setMessage(sb.toString())
                .setPositiveButton("닫기", (d, w) -> dialogOpen = false)
                .setOnCancelListener(d -> dialogOpen = false).show();
    }

    private void openBag() {
        List<Integer> bag = state.bagItems();
        if (bag.isEmpty()) {
            say("가방이 비어 있다.", null);
            return;
        }
        String[] names = new String[bag.size()];
        for (int i = 0; i < names.length; i++) {
            RpgData.Item it = RpgData.ITEMS[bag.get(i)];
            names[i] = it.name + (it.type == RpgData.KEY ? " (중요)" : " × " + state.items[it.id]) + " – " + it.desc;
        }
        chooseFrom("🎒 도구", names, i -> {
            int id = bag.get(i);
            RpgData.Item it = RpgData.ITEMS[id];
            if (it.type == RpgData.CONSUMABLE && id != RpgData.WING) {
                chooseAlly(t -> {
                    say(state.useItemInField(id, state.party.get(t)), null);
                    save();
                });
            } else {
                int oldMap = state.map;
                String result = state.useItemInField(id, state.party.get(0));
                if (state.map != oldMap) view.setState(state);
                say(result, null);
                save();
            }
        });
    }

    private void openFieldSpells() {
        List<String> names = new ArrayList<>();
        List<int[]> refs = new ArrayList<>();
        for (int hi = 0; hi < state.party.size(); hi++) {
            Hero h = state.party.get(hi);
            for (int sp : h.spells()) {
                RpgData.Spell s = RpgData.SPELLS[sp];
                names.add(h.name() + " · " + s.name + " (MP " + s.mp + ")" + (s.fieldUsable() ? "" : " – 전투 전용"));
                refs.add(new int[]{hi, sp});
            }
        }
        if (names.isEmpty()) {
            say("아직 아무도 주문을 모른다. 레벨을 올리면 익힐 수 있다.", null);
            return;
        }
        chooseFrom("✨ 주문", names.toArray(new String[0]), i -> {
            Hero caster = state.party.get(refs.get(i)[0]);
            int sp = refs.get(i)[1];
            if (!RpgData.SPELLS[sp].fieldUsable()) {
                say("그 주문은 전투 중에만 쓸 수 있다.", null);
                return;
            }
            chooseAlly(t -> say(state.castInField(caster, sp, state.party.get(t)), null));
        });
    }

    private void openEquip() {
        String[] heroes = new String[state.party.size()];
        for (int i = 0; i < heroes.length; i++) {
            Hero h = state.party.get(i);
            heroes[i] = h.name() + " – " + RpgData.ITEMS[h.weapon].name + " / " + RpgData.ITEMS[h.armor].name;
        }
        chooseFrom("🗡️ 누구의 장비?", heroes, hi -> {
            Hero h = state.party.get(hi);
            List<Integer> gear = new ArrayList<>();
            for (int id : state.bagItems()) {
                int type = RpgData.ITEMS[id].type;
                if (type == RpgData.WEAPON || type == RpgData.ARMOR) gear.add(id);
            }
            if (gear.isEmpty()) {
                say("가방에 바꿀 장비가 없다. 상점에서 사 보자.", null);
                return;
            }
            String[] names = new String[gear.size()];
            for (int i = 0; i < names.length; i++) {
                RpgData.Item it = RpgData.ITEMS[gear.get(i)];
                int cur = it.type == RpgData.WEAPON ? RpgData.ITEMS[h.weapon].power : RpgData.ITEMS[h.armor].power;
                int diff = it.power - cur;
                names[i] = it.name + " (" + it.desc + ", " + (diff >= 0 ? "+" : "") + diff + ")";
            }
            chooseFrom(h.name() + "에게 장비", names, i -> {
                RpgData.Item it = RpgData.ITEMS[gear.get(i)];
                if (state.equip(h, it.id)) say(Josa.eun(h.name()) + " " + Josa.eul(it.name) + " 장비했다.", null);
                save();
                refreshStatus();
            });
        });
    }

    // ---- 상점 · 여관 ----

    private void openShop(int shop) {
        chooseFrom("무엇을 하시겠어요?  (골드 " + state.gold + ")", new String[]{"사기", "팔기", "그만두기"}, i -> {
            if (i == 0) openBuy(shop);
            else if (i == 1) openSell(shop);
        });
    }

    private void openBuy(int shop) {
        int[] goods = RpgData.SHOPS[shop];
        String[] names = new String[goods.length];
        for (int i = 0; i < goods.length; i++) {
            RpgData.Item it = RpgData.ITEMS[goods[i]];
            names[i] = it.name + "  " + state.price(it.id) + "G  – " + it.desc;
        }
        chooseFrom("사기  (골드 " + state.gold + ")", names, i -> {
            RpgData.Item it = RpgData.ITEMS[goods[i]];
            if (!state.buy(it.id)) {
                say("[상인] 골드가 모자라시네요.", () -> openBuy(shop));
                return;
            }
            refreshStatus();
            save();
            if (it.type == RpgData.WEAPON || it.type == RpgData.ARMOR) {
                String[] who = new String[state.party.size() + 1];
                for (int k = 0; k < state.party.size(); k++) who[k] = state.party.get(k).name() + "에게 장비";
                who[state.party.size()] = "가방에 넣어 두기";
                chooseFrom(Josa.eul(it.name) + " 샀다! 지금 장비할까요?", who, k -> {
                    if (k < state.party.size()) state.equip(state.party.get(k), it.id);
                    refreshStatus();
                    save();
                    openBuy(shop);
                });
            } else {
                say("[상인] " + Josa.eul(it.name) + " 사셨습니다. 고마워요!", () -> openBuy(shop));
            }
        });
    }

    private void openSell(int shop) {
        List<Integer> sellable = new ArrayList<>();
        for (int id : state.bagItems()) if (state.canSell(id)) sellable.add(id);
        if (sellable.isEmpty()) {
            say("[상인] 팔 만한 물건이 없으시네요.", null);
            return;
        }
        String[] names = new String[sellable.size()];
        for (int i = 0; i < names.length; i++) {
            RpgData.Item it = RpgData.ITEMS[sellable.get(i)];
            names[i] = it.name + " × " + state.items[it.id] + "  → " + state.sellPrice(it.id) + "G";
        }
        chooseFrom("팔기  (골드 " + state.gold + ")", names, i -> {
            state.sell(sellable.get(i));
            refreshStatus();
            save();
            openSell(shop);
        });
    }

    private void openInn(int perPerson) {
        int price = state.innPrice(perPerson);
        dialogOpen = true;
        new AlertDialog.Builder(this, DIALOG_THEME)
                .setTitle("🛏️ 여관")
                .setMessage("하룻밤 " + price + "골드입니다. 묵으시겠어요?\n(가진 골드 " + state.gold + ")")
                .setPositiveButton("묵는다", (d, w) -> {
                    dialogOpen = false;
                    if (state.rest(perPerson)) {
                        save();
                        say(Arrays.asList("…… 편히 쉬었다.", "HP와 MP가 모두 회복되었다! 좋은 아침입니다."), null);
                    } else {
                        say("[여관 주인] 골드가 모자라시네요…", null);
                    }
                })
                .setNegativeButton("그만둔다", (d, w) -> dialogOpen = false)
                .setOnCancelListener(d -> dialogOpen = false)
                .show();
    }
}
