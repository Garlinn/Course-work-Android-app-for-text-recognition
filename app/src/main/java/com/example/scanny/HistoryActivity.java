package com.example.scanny;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.List;

public class HistoryActivity extends BaseActivity {

    private HistoryAdapter adapter;
    private List<HistoryDatabase.ScanRecord> items;
    private HistoryDatabase db;
    private LinearLayout llEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        RecyclerView rv              = findViewById(R.id.rvHistory);
        SwipeRefreshLayout swipe     = findViewById(R.id.swipeRefresh);
        llEmpty                      = findViewById(R.id.llEmpty);
        ImageView btnInfo            = findViewById(R.id.btnInfo);

        rv.setLayoutManager(new LinearLayoutManager(this));

        db    = new HistoryDatabase(this);
        items = db.getAll();

        adapter = new HistoryAdapter(items, record -> {
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra(ResultActivity.EXTRA_RECORD_ID, record.id);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });
        rv.setAdapter(adapter);
        updateEmptyState();

        new ItemTouchHelper(new SwipeToDeleteCallback()).attachToRecyclerView(rv);

        swipe.setOnRefreshListener(() -> {
            items.clear();
            items.addAll(db.getAll());
            adapter.notifyDataSetChanged();
            updateEmptyState();
            swipe.setRefreshing(false);
        });

        btnInfo.setOnClickListener(v ->
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.history_info_title))
                        .setMessage(getString(R.string.history_delete_hint))
                        .setPositiveButton(getString(R.string.dialog_ok), null)
                        .show()
        );

        findViewById(R.id.tabHome).setOnClickListener(v -> navigateTo(HomeActivity.class));
        findViewById(R.id.tabSettings).setOnClickListener(v -> navigateTo(SettingsActivity.class));
    }

    @Override
    protected void onResume() {
        super.onResume();
        items.clear();
        items.addAll(db.getAll());
        adapter.notifyDataSetChanged();
        updateEmptyState();
    }

    private void updateEmptyState() {
        llEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
    }


    private class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        SwipeToDeleteCallback() { super(0, ItemTouchHelper.LEFT); }

        @Override
        public boolean onMove(@NonNull RecyclerView rv,
                              @NonNull RecyclerView.ViewHolder vh,
                              @NonNull RecyclerView.ViewHolder target) { return false; }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int pos = viewHolder.getAdapterPosition();
            db.delete(items.get(pos).id);
            items.remove(pos);
            adapter.notifyItemRemoved(pos);
            updateEmptyState();
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView rv,
                                @NonNull RecyclerView.ViewHolder vh,
                                float dX, float dY, int actionState, boolean isActive) {
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                View item = vh.itemView;
                paint.setColor(0xFFE53935);
                c.drawRoundRect(new RectF(
                        item.getRight() + dX, item.getTop() + 4f,
                        item.getRight(),      item.getBottom() - 4f
                ), 12f, 12f, paint);

                paint.setColor(0xFFFFFFFF);
                paint.setTextSize(36f);
                paint.setTextAlign(Paint.Align.CENTER);
                c.drawText("Delete",
                        item.getRight() - 80f,
                        item.getTop() + item.getHeight() / 2f + 12f,
                        paint);
            }
            super.onChildDraw(c, rv, vh, dX, dY, actionState, isActive);
        }
    }

    interface OnItemClick { void onClick(HistoryDatabase.ScanRecord record); }

    static class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.VH> {

        private final List<HistoryDatabase.ScanRecord> items;
        private final OnItemClick listener;

        HistoryAdapter(List<HistoryDatabase.ScanRecord> items, OnItemClick listener) {
            this.items = items; this.listener = listener;
        }

        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_history, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH h, int pos) {
            HistoryDatabase.ScanRecord item = items.get(pos);
            String preview = item.text.length() > 60
                    ? item.text.substring(0, 60) + "…" : item.text;
            h.tvName.setText(preview);
            h.tvDate.setText(item.getDateLabel(h.itemView.getContext()));
            h.itemView.setOnClickListener(v -> listener.onClick(item));
        }

        @Override public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvDate;
            VH(View v) {
                super(v);
                tvName = v.findViewById(R.id.tvItemName);
                tvDate = v.findViewById(R.id.tvItemDate);
            }
        }
    }
}
