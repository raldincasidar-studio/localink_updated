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
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class UploadDocuments extends AppCompatActivity {


    Button upload_valid_id;

    Spinner type_of_work;

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
        String barangay_certificate = sharedPrefs.getString("barangay_certificate", "");
        String nbi_clearance = sharedPrefs.getString("nbi_clearance", "");

        if (!valid_id.isEmpty() || !barangay_certificate.isEmpty() || !nbi_clearance.isEmpty()) {
            // go to Dashboard Activity
            Intent intent = new Intent(UploadDocuments.this, AdminVerificationProcess.class);
            startActivity(intent);
            finish();
        }


        Button upload_barangay_clearance = findViewById(R.id.upload_barangay_certificate);

        AtomicReference<Uri> barangay_uri = new AtomicReference<>();

        // Create an ActivityResultLauncher
        ActivityResultLauncher<Intent> launcher_barangay_clearance = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the result data
                        barangay_uri.set(data.getData());
                        upload_barangay_clearance.setEnabled(true);
                        upload_barangay_clearance.setText("[OK!] Change Barangay Clearance (PDF/JPG/PNG)");
                    }
                });


        upload_barangay_clearance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add your code logic here for the upload_profile button click event

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                upload_barangay_clearance.setEnabled(false);
                upload_barangay_clearance.setText("File picking ...");
                launcher_barangay_clearance.launch(intent);
            }
        });

        Button upload_valid_id = findViewById(R.id.upload_valid_id);

        AtomicReference<Uri> valid_id_uri = new AtomicReference<>();

        // Create an ActivityResultLauncher
        ActivityResultLauncher<Intent> launcher_valid_id = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the result data
                        valid_id_uri.set(data.getData());
                        upload_valid_id.setEnabled(true);
                        upload_valid_id.setText("[OK!] Change Valid ID (PDF/JPG/PNG)");
                    }
                });


        upload_valid_id.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add your code logic here for the upload_profile button click event

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                upload_valid_id.setEnabled(false);
                upload_valid_id.setText("File picking ...");
                launcher_valid_id.launch(intent);
            }
        });

        Button upload_nbi_clearance = findViewById(R.id.upload_nbi_clearance);

        AtomicReference<Uri> nbi_uri = new AtomicReference<>();

        // Create an ActivityResultLauncher
        ActivityResultLauncher<Intent> launcher_nbi_clearance = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the result data
                        nbi_uri.set(data.getData());
                        upload_nbi_clearance.setEnabled(true);
                        upload_nbi_clearance.setText("[OK!] Change NBI Clearance (PDF/JPG/PNG)");
                    }
                });


        upload_nbi_clearance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add your code logic here for the upload_profile button click event

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                upload_nbi_clearance.setEnabled(false);
                upload_nbi_clearance.setText("File picking ...");
                launcher_nbi_clearance.launch(intent);
            }
        });


        Button submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {

              @Override
              public void onClick(View v) {

                  if(valid_id_uri.get() != null && barangay_uri.get() != null && nbi_uri.get() != null){
                      StorageReference storageReference = FirebaseStorage.getInstance().getReference();


                      String valid_id_filename = "valid_id_"+ UUID.randomUUID().toString();
                      String barangay_filename = "barangay_clearance_"+ UUID.randomUUID().toString();
                      String nbi_filename = "nbi_clearance_"+ UUID.randomUUID().toString();

                      UploadTask uploadTask1 = storageReference.child(valid_id_filename).putFile(valid_id_uri.get());
                      UploadTask uploadTask2 = storageReference.child(barangay_filename).putFile(barangay_uri.get());
                      UploadTask uploadTask3 = storageReference.child(nbi_filename).putFile(nbi_uri.get());

                      Task<List<Object>> allTasks = Tasks.whenAllSuccess(uploadTask1, uploadTask2, uploadTask3);

                      allTasks.addOnCompleteListener(new OnCompleteListener<List<Object>>() {
                          @Override
                          public void onComplete(@NonNull Task<List<Object>> task) {
                              if(task.isSuccessful()){

                                  Task<Uri> urlTask1 = uploadTask1.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                      @Override
                                      public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                          if (!task.isSuccessful()) {
                                              throw task.getException();
                                          }

                                          // Continue with the task to get the download URL
                                          return storageReference.child(valid_id_filename).getDownloadUrl();
                                      }
                                  });
                                  Task<Uri> urlTask2 = uploadTask2.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                      @Override
                                      public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                          if (!task.isSuccessful()) {
                                              throw task.getException();
                                          }

                                          // Continue with the task to get the download URL
                                          return storageReference.child(barangay_filename).getDownloadUrl();
                                      }
                                  });
                                  Task<Uri> urlTask3 = uploadTask3.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                      @Override
                                      public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                          if (!task.isSuccessful()) {
                                              throw task.getException();
                                          }

                                          // Continue with the task to get the download URL
                                          return storageReference.child(nbi_filename).getDownloadUrl();
                                      }
                                  });

                                  Task<List<Uri>> allTasksUrl = Tasks.whenAllSuccess(urlTask1, urlTask2, urlTask3);

                                  allTasksUrl.addOnCompleteListener(new OnCompleteListener<List<Uri>>() {
                                      @Override
                                      public void onComplete(@NonNull Task<List<Uri>> task) {
                                          if(task.isSuccessful()){
                                              List<Uri> result = task.getResult();
                                              FirebaseFirestore db = FirebaseFirestore.getInstance();

                                              Toast.makeText(UploadDocuments.this, "Download urls: "+result.get(0).toString()+", "+result.get(1).toString()+", "+result.get(2).toString(), Toast.LENGTH_SHORT).show();

                                              db.collection("users").document(sharedPrefs.getString("documentId", "")).update("valid_id", result.get(0).toString(), "barangay_clearance", result.get(1).toString(), "nbi_clearance", result.get(2).toString());

                                              if(sharedPrefs.getString("user_type", "").equals("Worker")){
                                                  db.collection("users").document(sharedPrefs.getString("documentId", "")).update("type_of_work", type_of_work.getSelectedItem().toString());
                                              }
                                          }
                                          else{
                                              Toast.makeText(UploadDocuments.this, "Error getting download url.", Toast.LENGTH_SHORT).show();
                                          }
                                      }
                                  });

                                  Toast.makeText(UploadDocuments.this, "All files uploaded successfully.", Toast.LENGTH_SHORT).show();
                                  Intent intent = new Intent(UploadDocuments.this, AdminVerificationProcess.class);
                                  startActivity(intent);
                              }
                              else{
                                  Toast.makeText(UploadDocuments.this, "Error uploading files.", Toast.LENGTH_SHORT).show();
                              }
                          }
                      });
                  }
                  else{
                      Toast.makeText(UploadDocuments.this, "Please upload all required documents.", Toast.LENGTH_SHORT).show();
                  }
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