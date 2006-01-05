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
import java.util.Iterator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import org.thanlwinsoft.doccharconvert.converter.SyllableConverter;
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
    boolean debug = false;
    boolean optional = false;
    public static final int UNKNOWN = -1;
    public static final int AMBIGUOUS = -3;
    /** Constructor for a mapping table. 
    * @param id name of table identifier used in XML file
    * @param columns array of components in the table
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
        leftEntries.add(null);
        rightEntries.add(null);
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
        Arrays.fill(leftMap, -1);
        for (int j =0; j<rightSizes.size(); j++)
        {
            rightMapSize *= rightSizes.elementAt(j).intValue();
        }
        rightMap = new int[rightMapSize];
        Arrays.fill(rightMap, -1);
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
        if (leftEntry == null || rightEntry == null || leftEntry.length != leftSizes.size() ||
             rightEntry.length != rightSizes.size())
        {
            throw new IllegalArgumentException("Expected " +leftSizes.size() +
                    "," + rightSizes.size() + " found " +  leftEntry.length + 
                    "," + rightEntry.length);
        }
        int leftOffset = getMapOffset(leftSizes, leftEntry);
        int entryIndexL = rightEntries.size();
        // sometimes a mapping already exists, in this case it is ambigous, so
        // another table will be needed to resolve the conversion - usually this
        // only happens on one side
        List<Integer> arrayR = new ArrayList<Integer>(rightEntry.length);
        for (int i = 0; i<rightEntry.length; i++) arrayR.add(i,rightEntry[i]);
        if (leftMap[leftOffset] != UNKNOWN)
        {
            arrayR = setAmbiguousFlag(arrayR, 
                                      rightEntries.get(leftMap[leftOffset]));   
        }
        rightEntries.add(arrayR);
        leftMap[leftOffset] = entryIndexL;
        
        int rightOffset = getMapOffset(rightSizes, rightEntry);
        int entryIndexR = leftEntries.size();
        
        List<Integer> arrayL = new ArrayList<Integer>(leftEntry.length);
        for (int i = 0; i<leftEntry.length; i++) arrayL.add(i,leftEntry[i]);
        if (rightMap[rightOffset] != UNKNOWN)
        {
            arrayL = setAmbiguousFlag(arrayL, 
                                      leftEntries.get(rightMap[rightOffset]));            
        }
        leftEntries.add(arrayL);
        rightMap[rightOffset] = entryIndexR; 
        if (debug)
            System.out.println(showEntry(0,leftEntry) + leftOffset + ":" + entryIndexL + 
                "\t" + showEntry(1,rightEntry) + rightOffset + ":" + entryIndexR);
    }
    /**
     * More than 2 lines match the same map offset on this side
     * This methods sets the component index to AMBIGUOUS for all components
     * which differ. Another map must be specified in which this component is 
     * not ambiguous.
     * @param newArray newest set of component indices, 
     * @param oldArray previous set of component indices
     * @return combined set of component indices with AMBIGUOUS index set where
     * the 2 arrays differ
     */
    protected List<Integer> setAmbiguousFlag(List <Integer> newArray, 
                                             List<Integer> oldArray)
    {
        assert (oldArray.size() == newArray.size());
        for (int i = 0; i<oldArray.size(); i++)
        {
            if (oldArray.get(i).intValue() != newArray.get(i).intValue())
            {   
                newArray.set(i, AMBIGUOUS);
            }
        }
        return newArray;
    }
    
    protected String showEntry(int side, Integer[] entries)
    {
        StringBuffer entry = new StringBuffer();
        for (int i = 0; i< entries.length; i++)
        {
            Component c = columns[i + (leftSizes.size() * side)];
            entry.append(c.getId());
            entry.append("=");
            if (entries[i] == AMBIGUOUS)
                entry.append("??");
            else
            {
                entry.append("'");
                entry.append(c.getComponentValue(entries[i]));
                entry.append("'");
            }
            entry.append("\t");
        }
        return entry.toString();
    }
    
    /**
    * Retrieve the offset in the linear array for this entry.
    * This is used to convert the multidimensional entries into a linear offset.
    * @param colSizes of each column
    * @param entries in each dimension
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
    * @param leftEntry values for each component in map on left hand side
    * @return corresponding values for each component in map on right hand side
    */
    public List<Integer> mapLeft2Right(Integer[] leftEntry)
    {
        int leftOffset = getMapOffset(leftSizes, leftEntry);
        int rightIndex = leftMap[leftOffset];
        if (rightIndex == -1 || rightIndex >= rightEntries.size()) return null;
        List<Integer> result = rightEntries.elementAt(rightIndex);
        if (debug)
            System.out.println("Mapped: " + showEntry(0,leftEntry) + " => " + 
                showEntry(1,result.toArray(new Integer[0])));
        return result;
    }
    /**
    * Map from right to left
    * @param rightEntry values for each component in map on right hand side
    * @return corresponding values for each component in map on left hand side
    */
    public List<Integer> mapRight2Left(Integer[] rightEntry)
    {
        int rightOffset = getMapOffset(rightSizes, rightEntry);
        int leftIndex = rightMap[rightOffset];
        if (leftIndex == -1 || leftIndex >= leftEntries.size()) return null;
        List<Integer> result = leftEntries.elementAt(leftIndex);
        if (debug)
            System.out.println("Mapped: " + showEntry(1,rightEntry) + " => " + 
                showEntry(0,result.toArray(new Integer[0])));
        return result;
    }
    /**
    * Generic version of map, which allows the side to be parsed as an 
    * identifier
    * Map from side A to side b
    * @param sourceSide - side A
    * @param entry for each component in map on side A
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
    public void setDebug(boolean on) { debug = on; }
    public void setOptional(boolean yes) { optional = yes; }
    public boolean isOptional() { return optional; }
}

