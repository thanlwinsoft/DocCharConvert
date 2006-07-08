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

package org.thanlwinsoft.doccharconvert.converter;
import java.nio.charset.Charset;

import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.FontStyle;

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
    public boolean isInitialized() { return true; }
    public void setDebug(boolean on)
    {
    }

    public void setEncodings(Charset iCharset, Charset oCharset)
    {
        // ignore        
    }
}
