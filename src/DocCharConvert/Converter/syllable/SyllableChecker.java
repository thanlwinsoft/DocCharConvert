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
 * $LastChangedBy: keith $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package DocCharConvert.Converter.syllable;

import java.util.Vector;
/**
 * Interface to allow Syllables to be tweaked after the main conversion
 * with SyllableConverter. This may be useful for script specific case 
 * conversion or multi-syllable conversion rules.
 * @author keith
 */
public interface SyllableChecker
{
  /**
  * Checks the specified syllables, assuming that the first syllable 
  * starts a sentence.
  * @param syllables Vector of converted Syllables
  * @param boolean flag to enable debug logging
  */
  public Vector <Syllable> checkSyllables(Vector <Syllable> syllables, boolean debug);
  /**
  * Generic initializer that may be used to set initialization variables
  * in the checker if needed.
  * @param args
  * @return true if initialisation was successful
  */
  public boolean initialize(Object [] args);
}
