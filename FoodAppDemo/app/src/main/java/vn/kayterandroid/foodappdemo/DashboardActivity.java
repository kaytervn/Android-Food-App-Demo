package vn.kayterandroid.foodappdemo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smarteist.autoimageslider.IndicatorView.animation.type.IndicatorAnimationType;
import com.smarteist.autoimageslider.SliderView;

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
import vn.kayterandroid.foodappdemo.adapter.SliderAdapter;
import vn.kayterandroid.foodappdemo.adapter.ViewPager2Adapter;
import vn.kayterandroid.foodappdemo.databinding.ActivityDashboardBinding;
import vn.kayterandroid.foodappdemo.databinding.ActivityProfileBinding;
import vn.kayterandroid.foodappdemo.model.Food;
import vn.kayterandroid.foodappdemo.utils.APIService;
import vn.kayterandroid.foodappdemo.utils.RetrofitClient;
import vn.kayterandroid.foodappdemo.utils.SessionManager;

public class DashboardActivity extends Fragment implements FoodAdapter.RecyclerViewClickListener {
    APIService apiService;
    ImageView imagePicture;
    TextView textName;
    List<Food> listFoods = new ArrayList<>();
    FoodAdapter foodsAdapter;
    RecyclerView recyclerViewFoods;
    EditText editSearch;
    SliderView sliderView;
    ArrayList<Integer> imageList;
    SliderAdapter sliderAdapter;
    ActivityDashboardBinding binding;
    Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    void getUser() {
        String id = SessionManager.getInstance(context).getId();
        if (id == "") {
            Intent intent = new Intent(context, LoginActivity.class);
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
                                Glide.with(context).load(userObject.get("image").getAsString()).into(imagePicture);
                            }
                            imagePicture.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(context, HomeActivity.class);
                                    intent.putExtra("tabIndex", 2);
                                    startActivity(intent);
                                }
                            });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(context, "Response Failed", Toast.LENGTH_SHORT).show();
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
                        foodsAdapter = new FoodAdapter(context, DashboardActivity.this);
                        foodsAdapter.setData(listFoods);
                        recyclerViewFoods.setHasFixedSize(true);
                        recyclerViewFoods.setLayoutManager(new GridLayoutManager(context, 2));
                        recyclerViewFoods.setAdapter(foodsAdapter);
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(context, "Response Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Failed to call API", t.getMessage());
            }
        });
    }

    void mapping() {
        context = getActivity();
        sliderView = binding.imageSlider;
        imagePicture = binding.imagePicture;
        textName = binding.textName;
        recyclerViewFoods = binding.recyclerViewFoods;
        editSearch = binding.editSearch;

        editSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchFood();
                }
                return false;
            }
        });
    }

    void searchFood() {
        String searchText = editSearch.getText().toString().trim();
        apiService = RetrofitClient.getAPIService();
        Call<ResponseBody> call = apiService.search(new Food(searchText));
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    try {
                        List<Food> filterFoods = new ArrayList<>();
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        JSONArray foodsArray = jsonObject.getJSONArray("foods");
                        for (int i = 0; i < foodsArray.length(); i++) {
                            JSONObject foodObject = foodsArray.getJSONObject(i);
                            filterFoods.add(new Food(
                                    foodObject.getString("_id"),
                                    foodObject.getString("image"),
                                    foodObject.getString("title"),
                                    Float.parseFloat(foodObject.getString("price")),
                                    foodObject.getString("description")
                            ));
                        }
                        listFoods = filterFoods;
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                    }
                    if (listFoods.size() == 0) {
                        Toast.makeText(context, "Không có kết quả", Toast.LENGTH_SHORT).show();
                    }
                    foodsAdapter.setData(listFoods);
                } else {
                    Toast.makeText(context, "Response Failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Failed to call API", t.getMessage());
            }
        });
        hideSoftKeyboard();
    }

    void hideSoftKeyboard() {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(requireView().getWindowToken(), 0);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onItemClick(Food food) {
        Intent intent = new Intent(context, FoodDetailActivity.class);
        intent.putExtra("foodId", food.getId());
        startActivity(intent);
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = ActivityDashboardBinding.inflate(inflater, container, false);
        mapping();
        getUser();
        getFoods();

        imageList = new ArrayList<>();
        imageList.add(R.drawable.shopee1);
        imageList.add(R.drawable.shopee2);
        imageList.add(R.drawable.shopee1);
        imageList.add(R.drawable.shopee4);

        sliderAdapter = new SliderAdapter(context, imageList);
        sliderView.setSliderAdapter(sliderAdapter);
        sliderView.setIndicatorAnimation(IndicatorAnimationType.WORM);
        sliderView.setAutoCycleDirection(SliderView.AUTO_CYCLE_DIRECTION_RIGHT);
        sliderView.setIndicatorSelectedColor(getResources().getColor(R.color.white));
        sliderView.setIndicatorUnselectedColor(Color.GRAY);
        sliderView.startAutoCycle();
        sliderView.setScrollTimeInSec(5);
        return binding.getRoot();
    }
}