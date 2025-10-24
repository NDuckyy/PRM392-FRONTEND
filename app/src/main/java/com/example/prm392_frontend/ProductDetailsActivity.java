package com.example.prm392_frontend;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Thêm import
import android.view.Gravity;
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
import com.example.prm392_frontend.api.ApiClient; // Thêm import
import com.example.prm392_frontend.models.ApiResponse; // Thêm import
import com.example.prm392_frontend.models.CartAddRequest; // Thêm import
import com.example.prm392_frontend.models.LocationResponse;
import com.example.prm392_frontend.models.ProductResponse;
import com.example.prm392_frontend.utils.AuthHelper;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call; // Thêm import
import retrofit2.Callback; // Thêm import
import retrofit2.Response; // Thêm import

public class ProductDetailsActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailsActivity"; // Thêm TAG để log
    private static final int REQUEST_CODE_LOGIN = 1001;

    private ViewPager2 imageViewPager;
    private TextView productName;
    private TextView productCategory;
    private TextView productPrice;
    private TextView productDescription;
    private TextView productSpecifications;
    private TextView quantityText;
    private MaterialButton btnDecrease;
    private MaterialButton btnIncrease;
    private MaterialButton addToCartButton;
    private TextView tvProviderName;
    private com.google.android.material.card.MaterialCardView providerCard;
    private Product product;
    private int quantity = 1;
    private AuthHelper authHelper;

    private FloatingActionButton btnDirection;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_details);

        // Initialize AuthHelper
        authHelper = new AuthHelper(this);

        // Get product from intent
        product = getIntent().getParcelableExtra("product");
        if (product == null) {
            finish();
            return;
        }
        btnDirection = findViewById(R.id.btn_direction);
        btnDirection.setOnClickListener(v -> fetchLocationAndOpenMyMap());

        // Initialize views
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapsing_toolbar);
        imageViewPager = findViewById(R.id.image_viewpager);
        productName = findViewById(R.id.product_detail_name);
        productCategory = findViewById(R.id.product_detail_category);
        productPrice = findViewById(R.id.product_detail_price);
        productDescription = findViewById(R.id.product_detail_description);
        productSpecifications = findViewById(R.id.product_specifications);
        quantityText = findViewById(R.id.quantity_text);
        btnDecrease = findViewById(R.id.btn_decrease);
        btnIncrease = findViewById(R.id.btn_increase);
        addToCartButton = findViewById(R.id.add_to_cart_button);
        tvProviderName = findViewById(R.id.tvProviderName);
        providerCard = findViewById(R.id.provider_card);

        // Setup toolbar
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Set collapsing toolbar title (only show when collapsed, hide when expanded to show image clearly)
        collapsingToolbar.setTitle(" ");

        // Populate data
        populateProductDetails();
        setupQuantityControls();
        setupProviderCard();

        // Add to cart button - check login first
        addToCartButton.setOnClickListener(v -> {
            if (authHelper.isLoggedIn()) {
                // User is logged in, add to cart
                addToCart();
            } else {
                // User not logged in, redirect to login
                Toast.makeText(this, "Please login to add items to cart", Toast.LENGTH_SHORT).show();
                Intent loginIntent = new Intent(ProductDetailsActivity.this, LoginActivity.class);
                // Pass product info to return after login
                loginIntent.putExtra("return_to_product", true);
                loginIntent.putExtra("product", product);
                loginIntent.putExtra("quantity", quantity);
                startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
            }
        });
    }

    private void populateProductDetails() {
        // Setup image gallery with animation
        ImagePagerAdapter imageAdapter = new ImagePagerAdapter(product.getImageUrls());
        imageViewPager.setAdapter(imageAdapter);
        imageViewPager.setPageTransformer(new ZoomOutPageTransformer());

        productName.setText(product.getName());
        productCategory.setText(product.getCategory());

        // Format currency with thousand separators
        NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));
        String formattedPrice = "₫" + currencyFormat.format(product.getPrice());
        productPrice.setText(formattedPrice);

        productDescription.setText(product.getDescription());
        productSpecifications.setText(product.getSpecifications());
    }

    private void setupProviderCard() {
        // Hide provider card if current user is a provider (providers cannot chat with other providers)
        if (authHelper.isProvider()) {
            providerCard.setVisibility(View.GONE);
            return;
        }

        // Display provider name
        String providerName = product.getProviderName();
        if (providerName != null && !providerName.isEmpty()) {
            tvProviderName.setText(providerName);
        } else {
            tvProviderName.setText("Unknown Provider");
        }

        // Set click listener to open chat
        providerCard.setOnClickListener(v -> openChatWithProvider());
    }

    private void openChatWithProvider() {
        if (!authHelper.isLoggedIn()) {
            Toast.makeText(this, "Please login to chat with provider", Toast.LENGTH_SHORT).show();
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
            return;
        }

        String providerId = String.valueOf(product.getProviderId());
        String providerName = product.getProviderName();

        if (providerId != null && !providerId.isEmpty()) {
            // Create conversation ID: currentUserId-providerId
            String currentUserId = authHelper.getUsername();
            String conversationId = currentUserId + "-" + providerId;

            Intent chatIntent = new Intent(this, ChatActivity.class);
            chatIntent.putExtra("conversationId", conversationId);
            chatIntent.putExtra("receiverId", providerId);
            chatIntent.putExtra("receiverName", providerName != null ? providerName : "Provider");
            startActivity(chatIntent);
        } else {
            Toast.makeText(this, "Provider information not available", Toast.LENGTH_SHORT).show();
        }
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
            // Chỉ tăng số lượng, không kiểm tra tồn kho ở đây
            // Đặt một giới hạn hợp lý để người dùng không bấm vô tận
            if (quantity < 999) {
                quantity++;
                quantityText.setText(String.valueOf(quantity));
                animateQuantityChange(quantityText);
            }
        });
    }


    /**
     * Hiển thị một Toast tùy chỉnh với thông báo lỗi và số lượng tồn kho.
     * @param stockAvailable Số lượng sản phẩm thực tế còn trong kho. */
    private void showErrorToast(int stockAvailable) {
        // Inflate layout tùy chỉnh từ file activity_error_cart.xml
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.activity_error_cart, null);

        // Tìm các TextView trong layout đã inflate
        TextView textErrorMessage = layout.findViewById(R.id.textViewErrorMessage);
        TextView textStockInfo = layout.findViewById(R.id.textViewStockInfo);

        // Thiết lập nội dung cho các TextView
        textErrorMessage.setText("Số lượng vượt quá giới hạn!");
        if (stockAvailable > 0) {
            textStockInfo.setText("Chỉ còn " + stockAvailable + " sản phẩm trong kho.");
        } else {
            textStockInfo.setText("Sản phẩm này đã hết hàng.");
        }

        // Tạo và hiển thị Toast
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
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

    private void addToCart() {
        // Vô hiệu hóa nút để ngăn người dùng nhấn liên tục
        addToCartButton.setEnabled(false);
        String originalButtonText = addToCartButton.getText().toString();
        addToCartButton.setText("Checking stock..."); // Cập nhật text để người dùng biết

        // Bước 1: Gọi API để lấy thông tin sản phẩm mới nhất (bao gồm cả stockQuantity)
        int productId = product.getId();
        ApiClient.getProductById(productId).enqueue(new Callback<ApiResponse<ProductResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<ProductResponse>> call, Response<ApiResponse<ProductResponse>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Lấy được thông tin sản phẩm thành công
                    ProductResponse productDetails = response.body().getData();
                    int stockQuantity = productDetails.getStockQuantity();

                    // Bước 2: So sánh số lượng người dùng chọn với số lượng trong kho
                    if (quantity > stockQuantity) {
                        // Nếu số lượng chọn LỚN HƠN số lượng trong kho -> BÁO LỖI
                        showErrorToast(stockQuantity);
                        // Kích hoạt lại nút và trả lại text cũ
                        addToCartButton.setEnabled(true);
                        addToCartButton.setText(originalButtonText);
                    } else {
                        // Nếu số lượng hợp lệ -> TIẾN HÀNH GỌI API THÊM VÀO GIỎ HÀNG
                        performAddToCart(originalButtonText);
                    }

                } else {
                    // Lỗi khi không lấy được thông tin sản phẩm
                    Toast.makeText(ProductDetailsActivity.this, "Could not check stock. Please try again.", Toast.LENGTH_SHORT).show();
                    addToCartButton.setEnabled(true);
                    addToCartButton.setText(originalButtonText);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductResponse>> call, Throwable t) {
                // Lỗi kết nối mạng
                Toast.makeText(ProductDetailsActivity.this, "Network error while checking stock.", Toast.LENGTH_SHORT).show();
                addToCartButton.setEnabled(true);
                addToCartButton.setText(originalButtonText);
            }
        });
    }

    private void performAddToCart(String originalButtonText) {
        addToCartButton.setText("Adding..."); // Cập nhật text

        String token = "Bearer " + authHelper.getToken();
        CartAddRequest request = new CartAddRequest(product.getId(), quantity);

        ApiClient.addProductToCart(token, request).enqueue(new Callback<ApiResponse<Object>>() {
            @Override
            public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                // Re-enable button
                addToCartButton.setEnabled(true);
                addToCartButton.setText(originalButtonText);

                if (response.isSuccessful() && response.body() != null) {
                    // Show success message
                    Toast.makeText(ProductDetailsActivity.this, "Added to cart successfully!", Toast.LENGTH_SHORT).show();

                    // Animate button on success
                    animateButtonSuccess();
                } else {
                    // Handle API error (e.g., product out of stock, invalid request)
                    String errorMessage = "Failed to add to cart. Please try again.";
                    if (response.body() != null && response.body().getMessage() != null) {
                        errorMessage = response.body().getMessage();
                    }
                    Toast.makeText(ProductDetailsActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "API Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                // Re-enable button
                addToCartButton.setEnabled(true);
                addToCartButton.setText(originalButtonText);

                // Handle network failure
                Toast.makeText(ProductDetailsActivity.this, "Network error. Please check your connection.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "Network Failure: ", t);
            }
        });
    }

    private void animateButtonSuccess() {
        // Animate button
        SpringAnimation scaleX = new SpringAnimation(addToCartButton, DynamicAnimation.SCALE_X, 1f);
        scaleX.setStartValue(0.9f);
        SpringForce springForce = new SpringForce(1f);
        springForce.setStiffness(SpringForce.STIFFNESS_MEDIUM);
        springForce.setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
        scaleX.setSpring(springForce);

        SpringAnimation scaleY = new SpringAnimation(addToCartButton, DynamicAnimation.SCALE_Y, 1f);
        scaleY.setStartValue(0.9f);
        scaleY.setSpring(springForce);

        scaleX.start();
        scaleY.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_LOGIN && resultCode == RESULT_OK) {
            // User logged in successfully, now add to cart
            if (authHelper.isLoggedIn()) {
                // Update AuthHelper to make sure it has the latest user info
                authHelper = new AuthHelper(this);
                addToCart();
            }
        }
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

    private void fetchLocationAndOpenMyMap() {
        btnDirection.setEnabled(false);

        String providerName;
        try {
            providerName = product.getProviderId();
        } catch (Exception e) {
            Toast.makeText(this, "Provider ID không hợp lệ", Toast.LENGTH_SHORT).show();
            btnDirection.setEnabled(true);
            return;
        }

        ApiClient.getLocationByName(providerName).enqueue(new retrofit2.Callback<com.example.prm392_frontend.models.LocationResponse>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.prm392_frontend.models.LocationResponse> call,
                                   retrofit2.Response<com.example.prm392_frontend.models.LocationResponse> response) {
                btnDirection.setEnabled(true);
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ProductDetailsActivity.this, "Không lấy được vị trí cửa hàng", Toast.LENGTH_LONG).show();
                    return;
                }

                com.example.prm392_frontend.models.LocationResponse body = response.body();
                double lat = body.latitude;
                double lng = body.longitude;
                String label = (body.provider != null && body.provider.providerName != null)
                        ? body.provider.providerName
                        : (product.getProviderName() != null ? product.getProviderName() : "Cửa hàng");

                // ✅ Mở MapsActivity thay vì Google Maps
                Intent intent = new Intent(ProductDetailsActivity.this, MapsActivity.class);
                intent.putExtra("store_lat", lat);
                intent.putExtra("store_lng", lng);
                intent.putExtra("store_address", label);
                startActivity(intent);
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.prm392_frontend.models.LocationResponse> call, Throwable t) {
                btnDirection.setEnabled(true);
                Toast.makeText(ProductDetailsActivity.this, "Lỗi mạng. Vui lòng thử lại.", Toast.LENGTH_LONG).show();
                Log.e(TAG, "fetchLocationAndOpenMyMap failure", t);
            }
        });
    }


}
