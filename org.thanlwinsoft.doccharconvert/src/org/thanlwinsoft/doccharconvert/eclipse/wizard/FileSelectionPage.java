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
package org.thanlwinsoft.doccharconvert.eclipse.wizard;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.thanlwinsoft.doccharconvert.BatchConversion;
import org.thanlwinsoft.doccharconvert.Config;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.eclipse.widgets.FileNameWidget;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.thanlwinsoft.doccharconvert.ConversionHelper;
import org.thanlwinsoft.doccharconvert.eclipse.EclipseMessageDisplay;

/**
 * @author keith
 *
 */
public class FileSelectionPage extends WizardPage implements ModifyListener
{
    private FileNameWidget inputFile;
    private FileNameWidget outputFile;
    private File oldInputFile = null;
    private BatchConversion conversion = null;
    private TableViewer tViewer = null;
    private Button checkBox = null;
    public FileSelectionPage(BatchConversion conversion)
    {
        super(ConversionWizard.FILE_SELECT_PAGE, 
              MessageUtil.getString("Wizard_FileSelectPageTitle"), null);
        this.conversion = conversion;
        this.conversion.setPairsMode(true);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        final Group mainControl  = new Group(parent, SWT.SHADOW_ETCHED_IN);
        RowLayout mainLayout = new RowLayout();
        mainLayout.type = SWT.VERTICAL;
        mainLayout.spacing = 1;
        mainLayout.fill = true;
        mainControl.setLayout(mainLayout);
        
        Label label = new Label(mainControl, SWT.LEFT | SWT.WRAP);
        label.setText(MessageUtil.getString("Wizard_SelectFiles"));
        inputFile = new FileNameWidget(mainControl, 
                MessageUtil.getString("Wizard_InputFile"), SWT.OPEN);
        inputFile.addModifyListener(this);
        inputFile.setLabelWidth(200);
        outputFile = new FileNameWidget(mainControl, 
                MessageUtil.getString("Wizard_OutputFile"), SWT.SAVE);
        outputFile.addModifyListener(this);
        outputFile.setLabelWidth(200);
        Config c = Config.getCurrent();
        try
        {
            if (c.getInputPath() != null)
                inputFile.setDefaultPath(c.getInputPath().getCanonicalPath());
            if (c.getOutputPath() != null)
                outputFile.setDefaultPath(c.getOutputPath().getCanonicalPath());
        }
        catch (IOException e)
        {
            MessageDialog.openError(this.getShell(), "Error", 
                                    e.getMessage());
        }
        RowLayout buttonsLayout = new RowLayout(SWT.HORIZONTAL);
        Composite buttonComposite = new Composite(mainControl, SWT.NONE);
        buttonComposite.setLayout(buttonsLayout);
        checkBox = new Button(buttonComposite, SWT.CHECK);
        checkBox.setText(MessageUtil.getString("Wizard_AddMultipleFiles"));
        final Table fileTable = new Table(mainControl, 
        		SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.MULTI);
        fileTable.setLayoutData(new RowData(300, 100));
        tViewer = new TableViewer(fileTable);
        tViewer.setLabelProvider(new FilePairLabelProvider());
        tViewer.setContentProvider(new ArrayContentProvider());
        tViewer.add(conversion.getInputFileList());
        tViewer.setInput(conversion.getInputFileList());
        TableColumn inputFileCol = new TableColumn(fileTable, SWT.LEFT);
        inputFileCol.setText(MessageUtil.getString("Wizard_InputCol"));
        inputFileCol.setWidth(150);
        inputFileCol.setResizable(true);
        TableColumn outputFileCol = new TableColumn(fileTable, SWT.LEFT);
        outputFileCol.setText(MessageUtil.getString("Wizard_OutputCol"));
        outputFileCol.setWidth(150);
        outputFileCol.setResizable(true);
        fileTable.setHeaderVisible(true);
        fileTable.setVisible(false);
        fileTable.setSize(300, 50);
        final Button add = new Button(buttonComposite, SWT.PUSH | SWT.CENTER);
        final Button remove = new Button(buttonComposite, SWT.PUSH | SWT.CENTER);
        final Button loadList = new Button(buttonComposite, SWT.PUSH | SWT.CENTER);
        final Button saveList = new Button(buttonComposite, SWT.PUSH | SWT.CENTER);
        add.setVisible(false);
        remove.setVisible(false);
        loadList.setVisible(false);
        saveList.setVisible(false);
        
        add.setText(MessageUtil.getString("Wizard_Add"));
        remove.setText(MessageUtil.getString("Wizard_Remove"));
        loadList.setText(MessageUtil.getString("Wizard_LoadList"));
        saveList.setText(MessageUtil.getString("Wizard_SaveList"));
        
        checkBox.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) 
			{
				fileTable.setVisible(checkBox.getSelection());
				add.setVisible(checkBox.getSelection());
				remove.setVisible(checkBox.getSelection());
                loadList.setVisible(checkBox.getSelection());
                saveList.setVisible(checkBox.getSelection());
                mainControl.layout();
			}
        });
        add.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) 
			{
				setPageComplete(validatePage());
			}
        });
        remove.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) 
			{
				TableItem [] rows = fileTable.getSelection();
				for (int i = 0; i < rows.length; i++)
				{
					if (rows[i].getData() instanceof Map.Entry)
					{
						Map.Entry<?,?> row = (Map.Entry<?,?>)rows[i].getData();
						conversion.removeFilePair(row);
					}
				}
				tViewer.setInput(conversion.getInputFileList());
				tViewer.refresh();
                mainControl.layout();
			}
        });
        final Shell shell = this.getShell();
        loadList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {}
            public void widgetSelected(SelectionEvent e) 
            {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                String inputPath = Config.getCurrent().getInputPath().getAbsolutePath();
                if (inputPath != null)
                {
                    //inputPath += File.separator + "*";
                    dialog.setFilterPath(inputPath);
                }
                String inputListFile = dialog.open();
                if (inputListFile != null)
                {
                    ConversionHelper.setMsgDisplay(new EclipseMessageDisplay(shell));
                    File listFile = new File(inputListFile);
                    ConversionHelper.loadFileList(conversion, listFile);
                    tViewer.setInput(conversion.getInputFileList());
                    tViewer.refresh();
                    //mainControl.layout();
                    setPageComplete(conversion.getInputFileList().length > 0);
                    Config.getCurrent().setInputPath(listFile.getParentFile().getAbsoluteFile());
                    Config.getCurrent().save();
                }
            }
        });
        saveList.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {}
            public void widgetSelected(SelectionEvent e) 
            {
                FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                //dialog.setFilterPath(Config.getCurrent().getOutputPath() + "/*");
                String listFileName = dialog.open();
                if (listFileName != null)
                {
                    ConversionHelper.setMsgDisplay(new EclipseMessageDisplay(shell));
                    try
                    {
                        File listFile = new File(listFileName);
                        ConversionHelper.saveFileList(conversion, listFile);
                        tViewer.setInput(conversion.getInputFileList());
                        tViewer.refresh();
                        Config.getCurrent().setOutputPath(listFile.getParentFile());
                        Config.getCurrent().save();
                    }
                    catch (IOException ioe)
                    {
                        MessageBox mb = new MessageBox(shell, 
                                                  SWT.ICON_WARNING | SWT.OK);
                        mb.setMessage(MessageUtil.getString("saveListError") + 
                                "\r\n" + ioe.getLocalizedMessage());
                    }
                }
            }
        });
        this.setControl(mainControl);
        setPageComplete(validatePage());
    }
    
    public void setPageComplete(boolean pc)
    {
        super.setPageComplete(pc);
    }
    
    protected boolean validatePage()
    {
        // remove previous entry if we are in single file mode
        if (oldInputFile != null && checkBox.getSelection() == false)
            conversion.removeInputFile(oldInputFile);
        oldInputFile = null;
        if (inputFile.getFileName() != null &&
            outputFile.getFileName() != null &&
            inputFile.getFileName().length() > 0 &&
            outputFile.getFileName().length() > 0 &&
            inputFile.getFileName().equals(outputFile.getFileName()) == false)
        {
            File iFile = new File(inputFile.getFileName());
            File oFile = new File(outputFile.getFileName());
            conversion.addFilePair(iFile, oFile);
            Config.getCurrent().setInputPath(iFile.getParentFile());
            Config.getCurrent().setOutputPath(oFile.getParentFile());
            Config.getCurrent().save();
            oldInputFile = iFile;
            tViewer.setInput(conversion.getInputFileList());
            tViewer.refresh();
            getControl().pack();
            //tViewer.getTable().pack();
            return true;
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
     */
    public void modifyText(ModifyEvent e)
    {
        // TODO Auto-generated method stub
    	if (checkBox.getSelection() == false)
    		setPageComplete(validatePage());
    }
    
    class FilePairLabelProvider extends LabelProvider implements ITableLabelProvider
    {

		public Image getColumnImage(Object element, int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof Map.Entry)
			{
				File f = null;
				Map.Entry<?,?> entry = (Map.Entry<?,?>)element;
				switch (columnIndex)
				{
				case 0:
					f = (File)entry.getKey();
					break;
				case 1:
					f = (File)entry.getValue();
					break;
				default:
					System.out.println("Unexpected column" + columnIndex);
					return "?";
				}
				return f.getAbsolutePath();
			}
			return null;
		}
    }
}
