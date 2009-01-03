/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

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
package org.thanlwinsoft.util;

import org.osgi.framework.Bundle;

/**
 * @author keith
 *
 */
public class BundleClassLoaderUtil implements IClassLoaderUtil
{
    private Bundle [] mBundles;
    /**
     * @param bundles
     */
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
