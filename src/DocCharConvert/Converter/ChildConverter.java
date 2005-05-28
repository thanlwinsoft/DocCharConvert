/*
 *  Copyright (C) 2005 Keith Stribley <doccharconvert@thanlwinsoft.org>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -----------------------------------------------------------------------
 * $HeadURL: $
 * $LastChangedBy: $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
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
    public void setDebug(boolean on)
    {
        parent.setDebug(on);
    }
}
