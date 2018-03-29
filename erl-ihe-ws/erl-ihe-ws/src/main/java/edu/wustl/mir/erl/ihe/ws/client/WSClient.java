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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.javatuples.LabelValue;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.server.WSServer;

/**
 * Managing interface between a GUI web application and test class(es) which
 * simulate wsdl web clients for individual tests. Run as a singleton under the
 * control of {@link WSServer}. If {@code <WSClient>} elements are present under
 * the {@code <WSTests>} element of the properties file, WSServer will start
 * WSClient using its static {@link #initialize(int) initialize} method.
 * WSClient will open an RMI server which can be accessed by the web application
 * to invoke client tests via the
 * {@link ClientTestRMIRemoteInterface#runTest(java.util.List) runTest} method.
 * 
 * @see <a href="http://en.wikipedia.org/wiki/Java_remote_method_invocation">
 * Wikipedia: Java remote method invocation</a>
 * @see <a
 * href="http://www.oracle.com/technetwork/articles/javaee/index-jsp-136424.html">
 * Oracle Java RMI</a>
 */
public class WSClient implements ClientTestRMIRemoteInterface, WSProperties,
   Serializable, Closeable {
   private static final long serialVersionUID = 1L;

   private static WSClient wsClient = null;
   private static Registry registry = null;
   private static Logger log = null;

   @SuppressWarnings("unused")
   private WSClient() throws RemoteException {
      super();
   }

   /**
    * Create and start RMI for WSClient Test RMI calls.
    * 
    * @param port RMI registry port.
    * @return WSClient instance
    * @throws RemoteException on communication error.
    * @throws Exception if calling parameters are invalid.
    */
   public static WSClient initialize(int port) throws RemoteException,
      Exception {
      Util.invoked();
      if (wsClient != null)
         throw new Exception("attempt to initialize WSClient multiple times.");
      wsClient = new WSClient();

      log = Logger.getLogger("rmi." + WSCLIENT_RMI_SHORT_NAME);

      ClientTestRMIRemoteInterface stub =
         (ClientTestRMIRemoteInterface) UnicastRemoteObject.exportObject(
            wsClient, 0);

      registry = LocateRegistry.getRegistry(port);
      registry.bind(WSCLIENT_RMI_LONG_NAME, stub);
      log.info(WSCLIENT_RMI_SHORT_NAME + " bound");

      return wsClient;
   } // EO initialize method

   /*
    * (non-Javadoc)
    * 
    * @see java.io.Closeable#close()
    */
   @Override
   public void close() throws IOException {
      try {
         Util.invoked(log);
         registry.unbind(WSCLIENT_RMI_LONG_NAME);
         UnicastRemoteObject.unexportObject(this, true);
         log.info(WSCLIENT_RMI_SHORT_NAME + " shut down");
      } catch (Exception e) {
         log.warn(e.getClass().getName() + " in " + Util.classMethod() + " "
            + Util.getEM(e));
      }
   } // EO close method

   /*
    * (non-Javadoc)
    * 
    * @see edu.wustl.mir.erl.ihe.ws.client.TestRMI#runTest(java.util.List)
    */
   @Override
   @SuppressWarnings("cast")
   public List <LabelValue <String, Object>> runTest(
      List <LabelValue <String, Object>> pars) {
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      try {
         String testStep = (String) Util.getValueForLabel(LABEL_TEST_STEP, pars);
         ClientTest test = ClientTest.getTest(testStep);
         return test.runTest(pars);
         // **************************** TODO current working location
      } catch (Exception e) {
         returns.add(new LabelValue <String, Object>(LABEL_ERROR, (Object) e
            .getMessage()));
      }
      return returns;
   }

} // EO WSClient class
