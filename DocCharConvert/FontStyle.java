/*
 * FontStyle.java
 *
 * Created on August 25, 2004, 3:30 PM
 */

package DocCharConvert;

/**
 *
 * @author  keith
 */
public class FontStyle implements TextStyle
{
    String fontName = null;
    String styleName = null;
    /** Creates a new instance of FontStyle */
    public FontStyle(String aFontName)
    {
        this.fontName = aFontName;
    }
    
    public boolean equals(Object obj)
    {
        if (obj instanceof TextStyle)
        {
            return fontName.equals(((TextStyle)obj).getFontName());
        }
        return fontName.equals(obj.toString());
    }
    public int hashCode()
    {
        return fontName.hashCode();
    }
    public String getFontName()
    {
        return new String(fontName);
    }
    
    public void setFontName(String aFontName)
    {
        fontName = aFontName;
    }
    
    public String getStyleName()
    {
        return styleName;
    }
    
    public void setStyleName(String newName)
    {
        styleName = newName;
    }
    
    public String getDescription()
    {
        return getFontName();
    }
    
}
