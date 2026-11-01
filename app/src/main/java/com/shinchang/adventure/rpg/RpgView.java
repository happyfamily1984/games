package com.shinchang.adventure.rpg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 아르카디아 지도와 전투 화면을 도트 그림으로 그린다. */
public class RpgView extends View {

    private static final int VIEW_TILES = 11;
    private static final long STEP_MS = 140;

    private RpgState state;
    private Battle battle;
    private final Map<String, Bitmap> cache = new HashMap<>();
    private final Paint pixelPaint = new Paint();
    private final Paint bgPaint = new Paint();
    private final Paint flashPaint = new Paint();

    // 걷기 애니메이션과 동료가 따라오는 위치
    private int fromX, fromY;
    private long stepStart;
    private int followX = -1, followY = -1, followFromX, followFromY;
    private int shownMap = -1;
    /** 맞은 적 깜빡임 */
    private int flashEnemy = -1;
    private long flashUntil;

    public RpgView(Context context) {
        super(context);
        pixelPaint.setAntiAlias(false);
        pixelPaint.setFilterBitmap(false);
        flashPaint.setColor(0x88FFFFFF);
    }

    public void setState(RpgState state) {
        this.state = state;
        shownMap = -1;
        invalidate();
    }

    public void setBattle(Battle battle) {
        this.battle = battle;
        invalidate();
    }

    /** 한 칸 이동했을 때 부드럽게 미끄러지도록 */
    public void stepped(int oldX, int oldY) {
        fromX = oldX;
        fromY = oldY;
        followFromX = followX < 0 ? oldX : followX;
        followFromY = followY < 0 ? oldY : followY;
        followX = oldX;
        followY = oldY;
        stepStart = SystemClock.uptimeMillis();
        invalidate();
    }

    /** 지도를 옮겼을 때 동료를 주인공 위치로 */
    public void resetFollower() {
        followX = followFromX = state.x;
        followY = followFromY = state.y;
        fromX = state.x;
        fromY = state.y;
        stepStart = 0;
    }

    public void flash(int enemyIndex) {
        flashEnemy = enemyIndex;
        flashUntil = SystemClock.uptimeMillis() + 250;
        invalidate();
    }

    // ---- 도트 그림 캐시 ----

    private Bitmap bitmap(String key, String[] rows, int size) {
        key = key + "@" + size;
        if (cache.size() > 300) cache.clear();
        Bitmap b = cache.get(key);
        if (b != null) return b;
        int h = rows.length, w = rows[0].length();
        int px = Math.max(1, size / Math.max(w, h));
        b = Bitmap.createBitmap(w * px, h * px, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        Paint p = new Paint();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                char ch = rows[y].charAt(x);
                if (ch == '.') continue;
                p.setColor(RpgArt.color(ch));
                c.drawRect(x * px, y * px, (x + 1) * px, (y + 1) * px, p);
            }
        }
        cache.put(key, b);
        return b;
    }

    /** 바탕이 투명한 물건 타일 */
    private static final String OBJECTS = "btoxFQYPANLZje";
    /** 물건 밑에 깔 수 있는 바닥 */
    private static final String GROUNDS = ";,:.f=r";
    private static final int[][] AROUND = {{-1, 0}, {1, 0}, {0, 1}, {0, -1}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}};

    private char groundAt(int tx, int ty) {
        for (int[] d : AROUND) {
            char c = state.tile(tx + d[0], ty + d[1]);
            if (GROUNDS.indexOf(c) >= 0) return c;
        }
        return ',';
    }

    private String tileKey(char t) {
        boolean cave = state.map == RpgData.CAVE1 || state.map == RpgData.CAVE2;
        if (cave && (t == ',' || t == '#' || t == 'X' || t == 'C' || t == 'c')) return "cave" + t;
        if (t == '~' && (SystemClock.uptimeMillis() / 500) % 2 == 1) return "~1";
        return String.valueOf(t);
    }

    // ---- 그리기 ----

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (state == null) return;
        if (battle != null) {
            drawBattle(canvas);
        } else {
            drawMap(canvas);
        }
    }

    private void drawMap(Canvas canvas) {
        if (shownMap != state.map) {
            shownMap = state.map;
            resetFollower();
        }
        int ts = Math.max(8, getWidth() / VIEW_TILES / 8 * 8);
        long now = SystemClock.uptimeMillis();
        float t = stepStart == 0 ? 1f : Math.min(1f, (now - stepStart) / (float) STEP_MS);
        float px = fromX + (state.x - fromX) * t;
        float py = fromY + (state.y - fromY) * t;
        float camX = px * ts + ts / 2f - getWidth() / 2f;
        float camY = py * ts + ts / 2f - getHeight() / 2f;

        canvas.drawColor(0xFF000000);
        int x0 = (int) Math.floor(camX / ts) - 1, y0 = (int) Math.floor(camY / ts) - 1;
        int x1 = x0 + getWidth() / ts + 3, y1 = y0 + getHeight() / ts + 3;
        boolean world = state.map == RpgData.WORLD;
        for (int ty = y0; ty <= y1; ty++) {
            for (int tx = x0; tx <= x1; tx++) {
                boolean outside = tx < 0 || ty < 0 || tx >= state.mapWidth() || ty >= state.mapHeight();
                char ch = outside ? (world ? '~' : ' ') : state.tile(tx, ty);
                if (ch == ' ') continue;
                if (OBJECTS.indexOf(ch) >= 0) {
                    // 가구·물건은 바탕이 투명하니 주변 바닥을 먼저 깐다
                    char g = groundAt(tx, ty);
                    canvas.drawBitmap(bitmap("t" + tileKey(g), RpgArt.tile(tileKey(g)), ts), tx * ts - camX, ty * ts - camY, pixelPaint);
                }
                String[] art = RpgArt.tile(tileKey(ch));
                if (art == null) art = RpgArt.tile(".");
                canvas.drawBitmap(bitmap("t" + tileKey(ch), art, ts), tx * ts - camX, ty * ts - camY, pixelPaint);
            }
        }

        for (RpgData.Npc n : state.visibleNpcs()) {
            drawChar(canvas, n.sprite, n.x * ts - camX, n.y * ts - camY, ts, false);
        }
        // 동료(리나)는 주인공 뒤를 따라온다
        if (state.party.size() > 1) {
            float fx = followFromX + (followX - followFromX) * t;
            float fy = followFromY + (followY - followFromY) * t;
            if (Math.round(fx) != state.x || Math.round(fy) != state.y || t < 1) {
                drawChar(canvas, state.party.get(1).base().sprite, fx * ts - camX, fy * ts - camY, ts, t < 1);
            }
        }
        drawChar(canvas, "hero", px * ts - camX, py * ts - camY, ts, t < 1);

        if (t < 1 || world) postInvalidateDelayed(t < 1 ? 16 : 250);
    }

    private void drawChar(Canvas canvas, String key, float x, float y, int ts, boolean walking) {
        String[] art = RpgArt.character(key);
        Bitmap b = bitmap("c" + key, art, ts);
        // 걷는 동안 살짝 통통 튄다
        float bob = walking && (SystemClock.uptimeMillis() / 70) % 2 == 0 ? -ts / 16f : 0;
        canvas.drawBitmap(b, x + (ts - b.getWidth()) / 2f, y + (ts - b.getHeight()) + bob, pixelPaint);
    }

    private void drawBattle(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        boolean cave = state.map == RpgData.CAVE1 || state.map == RpgData.CAVE2;
        int top = cave ? 0xFF1B1410 : 0xFF6FA8DC;
        int bottom = cave ? 0xFF3A2C22 : 0xFF2E7D32;
        bgPaint.setShader(new LinearGradient(0, 0, 0, h, top, bottom, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bgPaint);
        bgPaint.setShader(null);
        bgPaint.setColor(cave ? 0x55000000 : 0x33000000);
        canvas.drawOval(w * 0.08f, h * 0.62f, w * 0.92f, h * 0.84f, bgPaint);

        List<Battle.Enemy> enemies = battle.enemies;
        int n = enemies.size();
        int slot = w / (n + 1);
        long now = SystemClock.uptimeMillis();
        for (int i = 0; i < n; i++) {
            Battle.Enemy e = enemies.get(i);
            if (!e.alive()) continue;
            if (i == flashEnemy && now < flashUntil && (now / 60) % 2 == 0) continue;
            String[] art = RpgArt.monster(e.m.sprite);
            int size = battle.boss ? (int) (w * 0.6f) : Math.min((int) (slot * 0.9f), (int) (w * 0.32f));
            size = Math.max(16, size / art[0].length() * art[0].length());
            Bitmap b = bitmap("m" + e.m.sprite + size, art, size);
            float cx = battle.boss ? w / 2f : slot * (i + 1);
            canvas.drawBitmap(b, cx - b.getWidth() / 2f, h * 0.74f - b.getHeight(), pixelPaint);
        }
        if (now < flashUntil) postInvalidateDelayed(30);
    }
}
