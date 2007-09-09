/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import java.util.HashSet;
import java.util.StringTokenizer;
import java.util.TreeMap;

import javax.swing.filechooser.FileFilter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.DocInterface;

/**
 * @author keith
 *
 */
public class ExtensionConversionMode extends ConversionMode
{
    private static TreeMap <String,ConversionMode>map = new TreeMap <String,ConversionMode>();
    private String extId = null;
    private HashSet <String> fileExtensions = new HashSet(); 
    /**
     * @param id
     * @param name
     * @param styleSupport
     */
    protected ExtensionConversionMode(String extId, int id, String name, boolean styleSupport)
    {
        super(id, name, styleSupport);
        this.extId = extId;
        
    }
    
    private void setExtensions(String extensions)
    {
        StringTokenizer st = new StringTokenizer(", ");
        while (st.hasMoreTokens())
        {
            fileExtensions.add(st.nextToken());
        }
    }
    

    /* (non-Javadoc)
     * @see org.thanlwinsoft.doccharconvert.ConversionMode#getFileFilter()
     */
    @Override
    public FileFilter getFileFilter()
    {
        final String name = toString();
        FileFilter fileFilter = new FileFilter() {
            public boolean accept(java.io.File f)
            {
                String lcn = f.getName().toLowerCase();
                int extStart = lcn.lastIndexOf('.');
                if (extStart > -1 && 
                    fileExtensions.contains(lcn.substring(extStart)))
                    return true;
                // need to add directory to allow directory browsing
                if (f.isDirectory()) return true;
                return false;
            }
            public String getDescription() { return name; }
        };
        return fileFilter;
    }

    public static ConversionMode [] getExtensionModes()
    {
        if (map.size() > 0) 
            return map.values().toArray(new ConversionMode[map.size()]);
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("org.thanlwinsoft.doccharconvert.parser");
        if (point == null) return null;
        IExtension[] extensions = point.getExtensions();
        for (int i = 0; i < extensions.length; i++)
        {
            IConfigurationElement ce[] = extensions[i].getConfigurationElements();
            for (int j = 0; j < ce.length; j++)
            {
                String name = ce[j].getAttribute("name");
                String id = ce[j].getAttribute("id");
                if (name == null) // a null name causes problems
                    name = id;
                String hasStylesText = ce[j].getAttribute("styleSupport");
                
                boolean hasStyles = Boolean.parseBoolean(hasStylesText);
                String fileExtensions = ce[j].getAttribute("extensions");
                int numericId = ConversionMode.NUM_MODES + i;
                ExtensionConversionMode mode =
                    new ExtensionConversionMode(id, numericId, name, hasStyles);
                if (fileExtensions != null)
                    mode.setExtensions(fileExtensions);
                map.put(id, mode);
            }
        }
        return map.values().toArray(new ConversionMode[map.size()]);
    }
    
    public DocInterface getDocInterface()
    {
        DocInterface doc = null;
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry.getExtensionPoint("org.thanlwinsoft.doccharconvert.parser");
        if (point == null) return null;
        IExtension[] extensions = point.getExtensions();
        
        for (int i = 0; i < extensions.length; i++)
        {
            IConfigurationElement ce[] = extensions[i].getConfigurationElements();
            for (int j = 0; j < ce.length; j++)
            {
                if (ce[j].getAttribute("id").equals(extId))
                {
                    try
                    {
                        doc = (DocInterface)ce[j].createExecutableExtension("class");
                    }
                    catch (CoreException e)
                    {
                        DocCharConvertEclipsePlugin.log(IStatus.ERROR, e.getMessage(), e);
                    }
                }
            }
        }
        return doc;
    }
}
