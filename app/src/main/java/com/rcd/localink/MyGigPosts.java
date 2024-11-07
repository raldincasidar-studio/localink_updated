package com.rcd.localink;

import static androidx.fragment.app.FragmentManager.TAG;

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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class MyGigPosts extends AppCompatActivity {


    private FirebaseFirestore db;
    private SharedPreferences sharedPrefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_gig_posts);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);

        FloatingActionButton chat_support = findViewById(R.id.chat_support);


        chat_support.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChatPage.class);
            intent.putExtra("person", "admin");
            startActivity(intent);
        });

        String admin = sharedPrefs.getString("user_type", "");
        if (!admin.equals("Admin")) {
            chat_support.setVisibility(View.VISIBLE);
        } else {
            chat_support.setVisibility(View.GONE);
        }

        Button mesages = findViewById(R.id.mesages);

        mesages.setOnClickListener(v -> {
            Intent intent = new Intent(this, Chats.class);
            startActivity(intent);
        });

        String userType = sharedPrefs.getString("user_type", "");

        FloatingActionButton fab = findViewById(R.id.add_job_posting);

        ImageView backButton = findViewById(R.id.back_button);

        Button my_posts_button = findViewById(R.id.worker_list_button);

        backButton.setOnClickListener(v -> {
            finish();
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddGigWork.class);
            startActivity(intent);
        });

        my_posts_button.setOnClickListener(v -> {
            Intent intent = new Intent(this, GigWork.class);
            startActivity(intent);
        });

        fetchJobPostings();
    }

    private void fetchJobPostings() {
        Toast.makeText(this, "Fetching job postings", Toast.LENGTH_SHORT).show();

        db.collection("job_postings").whereEqualTo("employer_id", sharedPrefs.getString("documentId", "")).limit(1).get().addOnCompleteListener(task -> {
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
                            Intent intent = new Intent(MyGigPosts.this, JobPostDescription.class);
                            intent.putExtra("jobId", document.getId());
                            startActivity(intent);
                        });

                        gigWorkList.addView(gigWorkItem);
                    }
                }
            }
        });
    }
}