/*
 * TeXParser.java
 *
 * Created on November 18, 2004, 9:48 PM
 */

package DocCharConvert;


import java.util.HashSet;
import DocCharConvert.Converter.CharConverter;
/**
 *
 * @author  keith
 */
public class TeXParser extends TextParser
{
    private HashSet onCommands;
    private HashSet offCommands;
    private HashSet ignoreLineCommands;
    int lineIndex = 0;
    boolean conversionOn = true; // on by default
    boolean ignoreLine = false;
    StringBuffer convertedLine;
    String currentLine;
    String currentCommand = null;
    /** Creates a new instance of TeXParser */
    public TeXParser()
    {
        convertedLine = new StringBuffer();
        onCommands = new HashSet();
        offCommands =new HashSet();
        ignoreLineCommands = new HashSet();
        ignoreLineCommands.add(new String("id"));
        onCommands.add(new String("tx"));
        offCommands.add(new String("en"));
    }
    protected String parseLine(String line) 
        throws CharConverter.FatalException
    {
        lineIndex = 0;
        ignoreLine = false;
        currentLine = line;
        currentCommand = null;
        convertedLine.delete(0, convertedLine.length());
        try
        {
            
            while (lineIndex < currentLine.length())
            {
                String part = getNextPart();
                if (part.length() > 0)
                {
                    if (conversionOn && !ignoreLine)
                    {
                        convertedLine.append(converter.convert(part));
                    }
                    else
                    {
                        convertedLine.append(part);
                    }
                }
                processCommand();
            }    
        }
        catch (CharConverter.RecoverableException e)
        {
            System.out.println(e);
            return line; // output unconverted line
        }
        return convertedLine.toString();
    }
    private String getNextPart()
    {
        int commandIndex = currentLine.indexOf('\\', lineIndex);
        if (commandIndex > -1)
        {
            int endIndex = currentLine.indexOf(' ', commandIndex);
            if (endIndex < 0) endIndex = currentLine.length();
            currentCommand = currentLine.substring(commandIndex + 1,endIndex);
            String nextPart = currentLine.substring(lineIndex,commandIndex);
            // update line index
            lineIndex = endIndex;
            return nextPart;
        }
        else
        {
            currentCommand = null;
            String nextPart = currentLine.substring(lineIndex);
            lineIndex = currentLine.length();
            return nextPart;
        }
    }
    private void processCommand()
    {
        if (onCommands.contains(currentCommand))
        {
            conversionOn = true;
        }
        else if (offCommands.contains(currentCommand))
        {
            conversionOn = false;
        }
        else if (ignoreLineCommands.contains(currentCommand))
        {
            ignoreLine = true;
        }
        if (currentCommand != null)
        {
            convertedLine.append('\\');
            convertedLine.append(currentCommand);
        }
    }
    public String getStatusDesc()
    {
      return new String("");
    }
}
