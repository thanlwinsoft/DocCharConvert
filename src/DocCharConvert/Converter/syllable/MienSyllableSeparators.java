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

import java.util.Vector;

/**
* An implementation of SyllableChecker to control capitalisation &amp; carets
* between NRM and Thai/Lao scripts for the Mien language.
*/
public class MienSyllableSeparators implements SyllableChecker
{
  MienSyllableSeparators(){}
  // indices of components on NRM side
  static final int CONSONANT = 1;
  static final int VOWEL = 3;
  static final int TONE = 4;  
  static final int FINAL = 5;
/**
  * Checks the specified syllables, assuming that the first syllable 
  * starts a sentence.
  * @param syllables Vector of converted Syllables
  * @param boolean flag to enable debug logging
  * @return corrected Syllable Vector
  */
  public Vector <Syllable> checkSyllables(Vector <Syllable> syllables, 
                                          boolean debug)
  {
    assert(syllables.size() > 0);
    if (syllables.get(0).getNewSide() == 1)
    {
      return insertCaretsAsNeeded(syllables);
    }
    else
    {
      return stripUnwantedCarets(syllables);
    }
  }
  /**
  * Look for capitalized syllables separated by a space.
  * If they meet the criteria for droping the caret in NRM, 
  * then they should probably have one added going to Thai/Lao
  * @param syllables going from Thai/Lao to NRM
  * @return converted syllable
  */
  protected Vector <Syllable> insertCaretsAsNeeded(Vector <Syllable> syllables)
  {
    for (int i = 2; i < syllables.size(); i++)
    {
      if (syllables.get(i).isKnown() && syllables.get(i - 2).isKnown() &&
          ! syllables.get(i - 1).isKnown() && 
          syllables.get(i - 1).getInputString().equals(" "))
      {
        // see if the 2 syllables meet the criteria for not having a caret
        if (nrmNeedsCaret(syllables.get(i).getOldScript(),
            syllables.get(i - 2).getOriginal(),
            syllables.get(i).getOriginal()) == false)
        {
          // May need to insert one, but only if this looks like it is a 
          // in the middle of other capitalised syllables that indicate 
          // a name.
          char l = syllables.get(i - 2).getInputString().charAt(0);
          char r = syllables.get(i).getInputString().charAt(0);
          if (Character.isUpperCase(l) && Character.isUpperCase(r))
          {
            // it shouldn't have a caret in NRM, indeed it doesn't have a caret
            // in NRM, both syllables either side have capitals, so it 
            // should probably be one word in Thai or Lao - insert a caret
            syllables.set(i - 1, new Syllable("^"));
          }
        }
      }
    }
    return syllables;
  }
  /** 
  * Strip unwanted carets and replace with space. 
  * Capitalises multisyllable words.
  * @param syllables going from Thai/Lao to NRM
  * @return converted syllable
  */
  protected Vector <Syllable> stripUnwantedCarets(Vector <Syllable> syllables)
  {
    for (int i = 2; i < syllables.size(); i++)
    {
      if (syllables.get(i).isKnown() && syllables.get(i - 2).isKnown() &&
          ! syllables.get(i - 1).isKnown() && 
          syllables.get(i - 1).getInputString().equals("^"))
      {
        if (nrmNeedsCaret(syllables.get(i).getNewScript(),
            syllables.get(i - 2).getConversionResult(),
            syllables.get(i).getConversionResult()) == false)
        {
          syllables.set(i - 1, new Syllable(" "));
        }
        // either way if it has a caret between it is probably a name
        // and so needs capitalisation
        capitalize(syllables.get(i - 2));
        capitalize(syllables.get(i));
      }
    }
    return syllables;
  }
  
  /** Capitalize first letter of syllable.
  * This could just dump the syllable and convert that way, but
  * this algorithm converts the component only in case the 
  * syllable needs further processing by another checker.
  * @param syllable Syllable
  */
  protected void capitalize(Syllable syllable)
  {
    int first = 0;
    Integer [] result = syllable.getConversionResult();
    // find the first non empty component
    while (result[first] == 0)
    {
      first++;
      assert(first < result.length);
    }
    Component firstComponent =
      syllable.getNewScript().getSyllableComponent(first);
    String oldValue = firstComponent.getComponentValue(result[first]);
    String newValue = null;
    if (oldValue.length() > 1)
    {
      newValue = oldValue.substring(0,1).toUpperCase() +
                oldValue.substring(1);
    }
    else 
    {
      newValue = oldValue.substring(0,1).toUpperCase();
    }
    int newCIndex = firstComponent.getIndex(newValue);
    // the uppercase version may not yet exist
    if (newCIndex == -1)
      newCIndex = firstComponent.addValue(newValue);
    result[first] = newCIndex;
    // store upper case result
    syllable.setConversionResult(result);
  }

  /** Examines a pair of clusters to see whether the caret should be retained.
  * 1. If any two syllable word has a first syllable with a tone marker then 
  * the caret is retained.
  * 2.  Any name where the second syllable of a two syllable name is followed 
  * by a consonant cluster (in this case meaning 2 letters required to make up
  * the one consonant), then the caret is retained.
  * 3. If a vowel ends one syllable and is followed by a syllable containing a
  * vowel as the initial letter of the second syllable then the caret must be
  * retained.
  */
  private boolean nrmNeedsCaret(Script nrmScript, Integer [] a, Integer [] b)
  {
    // case 1
    if (a[TONE] > 0) return true;
    // case 2
    if (b[CONSONANT] > 0)
    {
      Component consComponent = nrmScript.getSyllableComponent(CONSONANT);
      if (consComponent.getComponentValue(b[CONSONANT]).length() > 1)
        return true;
    }
    // case 3
    if (a[FINAL] == 0 && a[TONE] == 0 && b[CONSONANT] == 0 && b[VOWEL] > 0)
      return true;
    return false;
  }
  /** Initialize does nothing in this implementation 
  * @param args ignored
  */
  public boolean initialize(Object [] args) { return true; }
}
