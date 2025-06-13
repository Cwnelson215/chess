package repls;

import serverfacade.websocket.NotificationHandler;
import websocket.messages.NotificationMessage;

import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl implements NotificationHandler {
    private final ChessClient client;

    public Repl(String serverUrl) {
        this.client = new ChessClient(serverUrl, this);
    }

    public void run() {
        System.out.println(RESET_TEXT_UNDERLINE + client.help());
        System.out.printf("[%s]>>> ", client.getState());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("Goodbye! \uD83D\uDE0A")) {
            String line = scanner.nextLine();
            try {
                result = client.eval(line);
                System.out.print(SET_TEXT_COLOR_GREEN);
                System.out.println(result);
                System.out.print(SET_TEXT_COLOR_GREEN);
                System.out.println();
                if(!result.equals("Goodbye! \uD83D\uDE0A")) {
                    System.out.printf(SET_TEXT_COLOR_BLUE + "[%s]>>> ", client.getState());
                }
            } catch (Exception e) {
                var message = e.getMessage();
                System.out.println(message);
            }
        }
        System.out.println();

    }

    public void notify(NotificationMessage notification) {
        client.notify(notification);
    }
}
