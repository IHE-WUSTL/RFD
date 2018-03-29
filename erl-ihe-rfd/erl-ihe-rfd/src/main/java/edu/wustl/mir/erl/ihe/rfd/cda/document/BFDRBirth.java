/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda.document;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;

/**
 * BFDR Live Birth reporting document
 */
public class BFDRBirth extends CDADocument {
   private static final long serialVersionUID = 1L;

   // -------------------------------- xpath for document

   private static QName[] qnames =
      { new QName("urn:ihe:iti:rfd:2007", "dummy", "urn"),
         new QName("urn:hl7-org:v3", "dummy", ""),
         new QName("urn:hl7-org:v3", "dummy", "nl"),
         new QName("http://www.w3.org/2001/XMLSchema-instance", "dummy", "xsi"),
         new QName("urn:hl7-org:v3/voc", "dummy", "voc") };

   /**
    * @return a new {@link XPath} instance with the {@link NamespaceContext} for
    * this document.
    */
   private XPath getXPath() {
      XPath xpath = newXPath();
      xpath.setNamespaceContext(new CDANamespaceContext(qnames));
      return xpath;
   }
   
   /**
    * Create new BFDRBirth instance
    */
   public BFDRBirth() {
      
   }

   /* (non-Javadoc)
    * @see edu.wustl.mir.erl.ihe.rfd.cda.document.CDADocument#populate()
    */
   @Override
   public String populate() {
      throw new UnsupportedOperationException("No form for this CDA Document");
   }
   /* (non-Javadoc)
    * @see edu.wustl.mir.erl.ihe.rfd.cda.document.CDADocument#populate()
    */
   @Override
   public String populate(String form) {
      throw new UnsupportedOperationException("No form for this CDA Document");
   }

}
