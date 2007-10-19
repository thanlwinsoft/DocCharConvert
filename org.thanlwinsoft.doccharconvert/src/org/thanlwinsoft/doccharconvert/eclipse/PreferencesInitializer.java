/*
Copyright (C) 2006-2007 Keith Stribley http://www.thanlwinsoft.org/

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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.converter.test.LogConvertedWords;
import org.thanlwinsoft.doccharconvert.eclipse.wizard.ConversionWizard;

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
        IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
        IProject myProject = myWorkspaceRoot.getProject(ConversionWizard.DEFAULT_PROJECT);
        
        try
        {
            if (myProject.exists() == false)
            {
                myProject.create(null);
            }
            myProject.open(null);
            
        }
        catch (CoreException e)
        {
            DocCharConvertEclipsePlugin.log(IStatus.WARNING, 
                "Failed to create " + ConversionWizard.DEFAULT_PROJECT, e);
        }
        
        IPath root = myWorkspaceRoot.getRawLocation();
        IPath projectPath = myProject.getFullPath();
        IPath projectFullPath = root.append(projectPath).makeAbsolute();
        prefStore.setDefault(Config.LOG_FILE, projectFullPath.toOSString());
        prefStore.setDefault(LogConvertedWords.WORD_SEPARATOR_KEY, 
            LogConvertedWords.WORD_SEPARATOR);
        IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        // HACK
        page.hideActionSet("org.eclipse.ui.edit.text.actionSet.navigation");
        page.hideActionSet("org.eclipse.ui.WorkingSetActionSet");
        page.hideActionSet("org.eclipse.ui.edit.text.actionSet.annotationNavigation");
        page.hideActionSet("org.eclipse.ui.externaltools.ExternalToolsSet");
    }

}
