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
package edu.wustl.mir.erl.ihe.ws.handlers;

import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.client.ClientTest;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.MessageType;
import edu.wustl.mir.erl.ihe.ws.db.TransactionType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * {@link SOAPHandler} implementation designed to:
 * <ul>
 * <li>Be the first handler for inbound messages and the last handler for
 * outbound messages.</li>
 * <li>Collect inbound and outbound SOAP related status information for the
 * transaction.</li>
 * </ul>
 * <b>Processing for inbound messages:</b> 
 * <b>Note:</b> All working storage is inside the method bodies, because handler
 * instances are not thread safe. Passing of information is done in the context.
 */
public class SOAPClientHandler extends BaseHandler <SOAPMessageContext>
   implements SOAPHandler <SOAPMessageContext> {

   private static final String WSA_URL = "http://www.w3.org/2005/08/addressing";
   private static final QName WSA_ACTION = new QName(WSA_URL, "Action");
   private static final QName WSA_MESSAGE_ID = new QName(WSA_URL, "MessageID");
   private static final QName WSA_REPLY_TO = new QName(WSA_URL, "ReplyTo");
   private static final QName WSA_TO = new QName(WSA_URL, "To");

   /**
    * Required no argument constructor
    */
   public SOAPClientHandler() {
      super();
      super.setHandlerTypeName(TransactionType.SOAP_CLIENT,
         "SOAP MSG Handler - SOAP Client");
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
   // ------------------------ Outbound message (Request) processing
   // **************************************************************
   private void handleOutboundMessage(SOAPMessageContext soapMessageContext) {

      Logger log = Util.getLog();
      LogTransaction logTransaction = null;
      ClientTest clientTest = null;
      WSLog wsLog = null;

      try {
         logContextEntries(soapMessageContext, log);

         logTransaction =
            (LogTransaction) soapMessageContext.get(WS_LOG_TRANSACTION);
         wsLog = logTransaction.getWsLog();
         log = logTransaction.getLog();
         clientTest = logTransaction.getClientTest();

         wsLog.addStatus(WSHandlerStatus.SOAP_OUT_IN_PROGRESS);

         // Add WS Addressing headers
         SOAPMessage message = soapMessageContext.getMessage();
         if (message.getSOAPPart().getEnvelope().getHeader() == null) {
            message.getSOAPPart().getEnvelope().addHeader();
         }
         SOAPHeader soapHeader =
            message.getSOAPPart().getEnvelope().getHeader();

         // wsa:Action header element
         SOAPHeaderElement action = soapHeader.addHeaderElement(WSA_ACTION);
         action.setMustUnderstand(true);
         action.addTextNode(clientTest.getAction());

         // wsa:To header element
         SOAPHeaderElement to = soapHeader.addHeaderElement(WSA_TO);
         to.addTextNode((String) soapMessageContext.get(SOAP_ENDPOINT_ADDRESS));

         // wsa:MessageID header element
         SOAPHeaderElement messageId =
            soapHeader.addHeaderElement(WSA_MESSAGE_ID);
         String uuid = UUID.randomUUID().toString();
         messageId.addTextNode("urn:uuid:" + uuid);

         // ------------------------------------------SOAP values
         wsLog.setSoapActionURI((String) soapMessageContext
            .get(SOAP_ACTION_URI));
         QName qn = (QName) soapMessageContext.get(WSDL_SERVICE);
         wsLog.setWsdlService(qn.toString());
         wsLog.setSoapMessageId(uuid);
         wsLog.setServiceEndpointAddress((String) soapMessageContext
            .get(SOAP_ENDPOINT_ADDRESS));

         // -------------------------------------------- soap message
         SOAPMessage soapMsg = soapMessageContext.getMessage();
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         soapMsg.writeTo(out);
         String strMsg = out.toString(Util.UTF_8);
         wsLog.addMessage("inbound SOAP", MessageType.SOAP_IN, strMsg);
         
         // store SOAP message
         if (clientTest.isStoreSOAPMessages()) {
            String subDir = clientTest.getSubDirectoryName();
            Path dirp =
               StoreSOAPMessages.storeMessage(subDir, strMsg,
                  MessageType.SOAP_IN);
            logTransaction.setStoreSOAPMessagesDirectoryPath(dirp);

            String p1 = StringUtils.substringBetween(strMsg, "prepopData");
            String p2 = StringUtils.substringAfter(p1, ">");
            String p3 = StringUtils.substringBeforeLast(p2, "<");
            if (StringUtils.isNotBlank(p3)) {
               StoreSOAPMessages.storeMessage(dirp, p3, MessageType.SOAP_PREPOP);
            }
         }
         

         wsLog.addStatus(WSHandlerStatus.SOAP_IN_OK);

         wsLog.addStatus(WSHandlerStatus.SOAP_OUT_OK);

      } catch (Exception e) {
         logContextEntries(soapMessageContext, log);
         StringBuilder em =
            new StringBuilder(Util.classMethod() + " error: " + e.getMessage());
         if (logTransaction != null)
            em.append(nl).append(logTransaction.getWsLog().toStringLong())
               .append(nl)
               .append(logTransaction.getWsLog().toStringClientIfno());
         log.warn(em);
      }
   } // EO handleOutboundMessage

   // **************************************************************
   // ------------------------ Inbound message (Response) processing
   // **************************************************************
   private void handleInboundMessage(SOAPMessageContext soapMessageContext) {

      Logger log = Util.getLog();
      LogTransaction logTransaction = null;
      ClientTest clientTest = null;
      WSLog wsLog = null;

      try {
         logContextEntries(soapMessageContext, log);

         logTransaction =
            (LogTransaction) soapMessageContext.get(WS_LOG_TRANSACTION);
         clientTest = logTransaction.getClientTest();
         wsLog = logTransaction.getWsLog();
         log = logTransaction.getLog();

         wsLog.addStatus(WSHandlerStatus.SOAP_IN_IN_PROGRESS);

         // -------------------------------------------- Http stuff
         wsLog.setHttpResponseCode((Integer) soapMessageContext
            .get(HTTP_RESPONSE_CODE));
         @SuppressWarnings("unchecked")
         Map <String, List <String>> headers =
            (Map <String, List <String>>) soapMessageContext
               .get(HTTP_RESPONSE_HEADERS);
         if (headers == null) {
            log.warn(NO_MAP + HTTP_REQUEST_HEADERS);
         } else {
            wsLog.setHttpResponseHeaders(headers);
            log.trace("http request headers:" + HandlerUtil.outputMap(headers));
         }

         // ------------------------------------ SOAP response message
         SOAPMessage soapMsg = soapMessageContext.getMessage();
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         soapMsg.writeTo(out);
         String strMsg = out.toString(Util.UTF_8);
         wsLog.addMessage("outbound SOAP ", MessageType.SOAP_OUT, strMsg);
         
         if (clientTest.isStoreSOAPMessages()) {
            Path dir = logTransaction.getStoreSOAPMessagesDirectoryPath();
            StoreSOAPMessages.storeMessage(dir, strMsg, MessageType.SOAP_OUT);
         }

         wsLog.addStatus(WSHandlerStatus.SOAP_IN_OK);

      } catch (Exception e) {
         logContextEntries(soapMessageContext, log);
         StringBuilder em =
            new StringBuilder(Util.classMethod() + " error: " + e.getMessage());
         if (logTransaction != null)
            em.append(nl).append(logTransaction.getWsLog().toStringLong())
               .append(nl)
               .append(logTransaction.getWsLog().toStringClientIfno());
         log.warn(em);
      }

   } // EO Inbound message handling

   /*
    * (non-Javadoc)
    * 
    * @see javax.xml.ws.handler.soap.SOAPHandler#getHeaders()
    */
   @Override
   public Set <QName> getHeaders() {
      return null;
   }

   /**
    * Logs all objects in the passed
    * {@link javax.xml.ws.handler.soap.SOAPMessageContext SOAPMessageContext}
    * 
    * @param soapMessageContext SOAPMessageContext to log
    * @param log {@link org.apache.log4j.Logger Logger} to use.
    */
   private void logContextEntries(SOAPMessageContext soapMessageContext,
      Logger log) {
      if (log.isDebugEnabled()) {
         Set <Entry <String, Object>> entries = soapMessageContext.entrySet();
         for (Entry <String, Object> entry : entries) {
            Object o = entry.getValue();
            log.debug(entry.getKey() + " = "
               + ((o == null) ? "null" : o.toString()));
         }
      }
   }

} // EO SOAPClientHandler class
