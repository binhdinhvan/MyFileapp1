package com.example.myfile.feature.viewer.exif;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfile.R;

import java.util.List;

public class ExifAdapter extends RecyclerView.Adapter<ExifAdapter.ViewHolder> {

    private final List<ExifReader.Entry> entries;

    public ExifAdapter(List<ExifReader.Entry> entries) {
        this.entries = entries;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_exif_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ExifReader.Entry entry = entries.get(position);
        holder.tvLabel.setText(entry.label);
        holder.tvValue.setText(entry.value);
    }

    @Override
    public int getItemCount() {
        return entries.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView tvLabel;
        final TextView tvValue;

        ViewHolder(View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.tvExifLabel);
            tvValue = itemView.findViewById(R.id.tvExifValue);
        }
    }
}
