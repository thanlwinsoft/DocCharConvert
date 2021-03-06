/*
Copyright 2009 Keith Stribley http://www.thanlwinsoft.org/

The java wrapper code is based on the java-firefox-extension which is
Copyright The SIMILE Project 2003-2005.

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
 */

Components.utils.import("resource://gre/modules/NetUtil.jsm");

const PERMS_FILE      = 0644;
// see nspr/prio.h line 612
const MODE_RDONLY   = 0x01;
const MODE_WRONLY   = 0x02;
const MODE_RDWR     = 0x04;
const MODE_CREATE   = 0x08;
const MODE_APPEND   = 0x10;
const MODE_TRUNCATE = 0x20;

var MyanmarConverterOptions = {
    traceEnabled : true,
    onLoad : function()
    {
        try
        {
          
            var prefs = Components.classes["@mozilla.org/preferences-service;1"]
                                      .getService(Components.interfaces.nsIPrefService)
                                      .getBranch("extensions.myanmarconverter.");

            this.enabled = (prefs)? prefs.getBoolPref("enabled") : true;
            this.traceEnabled = (prefs)? prefs.getBoolPref("trace") : false;
            var defaultEnable = document.getElementById("myanmarConverter.options.defaultEnableConversion");
            if (defaultEnable)
                defaultEnable.checked = this.enabled;
            this.zwspace = (prefs)? prefs.getBoolPref("useZwsp") : true;
            var zwspaceEnable = document.getElementById("myanmarConverter.options.zeroWidthSpaceEnableConversion");
            if (zwspaceEnable)
                zwspaceEnable.checked = this.zwspace;
                
            this.urlPatterns = this.loadUrlPatterns();
            var urlList = document.getElementById("myanmarConverter.options.urlList");
            for (var i = 0; i < this.urlPatterns.length; i++)
            {
                urlList.appendChild(this.createUrlListEntry(this.urlPatterns[i]));
            }
            if (typeof window != "undefined" && window.arguments && typeof window.arguments != "undefined")
            {
                this.extension = window.arguments[0];
                var urlHostname = document.getElementById("myanmarConverter.options.urlHostname");
                var urlPathname = document.getElementById("myanmarConverter.options.urlPathname");
                if (urlHostname && urlPathname && window.arguments[1] &&
                    window.arguments[1].protocol != "about")
                {
                    try
                    {
                        var patternIndex = this.findPatternForUrl(window.arguments[1]);
                        this.trace('location='+window.arguments[1] + " found at " + patternIndex);
                        if (patternIndex == -1)
                        {
                            urlList.selectedIndex = -1;
                            urlHostname.value = window.arguments[1].hostname;
                            urlPathname.value = window.arguments[1].pathname;
                            if( window.arguments[1].hostname.length == 0)
                            {
                               var hostExact=document
                                .getElementById("myanmarConverter.options.urlHostnameExact");
                                hostExact.parentNode.selectedItem = hostExact;
                            }
                            
                        }
                        else
                        {
                            var urlList = document.getElementById("myanmarConverter.options.urlList");
                            urlList.selectedIndex = patternIndex;
                        }
                    }
                    catch(e)
                    {
                        for (var i in e)
                            this.trace("MyanmarConverterOptions: Error getting location " + i + ":" + e[i]);
                    }
                }
            }
        }
        catch (e)
        {
            if (this.traceEnabled)
            {
                for (var i in e)
                {
                    this.trace("MyanmarConverterOptions::load " + i + ":" + e[i]);
                }
            }
        }
    },
    
    findPatternForUrl : function(url)
    {
        for(var i = 0 ;i < this.urlPatterns.length ; i++ )
        {
            var hostMatch = false;
            var pattern = this.urlPatterns[i];
            if (pattern.hostnameExact || (url.hostname.length == 0))
            {
                if (pattern.hostname == url.hostname)
                {
                    hostMatch = true;
                    //this.trace("MyanmarConverterOptions:: host exact match" + url.hostname + " at " + i);
                }
            }
            else
            {
                var pos = url.hostname.indexOf(pattern.hostname);
                if ((pos > -1) && (pos + pattern.hostname.length == url.hostname.length))
                {
                    hostMatch = true;
                    //this.trace("MyanmarConverterOptions:: host matched at pos " + pos);
                }
            }
            if (hostMatch &&
                ((pattern.pathnamePrefix && (url.pathname == pattern.pathname)) || 
                 (url.pathname.indexOf(pattern.pathname) == 0)))
            {
                return i;
            }
        }
        return -1;
    },
    
    trace : function(msg)
    {
        if (this.traceEnabled)
	    {
		    Components.classes["@mozilla.org/consoleservice;1"]
		                       .getService(Components.interfaces.nsIConsoleService)
		                       .logStringMessage(msg);
	    }
    },
    urlPatternsFile : function()
    {
        var dir = Components.classes["@mozilla.org/file/directory_service;1"]
            .getService(Components.interfaces.nsIProperties).get("ProfD",
                Components.interfaces.nsILocalFile);
        var file = Components.classes["@mozilla.org/file/local;1"]
            .createInstance(Components.interfaces.nsILocalFile);
        file.initWithPath(dir.path);
        file.appendRelativePath("myanmar-converter-urls.json");
        this.trace("ConverterOptions path: " + file.path);
        return file;
    },
    loadUrlPatterns : function()
    {
        var stream = Components.classes["@mozilla.org/network/file-input-stream;1"].
                 createInstance(Components.interfaces.nsIFileInputStream);
        var json = Components.classes["@mozilla.org/dom/json;1"].createInstance(Components.interfaces.nsIJSON);
        var file = this.urlPatternsFile();
        if (file.exists() && file.isReadable())
        {
            try
            {
              stream.init(file, MODE_RDONLY, PERMS_FILE, 0);
              return json.decodeFromStream(stream, stream.available());
            }
            catch(ex)
            {
              this.trace("MyanmarConverterOptions: Error reading file: " + ex);
            }
            finally
            {
              stream.close();
            }
        }
        return new Array();
    },
    createUrlListEntry : function(urlData)
    {
        var listitem = document.createElement("listitem");
        var listcell = document.createElement("listcell");
        if (urlData.enableConversion)
            listcell.setAttribute("label", "✔");
        else
            listcell.setAttribute("label", "✘");
        listitem.appendChild(listcell);
        listcell = document.createElement("listcell");
        var hostname = urlData.hostname;
        if (!urlData.hostnameExact)
            hostname = "*" + hostname;
        listcell.setAttribute("label", hostname);
        listitem.appendChild(listcell);
        listcell = document.createElement("listcell");
        var pathname = urlData.pathname;
        if (!urlData.pathnameExact)
            pathname = pathname + "*";
        listcell.setAttribute("label", pathname);
        listitem.appendChild(listcell);
        return listitem;        
    },
    addUrl : function()
    {
        var urlData = new Object();
        urlData.hostname = document.getElementById("myanmarConverter.options.urlHostname").value;
        urlData.hostnameExact = document.getElementById("myanmarConverter.options.urlHostnameExact").selected;
        if(urlData.hostname.length == 0)
        {
        urlData.hostnameExact=true;
        }
        urlData.pathname = document.getElementById("myanmarConverter.options.urlPathname").value;
        urlData.pathnameExact = document.getElementById("myanmarConverter.options.urlPathnameExact").selected;
        urlData.enableConversion = document.getElementById("myanmarConverter.options.enableConversionForPattern").checked;
        var urlList = document.getElementById("myanmarConverter.options.urlList");
        if (urlData.hostname.length || urlData.pathname.length) // ignore if no hostname
        {
            if (urlData.hostname.indexOf("/") == -1)
            {
                if (!this.urlPatterns) this.urlPatterns = new Array();
                for (var i = 0; i < this.urlPatterns.length; i++)
                {
                    // only allow one entry with the same hostname and pathname
                    if (this.urlPatterns[i].hostname == urlData.hostname &&
                       this.urlPatterns[i].pathname == urlData.pathname)
                    {
                        this.urlPatterns.splice(i, 1);
                        urlList.removeItemAt(i);
                        break;
                    }
                }
                this.urlPatterns.push(urlData);
                var listitem = this.createUrlListEntry(urlData);
                urlList.appendChild(listitem);
            }
            else
            {
                document.getElementById("myanmarConverter.options.urlHostname").value = urlData.hostname.replace("/","");
            }
        }
    },
    removeUrl : function()
    {
        var urlList = document.getElementById("myanmarConverter.options.urlList");
        var removeIndex = urlList.selectedIndex;
        if (removeIndex > -1)
        {
            this.urlPatterns.splice(removeIndex, 1);
            urlList.removeItemAt(removeIndex);
        }
    },
    urlOnSelect : function()
    {
        var urlList = document.getElementById("myanmarConverter.options.urlList");
        var selectedIndex = urlList.selectedIndex;
        if (selectedIndex > -1)
        {
            var urlData = MyanmarConverterOptions.urlPatterns[selectedIndex];
            document.getElementById("myanmarConverter.options.urlHostname").value = urlData.hostname;
            document.getElementById("myanmarConverter.options.urlPathname").value = urlData.pathname;
            var hostExact = document.getElementById("myanmarConverter.options.urlHostnameExact");
            var hostSuffix = document.getElementById("myanmarConverter.options.urlHostnameSuffix");
            var pathExact = document.getElementById("myanmarConverter.options.urlPathnameExact");
            var pathPrefix = document.getElementById("myanmarConverter.options.urlPathnamePrefix");
            
            if (urlData.hostnameExact)
                hostExact.parentNode.selectedItem = hostExact;
            else
               hostSuffix.parentNode.selectedItem = hostSuffix;
            if (urlData.pathnameExact)
                pathExact.parentNode.selectItem = pathExact;
            else
                pathPrefix.parentNode.selectItem = pathPrefix;
            document.getElementById("myanmarConverter.options.enableConversionForPattern").checked =
                urlData.enableConversion;
        }
    },
    doOk : function()
    {
        var prefs = Components.classes["@mozilla.org/preferences-service;1"]
                                      .getService(Components.interfaces.nsIPrefService)
                                      .getBranch("extensions.myanmarconverter.");
        var defaultEnable = document.getElementById("myanmarConverter.options.defaultEnableConversion");
        if (this.enabled != defaultEnable.checked)
        {
            this.enabled = defaultEnable.checked;
            prefs.setBoolPref("enabled", this.enabled);
        }
         var zwspaceEnable = document.getElementById("myanmarConverter.options.zeroWidthSpaceEnableConversion");
        if (this.zwspace != zwspaceEnable.checked)
        {
            this.zwspace = zwspaceEnable.checked;
            prefs.setBoolPref("useZwsp", this.zwspace);
        }
        if (this.extension)
        {
            this.extension.enabled = this.enabled;
            this.extension.urlPatterns = this.urlPatterns;
        }
        
        // save data
        var fos = Components.classes["@mozilla.org/network/file-output-stream;1"].
              createInstance(Components.interfaces.nsIFileOutputStream);
        var os = Components.classes["@mozilla.org/intl/converter-output-stream;1"]  
                    .createInstance(Components.interfaces.nsIConverterOutputStream);
        var file = this.urlPatternsFile();
        try
        {
            if (!file.exists())
            {
                file.create(0, PERMS_FILE);
            }
            fos.init(file, (MODE_RDWR | MODE_CREATE | MODE_TRUNCATE), PERMS_FILE, 0);
            os.init(fos, "UTF-8", 4096, 0x0000);
            var jsonString = JSON.stringify(this.urlPatterns);
            os.writeString(jsonString);
            prefs.setIntPref("urlPatternsUpdateTime", (new Date()).getTime());
        }
        catch(ex)
        {
          this.trace("MyanmarConverterOptions: Error writing file: " + ex);
        }
        finally
        {
            try
            {
                os.close();
                fos.close();
            }
            catch(ex)
            {
              this.trace("MyanmarConverterOptions: Error closing file: " + ex);
            }
        }

        return true;
    },
    doCancel : function()
    {
        return true;
    }
};

