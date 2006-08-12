/*
 * OpenDocParser.java
 *
 * Created on 05 August 2006, 21:41
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package org.thanlwinsoft.doccharconvert.opendoc;

//import org.thanlwinsoft.doccharconvert.DocInterface;
import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.Config;
//import org.thanlwinsoft.doccharconvert.DocInterface.InterfaceException;
//import org.thanlwinsoft.doccharconvert.DocInterface.WarningException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.TextStyle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.text.MessageFormat;

import java.nio.charset.Charset;
/**
 *
 * @author keith
 */
public class OpenDocParser implements org.thanlwinsoft.doccharconvert.DocInterface
{
  public final static String STYLES_XML = "styles.xml";
  public final static String CONTENT_XML = "content.xml";
  private java.util.ResourceBundle mr = null;
  private final static int BUFFER_LEN = 1024;
  private OpenDocParseStatus status = OpenDocParseStatus.UNINIT;
  private boolean abort = false;
  
  /** Creates a new instance of OpenDocParser */
    public OpenDocParser()
    {
      mr = Config.messageResource();
    }
    public void initialise() throws InterfaceException
    {

    }
    public void destroy()
    {
      
    }
    public void parse(File input, File output, Map<TextStyle,CharConverter> converters) 
        throws CharConverter.FatalException, InterfaceException, WarningException
    {
      abort = false;
      status = OpenDocParseStatus.UNINIT;
      try
      {
        JarFile inJar = new JarFile(input);
        OutputStream os = new BufferedOutputStream(new FileOutputStream(output));
        JarOutputStream outJar = new JarOutputStream(os);
        ZipEntry styleEntry = inJar.getEntry(STYLES_XML);
        if (styleEntry == null)
        {
          Object [] args = { STYLES_XML, input.getName() };
          String msg = MessageFormat.format(mr.getString("od_fileNotFount"), 
                                     args);
          throw new WarningException(msg);
        }
        else
        {
          status = OpenDocParseStatus.STYLE;
          outJar.putNextEntry(styleEntry);
          parseStyles(inJar.getInputStream(styleEntry), outJar);
          outJar.closeEntry();
        }
        if (abort) throw new WarningException(mr.getString("od_abort"));
        ZipEntry contentEntry = inJar.getEntry(CONTENT_XML);
        if (contentEntry == null)
        {
          Object [] args = { CONTENT_XML, input.getName() };
          String msg = MessageFormat.format(mr.getString("od_fileNotFount"), 
                                     args);
          throw new WarningException(msg);
        }
        else
        {
          status = OpenDocParseStatus.CONTENT;
          outJar.putNextEntry(contentEntry);
          parseContent(inJar.getInputStream(contentEntry), outJar);
          outJar.closeEntry();
        }
        if (abort) throw new WarningException(mr.getString("od_abort"));
        Enumeration<JarEntry> zee = inJar.entries();
        status = OpenDocParseStatus.FILES;
        byte [] buffer = new byte[BUFFER_LEN];
        while (zee.hasMoreElements())
        {
          JarEntry ze = zee.nextElement();
          if (ze.getName().equals(CONTENT_XML) || ze.getName().equals(STYLES_XML))
            continue;
          InputStream is = inJar.getInputStream(ze);
          outJar.putNextEntry(ze);
          int a;
          while ((a = is.available()) > 0)
          {
            int read = Math.min(a, BUFFER_LEN);
            read = is.read(buffer, 0, read);
            outJar.write(buffer, 0, read);
          }
          outJar.closeEntry();
        }
        outJar.close();
        status = OpenDocParseStatus.FINISHED;
      }
      catch (ZipException e)
      {
        System.out.println(e);
        throw new CharConverter.FatalException(e.getMessage());
      }
      catch (IOException e)
      {
        System.out.println(e);
        throw new CharConverter.FatalException(e.getMessage());
      }
    }
    protected void parseStyles(InputStream is, OutputStream os)
    {
      
    }
    protected void parseContent(InputStream is, OutputStream os)
    {
      
    }
    public String getStatusDesc()
    {
      return status.toString();
    }
    public ConversionMode getMode()
    {
      return ConversionMode.OD_MODE;
    }
    public void setInputEncoding(Charset iEnc)
    {

    }
    public void setOutputEncoding(Charset oEnc)
    {

    }
    public void abort()
    {
      abort = true;
    }
    protected enum OpenDocParseStatus {
      UNINIT ("Uninitialised","UNINIT"),
      STYLE ("Parsing Styles","STYLE"),
      CONTENT ("Parsing Content","CONTENT"),
      FILES ("Copying other Jar Files","FILES"),
      FINISHED ("Finished","FINISHED");
      String desc;
      String tag;
      OpenDocParseStatus(String desc, String tag)
      {
        this.tag = tag;
        this.desc = desc;
        String localized = Config.messageResource().getString("odstatus_" + tag);
        if (localized != null)
          this.desc = localized;
      }
      public String toString()
      {        
        return this.desc;
      }
    }
}
