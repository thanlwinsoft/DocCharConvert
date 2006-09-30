/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * @author keith
 *
 */
public class MainPreferencesPage extends FieldEditorPreferencePage 
    implements IWorkbenchPreferencePage
{
    
    private ScopedPreferenceStore prefStore = null;
    
    public MainPreferencesPage()
    {
        super(FieldEditorPreferencePage.FLAT);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors()
    {
        addField(new DirectoryFieldEditor(Config.CONVERTER_CONFIG_PATH,
                MessageUtil.getString("ConverterPath"),
                getFieldEditorParent()));
        
        addField(new IntegerFieldEditor(Config.TEST_FONT_SIZE,
                MessageUtil.getString("DefaultFontSize"), 
                getFieldEditorParent()));
        DirectoryFieldEditor ooPathFE = new DirectoryFieldEditor(Config.OOPATH,
                MessageUtil.getString("OOoPath"),
                getFieldEditorParent());
        // TODO: validate oo path
        addField(ooPathFE);
        addField(new StringFieldEditor(Config.OOOPTIONS,
                MessageUtil.getString("OOoOptions"),
                this.getFieldEditorParent()));
        addField(new StringFieldEditor(Config.OOUNO,
                MessageUtil.getString("OOoUNO"),
                this.getFieldEditorParent()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.PreferencePage#doGetPreferenceStore()
     */
    @Override
    protected IPreferenceStore doGetPreferenceStore()
    {
        ConfigurationScope configScope = new ConfigurationScope();
        prefStore = new ScopedPreferenceStore(configScope, 
            "org.thanlwinsoft.doccharconvert");
        return prefStore;
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
        doGetPreferenceStore();
        IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
        IProject converters = workspace.getRoot().getProject("Converters");
        String path = "Converters";
        try
        {
            if (converters.exists() == false)
            {
                converters.create(null);
            }
            path = converters.getLocation().toString();
        }
        catch (CoreException e)
        {
            MessageDialog.openError(workbench.getActiveWorkbenchWindow().getShell(), 
                    "Error", 
                    "Error creating Converters project:" + e.getMessage());
        }
        
        prefStore.setDefault(Config.CONVERTER_CONFIG_PATH, path);
        prefStore.setDefault(Config.TEST_FONT_SIZE, Config.DEFAULT_FONT_SIZE);
        prefStore.setDefault(Config.OOPATH, Config.DEFAULT_WIN_INSTALL);
        prefStore.setDefault(Config.OOUNO, Config.OO_DEFAULT_UNO);
        prefStore.setDefault(Config.OOOPTIONS, Config.OO_DEFAULT_OPTIONS);        
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        if (event.getProperty().equals(Config.OOPATH))
        {
            Location l = Platform.getInstallLocation();
            try
            {
                File base = new File(l.getURL().toURI());
                Config.getCurrent().setBasePath(base);
            }
            catch (URISyntaxException e)
            {
                e.printStackTrace();
            }
            Config.getCurrent().setOOPath(event.getNewValue().toString());
        }
    }
    
    

}
