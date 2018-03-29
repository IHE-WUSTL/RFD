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

import java.io.FileInputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;
import javax.xml.ws.Endpoint;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsConfigurator;
import com.sun.net.httpserver.HttpsServer;

import edu.wustl.mir.erl.ihe.util.NotificationThread;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.db.MessageType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.db.rmi.WSLogRMIClient;
import edu.wustl.mir.erl.ihe.ws.handlers.StoreSOAPMessages;

/**
 * Implements a wsdl web service {@link javax.xml.ws.Endpoint Endpoint} under
 * the control of {@link WSServer}.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class WSEndpoint extends NotificationThread implements Serializable,
   WSProperties {
   private static final long serialVersionUID = 1L;

   private static Set <String> endpointIds = new HashSet <>();
   private static Set <String> endpointNames = new HashSet <>();
   private static Map <Object, WSEndpoint> implementorToEndpoint =
      new HashMap <>();

   /**
    * A short string UID for this WSEndpoint, assigned from the id attribute of
    * the {@code <WSEndpoint>} element in the xml properties file. It can not be
    * empty. Each WSEndpoint is assigned a logger named named for the id with
    * the prefix "wse." allowing separate logging for endpoints.
    */
   private String endpointId;
   /**
    * A short human readable name given to each endpoint in application
    * properties. Used in logging.
    */
   private String endpointName;
   private String endpointClassName;
   private Class <?> endpointClass;
   private Object endpointClassInstance;
   private Endpoint endpoint;
   private String host = Util.getExternalHostName();
   /**
    * Listen port for this endpoint
    */
   private int port;
   /**
    * Path portion of the URI for this endpoint.
    */
   private String path;
   /**
    * The complete URI for this endpoint.
    */
   private URI uri;
   private String uriScheme;
   /**
    * Is {@link StoreSOAPMessages} facility enabled for this endpoint?
    */
   private boolean storeSOAPMessages = false;
   /**
    * SOAP Message storage sub directory for this endpoint.
    * 
    * @see StoreSOAPMessages#storeMessage(String, String, MessageType)
    */
   private String subDirectoryName;

   private List <HierarchicalConfiguration> testsProperties = null;

   /**
    * boolean, is this endpoint secure, (TLS)?
    */
   private boolean secure;
   /**
    * pfn of the java keystore file for this endpoint, if secure. Otherwise
    * blank.
    */
   private String keystoreFileName;
   private String keystorePassword;

   private KeyStore keyStore;
   private KeyManagerFactory kmf;
   private TrustManagerFactory tmf;
   private SSLContext sslContext;
   private static final String[] cyphersuites =
      { "TLS_RSA_WITH_AES_128_CBC_SHA" };
   /**
    * enum indicating the current status of this endpoint.
    */
   private Status status = Status.CREATED;
   /**
    * Additional text to specify endpoint status if needed, or blank.
    */
   private String statusMsg = "";

   /**
    * The {@link Logger} for this {@link WSEndpoint}. The logger name is based
    * on the {@link #endpointId}, but defaults to the SYSTEM log if the id is
    * invalid.
    */
   private Logger log = Util.getLog();

   private List <WSLogRMIClient> senders = new ArrayList <>();
   private Boolean storeToDB = false;

   /**
    * Constructor creates WSEndpoint instance using passed endpointProperties.
    * The instance can then be run, publishing the endpoint in a separate
    * thread.
    * 
    * @param endpointProperties the contents of one {@code <WSEndpoint>} element
    * from the application xml properties file.
    * @throws Exception if the properties are invalid or would generate a
    * duplicate endpoint.
    */
   WSEndpoint(HierarchicalConfiguration endpointProperties) throws Exception {

      try {

         // ------------------------------ must have id, not duplicate
         endpointId = endpointProperties.getString("[@id]");
         if (StringUtils.isBlank(endpointId))
            throw new Exception("skipping endpoint - no id");
         if (endpointIds.contains(endpointId))
            throw new Exception("skipping duplicate endpoint ID: " + endpointId);

         // ---------------------------- log for this WSEndpoint
         log = Logger.getLogger("wse." + endpointId);

         log.info("Loading WS Endpoint");

         // ----------------- must have name, not duplicate
         endpointName = endpointProperties.getString("[@name]");
         if (StringUtils.isBlank(endpointName))
            throw new Exception("skipping unamed endpoint");
         if (endpointNames.contains(endpointName))
            throw new Exception("skipping duplicate endpoint: " + endpointName);
         log.info(" Name:  " + endpointName);

         // ----------------------------- must have service class
         endpointClassName =
            StringUtils.trimToNull(endpointProperties.getString("[@class]"));
         if (StringUtils.isBlank(endpointClassName))
            throw new Exception("no service class name");
         try {
            endpointClass =
               Class.forName(endpointClassName, false,
                  ClassLoader.getSystemClassLoader());
         } catch (ClassNotFoundException cnfe) {
            throw new Exception("Could not load endpoint class: "
               + endpointClassName);
         }
         log.info(" service class: " + endpointClassName);

         // Does endpoint class implement WSServerTest?
         if (WSServerTest.class.isAssignableFrom(endpointClass) == false)
            throw new Exception("does not extend "
               + WSServerTest.class.getCanonicalName());

         // -------------------------------------- port and path
         port = endpointProperties.getInt("[@port]");
         if (port < 1 || port > 65535)
            throw new Exception("port # " + port + " not valid.");
         log.info(" port number: " + port);

         path = StringUtils.trimToNull(endpointProperties.getString("[@path]"));
         log.info(" path: " + (path == null ? "empty" : path));

         // ----------------- is connection secure? default true
         secure = endpointProperties.getBoolean("[@secure]", true);
         log.info(" secure: " + secure);

         // ------------------------------------ keystore and password
         uriScheme = "http";
         if (secure) {
            uriScheme = "https";
            keystoreFileName =
               endpointProperties.getString("keystore[@name]",
                  WSServer.getDefaultKeystoreName());
            log.trace(endpointName + " keystore: " + keystoreFileName);
            keystorePassword =
               endpointProperties.getString("keystore[@password]",
                  WSServer.getDefaultKeystorePassword());
         }

         // ------------------------------- Store SOAP Messages
         if (StoreSOAPMessages.isON()) {
            storeSOAPMessages = true;
            subDirectoryName = endpointId;
            try {
               HierarchicalConfiguration storeSOAPMessagesProperties =
                  endpointProperties.configurationAt("StoreSOAPMessages");
               storeSOAPMessages =
                  storeSOAPMessagesProperties.getBoolean("[@on]", true);
               subDirectoryName =
                  storeSOAPMessagesProperties.getString(
                     "[@subMessageDirectoryName]", subDirectoryName);
            } catch (Exception ee) {}
         }

         // --------------------------------------- WSLog RMI Senders
         if (WSServer.isRMIon()) {
            List <HierarchicalConfiguration> rmisProperties =
               endpointProperties.configurationsAt("RMI");
            for (HierarchicalConfiguration rmiProperties : rmisProperties) {
               if (rmiProperties.getBoolean("[@on]", true) == false) continue;
               int rmiPort =
                  rmiProperties.getInt("[@port]", DEFAULT_RMI_REGISTRY_PORT);
               try {
                  senders.add(new WSLogRMIClient(rmiPort, null));
               } catch (Exception e) {
                  log.info(e.getMessage());
               }
            } // EO add rmi Sender loop
         }

         // ------------------------------------- Tests for this endpoint
         testsProperties = endpointProperties.configurationsAt("Test");

         storeToDB = endpointProperties.getBoolean("[@storeToDB]", false);

      } catch (Exception e) {
         log.warn("Error creating WSEndpoint: - " + e.getMessage());
         throw e;
      }
   }

   /**
    * Instantiates the {@link Endpoint} and the implementor class and publishes
    * the Endpoint in a separate thread. If an Exception occurs logs an error
    * message and immediately returns.
    */
   public void startService() {
      try {
         // --------------------------- create / validate service URI
         uri = new URI(uriScheme, null, host, port, path, null, null);

         // ---------------- create service class instance and endpoint
         endpointClassInstance = endpointClass.newInstance();
         if (endpointClassInstance == null)
            throw new Exception("Could not instantiate " + endpointClassName);
         ((WSServerTest) endpointClassInstance).setWSEndpoint(this);
         if (testsProperties.isEmpty() == false)
            ((WSServerTest) endpointClassInstance).addTests(testsProperties);
         WebServiceFeature wsaFeature = new AddressingFeature(true, false);
         endpoint = Endpoint.create(endpointClassInstance, wsaFeature);
         endpoint.setExecutor(Util.getExec());

         // set up CORS Filter
         CORSFilter corsFilter = new CORSFilter(" ", log);
         HTTPLoggingFilter logFilter = new HTTPLoggingFilter(this);

         // ---------------------------------------- publish endpoint
         if (secure) {
            SecureRandom rand = new SecureRandom();
            rand.nextInt();

            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream(keystoreFileName),
               keystorePassword.toCharArray());
            kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, keystorePassword.toCharArray());

            // ------------------------------------ truststore
            tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(keyStore);
            log.trace("server certificates initialized");

            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), rand);
            log.trace("TLS trust context initialized");

            SSLParameters pars = sslContext.getDefaultSSLParameters();
            pars.setCipherSuites(cyphersuites);

            HttpsConfigurator configurator = new HttpsConfigurator(sslContext);

            HttpsServer httpsServer =
               HttpsServer.create(new InetSocketAddress(port), 0);
            httpsServer.setExecutor(Util.getExec());

            httpsServer.setHttpsConfigurator(configurator);

            HttpContext httpContext =
               httpsServer.createContext(uri.getPath().toString());
            httpContext.getFilters().add(corsFilter);
            httpContext.getFilters().add(logFilter);

            httpsServer.start();

            endpoint.publish(httpContext);

         } else {
            HttpServer httpServer =
               HttpServer.create(new InetSocketAddress(port), 0);
            HttpContext httpContext =
               httpServer.createContext(uri.getPath().toString());
            httpContext.getFilters().add(corsFilter);
            httpContext.getFilters().add(logFilter);
            httpServer.setExecutor(Util.getExec());
            httpServer.start();

            endpoint.publish(httpContext);
         }
         setStatus(Status.PUBLISHED, "");
         endpointNames.add(endpointName);
         implementorToEndpoint.put(endpointClassInstance, this);
         log.info(" published.");
         @SuppressWarnings("rawtypes")
         List <Handler> hndlrs = endpoint.getBinding().getHandlerChain();
         log.info(hndlrs.size() + " handlers");
      } catch (Exception e) {
         String em = "Attempt to publish endpoint failed: " + e.getMessage();
         setStatus(Status.PUBLISH_FAILED, em);
         if (endpoint != null) {
            endpoint.stop();
            endpoint = null;
         }
         log.warn(em);
         return;
      }

   } // EO startServer();

   @Override
   public void doRun() {
      try {
         while (true) {
            for (int i = 0; i < 60; i++ ) {
               Thread.sleep(15000);
               if (endpoint.isPublished() == false)
                  throw new Exception("no longer published");
            }
            log.trace(endpointName + " " + getEndpointStatus());
         }
      } catch (Exception ie) {
         log.error(endpointName + " error " + ie.getMessage()
            + " shutting down");
         if (endpoint != null && endpoint.isPublished()) endpoint.stop();
      }
   } // EO doRun method

   /**
    * @return {@link #endpointName}
    */
   public String getEndpointName() {
      return endpointName;
   }

   /**
    * @return short endpoint status string
    */
   public String getEndpointStatus() {
      return status.message + statusMsg
         + (status.terminated ? " terminated" : " ");
   }

   /**
    * @return {@link #port}
    */
   public int getPort() {
      return port;
   }

   /**
    * @return {@link #secure}
    */
   public boolean isSecure() {
      return secure;
   }

   /**
    * @return {@link #keystoreFileName}
    */
   public String getKeystoreFileName() {
      return keystoreFileName;
   }

   /**
    * @return {@link #endpointId}
    */
   public String getEndpointId() {
      return endpointId;
   }

   /**
    * @return {@link #path}
    */
   public String getPath() {
      return path;
   }

   /**
    * @return {@link #log}
    */
   public Logger getLog() {
      return log;
   }

   /**
    * @return the {@link #storeSOAPMessages} value.
    */
   public boolean isStoreSOAPMessages() {
      return storeSOAPMessages;
   }

   /**
    * @return the {@link #subDirectoryName} value.
    */
   public String getSubDirectoryName() {
      return subDirectoryName;
   }

   /**
    * @return the {@link #status} value.
    */
   public Status getStatus() {
      return status;
   }

   private void setStatus(Status stat, String msg) {
      status = stat;
      statusMsg = msg;
      if (status.terminated) {
         log.warn(status.message + " " + statusMsg);
      } else {
         log.info(status.message);
      }
   }

   /**
    * @return boolean is this endpoint terminated?
    */
   public boolean isTerminated() {
      return status.terminated;
   }

   /**
    * @return the {@link #statusMsg} value.
    */
   public String getStatusMsg() {
      return statusMsg;
   }

   /**
    * @return a short human readable description of the endpoint, of the form:
    * 
    * <pre>
    * Endpoint {@link #endpointId id} {@link #endpointName name}
    * </pre>
    */
   public String toStringShort() {
      return "Endpoint " + endpointId + " " + endpointName + " ";
   }

   /**
    * @return a human readable description of the endpoint, of the form:
    * 
    * <pre>
    * Endpoint {@link #endpointId id} {@link #endpointName name} {@link #uri}
    * </pre>
    */
   public String toStringLong() {
      return toStringShort() + " at " + uri.toString() + " ";
   }
   
   /**
    * @return the URI for this endpoint
    */
   public URI getURI() {
      return uri;
   }

   /**
    * @param implementor java instance of an implementor class used in an
    * {@link Endpoint} of the {@link WSServer}.
    * @return The {@link WSEndpoint} wrapping the {@link Endpoint} which uses
    * the passed implementor class instance.
    */
   public static WSEndpoint getWSEndpointForImplementor(Object implementor) {
      try {
         return implementorToEndpoint.get(implementor);
      } catch (Exception e) {
         Util.getLog().warn(
            "getWSEndpointForImplementor error - WS Implementor: "
               + implementor.toString() + " not found.");
      }
      return null;
   }

   /**
    * Inner enum for WSEndpoint status values. Also provides a short status
    * message, which should be augmented with the value of
    * {@link WSEndpoint#statusMsg statusMsg} if it is not empty.
    */
   enum Status {

      /**
       * CREATED: The WSEndpoint constructor has completed successfully. This is
       * the initial status of all Endpoints. Not terminated, no additional msg.
       */
      CREATED(false, "created"),
      /**
       * PUBLISH_FAILED: An attempt to publish the WSEndpoint has been made, but
       * it terminated unsuccessfully. Endpoint is terminated. Additional msg
       * should be available.
       */
      PUBLISH_FAILED(true, "Could not publish"),
      /**
       * PUBLISHED: The WSEndpoint was published successfully. Not terminated,
       * no additional msg.
       */
      PUBLISHED(false, "Published"),
      /**
       * CLOSED: Endpoint was published, and is now closed. Endpoint is
       * terminated. Additional msg should be available.
       */
      CLOSED(true, "Closed");

      boolean terminated;
      String message;

      private Status(boolean term, String msg) {
         terminated = term;
         message = msg;
      }
   } // EO Status enum

   /**
    * Sends passed {@link WSLog} instance to all RMI destinations set up for
    * this endpoint. Will not generate errors on failure.
    * 
    * @param wsLog instance to send.
    */
   private synchronized void sendWSLog(WSLog wsLog) {
      for (WSLogRMIClient sender : senders) {
         sender.sendWSLog(wsLog);
      }
   }

   /**
    * Persists {@link WSLog} instance as indicated for this WSEndpoint:
    * <ul>
    * <li>To any RMI destinations which have been set up in the properties.</li>
    * <li>To the DB if the storeToDB attribute in the properties is true.</li>
    * </ul>
    * 
    * @param wsLog transaction log instance
    */
   public synchronized void persistWSLog(WSLog wsLog) {
      sendWSLog(wsLog);
      try {
         if (storeToDB) wsLog.insert();
      } catch (Exception e) {
         log.error("Error persisting wslog instance for "
            + wsLog.getClientHostName() + " to database." + e.getMessage());
      }
   }

} // EO WSEndpoint class
