package RandomProject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.regex.Pattern;

class Token {
    private String value;
    public enum TypeToken{CTRL, SHIFT, ALT, LETTER, NUMBER, SPACE, ENTER, IGNORE, WINDOWS, TAB, WAIT, INT, STRING, ESC, TYPE};
    public enum SystemToken{KEYBOARD, SYSTEM, PSEUDO};
    private TypeToken type;
    private SystemToken systype;

    Token(String value, TypeToken type, SystemToken systype){
        this.value = value;
        this.type = type;
        this.systype = systype;
    }

    public String getValue() {
        return value;
    }

    public TypeToken getType() {
        return type;
    }

    public boolean isEqual(TypeToken type){
        return this.type.equals(type);
    }

    public SystemToken getSystype() {
        return systype;
    }
}

class Command {
    private LinkedList<Token> keyCombinations;
    enum CommandType{KEYBOARD, SYSTEM, PSEUDO};
    private CommandType commandType;

    Command(CommandType type){
        this.keyCombinations = new LinkedList<Token>();
        this.commandType = type;
    }

    public void addKey(Token token){
        this.keyCombinations.add(token);
    }

    public LinkedList<Token> getKeyCombinations() {
        return this.keyCombinations;
    }

    public int getCommandSize(){
        return this.keyCombinations.size();
    }

    public CommandType getCommandType() {
        return commandType;
    }

    @Override
    public String toString(){
        String msg = "";
        for (Token token: this.keyCombinations) {
            msg += token.getValue() + " ";
        }
        return msg;
    }
}

public class RPTCompiler {
    private Pattern spaces, comment;
    private Pattern tokenCtrl, tokenShift, tokenAlt, tokenWindows, tokenWait, tokenType;
    private Pattern tokenLetter, tokenNumber, tokenSpace, tokenEnter, tokenTab, tokenInt, tokenString, tokenEsc;
    private LinkedList<Token> tokens;

    enum LexerStates{NORMAL, WAIT, TYPE};
    private LexerStates currentState;

    RPTCompiler(){
        this.spaces = Pattern.compile("[ \n\t]+");
        this.comment = Pattern.compile("COM(.*)\n");
        this.tokenCtrl = Pattern.compile("[Cc][Tt][Rr][Ll]");
        this.tokenShift = Pattern.compile("[Ss][Hh][Ii][Ff][Tt]");
        this.tokenAlt = Pattern.compile("[Aa][Ll][Tt]");
        this.tokenTab = Pattern.compile("[Tt][Aa][Bb]");
        this.tokenLetter = Pattern.compile("[A-Za-z]");
        this.tokenNumber = Pattern.compile("[0-9]");
        this.tokenSpace = Pattern.compile("[Ss][Pp][Aa][Cc][Ee]");
        this.tokenEnter = Pattern.compile("[Ee][Nn][Tt][Ee][Rr]");
        this.tokenWindows = Pattern.compile("[Ww][Ii][Nn][Dd][Oo][Ww][Ss]");
        this.tokenEsc = Pattern.compile("[Ee][Ss][Cc]");
        this.tokenWait = Pattern.compile("[Ww][Aa][Ii][Tt]");
        this.tokenType = Pattern.compile("[Tt][Yy][Pp][Ee]");
        this.tokenInt = Pattern.compile("[0-9]+");
        this.tokenString = Pattern.compile("[A-Za-z]+");
        this.tokens = new LinkedList<Token>();
    }

    private Token tokenGenerator(String stringPiece) throws Exception{
        if (currentState.equals(LexerStates.NORMAL)) {
            if(comment.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.IGNORE, Token.SystemToken.KEYBOARD);
            else if(spaces.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.IGNORE, Token.SystemToken.KEYBOARD);
            else if(tokenCtrl.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.CTRL, Token.SystemToken.KEYBOARD);
            else if(tokenShift.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.SHIFT, Token.SystemToken.KEYBOARD);
            else if(tokenAlt.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.ALT, Token.SystemToken.KEYBOARD);
            else if(tokenLetter.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.LETTER, Token.SystemToken.KEYBOARD);
            else if(tokenNumber.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.NUMBER, Token.SystemToken.KEYBOARD);
            else if(tokenSpace.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.SPACE, Token.SystemToken.KEYBOARD);
            else if(tokenEnter.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.ENTER, Token.SystemToken.KEYBOARD);
            else if(tokenWindows.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.WINDOWS, Token.SystemToken.KEYBOARD);
            else if(tokenTab.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.TAB, Token.SystemToken.KEYBOARD);
            else if(tokenEsc.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.ESC, Token.SystemToken.KEYBOARD);
            else if(tokenWait.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.WAIT;
                return new Token(stringPiece, Token.TypeToken.WAIT, Token.SystemToken.SYSTEM);
            }
            else if(tokenType.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.TYPE;
                return new Token(stringPiece, Token.TypeToken.TYPE, Token.SystemToken.PSEUDO);
            }
            else {
                System.out.println("Not recognized key");
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.WAIT)){
            this.currentState = LexerStates.NORMAL;
            if(tokenInt.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.INT, Token.SystemToken.SYSTEM);
            else {
                System.out.println("Not recognized key");
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.TYPE)) {
            this.currentState = LexerStates.NORMAL;
            if(tokenString.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.STRING, Token.SystemToken.PSEUDO);
            else {
                System.out.println("Not recognized key");
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else {
            throw new Exception("RPT Error: Unreachable State");
        }
    }

    private void lexer(String line, LinkedList<Token> tokens) throws Exception{
        String[] pieces = line.split(" ");
        currentState = LexerStates.NORMAL;
        for (int i = 0; i < pieces.length ; i++) {
            tokens.addLast(tokenGenerator(pieces[i]));
        }
    }

    private LinkedList<Command> parse(LinkedList<Token> tokens) {
        /*
            Grammar:
            Program := [Command]+
            Command :=
                | WAIT int
                | CTRL [Command]*
                | SHIFT [Command]*
                | ALT [Command]*
                | Terminal
            Terminal := Number | Letter | Space | Enter
        */
        LinkedList<Command> program = new LinkedList<Command>();
        while(!tokens.isEmpty()) {

            Command command;
            if (tokens.peek().getSystype().equals(Token.SystemToken.SYSTEM)) {
                command = new Command(Command.CommandType.SYSTEM);
            }
            else if(tokens.peek().getSystype().equals(Token.SystemToken.PSEUDO)) {
                command = new Command(Command.CommandType.PSEUDO);
            }
            else {
                command = new Command(Command.CommandType.KEYBOARD);
            }
            Token token;
            do {
                token = tokens.poll();
                command.addKey(token);
            } while (token.isEqual(Token.TypeToken.CTRL)
                    | token.isEqual(Token.TypeToken.ALT)
                    | token.isEqual(Token.TypeToken.SHIFT)
                    | token.isEqual(Token.TypeToken.WAIT)
                    | token.isEqual(Token.TypeToken.TYPE));
            program.add(command);
        }

        return program;
    }

    public LinkedList<Command> analyse(String fileName) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        LinkedList<Token> tokens = new LinkedList<Token>();
        while ((line = br.readLine()) != null) {
            lexer(line, tokens);
        }
        System.out.println("Setup Bot>: # LEXER FINISHED # ");
        LinkedList<Command> program = parse(tokens);
        System.out.println("Setup Bot>: # PARSER FINISHED # ");
        return program;
    }
}
