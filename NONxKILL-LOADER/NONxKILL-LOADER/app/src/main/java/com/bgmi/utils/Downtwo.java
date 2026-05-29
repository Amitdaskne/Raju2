package com.bgmi.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Downtwo extends AsyncTask<String, Integer, String> {

    private static final String TAG = "Downtwo";

    // Native URLs from main.cpp
    public static native String Version();
    public static native String Link();

    public interface Callback {
        void onComplete(boolean success);
    }

    public interface ProgressListener {
        void onProgress(int percent);
    }

    private final Context context;
    private final Callback callback;
    private ProgressListener progressListener;

    private static final String PREF_NAME = "com.bgmi.download";
    private static final String PREF_VERSION_KEY = "version";

    private static final String ZIP_NAME = "auto.zip";
    private static final String LOADER_DIR = "loader";

    public Downtwo(Context context) {
        this(context, null);
    }

    public Downtwo(Context context, Callback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void setProgressListener(ProgressListener listener) {
        this.progressListener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        try {
            String serverVersion = getServerVersion();
            if (serverVersion == null) {
                return "Version fetch failed";
            }

            SharedPreferences prefs =
                    context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

            String localVersion = prefs.getString(PREF_VERSION_KEY, "0");

            if (localVersion.equals(serverVersion)) {
                Log.d(TAG, "Already latest version");
                return null;
            }

            String err = downloadAndExtract(Link());
            if (err != null) return err;

            prefs.edit().putString(PREF_VERSION_KEY, serverVersion).apply();
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Background error", e);
            return e.getMessage();
        }
    }

    @Override
    protected void onPostExecute(String result) {
        boolean success = (result == null);

        if (callback != null) {
            callback.onComplete(success);
        }

        if (!success) {
            showError(result);
        }
    }

    // ---------------- VERSION ----------------

    private String getServerVersion() {
        try {
            URL url = new URL(Version());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();

            Scanner sc = new Scanner(con.getInputStream());
            String v = sc.nextLine().trim();
            sc.close();
            con.disconnect();

            return v;
        } catch (Exception e) {
            Log.e(TAG, "Version error", e);
            return null;
        }
    }

    // ---------------- DOWNLOAD + EXTRACT ----------------

    private String downloadAndExtract(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.connect();

            int total = con.getContentLength();
            int done = 0;

            InputStream in = con.getInputStream();
            File zipFile = new File(context.getFilesDir(), ZIP_NAME);
            FileOutputStream out = new FileOutputStream(zipFile);

            byte[] buf = new byte[8192];
            int len;

            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                done += len;

                if (total > 0 && progressListener != null) {
                    progressListener.onProgress(
                            (int) ((done * 100L) / total)
                    );
                }
            }

            out.close();
            in.close();

            unzip(zipFile, context.getFilesDir());
            zipFile.delete();

            File so = new File(
                    context.getFilesDir(),
                    "loader/libbgmi.so"
            );

            if (!so.exists()) {
                return "libbgmi.so not found after extract";
            }

            Log.d(TAG, "lib extracted: " + so.getAbsolutePath());
            return null;

        } catch (Exception e) {
            Log.e(TAG, "Download/extract error", e);
            return e.getMessage();
        }
    }

    // ---------------- UNZIP ----------------

    private void unzip(File zipFile, File targetDir) throws Exception {
        ZipInputStream zis =
                new ZipInputStream(new FileInputStream(zipFile));

        ZipEntry entry;
        byte[] buffer = new byte[8192];

        while ((entry = zis.getNextEntry()) != null) {
            File outFile = new File(targetDir, entry.getName());

            if (entry.isDirectory()) {
                outFile.mkdirs();
            } else {
                new File(outFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(outFile);

                int count;
                while ((count = zis.read(buffer)) != -1) {
                    fos.write(buffer, 0, count);
                }
                fos.close();
            }
            zis.closeEntry();
        }
        zis.close();
    }

    // ---------------- ERROR UI ----------------

    private void showError(String msg) {
        if (!(context instanceof Activity)) return;
        Activity a = (Activity) context;
        if (a.isFinishing() || a.isDestroyed()) return;

        new MaterialAlertDialogBuilder(context)
                .setTitle("Update Failed")
                .setMessage(msg)
                .setPositiveButton("OK", null)
                .show();
    }
}