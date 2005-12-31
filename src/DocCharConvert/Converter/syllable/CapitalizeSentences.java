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
import java.util.Iterator;
import java.util.HashSet;

/**
 * Capitalises words at the beginning of sentences. 
 * useful for roman scripts converted from an Asian script which
 * doesn't use capitals. This implementation assumes that sentence
 * punctuation is stored as Syllables with Syllable.isKnown() == false
 * and no other characters except the punctuation in the Syllable object.
 * @author keith
 */
public class CapitalizeSentences implements SyllableChecker
{
  private boolean assumeStart = true;
  private boolean lastState = true;
  private HashSet<String> ends;
  /** 
  * Constructor initialises with default English end of sentence markers.
  */
  public CapitalizeSentences()
  {
    ends = new HashSet<String>(3);
    ends.add(new String("."));
    ends.add(new String("?"));
    ends.add(new String("!"));
  }
  /**
  * Change the characters that are assumed to mark the end of a sentence.
  * The original ends are overwritten.
  * @param newEnds HashSet of possible sentence end markers.
  */
  public void setSentenceEnds(HashSet <String> newEnds)
  {
    ends = ends;
  }
  /**
  * Checks the specified syllables, assuming that the first syllable 
  * starts a sentence.
  * @param syllables Vector of converted Syllables
  * @param boolean flag to enable debug logging
  */
  public Vector <Syllable> checkSyllables(Vector <Syllable> syllables, boolean debug)
  {
    boolean isStart = assumeStart;
    Iterator <Syllable> s = syllables.iterator();
    assert (s.hasNext());
    Syllable syllable = syllables.get(0);
    // only process if initial conversion ignored case
    if (syllable.getNewScript().ignoreCase() == false) 
      return syllables;
    while (s.hasNext())
    {
      syllable = s.next();
      if (syllable.isKnown())
      {
        if (isStart)
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
            newValue = oldValue.substring(0,1).toUpperCase() +
                      oldValue.substring(1);
          else 
            newValue = oldValue.substring(0,1).toUpperCase();
          int newCIndex = firstComponent.getIndex(newValue);
          // the uppercase version may not yet exist
          if (newCIndex == -1)
            newCIndex = firstComponent.addValue(newValue);
          result[first] = newCIndex;
          // store upper case result
          syllable.setConversionResult(result);
          isStart = false;
        }
      }
      else if (ends.contains(syllable.getInputString()))
      {
        isStart = true;
      }
    }
    lastState = isStart;
    return syllables;
  }

  /** Set whether the start of a syllable sequence should be interpreted
  * as the start of a sentence. 
  * @param assume true if all Sequences should be interpreted as the 
  * start of a sentence.
  */
  public void setAssumeStart(boolean assume) { assumeStart = assume; }

  /** 
  * A flag indicating whether the last sequence ended with a complete sentence.
  * If sentences are split over multiple calls to SyllableConverter.convert()
  * then you will need to use getLastState() and setAssumeStart() after each 
  * call to get the correct behaviour.
  * @return true if the last Syllable sequence ended with end of sentence
  * punctuation.
  */
  public boolean getLastState() { return lastState; }

  /** Initialize the end of sentence markers.
  * @param args assumed to be an array of strings containing end of 
  * sentence markers (Object.toString() is used to retrieve them)
  * @return true if succeeded
  */
  public boolean initialize(Object args[])
  {
    if (args != null && args.length > 0)
    {
      ends = new HashSet<String>();
      for (int i = 0; i<args.length; i++)
      {
        ends.add(args[i].toString());
      }
    }
    return true;
  }
}
