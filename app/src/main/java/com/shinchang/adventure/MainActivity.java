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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.shinchang.adventure.game.GameData;
import com.shinchang.adventure.game.GameState;
import com.shinchang.adventure.game.Money;

import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {

    private static final String PREFS = "shinchang";
    private static final String KEY_SAVE = "save";
    private static final long TICK_MS = 100;
    private static final long SAVE_INTERVAL_MS = 10_000;
    private static final int MAX_FLOATING_TEXTS = 25;
    private static final String[] CATEGORY_ICONS = {"🔑", "🏦", "📝"};

    private GameState state;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();
    private long lastTick;
    private long lastSave;
    private double autoTapAcc;
    private int shownJob = -1;
    private int shownHighestJob = -1;
    private int shownEventSeq;
    private int shownWeatherSeq;
    private int shownIncidentSeq;
    private int shownWeather = -1;

    private TextView jobText, moneyText, rateText, autoText, tapButton, tapHint, weatherBanner;
    private TextView starText, multText, sunText;
    private WeatherView weatherView;
    private View header;
    private TextView promoteTitle, promoteInfo;
    private TextView contractTitle, contractInfo;
    private ProgressBar clickBar, timeBar;
    private View contractPanel, promoteCard, jobBarScroll;
    private ImageView vehicleImage;
    private Button promoteButton;
    private Switch autoSwitch;
    private FrameLayout tapArea;
    private LinearLayout itemContainer, contractContainer, jobBar;
    private ItemRow[] rows = new ItemRow[0];
    private ContractRow[] contractRows = new ContractRow[0];

    private static final class ItemRow {
        View root;
        TextView name;
        TextView info;
        Button buy;
        /** 탈것 판매 버튼 (배달원만) */
        Button sell;
    }

    private static final class ContractRow {
        View root;
        TextView info;
        Button start;
    }

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            long now = SystemClock.elapsedRealtime();
            double dt = (now - lastTick) / 1000.0;
            lastTick = now;
            state.update(dt);

            if (state.autoOn && !state.isBroker()) {
                autoTapAcc += state.autoSpeed() * dt;
                if (autoTapAcc >= 1) {
                    int taps = (int) autoTapAcc;
                    autoTapAcc -= taps;
                    showAutoTap(taps);
                }
            }

            if (now - lastSave >= SAVE_INTERVAL_MS) save();
            showContractEvent();
            showWeatherEvents();
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
        tapHint = findViewById(R.id.tapHint);
        tapArea = findViewById(R.id.tapArea);
        vehicleImage = findViewById(R.id.vehicleImage);
        autoSwitch = findViewById(R.id.autoSwitch);
        promoteCard = findViewById(R.id.promoteCard);
        promoteTitle = findViewById(R.id.promoteTitle);
        promoteInfo = findViewById(R.id.promoteInfo);
        promoteButton = findViewById(R.id.promoteButton);
        itemContainer = findViewById(R.id.itemContainer);
        contractContainer = findViewById(R.id.contractContainer);
        contractPanel = findViewById(R.id.contractPanel);
        contractTitle = findViewById(R.id.contractTitle);
        contractInfo = findViewById(R.id.contractInfo);
        clickBar = findViewById(R.id.clickBar);
        timeBar = findViewById(R.id.timeBar);
        jobBarScroll = findViewById(R.id.jobBarScroll);
        jobBar = findViewById(R.id.jobBar);
        weatherView = findViewById(R.id.weatherView);
        weatherBanner = findViewById(R.id.weatherBanner);
        header = findViewById(R.id.header);
        starText = findViewById(R.id.starText);
        multText = findViewById(R.id.multText);
        sunText = findViewById(R.id.sunText);
        buildMenuBar(findViewById(R.id.menuBar));

        state = GameState.deserialize(prefs().getString(KEY_SAVE, null));
        shownEventSeq = state.eventSeq;
        shownWeatherSeq = state.weatherSeq;
        shownIncidentSeq = state.incidentSeq;

        tapArea.setOnTouchListener((v, e) -> {
            int action = e.getActionMasked();
            if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_POINTER_DOWN) {
                int i = e.getActionIndex();
                onTap(e.getX(i), e.getY(i));
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
                String how = state.isBroker()
                        ? "이제 클릭으로 돈을 벌지 않아요.\n아래 계약을 골라 제한 시간 안에 목표 클릭 수를 채우면 중개보수를 받습니다!"
                        : state.isBoostJob()
                        ? "클릭당 금액: " + Money.format(job.clickValue)
                        + "\n\n아래 소방 계약을 제한 시간 안에 성사시키면, 제한 시간의 3배 동안 오토 수익이 3배가 됩니다!"
                        : "클릭당 금액: " + Money.format(job.clickValue);
                new AlertDialog.Builder(this)
                        .setTitle(job.emoji + " " + job.name + "(으)로 전직!")
                        .setMessage(job.story + "\n\n" + how
                                + "\n\n위쪽 직업 버튼으로 이전 직업에 언제든 돌아갈 수 있어요.")
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
        state.receive(earned);
        new AlertDialog.Builder(this)
                .setTitle("🌙 자리를 비운 동안")
                .setMessage("오토클릭으로 " + Money.format(earned) + "을 벌었어요!\n(최대 8시간, 50% 적립)")
                .setPositiveButton("받기", null)
                .show();
    }

    private void onTap(float x, float y) {
        if (state.activeContract >= 0) {
            state.tap();
            spawnFloatingText(x, y, "+1", getColor(R.color.primary_dark));
            showContractEvent();
        } else if (state.isBroker()) {
            spawnFloatingText(x, y, "계약을 먼저 고르세요", getColor(R.color.text_sub));
            return;
        } else {
            double got = state.tap();
            spawnFloatingText(x, y, "+" + Money.format(got), getColor(R.color.primary_dark));
        }
        pulse();
        refresh();
    }

    // ---- 화면 갱신 ----

    private void refresh() {
        if (shownJob != state.job) buildLists();
        if (shownHighestJob != state.highestJob) buildJobBar();

        GameData.Job job = state.currentJob();
        jobText.setText(job.emoji + " Lv." + (state.job + 1) + " " + job.name);
        moneyText.setText(Money.format(state.money));
        starText.setText("⭐ 별 " + String.format(Locale.KOREA, "%,d", state.stars) + "개");
        multText.setText("🙏 수입 ×" + multiplierText(state.incomeMultiplier()));
        sunText.setVisibility(state.suns > 0 ? View.VISIBLE : View.GONE);
        sunText.setText("☀️ 해 " + String.format(Locale.KOREA, "%,d", state.suns) + "개");

        double speed = state.autoSpeed();
        if (state.isBroker()) {
            rateText.setText("계약 성사 시 중개보수를 받아요");
            double income = state.incomePerSecond();
            autoText.setText(!state.autoOn
                    ? "⏸ 오토클릭 꺼짐\n(켜면 초당 " + Money.speed(speed) + "회 자동 클릭)"
                    : "⚡ 자동 클릭 초당 " + Money.speed(speed) + "회\n"
                    + (income > 0 ? "🔁 반복 계약 초당 약 " + Money.format(income) : "🔁 마지막 계약을 자동 반복"));
        } else {
            rateText.setText("클릭당 " + Money.format(state.clickValue() * state.incomeMultiplier())
                    + (state.isDelivery() ? " (배달 1건)" : state.isBoostJob() ? " · 계약 성사 시 오토 수익 ×3" : ""));
            String boost = state.boostMultiplier() > 1 ? "  🔥×" + (int) state.boostMultiplier() : "";
            autoText.setText(state.autoOn
                    ? "⚡ 초당 " + Money.speed(speed) + "회" + boost + "\n💰 초당 " + Money.format(state.incomePerSecond())
                    : "⏸ 오토클릭 꺼짐\n(켜면 초당 " + Money.speed(speed) + "회)");
        }

        refreshWeather();
        refreshTapArea();
        refreshItems();
        refreshContracts();
        refreshPromotion();
        refreshJobBar();
    }

    private void refreshTapArea() {
        int vehicle = bestVehicleImage();
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) tapButton.getLayoutParams();
        if (state.hasContracts()) {
            vehicleImage.setVisibility(View.GONE);
            tapButton.setVisibility(View.VISIBLE);
            tapButton.setText(state.isBroker() ? "🤝" : "🧯");
            tapButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 52);
            setTapButtonLayout(lp, dp(124), Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, dp(22));
            contractPanel.setVisibility(View.VISIBLE);
            tapHint.setText(state.activeContract >= 0 ? "빠르게 연타!"
                    : state.isBroker() ? "아래에서 계약을 고르세요" : "탭해서 돈 벌기 · 계약은 아래에서");

            if (state.activeContract >= 0) {
                GameData.Contract c = state.contracts()[state.activeContract];
                contractTitle.setText(c.emoji + " " + c.name + (state.isBroker()
                        ? " 계약 중 · 보수 " + Money.format(c.reward)
                        : " 중 · 성공 시 " + (int) c.boostSeconds + "초간 ×3"));
                clickBar.setProgress((int) (1000 * Math.min(1, state.contractClicks / c.clicks)));
                timeBar.setProgress((int) (1000 * Math.max(0, state.contractTimeLeft / c.seconds)));
                contractInfo.setText(String.format(Locale.KOREA, "클릭 %d / %d   ·   남은 시간 %.1f초",
                        (int) state.contractClicks, c.clicks, Math.max(0, state.contractTimeLeft)));
            } else {
                boolean repeating = state.autoOn && state.lastContract >= 0;
                clickBar.setProgress(0);
                if (state.isBroker()) {
                    contractTitle.setText(repeating
                            ? "🔁 " + state.contracts()[state.lastContract].name + " 다음 손님 기다리는 중..."
                            : "📋 진행 중인 계약 없음");
                    timeBar.setProgress(0);
                    contractInfo.setText("월세 → 전세 → 매매 순으로 큰 계약이 열려요");
                } else if (state.boostLeft > 0) {
                    contractTitle.setText(String.format(Locale.KOREA, "🔥 오토 수익 ×3 · %d초 남음", (int) Math.ceil(state.boostLeft)));
                    timeBar.setProgress((int) (1000 * state.boostLeft / GameData.BOOST_MAX_SECONDS));
                    contractInfo.setText("계약을 또 성사시키면 시간이 더 쌓여요 (최대 10분)");
                } else {
                    contractTitle.setText("📋 계약을 성사시키면 오토 수익 ×3");
                    timeBar.setProgress(0);
                    contractInfo.setText("큰 계약일수록 클릭은 많지만 ×3 시간도 길어요");
                }
            }
        } else {
            contractPanel.setVisibility(View.GONE);
            tapHint.setText(R.string.tap_hint);
            if (vehicle != 0) {
                tapButton.setVisibility(View.INVISIBLE);
                vehicleImage.setVisibility(View.VISIBLE);
                vehicleImage.setImageResource(vehicle);
            } else {
                vehicleImage.setVisibility(View.GONE);
                tapButton.setVisibility(View.VISIBLE);
                tapButton.setText(bestOwnedEmoji());
                tapButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 72);
                setTapButtonLayout(lp, dp(170), Gravity.CENTER, 0);
            }
        }
    }

    private void setTapButtonLayout(FrameLayout.LayoutParams lp, int size, int gravity, int bottomMargin) {
        if (lp.width == size && lp.gravity == gravity && lp.bottomMargin == bottomMargin) return;
        lp.width = size;
        lp.height = size;
        lp.gravity = gravity;
        lp.bottomMargin = bottomMargin;
        tapButton.setLayoutParams(lp);
    }

    /** 배달원은 타고 있는 탈것, 그 외에는 가진 가장 좋은 아이템의 그림 (없으면 0) */
    private int bestVehicleImage() {
        GameData.Item[] items = state.currentJob().items;
        if (state.isDelivery()) return imageRes(items[state.selectedVehicle]);
        for (int k = items.length - 1; k >= 0; k--) {
            if (state.level(k) > 0) return imageRes(items[k]);
        }
        return 0;
    }

    private int imageRes(GameData.Item item) {
        if (item.image == null) return 0;
        return getResources().getIdentifier(item.image, "drawable", getPackageName());
    }

    private String bestOwnedEmoji() {
        GameData.Item[] items = state.currentJob().items;
        for (int k = items.length - 1; k >= 0; k--) {
            if (state.level(k) > 0) return items[k].emoji;
        }
        return state.currentJob().emoji;
    }

    private void buildLists() {
        shownJob = state.job;
        autoTapAcc = 0;

        itemContainer.removeAllViews();
        GameData.Item[] items = state.currentJob().items;
        rows = new ItemRow[items.length];
        for (int k = 0; k < items.length; k++) {
            rows[k] = createItemRow(k, items[k]);
            itemContainer.addView(rows[k].root);
        }

        contractContainer.removeAllViews();
        if (!state.hasContracts()) {
            contractRows = new ContractRow[0];
            return;
        }
        GameData.Contract[] cs = state.contracts();
        contractRows = new ContractRow[cs.length];
        int categories = state.isBroker() ? GameData.CONTRACT_CATEGORIES.length : 1;
        for (int cat = 0; cat < categories; cat++) {
            TextView header = new TextView(this);
            header.setText(state.isBroker()
                    ? CATEGORY_ICONS[cat] + " " + GameData.CONTRACT_CATEGORIES[cat] + " 계약"
                    : "🧯 소방 계약 (성사 시 오토 수익 ×3)");
            header.setTextColor(getColor(R.color.text));
            header.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            header.setTypeface(Typeface.DEFAULT_BOLD);
            header.setPadding(0, dp(16), 0, dp(8));
            contractContainer.addView(header);
            for (int i = 0; i < cs.length; i++) {
                if (cs[i].category != cat) continue;
                contractRows[i] = createContractRow(i, cs[i]);
                contractContainer.addView(contractRows[i].root);
            }
        }
    }

    private LinearLayout newCardRow() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.HORIZONTAL);
        root.setGravity(Gravity.CENTER_VERTICAL);
        root.setBackgroundResource(R.drawable.bg_card);
        root.setPadding(dp(10), dp(8), dp(10), dp(8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = dp(8);
        root.setLayoutParams(lp);
        return root;
    }

    private Button newActionButton() {
        Button b = new Button(this);
        b.setAllCaps(false);
        b.setBackgroundResource(R.drawable.bg_button);
        b.setTextColor(0xFFFFFFFF);
        b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setMinWidth(dp(96));
        b.setPadding(dp(8), 0, dp(8), 0);
        return b;
    }

    private TextView addTexts(LinearLayout root, TextView[] out) {
        LinearLayout texts = new LinearLayout(this);
        texts.setOrientation(LinearLayout.VERTICAL);
        texts.setPadding(dp(8), 0, dp(6), 0);
        root.addView(texts, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        TextView name = new TextView(this);
        name.setTextColor(getColor(R.color.text));
        name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        name.setTypeface(Typeface.DEFAULT_BOLD);
        texts.addView(name);

        TextView info = new TextView(this);
        info.setTextColor(getColor(R.color.text_sub));
        info.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        texts.addView(info);
        out[0] = name;
        return info;
    }

    private ItemRow createItemRow(int index, GameData.Item item) {
        ItemRow row = new ItemRow();
        LinearLayout root = newCardRow();

        int image = imageRes(item);
        if (image != 0) {
            ImageView iv = new ImageView(this);
            iv.setImageResource(image);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            root.addView(iv, new LinearLayout.LayoutParams(dp(72), dp(45)));
        } else {
            TextView emoji = new TextView(this);
            emoji.setText(item.emoji);
            emoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 30);
            emoji.setGravity(Gravity.CENTER);
            root.addView(emoji, new LinearLayout.LayoutParams(dp(52), LinearLayout.LayoutParams.WRAP_CONTENT));
        }

        TextView[] name = new TextView[1];
        row.info = addTexts(root, name);
        row.name = name[0];

        row.buy = newActionButton();
        row.buy.setOnClickListener(v -> {
            // 배달원: 가진 탈것이면 바꿔 타기, 아니면 구매
            if (state.selectVehicle(index) || state.buy(index)) {
                pulse();
                refresh();
            }
        });
        if (!state.isDelivery()) {
            root.addView(row.buy, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(48)));
            row.root = root;
            return row;
        }

        LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.VERTICAL);
        buttons.addView(row.buy, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)));
        row.sell = newActionButton();
        row.sell.setBackgroundResource(R.drawable.bg_button_sub);
        row.sell.setTextColor(getColor(R.color.text_sub));
        row.sell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        row.sell.setOnClickListener(v -> confirmSell(index));
        LinearLayout.LayoutParams sellLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(32));
        sellLp.topMargin = dp(4);
        buttons.addView(row.sell, sellLp);
        root.addView(buttons, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        row.root = root;
        return row;
    }

    private ContractRow createContractRow(int index, GameData.Contract c) {
        ContractRow row = new ContractRow();
        LinearLayout root = newCardRow();

        TextView emoji = new TextView(this);
        emoji.setText(c.emoji);
        emoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        emoji.setGravity(Gravity.CENTER);
        root.addView(emoji, new LinearLayout.LayoutParams(dp(44), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView[] name = new TextView[1];
        row.info = addTexts(root, name);
        name[0].setText(c.boostSeconds > 0
                ? c.name + "  🔥" + (int) c.boostSeconds + "초"
                : c.name + "  " + Money.format(c.reward));

        row.start = newActionButton();
        row.start.setOnClickListener(v -> {
            if (state.startContract(index)) refresh();
        });
        root.addView(row.start, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(48)));
        row.root = root;
        return row;
    }

    private void refreshItems() {
        if (state.isDelivery()) {
            refreshVehicles();
            return;
        }
        GameData.Job job = state.currentJob();
        GameData.Item[] items = job.items;
        int best = -1;
        for (int k = 0; k < items.length; k++) if (state.level(k) > 0) best = k;

        for (int k = 0; k < rows.length; k++) {
            ItemRow row = rows[k];
            GameData.Item item = items[k];
            int level = state.level(k);
            boolean unlocked = state.isUnlocked(k);
            boolean single = item.maxLevel == 1;

            row.name.setText(level > 0 && !single ? item.name + "  Lv." + level : item.name);
            row.root.setAlpha(unlocked ? 1f : 0.5f);

            String effect = state.isBroker()
                    ? "자동 클릭 +" + Money.speed(item.speedPerLevel) + "회/초"
                    : job.bestItemOnly
                    ? "오토 배달 초당 " + Money.speed(item.speedPerLevel) + "건"
                    : "오토 +" + Money.speed(item.speedPerLevel) + "회/초";

            if (!unlocked) {
                row.info.setText("🔒 " + items[k - 1].name + " 먼저 구매");
                row.buy.setText("잠김");
                row.buy.setEnabled(false);
            } else if (state.isMaxed(k)) {
                row.info.setText(single ? effect : effect + " · 최대 레벨");
                row.buy.setText(single ? (job.bestItemOnly && k == best ? "타는 중" : "보유") : "MAX");
                row.buy.setEnabled(false);
            } else {
                row.info.setText(single ? effect : effect + " · " + level + "/" + item.maxLevel);
                double cost = state.itemCost(k);
                row.buy.setText((level == 0 ? "구매\n" : "강화\n") + (cost == 0 ? "무료" : Money.format(cost)));
                row.buy.setEnabled(state.canBuy(k));
            }
        }
    }

    private void refreshVehicles() {
        GameData.Item[] items = state.currentJob().items;
        for (int k = 0; k < rows.length; k++) {
            ItemRow row = rows[k];
            GameData.Item item = items[k];
            boolean owned = state.level(k) > 0;
            boolean unlocked = state.isUnlocked(k);
            row.name.setText(item.name);
            row.root.setAlpha(unlocked ? 1f : 0.5f);
            row.info.setText("오토 배달 초당 " + Money.speed(item.speedPerLevel) + "건\n" + riskText(k));

            if (!unlocked) {
                row.buy.setText("잠김");
                row.buy.setEnabled(false);
            } else if (owned && state.selectedVehicle == k) {
                row.buy.setText("타는 중");
                row.buy.setEnabled(false);
            } else if (owned) {
                row.buy.setText("타기");
                row.buy.setEnabled(true);
            } else {
                double cost = state.itemCost(k);
                row.buy.setText("구매 " + Money.format(cost));
                row.buy.setEnabled(state.canBuy(k));
            }

            boolean sellable = state.canSell(k);
            row.sell.setVisibility(sellable ? View.VISIBLE : View.INVISIBLE);
            if (sellable) row.sell.setText("판매 " + Money.format(state.sellPrice(k)));
        }
    }

    private static String riskText(int vehicle) {
        double p = GameData.ACCIDENT_CHANCE_PER_SEC[vehicle];
        if (p == 0) return "🛡️ 사고 없음 (슬립만 주의)";
        if (p >= 0.012) return "⚠️ 사고 위험 높음";
        if (p >= 0.007) return "⚠️ 사고 위험 보통";
        if (p >= 0.003) return "✅ 사고 위험 낮음";
        return "✅ 사고 거의 없음";
    }

    private void confirmSell(int k) {
        if (!state.canSell(k)) return;
        GameData.Item item = state.currentJob().items[k];
        new AlertDialog.Builder(this)
                .setTitle(item.name + " 판매")
                .setMessage(item.name + "을(를) " + Money.format(state.sellPrice(k))
                        + "에 팔까요?\n(구매가의 70%)"
                        + (k == state.currentJob().items.length - 1 ? "\n\n⚠️ R1000이 없으면 전직할 수 없어요." : ""))
                .setPositiveButton("판매", (d, w) -> {
                    if (state.sell(k)) {
                        save();
                        refresh();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // ---- 날씨 ----

    private void refreshWeather() {
        int w = state.isDelivery() ? state.weather : GameData.CLEAR;
        if (w != shownWeather) {
            shownWeather = w;
            weatherView.setWeather(w);
            header.setBackgroundResource(w == GameData.RAIN ? R.drawable.bg_header_rain
                    : w == GameData.SNOW ? R.drawable.bg_header_snow : R.drawable.bg_header);
            tapArea.setBackgroundColor(w == GameData.RAIN ? 0xFFDCE6F0 : w == GameData.SNOW ? 0xFFF2F7FC : 0);
        }
        if (w == GameData.CLEAR) {
            if (state.boostMultiplier() > 1) {
                weatherBanner.setVisibility(View.VISIBLE);
                weatherBanner.setText(String.format(Locale.KOREA, "🔥 오토 수익 ×3 · %d초 남음",
                        (int) Math.ceil(state.boostLeft)));
            } else {
                weatherBanner.setVisibility(View.GONE);
            }
            return;
        }
        weatherBanner.setVisibility(View.VISIBLE);
        weatherBanner.setText(String.format(Locale.KOREA, "%s %s 피버! 배달 1건 %s · %d초 남음",
                w == GameData.RAIN ? "🌧️" : "❄️", GameData.WEATHER_NAMES[w],
                Money.format(GameData.WEATHER_FEE[w]), (int) Math.ceil(state.weatherLeft)));
    }

    private void showWeatherEvents() {
        float cx = tapArea.getWidth() / 2f;
        if (state.weatherSeq != shownWeatherSeq) {
            shownWeatherSeq = state.weatherSeq;
            if (state.isDelivery() && state.weather != GameData.CLEAR) {
                spawnFloatingText(cx, tapArea.getHeight() * 0.35f,
                        state.weather == GameData.RAIN ? "🌧️ 비가 온다! 피버타임 10,000원" : "❄️ 눈이 온다! 피버타임 20,000원",
                        getColor(R.color.accent));
            }
        }
        if (state.incidentSeq != shownIncidentSeq) {
            shownIncidentSeq = state.incidentSeq;
            boolean accident = state.incidentType == 2;
            spawnFloatingText(cx, tapArea.getHeight() * 0.6f,
                    (accident ? "💥 사고! -" : "💦 미끄러졌다! -") + Money.format(state.incidentPenalty), 0xFFD7262E);
            shake(accident ? dp(14) : dp(7));
        }
    }

    private void shake(float distance) {
        View target = vehicleImage.getVisibility() == View.VISIBLE ? vehicleImage : tapButton;
        target.animate().cancel();
        target.setTranslationX(distance);
        target.setRotation(distance > dp(10) ? -12 : -5);
        target.animate().translationX(0).rotation(0).setDuration(450)
                .setInterpolator(new android.view.animation.OvershootInterpolator(4f)).start();
    }

    private void refreshContracts() {
        for (int i = 0; i < contractRows.length; i++) {
            ContractRow row = contractRows[i];
            GameData.Contract c = state.contracts()[i];
            boolean unlocked = state.isContractUnlocked(i);
            row.root.setAlpha(unlocked ? 1f : 0.5f);
            if (!unlocked) {
                int need = state.unlockRequirement(i);
                row.info.setText(c.detail + "\n🔒 " + state.contracts()[need].name + " "
                        + Math.min(state.done()[need], GameData.CONTRACT_UNLOCK_COUNT) + "/"
                        + GameData.CONTRACT_UNLOCK_COUNT + "회 성사하면 열림");
                row.start.setText("잠김");
                row.start.setEnabled(false);
                continue;
            }
            row.info.setText(c.detail + "\n⏱ " + c.seconds + "초 안에 " + c.clicks + "번 클릭 · 성사 "
                    + state.done()[i] + "회"
                    + (c.boostSeconds > 0 ? "\n🔥 성공하면 " + (int) c.boostSeconds + "초간 오토 수익 ×3" : ""));
            if (state.activeContract == i) {
                row.start.setText("진행 중");
                row.start.setEnabled(false);
            } else {
                row.start.setText(state.lastContract == i && state.autoOn ? "🔁 반복 중" : "계약 시작");
                row.start.setEnabled(true);
            }
        }
    }

    private void showContractEvent() {
        if (state.eventSeq == shownEventSeq) return;
        shownEventSeq = state.eventSeq;
        if (state.eventContract >= state.contracts().length) return;
        GameData.Contract c = state.contracts()[state.eventContract];
        float x = tapArea.getWidth() / 2f;
        float y = tapArea.getHeight() / 2f;
        if (state.eventSuccess) {
            spawnFloatingText(x, y, c.boostSeconds > 0
                    ? "🔥 계약 성사! " + (int) c.boostSeconds + "초간 오토 ×3"
                    : "🎉 계약 성사! +" + Money.format(c.reward * state.incomeMultiplier()), getColor(R.color.primary_dark));
            pulse();
        } else {
            spawnFloatingText(x, y, "⌛ 시간 초과… 계약 실패", getColor(R.color.text_sub));
        }
    }

    private void buildJobBar() {
        shownHighestJob = state.highestJob;
        jobBar.removeAllViews();
        jobBarScroll.setVisibility(state.highestJob > 0 ? View.VISIBLE : View.GONE);
        for (int j = 0; j <= state.highestJob; j++) {
            final int target = j;
            Button b = newActionButton();
            GameData.Job job = GameData.JOBS[j];
            b.setText(job.emoji + " " + job.name);
            b.setOnClickListener(v -> {
                if (state.switchJob(target)) {
                    save();
                    refresh();
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(40));
            lp.rightMargin = dp(6);
            jobBar.addView(b, lp);
        }
    }

    private void refreshJobBar() {
        for (int j = 0; j < jobBar.getChildCount(); j++) {
            View b = jobBar.getChildAt(j);
            // 지금 직업 버튼은 눌린 상태(비활성)로 표시
            b.setEnabled(j != state.job);
        }
    }

    private void refreshPromotion() {
        GameData.Job cur = state.currentJob();
        if (state.job == GameData.lastJob()) {
            promoteCard.setVisibility(View.VISIBLE);
            promoteTitle.setText("🏆 최종 목표");
            GameData.Item[] items = cur.items;
            promoteInfo.setText(state.isEnding()
                    ? "🎉 신창은 대한민국 최고의 재벌이 되었다!\n총 수익 " + Money.format(state.totalEarned)
                    : items[items.length - 1].emoji + " " + items[items.length - 1].name + "를 세우면 엔딩!");
            promoteButton.setVisibility(View.GONE);
            return;
        }
        if (!state.hasNextJob()) {
            // 이전 직업으로 돌아와 있는 중: 전직은 가장 높은 직업에서만
            promoteCard.setVisibility(View.GONE);
            return;
        }

        promoteCard.setVisibility(View.VISIBLE);
        GameData.Job next = GameData.JOBS[state.job + 1];
        GameData.Item lastItem = cur.items[cur.items.length - 1];
        boolean itemReady = state.promoteItemReady();
        boolean moneyReady = state.money >= cur.promoteCost;

        String nextInfo = next.type == GameData.JobType.BROKER
                ? "월세·전세·매매 계약으로 큰돈을 버는 직업"
                : "클릭당 " + Money.format(next.clickValue);
        promoteTitle.setText("다음 직업: " + next.emoji + " " + next.name);
        promoteInfo.setText(nextInfo
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
                        + "손으로 누른 횟수: " + String.format(Locale.KOREA, "%,d", state.manualTaps) + "번\n\n"
                        + "계속 플레이하며 아이템을 모두 MAX로 만들어 보세요.")
                .setPositiveButton("최고야!", null)
                .show();
    }

    // ---- 신창에게 투자 / 교회 기부 / 완전 리셋 ----

    private static String multiplierText(double m) {
        return m < 100 ? String.format(Locale.ROOT, "%.2f", m) : String.format(Locale.KOREA, "%,d", (long) m);
    }

    private void buildMenuBar(LinearLayout bar) {
        String[] labels = {"🙋 신창에게 투자", "⛪ 교회 기부", "☀️ 완전 리셋"};
        Runnable[] actions = {this::showSelfInvest, this::confirmDonate, this::confirmFullReset};
        for (int i = 0; i < labels.length; i++) {
            Button b = newActionButton();
            b.setText(labels[i]);
            b.setMinWidth(0);
            b.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            if (i > 0) {
                b.setBackgroundResource(R.drawable.bg_button_sub);
                b.setTextColor(getColor(R.color.text));
            }
            Runnable action = actions[i];
            b.setOnClickListener(v -> action.run());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(42), 1);
            if (i > 0) lp.leftMargin = dp(6);
            bar.addView(b, lp);
        }
    }

    private void showSelfInvest() {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(16), dp(8), dp(16), dp(8));
        android.widget.ScrollView scroll = new android.widget.ScrollView(this);
        scroll.addView(list);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("🙋 신창에게 투자")
                .setView(scroll)
                .setPositiveButton("닫기", null)
                .create();
        fillSelfInvest(list);
        dialog.show();
    }

    private void fillSelfInvest(LinearLayout list) {
        list.removeAllViews();
        TextView head = new TextView(this);
        head.setText("자기 자신에게 투자하면 모든 직업의 수입이 올라요.\n교회 기부로 리셋해도 남아요.\n지금 투자 보너스: +"
                + Math.round(state.selfBonus() * 100) + "%");
        head.setTextColor(getColor(R.color.text_sub));
        head.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        head.setPadding(0, 0, 0, dp(10));
        list.addView(head);
        for (int i = 0; i < GameData.SELF_INVESTMENTS.length; i++) {
            GameData.SelfInvestment inv = GameData.SELF_INVESTMENTS[i];
            LinearLayout row = newCardRow();
            TextView emoji = new TextView(this);
            emoji.setText(inv.emoji);
            emoji.setTextSize(TypedValue.COMPLEX_UNIT_SP, 26);
            emoji.setGravity(Gravity.CENTER);
            row.addView(emoji, new LinearLayout.LayoutParams(dp(40), LinearLayout.LayoutParams.WRAP_CONTENT));
            TextView[] name = new TextView[1];
            TextView info = addTexts(row, name);
            name[0].setText(inv.name);
            info.setText("모든 수입 +" + Math.round(inv.bonus * 100) + "%");
            Button b = newActionButton();
            final int index = i;
            if (state.selfOwned[i]) {
                b.setText("완료 ✅");
                b.setEnabled(false);
            } else if (!state.isSelfUnlocked(i)) {
                b.setText("🔒 잠김");
                b.setEnabled(false);
                row.setAlpha(0.5f);
            } else {
                b.setText("투자\n" + Money.format(inv.cost));
                b.setEnabled(state.canInvest(i));
            }
            b.setOnClickListener(v -> {
                if (state.invest(index)) {
                    save();
                    refresh();
                    fillSelfInvest(list);
                }
            });
            row.addView(b, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(48)));
            list.addView(row);
        }
    }

    private void confirmDonate() {
        long stars = state.starsForDonation();
        double newMult = (1 + (state.stars + stars) * GameData.STAR_BONUS) * (1 + state.selfBonus());
        String msg = "가진 돈 " + Money.format(state.money) + "을 모두 교회에 기부합니다.\n\n"
                + "이번 회차 총수입: " + Money.format(state.totalEarned) + "\n"
                + "받을 별: ⭐ " + String.format(Locale.KOREA, "%,d", stars) + "개 (별 1개당 수입 +10%)\n"
                + "기부 후 수입 배수: ×" + multiplierText(newMult) + "\n\n"
                + "직업·아이템·탈것·계약 기록과 돈은 처음으로 돌아가요.\n"
                + "별, 신창에게 투자, 해는 남아요."
                + (stars < 1 ? "\n\n⚠️ 총수입이 1억원 이상이어야 별을 받을 수 있어요." : "");
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("⛪ 교회에 전액 기부")
                .setMessage(msg)
                .setPositiveButton("기부하기", (dialog, w) -> {
                    replaceState(state.donate());
                    new AlertDialog.Builder(this)
                            .setTitle("🙏 신의 축복")
                            .setMessage("별 " + stars + "개를 받았어요!\n이제 모든 수입이 ×"
                                    + multiplierText(state.incomeMultiplier()) + " 입니다.\n도보 배달부터 다시 시작해요.")
                            .setPositiveButton("좋아!", null)
                            .show();
                })
                .setNegativeButton("취소", null)
                .show();
        d.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(state.canDonate());
    }

    private void confirmFullReset() {
        long suns = state.sunsForFullReset();
        AlertDialog d = new AlertDialog.Builder(this)
                .setTitle("☀️ 완전 리셋")
                .setMessage("별(신의 축복)과 신창에게 투자까지 모두 지우고 완전히 처음부터 시작합니다.\n\n"
                        + "소지금 " + Money.format(state.money) + " 기준으로\n"
                        + "받을 해: ☀️ " + String.format(Locale.KOREA, "%,d", suns) + "개 (소지금 1조원부터)\n\n"
                        + "해의 기능은 추후 업데이트 예정이에요. 해는 계속 남아요.")
                .setPositiveButton("완전 리셋", (dialog, w) -> {
                    replaceState(state.fullReset());
                    Toast.makeText(this, "☀️ 해 " + suns + "개를 받고 새로 시작해요", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("취소", null)
                .show();
        d.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFD7262E);
    }

    private void replaceState(GameState next) {
        state = next;
        shownEventSeq = state.eventSeq;
        shownWeatherSeq = state.weatherSeq;
        shownIncidentSeq = state.incidentSeq;
        autoSwitch.setChecked(state.autoOn);
        shownJob = -1;
        shownHighestJob = -1;
        save();
        refresh();
    }

    private void confirmReset() {
        new AlertDialog.Builder(this)
                .setTitle("처음부터 다시 하기")
                .setMessage("모든 진행 상황이 지워집니다. 정말 처음부터 시작할까요?")
                .setPositiveButton("초기화", (d, w) -> {
                    state = new GameState();
                    shownEventSeq = state.eventSeq;
                    shownWeatherSeq = state.weatherSeq;
                    shownIncidentSeq = state.incidentSeq;
                    autoSwitch.setChecked(state.autoOn);
                    shownJob = -1;
                    shownHighestJob = -1;
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
        spawnFloatingText(x, y, "+" + Money.format(state.clickValue() * taps * state.incomeMultiplier()
                * state.boostMultiplier()), getColor(R.color.accent));
        pulse();
    }

    private void pulse() {
        View target = vehicleImage.getVisibility() == View.VISIBLE ? vehicleImage : tapButton;
        target.animate().cancel();
        target.setScaleX(0.92f);
        target.setScaleY(0.92f);
        target.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
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
        tv.setX(Math.max(0, Math.min(tapArea.getWidth() - tv.getMeasuredWidth(), x - tv.getMeasuredWidth() / 2f)));
        tv.setY(y - tv.getMeasuredHeight());
        tv.animate()
                .translationYBy(-dp(80))
                .alpha(0f)
                .setDuration(900)
                .withEndAction(() -> tapArea.removeView(tv))
                .start();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
