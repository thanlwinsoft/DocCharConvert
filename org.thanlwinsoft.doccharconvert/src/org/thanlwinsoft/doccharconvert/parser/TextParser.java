/*
Copyright (C) 2005-2007 Keith Stribley http://www.thanlwinsoft.org/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
------------------------------------------------------------------------------*/

package org.thanlwinsoft.doccharconvert.parser;

import java.io.File;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import java.util.Map;

import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.DocInterface;
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
/**
 *
 * @author  keith
 */
public class TextParser implements org.thanlwinsoft.doccharconvert.DocInterface
{
    protected BufferedReader reader;
    protected BufferedWriter writer;
    protected java.util.Map<TextStyle, CharConverter> converterMap;
    protected org.thanlwinsoft.doccharconvert.converter.CharConverter converter;
    protected Charset inputCharset;
    protected Charset outputCharset;
    protected boolean abort = false;
    private ConversionMode mode = ConversionMode.TEXT_MODE;
    /** Creates a new instance of TextParser */
    public TextParser()
    {
        // default to UTF-8
        inputCharset = Charset.forName("UTF-8");
        outputCharset = Charset.forName("UTF-8");
    }
    
    public void destroy()
    {
    }
    public void setInputEncoding(Charset iEnc)
    {
      inputCharset = iEnc;
    }
    public void setOutputEncoding(Charset oEnc)
    {
      outputCharset = oEnc;
    }
    
    public ConversionMode getMode()
    {
        return mode;
    }
    
    public void initialise()
    {
        
    }
    
    public void parse(File input, File output, 
                      Map<TextStyle, CharConverter> converters, ProgressNotifier notifier) 
        throws FatalException, InterfaceException, WarningException 
    {
        synchronized (this) { abort = false;} 
        try
        {
            reader = new BufferedReader(new InputStreamReader(
                                        new FileInputStream(input),
                                        inputCharset));
            writer = new BufferedWriter(new OutputStreamWriter(
                                        new FileOutputStream(output),
                                        outputCharset));
            this.converterMap = converters;
            if (converterMap.size() != 1)
            {
                System.out.println(converterMap.size() + " CharConverter");
                throw new DocInterface.WarningException("One converter must be specified");
            }
            java.util.Iterator<CharConverter> i = converterMap.values().iterator();
            converter = i.next();

            try
            {
                String line = reader.readLine();
                // remove byte order mark for now since it confuses
                // converters 
                if (line != null && line.length() > 0 && 
                    line.charAt(0) == '\ufeff' &&
                    inputCharset == Charset.forName("UTF-8") )
                {                  
                  line = line.substring(1,line.length());
                  if (outputCharset == Charset.forName("UTF-8"))
                      writer.write('\ufeff');
                  else System.out.println("Removing BOM");
                }
                while (line != null && !notifier.isCancelled())
                {    
                    parseLine(line);
                    line = reader.readLine();
                }
                reader.close();
                writer.close();
            }
            catch (java.io.IOException e)
            {
                System.out.println(e);
                throw new DocInterface.WarningException(e.getLocalizedMessage());
            }
        }
        catch (java.io.FileNotFoundException e)
        {
            throw new DocInterface.WarningException(e.getLocalizedMessage());
        }
    }
    protected String parseLine(String line) 
        throws CharConverter.FatalException, IOException
    {
        String convertedLine;
        try
        {
            convertedLine = converter.convert(line);
        }
        catch (CharConverter.RecoverableException e)
        {
            System.out.println(e);
            convertedLine = line; // output unconverted line
        }
        writer.write(convertedLine);
        writer.newLine();
        return convertedLine;
    }
    public String getStatusDesc()
    {
      return new String("");
    }
    public synchronized void abort()
    {
        abort = true;
    }

    
    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.DocInterface#setMode(org.thanlwinsoft.doccharconvert.ConversionMode)
     */
    public void setMode(ConversionMode mode)
    {
        this.mode = mode;
    }
}
