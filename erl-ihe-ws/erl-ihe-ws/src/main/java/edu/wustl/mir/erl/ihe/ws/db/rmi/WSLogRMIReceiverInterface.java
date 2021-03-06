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
package edu.wustl.mir.erl.ihe.ws.db.rmi;

import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Listener interface for classes receiving {@link WSLog} instances using
 * {@link WSLogRMIServer}.
 */
public interface WSLogRMIReceiverInterface {

   /**
    * Implementing class uses this method to store the received WSLog instance
    * and carry out any other needed processing
    * 
    * @param wsLog {@link WSLog} instance which has been received via RMI.
    */
   public void receiveWSLogRMI(WSLog wsLog);
}
