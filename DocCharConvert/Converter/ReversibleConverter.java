/*
 * ReversibleConverter.java
 *
 * Created on August 7, 2004, 6:47 PM
 */

package DocCharConvert.Converter;
import DocCharConvert.TextStyle;

/**
 *
 * @author  keith
 */
public abstract class ReversibleConverter implements CharConverter
{
    public void setDirection(boolean isForwards) { this.forwards = isForwards; }
    public boolean isForwards() { return forwards; }
    public void setOriginalStyle(TextStyle aName) { this.originalStyle = aName; }
    public void setTargetStyle(TextStyle aName) { this.targetStyle = aName; }
    public TextStyle getOldStyle() 
    { 
        if(forwards==true) return originalStyle; 
        else return targetStyle; 
    }
    public TextStyle getNewStyle() 
    { 
        if(forwards==true) return targetStyle;
        return originalStyle; 
    }
    protected boolean forwards = true;
    protected  TextStyle originalStyle = null;
    protected  TextStyle targetStyle = null;
}
