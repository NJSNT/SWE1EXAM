package view;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import viewmodel.LoginViewModel;

public class LoginViewController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private LoginViewModel viewModel;

    // Called automatically after FXML is loaded
    public void init(LoginViewModel viewModel) {
        this.viewModel = viewModel;

        // Bind UI fields directly to ViewModel properties (Bidirectional for input fields)
        usernameField.textProperty().bindBidirectional(viewModel.usernameProperty());
        passwordField.textProperty().bindBidirectional(viewModel.passwordProperty());
        
        // Unidirectional bind for the status label
        errorLabel.textProperty().bind(viewModel.errorProperty());
        errorLabel.textFillProperty().bind(viewModel.statusColorProperty());
    }

    @FXML
    public void onLoginButton() {
        viewModel.login();
    }

    @FXML
    public void onRegisterButton() {
        viewModel.registerStudent();
    }
}
