package viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.paint.Paint;
import model.ClientModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class LoginViewModel implements PropertyChangeListener {
    private ClientModel model;

    // Data binding properties for the view
    private StringProperty usernameProperty;
    private StringProperty passwordProperty;
    private StringProperty errorProperty;
    private ObjectProperty<Paint> statusColor;
    private Runnable onLoginSuccess;

    public LoginViewModel(ClientModel model) {
        this.model = model;
        this.usernameProperty = new SimpleStringProperty("");
        this.passwordProperty = new SimpleStringProperty("");
        this.errorProperty = new SimpleStringProperty("");
        this.statusColor = new SimpleObjectProperty<>(Paint.valueOf("#e74c3c"));

        // Listen for responses from the server via the Model
        this.model.addListener("LoginResult", this);
        this.model.addListener("RegisterResult", this);
    }

    public void login() {
        if (usernameProperty.get().isEmpty() || passwordProperty.get().isEmpty()) {
            showError("Please enter both username and password");
            return;
        }
        showInfo("Connecting...");
        model.login(usernameProperty.get(), passwordProperty.get());
    }

    public void registerStudent() {
        if (usernameProperty.get().isEmpty() || passwordProperty.get().isEmpty()) {
            showError("Please enter both username and password to register");
            return;
        }
        showInfo("Registering...");
        model.registerStudent(usernameProperty.get(), passwordProperty.get());
    }

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    public StringProperty usernameProperty() { return usernameProperty; }
    public StringProperty passwordProperty() { return passwordProperty; }
    public StringProperty errorProperty() { return errorProperty; }
    public ObjectProperty<Paint> statusColorProperty() { return statusColor; }

    public void resetForm() {
        usernameProperty.set("");
        passwordProperty.set("");
        errorProperty.set("");
        statusColor.set(Paint.valueOf("#e74c3c"));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        // This is called when the Model fires an event (which happens when NetworkClient receives a Response)
        // Since this might be called from a background thread, JavaFX UI updates should ideally be wrapped in Platform.runLater()
        // We will handle that in the Controller or here. For now, we update the property.
        
        javafx.application.Platform.runLater(() -> {
            if ("LoginResult".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    showSuccess("Login Successful! Welcome " + model.getCurrentUser().getName());
                    if (onLoginSuccess != null) {
                        onLoginSuccess.run();
                    }
                } else {
                    showError("Invalid credentials. Try again");
                }
            } else if ("RegisterResult".equals(evt.getPropertyName())) {
                if ("SUCCESS".equals(evt.getNewValue())) {
                    showSuccess("Registration Successful! You can now login");
                } else if ("EXISTS".equals(evt.getNewValue())) {
                    showError("Username already exists. Please choose another one.");
                } else {
                    showError("Registration Failed");
                }
            }
        });
    }

    private void showError(String message) {
        statusColor.set(Paint.valueOf("#e74c3c"));
        errorProperty.set(message);
    }

    private void showSuccess(String message) {
        statusColor.set(Paint.valueOf("#27ae60"));
        errorProperty.set(message);
    }

    private void showInfo(String message) {
        statusColor.set(Paint.valueOf("#34495e"));
        errorProperty.set(message);
    }
}
