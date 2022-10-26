package lexerJava;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private String cur_state = "Init";
    private final Scanner _scanner;

    private final static String number = "\\b\\d+|\\b\\d+.\\d+|\\b\\d+e\\d+|0[xX][0-9a-fA-F]+";
    private final static String identifier = "^([a-zA-Z_$])([a-zA-Z_$0-9])*$";
    private final static String punctuator = "([()\\[\\]{},:;.])";
    private final static String operator = "(\\*|/|%|\\+|-|<<|>>|<|>|>=|<=|is|as|==|!=|&|^|\\||&&|\\|\\||\\?\\?|=|\\+=|-=|\\*=|/=|%=|^=|<<=|>>=|\\?\\?=|=>|\\+\\+|--|~|!)";
    private final static String reserved = "abstract|base|bool|break|byte|case|catch|char|checked|class|const|continue|decimal|delegate|do|double|else|enum|event|explicit|extern|finally|fixed|float|for|foreach|goto|if|implicit|in|int|interface|internal|lock|long|namespace|new|object|operator|out|override|params|private|protected|public|readonly|ref|return|sbyte|sealed|short|sizeof|static|string|struct|switch|this|throw|try|typeof|uint|ulong|unchecked|unsafe|ushort|using|virtual|void|volatile|while";
    private final static String annotation = "#nullable|#if|#elif|#else|#endif|#define|#undef|#region|#endregion|#error|#warning|#line|#pragma|#pragma warning|#pragma checksum";
    private final static String literals = "true|false|null|default";
    private final ArrayList<Token> tokens;
    public Lexer(File file) throws FileNotFoundException {
        _scanner = new Scanner(file);
        tokens = new ArrayList<>();
        while (_scanner.hasNextLine()) {
            eatLine(_scanner.nextLine());
        }
    }
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Token token : tokens) {
            builder.append(token.toString());
            builder.append("\n");
        }
        return builder.toString();
    }

    private int matchString(String pattern, String string, int index) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(string);
        if (m.find(index)) {
            return m.end();
        } else
            return -1;
    }

    private void eatLine(String line) {
        int i = 0; //номер символа, який ми зараз читаємо
        String current = "";
        int index = 0;
        while (i < line.length()) {
            switch(cur_state){
                case "Init":
                    if (i<line.length()-1) {
                        if (line.substring(i, i + 2).matches("//")){
                            i = line.length();
                            break;
                        }
                        if (line.substring(i, i + 2).matches("/\\*")) {
                            cur_state = "CM";
                            i +=2;
                            break;
                        }
                    }
                    if (line.substring(i, i + 1).matches("\"")){
                        cur_state="STR";
                        i++;
                        break;
                    }
                    if(line.substring(i, i + 1).matches("'")) {
                        cur_state="CHR";
                        i++;
                        break;
                    }
                    if(line.substring(i, i+1).matches(punctuator)){
                        tokens.add(new Token(line.substring(i, i + 1), TokenType.PUNCTUATOR));
                        i++;
                        break;
                    }
                    if(line.substring(i, i+1).matches(operator)){
                        if(line.substring(i, i+2).matches(operator)){
                            tokens.add(new Token(line.substring(i, i + 2), TokenType.OPERATOR));
                            i+=2;
                            break;

                        }
                        else {
                            tokens.add(new Token(line.substring(i, i + 1), TokenType.OPERATOR));
                            i++;
                            break;

                        }
                    }
                    if(line.substring(i,i+1).matches("#")) {
                        cur_state="AN";
                        break;
                    }

                    if(line.substring(i, i+1).matches("\\s"))
                    {
                        i++;
                        break;
                    }

                    index = i;
                    while (i < line.length() && !line.substring(i, i + 1).matches("\\s") &&
                            !line.substring(i, i + 1).matches(punctuator) && !line.substring(i, i + 1).matches(operator)) {
                        i++;
                    }
                    current = line.substring(index, i);
                    i--;

                    // числа
                    if (current.matches(number)) {
                        tokens.add(new Token(current, TokenType.NUMBER));
                        i++;
                        break;
                    }
                        // зарезервовані слова
                    if (current.matches(reserved)) {
                        tokens.add(new Token(current, TokenType.RESERVED_WORD));
                        i++;
                        break;
                    }
                        // true|false|null
                    if (current.matches(literals)) {
                        tokens.add(new Token(current, TokenType.LITERALS));
                        i++;
                        break;
                    }
                        // ідентифікатори
                    if (current.matches(identifier)){
                        tokens.add(new Token(current, TokenType.IDENTIFIER));
                        i++;
                        break;
                    }
                    // все інше - помилки
                    System.out.println("Error in "+current);
                    break;

                case "CM":
                    index = matchString("\\*/", line, i);
                    //якщо в цій лінії комент закривається
                    if (index != -1){
                        i = index;
                        cur_state="Init";
                    }
                    else {
                        index=i;
                        i = line.length();
                        if(!_scanner.hasNextLine()) {
                            cur_state = "ERR";
                            System.out.println("ERROR: Unclosed comment");
                        }
                    }
                    break;
                case "STR":
                    index =  matchString("\"", line, i);
                    if (index != -1){
                        cur_state="Init";
                        tokens.add(new Token(line.substring(i-1, index), TokenType.STRING_CONSTANT));
                        i = index;
                    }
                    else{
                        i = line.length();
                        if(!_scanner.hasNextLine()) {
                            cur_state = "ERR";
                            System.out.println("ERROR: Unclosed string");
                        }
                    }
                    break;
                case "CHR":
                    if(i<line.length()-1 && line.substring(i+1, i+2).matches("'")){
                        cur_state="Init";
                        tokens.add(new Token(line.substring(i-1, i+2), TokenType.CHAR_CONSTANT));
                        i+=2;
                    }
                    else{
                        cur_state = "ERR";
                        System.out.println("ERROR: It is not a char");
                    }
                    break;
                case "AN":
                    index = matchString(annotation, line, i);
                    if(index != -1){
                        cur_state="Init";
                        tokens.add(new Token(line.substring(i, index), TokenType.ANNOTATION));
                        i=index;
                    }
                    else{
                        cur_state="ERR";
                        System.out.println("ERROR: annotation is not correct");
                    }
            }
        }
    }
}