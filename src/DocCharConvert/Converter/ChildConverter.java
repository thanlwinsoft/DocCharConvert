/*
 * ChildConverter.java
 *
 * Created on August 25, 2004, 4:11 PM
 */

package DocCharConvert.Converter;
import DocCharConvert.TextStyle;

/**
 * This is used as a light weight wrapper around another converter
 * It is useful when one type of converter can support many different
 * style mappings.
 * @author  keith
 */
public class ChildConverter implements CharConverter
{
    CharConverter parent = null;
    TextStyle oldStyle = null;
    TextStyle newStyle = null;
    String name = "Unknown";
    /** Creates a new instance of ChildConverter */
    public ChildConverter(TextStyle oldStyle, TextStyle newStyle,
        CharConverter parent)
    {
        this.parent = parent;
        this.oldStyle = oldStyle;
        this.newStyle = newStyle;
        name = parent.getName() + "(" + oldStyle.getDescription() + " => " + 
            newStyle.getDescription() + ")";
    }
    
    public String convert(String oldText) throws CharConverter.FatalException,
        CharConverter.RecoverableException
    {
        return parent.convert(oldText);
    }
    
    public void destroy()
    {
        parent.destroy();
    }
    
    public TextStyle getNewStyle()
    {
        return newStyle;
    }
    
    public TextStyle getOldStyle()
    {
        return oldStyle;
    }
    
    public void initialize() throws CharConverter.FatalException
    {
      if (!parent.isInitialized())
        parent.initialize();
    }
    public String getName()
    {
        return name;
    }
    public void setName(String newName)
    {
        name = newName;
    }
    public String toString()
    {
        return getName();
    }
    public boolean isInitialized()
    {
      return parent.isInitialized();
    }
    public CharConverter getParent()
    {
      return parent;
    }
}
