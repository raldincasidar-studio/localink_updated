package com.rcd.localink;

import static androidx.fragment.app.FragmentManager.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

public class GigWork extends AppCompatActivity {

    private FirebaseFirestore db;
    private SharedPreferences sharedPrefs;

    EditText searchEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_gig_work);

        db = FirebaseFirestore.getInstance();
        sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        searchEditText = findViewById(R.id.search);

        String userType = sharedPrefs.getString("user_type", "");

        FloatingActionButton fab = findViewById(R.id.add_job_posting);

        ImageView backButton = findViewById(R.id.back_button);

        Button my_posts_button = findViewById(R.id.my_posts_button);

        Button worker_list_button = findViewById(R.id.worker_list_button);

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

        if (!userType.equals("Employer")) {
            my_posts_button.setVisibility(View.GONE);
        }

        if (userType.equals("Employer")) {
            fab.show();
        } else {
            fab.hide();
        }

        Button mesages = findViewById(R.id.mesages);

        mesages.setOnClickListener(v -> {
            Intent intent = new Intent(this, Chats.class);
            startActivity(intent);
        });


        EditText search = findViewById(R.id.search);
        search.setOnTouchListener((v, event) -> {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            if (event.getAction() == MotionEvent.ACTION_UP) {
                if (event.getRawX() >= (search.getRight() - search.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                    if (userType.equals("Worker")) {
                        searchJobPostings();
                    } else if (userType.equals("Employer")) {
                        TextView gig_work_title = findViewById(R.id.gig_work_title);
                        gig_work_title.setText("Workers Available");
                        searchWorkers();
                    }

                    return true;
                }
            }
            return false;
        });

        if (userType.equals("Worker")) {
            worker_list_button.setText("Latest Job Posts");
        }



        backButton.setOnClickListener(v -> {
            finish();
        });

        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddGigWork.class);
            startActivity(intent);
        });

        my_posts_button.setOnClickListener(v -> {
            Intent intent = new Intent(this, MyGigPosts.class);
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

//    private void fetchJobPostings() {
//        Toast.makeText(this, "Fetching job postings", Toast.LENGTH_SHORT).show();
//
//        db.collection("job_postings").get().addOnCompleteListener(task -> {
//            Log.d(TAG, "fetchJobPostings: Fetching on db");
//            if (task.isSuccessful()) {
//                QuerySnapshot querySnapshot = task.getResult();
//                Log.d("GigWork", "Fetched " + querySnapshot.size() + " job postings");
//                if (querySnapshot != null) {
//                    LinearLayout gigWorkList = findViewById(R.id.gig_work_list);
//                    gigWorkList.removeAllViews();
//                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
//                        Log.d(TAG, "fetchJobPostings: Document ID: " + document.getId());
//                        String jobTitle = document.getString("title");
//                        String jobDescription = document.getString("employer_name");
//                        String profile_picture_firebase = document.getString("employer_profile_pic");
//
//                        // Display job postings in the UI
//                        View gigWorkItem = getLayoutInflater().inflate(R.layout.gig_work_item, null);
//                        TextView jobTitleTextView = gigWorkItem.findViewById(R.id.job_title);
//                        TextView jobDescriptionTextView = gigWorkItem.findViewById(R.id.worker_name);
//                        TextView online_or_not = gigWorkItem.findViewById(R.id.online_or_not);
//                        ImageView profile_picture = gigWorkItem.findViewById(R.id.profile_picture);
//
//
//
//                        jobTitleTextView.setText(jobTitle);
//                        jobDescriptionTextView.setText(jobDescription);
//                        online_or_not.setText("Looking for Hiring:");
//                        Picasso.get().load(profile_picture_firebase).into(profile_picture);
//
//                        gigWorkItem.setOnClickListener(v -> {
//                            Intent intent = new Intent(GigWork.this, JobPostDescription.class);
//                            intent.putExtra("jobId", document.getId());
//                            startActivity(intent);
//                        });
//
//                        gigWorkList.addView(gigWorkItem);
//                    }
//                }
//            }
//        });
//    }

    private Button searchButton;

    private void fetchJobPostings() {
        Toast.makeText(this, "Fetching job postings", Toast.LENGTH_SHORT).show();

        searchEditText = findViewById(R.id.search);
        searchEditText = findViewById(R.id.search);

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Log.d(TAG, "fetchJobPostings: enter pressed");
                String searchText = searchEditText.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    searchJobPostings();
                } else {
                    Toast.makeText(this, "Please enter search text", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        db.collection("job_postings").orderBy("title", Query.Direction.ASCENDING).get().addOnCompleteListener(task -> {
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

    private void searchJobPostings() {
        Toast.makeText(this, "Fetching job postings", Toast.LENGTH_SHORT).show();



        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Log.d(TAG, "fetchJobPostings: enter pressed");
                String searchText = searchEditText.getText().toString().trim();
                if (!searchText.isEmpty()) {
                    searchJobPostings();
                } else {
                    Toast.makeText(this, "Please enter search text", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

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



                        String searchText = searchEditText.getText().toString().trim().toLowerCase();
                        if (jobTitle.toLowerCase().contains(searchText) || jobDescription.toLowerCase().contains(searchText)) {
                            gigWorkList.addView(gigWorkItem);
                        }
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

    private void searchWorkers() {
//        Toast.makeText(this, "Fetching job postings", Toast.LENGTH_SHORT).show();
        db.collection("users").whereEqualTo("user_type", "Worker").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                LinearLayout gigWorkList = findViewById(R.id.gig_work_list);
                gigWorkList.removeAllViews();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null) {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String workerName = document.getString("firstName") + " " + document.getString("middleName") + " " + document.getString("lastName");
                        String workerSkills = document.getString("type_of_work");
                        if (workerSkills == null) {
                            workerSkills = "";
                        }
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

                        String searchText = searchEditText.getText().toString().trim().toLowerCase();
                        if (workerSkills.toLowerCase().contains(searchText) || workerName.toLowerCase().contains(searchText)) {
                            gigWorkList.addView(gigWorkItemView);
                        }
                    }
                }

                Toast.makeText(this, "Found " + querySnapshot.size() + " workers", Toast.LENGTH_SHORT).show();
            }
        });
    }
}