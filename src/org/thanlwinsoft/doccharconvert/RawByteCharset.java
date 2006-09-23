package org.thanlwinsoft.doccharconvert;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;

public class RawByteCharset extends Charset
{
    public static final String CHARSET_NAME = "RawBytes";
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
    public String getName()
    {
        return CHARSET_NAME;
    }
}
