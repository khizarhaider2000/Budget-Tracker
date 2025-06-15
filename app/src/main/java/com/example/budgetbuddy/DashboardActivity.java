package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeText, balanceText;
    private DatabaseReference userRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        welcomeText = findViewById(R.id.welcomeText);
        balanceText = findViewById(R.id.balanceText);
        mAuth = FirebaseAuth.getInstance();

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference("Users").child(uid).child("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getValue(String.class);
                    welcomeText.setText("Welcome, " + name);
                });


        // Calculate balance
        FirebaseDatabase.getInstance().getReference("Users")
                .child(uid).child("transactions")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        double balance = 0;
                        for (DataSnapshot txn : snapshot.getChildren()) {
                            String type = txn.child("type").getValue(String.class);
                            double amount = txn.child("amount").getValue(Double.class);
                            balance += type.equals("Income") ? amount : -amount;
                        }
                        balanceText.setText("Balance: $" + String.format("%.2f", balance));
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {}
                });

        findViewById(R.id.fab_add).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, AddTransactionActivity.class)));
    }
}
