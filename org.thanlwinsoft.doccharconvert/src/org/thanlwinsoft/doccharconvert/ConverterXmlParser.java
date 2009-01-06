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

package org.thanlwinsoft.doccharconvert;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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
import org.thanlwinsoft.util.IClassLoaderUtil;
import org.thanlwinsoft.util.SimpleClassLoaderUtil;

/**
 * 
 * @author keith
 */
public class ConverterXmlParser
{
    protected final static String TOP_NODE = "DocCharConverter";
    protected final static String CLASS_NODE = "ConverterClass";
    protected final static String NAME_ATTRIB = "name";
    protected final static String REVERSE_NAME_ATTRIB = "rname";
    protected final static String PARAMETER_NODE = "Parameter";
    protected final static String ARGUMENT_NODE = "Argument";
    protected final static String TYPE_ATTRIB = "type";
    protected final static String VALUE_ATTRIB = "value";
    protected final static String SCRIPT_ATTRIB = "script";
    protected final static String STYLES_NODE = "Styles";
    protected final static String STYLE_NODE = "Style";
    protected final static String FONT_NODE = "Font";
    protected final static String OLD = "old";
    protected final static String NEW = "new";
    /**
     * DocCharConvert configuration file extension
     */
    public final static String EXT = ".dccx";
    /**
     * namespace for Dccx files
     */
    public final static String NAMESPACE = 
        "http://www.thanlwinsoft.org/schemas/DocCharConvert";
    List<File> converterDir = null;
    List<URL> converterUrls = null;
    // File currentXmlFile = null;
    URL currentXmlUrl = null;
    Vector<CharConverter> rawConverters = null;
    Vector<ChildConverter> converters = null;
    StringBuffer errorLog = null;
    ProgressNotifier notifier = new ProgressNotifier();
    private IClassLoaderUtil mLoaderUtil = null;

    /**
     * Creates a new instance of ConverterXmlParser
     * @param converterDirs 
     * @param loader 
     * 
     * @param conveterDirs
     */
    public ConverterXmlParser(File[] converterDirs, IClassLoaderUtil loader)
    {
        this.converterDir = new ArrayList<File>();
        for (File dir : converterDirs)
        {
            this.converterDir.add(dir);
        }
        this.converters = new Vector<ChildConverter>();
        this.rawConverters = new Vector<CharConverter>();
        this.errorLog = new StringBuffer();
        this.mLoaderUtil = loader;
    }

    /**
     * 
     * @param converterUrls
     * @param loader
     */
    public ConverterXmlParser(URL[] converterUrls, IClassLoaderUtil loader)
    {
        this.converterDir = new ArrayList<File>();
        this.converterUrls = new ArrayList<URL>();
        for (URL url : converterUrls)
        {
            this.converterUrls.add(url);
        }
        this.converters = new Vector<ChildConverter>();
        this.rawConverters = new Vector<CharConverter>();
        this.errorLog = new StringBuffer();
        this.mLoaderUtil = loader;
    }

    /**
     * Creates a new instance of ConverterXmlParser
     * 
     * @param converterDir
     */
    public ConverterXmlParser(File converterDir)
    {
        this.converterDir = new ArrayList<File>();
        this.converterDir.add(converterDir);
        this.converters = new Vector<ChildConverter>();
        this.rawConverters = new Vector<CharConverter>();
        this.errorLog = new StringBuffer();
        this.mLoaderUtil = new SimpleClassLoaderUtil();
    }

    /**
     * default constructor
     */
    public ConverterXmlParser()
    {
        this.converters = new Vector<ChildConverter>();
        this.errorLog = new StringBuffer();
    }

    /**
     * 
     * @param converterDir
     * @return array of files
     */
    public static File[] getConverterFiles(File converterDir)
    {
        FilenameFilter filter = new FilenameFilter()
        {
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

    /**
     * 
     * @return true on successful parse
     */
    public boolean parse()
    {
        List<File> files = new ArrayList<File>();
        for (File dir : converterDir)
        {
            File[] dirFiles = getConverterFiles(dir);
            if (files == null)
            {
                errorLog.append(Config.getCurrent().getMsgResource().getString(
                        "noConvDir"));
                continue;
            }
            if (dirFiles != null)
            {
                for (File f : dirFiles)
                    files.add(f);
            }
        }
        notifier.beginTask(MessageUtil.getString("ConverterXmlParser_parsing"),
                files.size());
        for (int i = 0; i < files.size(); i++)
        {
            if (files.get(i).canRead())
            {
                notifier.subTask(files.get(i).getName());
                parseFile(files.get(i));
                notifier.worked(i);
            }
            if (notifier.isCancelled())
                break;
        }
        if (converterUrls != null)
        {
            for (URL url : converterUrls)
            {
                if (notifier.isCancelled())
                    break;
                InputStream is = null;
                try
                {
                    notifier.subTask(url.getFile());
                    currentXmlUrl = url;
                    is = url.openStream();
                    if (is != null)
                        parseStream(is);
                    notifier.worked(1);
                }
                catch (IOException e)
                {
                    errorLog.append(MessageUtil.getString("FailedOpenConverter", e
                            .getLocalizedMessage()));
                }
                finally
                {
                    try
                    {
                        is.close();
                    }
                    catch (IOException e)
                    {
                        errorLog.append(MessageUtil.getString(
                                "FailedClsoeConverter", e.getLocalizedMessage()));
                    }
                }
            }
        }
        notifier.done();
        if (errorLog.length() > 0)
            return false;
        return true;
    }

    /**
     * 
     * @return converters without styles
     */
    public Vector<CharConverter> getConverters()
    {
        return rawConverters;
    }

    /**
     * 
     * @return ChildConverter vector
     */
    public Vector<ChildConverter> getChildConverters()
    {
        return converters;
    }

    /**
     * 
     * @return log as String
     */
    public String getErrorLog()
    {
        return errorLog.toString();
    }

    /**
     * 
     * @param xmlFile
     * @return true i file parsed successfully
     */
    public boolean parseFile(File xmlFile)
    {
        try
        {
            currentXmlUrl = xmlFile.toURI().toURL();
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

    /**
     * 
     * @param fileStream
     * @return true if stream was parsed successfully
     */
    public boolean parseStream(InputStream fileStream)
    {
        org.w3c.dom.Document doc = null;
        try
        {
            DocumentBuilderFactory dfactory = DocumentBuilderFactory
                    .newInstance();
            dfactory.setNamespaceAware(true);
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
            if (!topNode.getLocalName().equals(TOP_NODE)
                    || topNode.getNodeType() != Node.ELEMENT_NODE)
            {
                if (currentXmlUrl != null)
                    errorLog.append(currentXmlUrl.toExternalForm());
                errorLog
                        .append(": DocCharConverter Element is not first node in file.\n");
                return false;
            }
            Element topElement = (Element) topNode;
            String converterName = topElement.getAttribute(NAME_ATTRIB);
            if ((converterName == null || converterName.length() == 0)
                    && currentXmlUrl != null)
                converterName = currentXmlUrl.getFile();
            String reverseName = converterName
                    + Config.messageResource().getString("reversed");
            if (topElement.hasAttribute(REVERSE_NAME_ATTRIB))
            {
                if (topElement.getAttribute(REVERSE_NAME_ATTRIB) != null
                        && topElement.getAttribute(REVERSE_NAME_ATTRIB)
                                .length() > 0)
                {
                    reverseName = topElement.getAttribute(REVERSE_NAME_ATTRIB);
                }
            }

            NodeList classList = doc.getElementsByTagNameNS(NAMESPACE, CLASS_NODE);
            if (classList.getLength() != 1)
            {
                if (currentXmlUrl != null)
                    errorLog.append(currentXmlUrl.toExternalForm());
                errorLog
                        .append(": You must have one ConverterClass Element per file\n");
                return false;
            }
            Element classElement = (Element) classList.item(0);
            String className = classElement.getAttribute(NAME_ATTRIB);
            // find the constructor arguments
            NodeList parameters = classElement
                    .getElementsByTagNameNS(NAMESPACE, ARGUMENT_NODE);
            Class<?>[] argumentTypes = new Class[parameters.getLength()];
            Object[] arguments = new Object[parameters.getLength()];
            for (int p = 0; p < parameters.getLength(); p++)
            {
                Element parameter = (Element) parameters.item(p);
                arguments[p] = createParameter(parameter, currentXmlUrl);
                if (arguments[p] == null)
                {
                    argumentTypes[p] = null;
                }
                else
                {
                    argumentTypes[p] = getClassFromParameter(arguments[p]);
                }
            }
            Class<?> ccc = null;
            try
            {
                ccc = Class.forName(className);
            }
            catch (ClassNotFoundException e)
            {
                ccc = mLoaderUtil.loadClass(className);
            }
            
            Constructor<?> constructor = ccc.getConstructor(argumentTypes);
            Object cco = constructor.newInstance(arguments);
            if (!(cco instanceof CharConverter))
            {
                errorLog.append(className);
                errorLog.append(" is not a CharConverter!\n");
                return false;
            }

            CharConverter masterConverter = (CharConverter) cco;
            masterConverter.setClassLoader(mLoaderUtil);
            masterConverter.setName(converterName);
            rawConverters.add(masterConverter);
            ReversibleConverter reverseConverter = null;
            if (masterConverter instanceof ReversibleConverter)
            {
                reverseConverter = (ReversibleConverter) constructor
                        .newInstance(arguments);
                reverseConverter.setDirection(false);
                reverseConverter.setName(converterName);
                reverseConverter.setReverseName(reverseName);
                reverseConverter.setClassLoader(mLoaderUtil);
                rawConverters.add(reverseConverter);
            }
            // now find the parameter arguments
            parameters = classElement.getElementsByTagNameNS(NAMESPACE, PARAMETER_NODE);
            for (int p = 0; p < parameters.getLength(); p++)
            {
                Element parameter = (Element) parameters.item(p);
                String fieldName = parameter.getAttribute(NAME_ATTRIB);
                // put a check in that no one is messing with the direction
                if (fieldName.equals("direction"))
                {
                    errorLog
                            .append("direction parameter ignored for reversible converters");
                    continue;
                }
                String setterName = "set"
                        + fieldName.substring(0, 1).toUpperCase()
                        + fieldName.substring(1);
                Object value = createParameter(parameter, currentXmlUrl);
                if (value != null)
                {
                    Class<?>[] argClass = { getClassFromParameter(value) };
                    Method method = ccc.getMethod(setterName, argClass);
                    Object[] arg = { value };
                    method.invoke(masterConverter, arg);
                    if (reverseConverter != null)
                    {
                        method.invoke(reverseConverter, arg);
                    }
                }
            }
            // now we are ready to create the style options
            NodeList styles = topElement.getElementsByTagNameNS(NAMESPACE, STYLES_NODE);
            if (styles.getLength() != 1)
            {
                if (currentXmlUrl != null)
                    errorLog.append(currentXmlUrl.toExternalForm());
                errorLog.append(": You must have one Style Element per file\n");
                return false;
            }
            NodeList styleList = ((Element) styles.item(0))
                    .getElementsByTagNameNS(NAMESPACE, STYLE_NODE);
            for (int s = 0; s < styleList.getLength(); s++)
            {
                addConverter((Element) styleList.item(s), masterConverter,
                        reverseConverter);
            }
        }
        catch (InvocationTargetException e)
        {
            System.out.println(e.getLocalizedMessage());
            errorLog.append(currentXmlUrl.toExternalForm());
            errorLog.append('\n');
            errorLog.append(e.getLocalizedMessage());
            errorLog.append('\n');
        }
        catch (InstantiationException e)
        {
            System.out.println(e.getLocalizedMessage());
            errorLog.append(currentXmlUrl.toExternalForm());
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
        if (errorLog.length() > 0)
            return false;
        return true;
    }

    protected void addConverter(Element sElement, CharConverter master,
            CharConverter reverseMaster)
    {
        TextStyle oldStyle = null;
        TextStyle newStyle = null;
        NodeList fonts = sElement.getElementsByTagNameNS(NAMESPACE, FONT_NODE);
        for (int f = 0; f < fonts.getLength(); f++)
        {
            Element e = (Element) fonts.item(f);
            String sTypeName = e.getAttribute(SCRIPT_ATTRIB);
            ScriptType.Type scriptType = ScriptType.Type.LATIN;
            if (sTypeName.equals("ctl"))
                scriptType = ScriptType.Type.COMPLEX;
            else
                if (sTypeName.equals("cjk"))
                    scriptType = ScriptType.Type.CJK;
            if (e.getAttribute(TYPE_ATTRIB).equals(OLD))
            {
                oldStyle = new FontStyle(e.getAttribute(NAME_ATTRIB));
                oldStyle.setScriptType(scriptType);
            }
            else
                if (e.getAttribute(TYPE_ATTRIB).equals(NEW))
                {
                    newStyle = new FontStyle(e.getAttribute(NAME_ATTRIB));
                    newStyle.setScriptType(scriptType);
                }
                else
                {
                    errorLog.append(e.getAttribute(TYPE_ATTRIB));
                    errorLog
                            .append(" Font type attribute must be \"old\" or \"new\"");
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

    Object createParameter(Element parameter, URL xmlFile)
    {
        String type = parameter.getAttribute(TYPE_ATTRIB);
        if (type.equals("String"))
        {
            return new String(parameter.getAttribute(VALUE_ATTRIB));
        }
        else
            if (type.equals("File"))
            {
                if (xmlFile == null)
                    return null;
                File testFile = new File(parameter.getAttribute(VALUE_ATTRIB));
                // if file does not exist, see if it is in the
                // current directory
                if (!testFile.exists())
                {
                    if (xmlFile.getProtocol().equals("file"))
                    {
                        //testFile = new File(new File(xmlFile.getPath())
                        
                        try
                        {
                            testFile = new File(new File(xmlFile.toURI())
                                    .getParentFile(), parameter
                                    .getAttribute(VALUE_ATTRIB));
                        }
                        catch (URISyntaxException e)
                        {
                            // try using the raw path
                            testFile = new File(new File(xmlFile.getPath())
                                .getParentFile(), parameter
                                .getAttribute(VALUE_ATTRIB));
                        }
                    }
                    else
                    {
                        String basePath = xmlFile.getPath();
                        if (!xmlFile.getPath().endsWith("/"))
                        {
                            basePath = basePath.substring(0, basePath
                                    .lastIndexOf('/') + 1);
                        }
                        try
                        {
                            URL url = new URL(xmlFile, basePath
                                    + parameter.getAttribute(VALUE_ATTRIB));
                            return url;
                        }
                        catch (MalformedURLException e)
                        {
                            errorLog.append("URL Error "
                                    + parameter.getAttribute(VALUE_ATTRIB)
                                    + e.getLocalizedMessage());
                        }
                    }
                }
                if (!testFile.exists())
                {
                    // warn if file does not exist, but carry on anyway
                    errorLog.append(testFile.getAbsolutePath());
                    errorLog.append(" does not exist.\n");
                }
                return testFile;

            }
            else
                if (type.equals("int"))
                {

                    return new Integer(parameter.getAttribute(VALUE_ATTRIB));
                }
                else
                    if (type.equals("double"))
                    {

                        return new Double(parameter.getAttribute(VALUE_ATTRIB));
                    }
                    else
                        if (type.equals("boolean"))
                        {

                            return new Boolean(parameter
                                    .getAttribute(VALUE_ATTRIB));
                        }
                        else
                        {
                            System.out
                                    .println("Invalid parameter type " + type);
                            return null;
                        }
    }

    Class<?> getClassFromParameter(Object obj)
    {
        Class<?> type = obj.getClass();
        // change primitive types
        if (type == Integer.class)
        {
            type = int.class;
        }
        else
            if (type == Boolean.class)
            {
                type = boolean.class;
            }
            else
                if (type == Double.class)
                {
                    type = double.class;
                }
        return type;
    }

    /**
     * 
     * @param pn
     */
    public void setProgressNotifier(ProgressNotifier pn)
    {
        this.notifier = pn;
    }
    /**
     * 
     * @return Class Loader Utility
     */
    public IClassLoaderUtil getLoaderUtil()
    {
        return mLoaderUtil;
    }
}
