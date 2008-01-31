<?xml version="1.0"?>
<xsl:stylesheet 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xslout="http://www.w3.org/1999/XSL/Transform"  
    version="1.0"   
    xmlns:java="http://xml.apache.org/xalan/java"
    exclude-result-prefixes="java">
    
    <xsl:template match="/">
        <xsl:apply-templates/>
    </xsl:template>

    <xsl:template match="xsl:variable">
        <xsl:element name="xsl:variable">
            <xsl:attribute name="name"><xsl:value-of select="@name"/></xsl:attribute>
            <xsl:value-of select="java:org.thanlwinsoft.doccharconvert.parser.XslConversionParser.convert(string(.))"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
