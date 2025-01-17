package nemosofts.online.live.utils;

import android.app.Activity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import nemosofts.online.live.callback.Callback;


public class IfSupported {

    private IfSupported() {
        throw new IllegalStateException("Utility class");
    }

    public static void IsRTL(Activity mContext) {
        try {
            if (Boolean.TRUE.equals(Callback.isRTL)) {
                Window window = mContext.getWindow();
                window.getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void IsScreenshot(Activity mContext) {
        try {
            if (Boolean.TRUE.equals(Callback.isScreenshot)) {
                Window window = mContext.getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void hideStatusBar(Activity mContext) {
        try {
            Window window = mContext.getWindow();
            View decorView = window.getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void keepScreenOn(Activity mContext) {
        try {
            Window window = mContext.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
