/*
Copyright (C) 2010 Keith Stribley http://www.thanlwinsoft.org/

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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.parser.XslConversionConfiguration;
import org.thanlwinsoft.doccharconvert.DocInterface.InterfaceException;
import org.thanlwinsoft.eclipse.widgets.FileNameWidget;
/**
 * @author keith
 * Page to configure the XSL parameters
 */
public class XslParametersPage extends WizardPage implements XslConversionConfiguration, ModifyListener
{
	/**
	 * @author keith
	 * Label Provider for parameter table
	 */
	public class ParameterLabelProvider extends LabelProvider implements
			IBaseLabelProvider, ITableLabelProvider
	{

		@Override
		public Image getColumnImage(Object element, int columnIndex)
		{
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex)
		{
			if (element instanceof Entry<?,?>)
			{
				Entry<?,?> entry = (Entry<?,?>)element;
				switch (columnIndex)
				{
				case 0:
					  return entry.getKey().toString();
				default:
						return entry.getValue().toString();
				}
				
			}
			return null;
		}


	}

	private File mXslFile = null;
	private Map<String, Object> mParamMap = new HashMap<String,Object>();
	private FileNameWidget mInputFile = null; 
	private TableViewer mTViewer = null;
/**
 * Constructor
 */
	public XslParametersPage()
	{
		super(XslParametersPage.class.getCanonicalName());
		setTitle(MessageUtil.getString("Wizard_XslPageTitle"));
	}

	@Override
	public void createControl(Composite parent)
	{
		final Group mainControl  = new Group(parent, SWT.SHADOW_ETCHED_IN);
    RowLayout mainLayout = new RowLayout();
    mainLayout.type = SWT.VERTICAL;
    mainLayout.spacing = 1;
    mainLayout.fill = true;
    mainControl.setLayout(mainLayout);
		
    mInputFile = new FileNameWidget(mainControl, 
                MessageUtil.getString("Wizard_XslFile"), SWT.OPEN);
    mInputFile.addModifyListener(this);
    //mInputFile.setLabelWidth(100);
    RowLayout paramLayout = new RowLayout(SWT.HORIZONTAL);
    Composite paramComposite = new Composite(mainControl, SWT.NONE);
    paramComposite.setLayout(paramLayout);
    Label nameLabel = new Label(paramComposite, SWT.NONE);
    final Text nameText = new Text(paramComposite, SWT.LEAD);
    Label valueLabel = new Label(paramComposite, SWT.NONE);
    final Text valueText = new Text(paramComposite, SWT.LEAD);
    nameLabel.setText(MessageUtil.getString("Name"));
    nameLabel.setToolTipText(MessageUtil.getString("ParameterName"));
    valueLabel.setText(MessageUtil.getString("Value"));
    valueLabel.setToolTipText(MessageUtil.getString("ParameterValue"));
    RowLayout buttonsLayout = new RowLayout(SWT.HORIZONTAL);
    Composite buttonComposite = new Composite(mainControl, SWT.NONE);
    buttonComposite.setLayout(buttonsLayout);
    final Button addButton = new Button(buttonComposite, SWT.PUSH);
    addButton.setText(MessageUtil.getString("Add"));

    Button deleteButton = new Button(buttonComposite, SWT.PUSH);
    deleteButton.setText(MessageUtil.getString("Delete"));
    final Table paramTable = new Table(mainControl, 
    		SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
    paramTable.setLayoutData(new RowData(300, 100));
    mTViewer = new TableViewer(paramTable);
    mTViewer.setLabelProvider(new ParameterLabelProvider());
    mTViewer.setContentProvider(new ArrayContentProvider());
    
    TableColumn nameCol = new TableColumn(paramTable, SWT.LEFT);
    nameCol.setText(MessageUtil.getString("Wizard_XslParamName"));
    nameCol.setWidth(150);
    nameCol.setResizable(true);
    nameCol.setToolTipText(MessageUtil.getString("ParameterName"));
    TableColumn valueCol = new TableColumn(paramTable, SWT.LEFT);
    valueCol.setText(MessageUtil.getString("Wizard_XslParamValue"));
    valueCol.setWidth(150);
    valueCol.setResizable(true);
    valueCol.setToolTipText(MessageUtil.getString("ParameterValue"));
    paramTable.setHeaderVisible(true);
    paramTable.setVisible(true);
    paramTable.setSize(300, 50);
    
    addButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) 
			{
				mParamMap.put(nameText.getText(), valueText.getText());
				mTViewer.setInput(mParamMap.entrySet().toArray());
				mTViewer.refresh();
				mainControl.layout();
			}
    	});
    deleteButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) 
			{
				IStructuredSelection s = (IStructuredSelection)mTViewer.getSelection();
				Object o = s.getFirstElement();
				if (o != null && o instanceof Entry<?,?>)
				{
					Entry<?,?>entry = (Entry<?,?>)o;
					mParamMap.remove(entry.getKey());
					mTViewer.setInput(mParamMap.entrySet().toArray());
					mTViewer.refresh();
				}
			}
    	});
    mainControl.layout();
    setControl(mainControl);
	}

	@Override
	public Map<String, Object> getParams()
	{
		return mParamMap;
	}

	@Override
	public InputStream xslStream() throws InterfaceException
	{
		if (mXslFile != null && mXslFile.canRead())
		{
			try
			{
				return new FileInputStream(mXslFile);
			}
			catch (FileNotFoundException e)
			{
				throw new InterfaceException(e);
			}
		}
		return null;
	}

	@Override
	public void modifyText(ModifyEvent e)
	{
		mXslFile = new File(mInputFile.getFileName());
		this.setPageComplete(validatePage());
	}
	
	protected boolean validatePage()
    {
    	return (mXslFile != null && mXslFile.canRead());
    }

}
