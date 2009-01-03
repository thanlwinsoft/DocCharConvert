/*
Copyright (C) 2006-2007 Keith Stribley http://www.thanlwinsoft.org/

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

import org.thanlwinsoft.util.IClassLoaderUtil;

/**
 * @author keith
 *
 */
public class NoChangeConverter extends ReversibleConverter
{
    private String name = "No Change Converter";
    private String reverseName = name;
    @Override
    public String getBaseName()
    {
        return name;
    }

    @Override
    public void setReverseName(String name)
    {
        this.reverseName = name;
    }

    @Override
    public String convert(String oldText) throws FatalException,
            RecoverableException
    {
        return oldText;
    }

    @Override
    public void destroy()
    {
        // NOOP
    }

    @Override
    public String getName()
    {
        return (isForwards())? name : reverseName;
    }

    @Override
    public void initialize() throws FatalException
    {
        // NOOP
    }

    @Override
    public boolean isInitialized()
    {
        return true;
    }

    @Override
    public void setClassLoader(IClassLoaderUtil loader)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setDebug(boolean on, File logDir)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setEncodings(Charset charset, Charset charset2)
    {
        // TODO Auto-generated method stub

    }

    @Override
    public void setName(String newName)
    {
        this.name = newName;
    }

}
