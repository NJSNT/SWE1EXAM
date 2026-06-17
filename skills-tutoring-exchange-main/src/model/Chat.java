package model;

import java.util.ArrayList;
import java.util.List;

public class Chat implements Subject {
    private List<Message> chatHistory;
    // Users involved in this chat
    private List<User> participants;
    private List<Observer> observers;

    public Chat() {
        this.chatHistory = new ArrayList<>();
        this.participants = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    @Override
    public void addObserver(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Notification notification) {
        for (Observer observer : observers) {
            observer.update(notification);
        }
    }

    public void addParticipant(User user) {
        if (!participants.contains(user)) {
            participants.add(user);
        }
    }

    public List<Message> getChatHistory() {
        return chatHistory;
    }

    public void addMessage(Message message) {
        this.chatHistory.add(message);
        
        Notification notification = new Notification("New Message", 
            message.getSender().getName() + ": " + message.getText(),
            message.getSender().getName());
        notifyObservers(notification);
    }

    public void sendMessage(Message message) {
        addMessage(message);
    }

    // Direct message notifications are now handled by the server socket that owns the receiver.
    private void triggerNotification(Message message) {
        // Kept empty because this class only represents the chat domain object.
    }
}
