/*
 * TextParser.java
 *
 * Created on November 18, 2004, 7:18 PM
 */

package DocCharConvert;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.BufferedReader;
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
    /** Creates a new instance of TextParser */
    public TextParser()
    {
        
    }
    
    public void destroy()
    {
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
        try
        {
            reader = new BufferedReader(new FileReader(input));
            writer = new BufferedWriter(new FileWriter(output));
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
}
