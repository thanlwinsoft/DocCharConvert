package org.thanlwinsoft.doccharconvert.opendoc;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.XMLFilterImpl;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.xml.ElementProperties;
//import org.thanlwinsoft.xml.XmlWriteFilter;

public class OpenDocFilter extends XMLFilterImpl
{
    OpenDocStyleManager styles = null;
    Map<TextStyle,CharConverter> converterMap = null;
    HashMap <String, ElementProperties> faces = null;
    HashMap <String, OOFaceConverter> faceMap;
    OpenDocStyle currentStyle = null;
    Stack<ElementProperties> eStack = null;
    Stack<CharConverter> cStack = null;
    CharConverter currentConv = null;
    
    OpenDocFilter(Map<TextStyle,CharConverter> convertMap, 
                  OpenDocStyleManager styleManager)
    {
        this.styles = styleManager;
        this.converterMap = convertMap;
        faces = new HashMap <String, ElementProperties>();
        eStack = new Stack<ElementProperties>();
    }
    ElementProperties currentElement = null;
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (currentConv == null)
            super.characters(ch, start, length);
        else
        {
            try
            {
                // should the characters be buffered in case we can concatenate
                // multiple characters calls?
                String result = currentConv.convert(new String(ch, start, length));
                super.characters(result.toCharArray(), 0, result.length());
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
        cStack.pop();
        currentElement.end(parentFilter());
    }

    private void endP() throws SAXException
    {
        // TODO Auto-generated method stub
        cStack.pop();
        currentElement.end(parentFilter());
    }

    private void endStyle() throws SAXException
    {
        currentStyle = null;
        currentElement.end(parentFilter());
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
            newFaceElement.start(parentFilter());
            newFaceElement.end(parentFilter());
            faces.put(newOOName, newFaceElement);
        }
        currentElement.end(parentFilter());
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

    private void startSpan() throws SAXException
    {
        AttributesImpl ai = currentElement.getAttributes();
        String styleName = ai.getValue("text:style-name");
        if (styleName != null)
        {
            OpenDocStyle.StyleFamily sf = 
                OpenDocStyle.getStyleForTag(currentElement.getQName());
            OpenDocStyle ods = styles.getStyle(sf.name(), styleName);
            String faceName = null;
            if (ods != null) 
            {
                faceName = ods.resolveFaceName();
                if (faceName == null) // try default style
                {
                    OpenDocStyle defaultStyle = styles.getStyle(sf.name(), null);
                    if (defaultStyle != null) 
                        faceName = defaultStyle.getFaceName();
                }
            }
            if (ods == null || faceName == null)
            {
                for (int i = eStack.size() - 2; i >= 0; i--)
                {
                    // look at parent elements
                    ElementProperties pep = eStack.get(i);
                    int sNameIndex = pep.getAttributes().getIndex("text:style-name"); 
                    if (sNameIndex > -1)
                    {
                        OpenDocStyle.StyleFamily psf = 
                            OpenDocStyle.getStyleForTag(currentElement.getQName());
                        ods = styles.getStyle(psf.name(), 
                            pep.getAttributes().getValue(sNameIndex));
                    }
                }
            }
            if (ods != null)
            {
                faceName = ods.resolveFaceName();
                if (faceName != null && faceMap.containsKey(faceName))
                {
                    currentConv = faceMap.get(faceName).converter;
                }
            }
        }
        cStack.push(currentConv);
        currentElement.start(parentFilter());
    }

    private void startP() throws SAXException
    {
        AttributesImpl ai = currentElement.getAttributes();
        String styleName = ai.getValue("text:style-name");
        if (styleName != null)
        {
            OpenDocStyle.StyleFamily sf = 
                OpenDocStyle.getStyleForTag(currentElement.getQName());
            OpenDocStyle ods = styles.getStyle(sf.name(), styleName);
            if (ods != null)
            {
                String faceName = ods.resolveFaceName();
                if (faceName == null) // try default style
                {
                    OpenDocStyle defaultStyle = styles.getStyle(sf.name(), null);
                    if (defaultStyle != null) 
                        faceName = defaultStyle.getFaceName();
                }
                if (faceName != null)
                {
                    currentConv = faceMap.get(faceName).converter;
                }
            }
        }
        cStack.push(currentConv);
        currentElement.start(parentFilter());
    }

    private void startTextProperties() throws SAXException
    {
        AttributesImpl ai = currentElement.getAttributes();
        String fName = ai.getValue("style:font-name");
        String faName = ai.getValue("style:font-name-asian");
        String fcName = ai.getValue("style:font-name-complex");
        // asian and CTL fonts may need more careful handling, especially from
        // parents
        if (faceMap.containsKey(fName) || faceMap.containsKey(fcName) ||
            faceMap.containsKey(faName))
        {
            OOFaceConverter oofc = faceMap.get(fName);
            if (oofc == null) 
            {
                oofc = faceMap.get(fcName);
                if (oofc == null) oofc = faceMap.get(faName);
            }
            
            ai.setValue(ai.getIndex("style:font-name"),oofc.newOOName);
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
        }
        currentElement.start(parentFilter());
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
                System.out.println("Unknown Parent style:" + parent);
                currentStyle = new OpenDocStyle(family, name);
            }
            else
            {
                currentStyle = new OpenDocStyle(family, name, parentStyle);
            }
        }
        else currentStyle = new OpenDocStyle(family, name);
        styles.addStyle(currentStyle);
        currentElement.start(parentFilter());
    }

    private void startFontFace() throws SAXException
    {
        // TODO Auto-generated method stub
        currentElement.start(parentFilter());
        String ooFaceName = currentElement.getAttributes().getValue("style:name"); 
        faces.put(ooFaceName, currentElement);
        String fontFamily = currentElement.getAttributes()
                            .getValue("svg:font-family");
        if (converterMap.containsKey(fontFamily))
        {
            CharConverter cc = converterMap.get(fontFamily);
            faceMap.put(ooFaceName, new OOFaceConverter(ooFaceName,cc));
        }
    }

    private void startFontFaceDecls() throws SAXException
    {
        // TODO Auto-generated method stub
        currentElement.start(parentFilter());
    }

    private XMLFilterImpl parentFilter()
    {
        if (getParent() instanceof XMLFilterImpl)
            return (XMLFilterImpl) getParent();
        return null;
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
