package com.bgmi;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
import android.Manifest;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bgmi.utils.Downtwo;
import com.bgmi.utils.Prefs;

import org.lsposed.lsparanoid.Obfuscate;

@Obfuscate
public class LogAct extends AppCompatActivity {
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
    static {
        try {
            System.loadLibrary("zenin");
        } catch (UnsatisfiedLinkError ignored) {
        }
    }

    private static final String TAG = "LogAct";
    private Prefs prefs;
    private final String USER = "USER";

    private EditText textUsername;
    private Button btnLogin;
    private View pasteBtn;
    private TextView getKey;

    private Dialog loadingDialog;

    private static final int REQUEST_MANAGE_STORAGE_PERMISSION = 100;
    private static final int REQUEST_MANAGE_UNKNOWN_APP_SOURCES = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Dark Status Bar
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);

        prefs = new Prefs(this);
        String savedKey = prefs.getSt(USER, "");

        Log.d(TAG, "onCreate - Saved key exists: " + !savedKey.isEmpty());

        // Automatic login if key exists
        if (!savedKey.isEmpty()) {
            setContentView(new View(this));
            findViewById(android.R.id.content).setBackgroundColor(Color.BLACK);
            Login(this, savedKey);
        } else {
            initLoginUI();
        }
    }

    private void initLoginUI() {
        setContentView(R.layout.activity_login);

        checkAndRequestPermissions();

        textUsername = findViewById(R.id.userkey);
        btnLogin = findViewById(R.id.login);
        pasteBtn = findViewById(R.id.paste);
        getKey = findViewById(R.id.GetKey);

        textUsername.setText(prefs.getSt(USER, ""));

        getKey.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(GetKey()));
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String userKey = textUsername.getText().toString().trim();
            if (!userKey.isEmpty()) {
                prefs.setSt(USER, userKey);
                Login(this, userKey);
            } else {
                textUsername.setError("Please enter key");
            }
        });
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
        pasteBtn.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            if (clipboard != null && clipboard.hasPrimaryClip()) {
                ClipData clip = clipboard.getPrimaryClip();
                if (clip != null && clip.getItemCount() > 0) {
                    String pasted = clip.getItemAt(0).getText().toString();
                    textUsername.setText(pasted);
                }
            }
        });
    }
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
    private void checkAndRequestPermissions() {
        if (!isStoragePermissionGranted()) {
            requestStoragePermissionDirect();
        } else if (!canRequestPackageInstalls()) {
            requestUnknownAppPermissionsDirect();
        }
    }

    private boolean isStoragePermissionGranted() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.R || Environment.isExternalStorageManager();
    }

    private void requestStoragePermissionDirect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.fromParts("package", getPackageName(), null));
            startActivityForResult(intent, REQUEST_MANAGE_STORAGE_PERMISSION);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_MANAGE_STORAGE_PERMISSION);
        }
    }

    private boolean canRequestPackageInstalls() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.O || getPackageManager().canRequestPackageInstalls();
    }

    private void requestUnknownAppPermissionsDirect() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQUEST_MANAGE_UNKNOWN_APP_SOURCES);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_MANAGE_STORAGE_PERMISSION || requestCode == REQUEST_MANAGE_UNKNOWN_APP_SOURCES) {
            checkAndRequestPermissions();
        }
    }
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
    private void Login(final Context m_Context, final String userKey) {
        Log.d(TAG, "Login attempt with key: " + userKey.substring(0, Math.min(4, userKey.length())) + "...");
        
        showLoadingDialog("Verifying Session...", false);

        Handler loginHandler = new Handler(Looper.getMainLooper(), msg -> {
            dismissLoadingDialog();
            if (msg.what == 0) {
                Log.d(TAG, "Login successful");
                startDownload(m_Context);
            } else {
                Log.e(TAG, "Login failed: " + msg.obj);
                prefs.setSt(USER, "");
                initLoginUI();
                String errorMsg = msg.obj != null ? msg.obj.toString() : "Session Expired or Invalid Key";
                Toast.makeText(m_Context, errorMsg, Toast.LENGTH_LONG).show();
            }
            return true;
        });

        new Thread(() -> {
            try {
                String result = Check(m_Context, userKey);
                Log.d(TAG, "Check result: " + result);
                
                if ("OK".equals(result)) {
                    loginHandler.sendEmptyMessage(0);
                } else {
                    android.os.Message msg = loginHandler.obtainMessage(1);
                    msg.obj = result;
                    loginHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                Log.e(TAG, "Login exception", e);
                android.os.Message msg = loginHandler.obtainMessage(1);
                msg.obj = "Connection error: " + e.getMessage();
                loginHandler.sendMessage(msg);
            }
        }).start();
    }

    private void startDownload(Context m_Context) {
        showLoadingDialog("Checking resources...", false);
        Downtwo task = new Downtwo(LogAct.this, success -> {
            dismissLoadingDialog();
            if (success) {
                Intent i = new Intent(m_Context, MAct.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                m_Context.startActivity(i);
                finish();
            } else {
                initLoginUI();
                Toast.makeText(LogAct.this, "Network error!", Toast.LENGTH_SHORT).show();
            }
        });
        task.execute(Downtwo.Link());
    }
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1
    private void showLoadingDialog(String message, boolean isError) {
        if (loadingDialog == null) {
            loadingDialog = new Dialog(this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            loadingDialog.setContentView(R.layout.ios_loading);
            loadingDialog.setCancelable(false);
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }
        }
        TextView loadingText = loadingDialog.findViewById(R.id.loadingText);
        loadingText.setText(message);
        if (!isFinishing()) loadingDialog.show();
    }

    private void dismissLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private static native String Check(Context mContext, String userKey);
    private native String GetKey();
}
    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1    //  JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1   JOIN @XDSRC1