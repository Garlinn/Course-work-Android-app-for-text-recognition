package com.example.scanny;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OcrManager {

    public interface OcrCallback {
        void onSuccess(String text);
        void onError(String message);
    }

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public OcrManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void recognize(Bitmap bitmap, String lang, OcrCallback callback) {
        executor.execute(() -> {
            try {
                File tessDir = getTessDataDir();
                for (String l : lang.split("\\+")) {
                    if (!isTessDataReady(tessDir, l)) copyTessDataFromAssets(tessDir, l);
                }

                TessBaseAPI tess = new TessBaseAPI();
                boolean ok = tess.init(tessDir.getAbsolutePath(), lang);
                if (!ok) {
                    mainHandler.post(() -> callback.onError(
                            context.getString(R.string.ocr_error_init)));
                    return;
                }

                tess.setImage(bitmap);
                String result = tess.getUTF8Text();
                tess.recycle();

                String trimmed = result == null ? "" : result.trim();
                mainHandler.post(() -> {
                    if (trimmed.isEmpty()) callback.onError(context.getString(R.string.ocr_error_empty));
                    else callback.onSuccess(trimmed);
                });

            } catch (Exception e) {
                mainHandler.post(() -> callback.onError(
                        context.getString(R.string.ocr_error_prefix) + e.getMessage()));
            }
        });
    }

    private File getTessDataDir() { return context.getFilesDir(); }

    private boolean isTessDataReady(File tessDir, String lang) {
        File f = new File(tessDir, "tessdata/" + lang + ".traineddata");
        return f.exists() && f.length() > 0;
    }

    private void copyTessDataFromAssets(File tessDir, String lang) throws IOException {
        File tessDataDir = new File(tessDir, "tessdata");
        if (!tessDataDir.exists()) tessDataDir.mkdirs();
        File outFile = new File(tessDataDir, lang + ".traineddata");
        try (InputStream in = context.getAssets().open("tessdata/" + lang + ".traineddata");
             FileOutputStream out = new FileOutputStream(outFile)) {
            byte[] buf = new byte[4096];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
    }
}
