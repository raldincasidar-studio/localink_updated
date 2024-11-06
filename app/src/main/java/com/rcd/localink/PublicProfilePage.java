package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class PublicProfilePage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_public_profile_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView profileImage = findViewById(R.id.worker_image);
        TextView name = findViewById(R.id.name);
        TextView service = findViewById(R.id.service);
        TextView location = findViewById(R.id.location);
        TextView rates = findViewById(R.id.rates);
        TextView availability = findViewById(R.id.availability);
        Button chat = findViewById(R.id.chat);

        TextView address = findViewById(R.id.address);
        TextView email = findViewById(R.id.email);
        TextView firstName = findViewById(R.id.first_name);
        TextView lastName = findViewById(R.id.last_name);
        TextView middleName = findViewById(R.id.middle_name);
        TextView phoneNumber = findViewById(R.id.phone_number);

        ImageView back_button = findViewById(R.id.back_button);

        back_button.setOnClickListener(v -> {
            finish();
        });



        String workerId = getIntent().getStringExtra("id");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(workerId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    String profile_picture_firebase = document.getString("profile_picture");
                    String workerName = document.getString("firstName") + " " + document.getString("middleName") + " " + document.getString("lastName");
                    String workerTitle = document.getString("type_of_work");
                    String workerLocation = document.getString("address");
                    String workerRates = document.getString("rates");
                    String workerAvailability = document.getString("availability");

                    String addressValue = document.getString("address");
                    String availabilityValue = document.getString("availability");
                    String emailValue = document.getString("email");
                    String firstNameValue = document.getString("firstName");
                    String lastNameValue = document.getString("lastName");
                    String middleNameValue = document.getString("middleName");
                    String phoneNumberValue = document.getString("phoneNumber");
                    String verificationStatus = document.getString("verificationStatus");
                    String validId = document.getString("valid_id");

                    TextView verification_status = findViewById(R.id.verification_status);
                    if (verificationStatus != null) {
                        verification_status.setText(verificationStatus);
                    } else {
                        verification_status.setText("Pending");
                    }

                    Button verify_user = findViewById(R.id.verify_user);
                    Button decline_user = findViewById(R.id.decline_user);
                    if (verificationStatus != null && !verificationStatus.equals("Pending")) {
                        verify_user.setVisibility(View.GONE);
                        decline_user.setVisibility(View.GONE);
                    }

                    Picasso.get().load(profile_picture_firebase).into(profileImage);
                    name.setText(workerName);

                    ImageView document_valid_id = findViewById(R.id.document_valid_id);



                    Picasso.get().load(validId).into(document_valid_id);

                    address.setText(addressValue);
                    availability.setText(availabilityValue);
                    email.setText(emailValue);
                    firstName.setText(firstNameValue);
                    lastName.setText(lastNameValue);
                    middleName.setText(middleNameValue);
                    phoneNumber.setText(phoneNumberValue);

                    chat.setOnClickListener(v -> {
                        Intent intent = new Intent(PublicProfilePage.this, ChatPage.class);
                        intent.putExtra("person", workerId);
                        startActivity(intent);
                    });

                    verify_user.setOnClickListener(v -> {
                        db.collection("users").document(workerId).update("verificationStatus", "Verified");
                        verify_user.setVisibility(View.GONE);
                        decline_user.setVisibility(View.GONE);
                        verification_status.setText("Verified");
                        finish();
                    });

                    decline_user.setOnClickListener(v -> {
                        db.collection("users").document(workerId).update("verificationStatus", "Declined");
                        verify_user.setVisibility(View.GONE);
                        decline_user.setVisibility(View.GONE);
                        verification_status.setText("Declined");
                        finish();
                    });

                    SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
                    String user_type = sharedPrefs.getString("user_type", "");
                    LinearLayout document_submitted = findViewById(R.id.document_submitted);
                    if (user_type.equals("Admin")) {
                        document_submitted.setVisibility(View.VISIBLE);
                    } else {
                        document_submitted.setVisibility(View.GONE);
                    }

                    // Get reviews
                    LinearLayout transaction_history_container = findViewById(R.id.reviews_container);
                    db.collection("reviews")
                            .whereEqualTo("for", workerId)
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

                }
            }
        });
    }
}