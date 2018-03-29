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

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.lang.StringUtils;
import org.javatuples.Pair;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Subclass of DataElement to handle coded value elements. The XPath expression
 * points to the Node. The target data is in the attributes. The value of the
 * code attribute is stored in the DataElement value property.
 */
public class CodedDataElement extends DataElement {
   private static final long serialVersionUID = 1L;

   private List <Pair <String, String>> codeSystems = new ArrayList <>();

   /**
    * short, human readable description of the code value. For example, for
    * raceCode code = 101, displayName = White. If no displayName attribute is
    * present in the coded value Element, the code value will be used.
    */
   protected String displayName;
   /**
    * The coding system identifier, often a UUID. For example, for
    * administrativeGenderCode, codeSystem = 2.16.840.1.113883.5.1. If no
    * codeSystem attribute is present in the coded value Element, a value may be
    * provided during document instantiation, or the value will be the empty
    * String.
    */
   protected String codeSystem = "";
   /**
    * A short, human readable description of the coding system. For example, for
    * administrativeGenderCode, codeSystemName = HL7 AdministrativeGender. If no
    * codeSystemName attribute is present in the coded value Element, a value
    * may be provided during document instantiation, or the value will be the
    * empty String.
    */
   protected String codeSystemName = "";

   /**
    * Instantiates coded data element with no default code system.
    * 
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    */
   public CodedDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression) {
      super(xpath, parName, element, description, xpathExpression);
   }

   /**
    * Instantiates coded data element with default code system. <b>Note: If
    * there are one or more code systems, but no default code system, use the
    * {@link #addCodeSystem} method rather than this constructor.
    * 
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    * @param codeSystem the default {@link #codeSystem} value
    * @param codeSystemName default {@link #codeSystemName} value
    */
   public CodedDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression, String codeSystem,
      String codeSystemName) {
      super(xpath, parName, element, description, xpathExpression);
      this.codeSystem = codeSystem;
      this.codeSystemName = codeSystemName;
      codeSystems.add(new Pair <String, String>(codeSystem, codeSystemName));
   }
   
   /**
    * Copy constructor
    * @param dataElement CodedDataElement
    */
   public CodedDataElement(CodedDataElement dataElement) {
      DataElement.move(dataElement, this);
      for (Pair<String, String> cs : dataElement.codeSystems)
         this.addCodeSystem(new Pair<String, String> (cs.getValue0(), cs.getValue1()));
   }
   
   

   /**
    * Add one or more code systems.
    * 
    * @param system A {@link Pair} representing the coding system to add.
    * {@link Pair#getValue0()} (the left item) is the code system, usually a
    * UUID. For example, for sex, "2.16.840.1.113883.5.1".
    * {@link Pair#getValue1()} (the right item) is the code system name. For
    * example, for sex, "HL7 AdministrativeGender".
    * @return this CodedDataElement, for method chaining.
    */
   public CodedDataElement addCodeSystem(Pair <String, String> system) {
      codeSystems.add(system);
      return this;
   }

   /**
    * @return the {@link #value} value, which is the code value for
    * CodedDataElements
    */
   public String getCode() {
      return value;
   }

   /**
    * @param value the {@link #value} to set. This is the code value for
    * CodedDataElements
    */
   public void setCode(String value) {
      this.value = value;
   }

   /**
    * @return the {@link #displayName} value.
    */
   public String getDisplayName() {
      return displayName;
   }

   /**
    * @param displayName the {@link #displayName} to set
    */
   public void setDisplayName(String displayName) {
      this.displayName = displayName;
   }

   /**
    * @return the {@link #codeSystem} value.
    */
   public String getCodeSystem() {
      return codeSystem;
   }

   /**
    * @param codeSystem the {@link #codeSystem} to set
    */
   public void setCodeSystem(String codeSystem) {
      this.codeSystem = codeSystem;
   }

   /**
    * @return the {@link #codeSystemName} value.
    */
   public String getCodeSystemName() {
      return codeSystemName;
   }

   /**
    * @param codeSystemName the {@link #codeSystemName} to set
    */
   public void setCodeSystemName(String codeSystemName) {
      this.codeSystemName = codeSystemName;
   }
   
   @Override
   public CodedDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public CodedDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }

   @Override
   public void loadValue(Element element2) throws Exception {
      value = null;
      String dn = null;
      String cs = null;
      String csn = null;
      Node attr = null;
      StringBuilder msg =
         new StringBuilder("Load Coded value: ").append(parName);
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
      attr = map.getNamedItem("code");
      if (attr != null) {
         value = attr.getNodeValue();
         msg.append(" code: ").append(value);
      }
      attr = map.getNamedItem("displayName");
      if (attr != null) {
         dn = attr.getNodeValue();
         msg.append(" displayName: ").append(dn);
      }
      attr = map.getNamedItem("codeSystem");
      if (attr != null) {
         cs = attr.getNodeValue();
         msg.append(" codeSystem: ").append(cs);
      }
      attr = map.getNamedItem("codeSystemName");
      if (attr != null) {
         csn = attr.getNodeValue();
         msg.append(" codeSystemName: ").append(csn);
      }

      // if only one of codeSystem and codeSystemName is present, look for
      // the other one.
      if (cs == null && csn != null) {
         for (Pair <String, String> pair : codeSystems) {
            if (pair.getValue0().equalsIgnoreCase(csn)) {
               cs = pair.getValue1();
               msg.append("  codeSystem (lookup): ").append(cs);
               break;
            }
         }
      } else if (cs != null && csn == null) {
         for (Pair <String, String> pair : codeSystems) {
            if (pair.getValue1().equalsIgnoreCase(cs)) {
               csn = pair.getValue0();
               msg.append("  codeSystemName (lookup): ").append(csn);
               break;
            }
         }
      }
      // ----------------------------- load what you found
      if (dn != null) displayName = dn;
      if (cs != null) codeSystem = cs;
      if (csn != null) codeSystemName = csn;

      log.debug(msg);
   } // EO load value 

   @Override
   public void loadWSLog(WSLog wsLog) {   
      String prefix = "prepopData " + this.element;
      if (value != null) {
         StringBuilder val = new StringBuilder(value);
         if (StringUtils.isNotBlank(displayName))
            val.append(" ").append(displayName);
         if (StringUtils.isNotBlank(codeSystem))
            val.append(" ").append(codeSystem);
         if (StringUtils.isNotBlank(codeSystemName))
            val.append(" ").append(codeSystemName);
         LoadNameValuePairs.addPair(wsLog, prefix, val,
            LoadNameValuePairs.SOAP_REQUEST);
      }

   } // EO load value for coded data item
   
   /** Copy class for CodedDataElement */
   public static class Copy implements DataElementCopy <CodedDataElement> {

      @Override
      public CodedDataElement copy(CodedDataElement dataElement) {
         return new CodedDataElement(dataElement);
      }      
   }

} // EO CodedDataElement class

