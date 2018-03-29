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
package edu.wustl.mir.erl.ihe.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import edu.wustl.mir.erl.ihe.util.HTTPProperties;
import edu.wustl.mir.erl.ihe.util.NotificationThread;
import edu.wustl.mir.erl.ihe.util.Plug;
import edu.wustl.mir.erl.ihe.util.Result;
import edu.wustl.mir.erl.ihe.util.Status;
import edu.wustl.mir.erl.ihe.util.StatusHelper;
import edu.wustl.mir.erl.ihe.util.StatusType;
import edu.wustl.mir.erl.ihe.util.StatusUpdateType;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.Util.PfnType;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.MessageType;
import edu.wustl.mir.erl.ihe.ws.db.TransactionType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.db.rmi.WSLogRMIClient;
import edu.wustl.mir.erl.ihe.ws.server.WSServer;

/**
 * Implements a simple HTTP file server which can be used along with
 * {@link edu.wustl.mir.erl.ihe.ws.server.WSEndpoint WSEndpoint} instances under
 * the control of a {@link edu.wustl.mir.erl.ihe.ws.server.WSServer WSServer},
 * in cases where Web services return URLs for files to be downloaded as part of
 * their response.
 */
public class FileServer extends NotificationThread implements Serializable,
   WSProperties, HTTPProperties {
   private static final long serialVersionUID = 1L;

   private static Set <String> fileServerIds = new HashSet <>();

   /**
    * A short string UID for this FileServer, assigned from the id attribute of
    * the {@code <FileServer>} element in the xml properties file. It can not be
    * empty. Each FileServer is assigned a logger named named for the id with
    * the prefix "fs." allowing separate logging for file servers.
    */
   private String fileServerId;
   private HttpServer fileServer;
   private Logger log = Util.getLog();
   private String host = Util.getExternalHostName();
   private int port;
   private Path fileDirectory;
   private Path formDirectory;
   private AtomicInteger nextFormNumber = new AtomicInteger(0);
   private Handler getHandler;
   private String desc;

   private List <WSLogRMIClient> senders = new ArrayList <>();
   private Boolean storeToDB = false;

   private boolean WSLogging() {
      if (storeToDB == false && senders.isEmpty()) return false;
      return true;
   }

   /**
    * Beginning of URI for this file server, up to and including the root path
    * slash character. For example:
    * 
    * <pre>
    * {@code
    *    http://gazelle-gold.wustl.edu:3300/
    * }
    * </pre>
    * 
    * @return uri prefix string
    */
   public String getFileUriPrefix() {
      return "http://" + host + ":" + port + "/";
   }

   /**
    * Beginning of URI for form server, up to and including the root path slash
    * character. For example:
    * 
    * <pre>
    * {@code
    *    http://gazelle-gold.wustl.edu:3300/form/
    * }
    * </pre>
    * 
    * @return uri prefix string
    */
   public String getFormUriPrefix() {
      return "http://" + host + ":" + port + "/form/";
   }

   /**
    * returns the unique file server id, never null or empty.
    * 
    * @return file server ID, as specified in the {@code <FileServer>} id
    * attribute in the properties file.
    */
   public String getId() {
      return fileServerId;
   }

   /**
    * @return next RFD form number.
    */
   public int getNextFormNumber() {
      return nextFormNumber.incrementAndGet();
   }

   /**
    * @return path of file directory root
    */
   public Path getFileDirectoryPath() {
      return fileDirectory;
   }

   /**
    * @return path of form directory root
    */
   public Path getFormDirectoryPath() {
      return formDirectory;
   }

   /**
    * Constructor creates FileServer instance using passed properties.
    * 
    * @param fileServerProperties the contents of a {@code <FileServer>} element
    * from the application xml properties file.
    * @throws Exception if the properties are invalid or would generate a
    * duplicate endpoint.
    */
   public FileServer(HierarchicalConfiguration fileServerProperties)
      throws Exception {

      try {

         // ------------------------------ file server id, not duplicate
         fileServerId = fileServerProperties.getString("[@id]");
         if (StringUtils.isBlank(fileServerId))
            throw new Exception("skipping fileserver - no id");
         if (fileServerIds.contains(fileServerId))
            throw new Exception("skipping duplicate fileserver: "
               + fileServerId);

         // ---------------------------- log for this file server
         log = Logger.getLogger("fs." + fileServerId);

         log.info("loading");

         // ------------------------------------------------- port
         port = fileServerProperties.getInt("[@port]");
         if (port < 1 || port > 65535)
            throw new Exception("port # " + port + " not valid.");
         log.info(" port number: " + port);

         /*
          * base directory for served files, relative to runDirectory. Must
          * exists and be rwx. Default name is "fileDirectory".
          */
         String fileDirectoryName =
            fileServerProperties.getString("[@fileDirectoryName]",
               "fileDirectory");
         if (StringUtils.isBlank(fileDirectoryName))
            throw new Exception("skipping fileserver - no fileDirectoryName");
         fileDirectory = Util.getRunDirectoryPath().resolve(fileDirectoryName);
         Util.isValidPfn(fileServerId + " file directory", fileDirectory,
            PfnType.DIRECTORY, "rwx");
         log.info(" file directory: " + fileDirectory);
         /*
          * base directory for RFD forms, relative to runDirectory. It will be
          * created if it does not exist. Default name is "formDirectory".
          */
         String formDirectoryName =
            fileServerProperties.getString("[@formDirectoryName]",
               "formDirectory");
         if (StringUtils.isBlank(formDirectoryName))
            throw new Exception("skipping fileserver - no formDirectoryName");
         formDirectory = Util.getRunDirectoryPath().resolve(formDirectoryName);
         formDirectory.toFile().mkdir();
         Util.isValidPfn(fileServerId + " form directory", formDirectory,
            PfnType.DIRECTORY, "rwx");
         log.info(" form directory: " + formDirectory);
         // The form directory gets cleared on startup.
         FileUtils.cleanDirectory(formDirectory.toFile());

         // --------------------------------------- WSLog RMI Senders
         if (WSServer.isRMIon()) {
            List <HierarchicalConfiguration> rmisProperties =
               fileServerProperties.configurationsAt("RMI");
            for (HierarchicalConfiguration rmiProperties : rmisProperties) {
               if (rmiProperties.getBoolean("[@on]", true) == false) continue;
               int rmiPort =
                  rmiProperties.getInt("[@port]", DEFAULT_RMI_REGISTRY_PORT);
               try {
                  WSLogRMIClient sndr = new WSLogRMIClient(rmiPort, null);
                  senders.add(sndr);
               } catch (Exception e) {
                  log.info(e.getMessage());
               }
            } // EO add rmi Sender loop
         }

         storeToDB = fileServerProperties.getBoolean("[@storeToDB]", false);

         fileServer = HttpServer.create(new InetSocketAddress(port), 0);
         getHandler = new Handler();
         HttpContext httpContext = fileServer.createContext("/", getHandler);
         httpContext.getFilters().add(new FaviconFilter(log));
         fileServer.setExecutor(Util.getExec());
         fileServer.start();
         desc = fileServerId + " - " + getFileUriPrefix();
         log.info(desc + " started.");

      } catch (Exception e) {
         log.warn("Error creating FileServer: - " + e.getMessage());
         throw e;
      }

   } // EO Constructor

   @Override
   public void doRun() {
      try {
         while (true) {
            for (int i = 0; i < 60; i++ ) {
               Thread.sleep(15000);
               (new Socket(Util.getExternalHostName(), port)).close();
            }
            log.trace(desc + " running.");
         }
      } catch (Exception e) {
         log.warn(desc + " shut down.");
         fileServer.stop(0);
      }
   }

   private class Handler implements HttpHandler {

      @SuppressWarnings("null")
      @Override
      public void handle(HttpExchange httpExchange) throws IOException {

         LogTransaction wsLogTransaction =
            new LogTransaction(TransactionType.HTTP_SERVER);
         wsLogTransaction.setFileServer(FileServer.this);
         WSLog wsLog = wsLogTransaction.getWsLog();
         wsLog.setLog(log);
         wsLog.setServiceName(fileServerId);
         wsLog.setConnOpenTime(new Date());
         wsLog.setTransactionType(TransactionType.HTTP_SERVER);
         wsLogTransaction.loadFromLocal(httpExchange.getLocalAddress()
            .getAddress());
         wsLogTransaction.loadFromRemote(httpExchange.getRemoteAddress()
            .getAddress());
         wsLog.setSecure(false);

         // -------- Pull HTTP Request data
         METHOD method = METHOD.getMethod(httpExchange.getRequestMethod());
         wsLog.setHttpMethod(method.name());
         URI requestUri = httpExchange.getRequestURI();
         wsLog.setHttpRequestURI(requestUri);
         String httpProtocol = httpExchange.getProtocol();
         wsLog.setHttpRequestProtocol(httpProtocol);
         Headers requestHeaders = httpExchange.getRequestHeaders();
         wsLog.setHttpRequestHeaders(requestHeaders);
         Headers responseHeaders = httpExchange.getResponseHeaders();

         // - Response items, probably will be updated before sending
         STATUS responseStatus = STATUS.OK;
         String responseBody = null;
         String contentType = MediaType.TEXT_PLAIN.typeSubtype;

         // ----- Build first part of log message for transaction.
         Level lvl = Level.INFO;
         StringBuilder str =
            new StringBuilder("from ")
               .append(httpExchange.getRemoteAddress().getHostString())
               .append(nl).append("   Request:  ").append(method.name())
               .append(" ").append(requestUri.toString()).append(" ")
               .append(httpProtocol).append(nl).append("   Response: ");

         String requestBody = null;
         try (InputStream is = httpExchange.getRequestBody()) {
            requestBody = IOUtils.toString(is, UTF_8);
         } catch (Exception e) {
            log.warn("Error reading HTTP Body: " + e.getMessage());
            wsLog.addStatus(FStatus.HTTP_BODY_ERR);
         }

         // process block for HTTP Request
         process: {

            // ---------------------------------------------- GET method
            if (method == METHOD.GET) {
               Path filePath;
               try {
                  // form path
                  String path =
                     StringUtils.removeStart(requestUri.getPath(), "/");
                  if (StringUtils.startsWithIgnoreCase(path, "form/")) {
                     path = StringUtils.removeStartIgnoreCase(path, "form/");
                     filePath = formDirectory.resolve(path);
                  } else {
                     // file path
                     filePath = fileDirectory.resolve(path);
                  }
                  Util
                     .isValidPfn("requested file", filePath, PfnType.FILE, "r");
               } catch (Exception e) {
                  responseStatus = STATUS.NOT_FOUND;
                  wsLog.addStatus(FStatus.HTTP_IN_FNF);
                  break process;
               }
               // -------------------------------------- Content type
               try {
                  contentType = Files.probeContentType(filePath);
               } catch (Exception e) {}
               // ----------------------------------- load file
               try {
                  responseBody = FileUtils.readFileToString(filePath.toFile());
               } catch (IOException io) {
                  responseStatus = STATUS.SERVER_ERROR;
                  wsLog.addStatus(FStatus.HTTP_IN_FILE_ERROR);
               }
               break process;
            } // ******************************************* EO Process GET

            // ---------------------------------------------- POST method
            if (httpExchange.getRequestMethod().equalsIgnoreCase("POST")) {
               break process;
            } // EO process POST method

            responseStatus = STATUS.NOT_IMPLEMENTED;
            responseHeaders.add("Allow", "GET, POST");
            wsLog.addStatus(FStatus.HTTP_IN_METHOD);

         } // EO process Request block

         if (responseStatus == STATUS.OK) wsLog.addStatus(FStatus.HTTP_IN_OK);

         if (StringUtils.isBlank(requestBody)) requestBody = "empty";
         wsLog.addMessage("HTTP " + method.name() + "Request",
            MessageType.HTTP_IN_BODY, requestBody);

         String bod =
            (StringUtils.isNotBlank(responseBody)) ? responseBody : "empty";
         wsLog.addMessage("HTTP " + method.name() + "Response",
            MessageType.HTTP_OUT_BODY, bod);

         wsLog.addStatus(FStatus.HTTP_OUT_IN_PROGRESS);

         try {
            if (StringUtils.isNotEmpty(responseBody)) {
               byte[] body = responseBody.getBytes(CHAR_SET_UTF_8);
               responseHeaders.add("Content-Type", contentType);
               wsLog.setHttpResponseHeaders(responseHeaders);
               httpExchange.sendResponseHeaders(responseStatus.statusCode,
                  body.length);
               OutputStream ob = httpExchange.getResponseBody();
               IOUtils.write(body, ob);
            } else {
               httpExchange.sendResponseHeaders(responseStatus.statusCode, -1);
            }
            httpExchange.close();
            wsLog.addStatus(FStatus.HTTP_OUT_OK);
            wsLog.setHttpResponseCode(responseStatus.statusCode);
            str.append(responseStatus.statusCode).append(" ")
               .append(responseStatus.reasonPhrase);
            log.log(lvl, str);
         } catch (Exception e) {
            log.warn("Error sending HTTP Response: " + e.getMessage());
            wsLog.addStatus(FStatus.HTTP_OUT_ERR);
         }

         if (WSLogging()) {
            try {
               wsLogTransaction.close();
            } catch (Exception e) {
               log.warn("Error closing WSLogTransaction");
            }
         }
      } // EO handle method
   } // EO Get Handler class

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
    * @param wsLog logging instance to be persisted.
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

   @SuppressWarnings("javadoc")
   public enum FStatus implements Status {

      // @formatter:off
      HTTP_IN_IN_PROGRESS  (Phase.HTTP_IN, Result.NA, Level.TRACE, "HTTP Request processing in progress"),
      HTTP_IN_FNF          (Phase.HTTP_IN, Result.FAIL, Level.DEBUG, "HTTP GET - File not Found"),
      HTTP_IN_FILE_ERROR   (Phase.HTTP_IN, Result.FAIL, Level.DEBUG, "HTTP GET - Error reading File"),
      HTTP_IN_METHOD       (Phase.HTTP_IN, Result.FAIL, Level.DEBUG, "HTTP GET - unsupported method"),
      HTTP_IN_OK           (Phase.HTTP_IN, Result.PASS, Level.DEBUG, "HTTP Request processed OK."),
      HTTP_OUT_IN_PROGRESS (Phase.HTTP_OUT, Result.NA, Level.TRACE, "HTTP Response processing in progress"),
      HTTP_OUT_ERR         (Phase.HTTP_OUT, Result.FAIL, Level.DEBUG, "Error sending HTTP Response"),
      HTTP_OUT_OK          (Phase.HTTP_OUT, Result.PASS, Level.DEBUG, "HTTP Request processed OK."),
      HTTP_BODY_ERR        (Phase.HTTP_EX, Result.NA, Level.DEBUG, "Error reading HTTP Request body");
      // @formatter:on

      private Phase phase;
      private Result result;
      private Level logLevel;
      private String statusMsg;

      private static final String setCode = "FileServer";

      private FStatus(Phase ph, Result pf, Level ll, String msg) {
         phase = ph;
         result = pf;
         logLevel = ll;
         statusMsg = msg;
      }

      @Override
      public StatusUpdateType getUpdateType() {
         return phase.updateType;
      }

      @Override
      public String getMsg() {
         return phase.phaseMsg + ": " + statusMsg;
      }

      @Override
      public String getStatusMsg() {
         return statusMsg;
      }

      @Override
      public String encode() {
         return setCode + "," + name() + "|";
      }

      @Override
      public Level getLogLevel() {
         return logLevel;
      }

      @Override
      public String getPhaseMsg() {
         return phase.phaseMsg;
      }

      @Override
      public String getSetCode() {
         return setCode;
      }

      /**
       * @param name of an instance of {@link FStatus}
       * @return the TSTStatus instance corresponding to the passed name, cast
       * to an {@link edu.wustl.mir.erl.ihe.util.Status Status} instance.
       */
      public static Status getStatus(String name) {
         return Enum.valueOf(FStatus.class, name);
      }

      // ----------------------- Phases for this status code
      static {
         StatusHelper.addCodeSet(setCode, FStatus.class);
      }

      @Override
      public Result getResult() {
         return result;
      }

      @Override
      public StatusType getStatusType() {
         return phase.statusType;
      }

   } // EO FStatus enum

   protected enum Phase {

      // @formatter:off
      HTTP_IN("HTTP request processing", StatusType.HTTP_REQUEST, StatusUpdateType.UPDATE),
      HTTP_OUT("HTTP response processing", StatusType.HTTP_RESPONSE, StatusUpdateType.UPDATE),
      HTTP_EX("HTTP exception", StatusType.NA, StatusUpdateType.ADD);
      // @formatter:on

      public String phaseMsg;
      public StatusType statusType;
      public StatusUpdateType updateType;

      private Phase(String nm, StatusType st, StatusUpdateType ut) {
         phaseMsg = nm;
         statusType = st;
         updateType = ut;
      }
   }
   
   /**
    * Publishes a new form to the file server form directory.
    * @param contents String contents of the form.
    * @param name String file name of the new form. If the name contains the
    * string '${seq}', it will be replaced with {@link #getNextFormNumber()}.
    * @return String URL used to retrieve the new from from the file server.
    * @throws Exception if params are not valid, if the passed file name does
    * not contain '${seq}' and results in a duplicate file, or on IO error.
    */
   public String publishForm(String contents, String name) throws Exception {
      if (StringUtils.isBlank(name))
         throw new Exception("publishForm called with invalid form name");
         if (StringUtils.isBlank(contents))
            throw new Exception("publishForm [" + name + "] called with invalid contents");
         if (StringUtils.contains(name, "${seq}"))
            name = new Plug(name).set("seq", getNextFormNumber()).get();
         Path formPath = formDirectory.resolve(name);
         FileUtils.writeStringToFile(formPath.toFile(), contents, CHAR_SET_UTF_8, false);
         return getFormUriPrefix() + name;
   }

} // EO FileServer class
