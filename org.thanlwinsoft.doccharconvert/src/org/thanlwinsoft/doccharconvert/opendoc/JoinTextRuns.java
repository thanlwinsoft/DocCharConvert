/**
 * 
 */
package org.thanlwinsoft.doccharconvert.opendoc;

import java.io.IOException;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * @author keith
 *
 */
public class JoinTextRuns extends XMLFilterImpl
{
    StringBuilder textBuffer = new StringBuilder();
    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        textBuffer.append(ch, start, length);
        //if (textBuffer.toString().contains("'"))
        //    System.out.println(textBuffer.toString());
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
    {
        // TODO Auto-generated method stub
        return super.resolveEntity(publicId, systemId);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#endElement(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        outputText();
        super.endElement(uri, localName, qName);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.XMLFilterImpl#startElement(java.lang.String, java.lang.String, java.lang.String, org.xml.sax.Attributes)
     */
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        outputText();
        super.startElement(uri, localName, qName, atts);
    }
    
    private void outputText() throws SAXException
    {
        if (textBuffer.length() > 0)
        {
            super.characters(textBuffer.toString().toCharArray(), 0, textBuffer.length());
            System.out.println(textBuffer.toString());
            textBuffer.delete(0, textBuffer.length());
        }
    }
}
