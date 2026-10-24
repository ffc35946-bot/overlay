package com.example.overlay;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(48, 96, 48, 48);

        Button start = new Button(this);
        start.setText("Iniciar overlay");
        start.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { startOverlay(); }
        });
        Button stop = new Button(this);
        stop.setText("Fechar overlay");
        stop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                stopService(new Intent(MainActivity.this, OverlayService.class));
            }
        });
        root.addView(start);
        root.addView(stop);
        setContentView(root);
    }

    private void startOverlay() {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
            return;
        }
        Intent i = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= 26) startForegroundService(i);
        else startService(i);
    }
}
