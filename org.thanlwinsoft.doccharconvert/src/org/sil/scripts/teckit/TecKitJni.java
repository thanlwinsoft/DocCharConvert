/*
 ------------------------------------------------------------------------
Copyright (C) 2002-2004 Keith Stribley

Distributable under the terms of either the Common Public License or the
GNU Lesser General Public License, as specified in the LICENSING.txt file.

File: TecKitJni.java
Responsibility: Keith Stribley
Last reviewed: Not yet.

Description:
    Implements a JNI interface to the TECkit conversion engine.
-------------------------------------------------------------------------*/

package org.sil.scripts.teckit;
//import java.net.URL;
import java.io.File;
/**
 *
 * @author  keith
 */
public class TecKitJni
{
    private static final String LIBRARY = "TecKitJni";
    private static final String [] LIB_PATHS = {
    	"/usr/lib/",
    	"/usr/local/lib/"
    };
    private static boolean libraryLoaded = false;
    /**
     * 
     * @param libraryPath
     * @return true if library loaded successfully
     */
    public static boolean loadLibrary(File libraryPath)
    {
        if (!libraryLoaded) 
        {
        	String systemLibName = System.mapLibraryName(LIBRARY);
            File library = new File(libraryPath, systemLibName);
            try
            {
                // try first with the specified library path
                System.load(library.getAbsolutePath());
                System.out.println("Loaded " + systemLibName);
                libraryLoaded = true;
            }
            catch (UnsatisfiedLinkError eFirst)
            {
            	// only give a message if the file exists, but failed to load
            	if (library.exists())
            		System.out.println(eFirst.getMessage());	
                // if that fails, see if it is in the system path
                try
                {
                  System.loadLibrary(LIBRARY); 
                  System.out.println("Loaded system library:" + LIBRARY);
                  libraryLoaded = true;
                }
                catch (UnsatisfiedLinkError eSecond)
                {
                	for (int i = 0; i < LIB_PATHS.length; i++)
                	{
	                	try
	                    {
	                      System.load(LIB_PATHS[i] + systemLibName); 
	                      System.out.println("Loaded system library:" + 
	                    		             LIB_PATHS[i] +systemLibName);
	                      libraryLoaded = true;
	                    }
	                    catch (UnsatisfiedLinkError e)
	                    {
	                    	//System.out.println(e.getMessage());	                    	
	                    }
                	}
                	if (libraryLoaded == false)
                    {
                        System.out.println(eSecond.getMessage());
                		System.out.println("Library:" + 
                        	System.mapLibraryName(LIBRARY) + " not found in: " + 
                        	System.getProperty("java.library.path"));
                    }
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return libraryLoaded;
    }
    /**
     * Checks whether the library is already loaded.
     * @return true if the library has been loaded.
     */
    public static boolean isLibraryLoaded() { return libraryLoaded; }
    
    /** Creates a new instance of TecKitJni */
    public TecKitJni()
    {
        
    }
    /**
     * Creates a converter for a given tec file.
     * @param path the full path to the .tec file
     * @param toUnicode true if the direction is from bytes to Unicode
     * false if the direction is from Unicode to bytes
     * @return id of the converter created. This must be used in subsequent 
     * invocations to convert.
     */
    public native long createConverter(String path, boolean toUnicode);
    /**
     * Creates a converter for a given tec file.
     * @param tec data
     * @param the bytes from the .tec file
     * @param toUnicode true if the direction is from bytes to Unicode
     * false if the direction is from Unicode to bytes
     * @return id of the converter created. This must be used in subsequent 
     * invocations to convert.
     */
    public native long createConverterFromBuffer(byte [] tec, boolean toUnicode);
    /**
     * Converts the input byte array using the specified converter, which must
     * have already be created with #createConverter.
     * @param convId returned by #createConverter
     * @param inputArray of bytes or UTF-8 encoded unicode.
     * @return byte array of converted string as UTF-8 or bytes depending on direction.
     */
    public native byte [] convert(long convId, byte [] inputArray);
    /**
     * Flushes the converter.
     * @param convId returned by #createConverter
     */
    public native void flush(long convId);
    /**
     * Destroys the specified converter, releasing any resources.
     * @param convId returned by #createConverter
     */
    public native void destroyConverter(long convId);
}
