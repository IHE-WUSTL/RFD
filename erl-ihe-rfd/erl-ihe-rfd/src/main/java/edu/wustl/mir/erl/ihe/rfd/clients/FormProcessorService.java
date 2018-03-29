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

import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;
import javax.xml.ws.WebEndpoint;
import javax.xml.ws.WebServiceClient;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;

import ihe.iti.rfd._2007.FormProcessorPortType;


/**
 * Based on the class {@link ihe.iti.rfd._2007.FormProcessorService
 * FormManagerService} which was generated by JAX-WS RI 2.2.8. Generated source
 * version: 2.2
 * 
 */
@WebServiceClient(
   name = "FormProcessor_Service", 
   targetNamespace = "urn:ihe:iti:rfd:2007", 
   wsdlLocation = "./wsdl/RFDFormProcessor.wsdl")
@SuppressWarnings("javadoc")
public class FormProcessorService extends Service {

    private final static URL FORMPROCESSORSERVICE_WSDL_LOCATION;
    private final static WebServiceException FORMPROCESSORSERVICE_EXCEPTION;
    private final static QName FORMPROCESSORSERVICE_QNAME = new QName(
       "urn:ihe:iti:rfd:2007", "FormProcessor_Service");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("./wsdl/RFDFormProcessor.wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        FORMPROCESSORSERVICE_WSDL_LOCATION = url;
        FORMPROCESSORSERVICE_EXCEPTION = e;
    }

    public FormProcessorService() {
        super(__getWsdlLocation(), FORMPROCESSORSERVICE_QNAME);
    }

    public FormProcessorService(WebServiceFeature... features) {
        super(__getWsdlLocation(), FORMPROCESSORSERVICE_QNAME, features);
    }

    public FormProcessorService(URL wsdlLocation) {
        super(wsdlLocation, FORMPROCESSORSERVICE_QNAME);
    }

    public FormProcessorService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, FORMPROCESSORSERVICE_QNAME, features);
    }

    public FormProcessorService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public FormProcessorService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    /**
     * @return returns FormProcessorPortType
     */
    @WebEndpoint(name = "FormProcessor_Port_Soap12")
    public FormProcessorPortType getFormProcessorPortSoap12() {
        return super.getPort(new QName("urn:ihe:iti:rfd:2007", 
           "FormProcessor_Port_Soap12"), FormProcessorPortType.class);
    }

    /**
     * @param features A list of {@link javax.xml.ws.WebServiceFeature} to 
     * configure on the proxy. Supported features not in the 
     * <code>features</code> parameter will have their default values.
     * @return returns FormProcessorPortType
     */
    @WebEndpoint(name = "FormProcessor_Port_Soap12")
    public FormProcessorPortType getFormProcessorPortSoap12(
       WebServiceFeature... features) {
       return super.getPort(new QName("urn:ihe:iti:rfd:2007", 
          "FormProcessor_Port_Soap12"), FormProcessorPortType.class, features);
    }

    private static URL __getWsdlLocation() {
        if (FORMPROCESSORSERVICE_EXCEPTION!= null) {
            throw FORMPROCESSORSERVICE_EXCEPTION;
        }
        return FORMPROCESSORSERVICE_WSDL_LOCATION;
    }

}
