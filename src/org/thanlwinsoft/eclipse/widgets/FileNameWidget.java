/**
 * 
 */
package org.thanlwinsoft.eclipse.widgets;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.thanlwinsoft.doccharconvert.MessageUtil;
/**
 * A widget for file selection with label, text input and browse button.
 * @author keith
 *
 */
public class FileNameWidget extends Composite 
{
    private RowLayout layout = null;
    private Label label = null;
    private Button button = null;
    private Text fileName = null;
    private String defaultPath = null;
    private int fileDialogType = 0;
    public FileNameWidget(Composite parent, String labelText, int type)
    {
        super(parent, 0);
        layout = new RowLayout();
        layout.type = SWT.HORIZONTAL;
        layout.spacing = 5;
        layout.fill = true;
        setLayout(layout);
        if (labelText != null)
        {
            label = new Label(this, SWT.WRAP);
            label.setText(labelText);
            label.setLayoutData(new RowData(200,SWT.DEFAULT));
        }
        fileName = new Text(this, SWT.RIGHT | SWT.SINGLE);
        fileName.setLayoutData(new RowData(150, SWT.DEFAULT));
        button = new Button(this, SWT.PUSH | SWT.CENTER);
        button.setText(MessageUtil.getString("Browse"));
        button.setLayoutData(new RowData(SWT.DEFAULT,SWT.DEFAULT));
        final Composite browseParent = parent;
        button.addMouseListener(new MouseAdapter() {
            public void mouseUp(MouseEvent e)
            {
                browseFile(browseParent);
            }
        });
        this.fileDialogType = type;
    }
    public void setLabelWidth(int width)
    {
    	((RowData)(label.getLayoutData())).width = width;
    }
    public String getFileName()
    {
        return fileName.getText();
    }
    public void setFileName(String name)
    {
        fileName.setText(name);
        fileName.setSelection(name.length());
    }
    public void setDefaultPath(String path)
    {
        defaultPath = path;
        if (!defaultPath.endsWith(File.separator))
        {
          defaultPath = path + File.separator + "*";
        }
    }
    protected void browseFile(Composite parent)
    {
        FileDialog dialog = new FileDialog(parent.getShell(), fileDialogType);
        dialog.setText(label.getText());
        dialog.setFilterPath(defaultPath);
        if (fileName.getText() != null && fileName.getText().length() > 0)
        {
            dialog.setFileName(fileName.getText());
        }
        String filePath = dialog.open();
        if (filePath != null)
        {
            setFileName(filePath);
            dialog.setFilterPath(new File(filePath).getParent() + "/*");
        }
    }
    public void    addModifyListener(ModifyListener listener)
    {
        fileName.addModifyListener(listener);
    }
    public void    removeModifyListener(ModifyListener listener)
    {
        fileName.removeModifyListener(listener);
    }
}
