/*
 * DummyConverter.java
 *
 * Created on August 6, 2004, 7:30 PM
 */

package DocCharConvert.Converter;
import DocCharConvert.TextStyle;
import DocCharConvert.FontStyle;

/**
 * THIS SHOULD ONLY BE USED FOR TESTING!
 * @author  keith
 */
public class DummyConverter implements CharConverter
{
    TextStyle oldStyle = null;
    TextStyle newStyle = null;
    /** Creates a new instance of DummyConverter */
    public DummyConverter()
    {
        oldStyle = new FontStyle("Padauk Academy");
        newStyle = new FontStyle("Padauk");
    }
    
    public String convert(String oldText)
    {
        return "<" + oldText + ">";
    }
    
    public TextStyle getNewStyle()
    {
        return newStyle;
    }
    
    public TextStyle getOldStyle()
    {
        return oldStyle;
    }
    
    public void destroy()
    {
    }
    
    public void initialize()
    {
    }
    public String getName() { return "DummyConverter"; }
    
    public void setName(String newName)
    {
    }
    
}
