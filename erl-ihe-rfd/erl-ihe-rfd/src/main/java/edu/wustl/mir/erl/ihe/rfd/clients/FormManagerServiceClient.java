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
package edu.wustl.mir.erl.ihe.rfd.clients;

import ihe.iti.rfd._2007.FormManagerPortType;

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.WebServiceFeature;

import edu.wustl.mir.erl.ihe.util.Util;

/**
 * RFD WS client. Used by a FormFiller acting as a client, communicating with a
 * FormManager acting as a server. Client instantiates this client with the
 * endpoint URL of the FormManager service and any desired
 * {@link javax.xml.ws.WebServiceFeature WebServiceFeatures}, obtains a port on
 * that service, then calls service methods on that port. For example:
 * 
 * <pre>
 * FormManagerServiceClient service = FormManagerServiceClient(
 *    &quot;http://somehost:80/FormManagerSimulator&quot;, new AddressingFeature(true, true));
 * FormManagerPortType port = service.getPort();
 * RetrieveFormResponseType retrieveformResponse = port
 *    .formManagerRetrieveForm(retrieveFormRequest);
 * </pre>
 */
public class FormManagerServiceClient {

   private static URL wsdlURL;
   private URL endpointURL;
   private FormManagerService service;
   private FormManagerPortType port;
   private BindingProvider bindingProvider;

   static {
      try {
         wsdlURL = new URL("file:../wsdl/RFDFormManager.wsdl");
      } catch (MalformedURLException e) {
         Util.exit(Util.getEM(e));
      }
   }

   /**
    * Create client for service at passed endpoint URL
    * 
    * @param endpointURLString endpoint URL of service, for example,
    * "http://somehost:80/FormManagerSimulator".
    * @param features WebServiceFeature desired for this service.
    * @throws Exception on error, including invalid URL, unable to locate
    * service and others.
    */
   public FormManagerServiceClient(String endpointURLString,
      WebServiceFeature... features) throws Exception {
      endpointURL = new URL(endpointURLString);
      service = new FormManagerService(wsdlURL, features);
      port = service.getFormManagerPortSoap12();
      bindingProvider = (BindingProvider) port;
      bindingProvider.getRequestContext().put(
         BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointURL.toString());
   }

   /**
    * @return port for this service.
    */
   public FormManagerPortType getPort() {
      return port;
   }

}
