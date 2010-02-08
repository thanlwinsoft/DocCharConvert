package org.thanlwinsoft.doccharconvert.parser;

import org.thanlwinsoft.doccharconvert.DocInterface.InterfaceException;

/**
 * Interface that can be added to a DocInterface to allow configuration.
 * @author keith
 *
 */
public interface ParserConfiguration
{
	/**
	 * 
	 * @param c class to implement
	 * @return
	 */
	Object getAdapter(Class<?> c);
	
	/**
	 * Sets the configuration to an object previously returned by getAdapter()
	 * @param o Object
	 * @throws InterfaceException 
	 */
	void setConfiguration(Object o) throws InterfaceException;
}
