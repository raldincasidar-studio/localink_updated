package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class Dashboard extends AppCompatActivity {

    private  ImageView back_button;
    private  ImageView profile_image;

    private ImageView announcement_image;

    private TextView announcement_title;
    private TextView announcement_description;
    private TextView announcement_date;

    private LinearLayout barangay_button;

    private LinearLayout community_wall_button;
    private LinearLayout gig_work;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        back_button = findViewById(R.id.back_button);
        profile_image = findViewById(R.id.profile_image);

        announcement_image = findViewById(R.id.announcement_image);
        announcement_title = findViewById(R.id.announcement_title);
        announcement_description = findViewById(R.id.announcement_description);
        announcement_date = findViewById(R.id.announcement_dateandtime);
        community_wall_button = findViewById(R.id.community_wall_button);
        barangay_button = findViewById(R.id.barangay_button);
        gig_work = findViewById(R.id.gig_work);
        ImageView notification = findViewById(R.id.notification);
        LinearLayout transaction_history_container = findViewById(R.id.transaction_history_container);

        notification.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Notifications.class);
            startActivity(intent);
        });

        community_wall_button.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, CommunityWall.class);
            startActivity(intent);
        });

        barangay_button.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, Barangay.class);
            startActivity(intent);
        });

        gig_work.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, GigWork.class);
            startActivity(intent);
        });


        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String user_type = sharedPrefs.getString("user_type", "");

        if (user_type.equals("Admin")) {
            Intent intent = new Intent(Dashboard.this, AdminPanel.class);
            startActivity(intent);
            finish();
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("announcements").whereEqualTo("isPinned", true)
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                String title = document.getString("title");
                String description = document.getString("description");
                String date = document.getString("date");
                String time = document.getString("time");

                announcement_title.setText(title);
                announcement_description.setText(description);
                announcement_date.setText(date + " - " + time);

                Picasso.get().load(document.getString("image")).into(announcement_image);
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });

        String profilePicture = sharedPrefs.getString("profile_picture", "");
        Picasso.get().load(profilePicture).into(profile_image);

        profile_image.setOnClickListener(v -> {
            Intent intent = new Intent(Dashboard.this, EditProfile.class);
            startActivity(intent);
        });

        back_button.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        SharedPreferences sharedPrefs2 = getSharedPreferences("userAuth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs2.edit();
                        editor.clear();
                        editor.apply();

                        Toast.makeText(Dashboard.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Dashboard.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();


        });



        String documentId = sharedPrefs.getString("documentId", "");

        db.collection("work_contracts")
                .whereEqualTo("for", documentId)
                .whereEqualTo("status", "Completed")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transaction_history_container.removeAllViews();
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
//                            work_contract_list.add(document.getData());


                            View view = getLayoutInflater().inflate(R.layout.transaction_history, null, false);
                            TextView contractor_name = view.findViewById(R.id.name);
                            TextView job_title = view.findViewById(R.id.type_of_work);
                            TextView status = view.findViewById(R.id.status);
                            ImageView contractor_image = view.findViewById(R.id.image);

                            contractor_name.setText(document.getString("notesToContractor"));
                            job_title.setText("Transaction Id: "+document.getId());
                            status.setText(document.getString("status"));

                            view.setOnClickListener(v -> {
                                Intent intent = new Intent(Dashboard.this, ContractPreview.class);
                                intent.putExtra("contractId", document.getId());
                                startActivity(intent);
                            });

                            if (status.getText().toString().equals("Completed")) {
                                status.setTextColor(Color.GREEN);
                            } else {
                                status.setTextColor(Color.parseColor("#FFA500"));
                            }

                            transaction_history_container.addView(view);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

        db.collection("work_contracts")
                .whereEqualTo("by", documentId)
                .whereEqualTo("status", "Completed")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        transaction_history_container.removeAllViews();
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
//                            work_contract_list.add(document.getData());

                            View view = getLayoutInflater().inflate(R.layout.transaction_history, null, false);
                            TextView contractor_name = view.findViewById(R.id.name);
                            TextView job_title = view.findViewById(R.id.type_of_work);
                            TextView status = view.findViewById(R.id.status);
                            ImageView contractor_image = view.findViewById(R.id.image);

                            contractor_name.setText(document.getString("notesToContractor"));
                            job_title.setText("Transaction Id: "+document.getId());
                            status.setText(document.getString("status"));

                            Picasso.get().load(document.getString("profile_picture")).into(contractor_image);

                            if (status.getText().toString().equals("Completed")) {
                                status.setTextColor(Color.GREEN);
                            } else {
                                status.setTextColor(Color.parseColor("#FFA500"));
                            }


                            view.setOnClickListener(v -> {
                                Intent intent = new Intent(Dashboard.this, ContractPreview.class);
                                intent.putExtra("contractId", document.getId());
                                startActivity(intent);
                            });


                            transaction_history_container.addView(view);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });


        LinearLayout ongoing_transaction_container = findViewById(R.id.ongoing_transaction_container);

        db.collection("work_contracts")
                .whereEqualTo("by", documentId)
                .whereEqualTo("status", "On-going transaction")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            ongoing_transaction_container.removeAllViews();
                        }
                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
//                            work_contract_list.add(document.getData());

                            View view = getLayoutInflater().inflate(R.layout.transaction_history, null, false);
                            TextView contractor_name = view.findViewById(R.id.name);
                            TextView job_title = view.findViewById(R.id.type_of_work);
                            TextView status = view.findViewById(R.id.status);
                            ImageView contractor_image = view.findViewById(R.id.image);

                            contractor_name.setText(document.getString("notesToContractor"));
                            job_title.setText(document.getString("modeOfPayment"));
                            status.setText(document.getString("status"));

                            if (status.getText().toString().equals("Completed")) {
                                status.setTextColor(Color.GREEN);
                            } else {
                                status.setTextColor(Color.parseColor("#FFA500"));
                            }


                            view.setOnClickListener(v -> {
                                Intent intent = new Intent(Dashboard.this, ContractPreview.class);
                                intent.putExtra("contractId", document.getId());
                                startActivity(intent);
                            });


                            ongoing_transaction_container.addView(view);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });

        db.collection("work_contracts")
                .whereEqualTo("for", documentId)
                .whereEqualTo("status", "On-going transaction")
                .limit(1)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {

                        if (!task.getResult().isEmpty()) {
                            ongoing_transaction_container.removeAllViews();
                        }

                        for (DocumentSnapshot document : task.getResult()) {
                            Log.d(TAG, document.getId() + " => " + document.getData());
//                            work_contract_list.add(document.getData());

                            View view = getLayoutInflater().inflate(R.layout.transaction_history, null, false);
                            TextView contractor_name = view.findViewById(R.id.name);
                            TextView job_title = view.findViewById(R.id.type_of_work);
                            TextView status = view.findViewById(R.id.status);
                            ImageView contractor_image = view.findViewById(R.id.image);

                            contractor_name.setText(document.getString("notesToContractor"));
                            job_title.setText(document.getString("modeOfPayment"));
                            status.setText(document.getString("status"));

                            if (status.getText().toString().equals("Completed")) {
                                status.setTextColor(Color.GREEN);
                            } else {
                                status.setTextColor(Color.parseColor("#FFA500"));
                            }


                            view.setOnClickListener(v -> {
                                Intent intent = new Intent(Dashboard.this, ContractPreview.class);
                                intent.putExtra("contractId", document.getId());
                                startActivity(intent);
                            });


                            ongoing_transaction_container.addView(view);
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });



    }
}