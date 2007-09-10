/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizard;
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

/**
 * @author keith
 * 
 */
public class ConverterUtil
{
    private static final String CONVERTER_DIR_ELEMENT = "converterDir";
    private static final String PATH_ATTR = "path";

    public static ConverterXmlParser parseConverters(IRunnableContext rc,
            Shell shell)
    {
        return parseConverters(rc, shell, null);
    }

    public static ConverterXmlParser parseConverters(IRunnableContext rc,
            Shell shell, ListViewer viewer)
    {

        ConverterXmlParser xmlParser = new ConverterXmlParser(
                getConverterPaths());

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

    private static File[] getConverterPaths()
    {
        ArrayList<File> files = new ArrayList<File>();
        files.add(getConverterPath());
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
                if (ce[i].getName().equals(CONVERTER_DIR_ELEMENT))
                {
                    String path = ce[i].getAttribute(PATH_ATTR);
                    String plugin = extensions[i].getContributor().getName();
                    Bundle b = Platform.getBundle(plugin);
                    URL url = b.getResource(path);
                    if (url.getProtocol().equals("file"))
                    {
                        try
                        {
                            File f = new File(url.toURI());
                            if (f.canRead())
                            {
                                files.add(f);
                            }
                            else
                            {
                                DocCharConvertEclipsePlugin.log(
                                        IStatus.WARNING, "File "
                                                + f.getAbsolutePath()
                                                + " can't be read.");
                            }
                        }
                        catch (URISyntaxException e)
                        {
                            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                                    "URISyntaxException for " + path, e);
                        }

                    }
                }
            }
        }
        return files.toArray(new File[files.size()]);
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
}
