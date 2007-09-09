/**
 * 
 */
package org.thanlwinsoft.doccharconvert.openoffice;

import java.io.File;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
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
public class OOPreferencesPage extends FieldEditorPreferencePage 
    implements IWorkbenchPreferencePage
{
    public static final String CONVERTERS = Config.CONVERTER_CONFIG_PATH;
    private ScopedPreferenceStore prefStore = null;
    
    public OOPreferencesPage()
    {
        super(FieldEditorPreferencePage.FLAT);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    @Override
    protected void createFieldEditors()
    {
        DirectoryFieldEditor ooPathFE = new DirectoryFieldEditor(OOConfig.OOPATH,
                MessageUtil.getString("OOoPath"),
                getFieldEditorParent());
        // TODO: validate oo path
        addField(ooPathFE);
        addField(new StringFieldEditor(OOConfig.OOOPTIONS,
                MessageUtil.getString("OOoOptions"),
                this.getFieldEditorParent()));
        addField(new StringFieldEditor(OOConfig.OOUNO,
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
    /** 
     * Initialises the preferences
     * The default Converter path as follows:
     * 1. If a Converters project exists in the current workbench, then that is
     *    used.
     * 2. If a Converters directory exists at the same level as the workbench
     *    directory, that is used.
     * 3. If a Converters subdirectory exists in the install area, that is used.
     * 4. If a Converters directory exists in the plugins state location area,
     *    that is used.
     * 5. If all else fails a Converters project is created in the current 
     *    workspace.   
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench)
    {
        doGetPreferenceStore();
        new OOPreferencesInitializer().initializeDefaultPreferences();
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
        if (event.getProperty().equals(OOConfig.OOPATH))
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
            new OOConfig().setOOPath(event.getNewValue().toString());
        }
    }
    
    

}
