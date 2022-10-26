package lexerJava;

public class Token {

    private final String tokenAttribute;
    private final TokenType tokenType;

    public Token(String tokenAttribute, TokenType tokenType){
        this.tokenAttribute = tokenAttribute;
        this.tokenType = tokenType;
    }

    public String toString(){
        return tokenAttribute + " - " + tokenType;
    }
}