/**
 * 
 */
package org.thanlwinsoft.doccharconvert.eclipse.editors;

import java.util.ArrayList;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.thanlwinsoft.schemas.syllableParser.C;
import org.thanlwinsoft.schemas.syllableParser.ComponentRef;
import org.thanlwinsoft.util.Pair;
/**
 * @author keith
 *
 */
public class ClassTableContentProvider implements IContentProvider,
    IStructuredContentProvider
{

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
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    public Object[] getElements(Object inputElement)
    {
        if (inputElement instanceof org.thanlwinsoft.schemas.syllableParser.Class)
        {
            org.thanlwinsoft.schemas.syllableParser.Class clazz = 
                (org.thanlwinsoft.schemas.syllableParser.Class)inputElement;
            ComponentRef a = clazz.getComponentArray(0);
            ComponentRef b = clazz.getComponentArray(1);
            ArrayList<Pair<C,C> > elements = new ArrayList<Pair<C,C>> ();
            for (int i = 0; i < a.sizeOfCArray() && i < b.sizeOfCArray(); i++)
            {
                elements.add(new Pair<C,C>(a.getCArray(i), b.getCArray(i)));
            }
            return elements.toArray(new Pair<?,?>[elements.size()]);
        }
        return null;
    }

}
