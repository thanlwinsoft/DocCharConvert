/*
 * SyllableComparator.java
 *
 * Created on December 31, 2005, 1:52 PM
 */

package DocCharConvert.Converter.syllable;

/**
 *
 * @author keith
 * A Syllable Comparator used to prioritise which match should be used.
 */
public class SyllableComparator implements java.util.Comparator<Syllable>
{
  public SyllableComparator()
  {

  }

  public int compare(Syllable a, Syllable b)
  {
    int relative = b.oldLength() - a.oldLength();
    if (relative == 0)
    {
      relative = b.getPriority() - a.getPriority();
      if (relative == 0)
        relative = b.getInputString().compareTo(b.getInputString());
    }
    return relative;
  }
}