import java.util.Scanner;
import ui.EscapeSequences;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;

public class PreLoginRepl {
    private final ChessClient client;

    public PreLoginRepl(ChessClient client) {
        this.client = client;
    }

    public void run() {
        System.out.println(client.help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while(!result.equals("quit")) {
            String line = scanner.nextLine();

            try {
                result = client.eval(line);
                System.out.println(SET_TEXT_COLOR_BLUE + result);
            } catch (Exception e) {
                var message = e.getMessage();
                System.out.println(message);
            }
        }
        System.out.println();
    }
}
