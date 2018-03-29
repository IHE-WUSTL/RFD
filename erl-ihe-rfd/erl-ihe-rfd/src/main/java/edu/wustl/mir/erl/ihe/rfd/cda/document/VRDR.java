/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda.document;

import java.io.IOException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;

import org.apache.commons.io.IOUtils;

import edu.wustl.mir.erl.ihe.rfd.cda.AddrDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.CODDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.CodedDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.DataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.IntervalDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.PersonNameDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.SequenceDataElement;
import edu.wustl.mir.erl.ihe.util.TS;
import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Vital Records Death Report document
 */
public class VRDR extends CDADocument {
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
            IOUtils.toString(HWS.class.getResource("VRDRForm.html"),
               CHAR_SET_UTF_8);
      } catch (IOException e) {
         Util.exit(Util.getEM(e));
      }
   }

   /**
    * Creates new VRDR instance
    */
   public VRDR() {

      addDataElement(
         new IntervalDataElement(
            getXPath(),
            "dateOfDeath",
            "Date of Death",
            "Calendar date when decedent died",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.13']]/nl:effectiveTime",
            TS.DATE));

         addDataElement(
         new IntervalDataElement(
            getXPath(),
            "timeOfDeath",
            "Time of Death",
            "Clock time when decedent died.",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.13']]/nl:effectiveTime",
            TS.DATETIME));

         addDataElement(
         new SequenceDataElement <CODDataElement>(
            getXPath(),
            "causesOfDeath",
            "Cause(s) of Death",
            "Cause(s) of Death and corresponding onsetToDeathInterval intervals",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:organizer[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.6'] and nl:code[@code='69453-9']]/nl:component[nl:observation/nl:code[@code='21984-0']]",
            new CODDataElement(
               getXPath(),
               "causeOfDeath",
               "Cause of Death",
               "Cause of Death and corresponding onsetToDeathInterval interval",""))
            .setSequenceXpathExpression("./nl:sequenceNumber/@value")
            .setCopy(new CODDataElement.Copy())
            );

         addDataElement(
         new DataElement(
            getXPath(),
            "otherSignificantConditions",
            "Other Significant Conditions",
            "Cause of Death - Other Significant Conditions",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:organizer[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.6'] and nl:code[@code='69453-9']]/nl:component/nl:observation[nl:code[@code='69441-4']]/nl:value/nl:originalText/text()"));

         addDataElement(
         new PersonNameDataElement(
            getXPath(),
            "certifierName",
            "Certifier Name",
            "Name of the person completing the cause of death",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.7']]/nl:performer/nl:assignedEntity/nl:assignedPerson/nl:name"));

         addDataElement(
         new AddrDataElement(
            getXPath(),
            "certifierAddress",
            "Certifier Address",
            "Address of the person completing the cause of death",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.7']]/nl:performer/nl:assignedEntity/nl:addr"));

         addDataElement(
         new IntervalDataElement(
            getXPath(),
            "dateCertified",
            "Date Certified",
            "Calendar date when the death record is certified",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.7']]/nl:effectiveTime",
            TS.DATE));

         addDataElement(
         new DataElement(getXPath(), "dateOfBirth", "Date of Birth (Mo/Day/Yr)",
            "Calendar date when decedent was born",
            "//nl:recordTarget/nl:patientRole/nl:patient/nl:birthTime/@value"));

         addDataElement(
         new DataElement(
            getXPath(),
            "dateOfInjury",
            "Date of Injury",
            "Actual or presumed date when decedent sustained injury",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:organizer[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.9']]/nl:component/nl:observation[nl:code[@code='11374-6']]/nl:effectiveTime/@value"));

         addDataElement(
         new CodedDataElement(
            getXPath(),
            "didTobaccoContribute",
            "Did tobacco use contribute to death?",
            "A clinical opinion on whether tobacco use contributed to the decedent’s death.",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.14']]/nl:value",
            "2.16.840.1.114222.4.11.6004", "Contributory Tobacco Use (NCHS)"));

         addDataElement(
         new AddrDataElement(
            getXPath(),
            "addressOfDeath",
            "Street address where death occurred",
            "If not in an institution, the geographic location where the death occurred is provided including the street & number.",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.10']]/nl:value",
            "value"));

         addDataElement(
         new CodedDataElement(
            getXPath(),
            "pregnancyStatus",
            "Pregnancy Status",
            "pregnancy status of the deceased woman within the last year of her life",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.12']]/nl:value",
            "2.16.840.1.114222.4.5.274", "PHIN VS (CDC Local Coding System)"));

         addDataElement(
         new CodedDataElement(
            getXPath(),
            "mannerOfDeath",
            "Manner of Death",
            "the manner or how the deceased died",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.11']]/nl:value",
            "2.16.840.1.113883.6.96", "SNOMED-CT"));

         addDataElement(
         new AddrDataElement(
            getXPath(),
            "locationOfDeath",
            "Place of Death",
            "The physical location where the decedent died",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.10']]/nl:value",
            "value"));

         addDataElement(
         new CodedDataElement(
            getXPath(),
            "sex",
            "Administrative Sex",
            "Patient’s sex",
            "//nl:recordTarget/nl:patientRole/nl:patient/nl:administrativeGenderCode",
            "2.16.840.1.113883.5.1", "HL7 AdministrativeGender"));

         addDataElement(
         new DataElement(
            getXPath(),
            "timeOfInjury",
            "Time of Injury",
            "Actual or presumed time of injury.",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:organizer[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.9']]/nl:component/nl:observation[nl:code[@code='11374-6']]/nl:effectiveTime/@value"));

         addDataElement(
         new CodedDataElement(
            getXPath(),
            "titleOfCertifier",
            "Title of Certifier",
            "Medical professional label used to signify a professional role or membership in a professional society",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.7']]/nl:performer/nl:assignedEntity/nl:code",
            "2.16.840.1.113883.6.96", "SNOMED-CT"));

         addDataElement(
         new CodedDataElement(
            getXPath(),
            "transportationInjury",
            "Transportation Injury",
            "Information on the role of the decedent involved in a transportation accident.",
            "//nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.2']]/nl:entry/nl:organizer[nl:templateId[@root='2.16.840.1.113883.10.20.26.1.9']]/nl:component/nl:observation[nl:code[@code='69451-3']]/nl:value",
            "2.16.840.1.113883.6.96", "SNOMED-CT"));

   } // EO no argument constructor

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
    * @return the certifierName value.
    */
   public PersonNameDataElement getCertifierName() {
      return (PersonNameDataElement) getDataElementByName("certifierName");
   }

   /**
    * @return the sex value.
    */
   public CodedDataElement getSex() {
      return (CodedDataElement) getDataElementByName("sex");
   }

} // EO VRDR class

