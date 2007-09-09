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
 * $LastChangedBy: keith $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package org.thanlwinsoft.doccharconvert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.InputStream;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ReversibleConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
import org.thanlwinsoft.doccharconvert.opendoc.ScriptType;
/**
 *
 * @author  keith
 */
public class ConverterXmlParser
{
    public final static String TOP_NODE = "DocCharConverter";
    public final static String CLASS_NODE = "ConverterClass";
    public final static String NAME_ATTRIB = "name"; 
    public final static String REVERSE_NAME_ATTRIB = "rname"; 
    public final static String PARAMETER_NODE = "Parameter";
    public final static String ARGUMENT_NODE = "Argument";
    public final static String TYPE_ATTRIB = "type";
    public final static String VALUE_ATTRIB = "value";
    public final static String SCRIPT_ATTRIB = "script";
    public final static String STYLES_NODE = "Styles";
    public final static String STYLE_NODE = "Style";
    public final static String FONT_NODE = "Font";
    public final static String OLD = "old";
    public final static String NEW = "new";
    public final static String EXT = ".dccx";
    File converterDir = null;
    File currentXmlFile = null;
    Vector<CharConverter> rawConverters = null;
    Vector<ChildConverter> converters = null;
    StringBuffer errorLog = null;
    ProgressNotifier notifier = new ProgressNotifier();
    /** Creates a new instance of ConverterXmlParser */
    public ConverterXmlParser(File converterDir)
    {
        this.converterDir = converterDir;
        this.converters = new Vector<ChildConverter>();
        this.rawConverters = new Vector<CharConverter>();
        this.errorLog = new StringBuffer();
    }
    public ConverterXmlParser()
    {
        this.converters = new Vector<ChildConverter>();
        this.errorLog = new StringBuffer();
    }
    public static File [] getConverterFiles(File converterDir)
    {
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name)
            {
                return name.toLowerCase().endsWith(EXT);
            }
        };
        if (converterDir == null) 
        {
            return null;
        }
        return converterDir.listFiles(filter);
    }
    public boolean parse()
    {
        File [] files = getConverterFiles(converterDir);
        if (files == null) 
        {
            errorLog.append(Config.getCurrent().getMsgResource().getString("noConvDir"));
            return false;
        }
        notifier.beginTask(MessageUtil.getString("ConverterXmlParser_parsing"),
                           files.length);
        for (int i = 0; i<files.length; i++)
        {
            if (files[i].canRead())
            {
                notifier.subTask(files[i].getName());
                parseFile(files[i]);
                notifier.worked(i);
            }
            if (notifier.isCancelled()) break;
        }
        notifier.done();
        if (errorLog.length() > 0) return false;        
        return true;
    }
    public Vector<CharConverter> getConverters()
    {
        return rawConverters;
    }
    public Vector<ChildConverter> getChildConverters()
    {
        return converters;
    }
    public String getErrorLog()
    {
        return errorLog.toString();
    }
    public boolean parseFile(File xmlFile)
    {
        try
        {
            currentXmlFile = xmlFile;
            return parseStream(xmlFile.toURI().toURL().openStream());
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
            errorLog.append(ioe.getLocalizedMessage());
            errorLog.append('\n');
            return false;
        }   
    }
    
    public boolean parseStream(InputStream fileStream)   
    {
        org.w3c.dom.Document doc = null;
        try 
        {
            DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dfactory.newDocumentBuilder();
            InputSource inputSource = new InputSource(fileStream);
            doc = docBuilder.parse(inputSource);            
        }
        catch (ParserConfigurationException pce)
        {
            System.out.println(pce.getMessage());
            errorLog.append(pce.getLocalizedMessage());
            errorLog.append('\n');
        }
        catch (SAXException se)
        {
            System.out.println(se.getMessage());
            errorLog.append(se.getLocalizedMessage());
            errorLog.append('\n');
        }
        catch (IOException ioe)
        {
            System.out.println(ioe.getMessage());
            errorLog.append(ioe.getLocalizedMessage());
            errorLog.append('\n');
        }
        if (doc == null)
        {
            errorLog.append("Failed to parse file");
            errorLog.append('\n');
            return false;
        }
        try
        {
            doc.normalize();
            Node topNode = doc.getFirstChild();
            if (!topNode.getNodeName().equals(TOP_NODE) ||
                topNode.getNodeType() != Node.ELEMENT_NODE) 
            {
                if (currentXmlFile != null) 
                    errorLog.append(currentXmlFile.getAbsolutePath());
                errorLog.append(": DocCharConverter Element is not first node in file.\n");
                return false;
            }
            Element topElement = (Element)topNode;
            String converterName = topElement.getAttribute(NAME_ATTRIB);
            if ((converterName == null || converterName.length() == 0) &&
                currentXmlFile != null)
                converterName = currentXmlFile.getName();
            String reverseName = converterName
                + Config.messageResource().getString("reversed");
            if (topElement.hasAttribute(REVERSE_NAME_ATTRIB))
            {
                if (topElement.getAttribute(REVERSE_NAME_ATTRIB) != null &&
                    topElement.getAttribute(REVERSE_NAME_ATTRIB).length() > 0)
                {
                  reverseName = topElement.getAttribute(REVERSE_NAME_ATTRIB);
                }
            }
            
            NodeList classList = doc.getElementsByTagName(CLASS_NODE);
            if (classList.getLength() != 1)
            {
                if (currentXmlFile != null) 
                    errorLog.append(currentXmlFile.getAbsolutePath());
                errorLog.append(": You must have one ConverterClass Element per file\n");
                return false;
            }
            Element classElement = (Element)classList.item(0);
            String className = classElement.getAttribute(NAME_ATTRIB);
            // find the constructor arguments
            NodeList parameters = classElement.getElementsByTagName(ARGUMENT_NODE);
            Class [] argumentTypes = new Class[parameters.getLength()];
            Object [] arguments = new Object[parameters.getLength()];
            for (int p = 0; p<parameters.getLength(); p++)
            {
                Element parameter = (Element)parameters.item(p);
                arguments[p] = createParameter(parameter, currentXmlFile);
                if (arguments[p] == null)
                {
                    argumentTypes[p] = null;
                }
                else
                {
                    argumentTypes[p] = getClassFromParameter(arguments[p]);
                }
            }
            Class ccc = Class.forName(className);
            Constructor constructor = ccc.getConstructor(argumentTypes);
            Object cco = constructor.newInstance(arguments);
            if (!(cco instanceof CharConverter))
            {
                errorLog.append(className);
                errorLog.append(" is not a CharConverter!\n");
                return false;
            }
                
            CharConverter masterConverter = (CharConverter) cco;
            masterConverter.setName(converterName);
            rawConverters.add(masterConverter);
            ReversibleConverter reverseConverter = null;
            if (masterConverter instanceof ReversibleConverter)
            {
                reverseConverter = (ReversibleConverter)
                    constructor.newInstance(arguments);
                reverseConverter.setDirection(false);
                reverseConverter.setName(converterName);
                reverseConverter.setReverseName(reverseName);
                rawConverters.add(reverseConverter);
            }
            // now find the parameter arguments
            parameters = classElement.getElementsByTagName(PARAMETER_NODE);
            for (int p = 0; p<parameters.getLength(); p++)
            {
                Element parameter = (Element)parameters.item(p);
                String fieldName = parameter.getAttribute(NAME_ATTRIB);
                // put a check in that no one is messing with the direction
                if (fieldName.equals("direction"))
                {
                    errorLog.append("direction parameter ignored for reversible converters");
                    continue;
                }
                String setterName = "set" +
                    fieldName.substring(0,1).toUpperCase() +
                    fieldName.substring(1);
                Object value = createParameter(parameter, currentXmlFile);
                if (value != null)
                {
                    Class [] argClass = {getClassFromParameter(value)};
                    Method method = ccc.getMethod(setterName, argClass);
                    Object [] arg = {value};
                    method.invoke(masterConverter, arg);
                    if (reverseConverter != null)
                    {
                        method.invoke(reverseConverter, arg);
                    }
                }
            }
            // now we are ready to create the style options
            NodeList styles = topElement.getElementsByTagName(STYLES_NODE);
            if (styles.getLength() != 1)
            {
                if (currentXmlFile != null) 
                    errorLog.append(currentXmlFile.getAbsolutePath());
                errorLog.append(": You must have one Style Element per file\n");
                return false;
            }
            NodeList styleList = ((Element)styles.item(0))
                .getElementsByTagName(STYLE_NODE);
            for (int s=0; s<styleList.getLength(); s++)
            {
                addConverter((Element)styleList.item(s), masterConverter,
                    reverseConverter);
            }
        }
        catch (InvocationTargetException e)
        {
            System.out.println(e.getLocalizedMessage());
            errorLog.append(currentXmlFile.getAbsolutePath());
            errorLog.append('\n');
            errorLog.append(e.getLocalizedMessage());
            errorLog.append('\n');
        }
        catch (InstantiationException e)
        {
            System.out.println(e.getLocalizedMessage());
            errorLog.append(currentXmlFile.getAbsolutePath());
            errorLog.append('\n');
            errorLog.append(e.getLocalizedMessage());
            errorLog.append('\n');
        }
        catch (IllegalAccessException e)
        {
            System.out.println(e.getLocalizedMessage());
            errorLog.append('\n');
            errorLog.append(e.getLocalizedMessage());
            errorLog.append('\n');
        }
        catch (ClassNotFoundException e)
        {
            System.out.println(e.getLocalizedMessage());
            errorLog.append(e.getLocalizedMessage());
            errorLog.append('\n');
        }
        catch (NoSuchMethodException e)
        {
            System.out.println(e.getLocalizedMessage());
            errorLog.append(e.getLocalizedMessage());
            errorLog.append('\n');
        }
        if (errorLog.length()>0) return false;
        return true;
    }
    protected void addConverter(Element sElement, CharConverter master,
        CharConverter reverseMaster)
    {
        TextStyle oldStyle = null;
        TextStyle newStyle = null;
        NodeList fonts = sElement.getElementsByTagName(FONT_NODE);
        for (int f = 0; f<fonts.getLength(); f++)
        {
            Element e = (Element)fonts.item(f);
            String sTypeName = e.getAttribute(SCRIPT_ATTRIB);
            ScriptType.Type scriptType = ScriptType.Type.LATIN;
            if (sTypeName.equals("ctl")) scriptType = ScriptType.Type.COMPLEX;
            else if (sTypeName.equals("cjk")) scriptType = ScriptType.Type.CJK;
            if (e.getAttribute(TYPE_ATTRIB).equals(OLD))
            {
                oldStyle = new FontStyle(e.getAttribute(NAME_ATTRIB));
                oldStyle.setScriptType(scriptType);
            }
            else if (e.getAttribute(TYPE_ATTRIB).equals(NEW))
            {
                newStyle = new FontStyle(e.getAttribute(NAME_ATTRIB));
                newStyle.setScriptType(scriptType);
            }
            else 
            {
                errorLog.append(e.getAttribute(TYPE_ATTRIB));
                errorLog.append(" Font type attribute must be \"old\" or \"new\"");
                errorLog.append('\n');
            }
        }
        ChildConverter cc = new ChildConverter(oldStyle, newStyle, master);
        if (!(converters.add(cc))) 
            errorLog.append("Failed to add converter.");
        if (reverseMaster != null)
        {
            cc = new ChildConverter(newStyle, oldStyle, reverseMaster);
            if (!converters.add(cc))
                errorLog.append("Failed to add converter.");
        }
    }
    
    Object createParameter(Element parameter, File xmlFile)
    {
        String type = parameter.getAttribute(TYPE_ATTRIB);
        if (type.equals("String"))
        {
            return new String(parameter.getAttribute(VALUE_ATTRIB));
        }
        else if (type.equals("File"))
        {
            if (xmlFile == null) return null;
            File testFile = 
                new File(parameter.getAttribute(VALUE_ATTRIB));
            // if file does not exist, see if it is in the 
            // current directory
            if (!testFile.exists())
            {
                testFile = 
                    new File(xmlFile.getParentFile(),parameter.getAttribute(VALUE_ATTRIB));
            }
            if (!testFile.exists())
            {
                // warn if file does not exist, but carry on anyway
                errorLog.append(testFile.getAbsolutePath());
                errorLog.append(" does not exist.\n");
            }
            return testFile;

        }
        else if (type.equals("int"))
        {
            
            return new Integer(parameter.getAttribute(VALUE_ATTRIB));
        }
        else if (type.equals("double"))
        {
            
            return new Double(parameter.getAttribute(VALUE_ATTRIB));
        }
        else if (type.equals("boolean"))
        {
            
            return new Boolean(parameter.getAttribute(VALUE_ATTRIB));
        }    
        else 
        {
            System.out.println("Invalid parameter type "  + type);
            return null;
        }
    }
    Class getClassFromParameter(Object obj)
    {
        Class type = obj.getClass();
        // change primitive types
        if (type == Integer.class)
        {
            type = int.class;
        }
        else if (type == Boolean.class)
        {
            type = boolean.class;
        }
        else if (type == Double.class)
        {
            type = double.class;
        }
        return type; 
    }
    public void setProgressNotifier(ProgressNotifier pn)
    {
        this.notifier = pn;
    }
}
