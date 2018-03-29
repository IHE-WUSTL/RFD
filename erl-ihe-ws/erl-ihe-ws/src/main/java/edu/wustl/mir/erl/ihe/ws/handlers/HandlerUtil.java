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
package edu.wustl.mir.erl.ihe.ws.handlers;

import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.handler.MessageContext;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.client.ClientTest;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.TransactionType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Static utility methods for handlers.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public class HandlerUtil implements WSProperties {

   /**
    * Get XML from a {@link Source} and convert it to a string.
    * 
    * @param source source of xml document
    * @return String representation of xml document.
    * @throws Exception on transformation error or I/O error.
    */
   public static String getXMLFromSource(Source source) throws Exception {
      String xml = null;

      try {
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         StreamResult sr = new StreamResult(bos);
         Transformer tf = TransformerFactory.newInstance().newTransformer();
         tf.transform(source, sr);
         xml = new String(bos.toString());
         bos.close();
      } catch (Exception e) {
         throw new Exception(e);
      }

      return xml;
   }

   /**
    * Generate a human readable string from {@link MessageContext} HTTP headers,
    * suitable for logging.
    * 
    * @param map {@link Map}, which is the form headers
    * are returned in.
    * @return String containing each header in the form:
    * 
    * <pre>
    * {@code
    * header name header value(s) <New Line>}
    * </pre>
    */
   public static String outputMap(Map <String, List <String>> map) {
      StringBuffer output = new StringBuffer("   ");
      Set <Entry <String, List <String>>> entries = map.entrySet();
      Iterator <Entry <String, List <String>>> it = entries.iterator();
      while (it.hasNext()) {
         Entry <String, List <String>> entry = it.next();
         String key = entry.getKey();
         if (key == null) key = "null";
         output.append(nl + "   ").append(key);
         for (String s : entry.getValue())
            output.append(" ").append(s);
      }
      return output.toString();
   }

   /**
    * Generate a human readable string from a QName, suitable for logging.
    * 
    * @param name {@link QName}.
    * @return String of the form: {namespaceURI}[prefix:]local_part
    */
   public static String outputQName(QName name) {
      StringBuffer output = new StringBuffer(" ");
      output.append("{" + name.getNamespaceURI() + "} ");
      if (name.getPrefix() != null) output.append(name.getPrefix() + ":");
      output.append(name.getLocalPart());
      return output.toString();
   }

   /**
    * Initialize handlers for a SOAP Client transaction. Must be invoked after
    * the port object is created and before the test request is sent. For
    * example, here is sample usage from the FormFiller for test
    * "RFD 0-1000 FM".
    * 
    * <pre>
    * {
    *    &#064;code
    *    FormManagerServiceClient service = new FormManagerServiceClient(url);
    *    FormManagerPortType port = service.getPort();
    *    HandlerUtil.initializeSOAPClientHandler((BindingProvider) port,
    *       (ClientTest) this, url);
    *    RetrieveFormResponseType response =
    *       port.formManagerRetrieveForm(retrieveFormRequest);
    * }
    * </pre>
    * 
    * @param bindingProvider the port class instance for the transaction, cast
    * to a {@link javax.xml.ws.BindingProvider BindingProvider} object.
    * @param clientTest the test class instance for the Client Test being
    * performed, cast to a {@link edu.wustl.mir.erl.ihe.ws.client.ClientTest
    * ClientTest} object.
    * @param wsServerURLString String URL for the WS Server Endpoint the
    * transaction is being sent to.
    * @return newly created LogTransaction instance.
    */
   public static LogTransaction initializeSOAPClientHandler(
      BindingProvider bindingProvider, ClientTest clientTest,
      String wsServerURLString) {
      LogTransaction logTran = null;
      try {
         URL wsServerURL = new URL(wsServerURLString);
         logTran = new LogTransaction(TransactionType.SOAP_CLIENT);
         logTran.loadFrom(clientTest);

         WSLog wsLog = logTran.getWsLog();
         int p = wsServerURL.getPort();
         if (p == -1) p = wsServerURL.getDefaultPort();
         if (p == -1) p = 80;
         wsLog.setServerPort(p);
         wsLog.setServiceName(wsServerURL.getPath());
         wsLog.setServerName(wsServerURL.getHost());

         Map <String, Object> requestContext =
            bindingProvider.getRequestContext();
         requestContext.put(WS_LOG_TRANSACTION, logTran);
         Binding binding = bindingProvider.getBinding();
         @SuppressWarnings("rawtypes")
         List <Handler> handlerChain = binding.getHandlerChain();
         handlerChain.add(new SOAPClientHandler());
         binding.setHandlerChain(handlerChain);
      } catch (Exception e) {
         Util.logEM(e, clientTest.getLog());
      }
      return logTran;
   }
} // EO HandlerUtil class
