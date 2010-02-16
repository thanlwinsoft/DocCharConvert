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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.thanlwinsoft.doccharconvert.MessageUtil;

/**
 * @author keith
 * A Cell editor giving a browse to a file button
 */
public class FileCellEditor extends DialogCellEditor {

	final String [] extensions;
	IResource moduleFile = null;
	/**
	 * @param parent
	 * @param styles
	 * @param file
	 * @param extensions
	 */
	public FileCellEditor(Composite parent, int styles, IResource file, String [] extensions)
	{
		super(parent, styles);
		this.moduleFile = file;
		this.extensions = extensions;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
	 */
	protected Object openDialogBox(Control cellEditorWindow) 
	{
		FileDialog dialog = new FileDialog(cellEditorWindow.getShell(), SWT.OPEN);
		dialog.setText(MessageUtil.getString("FileDialogTitle"));
		dialog.setFilterExtensions(extensions);
        if (getDefaultLabel().getText().length() > 0)
        {
            IPath path = new Path(getDefaultLabel().getText());
            dialog.setFilterPath(path.removeLastSegments(1).toOSString());
            dialog.setFileName(path.lastSegment());
        }
        if (moduleFile != null)
		{
            IPath parent = null;
            if (moduleFile instanceof IContainer)
                parent = moduleFile.getRawLocation();
            else
                parent = moduleFile.getRawLocation().removeLastSegments(1);
            dialog.setFilterPath(parent.toOSString());
            dialog.setFileName(moduleFile.getName());
		}
        
		String filePath = dialog.open(); 
		if (filePath != null)
		{
			this.getDefaultLabel().setText(filePath);
			this.getDefaultLabel().setToolTipText(filePath);
		}
        else 
        {
            if (moduleFile != null)
                filePath = moduleFile.getRawLocation().toOSString();
            else
                filePath = "";
        }
		return filePath;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DialogCellEditor#doSetValue(java.lang.Object)
	 */
	protected void doSetValue(Object value) 
	{
		super.doSetValue(value);
		if (value != null)
		{
		    Label label = this.getDefaultLabel(); 
		    label.setText(value.toString());
		    label.setToolTipText(value.toString());
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.DialogCellEditor#updateContents(java.lang.Object)
	 */
	protected void updateContents(Object value) {
		super.updateContents(value);
		if (getDefaultLabel() != null && value != null)
		{
			String basePath = "";
			String filePath = value.toString();
			if (moduleFile != null)
			{
				basePath = moduleFile.getRawLocation().toOSString();
				if (basePath.length() > 0 && filePath.indexOf(basePath) == 0)
				{
					if (basePath.length() == filePath.length())
						filePath = "";
					else
						filePath = filePath.substring(basePath.length() + 1);
				}
			}
			getDefaultLabel().setText(filePath);
			getDefaultLabel().setToolTipText(filePath);
		}
	}
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.DialogCellEditor#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite cell)
    {
        return super.createContents(cell);
    }
	
	
}
