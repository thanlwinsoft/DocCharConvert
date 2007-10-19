/*
Copyright (C) 2007 Keith Stribley http://www.thanlwinsoft.org/

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

package org.thanlwinsoft.eclipse;

import org.eclipse.core.resources.IFile;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FontDialog;
import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * @author keith
 *
 */
public class FontCellEditor extends DialogCellEditor {

	IFile moduleFile = null;
	public FontCellEditor(Composite parent, int styles)
	{
		super(parent, styles);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
	 */
	protected Object openDialogBox(Control cellEditorWindow) 
	{
        FontDialog dialog = new FontDialog(cellEditorWindow.getShell());
		dialog.setText(MessageUtil.getString("FontDialogTitle"));
        if (getValue() != null)
        {
            FontData [] oldFont = new FontData[] { 
                    new FontData(getValue().toString(), 12, SWT.NORMAL)
            }; 
            dialog.setFontList(oldFont);
        }
		FontData fontData = dialog.open(); 
		
		return fontData.getName();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DialogCellEditor#doSetValue(java.lang.Object)
	 */
	protected void doSetValue(Object value) 
	{
		super.doSetValue(value);
		this.getDefaultLabel().setText(value.toString());
		this.getDefaultLabel().setToolTipText(value.toString());
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DialogCellEditor#updateContents(java.lang.Object)
	 */
	protected void updateContents(Object value) {
		super.updateContents(value);
		if (getDefaultLabel() != null && value != null)
		{
			getDefaultLabel().setText(value.toString());
			getDefaultLabel().setToolTipText(value.toString());
		}
	}
	
	
}
