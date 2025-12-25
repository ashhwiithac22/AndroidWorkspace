package com.example.smartwallet.services;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.*;

public class FirebaseService {

    private static FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static FirebaseAuth auth = FirebaseAuth.getInstance();

    public static void initializeUser(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", user.getEmail());
        userData.put("name", user.getDisplayName());
        userData.put("totalBalance", 0.0);
        userData.put("createdAt", new Date());

        db.collection("users").document(user.getUid())
                .set(userData);

        // Initialize default vaults
        initializeDefaultVaults(user.getUid());
    }

    private static void initializeDefaultVaults(String userId) {
        String[] vaultNames = {"Emergency", "Rent", "Food", "Savings", "Lifestyle", "Investments"};

        for (String name : vaultNames) {
            Map<String, Object> vault = new HashMap<>();
            vault.put("name", name);
            vault.put("balance", 0.0);
            vault.put("target", 0.0);
            vault.put("createdAt", new Date());

            db.collection("users").document(userId)
                    .collection("vaults").document(name)
                    .set(vault);
        }
    }

    public static void updateVaultBalance(String vaultName, double amount) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        db.collection("users").document(userId)
                .collection("vaults").document(vaultName)
                .update("balance", amount);
    }

    public static void logPayment(String payee, double amount, String note, String type) {
        String userId = getCurrentUserId();
        if (userId == null) return;

        Map<String, Object> payment = new HashMap<>();
        payment.put("payee", payee);
        payment.put("amount", amount);
        payment.put("note", note);
        payment.put("type", type);
        payment.put("timestamp", new Date());
        payment.put("status", "completed");

        db.collection("users").document(userId)
                .collection("payments")
                .add(payment);
    }

    public static String getCurrentUserId() {
        FirebaseUser user = auth.getCurrentUser();
        return user != null ? user.getUid() : null;
    }
}