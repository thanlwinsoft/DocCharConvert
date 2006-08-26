/*
 *  Copyright (C) 2005 Keith Stribley <doccharconvert@thanlwinsoft.org>
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 * -----------------------------------------------------------------------
 * $HeadURL: $
 * $LastChangedBy: keith $
 * $LastChangedDate: $
 * $LastChangedRevision: $
 * -----------------------------------------------------------------------
 */

package org.thanlwinsoft.doccharconvert;

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
    public final static int OO_ID = 1;// openoffice api
    public final static int TEX_ID = 2;
    public final static int OD_ID = 3;/*< opendocument format */
    public final static int NUM_MODES = 4;
    
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
        new ConversionMode(TEXT_ID,"Plain Text",false);
    public static final ConversionMode OO_MODE = 
        new ConversionMode(OO_ID,"OpenOffice",true);
    public static final ConversionMode TEX_MODE = 
        new ConversionMode(TEX_ID,"TeX",false);
    public static final ConversionMode OD_MODE = 
        new ConversionMode(OD_ID,"OpenDocument",true);
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
            case OD_ID:
                return OD_MODE;
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
                case OD_ID:
                    fileFilter = new FileFilter() {
                        public boolean accept(java.io.File f)
                        {
                            String lcn = f.getName().toLowerCase();
                            if (lcn.endsWith(".odt")) return true;
                            else if (lcn.endsWith(".odp")) return true;
                            else if (lcn.endsWith(".ods")) return true;
                            else if (lcn.endsWith(".odg")) return true;
                            
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
