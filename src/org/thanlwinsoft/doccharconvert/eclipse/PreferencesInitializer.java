package org.thanlwinsoft.doccharconvert.eclipse;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.thanlwinsoft.doccharconvert.Config;

public class PreferencesInitializer extends AbstractPreferenceInitializer
{
    public static final String CONVERTERS = Config.CONVERTER_CONFIG_PATH;
    private ScopedPreferenceStore prefStore = null;
    public PreferencesInitializer()
    {
        
    }
    public IPreferenceStore getPrefStore()
    {
        if (prefStore == null) initializeDefaultPreferences();
        return prefStore;
    }
    @Override
    public void initializeDefaultPreferences()
    {
        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
        
        ConfigurationScope configScope = new ConfigurationScope();
        prefStore = new ScopedPreferenceStore(configScope, 
            "org.thanlwinsoft.doccharconvert");
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        File pluginStateLocation = 
            DocCharConvertEclipsePlugin.getDefault().getStateLocation().toFile();
        String wsParent = workspace.getRoot().getLocation().toFile().getParent();
        IProject converters = workspace.getRoot().getProject(CONVERTERS);
        String installArea = System.getProperty("osgi.install.area");
        String path = CONVERTERS;
        try
        {
            if (installArea.startsWith("file:"))
                installArea = installArea.substring(5);
            File installPath = new File(installArea, "configuration" + 
                            File.separatorChar + 
                            "org.thanlwinsoft.doccharconvert" + 
                            File.separatorChar +
                            CONVERTERS);
            if (converters.exists() == false)
            {
                File testFile = new File(wsParent, CONVERTERS);
                // in workspace project
                if (testFile.exists() && testFile.isDirectory())
                {
                    path = testFile.getAbsolutePath();
                }
                // in install path - may be problematic e.g. eclipse/Converters
                else if (installPath.isDirectory())
                {
                    path = installPath.getAbsolutePath();
                }
                else if (new File(pluginStateLocation, CONVERTERS).isDirectory())
                {
                    path = new File(pluginStateLocation, CONVERTERS).getAbsolutePath();
                }
                else
                {
                    converters.create(null);
                }
            }
            else path = converters.getLocation().toString();
        }
        catch (CoreException e)
        {
            MessageDialog.openError(shell, 
                    "Error", 
                    "Error creating Converters project:" + e.getMessage());
        }
//        catch (URISyntaxException e)
//        {
//            MessageDialog.openError(shell, 
//                "Error", 
//                "Error finding Converters project:" + e.getMessage());
//        }
        
        prefStore.setDefault(Config.CONVERTER_CONFIG_PATH, path);
        prefStore.setDefault(Config.TEST_FONT_SIZE, Config.DEFAULT_FONT_SIZE);
        prefStore.setDefault(Config.OOPATH, Config.DEFAULT_WIN_INSTALL);
        prefStore.setDefault(Config.OOUNO, Config.OO_DEFAULT_UNO);
        prefStore.setDefault(Config.OOOPTIONS, Config.OO_DEFAULT_OPTIONS);
    }

}
