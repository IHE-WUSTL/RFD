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
package edu.wustl.mir.erl.ihe.ws.handlers;

import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.Source;
import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.MessageType;
import edu.wustl.mir.erl.ihe.ws.db.TransactionType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.server.WSEndpoint;

/**
 * <p>{@link LogicalHandler} implementation designed to collect inbound and
 * outbound http layer related status information for the transaction. <b>
 * {@link SOAPServerHandler} must also be used, and run before HTTPLogging
 * handler.</b>
 * </p>
 * <b>Processing for inbound messages:</b>
 * <ul>
 * <li>Retrieves the stored WSLogTransaction instance from the message context.</li>
 * <li>Logs HTTP Request method, headers, and message (body).</li>
 * </ul>
 * <b>Processing for outbound messages:</b>
 * <ul>
 * <li>Retrieves the stored WSLogTransaction instance from the message context.</li>
 * <li>Logs HTTP Response code, headers, and message (body).</li>
 * </ul>
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */

public class HTTPServerHandler extends BaseHandler <LogicalMessageContext>
   implements LogicalHandler <LogicalMessageContext> {

   /**
    * Required no argument constructor
    */
   public HTTPServerHandler() {
      super();
      super.setHandlerTypeName(TransactionType.SOAP_SERVER,
         "Http handler for Http/SOAP server");
   }

   /**
    * Pull Message outbound property and invokes inbound or outbound message
    * processor routines.
    * 
    * @param logicalMessageContext message context.
    */
   @Override
   public boolean handleMessage(LogicalMessageContext logicalMessageContext) {
      Boolean outbound = null;
      try {
         outbound =
            (boolean) logicalMessageContext.get(MESSAGE_OUTBOUND_PROPERTY);
         if (outbound == null)
            throw new Exception(NO_MAP + MESSAGE_OUTBOUND_PROPERTY);
         if (!outbound) handleInboundMessage(logicalMessageContext);
      } catch (Exception ex) {
         StringBuilder em =
            new StringBuilder("MsgHandler#handleMessage error: "
               + ex.getMessage());
         Util.getLog().warn(em);
      }
      return true;
   } // EO handleMessage method

   // **************************************************************
   // ---------------------------------- Outbound message processing
   // **************************************************************
   private void handleInboundMessage(MessageContext lmc) {

      LogTransaction wsLogTransaction = null;
      WSEndpoint wsEndpoint = null;
      WSLog wsLog = null;
      Logger log = Util.getLog();

      try {
         // logContextEntries(lmc, log);

         wsLogTransaction = (LogTransaction) lmc.get(WS_LOG_TRANSACTION);
         if (wsLogTransaction == null)
            throw new Exception(NO_MAP + WS_LOG_TRANSACTION);
         wsEndpoint = wsLogTransaction.getWsEndpoint();
         wsLog = wsLogTransaction.getWsLog();
         log = wsEndpoint.getLog();
         wsLog.addStatus(WSHandlerStatus.HTTP_IN_IN_PROGRESS);

         // ---------------------- Get HTTP message
         LogicalMessage lm = ((LogicalMessageContext) lmc).getMessage();
         if (lm == null) {
            wsLog.addStatus(WSHandlerStatus.HTTP_IN_NO_BODY);
            throw new Exception("lmc.getMessage() returned null");
         }
         Source lmSrc = lm.getPayload();
         if (lmSrc == null) {
            wsLog.addStatus(WSHandlerStatus.HTTP_IN_NO_BODY);
            throw new Exception("lm.getPayload() returned null");
         }
         try {
            String lmStr = HandlerUtil.getXMLFromSource(lmSrc);
            wsLog.addMessage("HTTP Request message body",
               MessageType.HTTP_IN_BODY, lmStr);
         } catch (Exception e1) {
            wsLog.addStatus(WSHandlerStatus.HTTP_IN_BODY_ERR);
            throw new Exception("HandlerUtil.getXMLFromSource error: "
               + e1.getMessage());
         }

         wsLog.addStatus(WSHandlerStatus.HTTP_IN_OK);

         log.trace(Util.classMethod() + "processing completed.");

      } catch (Exception e) {
         logContextEntries(lmc, log);
         StringBuilder em =
            new StringBuilder("MsgHandler#handleMessage error: "
               + e.getMessage());
         if (wsLogTransaction != null)
            em.append(nl).append(wsLogTransaction.getWsLog().toStringLong())
               .append(nl)
               .append(wsLogTransaction.getWsLog().toStringClientIfno());
         log.warn(em);
      }
   } // EO handleOutboundMessage method

   private boolean logContextEntries(MessageContext logicalMessageContext,
      Logger log) {
      if (log.isDebugEnabled()) {
         Set <Entry <String, Object>> entries =
            logicalMessageContext.entrySet();
         for (Entry <String, Object> entry : entries) {
            Object o = entry.getValue();
            log.debug(entry.getKey() + " = "
               + ((o == null) ? "null" : o.toString()));
         }
         return true;
      }
      return false;
   }

   @Override
   public void close(MessageContext lmc) {}

} // EO HTTPLogging handler class
