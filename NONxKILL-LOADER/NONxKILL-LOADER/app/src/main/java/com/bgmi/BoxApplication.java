package com.bgmi;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import top.niunaijun.blackbox.BlackBoxCore;
import com.zcore.app.configuration.AppLifecycleCallback;
import com.zcore.app.configuration.ClientConfiguration;
import net_62v.external.MetaActivationManager;

import java.io.File;

public class BoxApplication extends Application {

    static {
        System.loadLibrary("zenin");
    }

    public static native String getSdkKey();

    private static final String TAG = "BoxApplication";

    // Games
    private static final String PKG_BGMI = "com.pubg.imobile";
    private static final String PKG_GLOBAL = "com.tencent.ig";

    // Lib paths (ZIP ke andar loader/)
    private static final String BGMI_LIB = "loader/libbgmi.so";
    private static final String GLOBAL_LIB = "loader/libpubgm.so";

    private final String[] process_names = {
            "com.pubg.krmobile",
            "com.tencent.ig",
            "com.rekoo.pubgm",
            "com.vng.pubgmobile",
            "com.pubg.imobile"
    };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        try {
            ZCoreCore.get().doAttachBaseContext(base, new ClientConfiguration() {

                @Override
                public String getHostPackageName() {
                    return base.getPackageName();
                }

                @Override
                public boolean isHideRoot() {
                    return true;
                }

                @Override
                public boolean isHideXposed() {
                    return true;
                }

                @Override
                public boolean isEnableDaemonService() {
                    return false;
                }

                @Override
                public boolean requestInstallPackage(File file) {
                    return true;
                }
            });

            checkAppName(base);

        } catch (Throwable t) {
            Log.e(TAG, "attachBaseContext error", t);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Init ZCore
        ZCoreCore.get().doCreate();
        
        MetaActivationManager.activateSdk("XKQK9SHG9W");

        // Activate
        BoxActivate.activateBox(getSdkKey());

        ZCoreCore.get().addAppLifecycleCallback(new AppLifecycleCallback() {

            @Override
            public void beforeCreateApplication(
                    String packageName,
                    String processName,
                    Context context,
                    int userId
            ) { }

            @Override
            public void beforeApplicationOnCreate(
                    String packageName,
                    String processName,
                    Application application,
                    int userId
            ) {
                // ❌ Early load hata diya (UI freeze fix)
            }

            @Override
            public void afterApplicationOnCreate(
                    String packageName,
                    String processName,
                    Application application,
                    int userId
            ) {

                if (!packageName.equals(processName)) return;

                for (String pkg : process_names) {
                    if (!pkg.equals(packageName)) continue;

                    // BGMI
                    if (PKG_BGMI.equals(pkg)) {
                        loadLibWithWait(BGMI_LIB);
                    }

                    // GLOBAL
                    if (PKG_GLOBAL.equals(pkg)) {
                        loadLibWithWait(GLOBAL_LIB);
                    }

                    break;
                }
            }
        });
    }

    /* ======================= LIB LOAD SAFE ======================= */

    private void loadLibWithWait(String relativePath) {
        new Thread(() -> {
            try {
                File soFile = new File(getFilesDir(), relativePath);

                // ⏳ wait max 5 sec for ZIP extract
                long start = System.currentTimeMillis();
                while (!soFile.exists()) {
                    if (System.currentTimeMillis() - start > 5000) {
                        Log.e(TAG, "Lib timeout: " + relativePath);
                        return;
                    }
                    Thread.sleep(100);
                }

                // small delay = engine ready
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        System.load(soFile.getAbsolutePath());
                        Log.d(TAG, "Loaded lib: " + relativePath);
                    } catch (Throwable t) {
                        Log.e(TAG, "Load failed: " + relativePath, t);
                    }
                }, 300);

            } catch (Throwable t) {
                Log.e(TAG, "Lib wait error", t);
            }
        }).start();
    }

    /* ======================= ANTI TAMPER ======================= */

    private void checkAppName(Context context) {
        String expectedAppName = "Spy Loader";
        try {
            ApplicationInfo appInfo =
                    context.getPackageManager().getApplicationInfo(
                            context.getPackageName(), 0);

            String appName =
                    context.getPackageManager().getApplicationLabel(appInfo).toString();
            if (!expectedAppName.equals(appName)) {
                throw new SecurityException("App name tampered");
            }
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}