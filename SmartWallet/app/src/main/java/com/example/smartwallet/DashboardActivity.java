package com.example.smartwallet;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartwallet.models.Transaction;
import com.example.smartwallet.models.Vault;
import com.example.smartwallet.services.AIService;
import com.example.smartwallet.services.BankAPIService;
import com.example.smartwallet.services.FirebaseService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private TextView tvTotalBalance, tvAiInsight, tvAiStatus;
    private RecyclerView rvVaults;
    private VaultAdapter vaultAdapter;
    private List<Vault> vaultList = new ArrayList<>();
    private AIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        aiService = new AIService(this);

        initializeViews();
        setupRecyclerView();
        loadUserData();
        setupClickListeners();

        // Automatically fetch and process transactions on app start
        autoFetchAndProcessTransactions();
    }

    private void initializeViews() {
        tvTotalBalance = findViewById(R.id.tvTotalBalance);
        tvAiInsight = findViewById(R.id.tvAiInsight);
        tvAiStatus = findViewById(R.id.tvAiStatus);
        rvVaults = findViewById(R.id.rvVaults);

        // Set initial AI status
        tvAiStatus.setText("🤖 AI is analyzing your financial patterns...");
    }

    private void setupRecyclerView() {
        vaultAdapter = new VaultAdapter(vaultList);
        rvVaults.setLayoutManager(new LinearLayoutManager(this));
        rvVaults.setAdapter(vaultAdapter);
    }

    private void loadUserData() {
        String userId = mAuth.getCurrentUser().getUid();

        // Load total balance
        db.collection("users").document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Double balance = documentSnapshot.getDouble("totalBalance");
                        if (balance != null) {
                            tvTotalBalance.setText(String.format("₹%,.0f", balance));
                        }
                    }
                });

        // Load vaults
        db.collection("users").document(userId).collection("vaults")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    vaultList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Vault vault = doc.toObject(Vault.class);
                        if (vault != null) {
                            vaultList.add(vault);
                        }
                    }
                    vaultAdapter.notifyDataSetChanged();
                });

        // Load AI insights
        db.collection("users").document(userId).collection("ai_insights")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot insightDoc = queryDocumentSnapshots.getDocuments().get(0);
                        String insight = insightDoc.getString("message");
                        if (insight != null) {
                            tvAiInsight.setText(insight);
                        }
                    }
                });
    }

    private void setupClickListeners() {
        Button btnAutoPay = findViewById(R.id.btnAutoPay);
        Button btnFetchTransactions = findViewById(R.id.btnFetchTransactions);

        btnAutoPay.setOnClickListener(v -> triggerAutoPayment());
        btnFetchTransactions.setOnClickListener(v -> fetchLatestTransactions());
    }

    private void autoFetchAndProcessTransactions() {
        tvAiStatus.setText("🔄 Auto-fetching transactions...");

        // Simulate automatic transaction fetch and AI processing
        BankAPIService.fetchTransactions(new BankAPIService.TransactionCallback() {
            @Override
            public void onSuccess(List<Transaction> transactions) {
                tvAiStatus.setText("🤖 AI is categorizing transactions...");

                // Process transactions with AI
                aiService.processTransactionsWithAI(transactions, new AIService.AICallback() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        tvAiStatus.setText("✅ Transactions processed by AI!");
                        tvAiInsight.setText((String) result.get("insight"));

                        // Update vaults with AI allocations
                        updateVaultsWithAIAllocations(result);

                        // Check for auto-payments
                        checkForAutoPayments();
                    }

                    @Override
                    public void onError(String error) {
                        tvAiStatus.setText("⚠️ AI processing failed");
                    }
                });
            }

            @Override
            public void onError(String error) {
                tvAiStatus.setText("⚠️ Fetch failed: " + error);
            }
        });
    }

    private void fetchLatestTransactions() {
        autoFetchAndProcessTransactions();
    }

    private void triggerAutoPayment() {
        // Create UPI Intent for Google Pay
        String upiId = "merchant@upi"; // Replace with actual UPI ID
        String name = "Smart Merchant";
        String amount = "1500";
        String note = "AI-validated payment";

        Uri uri = Uri.parse("upi://pay?pa=" + upiId +
                "&pn=" + name +
                "&am=" + amount +
                "&tn=" + note +
                "&cu=INR");

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(uri);
        intent.setPackage("com.google.android.apps.nbu.paisa.user"); // GPay package

        // Check if GPay is installed
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);

            // Log the payment in Firestore
            FirebaseService.logPayment(upiId, Double.parseDouble(amount), note, "auto");
        } else {
            // GPay not installed, use generic UPI app
            intent.setPackage(null);
            startActivity(intent);
        }
    }

    private void updateVaultsWithAIAllocations(Map<String, Object> result) {
        // Update vault balances based on AI allocation
        Map<String, Double> allocations = (Map<String, Double>) result.get("allocations");

        for (Map.Entry<String, Double> entry : allocations.entrySet()) {
            FirebaseService.updateVaultBalance(entry.getKey(), entry.getValue());
        }

        // Refresh vault display
        loadUserData();
    }

    private void checkForAutoPayments() {
        // Check if any scheduled payments are due
        aiService.checkScheduledPayments(new AIService.PaymentCallback() {
            @Override
            public void onPaymentDue(String payee, double amount, String description) {
                // Show notification or auto-trigger payment
                showPaymentNotification(payee, amount, description);
            }

            @Override
            public void onNoPaymentsDue() {
                // No payments due
            }
        });
    }

    private void showPaymentNotification(String payee, double amount, String description) {
        // Implementation for showing payment notification
        // This could trigger auto-payment or show a notification to user
    }
}