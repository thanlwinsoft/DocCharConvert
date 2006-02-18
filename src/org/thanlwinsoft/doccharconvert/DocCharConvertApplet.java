/*
 * DocCharConvertApplet.java
 *
 * Created on April 27, 2005, 9:42 AM
 *
 *
 * THIS APPLET IS NOT FINISHED YET!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 */

package org.thanlwinsoft.doccharconvert;

import java.io.IOException;
import java.util.Vector;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import java.net.URL;
import java.net.MalformedURLException;

import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.ChildConverter;
import org.thanlwinsoft.doccharconvert.converter.ReversibleConverter;

/**
 *
 * @author keith
 */
public class DocCharConvertApplet extends javax.swing.JApplet
{
    /**
	 * 
	 */
	private static final long serialVersionUID = -8885288908002896091L;
	private Vector <CharConverter> availableConverters = null;
    private DefaultComboBoxModel aModel = null;
    private JComboBox convCombo = null;
    /** Creates a new instance of DocCharConvertApplet */
    public DocCharConvertApplet()
    {
        
    }
    public void init()
    {
        availableConverters = new Vector<CharConverter>();
        aModel = new DefaultComboBoxModel();
        ConverterXmlParser xmlParser = new ConverterXmlParser();
        
        String convPath = null;
        int i = 0;
        convPath = getParameter("Converter" + Integer.toString(i));
        while (convPath != null)
        {
          try
          {
              URL convUrl = new URL(getCodeBase(), convPath);
              if (!xmlParser.parseStream(convUrl.openStream()))
              {
                    JOptionPane.showMessageDialog(this, 
                        new JScrollPane(new JTextArea(xmlParser.getErrorLog())),
                            "Error parsing Converter Configuration",
                    JOptionPane.WARNING_MESSAGE);
                    aModel.addElement(availableConverters.get(i));
               }
               aModel.addElement(availableConverters.get(i));
          }
          catch (MalformedURLException e) { System.out.println(e); }
          catch (IOException e) { System.out.println(e); }
          convPath = getParameter("Converter" + Integer.toString(++i));
        
        }
        
        
        convCombo = new JComboBox(aModel);
        getContentPane().add(convCombo);
    }
    public void start()
    {
        
    }
    public void stop()
    {
                
    }
    public void destroy()
    {
        
    }
    /** do the conversion
     * This is synchronous, so the display may freeze for very large input 
     * strings 
     * @param input string to convert
     * @param outputId id of html element to insert the result into or null
     * @param reverseid id of html element to insert result of a forward + 
     * reverse round trip conversion or null
     */
    public void convert(String input, String outputId, String reverseId)
    {
        Object co = convCombo.getSelectedItem();
        StringBuffer js = new StringBuffer();
        if (co instanceof ChildConverter)
        {
            ChildConverter cc = (ChildConverter)co;
            try
            {
                cc.initialize();
                String output = cc.convert(input);
                if (outputId != null)
                {
                    js.append("setElementText('");
                    js.append(outputId);
                    js.append("','");
                    js.append(output);
                    js.append("');");
                }
                CharConverter rcc = getReverseConverter(cc);
                if (rcc != null)
                {
                    rcc.initialize();
                    String reverse = rcc.convert(output);
                    if (reverseId != null)
                    {
                        js.append("setElementText('");
                        js.append(outputId);
                        js.append("','");
                        js.append(reverse);
                        js.append("');");
                    }
                    rcc.destroy();
                }
                cc.destroy();
                //JSObject window = getWindow(this);
            }
            catch (CharConverter.FatalException e)
            {              
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
            catch (CharConverter.RecoverableException e)
            {
                JOptionPane.showMessageDialog(this, e.getMessage());
            }
        }
    }
    protected CharConverter getReverseConverter(ChildConverter cc)
    {
        ChildConverter rcc = null;
        if (cc.getParent() instanceof ReversibleConverter)
        {
            // try to find the reverse converter
            ReversibleConverter ccParent = 
                (ReversibleConverter)cc.getParent();
            int i = 0;
            while (rcc == null && i<availableConverters.size())
            {
                if (availableConverters.elementAt(i) != null)
                {
                    Object rco = availableConverters.elementAt(i);
                    if (rco instanceof CharConverter)
                    {
                        ChildConverter tempCc = null;
                        tempCc = (ChildConverter)rco;
                        if (tempCc.getParent() instanceof ReversibleConverter)
                        {
                            ReversibleConverter tempParent = 
                                (ReversibleConverter)tempCc.getParent();
                            if (tempParent.getName().equals(ccParent.getName()) 
                               && (tempParent.isForwards() != 
                                   ccParent.isForwards()))
                            {
                                rcc = tempCc;
                            }
                        }
                    }
                }
            }
        }
        return rcc;
    }
}
