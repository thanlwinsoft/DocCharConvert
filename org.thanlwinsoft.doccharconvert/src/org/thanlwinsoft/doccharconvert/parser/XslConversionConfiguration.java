package org.thanlwinsoft.doccharconvert.parser;

import java.io.InputStream;
import java.util.Map;

import org.thanlwinsoft.doccharconvert.DocInterface.InterfaceException;

/**
 * 
 * Interface to retrieve conversion configuration
 */
public interface XslConversionConfiguration
{
	/**
	 * 
	 * @return xsl file as an input stream
	 * @throws InterfaceException 
	 */
	InputStream xslStream() throws InterfaceException;
	
	/**
	 * 
	 * @return map of parameter names and values
	 */
	Map<String,Object> getParams();
}
