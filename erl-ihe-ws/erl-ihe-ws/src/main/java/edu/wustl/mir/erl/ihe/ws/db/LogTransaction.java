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
package edu.wustl.mir.erl.ihe.ws.db;

import java.io.Serializable;
import java.net.InetAddress;
import java.nio.file.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.server.FileServer;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.client.ClientTest;
import edu.wustl.mir.erl.ihe.ws.server.WSEndpoint;

/**
 *  TODO Need to rework this doc after handlers are finished.
 * An instance of this class tracks a single HTTP or HTTP/SOAP transaction, 
 * making entries to the WSLog.
 * 
 * 
 * LogTransaction implements {@link java.lang.AutoCloseable AutoCloseable}, so
 * it can be instantiated using the "try using resources" syntax. If not, the
 * instance {@code close()} method needs to be invoked before returning from the
 * implementor method.<ul>
 * <li>The various status recording methods of this class should be invoked as
 * appropriate to signal validation progress and results; see the JavaDoc for
 * those methods for details.</li>
 * </ul>
 * @see edu.wustl.mir.erl.ihe.util.StatusHelper Status
 */
public class LogTransaction implements Serializable, AutoCloseable {
   private static final long serialVersionUID = 1L;

   private Logger log = Util.getLog();

   private WSLog wsLog;

   private WSEndpoint wsEndpoint = null;
   private FileServer fileServer = null;
   private ClientTest clientTest = null;
   
   private TransactionType transactionType;
   
   private Path storeSOAPMessagesDirectoryPath = null;
   
   
   /**
    * @return the {@link #storeSOAPMessagesDirectoryPath} value.
    */
   public Path getStoreSOAPMessagesDirectoryPath() {
      return storeSOAPMessagesDirectoryPath;
   }

   /**
    * @param storeSOAPMessagesDirectoryPath the {@link #storeSOAPMessagesDirectoryPath} to set
    */
   public void setStoreSOAPMessagesDirectoryPath(
      Path storeSOAPMessagesDirectoryPath) {
      this.storeSOAPMessagesDirectoryPath = storeSOAPMessagesDirectoryPath;
   }

   /**
    * Creates WSLogTransaction instance. Must declare TransactionType.
    * @param transType TransactionType for this transaction.
    */
   public LogTransaction(TransactionType transType) {
      wsLog = new WSLog();
      transactionType = transType;
      wsLog.setTransactionType(transactionType);
   }

   /**
    * Load log information from the passed WSEndpoint instance.
    * @param wsEndpnt {@link WSEndpoint} instance for the service which received
    * this transaction.
    * @throws Exception if not {@link TransactionType#SOAP_SERVER} or if passed
    * WSEndpoint is null.
    */
   public void loadFrom(WSEndpoint wsEndpnt) throws Exception {
      is(TransactionType.SOAP_SERVER);
      if (wsEndpnt == null) throw new Exception("loadFrom null WSEndpoint");
      log = wsEndpnt.getLog();
      wsEndpoint = wsEndpnt;
      wsLog.setLog(log);
      wsLog.setServerPort(wsEndpnt.getPort());
      wsLog.setServiceName(wsEndpnt.getEndpointName());
      wsLog.setSecure(wsEndpnt.isSecure());
      if (wsEndpnt.isSecure()) {
         wsLog.setCertificates(wsEndpnt.getKeystoreFileName());
      }
   }
   
   /**
    * Load log information from the passed ClientTest instance.
    * @param cltTest {@link ClientTest} instance of the client sending this
    * transaction.
    * @throws Exception if not {@link TransactionType#SOAP_CLIENT} or if passed
    * ClientTest is null.
    */
   public void loadFrom(ClientTest cltTest) throws Exception {
      is(TransactionType.SOAP_CLIENT);
      if (cltTest == null) throw new Exception("loadFrom null WSEndpoint");
      log = cltTest.getLog();
      clientTest = cltTest;
      wsLog.setLog(log);
      wsLog.setClientIp(Util.getExternalHostIp());
      wsLog.setClientHostName(Util.getExternalHostName());
      wsLog.setSecure(clientTest.getSecure());
   }
   
   /**
    * Load remote (client) connections data
    * @param remoteInetAddress {@link java.net.InetAddress InetAddress}
    */
   public void loadFromRemote(InetAddress remoteInetAddress) {
      if (remoteInetAddress == null) return;
      wsLog.setClientHostName(remoteInetAddress.getCanonicalHostName());
      wsLog.setClientIp(remoteInetAddress.getHostAddress());
   }/**
    * Load local (server) connections data
    * @param localInetAddress {@link java.net.InetAddress InetAddress}
    */
   public void loadFromLocal(InetAddress localInetAddress) {
      if (localInetAddress == null) return;
      wsLog.setServerName(localInetAddress.getCanonicalHostName());
      wsLog.setServerIp(localInetAddress.getHostAddress());
   }

   /**
    * Log a status code for the current transaction. For example
    * 
    * <pre>
    * {@code wlog.addStatus(RFDStatus.MSG_OK);}
    * </pre>
    * 
    * <p>This form would normally be used in cases where the Status message alone
    * is sufficient to document the event.
    * </p>
    * 
    * @param statusCode An {@link java.lang.Enum enum} instance which implements
    * the {@link Status} interface, for example RFDStatus.
    */
   public void addStatus(Status statusCode) {
      wsLog.addStatus(statusCode);
   }

   /**
    * Log a status code and message for the current transaction. For example
    * 
    * <pre>
    * {@code wlog.addStatus(RFDStatus.MSG_ERROR,"Form ID invalid");  or
    * wlog.addStatus(RFDStatus.MSG_EXCEPTION,ex.getMessage());}
    * </pre>
    * 
    * This form would normally be used in cases where the Phase-Status message
    * indicates the type of event, but additional description is useful.
    * 
    * @param statusCode An {@link java.lang.Enum enum} instance which implements
    * the {@link Status} interface, for example RFDStatus.
    * @param errorMessage A String conveying additional information about the
    * status.
    */
   public void addStatus(Status statusCode, String errorMessage) {
      wsLog.addStatus(statusCode);
      wsLog.setErrorMessage(errorMessage);
   }

   /**
    * Log a status code, message, and additional information related to a
    * parsing error for the current transaction. For example, when catching a
    * {@link org.xml.sax.SAXParseException SAXParseException}, which has
    * properties giving the line and column number of the error:
    * 
    * <pre>
    * catch (Exception e) {
    *   if (e instanceof SAXParseException) {
    *      SAXParseException s = (SAXParseException) e;
    *      wslog.addStatusParseError(RFDStatus.MSG_PARSE_ERROR,
    *         s.getMessage(), s.getColumnNumber(),
    *         s.getLineNumber(), "");
    *   }
    * }
    * </pre>
    * 
    * @param statusCode An {@link java.lang.Enum enum} instance which implements
    * the {@link Status} interface, for example RFDStatus.
    * @param errorMessage A String conveying additional information about the
    * status.
    * @param errorColumn column in which error occurred, from exception.
    * @param errorLine line in which error occurred, from exception.
    * @param errorSubstring string containing section of message where error
    * occurred, from exception.
    */
   public void addStatusParseError(Status statusCode, String errorMessage,
      int errorColumn, int errorLine, String errorSubstring) {
      wsLog.addStatus(statusCode);
      wsLog.setErrorMessage(errorMessage);
      wsLog.setErrorColumn(errorColumn);
      wsLog.setErrorLine(errorLine);
      wsLog.setErrorSubstring(errorSubstring);
   }

   /**
    * Add a message to the data logged for this transaction.
    * 
    * @param description A short, human readable description of the message.
    * @param type {@link MessageType} for the message, for example, HTTP_BODY,
    * or SOAP.
    * @param message actual message in string form.
    */
   public void logMessage(String description, MessageType type, String message) {
      try {
         if (type == null) type = MessageType.UNKNOWN;
         if (StringUtils.trimToNull(description) == null)
            throw new Exception("no description");
         if (message == null) throw new Exception("no message");
         wsLog.addMessage(description, type, message);
      } catch (Exception e) {
         log.warn("WSLogTransaction.logMessage error: " + e.getMessage());
      }
   }

   @Override
   public void close() throws Exception {
      log.trace(Util.classMethod() + "invoked");
      if (wsEndpoint != null) wsEndpoint.persistWSLog(wsLog);
      else if (fileServer != null) fileServer.persistWSLog(wsLog);
      else if (clientTest != null) clientTest.persistWSLog(wsLog);
      else throw new Exception("No WSEndpoint, FileServer, or ClientTest");
   }
   
   /**
    * @return the {@link #fileServer} value.
    */
   public FileServer getFileServer() {
      return fileServer;
   }

   /**
    * @param fileServer the {@link #fileServer} to set
    */
   public void setFileServer(FileServer fileServer) {
      this.fileServer = fileServer;
   }

   /**
    * @return the {@link #wsEndpoint} value.
    */
   public WSEndpoint getWsEndpoint() {
      return wsEndpoint;
   }

   /**
    * @return logger for this transaction
    */
   public Logger getLog() {
      return log;
   }

   /**
    * @return the {@link WSLog} instance for this transaction.
    */
   public WSLog getWsLog() {
      return wsLog;
   }
   
   /**
    * @return {@link TransactionType} for this transaction.
    */
   public TransactionType getTransactionType() {
      return transactionType;
   }

   /**
    * @return the {@link LogTransaction#toStringLong()} value.
    */
   public String toStringLong() {
      return getWsLog().toStringLong();
   }
   
   private void is(TransactionType tt) throws Exception {
      if (transactionType != tt) throw new Exception("not " + tt.toString());
   }

   /**
    * @return ClientTest instance for this transaction
    */
   public ClientTest getClientTest() {
      return clientTest;
   }

} // EO WSLogTransaction class
