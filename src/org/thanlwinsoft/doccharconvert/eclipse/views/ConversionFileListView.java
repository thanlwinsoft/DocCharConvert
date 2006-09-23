/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.views;

import java.io.File;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * @author keith
 *
 */
public class ConversionFileListView extends ViewPart
{
    private Table fileListTable = null;
    private TableViewer tableView = null;
    private TableColumn inputFileCol = null;
    private TableColumn outputFileCol = null;
    private TableColumn conversionStatus = null;
    private ConversionFileListProvider provider = null;
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        Group mainControl  = new Group(parent, SWT.SHADOW_ETCHED_IN);
        mainControl.setLayout(new FillLayout());
        
        fileListTable = new Table(mainControl, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        fileListTable.setHeaderVisible(true);
        //fileListTable.setVisible(true);
        tableView = new TableViewer(fileListTable);
        inputFileCol = new TableColumn(fileListTable, SWT.LEFT);
        inputFileCol.setText(MessageUtil.getString("FileList_Input"));
        inputFileCol.setResizable(true);
        inputFileCol.setWidth(100);
        outputFileCol = new TableColumn(fileListTable, SWT.LEFT);
        outputFileCol.setText(MessageUtil.getString("FileList_Output"));
        outputFileCol.setWidth(100);
        outputFileCol.setResizable(true);
        conversionStatus = new TableColumn(fileListTable, SWT.LEFT);
        conversionStatus.setText(MessageUtil.getString("FileList_Status"));
        conversionStatus.pack();
        conversionStatus.setResizable(true);
        provider = new ConversionFileListProvider();
        
        tableView.setContentProvider(provider);
        tableView.setLabelProvider(new ConversionFileListColumns());
        CellEditor [] editors = new CellEditor[3];
        editors[0] = new DialogCellEditor(){

            @Override
            protected Object openDialogBox(Control cellEditorWindow)
            {
                // TODO Auto-generated method stub
                String fileName = cellEditorWindow.getData().toString();
                if (Program.launch(fileName) == false)
                {
                    MessageBox msgBox = new MessageBox(cellEditorWindow.getShell(),
                                                       SWT.ICON_WARNING | SWT.OK);
                    msgBox.setMessage(MessageUtil.getString("ProgramNotFoundFor", 
                                                            fileName));
                    msgBox.setText(MessageUtil.getString("ProgramNotFound"));
                    msgBox.open();
                }
                return cellEditorWindow.getData();
            }
        };
        editors[1] = editors[0];
        editors[2] = null;
        tableView.setCellEditors(editors);
    }

    public void setConversion(BatchConversion conv)
    {
        tableView.setInput(conv);
        tableView.add(provider.getElements(conv));
        fileListTable.setData(conv);
        System.out.println(tableView.getTable().getItemCount());
        tableView.getTable().showColumn(inputFileCol);
        tableView.refresh();
        tableView.getTable().setEnabled(true);
        tableView.getControl().redraw();
        
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub
        tableView.getControl().setFocus();
    }
    public class ConversionFileListColumns extends LabelProvider
        implements ITableLabelProvider 
        
    {

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        public Image getColumnImage(Object element, int columnIndex)
        {
            String imageKey = ISharedImages.IMG_OBJ_FILE;
            if (columnIndex == 2)
                imageKey = ISharedImages.IMG_OBJS_INFO_TSK;
            return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        public String getColumnText(Object element, int columnIndex)
        {
            if (element instanceof ConversionFileListProvider.RowCell)
            {
                ConversionFileListProvider.RowCell rc = 
                    (ConversionFileListProvider.RowCell)element;
                System.out.println(rc.toString(columnIndex));
                return rc.toString(columnIndex);
            }
            return element.toString();
        }
    }
    public void updateStatus(File input, String status)
    {
        ConversionFileListProvider cflp = 
            (ConversionFileListProvider)tableView.getContentProvider();
        cflp.setStatus(input, status);
        tableView.refresh(true);
    }
}
