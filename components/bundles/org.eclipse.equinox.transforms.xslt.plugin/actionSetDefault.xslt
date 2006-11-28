 <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
     <xsl:template match="actionSet[@id='org.eclipse.ui.WorkingSetActionSet']">
     	 <actionSet>
		     <xsl:attribute name="visible">false</xsl:attribute>
		     <xsl:copy-of select="node()|@*[not(name()='visible')]" />
	     </actionSet>
     </xsl:template>
     <xsl:template match="node()|@*">
         <xsl:copy>
             <xsl:apply-templates select="node()|@*"/>
         </xsl:copy>
     </xsl:template>
 </xsl:stylesheet>