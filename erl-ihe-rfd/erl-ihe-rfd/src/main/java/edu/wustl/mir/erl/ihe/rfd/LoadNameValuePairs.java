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
package edu.wustl.mir.erl.ihe.rfd;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.wustl.mir.erl.ihe.rfd.cda.document.CDADocument;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import ihe.iti.rfd._2007.AnyXMLContentType;
import ihe.iti.rfd._2007.FormDataType;
import ihe.iti.rfd._2007.RetrieveFormRequestType;
import ihe.iti.rfd._2007.RetrieveFormResponseType;
import ihe.iti.rfd._2007.SubmitFormResponseType;
import ihe.iti.rfd._2007.WorkflowDataType;

/**
 * static utility methods for loading name value pairs to {@link WSLog}
 * instances in the RFD environment. Use these methods to simplify test
 * creation.
 */
public class LoadNameValuePairs implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * convenience / readability constant used to express value of isRequest
    * boolean parameter when the message is a SOAP Request.
    */
   public static final boolean SOAP_REQUEST = true;
   /**
    * convenience / readability constant used to express value of isRequest
    * boolean parameter when the message is a SOAP Response.
    */
   public static final boolean SOAP_RESPONSE = false;

   /**
    * Loads Name/Value Pairs for passed RetrieveFormRequest
    * 
    * @param request RetrieveFormRequest to load from
    * @param wsLog WSLog transaction to load to
    * @param cdaDoc The CDA XML document being used in the prepopData. If not 
    * null, only those data items currently being processed by this document
    * will be included in the name/value pairs for the prepopData. If null, all
    * the data items in the element will be included. This option is provided to
    * hold down the number of name value pairs listed in the log. <b>NOTE: All
    * tests using CDA documents should pass an instance of that document to this
    * method, even if the test does not use it.</b>
    */
   public static void retrieveFormRequest(RetrieveFormRequestType request,
      WSLog wsLog, CDADocument cdaDoc) {
         wsLog.setSoapRequestName("RFD retrieveFormRequest");
      try {

         if (cdaDoc == null)
            LoadNameValuePairs.anyXMLContentType("prepopData",
               request.getPrepopData(), wsLog, SOAP_REQUEST);
          else cdaDoc.loadWSLog(wsLog);

         workflowData: {
            WorkflowDataType workflowData = request.getWorkflowData();
            if (workflowData == null) {
               wsLog.addSoapRequestNameValuePair("workflowData", null);
               break workflowData;
            }
            String prefix = "workflowData.";
            wsLog.addSoapRequestNameValuePair(prefix + "formID",
               workflowData.getFormID());
            wsLog.addSoapRequestNameValuePair(prefix + "encodedResponse",
               Boolean.toString(workflowData.isEncodedResponse()));
            wsLog.addSoapRequestNameValuePair(prefix + "archiveURL",
               workflowData.getArchiveURL());
            wsLog.addSoapRequestNameValuePair(prefix + "instanceID",
               workflowData.getInstanceID());

            LoadNameValuePairs.anyXMLContentType(prefix + "context",
               workflowData.getContext(), wsLog, SOAP_REQUEST);

         } // EO workflowData
      } catch (Exception e) {
         wsLog.getLog().warn(
            "Error while processing retrieveFormRequest name/value pairs: "
               + e.getMessage());
      }

   } // EO retrieveFormRequest method

   /**
    * Loads Name/Value Pairs for passed RetrieveFormResponse
    * 
    * @param response RetrieveFormResponse to load from
    * @param wsLog WSLog transaction to load to
    */
   public static void retrieveFormResponse(RetrieveFormResponseType response,
      WSLog wsLog) {
      try {
         wsLog.setSoapResponseName("RFD retrieveFormResponse");

         LoadNameValuePairs.formDataType("form", response.getForm(), wsLog,
            SOAP_RESPONSE);

         wsLog.addSoapResponseNameValuePair("contentType",
            response.getContentType());
         wsLog.addSoapResponseNameValuePair("responseCode",
            response.getResponseCode());

      } catch (Exception e) {
         wsLog.getLog().warn(
            "Error while processing retrieveFormResponse name/value pairs: "
               + e.getMessage());
      }

   } // EO retrieveFormResponse method
   
   /**
    * Load Name/Value Pairs for passed submitFormRequest (AnyXMLContentType).
    * @param request {@link AnyXMLContentType} to load from
    * @param wsLog WSLog transaction to load to
    */
   public static void submitFormRequest(AnyXMLContentType request, WSLog wsLog) {
      wsLog.setSoapRequestName("RFD submitForm");
      LoadNameValuePairs.anyXMLContentType("", request, wsLog, SOAP_REQUEST);
   }

   /**
    * Loads Name/Value Pairs for passed SubmitFormResponse
    * 
    * @param response SubmitFormResponse to load from
    * @param wsLog WSLog transaction to load to
    */
   public static void submitFormResponse(SubmitFormResponseType response,
      WSLog wsLog) {
      try {
         LoadNameValuePairs.formDataType("content", response.getContent(),
            wsLog, SOAP_RESPONSE);

         wsLog.addSoapResponseNameValuePair("contentType",
            response.getContentType());
         wsLog.addSoapResponseNameValuePair("responseCode",
            response.getResponseCode());

      } catch (Exception e) {
         wsLog.getLog().warn(
            "Error while processing SubmitFormResponse name/value pairs: "
               + e.getMessage());
      }

   }

   private static void formDataType(String prefix, FormDataType formData,
      WSLog wsLog, boolean isRequest) {
      try {
         if (formData == null) {
            LoadNameValuePairs.addPair(wsLog, "form", null, isRequest);
            return;
         }
         LoadNameValuePairs.anyXMLContentType("form.structured",
            formData.getStructured(), wsLog, isRequest);

         byte[] unstructured = formData.getUnstructured();
         String us = null;
         if (unstructured != null) us = unstructured.length + " bytes";
         LoadNameValuePairs.addPair(wsLog, "form.unstructured", us, isRequest);
         LoadNameValuePairs.addPair(wsLog, "form.URL", formData.getURL(),
            isRequest);
      } catch (Exception e) {
         wsLog.getLog().warn(
            "Error while processing FormDataType name/value pairs: "
               + e.getMessage());
      }
   } // EO formDataType method

   /**
    * Loads Name/Value Pairs for passed AnyXMLContentType recursively traversing
    * the tree. Generally this method will be invoked from other methods in this
    * class, but it is made public so that it may be invoked from outside if
    * needed.
    * 
    * @param prefix String giving "ancestry" of this AnyXMLContentType, ending
    * with a period ("."), for example, in an RFD RetrieveFormRequest, an
    * Element from the workflow context data might be passed to this method with
    * the prefix "workflowData.context.". Additional names will be appended to
    * this when generating the name for the name value pair.
    * @param anyXMLContent AnyXMLContentType to load from
    * @param wsLog WSLog transaction to load to
    * @param isRequest boolean is element from SOAP Request? true for Request,
    * false for Response.
    */
   public static void anyXMLContentType(String prefix,
      AnyXMLContentType anyXMLContent, WSLog wsLog, boolean isRequest) {

      if (anyXMLContent == null) {
         LoadNameValuePairs.addPair(wsLog, prefix, null, isRequest);
         return;
      }
      List <Element> elements = anyXMLContent.getAny();
      if (elements == null || elements.size() == 0) {
         LoadNameValuePairs.addPair(wsLog, prefix, "", isRequest);
         return;
      }
      for (Element element : elements)
         LoadNameValuePairs.element(prefix, element, wsLog, isRequest);
   }

   /**
    * Loads Name/Value Pairs for passed Element recursively traversing the tree.
    * Generally this method will be invoked from other methods in this class,
    * but it is made public so that it may be invoked from outside if needed.
    * 
    * @param prefix String giving "ancestry" of this element, ending with a
    * period ("."), for example, in an RFD RetrieveFormRequest, an Element from
    * the workflow context data might be passed to this method with the prefix
    * "workflowData.context.". Additional names will be appended to this when
    * generating the name for the name value pair.
    * @param element Element to load from
    * @param wsLog WSLog transaction to load to
    * @param isRequest boolean is element from SOAP Request? true for Request,
    * false for Response.
    */
   public static void element(String prefix, Element element, WSLog wsLog,
      boolean isRequest) {
      String name = prefix + "." + element.getTagName();
      NodeList nodeList = element.getChildNodes();
      List <Element> childElements = new ArrayList <>();
      for (int i = 0; i < nodeList.getLength(); i++ ) {
         Node node = nodeList.item(i);
         short nodeType = node.getNodeType();
         if (nodeType == Node.TEXT_NODE) {
            addPair(wsLog, name, node.getNodeValue(), isRequest);
         }
         if (nodeType == Node.ELEMENT_NODE) childElements.add((Element) node);
      }
      for (Element childElement : childElements)
         LoadNameValuePairs.element(name, childElement, wsLog, isRequest);

   } // EO element method

   /**
    * Add a Name/Value pair to a WSLog instance
    * 
    * @param wsLog instance to add to
    * @param name string
    * @param value string
    * @param isRequest boolean is this Name/Value pair being added to the
    * request list or the response list. If {@link #SOAP_REQUEST} it is added to
    * the request list, if {@link #SOAP_RESPONSE} it is added to the response
    * list.
    */
   public static void addPair(WSLog wsLog, String name, Object value,
      boolean isRequest) {
      if (isRequest) wsLog.addSoapRequestNameValuePair(name, value);
      else wsLog.addSoapResponseNameValuePair(name, value);
   }

} // EO LoadNameValuePairs class
