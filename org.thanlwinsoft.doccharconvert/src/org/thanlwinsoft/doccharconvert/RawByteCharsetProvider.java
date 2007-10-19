/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

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
