package com.example.scanny;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

public class ResultActivity extends BaseActivity {

    public static final String EXTRA_RECORD_ID = "record_id";
    private String resultText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        TextView       tvResult = findViewById(R.id.tvResult);
        MaterialButton btnCopy  = findViewById(R.id.btnCopy);
        MaterialButton btnShare = findViewById(R.id.btnShare);
        ImageView      btnBack  = findViewById(R.id.btnBack);

        String recordId = getIntent().getStringExtra(EXTRA_RECORD_ID);
        if (recordId != null) {
            HistoryDatabase.ScanRecord record = new HistoryDatabase(this).getById(recordId);
            if (record != null) resultText = record.text;
        }

        tvResult.setText(resultText.isEmpty() ? getString(R.string.no_text) : resultText);

        btnBack.setOnClickListener(v -> { finish(); overridePendingTransition(0, 0); });

        btnCopy.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            cm.setPrimaryClip(ClipData.newPlainText("scanny_result", resultText));
            Toast.makeText(this, getString(R.string.copied), Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("text/plain");
            share.putExtra(Intent.EXTRA_TEXT, resultText);
            startActivity(Intent.createChooser(share, getString(R.string.btn_share)));
        });
    }
}
