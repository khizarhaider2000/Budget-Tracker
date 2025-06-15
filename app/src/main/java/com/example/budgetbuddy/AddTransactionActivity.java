package com.example.budgetbuddy;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private Spinner typeSpinner, categorySpinner;
    private EditText amountInput, noteInput;
    private TextView dateInput;
    private Button saveButton;
    private Calendar calendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        // Bind UI
        typeSpinner = findViewById(R.id.spinner_type);
        categorySpinner = findViewById(R.id.spinner_category);
        amountInput = findViewById(R.id.editText_amount);
        noteInput = findViewById(R.id.editText_note);
        dateInput = findViewById(R.id.textView_date);
        saveButton = findViewById(R.id.button_save);
        calendar = Calendar.getInstance();

        // Setup spinners
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeSpinner.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.transaction_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(categoryAdapter);

        // Date picker
        dateInput.setOnClickListener(v -> {
            new DatePickerDialog(AddTransactionActivity.this,
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                        dateInput.setText(sdf.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            ).show();
        });

        // Save to Firebase
        saveButton.setOnClickListener(v -> saveTransaction());
    }

    private void saveTransaction() {
        String type = typeSpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();
        String amountStr = amountInput.getText().toString().trim();
        String note = noteInput.getText().toString().trim();
        String date = dateInput.getText().toString().trim();

        if (amountStr.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Amount and date are required", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount = Double.parseDouble(amountStr);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference dbRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("transactions");

        String id = dbRef.push().getKey();

        Transaction txn = new Transaction(id, type, category, amount, date, note);

        dbRef.child(id).setValue(txn)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
