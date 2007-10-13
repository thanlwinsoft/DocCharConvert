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

package org.thanlwinsoft.doccharconvert.converter.syllable;

import java.util.Arrays;
import java.util.SortedSet;
import java.util.Vector;
//import java.util.Arrays;
import org.thanlwinsoft.doccharconvert.converter.SyllableConverter;
import org.thanlwinsoft.doccharconvert.converter.syllable.Component;
import org.thanlwinsoft.doccharconvert.converter.syllable.Script;

/**
* Helper class to hold properties of a syllable.
*/
public class Syllable
{
  boolean known = true;
  String text = "";
  Vector <Integer> syllable = null;
  Integer [] result = null;
  Script [] scripts = null;
  private int oldSide = 0;
  private int newSide = 1;
  private Syllable previous = null; // previous syllable
  private SortedSet <Syllable> nextCandidates = null;
  /**
   * Constructor for a matched Syllable
   * @param previous
   * @param scripts
   * @param oldSide
   * @param syllable
   * @param orig
   * @param result
   */
  public Syllable(Syllable previous, Script[] scripts, int oldSide, Vector<Integer> syllable, String orig, Integer [] result) 
  {
      this.previous = previous;
      this.oldSide = oldSide;
      if (this.oldSide == 1) this.newSide = 0;
      this.text = orig;
      this.syllable = syllable;
      this.result = result;
      this.scripts = scripts;
  }
  /**
   * Constructor when the Syllable does not match the tables
   * @param previous - previous Syllable
   * @param unknown - raw contents of the Syllable
   */
  public Syllable (Syllable previous, String unknown)
  {
      this.previous = previous;
      this.text = unknown;
      this.known = false;
      this.syllable = new Vector<Integer>();
  }
  /**
   * Copy constructor
   * @param copy
   */
  public Syllable(Syllable copy)
  {
      this.previous = copy.previous;
    this.known = copy.known;
    this.oldSide = copy.oldSide;
    this.newSide = copy.newSide;
    this.text = new String(copy.text);
    this.syllable = new Vector<Integer>(copy.syllable);
    this.result = Arrays.copyOf(copy.result, copy.result.length);
    this.scripts = copy.scripts;
  }
  public boolean isKnown() { return known; }
  public int originalLength() { return text.length(); }
  public int resultLength() { return getResultString().length(); }
  public Integer [] getConversionResult() { return result; }
  public Integer [] getOriginal() 
  { 
    java.util.List <Integer> subList = syllable.subList(1, syllable.size());
    return subList.toArray(new Integer[syllable.size() - 1]); 
  }
  public void setConversionResult(Integer [] newResult) { result = newResult; }
  public String getOriginalString() { return text; }
  public String getResultString() 
  {
    if (known)
      return dumpSyllable(); 
    return text;
  }
  public boolean equals(Syllable syl)
  {
      if (syl == null) return false;
      if (scripts != null && (scripts[oldSide].ignoreCase() == true))
      {
        return text.equalsIgnoreCase(syl.getOriginalString());
      }
      return text.equals(syl.getOriginalString());
  }
  public boolean isAmbiguous()
  {
      for (int i = 0; i< result.length; i++)
      {
        if (result[i] < 0)
            return true;
      }
      return false;
  }
  /* 
  * Priority = sum over each component of
  * number of char matched in component * priority of component
  */
  public int getPriority()
  {
    int p = 0;
    for (int i = 1; i<syllable.size(); i++)
    {
      if (syllable.get(i) > 0)
      {
        Component c = scripts[oldSide].getSyllableComponent(i - 1);
        String value = c.getComponentValue(syllable.get(i));
        p += c.getPriority() * value.length();
      }
    }
    return p;
  }
  /**
   * Sum of syllable priorities from first syllable to this one
   * @return
   */
  public int sumPriorities()
  {
      return ((previous == null)? 0 : previous.getPriority()) + getPriority();
  }
  
  public String dumpSyllables()
  {
      return ((previous == null)? "" : previous.dumpSyllables()) + dumpSyllable();
  }

  /**
     * Convert the list of reference indices representing the syllable into a
     * human readable string or the output string.
     * @return Output string
     */
  public String dumpSyllable()
  {
      StringBuffer textDump = new StringBuffer();
      if (result == null)
      {
          textDump.append(this.text);
      }
      else
      {
          for (int i = 0; i<result.length; i++)
          {
              Component comp = scripts[newSide].getSyllableComponent(i);
              if (result[i].intValue() < 0) 
                  textDump.append(SyllableConverter.UNKNOWN_CHAR);
              else textDump.append(comp.getComponentValue(result[i]));
          }
      }
      return textDump.toString();
  }
  // These methods won't work if the unknown constructor was used, client code
  // should be able to get the script information from some where else anyway
  //public int getOldSide() { return oldSide; }
  //public int getNewSide() { return newSide; }
  //public Script getOldScript() { return scripts[oldSide]; }
  //public Script getNewScript() { return scripts[newSide]; }
  /**
   * Retreive the previous syllable
   * @return previous syllable or null if start of string
   */
  public Syllable getPrevious()
  {
      return previous;
  }
  /**
   * Set the previous syllable to a different value
   * @param previous Syllable
   */
  public void setPrevious(Syllable p)
  {
      this.previous = p;
  }
  /**
   * Set the possible candidates for the next syllable
   * @param candidates
   */
  public void setNextCandidates(SortedSet <Syllable> candidates)
  {
      this.nextCandidates = candidates;
  }
  /**
   * Get the possible candidates for the next Syllable, most likely first
   * @return set of candidates
   */
  public SortedSet <Syllable> getNextCandidates()
  {
      return nextCandidates;
  }
  /**
   * Index of this syllable from the start
   * @return
   */
  public int getSyllableIndex()
  {
      return (previous == null)? 0 : previous.getSyllableIndex() + 1;
  }
  
  public String toString()
  {
      return syllable.toString() + "orig" + " > " + dumpSyllable(); 
  }
}
