package org.thanlwinsoft.doccharconvert.converters.myanmar;

import java.net.URL;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * Activator for Myanmar Converters
 * @author keith
 *
 */
public class MyanmarConverterActivator extends AbstractUIPlugin
{
	ResourceBundle mResource = null;
	static MyanmarConverterActivator sInstance = null;
	public final static String ID = "org.thanlwinsoft.doccharconvert.converters.myanmar";
	/**
	 * Activator for Myanmar Converters - constructor
	 */
	public MyanmarConverterActivator()
	{
		sInstance = this;
	}

	@Override
	public void start(BundleContext context) throws Exception
	{
		super.start(context);
		IPath msgPath = new Path("$nl$/messages.properties");
		URL msgUrl = FileLocator.find(context.getBundle(), msgPath, null);
		if (msgUrl != null)
		{
			mResource = new PropertyResourceBundle(msgUrl.openStream());
			msgUrl.openStream();
		}		
	}

	@Override
	public void stop(BundleContext context) throws Exception
	{
		// TODO Auto-generated method stub
		super.stop(context);
	}

	/**
	 * @param id
	 * @return localized message
	 */
	static public String msg(String id)
	{
		if (sInstance == null || sInstance.mResource == null ||
			!sInstance.mResource.containsKey(id))
			return id;
		
		return sInstance.mResource.getString(id);
	}
}
