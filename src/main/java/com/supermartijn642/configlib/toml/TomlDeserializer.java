package com.supermartijn642.configlib.toml;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Very primitive implementation of part of the TOML spec
 * <p>
 * Created 09/04/2022 by SuperMartijn642
 */
public class TomlDeserializer {

    public static TomlTable readTomlTable(BufferedReader reader) throws IOException{
        BufferedCharReader charReader = new BufferedCharReader(reader);
        return new TomlDeserializer().readFile(charReader);
    }

    private final TomlTable contents = new TomlTable();
    private String[] currentTable = new String[0];

    private TomlDeserializer(){
    }

    private TomlTable readFile(BufferedCharReader reader) throws IOException{
        while(true){
            // Skip spacing and comments
            this.readUntilNextContent(reader);

            int character = reader.peekChar();

            // Check for end of file
            if(character == -1)
                break;

            // Check for table header
            if(character == '[')
                this.readTableHeader(reader);
            else
                // Read key-value pair
                this.readKeyValuePair(reader);

            // Skip remainder of the line
            while(reader.peekChar() != -1){
                if(reader.peekChar() == ' ')
                    reader.skipChar();
                else if(reader.peekChar() == '\n'){
                    reader.skipChar();
                    break;
                }else if(reader.peekChar() == '#'){
                    reader.skipChar();
                    this.skipLine(reader);
                    break;
                }else
                    throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Encountered unexpected character '" + (char)character + "'!");
            }
        }

        return this.contents;
    }

    private void readTableHeader(BufferedCharReader reader) throws IOException{
        reader.skipChar();
        if(reader.peekChar() == '[')
            throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Table arrays are not yet supported!");

        List<String> key = new LinkedList<>();
        StringBuilder keyBuilder = new StringBuilder();
        while(true){
            int character = reader.peekChar();
            if(character == -1 || character == '\n')
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Missing table header ending ']'!");

            if(character == ' '){
                reader.skipChar();
                continue;
            }

            if(character == ']'){
                if(keyBuilder.length() == 0)
                    throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Invalid key '" + String.join(".", key) + "." + keyBuilder + "' in table header!");

                reader.skipChar();
                break;
            }

            if(character == '\'' || character == '"'){
                keyBuilder.append(this.readString(reader));
                continue;
            }else if(character == '=' || character == '[')
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Encountered unexpected character '" + (char)character + "' in table header!");
            else if(character == '.'){
                if(keyBuilder.length() == 0)
                    throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Encountered unexpected character '" + (char)character + "' in table header!");
                else
                    key.add(keyBuilder.toString());
                keyBuilder = new StringBuilder();
            }else if(Character.toString((char)character).matches("[A-Za-z0-9_-]"))
                keyBuilder.append((char)character);
            else
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Encountered invalid key character '" + (char)character + "' in table header!");

            reader.skipChar();
        }

        // Add the remaining key parts
        key.add(keyBuilder.toString());

        // Set the current table to the found key
        this.currentTable = key.toArray(new String[0]);
    }

    private void readKeyValuePair(BufferedCharReader reader) throws IOException{
        // Read the key
        List<String> key = new LinkedList<>();
        StringBuilder keyBuilder = new StringBuilder();
        while(true){
            int character = reader.peekChar();
            if(character == -1 || character == '\n')
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Invalid statement '" + String.join(".", key) + "." + keyBuilder + "'!");

            if(character == ' '){
                reader.skipChar();
                continue;
            }

            if(character == '='){
                if(keyBuilder.length() == 0)
                    throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Invalid key '" + String.join(".", key) + "." + keyBuilder + "'!");

                reader.skipChar();
                break;
            }

            if(character == '\'' || character == '"'){
                keyBuilder.append(this.readString(reader));
                continue;
            }else if(character == '[' || character == ']')
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Encountered unexpected character '" + (char)character + "' in key!");
            else if(character == '.'){
                if(keyBuilder.length() == 0)
                    throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Encountered unexpected character '" + (char)character + "' in key!");
                else
                    key.add(keyBuilder.toString());
                keyBuilder = new StringBuilder();
            }else if(Character.toString((char)character).matches("[A-Za-z0-9_-]"))
                keyBuilder.append((char)character);
            else
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Encountered invalid key character '" + (char)character + "'!");

            reader.skipChar();
        }

        // Add the remaining key parts
        key.add(keyBuilder.toString());

        // Skip until next content
        int character = reader.peekChar();
        while(true){
            if(character == -1 || character == '\n')
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Missing value for key '" + String.join(".", key) + "'!");
            if(character != ' ')
                break;
            reader.skipChar();
            character = reader.peekChar();
        }

        // Read value
        if(character == '\'' || character == '"'){
            String value = this.readString(reader);
            this.putKeyValue(key.toArray(new String[0]), TomlPrimitive.of(value));
        }else if(character == '+' || character == '-' || Character.isDigit(character)){
            Number number = this.readNumber(reader);
            if(number instanceof Integer)
                this.putKeyValue(key.toArray(new String[0]), TomlPrimitive.of((int)number));
            else
                this.putKeyValue(key.toArray(new String[0]), TomlPrimitive.of((double)number));
        }else if(reader.peekChars(4).equals("true") || reader.peekChars(5).equals("false")){
            boolean value = reader.readChars(4).equals("true");
            if(!value)
                reader.skipChar();
            this.putKeyValue(key.toArray(new String[0]), TomlPrimitive.of(value));
        }else if(character == '[')
            throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Array values are not yet supported!");
        else if(character == '{')
            throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Inline tables are not yet supported!");
        else
            throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Encountered unexpected character '" + (char)character + "' whilst reading value!");
    }

    private String readString(BufferedCharReader reader) throws IOException{
        char quoteChar = (char)reader.readChar();
        boolean isBasic = quoteChar == '"';

        StringBuilder stringBuilder = new StringBuilder();
        while(true){
            int character = reader.peekChar();
            if(character == -1 || character == '\n')
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Missing string ending '" + quoteChar + "'!");

            // End the string
            if(character == quoteChar){
                reader.skipChar();
                break;
            }

            // Check for escape sequence
            if(isBasic && character == '\\'){
                stringBuilder.append((char)this.readEscapeSequence(reader));
                continue;
            }

            // Check for invalid basic string characters
            if(isBasic && (character == '\u0000' || character == '\u0008' || character == '\u001F' || character == '\u007F'))
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Invalid basic string character '" + quoteChar + "'!");

            stringBuilder.append((char)character);
            reader.skipChar();
        }

        return stringBuilder.toString();
    }

    private int readEscapeSequence(BufferedCharReader reader) throws IOException{
        reader.skipChar();

        int character = reader.readChar();

        if(character == -1 || character == '\n')
            throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Unexpected end of line!");

        if(character == 'b')
            return '\b';
        if(character == 't')
            return '\t';
        if(character == 'n')
            return '\n';
        if(character == 'f')
            return '\f';
        if(character == 'r')
            return '\r';
        if(character == 'e')
            return '\u001B';
        if(character == '"')
            return '"';
        if(character == '\\')
            return '\\';
        if(character == 'u')
            return this.readUnicodeChar(reader, 4);
        if(character == 'U')
            return this.readUnicodeChar(reader, 8);

        throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Invalid escape sequence '\\" + (char)character + "'!");
    }

    private int readUnicodeChar(BufferedCharReader reader, int length) throws IOException{
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < length; i++){
            int character = reader.readChar();
            if(character == -1)
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Unexpected end of file!");
            if(Character.isDigit(character) || (character >= 'a' && character <= 'f') || (character >= 'A' && character <= 'F'))
                builder.append((char)character);
            else
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Invalid character '" + (char)character + "' in unicode escape sequence!");
        }

        // Parse the string as base 16
        return Integer.parseInt(builder.toString().toUpperCase(Locale.ROOT), 16);
    }

    private Number readNumber(BufferedCharReader reader) throws IOException{
        StringBuilder numberBuilder = new StringBuilder();

        // Read the entire number
        while(true){
            int character = reader.peekChar();
            if(character == -1 || character == '\n' || character == ' ' || character == '\t')
                break;

            if(character == '_'){
                reader.skipChar();
                continue;
            }

            if(Character.isDigit(character) || (character >= 'a' && character <= 'f') || (character >= 'A' && character <= 'F') || character == 'x' || character == 'o' || character == '-' || character == '+' || character == '.'){
                numberBuilder.append((char)character);
                reader.skipChar();
                continue;
            }

            throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Invalid character '" + (char)character + "' in number!");
        }

        // Check other base notations
        String numberString = numberBuilder.toString();
        if(numberString.startsWith("0x") || numberString.startsWith("0o") || numberString.startsWith("0b")){
            int base = numberString.startsWith("0x") ? 16 : numberString.startsWith("0o") ? 8 : 2;

            try{
                return Integer.parseInt(numberString.substring(2), base);
            }catch(NumberFormatException ignore){
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Not a valid number '" + numberString + "'!");
            }
        }

        // Check if the number is floating-point or integer
        if(numberString.contains(".") || numberString.contains("e") || numberString.contains("E")){
            try{
                return Double.parseDouble(numberString);
            }catch(NumberFormatException ignore){
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Not a valid number '" + numberString + "'!");
            }
        }else{
            try{
                return Integer.parseInt(numberString);
            }catch(NumberFormatException ignore){
                throw new MalformedTomlException("(Line " + reader.getLineIndex() + ":" + reader.getCharIndex() + ") Not a valid number '" + numberString + "'!");
            }
        }
    }

    private void readUntilNextContent(BufferedCharReader reader) throws IOException{
        while(reader.peekChar() != -1){
            if(reader.peekChar() == ' ' || reader.peekChar() == '\n' || reader.peekChar() == '\t')
                reader.skipChar();
            else if(reader.peekChar() == '#'){
                reader.skipChar();
                this.skipLine(reader);
            }else
                break;
        }
    }

    private void skipLine(BufferedCharReader reader) throws IOException{
        int character = reader.readChar();
        while(character != -1 && character != '\n'){
            character = reader.readChar();
        }
    }

    private void putKeyValue(String[] key, TomlElement value) throws MalformedTomlException{
        // Combine current table and key
        String[] path = new String[this.currentTable.length + key.length];
        System.arraycopy(this.currentTable, 0, path, 0, this.currentTable.length);
        System.arraycopy(key, 0, path, this.currentTable.length, key.length);

        // Find/create the correct parent object
        TomlTable object = this.contents;
        for(int i = 0; i < path.length - 1; i++){
            TomlElement member = object.get(path[i]);
            if(member == null){
                TomlTable newObject = new TomlTable();
                object.add(path[i], newObject);
                object = newObject;
            }else if(member.isTable())
                object = member.getAsTable();
            else
                throw new MalformedTomlException("Conflicting values '" + String.join(".", path) + "' and '" + String.join(".", Arrays.copyOfRange(path, 0, i + 1)) + "'!");
        }

        // Get the comment and hint from the old element
        if(object.get(path[path.length - 1]) != null)
            throw new MalformedTomlException("Key defined twice '" + String.join(".", path) + "'!");

        // Assign the actual value
        object.add(path[path.length - 1], value);
    }
}
