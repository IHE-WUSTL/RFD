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
package edu.wustl.mir.erl.ihe.rfd.tests.FF.VRDR_1_10000_VRDR_I;

import org.apache.log4j.Level;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.rfd.servers.FormReceiverTest;
import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;
import edu.wustl.mir.erl.ihe.util.TestFailException;
import edu.wustl.mir.erl.ihe.util.Util;
import ihe.iti.rfd._2007.AnyXMLContentType;
import ihe.iti.rfd._2007.SubmitFormResponseType;

/**
 * RFD FormReceiverSimulator simulator test for VRDR_1_10000_VRDR_I
 */

public class FormReceiver extends FormReceiverTest {
   private static final long serialVersionUID = 1L;

   private static final String TEST_ID = "VRDR_1_10000_VRDR_I";

   @Override
   public SubmitFormResponseType submitForm(AnyXMLContentType body) 
            throws TestFailException {
      Util.invoked(log);
      
      LoadNameValuePairs.submitFormRequest(body, wsLog);

     

      SubmitFormResponseType response = new SubmitFormResponseType();
      
      LoadNameValuePairs.submitFormResponse(response, wsLog);
      
      return response;
   }

   /**
    * Status codes used by this particular implementor.
    */
   @SuppressWarnings("javadoc")
   public enum TSTStatus implements Status {

      //@formatter:off
      RIM_UNRECOGNIZED_DATA(Phase.TST, Result.FAIL, Level.WARN, "Cannot recognize the posted data"), 
      TST_FAIL(Phase.TST, Result.FAIL, Level.DEBUG, "one or more test requirement failures");
      //@formatter:on

      private Phase phase;
      private Result result;
      private Level logLevel;
      private String statusMsg;

      private static final String setCode = TEST_ID + "_FR";

      private TSTStatus(Phase ph, Result pf, Level ll, String msg) {
         phase = ph;
         result = pf;
         logLevel = ll;
         statusMsg = msg;
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

      @Override
      public Result getResult() {
         return result;
      }

      @Override
      public StatusType getStatusType() {
         return phase.statusType;
      }
   } // EO TSTStatus enum

} // EO FormReceiverSimulator class
