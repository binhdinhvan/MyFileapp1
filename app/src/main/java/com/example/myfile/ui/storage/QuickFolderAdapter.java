package com.example.myfile.ui.storage;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfile.R;
import com.example.myfile.data.storage.QuickFolderItem;

import java.util.List;

public class QuickFolderAdapter extends RecyclerView.Adapter<QuickFolderAdapter.ViewHolder> {

    public interface OnQuickFolderClickListener {
        void onQuickFolderClick(QuickFolderItem item);
    }

    private final List<QuickFolderItem> items;
    private final OnQuickFolderClickListener listener;

    public QuickFolderAdapter(List<QuickFolderItem> items, OnQuickFolderClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_quick_folder, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuickFolderItem item = items.get(position);
        holder.tvEmoji.setText(item.getEmoji());
        holder.tvLabel.setText(item.getLabel());
        holder.itemView.setOnClickListener(v -> listener.onQuickFolderClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji;
        TextView tvLabel;

        ViewHolder(View itemView) {
            super(itemView);
            tvEmoji = itemView.findViewById(R.id.tvFolderEmoji);
            tvLabel = itemView.findViewById(R.id.tvFolderLabel);
        }
    }
}
