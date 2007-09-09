package org.thanlwinsoft.doccharconvert.openoffice;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.thanlwinsoft.doccharconvert.Config;

public class OOPreferencesInitializer extends AbstractPreferenceInitializer
{
    public static final String CONVERTERS = Config.CONVERTER_CONFIG_PATH;
    private ScopedPreferenceStore prefStore = null;
    public OOPreferencesInitializer()
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
        ConfigurationScope configScope = new ConfigurationScope();
        prefStore = new ScopedPreferenceStore(configScope, 
            "org.thanlwinsoft.doccharconvert");
        
        prefStore.setDefault(OOConfig.OOPATH, Config.DEFAULT_WIN_INSTALL);
        prefStore.setDefault(OOConfig.OOUNO, OOConfig.OO_DEFAULT_UNO);
        prefStore.setDefault(OOConfig.OOOPTIONS, OOConfig.OO_DEFAULT_OPTIONS);
        
    }

}
