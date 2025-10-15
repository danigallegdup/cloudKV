<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:param name="profileKeyPrefix" as="xs:string" select="''"/>
  <xsl:param name="includeKeys" as="xs:string*"/>
  <xsl:param name="excludeKeys" as="xs:string*"/>
  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/">
    <store>
      <xsl:for-each select="store/entry[ (not($profileKeyPrefix) or starts-with(@key,$profileKeyPrefix)) and (empty($includeKeys) or @key=$includeKeys) and not(@key=$excludeKeys) ]">
        <xsl:copy-of select="."/>
      </xsl:for-each>
    </store>
  </xsl:template>
</xsl:stylesheet>
