package com.rcd.localink;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class GigWork extends AppCompatActivity {

    private FirebaseFirestore db;
    private SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gig_work);

        db = FirebaseFirestore.getInstance();
        sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);


        String userType = sharedPrefs.getString("user_type", "");

        FloatingActionButton fab = findViewById(R.id.add_job_posting);

        ImageView backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, Dashboard.class);
            startActivity(intent);
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddGigWork.class);
            startActivity(intent);
        });


        if (userType.equals("Worker")) {
            fetchJobPostings();
        } else if (userType.equals("Employer")) {
            TextView gig_work_title = findViewById(R.id.gig_work_title);
            gig_work_title.setText("Workers Available");
            fetchWorkers();
        }
    }

    private void fetchJobPostings() {
        Toast.makeText(this, "Fetching job postings", Toast.LENGTH_SHORT).show();

        db.collection("job_postings").get().addOnCompleteListener(task -> {
            Log.d(TAG, "fetchJobPostings: Fetching on db");
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                Log.d("GigWork", "Fetched " + querySnapshot.size() + " job postings");
                if (querySnapshot != null) {
                    LinearLayout gigWorkList = findViewById(R.id.gig_work_list);
                    gigWorkList.removeAllViews();
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        Log.d(TAG, "fetchJobPostings: Document ID: " + document.getId());
                        String jobTitle = document.getString("title");
                        String jobDescription = document.getString("employer_name");
                        String profile_picture_firebase = document.getString("employer_profile_pic");

                        // Display job postings in the UI
                        View gigWorkItem = getLayoutInflater().inflate(R.layout.gig_work_item, null);
                        TextView jobTitleTextView = gigWorkItem.findViewById(R.id.job_title);
                        TextView jobDescriptionTextView = gigWorkItem.findViewById(R.id.worker_name);
                        TextView online_or_not = gigWorkItem.findViewById(R.id.online_or_not);
                        ImageView profile_picture = gigWorkItem.findViewById(R.id.profile_picture);



                        jobTitleTextView.setText(jobTitle);
                        jobDescriptionTextView.setText(jobDescription);
                        online_or_not.setText("Looking for Hiring:");
                        Picasso.get().load(profile_picture_firebase).into(profile_picture);

                        gigWorkItem.setOnClickListener(v -> {
                            Intent intent = new Intent(GigWork.this, JobPostDescription.class);
                            intent.putExtra("jobId", document.getId());
                            startActivity(intent);
                        });

                        gigWorkList.addView(gigWorkItem);
                    }
                }
            }
        });
    }

    private void fetchWorkers() {
//        Toast.makeText(this, "Fetching job postings", Toast.LENGTH_SHORT).show();
        db.collection("users").whereEqualTo("user_type", "Worker").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    LinearLayout gigWorkList = findViewById(R.id.gig_work_list);
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String workerName = document.getString("firstName") + " " + document.getString("middleName") + " " + document.getString("lastName");
                        String workerSkills = document.getString("type_of_work");
                        String profile_picture_firebase = document.getString("profile_picture");

                        // Inflate the gig_work_item layout
                        View gigWorkItemView = LayoutInflater.from(this).inflate(R.layout.gig_work_item, null);

                        // Set the text in the UI
                        TextView workerNameTextView = gigWorkItemView.findViewById(R.id.worker_name);
                        TextView workerSkillsTextView = gigWorkItemView.findViewById(R.id.job_title);
                        ImageView profile_picture = gigWorkItemView.findViewById(R.id.profile_picture);

                        workerNameTextView.setText(workerName);
                        workerSkillsTextView.setText(workerSkills);
                        Picasso.get().load(profile_picture_firebase).into(profile_picture);

                        gigWorkItemView.setOnClickListener(v -> {
                            Intent intent = new Intent(GigWork.this, GigWorkerProfile.class);
                            intent.putExtra("workerId", document.getId());
                            startActivity(intent);
                        });

                        // Add the view to the list
                        gigWorkList.addView(gigWorkItemView);
                    }
                }

                        Toast.makeText(this, "Found " + querySnapshot.size() + " workers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}