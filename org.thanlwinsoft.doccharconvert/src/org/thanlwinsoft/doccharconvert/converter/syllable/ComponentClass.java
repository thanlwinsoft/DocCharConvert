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

package org.thanlwinsoft.doccharconvert.converter.syllable;



import org.thanlwinsoft.doccharconvert.converter.SyllableConverter;
import java.util.Iterator;
import java.util.Vector;
import java.util.Collection;

/**
 *
 * @author keith
 */
public class ComponentClass
{
    private Component [] components = null;
    private Vector <Integer> leftVector =null;
    private Vector <Integer> rightVector = null;
    private String id = null;
    /** Creates a new instance of ComponentClass */
    public ComponentClass(Component left, Component right, String id)
    {
        components = new Component [2];
        components[SyllableConverter.LEFT] = left;
        components[SyllableConverter.RIGHT] = right;
        leftVector = new Vector<Integer>();
        rightVector = new Vector<Integer>();
        this.id = id;
    }
    public void addAll(Collection <Integer> leftValues, Collection <Integer> rightValues)
    {
      assert(leftValues.size() == rightValues.size());
      leftVector.addAll(leftValues);
      rightVector.addAll(rightValues);
    }
    public void add(int side, int refIndex)
    {
        switch (side)
        {
            case SyllableConverter.LEFT:
                 leftVector.add(new Integer(refIndex));
                 break;
            case SyllableConverter.RIGHT:
                rightVector.add(new Integer(refIndex));
                break;
            default:
                throw new IllegalArgumentException("Invalid side: " + side);
        }        
    }
    public Iterator <Integer> getIterator(int side)
    {
        Iterator <Integer> i = null;
        switch (side)
        {
            case SyllableConverter.LEFT:
                 i = leftVector.iterator();
                 break;
            case SyllableConverter.RIGHT:
                i = rightVector.iterator();
                break;
            default:
                throw new IllegalArgumentException("Invalid side: " + side);
        }
        return i;
    }
    public boolean validate()
    {
        if (leftVector.size() == rightVector.size())
              return true;
        return false;
    }
    public String getId() { return id; }
    
    public Component getComponent(int side)
    {
      assert(side == 0 || side == 1);
      return components[side];
    }
    
    public String toString()
    {
      return "Class: " + id + " (" + leftVector.size() + ")";
    }
    public int getCorrespondingRef(int side, int ref)
    {
        int index = 0;
        if (side == 0) 
        {
            index = leftVector.indexOf(ref);
            if (index == -1) return index;
            return rightVector.elementAt(index);
        }
        else if (side == 1)
        {
            index = rightVector.indexOf(ref);
            if (index == -1) return index;
            return leftVector.elementAt(index);
        }
        else throw new IllegalArgumentException("Invalid side " + side);
        
    }
}
