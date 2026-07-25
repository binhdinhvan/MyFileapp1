package com.example.myfile.ui.storage;

import android.content.Context;
import android.os.StatFs;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfile.R;
import com.example.myfile.data.storage.StorageVolumeItem;

import java.util.List;

public class StorageAdapter extends RecyclerView.Adapter<StorageAdapter.ViewHolder> {

    public interface OnStorageClickListener {
        void onStorageClick(StorageVolumeItem item);
    }

    private final List<StorageVolumeItem> items;
    private final OnStorageClickListener listener;

    public StorageAdapter(List<StorageVolumeItem> items, OnStorageClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_storage, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StorageVolumeItem item = items.get(position);
        holder.tvStorageName.setText(item.getLabel());
        holder.tvStorageDetail.setText(buildDetail(holder.itemView.getContext(), item.getPath()));
        holder.itemView.setOnClickListener(v -> listener.onStorageClick(item));
    }

    private String buildDetail(Context ctx, String path) {
        try {
            StatFs stat = new StatFs(path);
            long total = stat.getTotalBytes();
            long free = stat.getAvailableBytes();
            return Formatter.formatShortFileSize(ctx, free) + " trong / "
                    + Formatter.formatShortFileSize(ctx, total);
        } catch (Exception e) {
            return path;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvStorageName;
        TextView tvStorageDetail;

        ViewHolder(View itemView) {
            super(itemView);
            tvStorageName = itemView.findViewById(R.id.tvStorageName);
            tvStorageDetail = itemView.findViewById(R.id.tvStorageDetail);
        }
    }
}
