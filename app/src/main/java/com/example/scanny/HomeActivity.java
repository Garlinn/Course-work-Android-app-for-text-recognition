package com.example.scanny;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;

import java.io.IOException;

public class HomeActivity extends BaseActivity {

    private ImageView      ivPreview;
    private LinearLayout   llPlaceholder;
    private MaterialButton btnScan;
    private ProgressBar    progressBar;
    private Bitmap         selectedBitmap;

    private OcrManager      ocrManager;
    private HistoryDatabase historyDb;
    private AppPreferences  prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ivPreview     = findViewById(R.id.ivPreview);
        llPlaceholder = findViewById(R.id.llPlaceholder);
        btnScan       = findViewById(R.id.btnScan);
        progressBar   = findViewById(R.id.progressBar);
        FrameLayout uploadZone = findViewById(R.id.uploadZone);

        prefs      = new AppPreferences(this);
        historyDb  = new HistoryDatabase(this);
        ocrManager = new OcrManager(this);

        uploadZone.setOnClickListener(v -> openGallery());
        btnScan.setOnClickListener(v -> { if (selectedBitmap != null) runOcr(selectedBitmap); });

        findViewById(R.id.tabHistory).setOnClickListener(v -> navigateTo(HistoryActivity.class));
        findViewById(R.id.tabSettings).setOnClickListener(v -> navigateTo(SettingsActivity.class));
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            Uri uri = result.getData().getData();
                            try {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                                showPreview(selectedBitmap);
                            } catch (IOException e) {
                                Toast.makeText(this, getString(R.string.img_load_error), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        imagePickerLauncher.launch(intent);
    }

    private void runOcr(Bitmap bitmap) {
        setUiBusy(true);
        ocrManager.recognize(bitmap, prefs.getOcrLang(), new OcrManager.OcrCallback() {
            @Override public void onSuccess(String text) {
                setUiBusy(false);
                HistoryDatabase.ScanRecord record = historyDb.save(text);
                Intent intent = new Intent(HomeActivity.this, ResultActivity.class);
                intent.putExtra(ResultActivity.EXTRA_RECORD_ID, record.id);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            @Override public void onError(String message) {
                setUiBusy(false);
                Toast.makeText(HomeActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showPreview(Bitmap bitmap) {
        llPlaceholder.setVisibility(View.GONE);
        ivPreview.setVisibility(View.VISIBLE);
        ivPreview.setImageBitmap(bitmap);
        btnScan.setVisibility(View.VISIBLE);
    }

    private void setUiBusy(boolean busy) {
        btnScan.setEnabled(!busy);
        progressBar.setVisibility(busy ? View.VISIBLE : View.GONE);
    }
}
