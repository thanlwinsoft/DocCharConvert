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
 * $LastChangedBy: $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package DocCharConvert.Converter.syllable;

import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.Iterator;
import DocCharConvert.Converter.syllable.ComponentClass;

/**
 *
 * @author keith
 */
public class Component
{
    private Script script = null; 
    private String id = null;
    private String desc = null;
    private Vector <String> entries = null;
    private LinkedHashMap <String, ComponentClass> classMap = null;
    private int maxLength = 0;
    private int priority = 50;
    /** Creates a new instance of Component
     * @param script that this component refers to
     * @param id of this component used as a reference in the XML file
     * @param desc description of component
     */
    public Component(Script script, String id, String desc)
    {
        this.script = script;
        this.id = id;
        this.desc = desc;
        this.entries = new  Vector<String>();
        entries.add(new  String("")); // first element is always empty
        this.classMap = new LinkedHashMap<String, ComponentClass>();
    }
    /**
    * Retrieve the component value corresponding to a specified index
    * @param index
    * @return value of this component
    */
    public String getComponentValue(int index)
    {
        return entries.elementAt(index);
    }
    /**
    * Retrieve the index of a given component value
    * @param entry value of component
    * @return internal index reference of the value or -1 if it doesn't exist
    */
    public int getIndex(String entry)
    {
        return entries.indexOf(entry);
    }
    /** Description of component
    */
    public String getDescripton()
    {
        return desc;
    }
    /** ID of component used in XML
    */
    public String getId()
    {
        return id;
    }
    /**
    * script of this component 
    */
    public Script getScript()
    {
        return script;
    }
    /**
    * Number of values that this component can take
    */
    public int size()
    {
        return entries.size();
    }
    /** add value and give it a unique id
    * @param value
    * @return reference index of value that will be used internally
    */
    public int addValue(String value)
    {
      if (value.length() > maxLength) maxLength = value.length();
      int index = entries.size();
      entries.add(value);
      return index;
    }
    public ComponentClass getClass(String id)
    {
      return classMap.get(id);
    }
    public void addClass(ComponentClass theClass)
    {
      classMap.put(theClass.getId(), theClass);
    }
    public Iterator <String> getClassIdIterator()
    {
      return classMap.keySet().iterator();
    }
    public int getMaxLength()
    {
        return maxLength;
    }
    public void setPriority(int p)
    {
        priority = p;
    }
    public int getPriority()
    {
        return priority;
    }
}
