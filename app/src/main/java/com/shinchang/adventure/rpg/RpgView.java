package com.shinchang.adventure.rpg;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 아르카디아 지도와 전투 화면을 도트 그림으로 그린다. */
public class RpgView extends View {

    /** 화면 가로에 보이는 타일 수 (많을수록 도트가 작아 보인다) */
    private static final int VIEW_TILES = 14;
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
    /** 동료 둘이 차례로 따라온다: [0] = 바로 뒤, [1] = 그 뒤 */
    private final int[] followX = {-1, -1}, followY = {-1, -1}, followFromX = new int[2], followFromY = new int[2];
    private final RectF dst = new RectF();
    private int shownMap = -1;
    // 보스전 연출
    private long screenFlashUntil, shakeUntil;
    private int screenFlashColor = 0xFFFFFFFF;
    private boolean enraged;
    private final Paint overlayPaint = new Paint();

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
        followFromX[1] = followX[1] < 0 ? oldX : followX[1];
        followFromY[1] = followY[1] < 0 ? oldY : followY[1];
        followX[1] = followX[0] < 0 ? oldX : followX[0];
        followY[1] = followY[0] < 0 ? oldY : followY[0];
        followFromX[0] = followX[0] < 0 ? oldX : followX[0];
        followFromY[0] = followY[0] < 0 ? oldY : followY[0];
        followX[0] = oldX;
        followY[0] = oldY;
        stepStart = SystemClock.uptimeMillis();
        invalidate();
    }

    /** 지도를 옮겼을 때 동료를 주인공 위치로 */
    public void resetFollower() {
        for (int i = 0; i < 2; i++) {
            followX[i] = followFromX[i] = state.x;
            followY[i] = followFromY[i] = state.y;
        }
        fromX = state.x;
        fromY = state.y;
        stepStart = 0;
    }

    /** 보스 등장: 화면이 번쩍이고 흔들린다 */
    public void bossIntro() {
        enraged = false;
        screenFlash(0xFFFFFFFF, 700);
        shake(600);
    }

    /** 보스 격노: 붉게 물든다 */
    public void setEnraged(boolean on) {
        enraged = on;
        if (on) {
            screenFlash(0xFFFF2020, 500);
            shake(500);
        }
        invalidate();
    }

    /** 보스 격파: 하얗게 번쩍 */
    public void victoryFlash() {
        enraged = false;
        screenFlash(0xFFFFFFFF, 1200);
    }

    public void screenFlash(int color, long ms) {
        screenFlashColor = color;
        screenFlashUntil = SystemClock.uptimeMillis() + ms;
        invalidate();
    }

    public void shake(long ms) {
        shakeUntil = SystemClock.uptimeMillis() + ms;
        invalidate();
    }

    public void flash(int enemyIndex) {
        flashEnemy = enemyIndex;
        flashUntil = SystemClock.uptimeMillis() + 250;
        invalidate();
    }

    // ---- 도트 그림 캐시 ----

    /**
     * Scale2x(EPX): 도트를 두 배로 늘리면서 대각선 계단을 매끄럽게 다듬는다.
     * 한 번 돌리면 8x8 타일이 16x16, 두 번이면 32x32 해상도처럼 보인다.
     */
    static String[] scale2x(String[] src) {
        int h = src.length, w = src[0].length();
        char[][] out = new char[h * 2][w * 2];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                char p = src[y].charAt(x);
                char a = y > 0 ? src[y - 1].charAt(x) : p;
                char b = x < w - 1 ? src[y].charAt(x + 1) : p;
                char c = x > 0 ? src[y].charAt(x - 1) : p;
                char d = y < h - 1 ? src[y + 1].charAt(x) : p;
                out[2 * y][2 * x] = c == a && c != d && a != b ? a : p;
                out[2 * y][2 * x + 1] = a == b && a != c && b != d ? b : p;
                out[2 * y + 1][2 * x] = d == c && d != b && c != a ? c : p;
                out[2 * y + 1][2 * x + 1] = b == d && b != a && d != c ? d : p;
            }
        }
        String[] r = new String[h * 2];
        for (int y = 0; y < h * 2; y++) r[y] = new String(out[y]);
        return r;
    }

    /** 그림을 smooth 번 다듬은 뒤, 긴 변이 size 픽셀쯤 되도록 미리 그려 둔다 */
    private Bitmap bitmap(String key, String[] rows, int size, int smooth) {
        key = key + "@" + size + "/" + smooth;
        if (cache.size() > 400) cache.clear();
        Bitmap b = cache.get(key);
        if (b != null) return b;
        for (int i = 0; i < smooth; i++) rows = scale2x(rows);
        int h = rows.length, w = rows[0].length();
        int px = Math.max(1, Math.round(size / (float) Math.max(w, h)));
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

    /** 미리 그린 그림을 정확한 크기로 붙인다 (도트가 흐려지지 않게 최근접 확대) */
    private void blit(Canvas canvas, Bitmap b, float left, float top, float width, float height) {
        dst.set(left, top, left + width, top + height);
        canvas.drawBitmap(b, null, dst, pixelPaint);
    }

    /** 바탕이 투명한 물건 타일 */
    private static final String OBJECTS = "btoxFQYPANLZjeq";
    /** 물건 밑에 깔 수 있는 바닥 */
    private static final String GROUNDS = ";,:.f=r_an";
    private static final int[][] AROUND = {{-1, 0}, {1, 0}, {0, 1}, {0, -1}, {-1, 1}, {1, 1}, {-1, -1}, {1, -1}};

    private char groundAt(int tx, int ty) {
        for (int[] d : AROUND) {
            char c = state.tile(tx + d[0], ty + d[1]);
            if (GROUNDS.indexOf(c) >= 0) return c;
        }
        return ',';
    }

    private String tileKey(char t) {
        String theme = RpgData.theme(state.map);
        if (theme != null && ",#XCci".indexOf(t) >= 0) {
            String key = theme + t;
            if (RpgArt.tile(key) != null) return key;
        }
        if (t == '~' && (SystemClock.uptimeMillis() / 500) % 2 == 1) return "~1";
        // 등불을 받으면 해저 신전 입구에 돌계단이 떠오른다
        if (t == 'G' && state.step >= RpgData.STEP_LAMP) return "G1";
        // 결혼하면 흑염의 요새를 막던 검은 불길이 걷힌다
        if (t == 'J' && state.map == RpgData.WORLD4) return state.step >= RpgData.STEP_DAD_BACK ? "J1" : "J";
        if (t == 'J' && state.step >= RpgData.STEP_WEDDING) return "J1";
        if (t == 'l' && (SystemClock.uptimeMillis() / 400) % 2 == 1) return "l1";
        return String.valueOf(t);
    }

    private void drawTile(Canvas canvas, String key, float x, float y, int ts) {
        String[] art = RpgArt.tile(key);
        if (art == null) {
            key = ".";
            art = RpgArt.tile(key);
        }
        blit(canvas, bitmap("t" + key, art, ts, 1), x, y, ts, ts);
    }

    // ---- 그리기 ----

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (state == null) return;
        long now = SystemClock.uptimeMillis();
        boolean shaking = now < shakeUntil;
        if (shaking) {
            canvas.save();
            float d = getWidth() / 60f;
            canvas.translate((float) Math.sin(now / 18.0) * d, (float) Math.cos(now / 23.0) * d * 0.6f);
        }
        if (battle != null) {
            drawBattle(canvas);
        } else {
            drawMap(canvas);
        }
        if (shaking) canvas.restore();
        if (battle != null && enraged) {
            // 격노: 붉은 기운이 맥박처럼
            int a = (int) (40 + 30 * Math.sin(now / 200.0));
            overlayPaint.setColor((a << 24) | 0xC00000);
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
        }
        if (now < screenFlashUntil) {
            float left = (screenFlashUntil - now) / 700f;
            int a = (int) (Math.min(1f, left) * 200);
            overlayPaint.setColor((a << 24) | (screenFlashColor & 0xFFFFFF));
            canvas.drawRect(0, 0, getWidth(), getHeight(), overlayPaint);
        }
        if (shaking || now < screenFlashUntil || (battle != null && enraged)) postInvalidateDelayed(16);
    }

    private void drawMap(Canvas canvas) {
        if (shownMap != state.map) {
            shownMap = state.map;
            resetFollower();
        }
        int ts = Math.max(16, getWidth() / VIEW_TILES);
        long now = SystemClock.uptimeMillis();
        float t = stepStart == 0 ? 1f : Math.min(1f, (now - stepStart) / (float) STEP_MS);
        float px = fromX + (state.x - fromX) * t;
        float py = fromY + (state.y - fromY) * t;
        float camX = px * ts + ts / 2f - getWidth() / 2f;
        float camY = py * ts + ts / 2f - getHeight() / 2f;

        canvas.drawColor(0xFF000000);
        int x0 = (int) Math.floor(camX / ts) - 1, y0 = (int) Math.floor(camY / ts) - 1;
        int x1 = x0 + getWidth() / ts + 3, y1 = y0 + getHeight() / ts + 3;
        boolean world = RpgData.isOverworld(state.map);
        for (int ty = y0; ty <= y1; ty++) {
            for (int tx = x0; tx <= x1; tx++) {
                boolean outside = tx < 0 || ty < 0 || tx >= state.mapWidth() || ty >= state.mapHeight();
                char ch = outside ? (world ? '~' : ' ') : state.tile(tx, ty);
                if (ch == ' ') continue;
                if (OBJECTS.indexOf(ch) >= 0) {
                    // 가구·물건은 바탕이 투명하니 주변 바닥을 먼저 깐다
                    drawTile(canvas, tileKey(groundAt(tx, ty)), tx * ts - camX, ty * ts - camY, ts);
                }
                drawTile(canvas, tileKey(ch), tx * ts - camX, ty * ts - camY, ts);
            }
        }

        for (RpgData.Npc n : state.visibleNpcs()) {
            drawChar(canvas, n.sprite, n.x * ts - camX, n.y * ts - camY, ts, false);
        }
        // 동료들은 주인공 뒤를 차례로 따라온다 (맨 뒤부터 그린다)
        for (int i = Math.min(2, state.party.size() - 1); i >= 1; i--) {
            float fx = followFromX[i - 1] + (followX[i - 1] - followFromX[i - 1]) * t;
            float fy = followFromY[i - 1] + (followY[i - 1] - followFromY[i - 1]) * t;
            if (Math.round(fx) != state.x || Math.round(fy) != state.y || t < 1) {
                drawChar(canvas, spriteOf(state.party.get(i)), fx * ts - camX, fy * ts - camY, ts, t < 1);
            }
        }
        Hero leader = state.party.get(0);
        String leaderSprite = spriteOf(leader);
        drawChar(canvas, leaderSprite, px * ts - camX, py * ts - camY, ts, t < 1);

        if (t < 1 || world || "lava".equals(RpgData.theme(state.map))) postInvalidateDelayed(t < 1 ? 16 : 250);
    }

    private static String spriteOf(Hero h) {
        return h.id == 0 && h.isHero() ? "hero2" : h.base().sprite;
    }

    private void drawChar(Canvas canvas, String key, float x, float y, int ts, boolean walking) {
        String[] art = RpgArt.character(key);
        Bitmap b = bitmap("c" + key, art, ts, 1);
        // 걷는 동안 살짝 통통 튄다
        float bob = walking && (SystemClock.uptimeMillis() / 70) % 2 == 0 ? -ts / 16f : 0;
        float h = ts, w = ts * art[0].length() / (float) art.length;
        blit(canvas, b, x + (ts - w) / 2f, y + bob, w, h);
    }

    private void drawBattle(Canvas canvas) {
        int w = getWidth(), h = getHeight();
        String theme = RpgData.theme(state.map);
        boolean cave = theme != null;
        int top, bottom;
        if ("cave".equals(theme)) {
            top = 0xFF1B1410;
            bottom = 0xFF3A2C22;
        } else if ("sea".equals(theme)) {
            top = 0xFF0B1A2E;
            bottom = 0xFF1F4A6E;
        } else if ("tmp".equals(theme)) {
            top = 0xFF0E2E33;
            bottom = 0xFF2F7F86;
        } else if (battle.firstMonster == RpgData.VARGAS0 || battle.firstMonster == RpgData.HOSTAGE) {
            top = 0xFF120A14;
            bottom = 0xFF3A0E0E;
        } else if ("lava".equals(theme)) {
            top = 0xFF1A0804;
            bottom = 0xFF7A2A0A;
        } else if ("ruin".equals(theme)) {
            top = 0xFF5A4A2A;
            bottom = 0xFFC9A86A;
        } else if ("ice".equals(theme)) {
            top = 0xFF0E2A44;
            bottom = 0xFF9FC6E8;
        } else if (battle.firstMonster == RpgData.BEELZEBUB) {
            top = battle.blessing >= 3 ? 0xFFFFF3C0 : 0xFF0A0A0A;
            bottom = battle.blessing >= 3 ? 0xFFE0B040 : 0xFF2A1A3A;
        } else if ("gold".equals(theme)) {
            top = 0xFF3A2A08;
            bottom = 0xFFC9962A;
        } else if ("tower".equals(theme)) {
            top = 0xFF101828;
            bottom = 0xFF5B6168;
        } else if ("holy".equals(theme)) {
            top = 0xFF1A1020;
            bottom = 0xFF7B4BB5;
        } else if (state.map == RpgData.WORLD5 || state.map == RpgData.LUMEN) {
            top = 0xFFF5E3A0;
            bottom = 0xFF4CAF50;
        } else if ("moon".equals(theme)) {
            top = 0xFF0A1430;
            bottom = 0xFF2E5A5A;
        } else if ("demon".equals(theme)) {
            top = 0xFF1A0610;
            bottom = 0xFF6E1B3A;
        } else if (state.map == RpgData.LAKE) {
            top = 0xFFB8C4CC;
            bottom = 0xFF4A6A5A;
        } else if ("fort".equals(theme)) {
            top = 0xFF0C0612;
            bottom = 0xFF3B1640;
        } else if (state.map == RpgData.WORLD3 || state.map == RpgData.IGNA) {
            top = 0xFFE07A3A;
            bottom = 0xFF5A4A44;
        } else if (state.map == RpgData.WORLD2) {
            top = 0xFF7EC8F0;
            bottom = 0xFFE3C88F;
        } else {
            top = 0xFF6FA8DC;
            bottom = 0xFF2E7D32;
        }
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
            float size = battle.boss ? w * 0.62f : Math.min(slot * 0.9f, w * 0.32f);
            Bitmap b = bitmap("m" + e.m.sprite, art, (int) size, 2);
            float bw = size, bh = size * art.length / (float) art[0].length();
            float cx = battle.boss ? w / 2f : slot * (i + 1);
            // 진귀한 몬스터는 반짝반짝
            if (e.m.rare != 0 && (now / 150) % 4 == 0) bgPaint.setColor(0x66FFF3A0);
            else bgPaint.setColor(0);
            if (e.m.rare != 0) canvas.drawCircle(cx, h * 0.74f - bh / 2, bw * 0.6f, bgPaint);
            blit(canvas, b, cx - bw / 2f, h * 0.74f - bh, bw, bh);
        }
        boolean rare = false;
        for (Battle.Enemy e : enemies) if (e.alive() && e.m.rare != 0) rare = true;
        if (now < flashUntil || rare) postInvalidateDelayed(30);
    }
}
