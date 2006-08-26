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
 * $LastChangedBy: keith $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

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
