package vn.kayterandroid.foodappdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import vn.kayterandroid.foodappdemo.adapter.FoodAdapter;
import vn.kayterandroid.foodappdemo.model.Food;
import vn.kayterandroid.foodappdemo.utils.APIService;
import vn.kayterandroid.foodappdemo.utils.RetrofitClient;
import vn.kayterandroid.foodappdemo.utils.SessionManager;

public class DashboardActivity extends AppCompatActivity implements FoodAdapter.RecyclerViewClickListener {
    APIService apiService;
    ImageView imagePicture;
    TextView textName;
    List<Food> listFoods = new ArrayList<>();
    FoodAdapter foodsAdapter;
    RecyclerView recyclerViewFoods;
    FloatingActionButton buttonCart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mapping();
        getFoods();
        getUser();
    }

    void getUser() {
        String id = SessionManager.getInstance(getApplicationContext()).getId();
        if (id == "") {
            Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
            startActivity(intent);
        } else {
            apiService = RetrofitClient.getAPIService();
            Call<ResponseBody> call = apiService.getUser(id);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    if (response.isSuccessful()) {
                        try {
                            String json = response.body().string();
                            JsonObject userObject = new Gson().fromJson(json, JsonObject.class).getAsJsonObject("user");
                            textName.setText(userObject.get("name").getAsString());
                            if (userObject.get("image").getAsString().length() > 0) {
                                Glide.with(getApplicationContext()).load(userObject.get("image").getAsString()).into(imagePicture);
                            }
                            imagePicture.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                                    startActivity(intent);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "Response Failed", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.d("Failed to call API", t.getMessage());
                }
            });
        }
    }

    void getFoods() {
        apiService = RetrofitClient.getAPIService();
        Call<ResponseBody> call = apiService.getFoods();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        JSONArray foodsArray = jsonObject.getJSONArray("foods");
                        for (int i = 0; i < foodsArray.length(); i++) {
                            JSONObject foodObject = foodsArray.getJSONObject(i);
                            listFoods.add(new Food(
                                    foodObject.getString("_id"),
                                    foodObject.getString("image"),
                                    foodObject.getString("title"),
                                    Float.parseFloat(foodObject.getString("price")),
                                    foodObject.getString("description")
                            ));
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    foodsAdapter = new FoodAdapter(DashboardActivity.this, listFoods, DashboardActivity.this);
                    recyclerViewFoods.setHasFixedSize(true);
                    recyclerViewFoods.setLayoutManager(new GridLayoutManager(getApplicationContext(), 2));
                    recyclerViewFoods.setAdapter(foodsAdapter);
                    foodsAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(getApplicationContext(), "Response Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Failed to call API", t.getMessage());
            }
        });
    }

    void mapping() {
        imagePicture = findViewById(R.id.imagePicture);
        textName = findViewById(R.id.textName);
        recyclerViewFoods = findViewById(R.id.recyclerViewFoods);
        buttonCart = findViewById(R.id.buttonCart);
        buttonCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DashboardActivity.this, CartActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onItemClick(Food food) {
        Intent intent = new Intent(DashboardActivity.this, FoodDetailActivity.class);
        intent.putExtra("foodId", food.getId());
        startActivity(intent);
    }
}