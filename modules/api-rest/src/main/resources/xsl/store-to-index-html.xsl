<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="html" indent="no"/>
  <xsl:template match="/">
    <html><body>
      <h1>cloudKV Index</h1>
      <xsl:for-each-group select="store/entry" group-by="upper-case(substring(@key,1,1))">
        <h2><xsl:value-of select="current-grouping-key()"/></h2>
        <ul>
          <xsl:for-each select="current-group()">
            <xsl:sort select="@key"/>
            <li><b><xsl:value-of select="@key"/></b>: <xsl:value-of select="value"/></li>
          </xsl:for-each>
        </ul>
      </xsl:for-each-group>
    </body></html>
  </xsl:template>
</xsl:stylesheet>
