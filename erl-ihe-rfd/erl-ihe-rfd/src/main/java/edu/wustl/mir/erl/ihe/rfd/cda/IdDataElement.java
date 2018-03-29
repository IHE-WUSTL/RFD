/*******************************************************************************
 * Copyright (c) 2015 Washington University in St. Louis All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. The License is available at:
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. Contributors:
 * Initial author: Ralph Moulton / MIR WUSM IHE Development Project
 * moultonr@mir.wustl.edu
 ******************************************************************************/
package edu.wustl.mir.erl.ihe.rfd.cda;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Subclass of DataElement to handle id element.  The XPath expression
 * points to the Node. The target data is in the attributes. value is the root
 * attribute
 */
public class IdDataElement extends DataElement {
   private static final long serialVersionUID = 1L;
   
   /**
    * The extension, taken from that attribute
    */
   protected String extension = "";

   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    */
   public IdDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression) {
      super(xpath, parName, element, description, xpathExpression);
   }
  
   /**
    * Copy constructor
    * @param dataElement IdDataElement to copy
    */
   public IdDataElement(IdDataElement dataElement) {
      DataElement.move(dataElement, this);
   }

   /**
    * @return the {@link #extension} value.
    */
   public String getExtension() {
      return extension;
   }

   /**
    * @param extension the {@link #extension} to set
    */
   public void setExtension(String extension) {
      this.extension = extension;
   }
   
   @Override
   public IdDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public IdDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }
   
   @Override
   public void loadValue(Element element2) throws Exception {
      value = null;
      String ex = null;
      Node attr = null;
      StringBuilder msg =
         new StringBuilder("Load Id: ").append(parName);
      // -------------------------------------- look for coded node
      Node node = (Node) evaluate(element2, xpathExpression, XPathConstants.NODE);
      if (node == null) {
         msg.append(": node not found");
         log.debug(msg.toString());
         return;
      }

      // ---------------------------- pull attributes of node
      NamedNodeMap map = node.getAttributes();
      if (map == null) {
         msg.append(": node had no attributes");
         log.debug(msg.toString());
         return;
      }

      // ----------------------------------- get attribute values
      attr = map.getNamedItem("root");
      if (attr != null) {
         value = attr.getNodeValue();
         msg.append(" root: ").append(value);
      }
      attr = map.getNamedItem("extension");
      if (attr != null) {
         ex = attr.getNodeValue();
         msg.append(" extension: ").append(ex);
      }
      // ----------------------------- load what you found
      if (ex != null) extension = ex;

      log.debug(msg);

   } // EO load value 

   @Override
   public void loadWSLog(WSLog wsLog) {
      String prefix = "prepopData " + element;
      if (value != null) {
         StringBuilder val = new StringBuilder(value);
         if (StringUtils.isNotBlank(extension))
            val.append(" ").append(extension);
         LoadNameValuePairs.addPair(wsLog, prefix, val,
            LoadNameValuePairs.SOAP_REQUEST);
      }
   }
   
   public static class Copy implements DataElementCopy <IdDataElement> {

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.rfd.cda.DataElementCopy#copy(edu.wustl.mir.erl.ihe.rfd.cda.DataElement)
       */
      @Override
      public IdDataElement copy(IdDataElement dataElement) {
         return new IdDataElement(dataElement);
      }
      
   }

} // EO IdDataElement class
