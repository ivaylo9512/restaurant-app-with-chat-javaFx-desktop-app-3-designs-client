package Application;

import Models.Message;

import java.util.List;

public class MessageManager {
    private static MessageService messageService;

    private MessageManager() {
        messageService = new MessageService();
        messageService.setOnSucceeded(event -> {
            List<Message> newOrders = messageService.getValue();

            messageService.restart();
        });
    }

    static MessageManager initialize(){
        return new MessageManager();
    }
}
