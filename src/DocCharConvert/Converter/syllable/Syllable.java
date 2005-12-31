/*
 * Syllable.java
 *
 * Created on December 31, 2005, 12:04 PM
 */

package DocCharConvert.Converter.syllable;

import java.util.Vector;
import DocCharConvert.Converter.SyllableConverter;
import DocCharConvert.Converter.syllable.Component;
import DocCharConvert.Converter.syllable.Script;

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
  public boolean isKnown() { return known; }
  public int oldLength() { return text.length(); }
  public Integer [] getConversionResult() { return result; }
  public void setConversionResult(Integer [] newResult) { result = newResult; }
  public String getInputString() { return text; }
  public boolean equals(Syllable syl)
  {
      if (syl == null) return false;
      return text.equals(syl.getInputString());
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
     * @param side of conversion LEFT or RIGHT
     * @param integer of refrences indices of component values
     * @result Output string
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
  
  
}
