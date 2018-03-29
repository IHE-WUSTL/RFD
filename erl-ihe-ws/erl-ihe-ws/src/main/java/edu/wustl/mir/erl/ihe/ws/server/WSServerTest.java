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
package edu.wustl.mir.erl.ihe.ws.server;

import java.io.Serializable;
import java.util.List;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Base class for WSServer Tests
 */
public abstract class WSServerTest implements Serializable, WSProperties {
   private static final long serialVersionUID = 1L;

   @Resource
   protected WebServiceContext webServiceContext;
   protected LogTransaction wsLogTransaction = null;
   protected WSLog wsLog;
   protected Logger log;
   protected WSEndpoint wsEndpoint;

   /**
    * Injects {@link WSEndpoint} for this server, and loads {@link Logger}.
    * Meant to be called during creation of server.
    * @param wse WSEndpoint instance
    */
   public void setWSEndpoint(WSEndpoint wse) {
      wsEndpoint = wse;
      log = wsEndpoint.getLog();
   }

   protected void setup() {
      try {
         MessageContext context = webServiceContext.getMessageContext();
         wsLogTransaction = (LogTransaction) context.get(WS_LOG_TRANSACTION);
         wsEndpoint = wsLogTransaction.getWsEndpoint();
         wsLog = wsLogTransaction.getWsLog();
         log = wsLogTransaction.getLog();
      } catch (Exception e) {
         String em = Util.classMethod() + " error: " + e.getMessage();
         log.warn(em);
      }

   } // EO setup() method

   private static final QName qname = new QName("emDetail");

   protected SOAPFault senderFault(Status status, String... extraDetails) {
      try {
         if (wsLogTransaction != null) {
            setup();
            wsLog.addStatus(status);
         }
         SOAPFault fault =
            (SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL))
               .createFault(status.getPhaseMsg(),
                  SOAPConstants.SOAP_SENDER_FAULT);
         Detail detail = fault.addDetail();
         DetailEntry entry = detail.addDetailEntry(qname);
         entry.addTextNode(status.getStatusMsg());
         if (extraDetails.length > 0) {
            for (String d : extraDetails) {
               DetailEntry ent = detail.addDetailEntry(qname);
               ent.addTextNode(d);
            }
         }
         return fault;
      } catch (SOAPException e) {
         Util.exit(e.getMessage());
         return null;
      }
   } // EO senderFault method

   protected SOAPFault receiverFault(Status status, String... extraDetails) {
      try {
         if (wsLogTransaction != null) {
            setup();
            wsLog.addStatus(status);
         }
         SOAPFault fault =
            (SOAPFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL))
               .createFault(status.getPhaseMsg(),
                  SOAPConstants.SOAP_RECEIVER_FAULT);
         Detail detail = fault.addDetail();
         DetailEntry entry = detail.addDetailEntry(qname);
         entry.addTextNode(status.getStatusMsg());
         if (extraDetails.length > 0) {
            for (String d : extraDetails) {
               DetailEntry ent = detail.addDetailEntry(qname);
               ent.addTextNode(d);
            }
         }
         return fault;
      } catch (SOAPException e) {
         Util.exit(e.getMessage());
         return null;
      }
   } // EO receiverFault method

   /**
    * Add specific tests to the server simulator
    * 
    * @param testsProperties List of Test element properties for this simulator.
    * @throws Exception on error
    */
   public abstract void addTests(
      List <HierarchicalConfiguration> testsProperties) throws Exception;

   public enum Phase {
      /**
       * Test requirement for this test
       */
      TST("Test requirement ", StatusType.TEST, StatusUpdateType.ADD),
      /**
       * Required information missing
       */
      RIM("Required Information Missing", StatusType.SOAP_REQUEST,
         StatusUpdateType.ADD),
      /**
       * Unknown or invalid formId
       */
      UFI("Unknown FormID", StatusType.SOAP_REQUEST, StatusUpdateType.ADD),
      /**
       * Error on server not related to SOAP Request contents or format.
       */
      SRV("Server Error", StatusType.SOAP_RESPONSE, StatusUpdateType.ADD);

      public String phaseMsg;
      public StatusType statusType;
      public StatusUpdateType updateType;

      private Phase(String nm, StatusType st, StatusUpdateType ut) {
         phaseMsg = nm;
         statusType = st;
         updateType = ut;
      }

   } // EO Phase enum
}
