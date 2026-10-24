package com.example.overlay;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import java.util.List;

public class OverlayService extends Service {
    static { System.loadLibrary("native-lib"); }
    
    private WindowManager wm;
    private OverlayView espView;
    private LinearLayout menuLayout;
    private WindowManager.LayoutParams menuParams;
    private WindowManager.LayoutParams espParams;

    @Override public void onCreate() {
        super.onCreate(); 
        startForegroundCompat();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // 1. Configura a View do ESP (Tela cheia transparente)
        espView = new OverlayView(this);
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        espParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
        wm.addView(espView, espParams);

        // 2. Configura o Menu Flutuante (Botões)
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setPadding(30, 30, 30, 30);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1E1E2E"));
        bg.setCornerRadius(20);
        bg.setStroke(3, Color.parseColor("#4D6BFE"));
        menuLayout.setBackground(bg);

        Switch swAimbot = new Switch(this);
        swAimbot.setText("Aimbot");
        swAimbot.setTextColor(Color.WHITE);
        swAimbot.setOnCheckedChangeListener((buttonView, isChecked) -> setAimbot(isChecked));

        Switch swEsp = new Switch(this);
        swEsp.setText("ESP Box");
        swEsp.setTextColor(Color.WHITE);
        swEsp.setOnCheckedChangeListener((buttonView, isChecked) -> setEsp(isChecked));

        menuLayout.addView(swAimbot);
        menuLayout.addView(swEsp);

        menuParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        menuParams.gravity = Gravity.TOP | Gravity.START;
        menuParams.x = 100; menuParams.y = 100;
        
        wm.addView(menuLayout, menuParams);
        nativeStart();
    }

    public void updateEntities(List<Entity> entities) { espView.setEntities(entities); }
    
    // Funções nativas C++
    private native void nativeStart();
    public native void setAimbot(boolean state);
    public native void setEsp(boolean state);

    private void startForegroundCompat() {
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { NotificationChannel ch = new NotificationChannel("ovl", "Overlay", NotificationManager.IMPORTANCE_LOW); ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch); b = new Notification.Builder(this, "ovl"); } else { b = new Notification.Builder(this); }
        b.setContentTitle("Painel Ativo").setSmallIcon(android.R.drawable.ic_menu_view); startForeground(1, b.build());
    }

    @Override public void onDestroy() { 
        super.onDestroy(); 
        if (espView != null) wm.removeView(espView); 
        if (menuLayout != null) wm.removeView(menuLayout);
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
