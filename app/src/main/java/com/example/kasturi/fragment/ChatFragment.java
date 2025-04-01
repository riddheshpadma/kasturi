// ChatFragment.java
package com.example.kasturi.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kasturi.R;
import com.example.kasturi.classes.ChatbotService;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class ChatFragment extends Fragment {
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private Button sendButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages = new ArrayList<>();
    private ChatbotService chatbotService;

    @SuppressLint("MissingInflatedId")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        // Initialize UI components
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);

        // Initialize chatbot service with main thread executor
        Executor mainExecutor = ContextCompat.getMainExecutor(requireContext());
        chatbotService = new ChatbotService(requireContext(), mainExecutor);

        // Setup RecyclerView
        chatAdapter = new ChatAdapter(chatMessages);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(chatAdapter);

        // Add initial safety tips
        addBotMessage(getString(R.string.welcome_message));
        addSafetyTips();

        // Set send button click listener
        sendButton.setOnClickListener(v -> sendMessage());

        return view;
    }

    private void addSafetyTips() {
        String[] tips = getResources().getStringArray(R.array.safety_tips);
        for (String tip : tips) {
            addBotMessage(tip);
        }
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            addUserMessage(message);
            messageInput.setText("");

            // Use generateContent instead of getSafetyResponse
            chatbotService.generateContent(message, new ChatbotService.ChatResponseCallback() {
                @Override
                public void onResponse(String response) {
                    requireActivity().runOnUiThread(() -> addBotMessage(response));
                }

                @Override
                public void onError(String error) {
                    requireActivity().runOnUiThread(() -> {
                        addBotMessage(error);
                        addBotMessage(getString(R.string.use_emergency_button));
                    });
                }
            });
        }
    }

    private void addUserMessage(String message) {
        chatMessages.add(new ChatMessage(message, true));
        updateChatView();
    }

    private void addBotMessage(String message) {
        chatMessages.add(new ChatMessage(message, false));
        updateChatView();
    }

    private void updateChatView() {
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
    }

    // ChatMessage data class
    private static class ChatMessage {
        String message;
        boolean isUser;

        ChatMessage(String message, boolean isUser) {
            this.message = message;
            this.isUser = isUser;
        }
    }

    // RecyclerView Adapter
    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
        private final List<ChatMessage> messages;

        ChatAdapter(List<ChatMessage> messages) {
            this.messages = messages;
        }

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_message, parent, false);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatMessage message = messages.get(position);
            holder.messageText.setText(message.message);

            // Adjust layout based on who sent the message
            if (message.isUser) {
                holder.messageContainer.setBackgroundResource(R.drawable.user_message_bg);
                holder.messageText.setTextColor(getResources().getColor(R.color.white));
                ((LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams()).gravity =
                        android.view.Gravity.END;
            } else {
                holder.messageContainer.setBackgroundResource(R.drawable.bot_message_bg);
                holder.messageText.setTextColor(getResources().getColor(R.color.black));
                ((LinearLayout.LayoutParams) holder.messageContainer.getLayoutParams()).gravity =
                        android.view.Gravity.START;
            }
        }

        @Override
        public int getItemCount() {
            return messages.size();
        }

        class ChatViewHolder extends RecyclerView.ViewHolder {
            TextView messageText;
            LinearLayout messageContainer;

            ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                messageText = itemView.findViewById(R.id.messageText);
                messageContainer = itemView.findViewById(R.id.messageContainer);
            }
        }
    }
}