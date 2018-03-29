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
package edu.wustl.mir.erl.ihe.rfd.tests.FF.BFDR_E_1_10000_LDS_B;

import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.w3c.dom.Element;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.rfd.cda.FunctionDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.PersonNameDataElement;
import edu.wustl.mir.erl.ihe.rfd.cda.ValueSet;
import edu.wustl.mir.erl.ihe.rfd.cda.document.LDS;
import edu.wustl.mir.erl.ihe.rfd.servers.FormManagerTest;
import edu.wustl.mir.erl.ihe.server.FileServer;
import edu.wustl.mir.erl.ihe.util.Plug;
import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;
import edu.wustl.mir.erl.ihe.util.TestFailException;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.XmlUtil;
import edu.wustl.mir.erl.ihe.ws.server.WSServer;
import ihe.iti.rfd._2007.AnyXMLContentType;
import ihe.iti.rfd._2007.FormDataType;
import ihe.iti.rfd._2007.RetrieveFormRequestType;
import ihe.iti.rfd._2007.RetrieveFormResponseType;
import ihe.iti.rfd._2007.WorkflowDataType;

/**
 * FormManager simulator test for BFDR_E_1_10000_LDS_B
 */
public class FormManager extends FormManagerTest {
   private static final long serialVersionUID = 1L;

   private static final String TEST_ID = "BFDR_E_1_10000_LDS_B";
   private static FileServer fileServer = WSServer.getFileServerById("FS");
   
   // LDS requires use of ValueSets
   static { ValueSet.init(); }
  
   @Override
   public RetrieveFormResponseType formManagerRetrieveForm(
      RetrieveFormRequestType body) throws SOAPFaultException, TestFailException {
      Util.invoked(log);
      
      LDS lds = new LDS();

      AnyXMLContentType ppd = body.getPrepopData();
      WorkflowDataType wfd = body.getWorkflowData();
      Element prepopData = null;
      // -------------------------------------- Test Requirement Validations
      Result valid = Result.PASS;
      TSTStatus ts = null;
      TSTStatus pts = TSTStatus.TST_PREPOP_LDS_PASS;
      prepopData: {
         if (ppd == null) {
            pts = TSTStatus.TST_PREPOP_LDS_FAIL;
            break prepopData;
         }
         List <Element> any = ppd.getAny();
         if (XmlUtil.isEmpty(any)) {
            pts = TSTStatus.TST_PREPOP_LDS_FAIL;
            break prepopData;
         }
         prepopData = any.get(0);
         try {
            lds.loadValue(prepopData);
         } catch (Exception e) {
            pts = TSTStatus.TST_PREPOP_LDS_FAIL;
            break prepopData;
         }
         
         // TODO Code here to verify that prepopData is valid CDA LDS Document
         
         ts = TSTStatus.TST_PREPOP_MNAME_PASS;
         PersonNameDataElement mName = (PersonNameDataElement) lds.getDataElementByName("mName");
         if ((mName.getFamily().equalsIgnoreCase("Carson") == false) ||
              (mName.getGiven().equalsIgnoreCase("Rachel") == false))
            ts = TSTStatus.TST_PREPOP_MNAME_FAIL;
         wsLog.addStatus(ts);
         valid = ts.getResult().overall(null);
         
         ts = TSTStatus.TST_PREPOP_CHAM_PASS;
         FunctionDataElement cham = (FunctionDataElement) lds.getDataElementByName("cham");
         if (cham.getValue().equalsIgnoreCase("Y") == false)
            ts = TSTStatus.TST_PREPOP_CHAM_FAIL;
         wsLog.addStatus(ts);
         valid = ts.getResult().overall(null);
         
      }  // EO prepopData block
      
      wsLog.addStatus(pts);
      valid = pts.getResult().overall(null);

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
      
      LoadNameValuePairs.retrieveFormRequest(body, wsLog, lds);

      if (valid.equals(Result.FAIL))
         throw new TestFailException();
      
      // ----------------------------------------- Create form
      String urlString = null;
      try {
         lds.loadValue(prepopData);
      String form = lds.populate();
      form = new Plug(form)
         .set("formId", TEST_ID)
         .set("destinationEndpointURL","http://localhost:3302/FormReceiver")
         .get();
      urlString = fileServer.publishForm(form, "Form${seq}.html");
      } catch (Exception e) {
         log.warn(Util.getEM(e));
         throw new TestFailException(e.getMessage());
      }

      // ------------------------------ Create response object
      RetrieveFormResponseType retrieveFormResponse =
         new RetrieveFormResponseType();

      FormDataType formData = new FormDataType();
      formData.setURL(urlString);
      retrieveFormResponse.setForm(formData);

      LoadNameValuePairs.retrieveFormResponse(retrieveFormResponse, wsLog);
      
      return retrieveFormResponse;
   }

   /**
    * Status codes used by this particular implementor.
    */
   @SuppressWarnings("javadoc")
   public enum TSTStatus implements Status {

      TST_PREPOP_LDS_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "pre-population data is CDA LDS document: FAIL"),

      TST_PREPOP_LDS_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "pre-population data is CDA LDS document: PASS"),

      TST_PREPOP_MNAME_FAIL(Phase.TST, Result.FAIL, Level.TRACE,
         "pre-population data Mother's name is 'Rachel Carson: FAIL"),

      TST_PREPOP_MNAME_PASS(Phase.TST, Result.PASS, Level.DEBUG,
         "pre-population data Mother's name is 'Rachel Carson': PASS"),

      TST_PREPOP_CHAM_FAIL(Phase.TST, Result.FAIL, Level.TRACE,
         "pre-population data Chlamydia finding is 'Y': FAIL"),

      TST_PREPOP_CHAM_PASS(Phase.TST, Result.PASS, Level.DEBUG,
         "pre-population data Chlamydia finding is 'Y': PASS"),

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
