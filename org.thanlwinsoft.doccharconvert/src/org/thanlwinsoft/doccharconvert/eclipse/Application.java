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

    public void applicationRunning()
    {
        //TODO
    }

    @SuppressWarnings("unchecked")
    public Map getArguments()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getBrandingApplication()
    {
        
        return "org.thanlwinsoft.doccharconvert.eclipse.Application";
    }

    public Bundle getBrandingBundle()
    {
        return Platform.getBundle(DocCharConvertEclipsePlugin.ID);
    }

    public String getBrandingDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public String getBrandingId()
    {
        // TODO Auto-generated method stub
        return "DocCharConvertProduct";
    }

    public String getBrandingName()
    {
        return MessageUtil.getString("BrandingName");
    }

    public String getBrandingProperty(String key)
    {
        return MessageUtil.getString(key);
    }
}
