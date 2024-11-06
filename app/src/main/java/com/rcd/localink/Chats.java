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
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

public class Chats extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        LinearLayout chat_container = findViewById(R.id.chat_container);

        ImageView backButton = findViewById(R.id.back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("chats")
            .orderBy("date_added", Query.Direction.DESCENDING)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    chat_container.removeAllViews();
                    Toast.makeText(Chats.this, "Number of chats: " + task.getResult().size(), Toast.LENGTH_SHORT).show();
                    for (DocumentSnapshot document : task.getResult()) {
                        // Process each chat document here
                        // For example, you can log the document data
                        Log.d("Chats", document.getId() + " => " + document.getData());

                        Toast.makeText(Chats.this, document.getId(), Toast.LENGTH_SHORT).show();

                        String documentId = document.getId();
                        // Splice the document id with separator "-"
                        String[] idParts = documentId.split("-");
                        for (String part : idParts) {
                            Log.d("Chats", "ID Part: " + part);
                        }

                        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
                        String myDocumentId = sharedPrefs.getString("documentId", "");
                        if (Arrays.asList(idParts).contains(myDocumentId)) {
                            // This chat document contains my documentId
                            Log.d("Chats", "This chat document contains my documentId");

                            String chatWithDocumentId = "";
                            for (String part : idParts) {
                                if (!part.equals(myDocumentId)) {
                                    chatWithDocumentId = part;

                            db.collection("users")
                                .document(chatWithDocumentId)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        DocumentSnapshot userDocument = task2.getResult();
                                        // Process each chat document here
                                        // For example, you can log the document data
                                        Log.d("Chats", userDocument.getData().toString());

                                        String firstName = userDocument.getString("firstName");
                                        String lastName = userDocument.getString("lastName");
                                        String middleName = userDocument.getString("middleName");
                                        String profilePicture = userDocument.getString("profile_picture");

                                        View chatRow = getLayoutInflater().inflate(R.layout.review_item, null);
                                        TextView chatName = chatRow.findViewById(R.id.name);

                                        TextView chatContent = chatRow.findViewById(R.id.content);
                                        ImageView chatImage = chatRow.findViewById(R.id.image);

                                        String fullName = firstName + " " + middleName + " " + lastName;

                                        chatContent.setText(document.getString("last_message"));
                                        chatName.setText(fullName);
                                        Picasso.get().load(profilePicture).into(chatImage);

                                        String finalChatId = userDocument.getId();

                                        chatRow.setOnClickListener(v -> {
                                            Intent intent = new Intent(Chats.this, ChatPage.class);
                                            intent.putExtra("person", finalChatId);
                                            startActivity(intent);
                                        });

                                        chat_container.addView(chatRow);
                                    } else {
                                        Log.w("Chats", "Error getting user documents.", task2.getException());
                                    }
                                });
                                }
                            }

                        }

                    }
                } else {
                    Log.w("Chats", "Error getting documents.", task.getException());
                }
            });
    }
}