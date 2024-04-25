package vn.kayterandroid.foodappdemo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.kayterandroid.foodappdemo.adapter.CartAdapter;
import vn.kayterandroid.foodappdemo.dao.CartItemDatabase;
import vn.kayterandroid.foodappdemo.model.CartItem;
import vn.kayterandroid.foodappdemo.utils.SessionManager;

public class CartActivity extends AppCompatActivity {
    List<CartItem> cartItems = new ArrayList<>();
    CartAdapter cartAdapter;
    RecyclerView recyclerViewCartItems;
    String userId;
    Button buttonContinueShopping;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);
        recyclerViewCartItems = findViewById(R.id.recyclerViewCartItems);
        buttonContinueShopping = findViewById(R.id.buttonContinueShopping);
        userId = SessionManager.getInstance(getApplicationContext()).getId();
        buttonContinueShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CartActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        cartItems = CartItemDatabase.getInstance(this).cartItemDAO().getAll(userId);
        cartAdapter = new CartAdapter(this, new CartAdapter.OnItemClickListener() {
            @Override
            public void onIncreaseButtonClick(CartItem cartItem) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                CartItemDatabase.getInstance(CartActivity.this).cartItemDAO().updateCartItem(cartItem);
                loadData();
            }

            @Override
            public void onDecreaseButtonClick(CartItem cartItem) {
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                    CartItemDatabase.getInstance(CartActivity.this).cartItemDAO().updateCartItem(cartItem);
                    loadData();
                }
            }

            @Override
            public void onDeleteButtonClick(CartItem cartItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CartActivity.this);
                builder.setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng không?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CartItemDatabase.getInstance(CartActivity.this).cartItemDAO().deleteCartItem(cartItem);
                                loadData();
                            }
                        })
                        .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        })
                        .show();
            }
        });

        cartAdapter.setData(cartItems);
        recyclerViewCartItems.setHasFixedSize(false);
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerViewCartItems.setAdapter(cartAdapter);
    }

    void loadData() {
        cartItems = CartItemDatabase.getInstance(this).cartItemDAO().getAll(userId);
        cartAdapter.setData(cartItems);
    }
}