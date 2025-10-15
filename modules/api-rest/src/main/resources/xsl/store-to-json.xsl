<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="json" indent="yes"/>
  <xsl:template match="/">
    <xsl:sequence select="array{ for $e in store/entry return map{
      'key': string($e/@key), 'value': string($e/value), 'ttlMs': if ($e/@ttlMs) then number($e/@ttlMs) else () } }"/>
  </xsl:template>
</xsl:stylesheet>
