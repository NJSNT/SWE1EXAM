package viewmodel;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.ClientModel;
import model.User;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

public class ChatHistoryViewModel implements PropertyChangeListener {
    private ClientModel model;
    private ObservableList<User> chatPartners;
    private StringProperty statusMessage;

    public ChatHistoryViewModel(ClientModel model) {
        this.model = model;
        this.chatPartners = FXCollections.observableArrayList();
        this.statusMessage = new SimpleStringProperty("");

        this.model.addListener("ChatPartnersRetrieved", this);
        refreshChatPartners();
    }

    public void refreshChatPartners() {
        statusMessage.set("Loading chat history...");
        model.fetchChatPartners();
    }

    public ClientModel getModel() {
        return model;
    }

    public ObservableList<User> getChatPartners() {
        return chatPartners;
    }

    public StringProperty statusMessageProperty() {
        return statusMessage;
    }

    public void setStatusMessage(String message) {
        statusMessage.set(message);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Platform.runLater(() -> {
            if ("ChatPartnersRetrieved".equals(evt.getPropertyName())) {
                List<User> partners = (List<User>) evt.getNewValue();
                chatPartners.clear();
                chatPartners.addAll(partners);

                if (partners.isEmpty()) {
                    statusMessage.set("No chat history yet");
                } else {
                    statusMessage.set("Chat history loaded");
                }
            }
        });
    }
}
