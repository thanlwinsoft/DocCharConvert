package org.thanlwinsoft.eclipse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class PathUtil
{
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
