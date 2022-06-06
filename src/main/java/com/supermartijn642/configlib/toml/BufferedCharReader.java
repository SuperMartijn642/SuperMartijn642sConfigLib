package com.supermartijn642.configlib.toml;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created 20/04/2022 by SuperMartijn642
 */
public class BufferedCharReader {

    private final BufferedReader reader;
    private final List<String> bufferedLines = new LinkedList<>();
    private int lineIndex = 0, charIndex = 0;

    public BufferedCharReader(BufferedReader reader){
        this.reader = reader;
    }

    public int getLineIndex(){
        return this.lineIndex;
    }

    public int getCharIndex(){
        return this.charIndex;
    }

    public int readChar() throws IOException{
        if(this.bufferedLines.isEmpty() && !this.readNextLine())
            return -1;
        if(this.charIndex >= this.bufferedLines.get(0).length()){
            this.discardLine();
            this.charIndex = 0;
            if(this.bufferedLines.isEmpty() && !this.readNextLine())
                return -1;
        }

        return this.bufferedLines.get(0).charAt(this.charIndex++);
    }

    public String readChars(int length) throws IOException{
        if(length < 0)
            throw new IllegalArgumentException("Length must be positive!");

        StringBuilder builder = new StringBuilder();
        while(builder.length() < length){
            int character = this.readChar();
            if(character == -1)
                break;
            builder.append((char)character);
        }

        return builder.toString();
    }

    public int peekChar() throws IOException{
        if(this.bufferedLines.isEmpty() && !this.readNextLine())
            return -1;
        if(this.charIndex >= this.bufferedLines.get(0).length()){
            this.discardLine();
            this.charIndex = 0;
            if(this.bufferedLines.isEmpty() && !this.readNextLine())
                return -1;
        }

        return this.bufferedLines.get(0).charAt(this.charIndex);
    }

    public String peekChars(int length) throws IOException{
        if(length < 0)
            throw new IllegalArgumentException("Length must be positive!");

        StringBuilder builder = new StringBuilder();
        int lineOffset = 0;
        while(builder.length() < length){
            if(this.bufferedLines.size() == lineOffset && !this.readNextLine())
                break;

            String line = this.bufferedLines.get(lineOffset);
            if(lineOffset == 0)
                line = line.substring(this.charIndex);

            if(line.length() < length - builder.length()){
                builder.append(line);
                lineOffset++;
            }else
                builder.append(line, 0, length - builder.length());
        }

        return builder.toString();
    }

    public void skipChars(int length) throws IOException{
        for(int i = 0; i < length; i++){
            if(this.readChar() == -1)
                break;
        }
    }

    public void skipChar() throws IOException{
        this.skipChars(1);
    }

    private boolean readNextLine() throws IOException{
        String line = this.reader.readLine();
        if(line != null){
            this.bufferedLines.add(line + '\n');
            this.lineIndex++;
            return true;
        }
        return false;
    }

    private void discardLine(){
        this.bufferedLines.remove(0);
    }
}
