package src.LexicalAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;

public class LexicalAnalyzer implements ILexicalAnalyzer {
    /*
    Goal of this class:
    Read line by line the source code file, and parse tokens from it.


     */

    private final BufferedReader reader;
    private int currentChar;
    private int line=1;

    public LexicalAnalyzer(BufferedReader reader) throws IOException {
        this.reader = reader;
        advance();
    }


    /*
    Logic:
    ID Branch -> we check for ID first. If its not, we then check the integer branch. Otherwise, keep moving forwards.
    When integer is no longer true, we check if fraction is true from that failure point.
    If its true, we keep going until its no longer true. When this happens, we will check for the exponential case.
    If it passes (e+/-) , the integer test must pass.


     */
    @Override
    public Token nextToken() {
        Token token = new Token();

        if(isLetter(currentChar)){
            token = scanNumber();
        }


        return null;
    }

    private void advance() throws IOException {
        currentChar = reader.read();
        if (currentChar == '\n') {
            line++;
        }
    }


    //Fundamental Types.

    private boolean isLetter(int c){
        return Character.isLetter(c);
    }

    private boolean isDigit(int c){
        return Character.isDigit(c);
    }

    private boolean isNonZero(int c){
        return isDigit(c) && c!='0';
    }

    //Core scanners based off of NFAs.

    private Token scanId(){
        while(true){

        }
    }

    private Token scanNumber(){
        return null;
    }

    private Token scanFraction(){
        return null;
    }

    private Token scanExponent(){
        return null;
    }


}