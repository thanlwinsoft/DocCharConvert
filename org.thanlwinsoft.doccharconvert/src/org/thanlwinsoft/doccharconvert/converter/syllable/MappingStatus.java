/**
 * 
 */
package org.thanlwinsoft.doccharconvert.converter.syllable;

/**
 * @author keith
 * Flags to store the ambiguity of SyllableConverter mappings
 * An mapping is ambiguous in one direction if more that one row specifies a
 * mapping for the same syllable. This flag will not be set for the first 
 * mapping that maps this syllable, only the subsequent ones.
 */
public enum MappingStatus
{
    /**
     * Ambiguous in forwards direction
     */
    AMBIGUOUS_FORWARDS(1),
    /**
     * Ambiguous in backwards direction
     */
    AMBIGUOUS_BACKWARDS(2);
    // 4,8 should be used for subsequent values if needed

    int mValue;
    MappingStatus(int value) { mValue = value; }
    /**
     * 
     * @return bit field
     */
    public int bit() { return mValue; }
    /**
     * 
     * @param test
     * @return true if specified bit is set
     */
    public boolean isSet(int test) { return (test & mValue) > 0; }
}
