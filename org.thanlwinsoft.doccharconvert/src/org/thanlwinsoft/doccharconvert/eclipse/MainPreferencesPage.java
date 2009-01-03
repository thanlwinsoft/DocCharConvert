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


import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.converter.test.LogConvertedWords;

/**
 * @author keith
 *
 */
public class MainPreferencesPage extends FieldEditorPreferencePage 
    implements IWorkbenchPreferencePage
{
    /**
     * Converter configuration path
     */
    public static final String CONVERTERS = Config.CONVERTER_CONFIG_PATH;
    private ScopedPreferenceStore prefStore = null;
    
    /**
     * Constructor
     */
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
        
        addField(new DirectoryFieldEditor(Config.LOG_FILE,
                MessageUtil.getString("LogFile"),
                getFieldEditorParent()));
        
        addField(new StringFieldEditor(LogConvertedWords.WORD_SEPARATOR_KEY,
            MessageUtil.getString("WordDelimiterRegExp"),
            getFieldEditorParent()));
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
        new PreferencesInitializer().initializeDefaultPreferences();
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent event)
    {
        super.propertyChange(event);
    }
    
    

}
