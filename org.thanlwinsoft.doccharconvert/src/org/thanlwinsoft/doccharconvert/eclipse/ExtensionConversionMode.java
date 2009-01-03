/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.doccharconvert.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
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
import org.osgi.framework.Bundle;
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
    private HashSet <String> mFileExtensions = new HashSet<String>();
    private String mOptions;
    private String mPlugin;
    /**
     * @param id
     * @param name
     * @param styleSupport
     */
    protected ExtensionConversionMode(String plugin, String extId, int id, String name, boolean styleSupport)
    {
        super(id, name, styleSupport);
        this.extId = extId;
        this.mPlugin = plugin;
    }
    
    private void setExtensions(String extensions)
    {
        StringTokenizer st = new StringTokenizer(", ");
        while (st.hasMoreTokens())
        {
            mFileExtensions.add(st.nextToken());
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
                    mFileExtensions.contains(lcn.substring(extStart)))
                    return true;
                // need to add directory to allow directory browsing
                if (f.isDirectory()) return true;
                return false;
            }
            public String getDescription() { return name; }
        };
        return fileFilter;
    }

    /**
     * @return array of modes
     */
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
                String options = ce[j].getAttribute("options");
                int numericId = ConversionMode.NUM_MODES + i;
                String plugin = extensions[i].getContributor().getName();
                ExtensionConversionMode mode =
                    new ExtensionConversionMode(plugin, id, numericId, name, hasStyles);
                if (fileExtensions != null)
                    mode.setExtensions(fileExtensions);
                if (options != null)
                    mode.setOptions(options);
                map.put(id, mode);
            }
        }
        return map.values().toArray(new ConversionMode[map.size()]);
    }
    
    private void setOptions(String options)
    {
        this.mOptions = options;
    }
    /**
     * @return description of options
     */
    public String getOptions() { return mOptions; }
    
    /** opens an input stream on a path from the plugin that defined this 
     * extension.
     * @param path
     * @return stream or null, if the path wasn't found.
     * @throws IOException
     */
    public InputStream getPath(String path) throws IOException
    {
        Bundle b = Platform.getBundle(mPlugin);
        if (b == null) return null;
        URL url = b.getEntry(path);
        if (url != null)
            return url.openStream();
        return null;
    }

    /**
     * @return the document interface implemented by the extension
     */
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
                        doc.setMode(this);
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
