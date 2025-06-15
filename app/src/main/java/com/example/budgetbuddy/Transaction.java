package com.example.budgetbuddy;

public class Transaction {
    public String id;
    public String type;      // "Income" or "Expense"
    public String category;
    public double amount;
    public String date;
    public String note;

    public Transaction() {}  // Required for Firebase

    public Transaction(String id, String type, String category, double amount, String date, String note) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.note = note;
    }
}
