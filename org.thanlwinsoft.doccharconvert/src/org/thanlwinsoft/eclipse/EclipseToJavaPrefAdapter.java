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

package org.thanlwinsoft.eclipse;

import java.io.IOException;
import java.io.OutputStream;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

/**
 * @author keith
 *
 */
public class EclipseToJavaPrefAdapter extends Preferences
{
    org.osgi.service.prefs.Preferences eclipsePrefs = null;
    public EclipseToJavaPrefAdapter(org.osgi.service.prefs.Preferences prefs)
    {
        eclipsePrefs = prefs;
    }
    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#absolutePath()
     */
    @Override
    public String absolutePath()
    {
        // TODO Auto-generated method stub
        return eclipsePrefs.absolutePath();
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#addNodeChangeListener(java.util.prefs.NodeChangeListener)
     */
    @Override
    public void addNodeChangeListener(NodeChangeListener ncl)
    {
        
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#addPreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
     */
    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl)
    {

    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#childrenNames()
     */
    @Override
    public String[] childrenNames() throws BackingStoreException
    {
        try 
        {
            return eclipsePrefs.childrenNames();
        }
        catch (org.osgi.service.prefs.BackingStoreException e)
        {
            throw new BackingStoreException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#clear()
     */
    @Override
    public void clear() throws BackingStoreException
    {
        try 
        {
            eclipsePrefs.clear();
        }
        catch (org.osgi.service.prefs.BackingStoreException e)
        {
            throw new BackingStoreException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#exportNode(java.io.OutputStream)
     */
    @Override
    public void exportNode(OutputStream os) throws IOException,
            BackingStoreException
    {
//        try 
//        {
//            
//        }
//        catch (org.osgi.service.prefs.BackingStoreException e)
//        {
//            throw new BackingStoreException(e);
//        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#exportSubtree(java.io.OutputStream)
     */
    @Override
    public void exportSubtree(OutputStream os) throws IOException,
            BackingStoreException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#flush()
     */
    @Override
    public void flush() throws BackingStoreException
    {
        // TODO Auto-generated method stub
        try
        {
            eclipsePrefs.flush();
        }
        catch (org.osgi.service.prefs.BackingStoreException e)
        {
            throw new BackingStoreException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#get(java.lang.String, java.lang.String)
     */
    @Override
    public String get(String key, String def)
    {
        return eclipsePrefs.get(key, def);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#getBoolean(java.lang.String, boolean)
     */
    @Override
    public boolean getBoolean(String key, boolean def)
    {
        return eclipsePrefs.getBoolean(key, def);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#getByteArray(java.lang.String, byte[])
     */
    @Override
    public byte[] getByteArray(String key, byte[] def)
    {
        return eclipsePrefs.getByteArray(key, def);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#getDouble(java.lang.String, double)
     */
    @Override
    public double getDouble(String key, double def)
    {
        return eclipsePrefs.getDouble(key, def);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#getFloat(java.lang.String, float)
     */
    @Override
    public float getFloat(String key, float def)
    {
        return eclipsePrefs.getFloat(key, def);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#getInt(java.lang.String, int)
     */
    @Override
    public int getInt(String key, int def)
    {
        // TODO Auto-generated method stub
        return eclipsePrefs.getInt(key, def);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#getLong(java.lang.String, long)
     */
    @Override
    public long getLong(String key, long def)
    {
        // TODO Auto-generated method stub
        return eclipsePrefs.getLong(key, def);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#isUserNode()
     */
    @Override
    public boolean isUserNode()
    {
        // TODO Auto-generated method stub
        return true;
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#keys()
     */
    @Override
    public String[] keys() throws BackingStoreException
    {
        try
        {
            return eclipsePrefs.keys();
        }
        catch (org.osgi.service.prefs.BackingStoreException e)
        {
            throw new BackingStoreException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#name()
     */
    @Override
    public String name()
    {
        // TODO Auto-generated method stub
        return eclipsePrefs.name();
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#node(java.lang.String)
     */
    @Override
    public Preferences node(String pathName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#nodeExists(java.lang.String)
     */
    @Override
    public boolean nodeExists(String pathName) throws BackingStoreException
    {
        try
        {
            return eclipsePrefs.nodeExists(pathName);
        }
        catch (org.osgi.service.prefs.BackingStoreException e)
        {
            throw new BackingStoreException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#parent()
     */
    @Override
    public Preferences parent()
    {
        return new EclipseToJavaPrefAdapter(eclipsePrefs.parent());
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#put(java.lang.String, java.lang.String)
     */
    @Override
    public void put(String key, String value)
    {
        eclipsePrefs.put(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#putBoolean(java.lang.String, boolean)
     */
    @Override
    public void putBoolean(String key, boolean value)
    {
        eclipsePrefs.putBoolean(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#putByteArray(java.lang.String, byte[])
     */
    @Override
    public void putByteArray(String key, byte[] value)
    {
        eclipsePrefs.putByteArray(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#putDouble(java.lang.String, double)
     */
    @Override
    public void putDouble(String key, double value)
    {
        eclipsePrefs.putDouble(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#putFloat(java.lang.String, float)
     */
    @Override
    public void putFloat(String key, float value)
    {
        eclipsePrefs.putFloat(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#putInt(java.lang.String, int)
     */
    @Override
    public void putInt(String key, int value)
    {
        eclipsePrefs.putInt(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#putLong(java.lang.String, long)
     */
    @Override
    public void putLong(String key, long value)
    {
        eclipsePrefs.putLong(key, value);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#remove(java.lang.String)
     */
    @Override
    public void remove(String key)
    {
        eclipsePrefs.remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#removeNode()
     */
    @Override
    public void removeNode() throws BackingStoreException
    {
        try
        {
            eclipsePrefs.removeNode();
        }
        catch (org.osgi.service.prefs.BackingStoreException e)
        {
            throw new BackingStoreException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#removeNodeChangeListener(java.util.prefs.NodeChangeListener)
     */
    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl)
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#removePreferenceChangeListener(java.util.prefs.PreferenceChangeListener)
     */
    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl)
    {
        //eclipsePrefs.removePreferenceChangeListener(listener);
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#sync()
     */
    @Override
    public void sync() throws BackingStoreException
    {
        try
        {
            eclipsePrefs.sync();
        }
        catch (org.osgi.service.prefs.BackingStoreException e)
        {
            throw new BackingStoreException(e);
        }
    }

    /* (non-Javadoc)
     * @see java.util.prefs.Preferences#toString()
     */
    @Override
    public String toString()
    {
        return eclipsePrefs.toString();
    }

}
