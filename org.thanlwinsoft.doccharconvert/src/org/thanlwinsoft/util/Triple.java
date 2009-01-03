/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.util;


/**
 * @author keith
 * Triple to hold 3 parameters
 * @param <TA> 
 * @param <TB> 
 * @param <TC> 
 */
public class Triple<TA,TB,TC>
{
    /**
     * 1st of triple
     */
    final public TA first;
    /**
     * 2nd of triple
     */
    final public TB second;
    /**
     * 3rd of triple
     */
    final public TC third;
    /**
     * 
     * @param a
     * @param b
     * @param c
     */
    public Triple(TA a, TB b, TC c)
    {
        this.first = a;
        this.second = b;
        this.third = c;
    }
    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Triple<?,?,?>)
        {
            return equals((Triple<?,?,?>)o);
        }
        return false;
    }
    /**
     * 
     * @param c
     * @return true if all fields are equal between the 2 triples
     */
    public boolean equals(Triple<?,?,?> c)
    {
        if ((c.first == first || (c.first != null && c.first.equals(first))) &&
            (c.second == second || (c.second != null && c.second.equals(second))) &&
            (c.third == third || (c.third != null && c.third.equals(third))))
        {
            return true;
        }
        return false;
    }
    @Override
    public String toString()
    {
        return "(" + first.toString() + " : " + second.toString() + ")";
    }
    /**
     * 
     * @param i
     * @return value of specified field
     */
    public Object get(int i)
    {
        switch (i)
        {
        case 0: return first;
        case 1: return second;
        case 2: return third;
        default: throw new IllegalArgumentException();
        }
    }
}
