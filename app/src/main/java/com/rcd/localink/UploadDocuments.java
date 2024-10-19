package com.rcd.localink;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class UploadDocuments extends AppCompatActivity {


    Button upload_valid_id;

    EditText type_of_work;

    LinearLayout type_of_work_container;

    Button logout;

    String downloadUrl;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_upload_documents);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        upload_valid_id = findViewById(R.id.upload_valid_id);
        type_of_work = findViewById(R.id.type_of_work);
        type_of_work_container = findViewById(R.id.type_of_work_container);
        logout = findViewById(R.id.logout);


        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String userType = sharedPrefs.getString("user_type", "");

        if ("Worker".equals(userType)) {
            type_of_work_container.setVisibility(View.VISIBLE);
        } else {
            type_of_work_container.setVisibility(View.GONE);
        }

        String valid_id = sharedPrefs.getString("valid_id", "");

        if (!valid_id.isEmpty()) {
            // go to Dashboard Activity
            Intent intent = new Intent(UploadDocuments.this, Dashboard.class);
            startActivity(intent);
            finish();
        }

        // Create an ActivityResultLauncher
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                // Handle the result data

                String filename = UUID.randomUUID().toString();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference();
                StorageReference validIdRef = storageRef.child("valid_ids/" + filename);
                UploadTask uploadTask = validIdRef.putFile(data.getData());
                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        upload_valid_id.setEnabled(true);
                        upload_valid_id.setText("UPLOAD VALID ID (PDF/JPG/PNG)");
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return validIdRef.getDownloadUrl();
                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        downloadUrl = downloadUri.toString();

                        SharedPreferences sharedPrefss = getSharedPreferences("userAuth", MODE_PRIVATE);
                        String documentId = sharedPrefss.getString("documentId", "");
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("users").document(documentId)
                                .update("valid_id", downloadUrl, "type_of_work", type_of_work.getText().toString());

                        SharedPreferences.Editor editor = sharedPrefss.edit();
                        editor.putString("valid_id", downloadUrl);
                        editor.apply();

                        Toast.makeText(UploadDocuments.this, "Upload of valid id successful", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(UploadDocuments.this, Dashboard.class);
                        startActivity(intent);
                        finish();

                        upload_valid_id.setEnabled(true);
                        upload_valid_id.setText("UPLOAD VALID ID (PDF/JPG/PNG)");

                        // TODO: Redirect to dashboard
                    } else {
                        // Handle failures
                        // ...
                        Log.d("UploadValidId", "uploadTask:failed ", task.getException());
                        Toast.makeText(UploadDocuments.this, "Upload of valid id failed", Toast.LENGTH_SHORT).show();
                        upload_valid_id.setEnabled(true);
                        upload_valid_id.setText("UPLOAD VALID ID (PDF/JPG/PNG)");
                    }
                });
            }
        });


        upload_valid_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");

                upload_valid_id.setEnabled(false);
                upload_valid_id.setText("Uploading ...");

                launcher.launch(intent);



            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPrefs.edit();
                editor.clear();
                editor.apply();

                Toast.makeText(UploadDocuments.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                startActivity(new Intent(UploadDocuments.this, MainActivity.class));
            }
        });

    }
}