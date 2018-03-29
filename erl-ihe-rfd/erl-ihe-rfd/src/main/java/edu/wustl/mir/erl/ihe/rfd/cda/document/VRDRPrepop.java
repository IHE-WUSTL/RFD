/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda.document;

import java.io.IOException;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import edu.wustl.mir.erl.ihe.rfd.cda.AddrDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.CodedDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.DataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.FunctionDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.IdDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.InValueSetTrigger;
import edu.wustl.mir.erl.ihe.rfd.cda.IntervalDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.PersonNameDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.ProcedureDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.SequenceDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.ValueSet;
import edu.wustl.mir.erl.ihe.rfd.cda.ValueSet.Code;
import edu.wustl.mir.erl.ihe.rfd.cda.Variable;
import edu.wustl.mir.erl.ihe.util.TS;
import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Combination for Prepop CDA documents for VRDR
 */
public class VRDRPrepop extends CDADocument {
   private static final long serialVersionUID = 1L;

   // -------------------------------- xpath for document

   private static QName[] qnames = {
      new QName("urn:ihe:iti:rfd:2007", "dummy", "urn"),
      new QName("urn:hl7-org:v3", "dummy", ""),
      new QName("urn:hl7-org:v3", "dummy", "nl"),
      new QName("urn:hl7-org:sdtc", "dummy", "stdc"),
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
   private static String SHORT_FORM;

   static {
      try {
         MASTER_FORM =
            IOUtils.toString(HWS.class.getResource("VRDRMasterForm.html"),
               CHAR_SET_UTF_8);
         SHORT_FORM =
            IOUtils.toString(HWS.class.getResource("VRDRShortForm.html"),
               CHAR_SET_UTF_8);
      } catch (IOException e) {
         Util.exit(Util.getEM(e));
      }
   }

   /**
    * Creates new VRDRPrepop instance
    */
   public VRDRPrepop() {

      
      try {
      // @formatter:off
         
         addDataElement(
            new SequenceDataElement <IdDataElement> (
               getXPath(),
               "prepopDocument$roots",
               "root attribute(s)",
               "templateId root attribute value(s)",
               "/nl:ClinicalDocument/nl:templateId",
               new IdDataElement(
                  getXPath(),
                  "prepopDocument$root",
                  "root attribute",
                  "templateId root attribute value", "")
               .loadSnippet("DATAELEMENT_VARIABLE"))
               .setCopy(new IdDataElement.Copy())
               .setTrigger(new CDATypeTrigger(ValueSet.getValueSetForName("VRDR Prepop Document root")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
          );
         
         addDataElement(
            new FunctionDataElement(
               getXPath(),
               "prepopDocument",
               "Prepopulation Document Type",
               "CDA Type of Prepopulation Document, or blank.",
               "if (${prepopDocument$rootsCount} > 0) {'${prepopDocument$rootValue}'} else { '' }")
               .addVariable(new Variable(getDataElementByName("prepopDocument$roots")))
         );
         
         addDataElement(
            new PersonNameDataElement(
               getXPath(),
               "decedentName",
               "Decedent Name",
               "Name of the Decedent",
               "//nl:recordTarget/nl:patientRole/nl:patient/nl:name"));

      addDataElement(
         new DataElement(
            getXPath(), 
            "dateOfBirth", 
            "Date of Birth (Mo/Day/Yr)",
            "Calendar date when decedent was born",
            "//nl:recordTarget/nl:patientRole/nl:patient/nl:birthTime/@value"));

      addDataElement(
      new AddrDataElement(
         getXPath(),
         "decedentResidence",
         "Decedent Residence",
         "The geographic location of the decedent's residence",
         "//nl:recordTarget/nl:patientRole/nl:addr",
         "addr"));

      addDataElement(
      new CodedDataElement(
         getXPath(),
         "race",
         "Decedent Race",
         "Decedent Race",
         "//nl:recordTarget/nl:patientRole/nl:patient/nl:raceCode"));
      
      addDataElement(
         new SequenceDataElement<CodedDataElement>(
            getXPath(),
            "sex$Males",
            "Sex Male Code",
            "Sex Male Code",
            "//nl:recordTarget/nl:patientRole/nl:patient/nl:administrativeGenderCode",
            new CodedDataElement(
               getXPath(),
               "sex$Male",
               "Sex Male Code",
               "Sex Male Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Male Gender (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY));
      
      addDataElement(
         new SequenceDataElement<CodedDataElement>(
            getXPath(),
            "sex$Females",
            "Sex Female Code",
            "Sex Female Code",
            "//nl:recordTarget/nl:patientRole/nl:patient/nl:administrativeGenderCode",
            new CodedDataElement(
               getXPath(),
               "sex$Female",
               "Sex Female Code",
               "Sex Female Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Female Gender (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY));

      addDataElement(
    	 new FunctionDataElement(
    	    getXPath(),
    	    "sex",
    	    "Decedent Sex",
    	    "Gender of Decedent.",
    	    "IF Sex CONTAINS ValueSet (Male Gender (NCHS) Value Set) THEN 'SEX' SHALL ='M' " +
    	    "ELSE IF Sex CONTAINS ValueSet(Female Gender (NCHS) Value Set) THEN 'SEX' SHALL ='F' " + 
    	    "ELSE THEN 'SEX' SHALL ='U'")
    	    .addMapping(" ELSE ", " else ")
    	    .addMapping("THEN 'SEX' SHALL ='M'", "{'M'}")
    	    .addMapping("THEN 'SEX' SHALL ='F'", "{'F'}")
    	    .addMapping("THEN 'SEX' SHALL ='U'", "{'U'}")
    	    .addVariable(new Variable(getDataElementByName("sex$Males"))
    	    .addMapping("Sex CONTAINS ValueSet (Male Gender (NCHS) Value Set)","(${sex$MalesCount} > 0)"))
    	    .addVariable(new Variable(getDataElementByName("sex$Females"))
    	    .addMapping("Sex CONTAINS ValueSet(Female Gender (NCHS) Value Set)","(${sex$FemalesCount} > 0)"))
      );
      
      addDataElement(
         new DataElement(
            getXPath(),
            "ssn",
            "Jurisdiction Person Identifyier (e.g. SSN)",
            "Jurisdiction Person Identifyier (e.g. SSN) of Decedent",
            "/nl:ClinicalDocument/nl:recordTarget/nl:patientRole/nl:id[@root='2.16.840.1.113883.4.1']/@extension"));
         
      addDataElement(
         new IntervalDataElement(
            getXPath(),
            "dateOfDeath",
            "Date of Death",
            "Calendar date when decedent died",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.3.23.1']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.24.1.13']]/nl:effectiveTime",
            TS.DATE));

      /* Not testing. On hold for clarification in Supplement
      addDataElement(
      new IntervalDataElement(
         getXPath(),
         "timeOfDeath",
         "Time of Death",
         "Clock time when decedent died.",
         "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.5']]/nl:entry/nl:observation[nl:templateId[@root='2.16.840.1.113883.10.20.24.1.3']]/nl:effectiveTime",
         TS.DATETIME));
         */
      
      // Date Certified
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
            getXPath(),
            "dateCertified$Procedures",
            "Death certification(s)",
            "Death certification(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure",
            new ProcedureDataElement( 
               getXPath(),
               "dateCertified$Procedure",
               "Death certification",
               "Death certification", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Death Certification (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "dateCertified",
            "Date Certified",
            "Date of Certification of Death, if Certification Procedure performed.",
            "IF (Procedure CONTAINS (Certifying Death Procedure Performed)) then Date Certified SHALL = Procedure Date ELSE ''")
            .addMapping("then Date Certified SHALL = Procedure Date ELSE ''", "{'${dateCertified$Procedure-intervalLowValue}'} else {''}")
            .addVariable(new Variable(getDataElementByName("dateCertified$Procedures"))
               .addMapping("Certifying Death Procedure Performed", "Death Certification (NCHS)")
               .addMapping("Procedure CONTAINS (Death Certification (NCHS))","${dateCertified$ProceduresCount} > 0"))
         );
      
      // Date Pronounced
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
            getXPath(),
            "datePronounced$Procedures",
            "Death Pronouncement(s)",
            "Death Pronouncement(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure",
            new ProcedureDataElement( 
               getXPath(),
               "datePronounced$Procedure",
               "Death Pronouncement",
               "Death Pronouncement", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Death Pronouncement Procedure (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      // XPath for datePronounced$Findings not yet tested; will require second message
      
      addDataElement(
         new SequenceDataElement <ProcedureDataElement>(
            getXPath(),
            "datePronounced$Findings",
            "Death Pronouncement Finding(s)",
            "Death Pronouncement Finding(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation",
            new ProcedureDataElement(
               getXPath(),
               "datePronounced$Finding",
               "Death Pronouncement Finding",
               "Death Pronouncement Finding", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Death Pronouncement Finding (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
      );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "datePronounced",
            "Date Pronounced",
            "Date of Pronouncement of Death.",
            "IF (Procedure CONTAINS (Pronouncing Death Procedure Performed (NCHS))) " +
            "then Date Pronounced SHALL = Procedure Date ELSE " +
            "IF (Pronouncement of Death Finding CONTAINS (Death Pronouncement Finding (NCHS))) " +
            "then Date Pronounced SHALL = Pronouncement of Death Finding Date ELSE ''")
            .addMapping("then Date Pronounced SHALL = Procedure Date ELSE", "{'${datePronounced$Procedure-intervalLowValue}'} else {")
            .addMapping("then Date Pronounced SHALL = Pronouncement of Death Finding Date ELSE ''", "{'${datePronounced$Finding-intervalLowValue}'} else {''}}")
            .addVariable(new Variable(getDataElementByName("datePronounced$Procedures"))
               .addMapping("Procedure CONTAINS (Pronouncing Death Procedure Performed (NCHS))","${datePronounced$ProceduresCount} > 0"))
            .addVariable(new Variable(getDataElementByName("datePronounced$Findings"))
               .addMapping("Pronouncement of Death Finding CONTAINS (Death Pronouncement Finding (NCHS))","${datePronounced$FindingsCount} > 0"))
         );
      
      // Autopsy performed
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
                  getXPath(),
                  "autopsyPerformed$Procedures",
                  "Autopsy performed Procedure(s)",
                  "Autopsy performed Procedure(s)",
                  "//nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure",
                  new ProcedureDataElement( 
                     getXPath(),
                     "autopsyPerformed$Procedure",
                     "Autopsy performed Procedure",
                     "Autopsy performed Procedure", "")
                  .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
                  .setCopy(new ProcedureDataElement.Copy())
                  .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Autopsy Performed (NCHS)")))
                  .setSequenceType(SequenceDataElement.SequenceType.ANY)
             );
      
      // XPath for autopsyNotPerformed$Observations not yet tested, will require second document
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
                  getXPath(),
                  "autopsyNotPerformed$Observations",
                  "Autopsy not performed Observation(s)",
                  "Autopsy not performed Observation(s)",
                  "/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation",
                  new ProcedureDataElement( 
                     getXPath(),
                     "autopsyNotPerformed$Observation",
                     "Autopsy not performed Observation",
                     "Autopsy not performed Observation", "")
                  .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
                  .setCopy(new ProcedureDataElement.Copy())
                  .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Autopsy Not Performed (NCHS)")))
                  .setSequenceType(SequenceDataElement.SequenceType.ANY)
             );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "autopsyPerformed",
            "Autopsy Performed",
            "Information on wheterh or not an autopsy was performed.",
            "IF (Autopsy Procedure CONTAINS (Autopsy Performed (NCHS))) then AUTOP SHALL = ‘Y’ " + 
            "ELSE IF (Autopsy Findings CONTAINS CONTAINS (Autopsy Not Performed (NCHS))) then AUTOP SHALL = ‘N’")
            .addMapping("then AUTOP SHALL = ‘Y’", "{'Y'}")
            .addMapping("then AUTOP SHALL = ‘N’", "{'N'} else {''}")
            .addMapping("ELSE", "else")
            .addVariable(new Variable(getDataElementByName("autopsyPerformed$Procedures"))
               .addMapping("Autopsy Procedure CONTAINS (Autopsy Performed (NCHS))","${autopsyPerformed$ProceduresCount} > 0"))
            .addVariable(new Variable(getDataElementByName("autopsyNotPerformed$Observations"))
               .addMapping("Autopsy Findings CONTAINS CONTAINS (Autopsy Not Performed (NCHS))","${autopsyNotPerformed$ObservationsCount} > 0"))
         );
      
      
      
      // @formatter:on
   } catch (Exception e) {
      Util.exit(Util.getEM(e));
   }
      
   } // EO no argument constructor

   /**
    * If the data used to populate the form is from an MS-VRDR pre-population 
    * CDA document, use the master form, otherwise use the short form.
    */
   @Override
   public String populate() {
      DataElement prepopDocument = getDataElementByName("prepopDocument");
      if (prepopDocument != null
         && prepopDocument.getValue().contains("MS-VRDR"))
         return populate(MASTER_FORM);
      return populate(SHORT_FORM);
   }

   /**
    * @return the sex value.
    */
   public CodedDataElement getSex() {
      return (CodedDataElement) getDataElementByName("sex");
   }
   
   /**
    * @return the time of death element
    */
   public IntervalDataElement getTimeofDeath() {
      return (IntervalDataElement) getDataElementByName("timeOfDeath");
   }
   
   /**
    * @return decedent date of birth data element
    */
   public DataElement getDOB() {
      return getDataElementByName("dateOfBirth");
   }
   
   /**
    * Trigger returns code display name as value of matched DataElement. Does 
    * not match code OID. 
    */
   public class CDATypeTrigger extends InValueSetTrigger {
      private static final long serialVersionUID = 1L;
      
      /**
       * @param valueSet sets one value set, and match OID to false;
       */
      public CDATypeTrigger(ValueSet valueSet) {
         super(valueSet);
         matchOID = false;
      }
      
      @Override
      public boolean trigger(DataElement dataElement, StringBuilder msg) {
         Code cd = null;
         String code = dataElement.getValue();
         for (ValueSet vs : valuesets) {
            try { cd = vs.getCode(code); } catch (Exception e) {continue;}
            dataElement.setValue(cd.getDisplay());
            return true;
         }
         return false;
      }
      
   } // EO CDATypeTrigger
   
   PREPOP_DOCUMENT_TYPE prepopDocumentType = null;
   /**
    * @return prepopDocumentType value
    */
   public PREPOP_DOCUMENT_TYPE getPrepopDocumentType() {
      if (prepopDocumentType == null) {
         DataElement de = getDataElementByName("prepopDocument");
         if (de != null) {
            String val = de.getValue();
            if (StringUtils.isEmpty(val)) prepopDocumentType = PREPOP_DOCUMENT_TYPE.UNKNOWN;
            else if (val.contains("MS-VRDR")) prepopDocumentType = PREPOP_DOCUMENT_TYPE.MS_VRDR;
            else prepopDocumentType = PREPOP_DOCUMENT_TYPE.OTHER_VALID;
         }
      }
      return prepopDocumentType;
   }
   
   /**
    * Type of Prepop CDA document
    */
   @SuppressWarnings("javadoc")
   public enum PREPOP_DOCUMENT_TYPE {
      MS_VRDR,
      OTHER_VALID,
      UNKNOWN,
      INVALID
   }
   
   /**
    * Determines if the passed {@link Element} is the root ClinicalDocument
    * element of a valid VRDR Prepop CDA document.
    * @param element element to evaluate
    * @return {@link PREPOP_DOCUMENT_TYPE}
    */
   public static PREPOP_DOCUMENT_TYPE getPrepopDocumentType(Element element) {
      if (element == null) return PREPOP_DOCUMENT_TYPE.INVALID;
      VRDRPrepop v = new VRDRPrepop();
      try {
         v.loadValue(element);
      } catch (Exception e) {
         Util.getLog().warn(Util.getEM(e));
         return PREPOP_DOCUMENT_TYPE.INVALID;
      }
      return v.getPrepopDocumentType();
   }

}
