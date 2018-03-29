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
package edu.wustl.mir.erl.ihe.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.Filter;
import com.sun.net.httpserver.HttpExchange;

import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Simple {@link com.sun.net.httpserver.Filter Filter} to skip
 * <pre>
 * {@code
 * GET /favicon.ico
 * }
 * </pre>
 * HTTP Requests
 */
public class FaviconFilter extends Filter implements Serializable {
   private static final long serialVersionUID = 1L;

   private String description = "favicon filter";
   private Logger log = Util.getLog();

   /**
    * No argument constructor.
    */
   public FaviconFilter() {}

   /**
    * Constructor with logger
    * @param logger logger to use.
    */
   public FaviconFilter(Logger logger) {
      log = logger;
      log.info(description + "initialized.");
   }

   @Override
   public void doFilter(HttpExchange exchange, Chain chain) throws IOException {
      log.debug(Util.classMethod() + "invoked");

      String requestMethod = exchange.getRequestMethod();
      String requestPath = exchange.getRequestURI().getPath();
      if (requestMethod.equalsIgnoreCase("GET") &&
         requestPath.toLowerCase().startsWith("/favicon".toLowerCase())) {
         log.debug("GET /favicon HTTP Request skipped");
         // Consume request body
         InputStream is = exchange.getRequestBody();
         if (is != null) IOUtils.toString(is);
         // Send OK and close
         exchange.sendResponseHeaders(200, -1);
         exchange.close();
         return;
      }
      chain.doFilter(exchange);
      return;
   }

   @Override
   public String description() {
      return description;
   }

}
