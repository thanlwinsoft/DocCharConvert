/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IStatus;
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
    public static ConverterXmlParser parseConverters(IRunnableContext rc, Shell shell)
    {
        return parseConverters(rc, shell, null);
    }
    public static ConverterXmlParser parseConverters(IRunnableContext rc, Shell shell, ListViewer viewer)
    {
        ConverterXmlParser xmlParser = new ConverterXmlParser(getConverterPath());
        
        ParseRunnable pr = new ParseRunnable(xmlParser, shell.getDisplay(), viewer);
        try
        {
            rc.run(true, false, pr);
            if (xmlParser.getErrorLog().length() > 0)
            {
                MessageBox msgBox = new MessageBox(shell, SWT.ICON_WARNING);
                msgBox.setMessage(MessageUtil.getString("ConverterParseError") + 
                                  xmlParser.getErrorLog());
                msgBox.open();
            }
        }
        catch (InvocationTargetException e)
        {
            MessageDialog.openError(shell, "Error", 
                    "Error converting: InvocationTargetException " + 
                    e.getMessage());
        }
        catch (InterruptedException e)
        {
            MessageDialog.openError(shell, "Error", 
                    "Error converting: InterruptedException " + e.getMessage());
        }
        return xmlParser;
    }
    
    static File getConverterPath()
    {
        File converterConfigPath = Config.getCurrent().getConverterPath();
        if (converterConfigPath.isDirectory() == false)
        {
            PreferencesInitializer initPref = new PreferencesInitializer();
            
            IPreferenceStore prefStore = initPref.getPrefStore();
            
            converterConfigPath = 
                new File(prefStore.getString(Config.CONVERTER_CONFIG_PATH));
            DocCharConvertEclipsePlugin.log(IStatus.WARNING,
                            "Using pref store directly. " + 
                            converterConfigPath, null);
            
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
        DocCharConvertEclipsePlugin.log(IStatus.INFO, "config dir:" + 
                converterConfigPath.getAbsolutePath(), null);
        return converterConfigPath;
    }
}
