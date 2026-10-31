package com.shinchang.adventure;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.shinchang.adventure.game.GameData;
import com.shinchang.adventure.game.GameState;
import com.shinchang.adventure.game.Money;

import java.util.Random;

public class MainActivity extends Activity {

    private static final String PREFS = "shinchang";
    private static final String KEY_SAVE = "save";
    private static final long TICK_MS = 100;
    private static final long SAVE_INTERVAL_MS = 10_000;
    private static final int MAX_FLOATING_TEXTS = 25;

    private GameState state;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private long lastTick;
    private long lastSave;
    private double autoTapAcc;
    private int shownJob = -1;

    private TextView jobText, moneyText, rateText, autoText, tapButton;
    private TextView promoteTitle, promoteInfo;
    private Button promoteButton;
    private Switch autoSwitch;
    private FrameLayout tapArea;
    private LinearLayout itemContainer;
    private ItemRow[] rows = new ItemRow[0];

    private static final class ItemRow {
        View root;
        TextView name;
        TextView info;
        Button buy;
    }

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            long now = SystemClock.elapsedRealtime();
            double dt = (now - lastTick) / 1000.0;
            lastTick = now;
            state.update(dt);

            if (state.autoOn) {
                autoTapAcc += state.autoSpeed() * dt;
                if (autoTapAcc >= 1) {
                    int taps = (int) autoTapAcc;
                    autoTapAcc -= taps;
                    showAutoTap(taps);
                }
            }

            if (now - lastSave >= SAVE_INTERVAL_MS) save();
            refresh();
            checkEnding();
            handler.postDelayed(this, TICK_MS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        jobText = findViewById(R.id.jobText);
        moneyText = findViewById(R.id.moneyText);
        rateText = findViewById(R.id.rateText);
        autoText = findViewById(R.id.autoText);
        tapButton = findViewById(R.id.tapButton);
        tapArea = findViewById(R.id.tapArea);
        autoSwitch = findViewById(R.id.autoSwitch);
        promoteTitle = findViewById(R.id.promoteTitle);
        promoteInfo = findViewById(R.id.promoteInfo);
        promoteButton = findViewById(R.id.promoteButton);
        itemContainer = findViewById(R.id.itemContainer);

        state = GameState.deserialize(prefs().getString(KEY_SAVE, null));

        tapArea.setOnTouchListener((v, e) -> {
            int action = e.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int i = e.getActionIndex();
                double got = state.tap();
                spawnFloatingText(e.getX(i), e.getY(i), "+" + Money.format(got), getColor(R.color.primary_dark));
                pulse();
                refresh();
            }
            return true;
        });

        autoSwitch.setChecked(state.autoOn);
        autoSwitch.setOnCheckedChangeListener((b, checked) -> {
            state.autoOn = checked;
            refresh();
        });

        promoteButton.setOnClickListener(v -> {
            if (state.promote()) {
                save();
                refresh();
                GameData.Job job = state.currentJob();
                new AlertDialog.Builder(this)
                        .setTitle(job.emoji + " " + job.name + "(으)로 전직!")
                        .setMessage(job.story + "\n\n클릭당 금액: " + Money.format(job.clickValue)
                                + "\n새 아이템 " + job.items.length + "종이 열렸습니다.")
                        .setPositiveButton("좋아!", null)
                        .show();
            }
        });

        findViewById(R.id.titleText).setOnLongClickListener(v -> {
            confirmReset();
            return true;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyOfflineEarnings();
        lastTick = SystemClock.elapsedRealtime();
        handler.post(tick);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
        save();
    }

    private SharedPreferences prefs() {
        return getSharedPreferences(PREFS, MODE_PRIVATE);
    }

    private void save() {
        state.lastSavedMillis = System.currentTimeMillis();
        prefs().edit().putString(KEY_SAVE, state.serialize()).apply();
        lastSave = SystemClock.elapsedRealtime();
    }

    private void applyOfflineEarnings() {
        double earned = state.offlineEarnings(System.currentTimeMillis());
        save();
        if (earned < 1) return;
        state.earn(earned);
        new AlertDialog.Builder(this)
                .setTitle("🌙 자리를 비운 동안")
                .setMessage("오토클릭으로 " + Money.format(earned) + "을 벌었어요!\n(최대 8시간, 50% 적립)")
                .setPositiveButton("받기", null)
                .show();
    }

    // ---- 화면 갱신 ----

    private void refresh() {
        if (shownJob != state.job) buildItemRows();

        GameData.Job job = state.currentJob();
        jobText.setText(job.emoji + " Lv." + (state.job + 1) + " " + job.name);
        moneyText.setText(Money.format(state.money));
        rateText.setText("클릭당 " + Money.format(state.tapValue())
                + "  ·  오토 1회당 " + Money.format(state.clickValue()));
        tapButton.setText(bestOwnedEmoji());

        double speed = state.autoSpeed();
        autoText.setText(state.autoOn
                ? "⚡ 초당 " + Money.speed(speed) + "회\n💰 초당 " + Money.format(state.incomePerSecond())
                : "⏸ 오토클릭 꺼짐\n(켜면 초당 " + Money.speed(speed) + "회)");

        refreshItems();
        refreshPromotion();
    }

    private String bestOwnedEmoji() {
        GameData.Item[] items = state.currentJob().items;
        for (int k = items.length - 1; k >= 0; k--) {
            if (state.level(k) > 0) return items[k].emoji;
        }
        return state.currentJob().emoji;
    }

    private void buildItemRows() {
        shownJob = state.job;
        itemContainer.removeAllViews();
        GameData.Item[] items = state.currentJob().items;
        rows = new ItemRow[items.length];
        for (int k = 0; k < items.length; k++) {
            rows[k] = createRow(k, items[k]);
            itemContainer.addView(rows[k].root);
        }
    }

    private ItemRow createRow(int index, GameData.Item item) {
        ItemRow row = new ItemRow();

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setBackgroundResource(R.drawable.bg_card);
        root.setPadding(dp(12), dp(10), dp(12), dp(10));
        LinearLayout.LayoutParams rootLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        rootLp.bottomMargin = dp(8);
        root.setLayoutParams(rootLp);

        TextView emoji = new TextView(this);
        emoji.setText(item.emoji);
        emoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
        emoji.setGravity(Gravity.CENTER);
        root.addView(emoji, new LinearLayout.LayoutParams(dp(52), LinearLayout.LayoutParams.WRAP_CONTENT));

        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(6), 0, dp(6), 0);
        root.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        row.name = new TextView(this);
        row.name.setTextColor(getColor(R.color.text));
        row.name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        row.name.setTypeface(Typeface.DEFAULT_BOLD);
        texts.addView(row.name);

        row.info = new TextView(this);
        row.info.setTextColor(getColor(R.color.text_sub));
        row.info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        texts.addView(row.info);

        row.buy = new Button(this);
        row.buy.setAllCaps(false);
        row.buy.setBackgroundResource(R.drawable.bg_button);
        row.buy.setTextColor(0xFFFFFFFF);
        row.buy.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        row.buy.setTypeface(Typeface.DEFAULT_BOLD);
        row.buy.setMinWidth(dp(104));
        row.buy.setPadding(dp(8), 0, dp(8), 0);
        row.buy.setOnClickListener(v -> {
            if (state.buy(index)) {
                pulse();
                refresh();
            }
        });
        root.addView(row.buy, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, dp(48)));

        row.root = root;
        return row;
    }

    private void refreshItems() {
        GameData.Item[] items = state.currentJob().items;
        for (int k = 0; k < rows.length; k++) {
            ItemRow row = rows[k];
            GameData.Item item = items[k];
            int level = state.level(k);
            boolean unlocked = state.isUnlocked(k);

            row.name.setText(level > 0 ? item.name + "  Lv." + level : item.name);
            row.root.setAlpha(unlocked ? 1f : 0.5f);

            if (!unlocked) {
                row.info.setText("🔒 " + items[k - 1].name + " 먼저 구매");
                row.buy.setText("잠김");
                row.buy.setEnabled(false);
            } else if (state.isMaxed(k)) {
                row.info.setText("오토 +" + Money.speed(item.speedPerLevel * level) + "회/초 · 최대 레벨");
                row.buy.setText("MAX");
                row.buy.setEnabled(false);
            } else {
                row.info.setText("오토 +" + Money.speed(item.speedPerLevel) + "회/초 · "
                        + level + "/" + GameData.ITEM_MAX_LEVEL);
                row.buy.setText((level == 0 ? "구매\n" : "강화\n") + Money.format(state.itemCost(k)));
                row.buy.setEnabled(state.canBuy(k));
            }
        }
    }

    private void refreshPromotion() {
        if (!state.hasNextJob()) {
            promoteTitle.setText("🏆 최종 목표");
            GameData.Item[] items = state.currentJob().items;
            promoteInfo.setText(state.isEnding()
                    ? "🎉 신창은 대한민국 최고의 재벌이 되었다!\n총 수익 " + Money.format(state.totalEarned)
                    : items[items.length - 1].emoji + " " + items[items.length - 1].name + "를 세우면 엔딩!");
            promoteButton.setVisibility(View.GONE);
            return;
        }

        GameData.Job cur = state.currentJob();
        GameData.Job next = GameData.JOBS[state.job + 1];
        GameData.Item lastItem = cur.items[cur.items.length - 1];
        boolean itemReady = state.promoteItemReady();
        boolean moneyReady = state.money >= cur.promoteCost;

        promoteTitle.setText("다음 직업: " + next.emoji + " " + next.name);
        promoteInfo.setText("클릭당 " + Money.format(cur.clickValue) + " → " + Money.format(next.clickValue)
                + "\n" + (itemReady ? "✅ " : "⬜ ") + lastItem.emoji + " " + lastItem.name + " 보유"
                + "\n" + (moneyReady ? "✅ " : "⬜ ") + "전직 비용 " + Money.format(cur.promoteCost));
        promoteButton.setVisibility(View.VISIBLE);
        promoteButton.setText("전직하기");
        promoteButton.setEnabled(state.canPromote());
    }

    private void checkEnding() {
        if (!state.isEnding() || state.endingSeen) return;
        state.endingSeen = true;
        save();
        new AlertDialog.Builder(this)
                .setTitle("👑 엔딩: 신창 그룹 회장")
                .setMessage("도보 배달로 시작한 신창은 마침내 대한민국 최고의 재벌이 되었습니다!\n\n"
                        + "총 수익: " + Money.format(state.totalEarned) + "\n"
                        + "손으로 누른 횟수: " + String.format("%,d", state.manualTaps) + "번\n\n"
                        + "계속 플레이하며 아이템을 모두 MAX로 만들어 보세요.")
                .setPositiveButton("최고야!", null)
                .show();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("처음부터 다시 하기")
                .setMessage("모든 진행 상황이 지워집니다. 정말 처음부터 시작할까요?")
                .setPositiveButton("초기화", (d, w) -> {
                    state = new GameState();
                    autoSwitch.setChecked(state.autoOn);
                    shownJob = -1;
                    save();
                    refresh();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // ---- 효과 ----

    private void showAutoTap(int taps) {
        float x = tapArea.getWidth() / 2f + (random.nextFloat() - 0.5f) * dp(200);
        float y = tapArea.getHeight() / 2f + (random.nextFloat() - 0.5f) * dp(120);
        spawnFloatingText(x, y, "+" + Money.format(state.clickValue() * taps), getColor(R.color.accent));
        pulse();
    }

    private void pulse() {
        tapButton.animate().cancel();
        tapButton.setScaleX(0.9f);
        tapButton.setScaleY(0.9f);
        tapButton.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
    }

    private void spawnFloatingText(float x, float y, String text, int color) {
        if (tapArea.getChildCount() > MAX_FLOATING_TEXTS) return;
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(color);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setShadowLayer(4, 0, 2, 0x55000000);
        tapArea.addView(tv, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        tv.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        tv.setX(x - tv.getMeasuredWidth() / 2f);
        tv.setY(y - tv.getMeasuredHeight());
        tv.animate()
                .translationYBy(-dp(80))
                .alpha(0f)
                .setDuration(800)
                .withEndAction(() -> tapArea.removeView(tv))
                .start();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
