package com.supermartijn642.configlib.toml;

import com.google.common.base.Strings;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Created 09/04/2022 by SuperMartijn642
 */
public class TomlSerializer {

    protected static void writeTomlTable(BufferedWriter write, TomlTable object) throws IOException{
        // First write non-table elements
        for(Map.Entry<String,TomlElement> element : object.entrySet()){
            if(!element.getValue().isTable()){
                // Check if the key contains any spaces
                String elementKey = element.getKey();
                if(elementKey.contains(" "))
                    elementKey = '"' + elementKey + '"';
                // Write the element
                writeElement(write, 0, elementKey, element.getValue());
            }
        }
        // Write tables
        for(Map.Entry<String,TomlElement> element : object.entrySet()){
            if(element.getValue().isTable()){
                // Check if the key contains any spaces
                String elementKey = element.getKey();
                if(elementKey.contains(" "))
                    elementKey = '"' + elementKey + '"';
                // Write the element
                writeElement(write, 0, elementKey, element.getValue());
            }
        }
    }

    private static void writeElement(BufferedWriter writer, int indentation, String key, TomlElement element) throws IOException{
        // Write comments
        if(element.comment != null)
            comment(writer, indentation, element.comment.replace("\n", "\n#"));
        if(element.valueHint != null)
            comment(writer, indentation, element.valueHint);

        // Write per element type
        if(element.isEmpty()){
            // Skip
        }else if(element.isTable())
            writeTable(writer, indentation, key, element.getAsTable());
        else if(element.isInteger())
            writeValue(writer, indentation, key, Integer.toString(element.getAsInteger()));
        else if(element.isDouble())
            writeValue(writer, indentation, key, Double.toString(element.getAsDouble()));
        else if(element.isLong())
            writeValue(writer, indentation, key, Long.toString(element.getAsLong()));
        else if(element.isBoolean())
            writeValue(writer, indentation, key, Boolean.toString(element.getAsBoolean()));
        else if(element.isString())
            writeValue(writer, indentation, key, "\"" + element.getAsString() + "\"");

        // Add a new line for spacing
        if(!element.isTable())
            writer.newLine();
    }

    private static void writeTable(BufferedWriter writer, int indentation, String key, TomlTable object) throws IOException{
        // Write table header
        indentation(writer, indentation);
        writer.append("[").append(key).append("]");
        writer.newLine();

        // Write non-table entries
        for(Map.Entry<String,TomlElement> element : object.entrySet())
            if(!element.getValue().isTable())
                writeElement(writer, indentation + 1, element.getKey(), element.getValue());
        // Write table entries
        for(Map.Entry<String,TomlElement> element : object.entrySet())
            if(element.getValue().isTable()){
                // Check if the key contains any spaces
                String elementKey = element.getKey();
                if(elementKey.contains(" "))
                    elementKey = '"' + elementKey + '"';
                // Write the element
                writeElement(writer, indentation + 1, key + "." + elementKey, element.getValue());
            }
    }

    private static void writeValue(BufferedWriter writer, int indentation, String key, String valueSerialized) throws IOException{
        indentation(writer, indentation);
        writer.write(key);
        writer.write(" = ");
        writer.write(valueSerialized);
        writer.newLine();
    }

    private static void indentation(BufferedWriter writer, int indentation) throws IOException{
        writer.write(Strings.repeat("    ", indentation));
    }

    private static void comment(BufferedWriter writer, int indentation, String comment) throws IOException{
        indentation(writer, indentation);
        writer.write("# ");
        writer.write(comment);
        writer.newLine();
    }
}
