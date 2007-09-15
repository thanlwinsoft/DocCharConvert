package org.thanlwinsoft.doccharconvert.eclipse;

import java.util.Map;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;
import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * This class controls all aspects of the application's execution
 */
public class Application implements IApplicationContext
{
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.IPlatformRunnable#run(java.lang.Object)
     */
    public Object run(Object args) throws Exception
    {
        Display display = PlatformUI.createDisplay();
        try
        {
            int returnCode = PlatformUI.createAndRunWorkbench(display,
                    new ApplicationWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART)
            {
                return IApplication.EXIT_RESTART;
            }
            return IApplication.EXIT_OK;
        }
        finally
        {
            display.dispose();
        }
    }

    @Override
    public void applicationRunning()
    {
        //TODO
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map getArguments()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBrandingApplication()
    {
        
        return "org.thanlwinsoft.doccharconvert.eclipse.Application";
    }

    @Override
    public Bundle getBrandingBundle()
    {
        return Platform.getBundle(DocCharConvertEclipsePlugin.ID);
    }

    @Override
    public String getBrandingDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBrandingId()
    {
        // TODO Auto-generated method stub
        return "DocCharConvertProduct";
    }

    @Override
    public String getBrandingName()
    {
        return MessageUtil.getString("BrandingName");
    }

    @Override
    public String getBrandingProperty(String key)
    {
        return MessageUtil.getString(key);
    }
}
