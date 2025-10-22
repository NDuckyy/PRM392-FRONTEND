package com.example.prm392_frontend;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class PaymentSuccessActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_success);
        Button buttonBackToHome = findViewById(R.id.buttonGoToProductList);
        buttonBackToHome.setOnClickListener(v -> navigateToHome());
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true /* enabled by default */) {
            @Override
            public void handleOnBackPressed() {
                navigateToHome();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(PaymentSuccessActivity.this, ProductListActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
