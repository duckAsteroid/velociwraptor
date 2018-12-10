package com.asteroid.duck.velociwraptor.user;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import org.fusesource.jansi.AnsiPrintStream;

import javax.json.JsonArray;
import javax.json.JsonString;
import javax.json.JsonValue;
import javax.xml.ws.WebServiceException;
import java.io.PrintStream;
import java.util.Scanner;

import static org.fusesource.jansi.Ansi.Color.BLUE;
import static org.fusesource.jansi.Ansi.ansi;

public class ConsoleInteractive extends UserInteractive {
    private final Scanner input;
    private final PrintStream output;
    private final boolean noColors;

    public ConsoleInteractive(boolean noColors, Scanner input, PrintStream output) {
        this.noColors = noColors;
        this.input = input;
        this.output = output;
        Ansi.setEnabled(!noColors);
        if (!noColors) {
            AnsiConsole.systemInstall();
        }
    }

    public static UserInteractive console(boolean noColors) {
        return new ConsoleInteractive(noColors, new Scanner(System.in), System.out);
    }


    @Override
    public Boolean askBooleanOption(String key, boolean current) {
        // print the options
        output.println(ansi().fg(BLUE) + "[?] " + ansi().reset() + "Please choose an option for \""+key+"\":");
        Boolean selection = null;
        do {
            output.println("    Choose from " + current + "/" + (!current) + ansi().fgBright(BLUE) + "[default: " + current + "]" + ansi().reset());
            String line = input.nextLine().toLowerCase();
            if (line.length()<=0) {
                selection = current;
            }
            else if (line.startsWith("t") || line.startsWith("y")) {
                selection = Boolean.TRUE;
            }
            else if (line.startsWith("f") || line.startsWith("n")) {
                selection = Boolean.FALSE;
            }
        }
        while(selection == null);
        return selection;
    }

    /**
     * Ask the user which of the given options to select from
     * @return the selected option
     */
    public JsonValue askOption(String key, JsonArray options) {
        if (options == null) {
            throw new IllegalArgumentException(key +": options cannot be null");
        }
        // print the options
        output.println(ansi().fg(BLUE) + "[?] " + ansi().reset() + "Please choose an option for \""+key+"\":");
        for(int i = 0; i < options.size(); i++) {
            JsonValue value = options.get(i);
            if (!(value instanceof JsonString)) {
                throw new IllegalArgumentException("Unexpected array member: "+value);
            }
            output.println("    " + (i + 1) + " - " + value);
        }
        // read the selection
        Integer selection = null;
        do {
            // prompt to choose
            output.println("    Choose from 1.." + options.size()+ ansi().fgBright(BLUE) + "[default: 1]"+ansi().reset());
            String line = input.nextLine();
            if (line.length() <= 0) {
                // default selection
                selection = 1;
                break;
            }
            else {
                try {
                    selection = Integer.parseInt(line);
                }
                catch(NumberFormatException e) {
                    output.println(ansi().fgBrightRed() + "    Unrecognised option: "+ ansi().reset() + line);
                }
            }
            if (selection != null) {
                if (selection < 1 || selection > options.size()) {
                    selection = null;
                    output.println(ansi().fgBrightRed() + "    Invalid option: " + ansi().reset() + line);
                }
            }
        } while(selection == null);

        // now we have a selection - return it
        return options.get(selection - 1);
    }

    public String askFor(String key, String current) {
        output.println(ansi().fg(BLUE) + "[?] " + ansi().reset() + "Please choose an option for \""+key+"\" "+ansi().fgBrightBlue()+" [default: "+ current +"]"+ansi().reset()+":");
        String line = null;
        do {
            line = input.nextLine();
            if (line.length() <= 0) {
                line = current;
                break;
            }
        } while( line == null);

        return line;
    }

    @Override
    public void close() throws WebServiceException {
        if (!noColors) {
            AnsiConsole.systemUninstall();
        }
    }
}
