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
package edu.wustl.mir.erl.ihe.rfd.tests.FM.VRDR_1_10000_VRDR_I;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.BindingProvider;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Level;
import org.javatuples.LabelValue;
import org.w3c.dom.Element;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.rfd.cda.document.VRDRPrepop;
import edu.wustl.mir.erl.ihe.rfd.clients.FormManagerServiceClient;
import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.XmlUtil;
import edu.wustl.mir.erl.ihe.ws.client.ClientTest;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.handlers.HandlerUtil;
import edu.wustl.mir.erl.ihe.ws.server.WSServerTest.Phase;
import ihe.iti.rfd._2007.AnyXMLContentType;
import ihe.iti.rfd._2007.FormDataType;
import ihe.iti.rfd._2007.FormManagerPortType;
import ihe.iti.rfd._2007.RetrieveFormRequestType;
import ihe.iti.rfd._2007.RetrieveFormResponseType;
import ihe.iti.rfd._2007.WorkflowDataType;

/**
 *
 */
public class FormFiller extends ClientTest {

   private static final String TEST_ID = "FM.VRDR_1_10000_VRDR_I";
   private static Element cda;
   
   static {
      try {
         String cdaStr =
            IOUtils.toString(FormFiller.class.getResource("CDA.xml"),
               CHAR_SET_UTF_8);
         cda = XmlUtil.strToElement(cdaStr);
      } catch (Exception e) {
         Util.exit(Util.getEM(e));
      }
   }
   
   /**
    * Constructor should only be called from {@link ClientTest#initialize}
    */
   public FormFiller() {
      setAction(FormManagerPortType.class, "formManagerRetrieveForm");
   }

   /**
    * @see edu.wustl.mir.erl.ihe.ws.client.ClientTest#runTest(java.util.List)
    */
   @Override
   @SuppressWarnings({ "cast", "unused" })
   public List <LabelValue <String, Object>> runTest(
      List <LabelValue <String, Object>> pars) {
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      LogTransaction logTran = null;
      try {
         // get passed parameters
         String url = (String) Util.getValueForLabel(LABEL_WSSERVER_URL, pars);
         String formID = (String) Util.getValueForLabel(LABEL_FORM_ID, pars);
         // create form request
         RetrieveFormRequestType retrieveFormRequest =
            new RetrieveFormRequestType();
         AnyXMLContentType prepopData = new AnyXMLContentType();
         prepopData.getAny().add(cda);
         retrieveFormRequest.setPrepopData(prepopData);

         WorkflowDataType workflowData = new WorkflowDataType();
         workflowData.setFormID(formID);
         workflowData.setEncodedResponse(false);
         retrieveFormRequest.setWorkflowData(workflowData);

         // get port, send-receive
         FormManagerServiceClient service = new FormManagerServiceClient(url);
         FormManagerPortType port = service.getPort();
         logTran = HandlerUtil.initializeSOAPClientHandler((BindingProvider) port,
            (ClientTest) this, url);
         WSLog wsLog = logTran.getWsLog();
         
         LoadNameValuePairs.retrieveFormRequest(retrieveFormRequest, wsLog, new VRDRPrepop());
         
         RetrieveFormResponseType retrieveFormResponse =
            port.formManagerRetrieveForm(retrieveFormRequest);
         
         LoadNameValuePairs.retrieveFormResponse(retrieveFormResponse, wsLog);

         TSTStatus ts = TSTStatus.TST_URL_PASS;
         String formURL = null;
         form: {
            FormDataType form = retrieveFormResponse.getForm();
            if (form == null) {
               ts = TSTStatus.TST_URL_MISSING;
               break form;
            }
            formURL = form.getURL();
            if (formURL == null) {
               ts = TSTStatus.TST_URL_MISSING;
               break form;
            }
            try { new URL(formURL); 
            } catch (MalformedURLException mue) {
               ts = TSTStatus.TST_URL_INVALID;
               break form;
            }
         } // EO form block
         wsLog.addStatus(ts);
         if (formURL == null) formURL = "null";
         returns.add(new LabelValue <String, Object>(LABEL_FORM_URL,
                  (Object) formURL));
         
         logTran.close();

      } catch (Exception e) {
         returns.add(new LabelValue <String, Object>(LABEL_ERROR, (Object) Util
            .getEM(e)));
      }
      return returns;
   } // EO runTest method

   /**
    * Status codes used by this particular implementor.
    */   
   @SuppressWarnings("javadoc")
   public enum TSTStatus implements Status {
      
      TST_URL_MISSING(Phase.TST, Result.FAIL, Level.DEBUG,
               "form URL required, not present: FAIL"),
      TST_URL_INVALID(Phase.TST, Result.FAIL, Level.DEBUG,
               "valid form URL required, URL invalid: FAIL"),
      TST_URL_PASS(Phase.TST, Result.PASS, Level.TRACE,
               "valid form URL required: PASS");
      
      private Phase phase;
      private Result result;
      private Level logLevel;
      private String statusMsg;

      private static final String setCode = TEST_ID + "_FF";

      private TSTStatus(Phase ph, Result pf, Level ll, String msg) {
         phase = ph;
         result = pf;
         logLevel = ll;
         statusMsg = msg;
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#getPhaseMsg()
       */
      @Override
      public String getPhaseMsg() {
         return phase.phaseMsg;
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#getStatusMsg()
       */
      @Override
      public String getStatusMsg() {
         return statusMsg;
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#getMsg()
       */
      @Override
      public String getMsg() {
         return phase.phaseMsg + ": " + statusMsg;
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#getUpdateType()
       */
      @Override
      public StatusUpdateType getUpdateType() {
         return phase.updateType;
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#encode()
       */
      @Override
      public String encode() {
         return setCode + "," + name() + "|";
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#getLogLevel()
       */
      @Override
      public Level getLogLevel() {
         return logLevel;
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#getResult()
       */
      @Override
      public Result getResult() {
         return result;
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#getStatusType()
       */
      @Override
      public StatusType getStatusType() {
         return phase.statusType;
      }

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.util.Status#getSetCode()
       */
      @Override
      public String getSetCode() {
         return setCode;
      }
      
   }  // EO TSTStatus enum

} // EO FormFiller class
