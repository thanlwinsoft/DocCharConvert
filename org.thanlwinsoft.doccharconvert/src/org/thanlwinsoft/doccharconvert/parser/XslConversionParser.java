/*
Copyright (C) 2006-2007 Keith Stribley http://www.thanlwinsoft.org/

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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.FontStyle;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.RecoverableException;
import org.thanlwinsoft.doccharconvert.eclipse.ExtensionConversionMode;

/**
 * @author keith
 * Conversion parser which is invoked from within an XSL transformation
 */
public class XslConversionParser implements org.thanlwinsoft.doccharconvert.DocInterface
{
    private ConversionMode mMode = null;
    private InputStream mXslt = null;
    private TransformerFactory mTransformerFactory = null;
    private Source mXslSource = null;
    private static XslConversionParser theParser = null;
    private Map<TextStyle, CharConverter> mConverters;

    /**
     * Constructor
     */
    public XslConversionParser()
    {
        
    }
    
    @Override
    public void abort()
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void destroy()
    {
        if (mXslt != null)
        {
            try
            {
                mXslt.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            mXslt = null;
        }
    }

    @Override
    public ConversionMode getMode()
    {
        return mMode;
    }

    @Override
    public String getStatusDesc()
    {
        return MessageUtil.getString("XslConversionParserDesc");
    }

    @Override
    public void initialise() throws InterfaceException
    {
        mTransformerFactory = TransformerFactory.newInstance();
        if (mMode instanceof ExtensionConversionMode)
        {
            ExtensionConversionMode ecm = (ExtensionConversionMode)mMode;
            try
            {
                mXslt = ecm.getPath(ecm.getOptions());
                if (mXslt != null)
                    mXslSource = new StreamSource(mXslt);
            }
            catch (IOException e) 
            { 
                throw new InterfaceException(e.getMessage()); 
            }
        }
    }

    @Override
    public void parse(File input, File output,
            Map<TextStyle, CharConverter> converters, ProgressNotifier notifier)
            throws FatalException, InterfaceException, WarningException
    {
        // assume only one conversion at a time
        if(theParser != null && theParser != this)
            throw new FatalException("Another " + getClass().getCanonicalName() + 
                    " is already in use.");
        theParser = this;
        try
        {
            mConverters = converters;
            Transformer t = mTransformerFactory.newTransformer(mXslSource);
            StreamSource in = new StreamSource(input);
            StreamResult out = new StreamResult(output);
            t.transform(in, out);
            notifier.done();
        }
        catch (TransformerConfigurationException e)
        {
            throw new FatalException(e.getMessage());
        }
        catch (TransformerException e)
        {
            throw new FatalException(e.getMessage());
        }
        finally
        {
            theParser = null;
        }
    }
    /**
     * Convert using first CharConverter
     * @param in
     * @return conversion result
     */
    public static String convert(String in)
    {
        CharConverter cc = theParser.mConverters.values().iterator().next();
        try
        {
            if (cc != null) return cc.convert(in);
        }
        catch (FatalException e)
        {
            e.printStackTrace();
        }
        catch (RecoverableException e)
        {
            e.printStackTrace();
        }
        return in;
    }
    /**
     * Convert using CharConverter matching specified font
     * @param in
     * @param fontName
     * @return converted text
     */
    public static String convert(String in, String fontName)
    {
        CharConverter cc = theParser.mConverters.get(new FontStyle(fontName));
        try
        {
            if (cc != null) return cc.convert(in);
        }
        catch (FatalException e)
        {
            e.printStackTrace();
        }
        catch (RecoverableException e)
        {
            e.printStackTrace();
        }
        return in;
    }

    @Override
    public void setInputEncoding(Charset enc)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMode(ConversionMode mode)
    {
        mMode = mode;
    }

    @Override
    public void setOutputEncoding(Charset enc)
    {
        // TODO Auto-generated method stub
        
    }

}
