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
package edu.wustl.mir.erl.ihe.rfd.tests.FP.RFD_1_10000;

import ihe.iti.rfd._2007.AnyXMLContentType;
import ihe.iti.rfd._2007.FormDataType;
import ihe.iti.rfd._2007.RetrieveFormRequestType;
import ihe.iti.rfd._2007.RetrieveFormResponseType;
import ihe.iti.rfd._2007.SubmitFormResponseType;
import ihe.iti.rfd._2007.WorkflowDataType;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.rfd.servers.FormProcessorTest;
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

/**
 * FormProcessor simulator for RFD_1_10000, used to validate the test itself,
 * which in production would be run against a FormProcessor SUT.
 */
public class FormProcessor extends FormProcessorTest {
   private static final long serialVersionUID = 1L;

   private static final String SUT = "FP";
   private static final String FS_ID = "FS";
   private static final String TEST_ID = "RFD_1_10000";
   private static FileServer fileServer = WSServer.getFileServerById(FS_ID);
   private static String MASTER_FORM;

   static {
      try {
         MASTER_FORM =
            IOUtils.toString(FormProcessor.class.getResource("Form.html"),
               CHAR_SET_UTF_8);
      } catch (IOException e) {
         Util.exit(Util.getEM(e));
      }
   }
   
   @Override
   public RetrieveFormResponseType formProcessorRetrieveForm(
      RetrieveFormRequestType body) throws SOAPFaultException,
      TestFailException {
      Util.invoked(log);
      
      LoadNameValuePairs.retrieveFormRequest(body, wsLog, null);

      AnyXMLContentType ppd = body.getPrepopData();
      WorkflowDataType wfd = body.getWorkflowData();
      // -------------------------------------- Test Requirement Validations

      /*
       * We don't care whatever else the prepopData element contains, as long as
       * it contains a top level RFD element, which contains an age element with
       * a valid content. For simplicity, the first RFD.age found will be used.
       */
      TSTStatus ts = null;
      String ageValue = null;
      Result valid;
      Integer ageInt = null;
      // overall evaluation of prepop data value
      prepopData: {
         // Does <RFD><age> prepop tag exist?
         prepopTag: {
            ts = TSTStatus.TST_PREPOP_FAIL;
            if (ppd == null) break prepopTag;
            List <Element> elements = ppd.getAny();
            if (XmlUtil.isEmpty(elements)) break prepopTag;
            // pass elements under <prepopData> element, look for RFD elements
            for (Element element : elements) {
               if (!element.getTagName().equals("RFD")) continue;
               NodeList ages = element.getElementsByTagName("age");
               if (ages == null) continue;
               // pass age nodes
               for (int i = 0; i < ages.getLength(); i++ ) {
                  Node age = ages.item(i);
                  ageValue = XmlUtil.getFirstLevelTextContent(age);
                  if (ageValue == null) continue;
                  ts = TSTStatus.TST_PREPOP_PASS;
                  break prepopTag;
               }
            }
         } // EO prepopTag block
         wsLog.addStatus(ts);
         valid = ts.getResult().overall(null);
         if (ts == TSTStatus.TST_PREPOP_FAIL) break prepopData;

         // Age value found. Is it integer, 1-85?
         prepopAge: {
            ts = TSTStatus.TST_AGE_VALUE_FAIL;
            try {
               ageInt = Integer.parseInt(ageValue);
               if (ageInt < 1 || ageInt > 85) break prepopAge;
               ts = TSTStatus.TST_AGE_VALUE_PASS;
            } catch (Exception e) {}
         } // EO prepopAge block
         wsLog.addStatus(ts);
         valid = ts.getResult().overall(null);
      } // EO prepopData block

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

      // ------------------------ Create form
      int formNumber = fileServer.getNextFormNumber();
      String formName = "Form" + formNumber + ".html";
      Path formPath = fileServer.getFormDirectoryPath().resolve(formName);
      try {
         new Plug(MASTER_FORM).set("age", ageInt)
            .set("url", wsEndpoint.getURI())
            .get(formPath, false);
      } catch (Exception e) {
         log.warn("Error writing RFD form file " + e.getMessage());
         throw new SOAPFaultException(receiverFault(TSTStatus.SRV_IO_ERR,
            "IO error creating RFD form"));
      }

      // ------------------------------ Create response object
      RetrieveFormResponseType retrieveFormResponse =
         new RetrieveFormResponseType();

      FormDataType formData = new FormDataType();
      formData.setURL(fileServer.getFormUriPrefix() + formName);
      retrieveFormResponse.setForm(formData);

      wsLog.setSoapResponseName("RetrieveFormResponse");
      LoadNameValuePairs.retrieveFormResponse(retrieveFormResponse, wsLog);
      
      LoadNameValuePairs.retrieveFormResponse(retrieveFormResponse, wsLog);

      return retrieveFormResponse;
   }
   
   @Override
   public SubmitFormResponseType submitForm(AnyXMLContentType body) {
      Util.invoked(log);

      List <Element> elements = body.getAny();
      Result valid =
         elementsContains(elements, "age", TSTStatus.TST_AGE_PASS,
            TSTStatus.TST_AGE_FAIL);
      valid =
         elementsContains(elements, "bmi", TSTStatus.TST_BMI_PASS,
            TSTStatus.TST_BMI_FAIL).overall(valid);

      if (valid.equals(Result.FAIL))
         throw new SOAPFaultException(
            senderFault(TSTStatus.RIM_UNRECOGNIZED_DATA));

      SubmitFormResponseType response = new SubmitFormResponseType();

      wsLog.setSoapResponseName("SubmitFormResponse");
      LoadNameValuePairs.submitFormResponse(response, wsLog);
      return response;
   }

   /**
    * Status codes used by this particular implementor.
    */
   @SuppressWarnings("javadoc")
   public enum TSTStatus implements Status {
      
      TST_AGE_FAIL(Phase.TST, Result.FAIL, Level.DEBUG, "Age value missing: Fail"), 
      TST_AGE_PASS(Phase.TST, Result.PASS, Level.TRACE, "Age value found: Pass"), 
      TST_BMI_FAIL(Phase.TST, Result.FAIL, Level.DEBUG, "BMI value missing: Fail"), 
      TST_BMI_PASS(Phase.TST, Result.PASS, Level.TRACE, "BMI value found: Pass"),
      RIM_UNRECOGNIZED_DATA(Phase.TST, Result.FAIL, Level.WARN, "Cannot recognize the posted data"),
     

      TST_PREPOP_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "pre-population data has <RFD><age> tag: FAIL"),

      TST_PREPOP_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "pre-population data has <RFD><age> tag: PASS"),

      TST_AGE_VALUE_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "prepop age integer 1-85: FAIL"),

      TST_AGE_VALUE_PASS(Phase.TST, Result.PASS, Level.TRACE,
         "prepop age integer 1-85: PASS"),

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
         "formManagerRetrieveClarification not part of this test"),
         
      SRV_IO_ERR(Phase.TST, Result.FAIL, Level.WARN,
         "server IO error processing test");

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

} // EO FormProcessor class
