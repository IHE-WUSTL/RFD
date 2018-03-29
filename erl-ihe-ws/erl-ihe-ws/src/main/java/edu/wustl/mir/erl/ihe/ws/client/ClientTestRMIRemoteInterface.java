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

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import org.javatuples.LabelValue;

import edu.wustl.mir.erl.ihe.ws.WSProperties;

/**
 * RMI Remote interface used to pass parameters to a test instance, which will
 * in turn send a SOAP WS message to a WS server, for example, an RFD FormFiller
 */
public interface ClientTestRMIRemoteInterface extends Remote, WSProperties {

   /**
    * Runs a test sending a client message to a server.
    * 
    * @param pars label/value pairs containing information needed to run the
    * test. One of them must be label={@link WSProperties#LABEL_TEST_STEP
    * LABEL_TEST_STEP} with a string value containing the id of the test to be
    * run. The other values depend on the particular test.
    * @return a List of label/value pairs containing response information, for
    * the particular test. May be empty, but will never be null.
    * @throws RemoteException on error
    */
   List <LabelValue <String, Object>> runTest(
      List <LabelValue <String, Object>> pars) throws RemoteException;

}
