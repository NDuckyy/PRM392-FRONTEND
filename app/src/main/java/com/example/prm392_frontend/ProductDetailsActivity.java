package com.example.prm392_frontend;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.dynamicanimation.animation.DynamicAnimation;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ProductDetailsActivity extends AppCompatActivity {

    private ViewPager2 imageViewPager;
    private TextView productName;
    private TextView productCategory;
    private TextView productRating;
    private TextView productPrice;
    private TextView productDescription;
    private TextView productSpecifications;
    private TextView quantityText;
    private MaterialButton btnDecrease;
    private MaterialButton btnIncrease;
    private MaterialButton addToCartButton;
    private Product product;
    private int quantity = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Get product from intent
        product = getIntent().getParcelableExtra("product");
        if (product == null) {
            finish();
            return;
        }

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        imageViewPager = findViewById(R.id.image_viewpager);
        productName = findViewById(R.id.product_detail_name);
        productCategory = findViewById(R.id.product_detail_category);
        productRating = findViewById(R.id.product_detail_rating);
        productPrice = findViewById(R.id.product_detail_price);
        productDescription = findViewById(R.id.product_detail_description);
        productSpecifications = findViewById(R.id.product_specifications);
        quantityText = findViewById(R.id.quantity_text);
        btnDecrease = findViewById(R.id.btn_decrease);
        btnIncrease = findViewById(R.id.btn_increase);
        addToCartButton = findViewById(R.id.add_to_cart_button);

        // Setup toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set collapsing toolbar title
        collapsingToolbar.setTitle(product.getName());

        // Populate data
        populateProductDetails();
        setupQuantityControls();

        // Add to cart button
        addToCartButton.setOnClickListener(v -> {
            Toast.makeText(this, "Added " + quantity + "x " + product.getName() + " to cart", Toast.LENGTH_SHORT).show();
        });
    }

    private void populateProductDetails() {
        // Setup image gallery with animation
        ImagePagerAdapter imageAdapter = new ImagePagerAdapter(product.getImageUrls());
        imageViewPager.setAdapter(imageAdapter);
        imageViewPager.setPageTransformer(new ZoomOutPageTransformer());

        productName.setText(product.getName());
        productCategory.setText(product.getCategory());
        productRating.setText(String.format(Locale.getDefault(), "%.1f", product.getRating()));

        // Format currency with thousand separators
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = "₫" + currencyFormat.format(product.getPrice() * 1000);
        productPrice.setText(formattedPrice);

        productDescription.setText(product.getDescription());
        productSpecifications.setText(product.getSpecifications());
    }

    private void setupQuantityControls() {
        btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                quantityText.setText(String.valueOf(quantity));
                animateQuantityChange(quantityText);
            }
        });

        btnIncrease.setOnClickListener(v -> {
            if (quantity < 99) {
                quantity++;
                quantityText.setText(String.valueOf(quantity));
                animateQuantityChange(quantityText);
            }
        });
    }

    private void animateQuantityChange(TextView textView) {
        // Spring animation for scale
        SpringAnimation scaleXAnimation = new SpringAnimation(textView, DynamicAnimation.SCALE_X, 1f);
        scaleXAnimation.setStartValue(1.3f);
        SpringForce springForce = new SpringForce(1f);
        springForce.setStiffness(SpringForce.STIFFNESS_MEDIUM);
        springForce.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        scaleXAnimation.setSpring(springForce);

        SpringAnimation scaleYAnimation = new SpringAnimation(textView, DynamicAnimation.SCALE_Y, 1f);
        scaleYAnimation.setStartValue(1.3f);
        scaleYAnimation.setSpring(springForce);

        scaleXAnimation.start();
        scaleYAnimation.start();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    // Inner class: ImagePagerAdapter
    private static class ImagePagerAdapter extends RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {

        private List<String> imageUrls;

        public ImagePagerAdapter(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }

        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageView imageView = new ImageView(parent.getContext());
            imageView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setBackgroundColor(0xFFEEEEEE);
            return new ImageViewHolder(imageView);
        }

        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            String imageUrl = imageUrls.get(position);

            // Load image from URL using Glide
            Glide.with(holder.imageView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.product_image_placeholder)
                    .error(R.drawable.product_image_placeholder)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .into(holder.imageView);
        }

        @Override
        public int getItemCount() {
            return imageUrls != null ? imageUrls.size() : 0;
        }

        static class ImageViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;

            ImageViewHolder(ImageView imageView) {
                super(imageView);
                this.imageView = imageView;
            }
        }
    }

    // Inner class: ZoomOutPageTransformer
    private static class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
        private static final float MIN_SCALE = 0.85f;
        private static final float MIN_ALPHA = 0.5f;

        @Override
        public void transformPage(@NonNull View view, float position) {
            int pageWidth = view.getWidth();
            int pageHeight = view.getHeight();

            if (position < -1) { // [-Infinity,-1)
                // This page is way off-screen to the left.
                view.setAlpha(0f);
            } else if (position <= 1) { // [-1,1]
                // Modify the default slide transition to shrink the page as well
                float scaleFactor = Math.max(MIN_SCALE, 1 - Math.abs(position));
                float vertMargin = pageHeight * (1 - scaleFactor) / 2;
                float horzMargin = pageWidth * (1 - scaleFactor) / 2;
                if (position < 0) {
                    view.setTranslationX(horzMargin - vertMargin / 2);
                } else {
                    view.setTranslationX(-horzMargin + vertMargin / 2);
                }

                // Scale the page down (between MIN_SCALE and 1)
                view.setScaleX(scaleFactor);
                view.setScaleY(scaleFactor);

                // Fade the page relative to its size.
                view.setAlpha(MIN_ALPHA +
                        (scaleFactor - MIN_SCALE) /
                                (1 - MIN_SCALE) * (1 - MIN_ALPHA));
            } else { // (1,+Infinity]
                // This page is way off-screen to the right.
                view.setAlpha(0f);
            }
        }
    }
}
