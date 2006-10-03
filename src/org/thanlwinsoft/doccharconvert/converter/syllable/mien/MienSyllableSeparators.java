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
package org.thanlwinsoft.doccharconvert.converter.syllable.mien;

import java.util.Vector;
import java.util.HashSet;

import org.thanlwinsoft.doccharconvert.converter.syllable.Component;
import org.thanlwinsoft.doccharconvert.converter.syllable.Script;
import org.thanlwinsoft.doccharconvert.converter.syllable.Syllable;
import org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker;

/**
* An implementation of SyllableChecker to control capitalisation &amp; carets
* between NRM and Thai/Lao scripts for the Mien language.
*/
public class MienSyllableSeparators implements SyllableChecker
{
  public MienSyllableSeparators()
  { 
    script = new Script[2]; 
    separators = new String[2];
  }
  private Script[] script = null;
  private int oldSide = 0;
  private int newSide = 1;
  private String [] separators = null;
  private boolean changeAllSeparators = false;
  // indices of components on NRM side
  static final int CONSONANT = 1;
  static final int VOWEL = 3;  
  static final int FINAL = 4;
  static final int TONE = 5;
  
  static private HashSet<String> noCaretSuffixWords = null; 
/**
  * Checks the specified syllables, assuming that the first syllable 
  * starts a sentence.
  * @param syllables Vector of converted Syllables
  * @param boolean flag to enable debug logging
  * @return corrected Syllable Vector
  */
  public Vector <Syllable> checkSyllables(int oldSide, Vector <Syllable> syllables, 
                                          boolean debug)
  {
    this.oldSide = oldSide;
    this.newSide = (oldSide > 0) ? 0 : 1;
    assert(syllables.size() > 0);
    if (newSide == 1)
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
  * @param syllables going from NRM to Thai/Lao
  * @return converted syllable
  */
  protected Vector <Syllable> insertCaretsAsNeeded(Vector <Syllable> syllables)
  {
    for (int i = 1; i < syllables.size(); i++)
    {
      if (i > 1 && syllables.get(i).isKnown() && 
          syllables.get(i - 2).isKnown() &&
          ! syllables.get(i - 1).isKnown() && 
          syllables.get(i - 1).getOriginalString().equals(" "))
      {
        // see if the 2 syllables meet the criteria for not having a caret
        if (nrmNeedsCaret(script[oldSide],
            syllables.get(i - 2).getOriginal(),
            syllables.get(i).getOriginal()) == false)
        {
          // May need to insert one, but only if this looks like it is a 
          // in the middle of other capitalised syllables that indicate 
          // a name.
          char l = syllables.get(i - 2).getOriginalString().charAt(0);
          char r = syllables.get(i).getOriginalString().charAt(0);
          if (Character.isUpperCase(r) && (Character.isUpperCase(l) ||
              ((i - 2 > 0) &&
               syllables.get(i - 3).getOriginalString().equals(separators[oldSide]))))
          {
            // it shouldn't have a caret in NRM, indeed it doesn't have a caret
            // in NRM, both syllables either side have capitals, so it 
            // should probably be one word in Thai or Lao - insert a caret
            if (!noCaretSuffixWords.contains(syllables.get(i).getOriginalString()))
            {
                syllables.set(i - 1, new Syllable(separators[newSide]));
            }
          }
        }
      }
      else if (syllables.get(i).isKnown() && 
               syllables.get(i - 1).isKnown())
      {
        // 2 known syllables must be separated by a caret in Lao
        syllables.add(i, new Syllable(separators[newSide]));
        i++;
      }
      else if (changeAllSeparators && i > 1 && syllables.get(i).isKnown() && 
            syllables.get(i - 2).isKnown() &&
            syllables.get(i - 1).getOriginalString().equals(separators[oldSide]))
      {
    	  syllables.set(i - 1, new Syllable(separators[newSide]));
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
          syllables.get(i - 1).getOriginalString().equals(separators[oldSide]))
      {
        if (nrmNeedsCaret(script[newSide],
            syllables.get(i - 2).getConversionResult(),
            syllables.get(i).getConversionResult()) == false)
        {
          syllables.set(i - 1, new Syllable(" "));
          // capitalize second syllable since it is separated by a space
          capitalize(syllables.get(i));
        }
        else if (!separators[oldSide].equals(separators[newSide]))
        {
          syllables.set(i - 1, new Syllable(separators[newSide]));
        }
        // either way if it has a caret between it is probably a name
        // and so needs capitalisation of the first syllable
        if (i - 2 == 0 || !syllables.get(i - 3).getResultString().equals(separators[newSide]))
        {
          capitalize(syllables.get(i - 2));
        }
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
    Component firstComponent =script[newSide].getSyllableComponent(first);
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
  * @param args separators on left / right sides, optional flag for changing all
  * separators
  */
  public boolean initialize(Script [] scripts, Object [] args) 
  { 
    this.script[0] = scripts[0];
    this.script[1] = scripts[1];
    if (args.length >= 2)
    {
      separators[0] = args[0].toString();
      separators[1] = args[1].toString();
      if (args.length == 3 && args[2].equals("1"))
      {
    	  changeAllSeparators = true;
      }
      noCaretSuffixWords = new HashSet<String>(7);
      // hard coded suffix words!
      noCaretSuffixWords.add("Mungv");
      noCaretSuffixWords.add("Saengv");
      noCaretSuffixWords.add("Mienh");
      noCaretSuffixWords.add("Zingh");
      noCaretSuffixWords.add("Laangz");
      noCaretSuffixWords.add("Guoqv");
      noCaretSuffixWords.add("Deic-Bung");
      
      return true;
    }
    return false; 
  }
}
