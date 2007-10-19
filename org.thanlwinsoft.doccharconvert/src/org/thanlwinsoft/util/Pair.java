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
 *
 */

public class Pair<TA,TB>
{
    final public TA first;
    final public TB second;
    public Pair(TA a, TB b)
    {
        this.first = a;
        this.second = b;
    }
    public boolean equals(Object o)
    {
        if (o instanceof Pair<?,?>)
        {
            return equals((Pair<?,?>)o);
        }
        return false;
    }
    public boolean equals(Pair<?,?> c)
    {
        if ((c.first == first || (c.first != null && c.first.equals(first))) &&
            (c.second == second || (c.second != null && c.second.equals(second))))
        {
            return true;
        }
        return false;
    }
    public String toString()
    {
        return "(" + first.toString() + " : " + second.toString() + ")";
    }
    public Object get(int i)
    {
        switch (i)
        {
        case 0: return first;
        case 1: return second;
        default: throw new IllegalArgumentException();
        }
    }
}
