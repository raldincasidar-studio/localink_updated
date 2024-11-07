package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class CommunityWall extends AppCompatActivity {

    private ImageView back_button;

    private ImageView my_profile_picture;

    private TextView my_name;

    private EditText post_content;

    private ImageView image_preview;

    private Button upload_image_button;

    private Button new_post_button;

    private LinearLayout like_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_community_wall);
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
            post_content.setEnabled(false);
            upload_image_button.setEnabled(false);
            new_post_button.setText("Posting ...");

            if (imageUri.get() != null) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("posts/" + UUID.randomUUID().toString());
                UploadTask uploadTask = storageReference.putFile(imageUri.get());
                Task<Uri> urlTask = uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }

                    // Continue with the task to get the download URL
                    return storageReference.getDownloadUrl();

                }).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        String imageUrl = downloadUri.toString();
                        String postContent = post_content.getText().toString();
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("posts")
                                .add(new HashMap<String, Object>() {{

                                    String userId = sharedPrefs.getString("documentId", "");
                                    String user_fullname = sharedPrefs.getString("firstName", "") + " " + sharedPrefs.getString("middleName", "") + " " + sharedPrefs.getString("lastName", "");
                                    FieldValue date_added = FieldValue.serverTimestamp();
                                    String profile_image = sharedPrefs.getString("profile_picture", "");

                                    put("userId", userId);
                                    put("user_fullname", user_fullname);
                                    put("date_added", date_added);
                                    put("profile_image", profile_image);
                                    put("postContent", postContent);
                                    put("imageUrl", imageUrl);
                                    put("likes", new ArrayList<>());
                                    put("comments", new ArrayList<>());
                                }})
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                                        getPosts();
                                        Toast.makeText(CommunityWall.this, "Post added successfully", Toast.LENGTH_SHORT).show();

                                        image_preview.setVisibility(View.GONE);
                                        post_content.setText("");
                                        imageUri.set(null);
                                        new_post_button.setEnabled(true);
                                        new_post_button.setText("New Post");
                                        post_content.setEnabled(true);
                                        upload_image_button.setEnabled(true);
                                        upload_image_button.setText("Upload Image");

                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.w(TAG, "Error adding document", e);

                                        Toast.makeText(CommunityWall.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();

                                        new_post_button.setEnabled(true);
                                        upload_image_button.setText("New Post");
                                    }
                                });
                    }
                    else {

                        Toast.makeText(CommunityWall.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();

                        new_post_button.setEnabled(true);
                        upload_image_button.setText("New Post");

                        Log.e(TAG, task.getException().toString());
                    }
                });
            } else {
                String postContent = post_content.getText().toString();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("posts")
                        .add(new HashMap<String, Object>() {{

                            String userId = sharedPrefs.getString("documentId", "");
                            String user_fullname = sharedPrefs.getString("firstName", "") + " " + sharedPrefs.getString("middleName", "") + " " + sharedPrefs.getString("lastName", "");
                            FieldValue date_added = FieldValue.serverTimestamp();
                            String profile_image = sharedPrefs.getString("profile_picture", "");

                            put("userId", userId);
                            put("user_fullname", user_fullname);
                            put("date_added", date_added);
                            put("profile_image", profile_image);
                            put("postContent", postContent);
                            put("imageUrl", "");
                            put("likes", new ArrayList<>());
                            put("comments", new ArrayList<>());
                        }})
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                                getPosts();
                                Toast.makeText(CommunityWall.this, "Post added successfully", Toast.LENGTH_SHORT).show();

                                image_preview.setVisibility(View.GONE);
                                post_content.setText("");
                                imageUri.set(null);
                                new_post_button.setEnabled(true);
                                new_post_button.setText("New Post");
                                post_content.setEnabled(true);
                                upload_image_button.setEnabled(true);
                                upload_image_button.setText("Upload Image");

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w(TAG, "Error adding document", e);

                                Toast.makeText(CommunityWall.this, "Something went wrong, please try again", Toast.LENGTH_SHORT).show();

                                new_post_button.setEnabled(true);
                                upload_image_button.setText("New Post");
                            }
                        });
            }
        });




        back_button.setOnClickListener(v -> {
            finish();
        });


        getPosts();
    }

    private void getPosts() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("posts")
                .orderBy("date_added", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            LinearLayout linearLayout = findViewById(R.id.posts_container);
                            linearLayout.removeAllViews();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                View postView = getLayoutInflater().inflate(R.layout.post, null);
                                ImageView post_profile_picture = postView.findViewById(R.id.post_profile_picture);
                                TextView post_content_textView = postView.findViewById(R.id.post_content);
                                ImageView post_image_imageView = postView.findViewById(R.id.post_image);
                                TextView post_username_textView = postView.findViewById(R.id.post_name);
                                TextView post_date_textView = postView.findViewById(R.id.post_date);
                                LinearLayout like_button = postView.findViewById(R.id.like_button);
                                ImageView edit_post = postView.findViewById(R.id.edit_post);

                                String post_id = document.getId();
                                String postContent = document.getString("postContent");
                                String profile_image = document.getString("profile_image");
                                String imageUrl = document.getString("imageUrl");
                                String user_fullname = document.getString("user_fullname");
                                String userId3 = document.getString("userId");
                                Timestamp date_added = document.getTimestamp("date_added");

                                long likesCount = document.get("likes") == null ? 0 : ((ArrayList<?>)document.get("likes")).size();
                                long commentsCount = document.get("comments") == null ? 0 : ((ArrayList<?>)document.get("comments")).size();

                                SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
                                String documentId = sharedPrefs.getString("documentId", "");
                                if (userId3.equals(documentId) || sharedPrefs.getString("user_type", "").equals("Admin")) {
                                    edit_post.setVisibility(View.VISIBLE);
                                } else {
                                    edit_post.setVisibility(View.GONE);
                                }

                                TextView post_likes_textView = postView.findViewById(R.id.post_likes);
                                TextView post_comments_textView = postView.findViewById(R.id.post_comments);
                                EditText comment_box = postView.findViewById(R.id.comment_box);
                                Button comment_button = postView.findViewById(R.id.comment_button);

                                if (profile_image != "") {
                                    Picasso.get().load(profile_image).into(post_profile_picture);
                                }

                                post_likes_textView.setText(likesCount + " like(s)");
                                post_comments_textView.setText(commentsCount + " comment(s)");

                                ArrayList likesArray = (ArrayList<?>)document.get("likes");



                                if (likesArray.contains(sharedPrefs.getString("documentId", ""))) {
                                    post_likes_textView.setTextColor(Color.rgb(255, 0, 0));
                                }

                                post_content_textView.setText(postContent);
                                if (imageUrl != null && !imageUrl.isEmpty()) {
                                    Picasso.get().load(imageUrl).into(post_image_imageView);
                                } else {
                                    post_image_imageView.setVisibility(View.GONE);
                                }

                                comment_button.setOnClickListener(v -> {
                                    comment_button.setEnabled(false);
                                    comment_button.setText("Commenting...");

                                    String comment = comment_box.getText().toString();
                                    if (!comment.isEmpty()) {

                                        String profilePicture = sharedPrefs.getString("profile_picture", "");

                                        String firstName = sharedPrefs.getString("firstName", "");
                                        String middleName = sharedPrefs.getString("middleName", "");
                                        String lastName = sharedPrefs.getString("lastName", "");
                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                        HashMap<String, Object> newComment = new HashMap<>();
                                        newComment.put("userId", sharedPrefs.getString("documentId", ""));
                                        newComment.put("user_fullname", fullName);
                                        newComment.put("profile_image", sharedPrefs.getString("profile_picture", ""));
                                        newComment.put("date_added", new Timestamp(new Date()));
                                        newComment.put("comment", comment);
                                        newComment.put("likes", new ArrayList<>());

                                        db.collection("posts").document(post_id).update("comments", FieldValue.arrayUnion(newComment))
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(CommunityWall.this, "Comment added", Toast.LENGTH_SHORT).show();
                                                comment_box.setText("");

                                                comment_button.setEnabled(true);
                                                comment_button.setText("Comment");


                                                getPosts();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(CommunityWall.this, "Failed to add comment", Toast.LENGTH_SHORT).show();

                                                comment_button.setEnabled(true);
                                                comment_button.setText("Comment");
                                            });

                                        comment_box.setText("");
                                    }
                                });


                                like_button.setOnClickListener(v -> {

                                    if (likesArray.contains(sharedPrefs.getString("documentId", ""))) {

                                        post_likes_textView.setText("Unliking...");

                                        db.collection("posts").document(post_id).update("likes", FieldValue.arrayRemove(sharedPrefs.getString("documentId", "")))
                                            .addOnSuccessListener(aVoid -> {
                                                post_likes_textView.setTextColor(Color.rgb(0, 0, 0));
                                                like_button.setEnabled(true);
                                                getPosts();
                                            })
                                            .addOnFailureListener(e -> {
                                                Toast.makeText(CommunityWall.this, "Failed to unlike", Toast.LENGTH_SHORT).show();
                                                like_button.setEnabled(true);
                                                post_likes_textView.setText(likesCount + " like(s)");
                                            });

                                        return;
                                    }

                                    post_likes_textView.setText("Liking...");

                                    SharedPreferences sharedPrefs2 = getSharedPreferences("userAuth", MODE_PRIVATE);
                                    String userId = sharedPrefs2.getString("documentId", "");

                                    db.collection("posts").document(post_id).update("likes", FieldValue.arrayUnion(userId))
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(CommunityWall.this, "Liked", Toast.LENGTH_SHORT).show();
                                            like_button.setEnabled(true);
                                            getPosts();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(CommunityWall.this, "Failed to like", Toast.LENGTH_SHORT).show();

                                            like_button.setEnabled(true);
                                            post_likes_textView.setText(likesCount + " like(s)");
                                        });

                                });


                                edit_post.setOnClickListener(v -> {

                                    Intent intent = new Intent(CommunityWall.this, EditPost.class);
                                    intent.putExtra("post_id", post_id);
                                    startActivity(intent);
                                });

                                // Display comments
                                LinearLayout comment_container = postView.findViewById(R.id.comment_container);
                                comment_container.removeAllViews();
                                List<HashMap<String, Object>> comments = (List<HashMap<String, Object>>) document.get("comments");
                                if (comments != null) {
                                    for (HashMap<String, Object> comment : comments) {
                                        View commentView = getLayoutInflater().inflate(R.layout.commss, null);
                                        ImageView comment_profile_pic = commentView.findViewById(R.id.comment_profile_pic);
                                        TextView comment_fullname_textView = commentView.findViewById(R.id.comment_fullname);
                                        TextView comment_date_textView = commentView.findViewById(R.id.comment_date);
                                        TextView comment_comment_textView = commentView.findViewById(R.id.comment_content);
                                        ImageView edit_comment = commentView.findViewById(R.id.edit_comment);

                                        String userId = (String) comment.get("userId");
                                        String comment_profile_image = (String) comment.get("profile_image");
                                        String comment_fullname = (String) comment.get("user_fullname");
                                        Timestamp comment_date_added = (Timestamp) comment.get("date_added");
                                        String comment_content = (String) comment.get("comment");

                                        comment_fullname_textView.setText(comment_fullname);

                                        if (comment_profile_image != "") {
                                            Picasso.get().load(comment_profile_image).into(comment_profile_pic);
                                        }
                                        comment_date_textView.setText(new SimpleDateFormat("MMMM dd, yyyy hh:mm a").format(comment_date_added.toDate()));
                                        comment_comment_textView.setText(comment_content);

                                        String myDocumentId = sharedPrefs.getString("documentId", "");

                                        if (userId.equals(myDocumentId) || sharedPrefs.getString("user_type", "").equals("Admin")) {
                                            edit_comment.setVisibility(View.VISIBLE);
                                        } else {
                                            edit_comment.setVisibility(View.GONE);
                                        }

                                        edit_comment.setOnClickListener(v -> {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(CommunityWall.this);
                                            builder.setMessage("Are you sure you want to delete this comment?")
                                                .setPositiveButton("Yes", (dialog, id) -> {
                                                    // Code to delete the comment goes here

                                                    db.collection("posts").document(post_id).update("comments", FieldValue.arrayRemove(comment))
                                                        .addOnSuccessListener(aVoid -> {
                                                            Toast.makeText(CommunityWall.this, "Comment deleted", Toast.LENGTH_SHORT).show();
                                                            getPosts();
                                                        })
                                                        .addOnFailureListener(e -> {
                                                            Toast.makeText(CommunityWall.this, "Failed to delete comment", Toast.LENGTH_SHORT).show();
                                                        });

                                                })
                                                .setNegativeButton("No", (dialog, id) -> {
                                                    // User cancelled the dialog
                                                    dialog.dismiss();
                                                });
                                            AlertDialog alert = builder.create();
                                            alert.show();
                                        });

                                        comment_container.addView(commentView);
                                    }
                                }

                                post_username_textView.setText(user_fullname);
                                post_date_textView.setText(new SimpleDateFormat("MMMM dd, yyyy hh:mm a").format(date_added.toDate()));

                                linearLayout.addView(postView);
                            }

                            if (task.getResult().size() == 0) {
                                linearLayout.removeAllViews();
                                TextView textView = new TextView(CommunityWall.this);
                                textView.setPadding(30, 30, 30, 30);
                                textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                                textView.setTextSize(16);
                                textView.setText("No posts yet");
                                linearLayout.addView(textView);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}