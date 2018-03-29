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
 *******************************************************************************/
/**
 * 
 */
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
 * DataElement subclass for {@code <addr> } elements
 */
public class AddrDataElement extends DataElement {
   private static final long serialVersionUID = 1L;
   
   /**
    * Name of parent element containing address elements. Defaults to "addr",
    * but may be other, for example, "value".
    */
   protected String nameOfParentElement = "addr";
   /**
    * street address, for example "104 Main St"
    */
   protected String streetAddressLine = null;
   /**
    * city name, for example, "Chicago"
    */
   protected String city = null;
   /**
    * state name, usually two character abbreviation, for example "IL"
    */
   protected String state = null;
   /**
    * postal (zip) code, for example, "90012"
    */
   protected String postalCode = null;
   /**
    * country or country code, for example, "US"
    */
   protected String country = null;

   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    */
   public AddrDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression) {
      super(xpath, parName, element, description, xpathExpression);
   }
   
   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    * @param nameOfParentElement the name of the element containing the other
    * address component elements. This defaults to "addr".
    */
   public AddrDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression, String nameOfParentElement) {
      super(xpath, parName, element, description, xpathExpression);
      if (StringUtils.isBlank(nameOfParentElement)) {
         log.warn("nameOfParentElement is blank, using 'addr'");
      } else {
         this.nameOfParentElement = nameOfParentElement;
      }
   }
   
   /**
    * Copy Constructor
    * @param other AddrDataElement
    */
   public AddrDataElement(AddrDataElement other) {
      super();
      move(other, this);
      this.nameOfParentElement = other.nameOfParentElement;
      
   }

   /**
    * @return the {@link #streetAddressLine} value.
    */
   public String getStreetAddressLine() {
      return streetAddressLine;
   }

   /**
    * @param streetAddressLine the {@link #streetAddressLine} to set
    */
   public void setStreetAddressLine(String streetAddressLine) {
      this.streetAddressLine = streetAddressLine;
   }

   /**
    * @return the {@link #city} value.
    */
   public String getCity() {
      return city;
   }

   /**
    * @param city the {@link #city} to set
    */
   public void setCity(String city) {
      this.city = city;
   }

   /**
    * @return the {@link #state} value.
    */
   public String getState() {
      return state;
   }

   /**
    * @param state the {@link #state} to set
    */
   public void setState(String state) {
      this.state = state;
   }

   /**
    * @return the {@link #postalCode} value.
    */
   public String getPostalCode() {
      return postalCode;
   }

   /**
    * @param postalCode the {@link #postalCode} to set
    */
   public void setPostalCode(String postalCode) {
      this.postalCode = postalCode;
   }

   /**
    * @return the {@link #country} value.
    */
   public String getCountry() {
      return country;
   }

   /**
    * @param country the {@link #country} to set
    */
   public void setCountry(String country) {
      this.country = country;
   }
   
   @Override
   public AddrDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public AddrDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }
   
   @Override
   public void loadValue(Element element2) throws Exception {
      value = null;
      streetAddressLine = null;
      city = null;
      state = null;
      postalCode = null;
      country = null;
      StringBuilder msg =
         new StringBuilder("Load Address: ").append(parName + nl);
      Node node = (Node) evaluate(element2, xpathExpression, XPathConstants.NODE);

      // Must be correct name Element
      if (node == null || node.getNodeType() != Node.ELEMENT_NODE
         || node.getLocalName().equalsIgnoreCase(nameOfParentElement) == false) {
         msg.append(nameOfParentElement + " element not found");
         log.debug(msg.toString());
         return;
      }

      // -------------------------------------------------- streetAddressLine
      StringBuilder name = new StringBuilder();
      Element[] elements =
         XmlUtil.getFirstLevelChildElementsByName(node, "streetAddressLine");
      if (elements.length == 1) {
         streetAddressLine = XmlUtil.getFirstLevelTextContent(elements[0]);
         name.append(streetAddressLine + " ");
      }

      // -------------------------------------------------- given
      elements = XmlUtil.getFirstLevelChildElementsByName(node, "city");
      if (elements.length == 1) {
         city = XmlUtil.getFirstLevelTextContent(elements[0]);
         name.append(city + " ");
      }

      // -------------------------------------------------- given
      elements = XmlUtil.getFirstLevelChildElementsByName(node, "state");
      if (elements.length == 1) {
         state = XmlUtil.getFirstLevelTextContent(elements[0]);
         name.append(state + " ");
      }

      // -------------------------------------------------- given
      elements = XmlUtil.getFirstLevelChildElementsByName(node, "postalCode");
      if (elements.length == 1) {
         postalCode = XmlUtil.getFirstLevelTextContent(elements[0]);
         name.append(postalCode + " ");
      }

      // -------------------------------------------------- given
      elements = XmlUtil.getFirstLevelChildElementsByName(node, "country");
      if (elements.length == 1) {
         country = XmlUtil.getFirstLevelTextContent(elements[0]);
         name.append(country + " ");
      }
      value = name.toString();
      log.debug(msg.append(name));
   } // EO load value 

   @Override
   public void loadWSLog(WSLog wsLog) { 
      StringBuilder name = new StringBuilder();
      if (StringUtils.isNotBlank(streetAddressLine)) name.append(streetAddressLine + " ");
      if (StringUtils.isNotBlank(city)) name.append(city + " ");
      if (StringUtils.isNotBlank(state)) name.append(state + " ");
      if (StringUtils.isNotBlank(postalCode)) name.append(postalCode + " ");
      if (StringUtils.isNotBlank(country)) name.append(country + " ");
      LoadNameValuePairs.addPair(wsLog, "prepopData " + element, name,
         LoadNameValuePairs.SOAP_REQUEST);
   } 

} // EO addrDataElement class
