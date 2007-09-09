/*
 *  Copyright (C) 2005 Keith Stribley <doccharconvert@thanlwinsoft.org>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -----------------------------------------------------------------------
 * $HeadURL: $
 * $LastChangedBy: keith $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package org.thanlwinsoft.doccharconvert.parser;


import java.util.HashSet;
import java.io.IOException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
/**
 *
 * @author  keith
 */
public class TeXParser extends TextParser
{
    private HashSet <String>onCommands;
    private HashSet <String>offCommands;
    private HashSet <String>ignoreLineCommands;
    int lineIndex = 0;
    boolean conversionOn = true; // on by default
    boolean ignoreLine = false;
    StringBuffer inputText;
    StringBuffer convertedLine;
    String currentLine;
    String currentCommand = null;
    int parseLength = 0;
    private final String LINE_END = System.getProperty("line.separator");
    /** Creates a new instance of TeXParser */
    public TeXParser()
    {
    	inputText = new StringBuffer();
        convertedLine = new StringBuffer();
        onCommands = new HashSet<String>();
        offCommands =new HashSet<String>();
        ignoreLineCommands = new HashSet<String>();
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
        int prevLineIndex = 0;
        int commentIndex = line.indexOf('%');
        if (commentIndex < 0)
        {
            parseLength = currentLine.length();
        }
        else
        {
            parseLength = commentIndex;
        }
        try
        {

            while (lineIndex < parseLength)
            {
                String part = getNextPart();
                if (part.length() > 0)
                {
                    if (conversionOn && !ignoreLine)
                    {
                    	if (inputText.length() > 0)
                        	inputText.append(" ");// append a space
                    	inputText.append(part);
                    }
                    else
                    {
                        convertedLine.append(part);
                    }
                }
                
                if (currentCommand != null)
                {
                	if (inputText.length() > 0)
                    {
		            	convertedLine.append(converter.convert(inputText.toString()));
		        		inputText.delete(0,inputText.length());
		        		if (prevLineIndex == 0 && part.length() == 0)
		                {
		        			writer.write(convertedLine.toString());
		                	writer.newLine();
		                	convertedLine.delete(0, convertedLine.length());
		                }
                    }
                	processCommand();
                }
                prevLineIndex = lineIndex;
            }
            // add comment if there is one
            if (parseLength < currentLine.length())
            {
                if (inputText.length() > 0)
                {
                    convertedLine.append(converter.convert(inputText.toString()));
                    inputText.delete(0,inputText.length());
                    if (parseLength == 0)
                        convertedLine.append(LINE_END);
                }
                convertedLine.append(currentLine.substring(parseLength));
                writer.write(convertedLine.toString());
                writer.newLine();
            }
            else
            {
                writer.write(convertedLine.toString());
                if (inputText.length() == 0)
                    writer.newLine();
            }
        }
        catch (CharConverter.RecoverableException e)
        {
            System.out.println(e);
            try
            {
            	writer.write(line);
            	writer.newLine();
            }
            catch (IOException e2)
            {
            	System.out.println(e2.getMessage());
            }
            return line; // output unconverted line
        }
        catch (IOException e)
        {
            System.out.println(e);
            return line; // output unconverted line
        }
        
        return convertedLine.toString();
    }
    private String getNextPart()
    {
        int commandIndex = currentLine.indexOf('\\', lineIndex);
        if (commandIndex > -1 && commandIndex < parseLength)
        {
            int endIndex = commandIndex;
            while (++endIndex < parseLength && 
                (Character.isLetter(currentLine.charAt(endIndex)) ||
                 ((endIndex == commandIndex + 1) && 
                  (currentLine.charAt(endIndex) == '\\'))));
            //currentLine.indexOf(' ', commandIndex);
            if (endIndex < 0) endIndex = parseLength;
            currentCommand = currentLine.substring(commandIndex + 1,endIndex);
            String nextPart = currentLine.substring(lineIndex,commandIndex);
            // update line index
            lineIndex = endIndex;
            return nextPart;
        }
        else
        {
            currentCommand = null;
            String nextPart = currentLine.substring(lineIndex, parseLength);
            lineIndex = parseLength;
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
