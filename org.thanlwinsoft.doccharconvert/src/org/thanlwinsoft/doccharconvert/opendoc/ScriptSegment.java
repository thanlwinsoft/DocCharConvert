/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
------------------------------------------------------------------------------*/
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
    /**
     * @return type of script
     */
    public ScriptType.Type getType() { return type; }
    /**
     * @return array of characters
     */
    public char [] getArray() { return cArray; }
    /**
     * @return start of segment
     */
    public int getStart() { return start; }
    /**
     * @return length of segment
     */
    public int getLength() { return length; }
}
