package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class Signup extends AppCompatActivity {

    private String user_type;
    private Button employer_button;
    private Button worker;
    private ImageView imageView;
    private TextView textView;
    private Button signupGoogleButton;
    private Button upload_profile;
    private EditText firstNameEditText;
    private EditText middleNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private EditText phoneNumberEditText;
    private EditText addressEditText;

    private ImageView profile_image;

    private Button loginButton;

    private Uri profile_uri = null;

    private String firebaseDownloadUrl = null;

    public Map<String, Object> user = new HashMap<>();

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
        upload_profile = findViewById(R.id.upload_profile);
        profile_image = findViewById(R.id.profile_image);

        // signup onclick listener

        // Create an ActivityResultLauncher
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    // Handle the result data
                    profile_uri = data.getData();
                    upload_profile.setEnabled(true);
                    upload_profile.setText("Change profile picture");
                    profile_image.setImageURI(profile_uri);
                }
            });


        upload_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add your code logic here for the upload_profile button click event

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                upload_profile.setEnabled(false);
                upload_profile.setText("File picking ...");
                launcher.launch(intent);
            }
        });


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


                if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || phoneNumber.isEmpty() || address.isEmpty() || profile_uri == null) {
                    Toast.makeText(Signup.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                Pattern pat = Pattern.compile(emailRegex);
                if (!pat.matcher(email).matches()) {
                    Toast.makeText(Signup.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                String phoneNumberRegex = "^(09|\\+639)\\d{9}$";
                Pattern phoneNumberPat = Pattern.compile(phoneNumberRegex);
                if (!phoneNumberPat.matcher(phoneNumber).matches()) {
                    Toast.makeText(Signup.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }



                signupGoogleButton.setEnabled(false);
                signupGoogleButton.setText("Signing up ...");

                // Create a new user with the gathered data


                TextView terms_and_conditiom = findViewById(R.id.terms_and_conditiom);

                terms_and_conditiom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Signup.this, TermsAndConditions.class);
                        startActivity(intent);
                    }
                });

                // Upload the profile_uri to Firebase Storage
                String filename = UUID.randomUUID().toString();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference profileRef = storageRef.child("profile_picture/" + filename);
                UploadTask uploadTask = profileRef.putFile(profile_uri);
                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        // Handle the failed upload
                        Log.e(TAG, "Error uploading profile", task.getException());


                        Toast.makeText(Signup.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        signupGoogleButton.setText("SIGNUP (NEXT STEP)");
                        signupGoogleButton.setEnabled(true);
                        return null;
                    }
                    // Return the download URL
                    return profileRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Profile Picture: " + task.getResult());
                        firebaseDownloadUrl = task.getResult().toString();

                        user.put("firstName", firstName);
                        user.put("middleName", middleName);
                        user.put("lastName", lastName);
                        user.put("email", email);
                        user.put("password", password);
                        user.put("phoneNumber", phoneNumber);
                        user.put("address", address);
                        user.put("user_type", user_type);
                        user.put("profile_picture", firebaseDownloadUrl);


                        // Add a new document with a generated ID in the "users" collection
                        db.collection("users")
                            .add(user)
                            .addOnSuccessListener(documentReference -> {
                                // Successful addition
                                Toast.makeText(Signup.this, "User added Please login", Toast.LENGTH_SHORT).show();

                                // Redirect to the login page
                                Intent intent = new Intent(Signup.this, MainActivity.class);
                                startActivity(intent);


                                signupGoogleButton.setText("SIGNUP (NEXT STEP)");
                                signupGoogleButton.setEnabled(true);
                            })
                            .addOnFailureListener(e -> {
                                // Failed addition
                                Toast.makeText(Signup.this, "Error adding user: " + e.getMessage(), Toast.LENGTH_SHORT).show();


                                signupGoogleButton.setText("SIGNUP (NEXT STEP)");
                                signupGoogleButton.setEnabled(true);
                            });
                    } else {
                        Log.e(TAG, "Error getting download URL", task.getException());

                        Toast.makeText(Signup.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        signupGoogleButton.setText("SIGNUP (NEXT STEP)");
                        signupGoogleButton.setEnabled(true);
                    }
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