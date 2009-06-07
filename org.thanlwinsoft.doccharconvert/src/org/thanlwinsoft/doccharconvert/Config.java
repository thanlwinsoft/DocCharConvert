/*
Copyright (C) 2005-2007 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.doccharconvert;

import java.io.File;

import java.net.URL;
import java.util.prefs.Preferences;
import java.util.ResourceBundle;
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
    /**
     * log file key
     */
    public static final String LOG_FILE = "LogFile";
    private final String INPUT_PATH = "InputBrowsePath";
    private final String OUTPUT_PATH  = "OutputBrowsePath";
    /**
     * Install path key
     */
    public static  final String INSTALL_PATH = "InstallPath";
    /**
     * default install directory
     */
    public static final String DEFAULT_WIN_INSTALL = 
        "C:\\Program Files\\ThanLwinSoft.org\\DocCharConvert";
    /**
     * default config path
     */
    public static final String CONVERTER_CONFIG_PATH = "Converters";
    /**
     * default font size
     */
    public static final int DEFAULT_FONT_SIZE = 20;
    /**
     * font size for testing
     */
    public static final String TEST_FONT_SIZE = "TestFontSize";
    //private int testFontSize = DEFAULT_FONT_SIZE;
    private ResourceBundle i18nResource = null;
    //private String resourceBase = 
    //    this.getClass().getPackage().getName().replace(".","/");
    /**
     * 
     * @return current configuration implementation
     */
    public static Config getCurrent()
    {
        if (instance == null) 
        {
            instance = new Config();
        }
        return instance;
    }
    /**
     * 
     * @return message resource for localized strings
     */
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
    /**
     * Constructor for use from eclipse plugin
     * @param pref
     * @param rb 
     */
    public Config(Preferences pref, ResourceBundle rb)
    {
        this.packagePref = pref;
        this.i18nResource = rb;
        init();
        Config.instance = this;
    }
    /**
     * common initialization
     */
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
        	if (i18nResource == null)
        		i18nResource = ResourceBundle.getBundle("Messages");
        }
        catch (java.util.MissingResourceException mre)
        {
             System.out.println(mre.getMessage());
        }
    }
    /**
     * 
     * @return directory
     */
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
    /**
     * 
     * @return last input file path
     */
    public File getInputPath()
    {
        return new File(packagePref.get(INPUT_PATH, ""));
    }
    /**
     * 
     * @return last output file path
     */
    public File getOutputPath()
    {
        String path = packagePref.get(OUTPUT_PATH, "");
        if (path == null)
            path = packagePref.get(INPUT_PATH, "");
        return new File(path);
    }
    /**
     * 
     * @param file
     */
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
    /**
     * 
     * @param file
     */
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
    /**
     * 
     * @param file
     */
    public void setOutputPath(File file)
    {
        //outputPath = file;
        try
        {
            if (file != null)
                packagePref.put(OUTPUT_PATH, file.getCanonicalPath());
        }
        catch (java.io.IOException e) 
        {
            System.out.println(e.getMessage());
        }
    
    }
    /**
     * save configuration to disk
     */
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
    /**
     * 
     * @param size
     */
    public void setTestFontSize(int size) 
    { 
        //testFontSize = size; 
        packagePref.putInt(TEST_FONT_SIZE, size);
    }
    /**
     * 
     * @return internationalization resource
     */
    public ResourceBundle getMsgResource() { return i18nResource; }
    /**
     * 
     * @param basePath
     */
    public void setBasePath(File basePath)
    {
        this.basePath = basePath;
    }
    /**
     * 
     * @return path
     */
    public File getBasePath()
    {
    	return this.basePath;
    }
    /**
     * 
     * @return font size
     */
    public int getTestFontSize() 
    { 
        return packagePref.getInt(TEST_FONT_SIZE, DEFAULT_FONT_SIZE); 
    }
    /**
     * 
     * @return log file name
     */
    public String getLogFile()
    {
        return packagePref.get(LOG_FILE, null);
    }
    /**
     * 
     * @param value
     */
    public void setLogFile(String value)
    {
        packagePref.put(LOG_FILE, value);
    }
    /**
     * 
     * @return Preferences
     */
    public Preferences getPrefs() { return packagePref; }
}
