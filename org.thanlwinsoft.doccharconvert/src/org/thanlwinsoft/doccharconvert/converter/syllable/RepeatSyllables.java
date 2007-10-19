/*
Copyright (C) 2005-2007 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.doccharconvert.converter.syllable;

import java.util.Vector;

import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * Interface to allow Syllables to be tweaked after the main conversion with
 * SyllableConverter. This may be useful for script specific case conversion or
 * multi-syllable conversion rules.
 * 
 * @author keith
 */
public class RepeatSyllables implements SyllableChecker
{

    private Script[] scripts = null;

    /** Constructor called by SyllableXmlReader */
    public RepeatSyllables()
    {
        scripts = new Script[2];
    }

    /**
     * Checks the specified syllables, assuming that the first syllable starts a
     * sentence.
     * 
     * @param oldSide
     *            0=left, 1=right
     * @param syllables
     *            Vector of converted Syllables
     * @param boolean
     *            flag to enable debug logging
     * @return corrected syllable Vector
     */
    public Vector<Syllable> checkSyllables(int oldSide,
            Vector<Syllable> syllables, boolean debug)
    {
        int newSide = (oldSide == 1) ? 0 : 1;
        if (scripts[oldSide].usesRepeater())
        {
            boolean hasSeparator = !(scripts[newSide].getRepeatChar()
                    .equals(""));
            String repeater = scripts[oldSide].getRepeatChar();
            String repeatChar = scripts[newSide].getRepeatChar();
            for (int i = 1; i < syllables.size(); i++)
            {
                Syllable s = syllables.get(i);
                if (s.getOriginalString().equals(repeater))
                {
                    Syllable duplicate = new Syllable(syllables.get(i - 1));
                    duplicate.setPrevious(syllables.get(i - 1));
                    syllables.set(i, duplicate);
                    if (hasSeparator)
                    {
                        syllables.insertElementAt(new Syllable(syllables
                                .get(i - 1), repeatChar), i++);
                    }
                }
            }
        }
        else
        {
            boolean hasSeparator = !(scripts[oldSide].getRepeatChar()
                    .equals(""));
            int offset = 1;
            if (hasSeparator)
                offset = 2;
            String testSeparator = "";
            String separator = scripts[oldSide].getRepeatChar();
            // String preSyl = null;
            // String postSyl = null;
            for (int i = offset; i < syllables.size(); i++)
            {
                Syllable s = syllables.get(i);
                Syllable prev = syllables.get(i - offset);
                if (hasSeparator)
                    testSeparator = syllables.get(i - 1).getOriginalString();
                if (s.isKnown()
                        && prev.isKnown()
                        && s.equals(prev)
                        && ((hasSeparator == false)
                                || testSeparator.equals(separator) || testSeparator
                                .equals(" ")))
                {
                    if (hasSeparator)
                    {
                        if (i - offset > 0)
                        {
                            String testBefore = syllables.get(i - offset - 1)
                                    .getOriginalString();
                            if (testBefore.equals(separator))
                                continue;
                        }
                        if (i + 1 < syllables.size())
                        {
                            String testAfter = syllables.get(i + 1)
                                    .getOriginalString();
                            if (testAfter.equals(separator))
                                continue;
                        }
                    }
                    syllables.set(i, new Syllable(syllables.get(i)
                            .getPrevious(), scripts[newSide].getRepeatChar()));
                    if (hasSeparator)
                    {
                        syllables.remove(--i);
                    }
                }
            }
        }
        return syllables;
    }

    /**
     * Generic initializer that may be used to set initialization variables in
     * the checker if needed.
     * 
     * @param args
     *            may be null if the repeater settings were set direct in the
     *            xml arg[0] = side that uses repeat marker 0 = left, 1 = right
     *            the other side will be assumed to visibly repeat the syllable
     *            arg[1] = character that is used to mark a repeat or to
     *            separate syllables on left side arg[2] = character that is
     *            used to mark a repeat or to separate syllables on right side
     * @return true if initialisation was successful
     */
    public boolean initialize(Script[] scripts, Object[] args)
    {
        if (args == null || args.length == 0)
        {
            // just take it from the script properties
            this.scripts[0] = scripts[0];
            this.scripts[1] = scripts[1];
            return true;
        }
        else
            if (args.length == 3)
            {
                try
                {
                    int repeaterSide = Integer.parseInt(args[0].toString());
                    this.scripts[0] = scripts[0];
                    this.scripts[1] = scripts[1];
                    scripts[0].setRepeatChar((repeaterSide == 0), args[1]
                            .toString());
                    scripts[1].setRepeatChar((repeaterSide == 1), args[2]
                            .toString());
                    return true;
                }
                catch (NumberFormatException e)
                {
                    System.out.println(e.getMessage());
                }
            }

        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker#getArgumentDescriptions()
     */
    public String[] getArgumentDescriptions()
    {
        return new String[] { MessageUtil.getString("RepeatMarkerSide"),
                MessageUtil.getString("LeftSideRepeatMarker"),
                MessageUtil.getString("RightSideRepeatMarker") };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker#getArgumentTypes()
     */
    public Class<?>[] getArgumentTypes()
    {
        return new Class<?>[] { Integer.class, String.class, String.class };
    }
}
