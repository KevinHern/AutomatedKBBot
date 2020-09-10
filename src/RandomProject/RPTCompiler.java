package SPKB;

import jdk.nashorn.internal.parser.TokenType;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.LinkedList;
import java.util.regex.Pattern;

class Token {
    private String value;
    private int lineNumber;
    public enum TypeToken{
        IGNORE,
        CTRL, SHIFT, ALT,
        LETTER, NUMBER, SPACE, ENTER, ESC, PERIOD, COMMA, DELETE,
        PLUS, DASH, SLASH, ASTERISK,
        SCREENCAP,
        HOME, END, PGUP, PGDOWN,
        LARROW, UARROW, RARROW, DARROW,
        WINDOWS, TAB, FUNCTION,
        WAIT, PAUSE, HALT, VAR, ARRAY, ID,
        TINT, TSTRING, INT, STRING, LCBRACKET, RCBRACKET,
        TYPE,
        STACK, QUEUE
    };
    public enum SystemToken{KEYBOARD, SYSTEM, PSEUDO, SYMBOL};
    private TypeToken type;
    private SystemToken systype;

    Token(String value, TypeToken type, SystemToken systype, int lineNumber){
        this.value = value.toUpperCase();
        this.type = type;
        this.systype = systype;
        this.lineNumber = lineNumber;
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

    public int getLineNumber() {
        return lineNumber;
    }
}

class Command {
    private LinkedList<Token> keyCombinations;
    enum CommandType{KEYBOARD, SYSTEM, PSEUDO};
    private CommandType commandType;
    private int lineNumber;

    Command(CommandType type, int lineNumber){
        this.keyCombinations = new LinkedList<Token>();
        this.commandType = type;
        this.lineNumber = lineNumber;
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

    public int getLineNumber() {
        return lineNumber;
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

    // --------------------- LEXER TOKENS

    /* Symbols */
    private Pattern tokenLeftCurlyBracket, tokenRightCurlyBracket;

    /* Keyboard Commands */
    // Atomical Keys
    private Pattern tokenLetter, tokenNumber, tokenSpace, tokenPeriod, tokenComma;

    // Special Atomical Keys
    private Pattern tokenWindows, tokenEnter, tokenTab, tokenEsc, tokenFunction, tokenDelete,
            tokenLArrow, tokenUArrow, tokenRArrow, tokenDArrow,
            tokenPlus, tokenDash, tokenSlash, tokenAsterisk,
            tokenScreenCap,
            tokenHome, tokenEnd, tokenPgUp, tokenPgDown;

    // Combinational Keys
    private Pattern tokenCtrl, tokenShift, tokenAlt;

    /* System Commands */
    private Pattern tokenWait, tokenPause, tokenHalt, tokenId;
    private Pattern tokenVar;
    private Pattern tokenArray;
    private Pattern tokenPush, tokenPop, tokenPeek, tokenClearS;
    private Pattern tokenAdd, tokenPoll, tokenCirculate, tokenGlimpse, tokenClearQ;

    /* Pseudo Commands */
    private Pattern tokenType;

    // Others
    private Pattern tokenInt, tokenString;
    private Pattern tokenTypeInt, tokenTypeString;
    private Pattern tokenClipboard;
    private Pattern singleComment;

    private LinkedList<Token> tokens;

    enum LexerStates{NORMAL, WAIT, VAR, ARRAY, TYPE, QUEUE, STACK, COMMENT};
    private LexerStates currentState;

    RPTCompiler(){
        /* Keyboard Commands Patterns */
        this.tokenLetter = Pattern.compile("[A-Za-z]");
        this.tokenNumber = Pattern.compile("[0-9]");
        this.tokenSpace = Pattern.compile("[Ss][Pp][Aa][Cc][Ee]");

        this.tokenWindows = Pattern.compile("[Ww][Ii][Nn][Dd][Oo][Ww][Ss]");
        this.tokenEnter = Pattern.compile("[Ee][Nn][Tt][Ee][Rr]");
        this.tokenTab = Pattern.compile("[Tt][Aa][Bb]");
        this.tokenEsc = Pattern.compile("[Ee][Ss][Cc]");
        this.tokenPeriod = Pattern.compile("\\.");
        this.tokenComma = Pattern.compile(",");
        this.tokenFunction = Pattern.compile("[Ff][0-9]{1,2}");
        this.tokenLArrow = Pattern.compile("[Ll][Ee][Ff][Tt]");
        this.tokenUArrow = Pattern.compile("[Uu][Pp]");
        this.tokenRArrow = Pattern.compile("[Rr][Ii][Gg][Hh][Tt]");
        this.tokenDArrow = Pattern.compile("[Dd][Oo][Ww][Nn]");
        this.tokenDelete = Pattern.compile("[Dd][Ee][Ll][Ee][Tt][Ee]");

        this.tokenPlus = Pattern.compile("\\+");
        this.tokenDash = Pattern.compile("-");
        this.tokenSlash = Pattern.compile("/");
        this.tokenAsterisk = Pattern.compile("\\*");

        this.tokenScreenCap = Pattern.compile("[Ss][Cc][Rr][Ee][Ee][Nn][Cc][Aa][Pp]");
        this.tokenHome = Pattern.compile("[Hh][Oo][Mm][Ee]");
        this.tokenEnd = Pattern.compile("[Ee][Nn][Dd]");
        this.tokenPgUp = Pattern.compile("[Pp][Gg][Uu][Pp]");
        this.tokenPgDown = Pattern.compile("[Pp][Gg][Dd][Oo][Ww][Nn]");

        this.tokenCtrl = Pattern.compile("[Cc][Tt][Rr][Ll]");
        this.tokenShift = Pattern.compile("[Ss][Hh][Ii][Ff][Tt]");
        this.tokenAlt = Pattern.compile("[Aa][Ll][Tt]");

        /* System Commands Patterns */
        this.tokenWait = Pattern.compile("[Ww][Aa][Ii][Tt]");
        this.tokenPause = Pattern.compile("[Pp][Aa][Uu][Ss][Ee]");
        this.tokenHalt = Pattern.compile("[Hh][Aa][Ll][Tt]");
        this.tokenVar = Pattern.compile("[Vv][Aa][Rr]");
        this.tokenArray = Pattern.compile("[Aa][Rr][Rr][Aa][Yy]");
        this.tokenId = Pattern.compile("[a-z].*");

        // Stack
        this.tokenPush = Pattern.compile("[Pp][Uu][Ss][Hh]");
        this.tokenPop = Pattern.compile("[Pp][Oo][Pp]");
        this.tokenPeek = Pattern.compile("[Pp][Ee][Ee][Kk]");
        this.tokenClearS = Pattern.compile("[Cc][Ll][Ee][Aa][Rr][Ss]");

        // Queue
        this.tokenAdd = Pattern.compile("[Aa][Dd][Dd]");
        this.tokenPoll = Pattern.compile("[Pp][Oo][Ll][Ll]");
        this.tokenCirculate = Pattern.compile("[Cc][Ii][Rr][Cc]");
        this.tokenGlimpse = Pattern.compile("[Gg][Ll][Ii][Mm][Pp][Ss][Ee]");
        this.tokenClearQ = Pattern.compile("[Cc][Ll][Ee][Aa][Rr][Qq]");

        /* Pseudo Commands Patterns */
        this.tokenType = Pattern.compile("[Tt][Yy][Pp][Ee]");

        /* Symbols Patterns */
        this.tokenInt = Pattern.compile("[0-9]+");
        this.tokenString = Pattern.compile(".+");
        this.tokenLeftCurlyBracket = Pattern.compile("\\{");
        this.tokenRightCurlyBracket = Pattern.compile("}");

        /* Others */
        this.spaces = Pattern.compile("[ \n\t]+");
        this.comment = Pattern.compile("COM(.*)\n");
        this.tokenTypeInt = Pattern.compile("[Ii][Nn][Tt]");
        this.tokenTypeString = Pattern.compile("[Ss][Tt][Rr][Ii][Nn][Gg]");
        this.tokenClipboard = Pattern.compile("CLIPBOARD");
        this.singleComment = Pattern.compile("/--");

        //------------------------
        this.tokens = new LinkedList<>();
    }

    // -------------------------------- LEXER FUNCTIONS

    private Token tokenGenerator(String stringPiece, int lineNumber) throws Exception{
        if (currentState.equals(LexerStates.NORMAL)) {      // LEXER STATE = NORMAL
            if(comment.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.IGNORE, Token.SystemToken.KEYBOARD, lineNumber);
            else if(spaces.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.IGNORE, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenCtrl.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.CTRL, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenShift.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.SHIFT, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenAlt.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.ALT, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenLetter.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.LETTER, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenNumber.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.NUMBER, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenSpace.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.SPACE, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenEnter.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.ENTER, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenWindows.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.WINDOWS, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenTab.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.TAB, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenEsc.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.ESC, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenPeriod.matcher(stringPiece).matches()) return new Token("PERIOD", Token.TypeToken.PERIOD, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenComma.matcher(stringPiece).matches()) return new Token("COMMA", Token.TypeToken.COMMA, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenLArrow.matcher(stringPiece).matches()) return new Token("LARROW", Token.TypeToken.LARROW, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenUArrow.matcher(stringPiece).matches()) return new Token("UARROW", Token.TypeToken.UARROW, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenRArrow.matcher(stringPiece).matches()) return new Token("RARROW", Token.TypeToken.RARROW, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenDArrow.matcher(stringPiece).matches()) return new Token("DARROW", Token.TypeToken.DARROW, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenFunction.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.FUNCTION, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenDelete.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.DELETE, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenPlus.matcher(stringPiece).matches()) return new Token("PLUS", Token.TypeToken.PLUS, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenDash.matcher(stringPiece).matches()) return new Token("DASH", Token.TypeToken.DASH, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenSlash.matcher(stringPiece).matches()) return new Token("SLASH", Token.TypeToken.SLASH, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenAsterisk.matcher(stringPiece).matches()) return new Token("ASTERISK", Token.TypeToken.ASTERISK, Token.SystemToken.KEYBOARD, lineNumber);
            // Useful Keys
            else if(tokenScreenCap.matcher(stringPiece).matches()) return new Token("SCREENCAP", Token.TypeToken.SCREENCAP, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenHome.matcher(stringPiece).matches()) return new Token("HOME", Token.TypeToken.HOME, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenEnd.matcher(stringPiece).matches()) return new Token("END", Token.TypeToken.END, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenPgUp.matcher(stringPiece).matches()) return new Token("PGUP", Token.TypeToken.PGUP, Token.SystemToken.KEYBOARD, lineNumber);
            else if(tokenPgDown.matcher(stringPiece).matches()) return new Token("PGDOWN", Token.TypeToken.PGDOWN, Token.SystemToken.KEYBOARD, lineNumber);
                // Stack
            else if(tokenPush.matcher(stringPiece).matches()
                    || tokenPop.matcher(stringPiece).matches()
                    || tokenPeek.matcher(stringPiece).matches()
                    || tokenClearS.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.STACK;
                return new Token(stringPiece, Token.TypeToken.STACK, Token.SystemToken.SYSTEM, lineNumber);
            }
                // QUEUE
            else if(tokenAdd.matcher(stringPiece).matches()
                    || tokenPoll.matcher(stringPiece).matches()
                    || tokenCirculate.matcher(stringPiece).matches()
                    || tokenGlimpse.matcher(stringPiece).matches()
                    || tokenClearQ.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.QUEUE;
                return new Token(stringPiece, Token.TypeToken.QUEUE, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(tokenWait.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.WAIT;
                return new Token(stringPiece, Token.TypeToken.WAIT, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(tokenPause.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.PAUSE, Token.SystemToken.SYSTEM, lineNumber);
            else if(tokenHalt.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.HALT, Token.SystemToken.SYSTEM, lineNumber);
            else if(tokenVar.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.VAR;
                return new Token(stringPiece, Token.TypeToken.VAR, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(tokenVar.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.ARRAY;
                return new Token(stringPiece, Token.TypeToken.ARRAY, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(tokenType.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.TYPE;
                return new Token(stringPiece, Token.TypeToken.TYPE, Token.SystemToken.PSEUDO, lineNumber);
            }
            else if(singleComment.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.COMMENT;
                return new Token("", Token.TypeToken.IGNORE, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(stringPiece.isEmpty()) return new Token("", Token.TypeToken.IGNORE, Token.SystemToken.SYSTEM, lineNumber);
            else {
                System.out.println("Not recognized key: " + stringPiece);
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.WAIT)){     // LEXER STATE = WAIT

            if(tokenInt.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.NORMAL;
                return new Token(stringPiece, Token.TypeToken.INT, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(stringPiece.isEmpty()) return new Token("", Token.TypeToken.IGNORE, Token.SystemToken.SYSTEM, lineNumber);
            else {
                System.out.println("Not recognized key: " + stringPiece);
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.VAR)){     // LEXER STATE = VAR
            if(tokenId.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.ID, Token.SystemToken.SYSTEM, lineNumber);
            else if(tokenLeftCurlyBracket.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.LCBRACKET, Token.SystemToken.SYSTEM, lineNumber);
            else if(tokenTypeInt.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.TINT, Token.SystemToken.SYSTEM, lineNumber);
            else if(tokenTypeString.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.TSTRING, Token.SystemToken.SYSTEM, lineNumber);
            else if(tokenRightCurlyBracket.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.NORMAL;
                return new Token(stringPiece, Token.TypeToken.RCBRACKET, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(tokenString.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.STRING, Token.SystemToken.SYSTEM, lineNumber);
            else {
                System.out.println("Not recognized key: " + stringPiece);
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.ARRAY)){     // LEXER STATE = ARRAY
            if(tokenId.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.ID, Token.SystemToken.SYSTEM, lineNumber);
            else if(tokenLeftCurlyBracket.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.LCBRACKET, Token.SystemToken.SYSTEM, lineNumber);
            else if(tokenRightCurlyBracket.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.NORMAL;
                return new Token(stringPiece, Token.TypeToken.RCBRACKET, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(tokenString.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.STRING, Token.SystemToken.SYSTEM, lineNumber);
            else {
                System.out.println("Not recognized key: " + stringPiece);
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.TYPE)) {    // LEXER STATE = TYPE
            if(tokenLeftCurlyBracket.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.LCBRACKET, Token.SystemToken.PSEUDO, lineNumber);
            else if(tokenRightCurlyBracket.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.NORMAL;
                return new Token(stringPiece, Token.TypeToken.RCBRACKET, Token.SystemToken.PSEUDO, lineNumber);
            }
            else if(tokenString.matcher(stringPiece).matches()) return new Token(stringPiece, Token.TypeToken.STRING, Token.SystemToken.PSEUDO, lineNumber);
            else {
                System.out.println("Not recognized key: " + stringPiece);
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.STACK)) {    // LEXER STATE = STACK
            if(tokenClipboard.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.NORMAL;
                return new Token(stringPiece, Token.TypeToken.STACK, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(stringPiece.isEmpty()) return new Token("", Token.TypeToken.IGNORE, Token.SystemToken.SYSTEM, lineNumber);
            else {
                System.out.println("Not recognized key: " + stringPiece);
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.QUEUE)) {    // LEXER STATE = QUEUE
            if(tokenClipboard.matcher(stringPiece).matches()) {
                this.currentState = LexerStates.NORMAL;
                return new Token(stringPiece, Token.TypeToken.QUEUE, Token.SystemToken.SYSTEM, lineNumber);
            }
            else if(stringPiece.isEmpty()) return new Token("", Token.TypeToken.IGNORE, Token.SystemToken.SYSTEM, lineNumber);
            else {
                System.out.println("Not recognized key: " + stringPiece);
                throw new Exception("RPT Error: Syntax error, unrecognized command.");
            }
        }
        else if(currentState.equals(LexerStates.COMMENT))   // LEXER STATE = COMMENT
            return new Token("", Token.TypeToken.IGNORE, Token.SystemToken.SYSTEM, lineNumber);
        else {
            throw new Exception("RPT Error: Unreachable State");
        }
    }

    private void lexer(String line, LinkedList<Token> tokens, int lineNumber) throws Exception{
        String[] pieces = line.split(" ");
        currentState = LexerStates.NORMAL;
        for (int i = 0; i < pieces.length ; i++) {
            Token temporalToken = tokenGenerator(pieces[i], lineNumber);
            if (!temporalToken.isEqual(Token.TypeToken.IGNORE)) tokens.addLast(temporalToken);
        }
    }

    // ------------------------------- PARSE FUNCTIONS

    private boolean terminal(Command command, LinkedList<Token> tokens){
        Token token = tokens.peek();

        if (token.isEqual(Token.TypeToken.LETTER) || token.isEqual(Token.TypeToken.NUMBER)
                || token.isEqual(Token.TypeToken.SPACE) || token.isEqual(Token.TypeToken.ENTER) || token.isEqual(Token.TypeToken.DELETE)
                || token.isEqual(Token.TypeToken.WINDOWS) || token.isEqual(Token.TypeToken.TAB)
                || token.isEqual(Token.TypeToken.ESC) || token.isEqual(Token.TypeToken.PERIOD)
                || token.isEqual(Token.TypeToken.COMMA) || token.isEqual(Token.TypeToken.FUNCTION)
                || token.isEqual(Token.TypeToken.LARROW) || token.isEqual(Token.TypeToken.UARROW)
                || token.isEqual(Token.TypeToken.RARROW) || token.isEqual(Token.TypeToken.DARROW)
                || token.isEqual(Token.TypeToken.PLUS) || token.isEqual(Token.TypeToken.DASH)
                || token.isEqual(Token.TypeToken.ASTERISK) || token.isEqual(Token.TypeToken.SLASH)
                || token.isEqual(Token.TypeToken.SCREENCAP)
                || token.isEqual(Token.TypeToken.HOME) || token.isEqual(Token.TypeToken.END)
                || token.isEqual(Token.TypeToken.PGUP) || token.isEqual(Token.TypeToken.PGDOWN)){
            command.addKey(tokens.poll());
            return true;
        }
        else {
            return false;
        }
    }

    private boolean keyboard(Command command, LinkedList<Token> tokens){
        Token token = tokens.peek();

        if(token.isEqual(Token.TypeToken.CTRL) || token.isEqual(Token.TypeToken.ALT)
        || token.isEqual(Token.TypeToken.SHIFT)) {
            command.addKey(tokens.poll());
            return keyboard(command, tokens);
        }
        else return terminal(command, tokens);
    }

    private boolean value(Command command, LinkedList<Token> tokens) {
        Token token = tokens.peek();

        if(token.isEqual(Token.TypeToken.STRING) || token.isEqual(Token.TypeToken.INT)) {
            command.addKey(tokens.poll());
            return true;
        }
        else return false;
    }

    private boolean system(Command command, LinkedList<Token> tokens) {
        Token token = tokens.peek();

        if(token.isEqual(Token.TypeToken.WAIT)) {
            command.addKey(tokens.poll());
            if(tokens.peek().isEqual(Token.TypeToken.INT)) {
                command.addKey(tokens.poll());
                return true;
            }
            else return false;
        }
        else if (token.isEqual(Token.TypeToken.VAR)) {
            command.addKey(tokens.poll());
            if(tokens.peek().isEqual(Token.TypeToken.TINT) || tokens.peek().isEqual(Token.TypeToken.TSTRING)) {
                command.addKey(tokens.poll());
                if(tokens.peek().isEqual(Token.TypeToken.ID)) {
                    command.addKey(tokens.poll());
                    return  tokens.poll().isEqual(Token.TypeToken.LCBRACKET)
                            && value(command, tokens)
                            && tokens.poll().isEqual(Token.TypeToken.RCBRACKET);
                }
                else return false;
            }
            else return false;
        }
        else if(token.isEqual(Token.TypeToken.PAUSE) || token.isEqual(Token.TypeToken.HALT)){
            command.addKey(tokens.poll());
            return true;
        }
        else if(token.isEqual(Token.TypeToken.STACK)) {
            command.addKey(tokens.poll());
            if(tokens.peek().isEqual(Token.TypeToken.STACK)) {
                command.addKey(tokens.poll());
                return true;
            }
            else return false;
        }
        else if(token.isEqual(Token.TypeToken.QUEUE)) {
            command.addKey(tokens.poll());
            if(tokens.peek().isEqual(Token.TypeToken.QUEUE)) {
                command.addKey(tokens.poll());
                return true;
            }
            else return false;
        }
        else return false;
    }

    private boolean pseudo(Command command, LinkedList<Token> tokens) {
        Token token = tokens.peek();

        if (token.isEqual(Token.TypeToken.TYPE)) {
            command.addKey(tokens.poll());
            if(tokens.poll().isEqual(Token.TypeToken.LCBRACKET)) {
                while(tokens.peek().isEqual(Token.TypeToken.STRING)) command.addKey(tokens.poll());
                return tokens.poll().isEqual(Token.TypeToken.RCBRACKET);
            }
            else return false;
        }
        else return false;
    }

    private boolean parse(Command command, LinkedList<Token> tokens){
        return keyboard(command, tokens) || system(command, tokens) || pseudo(command, tokens);
    }

    private LinkedList<Command> parser(LinkedList<Token> tokens) throws Exception{
        /*
            RPT GRAMMAR

            Program := [Command]+
            Command := [Keyboard | System | Pseudo]+
            Keyboard :=
                CTRL Keyboard+
                | SHIFT Keyboard+
                | ALT Keyboard+
                | Terminal
            Terminal :=
                Number
                | Letter
                | Space
                | Enter
            System :=
                WAIT Int
                | VAR Value Id { Value }
                | PAUSE
                | HALT
            Pseudo :=
                | TYPE { String }
            Value :=
                String
                | Int
        */

        LinkedList<Command> program = new LinkedList<>();
        while(!tokens.isEmpty()) {

            Command command;
            if (tokens.peek().getSystype().equals(Token.SystemToken.SYSTEM)) {
                command = new Command(Command.CommandType.SYSTEM, tokens.peek().getLineNumber());
            }
            else if(tokens.peek().getSystype().equals(Token.SystemToken.PSEUDO)) {
                command = new Command(Command.CommandType.PSEUDO, tokens.peek().getLineNumber());
            }
            else {
                command = new Command(Command.CommandType.KEYBOARD, tokens.peek().getLineNumber());
            }

            /* Creating Command */
            if (parse(command, tokens)) {
                program.add(command);
            }
            else throw new Exception("RPT Error: Syntax error, bad written command in Line Number " + command.getLineNumber());
        }

        return program;
    }

    public LinkedList<Command> analyse(String fileName) throws Exception{
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String line;
        LinkedList<Token> tokens = new LinkedList<>();
        for(int lineNumber = 1; ((line = br.readLine()) != null) ; lineNumber++) {
            lexer(line, tokens, lineNumber);
        }
        System.out.println("Setup Bot>: # LEXER FINISHED # ");
        LinkedList<Command> program = parser(tokens);
        System.out.println("Setup Bot>: # PARSER FINISHED # ");
        //System.out.println("Null program?" + ((program == null)? "yes" : "no"));
        return program;
    }
}
