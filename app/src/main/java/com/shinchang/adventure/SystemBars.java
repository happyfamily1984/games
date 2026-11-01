package com.shinchang.adventure;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;

/**
 * 안드로이드 15부터는 앱이 상단바·하단바 아래까지 그려진다.
 * 화면 내용이 바에 가려지지 않도록 바 크기만큼 안쪽 여백을 준다.
 */
public final class SystemBars {

    public static void avoid(Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        content.setOnApplyWindowInsetsListener((v, insets) -> {
            int l, t, r, b;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets i = insets.getInsets(
                        WindowInsets.Type.systemBars() | WindowInsets.Type.displayCutout());
                l = i.left;
                t = i.top;
                r = i.right;
                b = i.bottom;
            } else {
                l = insets.getSystemWindowInsetLeft();
                t = insets.getSystemWindowInsetTop();
                r = insets.getSystemWindowInsetRight();
                b = insets.getSystemWindowInsetBottom();
            }
            v.setPadding(l, t, r, b);
            return insets;
        });
        content.requestApplyInsets();
    }

    private SystemBars() {
    }
}
