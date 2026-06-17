package view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.ClientModel;
import model.User;
import viewmodel.ChatViewModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ChatWindowManager {
    private static Map<String, Stage> openChatWindows = new HashMap<>();

    public static void openChat(ClientModel model, User chatPartner) {
        if (model == null || model.getCurrentUser() == null || chatPartner == null) {
            return;
        }

        // Opening the chat means messages from this user have been seen.
        model.markNotificationsFromUserRead(chatPartner.getName());

        String windowKey = createWindowKey(model, chatPartner);
        Stage existingStage = openChatWindows.get(windowKey);
        if (existingStage != null && existingStage.isShowing()) {
            existingStage.toFront();
            existingStage.requestFocus();
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(ChatWindowManager.class.getResource("/view/ChatView.fxml"));
            Parent root = loader.load();

            String chatTitle = "Chat with " + chatPartner.getName();
            ChatViewModel chatViewModel = new ChatViewModel(model, chatPartner, chatTitle);
            ChatViewController controller = loader.getController();
            controller.init(chatViewModel);

            Stage chatStage = new Stage();
            chatStage.setTitle(chatTitle);
            chatStage.setScene(new Scene(root));
            openChatWindows.put(windowKey, chatStage);

            // Remove the window from the registry when it is closed.
            chatStage.setOnHidden(event -> {
                chatViewModel.dispose();
                openChatWindows.remove(windowKey);
            });

            chatStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String createWindowKey(ClientModel model, User chatPartner) {
        return model.getCurrentUser().getName() + "->" + chatPartner.getName();
    }
}
