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

import java.util.Vector;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
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
  * @param oldSide 0=left, 1=right
  * @param syllables Vector of converted Syllables
  * @param debug flag to enable debug logging
 * @return viable syllables
  */
  public Vector <Syllable> checkSyllables(int oldSide, Vector <Syllable> syllables, boolean debug);
  /**
  * Generic initializer that may be used to set initialization variables
  * in the checker if needed.
 * @param scripts 
  * @param args
  * @return true if initialisation was successful
 * @throws CharConverter.FatalException 
  */
  public boolean initialize(Script [] scripts, Object [] args)  
      throws CharConverter.FatalException;
  /**
   * Get the Class types or the arguments to initialize
 * @return types of arguments
   */
  public Class<?> [] getArgumentTypes();
  /**
   * Get argument descriptions to assist entry in a UI
   * @return descriptions of arguments
   */
  public String [] getArgumentDescriptions();
}
