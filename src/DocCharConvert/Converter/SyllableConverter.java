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

package DocCharConvert.Converter;

import java.io.File;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;

import DocCharConvert.Converter.syllable.Script;
import DocCharConvert.Converter.syllable.Component;
import DocCharConvert.Converter.syllable.ComponentClass;
import DocCharConvert.Converter.syllable.MappingTable;
import DocCharConvert.Converter.syllable.SyllableXmlReader;

/**
 *
 * @author keith
 */
public class SyllableConverter extends ReversibleConverter
{
    Script [] scripts = null;
    Vector <MappingTable> mappingTables = null;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    private String name = null;
    protected boolean initOk = false;
    private File xmlFile = null;
    private int oldSide = 0;
    private int newSide = 1;
    private int INVALID_COMP = -2;
    private long filetime = -1;
        
    /** Creates a new instance of SyllableConverter */
    public SyllableConverter(File xmlFile)
    {
        scripts = new Script[2];
        this.xmlFile = xmlFile;
    }
    
    protected void addScript(Script script, int side)
    {
          scripts[side] = script;
    }
    
    public String getName()
    {
        return name;
        
    }
    public void setName(String newName)
    {
        this.name = newName;
    }
    public String convert(String oldText) 
        throws FatalException, RecoverableException
    {
        String converted = "";
        if (!isForwards())
        {
            oldSide = 1;
            newSide = 0;
        }
        Vector <Syllable> parseOutput = new Vector<Syllable>();       
        for (int offset = 0; offset < oldText.length(); )
        {
              Vector <Integer> syllable = new Vector <Integer>(scripts[oldSide].getNumComponents() + 1);
              syllable.add(0);
              Vector <Vector<Integer>> syllables = 
                      parseSyllableComponent(scripts[oldSide], oldText, offset, 0, syllable);
              if (syllables.size() <= 1) // always get one empty vector
              {
                  parseOutput.add(new Syllable(oldText.substring(offset, offset+1)));
                  offset++;
              }
              else
              {
                  Syllable syl = chooseSyllable(oldText, offset, syllables);
                  if (syl != null)
                  {
                    offset += syl.oldLength();
                    parseOutput.add(syl);
                  }
                  else
                  {
                      parseOutput.add(new Syllable(oldText.substring(offset, offset+1)));
                      offset++;
                  }
              }
        }
        return convertSyllables(parseOutput);
    }
    
    protected String convertSyllables(Vector < Syllable> parseOutput)
    {
        StringBuffer output = new StringBuffer();
        for (int i = 0; i< parseOutput.size(); i++)
        {
            Syllable s = parseOutput.get(i);
            Syllable nextS = null;
            if (i+1<parseOutput.size()) parseOutput.get(i + 1);
            // tbd add exception handling
            if (s.isKnown())
            {
                // tbd add repeat handling
                output.append(dumpSyllable(newSide, s.getConversionResult()));
            }
            else
                output.append(s.getInputString());
        }
        return output.toString();
    }
    
    protected Syllable chooseSyllable(String text, int offset, Vector <Vector<Integer>> syllables)
    {
        // choose the longest syllable
        Vector<Integer> chosen = null;
        assert(syllables.size() > 0);
        Iterator <Vector<Integer>> syl = syllables.iterator();
        Vector <Integer> longest = null; 
        int length = 0;
        Syllable result = null;
        while (syl.hasNext())
        {
            Vector<Integer> testSyl = syl.next();
            System.out.println("Choose syllable for  '" + text.substring(offset, offset + testSyl.elementAt(0)) + 
                    "' " + testSyl.toString() );
             Integer [] conversion = convertSyllable(testSyl);
       
            if (conversion != null)
            {
                int testLength = testSyl.elementAt(0);
                if ( testLength > length)
                {
                    length = testLength;
                    longest = testSyl;
                    if (length > text.length())
                        length = text.length();
                    result = new Syllable(longest, text.substring(offset, offset + length), conversion);
                }
                else if (testLength > 0 && testLength == length)
                {
                    // much harder to decide, choose the first one for now
                    System.out.println("Ambiguous conversion:\t" + text.substring(offset, offset + length) + '\t' + 
                            longest.toString() + " or " + testSyl.toString());
                }
            }
        }
        return result;
    }
    
    /**
     * element 0 of the compValues vector stores the cumulative length of the syllable 
     * Subsequent elements record the index of the value which matched for that component
     * There may be several matches for a given component if it is of variable length
     */
    protected Vector  <Vector<Integer>> parseSyllableComponent(Script script, String text, int offset, 
                                                                                                                  int cIndex, Vector<Integer>compValues)
    {
        Component comp = script.getSyllableComponent(cIndex);
        Vector <Vector<Integer>> candidates = new Vector<Vector<Integer>>();
        // find all possible matches for this component
        for (int i = offset;( i <= offset + comp.getMaxLength()) && (i <= text.length()); i++)
        {
            int valueIndex = comp.getIndex(text.substring(offset, i));
            if (valueIndex > -1)
            {
                Vector<Integer>candidate = new Vector<Integer>(compValues);
                candidate.add(valueIndex);
                int length = compValues.elementAt(0) + i - offset;
                candidate.set(0, length);
                if (cIndex < script.getNumComponents() - 1)
                {
                    Vector <Vector<Integer>> subCandidates = parseSyllableComponent(script, text, i, cIndex + 1, candidate);
                    candidates.addAll(subCandidates);
                }
                else
                    candidates.add(candidate);
            }
        }
        return candidates;
    }
    
    protected Integer[] convertSyllable(Vector<Integer>compValues)
    {
        Integer [] result = new Integer[scripts[newSide].getNumComponents()];
        for (int k = 0; k<result.length; k++) result[k] = INVALID_COMP;
        // apply mapping table rules
        Iterator <MappingTable> iMapTable = mappingTables.iterator();
        while (iMapTable.hasNext())
        {
            MappingTable table = iMapTable.next();
            Integer [] oldValues = new Integer[table.getNumColumns(oldSide)];
            for (int i = 0; i<oldValues.length; i++)
            {
                int indexInSyllable = mapId2ScriptId(table, oldSide,i);
                oldValues[i] = compValues.elementAt(indexInSyllable + 1);
            }
            List <Integer> newValues = table.map(oldSide, oldValues);
            if (newValues == null) return null;
            for (int j=0; j<newValues.size(); j++)
            {
                int indexInSyllable = mapId2ScriptId(table, newSide, j);
                if (result[indexInSyllable] > INVALID_COMP &&
                     result[indexInSyllable] != newValues.get(j))
                {
                    // remove leading char count for dump
                    Integer[] sylIndices = compValues.subList(1, compValues.size()).toArray(new Integer[0]);
                    System.out.println("Warning: overwriting syllable values " + dumpSyllable(oldSide,sylIndices));
                }
                result[indexInSyllable] = newValues.get(j);
            }
        }
        // now look for any unconverted components
        for (int k = 0; k<result.length; k++) 
        {
            if (result[k] == -2)
            {
                // try to find a class to convert it with
                Component comp = scripts[oldSide].getSyllableComponent(k);
                Iterator <String> iCId = comp.getClassIdIterator();
                while (iCId.hasNext())
                {
                    String cid = iCId.next();
                    ComponentClass cc = comp.getClass(cid);
                    Component origComp = cc.getComponent(oldSide);
                    int oldRef = compValues.elementAt(scripts[oldSide].getComponentIndex(origComp) + 1);
                    result[k] = cc.getCorrespondingRef(oldSide, oldRef);
                    if (result[k] > -1) break;
                }
                // nothing found, so can't convert
                return null;
            }
        }
        return result;
    }
    
    protected String dumpSyllable(int side, Integer [] compValues)
    {
        StringBuffer orig = new StringBuffer();
        for (int i = 0; i<compValues.length; i++)
        {
            Component comp = scripts[side].getSyllableComponent(i);
            orig.append(comp.getComponentValue(compValues[i]));
        }
        return orig.toString();
    }
    
    protected int mapId2ScriptId(MappingTable table, int side, int i)
    {
        String colId = table.getColumnId(side, i);
        assert(colId != null);
        Component col = scripts[side].getSyllableComponent(colId);
        int indexInSyllable = scripts[side].getComponentIndex(col);
        return indexInSyllable;
    }
    
    public boolean isInitialized()
    {       
        if (xmlFile.lastModified() > filetime) initOk = false;
        return initOk;
    }

    /**
     * Some converters may need to preinitialise some things
     */
    public void initialize() throws FatalException
    {
        filetime = xmlFile.lastModified();
        SyllableXmlReader reader = new SyllableXmlReader(xmlFile);
        if (reader.parse())
        {
          scripts = reader.getScripts();
          mappingTables = reader.getMappingTables();
          //initOk = true;
        }
        else
        {
          throw new FatalException(reader.getErrorLog());
        }
    }
    /**
     * Some converters may need to tidy up after conversion is finished
     * The converter should not be used after this has been called
     */
    public void destroy()
    {
        
        
    }
    
    protected class Syllable
    {
        boolean known = true;
        String text = "";
        Vector <Integer> syllable = null;
        Integer [] result = null;
        public Syllable(Vector<Integer> syllable, String orig, Integer [] result) 
        {
            this.text = orig;
            this.syllable = syllable;
            this.result = result;
        }
        public Syllable (String unknown)
        {
            this.text = unknown;
            this.known = false;
        }
        public boolean isKnown() { return known; }
        public int oldLength() { return syllable.elementAt(0); }
        public Integer [] getConversionResult() { return result; }
        public String getInputString() { return text; }
    }
}
