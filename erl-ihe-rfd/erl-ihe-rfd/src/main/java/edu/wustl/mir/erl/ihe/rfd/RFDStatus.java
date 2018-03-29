/*******************************************************************************
 * Copyright (c) 2014  Washington University in St. Louis
 *  All rights reserved. This program and the accompanying 
 *  materials are made available under the terms of the
 *  Apache License, Version 2.0 (the "License");  you may not 
 *  use this file except in compliance with the License.
 * The License is available at:
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, 
 *  software  distributed under the License is distributed on 
 *  an "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS  
 *  OF ANY KIND, either express or implied. See the License 
 *  for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Contributors:
 *    Initial author: Ralph Moulton / MIR WUSM IHE Development Project 
 *    moultonr@mir.wustl.edu
 *******************************************************************************/
package edu.wustl.mir.erl.ihe.rfd;

import org.apache.log4j.Level;

import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;

/**
 * Status codes used during RFD inbound message validation and processing
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public enum RFDStatus implements Status {

   /**
    * Validation is in progress.
    */
   MSG_IN_PROGRESS(Phase.MSG,  Result.UNKNOWN, Level.TRACE, "in Progress"),
   /**
    * Validation completed without error.
    */
   MSG_OK(Phase.MSG, Result.PASS, Level.TRACE, "OK"),
   /**
    * An error was found in the message
    */
   MSG_ERROR(Phase.MSG, Result.FAIL, Level.DEBUG, "error"),
   /**
    * A parsing error was found in the message
    */
   MSG_PARSE_ERROR(Phase.MSG, Result.FAIL, Level.DEBUG, "Parsing error"),
   /**
    * A processing error occurred during the validation of the message.
    */
   MSG_EXCEPTION(Phase.MSG, Result.FAIL, Level.DEBUG, "threw Exception");

   private Phase phase;
   private Result result;
   private Level logLevel;
   private String statusMsg;

   private static String setCode = "RFD";

   private RFDStatus(Phase ph, Result rs, Level ll, String sm) {
      phase = ph;
      result = rs;
      logLevel = ll;
      statusMsg = sm;
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
   public String encode() {
      return setCode + "," + name() + "|";
   }
   
   @Override
   public Result getResult() {
      return result;
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
   public String getStatusMsg() {
      return statusMsg;
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
    * @param name of an instance of {@link RFDStatus}
    * @return the RFDStatus instance corresponding to the passed name, cast to
    *         an {@link edu.wustl.mir.erl.ihe.util.Status Status} instance.
    */
   public static Status getStatus(String name) {
      return Enum.valueOf(RFDStatus.class, name);
   }

   static {
      StatusHelper.addCodeSet(setCode, RFDStatus.class);
   }
   
   // ----------------------- Phases for this status code
   enum Phase  {
      
      MSG("RFD message validation", StatusType.SOAP_REQUEST, StatusUpdateType.UPDATE);

      String phaseMsg;
      StatusType statusType;
      StatusUpdateType updateType;

      private Phase(String nm, StatusType st, StatusUpdateType ut) {
         phaseMsg = nm;
         statusType = st;
         updateType = ut;
      }     

   } // EO Phase enum

}  // EO RFDStatus enum
