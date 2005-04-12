/*
 * Config.java
 *
 * Created on August 28, 2004, 11:03 AM
 */

package DocCharConvert;

import java.io.File;
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
    private String ooPath = "soffice";
    public static final String OO_DEFAULT_UNO = "uno:socket,host=localhost,port=8100,tcpNoDelay=1;urp;StarOffice.ServiceManager";
    public static final String OO_DEFAULT_OPTIONS = "-accept=socket,port=8100,tcpNoDelay=1;urp;";
    private String oouno = OO_DEFAULT_UNO;
    private String ooOptions = OO_DEFAULT_OPTIONS;
    private String ooClassPath = "";
    private final String OO_INVALID_PATH = "Invalid OpenOffice path: ";
    private final String OO_CLASSES_UNFOUND = "OpenOffice classes not found:";
    private final String OO_PATH_FAIL = 
        "\nYou will not be able to convert OpenOffice files until this is fixed.\n" +
        "You can change the OpenOffice location in the configuration dialog after installation.\n" +
        "Text file conversions will still work.";
    private ResourceBundle i18nResource = null;
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
            File defaultConvPath = converterPath;
            converterPath = new File(packagePref.get(INSTALL_PATH, 
                                                converterPath.getCanonicalPath()));
            if (!converterPath.exists()) 
            {
                // revert to default
                converterPath = defaultConvPath;
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
        try
        {
            i18nResource = ResourceBundle.getBundle("DocCharConvert/Messages");
        }
        catch (java.util.MissingResourceException mre)
        {
             System.out.println(mre.getMessage());
        }
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
            if (!classDir.exists()) 
            {
              System.out.println(OO_CLASSES_UNFOUND + classDir.toString() + 
                                 OO_PATH_FAIL);
              MessageFormat mf = new MessageFormat("");
              Object [] args = {classDir.toString()};
              String msg = mf.format(i18nResource.getString("no_oo_classes"), 
                                     args);
              return null;
            }
            java.io.FilenameFilter jarFilter = new java.io.FilenameFilter() {
                public boolean accept(File parent, String name)
                {
                    if (name.toLowerCase().endsWith(".jar")) return true;
                    return false;
                }
            };
            if (classDir == null) return null;
            File [] jarFiles = classDir.listFiles(jarFilter);
            if (jarFiles == null) return null;
            URL [] urls = new URL[jarFiles.length + 1];
            urls[0] = basePath.toURL();
            for (int i = 0; i<jarFiles.length; i++)
            {
                urls[i + 1] = jarFiles[i].toURL();
                //ooPaths.append('"');
                String tempPath = jarFiles[i].toString();//jarFiles[i].getCanonicalPath();
                //ooPaths.append(tempPath.replaceAll("\\\\","/"));
                ooPaths.append(tempPath);
                //ooPaths.append("\" ");
                ooPaths.append(" ");
                //System.out.println(urls[i+1].toString());
            }
            ooClassPath = ooPaths.toString();
            System.out.println("OpenOffice class configuration successful!");
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
        File testFile = null;
        File testDir = new File(path);
        if (testDir.isDirectory())
        {
            testDir = new File(testDir, "program");
            if (testDir.isDirectory())
            {
                testFile = new File(testDir, "soffice");
                if (testFile.isFile()) this.ooPath = testFile.getAbsolutePath();
                else 
                {
                    testFile = new File(testDir, "soffice.exe");
                    if (testFile.isFile()) this.ooPath = testFile.getAbsolutePath();
                    else 
                    {
                        // doesn't seem valid
                        System.out.println(OO_INVALID_PATH + path + OO_PATH_FAIL);
                        return;
                    }
                }
            }
        }
        else
        {
            if (!testDir.isFile()) 
            {
                // doesn't seem valid
                System.out.println(OO_INVALID_PATH + path + OO_PATH_FAIL);
                return;
            }    
        }
        packagePref.put(OOPATH, ooPath);
        getOOClasses();
        java.util.jar.Manifest manifest = new java.util.jar.Manifest();
        java.util.jar.Attributes mainAttrib = manifest.getMainAttributes();
        mainAttrib.putValue("Manifest-Version","1.0");
        mainAttrib.putValue("CreatedBy","DocCharConvert.Config");
        mainAttrib.putValue("Class-Path",ooClassPath);
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
    public ResourceBundle getMsgResource() { return i18nResource; }
}
