package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.squareup.picasso.Picasso;

public class Barangay extends AppCompatActivity {


    private ImageView back_button;
    private  ImageView profile_image;

    private FloatingActionButton add_post_button;

    private LinearLayout volunteer_works_list;
    private LinearLayout current_volunteer_works_list;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_barangay);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        back_button = findViewById(R.id.back_button);
        profile_image = findViewById(R.id.profile_image);
        add_post_button = findViewById(R.id.add_post_button);

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String user_type = sharedPrefs.getString("user_type", "");
        if(user_type.equals("admin")){
            add_post_button.show();
        } else {
            add_post_button.hide();
        }

        Button hiring_button = findViewById(R.id.hiring_button);
        hiring_button.setOnClickListener(v -> {
            Intent intent = new Intent(this, BarangayHiring.class);
            startActivity(intent);
        });

        volunteer_works_list = findViewById(R.id.volunteer_works_list);
        current_volunteer_works_list = findViewById(R.id.current_volunteer_works_list);

        String profilePicture = sharedPrefs.getString("profile_picture", "");
        Picasso.get().load(profilePicture).into(profile_image);

        back_button.setOnClickListener(v -> {
            Intent intent = new Intent(this, Dashboard.class);
            startActivity(intent);
        });

        add_post_button.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddVolunteerWork.class);
            startActivity(intent);
        });


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("volunteer_works").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                volunteer_works_list.removeAllViews();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String volunteerName = document.getString("volunteer_name");
                    String duration = document.getString("duration");
                    String instruction = document.getString("instruction");
                    boolean activated = document.getBoolean("activated");

                    // Create a view to display the volunteer work details
                    View volunteerWorkView = getLayoutInflater().inflate(R.layout.volunteer_work_item, null);
                    Button volunteer_works_button = volunteerWorkView.findViewById(R.id.volunteer_works_button);

                    volunteer_works_button.setText(volunteerName);

                    volunteer_works_button.setOnClickListener(v -> {
                        Intent intent = new Intent(Barangay.this, VolunteerWorkDescription.class);
                        intent.putExtra("volunteer_work_id", document.getId());
                        startActivity(intent);
                    });

                    // Add the view to the volunteer_works_list
                    volunteer_works_list.addView(volunteerWorkView);
                }
            } else {
                Log.w("Firestore", "Error getting documents.", task.getException());
                Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
            }
        });

        String documentId = sharedPrefs.getString("documentId", "");

        db.collection("volunteer_transactions").whereEqualTo("volunteer", documentId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                current_volunteer_works_list.removeAllViews();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String volunteer_work = document.getString("volunteer_work");
                    String volunteer_work_name = document.getString("volunteer_name");
                    String volunteer_work_status = document.getBoolean("isCompleted") ? "Completed" : "Pending";

                    // Create a view to display the volunteer work details
                    View currentVolunteerWorkView = getLayoutInflater().inflate(R.layout.volunteer_work_item, null);
                    Button volunteer_works_button = currentVolunteerWorkView.findViewById(R.id.volunteer_works_button);

                    volunteer_works_button.setText(volunteer_work_name + " (" + volunteer_work_status + ")");

                    volunteer_works_button.setOnClickListener(v -> {
                        Intent intent = new Intent(Barangay.this, VolunteerWorkDescription.class);
                        intent.putExtra("volunteer_work_id", volunteer_work);
                        startActivity(intent);
                    });

                    // Add the view to the current_volunteer_lists
                    current_volunteer_works_list.addView(currentVolunteerWorkView);
                }
            } else {
                Log.w("Firestore", "Error getting documents.", task.getException());
                Toast.makeText(this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }


}