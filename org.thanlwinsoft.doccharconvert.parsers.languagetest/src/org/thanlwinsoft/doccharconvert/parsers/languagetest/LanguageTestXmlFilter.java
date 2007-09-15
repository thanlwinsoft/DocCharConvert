package org.thanlwinsoft.doccharconvert.parsers.languagetest;


import java.util.HashMap;
import java.util.Map;

import org.thanlwinsoft.doccharconvert.FontStyle;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter.RecoverableException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author keith
 *
 */
public class LanguageTestXmlFilter extends XMLFilterImpl
{
    private HashMap <String,CharConverter>langIdToConverter = null;
    private Map<TextStyle, CharConverter> converters = null;
    
    private static final String LANG_MODULE_TAG = "LanguageModule";
    private static final String LANG_TAG = "Lang";
    private static final String NATIVE_LANG_TAG = "NativeLang";
    private static final String FOREIGN_LANG_TAG = "ForeignLang";
    private static final String LANG_ATTR = "lang";
    private static final String FONT_ATTR = "font";
    private CharConverter currentConverter = null;
    private static final String NAMESPACE = "http://www.thanlwinsoft.org/schemas/languagetest";
    private String prefixMapUri = null;
    public LanguageTestXmlFilter(Map<TextStyle, CharConverter> converters)
    {
        this.converters = converters;
        langIdToConverter = new HashMap <String,CharConverter>();
    }
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#startDocument()
     */
    @Override
    public void startDocument() throws SAXException
    {
        super.startDocument();
    }
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (currentConverter == null)
        {
            super.characters(ch, start, length);
        }
        else
        {
            String toConvert = new String(ch, start, length);
            try
            {
                String result = currentConverter.convert(toConvert);
                super.characters(result.toCharArray(), 0, result.length());
            } 
            catch (FatalException e)
            {
                e.printStackTrace();
                super.characters(ch, start, length);
            } 
            catch (RecoverableException e)
            {
                e.printStackTrace();
                super.characters(ch, start, length);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals(NATIVE_LANG_TAG) || qName.equals(FOREIGN_LANG_TAG))
        {
            currentConverter = null;
        }
        super.endElement(uri, localName, qName);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        Attributes theAtts = atts;
        if (qName.equals(LANG_TAG))
        {
            String langId = atts.getValue(LANG_ATTR);
            String fontName = atts.getValue(FONT_ATTR);
            FontStyle fontStyle = new FontStyle(fontName);
            CharConverter converter = null;
            if (converters.containsKey(fontStyle))
            {
                converter = converters.get(fontStyle);
                langIdToConverter.put(langId, converters.get(fontStyle));
            }
            else
            {
                fontStyle = new FontStyle(fontName.toLowerCase());
                if (converters.containsKey(fontStyle))
                {
                    converter = converters.get(fontStyle);
                    langIdToConverter.put(langId, converters.get(fontStyle));
                }
            }
            if (converter != null)
            {
                String newFont = converter.getNewStyle().getFontName();
                AttributesImpl newAttr = new AttributesImpl();
                for (int i = 0; i < atts.getLength(); i++)
                {
                    if (atts.getQName(i).equals(FONT_ATTR))
                    {
                        newAttr.addAttribute(atts.getURI(i), 
                                atts.getLocalName(i),
                                atts.getQName(i),
                                atts.getType(i),
                                newFont);
                    }
                    else
                    {
                        newAttr.addAttribute(atts.getURI(i), 
                            atts.getLocalName(i),
                            atts.getQName(i),
                            atts.getType(i),
                            atts.getValue(i));
                    }
                }
                theAtts = newAttr;
            }
            
        }
        else if (qName.equals(NATIVE_LANG_TAG) || qName.equals(FOREIGN_LANG_TAG))
        {
            String langId = atts.getValue(LANG_ATTR);
            currentConverter = langIdToConverter.get(langId);
        }
        else if (qName.equals(LANG_MODULE_TAG))
        {
            if (prefixMapUri == null)
            {
                // fix the namespace if it doesn't exist
                super.startPrefixMapping("", NAMESPACE);
            }
        }
        super.startElement(uri, localName, qName, theAtts);
    }
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#startPrefixMapping(java.lang.String, java.lang.String)
     */
    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException
    {
        prefixMapUri = uri;
        super.startPrefixMapping(prefix, uri);
    }
    
}
