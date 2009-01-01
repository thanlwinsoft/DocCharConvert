/**
 * 
 */
package org.thanlwinsoft.doccharconvert.converter.syllable;

/**
 * @author keith
 *
 */
public enum MappingStatus
{
    AMBIGUOUS_FORWARDS(1), AMBIGUOUS_BACKWARDS(2),
    PRIORITY_FORWARDS(4), PRIORITY_BACKWARDS(8);

    int mValue;
    MappingStatus(int value) { mValue = value; }
    public int bit() { return mValue; }
    public boolean isSet(int test) { return (test & mValue) > 0; }
}
