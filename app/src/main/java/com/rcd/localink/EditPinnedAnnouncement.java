package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.UUID;

public class EditPinnedAnnouncement extends AppCompatActivity {

    private Uri profile_uri = null;

    private String firebaseDownloadUrl = null;


    private EditText titleEditText;
    private EditText descriptionEditText;
    private EditText dateEditText;
    private EditText timeEditText;
    private ImageView image;
    private Button uploadImageButton;
    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_pinned_announcement);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        titleEditText = findViewById(R.id.pinned_announcement_title);
        descriptionEditText = findViewById(R.id.pinned_announcement_description);
        dateEditText = findViewById(R.id.pinned_announcement_date);
        timeEditText = findViewById(R.id.pinned_announcement_time);
        image = findViewById(R.id.post_image);
        uploadImageButton = findViewById(R.id.upload_image_button);
        updateButton = findViewById(R.id.update_announcement);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("announcements").limit(1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                DocumentSnapshot document = task.getResult().getDocuments().get(0);
                String title = document.getString("title");
                String description = document.getString("description");
                String date = document.getString("date");
                String time = document.getString("time");
                String imageUrl = document.getString("image");

                titleEditText.setText(title);
                descriptionEditText.setText(description);
                dateEditText.setText(date);
                timeEditText.setText(time);

                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Picasso.get().load(imageUrl).into(image);
                }
            } else {
                Toast.makeText(EditPinnedAnnouncement.this, "Failed to load announcement", Toast.LENGTH_SHORT).show();
            }
        });


        ImageView backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(view -> {
            finish();
        });

        updateButton.setOnClickListener(v -> {

            if (profile_uri != null) {
                // Upload the profile_uri to Firebase Storage
                String filename = UUID.randomUUID().toString();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference profileRef = storageRef.child("profile_picture/" + filename);
                UploadTask uploadTask = profileRef.putFile(profile_uri);
                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        // Handle the failed upload
                        Log.e(TAG, "Error uploading profile", task.getException());


                        Toast.makeText(EditPinnedAnnouncement.this, "Something went wrong", Toast.LENGTH_SHORT).show();

                        updateButton.setText("UPDATE ANNOUNCEMENT");
                        updateButton.setEnabled(true);
                        return null;
                    }
                    // Return the download URL
                    return profileRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "Profile Picture: " + task.getResult());
                        firebaseDownloadUrl = task.getResult().toString();

                        updateAnnouncement(titleEditText, descriptionEditText, dateEditText, timeEditText, firebaseDownloadUrl);
                    }
                });
            } else {
                updateAnnouncement(titleEditText, descriptionEditText, dateEditText, timeEditText, null);
            }


            Toast.makeText(titleEditText.getContext(), "Announcement updated successfully", Toast.LENGTH_SHORT).show();
            finish();


        });

        // Create an ActivityResultLauncher
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the result data
                        profile_uri = data.getData();
                        uploadImageButton.setEnabled(true);
                        uploadImageButton.setText("Change profile picture");
                        image.setImageURI(profile_uri);
                    }
                });

        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add your code logic here for the upload_profile button click event

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                uploadImageButton.setEnabled(false);
                uploadImageButton.setText("File picking ...");
                launcher.launch(intent);
            }
        });
    }

    private static void updateAnnouncement(EditText titleEditText, EditText descriptionEditText, EditText dateEditText, EditText timeEditText, String firebaseDownloadUrl) {
        String title = titleEditText.getText().toString();
        String description = descriptionEditText.getText().toString();
        String date = dateEditText.getText().toString();
        String time = timeEditText.getText().toString();
        // Perform update logic here


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (firebaseDownloadUrl == null) {
            db.collection("announcements").limit(1).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                    String id = document.getId();
                    // Perform update logic here
                    db.collection("announcements").document(id).update(
                            "title", title,
                            "description", description,
                            "date", date,
                            "time", time
                    );
                }
            });
        } else {
            db.collection("announcements").limit(1).get().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null && task.getResult().size() > 0) {
                    DocumentSnapshot document = task.getResult().getDocuments().get(0);
                    String id = document.getId();
                    // Perform update logic here
                    db.collection("announcements").document(id).update(
                            "title", title,
                            "description", description,
                            "date", date,
                            "time", time,
                            "image", firebaseDownloadUrl
                    );
                }
            });
        }

    }
}