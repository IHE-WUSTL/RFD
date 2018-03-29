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

import java.io.IOException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;

import org.apache.commons.io.IOUtils;
import org.javatuples.Pair;

import edu.wustl.mir.erl.ihe.rfd.cda.CodedDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.DataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.ValueDataElement;
import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Healthy Weight Summary CDA CDADocument
 */
public class HWS extends CDADocument {
   private static final long serialVersionUID = 1L;

   // -------------------------------- xpath for document

   private static QName[] qnames = {
      new QName("urn:ihe:iti:rfd:2007", "dummy", "urn"),
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

   // ------------------------ Load master form for this document
   
   private static String MASTER_FORM;

   static {
      try {
         MASTER_FORM =
            IOUtils.toString(HWS.class.getResource("HWSForm.html"),
               CHAR_SET_UTF_8);
      } catch (IOException e) {
         Util.exit(Util.getEM(e));
      }
   }
   

   /**
    * Create new HWS instance
    */
   public HWS() {

      addDataElement(
         new CodedDataElement(
            getXPath(),
            "sex",
            "Administrative Sex",
            "Patient’s sex",
            "//nl:recordTarget/nl:patientRole/nl:patient/nl:administrativeGenderCode",
            "2.16.840.1.113883.5.1", "HL7 AdministrativeGender"));

      addDataElement(
         new CodedDataElement(getXPath(), "race", "Race",
         "Race(s) that best describes what the patient considers himself/herself to be ",
         "//nl:recordTarget/nl:patientRole/nl:patient/nl:raceCode")
            .addCodeSystem(new Pair <String, String>("2.16.840.1.113883.5.104", "H&P DSTU OID for Race"))
            .addCodeSystem(new Pair <String, String>("2.16.840.1.113883.1.11.14914", "PHINVADS link for HL7 V3 Race")));

      addDataElement(
         new ValueDataElement(
            getXPath(),
            "height",
            "Height",
            "Patient’s height, captured for patients 2 through 22 years.",
            "//nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.5.3.2']]/nl:entry/nl:organizer[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.1']]/nl:component/nl:observation[nl:code[@code='8302-2' or @code='3137-7' or @code='3138-5']]/nl:value"));

      addDataElement(
         new ValueDataElement(
            getXPath(),
            "weight",
            "Weight",
            "Weight (with or without clothes and shoes) .",
            "//nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.5.3.2']]/nl:entry/nl:organizer[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.1']]/nl:component/nl:observation[nl:code[@code='29463-7' or @code='3142-7' or @code='3141-9' or @code='8350-1' or @code='8351-9']]/nl:value"));
      
      addDataElement(
        new ValueDataElement(
           getXPath(),
           "ssbOther",
           "SSB Other",
           "how many times did the patient drink any punch, Kool-Aid, Tampico, other fruit-flavored drinks, or sports drinks?",
           "//nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.16.1']]/nl:entry/nl:observation[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.4'] and nl:code[@code='77297-0']]/nl:value"));
      
      addDataElement(
        new ValueDataElement(
           getXPath(),
           "ssbSoftDrinks",
           "SSB Soft Drinks",
           "how many times did the patient drink any regular (not diet) sodas or soft drinks?",
           "//nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.16.1']]/nl:entry/nl:observation[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.4'] and nl:code[@code='77300-2']]/nl:value"));
      
        addDataElement(
        new ValueDataElement(
           getXPath(),
           "milk",
           "Milk intake",
           "how much milk did the patient drink?",
           "//nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.16.1']]/nl:entry/nl:observation[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.4'] and nl:code[@code='77393-7']]/nl:value"));
      
        addDataElement(
        new ValueDataElement(
           getXPath(),
           "water",
           "Water intake",
           "how much water did the patient drink?",
           "//nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.16.1']]/nl:entry/nl:observation[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13.4'] and nl:code[@code='77295-4']]/nl:value"));
      
        addDataElement(
         new CodedDataElement(
            getXPath(),
            "insuranceType",
            "Insurance Type",
            "Patient’s Insurance details: insurance type",
            "//nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.5.3.7']]/nl:entry[nl:act/nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.17']]/nl:act/nl:entryRelationship/nl:act/nl:code"));
      
         addDataElement(
         new DataElement(
            getXPath(),
            "insuranceCompanyName",
            "Insurance Company",
            "Patient Insurance details: insurance company",
            "//nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.5.3.7']]/nl:entry/nl:act[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.17']]/nl:entryRelationship/nl:act[nl:templateId[@root='2.16.840.1.113883.10.20.1.26']]/nl:performer/nl:assignedEntity/nl:representedOrganization/nl:name/text()"));
   }

   /*
    * (non-Javadoc)
    * 
    * @see edu.wustl.mir.erl.ihe.rfd.cda.CDADocument#prepopulateForm()
    */
   @Override
   public String populate() {
      return populate(MASTER_FORM);
   }

   /**
    * @return the sex} value.
    */
   public CodedDataElement getSex() {
      return (CodedDataElement) getDataElementByName("sex");
   }

   /**
    * @return the race} value.
    */
   public CodedDataElement getRace() {
      return (CodedDataElement) getDataElementByName("race");
   }

   /**
    * @return the insuranceType} value.
    */
   public CodedDataElement getInsuranceType() {
      return (CodedDataElement) getDataElementByName("insuranceType");
   }

} // EO HWS class
