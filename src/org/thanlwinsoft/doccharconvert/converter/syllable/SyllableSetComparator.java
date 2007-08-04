/*
 * SyllableComparator.java
 *
 * Created on December 31, 2005, 1:52 PM
 */

package org.thanlwinsoft.doccharconvert.converter.syllable;

/**
 * A Syllable Comparator that compares the complete set of Syllables working 
 * backwards from this one to the first in the string. 
 * It is used to prioritise which match should be used.
 */
public class SyllableSetComparator implements java.util.Comparator<Syllable>
{
    public SyllableSetComparator()
    {
        // NOOP
    }

    public int compare(Syllable a, Syllable b)
    {
        // prefer fewer syllables
        int relative = a.getSyllableIndex() - b.getSyllableIndex();
        if (relative == 0)
        {
            relative = b.sumPriorities() - a.sumPriorities();
            while (relative == 0 && b != null && a != null)
            {
                relative = b.getPriority() - a.getPriority();
                b = b.getPrevious();
                a = a.getPrevious();
            }
        }
        return relative;
    }
}
