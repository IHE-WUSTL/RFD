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

import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Subclass of DataElement to handle  value elements. The XPath expression
 * points to the Node. The target data is in the attributes
 */
public class ValueDataElement extends DataElement {
   private static final long serialVersionUID = 1L;

   protected Map<String, String> attributes;
   
   protected String[] blanks = {"unit"};

   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    */
   public ValueDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression) {
      super(xpath, parName, element, description, xpathExpression);
   }
   
   /**
    * Copy constructor
    * @param other ValueDataElement
    */
   public ValueDataElement(ValueDataElement other) {
      super();
      move(other, this);
   }

   /**
    * Get attribute for name.
    * @param attributeName name of attribute
    * @return value of attribute, or null if no attribute with passed
    * attributeName exists.
    */
   public String getAttribute(String attributeName) {
      if (attributes == null) return null;
      return attributes.get(attributeName);
   }
   
   /**
    * sets the individual attribute values present in this ValueElement in the
    * passed CDAPlug instance. Note: This does NOT set the value item, which
    * is a representation of all the attributes. 
    * @param plug instance to set.
    */
   public void plug(CDAPlug plug) {
      if (attributes == null) return;
      for (Map.Entry <String, String> attribute: attributes.entrySet()) {
         plug.set(parName + attribute.getKey(), attribute.getValue());
      }
      // blanks is a list of parameters that should be blanked if they have no value.
      for (String blank : blanks) {
         if (attributes.containsKey(blank) == false) {
            plug.set(parName + blank, "");
         }
      }
   }
   
   @Override
   public ValueDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public ValueDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }
   

   @Override
   public void loadValue(Element element2) throws Exception {
      value = "";
      attributes = new LinkedHashMap<>();
      StringBuilder msg =
         new StringBuilder("Load Value: ").append(parName);
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
      
      for (int i = 0; i < map.getLength(); i++) {
         Node attr = map.item(i);
         String name = attr.getLocalName();
         String val = attr.getNodeValue();
         if (name != null) {
            attributes.put(name, val);
            String s = name + ": " + val + " ";
            value += s;
            msg.append(s);
         }
      }

      

      log.debug(msg);
   } // EO load value 

   @Override
   public void loadWSLog(WSLog wsLog) {   
      String prefix = "prepopData " + element;
      if (value != null) {
         LoadNameValuePairs.addPair(wsLog, prefix, value,
            LoadNameValuePairs.SOAP_REQUEST);
      }

   } // EO load value 

}
