package DocCharConvert;

/*
 * DocCharConvertMain.java
 *
 * Created on July 12, 2004, 8:03 PM
 */
import java.util.Hashtable;
import java.io.File;
import DocCharConvert.Converter.CharConverter;
import DocCharConvert.Converter.DummyConverter;
import DocCharConvert.Converter.ExternalConverter;
import DocCharConvert.Converter.TecKitConverter;
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
            Hashtable converters = new Hashtable();
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
