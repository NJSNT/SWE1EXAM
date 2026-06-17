package server;

import dao.ChatDAO;
import dao.CourseDAO;
import dao.NotificationDAO;
import dao.TagDAO;
import dao.UserDAO;
import model.Administrator;
import model.Course;
import model.Message;
import model.Notification;
import model.User;
import network.Request;
import network.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private static final String MAIN_ADMIN_NAME = "admin";
    private Socket clientSocket;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private UserDAO userDAO;
    private CourseDAO courseDAO;
    private ChatDAO chatDAO;
    private TagDAO tagDAO;
    private NotificationDAO notificationDAO;
    private User currentUser;

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
        this.userDAO = new UserDAO();
        this.courseDAO = new CourseDAO();
        this.chatDAO = new ChatDAO();
        this.tagDAO = new TagDAO();
        this.notificationDAO = new NotificationDAO();
        try {
            this.out = new ObjectOutputStream(clientSocket.getOutputStream());
            this.out.flush();
            this.in = new ObjectInputStream(clientSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            Object inputObject;
            while ((inputObject = in.readObject()) != null) {
                if (inputObject instanceof Request) {
                    Request request = (Request) inputObject;
                    Response response = handleRequest(request);
                    sendDataToClient(response);

                    // After a successful login, deliver any notifications that arrived
                    // while this user was offline. We do this after sending the login
                    // response so the client has already set its currentUser before the
                    // Notification objects start arriving.
                    if ("LOGIN".equals(request.getType()) && response.isSuccess()) {
                        deliverPendingNotifications();
                    }
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Client disconnected: " + clientSocket.getInetAddress());
        } finally {
            closeConnections();
            ServerMain.removeClient(this);
        }
    }

    private Response handleRequest(Request request) {
        switch (request.getType()) {
            case "LOGIN":
                String[] credentials = (String[]) request.getPayload();
                User loggedInUser = userDAO.authenticateUser(credentials[0], credentials[1]);
                if (loggedInUser != null) {
                    currentUser = loggedInUser;
                    return new Response(true, "Login successful", loggedInUser);
                } else {
                    return new Response(false, "Invalid credentials", null);
                }
            case "REGISTER":
                User newUser = (User) request.getPayload();
                if (newUser instanceof Administrator) {
                    return new Response(false, "Failed to create account", null);
                }
                if (userDAO.isUsernameExists(newUser.getName())) {
                    return new Response(false, "Username already exists", null);
                }
                boolean created = userDAO.createUser(newUser);
                return created
                        ? new Response(true, "Account created successfully", newUser)
                        : new Response(false, "Failed to create account", null);
            case "GET_COURSES":
                return new Response(true, "Courses retrieved", courseDAO.getAllCourses());
            case "GET_TAGS":
                return new Response(true, "Tags retrieved", tagDAO.getAllTags());
            case "GET_USERS":
                if (currentUser instanceof Administrator) {
                    return new Response(true, "Users retrieved", userDAO.getAllUsers());
                } else {
                    return new Response(false, "Only administrators can view users", null);
                }
            case "UPDATE_USER_TAGS":
                User userWithNewTags = (User) request.getPayload();
                boolean tagsUpdated = userDAO.updateUserTags(userWithNewTags);
                return tagsUpdated
                        ? new Response(true, "User tags updated successfully", userWithNewTags)
                        : new Response(false, "Failed to update user tags", null);
            case "ADD_COURSE":
                Course newCourse = (Course) request.getPayload();
                boolean courseAdded = courseDAO.addCourse(newCourse);
                return courseAdded
                        ? new Response(true, "Course added successfully", newCourse)
                        : new Response(false, "Failed to add course", null);
            case "DELETE_COURSE":
                Object[] deleteData = (Object[]) request.getPayload();
                boolean courseDeleted = courseDAO.deleteCourse((Course) deleteData[1], (User) deleteData[0]);
                return courseDeleted
                        ? new Response(true, "Course deleted successfully", deleteData[1])
                        : new Response(false, "Failed to delete course", null);
            case "ADMIN_DELETE_COURSE":
                Course adminCourseToDelete = (Course) request.getPayload();
                if (!(currentUser instanceof Administrator)) {
                    return new Response(false, "Only administrators can delete any course", null);
                }
                boolean adminCourseDeleted = courseDAO.deleteCourseAsAdmin(adminCourseToDelete);
                return adminCourseDeleted
                        ? new Response(true, "Admin course deleted successfully", adminCourseToDelete)
                        : new Response(false, "Failed to delete course as admin", null);
            case "ADMIN_DELETE_USER":
                User userToDelete = (User) request.getPayload();
                if (!(currentUser instanceof Administrator)) {
                    return new Response(false, "Only administrators can delete users", null);
                }
                if (userToDelete == null
                        || userToDelete.getName().equals(currentUser.getName())
                        || MAIN_ADMIN_NAME.equals(userToDelete.getName())) {
                    return new Response(false, "Administrator cannot delete this account", null);
                }
                if (userDAO.isAdministrator(userToDelete.getName()) && !isMainAdmin()) {
                    return new Response(false, "Only main administrator can delete administrators", null);
                }
                boolean adminUserDeleted = userDAO.deleteUser(userToDelete.getName());
                return adminUserDeleted
                        ? new Response(true, "Admin user deleted successfully", userToDelete)
                        : new Response(false, "Failed to delete user as admin", null);
            case "ADMIN_PROMOTE_USER":
                User userToPromote = (User) request.getPayload();
                if (!isMainAdmin()) {
                    return new Response(false, "Only main administrator can change user roles", null);
                }
                if (userToPromote == null || userToPromote.getName().equals(currentUser.getName())) {
                    return new Response(false, "Failed to promote user to admin", null);
                }
                boolean userPromoted = userDAO.promoteStudentToAdmin(userToPromote.getName());
                return userPromoted
                        ? new Response(true, "User promoted to admin successfully", userToPromote)
                        : new Response(false, "Failed to promote user to admin", null);
            case "ADMIN_DEMOTE_USER":
                User userToDemote = (User) request.getPayload();
                if (!isMainAdmin()) {
                    return new Response(false, "Only main administrator can change user roles", null);
                }
                if (userToDemote == null
                        || userToDemote.getName().equals(currentUser.getName())
                        || MAIN_ADMIN_NAME.equals(userToDemote.getName())) {
                    return new Response(false, "Failed to demote user to student", null);
                }
                boolean userDemoted = userDAO.demoteAdminToStudent(userToDemote.getName());
                return userDemoted
                        ? new Response(true, "User demoted to student successfully", userToDemote)
                        : new Response(false, "Failed to demote user to student", null);
            case "UPDATE_COURSE":
                Object[] updateData = (Object[]) request.getPayload();
                boolean courseUpdated = courseDAO.updateCourse((Course) updateData[1], (User) updateData[0]);
                return courseUpdated
                        ? new Response(true, "Course updated successfully", updateData[1])
                        : new Response(false, "Failed to update course", null);
            case "ENROLL_COURSE":
                Object[] enrollData = (Object[]) request.getPayload();
                boolean enrolled = courseDAO.enrollStudent((model.Student) enrollData[0], (Course) enrollData[1]);
                return enrolled
                        ? new Response(true, "Successfully enrolled in course", enrollData[1])
                        : new Response(false, "Failed to enroll or already enrolled", null);
            case "GET_REGISTERED_COURSES":
                User userForRegistrations = (User) request.getPayload();
                return new Response(true, "Registered courses retrieved",
                        courseDAO.getRegisteredCourses(userForRegistrations));
            case "CANCEL_REGISTRATION":
                Object[] cancelData = (Object[]) request.getPayload();
                boolean registrationCanceled = courseDAO.cancelRegistration((Course) cancelData[1], (User) cancelData[0]);
                return registrationCanceled
                        ? new Response(true, "Registration canceled successfully", cancelData[1])
                        : new Response(false, "Failed to cancel registration", null);
            case "GET_DIRECT_CHAT_HISTORY":
                Object[] chatUsers = (Object[]) request.getPayload();
                return new Response(true, "Direct chat history retrieved",
                        chatDAO.getDirectChatHistory((User) chatUsers[0], (User) chatUsers[1]));
            case "GET_CHAT_PARTNERS":
                return new Response(true, "Chat partners retrieved",
                        chatDAO.getChatPartners((User) request.getPayload()));
            case "SEND_DIRECT_MESSAGE":
                Message directMessage = (Message) request.getPayload();
                boolean messageSaved = chatDAO.saveMessage(directMessage);
                if (messageSaved && directMessage.getReceiver() != null) {
                    String receiverName = directMessage.getReceiver().getName();

                    Notification notification = new Notification(
                            "New Message",
                            directMessage.getSender().getName() + ": " + directMessage.getText(),
                            directMessage.getSender().getName()
                    );

                    // Always persist the notification so it survives if the recipient is offline.
                    notificationDAO.save(receiverName, notification);

                    // Try to push it live. If the user is online, mark it delivered right away
                    // so it isn't re-sent at their next login.
                    boolean deliveredLive = ServerMain.sendToUser(receiverName, notification);
                    if (deliveredLive) {
                        notificationDAO.markDelivered(notification.getId());
                    }
                }
                return messageSaved
                        ? new Response(true, "Direct message sent", null)
                        : new Response(false, "Failed to send direct message", null);
            case "DISMISS_NOTIFICATION":
                // Client dismissed a single notification — remove it from the DB permanently.
                int notifId = (Integer) request.getPayload();
                notificationDAO.deleteById(notifId);
                return new Response(true, "Notification dismissed", null);
            case "CLEAR_NOTIFICATIONS":
                // Client cleared all notifications — remove all DB records for this user.
                if (currentUser != null) {
                    notificationDAO.deleteAllForUser(currentUser.getName());
                }
                return new Response(true, "Notifications cleared", null);
            default:
                return new Response(false, "Unknown request type", null);
        }
    }

    /**
     * Fetches all undelivered notifications from the DB for the current user and
     * pushes them one by one as Notification objects. The client's existing listener
     * already handles these the same way as live notifications.
     * After delivery, marks them all as delivered so they aren't re-sent next login.
     */
    private void deliverPendingNotifications() {
        if (currentUser == null) return;
        List<Notification> pending = notificationDAO.getPendingForUser(currentUser.getName());
        for (Notification notification : pending) {
            sendDataToClient(notification);
        }
        if (!pending.isEmpty()) {
            notificationDAO.markAllDeliveredForUser(currentUser.getName());
            System.out.println("Delivered " + pending.size() + " pending notification(s) to " + currentUser.getName());
        }
    }

    public void sendDataToClient(Object data) {
        try {
            out.writeObject(data);
            out.flush();
            out.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getName() : null;
    }

    private boolean isMainAdmin() {
        return currentUser instanceof Administrator && MAIN_ADMIN_NAME.equals(currentUser.getName());
    }

    private void closeConnections() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
