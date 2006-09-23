package org.thanlwinsoft.doccharconvert.opendoc;



public class ScriptType
{
    /** Script Types used in OpenDocument. Fonts are selected based on which
     * UnicodeBlock a character falls in. 
     * WEAK characters are script neutral, so the 
     * font selection will depend on the surrounding characters. */
    public enum Type 
    {   
        LATIN, COMPLEX, CJK, WEAK ;
        public String toString() {return this.name(); }
    }
    
    
    /** find longest possible segment of text within cArray, which has the
     * same type of script
     * @param cArray
     * @param start position of search
     * @param length maximum length of text to search with in array
     * @return ScriptSegment object representing text with the same script type
     */
    public static ScriptSegment find(char [] cArray, int start, int length)
    {
        int i = start;
        int end = start + length;
        Type cType = get(cArray, i);
        Type segType = cType;
        while ((i + 1 < end) && 
               (segType == cType || cType == Type.WEAK))
        {
            if (cType != Type.WEAK) segType = cType;
            i++;
            cType = get(cArray, i);
        }
        if (i + 1 == end) i = end;
        final Type thisSegType = segType;
        ScriptSegment seg = new ScriptSegment(thisSegType, cArray, start, i - start); 
        return seg;
    }
    /** find script type for the given character using the next or previous 
     * character as appropriate if it is a surrogate
     * @param cArray
     * @param index
     * @return ScriptType.Type for the given character
     */
    public static Type get(char [] cArray, int index)
    {
        int codePoint = cArray[index];
        if (Character.isHighSurrogate(cArray[index]) && (index + 1 < cArray.length) && Character.isLowSurrogate(cArray[index + 1]))
        {
            codePoint = Character.toCodePoint(cArray[index], cArray[index + 1]);
        }
        else if (Character.isLowSurrogate(cArray[index]) && (index > 0) && Character.isHighSurrogate(cArray[index - 1]))
        {
            codePoint = Character.toCodePoint(cArray[index - 1], cArray[index]);
        }
        return get(codePoint);
    }
    /** find script type for the given Unicode code point
     * @param c code point
     * @return ScriptType.Type for the given code point
     */
    public static Type get(int c)
    {
        Type type = Type.WEAK;
        Character.UnicodeBlock cb = Character.UnicodeBlock.of(c);
        if (Character.UnicodeBlock.BASIC_LATIN == cb ||
            Character.UnicodeBlock.LATIN_1_SUPPLEMENT == cb ||
            Character.UnicodeBlock.LATIN_EXTENDED_A == cb ||
            Character.UnicodeBlock.LATIN_EXTENDED_B == cb ||
            Character.UnicodeBlock.IPA_EXTENSIONS == cb ||
            Character.UnicodeBlock.SPACING_MODIFIER_LETTERS == cb ||
            Character.UnicodeBlock.COMBINING_DIACRITICAL_MARKS == cb ||
            Character.UnicodeBlock.GREEK == cb ||
            Character.UnicodeBlock.CYRILLIC == cb ||
            Character.UnicodeBlock.ARMENIAN == cb ||
            Character.UnicodeBlock.GEORGIAN == cb ||
            Character.UnicodeBlock.ETHIOPIC == cb ||
            Character.UnicodeBlock.CHEROKEE == cb ||
            Character.UnicodeBlock.UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS == cb ||
            Character.UnicodeBlock.OGHAM == cb ||
            Character.UnicodeBlock.RUNIC == cb ||
            Character.UnicodeBlock.GREEK_EXTENDED == cb ||
            Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL == cb )
        {
            type = Type.LATIN;        
        }
        else if (Character.UnicodeBlock.HEBREW == cb ||
                Character.UnicodeBlock.ARABIC == cb ||
                Character.UnicodeBlock.SYRIAC == cb ||
                Character.UnicodeBlock.THAANA == cb ||
                Character.UnicodeBlock.DEVANAGARI == cb ||
                Character.UnicodeBlock.BENGALI == cb ||
                Character.UnicodeBlock.GURMUKHI == cb ||
                Character.UnicodeBlock.GUJARATI == cb ||
                Character.UnicodeBlock.ORIYA == cb ||
                Character.UnicodeBlock.TAMIL == cb ||
                Character.UnicodeBlock.TELUGU == cb ||
                Character.UnicodeBlock.KANNADA == cb ||
                Character.UnicodeBlock.MALAYALAM == cb ||
                Character.UnicodeBlock.SINHALA == cb ||
                Character.UnicodeBlock.THAI == cb ||
                Character.UnicodeBlock.LAO == cb ||
                Character.UnicodeBlock.TIBETAN == cb ||
                Character.UnicodeBlock.MYANMAR == cb ||
                Character.UnicodeBlock.KHMER == cb ||
                Character.UnicodeBlock.MONGOLIAN == cb ||
                Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A == cb ||
                Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B == cb ||
                Character.UnicodeBlock.KHMER_SYMBOLS == cb)
        {
            type = Type.COMPLEX;
        }
        else if (Character.UnicodeBlock.CJK_RADICALS_SUPPLEMENT == cb ||
                Character.UnicodeBlock.KANGXI_RADICALS == cb ||
                Character.UnicodeBlock.IDEOGRAPHIC_DESCRIPTION_CHARACTERS == cb ||
                Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION == cb ||
                Character.UnicodeBlock.HIRAGANA == cb ||
                Character.UnicodeBlock.KATAKANA == cb ||
                Character.UnicodeBlock.BOPOMOFO == cb ||
                Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO == cb ||
                Character.UnicodeBlock.KANBUN == cb ||
                Character.UnicodeBlock.BOPOMOFO_EXTENDED == cb ||
                Character.UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS == cb ||
                Character.UnicodeBlock.CJK_COMPATIBILITY == cb ||
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A == cb ||
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS == cb ||
                Character.UnicodeBlock.YI_SYLLABLES == cb ||
                Character.UnicodeBlock.YI_RADICALS == cb ||
                Character.UnicodeBlock.YI_SYLLABLES == cb ||
                Character.UnicodeBlock.HANGUL_SYLLABLES == cb ||
                Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS == cb ||
                Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS == cb ||
                Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS == cb ||
                Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B == cb)
        {
            type = Type.CJK;
        }
        else
        {
            type = Type.WEAK;
        }
        return type;
    }
    /** script types take from OOo */
    final static int UnicodeScriptType[][] = {
        {0x0000, 0x007f}, //  0. Basic Latin
        {0x0080, 0x00ff}, //  1. Latin-1 Supplement
        {0x0100, 0x017f}, //  2. Latin Extended-A
        {0x0180, 0x024f}, //  3. Latin Extended-B
        {0x0250, 0x02af}, //  4. IPA Extensions
        {0x02b0, 0x02ff}, //  5. Spacing Modifier Letters
        {0x0300, 0x036f}, //  6. Combining Diacritical Marks
        {0x0370, 0x03ff}, //  7. Greek
        {0x0400, 0x04ff}, //  8. Cyrillic
        {0x0530, 0x058f}, //  9. Armenian
        {0x0590, 0x05ff}, // 10. Hebrew
        {0x0600, 0x06ff}, // 11. Arabic
        {0x0700, 0x074f}, // 12. Syriac  
        {0x0780, 0x07bf}, // 13. Thaana
        {0x0900, 0x097f}, // 14. Devanagari
        {0x0980, 0x09ff}, // 15. Bengali
        {0x0a00, 0x0a7f}, // 16. Gurmukhi
        {0x0a80, 0x0aff}, // 17. Gujarati
        {0x0b00, 0x0b7f}, // 18. Oriya
        {0x0b80, 0x0bff}, // 19. Tamil
        {0x0c00, 0x0c7f}, // 20. Telugu
        {0x0c80, 0x0cff}, // 21. Kannada
        {0x0d00, 0x0d7f}, // 22. Malayalam
        {0x0d80, 0x0dff}, // 23. Sinhala
        {0x0e00, 0x0e7f}, // 24. Thai
        {0x0e80, 0x0eff}, // 25. Lao
        {0x0f00, 0x0fff}, // 26. Tibetan
        {0x1000, 0x109f}, // 27. Myanmar 
        {0x10a0, 0x10ff}, // 28. Georgian
        {0x1100, 0x11ff}, // 29. Hangul Jamo
        {0x1200, 0x137f}, // 30. Ethiopic
        {0x13a0, 0x13ff}, // 31. Cherokee
        {0x1400, 0x167f}, // 32. Unified Canadian Aboriginal Syllabics
        {0x1680, 0x169f}, // 33. Ogham
        {0x16a0, 0x16ff}, // 34. Runic
        {0x1780, 0x17ff}, // 35. Khmer
        {0x1800, 0x18af}, // 36. Mongolian
        {0x1e00, 0x1eff}, // 37. Latin Extended Additional
        {0x1f00, 0x1fff}, // 38. Greek Extended
        {0x2000, 0x206f}, // 39. General Punctuation
        {0x2070, 0x209f}, // 40. Superscripts and Subscripts
        {0x20a0, 0x20cf}, // 41. Currency Symbols
        {0x20d0, 0x20ff}, // 42. Combining Marks for Symbols
        {0x2100, 0x214f}, // 43. Letterlike Symbols
        {0x2150, 0x218f}, // 44. Number Forms
        {0x2190, 0x21ff}, // 45. Arrows
        {0x2200, 0x22ff}, // 46. Mathematical Operators
        {0x2300, 0x23ff}, // 47. Miscellaneous Technical
        {0x2400, 0x243f}, // 48. Control Pictures
        {0x2440, 0x245f}, // 49. Optical Character Recognition
        {0x2460, 0x24ff}, // 50. Enclosed Alphanumerics
        {0x2500, 0x257f}, // 51. Box Drawing
        {0x2580, 0x259f}, // 52. Block Elements
        {0x25a0, 0x25ff}, // 53. Geometric Shapes
        {0x2600, 0x26ff}, // 54. Miscellaneous Symbols
        {0x2700, 0x27bf}, // 55. Dingbats
        {0x2800, 0x28ff}, // 56. Braille Patterns
        {0x2e80, 0x2eff}, // 57. CJK Radicals Supplement
        {0x2f00, 0x2fdf}, // 58. Kangxi Radicals
        {0x2ff0, 0x2fff}, // 59. Ideographic Description Characters
        {0x3000, 0x303f}, // 60. CJK Symbols and Punctuation
        {0x3040, 0x309f}, // 61. Hiragana
        {0x30a0, 0x30ff}, // 62. Katakana
        {0x3100, 0x312f}, // 63. Bopomofo
        {0x3130, 0x318f}, // 64. Hangul Compatibility Jamo
        {0x3190, 0x319f}, // 65. Kanbun
        {0x31a0, 0x31bf}, // 66. Bopomofo Extended
        {0x3200, 0x32ff}, // 67. Enclosed CJK Letters and Months
        {0x3300, 0x33ff}, // 68. CJK Compatibility
        {0x3400, 0x4db5}, // 69. CJK Unified Ideographs Extension A
        {0x4e00, 0x9fff}, // 70. CJK Unified Ideographs
        {0xa000, 0xa48f}, // 71. Yi Syllables
        {0xa490, 0xa4cf}, // 72. Yi Radicals
        {0xac00, 0xd7a3}, // 73. Hangul Syllables
        {0xd800, 0xdb7f}, // 74. High Surrogates
        {0xdb80, 0xdbff}, // 75. High Private Use Surrogates
        {0xdc00, 0xdfff}, // 76. Low Surrogates
        {0xe000, 0xf8ff}, // 77. Private Use
        {0xf900, 0xfaff}, // 78. CJK Compatibility Ideographs
        {0xfb00, 0xfb4f}, // 79. Alphabetic Presentation Forms
        {0xfb50, 0xfdff}, // 80. Arabic Presentation Forms-A
        {0xfe20, 0xfe2f}, // 81. Combining Half Marks
        {0xfe30, 0xfe4f}, // 82. CJK Compatibility Forms
        {0xfe50, 0xfe6f}, // 83. Small Form Variants
        {0xfe70, 0xfefe}, // 84. Arabic Presentation Forms-B
        {0xfeff, 0xfeff}, // 85. kNoScript
        {0xff00, 0xffef} // 85. Halfwidth and Fullwidth Forms
    };

    /*
    static char typeList[] = {
        { UnicodeScript_kBasicLatin, UnicodeScript_kArmenian,   ScriptType::LATIN },    // 0-9,
        { UnicodeScript_kHebrew, UnicodeScript_kMyanmar,        ScriptType::COMPLEX },  // 10-27,
        { UnicodeScript_kGeorgian, UnicodeScript_kGeorgian,     ScriptType::LATIN },    // 28,
        { UnicodeScript_kHangulJamo, UnicodeScript_kHangulJamo, ScriptType::ASIAN },    // 29,
        { UnicodeScript_kEthiopic, UnicodeScript_kRunic,        ScriptType::LATIN },    // 30-34,
        { UnicodeScript_kKhmer, UnicodeScript_kMongolian,       ScriptType::COMPLEX },  // 35-36,
        { UnicodeScript_kLatinExtendedAdditional, 
          UnicodeScript_kGreekExtended,                         ScriptType::LATIN },    // 37-38,
        { UnicodeScript_kCJKRadicalsSupplement, 
          UnicodeScript_kHangulSyllable,                        ScriptType::ASIAN },    // 57-73,
        { UnicodeScript_kCJKCompatibilityIdeograph, 
          UnicodeScript_kCJKCompatibilityIdeograph,             ScriptType::ASIAN },    // 78,
        { UnicodeScript_kArabicPresentationA, 
          UnicodeScript_kArabicPresentationA,                   ScriptType::COMPLEX },  // 80,
        { UnicodeScript_kCJKCompatibilityForm, 
          UnicodeScript_kCJKCompatibilityForm,                  ScriptType::ASIAN },    // 82,
        { UnicodeScript_kArabicPresentationB, 
          UnicodeScript_kArabicPresentationB,                   ScriptType::COMPLEX },  // 84,
        { UnicodeScript_kHalfwidthFullwidthForm, 
          UnicodeScript_kHalfwidthFullwidthForm,                ScriptType::ASIAN },    // 86,
        { UnicodeScript_kScriptCount, 
          UnicodeScript_kScriptCount,                           ScriptType::WEAK }      // 88
    };*/
}
