package com.rcd.localink;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.Arrays;
import java.util.HashMap;

public class ChatPage extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Intent intent = getIntent();
        String person = intent.getStringExtra("person");
        ImageView sendButton = findViewById(R.id.send_button);
        ImageView backButton = findViewById(R.id.back_button);
        EditText message = findViewById(R.id.chatbox);
        LinearLayout chat_container = findViewById(R.id.chat_container);

        backButton.setOnClickListener(v -> {
            finish();
        });

        SharedPreferences sharedPrefs = getSharedPreferences("userAuth", MODE_PRIVATE);
        String documentId = sharedPrefs.getString("documentId", "");

        String[] ids = {person, documentId};

        Arrays.sort(ids);

        String chatId = ids[0] + "-" + ids[1];

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(person)
            .get()
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String firstName = document.getString("firstName");
                    TextView chat_name = findViewById(R.id.chat_name);
                    chat_name.setText(firstName);
                }
            });

        db.collection("chats").document(chatId).collection("chats")
            .orderBy("date_added", Query.Direction.ASCENDING)
            .addSnapshotListener((value, error) -> {
                chat_container.removeAllViews();
                if (error != null) {
                    // Handle the error
                    return;
                }
                if (value != null) {
                    for (QueryDocumentSnapshot document : value) {
                        // Process each document

                        if (document.getString("sender").equals(documentId)) {
                            Log.d("ChatPage", "document sender is equal to documentId");
                                View chat_message = getLayoutInflater().inflate(R.layout.chat_item_me, null);
                                TextView messageText = chat_message.findViewById(R.id.message_content);
                                messageText.setText(document.getString("message"));

                                chat_container.addView(chat_message);
                        } else {
                            Log.d("ChatPage", "document sender is NOTTT equal to documentId");
                            View chat_message = getLayoutInflater().inflate(R.layout.chat_item_sender, null );
                            TextView messageText = chat_message.findViewById(R.id.message_content);
                            messageText.setText(document.getString("message"));

                            chat_container.addView(chat_message);
                        }


                    }
                }
            });

        sendButton.setOnClickListener(v -> {
            String messageText = message.getText().toString();
            if (!messageText.isEmpty()) {
                db.collection("chats").document(chatId).collection("chats")
                    .add(new HashMap<String, Object>() {{
                        put("sender", documentId);
                        put("message", messageText);
                        put("type", "message");
                        put("date_added", System.currentTimeMillis());
                    }});
                message.setText("");
            }
        });
    }
}