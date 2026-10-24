package com.example.overlay;
import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import java.util.List;

public class OverlayService extends Service {
    static { System.loadLibrary("native-lib"); }
    
    private WindowManager wm;
    private OverlayView espView;
    private LinearLayout menuLayout;
    private WindowManager.LayoutParams menuParams;
    
    // Variáveis para arrastar o painel
    private float initialX, initialY;
    private int initialTouchX, initialTouchY;

    @Override public void onCreate() {
        super.onCreate(); 
        startForegroundCompat();
        wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        // 1. Configura a View do ESP (Tela cheia transparente)
        espView = new OverlayView(this);
        int type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE;
        WindowManager.LayoutParams espParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT);
        wm.addView(espView, espParams);

        // 2. Configura o Menu Flutuante (Painel)
        menuLayout = new LinearLayout(this);
        menuLayout.setOrientation(LinearLayout.VERTICAL);
        menuLayout.setPadding(40, 40, 40, 40);
        
        // Fundo escuro arredondado com borda azul
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1E1E2E"));
        bg.setCornerRadius(25);
        bg.setStroke(3, Color.parseColor("#4D6BFE"));
        menuLayout.setBackground(bg);

        // Título "DEEP PANEL" em Azul
        TextView title = new TextView(this);
        title.setText("DEEP PANEL");
        title.setTextColor(Color.parseColor("#4D6BFE"));
        title.setTextSize(20);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, 0, 0, 20);
        menuLayout.addView(title);

        // Linha Divisória
        View divider = new View(this);
        divider.setBackgroundColor(Color.parseColor("#4D6BFE"));
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 3);
        divParams.setMargins(0, 0, 0, 25);
        divider.setLayoutParams(divParams);
        menuLayout.addView(divider);

        // Botões (Switches)
        Switch swAimbot = new Switch(this);
        swAimbot.setText("Aimbot (Cabeça)");
        swAimbot.setTextColor(Color.WHITE);
        swAimbot.setPadding(0, 10, 0, 10);
        swAimbot.setOnCheckedChangeListener((buttonView, isChecked) -> setAimbot(isChecked));

        Switch swEsp = new Switch(this);
        swEsp.setText("ESP Box + Skeleton");
        swEsp.setTextColor(Color.WHITE);
        swEsp.setPadding(0, 10, 0, 10);
        swEsp.setOnCheckedChangeListener((buttonView, isChecked) -> setEsp(isChecked));

        menuLayout.addView(swAimbot);
        menuLayout.addView(swEsp);

        // Configuração da janela do painel
        menuParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        menuParams.gravity = Gravity.TOP | Gravity.START;
        menuParams.x = 100; menuParams.y = 100;

        // Lógica para arrastar o Painel
        menuLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = menuParams.x;
                        initialY = menuParams.y;
                        initialTouchX = (int) event.getRawX();
                        initialTouchY = (int) event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        menuParams.x = (int) (initialX + (event.getRawX() - initialTouchX));
                        menuParams.y = (int) (initialY + (event.getRawY() - initialTouchY));
                        wm.updateViewLayout(menuLayout, menuParams);
                        return true;
                }
                return false;
            }
        });
        
        wm.addView(menuLayout, menuParams);
        nativeStart();
    }

    public void updateEntities(List<Entity> entities) { espView.setEntities(entities); }
    
    private native void nativeStart();
    public native void setAimbot(boolean state);
    public native void setEsp(boolean state);

    private void startForegroundCompat() {
        Notification.Builder b;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { NotificationChannel ch = new NotificationChannel("ovl", "Overlay", NotificationManager.IMPORTANCE_LOW); ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(ch); b = new Notification.Builder(this, "ovl"); } else { b = new Notification.Builder(this); }
        b.setContentTitle("Deep Panel Ativo").setSmallIcon(android.R.drawable.ic_menu_view); startForeground(1, b.build());
    }

    @Override public void onDestroy() { 
        super.onDestroy(); 
        if (espView != null) wm.removeView(espView); 
        if (menuLayout != null) wm.removeView(menuLayout);
    }
    @Override public IBinder onBind(Intent intent) { return null; }
}
