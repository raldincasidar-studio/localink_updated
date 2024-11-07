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

        String type = sharedPrefs.getString("user_type", "");
        if (type.equals("Admin")) {
            edit_button.setVisibility(View.VISIBLE);
        } else {
            edit_button.setVisibility(View.GONE);
        }

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
        LinearLayout participants_container = findViewById(R.id.participants_container);
        String user_id = sharedPrefs.getString("documentId", "");

        LinearLayout participants_box = findViewById(R.id.participants_box);

        if (type.equals("Admin")) {
            participants_box.setVisibility(View.VISIBLE);
        } else {
            participants_box.setVisibility(View.GONE);
        }

        Button sort_complete = findViewById(R.id.sort_complete);
        Button sort_incomplete = findViewById(R.id.sort_incomplete);
        Button sort_all = findViewById(R.id.sort_all);



        sort_all.setOnClickListener(v -> {
            loadData(db, volunteer_work_id, participants_container, sharedPrefs);
        });


        sort_complete.setOnClickListener(v -> {
            loadDataAndFilter(db, volunteer_work_id, participants_container, sharedPrefs, true);
        });

        sort_incomplete.setOnClickListener(v -> {
            loadDataAndFilter(db, volunteer_work_id, participants_container, sharedPrefs, false);
        });

        loadData(db, volunteer_work_id, participants_container, sharedPrefs);

        AtomicReference<String> volunteerTransactionId = new AtomicReference<>();
        db.collection("volunteer_transactions").whereEqualTo("volunteer", user_id).whereEqualTo("volunteer_work", volunteer_work_id).limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && querySnapshot.size() > 0) {


                    yes_no.setVisibility(View.GONE);

                    if (type.equals("Admin")) {
                        done_cont.setVisibility(View.VISIBLE);
                    }

                    if(querySnapshot.getDocuments().get(0).getBoolean("isCompleted")){
                        done_button.setEnabled(false);
                        done_button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808080")));
                        done_button.setText("VOLUNTEER IS COMPLETED!");
                        done_cont.setVisibility(View.VISIBLE);
                        return;
                    }

                    volunteerTransactionId.set(querySnapshot.getDocuments().get(0).getId());
                    question_prompt.setText("Work is pending. Please coordinate with your Barangay Administrator");
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



        if (type.equals("Admin")) {
            yes_no.setVisibility(View.GONE);
        }



    }

    private void loadData(FirebaseFirestore db, String volunteer_work_id, LinearLayout participants_container, SharedPreferences sharedPrefs) {
        db.collection("volunteer_transactions").whereEqualTo("volunteer_work", volunteer_work_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                participants_container.removeAllViews();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && querySnapshot.size() > 0) {

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        View view = getLayoutInflater().inflate(R.layout.barangay_participants, participants_container, false);
                        TextView name = view.findViewById(R.id.name);
                        TextView status = view.findViewById(R.id.status);
                        ImageView profile_image = view.findViewById(R.id.image);

                        name.setText(document.getString("user_fullname"));
                        status.setText(document.getBoolean("isCompleted") ? "Completed" : "Not yet completed");

                        String profile_picture = document.getString("profile_image");
                        if (profile_picture != null) {
                            Picasso.get().load(profile_picture).into(profile_image);
                        }



                        Button mark_as_complete = view.findViewById(R.id.mark_as_complete);

                        if (document.getBoolean("isCompleted")) {
                            status.setText("Completed");
                            mark_as_complete.setEnabled(false);
                            mark_as_complete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808080")));
                        }

                        mark_as_complete.setOnClickListener(v -> {
                            db.collection("volunteer_transactions").document(document.getId()).update("isCompleted", true).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    status.setText("Completed");
                                    mark_as_complete.setEnabled(false);
                                    mark_as_complete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808080")));


                                    // Add Notification
                                    db.collection("notifications").add(new HashMap<String, Object>(){{
                                        put("date_added", FieldValue.serverTimestamp());
                                        put("for", document.getString("volunteer"));
                                        put("by", sharedPrefs.getString("documentId", ""));
                                        put("picture", sharedPrefs.getString("profile_picture", ""));
                                        put("notification_content", "Administrator has marked your participation as completed!");
                                        put("isSeen", false);
                                        put("extraIntent", volunteer_work_id);
                                        put("type", "volunteer_work");
                                    }}).addOnSuccessListener(documentReference -> {
                                        Toast.makeText(VolunteerWorkDescription.this, "Successfuly marked as completed", Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    Toast.makeText(VolunteerWorkDescription.this, "Error marking transaction as completed", Toast.LENGTH_SHORT).show();
                                }
                            });

                        });

                        participants_container.addView(view);
                    }
                }
            }
        });
    }

    private void loadDataAndFilter(FirebaseFirestore db, String volunteer_work_id, LinearLayout participants_container, SharedPreferences sharedPrefs, Boolean status2) {
        db.collection("volunteer_transactions").whereEqualTo("volunteer_work", volunteer_work_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                participants_container.removeAllViews();
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && querySnapshot.size() > 0) {

                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        View view = getLayoutInflater().inflate(R.layout.barangay_participants, participants_container, false);
                        TextView name = view.findViewById(R.id.name);
                        TextView status = view.findViewById(R.id.status);
                        ImageView profile_image = view.findViewById(R.id.image);

                        name.setText(document.getString("user_fullname"));
                        status.setText(document.getBoolean("isCompleted") ? "Completed" : "Not yet completed");

                        String profile_picture = document.getString("profile_image");
                        if (profile_picture != null) {
                            Picasso.get().load(profile_picture).into(profile_image);
                        }



                        Button mark_as_complete = view.findViewById(R.id.mark_as_complete);

                        if (document.getBoolean("isCompleted")) {
                            status.setText("Completed");
                            mark_as_complete.setEnabled(false);
                            mark_as_complete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808080")));
                        }

                        mark_as_complete.setOnClickListener(v -> {
                            db.collection("volunteer_transactions").document(document.getId()).update("isCompleted", true).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    status.setText("Completed");
                                    mark_as_complete.setEnabled(false);
                                    mark_as_complete.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#808080")));


                                    // Add Notification
                                    db.collection("notifications").add(new HashMap<String, Object>(){{
                                        put("date_added", FieldValue.serverTimestamp());
                                        put("for", document.getString("volunteer"));
                                        put("by", sharedPrefs.getString("documentId", ""));
                                        put("picture", sharedPrefs.getString("profile_picture", ""));
                                        put("notification_content", "Administrator has marked your participation as completed!");
                                        put("isSeen", false);
                                        put("extraIntent", volunteer_work_id);
                                        put("type", "volunteer_work");
                                    }}).addOnSuccessListener(documentReference -> {
                                        Toast.makeText(VolunteerWorkDescription.this, "Successfuly marked as completed", Toast.LENGTH_SHORT).show();
                                    });
                                } else {
                                    Toast.makeText(VolunteerWorkDescription.this, "Error marking transaction as completed", Toast.LENGTH_SHORT).show();
                                }
                            });

                        });


                        if (document.getBoolean("isCompleted").equals(status2)) {
                            participants_container.addView(view);
                        }
                    }
                }
            }
        });
    }


}