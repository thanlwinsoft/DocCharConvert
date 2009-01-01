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

package org.thanlwinsoft.doccharconvert.eclipse.editors;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.apache.xmlbeans.XmlException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.ComponentRef;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;
import org.thanlwinsoft.util.Triple;

/**
 * @author keith
 *
 */
public class ClassTableEditorPart extends EditorPart
{
    private Table table; 
    private TableViewer viewer;
    private final SyllableConverterEditor parentEditor;
    private org.thanlwinsoft.schemas.syllableParser.Class classTable;
    private Clipboard clipboard;
    private MenuManager menuManager;
    public final static int COL_OFFSET = 1; 
    public ClassTableEditorPart(SyllableConverterEditor parentEditor, 
        org.thanlwinsoft.schemas.syllableParser.Class clazz)
    {
        this.classTable = clazz;
        this.parentEditor = parentEditor;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
     */
    @Override
    public void doSave(IProgressMonitor monitor)
    {
        parentEditor.doSave(monitor);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#doSaveAs()
     */
    @Override
    public void doSaveAs()
    {
        parentEditor.doSaveAs();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#init(org.eclipse.ui.IEditorSite, org.eclipse.ui.IEditorInput)
     */
    @Override
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        this.setSite(site);
        clipboard = new Clipboard(site.getShell().getDisplay());
        final IEditorPart part = this;
        menuManager = new MenuManager(parentEditor.getPartName() + ":" + classTable.getId());
        Action insertAction = new Action(){
            public void run()
            {
                int mapIndex = getSelectedMapIndex();
                if (mapIndex < 0)
                    mapIndex = classTable.getComponentArray(0).sizeOfCArray();
                int insertRowCount = Math.max(1, table.getSelectionCount());                
                for (int i = 0; i < insertRowCount; i++)
                {
                    classTable.getComponentArray(0).insertNewC(mapIndex);
                    classTable.getComponentArray(1).insertNewC(mapIndex);
                }
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        insertAction.setId("Insert");
        insertAction.setText(MessageUtil.getString("Insert"));
        insertAction.setToolTipText(MessageUtil.getString("InsertToolTip"));
        Action deleteAction = new Action(){
            public void run()
            {
                int mapIndex = getSelectedMapIndex();
                Integer [] indices = getSelectedMapIndices();
                Arrays.sort(indices);
                if (mapIndex < 0)
                    return;
                // remove in reverse order so indices remain valid
                for (int i = indices.length - 1; i >= 0; i--)
                {
                    classTable.getComponentArray(0).removeC(mapIndex);
                    classTable.getComponentArray(1).removeC(mapIndex);
                }
                
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        deleteAction.setId("Delete");
        deleteAction.setText(MessageUtil.getString("Delete"));
        deleteAction.setToolTipText(MessageUtil.getString("DeleteToolTip"));
        
        Action moveUpAction = new Action()
        {
            @Override
            public void run()
            {
                int mapIndex = getSelectedMapIndex();
                if (mapIndex < 1)
                    return;
                C cA = (C)classTable.getComponentArray(0).getCArray(mapIndex).copy();
                C cB = (C)classTable.getComponentArray(1).getCArray(mapIndex).copy();
                classTable.getComponentArray(0).removeC(mapIndex);
                classTable.getComponentArray(1).removeC(mapIndex);
                C cNewA = classTable.getComponentArray(0).insertNewC(mapIndex-1);
                C cNewB = classTable.getComponentArray(1).insertNewC(mapIndex-1);
                if (cA.isSetHex())
                    cNewA.setHex(cA.getHex());
                else
                    cNewA.setStringValue(cA.getStringValue());
                if (cB.isSetHex())
                    cNewB.setHex(cB.getHex());
                else
                    cNewB.setStringValue(cB.getStringValue());
                assert (!cA.isSetR());
                assert (!cB.isSetR());
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        moveUpAction.setId("MoveUp");
        moveUpAction.setText(MessageUtil.getString("MoveUp"));
        moveUpAction.setToolTipText(MessageUtil.getString("MoveUpToolTip"));

        Action moveDownAction = new Action()
        {
            @Override
            public void run()
            {
                int mapIndex = getSelectedMapIndex();
                if (mapIndex < 0 || mapIndex + 1 >= table.getItemCount())
                    return;
                C cA = (C)classTable.getComponentArray(0).getCArray(mapIndex).copy();
                C cB = (C)classTable.getComponentArray(1).getCArray(mapIndex).copy();
                classTable.getComponentArray(0).removeC(mapIndex);
                classTable.getComponentArray(1).removeC(mapIndex);
                C cNewA = classTable.getComponentArray(0).insertNewC(mapIndex+1);
                C cNewB = classTable.getComponentArray(1).insertNewC(mapIndex+1);
                if (cA.isSetHex())
                    cNewA.setHex(cA.getHex());
                else
                    cNewA.setStringValue(cA.getStringValue());
                if (cB.isSetHex())
                    cNewB.setHex(cB.getHex());
                else
                    cNewB.setStringValue(cB.getStringValue());
                assert (!cA.isSetR());
                assert (!cB.isSetR());
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        moveDownAction.setId("MoveDown");
        moveDownAction.setText(MessageUtil.getString("MoveDown"));
        moveDownAction.setToolTipText(MessageUtil.getString("MoveDownToolTip"));

        
        Action deleteTableAction = new Action(){
            public void run()
            {
                if (MessageDialog.openConfirm(part.getSite().getShell(), 
                    part.getTitle(), 
                    MessageUtil.getString("ConfirmDeleteTable"))==false)
                {
                    return;
                }
                int index = parentEditor.getEditorIndex(part);
                parentEditor.removePage(index);
                SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
                for (int i = 0; i < sc.getClasses().sizeOfClass1Array(); i++)
                {
                    if (sc.getClasses().getClass1Array(i) == classTable)
                    {
                        sc.getClasses().removeClass1(i);
                        break;
                    }
                }
                parentEditor.setDirty(true);
            }
        };
        deleteTableAction.setId("DeleteTable");
        deleteTableAction.setText(MessageUtil.getString("DeleteTable"));
        deleteTableAction.setToolTipText(MessageUtil.getString("DeleteTableToolTip"));
        menuManager.add(deleteTableAction);
        menuManager.add(insertAction);
        menuManager.add(deleteAction);
        menuManager.add(moveUpAction);
        menuManager.add(moveDownAction);
        for (ComponentRef comp : classTable.getComponentArray())
        {
            final String r = comp.getR();
            Action copyColumn = new Action()
            {
                @Override
                public void run()
                {
                    doCopy(r, false);
                }
            };
            copyColumn.setText(MessageUtil.getString("CopyCol", comp.getR()));
            menuManager.add(copyColumn);
            Action pasteColumn = new Action()
            {
                @Override
                public void run()
                {
                    doPaste(r);
                }
            };
            pasteColumn.setText(MessageUtil.getString("PasteCol", comp.getR()));
            menuManager.add(pasteColumn);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#dispose()
     */
    @Override
    public void dispose()
    {
        if (clipboard != null && !clipboard.isDisposed())
        {
            clipboard.clearContents();
            clipboard.dispose();
        }
        super.dispose();
    }

    /**
     * @param r
     * @param b
     */
    protected void doCopy(String r, boolean b)
    {
        int side = 0;
        if (!classTable.getComponentArray(side).getR().equals(r))
            side = 1;
        assert(classTable.getComponentArray(side).getR().equals(r));
        ComponentRef copyContainer = ComponentRef.Factory.newInstance();
        copyContainer.setR(r);
        if (!(viewer.getSelection() instanceof IStructuredSelection))
            return;
        IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
        Iterator<?> i = ss.iterator();
        while (i.hasNext())
        {
            Object o = i.next();
            if (o instanceof Triple<?,?,?>)
            {
                Triple<?,?,?> pair = (Triple<?,?,?>)o;
                C cCopy = copyContainer.addNewC();
                C orig = null;
                // first is the row number
                if (side == 0)
                    orig = (C)pair.second;
                else orig = (C)pair.third;
                if (cCopy.isSetHex())
                    cCopy.setHex(orig.getHex());
                else
                    cCopy.setStringValue(orig.getStringValue());
            }
        }
        String xmlData = copyContainer.xmlText();
        clipboard.setContents(new Object[] { xmlData },
                new Transfer[] { TextTransfer.getInstance() });
    }

    /**
     * @param r
     */
    protected void doPaste(String r)
    {
        int side = 0;
        if (!classTable.getComponentArray(side).getR().equals(r))
            side = 1;
        assert(classTable.getComponentArray(side).getR().equals(r));
        
        String data = (String) clipboard
        .getContents(TextTransfer.getInstance());
        if (data == null)
            return;
        try
        {
            ComponentRef cData = ComponentRef.Factory.parse(data);
            if (!(viewer.getSelection() instanceof IStructuredSelection))
                return;
            IStructuredSelection ss = (IStructuredSelection) viewer.getSelection();
            Iterator<?> iSelection = ss.iterator();
            int i = 0;
            while (iSelection.hasNext() && i < cData.sizeOfCArray())
            {
                Object o = iSelection.next();
                if (o instanceof Triple<?,?,?>)
                {
                    Triple<?,?,?> pair = (Triple<?,?,?>)o;
                    C cCopy = cData.getCArray(i++);
                    C target = null;
                    // first is the row number
                    if (side == 0)
                        target = (C)pair.second;
                    else target = (C)pair.third;
                    if (cCopy.isSetHex())
                    {
                        target.setNil();
                        target.setHex(cCopy.getHex());
                    }
                    else
                    {
                        if (target.isSetHex())
                            target.unsetHex();
                        target.setStringValue(cCopy.getStringValue());
                    }
                }
            }
            parentEditor.setDirty(true);
            viewer.refresh();
        }
        catch (XmlException e)
        {
            // ignore for now
        }
    }

    /**
     * @return
     */
    protected int getSelectedMapIndex()
    {
        if (!(viewer.getSelection() instanceof IStructuredSelection)) return -1;
        IStructuredSelection s = (IStructuredSelection)viewer.getSelection();
        if (s.getFirstElement() instanceof Triple<?,?,?>)
        {
            Triple<?,?,?> selectedPair = (Triple<?,?,?>)s.getFirstElement();
            for (int i = 0; i < classTable.getComponentArray(0).sizeOfCArray(); i++)
            {
                if (classTable.getComponentArray(0).getCArray(i) == selectedPair.second)
                {
                    return i;
                }
            }
        }
        return -1;
    }
    
    /**
     * @return
     */
    protected Integer[] getSelectedMapIndices()
    {
        Vector <Integer> indices = new Vector<Integer>();
        if (!(viewer.getSelection() instanceof IStructuredSelection)) return indices.toArray(new Integer[0]);
        IStructuredSelection s = (IStructuredSelection)viewer.getSelection();

        for (Object o : s.toArray())
        {
            if (o instanceof Triple<?,?,?>)
            {
                Triple<?,?,?> selectedPair = (Triple<?,?,?>)o;
                for (int i = 0; i < classTable.getComponentArray(0).sizeOfCArray(); i++)
                {
                    if (classTable.getComponentArray(0).getCArray(i) == selectedPair.second)
                    {
                        indices.add(i);
                    }
                }
            }
        }
        return indices.toArray(new Integer[indices.size()]);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isDirty()
     */
    @Override
    public boolean isDirty()
    {
        return parentEditor.isDirty();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
     */
    @Override
    public boolean isSaveAsAllowed()
    {
        return parentEditor.isSaveAsAllowed();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    public void createPartControl(Composite parent)
    {
        parent.setLayout(new FillLayout());
        table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
        viewer = new TableViewer(table);
        viewer.setContentProvider(new ClassTableContentProvider());
        TableColumn tcNum = new TableColumn(table, SWT.TRAIL);
        tcNum.setWidth(35);
        TableViewerColumn tvcNum = new TableViewerColumn(viewer, tcNum);
        tvcNum.setLabelProvider(new CellLabelProvider(){
            
            @Override
            public void update(ViewerCell cell)
            {
                Object o = cell.getViewerRow().getElement();
                if (o instanceof Triple<?,?,?>)
                {
                    Object cValue = ((Triple<?,?,?>)o).get(0);
                    if (cValue instanceof Integer)
                    {
                        int row = ((Integer)cValue).intValue() + 1;
                        cell.setText(Integer.toString(row));
                    }
                }
            }
        });
        for (int colIndex = COL_OFFSET; colIndex <
             classTable.sizeOfComponentArray() + COL_OFFSET; colIndex++)
        {
            ComponentRef cr = classTable.getComponentArray(colIndex-COL_OFFSET);
            final String colRef = cr.getR();
            final int col = colIndex;
            TableColumn tc = new TableColumn(table, SWT.LEAD);
            tc.setText(cr.getR());
            tc.setWidth(100);
            TableViewerColumn tvc = new TableViewerColumn(viewer, tc);
            SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
            tc.setToolTipText(SyllableConverterUtils.getComponentName(sc, colRef));
            tvc.setEditingSupport(new CellEditingSupport(viewer, colRef, colIndex));
            tvc.setLabelProvider(new CellLabelProvider(){
                
                /* (non-Javadoc)
                 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipFont(java.lang.Object)
                 */
                @Override
                public Font getToolTipFont(Object object)
                {
                    if (object instanceof Triple<?,?,?>)
                    {
                        Object cValue = ((Triple<?,?,?>)object).get(col);
                        if (cValue instanceof C)
                        {
                            return (parentEditor.getFont(col-COL_OFFSET));
                        }
                    }
                    return super.getToolTipFont(object);
                }

                /* (non-Javadoc)
                 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
                 */
                @Override
                public String getToolTipText(Object element)
                {
                    if (element instanceof Triple<?,?,?>)
                    {
                        Object cValue = ((Triple<?,?,?>)element).get(col);
                        if (cValue instanceof C)
                        {
                            //cell.setText(SyllableConverterUtils.getCText((C)cValue));
                            String text = SyllableConverterUtils.getCTextWithCodes((C)cValue);
                            return text;
                        }
                    }
                    return super.getToolTipText(element);
                }

                @Override
                public void update(ViewerCell cell)
                {
                    Object o = cell.getViewerRow().getElement();
                    if (o instanceof Triple<?,?,?>)
                    {
                        Object cValue = ((Triple<?,?,?>)o).get(col);
                        if (cValue instanceof C)
                        {
                            //cell.setText(SyllableConverterUtils.getCText((C)cValue));
                            String text = SyllableConverterUtils.getCTextWithCodes((C)cValue);
                            cell.setText(text);
                            cell.setFont(parentEditor.getFont(col-COL_OFFSET));
                        }
                        else if (cValue instanceof Integer)
                        {
                            cell.setText(cValue.toString());
                        }
                    }
                }});
        }
        ColumnViewerToolTipSupport.enableFor(viewer);
        viewer.setInput(classTable);
        table.setHeaderVisible(true);
        viewer.refresh();
        menuManager.add(new Separator());
        menuManager.add(new GroupMarker (IWorkbenchActionConstants.MB_ADDITIONS));
        this.getEditorSite().registerContextMenu(menuManager, viewer);
        this.getEditorSite().setSelectionProvider(viewer);
        menuManager.setVisible(true);
        table.setMenu(menuManager.createContextMenu(table));
        table.setToolTipText(MessageUtil.getString("ClassTableToolTip"));
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        table.setFocus();
    }
    
    public class CellEditingSupport extends EditingSupport
    {
        final String colRef;
        final int colIndex;
        CellEditor editor;
        /**
         * @param viewer
         */
        public CellEditingSupport(ColumnViewer viewer, String colRef, int colIndex)
        {
            super(viewer);
            this.colRef = colRef;
            this.colIndex = colIndex;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
         */
        @Override
        protected boolean canEdit(Object element)
        {
            if (element instanceof Triple<?,?,?> && this.colIndex >= COL_OFFSET)
            {
                return true;
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
         */
        @Override
        protected CellEditor getCellEditor(Object element)
        {
            if (editor == null) editor = new TextCellEditor(table);
            Control control = editor.getControl();
            control.setFont(parentEditor.getFont(colIndex-COL_OFFSET));
            return editor;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
         */
        @Override
        protected Object getValue(Object element)
        {
            if (element instanceof Triple<?,?,?>)
            {
                C c = (C)((Triple<?,?,?>)element).get(colIndex);
                return SyllableConverterUtils.getCText(c);
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object, java.lang.Object)
         */
        @Override
        protected void setValue(Object element, Object value)
        {
            Object o = ((Triple<?,?,?>)element).get(colIndex);
            if (o instanceof C)
            {
                C c = (C)o;
                String oldValue = SyllableConverterUtils.getCText(c);
                if (value != null)
                {
                    if (!value.equals(oldValue))
                    {
                        String newValue = 
                            SyllableConverterUtils.parseUniInput(value.toString());
                        c.setStringValue(newValue);
                        viewer.refresh(element);
                        parentEditor.setDirty(true);
                    }
                }
                else
                {
                    c.setStringValue("");
                    viewer.refresh(element);
                    parentEditor.setDirty(true);
                }
            }
        }

    }
    
}
