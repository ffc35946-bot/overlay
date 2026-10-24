package com.example.overlay;
import android.app.*; import android.content.Context; import android.content.Intent; import android.graphics.PixelFormat; import android.os.Build; import android.os.IBinder; import android.view.WindowManager; import java.util.List;
public class OverlayService extends Service {
    static { System.loadLibrary("native-lib"); }
    private WindowManager wm; private OverlayView view;
    @Override public void onCreate() {
        super.onCreate(); startForegroundCompat();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE); view = new OverlayView(this);
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
        wm.addView(view, lp); nativeStart();
    }
    public void updateEntities(List<Entity> entities) { view.setEntities(entities); }
    private native void nativeStart();
    private void startForegroundCompat() {
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { NotificationChannel ch = new NotificationChannel("ovl", "Overlay", NotificationManager.IMPORTANCE_LOW); ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch); b = new Notification.Builder(this, "ovl"); } else { b = new Notification.Builder(this); }
        b.setContentTitle("ESP & Aimbot Ativo").setSmallIcon(android.R.drawable.ic_menu_view); startForeground(1, b.build());
    }
    @Override public void onDestroy() { super.onDestroy(); if (view != null) wm.removeView(view); }
    @Override public IBinder onBind(Intent intent) { return null; }
}
