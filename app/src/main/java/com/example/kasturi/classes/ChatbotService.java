package com.example.kasturi.classes;

import android.content.Context;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;

public class ChatbotService {
    private final GenerativeModelFutures model;
    private final Executor executor;

    public ChatbotService(Context context, Executor executor) {
        this.executor = executor;

        // Initialize with correct model name and API key
        GenerativeModel gm = new GenerativeModel(
                // Use the correct model name for your API version
                "gemini-1.5-pro-latest",  // Updated model name
                "AIzaSyATTuroisUkfuN8hZzKH6F5BI92E9A9hKs" // Your API key
        );
        this.model = GenerativeModelFutures.from(gm);
    }

    public void generateContent(String userMessage, ChatResponseCallback callback) {
        try {
            // Create content with safety instructions context
            Content content = new Content.Builder()
                    .addText("You are a women's safety assistant. " +
                            "Provide helpful safety advice and emergency protocols. " +
                            "Keep responses concise and practical. " +
                            "User question: " + userMessage)
                    .build();

            ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

            Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
                @Override
                public void onSuccess(GenerateContentResponse result) {
                    String responseText = result.getText();
                    if (responseText != null && !responseText.isEmpty()) {
                        callback.onResponse(responseText);
                    } else {
                        callback.onError("No response from safety assistant");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    callback.onError("Safety tip: If you're in immediate danger, " +
                            "please use the emergency button. " +
                            "(Technical issue: " + t.getMessage() + ")");
                }
            }, executor);
        } catch (Exception e) {
            callback.onError("Failed to process your request. Please try again later.");
        }
    }

    public interface ChatResponseCallback {
        void onResponse(String response);
        void onError(String error);
    }
}