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

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpExchange;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.MessageType;
import edu.wustl.mir.erl.ihe.ws.db.TransactionType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.server.WSEndpoint;

/**
 * {@link SOAPHandler} implementation designed to:
 * <ul>
 * <li>Be the first handler for inbound messages and the last handler for
 * outbound messages.</li>
 * <li>Collect inbound and outbound SOAP related status information for the
 * transaction.</li>
 * </ul>
 * <b>Processing for inbound messages:</b>
 * <ul>
 * <li>Retrieves the
 * {@link edu.wustl.mir.erl.ihe.ws.WSProperties#HTTP_EXCHANGE HTTP_EXCHANGE}
 * property, an {@link com.sun.net.httpserver.HttpExchange HttpExchange} object,
 * from the {@link javax.xml.ws.handler.soap.SOAPMessageContext SOAP Message
 * Context} and retrieves the HTTP Request path and the
 * {@link java.net.InetSocketAddress InetSocketAddress} for the local and remote
 * connectors.</li>
 * <li>Locates and loads the {@link edu.wustl.mir.erl.ihe.ws.server.WSEndpoint
 * WSEndpoint} instance corresponding to this transaction.</li>
 * <li>Creates a {@link LogTransaction} instance to store log information
 * regarding this messages and its response, and posts relevant data from the
 * {@link WSEndpoint} and the connectors for the message to it.</li>
 * <li>Inserts the WSLogTransaction instance into the
 * {@link LogicalMessageContext} for use by other handlers and the implementor.</li>
 * </ul>
 * <b>Processing for outbound messages:</b>
 * <ul>
 * <li>Retrieves the stored WSLogTransaction instance from the message context.</li>
 * <li>Posts the connection close time (approximate) to the WSLogTransaction
 * instance.</li>
 * <li>Closes the WSLogTransaction instance, persisting the log data for the
 * transaction.</li>
 * </ul>
 * <b>Notes:</b>
 * <ul>
 * <li>Must be the first Handler for inbound messages.</li>
 * <li>MOD Refractor out loading the WSLogTransaction</li>
 * </ul>
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class SOAPServerHandler extends BaseHandler <SOAPMessageContext>
   implements SOAPHandler <SOAPMessageContext> {

   /**
    * Required no argument constructor
    */
   public SOAPServerHandler() {
      super();
      super.setHandlerTypeName(TransactionType.SOAP_SERVER,
         "SOAP MSG Handler - SOAP Server");
   }

   /**
    * Pull Message outbound property and invokes inbound or outbound message
    * processor routines.
    * 
    * @param soapMessageContext message context.
    */
   @Override
   public boolean handleMessage(SOAPMessageContext soapMessageContext) {
      Boolean outbound = null;

      // logContextEntries(soapMessageContext, Util.getLog());
      try {
         outbound = (boolean) soapMessageContext.get(MESSAGE_OUTBOUND_PROPERTY);
         if (outbound == null)
            throw new Exception(NO_MAP + MESSAGE_OUTBOUND_PROPERTY);
         if (outbound) handleOutboundMessage(soapMessageContext);
         else handleInboundMessage(soapMessageContext);
      } catch (Exception ex) {
         StringBuilder em =
            new StringBuilder("MsgHandler#handleMessage error: "
               + ex.getMessage());
         Util.getLog().warn(em);
      }
      return true;
   } // EO handleMessage method

   // **************************************************************
   // ------------------------- Inbound message (Request) processing
   // **************************************************************
   private void handleInboundMessage(SOAPMessageContext soapMessageContext) {

      /*
       * All working storage is here, because handler instances are not thread
       * safe. Passing of information is done in the context.
       */

      // Start with std logger, switch to endpoint specific logger when known
      Logger log = Util.getLog();
      LogTransaction wsLogTransaction = null;
      WSLog wsLog = null;
      HttpExchange httpExchange = null;

      // InetSocketAddress remoteInetSocketAddress = null;
      // InetAddress remoteInetAddress = null;
      //
      // InetSocketAddress localInetSocketAddress = null;
      // InetAddress localInetAddress = null;
      // Integer localPort = null;
      //
      // URI requestURI = null;
      // String requestURIPath = null;

      try {

         // ------------------------------- Pull data from HTTP Exchange
         httpExchange = (HttpExchange) soapMessageContext.get(HTTP_EXCHANGE);
         if (httpExchange == null) { throw new Exception(NO_MAP + HTTP_EXCHANGE); }

         wsLogTransaction =
            (LogTransaction) httpExchange.getAttribute(WS_LOG_TRANSACTION);
         wsLog = wsLogTransaction.getWsLog();
         log = wsLogTransaction.getLog();

         /*
          * Put log transaction in message context so that the implementor will
          * be able to get it.
          */
         soapMessageContext.put(WS_LOG_TRANSACTION, wsLogTransaction);
         soapMessageContext.setScope(WS_LOG_TRANSACTION,
            MessageContext.Scope.APPLICATION);

         wsLog.addStatus(WSHandlerStatus.SOAP_IN_IN_PROGRESS);

         // ------------------------------------------SOAP values
         wsLog.setSoapActionURI((String) soapMessageContext
            .get(SOAP_ACTION_URI));
         QName qn = (QName) soapMessageContext.get(WSDL_SERVICE);
         wsLog.setWsdlService(qn.toString());
         wsLog.setSoapMessageId((String) soapMessageContext
            .get(SOAP_MESSAGE_ID));

         // -------------------------------------------- soap message
         SOAPMessage soapMsg = soapMessageContext.getMessage();
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         soapMsg.writeTo(out);
         String strMsg = out.toString(Util.UTF_8);
         wsLog.addMessage("inbound SOAP", MessageType.SOAP_IN, strMsg);
         
         // store SOAP message
         if (wsLogTransaction.getWsEndpoint().isStoreSOAPMessages()) {
            String subDir = wsLogTransaction.getWsEndpoint().getSubDirectoryName();
            Path dirp = StoreSOAPMessages.storeMessage(subDir, strMsg, MessageType.SOAP_IN);
            wsLogTransaction.setStoreSOAPMessagesDirectoryPath(dirp);

            String p1 = StringUtils.substringBetween(strMsg, "prepopData");
            String p2 = StringUtils.substringAfter(p1, ">");
            String p3 = StringUtils.substringBeforeLast(p2, "<");
            if (StringUtils.isNotBlank(p3)) {
               StoreSOAPMessages.storeMessage(dirp, p3, MessageType.SOAP_PREPOP);
            }
         }

         wsLog.addStatus(WSHandlerStatus.SOAP_IN_OK);
         
         

      } catch (Exception e) {
         logContextEntries(soapMessageContext, log);
         StringBuilder em =
            new StringBuilder(Util.classMethod() + " error: " + e.getMessage());
         if (wsLogTransaction != null)
            em.append(nl).append(wsLogTransaction.getWsLog().toStringLong())
               .append(nl)
               .append(wsLogTransaction.getWsLog().toStringClientIfno());
         log.warn(em);
      }
   } // EO handleMessage method

   // **************************************************************
   // ----------------------- Outbound message (Response) processing
   // **************************************************************
   private void handleOutboundMessage(SOAPMessageContext soapMessageContext) {

      LogTransaction wsLogTransaction = null;
      WSLog wsLog = null;
      Logger log = Util.getLog();
      HttpExchange httpExchange = null;

      try {

         wsLogTransaction =
            (LogTransaction) soapMessageContext.get(WS_LOG_TRANSACTION);
         if (wsLogTransaction == null)
            throw new Exception(NO_MAP + WS_LOG_TRANSACTION);
         wsLog = wsLogTransaction.getWsLog();
         log = wsLogTransaction.getLog();

         httpExchange = (HttpExchange) soapMessageContext.get(HTTP_EXCHANGE);
         httpExchange.setAttribute(WS_LOG_TRANSACTION, wsLogTransaction);

         wsLog.addStatus(WSHandlerStatus.SOAP_OUT_IN_PROGRESS);

         // ------------------------------------ SOAP response message
         SOAPMessage soapMsg = soapMessageContext.getMessage();
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         soapMsg.writeTo(out);
         String strMsg = out.toString(Util.UTF_8);
         wsLog.addMessage("outbound SOAP ", MessageType.SOAP_OUT, strMsg);
         
         if (wsLogTransaction.getWsEndpoint().isStoreSOAPMessages()) {
            Path dir = wsLogTransaction.getStoreSOAPMessagesDirectoryPath();
            StoreSOAPMessages.storeMessage(dir, strMsg, MessageType.SOAP_OUT);
         }

         wsLog.addStatus(WSHandlerStatus.SOAP_OUT_OK);

      } catch (Exception eo) {
         logContextEntries(soapMessageContext, log);

         StringBuilder em = new StringBuilder("SOAP out error " + nl);
         if (wsLogTransaction != null)
            em.append(wsLogTransaction.toStringLong() + nl);
         em.append(eo.getMessage());
         log.warn(em);
      }
   } // EO handleOutboundMessage method

   private boolean logContextEntries(SOAPMessageContext soapMessageContext,
      Logger log) {
      if (log.isDebugEnabled()) {
         Set <Entry <String, Object>> entries = soapMessageContext.entrySet();
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
   public boolean handleFault(SOAPMessageContext soapMessageContext) {
      handleOutboundMessage(soapMessageContext);
      return true;
   }

   /*
    * (non-Javadoc)
    * 
    * @see javax.xml.ws.handler.soap.SOAPHandler#getHeaders()
    */
   @Override
   public Set <QName> getHeaders() {
      return null;
   }

} // EO SOAP Message Handler class
