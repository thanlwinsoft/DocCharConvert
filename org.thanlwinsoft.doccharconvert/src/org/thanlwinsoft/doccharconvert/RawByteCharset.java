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

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

/**
 * @author keith
 *
 */
public class RawByteCharset extends Charset
{
    /**
     * name
     */
    public static final String CHARSET_NAME = "RawBytes";
    /**
     * 
     */
    public RawByteCharset()
    {
        super(CHARSET_NAME, null);
    }
    @Override
    public boolean contains(Charset cs)
    {
        if (cs instanceof RawByteCharset) return true;
        return false;
    }

    @Override
    public CharsetDecoder newDecoder()
    {
        return new CharsetDecoder(this, 1, 1) {

            @Override
            protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out)
            {
                while (in.remaining() > 0)
                {
                    Byte nextByte = new Byte(in.get());
                    char [] byteAsChar = new char[1];
                    if (nextByte.intValue() < 0)
                        byteAsChar = Character.toChars(nextByte.intValue() + 256);
                    else
                        byteAsChar = Character.toChars(nextByte.intValue());
                    out.put(byteAsChar);
                }
                return CoderResult.UNDERFLOW;
            }
        };
    }

    @Override
    public CharsetEncoder newEncoder()
    {
        
        return new CharsetEncoder(this, 1, 1)
        {

            @Override
            protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out)
            {
                while (in.remaining() > 0)
                {
                    char [] nextByte = {in.get()};
                    if (Character.isHighSurrogate(nextByte[0]) ||
                        Character.isLowSurrogate(nextByte[0]) ||
                        Character.codePointAt(nextByte,0) > 0xFF)
                    {
                        out.put(replacement());
                    }
                    else
                        out.put((byte)Character.codePointAt(nextByte,0));
                }
                return CoderResult.UNDERFLOW;
            }
            
        };
    }
    /**
     * 
     * @return name of Character Set
     */
    public String getName()
    {
        return CHARSET_NAME;
    }
}
