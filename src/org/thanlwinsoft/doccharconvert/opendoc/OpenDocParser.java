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
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
//import org.thanlwinsoft.doccharconvert.DocInterface.InterfaceException;
//import org.thanlwinsoft.doccharconvert.DocInterface.WarningException;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.xml.XmlWriteFilter;
import org.xml.sax.XMLReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLReaderFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
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
 * Parses and OpenDocument XML file, converting styles and text as requested
 * using the appropriate CharConverter instances.
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
  protected JoinTextRuns odFilterJoinRuns = null;
  protected OpenDocFilter odFilterStyles = null;
  protected OpenDocFilter odFilterContent = null;
  private ConversionMode mode = ConversionMode.OD_MODE;
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
    public void parse(File input, File output, 
        Map<TextStyle,CharConverter> converters, ProgressNotifier notifier) 
        throws CharConverter.FatalException, InterfaceException, WarningException
    {
      abort = false;
      status = OpenDocParseStatus.UNINIT;
      OpenDocStyleManager styleManager = new OpenDocStyleManager();
      odFilterStyles = new OpenDocFilter(converters, styleManager, 
                                         OpenDocFilter.FileType.STYLE);
      odFilterContent = new OpenDocFilter(converters, styleManager, 
                                          OpenDocFilter.FileType.CONTENT);
      byte [] buffer = new byte[BUFFER_LEN];
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
          File file = new File("odStyle.xml");
          //File file = File.createTempFile("styles",".xml");
          //file.deleteOnExit();
          ZipEntry newStyleEntry = new ZipEntry(STYLES_XML);
          FileOutputStream styleOut = new FileOutputStream(file);
          parseStyles(inJar.getInputStream(newStyleEntry), styleOut);
          styleOut.close();
          newStyleEntry.setSize(file.length());
          outJar.putNextEntry(newStyleEntry);
          int read;
          FileInputStream fis = new FileInputStream(file);
          do
          {
            read = BUFFER_LEN;
            read = fis.read(buffer, 0, read);
            if (read > 0) outJar.write(buffer, 0, read);
          } while (read > 0);
          fis.close();
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
          ZipEntry newContentEntry = new ZipEntry(CONTENT_XML);
          File file = new File("odContent.xml");
          //File file = File.createTempFile("content",".xml");
          //file.deleteOnExit();
          FileOutputStream contentOut = new FileOutputStream(file);
          parseContent(inJar.getInputStream(newContentEntry), contentOut);
          contentOut.close();
          newContentEntry.setSize(file.length());
          outJar.putNextEntry(newContentEntry);
          int read;
          FileInputStream fis = new FileInputStream(file);
          do
          {
            read = BUFFER_LEN;
            read = fis.read(buffer, 0, read);
            if (read > 0) outJar.write(buffer, 0, read);
          } while (read > 0);
          fis.close();
        }
        if (abort) throw new WarningException(mr.getString("od_abort"));
        Enumeration<JarEntry> zee = inJar.entries();
        status = OpenDocParseStatus.FILES;
        while (zee.hasMoreElements())
        {
          JarEntry ze = zee.nextElement();
          if (ze.getName().equals(CONTENT_XML) || ze.getName().equals(STYLES_XML))
            continue;
          InputStream is = inJar.getInputStream(ze);
          outJar.putNextEntry(ze);
          int read;
          do
          {
            //read = Math.min(a, BUFFER_LEN);
            read = BUFFER_LEN;
            read = is.read(buffer, 0, read);
            if (read > 0) outJar.write(buffer, 0, read);
          } while (read > 0);
          //outJar.closeEntry();
        }
        outJar.close();
        status = OpenDocParseStatus.FINISHED;
      }
      catch (ZipException e)
      {
        System.out.println(e);
        throw new CharConverter.FatalException(e.getMessage());
      }
      catch (SAXException e)
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
        throws IOException, SAXException
    {
      XMLReader myReader = XMLReaderFactory.createXMLReader();
      odFilterJoinRuns = new JoinTextRuns();
      odFilterJoinRuns.setParent(myReader);
      odFilterStyles.setParent(odFilterJoinRuns);
      XmlWriteFilter writeFilter = new XmlWriteFilter(os);
      writeFilter.setParent(odFilterStyles);
      writeFilter.parse(new InputSource(is));
    }
    protected void parseContent(InputStream is, OutputStream os)
        throws IOException, SAXException
    {
        XMLReader myReader = XMLReaderFactory.createXMLReader();
        odFilterJoinRuns = new JoinTextRuns();
        odFilterJoinRuns.setParent(myReader);
        odFilterContent.setParent(odFilterJoinRuns);
        XmlWriteFilter writeFilter = new XmlWriteFilter(os);
        writeFilter.setParent(odFilterContent);
        writeFilter.parse(new InputSource(is));
    }
    public String getStatusDesc()
    {
      return status.toString();
    }
    public ConversionMode getMode()
    {
      return mode;
    }
    public void setMode(ConversionMode mode)
    {
        this.mode = mode;
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
