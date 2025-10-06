package com.example.prm392_frontend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products;
    private OnProductClickListener listener;
    private int lastPosition = -1;

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener);

        // Animate items
        setAnimation(holder.itemView, position);
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(viewToAnimate.getContext(), R.anim.item_animation_fall_down);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private ImageView productImage;
        private TextView productName;
        private TextView productRating;
        private TextView productPrice;
        private TextView productSold;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_image);
            productName = itemView.findViewById(R.id.product_name);
            productRating = itemView.findViewById(R.id.product_rating);
            productPrice = itemView.findViewById(R.id.product_price);
            productSold = itemView.findViewById(R.id.product_sold);
        }

        public void bind(Product product, OnProductClickListener listener) {
            productName.setText(product.getName());
            productRating.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()));

            // Format currency with thousand separators
            NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
            String formattedPrice = "₫" + currencyFormat.format(product.getPrice() * 1000);
            productPrice.setText(formattedPrice);

            // Calculate random sold count for demo (you can add this to Product model later)
            int soldCount = (int) (Math.random() * 500) + 50;
            productSold.setText(String.format(Locale.getDefault(), "%d đã bán", soldCount));

            // Load image from URL using Glide
            Glide.with(productImage.getContext())
                    .load(product.getImageUrl())
                    .placeholder(R.drawable.product_image_placeholder)
                    .error(R.drawable.product_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(productImage);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });
        }
    }
}
