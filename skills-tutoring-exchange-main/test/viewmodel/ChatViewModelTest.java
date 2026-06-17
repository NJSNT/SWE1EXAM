package viewmodel;

import model.ClientModel;
import model.Student;
import model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class ChatViewModelTest {

    private ClientModel mockModel;
    private ChatViewModel viewModel;
    private User chatPartner;

    @BeforeEach
    public void setUp() {
        mockModel = mock(ClientModel.class);
        chatPartner = new Student("Bob", "pass");
        
        viewModel = new ChatViewModel(mockModel, chatPartner, "Chat with Bob");
    }

    // Rainy Scenario: Empty message
    @Test
    public void testSendEmptyMessage() {
        viewModel.messageInputProperty().set("");
        
        viewModel.sendMessage();
        
        verify(mockModel, never()).sendDirectMessage(any(User.class), anyString());
    }

    // Sunny Scenario: Send valid message
    @Test
    public void testSendValidMessage() {
        viewModel.messageInputProperty().set("Hello Bob!");
        
        viewModel.sendMessage();
        
        verify(mockModel, times(1)).sendDirectMessage(chatPartner, "Hello Bob!");
        assertEquals("", viewModel.messageInputProperty().get(), "Input should be cleared after sending.");
    }
}
