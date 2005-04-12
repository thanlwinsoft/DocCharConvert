/*
 * DocParser.java
 *
 * Created on August 28, 2004, 9:42 AM
 */

package DocCharConvert;

import java.io.File;
import java.nio.charset.Charset;
import DocCharConvert.Converter.CharConverter;
/**
 *
 * @author  keith
 */
public interface DocInterface
{
    void initialise() throws InterfaceException;
    void destroy();
    void parse(File input, File output, java.util.Map<TextStyle,CharConverter> converters) 
        throws CharConverter.FatalException, InterfaceException,
        WarningException;
    String getStatusDesc();
    public class InterfaceException extends Exception
    {
        public InterfaceException(String msg)
        {
            super(msg);
        }
    }
    public class WarningException extends Exception
    {
        public WarningException(String msg)
        {
            super(msg);
        }
    }
    ConversionMode getMode();
    public void setInputEncoding(Charset iEnc);
    public void setOutputEncoding(Charset oEnc);
    public void abort();
}
