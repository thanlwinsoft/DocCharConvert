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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Utility methods for handling relative paths to workspace files
 * @author keith
 *
 */
public class PathUtil
{
    /**
     * relative path between a and b if possible otherwise the full path to a
     * B is a file
     * @param a
     * @param b
     * @return path of a with respect to b, or a's full path
     */
    public static IPath relativePathOfAwrtFileB(IPath a, IPath b)
    {
        if (a.getDevice() == null)
        {
            if (b.getDevice() != null)
                return a;
        }
        else if (!a.getDevice().equals(b.getDevice()))
        {
            return a;
        }
        int commonSeg = a.matchingFirstSegments(b);
        IPath relativeSuffix = a.removeFirstSegments(commonSeg);
        String relativePrefix = "";
        while (commonSeg++ < b.segmentCount() - 1)
        {
            relativePrefix += "../";
        }
        IPath relative = new Path(relativePrefix).append(relativeSuffix);
        return relative;
    }
    /**
     * relative path between a and b if possible otherwise the full path to a
     * B is a directory
     * @param a
     * @param b
     * @return path of a with respect to b, or a's full path
     */
    public static IPath relativePathOfAwrtDirB(IPath a, IPath b)
    {
        if (!a.getDevice().equals(b.getDevice()))
        {
            return a;
        }
        int commonSeg = a.matchingFirstSegments(b);
        IPath relativeSuffix = a.removeFirstSegments(commonSeg);
        String relativePrefix = "";
        while (commonSeg++ < b.segmentCount())
        {
            relativePrefix += "../";
        }
        IPath relative = new Path(relativePrefix).append(relativeSuffix);
        return relative;
    }
}
