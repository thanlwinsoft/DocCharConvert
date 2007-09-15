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

package org.thanlwinsoft.doccharconvert.converter;

import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.util.IClassLoaderUtil;

/**
 * Implements conversion by calling an external program.
 * This will probably be fairly slow.
 * @author  keith
 */
public class ExternalConverter implements CharConverter, Runnable
{
    public final static int USE_STDINOUT = 0;
    public final static int USE_ARGSINOUT = 1;
    private final static String UTF8= "UTF-8";
    private String programPath = "";
    private String arguments = "";
    private int mode = 0;
    private Process process = null;
    private BufferedReader reader = null;
    private BufferedWriter writer = null;
    private boolean addedEol = false;
    private int eolCount = 0;
    private final int SLEEP_TIME = 10;
    private final int MAX_SLEEP_COUNT = 1000; // 10 seconds
    private char [] readBuffer = new char[2048];
    private TextStyle newStyle = null;
    private TextStyle oldStyle = null;
    private File inFile = null;
    private File outFile = null;
    public final String INTAG = "INFILE";
    public final String OUTTAG = "OUTFILE";
    protected String eol = "\n";
    private String name = "";
    private File execDir = null;
    private boolean initialized = false;
    private boolean debug = false;
    private Charset inCharset = Charset.forName(UTF8);
    private Charset outCharset = Charset.forName(UTF8);
    /** Creates a new instance of ExternalConverter 
     * This should be used when wrapped in a ChildConverter     
     */
    public ExternalConverter(String program, String arguments, int mode)
    {
        construct(program,arguments,mode,null,null);
    }
    public ExternalConverter(String program, String arguments, int mode, 
        TextStyle oldStyle, TextStyle newStyle)
    {
        construct(program,arguments, mode, oldStyle, newStyle);
    }
    private void construct(String program, String arguments, int mode, 
        TextStyle oldStyle, TextStyle newStyle)
    {
        if (mode < 0 || mode > USE_ARGSINOUT) 
            throw new java.lang.IllegalArgumentException("Invalid mode " + mode);
        this.execDir = Config.getCurrent().getConverterPath();
        this.mode = mode;
        this.programPath = program;
        this.arguments = arguments;
        this.oldStyle = oldStyle;
        this.newStyle = newStyle;
        this.name = program;
        eol = System.getProperty("line.separator");
    }
    public String getName() { return name; }
    public void setName(String aName) { this.name =aName; }
    public void setEndOfLine(String anEol)
    {
        this.eol = anEol;
    }
    /**
     * converts the input string into the output string
     * If STDIN and STDOUT are being used, then it assumes that the same number
     * of lines will be input as output. If eol is set wrongly, then this may
     * cause problems.
     * Each call to convert will cause at least
     * one newline to be sent to the external program.
     */
    public String convert(String oldText) throws CharConverter.RecoverableException,
        CharConverter.FatalException
    {
        if (oldText.length() == 0) return oldText; // catch zero length case
        String output = null;
        try
        {
            switch (mode)
            {
                case USE_STDINOUT:
                    output = stdInOutConvert(oldText);
                    break;
                case USE_ARGSINOUT:
                    output = fileConvert(oldText);
                    break;
            }
        }
        catch (IOException e)
        {
            throw new CharConverter.FatalException(e.getLocalizedMessage());
        }
        catch (InterruptedException e)
        {
            throw new CharConverter.RecoverableException("Conversion interrupted\n");
        }
        return output;
    }
    
    public void destroy()
    {
        try
        {
            
            if (writer != null) 
            {
                writer.close();
            }
            if (reader != null) reader.close();
            if (process != null) 
            {
                try
                {
                    // closing the reader may allow the process to quite nicely
                    try
                    {
                        Thread.sleep(SLEEP_TIME);
                    }
                    catch (InterruptedException e) {}
                    process.exitValue();
                }
                catch (IllegalThreadStateException e)
                {
                    // need to kill it
                    process.destroy();
                }
            }
            if (inFile != null) inFile.delete();
            if (outFile != null) outFile.delete();
        }
        catch (IOException e)
        {
            // at this stage we aren't very interested in the error since
            // we're finishing up anyway
            System.out.println(e);
        }
        process = null;
        reader = null;
        writer = null;
        inFile = null;
        outFile = null;
    }
    
    public TextStyle getNewStyle()
    {
        return newStyle;
    }
    
    public TextStyle getOldStyle()
    {
        return oldStyle;
    }
    
    public void initialize() throws CharConverter.FatalException
    {
        if (mode == USE_STDINOUT)
        {
            startProgram(arguments);
            initialized = true;
        }
        else
        {
            try
            {
                inFile = File.createTempFile("ExtCharConvert","In.txt");
                outFile = File.createTempFile("ExtCharConvert","Out.txt");
                // out file for java is in file for program and vice versa
                arguments = arguments.replaceAll(INTAG, outFile.getAbsolutePath());
                arguments = arguments.replaceAll(OUTTAG, inFile.getAbsolutePath());
                
                initialized = true;
            }
            catch (IOException e)
            {
                throw new CharConverter.FatalException(e.getLocalizedMessage());
            }
        }
    }
    
    protected void startProgram(String completeArguments)
        throws CharConverter.FatalException
    {
        try
        {
            if (process != null) 
            {
                process.destroy();
                process = null;
            }            
            //System.out.println(programPath + " " + completeArguments);
            
            process = Runtime.getRuntime().exec(programPath + " " + completeArguments,
                null,execDir);
            // note the writer is used to write the input to the external
            // converter, the reader to read the output, hence the slightly
            // non-intuitive choice of character sets
            reader = new BufferedReader(new InputStreamReader
                (process.getInputStream(),outCharset));
            writer = new BufferedWriter(new OutputStreamWriter
                (process.getOutputStream(),inCharset));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new CharConverter.FatalException(e.getLocalizedMessage());
        }
        catch (IOException e)
        {
            throw new CharConverter.FatalException(e.getLocalizedMessage());
        }      
    }
    
    public void run()
    {
        
    }
    
    protected String fileConvert(String oldText) throws CharConverter.FatalException,
        CharConverter.RecoverableException, IOException
    {
        StringBuffer output = new StringBuffer();
        writer = new BufferedWriter(
            new OutputStreamWriter(new FileOutputStream(outFile),inCharset));
        writer.write(oldText);
        writer.flush();
        writer.close();
        startProgram(arguments);
        BufferedReader errorReader = 
            new BufferedReader(new InputStreamReader(process.getErrorStream()));
        boolean exited = false;
        int sleepCount = 0;
        do 
        {
            try
            {
                if (process.exitValue() != 0)
                {
                    reader = new BufferedReader
                        (new InputStreamReader(process.getErrorStream()));
                    String line = null;
                    do 
                    { 
                        line = reader.readLine();
                        if (line != null) 
                        {
                            output.append(line);
                            output.append('\n');
                        }
                    } while (line != null);
                    throw new CharConverter.RecoverableException(output.toString());
                }
                else
                {
                    reader = new BufferedReader(
                        new InputStreamReader(new FileInputStream(inFile),outCharset));
                    int read = 0;
                    while(read >= 0)
                    {
                        read = reader.read(readBuffer,0, readBuffer.length);
                        if (read > 0)
                        {
                            output.append(readBuffer,0,read);
                        }
                    }
                }
                exited = true;
            }
            catch (IllegalThreadStateException e)
            {
                try
                {
                    Thread.sleep(10);
                    if (debug) System.out.println(errorReader.readLine());
                }
                catch (InterruptedException ie) 
                {
                    throw new CharConverter.RecoverableException
                        ("Interrupted while waiting for the external converter");
                }
                if (++sleepCount > MAX_SLEEP_COUNT)
                {
                    throw new CharConverter.RecoverableException
                        ("A timeout occurred waiting for the external converter");
                }
            }
        } while (!exited);
        return stripNulls(output.toString());
    }
    
    protected String stdInOutConvert(String oldText) 
        throws IOException, InterruptedException, 
        CharConverter.RecoverableException
    {
        StringBuffer output = new StringBuffer();
        writer.write(preProcess(oldText));
        //writer.flush();
        int sleepCount = 0;
        while (!reader.ready())
        {
            Thread.sleep(10);
            if (++sleepCount > MAX_SLEEP_COUNT)
            {
                throw new CharConverter.RecoverableException
                    ("A timeout occurred waiting for the external converter");
            }
        }
        do
        {
            int read = reader.read(readBuffer,0, readBuffer.length);
            if (read > 0)
            {
                output.append(readBuffer,0,read);
            }
            else
            {
                Thread.sleep(10);
                if (++sleepCount > MAX_SLEEP_COUNT)
                {
                    throw new CharConverter.RecoverableException
                        ("A timeout occurred waiting for the external converter");
                }
            }
        } while (eolCount > countEols(output.toString()));
        return postProcess(output.toString());
    }
    
    protected String preProcess(String input)
    {
        addedEol = false;
        eolCount = countEols(input);
        if (input.lastIndexOf('\n') == input.length() - 1) return input;
        else
        {
            addedEol = true;
            eolCount++;
            return input + '\n';
        }
    }
    protected String postProcess(String text)
    {
        if (addedEol == true)
        {
            if (text.lastIndexOf(eol) == text.length() - eol.length())
                return text.substring(0,text.length() - eol.length());
        }
        return text;
    }
    protected String stripNulls(String text)
    {
        return text.replaceAll("\0", "");
    }
    protected int countEols(String text)
    {
        int count = 0; 
        int index = text.indexOf(eol);
        while (index >= 0)
        {
            count++;
            index += eol.length();
            index = text.indexOf(eol,index);
        }
        return count;
    }
    public boolean isInitialized()
    {
      return initialized;
    }
    public void setDebug(boolean on, File logDir)
    {
        debug = on;
    }
    public void setEncodings(Charset iCharset, Charset oCharset)
    {
        inCharset = iCharset;
        outCharset = oCharset;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return getName();
    }

    public void setClassLoader(IClassLoaderUtil loader)
    {
        // TODO Auto-generated method stub
        
    }
}
