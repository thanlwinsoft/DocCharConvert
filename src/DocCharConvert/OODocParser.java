/*
 * OODocParser.java
 *
 * Created on July 12, 2004, 8:12 PM
 */

package DocCharConvert;

import com.sun.star.awt.Point;
import com.sun.star.awt.Size;
import com.sun.star.awt.FontWeight;

import com.sun.star.beans.Property;
import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XPropertyState;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.container.NoSuchElementException;

import com.sun.star.bridge.XUnoUrlResolver;

import com.sun.star.comp.servicemanager.ServiceManager;

import com.sun.star.connection.XConnector;
import com.sun.star.connection.XConnection;

import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.container.XNamed;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XIndexReplace;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XEnumerationAccess;
//
//import com.sun.star.drawing.XShape;
//import com.sun.star.drawing.XShapeGrouper;
//import com.sun.star.drawing.XShapes;
//import com.sun.star.drawing.XDrawPageSupplier;
//
//import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XComponentLoader;
//import com.sun.star.frame.XModel;
//import com.sun.star.frame.XController;

import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.style.NumberingType;
import com.sun.star.style.XStyle;
import com.sun.star.style.XStyleFamiliesSupplier;


import com.sun.star.table.XCell;

import com.sun.star.text.ControlCharacter;
import com.sun.star.text.ReferenceFieldSource;
import com.sun.star.text.ReferenceFieldPart;
import com.sun.star.text.TextColumn;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.text.XAutoTextContainer;
import com.sun.star.text.XAutoTextGroup;
import com.sun.star.text.XAutoTextEntry;
import com.sun.star.text.XDependentTextField;
import com.sun.star.text.XDocumentIndex;
import com.sun.star.text.XFootnote;
import com.sun.star.text.XFootnotesSupplier;
import com.sun.star.text.XParagraphCursor;
import com.sun.star.text.XReferenceMarksSupplier;
import com.sun.star.text.XRelativeTextContentInsert;
import com.sun.star.text.XSentenceCursor;
import com.sun.star.text.XSimpleText;
import com.sun.star.text.XText;
import com.sun.star.text.XTextColumns;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextField;
import com.sun.star.text.XTextFrame;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextSection;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.text.XTextFieldsSupplier;
import com.sun.star.text.XBookmarksSupplier;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XPageCursor;

import com.sun.star.text.XWordCursor;

import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.XInterface;
import com.sun.star.uno.XNamingService;

import com.sun.star.util.XRefreshable;

import com.sun.star.frame.XStorable;
import com.sun.star.view.XPrintable;

import DocCharConvert.Converter.CharConverter;

import java.util.HashSet;
/**
 *
 * @author  keith
 */
public class OODocParser
{
    private CharConverter converter = null;
    private final static String FONT_NAME = "CharFontName";
    private final static String PARA_STYLE = "ParaStyleName";
    private final static String CHAR_STYLE = "CharStyleName";
    private XTextDocument mxDoc = null;
    private XText mxDocText = null;
    private OOMainInterface ooMain = null;
    private java.io.File inFile = null;
    private StringBuffer warningBuffer = null;
    private java.util.Map converterMap = null;
    private XComponentLoader mxComponentLoader = null;
    private PropertyValue[] loadProps = null;
    private HashSet paraStyles = null;
    private HashSet charStyles = null;
    private boolean onlyStylesInUse = true;
    private int paraCount = 0;
    private static final int STAGE_INIT = 0;
    private static final int STAGE_PARAS = 1;
    private static final int STAGE_STYLES = 2;
    private int stage = STAGE_INIT;
    /** Creates a new instance of OODocParser */
    public OODocParser(OOMainInterface ooMain) 
        
    {
        this.ooMain = ooMain;
        this.inFile = inFile;
        warningBuffer = new StringBuffer();
        
    }
    
    public String getWarnings()
    {
        return warningBuffer.toString();
    }
    
    public void openDoc(java.io.File inFile) 
        throws com.sun.star.io.IOException, com.sun.star.uno.Exception, 
        com.sun.star.lang.IllegalArgumentException, 
        DocInterface.InterfaceException
    {
        stage = STAGE_INIT;
        warningBuffer.delete(0, warningBuffer.length());
        XComponent xWriterComponent = newDocComponent("swriter",inFile);
        if (xWriterComponent == null)
        {
            System.out.println("Failed to get writer component");
        }
        // TBD add a listener for document close events
        
        // query its XTextDocument interface to get the text
        mxDoc = (XTextDocument)UnoRuntime.queryInterface(
            XTextDocument.class, xWriterComponent);
        if (mxDoc != null)
        {
            // get a reference to the body text of the document
            mxDocText = mxDoc.getText();
            //showFilters();
        }
    }
    
    public void setOnlyStylesInUse(boolean onlyConvertStylesInUse)
    {
        this.onlyStylesInUse = onlyConvertStylesInUse;
        if (onlyStylesInUse)
        {
            paraStyles = new HashSet();
            charStyles = new HashSet();
        }
    }
    /** This method demonstrates how to iterate over paragraphs */
    protected void parse(java.util.Map converters) 
        throws CharConverter.FatalException, DocInterface.InterfaceException
    {
        if (mxDoc == null) return;
        this.converterMap = converters;
        try {

            // The service 'com.sun.star.text.Text' supports the XEnumerationAccess interface to
            // provide an enumeration

            // of the paragraphs contained by the text the service refers to.
            // Here, we access this interface
            XEnumerationAccess xParaAccess = (XEnumerationAccess) 
                UnoRuntime.queryInterface(XEnumerationAccess.class, mxDocText);

            // Call the XEnumerationAccess's only method to access the actual Enumeration

            XEnumeration xParaEnum = xParaAccess.createEnumeration();

            // While there are paragraphs, do things to them
            stage = STAGE_PARAS;
            while (xParaEnum.hasMoreElements()) {
                
                // Get a reference to the next paragraphs XServiceInfo interface. TextTables
                // are also part of this

                // enumeration access, so we ask the element if it is a TextTable, if it
                // doesn't support the
                // com.sun.star.text.TextTable service, then it is safe to assume that it
                // really is a paragraph
                XServiceInfo xInfo = (XServiceInfo) UnoRuntime.queryInterface(
                    XServiceInfo.class, xParaEnum.nextElement());

                if (!xInfo.supportsService("com.sun.star.text.TextTable")) {

                    // Access the paragraph's property set...the properties in this
                    // property set are listed
                    // in: com.sun.star.style.ParagraphProperties

                    XPropertySet xSet = (XPropertySet) UnoRuntime.queryInterface(
                        XPropertySet.class, xInfo);
                    
                    //showStyles(xInfo, xSet);
                    if (parseParagraph(xInfo, xSet))
                    {
                        // the paragraph itself may need its style converting
                        if (doConversion(xInfo, xSet))
                        {
                            setNewStyle(xInfo,xSet);
                            if (xInfo.supportsService("com.sun.star.style.ParagraphProperties") )
                            {

                                if (xSet.getPropertySetInfo().hasPropertyByName(PARA_STYLE))
                                {
                                    Object oName = 
                                        xSet.getPropertyValue(PARA_STYLE);
                                    String styleName = 
                                        (String)UnoRuntime.queryInterface(String.class, oName);
                                    //System.out.println("\nPara:"+styleName);
                                    paraStyles.add(new String(styleName));
                                }
                            }
                        }
                    }
                }
                paraCount++;
            }
            
            // convert the styles in the document if necessary
            stage = STAGE_STYLES;
            parseAllStyles();
            
        }
        catch (Exception e) 
        {
            e.printStackTrace (System.out);
            throw new DocInterface.InterfaceException(e.getLocalizedMessage());
        }

    }
    
    /*
     * Change the fonts for styles that are having their text converted
     */
    protected void parseAllStyles()
    {
        // Get the StyleFamiliesSupplier interface of the document
        XStyleFamiliesSupplier xSupplier = 
            (XStyleFamiliesSupplier)UnoRuntime.queryInterface(
            XStyleFamiliesSupplier.class, mxDoc);
        // Use the StyleFamiliesSupplier interface to get the XNameAccess 
        // interface of the actual style families
        XNameAccess xFamilies = (XNameAccess) UnoRuntime.queryInterface (
            XNameAccess.class, xSupplier.getStyleFamilies());
        // Access the 'ParagraphStyles' Family
        try
        {
            XNameContainer xFamily = (XNameContainer) UnoRuntime.queryInterface(
                XNameContainer.class, xFamilies.getByName("ParagraphStyles"));
            if (onlyStylesInUse)
            {
                updateUsedStyles(xFamily, paraStyles);
            }
            else
            {
                updateDocStyles(xFamily);
            }
            // also update character styles
            xFamily = (XNameContainer) UnoRuntime.queryInterface(
                XNameContainer.class, xFamilies.getByName("CharacterStyles"));
            if (onlyStylesInUse)
            {
                updateUsedStyles(xFamily, charStyles);
            }
            else
            {
                updateDocStyles(xFamily);
            }
        }
        catch (NoSuchElementException e)
        {
            System.out.println(e.getMessage());
            warningBuffer.append(e.getMessage());
        }
        catch (WrappedTargetException e)
        {
            System.out.println(e.getMessage());
            warningBuffer.append(e.getMessage());
        }
    }
    
    protected void updateUsedStyles(XNameContainer xFamily, HashSet names)
    {
        XNameAccess xI = (XNameAccess) UnoRuntime.queryInterface(
            XNameAccess.class, xFamily);
        java.util.Iterator i = names.iterator();
        while (i.hasNext())
        {
            XPropertySet xSet = null;
            try
            {
                String name = (String)i.next();
                xSet = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, xI.getByName(name));
                //System.out.println("Updating style:" + name);
                updateStyle(xSet);
            }
            catch (NoSuchElementException e)
            {
                System.out.println(e);
                break;
            }
            catch (WrappedTargetException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getLocalizedMessage()));
            }
        }
    }
    
    protected void updateDocStyles(XNameContainer xFamily)
    {
        XIndexAccess xI = (XIndexAccess) UnoRuntime.queryInterface(
            XIndexAccess.class, xFamily);
        
        for (int i = 0; i<xI.getCount(); i++)
        {
            /* Should we check whether the style is in use?
             XStyle xStyle = (XStyle)UnoRuntime.queryInterface(
                XStyle.class, xI.getByIndex(i));
             */
            XPropertySet xSet = null;
            try
            {
                xSet = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, xI.getByIndex(i));
                updateStyle(xSet);
            }
            catch (com.sun.star.lang.IndexOutOfBoundsException e)
            {
                System.out.println(e);
                break;
            }
            catch (WrappedTargetException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getLocalizedMessage()));
            }
        }
    }
    
    
    
    protected void updateStyle(XPropertySet xSet)
    {
        if (xSet.getPropertySetInfo().hasPropertyByName(FONT_NAME))
        {

            try 
            {
                Object oName = xSet.getPropertyValue(FONT_NAME);
                String charFontName = AnyConverter.toString(oName);
                TextStyle thisStyle = new FontStyle(charFontName);
                if (converterMap.containsKey(thisStyle)) 
                {
                    CharConverter converter = 
                        (CharConverter)converterMap.get(thisStyle);
                    TextStyle newStyle = converter.getNewStyle();
                    if (newStyle.getFontName() != null)
                    {
                        xSet.setPropertyValue(FONT_NAME, 
                            newStyle.getFontName());
                    }
                }
            }
            catch (UnknownPropertyException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getMessage()));
            }
            catch (com.sun.star.lang.IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getMessage()));
            }
            catch (com.sun.star.beans.PropertyVetoException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getLocalizedMessage()));
            }
            catch (com.sun.star.lang.WrappedTargetException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getLocalizedMessage()));
            }
        }
    }
    
    protected boolean parseParagraph(XServiceInfo xPInfo, XPropertySet xPSet)
        throws UnknownPropertyException, NoSuchElementException, 
        com.sun.star.lang.WrappedTargetException, CharConverter.FatalException
    {
        boolean converted = false;
            
        XEnumerationAccess xRunAccess = (XEnumerationAccess) 
            UnoRuntime.queryInterface(
            XEnumerationAccess.class, xPInfo);
        XEnumeration xRunEnum = xRunAccess.createEnumeration();

        while (xRunEnum.hasMoreElements()) 
        {
            XServiceInfo xRInfo = (XServiceInfo) UnoRuntime.queryInterface(
                XServiceInfo.class, xRunEnum.nextElement());
            XPropertySet xRSet = (XPropertySet) UnoRuntime.queryInterface(
                    XPropertySet.class, xRInfo);
            XTextRange xTextRange = (XTextRange) UnoRuntime.queryInterface(
                XTextRange.class, xRInfo);
            
            //System.out.println("Text:" + xTextRange.getString());
            //showStyles(xRInfo, xRSet);
            if (doConversion(xRInfo, xRSet))
            {
                converted = true;
                // if you append to a string you get the formatting of the text  
                // that precedes it. This saves effort, so we first insert the  
                // new text after the original and then go back and delete the
                // old text
                short origLength = (short)xTextRange.getString().length();
                // insertString seems to work at start of cursor range, 
                // so get end here
                XTextCursor cursor = 
                    mxDocText.createTextCursorByRange(xTextRange.getEnd());
                // if you append and then delete you maintain formatting!
                //System.out.println(cursor.getString().length());
                try
                {
                    String newText = converter.convert(new String(xTextRange.getString()));
                    mxDocText.insertString(cursor,newText,false);
                    cursor.goLeft((short)newText.length(), false);
                    cursor.goLeft(origLength, true);
                    //System.out.println("->" + cursor.getString().length());
                    mxDocText.insertString(cursor,"",true);
                    
                    // now set the style for the new text
                    setNewStyle(xRInfo, xRSet);
                }
                catch (CharConverter.RecoverableException e)
                {
                    warningBuffer.append(e.getLocalizedMessage());
                }
            }
        }

        //else 
//            if (xPInfo.supportsService("com.sun.star.text.TextRange"))
//        {
//            XTextRange xTextRange = (XTextRange) UnoRuntime.queryInterface(
//                XTextRange.class, xPInfo);
//            System.out.println(xTextRange.getString());
//        }
        return converted;
    }
    /**
     * Check whether the font matches the one that we want to convert
     */
    protected boolean doConversion(XServiceInfo xInfo, XPropertySet xSet)
        throws UnknownPropertyException,
        com.sun.star.lang.WrappedTargetException
    {
        boolean convert = false;
        if (xInfo.supportsService("com.sun.star.style.CharacterProperties") &&
            xSet.getPropertySetInfo().hasPropertyByName(FONT_NAME))
        {
            Object oName = 
                xSet.getPropertyValue(FONT_NAME);
            try 
            {
                String charFontName = AnyConverter.toString(oName);
                TextStyle thisStyle = new FontStyle(charFontName);
                //System.out.println("<" + thisStyle.getFontName() + ">");
                if (converterMap.containsKey(thisStyle)) 
                {
                    convert = true;
                    converter = (CharConverter)converterMap.get(thisStyle);
                }
                else converter = null;
            }
            catch (com.sun.star.lang.IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getMessage()));
            }
        }
        return convert;
    }
    
    protected void setNewStyle(XServiceInfo xInfo, XPropertySet xSet)
        throws UnknownPropertyException,
        com.sun.star.lang.WrappedTargetException
    {
        if (xSet.getPropertySetInfo().hasPropertyByName(FONT_NAME))
        {           
            try 
            {
                XPropertyState xState = (XPropertyState) UnoRuntime.queryInterface(
                        XPropertyState.class, xInfo);
                if (xState.getPropertyState(FONT_NAME) != 
                    PropertyState.DEFAULT_VALUE)
                {
                    // converter was set by doConversion
                    TextStyle newStyle = converter.getNewStyle();
                    if (newStyle.getFontName() != null)
                    {
                        xSet.setPropertyValue(FONT_NAME, 
                            newStyle.getFontName());
                    }
                }
                else
                {
                    if (xSet.getPropertySetInfo().hasPropertyByName("CharStyleNames"))
                    {
                        Object oName = xSet.getPropertyValue("CharStyleNames");       
                        // its a sequence of strings, not one string
                        if (AnyConverter.isArray(oName))
                        {
                            Object [] values = (Object[]) AnyConverter.toArray(oName);
                            for (int i = 0 ; i <values.length; i++)
                            {
                                String charStyle = values[i].toString();
                                charStyles.add(new String(charStyle));
                            }
                        }
                    }
                }
            }           
            catch (com.sun.star.beans.PropertyVetoException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getLocalizedMessage()));
            }
            catch (com.sun.star.lang.IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
                warningBuffer.append(new String(e.getLocalizedMessage()));
            }
        }
    }
    
    protected void showStyles(XServiceInfo xInfo, XPropertySet xSet)
        throws UnknownPropertyException,
        WrappedTargetException
    {
        

        if (xInfo.supportsService("com.sun.star.style.CharacterProperties") &&
            xSet.getPropertySetInfo().hasPropertyByName(FONT_NAME))
        {
            Object oName = 
                xSet.getPropertyValue(FONT_NAME);
            
            try {
                String charFontName = AnyConverter.toString(oName);
                System.out.println("\tFont:" + charFontName);
                if (xSet.getPropertySetInfo().hasPropertyByName(CHAR_STYLE))
                {
                    oName = xSet.getPropertyValue(CHAR_STYLE);                
                    String charStyle = AnyConverter.toString(oName);
                    System.out.println("\tCharStyle:" + charStyle);
                }
                
                if (xSet.getPropertySetInfo().hasPropertyByName("CharStyleNames"))
                {
                    oName = xSet.getPropertyValue("CharStyleNames");       
                    // its a sequence of strings, not one string
                    if (AnyConverter.isArray(oName))
                    {
                        Object [] values = (Object[]) AnyConverter.toArray(oName);
                        for (int i = 0 ; i <values.length; i++)
                        {
                            String charStyle = values[i].toString();
                            System.out.println("\tCharStyleNames:" + charStyle);
                        }
                    }
                }
                if (xSet.getPropertySetInfo().hasPropertyByName("CharLocale"))
                {
                    com.sun.star.lang.Locale locale = 
                        (com.sun.star.lang.Locale)xSet.getPropertyValue("CharLocale"); 
                    System.out.println("\tLocale:" + locale.Country + " " +
                        locale.Language + " " + locale.Variant);
                }
            }
            catch (com.sun.star.lang.IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
            }
        }
    }
    
    protected XComponent newDocComponent(String docType, java.io.File sourceFile) 
        throws com.sun.star.io.IOException, com.sun.star.uno.Exception,
        com.sun.star.lang.IllegalArgumentException, 
        DocInterface.InterfaceException
    {
        
        if (mxComponentLoader == null)
        {

            XMultiComponentFactory mxRemoteServiceManager = 
                ooMain.getRemoteServiceManager();
            Object desktop = mxRemoteServiceManager.createInstanceWithContext(
                "com.sun.star.frame.Desktop", ooMain.getRemoteContext());
            mxComponentLoader = (XComponentLoader)UnoRuntime.queryInterface(
                XComponentLoader.class, desktop);
            loadProps = new PropertyValue[2];
            loadProps[0] = new com.sun.star.beans.PropertyValue();
            loadProps[0].Name = new String("Hidden");
            loadProps[0].Value = new Boolean(true);
            loadProps[1] = new com.sun.star.beans.PropertyValue();        
            loadProps[1].Name = new String("ReadOnly");
            loadProps[1].Value = new Boolean(true);
        }
        StringBuffer sTemplateFileUrl = new StringBuffer("file:///");
        sTemplateFileUrl.append(sourceFile.getAbsolutePath().replace('\\', '/'));
        System.out.println("Openning " + sTemplateFileUrl.toString() + "\n");
        return mxComponentLoader.loadComponentFromURL(sTemplateFileUrl.toString(), 
            "_default", 0, loadProps);    
    }
    
    public boolean saveDocAs(String newUrl) 
        throws DocInterface.InterfaceException
    {
         // Export can be achieved by saving the document and using
        // a special filter which can write the desired format.
        // Normally this filter should be searched inside the filter
        // configuration (using service com.sun.star.document.FilterFactory)
        // but here we use well known filter names directly.

         String sFilter = null;

         // Detect document type by asking XServiceInfo

         com.sun.star.lang.XServiceInfo xInfo = (com.sun.star.lang.XServiceInfo)UnoRuntime.queryInterface (
          com.sun.star.lang.XServiceInfo .class, mxDoc);

        
             // Build necessary argument list for store properties.
             // Use flag "Overwrite" to prevent exceptions, if file already exists.

             com.sun.star.frame.XStorable xStore = 
                (com.sun.star.frame.XStorable)UnoRuntime.queryInterface 
                (com.sun.star.frame.XStorable.class, mxDoc);
             // for the moment just use default property detection
             try
             {
                 PropertyValue [] filterProps = getFilterProperties(newUrl);
                 xStore.storeAsURL (newUrl, filterProps);
             }
             catch (com.sun.star.io.IOException sIOE)
             {
                System.out.println(sIOE.getMessage());
                throw new DocInterface.InterfaceException
                    (new String(sIOE.getLocalizedMessage()));
             }
         
         return true;
    }
    /**
     * Taken straight from OfficeDev.htm in OO Developer documentation
     */
    public void closeDoc()
    {
        if (mxDoc == null) return;
        // Check supported functionality of the document (model or controller).
        com.sun.star.frame.XModel xModel =
            (com.sun.star.frame.XModel)UnoRuntime.queryInterface(
            com.sun.star.frame.XModel.class,mxDoc);

        if(xModel != null)
        {
            // It is a full featured office document.
            // Try to use close mechanism instead of a hard dispose().
            // But maybe such service is not available on this model.
            com.sun.star.util.XCloseable xCloseable =
                (com.sun.star.util.XCloseable)UnoRuntime.queryInterface(
                com.sun.star.util.XCloseable.class,xModel);

            if(xCloseable!=null)
            {
                try
                {
                    // use close(boolean DeliverOwnership)
                    // The boolean parameter DeliverOwnership tells objects 
                    // vetoing the close process that they may
                    // assume ownership if they object the closure by throwing 
                    // a CloseVetoException
                    // Here we give up ownership. To be on the safe side, 
                    // catch possible veto exception anyway.
                    xCloseable.close(true);
                }
                catch(com.sun.star.util.CloseVetoException exCloseVeto)
                {
                    System.out.println(exCloseVeto.getMessage());
                }
            }
            // If close is not supported by this model - try to dispose it.
            // But if the model disagree with a reset request for the modify state
            // we shouldn't do so. Otherwhise some strange things can happen.
            else
            {
                System.out.println("Closable not found, using dispose\n");
                
                com.sun.star.lang.XComponent xDisposeable =
                        (com.sun.star.lang.XComponent)UnoRuntime.queryInterface(
                        com.sun.star.lang.XComponent.class,xModel);      
                        xDisposeable.dispose();
                
            }
        }
        else
        {
            System.out.println("Null model\n");
        }
    }
    protected PropertyValue [] getFilterProperties(String outputUrl)
    {
        String filterName = null;
        com.sun.star.beans.PropertyValue[] lProperties = null;
        if (outputUrl.toLowerCase().endsWith(".doc"))
        {
            filterName = new String("MS Word 97");
        }        
        else if (outputUrl.toLowerCase().endsWith(".txt"))
        {
            filterName = new String("Text");
        }
        else if (outputUrl.toLowerCase().endsWith(".html"))
        {
            filterName = new String("HTML (StarWriter)");
        }
        else if (outputUrl.toLowerCase().endsWith(".htm"))
        {
            filterName = new String("HTML (StarWriter)");
        }
        else if (outputUrl.toLowerCase().endsWith(".xhtml"))
        {
            filterName = new String("XHTML File");
        }
        // defaults to native format
        if (filterName == null)
        {
            lProperties = new com.sun.star.beans.PropertyValue[0];
        }
        else
        {
            lProperties = new com.sun.star.beans.PropertyValue[2];
            lProperties[0]       = new com.sun.star.beans.PropertyValue();
            lProperties[0].Name  = "FilterName";
            lProperties[0].Value = filterName;
            lProperties[1]       = new com.sun.star.beans.PropertyValue();
            lProperties[1].Name  = "Overwrite";
            lProperties[1].Value = new Boolean(true);
        }
        return lProperties;
    }
    
    void showFilters()
    {
        try
        {
            XMultiComponentFactory mxRemoteServiceManager = 
                ooMain.getRemoteServiceManager();
            Object filterFactory = 
                mxRemoteServiceManager.createInstanceWithContext
                ("com.sun.star.document.FilterFactory", 
                 ooMain.getRemoteContext());
        
            XNameAccess xFilterFactory = (XNameAccess) 
                    UnoRuntime.queryInterface(XNameAccess.class, filterFactory);
            String[] filterNames = xFilterFactory.getElementNames();
            for (int i = 0; i < filterNames.length; i++)
            {
                PropertyValue[] filterProperties = (PropertyValue[])
                    UnoRuntime.queryInterface(PropertyValue[].class,
                        xFilterFactory.getByName(filterNames[i]));
                String typeName = "";
                //    AnyConverter.toString(filterProperties.getPropertyValue("Type"));
                System.out.println("Filter: " + filterNames[i] + "," +
                    typeName + "\n");
            }
            }
            catch (UnknownPropertyException e)
            {
                System.out.println(e.getMessage());
            }
            catch (NoSuchElementException e)
            {
                System.out.println(e.getMessage());
            }
            catch (com.sun.star.lang.IllegalArgumentException e)
            {
                System.out.println(e.getMessage());
            }
            catch (WrappedTargetException e)
            {
                System.out.println(e.getMessage());
            }
            catch (com.sun.star.uno.Exception e)
            {
                System.out.println(e.getMessage());
            }
            catch (DocCharConvert.DocInterface.InterfaceException e)
            {
                System.out.println(e.getMessage());
            }
    }
    public synchronized String getStatusDesc()
    {
      String desc = new String("");
      switch (stage)
      {
      case STAGE_INIT: 
        desc = new String("Initialising");
        break;
      case STAGE_PARAS: 
        desc = new String("Parsing paragraph " + paraCount);
        break;
      case STAGE_STYLES:
        desc = new String("Parsing styles");
        break;
      }
      return desc;
    }
}
