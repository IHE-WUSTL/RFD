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
package edu.wustl.mir.erl.ihe.ws.db;

import java.net.URI;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.javatuples.LabelValue;

import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.UtilProperties;
import edu.wustl.mir.erl.ihe.util.jdbc.DBHelper;
import edu.wustl.mir.erl.ihe.util.jdbc.DBTable;
import edu.wustl.mir.erl.ihe.util.jdbc.Helper;
import edu.wustl.mir.erl.ihe.util.jdbc.JDBC;
import edu.wustl.mir.erl.ihe.util.jdbc.JDBC.Connection;
import edu.wustl.mir.erl.ihe.util.jdbc.Queries;
import edu.wustl.mir.erl.ihe.util.jdbc.Query;

/**
 * Java bean for WS transaction logging table. One row is made in the table for
 * each transaction sent by a a user to a WS Endpoints. Includes:
 * <ul>
 * <li>Object representation of table row.</li>
 * <li>List of {@link WSMsg} instances logged for this transaction.</li>
 * <li>sql inner Enum containing queries for this table.</li>
 * <li>Constructor and methods to build objects from JDBC
 * {@link java.sql.ResultSet ResultSet}</li>
 * <li>Insert method.
 * <li>Comparator for sorting arrays of instances</li>
 * <li>toString method useful for inserting object in log.</li>
 * </ul>
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class WSLog implements DBTable, UtilProperties, WSLogProperties {
   private static final long serialVersionUID = 1L;

   @Override
   public String getLogicalDbName() {
      return LOGICAL_DB_NAME;
   }

   private transient Logger log = Util.getLog();

   @Override
   public void setLog(Logger logger) {
      log = logger;
   }
   /**
    * @return Logger for this transaction
    */
   public Logger getLog() {
      return log;
   }

   // ********************************************************
   // Persisted properties. (Object representation of table)
   // ********************************************************
   // TODO Update for multiple changes in table.

   /**
    * Primary unique key for this WSMsg row. DBA
    */
   private Integer id = null;
   // ------------------------------------ connection data
   /**
    * Web Service client IP address in dot format.
    */
   private String clientIp = ""; 
   /**
    * Web Service client Canonical host name.
    */
   private String clientHostName = ""; 
   /**
    * Web Service Server IP address in dot format.
    */
   private String serverIp = "";
   /**
    * Web Service server listen port.
    */
   private Integer serverPort = 0; 
   /**
    * Is connection secure (https)
    */
   private Boolean secure = null; 
   /**
    * Server certificate file name, or blank for non-secure connections.
    */
   private String certificates = ""; 
   /**
    * WS wsdl service name
    */
   private String serviceName = ""; 
   /**
    * Web Service Server canonical host name.
    */
   private String serverName = "";
   /**
    * Web Service Endpoint Address
    */
   private String serviceEndpointAddress = "";
   /**
    * Approximate time connection opened.
    */
   private Date connOpenTime = new Date(); 
   /**
    * Approximate time connection closed.
    */
   private Date connCloseTime = null;

   private TransactionType transactionType;
   // ------------------------ general status data (all phases)
   /**
    * {@link Status} compatible codes which have been posted to this WSLog.
    */
   private List <Status> statuses = new ArrayList <>();
   /**
    * Human readable error message, or blank.
    */
   private String errorMessage = "";
   // ----------------------- general status for parsing errors
   /**
    * For parsing errors where line/column information is provided, the line on
    * which the error occurred. If no error of this type occurred, null.
    */
   private Integer errorLine = null;
   /**
    * For parsing errors where line/column information is provided, the column
    * on which the error occurred. If no error of this type occurred, null.
    */
   private Integer errorColumn = null;
   /**
    * For parsing errors where substring information is provided, the substring
    * in which the error occurred. If no error of this type occurred, null.
    */
   private String errorSubstring = null;
   // ---------------------------------------------- HTTP data
   /**
    * HTTP Request method code, for example "GET".
    */
   private String httpMethod = null; // HttpLogging
   /**
    * HTTP Request URI
    */
   private URI httpRequestURI = null; // HttpLogging
   /**
    * HTTP Protocol for Request, HTTP 1.1
    */
   private String httpRequestProtocol = null; //
   /**
    * Map containing all the headers attached to the http Request. <b>Note:</b>
    * This property is <b>NOT</b> the {@link com.sun.net.httpserver.Headers
    * Headers} instance from the {@link com.sun.net.httpserver.HttpExchange
    * HttpExchange} instance. It is a deep <b>COPY</b> of the
    * {@code Map<String, List<String>>} contained in the Headers instance.
    * Changes made to this property will have no effect on the HttpExchange.
    */
   private Map <String, List <String>> httpRequestHeaders = null; // HttpLogging
   /**
    * The http Response code, for example, 500. See RFC-2616 section 10.
    */
   private Integer httpResponseCode = 0; // HttpLogging
   /**
    * Map containing all the headers attached to the http Response. <b>Note:</b>
    * This property is <b>NOT</b> the {@link com.sun.net.httpserver.Headers
    * Headers} instance from the {@link com.sun.net.httpserver.HttpExchange
    * HttpExchange} instance. It is a deep <b>COPY</b> of the
    * {@code Map<String, List<String>>} contained in the Headers instance.
    * Changes made to this property will have no effect on the HttpExchange.
    */
   private Map <String, List <String>> httpResponseHeaders = null; //

   // ---------------------------------------------------- SOAP data

   /**
    * SOAP Action URI, for example, urn:ihe:iti:2007:RetrieveForm
    * message context key: javax.xml.ws.soap.http.soapaction.uri
    */
   private String soapActionURI = null;

   /**
    * WSDL service name, for example, {urn:ihe:iti:rfd:2007}FormManager_Service 
    * message context key: javax.xml.ws.wsdl.service
    */
   private String wsdlService = null;

   /**
    * SOAP Message ID, for example, urn:uuid:76A2C3D9BCD3AECFF31217932910053 
    * message context key:com.sun.xml.internal.ws.api.addressing.messageId
    */
   private String soapMessageId = null;

   // ---------------------------------- SOAP Request/Response Label/Value pairs
   /**
    * The name of the Web Service request. For example "RetrieveFormRequest".
    */
   private String soapRequestName = null;
   /**
    * Name/Value pairs stored for the SOAP Request, as determined by the
    * specific test. Within each Pair in the List:
    * <ul>
    * <li>{@link LabelValue#getLabel()} is the {@link String} label for the
    * pair, for example "Age".</li>
    * <li>{@link LabelValue#getValue()} is the {@link Object} value of the
    * pair, for example {@link Integer} 34.</li>
    * </ul>
    */
   private List <LabelValue <String, Object>> soapRequestLabelValuePairs =
      new ArrayList <>();

   /**
    * The name of the Web Service response, for example "RetrieveFormResponse".
    */
   private String soapResponseName = null;
   /**
    * Name/Value pairs stored for the SOAP Request, as determined by the
    * specific test. Within each Pair in the List:
    * <ul>
    * <li>{@link LabelValue#getLabel()} is the {@link String} label for the
    * pair, for example "Age".</li>
    * <li>{@link LabelValue#getValue()} is the {@link Object} value of the
    * pair, for example {@link Integer} 34.</li>
    * </ul>
    */
   private List <LabelValue <String, Object>> soapResponseLabelValuePairs =
      new ArrayList <>();

   // ********************************************************
   // Non-persisted (transient) properties
   // ********************************************************

   /**
    * When dealing with an array of instances in a table, used to indicate
    * whether this instance is selected
    */
   private boolean selected = false;

   /**
    * Messages logged for this transaction. For new instances created with
    * {@link #WSLog()} this list will be empty. For existing instances, it will
    * be lazy loaded.
    */
   private List <WSMsg> wsMsgs = null;

   /**
    * {@link DBHelper} for this database class.
    */
   public static DBHelper <WSLog> helper = new DBHelper <WSLog>(WSLog.class);

   @Override
   public DBHelper <?> getDBHelper() {
      return helper;
   }

   private static Queries queries = JDBC.getQueries(WSLog.class);

   /**
    * enum of SQL queries and updates for this table.
    */
   public enum sql {
      /**
       * Create WSLog table in database.
       */
      create(),
      /**
       * Insert a single instance of WSLog as a new row in the database.
       */
      insert();

      private final String[] query;

      sql() {
         query = queries.loadQuery(this.name());
      }

      /**
       * @return String[] containing the query for this sql instance.
       */
      public String[] getQuery() {
         return query;
      }

   } // EO sql Enum

   /**
    * Returns query for passed name.
    * 
    * @param name String name of query
    * @return String[] query, one SQL statement per String in array.
    * @throws Exception if no such query.
    */
   public static String[] query(String name) throws Exception {
      String[] query = queries.getQuery(name);
      if (query == null)
         throw new Exception("WSLog error. No such query " + name);
      return query;
   }

   /**
    * No argument constructor creates instance with default (mostly, empty)
    * values, no ID. Use this when creating a new instance, with an empty WSMsg
    * list.
    */
   public WSLog() {
      wsMsgs = new ArrayList <>();
   }

   /**
    * Builds instance from the next row of the passed ResultSet.
    * 
    * @param resultSet ResultSet. Must be positioned just before row for which
    * instance is desired. Use this when retrieving an instance from the
    * database.
    * @return WSLog instance, or null if there is no next row in the ResultSet.
    * @throws Exception on error.
    */
   public WSLog getWSLog(ResultSet resultSet) throws Exception {
      return helper.loadNextRow(resultSet);
   }

   /**
    * Return an array of WSLog instances, one for each row in the passed
    * ResultSet.
    * 
    * @param resultSet Query results
    * @return WSLog[]. May have 0 elements, but will not be null.
    * @throws Exception on error.
    */
   public WSLog[] getWSLogs(ResultSet resultSet) throws Exception {
      List <WSLog> wsLogs = new ArrayList <>();
      wsLogs.add(helper.loadNextRow(resultSet));
      return wsLogs.toArray(new WSLog[0]);
   }

   /**
    * Convenience method for
    * {@link #insert(edu.wustl.mir.erl.ihe.util.jdbc.JDBC.Connection)
    * insert(Connection)}. Gets a connection, persists the WSLog instance,
    * closes and cleans up.
    * 
    * @return integer primary key (DBA) assigned to this row by the database.
    * @throws Exception on SQL error.
    */
   public int insert() throws Exception {
      try (JDBC.Connection conn = JDBC.getConnection()) {
         return insert(conn);
      }
   }

   /**
    * Persists this WSLog instance. This does <b>NOT</b> persist any WSMsg
    * instances which may have been added to the WSLog instance.
    * 
    * @param conn {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC.Connection
    * Connection}
    * @return int id assigned by the database to the new row.
    * @throws Exception on error.
    */
   public int insert(JDBC.Connection conn) throws Exception {
      if (id != null) throw new Exception("attempt to re-insert WSLog record");
      id = new Query(WSLog.sql.insert).setAll(this).dbInsertOne(conn);
      return id;
   }

   /**
    * Creates a new {@link WSMsg} instance and adds it to this WSLog instance.
    * Still must be persisted.
    * 
    * @param description {@link WSMsg#getDescription}
    * @param msgType {@link WSMsg#getMsgType}
    * @param message {@link WSMsg#getMessage}
    */
   public void addMessage(String description, MessageType msgType,
      String message) {
      WSMsg wsMsg = new WSMsg(msgType, description, message);
      wsMsgs.add(wsMsg);
      log.trace(description + " " + msgType.toString() + nl + wsMsg.getPretty());
   }

   // ********************************************************
   // Standard getters and setters
   // ********************************************************
   /**
    * @return current {@link #id} value
    */
   public Integer getId() {
      return id;
   }

   /**
    * set {@link #id} value
    * 
    * @param id new value
    */
   public void setId(Integer id) {
      this.id = id;
   }

   /**
    * @return current {@link #clientIp} value
    */
   @Helper(compareUsing = DBHelper.COMPARE_IP)
   public String getClientIp() {
      if (transactionType == TransactionType.CDA_CLIENT) 
         return "Not applicable";
      return clientIp;
   }

   /**
    * set {@link #clientIp} value
    * 
    * @param clientIp new value
    */
   public void setClientIp(String clientIp) {
      this.clientIp = clientIp;
   }

   /**
    * @return current {@link #clientHostName} value
    */
   public String getClientHostName() {
      return clientHostName;
   }

   /**
    * set {@link #clientHostName} value
    * 
    * @param clientHostName new value
    */
   public void setClientHostName(String clientHostName) {
      this.clientHostName = clientHostName;
   }

   /**
    * @return current {@link #serviceName} value
    */
   public String getServiceName() {
      return serviceName;
   }

   /**
    * set {@link #serviceName} value
    * 
    * @param serviceName new value
    */
   public void setServiceName(String serviceName) {
      this.serviceName = serviceName;
   }

   /**
    * @return current {@link #serverName} value
    */
   public String getServerName() {
      return serverName;
   }

   /**
    * set {@link #serverName} value
    * 
    * @param serverName new value
    */
   public void setServerName(String serverName) {
      this.serverName = serverName;
   }

   /**
    * @return the {@link #serviceEndpointAddress} value.
    */
   public String getServiceEndpointAddress() {
      return serviceEndpointAddress;
   }
   /**
    * @param serviceEndpointAddress the {@link #serviceEndpointAddress} to set
    */
   public void setServiceEndpointAddress(String serviceEndpointAddress) {
      this.serviceEndpointAddress = serviceEndpointAddress;
   }
   /**
    * @return current {@link #serverIp} value
    */
   @Helper(compareUsing = DBHelper.COMPARE_IP)
   public String getServerIp() {
      return serverIp;
   }

   /**
    * set {@link #serverIp} value
    * 
    * @param serverIp new value
    */
   public void setServerIp(String serverIp) {
      this.serverIp = serverIp;
   }

   /**
    * @return current {@link #serverPort} value
    */
   public Integer getServerPort() {
      return serverPort;
   }

   /**
    * set {@link #serverPort} value
    * 
    * @param serverPort new value
    */
   public void setServerPort(Integer serverPort) {
      this.serverPort = serverPort;
   }

   /**
    * @return is {@link #secure} true?
    */
   public Boolean isSecure() {
      return secure;
   }

   /**
    * set {@link #secure} value
    * 
    * @param secure new value
    */
   public void setSecure(Boolean secure) {
      this.secure = secure;
   }

   /**
    * @return current {@link #certificates} value
    */
   @Helper(ignoreComp = true)
   public String getCertificates() {
      return certificates;
   }

   /**
    * set {@link #certificates} value
    * 
    * @param certificates new value
    */
   public void setCertificates(String certificates) {
      this.certificates = certificates;
   }

   /**
    * @return current {@link #connOpenTime} value
    */
   public Date getConnOpenTime() {
      return connOpenTime;
   }

   /**
    * set {@link #connOpenTime} value
    * 
    * @param connOpenTime new value
    */
   public void setConnOpenTime(Date connOpenTime) {
      this.connOpenTime = connOpenTime;
   }

   /**
    * @return current {@link #connCloseTime} value
    */
   public Date getConnCloseTime() {
      return connCloseTime;
   }

   /**
    * set {@link #connCloseTime} value
    * 
    * @param connCloseTime new value
    */
   public void setConnCloseTime(Date connCloseTime) {
      this.connCloseTime = connCloseTime;
   }

   /**
    * @return the {@link #transactionType} value.
    */
   public TransactionType getTransactionType() {
      return transactionType;
   }

   /**
    * @param transactionType the {@link #transactionType} to set
    */
   public void setTransactionType(TransactionType transactionType) {
      this.transactionType = transactionType;
   }

   /**
    * External version of {@link #transactionType}
    * 
    * @return The name of the transactionType, for example "HTTP".
    */
   public String getType() {
      return transactionType.name();
   }

   /**
    * External version of {@link #transactionType}
    * 
    * @param type the name for the transactionType to be set. Not case
    * sensitive.
    */
   public void setType(String type) {
      transactionType = TransactionType.valueOf(type.toUpperCase());
   }

   /**
    * @return {@link #wsMsgs}
    */
   @Helper(ignoreComp = true, ignoreSQL = true, ignoreResultSet = true)
   public List <WSMsg> getWsMsgs() {
      if (wsMsgs == null) {
         try (Connection conn = JDBC.getConnection(WSMsg.LOGICAL_DB_NAME)) {
            ResultSet m =
               new Query(WSMsg.sql.wslogId).set("wslogId", id).dbQuery(conn);
            wsMsgs = WSMsg.loadRows(m);
         } catch (Exception e) {
            log.warn("Error loading WSMsgs: " + e.getMessage());
         }
      }
      return wsMsgs;
   }

   /**
    * @return is {@link #selected} true?
    */
   @Helper(ignoreComp = true, ignoreSQL = true)
   public boolean isSelected() {
      return selected;
   }

   /**
    * set {@link #selected} value
    * 
    * @param selected new value
    */
   @Helper(ignoreComp = true, ignoreResultSet = true)
   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   // Routines for db get and set for statuses

   /**
    * Converts {@link #statuses} List to an encoded String which can be stored
    * in the database.
    * 
    * @return encoded status string.
    * @see StatusHelper#exportToDb(List)
    */
   @Helper(ignoreComp = true)
   public String getStatusesTxt() {
      return StatusHelper.exportToDb(statuses);
   }

   /**
    * Converts a {@link #statuses} List which has been encoded to be stores in a
    * database to a List and places it in the statuses property of this instance
    * of WSLog.
    * 
    * @param statusesTxt String, encoded status List.
    * @see StatusHelper#importFromDb(String)
    */
   public void setStatusesTxt(String statusesTxt) {
      statuses = StatusHelper.importFromDb(statusesTxt);
   }

   /**
    * Adds or updates status for phase. Values are updated if (A) they are coded
    * as StatusUpdateType.UPDATE and (B) a status code with the same phase is
    * already present. Otherwise the code is added to the list.
    * 
    * @param status code
    */
   public void addStatus(Status status) {
      log.log(status.getLogLevel(), status.getMsg());
      switch (status.getUpdateType()) {
         case UPDATE:
            for (int i = 0; i < statuses.size(); i++ ) {
               if (statuses.get(i).getPhaseMsg().equals(status.getPhaseMsg())) {
                  statuses.set(i, status);
                  return;
               }
            }
            //$FALL-THROUGH$
         case ADD:
            statuses.add(status);
            break;
         default:
            Util.exit("WSLog.addStatus encountered unknown UpdateType "
               + status.getUpdateType());
      }
   }

   /**
    * Convenience method adds status and error message together
    * 
    * @param status parameter for {@link #addStatus(Status)}
    * @param errorMessage parameter for {@link #setErrorMessage(String)}
    */
   public void addStatus(Status status,
      @SuppressWarnings("hiding") String errorMessage) {
      addStatus(status);
      setErrorMessage(errorMessage);
   }

   /**
    * @return all statuses posted for this transaction.
    */
   public List <Status> getStatuses() {
      return statuses;
   }

   /**
    * @return current {@link #errorMessage} value
    */
   @Helper(ignoreComp = true)
   public String getErrorMessage() {
      return errorMessage;
   }

   /**
    * set {@link #errorMessage} value
    * 
    * @param errorMessage new value
    */
   public void setErrorMessage(String errorMessage) {
      this.errorMessage = errorMessage;
   }

   /**
    * @return current {@link #errorLine} value
    */
   @Helper(ignoreComp = true)
   public Integer getErrorLine() {
      return errorLine;
   }

   /**
    * set {@link #errorLine} value
    * 
    * @param errorLine new value
    */
   public void setErrorLine(Integer errorLine) {
      this.errorLine = errorLine;
   }

   /**
    * @return current {@link #errorColumn} value
    */
   @Helper(ignoreComp = true)
   public Integer getErrorColumn() {
      return errorColumn;
   }

   /**
    * set {@link #errorColumn} value
    * 
    * @param errorColumn new value
    */
   public void setErrorColumn(Integer errorColumn) {
      this.errorColumn = errorColumn;
   }

   /**
    * @return current {@link #errorSubstring} value
    */
   @Helper(ignoreComp = true)
   public String getErrorSubstring() {
      return errorSubstring;
   }

   /**
    * set {@link #errorSubstring} value
    * 
    * @param errorSubstring new value
    */
   public void setErrorSubstring(String errorSubstring) {
      this.errorSubstring = errorSubstring;
   }

   /**
    * Set {@link #soapRequestName}
    * @param soapRequestName name to set
    */
   public void setSoapRequestName(String soapRequestName) {
      this.soapRequestName = soapRequestName;
   }
   /**
    * @return {@link #soapRequestName}
    */
   public String getSoapRequestName() {
      return soapRequestName;
   }
   /**
    * Adds a Name/Value pair to {@link #soapRequestLabelValuePairs}
    * 
    * @param nameValuePair Name/Value pair to add
    */
   public void addSoapRequestNameValuePair(LabelValue <String, Object> nameValuePair) {
      soapRequestLabelValuePairs.add(nameValuePair);
   }
   
   /**
    * Adds a Name/Value pair to {@link #soapRequestLabelValuePairs}
    * @param name String name for pair
    * @param object Object value for pair
    */
   public void addSoapRequestNameValuePair(String name, Object object) {
      soapRequestLabelValuePairs.add(new LabelValue <String, Object>(name, object));
   }

   /**
    * @return {@link #soapRequestLabelValuePairs} which have been saved for this
    * transaction. The returned list may be empty, but it will not be null.
    */
   public List <LabelValue <String, Object>> getSoapRequestNameValuePairs() {
      return soapRequestLabelValuePairs;
   }

   /**
    * Set {@link #soapResponseName}
    * @param soapResponseName name to set
    */
   public void setSoapResponseName(String soapResponseName) {
      this.soapResponseName = soapResponseName;
   }
   /**
    * @return {@link #soapResponseName}
    */
   public String getSoapResponseName() {
      return soapResponseName;
   }
   /**
    * Adds a Name/Value pair to {@link #soapResponseLabelValuePairs}
    * 
    * @param nameValuePair Name/Value pair to add
    */
   public void addSoapResponseNameValuePair(LabelValue <String, Object> nameValuePair) {
      soapResponseLabelValuePairs.add(nameValuePair);
   }
   
   /**
    * Adds a Name/Value pair to {@link #soapResponseLabelValuePairs}
    * @param name String name for pair
    * @param object Object value for pair
    */
   public void addSoapResponseNameValuePair(String name, Object object) {
      soapResponseLabelValuePairs.add(new LabelValue <String, Object>(name, object));
   }

   /**
    * @return {@link #soapResponseLabelValuePairs} which have been saved for this
    * transaction. The returned list may be empty, but it will not be null.
    */
   public List <LabelValue <String, Object>> getSoapResponseNameValuePairs() {
      return soapResponseLabelValuePairs;
   }

   /**
    * @return current {@link #httpMethod} value
    */
   public String getHttpMethod() {
      return httpMethod;
   }

   /**
    * set {@link #httpMethod} value
    * 
    * @param httpMethod new value
    */
   public void setHttpMethod(String httpMethod) {
      this.httpMethod = httpMethod;
   }

   /**
    * @return current {@link #httpRequestURI} value
    */
   public URI getHttpRequestURI() {
      return httpRequestURI;
   }

   /**
    * set {@link #httpRequestURI} value
    * 
    * @param httpRequestURI new value
    */
   public void setHttpRequestURI(URI httpRequestURI) {
      this.httpRequestURI = httpRequestURI;
   }

   /**
    * @return current {@link #httpRequestProtocol} value
    */
   public String getHttpRequestProtocol() {
      return httpRequestProtocol;
   }

   /**
    * set {@link #httpRequestProtocol} value
    * 
    * @param httpRequestProtocol new value
    */
   public void setHttpRequestProtocol(String httpRequestProtocol) {
      this.httpRequestProtocol = httpRequestProtocol;
   }

   /**
    * @return current {@link #httpRequestHeaders} value.
    */
   @Helper(ignoreComp = true)
   public Map <String, List <String>> getHttpRequestHeaders() {
      return httpRequestHeaders;
   }

   /**
    * set {@link #httpRequestHeaders} value
    * 
    * @param httpRequestHeaders new value
    */
   public void setHttpRequestHeaders(
      Map <String, List <String>> httpRequestHeaders) {
      this.httpRequestHeaders = copyHeaders(httpRequestHeaders);
   }

   /**
    * @return current {@link #httpResponseCode} value
    */
   public Integer getHttpResponseCode() {
      return httpResponseCode;
   }

   /**
    * set {@link #httpResponseCode} value
    * 
    * @param httpResponseCode new value
    */
   public void setHttpResponseCode(Integer httpResponseCode) {
      this.httpResponseCode = httpResponseCode;
   }

   /**
    * @return current {@link #httpResponseHeaders} value
    */
   @Helper(ignoreComp = true)
   public Map <String, List <String>> getHttpResponseHeaders() {
      return httpResponseHeaders;
   }

   /**
    * set {@link #httpResponseHeaders} value
    * 
    * @param httpResponseHeaders new value
    */
   public void setHttpResponseHeaders(
      Map <String, List <String>> httpResponseHeaders) {
      this.httpResponseHeaders = copyHeaders(httpResponseHeaders);
   }

   /**
    * @return the {@link #soapActionURI} value.
    */
   public String getSoapActionURI() {
      return soapActionURI;
   }

   /**
    * @param soapAction the {@link #soapActionURI} to set
    */
   public void setSoapActionURI(String soapAction) {
      this.soapActionURI = soapAction;
   }

   /**
    * @return the {@link #wsdlService} value.
    */
   public String getWsdlService() {
      return wsdlService;
   }

   /**
    * @param wsdlService the {@link #wsdlService} to set
    */
   public void setWsdlService(String wsdlService) {
      this.wsdlService = wsdlService;
   }

   /**
    * @return the {@link #soapMessageId} value.
    */
   public String getSoapMessageId() {
      return soapMessageId;
   }

   /**
    * @param soapMessageId the {@link #soapMessageId} to set
    */
   public void setSoapMessageId(String soapMessageId) {
      this.soapMessageId = soapMessageId;
   }

   /**
    * Convenience method to set values for a parsing type error.
    * 
    * @param errorMessage String message generated by exception
    * @param errorLine Integer line error was on, null if substring based error.
    * @param errorColumn Integer column error was noted in; used for both
    * line/column and substring based errors.
    * @param subString for substring based errors, otherwise null.
    */
   public void setParseErrorLocation(String errorMessage, Integer errorLine,
      Integer errorColumn, String subString) {
      this.errorMessage = errorMessage;
      this.errorLine = errorLine;
      this.errorColumn = errorColumn;
      this.errorSubstring = subString;
   }

   /**
    * @return A customized "toString" method, which returns a String giving the
    * source, arrival time and service destination of the message, in a form
    * like:
    * 
    * <pre>
    * Message from clienthost.org at Sep 19,2004 8:04 AM to EBGL Service
    * </pre>
    */
   public String toStringLong() {
      SimpleDateFormat sdf = new SimpleDateFormat();
      return "Message from " + clientHostName + " at "
         + sdf.format(connOpenTime) + " to " + serviceName + " ";
   }

   /**
    * @return remote client host name and/or IP address. If neither has been
    * saved, returns "remote client not identified".
    */
   public String toStringClientIfno() {
      if ((clientHostName.length() + clientIp.length()) > 0)
         return clientHostName + ": " + clientIp;
      return "remote client not identified";
   }

   /**
    * @return a short description of the transaction:
    * <ul>
    * <li>For HTTP the method and path.</li>
    * <li>For SOAP the server, port and path.</li>
    * </ul>
    */
   public String getDescription() {
      switch (transactionType) {
         case SOAP_SERVER:
         case SOAP_CLIENT:
            return serverName + ":" + serverPort + "/" + serviceName;
         case HTTP_SERVER:
         case HTTP_CLIENT:
            return httpMethod + " " + httpRequestURI.toString();
         case CDA_CLIENT:
            return "Inspect generated CDA Document";
         default:
            return "Unknown transaction type";
      }
   }

   /**
    * Standard inner {@link Comparator} class for WSLog using {@link DBHelper}
    */
   public static class Comp implements Comparator <WSLog> {

      private String property;
      private boolean ascending;

      /**
       * @param property name of {@link WSLog} property to sort on.
       * @param ascending boolean ascending=true, descending=false.
       */
      public Comp(String property, boolean ascending) {
         this.property = property;
         this.ascending = ascending;
      }

      @Override
      public int compare(WSLog one, WSLog two) {
         if (StringUtils.isBlank(property)) return 0;
         return helper.compare(one, two, property, ascending);
      }
   } // EO Comparator inner class

   @Override
   public void close() {
      log.trace(Util.classMethod() + "invoked");
      try (Connection conn = JDBC.getConnection(LOGICAL_DB_NAME)) {
         if (id == null) id = insert(conn);
         for (WSMsg m : wsMsgs) {
            if (m.getId() == null) {
               m.setWslogId(id);
               m.insert(conn);
            }
         }
      } catch (Exception e) {
         log.warn("WSLog.close() error: " + e.getMessage());
      }
   }

   /**
    * Instantiates a new instance of {@link WSLog} using value from the next row
    * of the passed result set.
    * 
    * @param resultSet {@link ResultSet}
    * @return new WSLog instance.
    * @throws Exception on error.
    * @see DBHelper#loadNextRow(ResultSet)
    */
   public static WSLog loadNext(ResultSet resultSet) throws Exception {
      return helper.loadNextRow(resultSet);
   }

   /**
    * Instantiates new instance of {@link WSLog} using values from each row of
    * the passed result set beginning with the next one.
    * 
    * @param resultSet {@link ResultSet}
    * @return a {@link List} of instances of WSLog. If there are no instances
    * the list will be empty; It should not be null.
    * @throws Exception on error.
    * @see DBHelper#loadNextRow(ResultSet)
    */
   public static List <WSLog> loadRows(ResultSet resultSet) throws Exception {
      return helper.loadRows(resultSet);
   }

   private Map <String, List <String>> copyHeaders(
      Map <String, List <String>> headers) {
      Map <String, List <String>> copy = new HashMap <>();
      Set <Entry <String, List <String>>> entrySet = headers.entrySet();
      for (Entry <String, List <String>> entry : entrySet) {
         String key = entry.getKey();
         List <String> oldList = entry.getValue();
         List <String> newList = new ArrayList <>(oldList);
         log.trace("Header  " + key + " = " + newList.toString());
         copy.put(key, newList);
      }
      return copy;
   }

} // EO WSLog class

