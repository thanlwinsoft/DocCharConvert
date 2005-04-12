/*
 * TextParser.java
 *
 * Created on November 18, 2004, 7:18 PM
 */

package DocCharConvert;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.BufferedWriter;
import java.io.BufferedReader;
import java.nio.charset.Charset;
import DocCharConvert.Converter.CharConverter;
/**
 *
 * @author  keith
 */
public class TextParser implements DocCharConvert.DocInterface
{
    protected BufferedReader reader;
    protected BufferedWriter writer;
    protected java.util.Map converterMap;
    protected DocCharConvert.Converter.CharConverter converter;
    protected Charset inputCharset;
    protected Charset outputCharset;
    protected boolean abort = false;
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
        return ConversionMode.TEXT_MODE;
    }
    
    public void initialise()
    {
        
    }
    
    public void parse(java.io.File input, java.io.File output, 
                      java.util.Map converters)
               throws DocInterface.WarningException,
                      CharConverter.FatalException
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
            java.util.Iterator i = converterMap.values().iterator();
            converter = (CharConverter)i.next();

            try
            {
                String line = reader.readLine();
                while (line != null)
                {    
                    String convertedLine = parseLine(line);
                    writer.write(convertedLine);
                    writer.newLine();
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
        catch (java.io.IOException e)
        {
            throw new DocInterface.WarningException(e.getLocalizedMessage());
        }
    }
    protected String parseLine(String line) 
        throws CharConverter.FatalException
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
}
