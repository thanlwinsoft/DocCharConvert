/*
 * TecKitConverter.java
 *
 * Created on August 7, 2004, 7:00 PM
 */

package DocCharConvert.Converter;

import DocCharConvert.TextStyle;
import DocCharConvert.Config;

import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

/**
 *
 * @author  keith
 */
public class TecKitConverter extends ReversibleConverter
{
    private TecKitJni jni = null;
    private boolean initOk = false;
    private String mapFilePath = null;
    private String name = "TECKit";
    private ByteArrayOutputStream os = null;
    private StringBuffer ob = null;
    private Charset beforeCharset = null;
    private Charset afterCharset = null;
    private long converterInstance = 0;
    /** Creates a new instance of TecKitConverter */
    public TecKitConverter(File mapFile, TextStyle origFont, TextStyle targFont)
    {
        TecKitJni.loadLibrary(Config.getCurrent().getConverterPath());
        construct(mapFile, origFont, targFont);
    }
    public TecKitConverter(File mapFile)
    {
        TecKitJni.loadLibrary(Config.getCurrent().getConverterPath());
        construct(mapFile,null,null);
    }
    private void construct(File mapFile, TextStyle origFont, TextStyle targFont)
    {
        
        os = new ByteArrayOutputStream(128);
        ob = new StringBuffer(128);
        jni = new TecKitJni();
        try {
            mapFilePath = mapFile.getCanonicalPath();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
            mapFilePath = "Invalid MAP";
        }
        setOriginalStyle(origFont);
        setTargetStyle(targFont);
        name = "TECKit<" + mapFile.getName() + ">";
    }
    
    public void initialize() throws CharConverter.FatalException
    {
        if (initOk) return;
        System.out.println(this + "initialize");
        if (!TecKitJni.isLibraryLoaded())
        {
            throw new CharConverter.FatalException(
                "TecKit failed to load system library");
        }
        try
        {
            converterInstance = jni.createConverter(mapFilePath,isForwards());
            if (converterInstance == 0) initOk = false;
            else initOk = true;
            if (isForwards())
            {
                beforeCharset = Charset.forName("ISO-8859-1");
                afterCharset = Charset.forName("UTF-8");
            }
            else
            {
                beforeCharset = Charset.forName("UTF-8");
                afterCharset = Charset.forName("ISO-8859-1");
            }
        }
        catch (Exception e)
        {
            initOk = false;
            e.printStackTrace();
        }
        finally 
        {
            if (initOk == false)
            {
                throw new CharConverter.FatalException(
                    "TecKit failed to initialise with map:" + mapFilePath);
            }
        }
    }
    
    public String convert(String oldText) throws CharConverter.FatalException,
        CharConverter.RecoverableException
    {        
        if (initOk == false)
        {
            throw new CharConverter.FatalException(
                "TecKit not initialised");
        }
        // catch the empty string and just return it
        if (oldText.length() == 0) return oldText;
        String newText = convert(oldText, beforeCharset, afterCharset);
        //String newText = jni.convert(oldText);
        if (newText == null) 
        {
            throw new CharConverter.RecoverableException("TecKit converted <" 
                + oldText + "> to empty string");
        }
        return newText;
    }
    
    protected String convert(String source, Charset oldEncoding, Charset newEncoding) 
        throws CharConverter.FatalException
    {
        // encode
        ob.delete(0,ob.length());
        os.reset();
        try
        {
            OutputStreamWriter or = new OutputStreamWriter(os,oldEncoding);
            BufferedWriter bWriter = new BufferedWriter(or);
            bWriter.write(source);
            bWriter.close();
            byte [] inputBytes = os.toByteArray();
            // decode
            byte [] convertedBytes = jni.convert(converterInstance, inputBytes);
            ByteArrayInputStream is = new ByteArrayInputStream(convertedBytes);
            InputStreamReader ir = new InputStreamReader(is, newEncoding);
            BufferedReader bReader = new BufferedReader(ir);
            // reset the array ready for the next conversion
            String line = null;
            do 
            {
                line = bReader.readLine();
                if (line != null) ob.append(line);
            } while (line != null);
            bReader.close();
        }
        catch (java.io.IOException e)
        {
            throw new CharConverter.FatalException(
                "TecKit " + e.getLocalizedMessage());
            
        }
        return ob.toString();
    }
    
    public void destroy()
    {
        if (converterInstance != 0)
            jni.destroyConverter(converterInstance);
        converterInstance = 0;
        initOk = false;
    }
    
    public String getName() { return name; }
    public void setName(String aName) { this.name =aName; }
    public boolean isInitialized() { return initOk; }
}
