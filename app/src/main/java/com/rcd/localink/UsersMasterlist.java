package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

public class UsersMasterlist extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_users_masterlist);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        LinearLayout chat_container = findViewById(R.id.chat_container);

        ImageView backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        EditText search = findViewById(R.id.search);
        search.setOnTouchListener((v, event) -> {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (search.getRight() - search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                    initializeData(chat_container);

                    return true;
                }
            }
            return false;
        });


        initializeData(chat_container);

        Button sort_all = findViewById(R.id.sort_all);
        Button sort_verified = findViewById(R.id.sort_verified);
        Button sort_pending = findViewById(R.id.sort_pending);
        Button sort_declined = findViewById(R.id.sort_declined);

        sort_all.setOnClickListener(v -> {
           sortData(chat_container, "");
        });

        sort_verified.setOnClickListener(v -> {
           sortData(chat_container, "Verified");
        });

        sort_pending.setOnClickListener(v -> {
           sortData(chat_container, "Pending");
        });

        sort_declined.setOnClickListener(v -> {
           sortData(chat_container, "Declined");
        });
    }

    private void initializeData(LinearLayout chat_container) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .orderBy("verificationStatus", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chat_container.removeAllViews();
                        for (DocumentSnapshot document : task.getResult()) {
                            View reviewView = getLayoutInflater().inflate(R.layout.transaction_history, chat_container, false);

                            TextView chatName = reviewView.findViewById(R.id.name);

                            TextView chatContent = reviewView.findViewById(R.id.type_of_work);
                            TextView status = reviewView.findViewById(R.id.status);
                            ImageView chatImage = reviewView.findViewById(R.id.image);

                            String firstName = document.getString("firstName");
                            String middleName = document.getString("middleName");
                            String lastName = document.getString("lastName");
                            String userNameText = firstName + " " + middleName + " " + lastName;
                            chatName.setText(userNameText);
                            status.setText(document.getString("verificationStatus") != null ? document.getString("verificationStatus") : "pending");

                            String verificationStatus = document.getString("verificationStatus");
                            if ("Verified".equals(verificationStatus)) {
                                status.setTextColor(Color.parseColor("#4CAF50")); // success green
                            } else if ("Declined".equals(verificationStatus)) {
                                status.setTextColor(Color.parseColor("#F44336")); // error red
                            } else {
                                status.setTextColor(Color.parseColor("#FFA500")); // orange
                            }

                            String userType = document.getString("user_type");
                            chatContent.setText(userType);

                            String profilePicture = document.getString("profile_picture");
                            Picasso.get().load(profilePicture).into(chatImage);

                            reviewView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(UsersMasterlist.this, PublicProfilePage.class);
                                    intent.putExtra("id", document.getId());
                                    startActivity(intent);
                                }
                            });

                            EditText searchEditText = findViewById(R.id.search);

                            if (userType.equals("Admin")) {
                                continue;
                            }

                            String searchText = searchEditText.getText().toString().trim().toLowerCase();
                            if (userNameText.toLowerCase().contains(searchText)) {
                                chat_container.addView(reviewView);
                            }

                        }

                    } else {
                        Log.w("Chats", "Error getting documents.", task.getException());
                    }
                });
    }

    private void sortData(LinearLayout chat_container, String type) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .orderBy("verificationStatus", Query.Direction.ASCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        chat_container.removeAllViews();
                        for (DocumentSnapshot document : task.getResult()) {
                            View reviewView = getLayoutInflater().inflate(R.layout.transaction_history, chat_container, false);

                            TextView chatName = reviewView.findViewById(R.id.name);

                            TextView chatContent = reviewView.findViewById(R.id.type_of_work);
                            TextView status = reviewView.findViewById(R.id.status);
                            ImageView chatImage = reviewView.findViewById(R.id.image);

                            String firstName = document.getString("firstName");
                            String middleName = document.getString("middleName");
                            String lastName = document.getString("lastName");
                            String userNameText = firstName + " " + middleName + " " + lastName;
                            chatName.setText(userNameText);
                            status.setText(document.getString("verificationStatus") != null ? document.getString("verificationStatus") : "pending");

                            String verificationStatus = document.getString("verificationStatus");
                            if ("Verified".equals(verificationStatus)) {
                                status.setTextColor(Color.parseColor("#4CAF50")); // success green
                            } else if ("Declined".equals(verificationStatus)) {
                                status.setTextColor(Color.parseColor("#F44336")); // error red
                            } else {
                                status.setTextColor(Color.parseColor("#FFA500")); // orange
                            }

                            String userType = document.getString("user_type");
                            chatContent.setText(userType);

                            String profilePicture = document.getString("profile_picture");
                            Picasso.get().load(profilePicture).into(chatImage);

                            reviewView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(UsersMasterlist.this, PublicProfilePage.class);
                                    intent.putExtra("id", document.getId());
                                    startActivity(intent);
                                }
                            });

                            EditText searchEditText = findViewById(R.id.search);

                            if (userType.equals("Admin")) {
                                continue;
                            }

                            String searchText = type;

                            if (verificationStatus.equals(searchText)) {
                                chat_container.addView(reviewView);
                            }

                            if (searchText.equals("")) {
                                chat_container.addView(reviewView);
                            }

                        }

                    } else {
                        Log.w("Chats", "Error getting documents.", task.getException());
                    }
                });
    }
}