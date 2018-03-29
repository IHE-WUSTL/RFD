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

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Cause of Death and corresponding Onset to death interval. COD is stored in
 * value.
 */
public class CODDataElement extends DataElement {
   private static final long serialVersionUID = 1L;

   private static final String codXpathExpression = "./nl:observation/nl:value/nl:originalText/text()";

   private static final String onsetXpathExpression =
      "./nl:observation/nl:entryRelationship/nl:observation/nl:value/text()";

   /**
    * Onset to death interval for this cause of death.
    */
   protected String onsetToDeathInterval = null;

   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    */
   public CODDataElement(XPath xpath, String parName, String element, String description, String xpathExpression) {
      super(xpath, parName, element, description, xpathExpression);
   }

   private CODDataElement(CODDataElement dataElement) {
      super();
      DataElement.move(dataElement, this);
   }

   /**
    * @return causeOfDeath string
    */
   public String getCauseOfDeath() {
      return value;
   }

   /**
    * @return {@link #onsetToDeathInterval} value
    */
   public String getOnsetToDeathInterval() {
      return onsetToDeathInterval;
   }
   
   @Override
   public CODDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public CODDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }

   @Override
   public void loadValue(Element element2) throws Exception {
      value = null; // We're storing COD here
      onsetToDeathInterval = null;
      Node node = null;
      StringBuilder msg = new StringBuilder("Load Cause of Death: ").append(parName + " ");
      StringBuilder cod = new StringBuilder();
      if (StringUtils.isBlank(xpathExpression)) node = element2;
      else {
         node = (Node) evaluate(element2, xpathExpression, XPathConstants.NODE);
      }

      if (node == null || node.getNodeType() != Node.ELEMENT_NODE) {
         msg.append("base component/observation element not found");
         log.debug(msg.toString());
         return;
      }

      value = (String) evaluate(node, codXpathExpression, XPathConstants.STRING);
      if (value != null) {
         msg.append("COD: " + value + " ");
         cod.append(value + " ");
      }

      onsetToDeathInterval = (String) evaluate(node, onsetXpathExpression, XPathConstants.STRING);
      if (onsetToDeathInterval != null) {
         msg.append("onset: " + onsetToDeathInterval + " ");
         cod.append(onsetToDeathInterval);
      }

      log.debug(msg);
   } // EO load value 

   @Override
   public void loadWSLog(WSLog wsLog) {  
      StringBuilder cod = new StringBuilder();
      if (StringUtils.isNotBlank(value)) cod.append(value + " ");
      if (StringUtils.isNotBlank(onsetToDeathInterval)) cod.append(onsetToDeathInterval + " ");
      if (wsLog != null) {
         LoadNameValuePairs.addPair(wsLog, "prepopData " + element, cod, LoadNameValuePairs.SOAP_REQUEST);
      }

   } // EO loadValue method

   /**
    * Copy class for CODDataElement
    */
   public static class Copy implements DataElementCopy <CODDataElement> {

      @Override
      public CODDataElement copy(CODDataElement dataElement) {
         return new CODDataElement(dataElement);
      }
   }

}
