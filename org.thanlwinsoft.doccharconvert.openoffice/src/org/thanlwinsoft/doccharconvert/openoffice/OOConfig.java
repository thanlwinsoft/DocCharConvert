package org.thanlwinsoft.doccharconvert.openoffice;

import java.io.File;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import org.thanlwinsoft.doccharconvert.Config;

public class OOConfig 
{
	public static final String OO_DEFAULT_UNO = "uno:socket,host=localhost,port=8100,tcpNoDelay=1;urp;StarOffice.ServiceManager";
    public static final String OO_DEFAULT_OPTIONS = "-accept=socket,port=8100,tcpNoDelay=1;urp;";
	private String oouno = OO_DEFAULT_UNO;
	private String ooOptions = OO_DEFAULT_OPTIONS;
	private String ooClassPath = "";
    private String ooPath = "soffice";
	private final String OO_INVALID_PATH = "Invalid OpenOffice path: ";
    private final String OO_CLASSES_UNFOUND = "OpenOffice classes not found:";
    private final String OO_PATH_FAIL = 
        "\nYou will not be able to convert OpenOffice files until this is fixed.\n" +
        "You can change the OpenOffice location in the configuration dialog after installation.\n" +
        "Text file conversions will still work.";
    public static  final String OOPATH = "OOPath";
    public static  final String OOUNO = "OOUNO";
    public static  final String OOOPTIONS = "OOOptions";
    
    public OOConfig()
    {
    	
    }
    
    public URL [] getOOClasses()
    {
    	ResourceBundle i18nResource = Config.getCurrent().getMsgResource();
        try
        {
            StringBuffer ooPaths = new StringBuffer();
            File classDir = new File(new File(ooPath).getParent(),"classes");
            if (!classDir.exists()) 
            {
              System.out.println(OO_CLASSES_UNFOUND + classDir.toString() + 
                                 OO_PATH_FAIL);
              Object [] args = {classDir.toString()};
              String msg = MessageFormat.format(i18nResource.getString("no_oo_classes"), 
                                     args);
              JOptionPane.showMessageDialog(null, msg);
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
            urls[0] = Config.getCurrent().getBasePath().toURI().toURL();
            for (int i = 0; i<jarFiles.length; i++)
            {
                urls[i + 1] = jarFiles[i].toURI().toURL();
                String tempPath = jarFiles[i].toURI().toString();//jarFiles[i].getCanonicalPath();
                ooPaths.append(tempPath);
                ooPaths.append(" ");
            }
            ooClassPath = ooPaths.toString();
            System.out.println("OpenOffice class configuration successful!");
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
    	Preferences packagePref = Config.getCurrent().getPrefs();
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
        File ooClassPathJar = new File(Config.getCurrent().getBasePath(), "ooClassPath.jar");
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
        Config.getCurrent().getPrefs().put(OOOPTIONS, ooOptions);
    }
    public void setOOUNO(String unoPath)
    {
        this.oouno = unoPath;
        Config.getCurrent().getPrefs().put(OOUNO, oouno);
    }
    public String getOOPath() { return ooPath; }
    public String getOOOptions() { return ooOptions; }
    public String getOOUNO() { return oouno; }
}
