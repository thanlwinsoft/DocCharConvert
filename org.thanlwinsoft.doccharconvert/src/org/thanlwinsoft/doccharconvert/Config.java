/*
 *  Copyright (C) 2005 Keith Stribley <doccharconvert@thanlwinsoft.org>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -----------------------------------------------------------------------
 * $HeadURL: $
 * $LastChangedBy: keith $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package org.thanlwinsoft.doccharconvert;

import java.io.File;
import javax.swing.JOptionPane;

import org.eclipse.core.runtime.IStatus;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
//import org.thanlwinsoft.doccharconvert.openoffice.OOMainInterface;

import java.net.URL;
import java.util.prefs.Preferences;
import java.util.ResourceBundle;
import java.text.MessageFormat;
/**
 *
 * @author  keith
 */
public class Config
{
    private static Config instance = null; 
    private File basePath = null;
    //private File converterPath = null;
    private Preferences packagePref = null;
    //private File inputPath = null;
    //private File outputPath = null;
    public static final String LOG_FILE = "LogFile";
    private final String INPUT_PATH = "InputBrowsePath";
    private final String OUTPUT_PATH  = "OutputBrowsePath";
    public static  final String INSTALL_PATH = "InstallPath";
    public static final String DEFAULT_WIN_INSTALL = 
        "C:\\Program Files\\ThanLwinSoft.org\\DocCharConvert";
    public static final String CONVERTER_CONFIG_PATH = "Converters";
    public static final int DEFAULT_FONT_SIZE = 20;
    public static final String TEST_FONT_SIZE = "TestFontSize";
    //private int testFontSize = DEFAULT_FONT_SIZE;
    private ResourceBundle i18nResource = null;
    private String resourceBase = 
        this.getClass().getPackage().getName().replace(".","/");
    
    public static Config getCurrent()
    {
        if (instance == null) 
        {
            instance = new Config();
        }
        return instance;
    }
    public static ResourceBundle messageResource()
    {
        return Config.getCurrent().getMsgResource();
    }
    /** Creates a new instance of Config */
    protected Config()
    {
        packagePref = Preferences.userNodeForPackage(this.getClass());
        init();
    }
    public Config(Preferences pref)
    {
        this.packagePref = pref;
        init();
        Config.instance = this;
    }
    protected void init()
    {
        String className = this.getClass().getPackage().getName();
        URL classSource = 
            this.getClass().getClassLoader().getResource(className);
        File thisClassFile = null;
        //System.out.println(classSource);
        if (classSource != null)
        {
            String classSrcPath = classSource.getPath();
            int iJar = classSrcPath.indexOf("file:");
            int jJar = classSrcPath.indexOf("!");
            if (iJar > -1) 
            {
               // check to see if it contains a drive letter
               if (classSrcPath.substring(iJar + 7, iJar + 8).equals(":"))
               {
                 classSrcPath = classSrcPath.substring(iJar + 6,jJar);
                 // only deal with space for moment
                 classSrcPath = classSrcPath.replace("%20"," ");
               }
               else
                 classSrcPath = classSrcPath.substring(iJar + 5,jJar);
            }
            //System.out.println("ThisJar: " + classSrcPath);
            thisClassFile = new File(classSrcPath);
            if (thisClassFile != null)
            {
                
                basePath = thisClassFile.getParentFile();
                while (basePath != null && !basePath.isDirectory())
                {
                  basePath = basePath.getParentFile();
                }
                
            }
            System.out.println("Installed Path: " + basePath);
        }
        if (basePath == null)
        {
            basePath = new File(DEFAULT_WIN_INSTALL);
            if (!basePath.isDirectory())
                basePath = new File(System.getProperty("user.home"));
        }
        File converterPath = new File(basePath, CONVERTER_CONFIG_PATH);
        
        
//        ooPath = packagePref.get(OOPATH, OOMainInterface.OO_PATH);
//        ooOptions = packagePref.get(OOOPTIONS, OOMainInterface.RUN_OO);
//        oouno = packagePref.get(OOUNO, OOMainInterface.UNO_URL);
        try
        {
            File defaultConvPath = converterPath;
            converterPath = new File(packagePref.get(CONVERTER_CONFIG_PATH, 
                                                converterPath.getCanonicalPath()));
            if (!converterPath.exists()) 
            {
                // revert to default
                converterPath = defaultConvPath;
                //converterPath.mkdirs();
                if (converterPath.exists())
                    packagePref.put(CONVERTER_CONFIG_PATH, converterPath.getCanonicalPath());
            }
            
        }
        catch (java.io.IOException e)
        {
            System.out.println(e.getLocalizedMessage());
        }
        //if (!inputPath.isDirectory()) inputPath = null;
        //if (!outputPath.isDirectory()) outputPath = null;
        try
        {
            i18nResource = ResourceBundle.getBundle(resourceBase + "/Messages");
        }
        catch (java.util.MissingResourceException mre)
        {
             System.out.println(mre.getMessage());
        }
    }
    public File getConverterPath()
    {
        File path = new File(packagePref.get(CONVERTER_CONFIG_PATH, CONVERTER_CONFIG_PATH));
        if (path.isDirectory()) return path;
        //DocCharConvertEclipsePlugin.log(IStatus.WARNING,"No converter path: " +
        //                path.getAbsolutePath(), null);
        System.out.println("No converter path: " +
                           path.getAbsolutePath());
        // try making a guess based on where the windows installer puts it
        path = new File("configuration" + File.separator + 
                        "org.thanlwinsoft.doccharconvert" + File.separator +
                        CONVERTER_CONFIG_PATH);
        if (path.isDirectory()) setConverterPath(path);
        return path;
    }
    public File getInputPath()
    {
        return new File(packagePref.get(INPUT_PATH, ""));
    }
    public File getOutputPath()
    {
        String path = packagePref.get(OUTPUT_PATH, "");
        if (path == null)
            path = packagePref.get(INPUT_PATH, "");
        return new File(path);
    }
    public void setConverterPath(File file)
    {
        //converterPath = file;
        try
        {
            if (file.isDirectory())
                packagePref.put(CONVERTER_CONFIG_PATH, file.getCanonicalPath());
        }
        catch (java.io.IOException e) 
        {
            System.out.println(e.getMessage());
        }
    }
    
    public void setInputPath(File file)
    {
        //inputPath = file;
        try
        {
            packagePref.put(INPUT_PATH, file.getCanonicalPath());
        }
        catch (java.io.IOException e) 
        {
            System.out.println(e.getMessage());
        }
    }
    public void setOutputPath(File file)
    {
        //outputPath = file;
        try
        {
            packagePref.put(OUTPUT_PATH, file.getCanonicalPath());
        }
        catch (java.io.IOException e) 
        {
            System.out.println(e.getMessage());
        }
    
    }
    public void save()
    {
        try
        {
            packagePref.flush();
        }
        catch (java.util.prefs.BackingStoreException e)
        {
            System.out.println(e.getMessage());
        }
    }
    
    public void setTestFontSize(int size) 
    { 
        //testFontSize = size; 
        packagePref.putInt(TEST_FONT_SIZE, size);
    }
    
    public ResourceBundle getMsgResource() { return i18nResource; }
    public void setBasePath(File basePath)
    {
        this.basePath = basePath;
    }
    public File getBasePath()
    {
    	return this.basePath;
    }
    public int getTestFontSize() 
    { 
        return packagePref.getInt(TEST_FONT_SIZE, DEFAULT_FONT_SIZE); 
    }
    public String getLogFile()
    {
        return packagePref.get(LOG_FILE, null);
    }
    public void setLogFile(String value)
    {
        packagePref.put(LOG_FILE, value);
    }
    public Preferences getPrefs() { return packagePref; }
}
