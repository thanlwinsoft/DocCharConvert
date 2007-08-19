/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.editors;

import java.math.BigInteger;
import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.CellEditor.LayoutData;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.thanlwinsoft.doccharconvert.MessageUtil;
import org.thanlwinsoft.doccharconvert.eclipse.editors.ClassTableEditorPart.CellEditingSupport;
import org.thanlwinsoft.schemas.syllableParser.Columns;
import org.thanlwinsoft.schemas.syllableParser.Component;
import org.thanlwinsoft.schemas.syllableParser.MappingTable;
import org.thanlwinsoft.schemas.syllableParser.Script;
import org.thanlwinsoft.schemas.syllableParser.SyllableConverter;

/**
 * @author keith
 *
 */
public class ScriptsEditorPart extends EditorPart
{
    private final SyllableConverterEditor parentEditor;
    private FormToolkit toolkit;
    private ScrolledForm form;
    public ScriptsEditorPart(SyllableConverterEditor parent)
    {
        this.parentEditor = parent;
        
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
        final SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
        toolkit = new FormToolkit(parent.getDisplay());
        form = toolkit.createScrolledForm(parent);
        form.setText(MessageUtil.getString("SyllableParserEditor"));
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        form.getBody().setLayout(layout);
//        ColumnLayout layout = new ColumnLayout();
//        form.setLayout(layout);
        Section leftScript = toolkit.createSection(form.getBody(), SWT.LEFT);
        Section rightScript = toolkit.createSection(form.getBody(), SWT.LEFT);
        FormText leftName = toolkit.createFormText(form.getBody(), true);
        leftName.setText(sc.getScriptArray(0).getName(), false, false);
        FormText rightName = toolkit.createFormText(form.getBody(), true);
        rightName.setText(sc.getScriptArray(1).getName(), false, false);
        
        addScriptTable(form.getBody(), sc.getScriptArray(0));
        addScriptTable(form.getBody(), sc.getScriptArray(1));
        
        leftScript.setText(MessageUtil.getString("LeftScript"));
        rightScript.setText(MessageUtil.getString("RightScript"));
        //Section general = toolkit.createSection(form.getBody(), SWT.LEFT);
        //general.setLayout(new ColumnLayout());
        final Button backtrack = toolkit.createButton(form.getBody(), MessageUtil.getString("Backtrack"), SWT.CHECK);
        backtrack.setSelection(sc.isSetBacktrack() && sc.getBacktrack());
        backtrack.addSelectionListener(new SelectionListener(){
            public void widgetDefaultSelected(SelectionEvent e) {}
            public void widgetSelected(SelectionEvent e)
            {
                sc.setBacktrack(backtrack.getSelection());
                parentEditor.setDirty(true);
            }
        });
        GridData backtrackGd = new GridData();
        backtrackGd.horizontalSpan = 2;
        backtrack.setLayoutData(backtrackGd);
        final Button newClass = toolkit.createButton(form.getBody(), MessageUtil.getString("NewClassButton"), SWT.PUSH);
        //Button newClass = new Button(general, SWT.PUSH);
        //newClass.setText(MessageUtil.getString("NewClassButton"));
        newClass.setToolTipText(MessageUtil.getString("NewClassToolTip"));
        newClass.addSelectionListener(new SelectionListener(){

            public void widgetDefaultSelected(SelectionEvent e) {}
            public void widgetSelected(SelectionEvent e)
            {
                NewTableDialog dialog = new NewTableDialog(getEditorSite().getShell(), 
                    getPartName(), null, MessageUtil.getString("CreateNewClassTable"), 
                    SWT.SINGLE);
                int open = dialog.open();
                if (open != Window.CANCEL && dialog.name != null && dialog.name.length() > 0 && 
                    dialog.leftComponents.length > 0 &&
                    dialog.rightComponents.length > 0)
                {
                    SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
                    if (sc.getClasses() == null)
                    {
                        sc.addNewClasses();
                    }
                    org.thanlwinsoft.schemas.syllableParser.Class clazz = 
                        sc.getClasses().addNewClass1();
                    clazz.setId(dialog.name);
                    clazz.addNewComponent().setR(dialog.leftComponents[0]);
                    clazz.addNewComponent().setR(dialog.rightComponents[0]);
                    parentEditor.addClassTable(clazz);
                    parentEditor.setDirty(true);
                }
            }});
        Button newMapping =  toolkit.createButton(form.getBody(), MessageUtil.getString("NewMappingButton"), SWT.PUSH);
        //newMapping.setText(MessageUtil.getString("NewMappingButton"));
        newMapping.setToolTipText(MessageUtil.getString("NewMappingToolTip"));
        newMapping.addSelectionListener(new SelectionListener(){

            public void widgetDefaultSelected(SelectionEvent e) {}
            public void widgetSelected(SelectionEvent e)
            {
                NewTableDialog dialog = new NewTableDialog(getEditorSite().getShell(), 
                    getPartName(), null, MessageUtil.getString("CreateNewMappingTable"), 
                    SWT.MULTI);
                int open = dialog.open();
                if (open != Window.CANCEL && dialog.name != null && dialog.name.length() > 0 && 
                    dialog.leftComponents.length > 0 &&
                    dialog.rightComponents.length > 0)
                {
                    SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
                    MappingTable mt = sc.addNewMappingTable();
                    mt.setId(dialog.name);
                    Columns columns = mt.addNewColumns();
                    for (String ref : dialog.leftComponents)
                        columns.addNewComponent().setR(ref);
                    for (String ref : dialog.rightComponents)
                        columns.addNewComponent().setR(ref);
                    parentEditor.addMappingTable(mt);
                    parentEditor.setDirty(true);
                }
            }});

    }

    /**
     * @param section
     * @param scriptArray
     */
    private void addScriptTable(Composite parent, final Script scriptArray)
    {
        final Table table = new Table(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        table.setHeaderVisible(true);
        final TableViewer viewer = new TableViewer(table);
        viewer.setContentProvider(new IStructuredContentProvider(){

            public void dispose() {}
            public void inputChanged(Viewer viewer, Object oldInput,
                Object newInput)
            {
                
            }

            public Object[] getElements(Object inputElement)
            {
                if (inputElement instanceof Script)
                {
                    return ((Script)inputElement).getCluster().getComponentArray();
                }
                return null;
            }});
        final ITableLabelProvider labelProvider = new ITableLabelProvider(){
            // ID, Name, Priority, Min
            public Image getColumnImage(Object element, int columnIndex)
            {
                return null;
            }

            public String getColumnText(Object element, int columnIndex)
            {
                if (element instanceof Component)
                {
                    Component c = (Component)element;
                    switch (columnIndex)
                    {
                    case 0:
                        return c.getId();
                    case 1:
                        StringBuilder builder = new StringBuilder();
                        for (int i = 0; i < c.getDomNode().getChildNodes().getLength(); i++)
                            builder.append(c.getDomNode().getChildNodes().item(i).getNodeValue());
                        return builder.toString();
                    case 2:
                        if (c.isSetPriority())
                            return c.getPriority().toString();
                        else return "";
                    case 3:
                        if (c.isSetMin())
                            return c.getMin().toString();
                        else
                            return "";
                    }
                }
                return "";
            }

            public void addListener(ILabelProviderListener listener){}
            public void dispose(){}
            public boolean isLabelProperty(Object element, String property)
            {
                return false;
            }

            public void removeListener(ILabelProviderListener listener)
            {
            }};
        viewer.setLabelProvider(labelProvider);
        viewer.setInput(scriptArray);
        String [] columnNames = new String[] {
          MessageUtil.getString("ID"),
          MessageUtil.getString("Name"),
          MessageUtil.getString("Priority"),
          MessageUtil.getString("Minimum"),
        };
        for (int i = 0; i < 4; i++)
        {
            TableColumn col = new TableColumn(table, SWT.LEAD);
            col.setWidth(100);
            col.setText(columnNames[i]);
            TableViewerColumn tvc = new TableViewerColumn(viewer, col);
            SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
            final int colNum = i;
            tvc.setEditingSupport(new EditingSupport(viewer){
                TextCellEditor tce = null;
                @Override
                protected boolean canEdit(Object element)
                {
                    return (element instanceof Component);
                }

                @Override
                protected CellEditor getCellEditor(Object element)
                {
                    if (tce == null)
                        tce = new TextCellEditor(table);
                    return tce;
                }

                @Override
                protected Object getValue(Object element)
                {
                    
                    return labelProvider.getColumnText(element, colNum);
                }

                @Override
                protected void setValue(Object element, Object value)
                {
                    if (value == null || value.toString().equals(getValue(element)))
                        return;
                    try
                    {
                        Component c = (Component)element;
                        switch (colNum)
                        {
                        case 0:
                            c.setId(value.toString());
                            break;
                        case 1:
                            org.w3c.dom.Text t = c.getDomNode().getOwnerDocument().createTextNode(value.toString());
                            c.getDomNode().appendChild(t);
                            break;
                        case 2:
                            int iValue = Integer.parseInt(value.toString());
                            c.setPriority(BigInteger.valueOf(iValue));
                            break;
                        case 3:
                            int minValue = Integer.parseInt(value.toString());
                            c.setMin(BigInteger.valueOf(minValue));
                            break;
                        }
                        parentEditor.setDirty(true);
                        viewer.refresh(element);
                    }
                    catch (NumberFormatException e)
                    {
                        
                    }
                }
                
            });
            tvc.setLabelProvider(new CellLabelProvider(){

                @Override
                public void update(ViewerCell cell)
                {
                    cell.setText(labelProvider.getColumnText(cell.getElement(), cell.getColumnIndex()));
                    
                }
            });
        }
        MenuManager menuManager = new MenuManager(parentEditor.getPartName() + ":" + this.getPartName());
        Action insertAction = new Action(){
            public void run()
            {
                int mapIndex = table.getSelectionIndex();// redo if sort
                if (mapIndex < 0)
                    mapIndex = scriptArray.getCluster().sizeOfComponentArray();
                int insertRowCount = Math.max(1, table.getSelectionCount());                
                for (int i = 0; i < insertRowCount; i++)
                {
                    Component c = scriptArray.getCluster().insertNewComponent(i);
                    c.setId("Side" + scriptArray.getSide() + scriptArray.getCluster().sizeOfComponentArray());
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
                int mapIndex = table.getSelectionIndex();
                if (mapIndex < 0)
                    return;
                scriptArray.getCluster().removeComponent(mapIndex);
                
                viewer.refresh();
                parentEditor.setDirty(true);
            }
        };
        deleteAction.setId("Delete");
        deleteAction.setText(MessageUtil.getString("Delete"));
        deleteAction.setToolTipText(MessageUtil.getString("DeleteToolTip"));
        menuManager.add(insertAction);
        menuManager.add(deleteAction);
        menuManager.add(new Separator());
        menuManager.setVisible(true);
        toolkit.adapt(table);
        table.setMenu(menuManager.getMenu());
        
        viewer.refresh();
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
     */
    @Override
    public void setFocus()
    {
        form.setFocus();
    }
    
    public class NewTableDialog extends MessageDialog
    {
        
        private int style = SWT.NONE;
            /**
         * @param parentShell
         * @param dialogTitle
         * @param dialogTitleImage
         * @param dialogMessage
         * @param dialogImageType
         * @param dialogButtonLabels
         * @param defaultIndex
         */
        public NewTableDialog(Shell parentShell, String dialogTitle,
            Image dialogTitleImage, String dialogMessage, int style)
        {
            super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
                MessageDialog.INFORMATION, new String[]{
                    MessageUtil.getString(MessageUtil.getString("OK")),
                    MessageUtil.getString("Cancel")}, 0);
            this.style = style;
        }
            String name;
            String [] leftComponents;
            String [] rightComponents;
            /* (non-Javadoc)
             * @see org.eclipse.jface.dialogs.MessageDialog#createCustomArea(org.eclipse.swt.widgets.Composite)
             */
            @Override
            protected Control createCustomArea(Composite parent)
            {
                Composite control = new Composite(parent, SWT.LEAD);
                GridLayout gl = new GridLayout();
                gl.numColumns = 4;
                control.setLayout(gl);
                Label l1 = new Label(control, SWT.LEAD);
                l1.setText(MessageUtil.getString("Name"));
                final Text nameText = new Text(control, SWT.LEAD);
                nameText.addModifyListener(new ModifyListener(){

                    public void modifyText(ModifyEvent e)
                    {
                        name = nameText.getText();
                    }});
                Label l2 = new Label(control, SWT.LEAD);
                Label l3 = new Label(control, SWT.LEAD);
                Label lLeft = new Label(control, SWT.LEAD);
                lLeft.setText(MessageUtil.getString("LeftComponents"));
                SyllableConverter sc = parentEditor.getDocument().getSyllableConverter();
                final List leftList = new List(control, style);
                ListViewer leftViewer = new ListViewer(leftList);
                leftViewer.setLabelProvider(new LabelProvider());
                ArrayList <String> leftComponentsArray = new ArrayList<String>();
                for (Component c : sc.getScriptArray(0).getCluster().getComponentArray())
                {
                    leftComponentsArray.add(c.getId());
                }
                leftViewer.add(leftComponentsArray.toArray());
                leftList.addSelectionListener(new SelectionListener(){
                    public void widgetDefaultSelected(SelectionEvent e){}
                    public void widgetSelected(SelectionEvent e)
                    {
                        leftComponents = leftList.getSelection();
                    }});
                Label lRight = new Label(control, SWT.LEAD);
                lRight.setText(MessageUtil.getString("RightComponents"));
                final List rightList = new List(control, style);
                ListViewer rightViewer = new ListViewer(rightList);
                rightViewer.setLabelProvider(new LabelProvider());
                ArrayList <String> rightComponentsArray = new ArrayList<String>();
                for (Component c : sc.getScriptArray(1).getCluster().getComponentArray())
                {
                    rightComponentsArray.add(c.getId());
                }
                rightViewer.add(rightComponentsArray.toArray());
                rightList.addSelectionListener(new SelectionListener(){
                    public void widgetDefaultSelected(SelectionEvent e){}
                    public void widgetSelected(SelectionEvent e)
                    {
                        rightComponents = rightList.getSelection();
                    }});
                return control;
            }
            
    }
}
