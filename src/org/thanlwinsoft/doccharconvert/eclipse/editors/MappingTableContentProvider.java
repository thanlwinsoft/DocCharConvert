/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.editors;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.thanlwinsoft.schemas.syllableParser.MappingTable;

/**
 * @author keith
 *
 */
public class MappingTableContentProvider implements IStructuredContentProvider
{

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement)
    {
        if (inputElement instanceof MappingTable)
        {
            return ((MappingTable)inputElement).getMaps().getMArray();
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
        

    }

}
