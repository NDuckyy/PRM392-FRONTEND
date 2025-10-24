package com.example.prm392_frontend;

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
import java.util.ArrayList; // <-- SỬA: Thêm import này
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    // ====================================================================
    // SỬA 1: Khởi tạo danh sách ngay từ đầu để không bao giờ bị null
    // ====================================================================
    private List<CartItemResponse> cartItems = new ArrayList<>();
    private CartAdapterListener listener;

    // ====================================================================
    // SỬA 2: Xóa bỏ các hàm khởi tạo cũ và chỉ giữ lại một hàm duy nhất,
    // đơn giản để nhận listener từ Activity.
    // ====================================================================
    public CartAdapter(CartAdapterListener listener) {
        this.listener = listener;
    }

    // Interface không đổi
    public interface CartAdapterListener {
        void onUpdateQuantity(int cartItemId, int newQuantity);
        void onDeleteItem(int cartItemId);
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
        // Giờ đây `cartItems` không bao giờ null, nên có thể trả về trực tiếp
        return cartItems.size();
    }

    // ====================================================================
    // SỬA 3: Làm cho hàm updateData an toàn hơn bằng cách kiểm tra
    // dữ liệu mới có phải là null hay không.
    // ====================================================================
    public void updateData(List<CartItemResponse> newCartItems) {
        this.cartItems.clear(); // Dòng này giờ đã an toàn
        if (newCartItems != null) {
            this.cartItems.addAll(newCartItems);
        }
        notifyDataSetChanged();
    }

    // Lớp ViewHolder bên trong không cần thay đổi, giữ nguyên
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProduct;
        TextView textViewProductName, textViewProductPrice;

        // Edit View Components
        ConstraintLayout editViewLayout;
        ImageButton buttonDecrease, buttonIncrease;
        TextView textViewQuantityEdit;
        Button buttonDelete;
        Button buttonConfirmUpdate;

        private int originalQuantity;
        private int currentQuantity;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewProduct = itemView.findViewById(R.id.imageViewProduct);
            textViewProductName = itemView.findViewById(R.id.textViewProductName);
            textViewProductPrice = itemView.findViewById(R.id.textViewProductPrice);
            editViewLayout = itemView.findViewById(R.id.editViewLayout);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            textViewQuantityEdit = itemView.findViewById(R.id.textViewQuantityEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
            buttonConfirmUpdate = itemView.findViewById(R.id.buttonConfirmUpdate);
        }

        public void bind(final CartItemResponse item, final CartAdapterListener listener) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            textViewProductName.setText(item.getProductName());
            textViewProductPrice.setText(currencyFormat.format(item.getPrice()));

            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(imageViewProduct);

            originalQuantity = item.getQuantity();
            currentQuantity = originalQuantity;
            textViewQuantityEdit.setText(String.valueOf(currentQuantity));

            buttonConfirmUpdate.setVisibility(View.GONE);
            buttonDecrease.setEnabled(currentQuantity > 1);

            buttonDecrease.setOnClickListener(v -> {
                if (currentQuantity > 1) {
                    currentQuantity--;
                    textViewQuantityEdit.setText(String.valueOf(currentQuantity));
                    checkIfQuantityChanged();
                }
            });

            buttonIncrease.setOnClickListener(v -> {
                currentQuantity++;
                textViewQuantityEdit.setText(String.valueOf(currentQuantity));
                checkIfQuantityChanged();
            });

            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItem(item.getId());
                }
            });

            buttonConfirmUpdate.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onUpdateQuantity(item.getId(), currentQuantity);
                }
            });
        }

        private void checkIfQuantityChanged() {
            buttonConfirmUpdate.setVisibility(currentQuantity != originalQuantity ? View.VISIBLE : View.GONE);
            buttonDecrease.setEnabled(currentQuantity > 1);
        }
    }


    /**
     * Phương thức mới: Tìm một item trong danh sách dựa vào ID của nó.
     * @param cartItemId ID của mục giỏ hàng cần tìm.
     * @return CartItemResponse nếu tìm thấy, ngược lại trả về null.
     */
    public CartItemResponse findItemById(int cartItemId) {
        for (CartItemResponse item : cartItems) {
            if (item.getId() == cartItemId) {
                return item;
            }
        }
        return null;
    }

    /**
     * Phương thức mới: Cập nhật lại giao diện để hiển thị số lượng cũ của một item.
     * @param cartItemId ID của item cần hoàn tác.
     */
    public void revertItemQuantity(int cartItemId) {
        for (int i = 0; i < cartItems.size(); i++) {
            if (cartItems.get(i).getId() == cartItemId) {
                notifyItemChanged(i); // Ra lệnh cho RecyclerView vẽ lại item ở vị trí 'i'
                return;
            }
        }
    }

}
