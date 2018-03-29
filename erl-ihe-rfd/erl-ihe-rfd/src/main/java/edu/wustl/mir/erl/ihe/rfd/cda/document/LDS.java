/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda.document;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import edu.wustl.mir.erl.ihe.rfd.cda.AddrDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.CDAPlug;
import edu.wustl.mir.erl.ihe.rfd.cda.CodedDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.DataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.FunctionDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.IdDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.InValueSetTrigger;
import edu.wustl.mir.erl.ihe.rfd.cda.PersonNameDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.ProcedureDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.SequenceDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.ValueSet;
import edu.wustl.mir.erl.ihe.rfd.cda.Variable;
import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Labor Deliver Summary and -VR document
 */
public class LDS extends CDADocument {
   private static final long serialVersionUID = 1L;

   // -------------------------------- xpath for document

   private static QName[] qnames =
      { new QName("urn:ihe:iti:rfd:2007", "dummy", "urn"),
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
         MASTER_FORM = IOUtils.toString(HWS.class.getResource("LDSForm.html"),
            CHAR_SET_UTF_8);
      } catch (IOException e) {
         Util.exit(Util.getEM(e));
      }
   }

   /**
    * Create new LDS instance
    */
   public LDS() {
      
      try {
      // @formatter:off
      addDataElement(
         new AddrDataElement(
            getXPath(),
            "addressD",
            "Facility Address",
            "Facility Address, shall be populated with $ChildFacilityAddress",
            "/nl:ClinicalDocument/nl:componentOf/nl:encompassingEncounter/nl:location/nl:healthCareFacility/nl:location/nl:addr"
            ));
      
      addDataElement(
         new DataElement(
            getXPath(),
            "fName",
            "Facility Name",
            "The name of the facility where the delivery took place.",
            "/nl:ClinicalDocument/nl:componentOf/nl:encompassingEncounter/nl:location/nl:healthCareFacility/nl:location/nl:name/text()"
            ));
      
      addDataElement(
         new IdDataElement(
            getXPath(),
            "sfn",
            "Baby Facility State ID",
            "Baby Facility State ID",
            "/nl:ClinicalDocument/nl:componentOf/nl:encompassingEncounter/nl:location/nl:healthCareFacility/nl:id"
            ));
      
      addDataElement(
         new PersonNameDataElement(
            getXPath(),
            "mName",
            "Mother's Name",
            "Name of the Mother",
            "/nl:ClinicalDocument/nl:recordTarget/nl:patientRole/nl:patient/nl:name"));
      
      addDataElement(
         new PersonNameDataElement(
            getXPath(),
            "kidName",
            "Kid's Name",
            "The legal name of the child as provided by the parents.",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']]/nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]/nl:subject/nl:name"
            ));
      
      addDataElement(
         new IdDataElement(
            getXPath(),
            "irecnum",
            "Infant's MRN",
            "The medical record number assigned to the newborn.",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']]/nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]/nl:id"
            ));

      addDataElement(
         new DataElement(
            getXPath(),
            "idob",
            "Infant Date of Birth",
            "The infant's date/time of birth",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']]/nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]/nl:subject/nl:birthTime/@value"
            ));
      
      addDataElement(
         new DataElement(
            getXPath(),
            "floc",
            "Birth City",
            "Place where birth occurred: Facility City/Town",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']]/nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]/nl:addr/nl:city/text()"
            ));
      
      addDataElement(
         new DataElement(
            getXPath(),
            "cname",
            "Birth County",
            "Place where birth occurred: Facility County",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']]/nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]/nl:addr/nl:county/text()"
            ));
      
      // BPLACE 
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>(
            getXPath(),
            "bplace$EventOutcomesObservations",
            "Event Outcomes Observation(s)",
            "Event Outcomes Observation(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.1.13.7']]/nl:entry/nl:observation",
            new ProcedureDataElement( 
               getXPath(),
               "bplace$EventOutcomesObservation",
               "Event Outcomes Observation: BPLACE",
               "Event Outcomes Observation", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new BplaceTrigger())
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "bplace",
            "Birth Place",
            "Place where birth occurred.",
            "IF ($EventOutcomesObservationCode CONTAINS ValueSet (Birthplace Setting (NCHS))) " +
                "THEN IF $EventOutcomesObservationValue CONTAINS ValueSet (Birth Place Hospital (NCHS)) THEN BPLACE SHALL = '1' " +
                "ELSE IF $EventOutcomesObservationValue CONTAINS ValueSet (Birth Place Freestanding Birthing Center (NCHS)) THEN BPLACE SHALL = '2' " +
                "ELSE IF $EventOutcomesObservationValue CONTAINS ValueSet (Birth Place Home Intended (NCHS)) THEN BPLACE SHALL = '3' " +
                "ELSE IF $EventOutcomesObservationValue CONTAINS ValueSet (Birth Place Home Unintended (NCHS)) THEN BPLACE SHALL = '4' " +
                "ELSE IF $EventOutcomesObservationValue CONTAINS ValueSet (Birth Place Home Unknown Intention (NCHS)) THEN BPLACE SHALL = '5' " + 
                "ELSE IF $EventOutcomesObservationValue CONTAINS ValueSet (Birth Place Clinic Office (NCHS)) THEN BPLACE SHALL = '6' " + 
                "ELSE BPLACE SHALL = '7' } " +
             "ELSE BPLACE SHALL = ''")
             .addMapping("ELSE IF", "} else if")
             .addMapping("BPLACE SHALL =", "")
             .addMapping("''", "'' }")
             .addMapping("$EventOutcomesObservationValue CONTAINS ValueSet", "")
             .addVariable(
                new Variable(getDataElementByName("bplace$EventOutcomesObservations"))
                .addMapping("$EventOutcomesObservationCode CONTAINS ValueSet (Birthplace Setting (NCHS))", "${bplace$EventOutcomesObservationsCount} > 0")
                .addMapping("Birth Place Hospital (NCHS)", "${Birth Place Hospital (NCHS)Contains} == true")
                .addMapping("Birth Place Freestanding Birthing Center (NCHS)", "${Birth Place Freestanding Birthing Center (NCHS)Contains} == true")
                .addMapping("Birth Place Home Intended (NCHS)", "${Birth Place Home Intended (NCHS)Contains} == true")
                .addMapping("Birth Place Home Unintended (NCHS)", "${Birth Place Home Unintended (NCHS)Contains} == true")
                .addMapping("Birth Place Home Unknown Intention (NCHS)", "${Birth Place Home Unknown Intention (NCHS)Contains} == true")
                .addMapping("Birth Place Clinic Office (NCHS)", "${Birth Place Clinic Office (NCHS)Contains} == true"))
         );
      
      // CHAM
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cham$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cham$ProblemCode",
               "Problem Code: Chlamydia",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Chlamydia (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cham$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cham$InfectionHistoryProblemCode",
               "Infection History Problem Code: Chlamydia",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Chlamydia (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "cham",
            "Chlamydia",
            "A positive test for Chlamydia trachomatis. ",
            "IF (($ProblemCode  CONTAINS ValueSet (Chlamydia (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Chlamydia (NCHS)))) " +
            "THEN 'CHAM' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'CHAM' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("cham$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Chlamydia (NCHS))", "${cham$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("cham$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Chlamydia (NCHS))", "${cham$InfectionHistoryProblemCodesCount} > 0"))
         );
      
      // GON
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "gon$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "gon$ProblemCode",
               "Problem Code: Gonorrhea",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Gonorrhea (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "gon$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "gon$InfectionHistoryProblemCode",
               "Infection History Problem Code: Gonorrhea",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE")
            )
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Gonorrhea (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "gon",
            "Gonorrhea",
            "A positive test/culture for Neisseria gonorrhea.",
            "IF (($ProblemCode  CONTAINS ValueSet (Gonorrhea (NCHS))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Gonorrhea (NCHS))) " +
            "THEN 'GON' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'GON' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("gon$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Gonorrhea (NCHS)", "${gon$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("gon$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Gonorrhea (NCHS)", "${gon$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // HEPB
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "hepb$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "hepb$ProblemCode",
               "Problem Code: Hepatitis B",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Hepatitis B (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "hepb$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "hepb$InfectionHistoryProblemCode",
               "Infection History Problem Code: Hepatitis B",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Hepatitis B (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "hepb",
            "Hepatitis B (HBV, serum hepatitis)",
            "A positive test for the hepatitis B virus.",
            "IF (($ProblemCode  CONTAINS ValueSet (Hepatitis B (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Hepatitis B (NCHS)))) " +
            "THEN 'HEPB' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'HEPB' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("hepb$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Hepatitis B (NCHS))", "${hepb$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("hepb$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Hepatitis B (NCHS))", "${hepb$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // HEPC
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "hepc$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "hepc$ProblemCode",
               "Problem Code: Hepatitis C",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Hepatitis C (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "hepc$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "hepc$InfectionHistoryProblemCode",
               "Infection History Problem Code: Hepatitis C",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Hepatitis C (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "hepc",
            "Hepatitis C (non-A, non-B hepatitis (HCV))",
            "A positive test for the hepatitis C virus.",
            "IF (($ProblemCode  CONTAINS ValueSet (Hepatitis C (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Hepatitis C (NCHS)))) " +
            "THEN 'HEPC' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'HEPC' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("hepc$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Hepatitis C (NCHS))", "${hepc$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("hepc$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Hepatitis C (NCHS))", "${hepc$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // SYPH
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "syph$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "syph$ProblemCode",
               "Problem Code: Syphilis",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Syphilis (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "syph$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "syph$InfectionHistoryProblemCode",
               "Infection History Problem Code: Syphilis",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Syphilis (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "syph",
            "Syphilis (also called lues) ",
            " positive test for Treponema pallidum.",
            "IF (($ProblemCode  CONTAINS ValueSet (Syphilis (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Syphilis (NCHS)))) " +
            "THEN 'SYPH' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'SYPH' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("syph$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Syphilis (NCHS))", "${syph$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("syph$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Syphilis (NCHS))", "${syph$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // LM
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "lm$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "lm$ProblemCode",
               "Problem Code: Listeria",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Listeria (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "lm$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "lm$InfectionHistoryProblemCode",
               "Infection History Problem Code: Listeria",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Listeria (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "lm",
            "Listeria",
            "A diagnosis of or positive test for Listeria monocytogenes.",
            "IF (($ProblemCode  CONTAINS ValueSet (Listeria (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Listeria (NCHS)))) " +
            "THEN 'LM' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'LM' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("lm$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Listeria (NCHS))", "${lm$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("lm$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Listeria (NCHS))", "${lm$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // GBS
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "gbs$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "gbs$ProblemCode",
               "Problem Code: Group B Streptococcus",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Group B Streptococcus (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "gbs$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "gbs$InfectionHistoryProblemCode",
               "Infection History Problem Code: Group B Streptococcus",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Group B Streptococcus (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "gbs",
            "Group B Streptococcus",
            "A diagnosis of or positive test for Streptococcus agalactiae or group B streptococcus.",
            "IF (($ProblemCode  CONTAINS ValueSet (Group B Streptococcus (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Group B Streptococcus (NCHS)))) " +
            "THEN 'GBS' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'GBS' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("gbs$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Group B Streptococcus (NCHS))", "${gbs$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("gbs$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Group B Streptococcus (NCHS))", "${gbs$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // CMV
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cmv$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cmv$ProblemCode",
               "Problem Code: Cytomegalovirus",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Cytomegalovirus (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cmv$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cmv$InfectionHistoryProblemCode",
               "Infection History Problem Code: Cytomegalovirus",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Cytomegalovirus (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "cmv",
            "Cytomegalovirus (CMV)",
            "A diagnosis of or positive test for Cytomegalovirus.",
            "IF (($ProblemCode  CONTAINS ValueSet (Cytomegalovirus (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Cytomegalovirus (NCHS)))) " +
            "THEN 'CMV' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'CMV' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("cmv$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Cytomegalovirus (NCHS))", "${cmv$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("cmv$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Cytomegalovirus (NCHS))", "${cmv$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // B19
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "b19$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "b19$ProblemCode",
               "Problem Code: Parvovirus",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Parvovirus (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "b19$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "b19$InfectionHistoryProblemCode",
               "Infection History Problem Code: Parvovirus",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Parvovirus (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "b19",
            "Parvovirus",
            "A diagnosis of or positive test for Parvovirus B19.",
            "IF (($ProblemCode  CONTAINS ValueSet (Parvovirus (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Parvovirus (NCHS)))) " +
            "THEN 'B19' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'B19' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("b19$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Parvovirus (NCHS))", "${b19$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("b19$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Parvovirus (NCHS))", "${b19$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // TOXO
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "toxo$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "toxo$ProblemCode",
               "Problem Code: Toxoplasmosis",
               "Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Toxoplasmosis (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "toxo$InfectionHistoryProblemCodes",
            "Infection History Problem Code(s)",
            "Infection History Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.16.2.1.1.1']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "toxo$InfectionHistoryProblemCode",
               "Infection History Problem Code: Toxoplasmosis",
               "Infection History Problem Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Toxoplasmosis (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "toxo",
            "Toxoplasmosis",
            "A diagnosis of or positive test for Toxoplasma gondii.",
            "IF (($ProblemCode  CONTAINS ValueSet (Toxoplasmosis (NCHS)))  OR " +
            "($InfectionHistoryProblemCode  CONTAINS ValueSet (Toxoplasmosis (NCHS)))) " +
            "THEN 'TOXO' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'TOXO' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("toxo$ProblemCodes"))
                  .addMapping("$ProblemCode CONTAINS ValueSet (Toxoplasmosis (NCHS))", "${toxo$ProblemCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("toxo$InfectionHistoryProblemCodes"))
                  .addMapping("$InfectionHistoryProblemCode  CONTAINS ValueSet (Toxoplasmosis (NCHS))", "${toxo$InfectionHistoryProblemCodesCount} > 0"))
            
         );
      
      // CDIP    ***** NOT TESTED *****
      
      // XPath for cdip$GeneralAppearanceObservationCodes not fully tested
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cdip$GeneralAppearanceObservationCodes",
            "General Appearance Observation Code(s)",
            "General Appearance Observation Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.16']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cdip$GeneralAppearanceObservationCode",
               "General Appearance Observation Code: CDIP",
               "General Appearance Observation Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Suspected Chromosomal Disorder (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );

      // XPath for cdip$ProcedureCodes not fully tested
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cdip$ProcedureCodes",
            "Procedure Code(s)",
            "Procedure Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure[@classCode='PROC' and @moodCode='INT']/nl:code",
            new CodedDataElement(
               getXPath(),
               "cdip$ProcedureCode",
               "Procedure Code: CDIP",
               "Procedure Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Karyotype Determination (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      // XPath for cdip$CodedResultCodes not fully tested
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cdip$CodedResultCodes",
            "Coded Result Code(s)",
            "Coded Result Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.1.13.7']]/nl:entry[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.13']]/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cdip$CodedResultCode",
               "Coded Result Code: CDIP",
               "Coded Result Code", "")
            .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new CodedDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Karyotype Result (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      addDataElement(
         new FunctionDataElement( 
            getXPath(),
            "cdip",
            "Suspected CD",
            "Suspected chromosomal disorder karyotype pending",
            "IF (($GeneralAppearanceObservationCode CONTAINS ValueSet (Suspected Chromosomal Disorder (NCHS)) AND " + 
            "($ProcedureCode Contains (Karyotype Determination (NCHS)) AND act classCode='ACT' moodCode='INT') AND " + 
            "(NOT $CodedResultCode (Karyotype Result (NCHS))) " + 
            "THEN 'CDIP' SHALL = 'Y' ELSE 'N'.")
            .addMapping("THEN 'CDIP' SHALL = 'Y' ELSE 'N'.", "{'Y'} else {'N'}")
            .addVariable(
               new Variable(getDataElementByName("cdip$GeneralAppearanceObservationCodes"))
                  .addMapping("$GeneralAppearanceObservationCode CONTAINS ValueSet (Suspected Chromosomal Disorder (NCHS)", "${cdip$GeneralAppearanceObservationCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("cdip$ProcedureCodes"))
                  .addMapping("$ProcedureCode Contains (Karyotype Determination (NCHS)) AND act classCode='ACT' moodCode='INT'", "${cdip$ProcedureCodesCount} > 0"))
            .addVariable(
               new Variable(getDataElementByName("cdip$CodedResultCodes"))
                  .addMapping("NOT $CodedResultCode (Karyotype Result (NCHS)", "${cdip$CodedResultCodesCount} == 0")
               ));
      
      // AVEN1

      addDataElement(
         new DataElement(
            getXPath(),
            "aven1$idob",
            "Infant Date of Birth",
            "The infant's date/time of birth",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']]/nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]/nl:subject/nl:birthTime/@value")
            .loadSnippet("DATAELEMENT_VARIABLE"));
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
            getXPath(),
            "aven1$ProcedureCodes",
            "Procedure(s)",
            "Procedure(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure",
            new ProcedureDataElement( 
               getXPath(),
               "aven1$ProcedureCode",
               "Procedure: AVEN1",
               "Procedure", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new Aven1Trigger())
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "aven1",
            "Manual Breaths",
            "Infant given manual breaths with bag and mask or bag and endotracheal tube within the first several minutes.",
            "IF ($ProcedureCode CONTAINS ValueSet (Assisted Ventilation (NCHS)) AND " +
            "(($ProcedureStartTime - $BirthTime) < 5 minutes)) THEN AVEN1 SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN AVEN1 SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("aven1$idob")))
            .addVariable(new Variable(getDataElementByName("aven1$ProcedureCodes"))
               .addMapping("$ProcedureCode CONTAINS ValueSet (Assisted Ventilation (NCHS)) AND (($ProcedureStartTime - $BirthTime) < 5 minutes)", "${aven1$ProcedureCodesCount} > 0"))
      );
      
      // AVEN6
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
            getXPath(),
            "aven6$ProcedureCodes",
            "Procedure(s)",
            "Procedure(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure",
            new ProcedureDataElement( 
               getXPath(),
               "aven6$ProcedureCode",
               "Procedure: AVEN6",
               "Procedure", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new Aven6Trigger())
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "aven6",
            "Artificial Ventilation",
            "Infant given breathing assistance for more than 6 hours.",
            "IF (($ProcedureCode CONTAINS ValueSet (Assisted Ventilation (NCHS)) AND " + 
            "(($ProcedureEndTime – $ProcedureStartTime) >=6 hours)) THEN AVEN6 SHALL = 'Y' " + 
            "ELSE 'N'")
            .addMapping("THEN AVEN6 SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("aven6$ProcedureCodes"))
               .addMapping("($ProcedureCode CONTAINS ValueSet (Assisted Ventilation (NCHS)) AND (($ProcedureEndTime – $ProcedureStartTime) >=6 hours)", "${aven6$ProcedureCodesCount} > 0"))
      );
      
      // BINJ
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "binj$EventOutcomesObservationCodes",
            "Problem Observation(s)",
            "Problem Observation(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.1.13.7']]/nl:entry/nl:observation[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.5']]/nl:value",
            new CodedDataElement(
               getXPath(),
               "binj$EventOutcomesObservationCode",
               "Problem Observation: BINJ",
               "Problem Observation", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Significant Birth Injury (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "binj",
            "Birth Injury",
            "Significant Birth Injury which requires intervention.",
            "IF ($EventOutcomesObservationCode CONTAINS ValueSet (Significant Birth Injury (NCHS))) THEN BINJ SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN BINJ SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("binj$EventOutcomesObservationCodes"))
               .addMapping("$EventOutcomesObservationCode CONTAINS ValueSet (Significant Birth Injury (NCHS))","${binj$EventOutcomesObservationCodesCount} > 0"))
         );
      
      // SEIZ
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "seiz$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "seiz$ProblemCode",
               "Problem Code: SEIZ",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Seizure or Serious Neurologic Dysfunction (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "seiz",
            "Seizure",
            "Seizure.",
            "IF ($ProblemCode CONTAINS ValueSet (Seizure or Serious Neurologic Dysfunction (NCHS))) THEN SEIZ SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN SEIZ SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("seiz$ProblemCodes"))
               .addMapping("$ProblemCode CONTAINS ValueSet (Seizure or Serious Neurologic Dysfunction (NCHS))","${seiz$ProblemCodesCount} > 0"))
         );
      
      // SURF   ***** NOT TESTED *****
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(   // XPath expression not tested
            getXPath(),
            "surf$CodedProductNames",
            "Coded Product Name(s)",
            "Coded Product Name(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.21']]/nl:entry/nl:substanceAdministration/nl:consumable/nl:manufacturedProduct/nl:labeledDrug/nl:code",
            new CodedDataElement(
               getXPath(),
               "surf$CodedProductName",
               "Coded Product Name: SURF",
               "Coded Product Name", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Newborn Given Surfactant Replacement (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(   // XPath expression not tested
            getXPath(),
            "surf$ProcedureCodes",
            "Procedure Code(s)",
            "Procedure Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure/nl:code",
            new CodedDataElement(
               getXPath(),
               "surf$ProcedureCode",
               "Procedure Code: SURF",
               "Procedure Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Surfactant Replacement Therapy (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "surf",
            "Surfactant Therapy",
            "Surfactant Replacement Therapy given.",
            "IF ($CodedProductName CONTAINS ValueSet (Newborn Receiving Surfactant Replacement Therapy (NCHS)) " + 
            "OR $ProcedureCode CONTAINS ValueSet (Surfactant Replacement Therapy (NCHS)))" + 
            ", THEN SURF SHALL = 'Y' ELSE 'N'")
            .addMapping(", THEN SURF SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("surf$CodedProductNames"))
               .addMapping("$CodedProductName CONTAINS ValueSet (Newborn Receiving Surfactant Replacement Therapy (NCHS))","(${surf$CodedProductNamesCount} > 0)"))
            .addVariable(new Variable(getDataElementByName("surf$ProcedureCodes"))
               .addMapping("$ProcedureCode CONTAINS ValueSet (Surfactant Replacement Therapy (NCHS))","(${surf$ProcedureCodesCount} > 0)"))
         );
      
      // APGAR5
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
            getXPath(),
            "apgar5$GeneralAppearanceObservations",
            "APGAR5 Score(s)",
            "APGAR5 Score(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.16']]/nl:entry/nl:observation",
            new ProcedureDataElement( 
               getXPath(),
               "apgar5$GeneralAppearanceObservation",
               "APGAR5 Score",
               "APGAR5 Score", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("5 Min Apgar Score (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "apgar5",
            "APGAR5 Score",
            "APGAR5 Score",
            "IF ($GeneralAppearanceObservationCode CONTAINS ValueSet (5 Min Apgar Score (NCHS))), THEN 'APGAR5' = ($GeneralAppearanceObservationValue)")
            .addMapping(", THEN 'APGAR5' = ($GeneralAppearanceObservationValue)", "{'$GeneralAppearanceObservationValue'} else {'None'}")
            .addVariable(new Variable(getDataElementByName("apgar5$GeneralAppearanceObservations"))
               .addMapping("$GeneralAppearanceObservationCode CONTAINS ValueSet (5 Min Apgar Score (NCHS))", "${apgar5$GeneralAppearanceObservationsCount} > 0")
               .addMapping("$GeneralAppearanceObservationValue", "${apgar5$GeneralAppearanceObservation-valuevalue}"))
      );
      
      // APGAR10
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
            getXPath(),
            "apgar10$GeneralAppearanceObservations",
            "APGAR10 Score(s)",
            "APGAR10 Score(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.16']]/nl:entry/nl:observation",
            new ProcedureDataElement( 
               getXPath(),
               "apgar10$GeneralAppearanceObservation",
               "APGAR10 Score",
               "APGAR10 Score", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("10 Min Apgar Score (NCHS)")))
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "apgar10",
            "APGAR10 Score",
            "APGAR10 Score",
            "IF ('APGAR5' <6), AND ($GeneralAppearanceObservationCode CONTAINS ValueSet (10 Min Apgar Score (NCHS)), THEN 'APGAR10' = ($GeneralAppearanceObservationValue)")
            .addMapping("'APGAR5' <6), AND (", "")
            .addMapping(", THEN 'APGAR10' = ($GeneralAppearanceObservationValue)", "{'$GeneralAppearanceObservationValue'} else {''}")
            .addVariable(new Variable(getDataElementByName("apgar10$GeneralAppearanceObservations"))
               .addMapping("$GeneralAppearanceObservationCode CONTAINS ValueSet (10 Min Apgar Score (NCHS)", "${apgar10$GeneralAppearanceObservationsCount} > 0")
               .addMapping("$GeneralAppearanceObservationValue", "${apgar10$GeneralAppearanceObservation-valuevalue}"))
      );
      
      // BWG
      
      addDataElement( 
         new SequenceDataElement <ProcedureDataElement>( 
            getXPath(),
            "bwg$VitalSigns",
            "Vital Sign(s)",
            "Vital Sign(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.5.3.2']]/nl:entry/nl:organizer/nl:component/nl:observation",
            new ProcedureDataElement( 
               getXPath(),
               "bwg$VitalSign",
               "Vital Sign",
               "Vital Sign", "")
            .loadSnippet("PROCEDUREDATAELEMENT_SEQUENCE_VARIABLE"))
            .setCopy(new ProcedureDataElement.Copy())
            .setTrigger(new BirthWeightTrigger())
            .setSequenceType(SequenceDataElement.SequenceType.ANY)
       );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "bwg",
            "Birth Weight grams",
            "Birth Weight grams",
            "IF (($VitalSignsTypeCode CONTAINS ValueSet (Body Weight (NCHS))) AND " + 
            "($VitalSignsMethodCode CONTAINS ValueSet (Birth Weight (NCHS)))), " + 
            "THEN 'BWG' SHALL = $VitalSignsResultValue WHERE Result Value Units are expressed in grams")
            .addMapping("WHERE Result Value Units are expressed in grams", "")
            .addMapping(", THEN 'BWG' SHALL = $VitalSignsResultValue", "{'$VitalSignsResultValue'} else {''}")
            .addVariable(new Variable(getDataElementByName("bwg$VitalSigns"))
               .addMapping("($VitalSignsTypeCode CONTAINS ValueSet (Body Weight (NCHS))) AND ($VitalSignsMethodCode CONTAINS ValueSet (Birth Weight (NCHS)))", "${bwg$VitalSignsCount} > 0")
               .addMapping("$VitalSignsResultValue", "${bwg$VitalSign-valuevalue}"))
      );
      
      // INDL
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "indl$ProcedureCodes",
            "Procedure Code(s)",
            "Procedure Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.3']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure/nl:code",
            new CodedDataElement(
               getXPath(),
               "indl$Procedure Code",
               "Procedure Code: INDL",
               "Procedure Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Induction of Labor (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "indl",
            "Induction of Labor",
            "Initiation of uterine contractions by medical and/or surgical means.",
            "IF (Labor and Delivery Summary Labor and Delivery Procedures and Interventions Procedure Code CONTAINS ValueSet (Induction of Labor (NCHS)) THEN 'INDL' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'INDL' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("indl$ProcedureCodes"))
               .addMapping("Labor and Delivery Summary Labor and Delivery Procedures and Interventions Procedure Code CONTAINS ValueSet (Induction of Labor (NCHS)","${indl$ProcedureCodesCount} > 0"))
         );
      
      // STER
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "ster$ProcedureCodes",
            "Procedure Code(s)",
            "Procedure Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.3']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.13.2.11']]/nl:entry/nl:procedure/nl:code",
            new CodedDataElement(
               getXPath(),
               "ster$Procedure Code",
               "Procedure Code: STER",
               "Procedure Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Steroids For Fetal Lung Maturation (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "ster",
            "Steroids",
            "Steroids for fetal lung maturation.",
            "IF ($ProcedureCode CONTAINS ValueSet (Steroids For Fetal Lung Maturation (NCHS))) THEN 'STER' SHALL ='Y'ELSE 'N'")
            .addMapping("THEN 'STER' SHALL ='Y'ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("ster$ProcedureCodes"))
               .addMapping("$ProcedureCode CONTAINS ValueSet (Steroids For Fetal Lung Maturation (NCHS))","${ster$ProcedureCodesCount} > 0"))
         );
      
      // BFED
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "bfed$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "bfed$ProblemCode",
               "Problem Code: BFED",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Breastfed Infant (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "bfed",
            "Breast Fed",
            "Was the infant breast fed between birth and discharge.",
            "IF ($ProblemCode CONTAINS ValueSet (Breastfed Infant (NCHS))) THEN BFED SHALL be 'Y' ELSE 'N'")
            .addMapping("THEN BFED SHALL be 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("bfed$ProblemCodes"))
               .addMapping("$ProblemCode CONTAINS ValueSet (Breastfed Infant (NCHS))","${bfed$ProblemCodesCount} > 0"))
         );
      
      // ILIV
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "iliv$ProblemObservationCodes",
            "Problem Observation(s)",
            "Problem Observation(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.7.3.1.1.13.7']]/nl:entry/nl:observation[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.4.5']]/nl:value",
            new CodedDataElement(
               getXPath(),
               "iliv$ProblemObservationCode",
               "Problem Observation: ILIV",
               "Problem Observation", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Neonatal Death (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new DataElement(
            getXPath(),
            "iliv$DeceasedIndicator",
            "Deceased Indicator",
            "Infant is deceased?",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']]/nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]/nl:subject/stdc:deceasedInd/text()")
            .loadSnippet("DATAELEMENT_VARIABLE")
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "iliv",
            "Infant living",
            "Is infant living at time of report?",
            "IF (NOT ($ProblemObservationCode CONTAINS ValueSet(Neonatal Death (NCHS)) " + 
            "OR ($DeceasedIndicator = 'True'))) THEN 'ILIV' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'ILIV' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("iliv$DeceasedIndicator"))
               .addMapping("$DeceasedIndicator = 'True'", "iliv$DeceasedIndicatorValue == 'true' "))
            .addVariable(new Variable(getDataElementByName("iliv$ProblemObservationCodes"))
               .addMapping("$ProblemObservationCode CONTAINS ValueSet(Neonatal Death (NCHS))","${iliv$ProblemObservationCodesCount} > 0"))
         );
      
      // ANEN
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "anen$NervousSystemObservationCodes",
            "Nervous System Observation(s)",
            "Nervous System Observation(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.35']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "anen$NervousSystemObservationCode",
               "Nervous System Observation: ANEN",
               "Nervous System Observation", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Anencephaly of the Newborn (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "anen$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "anen$ProblemCode",
               "Problem Code: ANEN",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Anencephaly of the Newborn (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "anen",
            "Anencephaly",
            "Partial or complete absence of the brain and skull.",
            "IF (($NervousSystemObservationCode CONTAINS ValueSet (Anencephaly of the Newborn (NCHS))) " + 
            "OR ($ProblemCode CONTAINS ValueSet (Anencephaly of the Newborn (NCHS)))) THEN 'ANEN' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'ANEN' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("anen$NervousSystemObservationCodes"))
                .addMapping("$NervousSystemObservationCode CONTAINS ValueSet (Anencephaly of the Newborn (NCHS))","${anen$NervousSystemObservationCodesCount} > 0"))
            .addVariable(new Variable(getDataElementByName("anen$ProblemCodes"))
               .addMapping("$ProblemCode CONTAINS ValueSet (Anencephaly of the Newborn (NCHS))","${anen$ProblemCodesCount} > 0"))
         );
      
      // CL
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "cl$GeneralAppearanceObservationCodes",
            "General Appearance Observation Code(s)",
            "General Appearance Observation Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.16']]/nl:entry/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cl$GeneralAppearanceObservationCode",
               "General Appearance Observation Code: CL",
               "General Appearance Observation Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Cleft Lip with or without Cleft Palate (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cl$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cl$ProblemCode",
               "Problem Code: CL",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Cleft Lip with or without Cleft Palate (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "cl",
            "Cleft Lip",
            "Incomplete closure of the lip.",
            "IF (($GeneralAppearanceObservationCode CONTAINS ValueSet (Cleft Lip with or without Cleft Palate (NCHS))) " + 
            "OR ($ProblemCode CONTAINS ValueSet (Cleft Lip with or without Cleft Palate (NCHS)))) 'CL' SHALL = 'Y' ELSE 'N'")
            .addMapping("'CL' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("cl$GeneralAppearanceObservationCodes"))
               .addMapping("$GeneralAppearanceObservationCode CONTAINS ValueSet (Cleft Lip with or without Cleft Palate (NCHS))","${cl$GeneralAppearanceObservationCodesCount} > 0"))
            .addVariable(new Variable(getDataElementByName("cl$ProblemCodes"))
               .addMapping("$ProblemCode CONTAINS ValueSet (Cleft Lip with or without Cleft Palate (NCHS))","${cl$ProblemCodesCount} > 0"))
         );
      
      // CP
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "cp$GeneralAppearanceObservationCodes",
            "General Appearance Observation Code(s)",
            "General Appearance Observation Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.16']]/nl:entry/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cp$GeneralAppearanceObservationCode",
               "General Appearance Observation Code: CP",
               "General Appearance Observation Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Cleft Palate Alone (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "cp$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "cp$ProblemCode",
               "Problem Code: CP",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Cleft Palate Alone (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "cp",
            "Cleft Palate",
            "Incomplete fusion of the palatal shelves.",
            "IF (($GeneralAppearanceObservationCode CONTAINS ValueSet (Cleft Palate Alone (NCHS))) " +
            "OR ($ProblemCode CONTAINS ValueSet (Cleft Palate Alone (NCHS)))) THEN 'CP' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'CP' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("cp$GeneralAppearanceObservationCodes"))
               .addMapping("$GeneralAppearanceObservationCode CONTAINS ValueSet (Cleft Palate Alone (NCHS))","${cp$GeneralAppearanceObservationCodesCount} > 0"))
            .addVariable(new Variable(getDataElementByName("cp$ProblemCodes"))
                .addMapping("$ProblemCode CONTAINS ValueSet (Cleft Palate Alone (NCHS))","${cp$ProblemCodesCount} > 0"))
         );

      // LIMB
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "limb$MusculoskeletalObservationCodes",
            "Musculoskeletal Observation Code(s)",
            "Musculoskeletal Observation Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.34']]/nl:entry/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "limb$MusculoskeletalObservationCode",
               "Musculoskeletal Observation Code: LIMB",
               "Musculoskeletal Observation Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Limb Reduction Defect (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "limb$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "limb$ProblemCode",
               "Problem Code: LIMB",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Limb Reduction Defect (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "limb",
            "Limb reduction defect",
            "Complete or partial absence of a portion of an extremity secondary to failure to develop.",
            "IF (($MusculoskeletalObservationCode CONTAINS ValueSet (Limb Reduction Defect (NCHS))) " + 
            "OR ($ProblemCode CONTAINS ValueSet (Limb Reduction Defect (NCHS)))) THEN 'LIMB' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'LIMB' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("limb$MusculoskeletalObservationCodes"))
               .addMapping("$MusculoskeletalObservationCode CONTAINS ValueSet (Limb Reduction Defect (NCHS))","${limb$MusculoskeletalObservationCodesCount} > 0"))
            .addVariable(new Variable(getDataElementByName("limb$ProblemCodes"))
                    .addMapping("$ProblemCode CONTAINS ValueSet (Limb Reduction Defect (NCHS))","${limb$ProblemCodesCount} > 0"))
         );

      // MNSB
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "mnsb$NeurologicSystemObservationCodes",
            "Neurologic System Observation Code(s)",
            "Neurologic System Observation Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.35']]/nl:entry/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "mnsb$NeurologicSystemObservationCode",
               "Neurologic System Observation Code: MNSB",
               "Neurologic System Observation Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Meningomyelocele/Spina Bifida - Newborn (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "mnsb$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "mnsb$ProblemCode",
               "Problem Code: MNSB",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Meningomyelocele/Spina Bifida - Newborn (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "mnsb",
            "Spina bifida",
            "Herniation of the meninges and/or spinal cord tissue through a bony defect of spine closure.",
            "IF (($NeurologicSystemObservationCode CONTAINS ValueSet (Meningomyelocele/Spina Bifida - Newborn (NCHS))) " + 
            "OR ($ProblemCode CONTAINS ValueSet (Meningomyelocele/Spina Bifida - Newborn (NCHS)))) THEN 'MNSB' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'MNSB' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("mnsb$NeurologicSystemObservationCodes"))
               .addMapping("$NeurologicSystemObservationCode CONTAINS ValueSet (Meningomyelocele/Spina Bifida - Newborn (NCHS))","${mnsb$NeurologicSystemObservationCodesCount} > 0"))
            .addVariable(new Variable(getDataElementByName("mnsb$ProblemCodes"))
                .addMapping("$ProblemCode CONTAINS ValueSet (Meningomyelocele/Spina Bifida - Newborn (NCHS))","${mnsb$ProblemCodesCount} > 0"))
         );

      // OMPH
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "omph$AbdomenObservationCodes",
            "Abdomen Observation Code(s)",
            "Abdomen Observation Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject[nl:code[@code='NCHILD']]]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.31']]/nl:entry/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "omph$AbdomenObservationCode",
               "Abdomen Observation Code: OMPH",
               "Abdomen Observation Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Omphalocele of the Newborn (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "omph$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "omph$ProblemCode",
               "Problem Code: OMPH",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Omphalocele of the Newborn (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "omph",
            "Omphalocele",
            "A defect in the anterior abdominal wall, accompanied by herniation of some abdominal organs through a widened umbilical ring into the umbilical stalk.",
            "IF (($AbdomenObservationCode CONTAINS ValueSet (Omphalocele of the Newborn (NCHS))) " + 
            "OR ($ProblemCode CONTAINS ValueSet (Omphalocele of the Newborn (NCHS)))) THEN 'OMPH' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'OMPH' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("omph$AbdomenObservationCodes"))
               .addMapping("$AbdomenObservationCode CONTAINS ValueSet (Omphalocele of the Newborn (NCHS))","${omph$AbdomenObservationCodesCount} > 0"))
            .addVariable(new Variable(getDataElementByName("omph$ProblemCodes"))
               .addMapping("$ProblemCode CONTAINS ValueSet (Omphalocele of the Newborn (NCHS))","${omph$ProblemCodesCount} > 0"))
         );

      // HYPO
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(  
            getXPath(),
            "hypo$RenoGenitaliaObservationCodes",
            "RenoGenitalia Observation Code(s)",
            "RenoGenitalia Observation Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.15.1']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.9.36']]/nl:entry/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "hypo$RenoGenitaliaObservationCode",
               "RenoGenitalia Observation Code: HYPO",
               "RenoGenitalia Observation Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Hypospadias (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new SequenceDataElement <CodedDataElement>(
            getXPath(),
            "hypo$ProblemCodes",
            "Problem Code(s)",
            "Problem Code(s)",
            "/nl:ClinicalDocument/nl:component/nl:structuredBody/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.1.21.2.4']][nl:subject/nl:relatedSubject/nl:code[@code='NCHILD']]/nl:component/nl:section[nl:templateId[@root='1.3.6.1.4.1.19376.1.5.3.1.3.6']]/nl:entry/nl:act/nl:entryRelationship/nl:observation/nl:value",
            new CodedDataElement(
               getXPath(),
               "hypo$ProblemCode",
               "Problem Code: HYPO",
               "Problem Code", "")
               .loadSnippet("CODEDDATAELEMENT_SEQUENCE_VARIABLE"))
               .setCopy(new CodedDataElement.Copy())
               .setTrigger(new InValueSetTrigger(ValueSet.getValueSetForName("Hypospadias (NCHS)")))
               .setSequenceType(SequenceDataElement.SequenceType.ANY)
         );
      
      addDataElement(
         new FunctionDataElement(
            getXPath(),
            "hypo",
            "Hypospadias",
            "A defect in the anterior abdominal wall, accompanied by herniation of some abdominal organs through a widened umbilical ring into the umbilical stalk.",
            "IF (($RenoGenitaliaObservationCode CONTAINS ValueSet (Hypospadias (NCHS))) " + 
            "OR ($ProblemCode CONTAINS ValueSet (Hypospadias (NCHS)))) THEN 'HYPO' SHALL = 'Y' ELSE 'N'")
            .addMapping("THEN 'HYPO' SHALL = 'Y' ELSE 'N'", "{'Y'} else {'N'}")
            .addVariable(new Variable(getDataElementByName("hypo$RenoGenitaliaObservationCodes"))
               .addMapping("$RenoGenitaliaObservationCode CONTAINS ValueSet (Hypospadias (NCHS))","${hypo$RenoGenitaliaObservationCodesCount} > 0"))
            .addVariable(new Variable(getDataElementByName("hypo$ProblemCodes"))
               .addMapping("$ProblemCode CONTAINS ValueSet (Hypospadias (NCHS))","${hypo$ProblemCodesCount} > 0"))
         );
      
      
      
      // @formatter:on
   } catch (Exception e) {
      Util.exit(Util.getEM(e));
   }
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
    * Trigger for Aven1. Checks for Procedure matching value set and starting
    * within five minutes of birthtime
    */
   public class Aven1Trigger extends InValueSetTrigger {
      private static final long serialVersionUID = 1L;

      /**
       * Create Aven1Trigger instance
       * 
       * @throws Exception on error (value set not found)
       */
      public Aven1Trigger() throws Exception {
         super(ValueSet.getValueSetForName("Assisted Ventilation (NCHS)"));
      }

      @Override
      public boolean trigger(DataElement dataElement, StringBuilder msg) {
         if ((dataElement instanceof ProcedureDataElement) == false) {
            log.warn("Can only run Aven1Trigger on Procedure data elements: "
               + dataElement.getParName());
            return false;
         }
         ProcedureDataElement pde = (ProcedureDataElement) dataElement;
         if (super.trigger(pde, msg) == false) return false;
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
         try {
            Date btime = sdf.parse(getDataElementByName("idob").getValue());
            Date stime = sdf.parse(pde.getInterval().getValue());
            long i = stime.getTime() - btime.getTime();
            if (i < (1000 * 60 * 5)) return true;
            return false;
         } catch (Exception e) {
            log.warn("Aven1Trigger error " + Util.getEM(e));
            return false;
         }
      }

   } // EO Aven1 trigger

   /**
    * Trigger for Aven6. Checks for Procedure matching value set and lasting
    * over 6 hours
    */
   public class Aven6Trigger extends InValueSetTrigger {
      private static final long serialVersionUID = 1L;

      /**
       * Create Aven1Trigger instance
       * 
       * @throws Exception on error (value set not found)
       */
      public Aven6Trigger() throws Exception {
         super(ValueSet.getValueSetForName("Assisted Ventilation (NCHS)"));
      }

      @Override
      public boolean trigger(DataElement dataElement, StringBuilder msg) {
         if ((dataElement instanceof ProcedureDataElement) == false) {
            log.warn("Can only run Aven6Trigger on Procedure data elements: "
               + dataElement.getParName());
            return false;
         }
         ProcedureDataElement pde = (ProcedureDataElement) dataElement;
         if (super.trigger(pde, msg) == false) return false;
         SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
         try {
            Date stime = sdf.parse(pde.getInterval().getValue());
            Date etime = sdf.parse(pde.getInterval().getHighValue());
            long i = etime.getTime() - stime.getTime();
            if (i > (1000 * 60 * 60 * 6)) return true;
            return false;
         } catch (Exception e) {
            log.warn("Aven6Trigger error " + Util.getEM(e));
            return false;
         }
      }

   } // EO Aven6 trigger

   /**
    * Trigger for BPLACE. Checks for code and finds out which Value Set value is
    * in.
    */
   public class BplaceTrigger extends InValueSetTrigger {
      private static final long serialVersionUID = 1L;

      private List <MutablePair <ValueSet, Boolean>> sets;

      /**
       * instantiates a BplaceTrigger 
       * @throws Exception on error, for example an unknown Value Set name
       */
      public BplaceTrigger() throws Exception {
         super(ValueSet.getValueSetForName("Birthplace Setting (NCHS)"));
         sets = new ArrayList <>();
        // @formatter:off
        sets.add(new MutablePair<ValueSet, Boolean>(ValueSet.getValueSetForName("Birth Place Hospital (NCHS)"), false));
        sets.add(new MutablePair<ValueSet, Boolean>(ValueSet.getValueSetForName("Birth Place Home Intended (NCHS)"), false));
        sets.add(new MutablePair<ValueSet, Boolean>(ValueSet.getValueSetForName("Birth Place Home Unintended (NCHS)"), false));
        sets.add(new MutablePair<ValueSet, Boolean>(ValueSet.getValueSetForName("Birth Place Home Unknown Intention (NCHS)"), false));
        sets.add(new MutablePair<ValueSet, Boolean>(ValueSet.getValueSetForName("Birth Place Clinic Office (NCHS)"), false));
        sets.add(new MutablePair<ValueSet, Boolean>(ValueSet.getValueSetForName("Birth Place Freestanding Birthing Center (NCHS)"), false));
        // @formatter:on

      }

      @Override
      public boolean trigger(DataElement dataElement, StringBuilder msg) {
         if ((dataElement instanceof ProcedureDataElement) == false) {
            log.warn("Can only run BplaceTrigger on Procedure data elements: "
               + dataElement.getParName());
            return false;
         }
         ProcedureDataElement pde = (ProcedureDataElement) dataElement;
         if (super.trigger(pde, msg) == false) return false;
         String obsValue = pde.getObservationValue().getAttribute("code");
         for (MutablePair <ValueSet, Boolean> set : sets) {
            ValueSet vs = set.getLeft();
            if (vs.isCodeInValueSet(obsValue) == true) {
               set.setRight(true);
               break;
            }
         }
         return true;
      }

      /**
       * sets a parameter substitution for each of the extra value sets:
       * <ul>
       * <li>Name = Value Set name with "Contains" appended, for example:
       * "Birth Place Hospital (NCHS)Contains".</li>
       * <li/>Value is true or false, depending on whether the value code was
       * contained in this value set.</li>
       * </ul>
       */
      @Override
      public void plug(CDAPlug plug) {
         for (MutablePair <ValueSet, Boolean> set : sets) {
            ValueSet vs = set.getLeft();
            plug.set(vs.getName() + "Contains", set.getRight().toString());
         }
         return;
      }
   } // EO BplaceTrigger
   
   
   /**
    * Trigger class for Procedures which need to match both code and methodCode.
    */
   public class BirthWeightTrigger extends InValueSetTrigger {
      private static final long serialVersionUID = 1L;
      
      private ValueSet mcvs = ValueSet.getValueSetForName("Birth Weight (NCHS)");
      
      /**
       * @throws Exception on error, such as missing ValueSet name.
       */
      public BirthWeightTrigger() throws Exception {
         super(ValueSet.getValueSetForName("Body Weight (NCHS)"));
      }
      
      /**
       * trigger checks that Procedure code is in "Body Weight (NCHS)" and
       * Procedure methodCode is in "Birth Weight (NCHS)". Triggers if both are
       * true.
       */
      @Override
      public boolean trigger(DataElement dataElement, StringBuilder msg) {
         if ((dataElement instanceof ProcedureDataElement) == false) {
            log.warn("Can only run BplaceTrigger on Procedure data elements: "
               + dataElement.getParName());
            return false;
         }
         ProcedureDataElement pde = (ProcedureDataElement) dataElement;
         if (super.trigger(pde, msg) == false) return false;
         CodedDataElement mc = pde.getMethodCode();
         return mcvs.isCodeInValueSet(mc.getCode(), mc.getCodeSystem());
      }
      
   } // EO BirthWeightTrigger 

} // EO LDS class
