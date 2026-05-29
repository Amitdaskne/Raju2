package com.bgmi;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bgmi.utils.Prefs;
import com.zcore.ZCoreCore;
import com.zcore.entity.pm.InstallResult;
import org.lsposed.lsparanoid.Obfuscate;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

@Obfuscate
public class MAct extends AppCompatActivity {
    
    private static final String TAG = "MAct";
    
    static {
        try {
            System.loadLibrary("zenin");
        } catch (UnsatisfiedLinkError ignored) {}
    }

    private static final String PKG_BGMI = "com.pubg.imobile";
    private static final int USER_ID = 0;
    private boolean isObbCopied = false;
    
    public static native String exdate();

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setStatusBarColor(Color.BLACK);

        TextView deviceTv = findViewById(R.id.deviceName);
        if (deviceTv != null) {
            deviceTv.setText(Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL);
        }

        TextView expiryDateTv = findViewById(R.id.expiryDateTv);
        if (expiryDateTv != null) {
            String expiry = exdate();
            expiryDateTv.setText(expiry);
            Log.d(TAG, "Expiry date: " + expiry);
        }

        doCountTimerAccout();

        Button launchBtn = findViewById(R.id.installGame);
        launchBtn.setOnClickListener(v -> {
            try {
                if (ZCoreCore.get() != null) {
                    boolean isInstalled = ZCoreCore.get().isInstalled(PKG_BGMI, USER_ID);
                    if (!isInstalled) {
                        InstallResult result = ZCoreCore.get().installPackageAsUser(PKG_BGMI, USER_ID);
                        if (result.success) {
                            Toast.makeText(this, "Game Ready. Launching...", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Setup Error: " + result.msg, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    if (!isObbCopied) {
                        copyObbFiles();
                    } else {
                        launchGame();
                    }
                }
            } catch (Throwable e) {
                Log.e(TAG, "Launch error", e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private void doCountTimerAccout() {
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    String expiryStr = exdate();
                    Log.d(TAG, "Timer check - Expiry: " + expiryStr);
                    
                    if (expiryStr == null || expiryStr.equals("NULL") || expiryStr.equals("Expired")) {
                        handleExpiredSession();
                        return;
                    }
                    
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    Date expiryDate = dateFormat.parse(expiryStr);
                    
                    if (expiryDate == null) {
                        handleExpiredSession();
                        return;
                    }
                    
                    long now = System.currentTimeMillis();
                    long distance = expiryDate.getTime() - now;

                    if (distance <= 0) {
                        handleExpiredSession();
                        return;
                    }
    
                    long d = distance / (24 * 60 * 60 * 1000);
                    long h = (distance / (60 * 60 * 1000)) % 24;
                    long m = (distance / (60 * 1000)) % 60;
                    long s = (distance / 1000) % 60;

                    ((TextView) findViewById(R.id.tvD)).setText(d + "d");
                    ((TextView) findViewById(R.id.tvH)).setText(h + "h");
                    ((TextView) findViewById(R.id.tvM)).setText(m + "m");
                    ((TextView) findViewById(R.id.tvS)).setText(s + "s");

                    handler.postDelayed(this, 1000);
                } catch (Exception e) {
                    Log.e(TAG, "Timer error", e);
                    handler.postDelayed(this, 5000);
                }
            }
        };
        handler.post(runnable);
    }
     
    private void handleExpiredSession() {
        new Prefs(MAct.this).setSt("USER", "");
        Toast.makeText(MAct.this, "License Expired!", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(MAct.this, LogAct.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void copyObbFiles() {
        Toast.makeText(this, "Finalizing files...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(PKG_BGMI, 0);
                String gameObb = "main." + info.versionCode + "." + info.packageName + ".obb";
                File obbSource = new File(Environment.getExternalStorageDirectory(), "Android/obb/" + PKG_BGMI + "/" + gameObb);
                File obbDestDir = new File(getExternalFilesDir(null), "Android/obb/" + PKG_BGMI);
                
                if (!obbDestDir.exists()) {
                    obbDestDir.mkdirs();
                }
                File obbDestFile = new File(obbDestDir, gameObb);

                if (obbDestFile.exists()) {
                    isObbCopied = true;
                    runOnUiThread(this::launchGame);
                    return;
                }

                if (!obbSource.exists()) {
                    runOnUiThread(() -> Toast.makeText(this, "Original OBB not found!", Toast.LENGTH_LONG).show());
                    return;
                }

                java.io.FileInputStream fis = new java.io.FileInputStream(obbSource);
                java.io.FileOutputStream fos = new java.io.FileOutputStream(obbDestFile);
                byte[] buf = new byte[8192];
                int len;
                while ((len = fis.read(buf)) != -1) {
                    fos.write(buf, 0, len);
                }
                fis.close();
                fos.close();
                isObbCopied = true;
                runOnUiThread(this::launchGame);
            } catch (Exception e) {
                Log.e(TAG, "OBB copy error", e);
                runOnUiThread(() -> Toast.makeText(this, "File error: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void launchGame() {
        try {
            ZCoreCore.get().launchApk(PKG_BGMI, USER_ID);
        } catch (Exception e) {
            Log.e(TAG, "Launch failed", e);
            Toast.makeText(this, "Launch error", Toast.LENGTH_SHORT).show();
        }
    }
}