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

package org.thanlwinsoft.doccharconvert;


import java.util.HashSet;
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
    StringBuffer convertedLine;
    String currentLine;
    String currentCommand = null;
    /** Creates a new instance of TeXParser */
    public TeXParser()
    {
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
            int endIndex = commandIndex;
            while (++endIndex < currentLine.length() && 
                Character.isLetter(currentLine.charAt(endIndex)));
            if (endIndex < currentLine.length() &&
                currentLine.charAt(endIndex) == '\\')
              endIndex++;
            //currentLine.indexOf(' ', commandIndex);
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
