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
import android.widget.LinearLayout;
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
import com.squareup.picasso.Picasso;

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

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String profilePicture = sharedPrefs.getString("profile_picture", "");
        Picasso.get().load(profilePicture).into(profile_image);

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