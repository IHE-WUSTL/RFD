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
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.handlers.HandlerUtil;

/**
 * Simple {@link com.sun.net.httpserver.Filter Filter} to provide <a
 * href="http://en.wikipedia.org/wiki/Cross-origin_resource_sharing">CORS</a>
 * support for {@link WSEndpoint}s.
 */
@SuppressWarnings("javadoc")
public class CORSFilter extends Filter implements Serializable {
   private static final long serialVersionUID = 1L;

   private String description;
   /**
    * HTTP Request methods to accept CORS transactions on. Defaults to GET and
    * POST, but can be overridden in constructor.
    */
   private String[] methods = new String[] { "GET", "POST" };
   private Logger log = Util.getLog();

   public static final String ACCESS_CONTROL_ALLOW_ORIGIN =
      "Access-Control-Allow-Origin";
   public static final String ACCESS_CONTROL_ALLOW_CREDENTIALS =
      "Access-Control-Allow-Credentials";
   public static final String ACCESS_CONTROL_EXPOSE_HEADERS =
      "Access-Control-Expose-Headers";
   public static final String ACCESS_CONTROL_MAX_AGE = "Access-Control-Max-Age";
   public static final String ACCESS_CONTROL_ALLOW_METHODS =
      "Access-Control-Allow-Methods";
   public static final String ACCESS_CONTROL_ALLOW_HEADERS =
      "Access-Control-Allow-Headers";
   public static final String ORIGIN = "Origin";
   public static final String ACCESS_CONTROL_REQUEST_METHOD =
      "Access-Control-Request-Method";
   public static final String ACCESS_CONTROL_REQUEST_HEADERS =
      "Access-Control-Request-Headers";

   /**
    * Constructor
    * 
    * @param name of the {@link WSEndpoint} for this filter instance
    * @param log {@link org.apache.log4j.Logger Logger} to use. If null, the
    * {@link edu.wustl.mir.erl.ihe.util.Util#getLog() System log} is used.
    * @param method HTTP Request <a
    * href="http://tools.ietf.org/html/rfc2616#section-5.1.1">Methods</a> which
    * are to be accepted on CORS Requests. If no values are passed, defaults to
    * GET and PUT.
    */
   public CORSFilter(String name, Logger log, String... method) {
      description = name + " CORS support filter";
      if (log != null) this.log = log;
      if (method.length > 0) methods = method;
      this.log.info(description + " initialized");
   }

   /**
    * Provides CORS support functions.
    * 
    * @param exchange HTTP Exchange instance being processed.
    * @param chain The chain of filters associated with this HttpServer.
    */
   @Override
   public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
      log.trace(Util.classMethod() + "inbound");
      String requestMethod = exchange.getRequestMethod();
      Headers requestHeaders = exchange.getRequestHeaders();
      Headers responseHeaders = exchange.getResponseHeaders();

      if (requestMethod.equalsIgnoreCase("OPTIONS") == false) {
         chain.doFilter(exchange);
         log.trace(Util.classMethod() + "outbound");
         return;
      }

      log.trace(Util.classMethod() + "processing OPTIONS Request");

      // ------------------------------- All origins are allowed.
      responseHeaders.add(ACCESS_CONTROL_ALLOW_ORIGIN, "*");

      // -------------------------------- Allow specified Request methods
      for (String method : methods) {
         responseHeaders.add(ACCESS_CONTROL_ALLOW_METHODS, method);
      }

      // --------------------- All requested headers are allowed
      if (requestHeaders.containsKey(ACCESS_CONTROL_REQUEST_HEADERS)) {
         List <String> requestedHeaders =
            requestHeaders.get(ACCESS_CONTROL_REQUEST_HEADERS);
         for (String requestedHeader : requestedHeaders) {
            responseHeaders
               .add(ACCESS_CONTROL_ALLOW_HEADERS, requestedHeader);
         }
      }

      // ----------------------------- max age
      responseHeaders.add(ACCESS_CONTROL_MAX_AGE, "1728000");

      log.debug("Request Headers" + HandlerUtil.outputMap(requestHeaders));
      log.debug("Response Headers" + HandlerUtil.outputMap(responseHeaders));
      // Consume request body
      InputStream is = exchange.getRequestBody();
      if (is != null) IOUtils.toString(is);
      // Send OK and close
      exchange.sendResponseHeaders(200, -1);
      exchange.close();
   }

   /**
    * Returns human readable description of filter function.
    */
   @Override
   public String description() {
      return description;
   }

}
