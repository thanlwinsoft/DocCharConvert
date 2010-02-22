package org.thanlwinsoft.doccharconvert.converter.syllable;

import java.util.Iterator;

/**
 * Generic interface used by Component and ComponentRef
 * @author keith
 *
 */
public interface IComponent
{
	/**
	 * Retrieve the index of a given component value
	 * 
	 * @param entry
	 *            value of component
	 * @return internal index reference of the value or -1 if it doesn't exist
	 */
	public int getIndex(String entry);

	/**
	 * Description of component
	 * 
	 * @return description
	 */
	public String getDescription();

	/**
	 * ID of component used in XML
	 * 
	 * @return ID of Component
	 */
	public String getId();

	/**
	 * script of this component
	 * 
	 * @return script
	 */
	public Script getScript();

	/**
	 * Number of values that this component can take
	 * 
	 * @return number of values
	 */
	public int size();

	/**
	 * 
	 * @param id
	 * @return class with given id
	 */
	public ComponentClass getClass(String id);

	/**
	 * 
	 * @return iterator over class ids
	 */
	public Iterator<String> getClassIdIterator();

	/**
	 * 
	 * @return max length of any component
	 */
	public int getMaxLength();

	/**
	 * 
	 * @return priority
	 */
	public int getPriority();
	/**
	 *  Retrieve the actual component object
	 *  @return Component
	 */
	public Component getComponent();
}
