package com.example.prm392_frontend;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.prm392_frontend.models.CartItemResponse;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItemResponse> cartItems;
    private Context context;
    private CartAdapterListener listener;

    public interface CartAdapterListener {
        void onUpdateQuantity(int cartItemId, int newQuantity);
        void onDeleteItem(int cartItemId);
    }

    public CartAdapter(Context context, List<CartItemResponse> cartItems, CartAdapterListener listener) {
        this.context = context;
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItemResponse item = cartItems.get(position);
        holder.bind(item, listener);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName, textViewProductPrice;

        // Edit View Components
        ConstraintLayout editViewLayout;
        ImageButton buttonDecrease, buttonIncrease;
        TextView textViewQuantityEdit;
        Button buttonDelete; // Đổi tên từ buttonCancelUpdate để rõ nghĩa hơn
        Button buttonConfirmUpdate;

        private int originalQuantity;
        private int currentQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            // Ánh xạ các view
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);

            // Ánh xạ Edit View
            editViewLayout = itemView.findViewById(R.id.editViewLayout);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            textViewQuantityEdit = itemView.findViewById(R.id.textViewQuantityEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete); // Ánh xạ nút xóa
            buttonConfirmUpdate = itemView.findViewById(R.id.buttonConfirmUpdate);
        }

        public void bind(final CartItemResponse item, final CartAdapterListener listener) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            // Binding dữ liệu
            textViewProductName.setText(item.getProductName());
            textViewProductPrice.setText(currencyFormat.format(item.getPrice()));

            // Sử dụng Glide để load ảnh
            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imageViewProduct);

            // Lưu và hiển thị số lượng
            originalQuantity = item.getQuantity();
            currentQuantity = originalQuantity;
            textViewQuantityEdit.setText(String.valueOf(currentQuantity));

            // Ban đầu, nút "Cập nhật" sẽ bị ẩn đi
            buttonConfirmUpdate.setVisibility(View.GONE);
            // Và nút "-" sẽ bị vô hiệu hóa nếu số lượng là 1
            buttonDecrease.setEnabled(currentQuantity > 1);


            // ================== XỬ LÝ SỰ KIỆN CLICK ==================

            // 1. Nhấn nút Giảm (-)
            buttonDecrease.setOnClickListener(v -> {
                if (currentQuantity > 1) {
                    currentQuantity--;
                    textViewQuantityEdit.setText(String.valueOf(currentQuantity));
                    checkIfQuantityChanged();
                }
            });

            // 2. Nhấn nút Tăng (+)
            buttonIncrease.setOnClickListener(v -> {
                currentQuantity++;
                textViewQuantityEdit.setText(String.valueOf(currentQuantity));
                checkIfQuantityChanged();
            });

            // 3. Nhấn nút "Xóa"
            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItem(item.getId());
                }
            });

            // 4. Nhấn nút "Cập nhật"
            buttonConfirmUpdate.setOnClickListener(v -> {
                if (listener != null) {
                    // Gọi API để cập nhật số lượng
                    listener.onUpdateQuantity(item.getId(), currentQuantity);

                    // Sau khi gọi API thành công, bạn nên cập nhật lại originalQuantity
                    // và ẩn nút Cập nhật đi trong hàm callback ở Activity.
                    // Ví dụ: originalQuantity = currentQuantity;
                    //        checkIfQuantityChanged();
                }
            });
        }

        // Hàm kiểm tra xem số lượng có thay đổi không để ẩn/hiện nút "Cập nhật"
        private void checkIfQuantityChanged() {
            // Hiện/ẩn nút "Cập nhật"
            if (currentQuantity != originalQuantity) {
                buttonConfirmUpdate.setVisibility(View.VISIBLE);
            } else {
                buttonConfirmUpdate.setVisibility(View.GONE);
            }

            // Bật/tắt nút giảm
            // Điều này ngăn người dùng giảm số lượng xuống dưới 1
            buttonDecrease.setEnabled(currentQuantity > 1);
        }
    }

    public void updateData(List<CartItemResponse> newCartItems) {
        this.cartItems.clear();
        this.cartItems.addAll(newCartItems);
        notifyDataSetChanged();
    }
}
