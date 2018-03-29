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
package edu.wustl.mir.erl.ihe.rfd.cda.document;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.wustl.mir.erl.ihe.rfd.cda.CDAPlug;
import edu.wustl.mir.erl.ihe.rfd.cda.DataElement;
import edu.wustl.mir.erl.ihe.util.Plug;
import edu.wustl.mir.erl.ihe.util.UtilProperties;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Base class for CDA CDADocument handler beans
 */
public abstract class CDADocument implements Serializable, UtilProperties {
   private static final long serialVersionUID = 1L;

   /**
    * List of DataElements that make up the form, in the order they were
    * inserted.
    */
   protected List <DataElement> elements = new ArrayList <>();

   private XPathFactory xpathFactory = XPathFactory.newInstance();

   protected synchronized XPath newXPath() {
      return xpathFactory.newXPath();
   }

   /**
    * Load Values from CDA xml {@link Document} to the corresponding
    * {@link DataElement} instances in this {@link CDADocument}. In most cases,
    * the Element is the prepopData from an RFD RetrieveFormRequest (ITI-34).
    * 
    * @param element {@link Document} to search.
    * @throws Exception on error.
    */
   public void loadValue(Element element) throws Exception {
      if (element == null) return;
      for (DataElement de : elements)
         if (de.isLoadFromDocument()) de.loadValue(element);
   }

   /**
    * Load Name/Value pairs from from {@link DataElement} instances in this
    * {@link CDADocument} to the {@link WSLog} instance.
    * 
    * @param wsLog instance to load Name/Value pairs to.
    */
   public void loadWSLog(WSLog wsLog) {
      for (DataElement de : elements)
         if (de.isLoadFromDocument()) de.loadWSLog(wsLog);
   }

   /**
    * Load default form with prepop values.
    * 
    * @return String form with prepopData filled in. <b>Note:</b> Will still
    * need to have test specific parameters replaced, for example Form ID and
    * destination URL.
    */
   public abstract String populate();

   /**
    * Load passed form with Snippets and prepop values.
    * 
    * @param form String form containing parameters of the form ${paramName},
    * which will be replaced with prepopData values.
    * @return String form with prepopData filled in. <b>Note:</b> Will still
    * need to have test specific parameters replaced, for example Form ID and
    * destination URL.
    * @see Plug
    */
   public String populate(String form) {
      String frm = form;
      Matcher matcher =
         Pattern.compile("\\$\\{[A-Z|a-z|0-9|\\-]+Snippet}").matcher(form);
      while (matcher.find()) {
         String snipPar = form.substring(matcher.start(), matcher.end());
         String snipP = StringUtils.substringAfter(snipPar, "${");
         snipP = StringUtils.substringBefore(snipP, "Snippet}");
         DataElement snipDe = getDataElementByName(snipP);
         if (snipDe == null) continue;
         String snippet = snipDe.getResolvedSnippet();
         frm = StringUtils.replace(frm, snipPar, snippet);
      }
      List <DataElement> es = new ArrayList <>();
      for (DataElement element : elements)
         if (element.isPlugInForm()) es.add(element);
      return new CDAPlug(frm).setDataElement(es.toArray(new DataElement[0]))
         .get();
   }

   /**
    * Fetch DataElement by name
    * 
    * @param parName parameter name of element to return
    * @return DataElement or subclass instance matching parName, or null if not
    * found.
    */
   public DataElement getDataElementByName(String parName) {
      for (DataElement e : elements)
         if (e.getParName().equals(parName)) return e;
      return null;

   }

   /**
    * Create array of DataElements matching passed parameter names
    * 
    * @param parNames to find
    * @return array of corresponding DataElement instances
    * @throws Exception if any of the named DataElements do not exist.
    */
   public DataElement[] getElementsByName(String... parNames) throws Exception {
      DataElement[] dataElements = new DataElement[parNames.length];
      for (int i = 0; i < parNames.length; i++ ) {
         dataElements[i] = getDataElementByName(parNames[i]);
         if (dataElements[i] == null) throw new Exception(
            "getElementsByName error: " + parNames[i] + " not found");
      }
      return dataElements;
   }

   /**
    * Add DataElement to elements lists. Assumes that plug into form is true.
    * 
    * @param element DataElement to add.
    */
   public void addDataElement(DataElement element) {
      addDataElement(true, element);
   }

   /**
    * Add DataElement to list to process
    * 
    * @param element DataElement to add
    * @param plugInForm boolean is this element to be included in those
    * processed by {@link #populate}
    */
   public void addDataElement(boolean plugInForm, DataElement element) {
      element.setPlugInForm(plugInForm);
      elements.add(element);
   }

} // EO CDADocument class
