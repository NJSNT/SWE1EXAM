import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Window;
import model.ClientModel;
import model.Administrator;
import view.AdminDashboardController;
import view.LoginViewController;
import view.MainDashboardController;
import viewmodel.AdminDashboardViewModel;
import viewmodel.DashboardViewModel;
import viewmodel.LoginViewModel;

import java.io.IOException;
import java.util.ArrayList;

public class Main extends Application {

    private ClientModel clientModel;
    private LoginViewModel loginViewModel;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 1. Initialize Model and start network connection
        clientModel = new ClientModel();
        clientModel.start(); // This attempts to connect to the Server (make sure ServerMain is running!)

        // 2. Initialize ViewModel
        loginViewModel = new LoginViewModel(clientModel);

        // Define Scene transition on successful login
        loginViewModel.setOnLoginSuccess(() -> {
            try {
                if (clientModel.getCurrentUser() instanceof Administrator) {
                    openAdminDashboard(primaryStage);
                } else {
                    openStudentDashboard(primaryStage);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        // 3. Setup Scene and Stage
        openLoginView(primaryStage);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    @Override
    public void stop() {
        // Clean up network resources when window is closed
        System.out.println("Application closing, disconnecting...");
        // clientModel.stop(); // If implemented
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void openLoginView(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/LoginView.fxml"));
        Parent root = loader.load();

        LoginViewController controller = loader.getController();
        loginViewModel.resetForm();
        controller.init(loginViewModel);

        Scene loginScene = new Scene(root, 400, 500);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Skills & Tutoring Exchange");
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    private void openStudentDashboard(Stage primaryStage) throws IOException {
        FXMLLoader dashLoader = new FXMLLoader(getClass().getResource("/view/MainDashboard.fxml"));
        Parent dashRoot = dashLoader.load();

        DashboardViewModel dashViewModel = new DashboardViewModel(clientModel);
        MainDashboardController dashController = dashLoader.getController();
        dashController.init(dashViewModel);
        dashController.setOnLogout(() -> {
            dashViewModel.dispose();
            logout(primaryStage);
        });

        Scene dashScene = new Scene(dashRoot, 800, 650);
        primaryStage.setScene(dashScene);
        primaryStage.setTitle("Skills & Tutoring Exchange - Dashboard");
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    private void openAdminDashboard(Stage primaryStage) throws IOException {
        FXMLLoader adminLoader = new FXMLLoader(getClass().getResource("/view/AdminDashboard.fxml"));
        Parent adminRoot = adminLoader.load();

        AdminDashboardViewModel adminViewModel = new AdminDashboardViewModel(clientModel);
        AdminDashboardController adminController = adminLoader.getController();
        adminController.init(adminViewModel);
        adminController.setOnLogout(() -> {
            adminViewModel.dispose();
            logout(primaryStage);
        });

        Scene adminScene = new Scene(adminRoot, 800, 600);
        primaryStage.setScene(adminScene);
        primaryStage.setTitle("Skills & Tutoring Exchange - Admin");
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
    }

    private void logout(Stage primaryStage) {
        try {
            closeExtraWindows(primaryStage);
            clientModel.logout();
            openLoginView(primaryStage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void closeExtraWindows(Stage primaryStage) {
        for (Window window : new ArrayList<>(Window.getWindows())) {
            if (window instanceof Stage && window != primaryStage) {
                ((Stage) window).close();
            }
        }
    }
}
