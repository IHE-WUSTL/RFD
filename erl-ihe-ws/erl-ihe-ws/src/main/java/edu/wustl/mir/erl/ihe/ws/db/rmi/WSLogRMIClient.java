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
package edu.wustl.mir.erl.ihe.ws.db.rmi;

import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * RMI Client (Sender) for {@link WSLog} instances created when a WS message
 * is processed.
 * 
 * @see WSLogRMIServer
 */
public class WSLogRMIClient implements Serializable, WSProperties {
   private static final long serialVersionUID = 1L;

   private int rmiPort = DEFAULT_RMI_REGISTRY_PORT;
   private WSLogRemoteInterface stub = null;
   private Logger log = null;
   private String description;

   /**
    * Create WSLog RMI Sender (client) instance
    * 
    * @param port rmi registry port name, default is
    * {@link WSProperties#DEFAULT_RMI_REGISTRY_PORT}.
    * @param logger {@link org.apache.log4j.Logger Logger} instance to use for
    * any log messages generated during the construction or use of this
    * WSLogRMISender instance, default is {@link Util#getLog() system log}.
    * @throws Exception on errors, for example:
    * <ul>
    * <li>Not finding the rmi registry, which must exist before this
    * constructor is called.</li>
    * <li>Not finding the rmi entry for "name", or it is wrong type.</li>
    * </ul>
    */
   public WSLogRMIClient(Integer port, Logger logger)
      throws Exception {
      if (port != null) rmiPort = port;
      if (logger == null) log = Logger.getLogger("rmi." + WSLOG_RMI_SHORT_NAME);
      else log = logger;
      description =
         "WSLogRMIClient: port=" + rmiPort + " name="
            + WSLOG_RMI_SHORT_NAME + " ";
      try {
         Registry registry = LocateRegistry.getRegistry(rmiPort);
         stub = (WSLogRemoteInterface) registry.lookup(WSLOG_RMI_LONG_NAME);
         log.info(description + " connection established.");
      } catch (Exception e) {
         String em = description + "error: " + e.getMessage();
         log.error(em);
         throw new Exception(em);
      }
   } // EO constructor

   /**
    * Sends passed {@link WSLog} transaction instance to rmi destination. On
    * error, logs, but does not throw Exception.
    * 
    * @param wsLog instance to send.
    */
   public void sendWSLog(WSLog wsLog) {
      try {
         stub.submitWSLog(wsLog);
         log.info(description + " sent WSLog for message from "
            + wsLog.getServerName());
      } catch (Exception e) {
         log.error(description + "error sending WSLog for message from: "
            + wsLog.getServerName() + " " + e.getMessage());
      }
   }

} // EO WSLogRMI Sender
