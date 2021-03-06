
package edu.wustl.mir.erl.ihe.rfd.clients;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

import ihe.iti.rfd._2007.RFDFormReceiverPortType;

/**
 * Based on the class {@link ihe.iti.rfd._2007.FormReceiverService
 * FormReceiverService} which was generated by JAX-WS RI 2.2.8. Generated source
 * version: 2.2
 */
@WebServiceClient(name = "RFDFormReceiver_Service",
   targetNamespace = "urn:ihe:iti:rfd:2007",
   wsdlLocation = "./wsdl/RFDFormReceiver.wsdl")
@SuppressWarnings("javadoc")
public class RFDFormReceiverService extends Service {

   private final static URL RFDFORMRECEIVERSERVICE_WSDL_LOCATION;
   private final static WebServiceException RFDFORMRECEIVERSERVICE_EXCEPTION;
   private final static QName RFDFORMRECEIVERSERVICE_QNAME =
      new QName("urn:ihe:iti:rfd:2007", "RFDFormReceiver_Service");

   static {
      URL url = null;
      WebServiceException e = null;
      try {
         url = new URL("./wsdl/RFDFormReceiver.wsdl");
      } catch (MalformedURLException ex) {
         e = new WebServiceException(ex);
      }
      RFDFORMRECEIVERSERVICE_WSDL_LOCATION = url;
      RFDFORMRECEIVERSERVICE_EXCEPTION = e;
   }

   public RFDFormReceiverService() {
      super(__getWsdlLocation(), RFDFORMRECEIVERSERVICE_QNAME);
   }

   public RFDFormReceiverService(WebServiceFeature... features) {
      super(__getWsdlLocation(), RFDFORMRECEIVERSERVICE_QNAME, features);
   }

   public RFDFormReceiverService(URL wsdlLocation) {
      super(wsdlLocation, RFDFORMRECEIVERSERVICE_QNAME);
   }

   public RFDFormReceiverService(URL wsdlLocation,
      WebServiceFeature... features) {
      super(wsdlLocation, RFDFORMRECEIVERSERVICE_QNAME, features);
   }

   public RFDFormReceiverService(URL wsdlLocation, QName serviceName) {
      super(wsdlLocation, serviceName);
   }

   public RFDFormReceiverService(URL wsdlLocation, QName serviceName,
      WebServiceFeature... features) {
      super(wsdlLocation, serviceName, features);
   }

   /**
    * @return returns RFDFormReceiverPortType
    */
   @WebEndpoint(name = "RFDFormReceiver_Port")
   public RFDFormReceiverPortType getRFDFormReceiverPort() {
      return super.getPort(
         new QName("urn:ihe:iti:rfd:2007", "RFDFormReceiver_Port"),
         RFDFormReceiverPortType.class);
   }

   /**
    * @param features A list of {@link javax.xml.ws.WebServiceFeature} to
    * configure on the proxy. Supported features not in the
    * <code>features</code> parameter will have their default values.
    * @return returns RFDFormReceiverPortType
    */
   @WebEndpoint(name = "RFDFormReceiver_Port")
   public RFDFormReceiverPortType
      getRFDFormReceiverPort(WebServiceFeature... features) {
      return super.getPort(
         new QName("urn:ihe:iti:rfd:2007", "RFDFormReceiver_Port"),
         RFDFormReceiverPortType.class, features);
   }

   /**
    * @return returns RFDFormReceiverPortType
    */
   @WebEndpoint(name = "RFDFormReceiver_Port_Soap11")
   public RFDFormReceiverPortType getRFDFormReceiverPortSoap11() {
      return super.getPort(
         new QName("urn:ihe:iti:rfd:2007", "RFDFormReceiver_Port_Soap11"),
         RFDFormReceiverPortType.class);
   }

   /**
    * @param features A list of {@link javax.xml.ws.WebServiceFeature} to
    * configure on the proxy. Supported features not in the
    * <code>features</code> parameter will have their default values.
    * @return returns RFDFormReceiverPortType
    */
   @WebEndpoint(name = "RFDFormReceiver_Port_Soap11")
   public RFDFormReceiverPortType
      getRFDFormReceiverPortSoap11(WebServiceFeature... features) {
      return super.getPort(
         new QName("urn:ihe:iti:rfd:2007", "RFDFormReceiver_Port_Soap11"),
         RFDFormReceiverPortType.class, features);
   }

   private static URL __getWsdlLocation() {
      if (RFDFORMRECEIVERSERVICE_EXCEPTION != null) { throw RFDFORMRECEIVERSERVICE_EXCEPTION; }
      return RFDFORMRECEIVERSERVICE_WSDL_LOCATION;
   }

}
