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
package edu.wustl.mir.erl.ihe.ws.db;

import edu.wustl.mir.erl.ihe.util.UtilProperties;

/**
 * Type of transaction being processed
 */
public enum TransactionType implements UtilProperties {
   
   /**
    * HTTP transaction; we are server
    */
   HTTP_SERVER(ASCENDING),
   /**
    * HTTP transaction; we are client
    */
   HTTP_CLIENT(DESCENDING),
   /**
    * SOAP Web service transaction; we are server
    */
   SOAP_SERVER(ASCENDING),
   /**
    * SOAP Web service transaction; we are client
    */
   SOAP_CLIENT(DESCENDING),
   /**
    * CDA document inspection; we are client
    */
   CDA_CLIENT(DESCENDING);
   
   private boolean sortOrder;
   
   TransactionType(boolean sort) {
      sortOrder = sort;
   }
   
   /**
    * @return status sort order for this transaction type.
    */
   public boolean getSortOrder() {
      return sortOrder;
   }
}
