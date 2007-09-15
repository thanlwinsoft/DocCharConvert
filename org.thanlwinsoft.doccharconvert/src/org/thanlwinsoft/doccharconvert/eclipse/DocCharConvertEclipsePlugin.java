package org.thanlwinsoft.doccharconvert.eclipse;


import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.BundleContext;
import org.osgi.service.prefs.Preferences;
import org.thanlwinsoft.eclipse.EclipseToJavaPrefAdapter;

/**
 * The main plugin class to be used in the desktop.
 */
public class DocCharConvertEclipsePlugin extends AbstractUIPlugin 
{
    public final static String ID = "org.thanlwinsoft.doccharconvert";
	//The shared instance.
	private static DocCharConvertEclipsePlugin plugin = null;
	private PreferencesInitializer mPrefsInitializer = null;
	
	/**
	 * The constructor.
	 */
	public DocCharConvertEclipsePlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		ConfigurationScope configScope = new ConfigurationScope();
		Preferences configurationNode = 
            configScope.getNode(DocCharConvertEclipsePlugin.ID);
		new org.thanlwinsoft.doccharconvert.Config
            (new EclipseToJavaPrefAdapter(configurationNode));
	}

	@Override
    public IPreferenceStore getPreferenceStore()
    {
	    if (mPrefsInitializer == null)
	        mPrefsInitializer = new PreferencesInitializer();
	    mPrefsInitializer.initializeDefaultPreferences();
	    return mPrefsInitializer.getPrefStore();
    }

    /**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static DocCharConvertEclipsePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path.
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return AbstractUIPlugin.imageDescriptorFromPlugin(ID, path);
	}

    public static void log(int warning, String string, Throwable exception)
    {
        if (getDefault() == null)
            System.out.println("Error " + warning + ": " + string);
        else
            getDefault().getLog().log(new Status(warning,
                        "org.thanlwinsoft.doccharconvert",
                        IStatus.OK, string,exception));
    }

    public static void log(int warning, String string)
    {
        if (getDefault() == null)
            System.out.println("Error " + warning + ": " + string);
        else
            getDefault().getLog().log(new Status(warning,
                        "org.thanlwinsoft.doccharconvert",
                        IStatus.OK, string, null));
    }
}
