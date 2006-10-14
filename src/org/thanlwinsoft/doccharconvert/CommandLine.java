package org.thanlwinsoft.doccharconvert;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
import org.thanlwinsoft.doccharconvert.converter.ReversibleConverter;

public class CommandLine
{

    /**
     * @param args
     */
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            System.exit(CommandLine.runCommandLine(args));
        }
        try
        {
            printUsage();
            System.out.println("Warning: The Swing GUI is deprecated. ");
            System.out.println("Warning: It will no longer be maintained. ");
            System.out.println("Warning: You are recommended to use the Eclipse GUI instead.");
            // This is to try to allow the CommandLine to be used in a non-gui
            // environment such as an SSH shell on Linux.
            Class mainFormClass = ClassLoader.getSystemClassLoader()
                .loadClass("org.thanlwinsoft.doccharconvert.MainForm");
            Method guiMain = mainFormClass.getMethod("main", String[].class);
            guiMain.invoke(null, (Object)args);
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
    
    protected static File getConverterPath()
    {
        File converterConfigPath = Config.getCurrent().getConverterPath();
        System.out.println("Using config dir:" + 
            converterConfigPath.getAbsolutePath());
        return converterConfigPath;
    }
    
    
    protected static int runCommandLine(String args[])
    {
        if (args.length > 0)
        {
            final int NORMAL = 0;
            final int IN_ENC = 1;
            final int OUT_ENC = 2;
            final int FILE_LIST = 3;
            final int OOO_SETUP = 4;
            final int CONV_PATH = 5;
            Charset inEnc = null;
            Charset outEnc = null;
            int normalArgCount = 0;
            int state = NORMAL;
            String converter = null;
            String mode = null;
            String inputPath = null;
            String outputPath = null;
            File fileList = null;
            boolean isReverse = false;
            for (int a = 0; a<args.length; a++)
            {
              if (state == NORMAL)
              {
                if (args[a].equals("-i")) 
                {
                  state = IN_ENC;
                  continue;
                }
                else if (args[a].equals("-o")) 
                {
                  state = OUT_ENC;
                  continue;
                }
                else if (args[a].equals("-f")) 
                {
                  state = FILE_LIST;
                  continue;
                }
                else if (args[a].equals("-r")) 
                {
                  state = NORMAL;
                  isReverse = true;
                  continue;
                }
                else if (args[a].equals("--help")) 
                {
                  printUsage();
                  return 0;
                }
                else if (args[a].equals("--oopath")) 
                {
                  state = OOO_SETUP;
                  continue;
                }
                else if (args[a].equals("--converters")) 
                {
                  state = CONV_PATH;
                  continue;
                }
                else if (args[a].startsWith("-"))
                {
                  System.out.println("Unknown option: " + args[a]);
                  printUsage();
                  return 3;
                }
                else
                {
                  switch (normalArgCount)
                  {
                    case 0:
                      converter = args[a];
                      break;
                    case 1:
                      mode = args[a];
                      break;
                    case 2: 
                      inputPath = args[a];
                      break;
                    case 3:
                      outputPath = args[a];
                      break;
                    default:
                      System.out.println("Ignoring argument " + args[a]);
                  }
                  normalArgCount++;
                }
              }
              else 
              {
                try
                {
                  switch (state)
                  {
                  case IN_ENC:
                    inEnc = Charset.forName(args[a]);
                    break;
                  case OUT_ENC:
                    outEnc = Charset.forName(args[a]);
                    break;
                  case FILE_LIST:
                    fileList = new File(args[a]);
                    break;
                  case OOO_SETUP:
                    Config.getCurrent().setOOPath(args[a]);
                    return 0;
                  case CONV_PATH:
                      File path = new File(args[a]);
                      if (path.isDirectory())
                          Config.getCurrent().setConverterPath(path);
                      break;
                  }
                }
                catch (java.nio.charset.IllegalCharsetNameException e)
                {
                  System.out.println("Illegal Charset: " + e.getLocalizedMessage());
                  printUsage();
                  return 5;
                }
                catch (java.nio.charset.UnsupportedCharsetException e)
                {
                  System.out.println("Unknown charset: "+ e.getLocalizedMessage());
                  printUsage();
                  return 6;
                }
                state = NORMAL;
              }
              
            }
            // check that minimum number of arguments reached
            if (normalArgCount < 4)
            {
              if (normalArgCount < 2 || fileList == null)
              {
                printUsage();
                return 7;
              }
            }
            BatchConversion conv = new BatchConversion();
            try 
            {
              conv.setConversionMode(ConversionMode.getById(Integer.parseInt(mode)));
              conv.setCommandLine(true);
            }
            catch (NumberFormatException e)
            {
              System.out.println(e.getLocalizedMessage());
              printUsage();
              return 4;
            }
            if (inEnc != null) conv.setInputEncoding(inEnc);
            else conv.setInputEncoding(Charset.forName("UTF-8"));
            if (outEnc != null) conv.setOutputEncoding(outEnc);
            else conv.setOutputEncoding(Charset.forName("UTF-8"));
            
            File convPath = getConverterPath();
            ConverterXmlParser xmlParser = 
                new ConverterXmlParser(convPath);
            File convXml = new File(convPath, converter);
            if (!xmlParser.parseFile(convXml))
            {
                System.out.println(xmlParser.getErrorLog());
                return 2;
            }
            int convIndex = 0; 
            while (convIndex < xmlParser.getConverters().size())
            {
              CharConverter cc =
                (CharConverter)xmlParser.getConverters().elementAt(convIndex++);
              if (cc instanceof ChildConverter)
              {
                ChildConverter cccc = (ChildConverter)cc;
                if (cccc.getParent() instanceof ReversibleConverter)
                {
                  if (((ReversibleConverter)cccc.getParent()).isForwards())
                  {
                    if (!isReverse) 
                    {
                        conv.addConverter(cc);
                        System.out.println(cc);  
                    }
                  }
                  else
                  {
                    if (isReverse) 
                    {
                        conv.addConverter(cc);     
                        System.out.println(cc);  
                    } 
                  }
                }
                else conv.addConverter(cc);
              }
              else conv.addConverter(cc);
            }
            conv.setPairsMode(true);
            if (fileList == null)
            {
              File input = new File(inputPath);
              File output = new File(outputPath);
              conv.addFilePair(input,output);
            }
            else
            {
                MainForm.loadFileList(conv, fileList);
            }
            new Thread(conv).start();
            do 
            {
                try { Thread.sleep(100); }
                catch (java.lang.InterruptedException e) {}
            } while (conv.isRunning());
            conv.destroy();
            return 0;
        }
        else
        {
            printUsage();
            return 1;
        }
    }
    
    private static void printUsage()
    {
      File [] converterFiles = ConverterXmlParser.getConverterFiles(getConverterPath());
      System.out.println("Arguments: [-i iEnc] [-o oEnc] [-r] converter.dccx mode "); 
      System.out.println("           [-f list]|[inputFile outputFile]");
      System.out.println("           [--converters ConvertersPath]");
      System.out.println("Modes:");
      for (int m = 0; m<ConversionMode.NUM_MODES; m++)
      {
          System.out.println("\t" + m + "\t" + ConversionMode.getById(m));
      }
      System.out.println("Optional Arguments:");
      System.out.println("\t--help display this help");
      System.out.println("\t-r use the converter in reverse mode");
      System.out.println("\t-i iEnc = input encoding e.g. -i iso-8859-1 (default UTF-8)");
      System.out.println("\t-o oEnc = output encoding e.g. -o iso-8859-1 (default UTF-8)");
      System.out.println("\t-f fileList = file containing list input output files");
      System.out.println("\t--converters path = change the default Converters dir to path");
      System.out.println("Please choose from one of the following converters:");
      for (int i = 0; i<converterFiles.length; i++)
      {
          System.out.println("\t" + converterFiles[i].getName());          
      }
      System.out.println("Run with no arguments for Graphical mode and Configuration editor.");
    }
    
}
