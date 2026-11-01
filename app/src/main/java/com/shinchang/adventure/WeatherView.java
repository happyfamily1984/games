package com.shinchang.adventure;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.View;

import com.shinchang.adventure.game.GameData;

import java.util.Random;

/** 화면 위에 비/눈을 그리는 투명 오버레이. 터치는 아래 화면으로 그대로 넘어간다. */
public class WeatherView extends View {

    private static final int DROPS = 90;
    private static final int FLAKES = 70;
    /** 종교계: 금빛 반짝임이 천천히 떠오른다 */
    public static final int HOLY = 3;
    private static final int SPARKS = 45;

    private final Random random = new Random();
    private final Paint rainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint snowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint tintPaint = new Paint();
    private final Paint holyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final float[] x = new float[Math.max(DROPS, FLAKES)];
    private final float[] y = new float[x.length];
    private final float[] speed = new float[x.length];
    private final float[] size = new float[x.length];
    private int weather = GameData.CLEAR;
    private long lastFrame;

    public WeatherView(Context context, AttributeSet attrs) {
        super(context, attrs);
        float d = getResources().getDisplayMetrics().density;
        rainPaint.setColor(0x996FA8DC);
        rainPaint.setStrokeWidth(2 * d);
        rainPaint.setStrokeCap(Paint.Cap.ROUND);
        snowPaint.setColor(0xEEFFFFFF);
        holyPaint.setColor(0xCCF5C242);
        setClickable(false);
        setFocusable(false);
    }

    public void setWeather(int w) {
        if (w == weather) return;
        weather = w;
        for (int i = 0; i < x.length; i++) respawn(i, true);
        lastFrame = SystemClock.uptimeMillis();
        tintPaint.setColor(w == GameData.RAIN ? 0x2A1E3A5F : w == GameData.SNOW ? 0x22B8D4F0
                : w == HOLY ? 0x14FFF3C4 : 0);
        invalidate();
    }

    private void respawn(int i, boolean anywhere) {
        float d = getResources().getDisplayMetrics().density;
        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());
        x[i] = random.nextFloat() * w;
        y[i] = anywhere ? random.nextFloat() * h : -random.nextFloat() * 40 * d;
        if (weather == HOLY) {
            // 아래에서 위로
            y[i] = anywhere ? random.nextFloat() * h : h + random.nextFloat() * 40 * d;
            speed[i] = -(15 + random.nextFloat() * 30) * d;
            size[i] = (1.5f + random.nextFloat() * 3) * d;
        } else if (weather == GameData.RAIN) {
            speed[i] = (900 + random.nextFloat() * 600) * d / 3;
            size[i] = (14 + random.nextFloat() * 12) * d;
        } else {
            speed[i] = (40 + random.nextFloat() * 50) * d;
            size[i] = (2 + random.nextFloat() * 3) * d;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        for (int i = 0; i < x.length; i++) respawn(i, true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (weather == GameData.CLEAR) return;

        long now = SystemClock.uptimeMillis();
        float dt = Math.min(0.05f, (now - lastFrame) / 1000f);
        lastFrame = now;
        canvas.drawRect(0, 0, getWidth(), getHeight(), tintPaint);

        float t = now / 1000f;
        int count = weather == GameData.RAIN ? DROPS : weather == HOLY ? SPARKS : FLAKES;
        for (int i = 0; i < count; i++) {
            y[i] += speed[i] * dt;
            if (weather == HOLY) {
                float twinkle = 0.5f + 0.5f * (float) Math.sin(t * 3 + i * 1.7f);
                holyPaint.setAlpha((int) (80 + 150 * twinkle));
                float r = size[i] * (0.6f + 0.6f * twinkle);
                // 십자 반짝임
                canvas.drawCircle(x[i], y[i], r * 0.6f, holyPaint);
                canvas.drawRect(x[i] - r * 2, y[i] - r * 0.2f, x[i] + r * 2, y[i] + r * 0.2f, holyPaint);
                canvas.drawRect(x[i] - r * 0.2f, y[i] - r * 2, x[i] + r * 0.2f, y[i] + r * 2, holyPaint);
                if (y[i] < -size[i] * 3) respawn(i, false);
                continue;
            }
            if (weather == GameData.RAIN) {
                x[i] -= speed[i] * 0.2f * dt;
                canvas.drawLine(x[i], y[i], x[i] + size[i] * 0.2f, y[i] - size[i], rainPaint);
            } else {
                // 눈은 좌우로 살랑살랑
                float sway = (float) Math.sin(t * 1.5f + i) * size[i] * 4;
                canvas.drawCircle(x[i] + sway, y[i], size[i], snowPaint);
            }
            if (y[i] - size[i] > getHeight() || x[i] < -size[i] * 2) respawn(i, false);
        }
        postInvalidateOnAnimation();
    }
}
