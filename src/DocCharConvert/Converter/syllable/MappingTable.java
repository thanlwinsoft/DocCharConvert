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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Arrays;
import DocCharConvert.Converter.SyllableConverter;
/**
 * The MappingTable represents how one or more components from one
 * script are mapped to one or more in another.
 * The map can be thought of as having a multidimensional array for each side 
 * of the mapping. The number of dimensions in each array is equal to the 
 * number of components on that side of the map. The values in this array are 
 * the index of a Vector of corresponding values on the other side of the table.
 * All component values are referenced by internal id, not actual value.
 * The number of columns for each dimension corresponds to the number of valid
 * values that component can take.
 * The multidimensional array is currently implemented as a linear vector, but
 * this is hidden from the user.
 * @author keith
 */
public class MappingTable
{
    
    /** Creates a new instance of MappingTable */
    int [] leftMap;
    int [] rightMap;
    int leftMapSize = 1; 
    int rightMapSize = 1;
    Vector <Integer> leftSizes;
    Vector <Integer> rightSizes;
    Vector <List<Integer>> leftEntries;
    Vector <List<Integer>> rightEntries;
    String id = null;
    HashMap<String, Integer> leftColumnMap = null;
    HashMap<String, Integer> rightColumnMap = null;
    Component [] columns = null;
    /** Constructor for a mapping table. 
    * @param id name of table identifier used in XML file
    * @columns array of components in the table
    * there must be components present from 2 scripts
    * 
    */
    public MappingTable(String id, Component [] columns) 
        throws IllegalArgumentException, ConflictException
    {
        this.id = id;
        this.columns = columns;
        Script script = columns[0].getScript(); 
        int side = SyllableConverter.LEFT;
        leftSizes = new Vector<Integer>();
        rightSizes = new Vector<Integer>();
        leftColumnMap = new LinkedHashMap<String, Integer>();
        rightColumnMap = new LinkedHashMap<String, Integer>();
        leftEntries = new Vector <List<Integer>>();
        rightEntries = new Vector <List<Integer>>();
        HashMap<String, Integer>colMap = leftColumnMap;
        Vector <Integer> sizes = leftSizes;
        for (int i = 0; i<columns.length; i++)
        {
            if (script != columns[i].getScript() )
            {
                script = columns[i].getScript();
                side++; 
                if (side > SyllableConverter.RIGHT)
                    throw new IllegalArgumentException(
                      "Only 2 scripts allowed in MappingTable");
                sizes = rightSizes;
                colMap = rightColumnMap;
            }
            colMap.put(columns[i].getId(), new Integer(sizes.size()));
            sizes.add(columns[i].size());
        }
        if (leftSizes.size() == 0 || rightSizes.size() == 0)
            throw new ConflictException("MappingTable " + id + 
                    " must have columns on both sides");
        // allocate maps
       for (int j =0; j<leftSizes.size(); j++)
        {
            leftMapSize *= leftSizes.elementAt(j).intValue();
        }
        leftMap = new int[leftMapSize];
        for (int j =0; j<rightSizes.size(); j++)
        {
            rightMapSize *= rightSizes.elementAt(j).intValue();
        }
        rightMap = new int[rightMapSize];
    }
    /** 
    * get the index of the specified component in the left hand map
    * @param id of component
    * @return column index
    */
    public int getColumnLeftIndex(String id)
    {
        return leftColumnMap.get(id).intValue();
    }
    /** get the index of the specified component in the right hand map
    * @param id of component
    * @return column index
    */
    public int getColumnRightIndex(String id)
    {
        return rightColumnMap.get(id).intValue();
    }
    /** 
    * Add an entry to the map. This uses the indices returned from a component 
    * object.
    * @param leftEntry array of integers representing the component values
    * on the left hand side of the map
    * @param rightEntry array of integers representing the component values
    * on the right hand side of the map
    */
    public void addMap(Integer [] leftEntry, Integer [] rightEntry)
        throws ConflictException, IllegalArgumentException
    {
        if (leftEntry.length != leftSizes.size() ||
             rightEntry.length != rightSizes.size())
        {
            throw new IllegalArgumentException("Expected " +leftSizes.size() +
                    "," + rightSizes.size() + " found " +  leftEntry.length + 
                    "," + rightEntry.length);
        }
        int leftOffset = getMapOffset(leftSizes, leftEntry);
        int entryIndex = rightEntries.size();
        rightEntries.add(Arrays.asList(rightEntry));
        leftMap[leftOffset] = entryIndex;
        
        int rightOffset = getMapOffset(rightSizes, rightEntry);
        entryIndex = leftEntries.size();
        leftEntries.add(Arrays.asList(leftEntry));
        rightMap[rightOffset] = entryIndex; 
    }
    /**
    * Retrieve the offset in the linear array for this entry.
    * This is used to convert the multidimensional entries into a linear offset.
    * @param size of each column
    * @param index in each dimension
    * @return index in linear array.
    */
    protected int getMapOffset(Vector <Integer> colSizes, Integer[] entries)
    {
        int offset = 0;
        int rowOffset = 1;
        for (int i = 0; i<entries.length; i++)
        {
            offset += rowOffset * entries[i];
            rowOffset *= colSizes.elementAt(i).intValue();
        }
        return offset;
    }
    /**
    * Map from left to right
    * @param values for each component in map on left hand side
    * @return corresponding values for each component in map on right hand side
    */
    public List<Integer> mapLeft2Right(Integer[] leftEntry)
    {
        int leftOffset = getMapOffset(leftSizes, leftEntry);
        int rightIndex = leftMap[leftOffset];
        if (rightIndex >= rightEntries.size()) return null;
        return rightEntries.elementAt(rightIndex);
    }
    /**
    * Map from right to left
    * @param values for each component in map on right hand side
    * @return corresponding values for each component in map on left hand side
    */
    public List<Integer> mapRight2Left(Integer[] rightEntry)
    {
        int rightOffset = getMapOffset(rightSizes, rightEntry);
        int leftIndex = rightMap[rightOffset];
        if (leftIndex >= leftEntries.size()) return null;
        return leftEntries.elementAt(leftIndex);
    }
    /**
    * Generic version of map, which allows the side to be parsed as an 
    * identifier
    * Map from side A to side b
    * @param values for each component in map on side A
    * @return corresponding values for each component in map on side B
    */
    public List<Integer> map(int sourceSide, Integer[] entry)
    {
        List<Integer> result = null;
        switch (sourceSide)
        {
            case SyllableConverter.LEFT:
                result = mapLeft2Right(entry);
                break;
            case SyllableConverter.RIGHT:
                result = mapRight2Left(entry);
                break;
            default:
                throw new IllegalArgumentException("Unknown side " + sourceSide + 
                        " expected 0 or 1");
        }
        return result;
    }
    public int getNumLeftColumns()
    {
      return leftColumnMap.size();
    }
    public int getNumRightColumns()
    {
      return rightColumnMap.size();
    }
    public int getNumColumns(int side)
    {
        int result = 0;
         switch (side)
        {
            case SyllableConverter.LEFT:
                result = getNumLeftColumns();
                break;
            case SyllableConverter.RIGHT:
                result = getNumRightColumns();
                break;
            default:
                throw new IllegalArgumentException("Unknown side " + side + 
                        " expected 0 or 1");
        }
        return result;
    }
    public String getColumnId(int side, int index)
    {
        String result = null;
        switch (side)
        {
            case SyllableConverter.LEFT:
                result = getLeftColumnId(index);
                break;
            case SyllableConverter.RIGHT:
                result = getRightColumnId(index);
                break;
            default:
                throw new IllegalArgumentException("Unknown side " + side + 
                        " expected 0 or 1");
        }
        return result;
    }
    public String getRightColumnId(int index)
    {
      return rightColumnMap.keySet().toArray(new String[0])[index];
    }
    public String getLeftColumnId(int index)
    {
      return leftColumnMap.keySet().toArray(new String[0])[index];
    }
}

