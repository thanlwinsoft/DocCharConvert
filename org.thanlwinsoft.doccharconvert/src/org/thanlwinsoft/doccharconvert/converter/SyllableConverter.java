/*
 * Copyright (C) 2005 Keith Stribley <doccharconvert@thanlwinsoft.org>
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * -----------------------------------------------------------------------
 * $HeadURL: $ $LastChangedBy: keith $ $LastChangedDate: $ $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package org.thanlwinsoft.doccharconvert.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.SortedSet;

import org.thanlwinsoft.doccharconvert.converter.syllable.Script;
import org.thanlwinsoft.doccharconvert.converter.syllable.Component;
import org.thanlwinsoft.doccharconvert.converter.syllable.ComponentClass;
import org.thanlwinsoft.doccharconvert.converter.syllable.MappingTable;
import org.thanlwinsoft.doccharconvert.converter.syllable.Syllable;
import org.thanlwinsoft.doccharconvert.converter.syllable.SyllableComparator;
import org.thanlwinsoft.doccharconvert.converter.syllable.SyllableSetComparator;
import org.thanlwinsoft.doccharconvert.converter.syllable.SyllableXmlReader;
import org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker;
import org.thanlwinsoft.doccharconvert.converter.syllable.ExceptionList;
import org.thanlwinsoft.util.IClassLoaderUtil;

/**
 * Convert words defined as Syllables defined in one script to another. This
 * will be most applicable for Asian languages consisting of consonant clusters.
 */
public class SyllableConverter extends ReversibleConverter
{
    Script[] scripts = null;
    Vector<MappingTable> mappingTables = null;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final String UNKNOWN_CHAR = "??";
    private String name = null;
    private String reverseName = null;
    protected boolean initOk = false;
    private URL xmlURL = null;
    private File xmlFile = null;
    private int oldSide = 0;
    private int newSide = 1;
    private int INVALID_COMP = -2;
    private long filetime = -1;
    private boolean debug = false;
    private boolean backtrackEnabled = false;
    private Vector<SyllableChecker> checkers = null;
    private PrintStream debugStream = System.out;
    private Syllable previousSyl = null;
    private IClassLoaderUtil mLoader = null;

    /**
     * Creates a new instance of SyllableConverter
     * 
     * @param xmlFile XML config file
     */
    public SyllableConverter(File xmlFile)
    {
        this.xmlFile = xmlFile;
        try
        {
            construct(xmlFile.toURI().toURL());
        }
        catch (MalformedURLException e)
        {
            // TODO how to handle this
        }
    }

    public SyllableConverter(URL xmlFile)
    {
        construct(xmlFile);
    }

    /**
     * Creates a new instance of SyllableConverter
     * 
     * @param xmlFile
     *            XML config file
     * @param leftExceptions
     *            exceptions on the left side
     * @param rightExceptions
     *            corresponding exceptions on right side
     */
    protected void construct(URL xmlFile)
    {
        this.checkers = new Vector<SyllableChecker>();
        this.scripts = new Script[2];
        this.xmlURL = xmlFile;
    }

    protected void addScript(Script script, int side)
    {
        scripts[side] = script;
    }

    public String getName()
    {
        if (isForwards())
            return name;
        else
            return reverseName;
    }

    public String getBaseName()
    {
        return name;
    }

    public void setName(String newName)
    {
        this.name = newName;
    }

    public void setReverseName(String aName)
    {
        this.reverseName = aName;
    }

    public void setDebug(boolean on, File logDir)
    {
        debug = on;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        sdf.setTimeZone(TimeZone.getDefault());
        if (logDir != null)
        {
            File logFile = new File(logDir, getBaseName()
                    + sdf.format(new Date()) + ".log");
            try
            {
                debugStream = new PrintStream(logFile);
            }
            catch (FileNotFoundException e)
            {
                System.err.println(e);
            }
        }
    }

    /**
     * Convert text using the converter - the main entry point for conversion
     * 
     * @param inputText
     *            original text
     * @return converted text
     */
    public String convert(String inputText) throws FatalException,
            RecoverableException
    {
        if (!isForwards())
        {
            oldSide = 1;
            newSide = 0;
        }
        String oldText = inputText;
        previousSyl = new Syllable(null, "");
        // SortedSet <Syllable> conversionSet = new TreeSet<Syllable>(new
        // SyllableSetComparator());
        // case is handled inside parseSyllableComponent
        // if (scripts[oldSide].ignoreCase()) oldText = inputText.toLowerCase();

        Syllable lastSyllable = findSyllables(oldText, 0, oldText.length());
        Syllable s = findOptimumSyllables(oldText, lastSyllable, oldText
                .length()); // conversionSet.first();

        Vector<Syllable> parseOutput = new Vector<Syllable>(s
                .getSyllableIndex() + 1);
        while (s != null)
        {
            parseOutput.add(0, s);
            s = s.getPrevious();
        }
        return convertSyllables(parseOutput);
    }

    /**
     * Main loop for finding and analysing syllables. Called recursively during
     * Syllable optimisation.
     * 
     * @param text
     * @param startOffset
     * @param endOffset
     * @return
     */
    private Syllable findSyllables(String text, int startOffset, int endOffset)
    {
        String oldText = text;
        for (int offset = startOffset; offset < endOffset;)
        {
            Vector<Integer> syllable = new Vector<Integer>(scripts[oldSide]
                    .getNumComponents() + 1);
            syllable.add(0);
            Vector<Vector<Integer>> syllables = parseSyllableComponent(
                    scripts[oldSide], oldText, offset, 0, syllable);
            if (syllables.size() <= 1) // always get one empty vector
            {
                // there was no match, but this is a useful point to try back
                // tracking from to prevent the number of possibilities
                // growing too large
                if (startOffset == 0 && endOffset == text.length())
                    previousSyl = findOptimumSyllables(text, previousSyl,
                            offset);
                previousSyl = new Syllable(previousSyl, oldText.substring(
                        offset, offset + 1));
                offset++;
            }
            else
            {
                SortedSet<Syllable> options = previousSyl.getNextCandidates();
                if (options == null)
                {
                    options = chooseSyllable(previousSyl, oldText, offset,
                            syllables);
                    previousSyl.setNextCandidates(options);
                }
                Syllable syl = null;
                if (options.size() > 0)
                    syl = options.first();
                if (syl != null)
                {
                    offset += syl.originalLength();
                    previousSyl = syl;
                }
                else
                // no match, add a one char syllable and proceed
                {
                    Syllable unmatched = new Syllable(previousSyl, oldText
                            .substring(offset, offset + 1));
                    // try to backtrack
                    Syllable willReplace = previousSyl;
                    Syllable backTrack = willReplace.getPrevious();
                    if (backTrack == null)
                    {
                        // nowhere to backtrack
                        previousSyl = unmatched;
                        offset++;
                        continue;
                    }
                    // backTrackPoint.addLast(unmatched);
                    offset = backTrack(willReplace, offset);
                    // parseOutput.add(previousSyl);
                    if (previousSyl == null)
                    {
                        previousSyl = unmatched;
                        offset++;
                    }
                }
            }
        }
        return previousSyl;
    }

    /**
     * Find the optimum syllables up to offset, starting with previousSyllable
     * as the first possibility.
     * 
     * @param previousSyl
     * @param offset
     * @return
     */
    private Syllable findOptimumSyllables(String text,
            Syllable previousSyllable, int offset)
    {
        assert (previousSyllable == previousSyl);
        if (backtrackEnabled == false)
            return previousSyllable;
        SortedSet<Syllable> conversionSet = new TreeSet<Syllable>(
                new SyllableSetComparator());
        conversionSet.add(previousSyllable);
        int prevBackOffset = offset;
        do
        {
            int backOffset = backTrack(previousSyllable, offset);
            if (previousSyl == null)
                break;
            Syllable s = findSyllables(text, backOffset, offset);
            if (backOffset == prevBackOffset)
                break;
            conversionSet.add(s);
            prevBackOffset = backOffset;
        } while (previousSyl != null);
        return conversionSet.first();
    }

    /**
     * back track from fromSyllable and return the first offset with an
     * alternative conversion. If no alternatives are found between offset and
     * the last unknown node, then previousSyl will be set to null.
     * 
     * @param fromSyllable
     * @param origOffset
     * @return
     */
    protected int backTrack(Syllable fromSyllable, int origOffset)
    {
        Syllable willReplace = fromSyllable;
        int offset = origOffset;
        Syllable backTrack = willReplace.getPrevious();
        while (backTrack != null)
        {
            if (backTrack.getNextCandidates() != null)
            {
                SortedSet<Syllable> candidates = backTrack.getNextCandidates()
                        .tailSet(willReplace);
                Iterator<Syllable> ic = candidates.iterator();
                Syllable candidate = null;
                while (ic.hasNext())
                {
                    candidate = ic.next();
                    if (candidate == willReplace)
                        continue;
                    if (candidate.originalLength() < willReplace
                            .originalLength())
                    {
                        break;
                    }
                    candidate = null;
                }
                if (candidate != null && candidate != willReplace)
                {
                    offset += candidate.originalLength()
                            - willReplace.originalLength();
                    previousSyl = candidate;
                    break;
                }
            }
            else
            {
                // don't backtrack beyond an unknown
                previousSyl = null;
                offset = origOffset;
                return offset;
            }
            offset -= willReplace.originalLength();
            willReplace = backTrack;
            backTrack = willReplace.getPrevious();
        }
        if (backTrack == null)
        {
            // back track failed
            previousSyl = null;
            offset = origOffset;
        }
        return offset;
    }

    /**
     * loop over the syllable objects and output the converted string
     * 
     * @param origParseOutput
     *            Vector of syllables and unknown characters
     * @return converted String
     */
    protected String convertSyllables(Vector<Syllable> origParseOutput)
    {
        Vector<Syllable> parseOutput = origParseOutput;
        Iterator<SyllableChecker> c = checkers.iterator();
        // run all the syllable checkers
        while (c.hasNext())
        {
            parseOutput = c.next().checkSyllables(oldSide, parseOutput, debug);
        }
        // loop over output and dump to string
        StringBuffer output = new StringBuffer();
        for (int i = 0; i < parseOutput.size(); i++)
        {
            Syllable s = parseOutput.get(i);
            String syllableText = s.getResultString();
            if (debug && syllableText.contains(UNKNOWN_CHAR))
                debugStream.println("Ambiguous conversion:\t"
                        + s.getOriginalString() + '\t' + syllableText);
            output.append(syllableText);
        }
        return output.toString();
    }

    /**
     * Choose a syllable to use as the correct conversion from all the
     * candidates. The algorithm first checks to see if a valid conversion is
     * possible for the candidate syllable. It then chooses the longest syllable
     * that has a valid conversion.
     * 
     * @param text
     *            source text
     * @param offset
     *            of syllable in source text
     * @param syllables
     *            Vector of possible syllables to choose from.
     * @return Syllable object representing the original and converted syllable
     *         or null of no conversion was found.
     */
    protected TreeSet<Syllable> chooseSyllable(Syllable previousSyl,
            String text, int offset, Vector<Vector<Integer>> syllables)
    {
        // choose the longest syllable
        assert (syllables.size() > 0);
        Iterator<Vector<Integer>> syl = syllables.iterator();
        syl.next(); // ignore null result
        TreeSet<Syllable> results = new TreeSet<Syllable>(
                new SyllableComparator());
        while (syl.hasNext())
        {
            Vector<Integer> testSyl = syl.next();
            if (debug)
            {
                debugStream.println("Choose syllable for  '"
                        + text.substring(offset, offset + testSyl.elementAt(0))
                        + "' " + testSyl.toString());
            }
            Integer[] conversion = convertSyllable(testSyl);

            if (conversion != null)
            {
                int testLength = testSyl.elementAt(0);
                if (testLength > 0)
                {
                    Syllable s = new Syllable(previousSyl, scripts, oldSide,
                            testSyl, text
                                    .substring(offset, offset + testLength),
                            conversion);
                    if (s.isAmbiguous())
                    {
                        if (debug)
                            debugStream.println("Ignoring" + s.toString());
                    }
                    else
                    {
                        results.add(s);
                    }
                }
            }
        }
        if (debug && results.size() > 0)
        {
            debugStream.println("Chose: "
                    + ((results.size() > 0) ? results.first().toString() : ""));
        }

        return results;
    }

    /**
     * Parses a syllable component. Element 0 of the compValues vector stores
     * the cumulative length of the syllable. Subsequent elements record the
     * index of the value which matched for that component. There may be several
     * matches for a given component if it is of variable length.
     * 
     * @param script
     * @param text
     * @param offset
     *            into text
     * @param cIndex
     *            index of cluster component
     * @param compValues
     *            running vector of component values for syllable
     * @return vector of component Values updated with this component
     */
    protected Vector<Vector<Integer>> parseSyllableComponent(Script script,
            String text, int offset, int cIndex, Vector<Integer> compValues)
    {
        Component comp = script.getSyllableComponent(cIndex);
        Vector<Vector<Integer>> candidates = new Vector<Vector<Integer>>();
        // find all possible matches for this component
        for (int i = offset; (i <= offset + comp.getMaxLength())
                && (i <= text.length()); i++)
        {
            int valueIndex = comp.getIndex(text.substring(offset, i));
            if (valueIndex == -1 && script.ignoreCase())
                valueIndex = comp.getIndex(text.substring(offset, i)
                        .toLowerCase());
            if (valueIndex > -1)
            {
                Vector<Integer> candidate = new Vector<Integer>(compValues);
                candidate.add(valueIndex);
                int length = compValues.elementAt(0) + i - offset;
                candidate.set(0, length);
                if (cIndex < script.getNumComponents() - 1)
                {
                    Vector<Vector<Integer>> subCandidates = parseSyllableComponent(
                            script, text, i, cIndex + 1, candidate);
                    candidates.addAll(subCandidates);
                }
                else
                    candidates.add(candidate);
            }
        }
        return candidates;
    }

    /**
     * Convert the syllable from one script to the other in terms of reference
     * indices used in the Component objects.
     * 
     * @param compValues
     *            of indices of components of syllable on original side (first
     *            value is the length of the syllable in Characters)
     * @return array of indices of components of syllable on destination side
     */
    protected Integer[] convertSyllable(Vector<Integer> compValues)
    {
        Integer[] result = new Integer[scripts[newSide].getNumComponents()];
        for (int k = 0; k < result.length; k++)
            result[k] = INVALID_COMP;
        // apply mapping table rules
        Iterator<MappingTable> iMapTable = mappingTables.iterator();
        while (iMapTable.hasNext())
        {
            MappingTable table = iMapTable.next();
            Integer[] oldValues = new Integer[table.getNumColumns(oldSide)];
            for (int i = 0; i < oldValues.length; i++)
            {
                int indexInSyllable = mapId2ScriptId(table, oldSide, i);
                oldValues[i] = compValues.elementAt(indexInSyllable + 1);
            }
            List<Integer> newValues = table.map(oldSide, oldValues);
            if (newValues == null)
            {
                if (table.isOptional())
                    continue;
                else
                    return null;
            }
            for (int j = 0; j < newValues.size(); j++)
            {
                int indexInSyllable = mapId2ScriptId(table, newSide, j);
                if (newValues.get(j) == MappingTable.AMBIGUOUS
                        && result[indexInSyllable] != INVALID_COMP)
                    continue;
                int oldValue = result[indexInSyllable];
                int newValue = newValues.get(j);
                // Don't let optional tables overwrite existing values
                // They should only be used if there is an ambiguity
                if (oldValue <= INVALID_COMP
                        || (oldValue != newValue && table.isOptional() == false))
                {
                    if (debug && oldValue > INVALID_COMP)
                    {
                        // remove leading char count for dump
                        Integer[] sylIndices = compValues.subList(1,
                                compValues.size()).toArray(new Integer[0]);
                        debugStream
                                .println("Warning: overwriting syllable values "
                                        + dumpSyllable(oldSide, sylIndices)
                                        + " with "
                                        + scripts[newSide]
                                                .getSyllableComponent(
                                                        indexInSyllable)
                                                .getComponentValue(
                                                        newValues.get(j)));
                    }
                    result[indexInSyllable] = newValues.get(j);
                }
            }
        }
        // now look for any unconverted components
        for (int k = 0; k < result.length; k++)
        {
            if (result[k] == INVALID_COMP)
            {
                // try to find a class to convert it with
                Component comp = scripts[oldSide].getSyllableComponent(k);
                Iterator<String> iCId = comp.getClassIdIterator();
                while (iCId.hasNext())
                {
                    String cid = iCId.next();
                    ComponentClass cc = comp.getClass(cid);
                    Component origComp = cc.getComponent(oldSide);
                    int oldRef = compValues.elementAt(scripts[oldSide]
                            .getComponentIndex(origComp) + 1);
                    result[k] = cc.getCorrespondingRef(oldSide, oldRef);
                    if (result[k] > -1)
                        break;
                }
                // nothing found, so can't convert
                return null;
            }
        }
        return result;
    }

    /**
     * Convert the list of reference indices representing the syllable into a
     * human readable string or the output string.
     * 
     * @param side
     *            of conversion LEFT or RIGHT
     * @param compValues
     *            integer of refrences indices of component values
     * @return Output string
     */
    protected String dumpSyllable(int side, Integer[] compValues)
    {
        StringBuffer orig = new StringBuffer();
        for (int i = 0; i < compValues.length; i++)
        {
            Component comp = scripts[side].getSyllableComponent(i);
            if (compValues[i].intValue() < 0)
                orig.append(UNKNOWN_CHAR);
            else
                orig.append(comp.getComponentValue(compValues[i]));
        }
        return orig.toString();
    }

    /**
     * Convert the list of reference indices representing the syllable into a
     * human readable string or the output string.
     * 
     * @param side
     *            of conversion LEFT or RIGHT
     * @param compValues
     *            integer of references indices of component values
     * @return Output string
     */
    protected String dumpDebugSyllable(int side, Integer[] compValues)
    {
        StringBuffer orig = new StringBuffer();
        for (int i = 0; i < compValues.length; i++)
        {
            Component comp = scripts[side].getSyllableComponent(i);
            if (compValues[i].intValue() < 0)
                orig.append(UNKNOWN_CHAR);
            else
                orig.append(comp.getComponentValue(compValues[i]));
            orig.append('|');
        }
        return orig.toString();
    }

    /**
     * Convert the index of the component in the mapping table into an index in
     * the syllable that can be retrieved from the script
     * 
     * @param table
     * @param side
     *            LEFT or RIGHT
     * @param i
     *            index of column in table on side
     * @return index of component in syllable
     */
    protected int mapId2ScriptId(MappingTable table, int side, int i)
    {
        String colId = table.getColumnId(side, i);
        assert (colId != null);
        Component col = scripts[side].getSyllableComponent(colId);
        int indexInSyllable = scripts[side].getComponentIndex(col);
        return indexInSyllable;
    }

    /**
     * Test for successful initialisation. This is reset to false if the config
     * file has changed on disk.
     * 
     * @return initOK true if initialisation successful
     */
    public boolean isInitialized()
    {
        if (xmlFile != null && xmlFile.lastModified() > filetime)
            initOk = false;
        Iterator<SyllableChecker> checker = checkers.iterator();
        while (checker.hasNext())
        {
            SyllableChecker sc = checker.next();
            if (sc instanceof ExceptionList)
            {
                if (((ExceptionList) sc).fileChanged())
                {
                    initOk = false;
                }
            }
        }
        return initOk;
    }

    /**
     * Some converters may need to preinitialise some things
     */
    public void initialize() throws FatalException
    {
        if (xmlFile != null)
            filetime = xmlFile.lastModified();
        
        SyllableXmlReader reader = new SyllableXmlReader(xmlURL, mLoader, debug,
                debugStream);
        if (reader.parse())
        {
            scripts = reader.getScripts();
            mappingTables = reader.getMappingTables();
            checkers = reader.getCheckers();
            backtrackEnabled = reader.isBacktrackEnabled();
            initOk = true;
        }
        else
        {
            throw new FatalException(reader.getErrorLog());
        }
    }

    /**
     * Some converters may need to tidy up after conversion is finished The
     * converter should not be used after this has been called
     */
    public void destroy()
    {

    }

    public void setEncodings(Charset iCharset, Charset oCharset)
    {
        // Processing is done in unicode, so this converter ignores the charsets
    }

    public URL getXmlFile()
    {
        return xmlURL;
    }

    public void disableChecker(Class<?> clazz)
    {
        Iterator<SyllableChecker> iChecker = checkers.iterator();
        while (iChecker.hasNext())
        {
            SyllableChecker sc = iChecker.next();
            if (sc.getClass().equals(clazz))
            {
                iChecker.remove();
            }
        }
    }

    public void setClassLoader(IClassLoaderUtil loader)
    {
        mLoader  = loader;
    }
}
