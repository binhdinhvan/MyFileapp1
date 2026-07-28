package com.example.myfile.feature.viewer.image;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myfile.R;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.util.List;

/** Adapter cho ViewPager2 cua ImageViewerActivity - moi trang 1 anh, ho tro pinch-zoom. */
public class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.PageViewHolder> {

    private final List<String> imagePaths;

    public ImagePagerAdapter(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_page, parent, false);
        return new PageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        String path = imagePaths.get(position);
        Glide.with(holder.photoView)
                .load(new File(path))
                .placeholder(R.drawable.ic_type_image)
                .error(R.drawable.ic_type_image)
                .into(holder.photoView);
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    static class PageViewHolder extends RecyclerView.ViewHolder {
        final PhotoView photoView;

        PageViewHolder(View itemView) {
            super(itemView);
            photoView = itemView.findViewById(R.id.photoView);
        }
    }
}
