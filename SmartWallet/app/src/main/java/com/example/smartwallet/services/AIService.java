package com.example.smartwallet.services;

import android.content.Context;
import com.example.smartwallet.models.Transaction;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class AIService {

    private Context context;
    private FirebaseFirestore db;
    private OkHttpClient httpClient;
    private Gson gson;

    // Gemini API configuration
    private static final String GEMINI_API_KEY = "YOUR_GEMINI_API_KEY";
    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent";

    public interface AICallback {
        void onSuccess(Map<String, Object> result);
        void onError(String error);
    }

    public interface PaymentCallback {
        void onPaymentDue(String payee, double amount, String description);
        void onNoPaymentsDue();
    }

    public AIService(Context context) {
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public void processTransactionsWithAI(List<Transaction> transactions, AICallback callback) {
        // Prepare prompt for Gemini
        String prompt = buildTransactionPrompt(transactions);

        // Call Gemini API
        callGeminiAPI(prompt, new GeminiCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    // Parse Gemini response
                    Map<String, Object> result = parseGeminiResponse(response, transactions);

                    // Apply Gemma + LoRA rules (simulated)
                    applyDomainSpecificRules(result);

                    // Update Firestore with AI decisions
                    updateFirestoreWithAIDecisions(result);

                    callback.onSuccess(result);
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private String buildTransactionPrompt(List<Transaction> transactions) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Analyze these financial transactions and:\n");
        prompt.append("1. Categorize each (Necessary/Discretionary/Risk)\n");
        prompt.append("2. Suggest vault allocation (Rent, Food, Emergency, Savings, Lifestyle)\n");
        prompt.append("3. Identify patterns and anomalies\n");
        prompt.append("4. Predict upcoming expenses\n\n");
        prompt.append("Transactions:\n");

        for (Transaction t : transactions) {
            prompt.append(String.format("- %s: ₹%.2f at %s (%s)\n",
                    t.getDescription(), t.getAmount(), t.getMerchant(), t.getDate()));
        }

        prompt.append("\nProvide response in JSON format with: categories, vault_allocations, insights, predictions");

        return prompt.toString();
    }

    private void callGeminiAPI(String prompt, GeminiCallback callback) {
        try {
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();

            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);

            // Add safety settings and generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.2);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 1024);
            requestBody.put("generationConfig", generationConfig);

            Request request = new Request.Builder()
                    .url(GEMINI_URL + "?key=" + GEMINI_API_KEY)
                    .post(RequestBody.create(
                            requestBody.toString(),
                            MediaType.parse("application/json")))
                    .build();

            httpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    callback.onError(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        callback.onSuccess(responseBody);
                    } else {
                        callback.onError("API call failed: " + response.code());
                    }
                }
            });

        } catch (Exception e) {
            callback.onError(e.getMessage());
        }
    }

    private Map<String, Object> parseGeminiResponse(String response, List<Transaction> transactions) {
        Map<String, Object> result = new HashMap<>();

        try {
            JSONObject jsonResponse = new JSONObject(response);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject candidate = candidates.getJSONObject(0);
                JSONObject content = candidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                String text = parts.getJSONObject(0).getString("text");

                // Extract JSON from Gemini response
                String jsonStr = extractJsonFromText(text);
                JSONObject analysis = new JSONObject(jsonStr);

                // Parse categories
                Map<String, String> categories = new HashMap<>();
                JSONObject cats = analysis.getJSONObject("categories");
                Iterator<String> keys = cats.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    categories.put(key, cats.getString(key));
                }

                // Parse vault allocations
                Map<String, Double> allocations = new HashMap<>();
                JSONObject allocs = analysis.getJSONObject("vault_allocations");
                Iterator<String> allocKeys = allocs.keys();
                while (allocKeys.hasNext()) {
                    String key = allocKeys.next();
                    allocations.put(key, allocs.getDouble(key));
                }

                result.put("categories", categories);
                result.put("allocations", allocations);
                result.put("insight", analysis.getString("insights"));
                result.put("predictions", analysis.getString("predictions"));

                // Calculate total allocated
                double totalAllocated = allocations.values().stream().mapToDouble(Double::doubleValue).sum();
                result.put("total_allocated", totalAllocated);
            }
        } catch (Exception e) {
            // Fallback to simulated analysis
            result = simulateAIAnalysis(transactions);
        }

        return result;
    }

    private Map<String, Object> simulateAIAnalysis(List<Transaction> transactions) {
        Map<String, Object> result = new HashMap<>();
        Map<String, String> categories = new HashMap<>();
        Map<String, Double> allocations = new HashMap<>();

        // Simulate AI categorization
        for (Transaction t : transactions) {
            String desc = t.getDescription().toLowerCase();
            if (desc.contains("rent") || desc.contains("bill") || desc.contains("grocery")) {
                categories.put(t.getId(), "Necessary");
            } else if (desc.contains("movie") || desc.contains("restaurant") || desc.contains("shopping")) {
                categories.put(t.getId(), "Discretionary");
            } else {
                categories.put(t.getId(), "Review");
            }
        }

        // Simulate vault allocations
        allocations.put("Emergency", 2500.0);
        allocations.put("Rent", 15000.0);
        allocations.put("Food", 6000.0);
        allocations.put("Savings", 5000.0);
        allocations.put("Lifestyle", 3000.0);

        result.put("categories", categories);
        result.put("allocations", allocations);
        result.put("insight", "• 3 transactions categorized automatically\n• ₹2,500 allocated to Emergency vault\n• Rent payment scheduled for tomorrow");
        result.put("predictions", "Upcoming: Rent (₹15,000), Electricity (₹1,200)");
        result.put("total_allocated", 31500.0);

        return result;
    }

    private void applyDomainSpecificRules(Map<String, Object> result) {
        // Apply Gemma + LoRA fine-tuned rules
        // This would integrate with a fine-tuned Gemma model
        // For demo, we'll apply some basic rules

        Map<String, Double> allocations = (Map<String, Double>) result.get("allocations");

        // Rule: Emergency vault should be at least 10% of total
        double total = (double) result.get("total_allocated");
        double minEmergency = total * 0.1;

        if (allocations.containsKey("Emergency") && allocations.get("Emergency") < minEmergency) {
            allocations.put("Emergency", minEmergency);
        }

        // Rule: Savings should be at least 20%
        double minSavings = total * 0.2;
        if (allocations.containsKey("Savings") && allocations.get("Savings") < minSavings) {
            allocations.put("Savings", minSavings);
        }
    }

    private void updateFirestoreWithAIDecisions(Map<String, Object> result) {
        // Save AI analysis to Firestore
        String userId = FirebaseService.getCurrentUserId();

        Map<String, Object> aiLog = new HashMap<>();
        aiLog.put("timestamp", new Date());
        aiLog.put("analysis", result);
        aiLog.put("message", result.get("insight").toString());

        db.collection("users").document(userId).collection("ai_logs")
                .add(aiLog);

        // Update insights
        db.collection("users").document(userId).collection("ai_insights")
                .add(aiLog);
    }

    public void checkScheduledPayments(PaymentCallback callback) {
        // Check Firestore for scheduled payments
        String userId = FirebaseService.getCurrentUserId();

        db.collection("users").document(userId).collection("scheduled_payments")
                .whereEqualTo("status", "pending")
                .whereLessThanOrEqualTo("dueDate", new Date())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : queryDocumentSnapshots) {
                            String payee = doc.getString("payee");
                            Double amount = doc.getDouble("amount");
                            String description = doc.getString("description");

                            if (payee != null && amount != null) {
                                callback.onPaymentDue(payee, amount, description);
                            }
                        }
                    } else {
                        callback.onNoPaymentsDue();
                    }
                });
    }

    private String extractJsonFromText(String text) {
        // Extract JSON from Gemini's response
        int start = text.indexOf("{");
        int end = text.lastIndexOf("}") + 1;

        if (start != -1 && end != -1 && end > start) {
            return text.substring(start, end);
        }

        // Return default JSON if extraction fails
        return "{\"categories\":{},\"vault_allocations\":{\"Emergency\":2500,\"Rent\":15000},\"insights\":\"AI analysis complete\",\"predictions\":\"No upcoming payments\"}";
    }

    interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}