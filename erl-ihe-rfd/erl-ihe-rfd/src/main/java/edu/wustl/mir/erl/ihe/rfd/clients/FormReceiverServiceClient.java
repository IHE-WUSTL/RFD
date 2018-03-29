/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.clients;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceFeature;

import edu.wustl.mir.erl.ihe.util.Util;
import ihe.iti.rfd._2007.RFDFormReceiverPortType;

/**
/**
 * RFD WS client. Used by a FormFiller acting as a client, communicating with a
 * FormReceiver acting as a server. Client instantiates this client with the
 * endpoint URL of the FormReceive service and any desired
 * {@link javax.xml.ws.WebServiceFeature WebServiceFeatures}, obtains a port on
 * that service, then calls service methods on that port. For example:
 * 
 * <pre>
 * FormReceiverServiceClient service = FormReceiverServiceClient(
 *    &quot;http://somehost:80/FormReceiverSimulator&quot;, new AddressingFeature(true, true));
 * FormReceiverPortType port = service.getPort();
 * SubmitFormResponseType submitformResponse = port
 *    .formReceiverRetrieveForm(retrieveFormRequest);
 * </pre>
 */
public class FormReceiverServiceClient {

   private static URL wsdlURL;
   private URL endpointURL;
   private RFDFormReceiverService service;
   private RFDFormReceiverPortType port;
   private BindingProvider bindingProvider;

   static {
      try {
         wsdlURL = new URL("file:../wsdl/RFDFormReceiver.wsdl");
      } catch (MalformedURLException e) {
         Util.exit(Util.getEM(e));
      }
   }

   /**
    * Create client for service at passed endpoint URL
    * 
    * @param endpointURLString endpoint URL of service, for example,
    * "http://somehost:80/FormReceiverSimulator".
    * @param features WebServiceFeature desired for this service.
    * @throws Exception on error, including invalid URL, unable to locate
    * service and others.
    */
   public FormReceiverServiceClient(String endpointURLString,
      WebServiceFeature... features) throws Exception {
      endpointURL = new URL(endpointURLString);
      service = new RFDFormReceiverService(wsdlURL, features);
      port = service.getRFDFormReceiverPort();
      bindingProvider = (BindingProvider) port;
      bindingProvider.getRequestContext().put(
         BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL.toString());
   }

   /**
    * @return port for this service.
    */
   public RFDFormReceiverPortType getPort() {
      return port;
   }


}
