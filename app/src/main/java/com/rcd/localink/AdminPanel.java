package com.rcd.localink;

import android.app.AlertDialog;
import android.app.Notification;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.Picasso;

public class AdminPanel extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_panel);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Initialize all views with @+id/ identifiers
        ImageView backButton = findViewById(R.id.back_button);

        ImageView profile_image = findViewById(R.id.profile_image);
        ImageView notification = findViewById(R.id.notification);


        notification.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, Notification.class);
            startActivity(intent);
        });

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String profilePicture = sharedPrefs.getString("profile_picture", "");
        Picasso.get().load(profilePicture).into(profile_image);

        profile_image.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, EditProfile.class);
            startActivity(intent);
        });





        backButton.setOnClickListener(view -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialo1g, id) -> {
                        SharedPreferences sharedPrefs2 = getSharedPreferences("userAuth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs2.edit();
                        editor.clear();
                        editor.apply();

                        Toast.makeText(AdminPanel.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AdminPanel.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        });


        Button users_masterlist = findViewById(R.id.users_masterlist);

        users_masterlist.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, UsersMasterlist.class);
            startActivity(intent);
        });

        Button messages = findViewById(R.id.messages);

        messages.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, Chats.class);
            startActivity(intent);
        });

        Button barangay_management = findViewById(R.id.barangay_management);

        barangay_management.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, Barangay.class);
            startActivity(intent);
        });

        Button community_wall = findViewById(R.id.community_wall);

        community_wall.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, CommunityWall.class);
            startActivity(intent);
        });

        Button transaction_monitoring = findViewById(R.id.transaction_monitoring);

        transaction_monitoring.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, TransactionMonitoring.class);
            startActivity(intent);
        });

        Button pinned_announcement_edit = findViewById(R.id.pinned_announcement_edit);

        pinned_announcement_edit.setOnClickListener(view -> {
            Intent intent = new Intent(AdminPanel.this, EditPinnedAnnouncement.class);
            startActivity(intent);
        });
    }
}