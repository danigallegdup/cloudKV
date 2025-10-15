<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" encoding="UTF-8"/>
  <xsl:template match="/">
    <xsl:text>key,value,ttlMs,createdAtMs&#10;</xsl:text>
    <xsl:for-each select="store/entry">
      <xsl:value-of select="string-join((@key, replace(normalize-space(value), '"', '""'), string(@ttlMs), string(@createdAtMs)), ',')"/>
      <xsl:text>&#10;</xsl:text>
    </xsl:for-each>
  </xsl:template>
</xsl:stylesheet>
