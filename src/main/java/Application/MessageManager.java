package Application;

import Helpers.ServiceErrorHandler;
import Models.Message;
import Models.Order;

import java.util.List;

public class MessageManager {
    private static MessageService messageService;

    private MessageManager() {
        messageService = new MessageService();
        messageService.setOnSucceeded(event -> {
            List<Message> newOrders = messageService.getValue();

            messageService.restart();
        });

        messageService.setOnFailed(new ServiceErrorHandler());

    }

    static MessageService initialize(){
        return new MessageService();
    }
}
