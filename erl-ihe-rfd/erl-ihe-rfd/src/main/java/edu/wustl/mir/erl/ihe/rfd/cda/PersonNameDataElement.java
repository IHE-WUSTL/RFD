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
import edu.wustl.mir.erl.ihe.util.XmlUtil;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class PersonNameDataElement extends DataElement {
   private static final long serialVersionUID = 1L;

   /**
    * A set of names given to a person at birth, but not including the family
    * name.
    */
   protected String given = null;
   /**
    * The family name
    */
   protected String family = null;
   /**
    * a set of honorific terms that typically appear before a person's name, for
    * example Mr., Mrs., Dr., etc.
    */
   protected String prefix = null;
   /**
    * Contains a list of honorific terms that typically appear after a person's
    * name, for example Jr., Sr., MD, RN, etc.
    */
   protected String suffix = null;

   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    */
   public PersonNameDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression) {
      super(xpath, parName, element, description, xpathExpression);
   }
   
   /**
    * Copy Constructor
    * @param other PersonNameDataElement
    */
   public PersonNameDataElement(PersonNameDataElement other) {
      super();
      move(other, this);
   }

   /**
    * @return the {@link #given} value.
    */
   public String getGiven() {
      return given;
   }

   /**
    * @param given the {@link #given} to set
    */
   public void setGiven(String given) {
      this.given = given;
   }

   /**
    * @return the {@link #prefix} value.
    */
   public String getPrefix() {
      return prefix;
   }

   /**
    * @param prefix the {@link #prefix} to set
    */
   public void setPrefix(String prefix) {
      this.prefix = prefix;
   }

   /**
    * @return the {@link #suffix} value.
    */
   public String getSuffix() {
      return suffix;
   }

   /**
    * @param suffix the {@link #suffix} to set
    */
   public void setSuffix(String suffix) {
      this.suffix = suffix;
   }

   /**
    * @return family, The portion of a person's name that reflects the genealogy
    * of the person.
    */
   public String getFamily() {
      return family;
   }

   /**
    * Set family, The portion of a person's name that reflects the genealogy of
    * the person.
    * 
    * @param family String
    */
   public void setFamily(String family) {
      this.family = family;
   }
   
   @Override
   public PersonNameDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public PersonNameDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }

   @Override
   public void loadValue(Element element2) throws Exception {
      value = null;
      given = null;
      family = null;
      prefix = null;
      suffix = null;
      StringBuilder msg =
         new StringBuilder("Load Person Name: ").append(parName + nl);
      Node node = (Node) evaluate(element2, xpathExpression, XPathConstants.NODE);

      // Must be name Element
      if (node == null || node.getNodeType() != Node.ELEMENT_NODE
         || node.getLocalName().equalsIgnoreCase("name") == false) {
         msg.append("name element not found");
         log.debug(msg.toString());
         return;
      }

      // -------------------------------------------------- prefix
      StringBuilder name = new StringBuilder();
      Element[] elements =
         XmlUtil.getFirstLevelChildElementsByName(node, "prefix");
      if (elements.length == 1) {
         prefix = XmlUtil.getFirstLevelTextContent(elements[0]);
         name.append(prefix + " ");
      }

      // -------------------------------------------------- given
      elements = XmlUtil.getFirstLevelChildElementsByName(node, "given");
      StringBuilder g = new StringBuilder();
      for (Element e : elements) {
         g.append(XmlUtil.getFirstLevelTextContent(e)).append(" ");
      }
      given = StringUtils.trimToNull(g.toString());
      if (given != null) name.append(given + " ");

      // -------------------------------- family 
      elements = XmlUtil.getFirstLevelChildElementsByName(node, "family");
      if (elements.length == 1) {
         family = XmlUtil.getFirstLevelTextContent(elements[0]);
         name.append(family + " ");
      }

      // -------------------------------------------------- suffix
      elements = XmlUtil.getFirstLevelChildElementsByName(node, "suffix");
      if (elements.length == 1) {
         suffix = XmlUtil.getFirstLevelTextContent(elements[0]);
         name.append(suffix + " ");
      }
      // Entire name stored in value
      value = name.toString().trim();
      log.debug(msg.append(name));
   } // EO load value 

   @Override
   public void loadWSLog(WSLog wsLog) {   
      if (wsLog != null) {
         LoadNameValuePairs.addPair(wsLog, "prepopData " + element, value,
            LoadNameValuePairs.SOAP_REQUEST);
      }
   } // EO loadValue method

} // EO PersonNameDataElement class
