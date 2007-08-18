/**
 * 
 */
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
