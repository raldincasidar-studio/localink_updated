package com.rcd.localink;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class EditPost extends AppCompatActivity {

    private ImageView back_button;

    private ImageView my_profile_picture;

    private TextView my_name;

    private EditText post_content;

    private ImageView image_preview;

    private Button upload_image_button;

    private Button new_post_button;

    private Button delete_post_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_post);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        back_button = findViewById(R.id.back_button);
        my_profile_picture = findViewById(R.id.my_profile_picture);
        my_name = findViewById(R.id.my_full_name);
        post_content = findViewById(R.id.post_content);
        image_preview = findViewById(R.id.image_preview);
        upload_image_button = findViewById(R.id.upload_image_button);
        new_post_button = findViewById(R.id.new_post_button);
        delete_post_button = findViewById(R.id.delete_post_button);



        back_button.setOnClickListener(v -> {
            Intent intent = new Intent(EditPost.this, CommunityWall.class);
            startActivity(intent);
        });

        //        Get the profile image and display it
        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String profilePicture = sharedPrefs.getString("profile_picture", "");
        Picasso.get().load(profilePicture).into(my_profile_picture);

//        Show the full name in the screen
        String firstName = sharedPrefs.getString("firstName", "");
        String middleName = sharedPrefs.getString("middleName", "");
        String lastName = sharedPrefs.getString("lastName", "");
        String fullName = firstName + " " + middleName + " " + lastName;
        my_name.setText(fullName);



        String post_id = getIntent().getStringExtra("post_id");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("posts").document(post_id).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    post_content.setText(document.getString("postContent"));
                    String imageUri = document.getString("imageUrl");
                    if (!imageUri.isEmpty()) {
                        Picasso.get().load(imageUri).into(image_preview);
                        image_preview.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        AtomicReference<Uri> imageUri = new AtomicReference<>();
        ActivityResultLauncher<Intent> launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        // Handle the result data
                        imageUri.set(data.getData());

                        image_preview.setImageURI(imageUri.get());
                        image_preview.setVisibility(View.VISIBLE);

                        upload_image_button.setEnabled(true);
                        upload_image_button.setText("Change image");
                        image_preview.setImageURI(imageUri.get());
                    }
                });

        upload_image_button.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            upload_image_button.setEnabled(false);
            upload_image_button.setText("File picking ...");
            launcher.launch(intent);
        });

        new_post_button.setOnClickListener(v -> {

            new_post_button.setEnabled(false);
            new_post_button.setText("Updating ...");

            String updatedContent = post_content.getText().toString();
            if (imageUri.get() != null) {
                String randomFilename = "posts/" + UUID.randomUUID().toString();
                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReference().child(randomFilename);
                storageRef.putFile(imageUri.get())
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            db.collection("posts").document(post_id)
                                .update("postContent", updatedContent, "imageUrl", uri.toString())
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(EditPost.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(EditPost.this, CommunityWall.class);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(EditPost.this, "Failed to update post", Toast.LENGTH_SHORT).show();
                                        new_post_button.setEnabled(true);
                                        new_post_button.setText("SAVE CHANGES");
                                    }
                                });
                        });
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(EditPost.this, "Failed to upload image", Toast.LENGTH_SHORT).show();
                        new_post_button.setEnabled(true);
                        new_post_button.setText("SAVE CHANGES");
                    });
            } else {
                db.collection("posts").document(post_id)
                    .update("postContent", updatedContent)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(EditPost.this, "Post updated successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(EditPost.this, CommunityWall.class);
                            startActivity(intent);
                        } else {
                            Toast.makeText(EditPost.this, "Failed to update post", Toast.LENGTH_SHORT).show();
                            new_post_button.setEnabled(true);
                            new_post_button.setText("SAVE CHANGES");
                        }
                    });
            }
        });


        delete_post_button.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Delete post?");
            builder.setMessage("This action is permanent and cannot be undone. Are you sure you want to delete this post?");
            builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    db.collection("posts").document(post_id)
                            .delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EditPost.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(EditPost.this, CommunityWall.class);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(EditPost.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            });
            builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            builder.create().show();
        });
    }
}