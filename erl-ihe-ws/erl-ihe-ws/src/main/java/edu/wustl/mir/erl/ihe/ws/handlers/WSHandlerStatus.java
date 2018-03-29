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
package edu.wustl.mir.erl.ihe.ws.handlers;

import org.apache.log4j.Level;

import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;

/**
 * Status codes used during HTTP and SOAP handler validation
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 *
 */
public enum WSHandlerStatus implements Status {
   /**
    * msg identification in progress
    */
   MSG_IN_PROGRESS(Phase.MSG, Result.UNKNOWN, Level.TRACE, "in Progress"),
   /**
    * http exchange information not in message context
    */
   MSG_NO_HTTP_EXCHANGE (Phase.MSG, Result.UNKNOWN, Level.WARN, "No http exchange data found"),
   /**
    * msg identified successfully
    */
   MSG_OK(Phase.MSG, Result.PASS, Level.TRACE, "successful"),
   /**
    * handler in progress
    */
   HTTP_IN_IN_PROGRESS(Phase.HTTP_IN, Result.UNKNOWN, Level.TRACE, "in progress"),
   /**
    * No message body found.
    */
   HTTP_IN_NO_BODY(Phase.HTTP_IN, Result.FAIL, Level.WARN, "no message body found"),
   /**
    * Message body format or I/O error
    */
   HTTP_IN_BODY_ERR(Phase.HTTP_IN, Result.FAIL, Level.WARN, "could not read message body"),
   /**
    * handler processing complete
    */
   HTTP_IN_OK(Phase.HTTP_IN, Result.PASS, Level.TRACE, "completed"),
   /**
    * handler in progress
    */
   HTTP_OUT_IN_PROGRESS(Phase.HTTP_OUT, Result.UNKNOWN, Level.TRACE, "in progress"),
   /**
    * No message body found.
    */
   HTTP_OUT_NO_BODY(Phase.HTTP_IN, Result.FAIL, Level.WARN, "no message body found"),
   /**
    * Message body format or I/O error
    */
   HTTP_OUT_BODY_ERR(Phase.HTTP_IN, Result.FAIL, Level.WARN, "could not read message body"),
   /**
    * handler processing complete
    */
   HTTP_OUT_OK(Phase.HTTP_OUT, Result.PASS, Level.TRACE, "completed"),
   /**
    * handler in progress
    */
   SOAP_IN_IN_PROGRESS(Phase.SOAP_IN, Result.UNKNOWN, Level.TRACE, "in progress"),
   /**
    * handler processing complete
    */
   SOAP_IN_OK(Phase.SOAP_IN, Result.PASS, Level.TRACE, "completed"),
   /**
    * handler in progress
    */
   SOAP_OUT_IN_PROGRESS(Phase.SOAP_OUT, Result.UNKNOWN, Level.TRACE, "in progress"),
   /**
    * handler processing complete
    */
   SOAP_OUT_OK(Phase.SOAP_OUT, Result.PASS, Level.TRACE, "completed")
   ;
   
   private Phase phase;
   private Result result;
   private Level logLevel;
   private String statusMsg;

   private static String setCode = "WSHandler";

   private WSHandlerStatus(Phase ph, Result rs, Level ll, String sm) {
      phase = ph;
      result = rs;
      logLevel = ll;
      statusMsg = sm;
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
   public String getMsg() {
      return phase.phaseMsg + ": " + statusMsg;
   }

   @Override
   public StatusUpdateType getUpdateType() {
      return phase.updateType;
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
   public String getSetCode() {
      return setCode;
   }
   
   @Override
   public StatusType getStatusType() {
      return phase.statusType;
   }

   /**
    * @param name of an instance of {@link WSHandlerStatus}
    * @return the RFDStatus instance corresponding to the passed name, cast to
    *         an {@link edu.wustl.mir.erl.ihe.util.Status Status} instance.
    */
   public static Status getStatus(String name) {
      return Enum.valueOf(WSHandlerStatus.class, name);
   }

   static {
      StatusHelper.addCodeSet(setCode, WSHandlerStatus.class);
   }
   
   // ----------------------- Phases for this status code
   enum Phase  {
      
      MSG("Msg type, Endpoint identification", StatusType.CONNECTION , StatusUpdateType.UPDATE),
      HTTP_IN("HTTP inbound handler validation", StatusType.HTTP_REQUEST , StatusUpdateType.UPDATE),
      SOAP_IN("SOAP inbound handler validation", StatusType.SOAP_REQUEST , StatusUpdateType.UPDATE),
      HTTP_OUT("HTTP outbound handler validation", StatusType.HTTP_RESPONSE , StatusUpdateType.UPDATE),
      SOAP_OUT("SOAP outbound handler validation", StatusType.SOAP_RESPONSE , StatusUpdateType.UPDATE);

      String phaseMsg;
      StatusType statusType;
      StatusUpdateType updateType;

      private Phase(String nm, StatusType st, StatusUpdateType ut) {
         phaseMsg = nm;
         statusType = st;
         updateType = ut;
      }     

   } // EO Phase enum

}
