/**
 * 
 */
package org.thanlwinsoft.doccharconvert;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author keith
 */
public class ConversionHelper
{
    private static IMessageDisplay msgDisplay = new NoGuiMessageDisplay();
    
    public static void setMsgDisplay(IMessageDisplay md)
    {
        msgDisplay = md;
    }
    
    public static void loadFileList(BatchConversion conv, File fileList)
    {
        try
        {
            BufferedReader reader = new BufferedReader
                (new FileReader(fileList));
            Pattern p = Pattern.compile("([^\\s]*?[^\\\\])\\s+(.*)");
            Pattern qp = Pattern.compile("\"([^\"]*?)\"\\s+\"(.*)\"");
            StringBuffer invalidLines = new StringBuffer();
            String line = reader.readLine();
            // if we have got this far without an exception, 
            // then we assume file is OK

            while (line != null)
            {
                String trimmedLine = line.trim();
                Matcher m = qp.matcher(trimmedLine);
                // try match quoted file names if first match fails
                if (!m.matches()) m = p.matcher(trimmedLine);
                if (m.matches())
                {
                    File iFile = new File(m.group(1));
                    File oFile = new File(m.group(2));
                    // if iFile doesn't exist, try appending 
                    // listFile dir to it
                    if (!iFile.exists())
                    {
                        iFile = new File(fileList.getParent(),m.group(1));
                        oFile = new File(fileList.getParent(),m.group(2));
                    }
                    if (oFile.getParentFile().exists() == false)
                    {
                      oFile.getParentFile().mkdirs();
                    }
                    if (iFile.canRead() &&
                        oFile.getParentFile().exists())
                    {
                        conv.addFilePair(iFile, oFile);
                    }
                    else
                    {
                        invalidLines.append(m.group(1));
                        invalidLines.append(' ');
                        invalidLines.append(m.group(2));
                        invalidLines.append(" (File not readable)\n");
                    }
                }
                else
                {
                    invalidLines.append(line);
                    invalidLines.append('\n');
                }
                line = reader.readLine();
            }
            if (invalidLines.length()>0)
            {
                msgDisplay.showWarningMessage(
                        MessageUtil.getString("fileListIgnoreLine") + "\r\n" +
                        invalidLines.toString(),
                        MessageUtil.getString("invalidFormat"));
                
//                Object [] msg = new Object[2];
//                msg[0] = Config.getCurrent().getMsgResource().getString("fileListIgnoreLine");
//                JScrollPane pane = 
//                    new JScrollPane(new JTextArea(invalidLines.toString()));
//                pane.setPreferredSize(new Dimension(400, 300));
//                msg[1] = pane;            
//                
//                JOptionPane.showMessageDialog(null, msg,
//                    Config.getCurrent().getMsgResource().getString("invalidFormat"),
//                    JOptionPane.WARNING_MESSAGE);
            }
            reader.close();
        }
        catch (java.io.IOException e)
        {
            msgDisplay.showWarningMessage(
                    MessageUtil.getString("fileListError") + "\n" +
                    fileList.getName() + "\n" + 
                    e.getLocalizedMessage(),
                    MessageUtil.getString("fileListError"));
//            JOptionPane.showMessageDialog(null,
//                "Error reading " + fileList.getName() + "\n" + 
//                e.getLocalizedMessage(),
//                Config.getCurrent().getMsgResource().getString("fileListError"),
//                JOptionPane.ERROR_MESSAGE);
        }
        catch (java.util.regex.PatternSyntaxException e)
        {
            // shouldn't happen in normal operation
            System.out.println(e.getMessage());
        }
        finally 
        {
        }
    }
    
    public static void saveFileList(BatchConversion conversion, File listFile) throws IOException
    {
        BufferedWriter bw = new BufferedWriter(new FileWriter(listFile));
        if (conversion.getInputFileList() != null)
        {
          Object [] iFiles = conversion.getInputFileList();    
          if (iFiles.length > 0)
          {
              if (iFiles[0] instanceof java.util.Map.Entry &&
                  ((Map.Entry<?,?>)iFiles[0]).getKey() instanceof File)
              {
                  for (int i = 0; i< iFiles.length; i++)
                  {
                    Map.Entry<?,?> pair = (Map.Entry<?,?>)iFiles[i];
                    if (pair.getKey() instanceof File && 
                        pair.getValue() instanceof File)
                    {
                        bw.write("\"" + 
                            ((File)pair.getKey()).getAbsolutePath() + "\" \"" 
                            + ((File)pair.getValue()).getAbsolutePath() + "\"\n");
                    }
                  }
              }

          }
        }
        bw.close();
    }
    
    public static void debugDump(String text, StringBuilder buffer)
    {
        for (int i = 0; i<text.length(); i++)
        {
            String hex = Integer.toHexString(text.charAt(i));
            if (hex.length() < 4) 
            {
              int j = 4 - hex.length();
              while (j-- > 0)
                buffer.append(' ');
            }
            buffer.append(hex);
            buffer.append(" ");
        }
        buffer.append("\n");
    }
    
}
