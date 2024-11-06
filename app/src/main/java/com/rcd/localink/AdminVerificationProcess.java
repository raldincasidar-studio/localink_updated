package com.rcd.localink;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class AdminVerificationProcess extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_verification_process);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button chat = findViewById(R.id.chat);
        chat.setOnClickListener(v -> {
            Intent intent = new Intent(AdminVerificationProcess.this, ChatPage.class);
            intent.putExtra("person", "admin");
            startActivity(intent);
        });

        SharedPreferences userPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String verificationStatus = userPrefs.getString("verificationStatus", "");
        String user_type = userPrefs.getString("user_type", "");

        if (verificationStatus.equals("Verified") || user_type.equals("Admin")) {

            Intent intent = new Intent(AdminVerificationProcess.this, Dashboard.class);
            startActivity(intent);

            finish();
        }


        Button logout = findViewById(R.id.logout);
        logout.setOnClickListener(v -> {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Are you sure you want to log out?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> {
                        SharedPreferences sharedPrefs2 = getSharedPreferences("userAuth", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPrefs2.edit();
                        editor.clear();
                        editor.apply();

                        Toast.makeText(AdminVerificationProcess.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(AdminVerificationProcess.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("No", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();


        });
    }
}