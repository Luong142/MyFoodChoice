package com.example.myfoodchoice.UserFragment;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myfoodchoice.Adapter.DishGuestUserAdapter;
import com.example.myfoodchoice.AdapterInterfaceListener.OnDishClickListener;
import com.example.myfoodchoice.ModelCaloriesNinja.FoodItem;
import com.example.myfoodchoice.ModelMeal.Meal;
import com.example.myfoodchoice.ModelSignUp.UserProfile;
import com.example.myfoodchoice.R;
import com.example.myfoodchoice.RetrofitProvider.CaloriesNinjaAPI;
import com.example.myfoodchoice.RetrofitProvider.RetrofitClient;
import com.example.myfoodchoice.UserActivity.UserLogMealActivity;
import com.example.myfoodchoice.ml.Model;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.jetbrains.annotations.Contract;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLogMealNutritionAnalysisFragment extends Fragment implements OnDishClickListener
{
    final static String TAG = "UserLogMealNutritionFragment";
    int imageSize;

    // TODO: declare UI components
    DatabaseReference databaseReferenceUserProfile,
            databaseReferenceDailyFoodIntake,
            databaseReferenceDailyFoodIntakeChild,
            databaseReferenceAccount;
    ImageView foodImage;

    TextView cholesterolTextView, sugarTextView, saltTextView, caloriesTextView;

    TextView checkInTextView, foodNameTextView;

    // TODO: add in one more button for taking photo I think.
    FloatingActionButton takePhotoBtn, uploadPhotoBtn;

    CardView logMealBtn;

    LinearLayout addDishBtn;

    ActivityResultLauncher<Intent> uploadPhotoactivityResultLauncher;

    ActivityResultLauncher<Intent> takePhotoActivityResultLauncher;

    ActivityResultLauncher<String[]> requestPermissionLauncher;

    // for image file

    Bitmap image;

    int dimension;

    // calling calories ninja API
    private CaloriesNinjaAPI caloriesNinjaAPI;

    private FoodItem foodItem;
    FoodItem.Item  item;

    List<FoodItem.Item> foodItems;
    // firebase

    FirebaseAuth firebaseAuth;

    FirebaseDatabase firebaseDatabase;

    FirebaseUser firebaseUser;

    UserProfile userProfile;

    String userID, gender, foodName;

    double maxCalories, maxCholesterol, maxSugar, maxSalt;

    boolean isDiabetes, isHighBloodPressure, isHighCholesterol;

    Intent intentNavToLogMeal;

    final static String PATH_USERPROFILE = "User Profile"; // FIXME: the path need to access the account.

    Meal meal;
    double totalCalories, totalCholesterol, totalSalt, totalSugar;

    RecyclerView dishRecyclerView;

    DishGuestUserAdapter dishGuestUserAdapter;
    private Uri selectedImageUri;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);


        // TODO: init Firebase Database
        firebaseDatabase = FirebaseDatabase.getInstance
                ("https://myfoodchoice-dc7bd-default-rtdb.asia-southeast1.firebasedatabase.app/");

        // TODO: init Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // TODO: init user id
        firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null)
        {
            gender = "Male"; // fixme: by default value
            userID = firebaseUser.getUid();

            // TODO: init database reference for user profile
            databaseReferenceUserProfile =
                    firebaseDatabase.getReference(PATH_USERPROFILE).child(userID);

            databaseReferenceUserProfile.addValueEventListener(onGenderHealthValueListener());
        }

        meal = new Meal();

        // set the food item in the Meal object
        foodItem = new FoodItem();
        meal.setDishes(foodItem);

        item = new FoodItem.Item();

        // init nutrition value to 0
        totalCalories = 0;
        totalCholesterol = 0;
        totalSalt = 0;
        totalSugar = 0;

        // todo: init boolean value
        isDiabetes = false;
        isHighBloodPressure = false;
        isHighCholesterol = false;

        // TODO: init UI components
        checkInTextView = view.findViewById(R.id.checkInTextView);
        foodNameTextView = view.findViewById(R.id.foodName);

        // fixme: should be matched with the ID.
        // progressBarCalories = view.findViewById(R.id.progressBarCalories);
        //progressCaloriesTextView = view.findViewById(R.id.progressCaloriesTextView); // id name incorrect

        //progressBarCholesterol = view.findViewById(R.id.progressBarCholesterol);
        //progressCholesterolTextView = view.findViewById(R.id.progressCholesterolTextView);

        //progressBarSalt = view.findViewById(R.id.progressBarSodium);
        //progressSaltTextView = view.findViewById(R.id.progressSodiumTextView);

        //progressBarSugar = view.findViewById(R.id.progressBarSugar);
        //progressSugarTextView = view.findViewById(R.id.progressSugarTextView);

        // todo: init text view for nutrition value
        caloriesTextView = view.findViewById(R.id.caloriesTextView);
        cholesterolTextView = view.findViewById(R.id.cholesterolTextView);
        saltTextView = view.findViewById(R.id.sodiumTextView);
        sugarTextView = view.findViewById(R.id.sugarTextView);

        takePhotoBtn = view.findViewById(R.id.takePhotoBtn);
        uploadPhotoBtn = view.findViewById(R.id.uploadPhotoBtn);

        // todo: this is actually a card view.
        logMealBtn = view.findViewById(R.id.logMealBtn);
        logMealBtn.setOnClickListener(onNavToLogMealListener());

        // init add dish btn card view
        addDishBtn = view.findViewById(R.id.addDishBtn);
        addDishBtn.setOnClickListener(onAddDishListener());

        // init and set recycler view
        foodItems = new ArrayList<>();
        dishRecyclerView = view.findViewById(R.id.dishRecyclerView);
        dishGuestUserAdapter = new DishGuestUserAdapter(foodItems, this);
        setAdapter();
        dishRecyclerView.setVerticalScrollBarEnabled(true);

        foodImage = view.findViewById(R.id.foodPhoto);

        // todo: set onclick here
        // logMealBtn.setOnClickListener(onNavToLogMealListener());
        // historyMealBtn.setOnClickListener(onNavToHistoryMealListener());
        uploadPhotoBtn.setOnClickListener(onUploadPhotoListener());
        takePhotoBtn.setOnClickListener(onTakePhotoListener());

        // todo: init API
        caloriesNinjaAPI = RetrofitClient.getRetrofitInstance().create(CaloriesNinjaAPI.class);

        imageSize = 224; // important?

        // for camera activity
        takePhotoActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), onTakePhotoActivityLauncher());

        // for upload photo
        uploadPhotoactivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), onUploadPhotoActivityLauncher());

        // for permission
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), onPermissionLauncher());

    }

    @NonNull
    @Contract(" -> new")
    private ValueEventListener onGenderHealthValueListener()
    {
        return new ValueEventListener()
        {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot)
            {
                userProfile = snapshot.getValue(UserProfile.class);

                if (userProfile != null)
                {
                    gender = userProfile.getGender();
                    switch(gender)
                    {
                        // todo: important here this should be a goal or something else?
                        case "Male":
                            maxCalories = 3000; // per calories
                            maxCholesterol = 300; // per mg
                            maxSugar = 36; // per grams
                            maxSalt = 2300; // per mg
                            break;
                        case "Female":
                            maxCalories = 2000;
                            maxCholesterol = 240;
                            maxSugar = 24;
                            maxSalt = 2300; // per mg, should be sodium
                            break;
                        default:
                            // wrong gender no default value.
                            Log.d(TAG, "Unknown gender: " + gender);
                            break;
                    }

                    // todo: for health
                    isDiabetes = userProfile.isDiabetes();
                    isHighBloodPressure = userProfile.isHighBloodPressure();
                    isHighCholesterol = userProfile.isHighCholesterol();

                    StringBuilder alertDialogMessage = new StringBuilder();

                    if (isDiabetes)
                    {
                        maxCalories *= 0.5; // minus 50%
                        alertDialogMessage.append("Diabetes detected. " +
                                        "Your calorie limit has been reduced to ")
                                .append((int) maxCalories)
                                .append(" calories to help manage your condition.\n");
                    }

                    if (isHighBloodPressure)
                    {
                        maxSalt *= 0.5; // minus 50%
                        alertDialogMessage.append("High blood pressure detected. " +
                                        "Your salt intake limit has been reduced to ")
                                .append((int) maxSalt)
                                .append(" mg to help manage your condition.\n");
                    }

                    if (isHighCholesterol)
                    {
                        maxCholesterol *= 0.5;
                        alertDialogMessage.append("High cholesterol detected. " +
                                        "Your cholesterol limit has been reduced to ")
                                .append((int) maxCholesterol)
                                .append(" mg to help manage your condition.\n");
                    }
                    if (alertDialogMessage.length() > 0)
                    {
                        // Create an AlertDialog Builder
                        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());

                        // Set the message to display
                        builder.setMessage(alertDialogMessage.toString());

                        // Set the positive button
                        builder.setPositiveButton("OK", (dialog, which) ->
                        {
                            // handle here
                            dialog.dismiss();
                        });

                        // Create and show the AlertDialog
                        AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {
                Log.d(TAG, "onCancelled: " + error);
            }
        };
    }

    public void classifyImage(@NonNull Bitmap image) // todo: algo using tensorflow lite to label image.
    {
        try {
            Model model = Model.newInstance(requireActivity().getApplicationContext());

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

            // todo: a bit hard to do later we will settle this.
            ByteBuffer byteBuffer = ByteBuffer.allocate(4 * imageSize  * imageSize * 3);
            byteBuffer.order(ByteOrder.nativeOrder());

            // what is this code about, using double for loop to put the float value inside of that byteBuffer.
            int [] intValues = new int[imageSize * imageSize];
            image.getPixels(intValues, 0, image.getWidth(), 0, 0, image.getWidth(), image.getHeight());
            int pixel = 0;
            for (int i = 0; i < imageSize; i++)
            {
                for (int j = 0; j < imageSize; j++)
                {
                    int val = intValues[pixel++];
                    // todo: predefined value to normalize the extraction of RGB.

                    // todo: RED
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255.f));

                    // todo: GREEN
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255.f));

                    // todo: BLUE
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255.f));
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Model.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();
            // find the index of the class with the biggest confidence.
            int maxPos = 0;
            float maxConfidence = 0;
            for(int i = 0; i < confidences.length; i++)
            {
                if(confidences[i] > maxConfidence)
                {
                    // todo: pls note that confidence is currently not being used, only maxPos.
                    maxConfidence = confidences[i];
                    maxPos = i;
                }
            }

            String[] classes = {"Nasi Lemak", "Kaya Toast", "Curry Puff", "Sliced Fish Soup"};
            // fixme: eggs need to remove, we can add Laksa

            // result.setText(classes[maxPos]);
            // todo: need to test image recognition algo.
            foodName = classes[maxPos];
            // Log.d(TAG, "The dish name is classified as: " + foodName);
            foodNameTextView.setText(foodName);

            // call API, and get result with that model class.
            Call<FoodItem> call = caloriesNinjaAPI.getFoodItem(foodName);
            // todo: uncomment this part below to do get calories info and more from this API.
            call.enqueue(callBackResponseFromAPI());

            // todo: input from user when search for recipe,
            // todo: if the "ingredients" contains the "allergies", we can show warning contains "nuts" to user, best option.
            // todo: 3 options

            StringBuilder s = new StringBuilder();
            for(int i = 0; i < classes.length; i++)
            {
                s.append(String.format(Locale.ROOT, "%s: %.1f%%\n", classes[i], confidences[i] * 100));
            }
            // confidence.setText(s);
            // Log.d(TAG, "The dish info is: \n" + s);

            // Releases model resources if no longer used.
            model.close();
        }
        catch (IOException e)
        {
            // TODO Handle the exception
            Log.d(TAG, "ClassifyImage: " + e.getMessage());
        }
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onAddDishListener()
    {
        return v ->
        {
            if (item.getName() == null || item.getFoodImage() == null)
            {
                Toast.makeText(requireContext(), "Please select a dish", Toast.LENGTH_SHORT).show();
                return;
            }

            // fixme: there might be a problem with the dish
            Toast.makeText(requireContext(), "Dish is added.", Toast.LENGTH_SHORT).show();

            // Ensure the list is initialized before adding an item
            if (meal.getDishes().getItems() == null)
            {
                meal.getDishes().setItems(new ArrayList<>());
            }

            // this object for the next activity to record.
            meal.getDishes().getItems().add(item);

            // this one is for adapter which means for UI to show.
            foodItems.add(item);

            /*
            for (FoodItem.Item dish : meal.getDishes().getItems())
            {
                Log.d(TAG, "real object: " + dish);
            }

            Log.d(TAG, "adapter object: " + foodItems);
             */

            // fixme: there is another problem that the food item can be duplicated.
            // Log.d(TAG, "onAddDishListener: " + foodItems);
            dishGuestUserAdapter.notifyItemInserted(foodItems.size() - 1);
        };
    }

    @Override
    public void onClickDish(int position)
    {
        // todo: to remove the dish from the list.
        if (meal.getDishes().getItems() == null)
        {
            meal.getDishes().setItems(new ArrayList<>());
        }

        // this object for the next activity to record.
        meal.getDishes().getItems().remove(position);

        // this one is for adapter which means for UI to show.
        foodItems.remove(position);

        /*
        for (FoodItem.Item dish : meal.getDishes().getItems())
        {
            Log.d(TAG, "real object: " + dish);
        }

        if (meal.getDishes().getItems().isEmpty())
        {
            Log.d(TAG,"Empty alr: " + meal.getDishes().getItems().isEmpty());
        }

        Log.d(TAG, "adapter object: " + foodItems);

         */
        dishGuestUserAdapter.notifyItemRemoved(position);
    }

    @NonNull
    @Contract(" -> new")
    private Callback<FoodItem> callBackResponseFromAPI()
    {
        return new Callback<FoodItem>()
        {
            @Override
            public void onResponse(@NonNull Call<FoodItem> call, @NonNull Response<FoodItem> response)
            {
                if (response.isSuccessful())
                {
                    StringBuilder caloriesMessage = new StringBuilder();
                    StringBuilder cholesterolMessage = new StringBuilder();
                    StringBuilder saltMessage = new StringBuilder();
                    StringBuilder sugarMessage = new StringBuilder();

                    foodItem = response.body();
                    if (foodItem != null)
                    {
                        // assign the variable to the is foodItems array list.

                        // Log.d(TAG, "onResponse: " + foodItem);
                        // todo: set progress bar here

                        // get all total calculations
                        for (FoodItem.Item itemLoop : foodItem.getItems())
                        {
                            totalCalories += itemLoop.getCalories();
                            totalCholesterol += itemLoop.getCholesterol_mg();
                            totalSalt += itemLoop.getSodium_mg();
                            totalSugar += itemLoop.getSugar_g();

                            // todo: set the item.
                            item = itemLoop;
                            item.setFoodImage(selectedImageUri.toString());
                            // foodItems.add(itemLoop);
                        }

                        // todo: set text
                        caloriesMessage
                                .append(totalCalories)
                                .append(" kcal");
                        caloriesTextView.setText(caloriesMessage.toString());

                        cholesterolMessage
                                .append(totalCholesterol)
                                .append(" mg");
                        cholesterolTextView.setText(cholesterolMessage.toString());

                        saltMessage
                                .append(totalSalt)
                                .append(" mg");
                        saltTextView.setText(saltMessage.toString());

                        sugarMessage
                                .append(totalSugar)
                                .append(" g");
                        sugarTextView.setText(sugarMessage.toString());

                        // reset the value
                        totalCalories = 0;
                        totalCholesterol = 0;
                        totalSalt = 0;
                        totalSugar = 0;

                        // calculate percentage
                        //double percentageCalories = (totalCalories / maxCalories) * 100;
                        //double percentageCholesterol = (totalCholesterol / maxCholesterol) * 100;
                        //double percentageSalt = (totalSalt / maxSalt) * 100;
                        //double percentageSugar = (totalSugar / maxSugar) * 100;

                        // fixme: null pointer exception
                        /*
                        progressBarCalories.setProgress((int) percentageCalories);
                        progressCaloriesTextView.setText(String.format(Locale.ROOT, "%.1f%%",
                                percentageCalories));

                        progressBarCholesterol.setProgress((int) percentageCholesterol);
                        progressCholesterolTextView.setText(String.format(Locale.ROOT, "%.1f%%",
                                percentageCholesterol));

                        // fixme: recalculate sodium percentage
                        progressBarSalt.setProgress((int) percentageSalt);
                        progressSaltTextView.setText(String.format(Locale.ROOT, "%.1f%%",
                                percentageSalt));

                        progressBarSugar.setProgress((int) percentageSugar);
                        progressSugarTextView.setText(String.format(Locale.ROOT, "%.1f%%",
                                percentageSugar));
                         */
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<FoodItem> call, @NonNull Throwable t)
            {
                Log.d(TAG, "onFailure: " + t.getMessage());
            }
        };
    }

    @NonNull
    @Contract(" -> new")
    private View.OnClickListener onTakePhotoListener()
    {
        return v ->
        {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissionLauncher.launch(new String[]{Manifest.permission.CAMERA});
            }
            else
            {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null)
                {
                    takePhotoActivityResultLauncher.launch(cameraIntent);
                }
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onNavToLogMealListener()
    {
        return v ->
        {
            StringBuilder message = new StringBuilder();

            if (foodItems.isEmpty())
            {
                message.append("Dish is required to be added before logging your meal.");
                Toast.makeText(requireContext(), message.toString(), Toast.LENGTH_LONG).show();
                return;
            }

            // fixme: testing
            List<FoodItem.Item> testFoodItems;

            testFoodItems = meal.getDishes().getItems();

            for (FoodItem.Item dish : testFoodItems)
            {
                Log.d(TAG, "onNavToLogMealListener: " + dish.getName());
                Log.d(TAG, "onNavToLogMealListener: " + dish.getFoodImage());
                Log.d(TAG, "onNavToLogMealListener: " + dish.getCalories());
                Log.d(TAG, "onNavToLogMealListener: " + dish.getCholesterol_mg());
                Log.d(TAG, "onNavToLogMealListener: " + dish.getSodium_mg());
                Log.d(TAG, "onNavToLogMealListener: " + dish.getSugar_g());
            }

            intentNavToLogMeal = new Intent(requireContext(), UserLogMealActivity.class);
            intentNavToLogMeal.putExtra("gender", gender);
            intentNavToLogMeal.putExtra("meal", meal);
            startActivity(intentNavToLogMeal);
            requireActivity().finish();
        };
    }

    private void setAdapter()
    {
        // set the adapter
        RecyclerView.LayoutManager layoutManager = new
                LinearLayoutManager(requireActivity().getApplicationContext());
        dishRecyclerView.setLayoutManager(layoutManager);
        dishRecyclerView.setItemAnimator(new DefaultItemAnimator());
        dishRecyclerView.setAdapter(dishGuestUserAdapter);
    }

    @NonNull
    @Contract(pure = true)
    private ActivityResultCallback<Map<String, Boolean>> onPermissionLauncher()
    {
        return permissions ->
        {
            if (Boolean.TRUE.equals(permissions.get(Manifest.permission.CAMERA)))
            {
                // Permission granted, can now start the camera intent
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                if (cameraIntent.resolveActivity(getActivity().getPackageManager()) != null)
                {
                    takePhotoActivityResultLauncher.launch(cameraIntent);
                }
            }
            else
            {
                // denied, show toast
                Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private ActivityResultCallback<ActivityResult> onTakePhotoActivityLauncher()
    {
        return result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                Intent data = result.getData();
                if (data != null && data.getData() != null)
                {
                    selectedImageUri = data.getData();
                    Bundle extras = data.getExtras();
                    if (extras != null)
                    {
                        image = (Bitmap) extras.get("data");
                        if (image != null)
                        {
                            dimension = Math.min(image.getWidth(), image.getHeight());
                            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

                            // Set the Bitmap to the ImageView
                            foodImage.setImageBitmap(image);

                            // I don't know what is this?
                            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                            classifyImage(image);
                        }
                    }
                }
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private ActivityResultCallback<ActivityResult> onUploadPhotoActivityLauncher()
    {
        return result ->
        {
            if (result.getResultCode() == Activity.RESULT_OK)
            {
                Intent data = result.getData();
                if (data != null && data.getData() != null)
                {
                    selectedImageUri = data.getData();
                    try {
                        // Decode the URI to a Bitmap
                        image = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(),
                                selectedImageUri);
                        if (image != null)
                        {
                            // Set dimension
                            dimension = Math.min(image.getWidth(), image.getHeight());
                            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension);

                            // Set the Bitmap to the ImageView
                            foodImage.setImageBitmap(image);

                            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                            classifyImage(image);
                        }
                    }
                    catch (IOException e)
                    {
                        Log.d(TAG, "error here: " + e);
                    }
                }
            }
        };
    }

    @NonNull
    @Contract(pure = true)
    private View.OnClickListener onUploadPhotoListener()
    {
        return v ->
        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            uploadPhotoactivityResultLauncher.launch(Intent.createChooser(intent, "Select File"));
        };
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_log_meal_nutrition_analysis, container, false);
    }
}

/*
private void populateItem()
    {
        // fixme: this test is ok, so next step is to add the real one.
        FoodItem.Item testItem = new FoodItem.Item(
                "Test Dish", // name
                200, // calories
                100, // serving_size_g
                10, // fat_total_g
                5, // fat_saturated_g
                20, // protein_g
                100, // sodium_mg
                50, // potassium_mg
                5, // cholesterol_mg
                50, // carbohydrates_total_g
                10, // fiber_g
                10 // sugar_g
        );

        int testImage = R.drawable.about_us_icon;

        testItem.setFoodImage(String.valueOf(testImage));

        foodItems.add(testItem);
    }


 */