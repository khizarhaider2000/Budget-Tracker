package com.example.budgetbuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class DashboardActivity extends AppCompatActivity {

    private TextView welcomeText, balanceText, incomeExpenseSummary, recentTransactionsText;
    private ProgressBar spendingProgress;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        welcomeText = findViewById(R.id.welcomeText);
        balanceText = findViewById(R.id.balanceText);
        incomeExpenseSummary = findViewById(R.id.incomeExpenseSummary);
        recentTransactionsText = findViewById(R.id.recentTransactionsText);
        spendingProgress = findViewById(R.id.spendingProgress);
        mAuth = FirebaseAuth.getInstance();

        String uid = mAuth.getCurrentUser().getUid();

        FirebaseDatabase.getInstance().getReference("Users").child(uid).child("name")
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getValue(String.class);
                    welcomeText.setText("Welcome, " + name);
                });

        loadUserTransactions(uid);

        findViewById(R.id.fab_add).setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, AddTransactionActivity.class)));
    }

    private void loadUserTransactions(String uid) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("transactions");

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                double incomeTotal = 0;
                double expenseTotal = 0;
                double balance = 0;
                StringBuilder recent = new StringBuilder();

                for (DataSnapshot data : snapshot.getChildren()) {
                    Transaction txn = data.getValue(Transaction.class);
                    if (txn == null) continue;

                    // Recent Transactions
                    recent.append("- ")
                            .append((txn.note != null && !txn.note.isEmpty()) ? txn.note : txn.category)
                            .append(" $").append(txn.amount).append("\n");

                    if (txn.type.equalsIgnoreCase("Income")) {
                        incomeTotal += txn.amount;
                        balance += txn.amount;
                    } else {
                        expenseTotal += txn.amount;
                        balance -= txn.amount;
                    }
                }

                incomeExpenseSummary.setText("Income: $" + incomeTotal + "   •   Expenses: $" + expenseTotal);
                balanceText.setText("Balance: $" + String.format("%.2f", balance));
                recentTransactionsText.setText(recent.toString().trim());

                int progress = (int) (expenseTotal / (incomeTotal + 0.01) * 100); // avoid divide by zero
                spendingProgress.setProgress(progress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Dashboard", "Error loading transactions", error.toException());
            }
        });
    }
}
