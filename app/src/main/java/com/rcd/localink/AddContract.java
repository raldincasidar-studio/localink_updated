package com.rcd.localink;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
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

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class AddContract extends AppCompatActivity {

    AtomicReference<String> contractId = new AtomicReference<>();

    EditText dateOfContract = null;
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
        dateOfContract = findViewById(R.id.date_of_contract);
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

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String profilePicture = sharedPrefs.getString("profile_picture", "");

        String jobId = getIntent().getStringExtra("jobId");
        String type = getIntent().getStringExtra("type");

        dateOfContract.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                dateOfContract.setText(format.format(calendar.getTime()));
            }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        duration.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (datePicker, year, month, dayOfMonth) -> {
                Calendar calendar = Calendar.getInstance();
                calendar.set(year, month, dayOfMonth);
                SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
                duration.setText(format.format(calendar.getTime()));
            }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });



        propose_contract.setOnClickListener(v -> {

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            AtomicReference<String> userId = new AtomicReference<>();
            if (type.equals("job")) {
                db.collection("job_postings").document(jobId).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null && document.exists()) {
                            userId.set(document.getString("employer_id"));
                            Toast.makeText(AddContract.this, "userId 1: " + userId.get(), Toast.LENGTH_SHORT).show();
                            addContract(jobId, type, duration, document.getString("employer_id"), siteOfContract, notesToContractor, otherSpecificNegotiations, modeOfPayment, sharedPrefs, db);

                        }
                    }
                });
            } else {
                userId.set(jobId);
                Toast.makeText(AddContract.this, "userId 2: " + userId.get(), Toast.LENGTH_SHORT).show();

                addContract(jobId, type, duration, jobId, siteOfContract, notesToContractor, otherSpecificNegotiations, modeOfPayment, sharedPrefs, db);

            }
        });

    }

    private void addContract(String jobId, String type, EditText duration, String for_id, EditText siteOfContract, EditText notesToContractor, EditText otherSpecificNegotiations, EditText modeOfPayment, SharedPreferences sharedPrefs, FirebaseFirestore db) {

        String documentId = sharedPrefs.getString("documentId", "");



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
        data.put("for", for_id);
        data.put("by", documentId);
        data.put("dateAdded", FieldValue.serverTimestamp());
        db.collection("work_contracts").add(data).addOnSuccessListener(documentReference -> {
            Toast.makeText(AddContract.this, "Proposal sent", Toast.LENGTH_SHORT).show();
            contractId.set(documentReference.getId());

            addData(db, for_id, documentReference.getId());
            finish();
        });
    }

    private void addData(FirebaseFirestore db, String userId, String extraIntent) {

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String firstName = sharedPrefs.getString("firstName", "");
        String myUserId = sharedPrefs.getString("documentId", "");
        String profilePicture = sharedPrefs.getString("profile_picture", "");
        String jobId = getIntent().getStringExtra("jobId");
        String type = getIntent().getStringExtra("type");


        db.collection("notifications").add(new HashMap<String, Object>(){{
            put("date_added", FieldValue.serverTimestamp());
            put("for", userId);
            put("by", myUserId);
            put("picture", profilePicture);
            put("notification_content", firstName + " have a new proposal for you");
            put("isSeen", false);
            put("extraIntent", extraIntent);
            put("type", "contract");
        }}).addOnSuccessListener(documentReference -> {
            Toast.makeText(AddContract.this, "Proposal successfuly sent! for: " + userId, Toast.LENGTH_SHORT).show();
        });
    }


    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            // Handle date selection
            String selectedDate = String.format("%d/%d/%d", dayOfMonth, month + 1, year);
            dateOfContract.setText(selectedDate);
        }, Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }
}