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
 *******************************************************************************/

package edu.wustl.mir.erl.ihe.rfd.servers;

import ihe.iti.rfd._2007.FormManagerPortType;
import ihe.iti.rfd._2007.RetrieveClarificationRequestType;
import ihe.iti.rfd._2007.RetrieveFormRequestType;
import ihe.iti.rfd._2007.RetrieveFormResponseType;
import ihe.iti.rfd._2007.WorkflowDataType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebService;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPFaultException;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;

import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;
import edu.wustl.mir.erl.ihe.util.TestFailException;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.server.WSServerTest;

/**
 * RFD FormManagerSimulator Simulator Endpoint Implementation class based on the
 * class
 * {@link ihe.iti.rfd._2007.FormManager_Service_FormManager_Port_Soap12Impl
 * FormManager_Port_SOAP12}, which was generated by JAX-WS RI, version JAX-WS RI
 * 2.2.8. Generated source version: 2.2.
 */
@WebService(portName = "FormManager_Port_Soap12",
   serviceName = "FormManager_Service",
   targetNamespace = "urn:ihe:iti:rfd:2007",
   wsdlLocation = "./wsdl/RFDFormManager.wsdl",
   endpointInterface = "ihe.iti.rfd._2007.FormManagerPortType")
@BindingType("http://www.w3.org/2003/05/soap/bindings/HTTP/")
public class FormManagerSimulator extends WSServerTest implements
   FormManagerPortType {
   private static final long serialVersionUID = 1L;

   private Map <String, FormManagerTest> tests = new HashMap <>();

   @Override
   public void addTests(List <HierarchicalConfiguration> testsProperties)
      throws Exception {
      for (HierarchicalConfiguration testProperties : testsProperties) {
         String fid = testProperties.getString("[@formId]");
         if (StringUtils.isBlank(fid))
            throw new Exception("invalid Test.formId");
         if (tests.containsKey(fid))
            throw new Exception("Duplicate Test.formId");
         log.info("Loading Test ID: " + fid);
         String tcn = testProperties.getString("[@class]");
         if (StringUtils.isBlank(tcn))
            throw new Exception("fid: " + fid + " - invalid Test.class");
         log.info(" test class: " + tcn);
         Class <?> tc = null;
         try {
            tc = Class.forName(tcn, false, ClassLoader.getSystemClassLoader());
         } catch (ClassNotFoundException cnfe) {
            throw new Exception("fid: " + fid
               + " - Could not load endpoint class: " + tcn);
         }
         if (FormManagerTest.class.isAssignableFrom(tc) == false)
            throw new Exception("fid: " + fid + " class " + tcn
               + " does not extend " + FormManagerTest.class.getCanonicalName());
         FormManagerTest test = (FormManagerTest) tc.newInstance();
         tests.put(fid, test);
      } // EO process tests loop
      if (tests.isEmpty())
         throw new Exception("FormManagerSimulator, no valid tests");
   } // EO addTests method

   /*
    * (non-Javadoc)
    * 
    * @see
    * ihe.iti.rfd._2007.FormManagerPortType#formManagerRetrieveForm(ihe.iti.
    * rfd._2007.RetrieveFormRequestType)
    */
   @Override
   public RetrieveFormResponseType formManagerRetrieveForm(
      RetrieveFormRequestType request) throws SOAPFaultException {
      setup();
      Util.invoked(log);

      // ------------------------ Pull workflow data
      WorkflowDataType wfd = request.getWorkflowData();
      if (wfd == null)
         throw new SOAPFaultException(senderFault(TSTStatus.RIM_WORKFLOW_NULL));

      // ------------------------------ pull form id
      String fid = wfd.getFormID();
      if (fid == null)
         throw new SOAPFaultException(senderFault(TSTStatus.UFI_FORMID_NULL));

      // -------------------------- Pull test
      FormManagerTest test = tests.get(fid);
      if (test == null)
         throw new SOAPFaultException(senderFault(TSTStatus.UFI_FORMID_INV));
      test.setup(wsLogTransaction);
      RetrieveFormResponseType response = null;
      try {
         response = test.formManagerRetrieveForm(request);
      } catch (UnsupportedOperationException uoe) {
         throw new SOAPFaultException(senderFault(TSTStatus.TST_RETRIEVE));
      } catch (TestFailException tfe) {
         throw new SOAPFaultException(senderFault(TSTStatus.TST_FAIL));
      }

      return response;
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * ihe.iti.rfd._2007.FormManagerPortType#formManagerRetrieveClarification
    * (ihe.iti.rfd._2007.RetrieveClarificationRequestType)
    */
   @Override
   public RetrieveFormResponseType formManagerRetrieveClarification(
      RetrieveClarificationRequestType request) {
      throw new SOAPFaultException(senderFault(TSTStatus.TST_CLARIFICATION));
   }

   /**
    * Status codes used by this FormManager
    */
   @SuppressWarnings("javadoc")
   public enum TSTStatus implements Status {

      TST_FAIL(Phase.TST, Result.FAIL, Level.DEBUG,
         "one or more test requirement failures"),

      TST_RETRIEVE(Phase.TST, Result.FAIL, Level.WARN,
         "formManagerRetrieveClarification not part of this test"),

      TST_CLARIFICATION(Phase.TST, Result.FAIL, Level.WARN,
         "formManagerRetrieveForm not part of this test"),

      RIM_WORKFLOW_NULL(Phase.RIM, Result.FAIL, Level.WARN,
         "workFlowData null"),

      UFI_FORMID_NULL(Phase.UFI, Result.FAIL, Level.WARN, "formID null"),

      UFI_FORMID_INV(Phase.UFI, Result.FAIL, Level.DEBUG,
         "Unknown or invalid formID");

      private Phase phase;
      private Result result;
      private Level logLevel;
      private String statusMsg;

      private static final String setCode = "FMSim";

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

} // EO FormManagerSimulator Simulator class
