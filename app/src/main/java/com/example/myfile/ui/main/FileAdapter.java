package com.example.myfile.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myfile.R;
import com.example.myfile.data.model.FileItem;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(FileItem item);
        void onItemLongClick(FileItem item);
        void onSelectionChanged(boolean selectionMode, int count);
    }

    private List<FileItem> items;
    private final OnItemClickListener listener;
    private boolean selectionMode = false;
    private final Set<String> selectedPaths = new HashSet<>();
    private int viewMode = 0; 
    private boolean isRecentMode = false;

    public FileAdapter(List<FileItem> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    public void updateData(List<FileItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setViewMode(int mode) {
        this.viewMode = mode;
        notifyDataSetChanged();
    }

    public void setRecentMode(boolean recentMode) {
        this.isRecentMode = recentMode;
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

    @Override
    public int getItemViewType(int position) {
        if (items.get(position).isHeader()) return 3;
        return viewMode;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 3) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new HeaderViewHolder(view);
        }
        int res = (viewType == 0) ? R.layout.item_file : R.layout.item_file_grid;
        View view = LayoutInflater.from(parent.getContext()).inflate(res, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        FileItem item = items.get(position);
        
        if (getItemViewType(position) == 3) {
            HeaderViewHolder headerHolder = (HeaderViewHolder) holder;
            headerHolder.tvHeaderTitle.setText(item.getHeaderTitle());
            return;
        }
        
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        itemHolder.tvName.setText(item.getName());

        if (item.isDirectory()) {
            itemHolder.tvName.setTextColor(0xFF1976D2);
            String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(item.getLastModified());
            int childCount = 0;
            java.io.File f = new java.io.File(item.getPath());
            if (f.exists() && f.isDirectory()) {
                String[] children = f.list();
                if (children != null) childCount = children.length;
            }
            String countText = childCount == 1 ? "1 item" : childCount + " items";
            itemHolder.tvDetail.setText(date + "  |  " + countText);
            itemHolder.tvBadge.setText("\uD83D\uDCC1");
            itemHolder.tvBadge.setTextSize(22);
            itemHolder.tvBadge.setBackground(
                    ContextCompat.getDrawable(itemHolder.itemView.getContext(), R.drawable.bg_badge_folder));
        } else {
            itemHolder.tvName.setTextColor(0xFF212121);
            String sizeText = formatSize(item.getSize());
            if (isRecentMode) {
                java.io.File f = new java.io.File(item.getPath());
                String parentName = f.getParentFile() != null ? f.getParentFile().getName() : "Unknown";
                itemHolder.tvDetail.setText(sizeText + "  |  " + parentName);
            } else {
                String date = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()).format(item.getLastModified());
                itemHolder.tvDetail.setText(date + "  |  " + sizeText);
            }
            itemHolder.tvBadge.setText(getBadgeLabel(item.getName()));
            itemHolder.tvBadge.setTextSize(22);
            itemHolder.tvBadge.setBackground(
                    ContextCompat.getDrawable(itemHolder.itemView.getContext(), getBadgeDrawable(item.getName())));
        }

        boolean isSelected = selectedPaths.contains(item.getPath());
        itemHolder.itemView.setAlpha(isSelected ? 0.85f : 1.0f);

        if (isSelected) {
            itemHolder.itemView.setBackgroundResource(R.drawable.bg_card_selected);
        } else {
            itemHolder.itemView.setBackgroundResource(R.drawable.bg_card_item);
        }
        itemHolder.tvCheck.setVisibility(selectionMode ? View.VISIBLE : View.GONE);
        itemHolder.tvCheck.setChecked(isSelected);

        itemHolder.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(item);
            } else {
                listener.onItemClick(item);
            }
        });
        itemHolder.itemView.setOnLongClickListener(v -> {
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

    private String getBadgeLabel(String name) {
        String lower = name.toLowerCase(Locale.getDefault());
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp")) {
            return "\uD83D\uDDBC";
        } else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") || lower.endsWith(".mov")) {
            return "\uD83C\uDFAC";
        } else if (lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z")) {
            return "\uD83D\uDCE6";
        } else if (lower.endsWith(".pdf")) {
            return "\uD83D\uDCCB";
        } else if (lower.endsWith(".txt") || lower.endsWith(".md")) {
            return "\uD83D\uDCDD";
        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac") || lower.endsWith(".ogg")) {
            return "\uD83C\uDFB5";
        } else if (lower.endsWith(".apk")) {
            return "\uD83D\uDCF1";
        } else if (lower.endsWith(".doc") || lower.endsWith(".docx")) {
            return "\uD83D\uDCC3";
        } else if (lower.endsWith(".xls") || lower.endsWith(".xlsx") || lower.endsWith(".csv")) {
            return "\uD83D\uDCCA";
        }
        return "\uD83D\uDCC4";
    }

    
    private String getBadgeEmoji(String name) {
        return getBadgeLabel(name);
    }

    private int getBadgeDrawable(String name) {
        String lower = name.toLowerCase(Locale.getDefault());
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".gif") || lower.endsWith(".webp")) {
            return R.drawable.bg_badge_img;
        } else if (lower.endsWith(".mp4") || lower.endsWith(".avi") || lower.endsWith(".mkv") || lower.endsWith(".mov")) {
            return R.drawable.bg_badge_video;
        } else if (lower.endsWith(".mp3") || lower.endsWith(".wav") || lower.endsWith(".flac") || lower.endsWith(".ogg")) {
            return R.drawable.bg_badge_audio;
        }
        return R.drawable.bg_badge_file;
    }

    
    private String getBadgeText(String name) {
        return getBadgeEmoji(name);
    }

    
    private int getBadgeColor(String name) {
        return 0xFFFFF3E0;
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String unit = "KMGTPE".charAt(exp - 1) + "B";
        return String.format(Locale.getDefault(), "%.1f %s", bytes / Math.pow(1024, exp), unit);
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView tvBadge;
        TextView tvName;
        TextView tvDetail;
        CheckBox tvCheck;

        ItemViewHolder(View itemView) {
            super(itemView);
            tvBadge = itemView.findViewById(R.id.tvBadge);
            tvName = itemView.findViewById(R.id.tvName);
            tvDetail = itemView.findViewById(R.id.tvDetail);
            tvCheck = itemView.findViewById(R.id.tvCheck);
        }
    }
    
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeaderTitle;
        
        HeaderViewHolder(View itemView) {
            super(itemView);
            tvHeaderTitle = itemView.findViewById(R.id.tvHeaderTitle);
        }
    }
}
