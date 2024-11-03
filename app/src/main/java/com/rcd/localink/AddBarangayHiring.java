package com.rcd.localink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddBarangayHiring extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_barangay_hiring);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        EditText contract_name = findViewById(R.id.contract_name);
        EditText starts_in = findViewById(R.id.starts_in);
        EditText ends_in = findViewById(R.id.ends_in);
        EditText Time = findViewById(R.id.time);
        EditText instruction = findViewById(R.id.instruction);
        CheckBox activate_accept_volunteer_works = findViewById(R.id.activate_accept_volunteer_works);
        Button publish = findViewById(R.id.publish);

        publish.setOnClickListener(v -> {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String contractName = contract_name.getText().toString();
            String startsIn = starts_in.getText().toString();
            String endsIn = ends_in.getText().toString();
            String time = Time.getText().toString();
            String instructionText = instruction.getText().toString();
            boolean activateAcceptVolunteerWorks = activate_accept_volunteer_works.isChecked();
            HashMap<String, Object> data = new HashMap<>();
            data.put("contractName", contractName);
            data.put("startsIn", startsIn);
            data.put("endsIn", endsIn);
            data.put("time", time);
            data.put("instruction", instructionText);
            data.put("activateAcceptVolunteerWorks", activateAcceptVolunteerWorks);
            data.put("dateAdded", FieldValue.serverTimestamp());
            db.collection("barangay_hiring").add(data).addOnSuccessListener(documentReference -> {
                Toast.makeText(AddBarangayHiring.this, "Added new barangay hiring", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(AddBarangayHiring.this, BarangayHiring.class);
                startActivity(intent);
            });
        });
    }
}