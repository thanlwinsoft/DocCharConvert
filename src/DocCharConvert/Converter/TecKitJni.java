/*
 * TecKitJni.java
 *
 * Created on August 6, 2004, 9:59 PM
 */

package DocCharConvert.Converter;
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
    static boolean loadLibrary(File libraryPath)
    {
        if (!libraryLoaded) 
        {
            try
            {
                File library = new File
                    (libraryPath, 
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
        return libraryLoaded;
    }
    public static boolean isLibraryLoaded() { return libraryLoaded; }
    
    /** Creates a new instance of TecKitJni */
    public TecKitJni()
    {
        
    }
    
    public native long createConverter(String path, boolean toUnicode);
    
    public native byte [] convert(long convId, byte [] inputArray);
    
    public native void flush(long convId);
    public native void destroyConverter(long convId);
}
