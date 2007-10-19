/*
Copyright (C) 2005-2007 Keith Stribley http://www.thanlwinsoft.org/

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

import org.thanlwinsoft.doccharconvert.opendoc.ScriptType;
import org.thanlwinsoft.doccharconvert.opendoc.ScriptType.Type;

/**
 *
 * @author  keith
 */
public class FontStyle implements TextStyle
{
    String fontName = null;
    String styleName = null;
    ScriptType.Type type = ScriptType.Type.LATIN;
    /** Creates a new instance of FontStyle */
    public FontStyle(String aFontName)
    {
        this.fontName = aFontName;
        this.styleName = aFontName;
    }
    
    public boolean equals(Object obj)
    {
        //System.out.println(this.getFontName() + obj.toString());
        if (obj instanceof TextStyle)
        {
            if (fontName.equals(((TextStyle)obj).getFontName())) 
              return true;
            return false;
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

    public ScriptType.Type getScriptType()
    {
        return type;
    }

    public void setScriptType(Type type)
    {
        this.type = type;
    }
    
}
