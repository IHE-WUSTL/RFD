/*******************************************************************************
 * Copyright (c) 2014 Washington University in St. Louis All rights reserved.
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
package edu.wustl.mir.erl.ihe.rfd.tests.FF.RFD_0_10000;

import ihe.iti.rfd._2007.AnyXMLContentType;
import ihe.iti.rfd._2007.FormDataType;
import ihe.iti.rfd._2007.RetrieveFormRequestType;
import ihe.iti.rfd._2007.RetrieveFormResponseType;
import ihe.iti.rfd._2007.WorkflowDataType;

import java.nio.file.Path;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.rfd.servers.FormManagerTest;
import edu.wustl.mir.erl.ihe.server.FileServer;
import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;
import edu.wustl.mir.erl.ihe.util.TestFailException;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.Util.PfnType;
import edu.wustl.mir.erl.ihe.util.XmlUtil;
import edu.wustl.mir.erl.ihe.ws.server.WSServer;

/**
 * <p>RFD FormManagerSimulator WS implementation class for IHE test FormFiller 
 * RFD 0-10000.
 * </p>
 * This test requires that the Form Filler initiate an ITI-34 transaction with
 * the following requirements:
 * <ol>
 * <li>Pre-population data is null</li>
 * <li>workflowData.formID: RFD 0-10000-FF</li>
 * <li>workflowData.encodedResponse: false, meaning the Form Filler is
 * requesting the URL of a form that can be opened in a web browser</li>
 * <li>workflowData.archiveURL: empty string</li>
 * <li>workflowData.context: null</li>
 * <li>workflowData.instanceID: null</li>
 * </ol>
 * <b>Sample Message:</b>
 * 
 * <pre>
 * {@code
 * <soap:Envelope 
 *    xmlns:soap="http://www.w3.org/2003/05/soap-envelope" 
 *    xmlns:wsa="http://www.w3.org/2005/08/addressing"
 *    xmlns:urn="urn:ihe:iti:rfd:2007">
 * <soap:Header>
 *    <wsa:To>http://localhost:4040/axis2/services/someservice</wsa:To> 
 *    <wsa:MessageID>urn:uuid:76A2C3D9BCD3AECFF31217932910053</wsa:MessageID> 
 *    <wsa:Action soap:mustUnderstand="1">urn:ihe:iti:2007:RetrieveForm</wsa:Action>
 * </soap:Header>
 * <soap:Body>
 *    <urn:RetrieveFormRequest>
 *       <urn:prepopData/>
 *       <urn:workflowData>
 *          <urn:formID>RFD 0-10000-FF</urn:formID>
 *          <urn:encodedResponse>false</urn:encodedResponse>
 *          <urn:context/>
 *          <urn:instanceID/>
 *       </urn:workflowData>
 *    </urn:RetrieveFormRequest>
 * </soap:Body>
 * </soap:Envelope>
 * }
 * </pre>
 * 
 * <b>Sample Response:</b>
 * 
 * <pre>
 * {@code
 * <soap:Envelope 
 *    xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
 *    xmlns:wsa="http://www.w3.org/2005/08/addressing"
 *    xmlns:urn="urn:ihe:iti:rfd:2007">
 *       <soap:Header>
 *          <wsa:To>http://www.w3.org/2005/08/addressing/anonymous</To>
 *          <wsa:Action>urn:ihe:iti:2007:RetrieveFormResponse</Action>
 *          <wsa:MessageID>urn:uuid:491a84ae-d83c-48ba-8d0b-2d092a671c93</MessageID>
 *          <wsa:RelatesTo>urn:uuid:76A2C3D9BCD3AECFF31217932910053</RelatesTo>
 *       </soap:Header>
 *       <soap:Body>
 *          <urn:RetrieveFormResponse>
 *             <urn:form>
 *                <urn:URL>http://localhost:3300/RFD_0_10000_FF/Form.xml</URL>
 *             </urn:form>
 *          </urn:RetrieveFormResponse>
 *       </soap:Body>
 *    </soap:Envelope>
 * }
 * </pre>
 * 
 * <b>Sample Fault Response:</b>
 * 
 * <pre>
 * {@code
 * <soap:Envelope 
 *    xmlns:soap="http://www.w3.org/2003/05/soap-envelope"
 *    xmlns:wsa="http://www.w3.org/2005/08/addressing"
 *    xmlns:ns3="http://www.w3.org/2003/05/soap-envelope">
 *       <soap:Header>
 *          <wsa:To>http://www.w3.org/2005/08/addressing/anonymous</To>
 *          <wsa:Action>http://www.w3.org/2005/08/addressing/fault</Action>
 *          <wsa:MessageID>uuid:2841f4b4-48ed-4519-a8d9-6d623d089f47</MessageID>
 *          <wsa:RelatesTo>urn:uuid:76A2C3D9BCD3AECFF31217932910053</RelatesTo>
 *       </soap:Header>
 *       <soap:Body>
 *          <ns3:Fault>
 *             <ns3:Code>
 *                <ns3:Value>ns3:Sender</ns3:Value>
 *             </ns3:Code>
 *             <ns3:Reason>
 *                <ns3:Text xml:lang="en">Unknown FormID</ns3:Text>
 *             </ns3:Reason>
 *             <ns3:Detail>
 *                <emDetail>formID must be RFD 0-1000 for this test</emDetail>
 *             </ns3:Detail>
 *          </ns3:Fault>
 *       </soap:Body>
 *    </soap:Envelope>
 * }
 * </pre>
 * 
 * Based on the class
 * {@link ihe.iti.rfd._2007.FormManager_Service_FormManager_Port_Soap12Impl
 * FormManager_Port_SOAP12}, which was generated by JAX-WS RI, version JAX-WS RI
 * 2.2.8. Generated source version: 2.2.
 */

public class FormManager extends FormManagerTest {
   private static final long serialVersionUID = 1L;

   private static final String SUT = "FF";
   private static final String FS_ID = "FS";
   private static final String TEST_ID = "RFD_0_10000";
   private static final String FORM_NAME = "Form.html";
   private static  String DOCUMENT_URL;
   
   static {
      FileServer fileServer = WSServer.getFileServerById(FS_ID);
      try {
         Path path = fileServer.getFileDirectoryPath().resolve(SUT + fs + TEST_ID);
         Util.isValidPfn(FS_ID + " file directory", path, PfnType.DIRECTORY, "r");
         path = path.resolve(FORM_NAME);
         Util.isValidPfn("form", path, PfnType.FILE, "r");
         DOCUMENT_URL = fileServer.getFileUriPrefix() + SUT + "/" + TEST_ID + 
            "/" + FORM_NAME;
      } catch (Exception e) {
         Util.exit("Could not init " + FormManager.class.getCanonicalName()
            + " : " + e.getMessage());
      }
   }
  
   @Override
   public RetrieveFormResponseType formManagerRetrieveForm(
      RetrieveFormRequestType body) throws SOAPFaultException, TestFailException {
      Util.invoked(log);
      
      LoadNameValuePairs.retrieveFormRequest(body, wsLog, null);

      AnyXMLContentType ppd = body.getPrepopData();
      WorkflowDataType wfd = body.getWorkflowData();
      // -------------------------------------- Test Requirement Validations

      TSTStatus ts = TSTStatus.TST_PREPOP_NULL_PASS;
      if (ppd != null && XmlUtil.isNotEmpty(ppd.getAny()))
         ts = TSTStatus.TST_PREPOP_NULL_FAIL;
      wsLog.addStatus(ts);
      Result valid = ts.getResult().overall(null);

      ts = TSTStatus.TST_ENCODED_PASS;
      if (wfd == null || wfd.isEncodedResponse() == true)
         ts = TSTStatus.TST_ENCODED_FAIL;
      wsLog.addStatus(ts);
      valid = ts.getResult().overall(valid);

      ts = TSTStatus.TST_ARCH_URL_PASS;
      if (wfd != null && StringUtils.isNotEmpty(wfd.getArchiveURL()))
         ts = TSTStatus.TST_ARCH_URL_FAIL;
      wsLog.addStatus(ts);
      valid = ts.getResult().overall(valid);

      ts = TSTStatus.TST_CONTXT_PASS;
      if (wfd != null) {
         AnyXMLContentType ctx = wfd.getContext();
         if (ctx != null && XmlUtil.isNotEmpty(ctx.getAny())) 
            ts = TSTStatus.TST_CONTXT_FAIL;
      }
      wsLog.addStatus(ts);
      valid = ts.getResult().overall(valid);

      ts = TSTStatus.TST_INSTANCEID_PASS;
      if (wfd != null && StringUtils.isNotEmpty(wfd.getInstanceID()))
         ts = TSTStatus.TST_INSTANCEID_FAIL;
      wsLog.addStatus(ts);
      valid = ts.getResult().overall(valid);

      if (valid.equals(Result.FAIL))
         throw new TestFailException();

      // ------------------------------ Create response object
      RetrieveFormResponseType retrieveFormResponse =
         new RetrieveFormResponseType();

      FormDataType formData = new FormDataType();
      formData.setURL(DOCUMENT_URL);
      retrieveFormResponse.setForm(formData);
      
      LoadNameValuePairs.retrieveFormResponse(retrieveFormResponse, wsLog);

      return retrieveFormResponse;
   }

   /**
    * Status codes used by this particular implementor.
    */
   @SuppressWarnings("javadoc")
   public enum TSTStatus implements Status {

      TST_PREPOP_NULL_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "pre-population data is null: FAIL"),

      TST_PREPOP_NULL_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "pre-population data is null: PASS"),

      TST_FORMID_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "workflowData.formId is RFD 0-10000-FF: FAIL"),

      TST_FORMID_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "workflowData.formId is RFD 0-10000-FF: PASS"),

      TST_ENCODED_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "workflowData.encodedResponse is false: FAIL"),

      TST_ENCODED_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "workflowData.encodedResponse is false: PASS"),

      TST_ARCH_URL_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "workflowData.archiveURL is null: FAIL"),

      TST_ARCH_URL_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "workflowData.archiveURL is null: PASS"),

      TST_CONTXT_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "workflowData.context is null: FAIL"),

      TST_CONTXT_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "workflowData.context is null: PASS"),

      TST_INSTANCEID_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "workflowData.instanceID is null: FAIL"),

      TST_INSTANCEID_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "workflowData.instanceID is null: PASS"),

      TST_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "one or more test requirement failures"),

      TST_CLARIFICATION(Phase.TST, Result.FAIL, Level.WARN,
         "formManagerRetrieveClarification not part of this test");

      private Phase phase;
      private Result result;
      private Level logLevel;
      private String statusMsg;

      private static final String setCode = TEST_ID + "_FM";

      private TSTStatus(Phase ph, Result pf, Level ll, String msg) {
         phase = ph;
         result = pf;
         logLevel = ll;
         statusMsg = msg;
      }

      @Override
      public Result getResult() {
         return result;
      }

      @Override
      public StatusUpdateType getUpdateType() {
         return phase.updateType;
      }

      @Override
      public String getMsg() {
         return phase.phaseMsg + ": " + statusMsg;
      }

      @Override
      public String getStatusMsg() {
         return statusMsg;
      }

      @Override
      public String encode() {
         return setCode + "," + name() + "|";
      }

      @Override
      public Level getLogLevel() {
         return logLevel;
      }

      @Override
      public String getPhaseMsg() {
         return phase.phaseMsg;
      }

      @Override
      public String getSetCode() {
         return setCode;
      }

      @Override
      public StatusType getStatusType() {
         return phase.statusType;
      }

      /**
       * @param name of an instance of {@link TSTStatus}
       * @return the TSTStatus instance corresponding to the passed name, cast
       * to an {@link edu.wustl.mir.erl.ihe.util.Status Status} instance.
       */
      public static Status getStatus(String name) {
         return Enum.valueOf(TSTStatus.class, name);
      }

      // ----------------------- Phases for this status code
      static {
         StatusHelper.addCodeSet(setCode, TSTStatus.class);
      }
   } // EO TSTStatus enum

} // EO FormManagerSimulator class
