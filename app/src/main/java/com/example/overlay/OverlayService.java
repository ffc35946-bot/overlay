package com.example.overlay;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.util.ArrayList;
import java.util.List;

public class OverlayService extends Service {
    private WindowManager wm;
    private OverlayView view;
    private final Handler handler = new Handler();
    private boolean demoRunning = false;
    private float t = 0f;

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundCompat();

        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        view = new OverlayView(this);

        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        wm.addView(view, lp);

        demoRunning = true;
        handler.post(demoTick);
    }

    public void updateEntities(List<Entity> entities) {
        view.setEntities(entities);
    }

    private final Runnable demoTick = new Runnable() {
        @Override
        public void run() {
            if (!demoRunning) return;
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getRealMetrics(dm);
            int w = dm.widthPixels, h = dm.heightPixels;
            float[] vp = buildViewProj(w, h);

            t += 0.03f;
            List<Entity> list = new ArrayList<Entity>();
            for (int i = 0; i < 2; i++) {
                float wx = (i == 0 ? -2f : 2f) + (float) Math.sin(t + i) * 1.5f;
                float wz = 8f + i * 4f;
                float[] head  = OverlayView.nativeWorldToScreen(vp, wx, 0.2f,  wz, w, h);
                float[] chest = OverlayView.nativeWorldToScreen(vp, wx, -0.4f, wz, w, h);
                float[] hip   = OverlayView.nativeWorldToScreen(vp, wx, -0.9f, wz, w, h);
                float[] feet  = OverlayView.nativeWorldToScreen(vp, wx, -1.6f, wz, w, h);
                if (head == null || chest == null || hip == null || feet == null) continue;

                float bh = feet[1] - head[1];
                float bw = bh * 0.45f;
                float[] seg = new float[] {
                        head[0], head[1], chest[0], chest[1],
                        chest[0], chest[1], hip[0], hip[1],
                        hip[0], hip[1], feet[0], feet[1]
                };
                list.add(new Entity(head[0] - bw / 2f, head[1], bw, bh,
                        Color.RED, seg, Color.WHITE));
            }
            updateEntities(list);
            handler.postDelayed(this, 16);
        }
    };

    private static float[] buildViewProj(int w, int h) {
        float aspect = (float) w / (float) h;
        float f = (float) (1.0 / Math.tan(Math.toRadians(60) / 2.0));
        float n = 0.1f, fa = 200f;
        return new float[] {
                f / aspect, 0, 0, 0,
                0, f, 0, 0,
                0, 0, (fa + n) / (fa - n), -2f * fa * n / (fa - n),
                0, 0, 1, 0
        };
    }

    private void startForegroundCompat() {
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel("ovl", "Overlay",
                    NotificationManager.IMPORTANCE_LOW);
            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                    .createNotificationChannel(ch);
            b = new Notification.Builder(this, "ovl");
        } else {
            b = new Notification.Builder(this);
        }
        b.setContentTitle("Overlay ativo").setSmallIcon(android.R.drawable.ic_menu_view);
        startForeground(1, b.build());
    }

    @Override
    public void onDestroy() {
        demoRunning = false;
        handler.removeCallbacksAndMessages(null);
        if (view != null) wm.removeView(view);
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
}
