/*
 * CharConverter.java
 *
 * Created on August 6, 2004, 7:10 PM
 */

package DocCharConvert.Converter;

import DocCharConvert.TextStyle;
/**
 *
 * @author  keith
 */
public interface CharConverter
{
    public String getName();
    public void setName(String newName);
    public String convert(String oldText) 
        throws FatalException, RecoverableException;
    
    public boolean isInitialized();
    public TextStyle getOldStyle();
    public TextStyle getNewStyle();
    /**
     * Some converters may need to preinitialise some things
     */
    public void initialize() throws FatalException;
    /**
     * Some converters may need to tidy up after conversion is finished
     * The converter should not be used after this has been called
     */
    public void destroy();
    /**
     * This should be used for unrecoverable errors that prevent
     * the converter being used again.
     */
    public class FatalException extends Exception
    {
        public FatalException(String desc)
        {
            super(desc);
        }
    }
    /**
     * This should be used when the conversion fails, but subsequent 
     * conversions may succeed.
     */
    public class RecoverableException extends Exception
    {
        public RecoverableException(String desc)
        {
            super(desc);
        }
    }
}
