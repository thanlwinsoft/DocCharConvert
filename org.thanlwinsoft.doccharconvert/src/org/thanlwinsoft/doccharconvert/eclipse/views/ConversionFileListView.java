/*
Copyright (C) 2006-2007 Keith Stribley http://www.thanlwinsoft.org/

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
------------------------------------------------------------------------------*/

package org.thanlwinsoft.doccharconvert.eclipse.views;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.eclipse.DocCharConvertEclipsePlugin;
import org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListProvider.RowCell;

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
    private BatchConversion batchConversion = null;
    // private String activeColumn = null;
    protected static final String INPUT_COL = "input";
    protected static final String OUTPUT_COL = "output";
    protected static final String STATUS_COL = "status";

    /**
     * view ID
     */
    public final static String ID = "org.thanlwinsoft.doccharconvert.eclipse.views.ConversionFileListView";

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        Group mainControl = new Group(parent, SWT.SHADOW_ETCHED_IN);
        mainControl.setLayout(new FillLayout());

        fileListTable = new Table(mainControl, SWT.MULTI | SWT.FULL_SELECTION
                | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
        fileListTable.setHeaderVisible(true);
        fileListTable.setVisible(true);
        tableView = new TableViewer(fileListTable);
        inputFileCol = new TableColumn(fileListTable, SWT.LEFT);
        inputFileCol.setText(MessageUtil.getString("FileList_Input"));
        inputFileCol.setResizable(true);
        inputFileCol.setWidth(150);
        outputFileCol = new TableColumn(fileListTable, SWT.LEFT);
        outputFileCol.setText(MessageUtil.getString("FileList_Output"));
        outputFileCol.setWidth(150);
        outputFileCol.setResizable(true);
        outputFileCol.addSelectionListener(new SelectionListener()
        {

            public void widgetDefaultSelected(SelectionEvent e)
            {
            }

            public void widgetSelected(SelectionEvent e)
            {
                // System.out.println(e);
            }
        });
        conversionStatus = new TableColumn(fileListTable, SWT.LEFT);
        conversionStatus.setText(MessageUtil.getString("FileList_Status"));
        conversionStatus.pack();
        // conversionStatus.setResizable(true);
        conversionStatus.setWidth(150);

        provider = new ConversionFileListProvider();

        tableView.setContentProvider(provider);
        tableView.setLabelProvider(new ConversionFileListColumns());

        tableView.setColumnProperties(new String[] { INPUT_COL, OUTPUT_COL,
                STATUS_COL });

        // tableView.setCellModifier(new ICellModifier() {
        //
        // public boolean canModify(Object element, String property) {
        // // TODO Auto-generated method stub
        // if (property.equals(INPUT_COL))
        // {
        // tableView.editElement(element,
        // ConversionFileListProvider.COLUMN_INPUT);
        // activeColumn = INPUT_COL;
        // return true;
        // }
        // if (property.equals(OUTPUT_COL))
        // {
        // tableView.editElement(element,
        // ConversionFileListProvider.COLUMN_OUTPUT);
        // activeColumn = OUTPUT_COL;
        // return true;
        // }
        // activeColumn = null;
        // return false;
        // }
        //
        // public Object getValue(Object element, String property) {
        // // TODO Auto-generated method stub
        // return element.toString();
        // }
        //
        // public void modify(Object element, String property, Object value) {
        // // TODO Auto-generated method stub
        //				
        // }
        //        	
        // });

        CellEditor[] editors = new CellEditor[3];
        // editors[0] = new DialogCellEditor(){
        //
        // @Override
        // protected Object openDialogBox(Control cellEditorWindow)
        // {
        // String fileName = cellEditorWindow.getData().toString();
        // if (Program.launch(fileName) == false)
        // {
        // MessageBox msgBox = new MessageBox(cellEditorWindow.getShell(),
        // SWT.ICON_WARNING | SWT.OK);
        // msgBox.setMessage(MessageUtil.getString("ProgramNotFoundFor",
        // fileName));
        // msgBox.setText(MessageUtil.getString("ProgramNotFound"));
        // msgBox.open();
        // }
        // return cellEditorWindow.getData();
        // }
        // };
        // editors[1] = editors[0];
        // editors[2] = null;
        tableView.setCellEditors(editors);

        tableView.getTable().addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent event)
            {
                // System.out.println(event.toString());
            }

            public void widgetDefaultSelected(SelectionEvent event)
            {
                doDefaultFileAction(getSelectedFiles(), -1);
            }

            
        });
        tableView.getTable().setEnabled(true);
        MenuManager menuManager = new MenuManager(getClass().getCanonicalName());
        Action openInput = new Action(){

            @Override
            public void run()
            {
                doDefaultFileAction(getSelectedFiles(), 0);
            }
        };
        openInput.setText(MessageUtil.getString("OpenInputFile"));
        Action openOutput = new Action(){

            @Override
            public void run()
            {
                doDefaultFileAction(getSelectedFiles(), 1);
            }
        };
        openOutput.setText(MessageUtil.getString("OpenOutputFile"));
        menuManager.add(openInput);
        menuManager.add(openOutput);
        
        Action openInputEditor = new Action(){

            @Override
            public void run()
            {
                doFileEditorAction(getSelectedFiles(), 0);
            }
        };
        openInputEditor.setText(MessageUtil.getString("OpenInputEditor"));
        Action openOutputEditor = new Action(){

            @Override
            public void run()
            {
                doFileEditorAction(getSelectedFiles(), 1);
            }
        };
        openOutputEditor.setText(MessageUtil.getString("OpenOutputEditor"));
        menuManager.add(openInputEditor);
        menuManager.add(openOutputEditor);
        tableView.getTable().setMenu(menuManager.createContextMenu(tableView.getTable()));
    }
    
    private RowCell[] getSelectedFiles()
    {
        final TableItem[] items = tableView.getTable().getSelection();
        final RowCell[] row = new RowCell[items.length];

        for (int i = 0; i < items.length; ++i)
        {
            Object o = items[i].getData();
            if (o instanceof RowCell)
                row[i] = (RowCell) o;
        }
        return row;
    }

    private void doFileEditorAction(RowCell[] selectedFiles, int inOut)
    {
        for (int i = 0; i < selectedFiles.length; i++)
        {
            IPath p = null;
            switch (inOut)
            {
            case 0:
                p = new Path(selectedFiles[i].getInput().getAbsolutePath());
                break;
            case 1:
                p = new Path(selectedFiles[i].getOutput().getAbsolutePath());
                break;
            default:
                return;
            }
            try
            {
                IFileStore fileStore =  EFS.getLocalFileSystem().getStore(p);
                IDE.openEditorOnFileStore(getSite().getPage(), fileStore);
            }
            catch (PartInitException e)
            {
                DocCharConvertEclipsePlugin.log(IStatus.WARNING, "Error openning editor", e);
            }
        }
    }
    
    protected void doDefaultFileAction(RowCell[] selectedFiles, int inOut)
    {
        for (int i = 0; i < selectedFiles.length; i++)
        {
            String[] fileName = new String[2];
            fileName[0] = selectedFiles[i].getInput().getAbsolutePath();
            fileName[1] = selectedFiles[i].getOutput().getAbsolutePath();
            for (int j = 0; j < 2; j++)
            {
                if (inOut > -1 && inOut != j) continue;
                if (Program.launch(fileName[j]) == false)
                {
                    MessageBox msgBox = new MessageBox(this.getSite()
                            .getShell(), SWT.ICON_WARNING | SWT.OK);
                    msgBox.setMessage(MessageUtil.getString(
                            "ProgramNotFoundFor", fileName[j]));
                    msgBox.setText(MessageUtil.getString("ProgramNotFound"));
                    msgBox.open();
                }
            }
        }
    }

    /**
     * 
     * @param conv
     */
    public void setConversion(BatchConversion conv)
    {
        tableView.setInput(conv);
        tableView.add(provider.getElements(conv));
        fileListTable.setData(conv);
        // System.out.println(tableView.getTable().getItemCount());
        tableView.getTable().showColumn(inputFileCol);
        tableView.refresh();
        tableView.getTable().setEnabled(true);
        tableView.getControl().redraw();
        this.batchConversion = conv;
        this.setPartName(MessageUtil.getString("ConvertedFiles", conv
                .toString()));
    }

    protected BatchConversion getConversion()
    {
        return this.batchConversion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        // TODO Auto-generated method stub
        tableView.getControl().setFocus();
    }

    protected class ConversionFileListColumns extends LabelProvider implements
            ITableLabelProvider

    {

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object,
         *      int)
         */
        public Image getColumnImage(Object element, int columnIndex)
        {
            String imageKey = ISharedImages.IMG_OBJ_FILE;
            if (columnIndex == 2)
                imageKey = ISharedImages.IMG_OBJS_INFO_TSK;
            return PlatformUI.getWorkbench().getSharedImages().getImage(
                    imageKey);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object,
         *      int)
         */
        public String getColumnText(Object element, int columnIndex)
        {
            if (element instanceof ConversionFileListProvider.RowCell)
            {
                ConversionFileListProvider.RowCell rc = (ConversionFileListProvider.RowCell) element;
                // System.out.println(rc.toString(columnIndex));
                return rc.toString(columnIndex);
            }
            return element.toString();
        }
    }

    /**
     * reset status of each row to unconverted
     */
    public void resetStatus()
    {
        ConversionFileListProvider cflp = (ConversionFileListProvider) tableView
                .getContentProvider();
        for (int i = 0; tableView.getElementAt(i) != null; i++)
        {
            if (tableView.getElementAt(i) instanceof RowCell)
            {
                RowCell row = (RowCell) tableView.getElementAt(i);
                cflp.setStatus(row.getInput(), MessageUtil
                        .getString("Unconverted"));
            }
        }
        tableView.refresh();
    }

    /**
     * 
     * @param input
     * @param status
     */
    public void updateStatus(File input, String status)
    {
        ConversionFileListProvider cflp = (ConversionFileListProvider) tableView
                .getContentProvider();
        RowCell modified = cflp.setStatus(input, status);
        // tableView.update(modified, true);
        tableView.update(modified, null);
        // tableView.refresh(true);
    }

    /**
     * 
     * @return BatchConversion
     */
    public BatchConversion getSelectedConversion()
    {
        final BatchConversion selectedConversion = new BatchConversion(
                getConversion());
        assert (tableView.getSelection() instanceof IStructuredSelection);
        IStructuredSelection s = (IStructuredSelection) tableView
                .getSelection();
        for (Object o : s.toArray())
        {
            if (o instanceof RowCell)
            {
                RowCell row = (RowCell) o;
                selectedConversion.addFilePair(row.getInput(), row.getOutput());
            }
        }
        return selectedConversion;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter)
    {
        if (adapter.equals(IPersistableElement.class))
        {
            return super.getAdapter(adapter);
        }
        return super.getAdapter(adapter);
    }

}
