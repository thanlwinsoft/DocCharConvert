package org.thanlwinsoft.doccharconvert.opendoc;


/**
 * A class to represent a segment of text of the same script type.
 * @author keith
 *
 */
public class ScriptSegment
{
    /** 
     * Construct a ScriptSegment. This is output by the ScriptType.find method.
     * @param type
     * @param cArray
     * @param start index within cArray of a segment of the given type
     * @param len length of characters with the given script type starting at start 
     */
    public ScriptSegment(final ScriptType.Type type, char [] cArray, int start, int len) 
    { 
        this.type = type;
        this.cArray = cArray;
        this.start = start;
        this.length = len;
    }
    char [] cArray;
    int start;
    int length;
    final ScriptType.Type type;
    public ScriptType.Type getType() { return type; }
    public char [] getArray() { return cArray; }
    public int getStart() { return start; }
    public int getLength() { return length; }
}
