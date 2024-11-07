package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class ContractPreview extends AppCompatActivity {


    public LinearLayout review_to_user;
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
        Button complete_contract = findViewById(R.id.complete_contract);







        LinearLayout accept_buttons = findViewById(R.id.accept_buttons);
        review_to_user = findViewById(R.id.review_to_user);

        String id = getIntent().getStringExtra("contractId");
        FirebaseFirestore db = FirebaseFirestore.getInstance();



        AtomicReference<String> by = new AtomicReference<>();
        AtomicReference<String> for_id = new AtomicReference<>();
        AtomicReference<String> job_id = new AtomicReference<>();



        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);

        initializeData(db, id, date_of_contract, duration, mode_of_payment, notes_to_contractor, other_specific_negotiations, site_of_contract, status, by, for_id, job_id, sharedPrefs, accept_buttons, complete_contract, contract_details);

        complete_contract.setOnClickListener(v -> {
            db.collection("work_contracts").document(id).update("status", "Completed")
                    .addOnSuccessListener(aVoid -> {
                        status.setText("Completed");


                        String user_to_notify = by.get().equals(sharedPrefs.getString("documentId", "")) ? for_id.get() : by.get();

                        db.collection("notifications").add(new HashMap<String, Object>(){{
                            put("date_added", FieldValue.serverTimestamp());
                            put("for", user_to_notify);
                            put("by", sharedPrefs.getString("documentId", ""));
                            put("picture", sharedPrefs.getString("profile_picture", ""));
                            put("notification_content", "Your contract with " + sharedPrefs.getString("firstName", "") + " has been completed. Leave a Review now!");
                            put("isSeen", false);
                            put("extraIntent", id);
                            put("type", "contract");
                        }}).addOnSuccessListener(documentReference -> {
                            Toast.makeText(ContractPreview.this, "Successfuly completed", Toast.LENGTH_SHORT).show();
                            initializeData(db, id, date_of_contract, duration, mode_of_payment, notes_to_contractor, other_specific_negotiations, site_of_contract, status, by, for_id, job_id, sharedPrefs, accept_buttons, complete_contract, contract_details);
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ContractPreview.this, "Failed to accept contract", Toast.LENGTH_SHORT).show();
                    });
        });


        accept.setOnClickListener(v -> {


            db.collection("work_contracts")
                .whereEqualTo("for", sharedPrefs.getString("documentId", ""))
                .whereEqualTo("status", "On-going transaction")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        Toast.makeText(ContractPreview.this, "You already have an ongoing transaction", Toast.LENGTH_SHORT).show();
                    } else {
                        db.collection("work_contracts")
                            .whereEqualTo("by", sharedPrefs.getString("documentId", ""))
                            .whereEqualTo("status", "On-going transaction")
                            .get()
                            .addOnCompleteListener(task2 -> {
                                if (task2.isSuccessful() && !task2.getResult().isEmpty()) {
                                    Toast.makeText(ContractPreview.this, "You already have an ongoing transaction", Toast.LENGTH_SHORT).show();
                                } else {
                                    db.collection("work_contracts").document(id).update("status", "On-going transaction")
                                        .addOnSuccessListener(aVoid -> {
                                            status.setText("On-going transaction");
                                            accept_buttons.setVisibility(View.GONE);

                                            db.collection("notifications").add(new HashMap<String, Object>() {{
                                                put("date_added", FieldValue.serverTimestamp());
                                                put("for", by.get());
                                                put("by", sharedPrefs.getString("documentId", ""));
                                                put("picture", sharedPrefs.getString("profile_picture", ""));
                                                put("notification_content", sharedPrefs.getString("firstName", "") + " have accepted your contract");
                                                put("isSeen", false);
                                                put("extraIntent", id);
                                                put("type", "contract");
                                            }}).addOnSuccessListener(documentReference -> {
                                                Toast.makeText(ContractPreview.this, "Successfully accepted", Toast.LENGTH_SHORT).show();
                                                initializeData(db, id, date_of_contract, duration, mode_of_payment, notes_to_contractor, other_specific_negotiations, site_of_contract, status, by, for_id, job_id, sharedPrefs, accept_buttons, complete_contract, contract_details);
                                            });
                                        });
                                }
                            });
                    }
                });


            db.collection("work_contracts").document(id).update("status", "On-going transaction")
                .addOnSuccessListener(aVoid -> {

                    status.setText("On-going transaction");


                    accept_buttons.setVisibility(View.GONE);

                    db.collection("notifications").add(new HashMap<String, Object>(){{
                        put("date_added", FieldValue.serverTimestamp());
                        put("for", by.get());
                        put("by", sharedPrefs.getString("documentId", ""));
                        put("picture", sharedPrefs.getString("profile_picture", ""));
                        put("notification_content", sharedPrefs.getString("firstName", "") + " have a accepted your contract");
                        put("isSeen", false);
                        put("extraIntent", id);
                        put("type", "contract");
                    }}).addOnSuccessListener(documentReference -> {
                        Toast.makeText(ContractPreview.this, "Successfuly accepted", Toast.LENGTH_SHORT).show();
                        initializeData(db, id, date_of_contract, duration, mode_of_payment, notes_to_contractor, other_specific_negotiations, site_of_contract, status, by, for_id, job_id, sharedPrefs, accept_buttons, complete_contract, contract_details);
                    });
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

                    db.collection("notifications").add(new HashMap<String, Object>(){{
                        put("date_added", FieldValue.serverTimestamp());
                        put("for", by.get());
                        put("by", sharedPrefs.getString("documentId", ""));
                        put("picture", sharedPrefs.getString("profile_picture", ""));
                        put("notification_content", sharedPrefs.getString("firstName", "") + " have a declined your contract");
                        put("isSeen", false);
                        put("extraIntent", id);
                        put("type", "contract");
                    }}).addOnSuccessListener(documentReference -> {
                        Toast.makeText(ContractPreview.this, "Successfuly accepted", Toast.LENGTH_SHORT).show();
                        initializeData(db, id, date_of_contract, duration, mode_of_payment, notes_to_contractor, other_specific_negotiations, site_of_contract, status, by, for_id, job_id, sharedPrefs, accept_buttons, complete_contract, contract_details);
                    });
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

    private void initializeData(FirebaseFirestore db, String id, TextView date_of_contract, TextView duration, TextView mode_of_payment, TextView notes_to_contractor, TextView other_specific_negotiations, TextView site_of_contract, TextView status, AtomicReference<String> by, AtomicReference<String> for_id, AtomicReference<String> job_id, SharedPreferences sharedPrefs, LinearLayout accept_buttons, Button complete_contract, Button contract_details) {
        db.collection("work_contracts").document(id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null) {
                    date_of_contract.setText(document.getString("dateOfContract"));
                    duration.setText(document.getString("duration"));
                    mode_of_payment.setText(document.getString("modeOfPayment"));
                    notes_to_contractor.setText(document.getString("notesToContractor"));
                    other_specific_negotiations.setText(document.getString("otherSpecificNegotiations"));
                    site_of_contract.setText(document.getString("siteOfContract"));
                    status.setText(document.getString("status"));

                    by.set(document.getString("by"));
                    for_id.set(document.getString("for"));
                    job_id.set(document.getString("jobId"));

                    ImageView back_button = findViewById(R.id.back_button);

                    back_button.setOnClickListener(v -> {
                        finish();
                    });

                    String type = document.getString("type");

                    String documentId = sharedPrefs.getString("documentId", "");

                    if (documentId.equals(for_id.get()) && document.getString("status").equals("pending") ) {
                        accept_buttons.setVisibility(View.VISIBLE);
                    } else {
                        accept_buttons.setVisibility(View.GONE);
                    }

                    if (document.getString("status").equals("On-going transaction") && sharedPrefs.getString("user_type", "").equals("Employer")) {
                        complete_contract.setVisibility(View.VISIBLE);
                    }
                    else {
                        complete_contract.setVisibility(View.GONE);
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



                    String opponent_id;
                    String other_id;
                    if(by.get().equals(sharedPrefs.getString("documentId", ""))){
                        opponent_id = for_id.get();
                        other_id = by.get();
                    } else {
                        opponent_id = by.get();
                        other_id = for_id.get();
                    }


                    LinearLayout profile_clickable = findViewById(R.id.profile_clickable);

                    profile_clickable.setOnClickListener(v -> {
                        Intent intent = new Intent(ContractPreview.this, PublicProfilePage.class);
                        intent.putExtra("id", opponent_id);
                        startActivity(intent);
                    });




                    db.collection("users").document(opponent_id).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String opponentFirstName = documentSnapshot.getString("firstName");
                            String opponentMiddleName = documentSnapshot.getString("middleName");
                            String opponentLastName = documentSnapshot.getString("lastName");
                            String opponentName = opponentFirstName + " " + opponentMiddleName + " " + opponentLastName;
                            String opponentEmail = documentSnapshot.getString("email");
                            String opponentProfilePicture = documentSnapshot.getString("profile_picture");
                            String opponentuserType = documentSnapshot.getString("user_type");

                            ImageView profile_pic = findViewById(R.id.worker_image);
                            TextView name = findViewById(R.id.name);
                            TextView user_type = findViewById(R.id.user_type);

                            Picasso.get().load(opponentProfilePicture).into(profile_pic);
                            name.setText(opponentName);
                            user_type.setText(opponentuserType);

                            // Use opponentName and opponentEmail as required
                        } else {
                            Toast.makeText(ContractPreview.this, "Opponent not found", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ContractPreview.this, "Error fetching opponent data", Toast.LENGTH_SHORT).show();
                    });

                    LinearLayout show_for_admin_clickable = findViewById(R.id.show_for_admin_clickable);

                    show_for_admin_clickable.setOnClickListener(v -> {
                        Intent intent = new Intent(ContractPreview.this, PublicProfilePage.class);
                        intent.putExtra("id", other_id);
                        startActivity(intent);
                    });


                    TextView contract_details_text = findViewById(R.id.contract_details_text);
                    if (sharedPrefs.getString("user_type", "").equals("Admin")) {
                        show_for_admin_clickable.setVisibility(View.VISIBLE);
                        review_to_user.setVisibility(View.GONE);
                        contract_details_text.setVisibility(View.GONE);
                        contract_details.setVisibility(View.GONE);
                    } else {
                        show_for_admin_clickable.setVisibility(View.GONE);
                        review_to_user.setVisibility(View.GONE);
                        contract_details_text.setVisibility(View.GONE);
                        contract_details.setVisibility(View.GONE);
                    }

                    if (document.getString("status").equals("Completed")) {
                        review_to_user.setVisibility(View.VISIBLE);
                    } else {
                        review_to_user.setVisibility(View.GONE);
                    }

                    db.collection("users").document(other_id).get().addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String opponentFirstName = documentSnapshot.getString("firstName");
                            String opponentMiddleName = documentSnapshot.getString("middleName");
                            String opponentLastName = documentSnapshot.getString("lastName");
                            String opponentName = opponentFirstName + " " + opponentMiddleName + " " + opponentLastName;
                            String opponentEmail = documentSnapshot.getString("email");
                            String opponentProfilePicture = documentSnapshot.getString("profile_picture");
                            String opponentuserType = documentSnapshot.getString("user_type");

                            ImageView profile_pic = findViewById(R.id.worker_image2);
                            TextView name = findViewById(R.id.name2);
                            TextView user_type = findViewById(R.id.user_type2);

                            Picasso.get().load(opponentProfilePicture).into(profile_pic);
                            name.setText(opponentName);
                            user_type.setText(opponentuserType);



                            // Use opponentName and opponentEmail as required
                        } else {
                            Toast.makeText(ContractPreview.this, "Opponent not found", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(e -> {
                        Toast.makeText(ContractPreview.this, "Error fetching opponent data", Toast.LENGTH_SHORT).show();
                    });

                    EditText comment_box = findViewById(R.id.comment_box);
                    Button comment_button = findViewById(R.id.comment_button);

                    comment_button.setOnClickListener(v -> {
                        String comment = comment_box.getText().toString();

                        SharedPreferences userPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
                        String jobId = userPrefs.getString("jobId", "");
                        String posterId = (by.get().equals(userPrefs.getString("documentId", "")) ? for_id.get() : by.get());
                        String userId = userPrefs.getString("documentId", "");
                        String firstName = userPrefs.getString("firstName", "");
                        String middleName = userPrefs.getString("middleName", "");
                        String lastName = userPrefs.getString("lastName", "");
                        String userName = firstName + " " + middleName + " " + lastName;
                        String userProfilePicture = userPrefs.getString("profile_picture", "");

                        HashMap<String, Object> review = new HashMap<>();
                        review.put("by", userId);
                        review.put("for", posterId);
                        review.put("by_name", userName);
                        review.put("by_profile_picture", userProfilePicture);
                        review.put("content", comment);
                        review.put("date_added", FieldValue.serverTimestamp());


                        db.collection("reviews").whereEqualTo("by", userId).whereEqualTo("for", posterId).get().addOnCompleteListener(task2 -> {
                            if (task2.isSuccessful() && task2.getResult().size() > 0) {
                                Toast.makeText(ContractPreview.this, "You already reviewed this person", Toast.LENGTH_SHORT).show();
                            } else {
                                db.collection("reviews").add(review).addOnSuccessListener(documentReference -> {
                                    Toast.makeText(ContractPreview.this, "Review sent", Toast.LENGTH_SHORT).show();
                                    comment_box.setText("");
                                });
                            }
                        });

                    });
                }
            }
        });
    }
}