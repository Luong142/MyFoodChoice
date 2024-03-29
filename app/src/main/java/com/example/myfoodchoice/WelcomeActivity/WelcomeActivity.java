package com.example.myfoodchoice.WelcomeActivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myfoodchoice.AuthenticationActivity.LoginActivity;
import com.example.myfoodchoice.AuthenticationActivity.RegisterBusinessActivity;
import com.example.myfoodchoice.AuthenticationActivity.RegisterGuestActivity;
import com.example.myfoodchoice.CallAPI.ChatGPTAPI;
import com.example.myfoodchoice.CallAPI.ClaudeAPI;
import com.example.myfoodchoice.CallAPI.ClaudeAPIService;
import com.example.myfoodchoice.CallAPI.Message;
import com.example.myfoodchoice.CallAPI.MessageRequest;
import com.example.myfoodchoice.CallAPI.MessageResponse;
import com.example.myfoodchoice.Prevalent.Prevalent;
import com.example.myfoodchoice.R;
import com.example.myfoodchoice.UserActivity.UserMainMenuActivity;
import com.google.firebase.auth.FirebaseAuth;
import org.jetbrains.annotations.Contract;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;

import io.paperdb.Paper;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WelcomeActivity extends AppCompatActivity
{
    // declare buttons
    Button signingBtn;
    String emailRememberMe, passwordRememberMe;

    TextView signUpAsGuest, signUpAsBusiness;

    SpannableString spannableSignUpAsGuestNav, spannableSignUpAsBusiness;

    FirebaseAuth firebaseAuth;

    static final int INDEXSTART = 0;

    static final String TAG = "WelcomeActivity";

    AlertDialog.Builder builder;

    private static final String BASE_URL = "https://api.anthropic.com/";

    private ClaudeAPIService claudeAPIService;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // TODO: test ClaudeAPI part
        /*
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
         */
        // claudeAPIService = retrofit.create(ClaudeAPIService.class);
        // testClaudeAPI(); FIXME: this one failed.

        // FIXME: this one is not free too.
        // ChatGPTAPI chatGPTAPI = new ChatGPTAPI();
        // chatGPTAPI.generateOutput("What is the meaning of life?", this);

        // init paper
        Paper.init(WelcomeActivity.this);

        // init firebase auth;
        firebaseAuth = FirebaseAuth.getInstance();

        // init buttons
        signingBtn = findViewById(R.id.signInBtn);

        // init text view.
        signUpAsGuest = findViewById(R.id.signUpAsGuest);
        signUpAsBusiness = findViewById(R.id.signUpAsBusiness);

        // nav to sign up page based on text click
        // FIXME: the index is out of bound.
        spannableSignUpAsGuestNav = new SpannableString(signUpAsGuest.getText());
        spannableSignUpAsGuestNav.setSpan(clickableSignUpAsGuest(), INDEXSTART, signUpAsGuest.length(), 0);
        signUpAsGuest.setText(spannableSignUpAsGuestNav);
        signUpAsGuest.setMovementMethod(LinkMovementMethod.getInstance());

        // nav to sign up page based on text click
        spannableSignUpAsBusiness = new SpannableString(signUpAsBusiness.getText());
        spannableSignUpAsBusiness.setSpan(clickableSignUpAsBusiness(), INDEXSTART, signUpAsBusiness.length(), 0);
        signUpAsBusiness.setText(spannableSignUpAsBusiness);
        signUpAsBusiness.setMovementMethod(LinkMovementMethod.getInstance());

        signingBtn.setOnClickListener(onSignInListener);

        // assign to email and password
        emailRememberMe = Paper.book().read(Prevalent.UserEmailKey);
        passwordRememberMe = Paper.book().read(Prevalent.UserPasswordKey);

        if (emailRememberMe != null && passwordRememberMe != null)
        {
            if (!TextUtils.isEmpty(emailRememberMe) && !TextUtils.isEmpty(passwordRememberMe))
            {
                allowLogin(emailRememberMe, passwordRememberMe);

                builder = new AlertDialog.Builder(WelcomeActivity.this);
                builder.setTitle("Already Logged in");
                builder.setMessage("Please wait...");
                builder.show();
            }
        }

        // TODO: we have normal user, guest, business vendor with two types
        //  (Dietitian and Trainer)
        // TODO: use the attribute to identify which user type.
        //  (Dietitian = 1, Trainer = 2, Normal User = 3)
        //  (Dietitian = 1, Trainer = 2, Normal User = 3)
    }

    private void testClaudeAPI()
    {
        String prompt = "What is the capital of France?";
        ClaudeAPI claudeAPI = new ClaudeAPI();
        claudeAPI.callAPI(prompt, new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                Log.e(TAG, "API call failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    String apiResponse = Objects.requireNonNull(response.body()).string();
                    Log.d(TAG, "AI Response: " + apiResponse);
                    // Do something with the API response
                }
                else
                {
                    Log.e(TAG, "API call failed with code: " + response.code());
                    // Log.e(TAG, "API call failed with message: " + response.message());
                }
            }
        });
    }

    @NonNull
    @Contract(" -> new")
    private ClickableSpan clickableSignUpAsBusiness()
    {
        return new ClickableSpan()
        {
            @Override
            public void onClick(View widget)
            {
                Intent intent = new Intent(WelcomeActivity.this,
                        RegisterBusinessActivity.class);
                startActivity(intent);
            }
        };
    }

    @NonNull
    @Contract(" -> new")
    private ClickableSpan clickableSignUpAsGuest()
    {
        return new ClickableSpan()
        {
            @Override
            public void onClick(View widget)
            {
                Intent intent = new Intent(WelcomeActivity.this,
                        RegisterGuestActivity.class);
                startActivity(intent);
            }
        };
    }

    private void allowLogin(String email, String password)
    {
        // TODO: login function

        // authentication login
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task ->
        {
            if (task.isSuccessful())
            {
                Toast.makeText(WelcomeActivity.this, "Welcome to Smart Food Choice!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(WelcomeActivity.this, UserMainMenuActivity.class);
                startActivity(intent);
                finish();
            }
            else
            {
                Toast.makeText(WelcomeActivity.this, "", Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    private final View.OnClickListener onSignInListener = v ->
    {
        Intent intent = new Intent(WelcomeActivity.this, LoginActivity.class);
        startActivity(intent);
    };
}