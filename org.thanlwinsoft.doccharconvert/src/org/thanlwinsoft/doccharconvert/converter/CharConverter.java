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

import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.util.IClassLoaderUtil;

import java.io.File;
import java.nio.charset.Charset;
/**
 * Generic interface used by all Character Converters.
 */
public interface CharConverter
{
    /**
     * A name to identify the converter
     * @return
     */
    public String getName();
    /**
     * Set the converters name - called by the 
     * org.thanlwinsoft.doccharconvert.ConverterXmlParser
     * @param newName
     */
    public void setName(String newName);
    /**
     * Convert text using the converter
     * @param oldText original text
     * @return converted text
     */
    public String convert(String oldText) 
        throws FatalException, RecoverableException;
    /**
     * Has initialize() been called and was initialisation successful?
     * @return true if initialised
     */
    public boolean isInitialized();
    /**
     * Enable debugging. If logDir is null, logging can be to System.out. 
     * Otherwise, logging should be to an appropriate file in the log directory.
     * @param on
     * @param logDir 
     */
    public void setDebug(boolean on, File logDir);
    public TextStyle getOldStyle();
    public TextStyle getNewStyle();
    /**
     * Set the input and output encodings.
     * @param iCharset Input Encoding
     * @param oCharset Output Encoding
     */
    public void setEncodings(Charset iCharset, Charset oCharset);
    /**
     * Some converters may need to preinitialise some things
     */
    public void initialize() throws FatalException;
    /**
     * Some converters may need to tidy up after conversion is finished
     * The converter should not be used after this has been called
     */
    public void destroy();
    /**
     * This should be used for unrecoverable errors that prevent
     * the converter being used again.
     */
    public class FatalException extends Exception
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = -2260785362431150554L;

		public FatalException(String desc)
        {
            super(desc);
        }
    }
    /**
     * This should be used when the conversion fails, but subsequent 
     * conversions may succeed.
     */
    public class RecoverableException extends Exception
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = -8169772486169734912L;
		/**
		 * Construct with a description of the error.
		 * @param desc
		 */
		public RecoverableException(String desc)
        {
            super(desc);
        }
    }
    
    public void setClassLoader(IClassLoaderUtil loader);
}
