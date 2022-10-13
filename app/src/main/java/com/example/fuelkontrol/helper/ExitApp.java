package com.example.fuelkontrol.helper;

import android.app.Activity;
import android.widget.Toast;

import androidx.annotation.StringRes;

public class ExitApp {private static long lastClickTime;

    public static void now(Activity ctx, @StringRes int message) {
        now(ctx, ctx.getString(message), 2500);
    }

    public static void now(Activity ctx, @StringRes int message, long time) {
        now(ctx, ctx.getString(message), time);
    }

    public static void now(Activity ctx, String message, long time) {
        if (ctx != null && !message.isEmpty() && time != 0) {
            if (lastClickTime + time > System.currentTimeMillis()) {
                ctx.finish();
            } else {
                Toast.makeText(ctx, message, Toast.LENGTH_SHORT).show();
                lastClickTime = System.currentTimeMillis();
            }
        }
    }

}