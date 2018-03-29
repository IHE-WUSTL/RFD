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
package edu.wustl.mir.erl.ihe.ws.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import org.apache.commons.cli.Option;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang3.text.StrBuilder;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.server.FileServer;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.UtilProperties;
import edu.wustl.mir.erl.ihe.util.jdbc.JDBC;
import edu.wustl.mir.erl.ihe.util.jdbc.RDBMS;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.client.ClientTest;
import edu.wustl.mir.erl.ihe.ws.client.WSClient;
import edu.wustl.mir.erl.ihe.ws.handlers.StoreSOAPMessages;

/**
 * WSDL Web Service Server main class. Publishes one or more web service
 * {@link WSEndpoint}(s) in separate threads, as specified in the application
 * properties file. Handles parameter validation and server setup functions.
 */
public class WSServer implements Serializable, UtilProperties {
   private static final long serialVersionUID = 1L;

   /** Default logical DB name for all WSServer based applications */
   protected static final String wslog = "wslog";
   /** default system log */
   private static Logger log = null;
   /** Application configuration file */
   private static XMLConfiguration properties = null;
   /** application thread executor */
   private static Executor exec = Util.getExec();
   /**
    * The default certificate keystore name for the application, taken from the
    * properties file WSTests.keystore[@name] value. This keystore will be used
    * by all WSTests unless they have their own &lt;keystore> element.
    */
   private static String defaultKeystoreName;
   /**
    * The password for the application default certificate keystore, taken from
    * the properties file WSTests.keystore[@password] value.
    */
   private static String defaultKeystorePassword;

   /**
    * boolean, should RMI be used?
    * If true, RMI is used as indicated in the properties.xml file.
    * If false, RMI is not used, regardless of settings in the properties.xml
    * file.
    */
   private static boolean RMIon = false;

   /**
    * List of the WSEndpoints which have been successfully published by
    * WSServer.
    */
   private static List <WSEndpoint> wsEndpoints = new ArrayList <>();
   private static int numberOfServices;
   @SuppressWarnings("unused")
   private static WSClient wsClient = null;
   @SuppressWarnings("unused")
   private static int numberOfClients;

   @SuppressWarnings("unused")
   private static WSServer wsServer = new WSServer();

   private static Map <String, FileServer> fileServers = new HashMap <>();

   /**
    * Main method for WSServer. Starts {@link WSEndpoint}(s) which encapsulate
    * {@link javax.xml.ws.Endpoint Endpoint}(s) as indicated in .properties
    * file. Each Endpoint is in a separate thread, and web methods are invoked
    * in separate threads.
    * 
    * @param args In addition to the parameters in
    * {@link Util#initializeCommandLine(String, String[], Object[])
    * Util.initializeCommandLine}, a boolean parameter "-rmi" is added. See
    * {@link #RMIon}.
    */
   public static void main(String[] args) {

      try {

         // ----------- Initialize from command line parameters.

         Object[] rmi =
            new Object[] { new Option("rmi", "rmi", false, "use RMI?") };

         Map <String, String> addOnPars =
            Util.initializeCommandLine("WSServer", args, rmi);
         if (addOnPars.containsKey("rmi")) RMIon = true;

         log = Util.getLog();

         properties = Util.getProperties();

         JDBC.setSupportedDatabases(RDBMS.POSTGRESQL);
         JDBC.init(wslog);

         defaultKeystoreName =
            properties.getString("WSTests.keystore[@name]", null);
         defaultKeystorePassword =
            properties.getString("WSTests.keystore[@password]", null);
         
         StoreSOAPMessages.initStoreSOAPMessages();

         // *********************************** Start FileServers
         List <HierarchicalConfiguration> fileServersProperties =
            properties.configurationsAt("FileServers.FileServer");
         for (HierarchicalConfiguration fileServerProperties : fileServersProperties) {
            try {
               FileServer fileServer = new FileServer(fileServerProperties);
               fileServer.addListenerClass(WSServer.class);
               fileServers.put(fileServer.getId(), fileServer);
               exec.execute(fileServer);
            } catch (Exception fsce) {
               Util.exit(fsce.getMessage());
            }
         } // EO start file servers loop

         // ************************************* Start endpoints

         // ---------------------- Pull endpoint elements from properties
         List <HierarchicalConfiguration> endpointsProperties =
            properties.configurationsAt("WSTests.WSEndpoint");

         // This is the number of endpoints we need to account for.
         numberOfServices = endpointsProperties.size();

         // ------ first pass through endpoints, instantiate WSEndpoint objects
         for (HierarchicalConfiguration endpointProperties : endpointsProperties) {
            WSEndpoint wsEndpoint = null;
            try {
               wsEndpoint = new WSEndpoint(endpointProperties);
            } catch (Exception ex) {
               // Couldn't instantiate. Accounts for one service.
               numberOfServices-- ;
               log.warn("Couldn't instantiate service: " + ex.getMessage());
               continue;
            }
            try {
               wsEndpoint.addListenerClass(WSServer.class);
               // Add newly instantiated WSEndpoint to list
               wsEndpoints.add(wsEndpoint);
               // Publish WSEndpoint
               wsEndpoint.startService();

               exec.execute(wsEndpoint);
            } catch (Exception e) {
               /*
                * Any error here is probably a programming error, as errors in
                * the run method of wsEndpoint don't come here.
                */
               log.error("Error in add or execute " + e.getMessage());
               // Couldn't process
               wsEndpoints.remove(wsEndpoint);
               numberOfServices-- ;
            }

         } // EO Process Services loop

         // ************************************* start clients
         clients: {
            numberOfClients = 0;
            if (!RMIon) {
               log.info("RMI not on. client tests not started");
               break clients;
            }
            // Pull property sets for clients
            List <HierarchicalConfiguration> clientsProperties =
               properties.configurationsAt("WSTests.WSClient");
            // Start client RMI server class
            if (clientsProperties.isEmpty() == false) {
               int port =
                  properties.getInt("WSTests[@port]",
                     WSProperties.DEFAULT_RMI_REGISTRY_PORT);
               wsClient = WSClient.initialize(port);
            }
            // Pass client property sets
            for (HierarchicalConfiguration clientProperties : clientsProperties) {
               try {
                  ClientTest.initialize(clientProperties);
                  numberOfClients++;
               } catch (Exception e) {
                  log.warn(e.getMessage());
               }
            }
         } // EO clients block
         
         // ******************** TODO Need to add clients to below code

         if (numberOfServices == 0)
            throw new Exception("No valid services started");

         // dumb calculations to line up status log entries.
         int cid = 0, cn = 0, cs = 11, co = 5, i;
         for (WSEndpoint e : wsEndpoints) {
            if ((i = e.getEndpointId().length() + 1) > cid) cid = i;
            if ((i = e.getEndpointName().length() + 1) > cn) cn = i;
            if ((i = e.getStatus().message.length() + 1) > cs) cs = i;
            if ((i = (new Integer(e.getPort()).toString().length()) + 1) > co)
               co = i;
         }

         // status log entries, using dumb calculations.
         while (true) {
            Thread.sleep(60 * 15 * 1000);
            if (numberOfServices == 0)
               throw new Exception("No published services");
            StrBuilder msg = new StrBuilder("Enpoint status:" + nl);
            for (WSEndpoint endPt : wsEndpoints) {
               StrBuilder line =
                  new StrBuilder("  ")
                     .appendFixedWidthPadRight(endPt.getEndpointId(), cid, ' ')
                     .appendFixedWidthPadRight(endPt.getEndpointName(), cn, ' ')
                     .appendFixedWidthPadRight(endPt.getStatus().message, cs,
                        ' ')
                     .appendFixedWidthPadLeft(endPt.getPort(), co - 1, ' ')
                     .append(" ").append(endPt.getPath()).append(nl);
               msg.append(line);
            }
            log.info(msg);
         }

      } // EO primary try block in main method
      catch (Exception e) {
         String em = "WSServer exiting: " + e.getMessage();
         e.printStackTrace();
         log.fatal(em);
         shutDownWSServer();
      }

   } // EO main method

   /**
    * @return {@link #defaultKeystoreName}
    */
   public static String getDefaultKeystoreName() {
      return defaultKeystoreName;
   }

   /**
    * @return {@link #defaultKeystorePassword}
    */
   public static String getDefaultKeystorePassword() {
      return defaultKeystorePassword;
   }

   /**
    * @return {@link #wsEndpoints}
    */
   public static List <WSEndpoint> getWSEndpoints() {
      return wsEndpoints;
   }

   /**
    * @return boolean is RMI turned on? {@link #RMIon}
    */
   public static boolean isRMIon() {
      return RMIon;
   }

   /**
    * Get the {@link WSEndpoint} matching the passed parameters
    * 
    * @param port {@link WSEndpoint#getPort() port}
    * @param path {@link WSEndpoint#getPath() path}
    * @return WSEndpoint instance published on the passed port and path, or
    * <code>null</code> if no WSEndpoing instance matches.
    * <b>Note:</b>If there is only one WSEndpoing, it is automatically returned.
    */
   public static WSEndpoint getWSEndpoint(Integer port, String path) {
      if (wsEndpoints.size() == 1) return wsEndpoints.get(0);
      for (WSEndpoint wse : wsEndpoints) {
         if (port == wse.getPort() && path.equals(wse.getPath())) return wse;
      }
      return null;
   }

   /**
    * static Listener method called by WSEndpoint instances when they complete.
    * 
    * @param wsEndpoint instance which has just completed.
    */
   public static void threadComplete(WSEndpoint wsEndpoint) {

      wsEndpoints.remove(wsEndpoint);
      numberOfServices-- ;
      if (numberOfServices == 0) {
         log.fatal("No more running services. Exiting.");
         shutDownWSServer();
      }
   }

   private static void shutDownWSServer() {
      System.exit(1);
   }

   /**
    * @param fileServerId the unique file server id for the desired file server
    * as specified in the {@code <FileServer>} id attribute in the properties
    * file.
    * @return The FileServer instance corresponding to the fileServerId, or null
    * if no such instance exists.
    */
   public static FileServer getFileServerById(String fileServerId) {
      if (fileServers.containsKey(fileServerId))
         return fileServers.get(fileServerId);
      return null;
   }

   /**
    * static Listener method called by WSEndpoint instances when they complete.
    * 
    * @param fileServer instance which has just completed.
    */
   public static void threadComplete(FileServer fileServer) {
      fileServers.remove(fileServer.getId());
   }

   /**
    * @return server Logger
    */
   public static Logger getLog() {
      return log;
   }

} // EO WSServer class
