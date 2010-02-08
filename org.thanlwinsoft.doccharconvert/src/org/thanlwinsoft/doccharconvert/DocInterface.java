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

import java.io.File;
import java.nio.charset.Charset;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
/**
 *
 * @author  keith
 */
public interface DocInterface
{
    /**
     * @throws InterfaceException
     */
    void initialise() throws InterfaceException;
    /**
     * dispose any OS resources
     */
    void destroy();
    /**
     * @param input
     * @param output
     * @param converters
     * @param notifier
     * @throws CharConverter.FatalException
     * @throws InterfaceException
     * @throws WarningException
     */
    void parse(File input, File output, java.util.Map<TextStyle,CharConverter> converters, ProgressNotifier notifier) 
        throws CharConverter.FatalException, InterfaceException,
        WarningException;
    /**
     * @return descriptio of status
     */
    String getStatusDesc();
    /**
     * @author keith
     *
     */
    public class InterfaceException extends Exception
    {
        /**
         * 
         */
        private static final long serialVersionUID = 5240937337192021874L;

        /**
         * 
         * @param msg
         */
        public InterfaceException(String msg)
        {
            super(msg);
        }
        /**
         * Wrap another exception
         * @param e
         */
        public InterfaceException(Exception e)
        {
        	super(e);
        }
    }
    /**
     * 
     * @author keith
     * Warning Exception - non fatal, Conversion can proceed but may be incorrect
     */
    public class WarningException extends Exception
    {
        /**
		 * 
		 */
		private static final long serialVersionUID = 2860752860816168163L;

		/**
		 * @param msg
		 */
		public WarningException(String msg)
        {
            super(msg);
        }
    }
    /**
     * @return mode
     */
    public ConversionMode getMode();
    /**
     * @param mode
     */
    public void setMode(ConversionMode mode);
    
    /**
     * @param iEnc
     */
    public void setInputEncoding(Charset iEnc);
    /**
     * @param oEnc
     */
    public void setOutputEncoding(Charset oEnc);
    /**
     * 
     */
    public void abort();
}
