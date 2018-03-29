/*
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
 */
package edu.wustl.mir.erl.ihe.util;

/**
 * Enum of Status Types. Use to organize Status enums, in particular for sorting
 * status code lists.
 */
public enum StatusType {
   
   /**
    * status related to TCP/TLS connection
    */
   CONNECTION("connection"),
   /**
    * status related to http request
    */
   HTTP_REQUEST("http request"),
   /**
    * status related to SOAP request
    */
   SOAP_REQUEST("soap request"),
   /**
    * status related to specific test
    */
   TEST("test"),
   /**
    * status related to SOAP response
    */
   SOAP_RESPONSE("soap response"),
   /**
    * status related to http response
    */
   HTTP_RESPONSE("http response"),
   /**
    * status not known.
    */
   UNKNOWN("unknown"),
   /**
    * status type not applicable to this status
    */
   NA("not applicable");
   
   private String string;
   
   private StatusType(String str) {
      string = str;
   }
   
   @Override
   public String toString() { return string; }

}
