package com.rcd.localink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

                            posterName.setText(posterNameText);
                            Picasso.get().load(posterImageText).into(posterImage);
                            description.setText(descriptionText);
                            employerName.setText(employerNameText);
                            location.setText(locationText);
                            rate.setText(String.valueOf(rateText));
                            requiredHours.setText(String.valueOf(requiredHoursText));

                            chat.setOnClickListener(v -> {
                                Intent intent = new Intent(this, ChatPage.class);
                                intent.putExtra("jobId", employerId);
                                startActivity(intent);
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