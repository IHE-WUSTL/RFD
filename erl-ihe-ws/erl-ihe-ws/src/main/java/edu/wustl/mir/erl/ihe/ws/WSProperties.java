package edu.wustl.mir.erl.ihe.ws;

import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;

import edu.wustl.mir.erl.ihe.util.UtilProperties;
import edu.wustl.mir.erl.ihe.ws.client.WSClient;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.server.WSEndpoint;

/**
 * Constants use in erl-ihe-ws project.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public interface WSProperties extends UtilProperties {
   // --------------------------------------------------- Handler names
   /**
    * Canonical class name of {@link LogTransaction}
    */
   // ------------------------ Additional MessageContext property names
   public static final String WS_LOG_TRANSACTION = LogTransaction.class
      .getCanonicalName();
   /**
    * Canonical class name of {@link WSEndpoint}
    */
   public static final String WS_ENDPOINT = WSEndpoint.class.getCanonicalName();
   /**
    * {@link LogicalMessageContext} Message Outbound property name.
    */
   public static final String MESSAGE_OUTBOUND_PROPERTY =
      LogicalMessageContext.MESSAGE_OUTBOUND_PROPERTY;

   /**
    * HTTP Exchange class, and key in context map
    */
   public static final String HTTP_EXCHANGE =
      "com.sun.xml.internal.ws.http.exchange";
   /**
    * {@link MessageContext} HTTP Request method property name.
    */
   public static final String HTTP_REQUEST_METHOD =
      MessageContext.HTTP_REQUEST_METHOD;
   /**
    * {@link MessageContext} HTTP Request path property name.
    */
   public static final String PATH_INFO = MessageContext.PATH_INFO;

   /**
    * {@link MessageContext} HTTP Request query string property name.
    */
   public static final String QUERY_STRING = MessageContext.QUERY_STRING;
   /**
    * {@link MessageContext} HTTP Request headers property name.
    */
   public static final String HTTP_REQUEST_HEADERS =
      MessageContext.HTTP_REQUEST_HEADERS;
   /**
    * {@link MessageContext} HTTP Response code property name.
    */
   public static final String HTTP_RESPONSE_CODE =
      MessageContext.HTTP_RESPONSE_CODE;
   /**
    * {@link MessageContext} HTTP Response headers property name.
    */
   public static final String HTTP_RESPONSE_HEADERS =
      MessageContext.HTTP_RESPONSE_HEADERS;

   /**
    * SOAP MessageContext SOAP Action URI property name.
    */
   public static final String SOAP_ACTION_URI =
      "javax.xml.ws.soap.http.soapaction.uri";

   /**
    * SOAP MessageContext endpoint address.
    */
   public static final String SOAP_ENDPOINT_ADDRESS =
      "javax.xml.ws.service.endpoint.address";

   /**
    * SOAP MessageContext WSDL service name namespace property name.
    */
   public static final String WSDL_SERVICE = "javax.xml.ws.wsdl.service";

   /**
    * SOAP MessageContext SOAP Message UID property name.
    */
   public static final String SOAP_MESSAGE_ID =
      "com.sun.xml.internal.ws.api.addressing.messageId";
   /**
    * WSLog RMI default rmi short name
    */
   public static final String WSLOG_RMI_SHORT_NAME = "WSLogRMI";
   /**
    * WSLog RMI default rmi register name
    */
   public static final String WSLOG_RMI_LONG_NAME = WSLog.class
      .getCanonicalName() + WSLOG_RMI_SHORT_NAME;
   /**
    * client test RMI default short name
    */
   public static final String WSCLIENT_RMI_SHORT_NAME = "WSCLientTestRMI";
   /**
    * client test RMI default register name
    */
   public static final String WSCLIENT_RMI_LONG_NAME = WSClient.class
      .getCanonicalName() + WSCLIENT_RMI_SHORT_NAME;
   /**
    * Fixed client LabelValue pair Label names: TestStep label
    */
   public static final String LABEL_TEST_STEP = "TestStep";
   /**
    * Fixed client LabelValue pair Label names: WSServer URL label
    */
   public static final String LABEL_WSSERVER_URL = "WSServerURL";
   /**
    * Fixed client LabelValue pair Label names: form ID
    */
   public static final String LABEL_FORM_ID = "formID";
   /**
    * Fixed client LabelValue pair Label names: form URL
    */
   public static final String LABEL_FORM_URL = "formURL";
   /**
    * Fixed client LabelValue pair Label names: WSServer URL label
    */
   public static final String LABEL_ERROR = "error";
   /**
    * Fixed FormReceiver Label name for formid
    */
   public static final String TEST_FORM_ID = "testFormID";

} // EO WSProperties
