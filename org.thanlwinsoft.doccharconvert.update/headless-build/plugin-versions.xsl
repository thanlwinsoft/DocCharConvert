<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>

<xsl:template match="/">
<xsl:text>equinoxLauncherPluginVersion=</xsl:text>
<xsl:call-template name="pluginVersion">
<xsl:with-param name="pluginId">org.eclipse.equinox.launcher</xsl:with-param>
</xsl:call-template>
<xsl:text>
eclipsePdeBuildVersion=</xsl:text>
<xsl:call-template name="pluginVersion">
<xsl:with-param name="pluginId">org.eclipse.pde.build</xsl:with-param>
</xsl:call-template>
<xsl:text>
</xsl:text>

</xsl:template>

<xsl:template name="pluginVersion">
<xsl:param name="pluginId"/>
<xsl:for-each select="/repository/artifacts/artifact[@id=$pluginId]/@version">
<xsl:sort select="." order="descending" />
<xsl:if test="position() = 1">
<xsl:value-of select="."/>
</xsl:if>
</xsl:for-each>
</xsl:template>

</xsl:stylesheet>

