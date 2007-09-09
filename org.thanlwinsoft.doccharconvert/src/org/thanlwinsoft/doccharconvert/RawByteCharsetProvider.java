package org.thanlwinsoft.doccharconvert;

import java.nio.charset.Charset;
import java.nio.charset.spi.CharsetProvider;
import java.util.Iterator;
import java.util.Vector;

public class RawByteCharsetProvider extends CharsetProvider
{
    Vector <Charset> charsetList = null;
    public RawByteCharsetProvider()
    {
        charsetList = new Vector<Charset> (1);
        charsetList.add(new RawByteCharset());  
    }
    @Override
    public Iterator<Charset> charsets()
    {
        if (charsetList == null)
        {
            return null;
        }
        return charsetList.iterator();
    }

    @Override
    public Charset charsetForName(String charsetName)
    {
        if (charsetName.equalsIgnoreCase(RawByteCharset.CHARSET_NAME))
            return new RawByteCharset();
        return null;
    }        
}
