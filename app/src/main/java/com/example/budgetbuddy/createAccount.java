package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class createAccount extends AppCompatActivity {

    private EditText nameInput, emailInput, passwordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        mAuth = FirebaseAuth.getInstance();

        nameInput = findViewById(R.id.createAccountname);     // Name
        emailInput = findViewById(R.id.createAccountEmail);   // Email
        passwordInput = findViewById(R.id.createAccountPassword); // Make sure ID is updated

        findViewById(R.id.create).setOnClickListener(v -> createAccount());
        findViewById(R.id.createAccountback).setOnClickListener(v ->
                startActivity(new Intent(createAccount.this, MainActivity.class)));
    }

    private void createAccount() {
        String name = nameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        assert user != null;

                        // Save additional user info in Firebase Realtime Database
                        FirebaseDatabase.getInstance().getReference("Users")
                                .child(user.getUid())
                                .setValue(new User(name, email));

                        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Failed to create account: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public static class User {
        public String name, email;

        public User() {}  // Required for Firebase

        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }
    }
}
