package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import viewmodel.ChatViewModel;

public class ChatViewController {

    @FXML private Label chatTitleLabel;
    @FXML private ListView<model.Message> messageListView;
    @FXML private TextField messageInputField;
    @FXML private Label chatStatusLabel;

    private ChatViewModel viewModel;

    public void init(ChatViewModel viewModel) {
        this.viewModel = viewModel;

        messageListView.setItems(viewModel.getMessageList());
        chatTitleLabel.textProperty().bind(viewModel.chatTitleProperty());
        messageInputField.textProperty().bindBidirectional(viewModel.messageInputProperty());
        chatStatusLabel.textProperty().bind(viewModel.chatStatusProperty());
        
        // Auto-scroll to bottom when new messages arrive
        viewModel.getMessageList().addListener((javafx.collections.ListChangeListener.Change<? extends model.Message> c) -> {
            messageListView.scrollTo(viewModel.getMessageList().size() - 1);
        });
    }

    @FXML
    public void onSendButton() {
        viewModel.sendMessage();
    }
}
