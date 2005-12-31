/*
 * SyllableChecker.java
 *
 * Created on December 31, 2005, 12:13 PM
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
  public Vector <Syllable> checkSyllables(Vector <Syllable> syllables);
}
