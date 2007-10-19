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

package org.thanlwinsoft.doccharconvert.converter;
import java.io.File;
import java.nio.charset.Charset;

import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.util.IClassLoaderUtil;

/**
 * This is used as a light weight wrapper around another converter
 * It is useful when one type of converter can support many different
 * style mappings.
 * @author  keith
 */
public class ChildConverter implements CharConverter
{
    protected CharConverter parent = null;
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
        if (oldStyle == null || newStyle == null)
            name = parent.getName();
        else
        {
            name = parent.getName() + "(" + oldStyle.getDescription() + " => " + 
                newStyle.getDescription() + ")";
        }
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
        if (parent instanceof ChildConverter)
        {
            return ((ChildConverter)parent).getParent();
        }
        return parent;
    }
    public void setDebug(boolean on, File logDir)
    {
        parent.setDebug(on, logDir);
    }

    public void setEncodings(Charset iCharset, Charset oCharset)
    {
        getParent().setEncodings(iCharset, oCharset);
    }

    public void setClassLoader(IClassLoaderUtil loader)
    {
        // TODO Auto-generated method stub
        
    }
}
