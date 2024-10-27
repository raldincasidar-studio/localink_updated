package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.HashMap;


public class EditVolunteerWork extends AppCompatActivity {

    private  ImageView back_button;

    private EditText volunteer_name;

    private EditText duration;

    private EditText instruction;

    private CheckBox activate_accept_volunteer_works;

    private Button publish;

    private ImageView profile_image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_volunteer_work);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        volunteer_name = findViewById(R.id.volunteer_name);
        duration = findViewById(R.id.duration);
        instruction = findViewById(R.id.instruction);
        activate_accept_volunteer_works = findViewById(R.id.activate_accept_volunteer_works);
        publish = findViewById(R.id.publish);

        back_button = findViewById(R.id.back_button);

        profile_image = findViewById(R.id.profile_image);


        back_button.setOnClickListener(v -> {
            Intent intent = new Intent(EditVolunteerWork.this, Barangay.class);
            startActivity(intent);
        });

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String profilePicture = sharedPrefs.getString("profile_picture", "");
        Picasso.get().load(profilePicture).into(profile_image);

        String volunteerWorkId = getIntent().getStringExtra("volunteer_work_id");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("volunteer_works").document(volunteerWorkId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String volunteerName = document.getString("volunteer_name");
                    String durationStr = document.getString("duration");
                    String instructionStr = document.getString("instruction");
                    boolean activated = document.getBoolean("activated");

                    volunteer_name.setText(volunteerName);
                    duration.setText(durationStr);
                    instruction.setText(instructionStr);
                    activate_accept_volunteer_works.setChecked(activated);
                }
            }
        });


        publish.setOnClickListener(v -> {
            if(volunteer_name.getText().toString().isEmpty() || duration.getText().toString().isEmpty() || instruction.getText().toString().isEmpty()){
                Toast.makeText(EditVolunteerWork.this, "Please fill in all required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            HashMap<String, Object> volunteerWork = new HashMap<>();
            volunteerWork.put("volunteer_name", volunteer_name.getText().toString());
            volunteerWork.put("duration", duration.getText().toString());
            volunteerWork.put("instruction", instruction.getText().toString());
            volunteerWork.put("activated", activate_accept_volunteer_works.isChecked());

            db.collection("volunteer_works").document(volunteerWorkId).update(volunteerWork).addOnCompleteListener(task -> {
                if(task.isSuccessful()){
                    Toast.makeText(EditVolunteerWork.this, "Volunteer work updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditVolunteerWork.this, Barangay.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(EditVolunteerWork.this, "Error updating volunteer work", Toast.LENGTH_SHORT).show();
                }
            });
        });

    }
}