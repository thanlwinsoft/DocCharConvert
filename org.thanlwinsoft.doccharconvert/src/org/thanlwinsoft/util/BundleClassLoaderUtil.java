package org.thanlwinsoft.util;

import org.osgi.framework.Bundle;

public class BundleClassLoaderUtil implements IClassLoaderUtil
{
    private Bundle [] mBundles;
    public BundleClassLoaderUtil(Bundle [] bundles)
    {
        mBundles = bundles;
    }
    public Class<?> loadClass(String className) throws ClassNotFoundException
    {
        for (Bundle b: mBundles)
        {
            if (b.getState() != Bundle.UNINSTALLED)
            {
                try
                {
                    Class <?> c = b.loadClass(className);
                    if (c != null)
                        return c;
                }
                catch (ClassNotFoundException e)
                {
                    // ignore
                }
            }
        }
        throw new ClassNotFoundException(className);
    }
}
