package com.example.smartwallet.services;

import com.example.smartwallet.models.Transaction;
import java.util.*;

public class BankAPIService {

    public interface TransactionCallback {
        void onSuccess(List<Transaction> transactions);
        void onError(String error);
    }

    public interface BalanceCallback {
        void onSuccess(double balance);
        void onError(String error);
    }

    public static void fetchTransactions(TransactionCallback callback) {
        // Simulate bank API call
        // In production, integrate with actual bank APIs or use sandbox

        List<Transaction> transactions = new ArrayList<>();

        // Sample transactions (simulated data)
        transactions.add(new Transaction(
                "1",
                "Rent Payment",
                15000.0,
                "Landlord",
                new Date(),
                "debit"
        ));

        transactions.add(new Transaction(
                "2",
                "Grocery Shopping",
                2500.0,
                "Supermarket",
                new Date(),
                "debit"
        ));

        transactions.add(new Transaction(
                "3",
                "Salary Credit",
                75000.0,
                "Company",
                new Date(),
                "credit"
        ));

        transactions.add(new Transaction(
                "4",
                "Movie Tickets",
                800.0,
                "Cinema",
                new Date(),
                "debit"
        ));

        transactions.add(new Transaction(
                "5",
                "Electricity Bill",
                1200.0,
                "Utility",
                new Date(),
                "debit"
        ));

        // Simulate API delay
        new android.os.Handler().postDelayed(() -> {
            callback.onSuccess(transactions);
        }, 1500);
    }

    public static void fetchAccountBalance(BalanceCallback callback) {
        // Simulate balance fetch
        new android.os.Handler().postDelayed(() -> {
            callback.onSuccess(85420.0); // Sample balance
        }, 1000);
    }
}