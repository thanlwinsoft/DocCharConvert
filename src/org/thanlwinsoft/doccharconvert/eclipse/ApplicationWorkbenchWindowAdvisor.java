package org.thanlwinsoft.doccharconvert.eclipse;


import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
import org.eclipse.ui.application.WorkbenchWindowAdvisor;
import org.osgi.service.prefs.Preferences;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.eclipse.EclipseToJavaPrefAdapter;

public class ApplicationWorkbenchWindowAdvisor extends WorkbenchWindowAdvisor {

    public ApplicationWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
        super(configurer);
    }

    public ActionBarAdvisor createActionBarAdvisor(IActionBarConfigurer configurer) {
        return new ApplicationActionBarAdvisor(configurer);
    }
    
    public void preWindowOpen() {
        IWorkbenchWindowConfigurer configurer = getWindowConfigurer();
        configurer.setInitialSize(new Point(780, 580));
        configurer.setShowCoolBar(true);
        configurer.setShowStatusLine(true);
        configurer.setTitle(MessageUtil.getString("dialogTitle"));
//        IPreferencesService service = Platform.getPreferencesService();
//        if (service != null)
//        {
//            ConfigurationScope configScope = new ConfigurationScope();
//            Preferences configurationNode = 
//                configScope.getNode(DocCharConvertEclipsePlugin.ID);
//            try
//            {
//                new org.thanlwinsoft.doccharconvert.Config
//                    (new EclipseToJavaPrefAdapter(configurationNode));
//                // can't initialize pref here - there is no shell yet
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//            }
//        }
    }
}
