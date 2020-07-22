package RandomProject;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.KeyEvent;
import java.util.regex.Pattern;

class Automaton {
    private String browser;
    private String supportedCommands = "[A-Za-z0-9] | (ENTER) | (TAB) | (CTRL ([A-Za-z]|ENTER)) | (WINDOWS [A-Za-z]) | (SHIFT ([A-Za-z]|ENTER))";
    private int delayTime;
    private Scanner scio;
    private LinkedList<String> keyActions;
    private int repetitions;
    private LinkedList<Command> program;

    Automaton(int delayTime, int repetitions){
        this.delayTime = delayTime;
        this.scio = new Scanner(System.in);
        keyActions = new LinkedList<String>();
        this.repetitions = repetitions;
        this.program = new LinkedList<Command>();
    }

    public void setDelayTime(int newTime){
        this.delayTime = newTime;
    }

    public LinkedList<String> getKeyActions(){
        return this.keyActions;
    }
    public LinkedList<Command> getProgram(){
        return this.program;
    }

    private void showKeyActions() {
        System.out.println("Downloader Bot>: Showing the expected Keyboard Combinations so I can copy them...\n");
        System.out.println("< Supported Keys and Keys Combination >");
        System.out.println("Alpha Keys: A, B, C, D, E, F, G, H, ... , W , X, Y, Z");
        System.out.println("Number Keys: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 (Numpad not supported)");
        System.out.println("WINDOWS [Alpha Key]");
        System.out.println("ENTER");
        System.out.println("TAB");
        System.out.println("CTRL [Alpha Key|ENTER]");
        System.out.println("SHIFT [Alpha Key|ENTER]");
    }

    private void setupBrowser(){
        String[] browsers = {"Google Chrome", "Mozilla Firefox", "Opera", "Opera GX"};
        String[] browsersCommand = {"chrome", "firefox", "opera", "opera"};
        System.out.println("Downloader Bot>: Tell me what is your favorite browser, so I will work on a separate window.");
        for (int i = 0; i < browsers.length ; i++) {
            System.out.println("[" + i + "] " + browsers[i]);
        }
        System.out.println();

        while (true) {
            System.out.print("Downloader Bot>: What is your favorite browser?    ");
            try {
                int tempOption = scio.nextInt();
                if (tempOption < 0 || tempOption >= browsers.length) throw new Exception();
                else this.browser = browsersCommand[tempOption];

                break;
            }
            catch (Exception ex) {
                System.out.println("Downloader Bot>: Sorry, I didn't understand... Try again");
            }
        }
    }

    private boolean confirm() {
        boolean confirmation = false;

        while(true) {
            try {
                System.out.print("Downloader Bot>: Are you ok with this?    ");
                String answer = scio.nextLine();
                if (answer.equals("y")) {
                    confirmation = true;
                    break;
                }
                else if (answer.equals("n")) {
                    confirmation = false;
                    break;
                }
                else throw new Exception();
            }
            catch (Exception e) {
                System.out.println("Downloader Bot>: Bad answer detected... Try again");
            }
        }

        return confirmation;
    }

    private boolean confirmActions(LinkedList<String> actionList){
        boolean confirmation = false;
        System.out.println("Downloader Bot>: Are these actions ok? [y/n]");
        for (int i = 0; i < actionList.size() ; i++) {
            System.out.println(i + ") " + actionList.get(i));
        }

        while(true) {
            try {
                String answer = scio.nextLine();
                if (answer.equals("y")) {
                    confirmation = true;
                    break;
                }
                else if (answer.equals("n")) {
                    confirmation = false;
                    break;
                }
                else throw new Exception();
            }
            catch (Exception e) {
                System.out.println("Downloader Bot>: Bad answer detected... Try again");
            }
        }

        return confirmation;
    }

    private void setupProgram(){
        while (true) {
            try {
                System.out.print("Setup Bot>: Provide the file that contains your program:    ");
                String fileName = scio.nextLine();

                RPTCompiler compiler = new RPTCompiler();
                LinkedList<Command> generatedProgram = compiler.analyse(fileName);

                this.program = (generatedProgram == null) ? null : generatedProgram;
                break;
            }
            catch(IOException e){
                System.out.println("Setup Bot>: File not found... Try again");
            }
            catch (Exception e) {
                System.out.println("Setup Bot>: Syntax error detected in your program. Rejecting Program...");
            }
        }
    }

    private void setupRepetitions(){
        while (true) {
            try {
                System.out.print("Setup Bot>: How many times do you want me to execute this program?    ");
                int rep = scio.nextInt();
                if(rep < 0) throw new Exception("Setup Bot>: Invalid repetitions, rejecting program.");

                this.repetitions = rep;

                break;
            }
            catch (Exception e) {
                System.out.println("Setup Bot>: Syntax error detected in your program. Rejecting Program...");
            }
        }
    }

    public void setupBot(){
        System.out.println("Setup Bot>: <Entering Setup Mode>\n");
        System.out.println("Setup Bot>: We need to configure a few things before launching the bot...\n");
        //this.setupBrowser();
        this.setupProgram();
        this.setupRepetitions();
        System.out.println("Setup Bot>: <Setup Mode Done>");
    }

    int executeSingleKey(Robot robot, String action) throws Exception{
        int toPress = 0;
        switch (action) {
            case "A":
                toPress = KeyEvent.VK_A;
                break;
            case "B":
                toPress = KeyEvent.VK_B;
                break;
            case "C":
                toPress = KeyEvent.VK_C;
                break;
            case "D":
                toPress = KeyEvent.VK_D;
                break;
            case "E":
                toPress = KeyEvent.VK_E;
                break;
            case "F":
                toPress = KeyEvent.VK_F;
                break;
            case "G":
                toPress = KeyEvent.VK_G;
                break;
            case "H":
                toPress = KeyEvent.VK_H;
                break;
            case "I":
                toPress = KeyEvent.VK_I;
                break;
            case "J":
                toPress = KeyEvent.VK_J;
                break;
            case "K":
                toPress = KeyEvent.VK_K;
                break;
            case "L":
                toPress = KeyEvent.VK_L;
                break;
            case "M":
                toPress = KeyEvent.VK_M;
                break;
            case "N":
                toPress = KeyEvent.VK_N;
                break;
            case "O":
                toPress = KeyEvent.VK_O;
                break;
            case "P":
                toPress = KeyEvent.VK_P;
                break;
            case "Q":
                toPress = KeyEvent.VK_Q;
                break;
            case "R":
                toPress = KeyEvent.VK_R;
                break;
            case "S":
                toPress = KeyEvent.VK_S;
                break;
            case "T":
                toPress = KeyEvent.VK_T;
                break;
            case "U":
                toPress = KeyEvent.VK_U;
                break;
            case "V":
                toPress = KeyEvent.VK_V;
                break;
            case "W":
                toPress = KeyEvent.VK_W;
                break;
            case "X":
                toPress = KeyEvent.VK_X;
                break;
            case "Y":
                toPress = KeyEvent.VK_Y;
                break;
            case "Z":
                toPress = KeyEvent.VK_Z;
                break;
            case "ENTER":
                toPress = KeyEvent.VK_ENTER;
                break;
            case "SPACE":
                toPress = KeyEvent.VK_SPACE;
                break;
            case "TAB":
                toPress = KeyEvent.VK_TAB;
                break;
            case "ALT":
                toPress = KeyEvent.VK_ALT;
                break;
            case "CTRL":
                toPress = KeyEvent.VK_CONTROL;
                break;
            case "WINDOWS":
                toPress = KeyEvent.VK_WINDOWS;
                break;
            case "SHIFT":
                toPress = KeyEvent.VK_SHIFT;
                break;
            case "ESC":
                toPress = KeyEvent.VK_ESCAPE;
                break;
            default:
                toPress = KeyEvent.VK_WINDOWS;
                break;
        }

        robot.keyPress(toPress);
        return toPress;

        /* Using an online java executor, execute this to generate the code inside the switch:
            String[] caps = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "ENTER"};

            for(int i = 0; i < caps.length ; i++) {
                System.out.println("case \"" + caps[i] + "\":" );
                //System.out.println("\trobot.keyPress(KeyEvent.VK_" + caps[i] + ");");
                System.out.println("\ttoPress = KeyEvent.VK_" + caps[i] + ";");
                System.out.println("\tbreak;");
            }

         */
    }

    boolean executeKeyCombo(Robot robot, Command command){
        try {
            int[] keysPressed = new int[command.getCommandSize()];

            LinkedList<Token> keys = command.getKeyCombinations();
            for (int i = 0; i < keys.size() ; i++){
                keysPressed[i] = this.executeSingleKey(robot, keys.get(i).getValue());
            }

            for (int i = 0; i < keys.size() ; i++){
                robot.keyRelease(keysPressed[i]);
            }

            return true;
        }
        catch (Exception e) {
            System.out.println("Downloader Bot>: Something went bad when executing: " + command.toString());
            return false;
        }
    }

    private boolean executeSystemCommand(Command command){
        try {
            int[] keysPressed = new int[command.getCommandSize()];

            LinkedList<Token> args = command.getKeyCombinations();
            switch (args.get(0).getValue()) {
                case "WAIT":
                    Thread.sleep(Integer.parseInt(args.get(1).getValue()));
                    break;
                default:
                    break;
            }

            return true;
        }
        catch (Exception e) {
            System.out.println("Bot>: Something went bad when executing: " + command.toString());
            return false;
        }
    }

    private boolean executePseudoCommand(Robot robot, Command command){
        try {
            int[] pseudoArgs = new int[command.getCommandSize()];

            LinkedList<Token> args = command.getKeyCombinations();
            switch (args.get(0).getValue()) {
                case "TYPE":
                    char[] chars = args.get(1).getValue().toUpperCase().toCharArray();
                    for (int i = 0; i < chars.length ; i++){
                        executeSingleKey(robot, "" + chars[i]);
                    }
                    break;
                default:
                    break;
            }

            return true;
        }
        catch (Exception e) {
            System.out.println("Bot>: Something went bad when executing: " + command.toString() + " " + e.getMessage());
            return false;
        }
    }

    private boolean executeCommand(Robot robot, Command command) throws Exception{
        if (command.getCommandType().equals(Command.CommandType.KEYBOARD)) {
            return executeKeyCombo(robot, command);
        }
        else if(command.getCommandType().equals(Command.CommandType.SYSTEM)) {
            return executeSystemCommand(command);
        }
        else if(command.getCommandType().equals(Command.CommandType.PSEUDO)) {
            return executePseudoCommand(robot, command);
        }
        else {
            throw new Exception("Unreachable command type");
        }
    }

    public void run(LinkedList<Command> program) throws Exception{
        Robot autobot = new Robot();/*
        for (int i = 0 ; i < this.repetitions ; i++) {
            for (Command action: program) {
                if(executeKeyCombo(autobot, action))
                    System.out.println("Downloader Bot>: " + action.toString() + "executed succesfully");
                else {
                    System.out.println("Downloader Bot>: Something went wrong wtih " + action + ". Stopping execution");
                    throw new Exception(action + " could not be executed");
                }

                Thread.sleep(500);
            }
        }*/
        System.out.println("Bot>: Click on the correct tab so the bot works properly. (Else, results can be disastrous!!!)");

        for (int i = 10; i > 0 ; i--) {
            System.out.println("Bot>: Press CTRL + C to cancel operation.");
            System.out.println("Bot>: " + i + "second(s) before executing...");
            Thread.sleep(1000);
        }

        System.out.println("Bot>: Starting...");
        for (int k = 0; k < this.repetitions; k++) {
            System.out.println("\nBot>: <ITERATION #" + k + ">");
            for (Command action: program) {
                if(executeCommand(autobot, action))
                    System.out.println("Bot>: " + action.toString() + "executed successfully");
                else {
                    System.out.println("Bot>: Something went wrong with " + action + ". Stopping execution");
                    throw new Exception(action + " could not be executed");
                }

                Thread.sleep(300);
            }
        }
    }
}

public class Downloader {
    public static void main(String[] args) {
        Automaton automaton = new Automaton(500, 1);

        automaton.setupBot();

        try {
            automaton.run(automaton.getProgram());
        }
        catch(Exception e) {
            System.out.println(e.getMessage());
        }
    }
}

