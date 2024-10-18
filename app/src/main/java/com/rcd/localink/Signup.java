package com.rcd.localink;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Signup extends AppCompatActivity {

    private String user_type;
    private Button employer_button;
    private Button worker;
    private ImageView imageView;
    private TextView textView;
    private Button signupGoogleButton;
    private EditText firstNameEditText;
    private EditText middleNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText phoneNumberEditText;
    private EditText addressEditText;

    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize your views here
        user_type = "Employer";
        employer_button = findViewById(R.id.employer_button);
        worker = findViewById(R.id.worker_button);
        imageView = findViewById(R.id.imageView);
        textView = findViewById(R.id.textView);
        signupGoogleButton = findViewById(R.id.signup_button);
        firstNameEditText = findViewById(R.id.first_name);
        middleNameEditText = findViewById(R.id.middle_name);
        lastNameEditText = findViewById(R.id.last_name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        phoneNumberEditText = findViewById(R.id.phone_number);
        addressEditText = findViewById(R.id.address);
        loginButton = findViewById(R.id.login_button);


        // signup onclick listener


        employer_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_type = "Employer";
                employer_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22996E")));
                employer_button.setTextColor(Color.BLACK);
                worker.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
                worker.setTextColor(Color.WHITE);
            }
        });

        worker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user_type = "Worker";
                worker.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22996E")));
                worker.setTextColor(Color.BLACK);
                employer_button.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
                employer_button.setTextColor(Color.WHITE);
            }
        });

        signupGoogleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                // Initialize Firestore
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Get the text from the EditTexts
                String firstName = firstNameEditText.getText().toString();
                String middleName = middleNameEditText.getText().toString();
                String lastName = lastNameEditText.getText().toString();
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String phoneNumber = phoneNumberEditText.getText().toString();
                String address = addressEditText.getText().toString();

                // Create a new user with the gathered data
                Map<String, Object> user = new HashMap<>();
                user.put("firstName", firstName);
                user.put("middleName", middleName);
                user.put("lastName", lastName);
                user.put("email", email);
                user.put("password", password);
                user.put("phoneNumber", phoneNumber);
                user.put("address", address);
                user.put("user_type", user_type);

                // Add a new document with a generated ID in the "users" collection
                db.collection("users")
                    .add(user)
                    .addOnSuccessListener(documentReference -> {
                        // Successful addition
                        Toast.makeText(Signup.this, "User added with ID: " + documentReference.getId(), Toast.LENGTH_SHORT).show();

                        // Redirect to the login page
                        Intent intent = new Intent(Signup.this, MainActivity.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        // Failed addition
                        Toast.makeText(Signup.this, "Error adding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });

            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // go to Login Activity
                Intent intent = new Intent(Signup.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }
}