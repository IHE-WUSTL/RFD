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

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

/**
 * Based on the class {@link ihe.iti.rfd._2007.FormManagerService
 * FormManagerService} which was generated by JAX-WS RI 2.2.8. Generated source
 * version: 2.2
 */
@WebServiceClient(
   name = "FormManager_Service",
   targetNamespace = "urn:ihe:iti:rfd:2007",
   wsdlLocation = "../wsdl/RFDFormManager.wsdl")
public class FormManagerService extends Service {

   private final static URL FORMMANAGERSERVICE_WSDL_LOCATION;
   private final static WebServiceException FORMMANAGERSERVICE_EXCEPTION;
   private final static QName FORMMANAGERSERVICE_QNAME = new QName(
      "urn:ihe:iti:rfd:2007", "FormManager_Service");

   static {
      URL url = null;
      WebServiceException e = null;
      try {
         url =
            new URL(
               "./wsdl/RFDFormManager.wsdl");
      } catch (MalformedURLException ex) {
         e = new WebServiceException(ex);
      }
      FORMMANAGERSERVICE_WSDL_LOCATION = url;
      FORMMANAGERSERVICE_EXCEPTION = e;
   }

   public FormManagerService() {
      super(__getWsdlLocation(), FORMMANAGERSERVICE_QNAME);
   }

   public FormManagerService(WebServiceFeature... features) {
      super(__getWsdlLocation(), FORMMANAGERSERVICE_QNAME, features);
   }

   public FormManagerService(URL wsdlLocation) {
      super(wsdlLocation, FORMMANAGERSERVICE_QNAME);
   }

   public FormManagerService(URL wsdlLocation, WebServiceFeature... features) {
      super(wsdlLocation, FORMMANAGERSERVICE_QNAME, features);
   }

   public FormManagerService(URL wsdlLocation, QName serviceName) {
      super(wsdlLocation, serviceName);
   }

   public FormManagerService(URL wsdlLocation, QName serviceName,
      WebServiceFeature... features) {
      super(wsdlLocation, serviceName, features);
   }

   /**
    * @return returns FormManagerPortType
    */
   @WebEndpoint(name = "FormManager_Port_Soap12")
   public FormManagerPortType getFormManagerPortSoap12() {
      return super.getPort(new QName("urn:ihe:iti:rfd:2007",
         "FormManager_Port_Soap12"), FormManagerPortType.class);
   }

   /**
    * @param features A list of {@link javax.xml.ws.WebServiceFeature} to
    * configure on the proxy. Supported features not in the
    * <code>features</code> parameter will have their default values.
    * @return returns FormManagerPortType
    */
   @WebEndpoint(name = "FormManager_Port_Soap12")
   public FormManagerPortType getFormManagerPortSoap12(
      WebServiceFeature... features) {
      return super.getPort(new QName("urn:ihe:iti:rfd:2007",
         "FormManager_Port_Soap12"), FormManagerPortType.class, features);
   }

   private static URL __getWsdlLocation() {
      if (FORMMANAGERSERVICE_EXCEPTION != null) { throw FORMMANAGERSERVICE_EXCEPTION; }
      return FORMMANAGERSERVICE_WSDL_LOCATION;
   }

}
