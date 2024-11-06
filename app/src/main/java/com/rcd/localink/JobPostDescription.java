package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class JobPostDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_job_post_description);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView posterName = findViewById(R.id.poster_name);
        ImageView posterImage = findViewById(R.id.poster_image);
        TextView description = findViewById(R.id.description);
        TextView employerName = findViewById(R.id.employer_name);
        TextView location = findViewById(R.id.location);
        TextView rate = findViewById(R.id.rate);
        TextView requiredHours = findViewById(R.id.required_hours);
        ImageView backButton = findViewById(R.id.back_button);
        Button chat = findViewById(R.id.chat);
        Button propose_contract = findViewById(R.id.propose_contract);


        backButton.setOnClickListener(v -> {
            finish();
        });


        String jobId = getIntent().getStringExtra("jobId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("job_postings").document(jobId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            String posterNameText = document.getString("employer_name");
                            String posterImageText = document.getString("employer_profile_pic");
                            String descriptionText = document.getString("description");
                            String employerId = document.getString("employer_id");
                            String employerNameText = document.getString("employer_name");
                            String locationText = document.getString("location");
                            double rateText = document.getDouble("rate");
                            double requiredHoursText = document.getDouble("required_hours");

                            LinearLayout profile_container = findViewById(R.id.user_profile_container);

                            profile_container.setOnClickListener(v -> {
                                Intent intent = new Intent(JobPostDescription.this, PublicProfilePage.class);
                                intent.putExtra("id", employerId);
                                startActivity(intent);
                            });

                            posterName.setText(posterNameText);
                            Picasso.get().load(posterImageText).into(posterImage);
                            description.setText(descriptionText);
                            employerName.setText(employerNameText);
                            location.setText(locationText);
                            rate.setText(String.valueOf(rateText));
                            requiredHours.setText(String.valueOf(requiredHoursText));

                            chat.setOnClickListener(v -> {
                                Intent intent = new Intent(this, ChatPage.class);
                                intent.putExtra("person", employerId);
                                startActivity(intent);
                            });

                            SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
                            String documentId = sharedPrefs.getString("documentId", "");
                            Button delete_job_post = findViewById(R.id.delete_job_post);
                            if (documentId.equals(employerId)) {
                                chat.setVisibility(View.GONE);
                                propose_contract.setVisibility(View.GONE);

                                delete_job_post.setVisibility(View.VISIBLE);
                                delete_job_post.setOnClickListener(v -> {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                    builder.setTitle("Delete Job Post");
                                    builder.setMessage("Are you sure you want to delete this job post?");
                                    builder.setPositiveButton("Yes", (dialogInterface, i) -> {

                                        db.collection("job_postings").document(jobId).delete();
                                        finish();
                                    });
                                    builder.setNegativeButton("No", (dialogInterface, i) -> {

                                    });
                                    AlertDialog dialog = builder.create();
                                    dialog.show();
                                });
                            } else {
                                delete_job_post.setVisibility(View.GONE);
                            }
                            LinearLayout transaction_history_container = findViewById(R.id.reviews_container);

//                          Get reviews
                            db.collection("reviews")
                                    .whereEqualTo("for", employerId)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            transaction_history_container.removeAllViews();
                                            for (DocumentSnapshot document2 : task2.getResult()) {
                                                Log.d(TAG, document2.getId() + " => " + document2.getData());

                                                View view = getLayoutInflater().inflate(R.layout.review_item, null, false);
                                                TextView name = view.findViewById(R.id.name);
                                                ImageView image = view.findViewById(R.id.image);
                                                TextView content = view.findViewById(R.id.content);

                                                name.setText(document2.getString("by_name"));
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

                                Intent intent = new Intent(this, AddContract.class);
                                intent.putExtra("jobId", jobId);
                                intent.putExtra("type", "job");
                                startActivity(intent);

                            });
                        }
                    }
                });
    }
}