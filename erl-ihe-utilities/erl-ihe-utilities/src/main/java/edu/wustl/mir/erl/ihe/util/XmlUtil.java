/*
 * Copyright (c) 2014 Washington University in St. Louis All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. The License is available at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: Initial author: Ralph Moulton / MIR WUSM IHE Development
 * Project moultonr@mir.wustl.edu
 */
package edu.wustl.mir.erl.ihe.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * static XML related Utility methods. *
 */
public class XmlUtil implements Serializable, UtilProperties {
   private static final long serialVersionUID = 1L;

   /**
    * null safe check, does list contain any Elements?
    * 
    * @param elementList list to check
    * @return boolean true if list is null or empty, false otherwise.
    */
   public static boolean isEmpty(List <Element> elementList) {
      if (elementList == null) return true;
      if (elementList.isEmpty()) return true;
      return false;
   }

   /**
    * null safe check, does list not contain any Elements?
    * 
    * @param elementList list to check
    * @return boolean false if list is null or empty, true otherwise.
    */
   public static boolean isNotEmpty(List <Element> elementList) {
      return !isEmpty(elementList);
   }

   /**
    * Returns text content of passed node, but not that of descendant nodes.
    * 
    * @param node Node to get text from.
    * @return Text of this node; null if node is null or no text subnodes exist.
    */
   public static String getFirstLevelTextContent(Node node) {
      if (node == null) return null;
      NodeList list = node.getChildNodes();
      if (list == null) return null;
      boolean textFound = false;
      StringBuilder textContent = new StringBuilder();
      for (int i = 0; i < list.getLength(); ++i) {
         Node child = list.item(i);
         if (child.getNodeType() == Node.TEXT_NODE) {
            textContent.append(child.getTextContent());
            textFound = true;
         }
      }
      if (textFound) return textContent.toString();
      return null;
   }

   /**
    * Parses an xml string and returns the corresponding {@link Element}. 
    * 
    * @param xmlStr String to parse; should be valid xml.
    * @return Element represented by string
    * @throws Exception on parsing error.
    */
   public static Element strToElement(String xmlStr) throws Exception {
      InputStream sbis =
         new ByteArrayInputStream(xmlStr.getBytes(CHAR_SET_UTF_8));
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(sbis);
      return doc.getDocumentElement();
   }
   
   /**
    * Gets first level child Elements of passed node which have passed name.
    * @param node Node to examine
    * @param name String name to match
    * @return an array of the Elements found. May be empty, but will not be null
    */
   public static Element[] getFirstLevelChildElementsByName(Node node,
      String name) {
      List <Element> found = new ArrayList <>();
      if (node != null && node.getNodeType() == Node.ELEMENT_NODE) {
         NodeList children = node.getChildNodes();
         for (int i = 0; i < children.getLength(); i++ ) {
            Node child = children.item(i);
            if (child != null && child.getNodeType() == Node.ELEMENT_NODE
               && child.getLocalName().equalsIgnoreCase(name))
               found.add((Element) child);
         }
      }
      return found.toArray(new Element[0]);
   }

} // EO XmlUtil class
