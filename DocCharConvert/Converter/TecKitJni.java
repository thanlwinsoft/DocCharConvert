/*
 * TecKitJni.java
 *
 * Created on August 6, 2004, 9:59 PM
 */

package DocCharConvert.Converter;
import DocCharConvert.Config;
import java.net.URL;
import java.io.File;
/**
 *
 * @author  keith
 */
public class TecKitJni
{
    static final String LIBRARY = "TecKitJni";
    static boolean libraryLoaded = false;
    //static final String PATH = "/usr/local/lib/";
    //static final String PATH = "/home/keith/projects/DocCharConvert/teckitjni/src/";
    static 
    {
        try
        {
            File library = new File
                (Config.getCurrent().getBasePath(), 
                System.mapLibraryName(LIBRARY));
            System.load(library.getAbsolutePath());
            System.out.println("Loaded " + System.mapLibraryName(LIBRARY));
            libraryLoaded = true;
        }
        catch (UnsatisfiedLinkError e)
        {
            System.out.println(e);
            System.out.println(e.getMessage() + " " + 
                System.mapLibraryName(LIBRARY) + " " + 
                System.getProperty("java.library.path"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    public static boolean isLibraryLoaded() { return libraryLoaded; }
    
    /** Creates a new instance of TecKitJni */
    public TecKitJni()
    {
        
    }
    
    public native boolean createConverter(String path, boolean toUnicode);
    //public native String convert(String aString);
    
    public native byte [] convert(byte [] inputArray);
    
    public native void flush();
    public native void destroyConverter();
}
