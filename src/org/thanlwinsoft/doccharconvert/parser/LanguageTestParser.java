package org.thanlwinsoft.doccharconvert.parser;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.DocInterface;
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.xml.XmlWriteFilter;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class LanguageTestParser implements DocInterface
{
    private final static String PARSING_MSG = "Parsing";
    private final static String IDLE_MSG = "Idle";
    private String message = IDLE_MSG;
    private ConversionMode mode = null;
    public LanguageTestParser()
    {
        
    }
    public void abort()
    {
        // not implemented
    }

    public void destroy()
    {
        // nothing to do
    }

    public ConversionMode getMode()
    {
        return mode;
    }

    public String getStatusDesc()
    {
        return message;
    }

    public void initialise() throws InterfaceException
    {
        // nothing needed in this implementation
    }

    public void parse(File input, File output,
            Map<TextStyle, CharConverter> converters, ProgressNotifier notifier) 
    throws FatalException,  InterfaceException, WarningException
    {
        message = PARSING_MSG;
        try
        {
            LanguageTestXmlFilter xmlFilter = new LanguageTestXmlFilter(converters);
            InputStream is = new BufferedInputStream(new FileInputStream(input));
            OutputStream os = new BufferedOutputStream(new FileOutputStream(output));
            XMLReader myReader;
            myReader = XMLReaderFactory.createXMLReader();
            xmlFilter.setParent(myReader);
            XmlWriteFilter writeFilter = new XmlWriteFilter(os);
            writeFilter.setParent(xmlFilter);
            writeFilter.parse(new InputSource(is));
            message = IDLE_MSG;
        } 
        catch (SAXException e)
        {
            message = e.getLocalizedMessage();
            throw new WarningException(e.getMessage());
        } 
        catch (FileNotFoundException e)
        {
            message = e.getLocalizedMessage();
            throw new FatalException(e.getMessage());
        } 
        catch (IOException e)
        {
            message = e.getLocalizedMessage();
            throw new WarningException(e.getMessage());
        }
    }

    public void setInputEncoding(Charset iEnc)
    {
        
    }

    public void setOutputEncoding(Charset oEnc)
    {
        
    }
    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.DocInterface#setMode(org.thanlwinsoft.doccharconvert.ConversionMode)
     */
    public void setMode(ConversionMode mode)
    {
        this.mode = mode;
    }

}
