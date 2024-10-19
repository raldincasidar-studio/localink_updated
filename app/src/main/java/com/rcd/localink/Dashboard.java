package com.rcd.localink;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Dashboard extends AppCompatActivity {

    private  ImageView back_button;
    private  ImageView profile_image;

    private ImageView announcement_image;

    private TextView announcement_title;
    private TextView announcement_description;
    private TextView announcement_date;

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
                Uri imageUri = Uri.parse(document.getString("image"));

                announcement_title.setText(title);
                announcement_description.setText(description);
                announcement_date.setText(date + " - " + time);

                FirebaseStorage storage = FirebaseStorage.getInstance();
                StorageReference storageRef = storage.getReferenceFromUrl(imageUri.toString());
                storageRef.getBytes(10240 * 10240).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        announcement_image.setImageBitmap(bitmap);
                    }
                });
            } else {
                Log.d(TAG, "Error getting documents: ", task.getException());
            }
        });

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String profilePicture = sharedPrefs.getString("profile_picture", "");
        if (!profilePicture.isEmpty()) {
            Log.d(TAG, "Profile picure link: " + profilePicture);
            StorageReference storageRef = FirebaseStorage.getInstance().getReferenceFromUrl(profilePicture);
            storageRef.getBytes(10240 * 10240).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                @Override
                public void onSuccess(byte[] bytes) {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    profile_image.setImageBitmap(bitmap);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Handle failure
                }
            });
        }

        back_button.setOnClickListener(v -> {

            SharedPreferences sharedPrefs2 = getSharedPreferences("userAuth", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPrefs2.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(Dashboard.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            finish();
        });
    }
}