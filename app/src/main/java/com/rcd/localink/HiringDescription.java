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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class HiringDescription extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_hiring_description);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView back_button = findViewById(R.id.back_button);

        back_button.setOnClickListener(v -> {
            Intent intent = new Intent(HiringDescription.this, BarangayHiring.class);
            startActivity(intent);
        });


        TextView title = findViewById(R.id.title);
        Button edit_button = findViewById(R.id.edit_button);
        TextView duration = findViewById(R.id.duration);
        TextView instruction = findViewById(R.id.instruction);
        TextView questionPrompt = findViewById(R.id.question_prompt);
        Button yesButton = findViewById(R.id.yes_button);
        Button noButton = findViewById(R.id.no_button);
        Button done = findViewById(R.id.done_button);
        LinearLayout done_cont = findViewById(R.id.done_cont);
        LinearLayout yes_no = findViewById(R.id.yes_no);
        LinearLayout pending = findViewById(R.id.pending);

        FirebaseFirestore db = FirebaseFirestore.getInstance();



        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String type = sharedPrefs.getString("user_type", "");
        if (type.equals("admin")) {
            edit_button.setVisibility(View.VISIBLE);
        } else {
            edit_button.setVisibility(View.GONE);
        }

        yesButton.setOnClickListener(v -> {
            String userId = sharedPrefs.getString("documentId", "");
            HashMap<String, Object> data = new HashMap<>();
            data.put("userId", userId);
            data.put("user_fullname", sharedPrefs.getString("firstName", "") + " " + sharedPrefs.getString("middleName", "") + " " + sharedPrefs.getString("lastName", ""));
            data.put("profile_image", sharedPrefs.getString("profile_picture", ""));
            data.put("hiringId", getIntent().getStringExtra("volunteer_work_id"));
            data.put("hiring_name", title.getText().toString());
            data.put("status", "pending");
            data.put("duration", duration.getText().toString());
            data.put("date_added", FieldValue.serverTimestamp());

            db.collection("barangay_hiring_contracts").add(data).addOnSuccessListener(documentReference -> {
                Toast.makeText(HiringDescription.this, "Contract added successfully", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(HiringDescription.this, BarangayHiring.class);
                startActivity(intent);
            }).addOnFailureListener(e -> {
                Toast.makeText(HiringDescription.this, "Failed to add contract", Toast.LENGTH_SHORT).show();
            });
        });


        noButton.setOnClickListener(v -> {
            Intent intent = new Intent(HiringDescription.this, BarangayHiring.class);
            startActivity(intent);
        });

        String hiring_id = getIntent().getStringExtra("volunteer_work_id");

        db.collection("barangay_hiring").document(hiring_id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            title.setText(document.getString("contractName"));
                            edit_button.setOnClickListener(v -> {
                                Intent intent = new Intent(HiringDescription.this, EditBarangayHiring.class);
                                intent.putExtra("hiring_id", hiring_id);
                                startActivity(intent);
                            });
                            duration.setText("Starts in: " + document.getString("startsIn") + "\nEnds in: " + document.getString("endsIn") + "\nTime: " + document.getString("time"));
                            instruction.setText(document.getString("instruction"));

                            String userId = sharedPrefs.getString("documentId", "");


                            AtomicReference<String> hiring_contract = new AtomicReference<>();
                            db.collection("barangay_hiring_contracts").whereEqualTo("hiringId", hiring_id).whereEqualTo("userId", userId)
                                    .get()
                                    .addOnCompleteListener(task2 -> {
                                        if (task2.isSuccessful()) {
                                            QuerySnapshot querySnapshot = task2.getResult();
                                            if (querySnapshot != null && !querySnapshot.isEmpty()) {

                                                DocumentSnapshot document2 = querySnapshot.getDocuments().get(0);

                                                hiring_contract.set(document2.getId());

                                                if (document2.getString("status").equals("pending")) {
                                                    pending.setVisibility(View.VISIBLE);
                                                    yes_no.setVisibility(View.GONE);
                                                    done_cont.setVisibility(View.GONE);
                                                } else if (document2.getString("status").equals("active")) {
                                                    pending.setVisibility(View.GONE);
                                                    yes_no.setVisibility(View.GONE);
                                                    if (type.equals("admin")) {
                                                        done_cont.setVisibility(View.VISIBLE);
                                                    } else {
                                                        done_cont.setVisibility(View.GONE);
                                                    }
                                                } else if (document2.getString("status").equals("done")) {
                                                    pending.setVisibility(View.GONE);
                                                    yes_no.setVisibility(View.GONE);
                                                    if (type.equals("admin")) {
                                                        done_cont.setVisibility(View.VISIBLE);

                                                        done.setEnabled(false);
                                                        done.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                                        done.setText("Project already finished");
                                                    } else {
                                                        done_cont.setVisibility(View.GONE);
                                                    }
                                                }

                                            }
                                        }
                                    });


                            done.setOnClickListener(v -> {
                                String userId3 = sharedPrefs.getString("documentId", "");
                                String hiringId = getIntent().getStringExtra("volunteer_work_id");

                                db.collection("barangay_hiring_contracts").document(hiring_contract.get()).update("status", "done");

                                done.setEnabled(false);
                                done.setBackgroundTintList(ColorStateList.valueOf(Color.GRAY));
                                done.setText("Project already finished");
                            });
                        }
                    }
                });
    }
}