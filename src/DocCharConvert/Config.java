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
    private File converterPath = null;
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
    private String ooClassPath = "";
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
            String classSrcPath = classSource.getPath();
            int iJar = classSrcPath.indexOf("file:");
            int jJar = classSrcPath.indexOf("!");
            if (iJar > -1) 
            {
               classSrcPath = classSrcPath.substring(iJar + 5,jJar);
            }
            thisClassFile = new File(classSrcPath);
            if (thisClassFile != null)
            {
                
                basePath = thisClassFile.getParentFile();
                while (basePath != null && !basePath.isDirectory())
                {
                  basePath = basePath.getParentFile();
                }
                
            }
            System.out.println("Path: " + basePath);
        }
        if (basePath == null)
        {
            basePath = new File(System.getProperty("user.home"));
        }
        converterPath = new File(basePath, CONVERTER_CONFIG_PATH);
        packagePref = Preferences.userNodeForPackage(this.getClass());
        inputPath = new File(packagePref.get(INPUT_PATH, ""));
        outputPath = new File(packagePref.get(OUTPUT_PATH, ""));
        ooPath = packagePref.get(OOPATH, OOMainInterface.OO_PATH);
        ooOptions = packagePref.get(OOOPTIONS, OOMainInterface.RUN_OO);
        oouno = packagePref.get(OOUNO, OOMainInterface.UNO_URL);
        try
        {
            converterPath = new File(packagePref.get(INSTALL_PATH, 
                                                converterPath.getCanonicalPath()));
            if (!converterPath.exists()) 
            {
                converterPath.mkdirs();
            }
            packagePref.put(INSTALL_PATH, converterPath.getCanonicalPath());
        }
        catch (java.io.IOException e)
        {
            System.out.println(e.getLocalizedMessage());
        }
        if (!inputPath.isDirectory()) inputPath = null;
        if (!outputPath.isDirectory()) outputPath = null;
    }
    private File getBasePath()
    {
        return basePath;
    }
    public File getConverterPath()
    {
        return converterPath;
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
    public void setConverterPath(File file)
    {
        converterPath = file;
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
            StringBuffer ooPaths = new StringBuffer();
            File classDir = new File(new File(ooPath).getParent(),"classes");
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
                ooPaths.append(jarFiles[i].getCanonicalPath());
                ooPaths.append(' ');
                System.out.println(urls[i+1].toString());
            }
            ooClassPath = ooPaths.toString();
            return urls;
        }
        catch (java.net.MalformedURLException e)
        {
            System.out.println(e.getMessage());
        }
        catch (java.io.IOException e)
        {
            System.out.println(e.getMessage());
        }
        return null;
    }
    
    public void setOOPath(String path)
    {
        this.ooPath = path;
        packagePref.put(OOPATH, ooPath);
        getOOClasses();
        java.util.jar.Manifest manifest = new java.util.jar.Manifest();
        java.util.jar.Attributes mainAttrib = manifest.getMainAttributes();
        System.out.println(mainAttrib);
        mainAttrib.putValue("Manifest-Version","1.0");
        mainAttrib.putValue("CreatedBy","DocCharConvert.Config");
        mainAttrib.putValue("Class-Path",ooClassPath);
        System.out.println(mainAttrib);
        File ooClassPathJar = new File(basePath, "ooClassPath.jar");
        try
        {
          java.io.FileOutputStream fos = new java.io.FileOutputStream(ooClassPathJar);
          java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(fos, manifest);
          jos.close();
        }
        catch (java.io.FileNotFoundException e)
        {
            System.out.println(e.getMessage());
        }
        catch (java.io.IOException e)
        {
            System.out.println(e.getMessage());
        }
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
