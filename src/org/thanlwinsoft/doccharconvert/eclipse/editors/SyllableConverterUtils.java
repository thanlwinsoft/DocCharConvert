/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.Component;
import org.thanlwinsoft.schemas.syllableParser.ComponentRef;
import org.thanlwinsoft.schemas.syllableParser.Classes;
import org.thanlwinsoft.schemas.syllableParser.Map;
import org.thanlwinsoft.schemas.syllableParser.Script;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.w3c.dom.Node;

/**
 * @author keith
 *
 */
public class SyllableConverterUtils
{
    public static String getComponentName(SyllableConverter sc, String ref)
    {
        for (Script s : sc.getScriptArray())
        {
            for (Component c : s.getCluster().getComponentArray())
            {
                if (c.getId().equals(ref))
                {
                    StringBuilder sb = new StringBuilder();
                    Node n = c.getDomNode().getFirstChild();
                    while (n != null)
                    {
                        sb.append(n.getNodeValue());
                        n = n.getNextSibling();
                    }
                    return sb.toString();
                }
            }
        }
        return ref;
    }
    public static String getCText(C c)
    {
        if (c == null)
            return "";
        if (c.isSetHex())
        {
            StringBuilder sb = new StringBuilder();
            for (String hex : c.getHex().split("\\s"))
            {
                int codePoint = Integer.parseInt(hex, 16);
                sb.append(Character.toChars(codePoint));
            }
            return sb.toString();
        }
        if (c.isSetClass1())
            return "<" + c.getClass1() + ">";
        return c.getStringValue();
    }
    
    public static C getCFromMap(Map m, String ref)
    {
        for (C c : m.getCArray())
        {
            if (c.isSetR() && c.getR().equals(ref))
            {
                return c;
            }
        }
        return null;
    }
    
    public static Vector<String> getApplicableClasses(SyllableConverter sc, String colRef)
    {
        Classes classes =  sc.getClasses();
        Vector <String>classRefs = new Vector<String>(classes.sizeOfClass1Array());
        for (org.thanlwinsoft.schemas.syllableParser.Class c : classes.getClass1Array())
        {
            for (ComponentRef cr : c.getComponentArray())
            {
                if (colRef.equals(cr.getR()))
                {
                    classRefs.add(c.getId());
                }
            }
        }
        //return classRefs.toArray(new String[classRefs.size()]);
        return classRefs;
    }
}
