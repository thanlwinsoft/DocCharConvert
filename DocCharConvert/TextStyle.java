/*
 * TextStyle.java
 *
 * Created on August 25, 2004, 3:27 PM
 */

package DocCharConvert;

/**
 *
 * @author  keith
 */
public interface TextStyle
{
    public String getDescription();
    public String getFontName();
    public void setFontName(String aFontName);
    public String getStyleName();
    public void setStyleName(String newName);
    public boolean equals(Object obj);
    public int hashCode();
}
