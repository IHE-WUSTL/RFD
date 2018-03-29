/*******************************************************************************
 * Copyright (c) 2014  Washington University in St. Louis
 *  All rights reserved. This program and the accompanying 
 *  materials are made available under the terms of the
 *  Apache License, Version 2.0 (the "License");  you may not 
 *  use this file except in compliance with the License.
 * The License is available at:
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, 
 *  software  distributed under the License is distributed on 
 *  an "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS  
 *  OF ANY KIND, either express or implied. See the License 
 *  for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Contributors:
 *    Initial author: Ralph Moulton / MIR WUSM IHE Development Project 
 *    moultonr@mir.wustl.edu
 *******************************************************************************/
package edu.wustl.mir.erl.ihe.ws.db.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * RMI Remote interface used to construct RMI interchange of {@link WSLog} 
 * instances created during the processing of a WS transaction to another 
 * process, for example, a web application. 
 */
public interface WSLogRemoteInterface extends Remote {

   /**
    * @param wsLog {@link WSLog} instance which will be submitted to the server
    * process from the client {@link edu.wustl.mir.erl.ihe.ws.server.WSServer
    * WSServer} process.
    * @throws RemoteException on communications error.
    * @throws NullPointerException if there is no place to put the received
    * WSLog instance.
    */
   void submitWSLog(WSLog wsLog) throws RemoteException, NullPointerException;
}
