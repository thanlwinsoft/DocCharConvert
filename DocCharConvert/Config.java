/*
 * Config.java
 *
 * Created on August 28, 2004, 11:03 AM
 */

package DocCharConvert;

import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;
/**
 *
 * @author  keith
 */
public class Config
{
    private static Config instance = null; 
    private File basePath = null;
    private Preferences packagePref = null;
    private File inputPath = null;
    private File outputPath = null;
    private final String INPUT_PATH = "InputBrowsePath";
    private final String OUTPUT_PATH  = "OutputBrowsePath";
    private final String INSTALL_PATH = "InstallPath";
    private final String OOPATH = "OOPath";
    private final String OOUNO = "OOUNO";
    private final String OOOPTIONS = "OOOptions";
    public static final String CONVERTER_CONFIG_PATH = "Converters";
    private String ooPath = "ooffice";
    private String oouno = "uno:socket,host=localhost,port=8100;urp;StarOffice.ServiceManager";
    private String ooOptions = " -headless -accept=socket,hostname=localhost,port=8100;urp;";
    public static Config getCurrent()
    {
        if (instance == null) instance = new Config();
        return instance;
    }
    /** Creates a new instance of Config */
    protected Config()
    {
        String className = this.getClass().getPackage().getName();
        URL classSource = 
            this.getClass().getClassLoader().getResource(className);
        
        File thisClassFile = null;
        System.out.println(classSource);
        if (classSource != null)
        {
            thisClassFile = new File(classSource.getPath());
            basePath = thisClassFile.getParentFile();
            while (basePath != null && !basePath.isDirectory())
            {
              basePath = basePath.getParentFile();
            }
        }
        if (basePath == null)
        {
            basePath = new File(System.getProperty("user.home"));
        }
        basePath = new File(basePath, CONVERTER_CONFIG_PATH);
        packagePref = Preferences.userNodeForPackage(this.getClass());
        inputPath = new File(packagePref.get(INPUT_PATH, ""));
        outputPath = new File(packagePref.get(OUTPUT_PATH, ""));
        ooPath = packagePref.get(OOPATH, OOMainInterface.OO_PATH);
        ooOptions = packagePref.get(OOOPTIONS, OOMainInterface.RUN_OO);
        oouno = packagePref.get(OOUNO, OOMainInterface.UNO_URL);
        try
        {
            basePath = new File(packagePref.get(INSTALL_PATH, 
                                                basePath.getCanonicalPath()));
            if (!basePath.exists()) 
            {
                basePath.mkdirs();
            }
            packagePref.put(INSTALL_PATH, basePath.getCanonicalPath());
        }
        catch (java.io.IOException e)
        {
            System.out.println(e.getLocalizedMessage());
        }
        if (!inputPath.isDirectory()) inputPath = null;
        if (!outputPath.isDirectory()) outputPath = null;
    }
    public File getBasePath()
    {
        return basePath;
    }
    public File getInputPath()
    {
        return inputPath;
    }
    public File getOutputPath()
    {
        if (outputPath == null) outputPath = inputPath;
        return outputPath;
    }
    public void setBasePath(File file)
    {
        basePath = file;
        try
        {
            packagePref.put(INSTALL_PATH, file.getCanonicalPath());
        }
        catch (java.io.IOException e) 
        {
            System.out.println(e.getMessage());
        }
    }
    
    public void setInputPath(File file)
    {
        inputPath = file;
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
        outputPath = file;
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
    public URL [] getOOClasses()
    {
        try
        {
            File classDir = new File("/usr/lib/openoffice/program/classes/");
            java.io.FilenameFilter jarFilter = new java.io.FilenameFilter() {
                public boolean accept(File parent, String name)
                {
                    if (name.toLowerCase().endsWith(".jar")) return true;
                    return false;
                }
            };
            File [] jarFiles = classDir.listFiles(jarFilter);
            URL [] urls = new URL[jarFiles.length + 1];
            urls[0] = basePath.toURL();
            for (int i = 0; i<jarFiles.length; i++)
            {
                urls[i + 1] = jarFiles[i].toURL();
                System.out.println(urls[i+1].toString());
            }
            return urls;
        }
        catch (java.net.MalformedURLException e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    public void setOOPath(String path)
    {
        this.ooPath = path;
        packagePref.put(OOPATH, ooPath);
    }
    public void setOOOptions(String options)
    {
        this.ooOptions = options;
        packagePref.put(OOOPTIONS, ooOptions);
    }
    public void setOOUNO(String unoPath)
    {
        this.oouno = unoPath;
        packagePref.put(OOUNO, oouno);
    }
    public String getOOPath() { return ooPath; }
    public String getOOOptions() { return ooOptions; }
    public String getOOUNO() { return oouno; }
}
