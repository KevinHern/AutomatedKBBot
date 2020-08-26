package SPKB;

import jdk.nashorn.internal.parser.TokenType;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.KeyEvent;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.util.regex.Pattern;

class Automaton implements ClipboardOwner {

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        System.out.println("Bot>: Clipboard contents were replaced");
    }

    private String browser;
    private String supportedCommands = "[A-Za-z0-9] | (ENTER) | (TAB) | (CTRL ([A-Za-z]|ENTER)) | (WINDOWS [A-Za-z]) | (SHIFT ([A-Za-z]|ENTER))";
    private int delayTime;
    private Scanner scio;
    private LinkedList<String> keyActions;
    private int repetitions;
    private LinkedList<Command> program;
    private Hashtable<String, String> symbolTableString;
    private Hashtable<String, Integer> symbolTableInt;
    private Hashtable<String, Stack<String>> stackTable;
    private Hashtable<String, PriorityQueue<String>> queueTable;

    Automaton(int delayTime, int repetitions){
        this.delayTime = delayTime;
        this.scio = new Scanner(System.in);
        keyActions = new LinkedList<String>();
        this.repetitions = repetitions;
        this.program = new LinkedList<Command>();
        this.symbolTableString = new Hashtable<>();
        this.symbolTableInt = new Hashtable<>();
        this.stackTable = new Hashtable<>();
        stackTable.put("CLIPBOARD", new Stack<>());
        this.queueTable = new Hashtable<>();
        queueTable.put("CLIPBOARD", new PriorityQueue<>());
        this.preloadValues();
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

    private boolean setupProgram(){
        while (true) {
            try {
                System.out.print("Setup Bot>: Provide the file that contains your program: ");
                String fileName = scio.nextLine();
                while(!fileName.endsWith(".txt")) {
                    System.out.println("Setup Bot>: Error in file format, Expected '.txt'. Provide the name again.");
                    System.out.print("Setup Bot>: Provide the file that contains your program: ");
                    fileName = scio.nextLine();
                }

                RPTCompiler compiler = new RPTCompiler();
                LinkedList<Command> generatedProgram = compiler.analyse(fileName);

                this.program = generatedProgram;
                //System.out.println("Null program?" + ((this.program == null)? "yes" : "no"));
                return (this.program != null);
            }
            catch(IOException e){
                System.out.println(e.getMessage());
                return false;
            }
            catch (Exception e) {
                System.out.println(e.getMessage());
                return false;
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

    public void preloadValues(){
        try {

            Pattern stack = Pattern.compile("[Ss][Tt][Aa][Cc][Kk]");
            Pattern queue = Pattern.compile("[Qq][Uu][Ee][Uu][Ee]");
            System.out.println("Setup Bot>: Searching for 'preload.txt' to load values into Clipboard before starting execution...");

            BufferedReader br = new BufferedReader(new FileReader("preload.txt"));
            String line;
            LinkedList<String> lines = new LinkedList<>();
            for(; ((line = br.readLine()) != null) ;) {
                lines.add(new String(line));
            }

            if(lines.size() > 1) {
                if (stack.matcher(lines.peek()).matches()) {
                    lines.removeFirst();
                    Stack<String> preloadStack = this.stackTable.get("CLIPBOARD");
                    if(preloadStack == null) throw new Exception("Stack not found, proceeding.");
                    for (String pvalue: lines) {
                        preloadStack.push(pvalue);
                    }
                }
                else if(queue.matcher(lines.peek()).matches()) {
                    lines.removeFirst();
                    PriorityQueue<String> preloadQueue = this.queueTable.get("CLIPBOARD");
                    if(preloadQueue == null) throw new Exception("Queue not found, proceeding.");
                    for (String pvalue: lines) {
                        preloadQueue.add(pvalue);
                    }
                }
                else throw new Exception("Error detected in preload file format, proceeding.");
                System.out.println("Setup Bot>: Preload complete, proceeding.");
            }
        }
        catch (FileNotFoundException e) {
            System.out.println("Setup Bot>: File not found, proceeding.");
        }
        catch (Exception e) {
            System.out.println("Setup Bot>: " + e.getMessage());
        }
    }

    public boolean setupBot(){
        System.out.println("Setup Bot>: <Entering Setup Mode>\n");
        System.out.println("Setup Bot>: We need to configure a few things before launching the bot...\n");
        //this.setupBrowser();
        if (this.setupProgram()) {
            this.setupRepetitions();
            System.out.println("Setup Bot>: <Setup Mode Done>");
            return true;
        }
        else return false;
    }

    private int executeSingleKey(Robot robot, String action) throws Exception{
        int toPress = 0;
        //System.out.println(action);
        switch (action.toUpperCase()) {
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
            case "1":
                toPress = KeyEvent.VK_1;
                break;
            case "2":
                toPress = KeyEvent.VK_2;
                break;
            case "3":
                toPress = KeyEvent.VK_3;
                break;
            case "4":
                toPress = KeyEvent.VK_4;
                break;
            case "5":
                toPress = KeyEvent.VK_5;
                break;
            case "6":
                toPress = KeyEvent.VK_6;
                break;
            case "7":
                toPress = KeyEvent.VK_7;
                break;
            case "8":
                toPress = KeyEvent.VK_8;
                break;
            case "9":
                toPress = KeyEvent.VK_9;
                break;
            case "0":
                toPress = KeyEvent.VK_0;
                break;
            case "(":
                toPress = KeyEvent.VK_LEFT_PARENTHESIS;
                break;
            case ")":
                toPress = KeyEvent.VK_RIGHT_PARENTHESIS;
                break;
            case "[":
                toPress = KeyEvent.VK_OPEN_BRACKET;
                break;
            case "]":
                toPress = KeyEvent.VK_CLOSE_BRACKET;
                break;
            case "{":
                toPress = KeyEvent.VK_BRACELEFT;
                break;
            case "}":
                toPress = KeyEvent.VK_BRACERIGHT;
                break;
            case "\"":
                toPress = KeyEvent.VK_QUOTEDBL;
                break;
            case "'":
                toPress = KeyEvent.VK_QUOTE;
                break;
            case "~":
                toPress = KeyEvent.VK_DEAD_TILDE;
                break;
            case "+":
            case "PLUS":
                toPress = KeyEvent.VK_ADD;
                break;
            case "-":
            case "DASH":
                toPress = KeyEvent.VK_MINUS;
                break;
            case "/":
            case "SLASH":
                toPress = KeyEvent.VK_DIVIDE;
                break;
            case "*":
            case "ASTERISK":
                toPress = KeyEvent.VK_MULTIPLY;
                break;
            case "^":
                toPress = KeyEvent.VK_DEAD_CIRCUMFLEX;
                break;
            case ".":
            case "PERIOD":
                toPress = KeyEvent.VK_PERIOD;
                break;
            case ",":
            case "COMMA":
                toPress = KeyEvent.VK_COMMA;
                break;
            case ":":
            case "COLON":
                toPress = KeyEvent.VK_COLON;
                break;
            case ";":
            case "SEMI":
                toPress = KeyEvent.VK_SEMICOLON;
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
            case "DELETE":
                toPress = KeyEvent.VK_DELETE;
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
            case "LARROW":
                toPress = KeyEvent.VK_LEFT;
                break;
            case "UARROW":
                toPress = KeyEvent.VK_UP;
                break;
            case "RARROW":
                toPress = KeyEvent.VK_RIGHT;
                break;
            case "DARROW":
                toPress = KeyEvent.VK_DOWN;
                break;
            case "F1":
                toPress = KeyEvent.VK_F1;
                break;
            case "F2":
                toPress = KeyEvent.VK_F2;
                break;
            case "F3":
                toPress = KeyEvent.VK_F3;
                break;
            case "F4":
                toPress = KeyEvent.VK_F4;
                break;
            case "F5":
                toPress = KeyEvent.VK_F5;
                break;
            case "F6":
                toPress = KeyEvent.VK_F6;
                break;
            case "F7":
                toPress = KeyEvent.VK_F7;
                break;
            case "F8":
                toPress = KeyEvent.VK_F8;
                break;
            case "F9":
                toPress = KeyEvent.VK_F9;
                break;
            default:
                throw new Exception("Unsupported Key Command. Halting execution.");
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

    private boolean executeKeyCombo(Robot robot, Command command) throws Exception{
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
            throw new Exception("Downloader Bot>: Something went bad when executing: " + command.toString() + "\nReason: " + e.getMessage());
        }
    }

    private boolean executeSystemCommand(Command command) throws Exception{
        try {
            LinkedList<Token> args = command.getKeyCombinations();
            String tempStr = "";
            switch (args.get(0).getValue().toUpperCase()) {
                case "WAIT":
                    Thread.sleep(Integer.parseInt(args.get(1).getValue()));
                    break;
                case "PAUSE":
                    System.out.println("Bot>: Bot paused, press Enter to resume execution...");
                    scio.nextLine();
                    for (int i = 7; i > 0 ; i--) {
                        System.out.println("Bot>: Resuming execution in " + i + " second(s)...");
                        Thread.sleep(1000);
                    }
                    break;
                case "HALT":
                    throw new Exception("Execution halted.");
                case "VAR":
                    switch (args.get(1).getValue()) {
                        case "INT":
                            if(this.symbolTableInt.putIfAbsent(args.get(2).getValue(), Integer.parseInt(args.get(3).getValue())) != null)
                                throw new Exception("Variable already exists. Halting execution.");
                            break;
                        case "STRING":
                            if(this.symbolTableString.putIfAbsent(args.get(2).getValue(), args.get(3).getValue()) != null)
                                throw new Exception("Variable already exists. Halting execution.");
                            break;
                        default:
                            throw new Exception("Unreachable SYSTEM command state");
                    }
                    break;
                // STACKS
                case "PUSH":
                    String pushStr = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    stackTable.get(args.get(1).getValue()).push(pushStr);
                    System.out.println("Bot >: Successfully saved " + pushStr + " to the CLIPBOARD Stack.");
                    break;
                case "POP":
                    tempStr = stackTable.get(args.get(1).getValue()).pop();
                    if(tempStr != null) {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(tempStr), this);
                        System.out.println("Bot >: Successfully popped " + tempStr + " from the CLIPBOARD Stack.");
                    }
                    break;
                case "PEEK":
                    tempStr = stackTable.get(args.get(1).getValue()).peek();
                    if(tempStr != null) {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(tempStr), this);
                        System.out.println("Bot >: Successfully peeked " + tempStr + " from the CLIPBOARD Stack.");
                    }
                    break;
                case "CLEARS":
                    stackTable.get(args.get(1).getValue()).clear();
                    break;

                // QUEUES
                case "ADD":
                    String addStr = (String)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                    queueTable.get(args.get(1).getValue()).add(addStr);
                    System.out.println("Bot >: Successfully added " + addStr + " to the CLIPBOARD Queue.");
                    break;
                case "POLL":
                    tempStr = queueTable.get(args.get(1).getValue()).poll();
                    if(tempStr != null) {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(tempStr), this);
                        System.out.println("Bot >: Successfully polled " + tempStr + " from the CLIPBOARD Queue.");
                    }
                    break;
                case "GLIMPSE":
                    tempStr = queueTable.get(args.get(1).getValue()).peek();
                    if(tempStr != null) {
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(tempStr), this);
                        System.out.println("Bot >: Successfully glimpsed " + tempStr + " from the CLIPBOARD Queue.");
                    }
                    break;
                case "CIRC":
                    tempStr = queueTable.get(args.get(1).getValue()).poll();
                    if(tempStr != null) {
                        System.out.println("Bot >: Successfully retrieved " + tempStr + " from the CLIPBOARD Queue.");
                        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(tempStr), this);
                        queueTable.get(args.get(1).getValue()).add(tempStr);
                        System.out.println("Bot >: Successfully added " + tempStr + " to the CLIPBOARD Queue.");
                    }
                    break;
                case "CLEARQ":
                    queueTable.get(args.get(1).getValue()).clear();
                    break;
                default:
                    throw new Exception("Unreachable SYSTEM command state");
            }

            return true;
        }
        catch (NumberFormatException e) {
            throw new Exception("Bot>: Something happened while executing: " + command.toString() + "\nReason: Unable to convert String to Int for this given variable.");
        }
        catch (Exception e) {
            throw new Exception("Bot>: Something happened while executing: " + command.toString() + "\nReason: " + e.getMessage());
        }
    }

    private boolean executePseudoCommand(Robot robot, Command command) throws Exception{
        try {
            LinkedList<Token> args = command.getKeyCombinations();
            switch (args.get(0).getValue().toUpperCase()) {
                case "TYPE":
                    for (int k = 1; k < args.size(); k++) {
                        char[] chars = args.get(k).getValue().toUpperCase().toCharArray();
                        for (int i = 0; i < chars.length ; i++){
                            robot.keyRelease(executeSingleKey(robot, "" + chars[i]));

                        }
                        //System.out.println("Args size: " + args.size());
                        //System.out.println("K value: " + k);
                        Thread.sleep(50);
                        if((args.size() - 1) > k) robot.keyRelease(executeSingleKey(robot, "SPACE"));

                    }
                    break;
                default:
                    throw new Exception("Unreachable PSEUDO command state");
            }

            return true;
        }
        catch (Exception e) {
            throw new Exception("Bot>: Something happened while executing: " + command.toString() + "\nReason: " + e.getMessage());
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
            throw new Exception("Bot>: Unreachable command type");
        }
    }

    public void run(LinkedList<Command> program) throws Exception {
        Robot autobot = new Robot();
        System.out.println("Bot>: Click on the correct tab so the bot works properly. (Else, results can be disastrous!!!)");
        System.out.println("Bot>: Type \"go\" to initiate me...");
        while(!scio.nextLine().equals("go"));

        for (int i = 10; i > 0 ; i--) {
            System.out.println("Bot>: Press CTRL + C to cancel operation.");
            System.out.println("Bot>: " + i + "second(s) before executing...");
            Thread.sleep(1000);
        }

        System.out.println("Bot>: Starting...");
        for (int k = 0; k < this.repetitions; k++) {
            System.out.println("\nBot>: <ITERATION #" + (k+1) + ">");
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

public class Bot {
    public static void main(String[] args) {
        Automaton automaton = new Automaton(500, 1);

        if(automaton.setupBot()) {
            try {
                automaton.run(automaton.getProgram());
            }
            catch(Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}

