package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.content.Intent;
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

public class GigWorkerProfile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gig_worker_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        
        
        ImageView profileImage = findViewById(R.id.worker_image);
        TextView name = findViewById(R.id.name);
        TextView workTitle = findViewById(R.id.work_title);
        TextView service = findViewById(R.id.service);
        TextView location = findViewById(R.id.location);
        TextView rates = findViewById(R.id.rates);
        TextView availability = findViewById(R.id.availability);
        Button propose_contract = findViewById(R.id.propose_contract);
        Button chat = findViewById(R.id.chat);

        ImageView back_button = findViewById(R.id.back_button);

        back_button.setOnClickListener(v -> {
            finish();
        });

        
        
        String workerId = getIntent().getStringExtra("workerId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(workerId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    String profile_picture_firebase = document.getString("profile_picture");
                    String workerName = document.getString("firstName") + " " + document.getString("middleName") + " " + document.getString("lastName");
                    String workerTitle = document.getString("type_of_work");
                    String workerLocation = document.getString("address");
                    String workerRates = document.getString("rate");
                    String workerAvailability = document.getString("availability");

                    Picasso.get().load(profile_picture_firebase).into(profileImage);
                    name.setText(workerName);
                    workTitle.setText(workerTitle != null ? workerTitle : "Not set yet");
                    location.setText(workerLocation != null ? workerLocation : "Not set yet");
                    rates.setText(workerRates != null ? workerRates : "Not set yet");
                    availability.setText(workerAvailability != null ? workerAvailability : "Not set yet");

                    chat.setOnClickListener(v -> {
                        Intent intent = new Intent(GigWorkerProfile.this, ChatPage.class);
                        intent.putExtra("person", workerId);
                        startActivity(intent);
                    });

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

                    propose_contract.setOnClickListener(v -> {

                        Intent intent = new Intent(GigWorkerProfile.this, AddContract.class);
                        intent.putExtra("jobId", workerId);
                        intent.putExtra("type", "worker");
                        startActivity(intent);

                    });
                }
            }
        });
    }
}