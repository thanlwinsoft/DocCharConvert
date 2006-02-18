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
  public Syllable(Script[] scripts, int oldSide, Vector<Integer> syllable, String orig, Integer [] result) 
  {
      this.oldSide = oldSide;
      if (this.oldSide == 1) this.newSide = 0;
      this.text = orig;
      this.syllable = syllable;
      this.result = result;
      this.scripts = scripts;
  }
  public Syllable (String unknown)
  {
      this.text = unknown;
      this.known = false;
  }
  public Syllable(Syllable copy)
  {
    this.known = copy.known;
    this.oldSide = copy.oldSide;
    this.newSide = copy.newSide;
    this.text = new String(copy.text);
    this.syllable = new Vector<Integer>(copy.syllable);
    this.result = copy.result;
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
     * Convert the list of reference indices representing the syllable into a
     * human readable string or the output string.
     * @return Output string
     */
  public String dumpSyllable()
  {
      StringBuffer text = new StringBuffer();
      for (int i = 0; i<result.length; i++)
      {
          Component comp = scripts[newSide].getSyllableComponent(i);
          if (result[i].intValue() < 0) 
            text.append(SyllableConverter.UNKNOWN_CHAR);
          else text.append(comp.getComponentValue(result[i]));
      }
      return text.toString();
  }
  // These methods won't work if the unknown constructor was used, client code
  // should be able to get the script information from some where else anyway
  //public int getOldSide() { return oldSide; }
  //public int getNewSide() { return newSide; }
  //public Script getOldScript() { return scripts[oldSide]; }
  //public Script getNewScript() { return scripts[newSide]; }

}
