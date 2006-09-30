/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.views;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * @author keith
 *
 */
public class ConversionFileListProvider implements IStructuredContentProvider
{
    BatchConversion batchConversion = null;
    HashMap <File, String>statusMap = null;
    
    public static final int COLUMN_INPUT = 0; 
    public static final int COLUMN_OUTPUT = 1; 
    public static final int COLUMN_STATUS = 2; 
    public static final int COLUMN_COUNT = 3; 
    public class RowCell
    {
        File input;
        BatchConversion conversion;
        HashMap <File, String> statusMap;
        public RowCell(BatchConversion conv, HashMap <File, String> statusMap,
                       File inputFile)
        {
            this.conversion = conv;
            this.statusMap = statusMap;
            this.input = inputFile;
        }
        public File getInput() { return input; }
        public File getOutput() { return conversion.getOutputFile(input); }
        public String getStatus() { return statusMap.get(input); }
        public File getParent()
        {
            return input;
        }
        public String toString(int column)
        {
            String value = "";
            switch (column)
            {
            case COLUMN_INPUT:
                value = input.getName();
                break;
            case COLUMN_OUTPUT:
                File outputFile = conversion.getOutputFile(input);
                if (outputFile != null)
                {
                    value = conversion.getOutputFile(input).getName();
                }
                break;
            case COLUMN_STATUS:
                if (statusMap.containsKey(input))
                {
                    value = statusMap.get(input);
                }
                else
                {
                    value = MessageUtil.getString("Unconverted");
                }
                break;
            }
            return value;
        }
    }
    
    /** Construct ConversionFileListProvider with the given BatchConversion 
     * @param conversion
     */
    public ConversionFileListProvider()
    {
        this.batchConversion = null;
        statusMap = new HashMap <File, String>();        
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement)
    {
        System.out.println("getElements: " + inputElement.toString());
        if (inputElement instanceof BatchConversion)
        {
            batchConversion = (BatchConversion)inputElement;
            try
            {
                int fileCount = batchConversion.getInputFileList().length;
                Object [] elements = new Object[fileCount];
                for (int i = 0; i < fileCount; i++)
                {
                    Map.Entry me = (Map.Entry)batchConversion.getInputFileList()[i];
                    if (me.getKey() instanceof File)
                    {
                        elements[i] = new RowCell(batchConversion, statusMap, 
                                                  (File)(me.getKey()));
                    }
                }
                return elements;
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#dispose()
     */
    public void dispose()
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
    {
        
        TableViewer tv = (TableViewer)viewer;
        
        if (oldInput != null)
        {
            //if (oldInput.equals(newInput)) return;
            //tv.getTable().clearAll();
        }
        if (newInput != null && newInput instanceof BatchConversion)
        {
            batchConversion = (BatchConversion)newInput;
            if (batchConversion.getInputFileList() == null) return;
            int itemCount = batchConversion.getInputFileList().length;
            Object [] entries = batchConversion.getInputFileList();
            for (int i = 0; i<itemCount; i++)
            {
                
                TableItem ti = new TableItem(tv.getTable(), SWT.LEFT, i);
                if (entries[i] instanceof Map.Entry)
                {
                    Map.Entry entry = (Map.Entry)entries[i];
                    if (entry.getKey() instanceof File)
                    {
                        RowCell rc = new RowCell(batchConversion,
                                statusMap,
                                (File)entry.getKey());
                        ti.setData(rc);
                        ti.setText(0, rc.toString(0));
                        ti.setText(1, rc.toString(1));
                        ti.setText(2, rc.toString(2));
                        
                    }
                }
            }
        }
    }
    void setStatus(File input, String status)
    {
        statusMap.put(input, status);
    }
}
