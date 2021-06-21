package pins.phase.lexan;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


public class LexAn {
    private final BufferedReader reader;
    private final StringBuilder sb = new StringBuilder();
    private final StringBuilder ids = new StringBuilder();

    public int currSymbol = 0;
    private boolean runNextLine = true, readNext = false, wasHere = false;
    private int next = 0;
    private int character = 0;
    private int row = 1;

    public LexAn(String srcFile) throws FileNotFoundException {
        this.reader = new BufferedReader(new FileReader(srcFile));
    }

    public Symbol nextSymbol() {
        while (true) {
            Symbol s = read();
            if (s != null) {
                return s;
            } else if (currSymbol == -1) {
                // EOF
                return new Symbol(0, 0, 0, "EOF", "EOF");
            }
        }
    }

    // 32 = SPACE
    // 13 = CR carriage return (ENTER)
    // 10 = LF - New Line (NL) feed
    // -1 EOF (end of file)
    // 9 = tab
    private Symbol read() {
        try {
            if (readNext) {
                currSymbol = next;
                readNext = false;
            } else {
                currSymbol = reader.read();
                wasHere = false; // this is bad
            }

            if (currSymbol == 10) {
                row += 1;
                character = -1;
                runNextLine = true;
            }

            if (currSymbol == 9) { // tab
                character += 8;
            } else {
                character += 1;
            }

            if (runNextLine) {
                // space, enter, new line, tab, EOF
                if (currSymbol == 32 || currSymbol == 13 || currSymbol == 10 || currSymbol == 9 || currSymbol == -1) {
                }

                // %, &, special chars, same, 58 58 = : ;
                else if (currSymbol == 37 || currSymbol == 38 || currSymbol >= 40 && currSymbol <= 47 ||
                        currSymbol == 58 || currSymbol == 59 || currSymbol >= 91 && currSymbol <= 94 ||
                        currSymbol >= 123 && currSymbol <= 125) { // || currSymbol >= 60 && currSymbol <= 62) {
                    return getSymbol(String.valueOf((char) currSymbol), character, row);
                }

                // <, =, >, !
                else if (currSymbol >= 60 && currSymbol <= 62 || currSymbol == 33) {
                    next = reader.read();
                    readNext = true;
                    if (next >= 60 && next <= 62) {
                        sb.setLength(0);
                        sb.append((char) currSymbol);
                        sb.append((char) next);
                        wasHere = true;
                        return getSymbol(sb.toString(), character - 1, row);
                    } else if (!wasHere) {
                        return getSymbol(String.valueOf((char) currSymbol), character, row);
                    }
                } else if (currSymbol == 39) {
                    sb.setLength(0);
                    sb.append((char) currSymbol);
                    sb.append((char) reader.read());
                    char ch = (char) reader.read();
                    character += 2;
                    if (!String.valueOf(ch).equals("'")) {
                        System.out.println("Error, chars should be 1 long.");
                    } else {
                        sb.append(ch);
                        return getSymbol(sb.toString(), character - 2, row);
                    }
                }
                // comment
                else if (currSymbol == 35) {
                    runNextLine = false;
                } else {
                    ids.append((char) currSymbol);
                    next = reader.read();
                    if (next >= 48 && next <= 57 || next >= 65 && next <= 90 || next >= 97 && next <= 122 || next == 95) {
                        readNext = true;
                    } else {
                        readNext = true;
                        String s = ids.toString();
                        ids.setLength(0);
                        return getSymbol(s, character - s.length() + 1, row);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Symbol getSymbol(String s, int character, int row) {
        // lazy fix about <=, !=, >=
        if (s.length() == 2 && s.contains("=")) {
            if (s.charAt(0) == '<') {
                // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character + 1, row, character + s.length(), s, "Less or equal");
                return new Symbol(row, character + 1, character + s.length(), s, "le");
            } else if (s.charAt(0) == '>') {
                // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character + 1, row, character + s.length(), s, "Greater or equal");
                return new Symbol(row, character + 1, character + s.length(), s, "ge");
            } else if (s.charAt(0) == '!') {
                // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character + 1, row, character + s.length(), s, "Not equal");
                return new Symbol(row, character + 1, character + s.length(), s, "ne");
            } else {
                // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character + 1, row, character + s.length(), s, "Equals");
                return new Symbol(row, character + 1, character + s.length(), s, "eq");
            }
        } else {
            switch (s) {
                // symbols
                case "(":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Open Parentheses");
                    return new Symbol(row, character, character + s.length() - 1, s, "openParent");
                case ")":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Closed Parentheses");
                    return new Symbol(row, character, character + s.length() - 1, s, "closeParent");
                case "[":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Open Brackets");
                    return new Symbol(row, character, character + s.length() - 1, s, "openBracket");
                case "]":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Closed Brackets");
                    return new Symbol(row, character, character + s.length() - 1, s, "closeBracket");
                case "{":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Open Braces");
                    return new Symbol(row, character, character + s.length() - 1, s, "openBrace");
                case "}":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Closed Braces");
                    return new Symbol(row, character, character + s.length() - 1, s, "closeBrace");
                case ",":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Comma");
                    return new Symbol(row, character, character + s.length() - 1, s, "comma");
                case ":":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Colon");
                    return new Symbol(row, character, character + s.length() - 1, s, "colon");
                case ";":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Semicolon");
                    return new Symbol(row, character, character + s.length() - 1, s, "semic");
                case "&":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Ampersand");
                    return new Symbol(row, character, character + s.length() - 1, s, "amper");
                case "|":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Vertical bar");
                    return new Symbol(row, character, character + s.length() - 1, s, "bar");
                case "!":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Exclamation mark");
                    return new Symbol(row, character, character + s.length() - 1, s, "exclamation");
                case "*":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Multiply");
                    return new Symbol(row, character, character + s.length() - 1, s, "mul");
                case "/":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Divide");
                    return new Symbol(row, character, character + s.length() - 1, s, "div");
                case "%":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Percent");
                    return new Symbol(row, character, character + s.length() - 1, s, "percent");
                case "+":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Add");
                    return new Symbol(row, character, character + s.length() - 1, s, "add");
                case "-":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Sub");
                    return new Symbol(row, character, character + s.length() - 1, s, "sub");
                case "^":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Hat");
                    return new Symbol(row, character, character + s.length() - 1, s, "hat");
                case "=":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Equals");
                    return new Symbol(row, character, character + s.length() - 1, s, "assign");
                case "<":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Less than");
                    return new Symbol(row, character, character + s.length() - 1, s, "lt");
                case ">":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Greater than");
                    return new Symbol(row, character, character + s.length() - 1, s, "gt");

                // keywords
                case "char":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword char");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyChar");
                case "del":
                    //  System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword del");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyDel");
                case "do":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword do");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyDo");
                case "else":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword else");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyElse");
                case "end":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword end");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyEnd");
                case "fun":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword fun");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyFun");
                case "if":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword if");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyIf");
                case "int":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword int");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyInt");
                case "new":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword new");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyNew");


                case "then":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword then");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyThen");
                case "typ":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword typ");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyTyp");
                case "var":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword var");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyVar");
                case "void":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword void");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyVoid");
                case "where":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword where");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyWhere");
                case "while":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Keyword while");
                    return new Symbol(row, character, character + s.length() - 1, s, "keyWhile");

                // consts
                case "none":
                    // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Const none");
                    return new Symbol(row, character, character + s.length() - 1, s, "constNone");

                case "nil":
                    //  System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Const nil");
                    return new Symbol(row, character, character + s.length() - 1, s, "constNil");

                default:
                    // numbers
                    if (s.matches("^[0-9]*$")) {
                        // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Const int");
                        return new Symbol(row, character, character + s.length() - 1, s, "constInt");
                    }
                    // chars
                    else if (s.length() == 3 && s.contains("'")) {
                        // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Const char");
                        return new Symbol(row, character, character + s.length() - 1, s, "constChar");
                    }
                    // names
                    else {
                        // System.out.printf("%d:%d - %d:%d : %s : %s\n", row, character, row, character + s.length() - 1, s, "Variable name");
                        return new Symbol(row, character, character + s.length() - 1, s, "id");
                    }
            }
        }
    }
}
