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

import java.io.File;
import java.nio.charset.Charset;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
/**
 *
 * @author  keith
 */
public interface DocInterface
{
    void initialise() throws InterfaceException;
    void destroy();
    void parse(File input, File output, java.util.Map<TextStyle,CharConverter> converters) 
        throws CharConverter.FatalException, InterfaceException,
        WarningException;
    String getStatusDesc();
    public class InterfaceException extends Exception
    {
        public InterfaceException(String msg)
        {
            super(msg);
        }
    }
    public class WarningException extends Exception
    {
        public WarningException(String msg)
        {
            super(msg);
        }
    }
    ConversionMode getMode();
    public void setInputEncoding(Charset iEnc);
    public void setOutputEncoding(Charset oEnc);
    public void abort();
}
