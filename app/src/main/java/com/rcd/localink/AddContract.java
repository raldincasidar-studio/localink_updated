package com.rcd.localink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class AddContract extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_contract);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView backButton = findViewById(R.id.back_button);
        EditText dateOfContract = findViewById(R.id.date_of_contract);
        EditText duration = findViewById(R.id.duration);
        EditText siteOfContract = findViewById(R.id.site_of_contract);
        EditText notesToContractor = findViewById(R.id.notes_to_contractor);
        EditText otherSpecificNegotiations = findViewById(R.id.other_specific_negotiations);
        EditText modeOfPayment = findViewById(R.id.mode_of_payment);
        Button propose_contract = findViewById(R.id.propose_contract);

        // Go back
        backButton.setOnClickListener(v -> {
            finish();
        });

        String jobId = getIntent().getStringExtra("jobId");
        String type = getIntent().getStringExtra("type");

        propose_contract.setOnClickListener(v -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            HashMap<String, Object> data = new HashMap<>();
            data.put("jobId", jobId);
            data.put("type", type);
            data.put("dateOfContract", dateOfContract.getText().toString());
            data.put("duration", duration.getText().toString());
            data.put("siteOfContract", siteOfContract.getText().toString());
            data.put("notesToContractor", notesToContractor.getText().toString());
            data.put("otherSpecificNegotiations", otherSpecificNegotiations.getText().toString());
            data.put("modeOfPayment", modeOfPayment.getText().toString());
            data.put("status", "pending");
            data.put("dateAdded", FieldValue.serverTimestamp());
            db.collection("work_contracts").add(data).addOnSuccessListener(documentReference -> {
                Toast.makeText(AddContract.this, "Proposal sent", Toast.LENGTH_SHORT).show();
                finish();
            });

        });



    }
}