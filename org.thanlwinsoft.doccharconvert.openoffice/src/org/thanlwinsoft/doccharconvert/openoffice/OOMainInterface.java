/*
 *  Copyright (C) 2005 Keith Stribley <doccharconvert@thanlwinsoft.org>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -----------------------------------------------------------------------
 * $HeadURL: $
 * $LastChangedBy: keith $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package org.thanlwinsoft.doccharconvert.openoffice;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.beans.XPropertySet;
import com.sun.star.bridge.XUnoUrlResolver;

import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.ConversionMode;
import org.thanlwinsoft.doccharconvert.DocInterface;
import org.thanlwinsoft.doccharconvert.ProgressNotifier;
import org.thanlwinsoft.doccharconvert.TextStyle;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;

/**
 *
 * @author  keith
 */
public class OOMainInterface implements DocInterface
{
    public final static String OO_PATH = "soffice";
    public final static String UNO_URL = OOConfig.OO_DEFAULT_UNO;
    public final static String RUN_OO = OOConfig.OO_DEFAULT_OPTIONS;
    private XComponentContext xRemoteContext = null;

    private XMultiComponentFactory xRemoteServiceManager = null;

    private Process ooProcess = null;
    private int sleepCount = 0;
    private int SLEEP = 10;
    private int MAX_SLEEP_COUNT = 3000; // wait 30 seconds
    private boolean onlyStylesInUse = false;
    private OODocParser parser = null;
    private boolean abort = false;
    private ConversionMode mode =  null;
    private OOConfig ooConfig = new OOConfig();
    /** Creates a new instance of OOMainInterface */
    public OOMainInterface()
    {
        
    }
    public void setOnlyStylesInUse(boolean onlyConvertStylesInUse)
    {
        this.onlyStylesInUse = onlyConvertStylesInUse;
    }
    
    public void initialise() throws InterfaceException
    {
        try
        {
            if (xRemoteServiceManager == null)
            {
                createConnection();
            }
        }
        catch (Exception e)
        {
            System.out.println(e);
            throw new InterfaceException(new String(e.getLocalizedMessage()));
        }
    }
    
    
    
    public void destroy()
    {
        if (ooProcess != null)
        {
            try
            {
                // detect if it has alreay exited another
                ooProcess.exitValue();
            }
            catch (IllegalThreadStateException e)
            {
                // not yet terminated so don't do anything
                System.out.println(
                    "\"" + ooConfig.getOOPath() + 
                    "\" is still running. Killing...");
                // forcibly destroy it
                ooProcess.destroy();
            }
            ooProcess = null;
        }
        parser = null;
        xRemoteContext = null;
        xRemoteServiceManager = null;
    }

    protected void useConnection(File inputFile, File outputFile, Map<TextStyle,CharConverter> converters) 
        throws InterfaceException, WarningException
    {
        synchronized (this) {  abort = false; }
        try {
            if (xRemoteServiceManager == null)
            {
                createConnection();
            }
            // do something with the service manager...
            
            if (parser == null)
            {
                parser = new OODocParser(this);
            }
            try
            {       
                //parser = new OODocParser(this);
                parser.openDoc(inputFile);
                parser.setOnlyStylesInUse(onlyStylesInUse);
                parser.parse(converters);
                parser.saveDocAs(outputFile.toURI().toURL().toExternalForm());
                parser.closeDoc();
                String warnings = parser.getWarnings();
                if (warnings.length()>0)
                {
                    throw new DocInterface.WarningException(warnings);
                }
            }
            catch (java.net.MalformedURLException e)
            {
                System.out.println(e.getLocalizedMessage());
                throw new InterfaceException(new String(e.getLocalizedMessage()));
            }
            catch (CharConverter.FatalException e)
            {
                System.out.println(e.getLocalizedMessage());
                throw new InterfaceException(new String(e.getLocalizedMessage()));
            }
            catch (InterfaceException e)
            {
                parser = null; // force reset of parser next time around
                xRemoteContext = null;
                xRemoteServiceManager = null;
                throw e;
            }
            finally
            {
                
            }
        }
        catch (com.sun.star.io.IOException e)
        {
            e.printStackTrace();
            throw new InterfaceException(new String(e.getLocalizedMessage()));
        }
        catch (com.sun.star.uno.Exception e)
        {
            e.printStackTrace();
            throw new InterfaceException(new String(e.getLocalizedMessage()));
        }
        /*
        catch (com.sun.star.connection.NoConnectException e) 
        {
                System.err.println("No process listening on the resource");
                e.printStackTrace();
                throw new InterfaceException(e.getLocalizedMessage());
        }*/

        catch (com.sun.star.lang.DisposedException e) { //works from Patch 1

            xRemoteContext = null;

            throw new InterfaceException(new String(e.getLocalizedMessage()));

        }          

    }

    public void createConnection() throws InterfaceException
    {
        sleepCount = 0;
        try 
        {    
            xRemoteServiceManager = this.getRemoteServiceManager(ooConfig.getOOUNO());
        }
        
        catch (com.sun.star.connection.NoConnectException e) 
        {
            System.err.println("No process listening on the resource");
            spawnLocalOO();
        }
        
        while (xRemoteServiceManager == null)
        {
            try
            {
                xRemoteServiceManager = this.getRemoteServiceManager(ooConfig.getOOUNO());                
            }
            catch (com.sun.star.connection.NoConnectException e1)
            {   
                
                waitForOOToStart();
            }
        }
        
        String available = (null != xRemoteServiceManager ? 
            "available" : "not available");
        System.out.println("remote ServiceManager is " + available);
    }

    protected void waitForOOToStart() throws InterfaceException
    {
        try 
        {           
            int eValue = 0;
            if ((eValue = ooProcess.exitValue()) != 0)
            {
                ooProcess = null;
                throw new InterfaceException(ooConfig.getOOPath() + " exited with " + eValue);
            }
            else
            {
                if (++sleepCount > MAX_SLEEP_COUNT) 
                {
                    throw new InterfaceException(Config.getCurrent().getMsgResource()
                                                 .getString("oo_connect_timeout"));
                }
            }
        }
        catch (IllegalThreadStateException e2)
        {
            // thread still going so wait 
            try
            {
                Thread.sleep(SLEEP);
            }
            catch (InterruptedException e) {} // ignore
            if (++sleepCount > MAX_SLEEP_COUNT) 
            {
                throw new InterfaceException(Config.getCurrent().getMsgResource()
                                             .getString("oo_connect_timeout"));
            }
        }
    }
    
    protected void spawnLocalOO() throws InterfaceException
    {
        // try to start OO ourselves
        if (ooProcess == null)
        {
        	System.err.println("Executing: " + ooConfig.getOOPath() + " -headless " +
                ooConfig.getOOOptions());
            try
            {
                // oo dir is grand father of program
                File ooDir = (new File(ooConfig.getOOPath()))
                    .getParentFile();
                if (ooDir != null) ooDir = ooDir.getParentFile();
                //ooProcess = Runtime.getRuntime().exec(args, null, ooDir);
                ProcessBuilder pb = new ProcessBuilder(
                		ooConfig.getOOPath(), //"-headless", 
                		ooConfig.getOOOptions());
                pb.redirectErrorStream(true);
                pb.directory(ooDir);
                
        
                ooProcess = pb.start();
                if (ooProcess != null) 
                {
                    BufferedReader br = new BufferedReader(
                            new InputStreamReader(ooProcess.getInputStream()));
                    final BufferedReader ooor = br;
                    Runnable stdoutMonitor =  new Runnable() {
                        public void run()
                        {                            
                            System.out.println("OO Stdout monitor running\n");
                            try {
                                // need to read stdout otherwise it stalls OpenOffice
                                String line = "";
                                while (line != null) {
                                    line = ooor.readLine();
                                    //System.out.println(line);
                                }
                            }
                            catch (java.io.IOException e)
                            {
                             System.out.println(e.getMessage());

                            }
                            try {
                                if (ooor != null) ooor.close();
                            }
                            catch (java.io.IOException e)
                            {
                                System.out.println(e.getMessage());
                            }
                            System.out.println("OO Stdout monitor finished\n");
                        }
                    };
                    new Thread(stdoutMonitor).start();
                }
            }
            catch (java.io.IOException ioe)
            {
                throw new InterfaceException (new String(ioe.getLocalizedMessage()));
            }
        }
    }
    
    protected XMultiComponentFactory getRemoteServiceManager(String unoUrl) 
        throws com.sun.star.connection.NoConnectException, InterfaceException
    {

        if (xRemoteContext == null) {

            // First step: create local component context, get local 
            // servicemanager and ask it to create a UnoUrlResolver object with 
            // an XUnoUrlResolver interface
            XComponentContext xLocalContext = null;
            try
            {
                xLocalContext = com.sun.star.comp.helper.Bootstrap
                    .createInitialComponentContext(null);
            
            XMultiComponentFactory xLocalServiceManager = 
                xLocalContext.getServiceManager();

            Object urlResolver  = xLocalServiceManager.createInstanceWithContext
                ("com.sun.star.bridge.UnoUrlResolver", xLocalContext );
            // query XUnoUrlResolver interface from urlResolver object

            XUnoUrlResolver xUnoUrlResolver = (XUnoUrlResolver) 
                UnoRuntime.queryInterface(XUnoUrlResolver.class, urlResolver);

            // Second step: use xUrlResolver interface to import the remote 
            // StarOffice.ServiceManager, retrieve its property DefaultContext 
            // and get the remote servicemanager
            Object initialObject = xUnoUrlResolver.resolve(unoUrl);
            XPropertySet xPropertySet = (XPropertySet)
                UnoRuntime.queryInterface(XPropertySet.class, initialObject);
            Object context = xPropertySet.getPropertyValue("DefaultContext");            
            xRemoteContext = (XComponentContext)
                UnoRuntime.queryInterface(XComponentContext.class, context);
            }
            catch (com.sun.star.connection.NoConnectException e)
            {
                throw e;
            }
            catch (com.sun.star.connection.ConnectionSetupException e)
            {
                e.printStackTrace();
                throw new InterfaceException (e.getLocalizedMessage());
            }
            catch (com.sun.star.beans.UnknownPropertyException e)
            {
                e.printStackTrace();
                throw new InterfaceException (e.getLocalizedMessage());
            }
            catch (com.sun.star.uno.Exception e)
            {
                e.printStackTrace();
                throw new InterfaceException (e.getLocalizedMessage());
            }
            catch (java.lang.Exception e)
            {
                e.printStackTrace();
                throw new InterfaceException (e.getLocalizedMessage());
            }
        }

        return xRemoteContext.getServiceManager();
    }
     
    public XMultiComponentFactory getRemoteServiceManager() 
        throws InterfaceException, com.sun.star.connection.NoConnectException
    {
        return getRemoteServiceManager(ooConfig.getOOUNO());
    }
    public XComponentContext getRemoteContext()
    {
        return xRemoteContext;
    }
    
    
    
    public void parse(File input, File output, java.util.Map<TextStyle,CharConverter> converters, ProgressNotifier notifier) 
        throws org.thanlwinsoft.doccharconvert.converter.CharConverter.FatalException,
        InterfaceException, WarningException
    {
        useConnection(input, output, converters);
        parser = null;
        xRemoteContext = null;
        xRemoteServiceManager = null;
    }
    
    public ConversionMode getMode()
    {
        return mode;
    }
    public void setMode(ConversionMode mode)
    {
        this.mode = mode;
    }
    /** Not implemented yet */
    public void setInputEncoding(Charset iEnc)
    {
      //inputCharset = iEnc;
    }
    public void setOutputEncoding(Charset oEnc)
    {
      //outputCharset = oEnc;
    }
    public String getStatusDesc()
    {
      if (parser == null)
        return new String("Initializing");
      else return parser.getStatusDesc();
    }
    public synchronized void abort()
    {
        abort = true;
    }
    public synchronized boolean doAbort()
    {
        return abort;
    }
}
