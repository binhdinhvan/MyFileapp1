package com.example.myfile.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myfile.R;
import com.example.myfile.data.model.FileItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileAdapter extends RecyclerView.Adapter<FileAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(FileItem item);
        void onItemLongClick(FileItem item);
        void onSelectionChanged(boolean selectionMode, int count);
    }

    private List<FileItem> items;
    private final OnItemClickListener listener;
    private boolean selectionMode = false;
    private final Set<String> selectedPaths = new HashSet<>();
    private int itemLayoutRes = R.layout.item_file;

    public FileAdapter(List<FileItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateData(List<FileItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setGridMode(boolean grid) {
        itemLayoutRes = grid ? R.layout.item_file_grid : R.layout.item_file;
        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {
        return selectionMode;
    }

    public List<String> getSelectedPaths() {
        return new ArrayList<>(selectedPaths);
    }

    public void enterSelectionMode() {
        selectionMode = true;
        selectedPaths.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(true, 0);
    }

    public void exitSelectionMode() {
        selectionMode = false;
        selectedPaths.clear();
        notifyDataSetChanged();
        listener.onSelectionChanged(false, 0);
    }

    private void toggleSelection(FileItem item) {
        if (selectedPaths.contains(item.getPath())) {
            selectedPaths.remove(item.getPath());
        } else {
            selectedPaths.add(item.getPath());
        }
        notifyDataSetChanged();
        listener.onSelectionChanged(true, selectedPaths.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(itemLayoutRes, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FileItem item = items.get(position);
        holder.tvName.setText(item.getName());

        if (item.isDirectory()) {
            holder.tvName.setTextColor(0xFF1976D2);
            holder.tvDetail.setText("Thu muc");
            holder.tvBadge.setText("DIR");
            holder.tvBadge.setBackgroundColor(0xFF1976D2);
        } else {
            holder.tvName.setTextColor(0xFF212121);
            String date = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(item.getLastModified());
            String sizeText = formatSize(item.getSize());
            holder.tvDetail.setText(sizeText + " - " + date);
            holder.tvBadge.setText(getBadgeText(item.getName()));
            holder.tvBadge.setBackgroundColor(getBadgeColor(item.getName()));
        }

        boolean isSelected = selectedPaths.contains(item.getPath());
        holder.itemView.setBackgroundColor(isSelected ? 0xFFE3F2FD : 0xFFFFFFFF);
        holder.tvCheck.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        holder.tvCheck.setChecked(isSelected);

        holder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(item);
            } else {
                listener.onItemClick(item);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (selectionMode) {
                toggleSelection(item);
            } else {
                listener.onItemLongClick(item);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    private String getBadgeText(String name) {
        String lower = name.toLowerCase(Locale.getDefault());
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
            return "IMG";
        } else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv")) {
            return "VID";
        } else if (lower.endsWith(".zip") || lower.endsWith(".rar")) {
            return "ZIP";
        } else if (lower.endsWith(".pdf")) {
            return "PDF";
        } else if (lower.endsWith(".txt")) {
            return "TXT";
        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav")) {
            return "MP3";
        }
        return "FILE";
    }

    private int getBadgeColor(String name) {
        String lower = name.toLowerCase(Locale.getDefault());
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png")) {
            return 0xFF43A047;
        } else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv")) {
            return 0xFF8E24AA;
        } else if (lower.endsWith(".zip") || lower.endsWith(".rar")) {
            return 0xFFFB8C00;
        } else if (lower.endsWith(".pdf")) {
            return 0xFFE53935;
        } else if (lower.endsWith(".txt")) {
            return 0xFF607D8B;
        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav")) {
            return 0xFFD81B60;
        }
        return 0xFF90A4AE;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024, exp), unit);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadge;
        TextView tvName;
        TextView tvDetail;
        CheckBox tvCheck;

        ViewHolder(View itemView) {
            super(itemView);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvCheck = itemView.findViewById(R.id.tvCheck);
        }
    }
}
