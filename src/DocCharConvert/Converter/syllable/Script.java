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
import java.util.Iterator;
import java.util.Vector;

/**
 * Class to represent a Script on one side of a mapping.
 * @author keith
 */
public class Script
{
    private String name;
    // need access both by id and by index
    private LinkedHashMap <String, Component> componentMap;
    private Vector <Component> componentVector;
    private boolean repeater = false;
    // repeatChar is the character to designate a repeat if repeater = true
    // otherwise it is the repeat separator
    private String repeatChar = null;
    private boolean caseInsensitive = false;
    /** Creates a new instance of Script 
    * @param name of script
    */
    public Script(String name)
    {
        this.name = name;
        componentMap = new LinkedHashMap<String, Component>();
        componentVector = new Vector<Component>(); 
    }
    /** retreive the specified component object
    * @param id of component referenced in XML
    * @return Component object 
    */
    public Component getSyllableComponent(String id)
    {      
          return componentMap.get(id);
    }
    /**
    * Add a component to this script
    * @param id of component in XML
    * @param component object
    */
    public void addComponent(String id, Component component)
    {
        componentMap.put(id, component);
        componentVector.add(component);
    }
    /**
    * Add a component to this script
    * @param id of component in XML
    * @param component object
    */
    public int getComponentIndex(Component component)
    {
        return componentVector.indexOf(component);
    }
    /**
    * Obtains an interator over the components in this script. 
    * The order returned should be the same in which the components were added.
    */
    public Iterator<Component> getComponentIterator()
    {
        return componentVector.iterator();
    }
    /** name of component 
    * @return description of script
    */
    public String toString()
    {
        return name + " " + componentVector.size() + " components";
    }
    /** name of script */
    public String getName()
    {
        return new String(name);
    }
    /**
    * Retrieve component by index within syllable
    * @param index of component in syllable
    * @return component
    */
    public Component getSyllableComponent(int index)
    {
        return componentVector.elementAt(index);
    }
    public int getNumComponents()
    {
         return componentVector.size();
    }
    public boolean usesRepeater() { return repeater; }
    public String getRepeatChar() { return repeatChar; }
    /** sets the repeater (isRepeater == true) or separator character 
     *  (isRepeater == false)
     * @param isRepeater 
     * @param repeater or separator character
     */
    public void setRepeatChar(boolean isRepeater, String repeat) 
    {
        repeater = isRepeater;
        repeatChar = repeat; 
    } 
    /**
     * Should the conversion convert to lower case before conversion proceeds?
     */
    public boolean ignoreCase()
    {
      return caseInsensitive;
    }
    public void setIgnoreCase(boolean ignore)
    {
      caseInsensitive = ignore;
    }
}
