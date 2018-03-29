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
package edu.wustl.mir.erl.ihe.ws.server;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

import org.apache.log4j.Logger;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.TransactionType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.handlers.HandlerUtil;
import edu.wustl.mir.erl.ihe.ws.handlers.WSHandlerStatus;

/**
 * <p>HTTP Logging Filter for Web service endpoints. A single instance is created
 * during the creation and publishing of a WS endpoint in
 * {@link WSEndpoint#startService()}, and added to the filters in the
 * {@link com.sun.net.httpserver.HttpContext HttpContext} for that endpoint. The
 * {@link #doFilter(HttpExchange, Chain) doFilter} method is then invoked for
 * each http-SOAP exchange processed for that endpoint, and processes both
 * {@link #inbound(HttpExchange) inbound} and {@link #outbound(HttpExchange)
 * outbound}.
 * </p>
 * <b>Notes to Programmers:</b>
 * <ul>
 * <li>All access to instance properties of this class must be thread safe.</li>
 * <li>This class can only deal with data stored as an
 * {@link com.sun.net.httpserver.HttpExchange HttpExchange} attribute.</li>
 * </ul>
 */
public class HTTPLoggingFilter extends Filter implements Serializable,
   WSProperties {
   private static final long serialVersionUID = 1L;

   private static final String NO_MAP = "httpExchange has no mapping for ";

   /** WSEndpoint instance this filter instance was created for. */
   private WSEndpoint wsEndpoint;
   /** Text description of this filter, required by super class. */
   private String description;
   /**
    * Logger for this filter. All log messages for a given endpoint will go to
    * the same Logger.
    */
   private Logger log;

   /**
    * @param wsEndpoint the {@link WSEndpoint} this filter instance is attached
    * to.
    */
   public HTTPLoggingFilter(WSEndpoint wsEndpoint) {
      this.wsEndpoint = wsEndpoint;
      description =
         wsEndpoint.getEndpointName() + " HTTP Request/Response Logging Filter";
      log = wsEndpoint.getLog();
   }

   /*
    * (non-Javadoc)
    * 
    * @see
    * com.sun.net.httpserver.Filter#doFilter(com.sun.net.httpserver.HttpExchange
    * , com.sun.net.httpserver.Filter.Chain)
    */
   @Override
   public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
      inbound(exchange);
      chain.doFilter(exchange);
      Util.pause(3, log, "testing pause");
      outbound(exchange);
   }

   /**
    * HTTP Logging Filter inbound processing. First 'contact' with the incoming
    * HTTP-SOAP Request:
    * <ul>
    * <li>Create and initialize the
    * {@link edu.wustl.mir.erl.ihe.ws.db.LogTransaction WSLogTransaction}.</li>
    * <li>Add inbound {@link edu.wustl.mir.erl.ihe.ws.server.WSEndpoint
    * WSEndpoint} and HTTP related data to
    * {@link edu.wustl.mir.erl.ihe.ws.db.WSLog WSLog}.</li>
    * <li>Store the WSLogTransaction as an
    * {@link com.sun.net.httpserver.HttpExchange HttpExchange} attribute</li>
    * </ul>
    * 
    * @param httpExchange {@link com.sun.net.httpserver.HttpExchange
    * HttpExchange} instance for the Request/Response being processed.
    */
   private void inbound(HttpExchange httpExchange) {
      log.trace(Util.classMethod());

      LogTransaction wsLogTransaction = null;
      WSLog wsLog = null;

      try {
         // ---------------------------- Create transaction log instance
         wsLogTransaction = new LogTransaction(TransactionType.SOAP_SERVER);
         wsLog = wsLogTransaction.getWsLog();
         // ----------------------------------- store in HTTP exchange object
         httpExchange.setAttribute(WS_LOG_TRANSACTION, wsLogTransaction);

         // --------------------------------- store http Request related data
         wsLog.addStatus(WSHandlerStatus.HTTP_IN_IN_PROGRESS);

         wsLogTransaction.loadFrom(wsEndpoint);

         wsLogTransaction.loadFromRemote(httpExchange.getRemoteAddress()
            .getAddress());
         wsLogTransaction.loadFromLocal(httpExchange.getLocalAddress()
            .getAddress());

         wsLog.setHttpMethod(httpExchange.getRequestMethod());
         wsLog.setHttpRequestURI(httpExchange.getRequestURI());
         wsLog.setHttpRequestProtocol(httpExchange.getProtocol());

         Headers headers = httpExchange.getRequestHeaders();
         if (headers == null) {
            log.warn(NO_MAP + HTTP_REQUEST_HEADERS);
         } else {
            wsLog.setHttpRequestHeaders(headers);
            log.trace("http request headers:" + HandlerUtil.outputMap(headers));
         }

         
//         InputStream is = httpExchange.getRequestBody();
//         if (is != null) {
//            wsLog.addMessage("HTTP Request Body", MessageType.HTTP_IN_BODY,
//               IOUtils.toString(is, UTF_8));
//         } else {
//            wsLog.addStatus(WSHandlerStatus.HTTP_IN_NO_BODY);
//         }

         wsLog.addStatus(WSHandlerStatus.HTTP_IN_OK);

      } catch (Exception e) {
         StringBuilder em =
            new StringBuilder(Util.classMethod() + " error: " + e.getMessage());
         if (wsLogTransaction != null)
            em.append(nl).append(wsLogTransaction.getWsLog().toStringLong())
               .append(nl)
               .append(wsLogTransaction.getWsLog().toStringClientIfno());
         log.warn(em);
      }

   } // EO inbound method

   /**
    * HTTP Logging Filter outbound processing. Last 'contact' with the incoming
    * HTTP-SOAP Request:
    * <ul>
    * <li>Add outbound HTTP related data to
    * {@link edu.wustl.mir.erl.ihe.ws.db.WSLog WSLog}.</li>
    * <li>Close out the {@link edu.wustl.mir.erl.ihe.ws.db.LogTransaction
    * WSLogTransaction}.</li>
    * </ul>
    * 
    * @param httpExchange {@link com.sun.net.httpserver.HttpExchange
    * HttpExchange} instance for the Request/Response being processed.
    */
   private void outbound(HttpExchange httpExchange) {

      // Util.pause(3, log, "test theory on SAX error");
      log.trace(Util.classMethod());

      LogTransaction wsLogTransaction = null;
      WSLog wsLog = null;

      try {

         wsLogTransaction =
            (LogTransaction) httpExchange.getAttribute(WS_LOG_TRANSACTION);
         wsLog = wsLogTransaction.getWsLog();

         wsLog.addStatus(WSHandlerStatus.HTTP_OUT_IN_PROGRESS);

         wsLog.setHttpResponseCode(httpExchange.getResponseCode());
         wsLog.setHttpResponseHeaders(httpExchange.getResponseHeaders());

         wsLog.addStatus(WSHandlerStatus.HTTP_OUT_OK);

         // log (approximate) connection close and close out.
         wsLogTransaction.getWsLog().setConnCloseTime(new Date());
         wsLogTransaction.close();

      } catch (Exception e) {
         StringBuilder em =
            new StringBuilder(Util.classMethod() + " error: " + e.getMessage());
         if (wsLogTransaction != null)
            em.append(nl).append(wsLogTransaction.getWsLog().toStringLong())
               .append(nl)
               .append(wsLogTransaction.getWsLog().toStringClientIfno());
         log.warn(em);
      }
   } // EO outbound method

   /*
    * (non-Javadoc)
    * 
    * @see com.sun.net.httpserver.Filter#description()
    */
   @Override
   public String description() {
      return description;
   }

}
