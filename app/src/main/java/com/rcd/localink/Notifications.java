package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.squareup.picasso.Picasso;

public class Notifications extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView back_button = findViewById(R.id.back_button);

        LinearLayout notification_container = findViewById(R.id.notification_container);

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String documentId = sharedPrefs.getString("documentId", "");

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("notifications")
                .whereEqualTo("for", documentId)
                .orderBy("date_added", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        notification_container.removeAllViews();
                        for (DocumentSnapshot document : task.getResult()) {
                            View notification_item = getLayoutInflater().inflate(R.layout.notification_item, null);
                            ImageView image = notification_item.findViewById(R.id.image);
                            TextView new_indicator = notification_item.findViewById(R.id.new_indicator);
                            TextView notification_text = notification_item.findViewById(R.id.notification_content);
                            notification_text.setText(document.getString("notification_content"));
                            Picasso.get().load(document.getString("picture")).into(image);
                            if(document.getBoolean("isSeen")){
                                new_indicator.setVisibility(View.GONE);
                            } else {
                                new_indicator.setVisibility(View.VISIBLE);
                            }

                            notification_item.setOnClickListener(v -> {
                                db.collection("notifications").document(document.getId()).update("isSeen", true);

                                String type = document.getString("type");


                                if (type.equals("contract")) {
                                Intent intent = new Intent(Notifications.this, ContractPreview.class);
                                intent.putExtra("contractId", document.getString("extraIntent"));
                                startActivity(intent);
                                }
                            });


                            notification_container.addView(notification_item);
                        }
                    } else {
                        Toast.makeText(Notifications.this, "Error fetching notifications", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(Notifications.this, "Error fetching notifications", Toast.LENGTH_SHORT).show();
                    Log.e("Notifications", "Error fetching notifications", e);
                });

        back_button.setOnClickListener(view -> {
            finish();
        });
    }
}