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
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.ConverterXmlParser;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.doccharconvert.eclipse.PreferencesInitializer;
import org.thanlwinsoft.doccharconvert.eclipse.wizard.ParseRunnable;
import org.thanlwinsoft.util.BundleClassLoaderUtil;
import org.thanlwinsoft.util.Pair;

/**
 * @author keith
 * 
 */
public class ConverterUtil
{
    private static final String CONVERTER_DIR_ELEMENT = "converterDir";
    private static final String PATH_ATTR = "path";
    private static final String SEARCH_PATTERN = "*" + ConverterXmlParser.EXT;

    public static ConverterXmlParser parseConverters(IRunnableContext rc,
            Shell shell)
    {
        return parseConverters(rc, shell, null);
    }

    public static ConverterXmlParser parseConverters(IRunnableContext rc,
            Shell shell, ListViewer viewer)
    {
        Pair<URL[], Bundle[]> paths = getConverterPaths();
        ConverterXmlParser xmlParser = new ConverterXmlParser(paths.first,
                new BundleClassLoaderUtil(paths.second));

        ParseRunnable pr = new ParseRunnable(xmlParser, shell.getDisplay(),
                viewer);
        try
        {
            rc.run(true, false, pr);
            if (xmlParser.getErrorLog().length() > 0)
            {
                MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING);
                msgBox.setMessage(MessageUtil.getString("ConverterParseError")
                        + xmlParser.getErrorLog());
                msgBox.open();
            }
        }
        catch (InvocationTargetException e)
        {
            MessageDialog.openError(shell, "Error",
                    "Error converting: InvocationTargetException "
                            + e.getMessage());
        }
        catch (InterruptedException e)
        {
            MessageDialog.openError(shell, "Error",
                    "Error converting: InterruptedException " + e.getMessage());
        }
        return xmlParser;
    }

    private static Pair<URL[], Bundle[]> getConverterPaths()
    {
        ArrayList<URL> files = new ArrayList<URL>();
        ArrayList<Bundle> bundles = new ArrayList<Bundle>();
        try
        {
            File userDir = getConverterPath();
            File[] converters = userDir.listFiles(new FilenameFilter()
            {
                public boolean accept(File dir, String name)
                {
                    return name.endsWith(ConverterXmlParser.EXT);
                }
            });
            for (File f : converters)
            {
                files.add(f.toURI().toURL());
            }
        }
        catch (MalformedURLException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                    "getConverterPaths()", e);
        }
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint point = registry
                .getExtensionPoint("org.thanlwinsoft.doccharconvert.converters");
        if (point == null)
            return null;
        IExtension[] extensions = point.getExtensions();
        for (int i = 0; i < extensions.length; i++)
        {
            IConfigurationElement ce[] = extensions[i]
                    .getConfigurationElements();
            for (int j = 0; j < ce.length; j++)
            {
                if (ce[j].getName().equals(CONVERTER_DIR_ELEMENT))
                {
                    String path = ce[j].getAttribute(PATH_ATTR);
                    String plugin = extensions[i].getContributor().getName();
                    Bundle b = Platform.getBundle(plugin);
                    Enumeration<?> converters = b.findEntries(path,
                            SEARCH_PATTERN, true);
                    while (converters.hasMoreElements())
                    {
                        Object o = converters.nextElement();
                        if (o instanceof URL)
                        {
                            files.add((URL) o);
                            if (!bundles.contains(b))
                                bundles.add(b);
                        }
                    }
                }
            }
        }
        return new Pair<URL[], Bundle[]>(files.toArray(new URL[files.size()]), 
                bundles.toArray(new Bundle[bundles.size()]));
    }

    static File getConverterPath()
    {
        File converterConfigPath = Config.getCurrent().getConverterPath();
        if (converterConfigPath.isDirectory() == false)
        {
            PreferencesInitializer initPref = new PreferencesInitializer();

            IPreferenceStore prefStore = initPref.getPrefStore();

            converterConfigPath = new File(prefStore
                    .getString(Config.CONVERTER_CONFIG_PATH));
            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                    "Using pref store directly. " + converterConfigPath, null);

        }
        if (converterConfigPath.isDirectory() == false)
        {
            Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                    .getShell();
            MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING);
            msgBox.setMessage(MessageUtil.getString("NoConverterPath",
                    converterConfigPath.getAbsolutePath()));
            msgBox.open();
        }
        DocCharConvertEclipsePlugin.log(IStatus.INFO, "config dir:"
                + converterConfigPath.getAbsolutePath(), null);
        return converterConfigPath;
    }
    
    public static String cononicalizeName(String name)
    {
        // what about unicode characters?
        return name.replaceAll("[=:></\\$&*?]*", "");
    }
}
