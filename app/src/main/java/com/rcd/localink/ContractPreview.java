package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.atomic.AtomicReference;

public class ContractPreview extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_contract_preview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        TextView date_of_contract = findViewById(R.id.date_of_contract);
        TextView duration = findViewById(R.id.duration);
        TextView mode_of_payment = findViewById(R.id.mode_of_payment);
        TextView notes_to_contractor = findViewById(R.id.notes_to_contractor);
        TextView other_specific_negotiations = findViewById(R.id.other_specific_negotiations);
        TextView site_of_contract = findViewById(R.id.site_of_contract);
        TextView status = findViewById(R.id.status);

        Button accept = findViewById(R.id.accept);
        Button decline = findViewById(R.id.decline);
        Button chat = findViewById(R.id.chat);
        Button contract_details = findViewById(R.id.contract_details);

        LinearLayout accept_buttons = findViewById(R.id.accept_buttons);

        String id = getIntent().getStringExtra("contractId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        AtomicReference<String> by = new AtomicReference<>();
        AtomicReference<String> for_id = new AtomicReference<>();
        AtomicReference<String> job_id = new AtomicReference<>();



        db.collection("work_contracts").document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    date_of_contract.setText(document.getString("dateOfContract"));
                    duration.setText(document.getString("duration"));
                    mode_of_payment.setText(document.getString("modeOfPayment"));
                    notes_to_contractor.setText(document.getString("notesToContractor"));
                    other_specific_negotiations.setText(document.getString("otherSpecificNegotiation"));
                    site_of_contract.setText(document.getString("siteOfContract"));
                    status.setText(document.getString("status"));

                    by.set(document.getString("by"));
                    for_id.set(document.getString("for"));
                    job_id.set(document.getString("jobId"));

                    String type = document.getString("type");


                    SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
                    String documentId = sharedPrefs.getString("documentId", "");

                    if (documentId.equals(for_id.get())) {
                        accept_buttons.setVisibility(View.VISIBLE);
                    } else {
                        accept_buttons.setVisibility(View.GONE);
                    }


                    if (type.equals("worker")) {
                        contract_details.setText("Hiring Details");
                        contract_details.setOnClickListener(v -> {
                            Intent intent = new Intent(ContractPreview.this, GigWorkerProfile.class);
                            intent.putExtra("workerId", by.get());
                            startActivity(intent);
                        });
                    } else if (type.equals("job")) {
                        contract_details.setText("Work Details");
                        contract_details.setOnClickListener(v -> {
                            Intent intent = new Intent(ContractPreview.this, JobPostDescription.class);
                            intent.putExtra("jobId", by.get());
                            startActivity(intent);
                        });
                    }
                }
            }
        });
        accept.setOnClickListener(v -> {
            db.collection("work_contracts").document(id).update("status", "Accepted")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ContractPreview.this, "Contract accepted", Toast.LENGTH_SHORT).show();
                    status.setText("Accepted");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ContractPreview.this, "Failed to accept contract", Toast.LENGTH_SHORT).show();
                });
        });

        decline.setOnClickListener(v -> {
            db.collection("work_contracts").document(id).update("status", "Declined")
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(ContractPreview.this, "Contract declined", Toast.LENGTH_SHORT).show();
                    status.setText("Declined");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ContractPreview.this, "Failed to decline contract", Toast.LENGTH_SHORT).show();
                });
        });

        chat.setOnClickListener(v -> {
            Toast.makeText(ContractPreview.this, "Chat with employer", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ContractPreview.this, ChatPage.class);
            intent.putExtra("person", by.get());
            startActivity(intent);
        });
    }
}