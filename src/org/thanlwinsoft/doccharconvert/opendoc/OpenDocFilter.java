package org.thanlwinsoft.doccharconvert.opendoc;

import java.util.HashMap;
import java.util.EnumMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Stack;
import java.util.EnumSet;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.xml.ElementProperties;
import org.thanlwinsoft.doccharconvert.opendoc.ScriptType.Type;
//import org.thanlwinsoft.xml.XmlWriteFilter;

public class OpenDocFilter extends XMLFilterImpl
{
    /** manager to store all the styles for different families */
    OpenDocStyleManager styles = null;
    /** map of text styles to char converters passed in from DocCharConvert */
    Map<TextStyle,CharConverter> converterMap = null;
    /** map an OO face name to a corresponding face element */
    HashMap <String, ElementProperties> faces = null;
    /** Map a face name to a face converter object */
    HashMap <String, OOFaceConverter> faceMap;
    /** current style definition when inside office:document-styles */
    OpenDocStyle currentStyleDef = null;
    /** styles that are active for current text */
    EnumMap <ScriptType.Type, OpenDocStyle> currentStyle = null;
    // element stack
    Stack<ElementProperties> eStack = null;
    // converter map stack - one map per element
    Stack<EnumMap <ScriptType.Type, OOFaceConverter>> cStack = null;
    EnumMap <ScriptType.Type, OOFaceConverter> currentConv = null;
    ElementProperties pendingStyle = null;
    final static String STYLE_URI = 
        "urn:oasis:names:tc:opendocument:xmlns:style:1.0";
    final static String TEXT_URI = 
        "urn:oasis:names:tc:opendocument:xmlns:text:1.0";
    final static String ATTRIB_TYPE = "CNAME";
    ElementProperties currentElement = null;

    static EnumMap <ScriptType.Type, String> scriptAttrib = 
        new EnumMap <ScriptType.Type, String> (ScriptType.Type.class); 
    /** Static initialisation */
    {
        scriptAttrib.put(ScriptType.Type.LATIN, "style:font-name");
        scriptAttrib.put(ScriptType.Type.CJK, "style:font-name-asian");
        scriptAttrib.put(ScriptType.Type.COMPLEX, "style:font-name-complex");
    }
    /**
     * Construct a filter with the given style to converter mapping.
     * @param convertMap
     * @param styleManager to keep track of the different styles within the document
     */
    OpenDocFilter(Map<TextStyle,CharConverter> convertMap, 
                  OpenDocStyleManager styleManager)
    {
        this.styles = styleManager;
        this.converterMap = convertMap;
        faces = new HashMap <String, ElementProperties>();
        eStack = new Stack<ElementProperties>();
        cStack = new Stack<EnumMap <ScriptType.Type, OOFaceConverter>>();
        faceMap = new HashMap <String, OOFaceConverter> ();
        currentConv = new EnumMap <ScriptType.Type, OOFaceConverter> 
            (ScriptType.Type.class);
        currentStyle = new EnumMap <ScriptType.Type, OpenDocStyle> 
            (ScriptType.Type.class);
    }
    /**
    * @Override
    */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        int s = start;
        int l;
        do
        {
            ScriptSegment seg = ScriptType.find(ch, s, start + length - s);
            l = seg.getLength();
            OOFaceConverter converter = currentConv.get(seg.getType());
            if (converter == null || converter.converter == null)
                super.characters(ch, s, l);
            else
            {
                try
                {
                    // should the characters be buffered in case we can
                    // concatenate multiple characters calls?
                    String toConvert = new String(ch, s, l);
                    String result = converter.converter.convert(toConvert);
                    OpenDocStyle activeStyle = currentStyle.get(seg.getType());
                    System.out.println(converter.converter.getName() + " on " + 
                            activeStyle.getName() + " > " + 
                            activeStyle.convertedStyle.getName());
                    if (activeStyle != null && (l != length ||
                        (activeStyle != activeStyle.getConvertedStyle())))
                    {
                        // need to create a span to encompass text
                        AttributesImpl spanAttr = new AttributesImpl();
                        spanAttr.addAttribute(currentElement.getUri(), 
                                currentElement.getLocalName(), 
                                "text:style-name", "UNAME",
                                activeStyle.convertedStyle.getName());
                        super.startElement(currentElement.getUri(), 
                            currentElement.getLocalName(), 
                            "text:span",
                            spanAttr);
                    
                        super.characters(result.toCharArray(), 0, result.length());
                    
                        super.endElement(currentElement.getUri(), 
                            currentElement.getLocalName(), 
                            "text:span");
                    }
                    else
                    {
                        super.characters(result.toCharArray(), 0, result.length());
                    }
                }
                catch (CharConverter.FatalException fe)
                {
                    throw new SAXException(fe.getMessage());
                }
                catch (CharConverter.RecoverableException re)
                {
                    System.out.println(re);
                    super.characters(ch, start, length);
                }
            }
            s += seg.getLength();
        } while (s < start + length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        //ElementProperties endElement = 
        eStack.pop();
        if (qName.equals("office:font-face-decls"))
        {
            endFontFaceDecls();
        }
        else if (qName.equals("style:style") || qName.equals("style:default-style"))
        {
            endStyle();
        }
        else if (qName.equals("text:p"))
        {
            endP();
        }
        else if (qName.equals("text:span"))
        {
            endSpan();
        }
        else
        {
            super.endElement(uri, localName, qName);
        }
        currentElement = currentElement.getParent();
    }
    
    
    private void endSpan() throws SAXException
    {
        // TODO Auto-generated method stub
        currentConv = cStack.pop();
        endElement(currentElement);
    }

    private void endP() throws SAXException
    {
        // TODO Auto-generated method stub
        currentConv = cStack.pop();
        endElement(currentElement);
    }

    private void endStyle() throws SAXException
    {
        currentStyleDef = null;
        endElement(currentElement);
        if (pendingStyle != null)
        {
            startElement(pendingStyle);
            ElementProperties t[] = new ElementProperties [pendingStyle.getChildren().size()];
            // warning: assumes children are childless
            for(ElementProperties i : pendingStyle.getChildren().toArray(t))
            {
                startElement(i);
                endElement(i);
            }
            endElement(pendingStyle);
            pendingStyle = null;
        }
        
    }

    /**
     * Look at each of the faces that will be converted and add
     * a new entry for the new face that will replace it
     * @throws SAXException
     */
    private void endFontFaceDecls() throws SAXException
    {
        
        Iterator <OOFaceConverter> i = faceMap.values().iterator();
        while (i.hasNext())
        {
            OOFaceConverter fc = i.next();
            String newFamily = fc.getConverter().getNewStyle().getFontName();
            // should we normalize the face at this stage?
            //newFamily = OpenDocStyle.normalizeFace(newFamily);
            String newOOName = newFamily;
            int nameCnt = 0;
            while (faces.containsKey(newOOName))
            {
                newOOName = newFamily + Integer.toString(++nameCnt);
            }
            fc.newOOName = newOOName;
            ElementProperties newFaceElement = faces.get(fc.oldOOName).clone();
            AttributesImpl ai = newFaceElement.getAttributes();
            // adornments, pitch, etc are left as they were before
            ai.setValue(ai.getIndex("svg:font-family"),newFamily);
            ai.setValue(ai.getIndex("style:name"),newOOName);
            startElement(newFaceElement);
            endElement(newFaceElement);
            faces.put(newOOName, newFaceElement);
        }
        endElement(currentElement);
    }

    @Override
    public void startElement(String uri, String localName, String qName, 
                             Attributes atts) throws SAXException
    {
        ElementProperties thisElement = new ElementProperties(uri, localName, 
                                                              qName, atts);
        thisElement.setParent(currentElement);
        currentElement = thisElement;
        eStack.push(currentElement);
        if (qName.equals("office:font-face-decls"))
        {
            startFontFaceDecls();
        }
        else if (qName.equals("style:font-face"))
        {
            startFontFace();
        }
        else if (qName.equals("style:style") || qName.equals("style:default-style"))
        {
            startStyle();
        }
        else if (qName.equals("style:text-properties"))
        {
            startTextProperties();
        }
        else if (qName.equals("text:p"))
        {
            startP();
        }
        else if (qName.equals("text:span"))
        {
            startSpan();
        }
        else
        {
            super.startElement(uri, localName, qName, atts);
        }
    }
    
    protected void resolveActiveStyles()
    {
        for (ScriptType.Type st : EnumSet.range(ScriptType.Type.LATIN, 
                                                ScriptType.Type.CJK))
        {            
            OpenDocStyle ods = null;
            String faceName = null;
            for (int i = eStack.size() - 1; (i >= 0 && (faceName == null)); i--)
            {
                // look at parent elements
                ElementProperties pep = eStack.get(i);
                int sNameIndex = pep.getAttributes().getIndex("text:style-name"); 
                if (sNameIndex > -1)
                {
                    OpenDocStyle.StyleFamily psf = 
                        OpenDocStyle.getStyleForTag(currentElement.getQName());
                    ods = styles.getStyle(psf, 
                        pep.getAttributes().getValue(sNameIndex));
                    // find face for style
                    ods = resolveFace(st, ods);
                    if (ods != null)
                    {
                        faceName = ods.getFaceName(st);
                    }
                    if (faceName == null) // try default style
                    {
                        ods = styles.getStyle(psf,null);
                        if (ods != null) 
                            faceName = ods.resolveFaceName(st);
                    }
                }
            }
            if (addCurrentConvIfMatches(st, faceName))
            {
                System.out.print(ods.getName());
                System.out.println(" / " + ods.getConvertedStyle().getName());
            }
            currentStyle.put(st, ods);
        }
    }
    
    private OpenDocStyle resolveFace(Type type, OpenDocStyle style)
    {
        OpenDocStyle ods = style;
        while (ods != null && ods.getFaceName(type) == null)
        {
            ods = ods.getParentStyle();
        }
        return ods;
    }

    private void startSpan() throws SAXException
    {
        currentConv = new EnumMap<Type, OOFaceConverter>(Type.class);
        resolveActiveStyles();
        cStack.push(currentConv);
        startElement(currentElement);
    }
    
    private boolean addCurrentConvIfMatches(ScriptType.Type type, String faceName)
    {
        boolean convMatches = false;
        if (faceName != null && faceMap.containsKey(faceName))
        {
            OOFaceConverter ofc = faceMap.get(faceName);
            CharConverter cc = ofc.converter;
            if (cc.getOldStyle().getScriptType().equals(type))
            {
                currentConv.put(type, ofc);
                System.out.println(type.toString() + " " + faceName);
                convMatches = true;
            }
        }
        return convMatches;
    }

    private void startP() throws SAXException
    {
        currentConv = new EnumMap<Type, OOFaceConverter>(Type.class);
        resolveActiveStyles();
        cStack.push(currentConv);
        startElement(currentElement);
    }

    private void startTextProperties() throws SAXException
    {
        AttributesImpl ai = currentElement.getAttributes();
        EnumMap <Type, Integer> fName = 
            new EnumMap <Type, Integer>(Type.class);  
        fName.put(Type.LATIN, ai.getIndex("style:font-name"));
        fName.put(Type.CJK, ai.getIndex("style:font-name-asian"));
        fName.put(Type.COMPLEX, ai.getIndex("style:font-name-complex"));
        // asian and CTL fonts may need more careful handling, especially from
        // parents
        for(Type sType : EnumSet.range(Type.LATIN,Type.CJK))
        {
            String faceName = ai.getValue(fName.get(sType));
            if (currentStyleDef != null && faceName != null)
                currentStyleDef.setFaceName(sType, faceName);
            if (faceMap.containsKey(faceName))
            { 
                CharConverter cc = faceMap.get(faceName).converter;
                Type oldType =  cc.getOldStyle().getScriptType();
                
                if (currentStyleDef != null)
                {
                    System.out.println(currentStyleDef.getName() + " " + 
                        currentStyleDef.getFamily().name() + " " + 
                        faceMap.get(faceName).getNewOOFaceName());
                }
                else
                {
                    System.out.println("No style: " + 
                                       faceMap.get(faceName).getNewOOFaceName());
                }
                if (oldType.equals(sType))
                {
                    Type newType =  cc.getNewStyle().getScriptType();
//                  simple case, there is no change of script type
                    if (newType.equals(oldType))
                    {
                        ai.setValue(fName.get(sType), 
                                    faceMap.get(faceName).getNewOOFaceName());
                    }
                    else
                    {
                        // the script type has changed, so we need to preserve
                        // this style for the new script type in case there is 
                        // text in the new script type which shouldn't be 
                        // converted create a new span style, that just has the
                        //  relevant script type's font changed
                        AttributesImpl styleAttrib = new AttributesImpl();
                        AttributesImpl tpAttrib = new AttributesImpl();
                        final String styleType = 
                            OpenDocStyle.StyleFamily.TEXT.toString();
                        String newStyleName = 
                            getUniqueStyleName(styleType, 
                                    currentStyleDef.getName() + "_conv");
      
                        styleAttrib.addAttribute(STYLE_URI, "style", 
                                "style:name", ATTRIB_TYPE, newStyleName);
                        styleAttrib.addAttribute(STYLE_URI, "style", 
                                "style:family", ATTRIB_TYPE, styleType);
                        
                        tpAttrib.addAttribute(STYLE_URI, "style", 
                                scriptAttrib.get(newType), ATTRIB_TYPE,
                                faceMap.get(faceName).getNewOOFaceName());
                        pendingStyle = new ElementProperties(
                                STYLE_URI, "style", "style:style",
                                styleAttrib);
                        ElementProperties pendingTP = new ElementProperties(
                                STYLE_URI, "style", "style:text-properties",
                                tpAttrib);
                        pendingStyle.addChild(pendingTP);
                        OpenDocStyle convStyle = 
                            new OpenDocStyle(styleType, newStyleName);
                        currentStyleDef.setConvertedStyle(convStyle);
                    }
                }
                else
                {
                    
                }
            }
                
        }
        /*
        if (faceMap.containsKey(fName) || faceMap.containsKey(fcName) ||
            faceMap.containsKey(faName))
        {
            OOFaceConverter oofc = faceMap.get(fName);
            if (oofc == null) 
            {
                oofc = faceMap.get(fcName);
                if (oofc == null) oofc = faceMap.get(faName);
            }
            int normalIndex = ai.getIndex("style:font-name");
            if (normalIndex > -1)
            {
                ai.setValue(normalIndex,oofc.newOOName);
            }
            else
            {
                ai.addAttribute(currentElement.getUri(), currentElement.getLocalName(),
                        "style:font-name","CDATA",oofc.newOOName);
            }
            int ctlIndex = ai.getIndex("style:font-name-complex"); 
            if (ctlIndex > -1 &&
                faceMap.containsKey(ai.getValue(ctlIndex)))
            {
                ai.setValue(ctlIndex, 
                            faceMap.get(ai.getValue(ctlIndex)).newOOName);
            }
            int asianIndex = ai.getIndex("style:font-name-asian"); 
            if (asianIndex > -1 &&
                faceMap.containsKey(ai.getValue(asianIndex)))
            {
                ai.setValue(asianIndex, 
                            faceMap.get(ai.getValue(asianIndex)).newOOName);
            }
        }*/
        startElement(currentElement);
    }
    
    private String getUniqueStyleName(String family, String trialName)
    {
        String styleName = trialName;
        int i = 0;
        while (styles.getStyle(family, trialName) != null)
        {
            styleName = trialName + Integer.toString(i);
        }
        return styleName;
    }

    private void startStyle() throws SAXException
    {
        String family = currentElement.getAttributes().getValue("style:family");
        String name = currentElement.getAttributes().getValue("style:name");
        String parent = currentElement.getAttributes().getValue("style:parent-style-name");
        if (parent != null)
        {
            OpenDocStyle parentStyle = styles.getStyle(family, parent);
            if (parentStyle == null) 
            {
                currentStyleDef = new OpenDocStyle(family, name, parent);
            }
            else
            {
                currentStyleDef = new OpenDocStyle(family, name, parentStyle);
            }
            currentStyleDef.setManager(styles);
        }
        else currentStyleDef = new OpenDocStyle(family, name);
        styles.addStyle(currentStyleDef);
        startElement(currentElement);
    }

    private void startFontFace() throws SAXException
    {
        // TODO Auto-generated method stub
        startElement(currentElement);
        String ooFaceName = currentElement.getAttributes().getValue("style:name"); 
        faces.put(ooFaceName, currentElement);
        String fontFamily = currentElement.getAttributes()
                            .getValue("svg:font-family");
        if (converterMap.containsKey(fontFamily))
        {
            CharConverter cc = converterMap.get(fontFamily);
            faceMap.put(ooFaceName, new OOFaceConverter(ooFaceName,cc));
            System.out.println("matched face " + fontFamily);
        }
    }

    private void startFontFaceDecls() throws SAXException
    {
        // TODO Auto-generated method stub
        startElement(currentElement);
    }
    
    private void startElement(ElementProperties ep) throws SAXException
    {
        super.startElement(ep.getUri(), ep.getLocalName(), 
                           ep.getQName(), ep.getAttributes());
    }
    private void endElement(ElementProperties ep) throws SAXException
    {
        super.endElement(ep.getUri(), ep.getLocalName(), ep.getQName());
    }
    
    
    protected class OOFaceConverter
    {
        String oldOOName;
        String newOOName = null;
        CharConverter converter;
        protected OOFaceConverter(String oldOOName, CharConverter converter)
        {
            this.oldOOName = oldOOName;
            this.converter = converter;
        }
        String getOldOOFaceName() {return oldOOName;}
        String getNewOOFaceName() {return newOOName;}
        CharConverter getConverter() {return converter;}
    }
}
