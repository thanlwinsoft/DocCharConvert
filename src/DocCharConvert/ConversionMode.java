/*
 * ConversionMode.java
 *
 * Created on August 20, 2004, 4:08 PM
 */

package DocCharConvert;

import javax.swing.filechooser.FileFilter;
/**
 *
 * @author  keith
 */
public class ConversionMode
{
    private int id; 
    private String name;
    private boolean styleSupport = false;
    private FileFilter fileFilter = null;
    private static FileFilter allFilesFilter = null;
    public final static int TEXT_ID = 0;
    public final static int OO_ID = 1;
    public final static int TEX_ID = 2;
    public final static int NUM_MODES = 3;
    /** Creates a new instance of ConversionMode */
    protected ConversionMode(int id, String name, boolean styleSupport)
    {
        this.id = id;
        this.name = name;
        this.styleSupport = styleSupport;
    }
    public int getId() { return id; }
    public String toString() { return name; }
    public boolean hasStyleSupport() { return styleSupport; }
    public static final ConversionMode TEXT_MODE = 
        new ConversionMode(0,"Plain Text",false);
    public static final ConversionMode OO_MODE = 
        new ConversionMode(1,"OpenOffice",true);
    public static final ConversionMode TEX_MODE = 
        new ConversionMode(2,"TeX",false);
    public static ConversionMode getById(int id)
    {
        switch (id)
        {
            case TEXT_ID:
                return TEXT_MODE;
            case OO_ID:
                return OO_MODE;
            case TEX_ID:
                return TEX_MODE;
        }
        return null;
    }
    public FileFilter getFileFilter()
    {
        if (fileFilter == null)
        {
            switch (id)
            {
                case TEXT_ID:
                    fileFilter = new FileFilter() {
                        public boolean accept(java.io.File f)
                        {
                            String lcn = f.getName().toLowerCase();
                            if (lcn.endsWith(".txt")) return true;
                            // need to add directory to allow directory browsing
                            if (f.isDirectory()) return true;
                            return false;
                        }
                        public String getDescription() { return name; }
                    };
                    break;
                case OO_ID:
                    fileFilter = new FileFilter() {
                        public boolean accept(java.io.File f)
                        {
                            String lcn = f.getName().toLowerCase();
                            if (lcn.endsWith(".doc") ||
                                lcn.endsWith(".rtf") ||
                                lcn.endsWith(".sxw") ||
                                lcn.endsWith(".txt") ||
                                lcn.endsWith(".html") ||
                                lcn.endsWith(".htm")) return true;
                            // need to add directory to allow directory browsing
                            if (f.isDirectory()) return true;
                            return false;
                        }
                        public String getDescription() { return name; }
                    };
                    break;
                case TEX_ID:
                    fileFilter = new FileFilter() {
                        public boolean accept(java.io.File f)
                        {
                            String lcn = f.getName().toLowerCase();
                            if (lcn.endsWith(".tex")) return true;
                            // need to add directory to allow directory browsing
                            if (f.isDirectory()) return true;
                            return false;
                        }
                        public String getDescription() { return name; }
                    };
                    break;
            }
        }
        return fileFilter;
    }
    public static FileFilter getAllFilesFilter()
    {
        if (allFilesFilter == null)
        {
            allFilesFilter = new FileFilter() {
                public boolean accept(java.io.File f)
                {
                    if (f.isFile()) return true;
                    if (f.isDirectory()) return true;
                    return false;
                }
                public String getDescription() { return "All files"; }
            };
        }
        return allFilesFilter;
    }
}
