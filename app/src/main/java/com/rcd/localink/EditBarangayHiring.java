package com.rcd.localink;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class EditBarangayHiring extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_barangay_hiring);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        EditText contract_name = findViewById(R.id.contract_name);
        EditText starts_in = findViewById(R.id.starts_in);
        EditText ends_in = findViewById(R.id.ends_in);
        EditText Time = findViewById(R.id.time);
        EditText instruction = findViewById(R.id.instruction);
        CheckBox activate_accept_volunteer_works = findViewById(R.id.activate_accept_volunteer_works);
        Button publish = findViewById(R.id.publish);

        String barangay_hiring_id = getIntent().getStringExtra("hiring_id");
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("barangay_hiring").document(barangay_hiring_id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document != null) {
                            contract_name.setText(document.getString("contractName"));
                            starts_in.setText(document.getString("startsIn"));
                            ends_in.setText(document.getString("endsIn"));
                            Time.setText(document.getString("time"));
                            instruction.setText(document.getString("instruction"));
                            activate_accept_volunteer_works.setChecked(document.getBoolean("activateAcceptVolunteerWorks"));
                        }
                    }
                });

        publish.setOnClickListener(v -> {
            HashMap<String, Object> data = new HashMap<>();
            data.put("contractName", contract_name.getText().toString());
            data.put("startsIn", starts_in.getText().toString());
            data.put("endsIn", ends_in.getText().toString());
            data.put("time", Time.getText().toString());
            data.put("instruction", instruction.getText().toString());
            data.put("activateAcceptVolunteerWorks", activate_accept_volunteer_works.isChecked());
            db.collection("barangay_hiring").document(barangay_hiring_id).update(data);


            db.collection("barangay_hiring_contracts")
                .whereEqualTo("hiringId", barangay_hiring_id)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            db.collection("barangay_hiring_contracts").document(document.getId()).delete();
                        }
                    }
                });

            Toast.makeText(EditBarangayHiring.this, "Barangay hiring updated successfully", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(EditBarangayHiring.this, BarangayHiring.class);
            startActivity(intent);
        });

    }
}