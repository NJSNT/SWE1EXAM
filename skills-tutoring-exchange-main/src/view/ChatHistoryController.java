package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import model.User;
import viewmodel.ChatHistoryViewModel;

public class ChatHistoryController {

    @FXML private ListView<User> chatPartnerListView;
    @FXML private Label statusLabel;

    private ChatHistoryViewModel viewModel;

    public void init(ChatHistoryViewModel viewModel) {
        this.viewModel = viewModel;

        chatPartnerListView.setItems(viewModel.getChatPartners());
        statusLabel.textProperty().bind(viewModel.statusMessageProperty());

        chatPartnerListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                openSelectedChat();
            }
        });
    }

    @FXML
    public void onOpenChatButton() {
        openSelectedChat();
    }

    @FXML
    public void onRefreshButton() {
        viewModel.refreshChatPartners();
    }

    private void openSelectedChat() {
        User selectedUser = chatPartnerListView.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            viewModel.setStatusMessage("Please select a chat");
            return;
        }

        ChatWindowManager.openChat(viewModel.getModel(), selectedUser);
    }
}
