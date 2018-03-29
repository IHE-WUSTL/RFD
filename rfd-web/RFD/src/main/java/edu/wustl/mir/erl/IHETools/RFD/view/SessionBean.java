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
package edu.wustl.mir.erl.IHETools.RFD.view;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.apache.commons.lang.StringUtils;
import org.javatuples.LabelValue;
import org.javatuples.Pair;
import org.primefaces.context.RequestContext;

import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.UtilProperties;
import edu.wustl.mir.erl.ihe.ws.db.TransactionType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.db.WSMsg;

/**
 * General Session bean for the application. Handles the test transaction table
 * and display functions.
 */
@ManagedBean
@SessionScoped
public class SessionBean implements Serializable, UtilProperties {
   private static final long serialVersionUID = 1L;

   ApplicationBean applicationBean = ApplicationBean.getInstance();

   /**
    * Constructor. Initializes the transaction table.
    */
   public SessionBean() {
      initTransactionsTable();
   }

   // **********************************************************
   // Available transactions(WSLog) table
   // **********************************************************

   private void initTransactionsTable() {
      transactions = applicationBean.getTransactions();
      clearTransaction();
   }

   /**
    * {@link edu.wustl.mir.erl.ihe.ws.db.WSLog WSLog} transactions currently
    * available for viewing.
    */
   private List <WSLog> transactions;
   /**
    * {@link edu.wustl.mir.erl.ihe.ws.db.WSLog WSLog} transaction which has been
    * selected from the transaction table.
    */
   private WSLog selectedTransaction;

   /**
    * @return {@link #transactions}
    */
   public List <WSLog> getTransactions() {
      return transactions;
   }

   /**
    * @return {@link #selectedTransaction}
    */
   public WSLog getSelectedTransaction() {
      return selectedTransaction;
   }

   /**
    * @param trans set {@link #selectedTransaction}
    */
   public void setSelectedTransaction(WSLog trans) {
      selectedTransaction = trans;
   }

   /**
    * @return boolean has a transaction been selected
    */
   public boolean isTransactionSelected() {
      return selectedTransaction != null;
   }
   
   // ***************************************************************
   // * Transaction Table command buttons
   // ***************************************************************
   
   /**
    * @return boolean true if the "Delete Transaction" menuitem on the
    * transactiontableform context menu should be disabled, false otherwise.
    */
   public boolean isDeleteTransactionDisabled() {
      return !isTransactionSelected();
   }
   /**
    * ActionListener for transaction table context menu
    * "Delete This Transaction" selection. Clears the selected transaction from
    * the table in both the SessionBean and the ApplicationBean.
    */
   public void deleteThisTransaction() {
      transactions.remove(selectedTransaction);
      applicationBean.removeTransaction(selectedTransaction);
      clearTransaction();
   }

   /**
    * @return boolean true if the "Clear All Transactions" menuitem on the
    * transactiontableform context menu should be disabled, false otherwise.
    */
   public boolean isClearAllTransactionsDisabled() {
      return transactions == null || transactions.isEmpty();
   }

   /**
    * ActionListener for transaction table context menu "Clear All Transactions"
    * selection. Clears all transactions from the table in both the SessionBean
    * and the ApplicationBean. Note: Does NOT clear transactions in the
    * ApplicationBean table which are not present in the SessionBean table.
    */
   public void clearAllTransactions() {
      Iterator<WSLog> i = transactions.iterator();
      while (i.hasNext()) {
         WSLog wsLog = i.next();
         i.remove();
         applicationBean.removeTransaction(wsLog);
      }
      clearTransaction();
   }

   /**
    * @return boolean true if the "Update Transactions Table" menuitem on the
    * transactiontableform context menu should be disabled, false otherwise.
    */
   public boolean isUpdateTransactionsTableDisabled() {
      return applicationBean.getTransactions().size() == transactions.size();
   }

   /**
    * ActionListener for transaction table context menu "Update Transaction
    * Table" selection. Updates the SessionBean transaction list to match the
    * current ApplicationBean list.
    */
   public void updateTransactionTable() {
      initTransactionsTable();
   }

   // **********************************************************
   // Transaction details
   // **********************************************************

   /**
    * @return boolean, should the HTTP & SOAP properties table be rendered? It
    * is not rendered for CDA document inspections, which are not transactions.
    */
   public boolean isRenderProperties() {
      if (loadedTransaction != null && 
         loadedTransaction.getTransactionType() != TransactionType.CDA_CLIENT)
         return true;
      return false;
   }

   // WSLog transaction currently loaded for detail display
   private WSLog loadedTransaction = null;

   // Transaction properties table items.
   private List <LabelValue <String, String>> properties = new ArrayList <>();

   // Transaction Status entries
   private List <Status> statuses = new ArrayList <>();

   // Transaction SOAP Request name and name/value pairs
   private String soapRequestName = null;
   private List <LabelValue <String, String>> requestPairs = null;

   // Transaction SOAP Response name and name/value pairs
   private String soapResponseName = null;
   private List <LabelValue <String, String>> responsePairs = null;
   
   // Transaction Messages
   private List<WSMsg> msgs = null;
   

   /**
    * Loads data from selected WSLog transaction for detail display, if not
    * already loaded.
    */
   private void loadTransaction() {
      if (selectedTransaction == null || 
          selectedTransaction.equals(loadedTransaction)) return;
      loadedTransaction = selectedTransaction;

      // --------------- properties table items
      properties = new ArrayList <>();
      a("connection start/end",
         dateToString(loadedTransaction.getConnOpenTime()),
         dateToString(loadedTransaction.getConnCloseTime()));
      a("Client host", loadedTransaction.getClientHostName(), 
         loadedTransaction.getClientIp());
      a("Server host", loadedTransaction.getServerName(),
         loadedTransaction.getServerIp());
      a("Service Endpoint Address", loadedTransaction.getServiceEndpointAddress());
      Boolean is = loadedTransaction.isSecure();
      if (is != null && is == true) {
         a("Secure", loadedTransaction.getCertificates());
      }
      URI uri = loadedTransaction.getHttpRequestURI();
      a("HTTP Request", loadedTransaction.getHttpMethod(), (uri == null) ? ""
         : uri.toString());
      headers("HTTP request header ", loadedTransaction.getHttpRequestHeaders());
      if (loadedTransaction.getTransactionType() == TransactionType.SOAP_SERVER 
               && loadedTransaction.getTransactionType() == TransactionType.SOAP_CLIENT)
         a("Web service", loadedTransaction.getServerPort().toString(),
            loadedTransaction.getServiceName());
      a("SOAP Action URI", loadedTransaction.getSoapActionURI());
      a("WSDL Service", loadedTransaction.getWsdlService());
      a("SOAP Msg ID", loadedTransaction.getSoapMessageId());
      Integer rc = loadedTransaction.getHttpResponseCode();
      if (rc != null) a("HTTP Response", rc.toString());
      headers("HTTP response headers",
         loadedTransaction.getHttpResponseHeaders());

      // -------------- status table items
      statuses = StatusHelper.sortByStatusType(loadedTransaction.getStatuses(),
         loadedTransaction.getTransactionType().getSortOrder());

      // -------------------- SOAP Request name and Name/Value pairs
      soapRequestName = loadedTransaction.getSoapRequestName();
      requestPairs = new ArrayList <LabelValue <String, String>>();
      List <LabelValue <String, Object>> nvpairs =
         loadedTransaction.getSoapRequestNameValuePairs();
      for (LabelValue <String, Object> lvpair : nvpairs) {
         String value = "null";
         if (lvpair.getValue() != null) value = lvpair.getValue().toString();
         requestPairs.add(new LabelValue <String, String>(lvpair.getLabel(), value));
      }

      // -------------------- SOAP Response name and Name/Value pairs
      soapResponseName = loadedTransaction.getSoapResponseName();
      responsePairs = new ArrayList <LabelValue <String, String>>();
      nvpairs = loadedTransaction.getSoapResponseNameValuePairs();
      for (LabelValue <String, Object> lvpair : nvpairs) {
         String value = "null";
         if (lvpair.getValue() != null) value = lvpair.getValue().toString();
         responsePairs.add(new LabelValue <String, String>(lvpair.getLabel(), value));
      }
      
      // HTTP and SOAP messages, sorted in logical order
      List<Pair<Integer, WSMsg>> ms = new ArrayList<>();
      for (WSMsg m : loadedTransaction.getWsMsgs()) {
         switch (m.getMsgType()) {
            case HTTP_IN_BODY:
               ms.add(Pair.with(1, m));
               break;
            case SOAP_IN:
               ms.add(Pair.with(2, m));
               break;
            case SOAP_OUT:
               ms.add(Pair.with(3, m));
               break;
            case HTTP_OUT_BODY:
               ms.add(Pair.with(4, m));
               break;
            default:
               ms.add(Pair.with(5, m));
               break;
         } // EO switch
      }
      Collections.sort(ms, new Comparator<Pair<Integer, WSMsg>>() {

         @Override
         public int compare(Pair <Integer, WSMsg> o1,
            Pair <Integer, WSMsg> o2) {
               return Integer.compare(o1.getValue0(), o2.getValue0());
         }
         
      });
      msgs = new ArrayList <>();
      for (Pair <Integer, WSMsg> n : ms)
         msgs.add(n.getValue1());

   } // EO loadTransaction method
   
   private void clearTransaction() {
      selectedTransaction = null;
      loadedTransaction = null;
      properties = new ArrayList <>();
      statuses = new ArrayList <>();
      requestPairs = new ArrayList <>();
      responsePairs = new ArrayList <>();
      msgs = new ArrayList <>();
   }

   private void a(String left, String... right) {
      StringBuilder str = new StringBuilder();
      for (String r : right) {
         r = StringUtils.trimToEmpty(r);
         if (r.length() > 0) str.append(r).append(" ");
      }
      String s = StringUtils.trimToEmpty(str.toString());
      if (StringUtils.isNotBlank(s))
         properties.add(new LabelValue <String, String>(left, s));
   }

   private String dateToString(Date date) {
      if (date == null) return "";
      LOG_TIMESTAMP_FORMAT.format(date);
      return Util.getRFC3339TimeStamp(date);
   }

   private void headers(String prefix, Map <String, List <String>> hdrs) {
      if (hdrs == null) return;
      for (Map.Entry <String, List <String>> hdr : hdrs.entrySet()) {
         a(prefix + " " + hdr.getKey(), hdr.getValue().toArray(new String[0]));
      }
   }

   /**
    * A list of {@link org.javatuples.LabelValue Label Value Pairs} containing
    * labels and corresponding property information for the
    * {@link #selectedTransaction} for display in the transaction detail table.
    * 
    * @return properties list.
    */
   public List <LabelValue <String, String>> getProperties() {
      loadTransaction();
      return properties;
   }

   /**
    * A list of {@link edu.wustl.mir.erl.ihe.util.Status Status} instances
    * posted for this transaction for display in the status table.
    * 
    * @return list of Statuses
    */
   public List <Status> getStatuses() {
      loadTransaction();
      return statuses;
   }

   /**
    * @return {@link #soapRequestName}
    */
   public String getRequestName() {
      return soapRequestName;
   }

   /**
    * @return List of Name/Value pairs for selected SOAP Request for display in
    * the data table.
    */
   public List <LabelValue <String, String>> getRequestPairs() {
      loadTransaction();
      return requestPairs;
   }

   /**
    * @return {@link #soapResponseName}
    */
   public String getResponseName() {
      return soapResponseName;
   }

   /**
    * @return List of Name/Value pairs for selected SOAP Response for display in
    * the data table.
    */
   public List <LabelValue <String, String>> getResponsePairs() {
      loadTransaction();
      return responsePairs;
   }
   
   /**
    * @return List of HTTP and SOAP messages for transaction, sorted in logical
    * order
    */
   public List<WSMsg> getMsgs() {
      loadTransaction();
      return msgs;
   }

   /**
    * Returns style for field based on passed parameter.
    * 
    * @param status input status
    * @return style string
    */
   public String statusStyle(Status status) {
      String styleClass = "normal";
      switch (status.getResult()) {
         case FAIL:
            styleClass = "orange";
            break;
         case NA:
            break;
         case PASS:
            styleClass = "palegreen";
            break;
         case UNKNOWN:
            break;
         default:
            break;

      }
      return styleClass;
   }

   /**
    * Adds new SessionBeans to list in ApplicationBean
    */
   @PostConstruct
   public void postConstruct () {
      ApplicationBean.addSessionBean(this);
   }
   /**
    * Removes obsolete SessionBeans from list in ApplicationBean
    */
   @PreDestroy
   public void preDestroy() {
      ApplicationBean.removeSessionBean(this);
   }
   
   /**
    * Force refresh of Screen relating to this SessionBean instance.
    */
   public void refreshScreen() {
      RequestContext rc = RequestContext.getCurrentInstance();
      rc.update("transactionTableForm");
   }

} // EO SessionBean
