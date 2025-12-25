const functions = require('firebase-functions');
const admin = require('firebase-admin');
const {VertexAI} = require('@google-cloud/vertexai');

admin.initializeApp();

// Initialize Vertex AI
const vertexAi = new VertexAI({
  project: 'YOUR_PROJECT_ID',
  location: 'us-central1'
});
const generativeModel = vertexAi.getGenerativeModel({
  model: 'gemini-1.0-pro'
});

// Cloud Function: Auto-process transactions
exports.processTransactions = functions.firestore
  .document('users/{userId}/transactions/{transactionId}')
  .onCreate(async (snap, context) => {
    const transaction = snap.data();
    const userId = context.params.userId;

    // Get user's vaults
    const vaultsSnapshot = await admin.firestore()
      .collection('users').doc(userId)
      .collection('vaults').get();

    const vaults = {};
    vaultsSnapshot.forEach(doc => {
      vaults[doc.id] = doc.data();
    });

    // Call Gemini API for categorization
    const prompt = `Categorize this transaction: ${transaction.description}, Amount: ${transaction.amount}, Merchant: ${transaction.merchant}.
    Available vaults: ${Object.keys(vaults).join(', ')}.
    Return JSON with: category, recommended_vault, confidence_score, ai_reasoning`;

    const result = await generativeModel.generateContent(prompt);
    const aiAnalysis = JSON.parse(result.response.candidates[0].content.parts[0].text);

    // Apply Gemma + LoRA rules
    const finalDecision = await applyGemmaLORARules(aiAnalysis, transaction, vaults);

    // Update vault balance
    await admin.firestore()
      .collection('users').doc(userId)
      .collection('vaults').doc(finalDecision.vault)
      .update({
        balance: admin.firestore.FieldValue.increment(transaction.amount)
      });

    // Log AI decision
    await admin.firestore()
      .collection('users').doc(userId)
      .collection('ai_decisions')
      .add({
        transactionId: snap.id,
        decision: finalDecision,
        timestamp: admin.firestore.FieldValue.serverTimestamp()
      });

    // Check if auto-payment should be triggered
    if (finalDecision.autoPay) {
      await triggerGPayPayment(userId, transaction, finalDecision);
    }

    return null;
  });

// Cloud Function: Predictive analytics
exports.predictiveAnalytics = functions.pubsub
  .schedule('every 24 hours')
  .onRun(async (context) => {
    const usersSnapshot = await admin.firestore().collection('users').get();

    for (const userDoc of usersSnapshot.docs) {
      const userId = userDoc.id;

      // Get last 30 days transactions
      const thirtyDaysAgo = new Date();
      thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

      const transactionsSnapshot = await admin.firestore()
        .collection('users').doc(userId)
        .collection('transactions')
        .where('date', '>=', thirtyDaysAgo)
        .get();

      const transactions = transactionsSnapshot.docs.map(doc => doc.data());

      // Call Vertex AI for predictions
      const prompt = `Analyze these transactions and predict next month's expenses: ${JSON.stringify(transactions)}`;

      const result = await generativeModel.generateContent(prompt);
      const predictions = JSON.parse(result.response.candidates[0].content.parts[0].text);

      // Save predictions
      await admin.firestore()
        .collection('users').doc(userId)
        .collection('predictions')
        .add({
          predictions: predictions,
          generatedAt: admin.firestore.FieldValue.serverTimestamp()
        });

      // Send FCM notification
      await sendFCMPrediction(userId, predictions);
    }

    return null;
  });

// Helper function: Apply Gemma + LoRA rules
async function applyGemmaLORARules(aiAnalysis, transaction, vaults) {
  // This would integrate with a fine-tuned Gemma model
  // For demo, we'll use simple rules

  const decision = {
    category: aiAnalysis.category,
    vault: aiAnalysis.recommended_vault,
    confidence: aiAnalysis.confidence_score,
    reasoning: aiAnalysis.ai_reasoning,
    autoPay: false
  };

  // Rule: Auto-pay for recurring bills with high confidence
  if (aiAnalysis.confidence_score > 0.8 &&
      ['rent', 'utility', 'subscription'].includes(aiAnalysis.category)) {
    decision.autoPay = true;
  }

  // Rule: Check vault limits
  const vault = vaults[decision.vault];
  if (vault && vault.balance + transaction.amount > vault.limit) {
    // Find alternative vault
    decision.vault = 'Emergency'; // Default to emergency
  }

  return decision;
}

// Helper function: Trigger GPay payment
async function triggerGPayPayment(userId, transaction, decision) {
  // Create payment record
  await admin.firestore()
    .collection('users').doc(userId)
    .collection('scheduled_payments')
    .add({
      payee: transaction.merchant,
      amount: transaction.amount,
      description: `AI-approved: ${transaction.description}`,
      status: 'pending',
      dueDate: new Date(Date.now() + 24 * 60 * 60 * 1000), // Tomorrow
      vault: decision.vault
    });

  // Send FCM notification
  const userDoc = await admin.firestore().collection('users').doc(userId).get();
  const fcmToken = userDoc.data().fcmToken;

  if (fcmToken) {
    const message = {
      notification: {
        title: '💰 Auto-payment Scheduled',
        body: `₹${transaction.amount} to ${transaction.merchant} will be paid tomorrow`
      },
      token: fcmToken,
      data: {
        type: 'auto_payment',
        transactionId: transaction.id,
        amount: transaction.amount.toString()
      }
    };

    await admin.messaging().send(message);
  }
}

// Helper function: Send FCM prediction
async function sendFCMPrediction(userId, predictions) {
  const userDoc = await admin.firestore().collection('users').doc(userId).get();
  const fcmToken = userDoc.data().fcmToken;

  if (fcmToken && predictions.summary) {
    const message = {
      notification: {
        title: '📊 AI Financial Insights',
        body: predictions.summary.substring(0, 100) + '...'
      },
      token: fcmToken,
      data: {
        type: 'monthly_insights'
      }
    };

    await admin.messaging().send(message);
  }
}