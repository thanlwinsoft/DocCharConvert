/*
Copyright (C) 2006-2007 Keith Stribley http://www.thanlwinsoft.org/

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
package org.thanlwinsoft.doccharconvert.converter.syllable.mien;

import java.util.Vector;
import java.util.regex.Pattern;

import org.thanlwinsoft.doccharconvert.converter.syllable.Script;
import org.thanlwinsoft.doccharconvert.converter.syllable.Syllable;
import org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker;
/**
 * Handle Old Roman Tone changes
 * @author keith
 *
 */
public class MienOrmToneChange implements SyllableChecker 
{
    protected int MNR_SIDE = 0;
    protected int MOR_SIDE = 1;
    protected Pattern nrmFinal_mnng;
    protected Pattern nrmStop_kpt;
    private Script [] scripts = null;
    int hToneMnrId = 0;
    int cToneMnrId = 0;
    int bToneMorId = 0;
    int gToneMorId = 0;
    int mnrVowelComponent = -1;
    int mnrFinalComponent = -1;
    int morFinalComponent = -1;
    int mnrToneComponent = -1;
    int morToneComponent = -1;
    /** Constructor called by SyllableXmlReader */
    public MienOrmToneChange()
    {
      scripts = new Script[2];
      nrmFinal_mnng = Pattern.compile("m|n|ng");
      nrmStop_kpt = Pattern.compile("k|p|t");
    }
    
	public Vector<Syllable> checkSyllables(int oldSide,
			Vector<Syllable> syllables, boolean debug) 
    {
        if (oldSide == MNR_SIDE)
        {
            return checkMnrSyllables(syllables, debug);
        }
        else if (oldSide == MOR_SIDE)
        {
            return checkMorSyllables(syllables, debug);
        }
        else
        {
            throw new IllegalArgumentException("Unknown side: " + oldSide);
        }
	}
    
    protected Vector<Syllable> checkMnrSyllables(Vector<Syllable> syllables, 
                                                 boolean debug)
    {
        for (int i = 2; i < syllables.size(); i++)
        {
            if (syllables.get(i - 2).isKnown() &&
                syllables.get(i).isKnown())
            {
                if (syllables.get(i - 1).getOriginalString().equals("-") ||
                     (syllables.get(i - 1).getOriginalString().equals(" ") &&
                      syllables.get(i).getOriginalString()
                      .equals(syllables.get(i - 2).getOriginalString())))
                {
                    int finalId = syllables.get(i - 2).getOriginal()[mnrFinalComponent];
                    int vowelId = syllables.get(i - 2).getOriginal()[mnrVowelComponent];
                    Integer [] oldResult = syllables.get(i - 2).getConversionResult();
                    if (finalId == 0)
                    {
                        String vowel = scripts[MNR_SIDE].getSyllableComponent("v")
                            .getComponentValue(vowelId);
                        //System.out.println(vowel);
                        if (vowel.endsWith("q"))
                        {
                            oldResult[morToneComponent] = gToneMorId;
                        }
                        else
                        {
                            oldResult[morToneComponent] = bToneMorId;
                        }
                    }
                    else
                    {
                        String finalCons = scripts[MNR_SIDE].getSyllableComponent("f")
                            .getComponentValue(finalId);
                        if (nrmFinal_mnng.matcher(finalCons).matches())
                        {
                            oldResult[morToneComponent] = bToneMorId;
                        }
                        else if (nrmStop_kpt.matcher(finalCons).matches())
                        {
                            oldResult[morToneComponent] = gToneMorId;
                        }
                    }
                    syllables.get(i - 2).setConversionResult(oldResult);
                    syllables.set(i - 1, new Syllable(null, " "));
                }
                else if (syllables.get(i - 1).getOriginalString().equals("^"))
                {
                    syllables.set(i - 1, new Syllable(null, "-"));
                }
            }            
        }
        for (int i = 1; i < syllables.size(); i++)
        {
            if (syllables.get(i - 1).isKnown() &&
                syllables.get(i).isKnown())
            {
                syllables.insertElementAt(new Syllable(null, "-"), i);
            }
        }
        return syllables;
    }
    
    protected Vector<Syllable> checkMorSyllables(
            Vector<Syllable> syllables, boolean debug) 
    {
        for (int i = 2; i < syllables.size(); i++)
        {
            if (syllables.get(i - 2).isKnown() &&
                syllables.get(i).isKnown() &&
                (syllables.get(i - 1).getOriginalString().equals("-") ||
                 syllables.get(i - 1).getOriginalString().equals(" ")))
            {
                boolean same = true;
                for (int part = 0; same &&
                     part < scripts[MOR_SIDE].getNumComponents(); part++)
                {
                    if (part != morToneComponent)
                    {
                        if (syllables.get(i).getOriginal()[part] != 
                            syllables.get(i - 2).getOriginal()[part])
                            same = false;
                    }
                }
                if (same)
                {
                    Integer [] result1 = syllables.get(i - 2).getConversionResult();
                    Integer [] result2 = syllables.get(i).getConversionResult();
                    result1[mnrToneComponent] = result2[mnrToneComponent];
                    syllables.get(i - 2).setConversionResult(result1);
                }
            }
        }
        return syllables;
    }

	public boolean initialize(Script[] scripts, Object[] args) 
    {
        //    just take it from the script properties
      this.scripts[0] = scripts[0];
      this.scripts[1] = scripts[1];
      mnrVowelComponent = scripts[MNR_SIDE].getComponentIndex(scripts[MNR_SIDE]
                                           .getSyllableComponent("v"));
      mnrFinalComponent = scripts[MNR_SIDE].getComponentIndex(scripts[MNR_SIDE]
                                          .getSyllableComponent("f"));
      morFinalComponent = scripts[MOR_SIDE].getComponentIndex(scripts[MOR_SIDE]
                                          .getSyllableComponent("of"));
      mnrToneComponent = scripts[MNR_SIDE].getComponentIndex(scripts[MNR_SIDE]
                                          .getSyllableComponent("t"));
      morToneComponent = scripts[MOR_SIDE].getComponentIndex(scripts[MOR_SIDE]
                                          .getSyllableComponent("ot"));
      hToneMnrId = scripts[MNR_SIDE].getSyllableComponent("t").getIndex("h");
      cToneMnrId = scripts[MNR_SIDE].getSyllableComponent("t").getIndex("c");
      bToneMorId = scripts[MOR_SIDE].getSyllableComponent("ot").getIndex("b");
      gToneMorId = scripts[MOR_SIDE].getSyllableComponent("ot").getIndex("g");
      return true;
	}

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker#getArgumentDescriptions()
     */
    public String[] getArgumentDescriptions()
    {
        return new String[] {};
    }

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.converter.syllable.SyllableChecker#getArgumentTypes()
     */
    public Class<?>[] getArgumentTypes()
    {
        return new Class<?>[] {};
    }

}
