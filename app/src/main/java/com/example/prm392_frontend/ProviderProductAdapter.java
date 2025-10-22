package com.example.prm392_frontend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.button.MaterialButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProviderProductAdapter extends RecyclerView.Adapter<ProviderProductAdapter.ViewHolder> {

    private List<Product> products;
    private OnProductActionListener listener;

    public interface OnProductActionListener {
        void onEditClick(Product product);
        void onDeleteClick(Product product);
    }

    public ProviderProductAdapter(List<Product> products, OnProductActionListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_provider_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateProducts(List<Product> newProducts) {
        this.products = newProducts;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName;
        TextView tvProductPrice;
        TextView tvProductCategory;
        MaterialButton btnEdit;
        MaterialButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            tvProductCategory = itemView.findViewById(R.id.tvProductCategory);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }

        void bind(Product product, OnProductActionListener listener) {
            tvProductName.setText(product.getProductName());

            // Format price
            NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            tvProductPrice.setText(formatter.format(product.getPrice()));

            // Category name
            if (product.getCategoryID() != null) {
                tvProductCategory.setText(product.getCategoryID().getCategoryName());
            } else {
                tvProductCategory.setText("No category");
            }

            // Load image
            if (product.getImageURL() != null && !product.getImageURL().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageURL())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.product_image_placeholder)
                        .error(R.drawable.product_image_placeholder)
                        .centerCrop()
                        .into(ivProductImage);
            } else {
                ivProductImage.setImageResource(R.drawable.product_image_placeholder);
            }

            // Edit button
            btnEdit.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(product);
                }
            });

            // Delete button
            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(product);
                }
            });
        }
    }
}
