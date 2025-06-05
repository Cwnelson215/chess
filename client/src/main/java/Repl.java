import java.util.Scanner;

import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;

    public Repl(ChessClient client) {
        this.client = client;
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
                System.out.println(SET_TEXT_COLOR_GREEN + result);
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
}
