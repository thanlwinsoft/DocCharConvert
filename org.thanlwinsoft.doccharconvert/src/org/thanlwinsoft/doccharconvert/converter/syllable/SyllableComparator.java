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

/**
 *
 * @author keith
 * A Syllable Comparator used to prioritise which match should be used.
 * Compares individual Syllables
 */
public class SyllableComparator implements java.util.Comparator<Syllable>
{
  /**
 * Compares 2 Syllables
 */
public SyllableComparator()
  {

  }

  public int compare(Syllable a, Syllable b)
  {
    int relative = b.originalLength() - a.originalLength();
    if (relative == 0)
    {
      relative = b.getPriority() - a.getPriority();
      if (relative == 0)
        relative = b.getOriginalString().compareTo(b.getOriginalString());
    }
    return relative;
  }
}
