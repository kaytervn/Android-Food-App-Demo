package vn.kayterandroid.foodappdemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import vn.kayterandroid.foodappdemo.adapter.CartAdapter;
import vn.kayterandroid.foodappdemo.dao.CartItemDatabase;
import vn.kayterandroid.foodappdemo.databinding.ActivityCartBinding;
import vn.kayterandroid.foodappdemo.databinding.ActivityDashboardBinding;
import vn.kayterandroid.foodappdemo.model.CartItem;
import vn.kayterandroid.foodappdemo.utils.SessionManager;

public class CartActivity extends Fragment {
    List<CartItem> cartItems = new ArrayList<>();
    CartAdapter cartAdapter;
    RecyclerView recyclerViewCartItems;
    String userId;
    Button buttonContinueShopping;
    ActivityCartBinding binding;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void mapping() {
        context = getActivity();
        recyclerViewCartItems = binding.recyclerViewCartItems;
        buttonContinueShopping = binding.buttonContinueShopping;
        userId = SessionManager.getInstance(context).getId();
        buttonContinueShopping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, HomeActivity.class);
                intent.putExtra("tabIndex", 0);
                startActivity(intent);
            }
        });
        cartItemsMapping();
    }

    void cartItemsMapping() {

        cartItems = CartItemDatabase.getInstance(context).cartItemDAO().getAll(userId);
        cartAdapter = new CartAdapter(context, new CartAdapter.OnItemClickListener() {
            @Override
            public void onIncreaseButtonClick(CartItem cartItem) {
                cartItem.setQuantity(cartItem.getQuantity() + 1);
                CartItemDatabase.getInstance(context).cartItemDAO().updateCartItem(cartItem);
                loadData();
            }

            @Override
            public void onDecreaseButtonClick(CartItem cartItem) {
                if (cartItem.getQuantity() > 1) {
                    cartItem.setQuantity(cartItem.getQuantity() - 1);
                    CartItemDatabase.getInstance(context).cartItemDAO().updateCartItem(cartItem);
                    loadData();
                }
            }

            @Override
            public void onDeleteButtonClick(CartItem cartItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Xác nhận xóa")
                        .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng không?")
                        .setPositiveButton("Có", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                CartItemDatabase.getInstance(context).cartItemDAO().deleteCartItem(cartItem);
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
        recyclerViewCartItems.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewCartItems.setAdapter(cartAdapter);
    }

    void loadData() {
        cartItems = CartItemDatabase.getInstance(context).cartItemDAO().getAll(userId);
        cartAdapter.setData(cartItems);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = ActivityCartBinding.inflate(inflater, container, false);
        mapping();
        return binding.getRoot();
    }
}