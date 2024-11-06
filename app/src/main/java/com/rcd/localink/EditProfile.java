package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

public class EditProfile extends AppCompatActivity {

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
    private EditText confirmPasswordEditText;
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
        setContentView(R.layout.activity_edit_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize your views here
        user_type = "Employer";
        employer_button = findViewById(R.id.employer_button);
        worker = findViewById(R.id.worker_button);
        textView = findViewById(R.id.textView);
        signupGoogleButton = findViewById(R.id.signup_button);
        firstNameEditText = findViewById(R.id.first_name);
        middleNameEditText = findViewById(R.id.middle_name);
        lastNameEditText = findViewById(R.id.last_name);
        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        confirmPasswordEditText = findViewById(R.id.confirm_password);
        phoneNumberEditText = findViewById(R.id.phone_number);
        addressEditText = findViewById(R.id.address);
        upload_profile = findViewById(R.id.upload_profile);
        profile_image = findViewById(R.id.profile_image);

        LinearLayout workerInformationLayout = findViewById(R.id.worker_information);
        EditText ratesEditText = findViewById(R.id.rates);
        EditText availabilityEditText = findViewById(R.id.availability);
        EditText typeOfWorkEditText = findViewById(R.id.type_of_work);


        ImageView back_button = findViewById(R.id.back_button);
        back_button.setOnClickListener(v -> {
            finish();
        });

        // signup onclick listener

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String userId = sharedPrefs.getString("documentId", "");
        String firstName = sharedPrefs.getString("firstName", "");
        String middleName = sharedPrefs.getString("middleName", "");
        String lastName = sharedPrefs.getString("lastName", "");
        String email = sharedPrefs.getString("email", "");
        String phoneNumber = sharedPrefs.getString("phoneNumber", "");
        String address = sharedPrefs.getString("address", "");
        String profilePicture = sharedPrefs.getString("profile_picture", "");
        String type_of_work = sharedPrefs.getString("type_of_work", "");
        String availability = sharedPrefs.getString("availability", "");
        String rates = sharedPrefs.getString("rates", "");




        // Get reviews
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        LinearLayout transaction_history_container = findViewById(R.id.reviews_container);
        db.collection("reviews")
                .whereEqualTo("for", userId)
                .get()
                .addOnCompleteListener(task2 -> {
                    if (task2.isSuccessful()) {
                        transaction_history_container.removeAllViews();
                        for (DocumentSnapshot document2 : task2.getResult()) {
                            Log.d(TAG, document2.getId() + " => " + document2.getData());

                            View view = getLayoutInflater().inflate(R.layout.review_item, null, false);
                            TextView name2 = view.findViewById(R.id.name);
                            ImageView image = view.findViewById(R.id.image);
                            TextView content = view.findViewById(R.id.content);

                            name2.setText(document2.getString("by_name"));
                            Picasso.get().load(document2.getString("by_profile_picture")).into(image);
                            content.setText(document2.getString("content"));

                            view.setOnClickListener(v -> {
                                Toast.makeText(this, "Review item clicked", Toast.LENGTH_SHORT).show();
                            });

                            transaction_history_container.addView(view);
                            view.setVisibility(View.VISIBLE);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task2.getException());
                    }
                });



        firstNameEditText.setText(firstName);
        middleNameEditText.setText(middleName);
        lastNameEditText.setText(lastName);
        emailEditText.setText(email);
        phoneNumberEditText.setText(phoneNumber);
        addressEditText.setText(address);
        ratesEditText.setText(rates);
        availabilityEditText.setText(availability);
        typeOfWorkEditText.setText(type_of_work);
        user_type = sharedPrefs.getString("user_type", "");

        if (user_type.equals("Worker")) {
            worker.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22996E")));
            worker.setTextColor(Color.BLACK);
            employer_button.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            employer_button.setTextColor(Color.WHITE);
            workerInformationLayout.setVisibility(View.VISIBLE);
        } else {
            employer_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#22996E")));
            employer_button.setTextColor(Color.BLACK);
            worker.setBackgroundTintList(ColorStateList.valueOf(Color.BLACK));
            worker.setTextColor(Color.WHITE);
            workerInformationLayout.setVisibility(View.GONE);
        }

        Picasso.get().load(profilePicture).into(profile_image);

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


        if (user_type.equals("Admin")) {
            employer_button.setVisibility(View.GONE);
            worker.setVisibility(View.GONE);
        }

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
                String type_of_work = typeOfWorkEditText.getText().toString();
                String availability = availabilityEditText.getText().toString();
                String rate = ratesEditText.getText().toString();


                if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || phoneNumber.isEmpty() || address.isEmpty() || profile_uri == null) {
                    Toast.makeText(EditProfile.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.isEmpty() && !password.equals(confirmPasswordEditText.getText().toString())) {
                    Toast.makeText(EditProfile.this, "Password and confirm password is not the same", Toast.LENGTH_SHORT).show();
                    return;
                }

                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                Pattern pat = Pattern.compile(emailRegex);
                if (!pat.matcher(email).matches()) {
                    Toast.makeText(EditProfile.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                    return;
                }

                String phoneNumberRegex = "^(09|\\+639)\\d{9}$";
                Pattern phoneNumberPat = Pattern.compile(phoneNumberRegex);
                if (!phoneNumberPat.matcher(phoneNumber).matches()) {
                    Toast.makeText(EditProfile.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                String confirmPassword = confirmPasswordEditText.getText().toString();
                if(!password.equals(confirmPassword)) {
                    Toast.makeText(EditProfile.this, "Password and confirm password is not the same", Toast.LENGTH_SHORT).show();
                    return;
                }



                signupGoogleButton.setEnabled(false);
                signupGoogleButton.setText("Signing up ...");

                // Create a new user with the gathered data


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


                        Toast.makeText(EditProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        signupGoogleButton.setText("Update Profile");
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
                        if (!password.isEmpty()) {
                            user.put("password", password);
                        }
                        user.put("phoneNumber", phoneNumber);
                        user.put("address", address);
                        user.put("user_type", user_type);
                        user.put("type_of_work", type_of_work);
                        user.put("availability", availability);
                        user.put("rates", rate);
                        user.put("profile_picture", firebaseDownloadUrl);


                        // Update the document with the given ID in the "users" collection
                        db.collection("users")
                                .document(userId)
                                .update(user)
                                .addOnSuccessListener(unused -> {
                                    // Successful update
                                    Toast.makeText(EditProfile.this, "User updated Please login", Toast.LENGTH_SHORT).show();

                                    // Redirect to the login page
                                    Intent intent = new Intent(EditProfile.this, MainActivity.class);
                                    startActivity(intent);


                                    signupGoogleButton.setText("Update Profile");
                                    signupGoogleButton.setEnabled(true);
                                })
                                .addOnFailureListener(e -> {
                                    // Failed update
                                    Toast.makeText(EditProfile.this, "Error updating user: " + e.getMessage(), Toast.LENGTH_SHORT).show();


                                    signupGoogleButton.setText("Update Profile");
                                    signupGoogleButton.setEnabled(true);
                                });
                    } else {
                        Log.e(TAG, "Error getting download URL", task.getException());

                        Toast.makeText(EditProfile.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        signupGoogleButton.setText("Update Profile");
                        signupGoogleButton.setEnabled(true);
                    }
                });





            }
        });
    }
}