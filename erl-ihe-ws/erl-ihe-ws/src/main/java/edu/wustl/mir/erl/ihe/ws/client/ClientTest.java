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
package edu.wustl.mir.erl.ihe.ws.client;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.WebMethod;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.javatuples.LabelValue;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.db.MessageType;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.db.rmi.WSLogRMIClient;
import edu.wustl.mir.erl.ihe.ws.handlers.StoreSOAPMessages;
import edu.wustl.mir.erl.ihe.ws.server.WSServer;

/**
 * Super class which must be extended by all Client tests
 */
public abstract class ClientTest implements WSProperties {

   private static Set <String> ids = new HashSet <>();
   private static Map <String, ClientTest> tests = new HashMap <>();

   /**
    * Short ID for the test. Must be unique. Used to determine logger.
    */
   private String id;
   /**
    * Test ID and step number for this step
    */
   private String testStep;
   /**
    * User readable name for test step
    */
   private String name;
   /**
    * Canonical class name of test step, subset of ClientTest
    */
   private String className;
   /**
    * class object for this test step
    */
   private Class <?> testClass;
   /**
    * wsa Action for this test step
    */
   private String action = "";
   /**
    * boolean true if WS connection is secure (TLS)
    */
   private Boolean secure;
   /**
    * boolean true if test results should be stored to RDBMS.
    */
   private Boolean storeToDB;
   /**
    * logger for this test step
    */
   private Logger log;
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
   
   private List <WSLogRMIClient> senders = new ArrayList <>();

   /**
    * Method runs a test step in which it acts as a client sending a wsdl WS
    * SOAP based to a WS Server. The message is monitored and a {@link WSLog}
    * instance is generated.
    * 
    * @param pars a {@link java.util.List List} of {@link LabelValue} pairs
    * representing named parameters for the particular test.
    * @return List of LabelValue pairs representing named return values for the
    * particular test.
    */
   public abstract List <LabelValue <String, Object>> runTest(
      List <LabelValue <String, Object>> pars);

   /**
    * Validates properties for client test step, creates and initializes test
    * step instance. Each client test step is configured using an WSClient
    * element inside the WSTests element of the properties file. For example:
    * 
    * <pre>
    * {@code
    *  <WSClient id="CFF-1" testStep="RFD 0-10000-FM/1" name="RFD 0-10000-FM Form Filler" 
    *     class="edu.wustl.mir.erl.ihe.rfd.tests.RFD_0_10000_FM.FormFiller"
    *     secure="false" storeToDB="false">
    *     <RMI on="true" host="localhost" port="1099" name="WSLogRMI" />
    * </WSClient>}
    * </pre>
    * 
    * @param clientProperties HierarchicalConfiguration containing contents of
    * one WSClient element.
    * @return instance of configured class, cast to ClientTest
    * @throws Exception on error, including:
    * <ul>
    * <li>missing, empty, or invalid configuration parameters.</li>
    * <li>Duplicate id or teststep</li>
    * <li>test class can't be loaded or is not a subclass of ClientTest.</li>
    * </ul>
    */
   public static final ClientTest initialize(
      HierarchicalConfiguration clientProperties) throws Exception {
      // Validate properties filled in
      String id = clientProperties.getString("[@id]", "");
      String testStep = clientProperties.getString("[@testStep]", "");
      String name = clientProperties.getString("[@name]", "");
      String className = clientProperties.getString("[@class]", "");
      boolean secure = clientProperties.getBoolean("[@secure]", false);
      boolean storeToDB = clientProperties.getBoolean("[@storeToDB]", false);
      StringBuffer em =
         new StringBuffer("skipping WSClient " + testStep + " " + name + nl);
      int eml = em.length();
      if (StringUtils.isBlank(id)) em.append("   blank id" + nl);
      if (ids.contains(id)) em.append("   duplicate id" + nl);
      if (StringUtils.isBlank(testStep)) em.append("  Blank test step" + nl);
      if (tests.containsKey(testStep))
         em.append("   duplicate test step" + nl);
      if (StringUtils.isBlank(name)) em.append("  Blank name" + nl);
      if (StringUtils.isBlank(className))
         em.append("   Blank class name" + nl);
      if (em.length() > eml) throw new Exception(em.toString());
      // Does test class exist?
      Class <?> testClass = null;
      try {
         testClass =
            Class.forName(className, false, ClassLoader.getSystemClassLoader());
      } catch (ClassNotFoundException cnfe) {
         em.append("   client class not found: ").append(className);
         throw new Exception(em.toString());
      }
      // Is test class a subclass of this class
      if (ClientTest.class.isAssignableFrom(testClass) == false) {
         em.append("   " + className + " not a subclass of ClientTest");
         throw new Exception(em.toString());
      }
      // create instance of test class, cast to ClientTest
      ClientTest test = (ClientTest) testClass.newInstance();
      test.setId(id);
      test.setTestStep(testStep);
      test.setName(name);
      test.setClassName(className);
      test.setTestClass(testClass);
      test.setSecure(secure);
      test.setStoreToDB(storeToDB);
      test.setLog(Logger.getLogger("wsc." + id));
      ids.add(id);
      tests.put(testStep, test);

      // ------------------------------- Store SOAP Messages
      if (StoreSOAPMessages.isON()) {
         test.storeSOAPMessages = true;
         test.subDirectoryName = id;
         try {
            HierarchicalConfiguration storeSOAPMessagesProperties =
               clientProperties.configurationAt("StoreSOAPMessages");
            test.storeSOAPMessages =
               storeSOAPMessagesProperties.getBoolean("[@on]", true);
            test.subDirectoryName =
               storeSOAPMessagesProperties.getString("[@subMessageDirectoryName]",
                  test.subDirectoryName);
         } catch (Exception ee) {}
      }
      
      test.setupRMI(clientProperties);
      
      return test;
   } // EO initialize method
   
   private void setupRMI(HierarchicalConfiguration clientProperties) {
   // --------------------------------------- WSLog RMI Senders
      if (WSServer.isRMIon()) {
      List <HierarchicalConfiguration> rmisProperties =
         clientProperties.configurationsAt("RMI");
      for (HierarchicalConfiguration rmiProperties : rmisProperties) {
         if (rmiProperties.getBoolean("[@on]", true) == false) continue;
         int rmiPort = rmiProperties.getInt("[@port]", DEFAULT_RMI_REGISTRY_PORT);
         try {
            senders.add(new WSLogRMIClient(rmiPort, null));
         } catch (Exception e) {
            log.info(e.getMessage());
         }
      } // EO add rmi Sender loop
      }
   }

   /**
    * Returns singleton instance of class for passed test step, cast to
    * ClientTest.
    * 
    * @param testStep test/step for test. For example, "RFD 0-10000-FM/1".
    * @return test instance.
    * @throws Exception if no test instance is found for the passed test step.
    */
   public static ClientTest getTest(String testStep) throws Exception {
      ClientTest test = tests.get(testStep);
      if (test == null)
         throw new Exception("test for " + testStep + " not found.");
      return test;
   }

   /**
    * @return the {@link #id} value.
    */
   public String getId() {
      return id;
   }

   /**
    * @param id the {@link #id} to set
    */
   public void setId(String id) {
      this.id = id;
   }

   /**
    * @return the {@link #testStep} value.
    */
   public String getTestStep() {
      return testStep;
   }

   /**
    * @param testStep the {@link #testStep} to set
    */
   public void setTestStep(String testStep) {
      this.testStep = testStep;
   }

   /**
    * @return the {@link #name} value.
    */
   public String getName() {
      return name;
   }

   /**
    * @param name the {@link #name} to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the {@link #className} value.
    */
   public String getClassName() {
      return className;
   }

   /**
    * @param className the {@link #className} to set
    */
   public void setClassName(String className) {
      this.className = className;
   }

   /**
    * @return the {@link #testClass} value.
    */
   public Class <?> getTestClass() {
      return testClass;
   }

   /**
    * @param clss the {@link #testClass} to set
    */
   public void setTestClass(Class <?> clss) {
      this.testClass = clss;
   }

   /**
    * @return the {@link #secure} value.
    */
   public Boolean getSecure() {
      return secure;
   }

   /**
    * @param secure the {@link #secure} to set
    */
   public void setSecure(Boolean secure) {
      this.secure = secure;
   }

   /**
    * @return the {@link #storeToDB} value.
    */
   public Boolean getStoreToDB() {
      return storeToDB;
   }

   /**
    * @param storeToDB the {@link #storeToDB} to set
    */
   public void setStoreToDB(Boolean storeToDB) {
      this.storeToDB = storeToDB;
   }

   /**
    * @return the {@link #log} value.
    */
   public Logger getLog() {
      return log;
   }

   /**
    * @param log the {@link #log} to set
    */
   public void setLog(Logger log) {
      this.log = log;
   }

   /**
    * @return the {@link #action} value.
    */
   public String getAction() {
      return action;
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
    * Determine the wsa:action value for this test.
    * @param cls WS Port class
    * @param methodName String name of method in port class being invoked.
    */
   protected void
      setAction(Class <?> cls, String methodName) {
      try {
         Method[] methods = cls.getMethods();
         for (Method method : methods)
            if (method.getName().equals(methodName)) {
               WebMethod webMethod = method.getAnnotation(WebMethod.class);
               if (webMethod == null)
                  throw new Exception("No @WebMethod annotation");
               action = webMethod.action();
               return;
            }
         throw new Exception("method not found");
      } catch (Exception e) {
         log.warn("setAction error for " + cls.getName() + "#" + methodName
            + " " + Util.getEM(e));
      }
   } // EO setAction method


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
    * @param wsLog WSLog instance to persist.
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
   
   

} // EO ClientTest class
