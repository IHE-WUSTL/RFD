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
package edu.wustl.mir.erl.ihe.rfd.servers;

import java.io.Serializable;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;

import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.server.WSEndpoint;

/**
 *
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 *
 */
public class Test implements Serializable , WSProperties {
   private static final long serialVersionUID = 1L;
   
   protected LogTransaction wsLogTransaction = null;
   protected WSLog wsLog;
   protected Logger log;
   protected WSEndpoint wsEndpoint;
   
   /**
    * passes logging info to test
    * @param logTrans {@link LogTransaction} instance.
    */
   public void setup(LogTransaction logTrans) {
      wsLogTransaction = logTrans;
      wsEndpoint = wsLogTransaction.getWsEndpoint();
      wsLog = wsLogTransaction.getWsLog();
      log = wsLogTransaction.getLog();
   }

   private static final QName qname = new QName("emDetail");

   protected SOAPFault senderFault(Status status, String... extraDetails) {
      try {
            wsLog.addStatus(status);
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
         
            wsLog.addStatus(status);
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



   protected Result elementsContains(List <Element> elements, String name,
      Status passStatus, Status failStatus) {
      for (Element e : elements) {
         if (e.getNodeName().equalsIgnoreCase(name)) {
            String value = e.getTextContent();
            if (StringUtils.isNotBlank(value)) {
               wsLog.addStatus(passStatus, "value=" + value);
               return Result.PASS;
            }
         }
      }
      wsLog.addStatus(failStatus);
      return Result.FAIL;
   }
   
   /**
    * Phases for tests
    */
   @SuppressWarnings("javadoc")
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
