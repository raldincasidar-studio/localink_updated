package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class VolunteerWorkDescription extends AppCompatActivity {

    private ImageView back_button;
    private ImageView profile_image;

    private TextView title;

    private TextView duration;
    private TextView instruction;
    private Button yes_button;
    private Button no_button;

    private Button done_button;
    private LinearLayout done_cont;
    private LinearLayout yes_no;
    private TextView question_prompt;

    private Button edit_button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_volunteer_work_description);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back_button = findViewById(R.id.back_button);
        profile_image = findViewById(R.id.profile_image);



        back_button.setOnClickListener(v -> {
            Intent intent = new Intent(VolunteerWorkDescription.this, Barangay.class);
            startActivity(intent);
        });

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String profilePicture = sharedPrefs.getString("profile_picture", "");
        Picasso.get().load(profilePicture).into(profile_image);

        title = findViewById(R.id.title);

        duration = findViewById(R.id.duration);
        instruction = findViewById(R.id.instruction);
        yes_button = findViewById(R.id.yes_button);
        no_button = findViewById(R.id.no_button);
        done_button = findViewById(R.id.done_button);
        done_cont = findViewById(R.id.done_cont);
        yes_no = findViewById(R.id.yes_no);
        edit_button = findViewById(R.id.edit_button);

        question_prompt = findViewById(R.id.question_prompt);

        String volunteer_work_id = getIntent().getStringExtra("volunteer_work_id");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("volunteer_works").document(volunteer_work_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String volunteer_name = document.getString("volunteer_name");
                    String duration_str = document.getString("duration");
                    String instruction_str = document.getString("instruction");

                    title.setText(volunteer_name);
                    duration.setText(duration_str);
                    instruction.setText(instruction_str);
                }
            }
        });

        String user_id = sharedPrefs.getString("documentId", "");
        AtomicReference<String> volunteerTransactionId = new AtomicReference<>();
        db.collection("volunteer_transactions").whereEqualTo("volunteer", user_id).whereEqualTo("volunteer_work", volunteer_work_id).limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && querySnapshot.size() > 0) {


                    yes_no.setVisibility(View.GONE);
                    done_cont.setVisibility(View.VISIBLE);

                    if(querySnapshot.getDocuments().get(0).getBoolean("isCompleted")){
                        done_button.setEnabled(false);
                        done_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808080")));
                        done_button.setText("VOLUNTEER IS COMPLETED!");
                        return;
                    }

                    volunteerTransactionId.set(querySnapshot.getDocuments().get(0).getId());
                    question_prompt.setText("Have you completed this volunteer work?");
                }
            }
        });

        done_button.setOnClickListener(v -> {
            if(volunteerTransactionId.get() != null){
                db.collection("volunteer_transactions").document(volunteerTransactionId.get()).update("isCompleted", true).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(VolunteerWorkDescription.this, "Transaction updated successfully", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(VolunteerWorkDescription.this, Barangay.class);
                        startActivity(intent);
                    }
                });
            }
        });


        edit_button.setOnClickListener(v -> {
            Intent intent = new Intent(VolunteerWorkDescription.this, EditVolunteerWork.class);
            intent.putExtra("volunteer_work_id", volunteer_work_id);
            startActivity(intent);
        });

        no_button.setOnClickListener(v -> {
            Intent intent = new Intent(VolunteerWorkDescription.this, Barangay.class);
            startActivity(intent);
        });

        yes_button.setOnClickListener(v -> {

            String user_id1 = sharedPrefs.getString("documentId", "");
            String firstName = sharedPrefs.getString("firstName", "");
            String middleName = sharedPrefs.getString("middleName", "");
            String lastName = sharedPrefs.getString("lastName", "");
            String user_fullname = firstName + " " + middleName + " " + lastName;
            String profile_image_str = sharedPrefs.getString("profile_picture", "");

            HashMap<String, Object> volunteerTransaction = new HashMap<>();
            volunteerTransaction.put("date_added", FieldValue.serverTimestamp());
            volunteerTransaction.put("volunteer", user_id1);
            volunteerTransaction.put("user_fullname", user_fullname);
            volunteerTransaction.put("profile_image", profile_image_str);
            volunteerTransaction.put("isCompleted", false);
            volunteerTransaction.put("volunteer_work", volunteer_work_id);
            volunteerTransaction.put("volunteer_name", title.getText().toString());
            volunteerTransaction.put("duration", duration.getText().toString());

            db.collection("volunteer_transactions").add(volunteerTransaction).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(VolunteerWorkDescription.this, "Successfuly Volunteered", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(VolunteerWorkDescription.this, Barangay.class);
                    startActivity(intent);
                }
            });

        });



    }


}