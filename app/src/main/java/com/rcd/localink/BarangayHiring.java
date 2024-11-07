package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class BarangayHiring extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_barangay_hiring);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        FloatingActionButton fab = findViewById(R.id.add_post_button);
        LinearLayout volunteer_works_list = findViewById(R.id.volunteer_works_list);
        LinearLayout current_volunteer_works_list = findViewById(R.id.current_volunteer_works_list);
        Button volunteer_button = findViewById(R.id.volunteer_button);
        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String user_type = sharedPrefs.getString("user_type", "");

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

        ImageView back_button = findViewById(R.id.back_button);

        back_button.setOnClickListener(v -> {
            finish();
        });


        LinearLayout hide_for_admin = findViewById(R.id.hide_for_admin);

        if(user_type.equals("Admin")){
            fab.show();
            hide_for_admin.setVisibility(View.GONE);
        } else {
            fab.hide();
            hide_for_admin.setVisibility(View.VISIBLE);
        }

        volunteer_button.setOnClickListener(v -> {
            Intent intent = new Intent(this, Barangay.class);
            startActivity(intent);
        });


        fab.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddBarangayHiring.class);
            startActivity(intent);
        });

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("barangay_hiring").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                volunteer_works_list.removeAllViews();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String contractName = document.getString("contractName");
                    String startsIn = document.getString("startsIn");
                    String endsIn = document.getString("endsIn");
                    String time = document.getString("time");
                    String instruction = document.getString("instruction");
                    boolean activated = document.getBoolean("activateAcceptVolunteerWorks");

                    // Create a view to display the volunteer work details
                    View volunteerWorkView = getLayoutInflater().inflate(R.layout.volunteer_work_item, null);
                    Button volunteer_works_button = volunteerWorkView.findViewById(R.id.volunteer_works_button);

                    volunteer_works_button.setText(contractName);

                    volunteer_works_button.setOnClickListener(v -> {
                        Intent intent = new Intent(BarangayHiring.this, HiringDescription.class);
                        intent.putExtra("volunteer_work_id", document.getId());
                        startActivity(intent);
                    });

                    db.collection("barangay_hiring_contracts").whereEqualTo("hiringId", document.getId()).whereEqualTo("userId", sharedPrefs.getString("documentId", "")).limit(1).get().addOnCompleteListener(task2 -> {
                        if (task2.isSuccessful()) {
                            QuerySnapshot querySnapshot = task2.getResult();
                            if (querySnapshot != null && querySnapshot.size() > 0) {
                                // I have already joined this volunteer work
                                String status = querySnapshot.getDocuments().get(0).getString("status");

                                volunteer_works_button.setText(contractName + " (" + status + ")");
                            }
                        }
                    });

                    // Add the view to the volunteer_works_list
                    volunteer_works_list.addView(volunteerWorkView);
                }
            } else {
                Log.w("Firestore", "Error getting documents.", task.getException());
                Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
            }
        });

//        String documentId = sharedPrefs.getString("documentId", "");
//        db.collection("barangay_hiring_contracts").whereEqualTo("userId", documentId).get().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                current_volunteer_works_list.removeAllViews();
//                for (QueryDocumentSnapshot document : task.getResult()) {
//                    String volunteer_work = document.getString("hiringId");
//                    String volunteer_work_name = document.getString("hiring_name");
//                    String volunteer_work_status = document.getString("status");
//
//                    // Create a view to display the volunteer work details
//                    View currentVolunteerWorkView = getLayoutInflater().inflate(R.layout.volunteer_work_item, null);
//                    Button volunteer_works_button = currentVolunteerWorkView.findViewById(R.id.volunteer_works_button);
//
//                    volunteer_works_button.setText(volunteer_work_name + " (" + volunteer_work_status + ")");
//
//                    volunteer_works_button.setOnClickListener(v -> {
//                        Intent intent = new Intent(BarangayHiring.this, HiringDescription.class);
//                        intent.putExtra("volunteer_work_id", volunteer_work);
//                        startActivity(intent);
//                    });
//
//
//
//                    // Add the view to the current_volunteer_lists
//                    current_volunteer_works_list.addView(currentVolunteerWorkView);
//                }
//            } else {
//                Log.w("Firestore", "Error getting documents.", task.getException());
//                Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
//            }
//        });
    }
}