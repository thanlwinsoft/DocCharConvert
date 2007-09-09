package org.thanlwinsoft.doccharconvert;

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
 
import java.util.Hashtable;
import java.io.File;
import org.thanlwinsoft.doccharconvert.converter.CharConverter;
import org.thanlwinsoft.doccharconvert.converter.DummyConverter;
import org.thanlwinsoft.doccharconvert.openoffice.OOMainInterface;

/**
 *
 * @author  keith
 */
public class DocCharConvertMain
{
    
    /** Creates a new instance of DocCharConvertMain */
    public DocCharConvertMain()
    {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        OOMainInterface firstConnection1 = new OOMainInterface();
        CharConverter converter = null;
        try 
        {
            converter = new DummyConverter();
                /*
                converter =
                    new TecKitConverter(new File("/home/keith/projects/DocCharConvert/academy.tec"),
                        "Padauk Academy",
                        "Padauk");
                 */
                /*
                converter = 
                    new ExternalConverter(
                        "testTecKit", 
                        "-char2utf8 /home/keith/projects/DocCharConvert/academy.tec <INFILE/> <OUTFILE/>", 
                        ExternalConverter.USE_ARGSINOUT, 
                        "Padauk Academy",
                        "Padauk");
                 */
            Hashtable<TextStyle,CharConverter> converters = new Hashtable<TextStyle,CharConverter>();
            converters.put(new FontStyle(""), converter);
            firstConnection1.useConnection(new File("test1.sxw"), 
                new File("test1Output.sxw"),converters);
        }

        catch (java.lang.Exception e) 
        {
            // TBD run command ourselves and retry
            // 
            e.printStackTrace();
        }

        finally {
            System.out.println("Finished!");
            System.exit(0);

        }
    }
    
}
